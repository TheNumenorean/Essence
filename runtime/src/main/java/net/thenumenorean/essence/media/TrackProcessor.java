/**
 * 
 */
package net.thenumenorean.essence.media;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.bson.Document;

import com.github.axet.vget.VGet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import net.thenumenorean.essence.EssenceRuntime;
import net.thenumenorean.essence.utils.RepeatingRunnable;

/**
 * @author Francesco Macagno
 *
 */
public class TrackProcessor extends RepeatingRunnable {

	private static String DEFAULT_DOWNLOADS_DIR = "tracks/tmp/";

	private static final int DEFAULT_WAIT = 10000;
	private final MongoCollection<Document> trackSource;
	private AudioEncoder audioEncoder;

	public TrackProcessor(MongoCollection<Document> trackSource, AudioEncoder audioEncoder) {
		this(trackSource, audioEncoder, DEFAULT_WAIT);
	}

	public TrackProcessor(MongoCollection<Document> trackSource, AudioEncoder audioEncoder, int wait) {
		super(wait);
		this.trackSource = trackSource;
		this.audioEncoder = audioEncoder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.thenumenorean.essence.utils.RepeatingRunnable#loop()
	 */
	@Override
	public void loop() {

		// Get unprocessed tracks
		for (Document doc : trackSource.find(Filters.eq("processed", false))) {
			String currLoc = doc.getString("location");

			EssenceRuntime.log.info("Processing video " + doc.getString("name"));

			File result = null;
			if (currLoc == null || currLoc.isEmpty()) { // Is a web video

				// Attempt to download the video
				result = getInternetTrack(doc.getString("webaddress"));
				if (result == null)
					EssenceRuntime.log.severe("Couldnt get file from web!");

			} else { // Uploaded file

				File upload = new File(currLoc);
				try {

					result = new File(EssenceRuntime.TRACK_DIR,
							upload.getName().substring(0, upload.getName().indexOf('.')) + ".mp3");
					audioEncoder.convert(upload, result);

				} catch (Exception e) {
					EssenceRuntime.log
							.warning("Error occured converting track " + upload.getPath() + ": " + e.getMessage());
					result = null;
				} finally {
					upload.delete(); // Delete the uploaded file regardless
				}

			}

			if (result != null) {
				trackSource.replaceOne(Filters.eq("_id", doc.getObjectId("_id")),
						doc.append("location", result.getPath()).append("processed", true));
			} else {
				trackSource.deleteOne(Filters.eq("_id", doc.getObjectId("_id")));
			}

		}

	}

	/**
	 * Downloads the video from the given web link and then chooses the best
	 * candidate file for extracting the soundtrack. It then converts the format
	 * to mp3 and copies it to the tracks folder.
	 * 
	 * @param url
	 *            The url in string format to download the video from
	 * @return A file, or null if an error occured
	 */
	private File getInternetTrack(String url) {

		// Create a temporary folder to download files into
		File tmp = new File(DEFAULT_DOWNLOADS_DIR + (new Random().nextInt()));
		tmp.mkdirs();

		// Download all available files
		File[] files;
		try {
			files = downloadVideo(url, tmp);
		} catch (Exception e) {
			EssenceRuntime.log.severe("Error downloading video at " + url + ": " + e.getMessage());
			e.printStackTrace();
			return null;
		}

		// If no files, report failiure
		if (files == null || files.length < 1) {
			EssenceRuntime.log.severe("Video download didnt result in any usable files");
			return null;
		}

		// Sort the files by which ones are most preferable
		Arrays.sort(files, 0, files.length, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {

				// Sort by type so we take the most preferable
				if (o1.getName().endsWith(".webm") && !o2.getName().endsWith(".webm")) {
					return -1;
				}

				return o1.getName().compareTo(o2.getName());
			}

		});

		File newTrack = null;

		// Go through each file and attempt to generate a valid output file
		for (File f : files) {
			newTrack = new File(EssenceRuntime.TRACK_DIR, f.getName().substring(0, f.getName().indexOf('.')) + ".mp3");

			try {
				audioEncoder.convert(f, newTrack);

				break; // If the conversion succedded, this is a good file

			} catch (Exception e) {
				// If an error occurs, continue so we can try to see if other
				// files may work
				EssenceRuntime.log.warning("Error occured converting track " + f.getPath() + ": " + e.getMessage());
				newTrack = null; // Set to null so if no files work we can tell
			}
		}

		// Check if there was asuccess
		if (newTrack == null)
			EssenceRuntime.log.warning("All files failed to convert!");

		// Delete the tmp directory, regardless of success
		deleteFolder(tmp);

		return newTrack;

	}

	/**
	 * Download all available files from the given address.
	 * 
	 * @param url
	 *            The address to download from
	 * @param downloadDir
	 *            The drectory to download to
	 * @return An array of Files. Each object points to an actual file, and not
	 *         a folder. If no files were found, the array will be empty. If an
	 *         error occured, the return may be null.
	 * @throws MalformedURLException
	 */
	private File[] downloadVideo(String url, File downloadDir) throws MalformedURLException {

		// Get the actual files
		VGet get = new VGet(new URL(url), downloadDir);
		get.download();

		// filter the results to only include valid candidate files
		File[] res = downloadDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return false;
				return true;
			}

		});

		return res;

	}

	/**
	 * Recursively deletes a folder.
	 * 
	 * @param f
	 *            Folder to remove all files from and then remove
	 */
	private void deleteFolder(File f) {
		if (f == null)
			return;

		try {
			// Walk the file tree begginning with the given folder/file
			Files.walkFileTree(f.getParentFile().toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

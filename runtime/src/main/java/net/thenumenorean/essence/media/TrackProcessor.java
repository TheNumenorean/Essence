/**
 * 
 */
package net.thenumenorean.essence.media;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
		for (Document doc : trackSource.find(Filters.eq("processed", false))) {
			String currLoc = doc.getString("location");
			File out = null;
			boolean isWeb = false;
			if (currLoc == null || currLoc.isEmpty()) {
				isWeb = true;
				out = downloadVideo(doc.getString("webaddress"));
				if(out == null) {
					EssenceRuntime.log.severe("Couldnt get file from web!");
					continue;
				}
			} else {
				out = new File(currLoc);
			}
			
			
				

			File to = new File(EssenceRuntime.TRACK_DIR,
					out.getName().substring(0, out.getName().indexOf('.')) + ".mp3");
			audioEncoder.convert(out, to);
			out.delete();
			if (isWeb) {
				deleteFolder(out.getParentFile());
			}
			
			trackSource.replaceOne(Filters.eq("_id", doc.getObjectId("_id")), doc.append("location", to.getPath()).append("processed", true));

		}

	}

	/**
	 * Downloads the video from the given web link and then chooses the best candidate file for extracting the soundtrack and returns it.
	 * @param url The url in string format to download the video from
	 * @return A file, or null if an error occured
	 */
	private File downloadVideo(String url) {

		EssenceRuntime.log.info("Downloading video at " + url);

		try {
			// Make sure the destination exists
			File tmp = new File(DEFAULT_DOWNLOADS_DIR + (new Random().nextInt()));
			tmp.mkdirs();

			// Get the actual files
			VGet get = new VGet(new URL(url), tmp);
			get.download();

			// filter the results to only include valid candidate files
			File[] res = tmp.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return false;
					if (pathname.getName().endsWith(".webm"))
						return true;
					return false;
				}

			});

			if (res.length < 1) {
				EssenceRuntime.log.severe("Video download didnt result in any usable files");
				return null;
			}

			Arrays.sort(res, 0, res.length, new Comparator<File>() {

				@Override
				public int compare(File o1, File o2) {
					// TODO: Sort by type so we take the most preferable
					return o1.getName().compareTo(o2.getName());
				}

			});

			EssenceRuntime.log.info("Download finished, result file: " + res[0].getName());

			return res[0];

		} catch (Exception e) {
			EssenceRuntime.log.severe("Error downloading video at " + url + ": " + e.getMessage());
			e.printStackTrace();
			return null;
		}

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

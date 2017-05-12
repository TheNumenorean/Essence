/**
 * 
 */
package net.thenumenorean.essence;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import net.thenumenorean.essence.media.AudioEncoder;
import net.thenumenorean.essence.media.TrackProcessor;
import net.thenumenorean.essence.pl.InsertOrderPlaylist;
import net.thenumenorean.essence.pl.PlaylistGenerator;
import net.thenumenorean.essence.utils.RepeatingRunnable;

/**
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {

	static final String FFMPEG_PATH = "/usr/bin/ffmpeg";
	static final String FFPROBE_PATH = "/usr/bin/ffprobe";

	static final File LOG_DIR = new File("log/");

	static final String STREAM_CONF = "ezstream.xml";

	public static final File TRACK_DIR = new File("tracks/");
	public static final File UPLOADS_DIR = new File(TRACK_DIR, "uploads/");
	static final File NEXT_FILE = new File(TRACK_DIR, "next.txt");
	
	static final int MAX_PLAYLIST_SIZE = 3;

	public static Logger log;

	private ProcessBuilder pb;
	private Process ezstream;

	public AudioEncoder audioEncoder;

	private OnDeckTrackManager onDeckTrackManager;
	private TrackProcessor trackProcessor;
	private PlaylistGenRunner playlistGenRunner;
	
	private boolean stop;
	private Thread shutdownHook;
	private MongoDriver mongoDriver;

	/**
	 * @throws IOException
	 * 
	 */
	public EssenceRuntime() throws IOException {

		stop = false;
		LOG_DIR.mkdirs();

		log = Logger.getLogger("EssenceRuntime");
		log.addHandler(new FileHandler(LOG_DIR.getAbsolutePath() + "/EssenceRuntime.log"));

		shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {
				stop();
			}
		});

		TRACK_DIR.mkdirs();
		UPLOADS_DIR.mkdirs();

		NEXT_FILE.createNewFile();

		// Create a log file for the ezstream process to output to
		File log = new File(LOG_DIR,
				"ezstream-" + (new SimpleDateFormat("YYYY_MM_dd_HH-mm").format(Date.from(Instant.now()))) + ".log");
		if (!log.exists()) {
			log.createNewFile();
		}

		// Create the EZStream process to get it ready to run
		pb = new ProcessBuilder("ezstream", "-c", STREAM_CONF);
		pb.redirectError(log);
		pb.redirectOutput(log);
		pb.directory(null);

		audioEncoder = new AudioEncoder(FFMPEG_PATH, FFPROBE_PATH);

		mongoDriver = new MongoDriver();

		onDeckTrackManager = new OnDeckTrackManager(mongoDriver, NEXT_FILE);
		trackProcessor = new TrackProcessor(mongoDriver.getTrackColection(), audioEncoder);
		playlistGenRunner = new PlaylistGenRunner();

	}

	@Override
	public void run() {
		log.info("Starting EssenceRuntime");

		Runtime.getRuntime().addShutdownHook(shutdownHook);

		try {
			ezstream = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		new Thread(onDeckTrackManager).start();
		new Thread(trackProcessor).start();
		new Thread(playlistGenRunner).start();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (!stop) {

			try {
				while (!br.ready() && !stop)
					Thread.sleep(500);

				if (br.ready())
					handleCommand(br.readLine());
			} catch (InterruptedException e) {
			} catch (IOException e) {
				log.severe(e.getMessage());
			}
		}

	}

	private void handleCommand(String line) {

		log.info("Received command: " + line);

		if (line.equals("stop"))
			stop();

	}

	public void stop() {

		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (IllegalStateException e) {
		}

		stop = true;

		log.info("Killing ezstream...");
		ezstream.destroy();

		log.info("Killing onDeckTrackManager...");
		onDeckTrackManager.stopAndWait();

		log.info("Killing TrackProcessor...");
		trackProcessor.stopAndWait();

		log.info("Killing PlaylistGenRunner...");
		playlistGenRunner.stopAndWait();
	}
	
	private class PlaylistGenRunner extends RepeatingRunnable {
		
		private static final int DEFAULT_WAIT = 10000;
		
		private PlaylistGenerator pg;

		public PlaylistGenRunner() {
			this(DEFAULT_WAIT);
		}
		
		public PlaylistGenRunner(int wait) {
			super(wait);
			pg = new InsertOrderPlaylist(mongoDriver);
		}

		@Override
		public void loop() {

			log.info("Running playlist generation");

			// Go through all the requests and only use ones that point to a track that has been processed
			// Also sort the documents by timestamp low to high so that the are in order of oldest to newest
			List<Document> requests = new ArrayList<>();
			for(Document req : mongoDriver.getRequestColection().find().sort(Sorts.ascending("timestamp")))
				if(mongoDriver.getTrack(req.getObjectId("track_id")).getBoolean("processed"))
					requests.add(req);
			
			
			List<Document> docs = pg.generatePlaylist(mongoDriver.getPlaylistColection().find().into(new ArrayList<>()), requests);
			
			// Sort the results by rank in order to ensure they are in the right order
			docs.sort(new Comparator<Document>() {

				@Override
				public int compare(Document arg0, Document arg1) {
					return arg1.getInteger("rank") - arg0.getInteger("rank");
				}
				
			});
			
			// Prevent changes to playlist in the middle of operations
			synchronized(mongoDriver.getPlaylistColection()) {
				
				long remainingTracks = mongoDriver.getPlaylistColection().count();
				
				// Add only as many as gets us to the total allowed playlist size, since these are now set in stone.
				for(int i = 0; i < docs.size() && i < MAX_PLAYLIST_SIZE - remainingTracks; i++) {
					Document newTrack = docs.get(i);
					newTrack.put("rank", remainingTracks - 1 + i); // Set the relevant rank relative to the prexisting playlist

					mongoDriver.getPlaylistColection().insertOne(newTrack);
					mongoDriver.getRequestColection().deleteOne(Filters.eq("_id", newTrack.getObjectId("req_id")));
				}
				
			}
			
		}
		
	}



}

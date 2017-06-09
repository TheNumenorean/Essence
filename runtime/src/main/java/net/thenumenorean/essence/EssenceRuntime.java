/**
 * 
 */
package net.thenumenorean.essence;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import net.thenumenorean.essence.media.AudioEncoder;
import net.thenumenorean.essence.media.TrackProcessor;
import net.thenumenorean.essence.pl.PlaylistGeneratorService;

/**
 * This is the main class for the Essence runtime, which serves music, processes
 * uploaded tracks, and calculates playlists using either local or external
 * scripts.
 * 
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {

	// Default folders
	public static final File LOG_DIR = new File("log/");
	public static final File TRACK_DIR = new File("tracks/");
	public static final File UPLOADS_DIR = new File(TRACK_DIR, "uploads/");

	// The number of tracs which are unchangeable
	public static final int MAX_PLAYLIST_SIZE = 3;

	public static Logger log;

	// Helper classes
	private AudioEncoder audioEncoder;
	private MongoDriver mongoDriver;

	// Services
	private TrackStreamer trackStreamer;
	private TrackProcessor trackProcessor;
	private PlaylistGeneratorService playlistGenRunner;

	// Keeps track of a stop command
	private boolean stop;

	// Allow the program to stop gracefully on an externel influence
	private Thread shutdownHook;

	/**
	 * Creates a new EssenceRuntime using the given properties file.
	 * 
	 * The runtime will not begin until run() is called.
	 * 
	 * 
	 * @throws IOException
	 *             If there are any errors initializing.
	 * 
	 */
	public EssenceRuntime(Properties p) throws IOException {

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

		// Make sure these exists or the program will crash
		TRACK_DIR.mkdirs();
		UPLOADS_DIR.mkdirs();

		// Helper Classes
		audioEncoder = new AudioEncoder(p.getProperty("ffmpegPath"), p.getProperty("ffprobePath"));
		mongoDriver = new MongoDriver(p.getProperty("mongoDBHost"), Integer.parseInt(p.getProperty("mongoDBPort")),
				p.getProperty("mongoDBName"));

		// Services
		trackStreamer = new TrackStreamer(mongoDriver, p);
		trackProcessor = new TrackProcessor(mongoDriver.getTrackColection(), audioEncoder);
		playlistGenRunner = new PlaylistGeneratorService(mongoDriver, p.getProperty("playlistGenerator"));

	}

	@Override
	public void run() {
		log.info("Starting EssenceRuntime");

		Runtime.getRuntime().addShutdownHook(shutdownHook);

		new Thread(trackStreamer).start();
		new Thread(trackProcessor).start();
		new Thread(playlistGenRunner).start();

		// Listen for commands
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

	/**
	 * Handle commands to the runtime.
	 * 
	 * Current commands:
	 * 
	 * Stop
	 * 
	 * 
	 * @param line
	 *            The command to analyze
	 */
	private void handleCommand(String line) {

		log.info("Received command: " + line);

		if (line.equals("stop"))
			stop();

	}

	/**
	 * Stops the runtime, gracefully killing all sub-processes
	 */
	public void stop() {

		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (IllegalStateException e) {
		}

		stop = true;

		log.info("Killing trackStreamer...");
		trackStreamer.stopAndWait();

		log.info("Killing TrackProcessor...");
		trackProcessor.stopAndWait();

		log.info("Killing PlaylistGeneratorService...");
		playlistGenRunner.stopAndWait();

		mongoDriver.close();
	}

}

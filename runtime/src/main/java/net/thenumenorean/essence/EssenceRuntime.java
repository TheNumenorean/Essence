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
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {

	public static final File LOG_DIR = new File("log/");
	public static final File TRACK_DIR = new File("tracks/");
	public static final File UPLOADS_DIR = new File(TRACK_DIR, "uploads/");
	
	public static final int MAX_PLAYLIST_SIZE = 3;

	public static Logger log;

	public AudioEncoder audioEncoder;

	private TrackStreamer trackStreamer;
	private TrackProcessor trackProcessor;
	private PlaylistGeneratorService playlistGenRunner;
	private MongoDriver mongoDriver;
	
	private boolean stop;
	private Thread shutdownHook;

	/**
	 * @throws IOException
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

		TRACK_DIR.mkdirs();
		UPLOADS_DIR.mkdirs();

		audioEncoder = new AudioEncoder(p.getProperty("ffmpegPath"), p.getProperty("ffprobePath"));

		mongoDriver = new MongoDriver(p.getProperty("mongoDBHost"), Integer.parseInt(p.getProperty("mongoDBPort")), p.getProperty("mongoDBName"));

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

		log.info("Killing trackStreamer...");
		trackStreamer.stopAndWait();

		log.info("Killing TrackProcessor...");
		trackProcessor.stopAndWait();

		log.info("Killing PlaylistGeneratorService...");
		playlistGenRunner.stopAndWait();
		
		mongoDriver.close();
	}



}

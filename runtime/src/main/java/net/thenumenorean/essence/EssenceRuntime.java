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
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import net.thenumenorean.essence.media.AudioEncoder;

/**
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {
	
	static final String FFMPEG_PATH = "/usr/bin/ffmpeg";
	static final String FFPROBE_PATH = "/usr/bin/ffprobe";

	static final File LOG_DIR = new File("log/");

	static final String STREAM_CONF = "ezstream.xml";

	static final File TRACK_DIR = new File("tracks/");
	static final File UPLOADS_DIR = new File(TRACK_DIR, "uploads/");
	static final File OUT_FILE = new File(TRACK_DIR, "current/next.mp3");

	static final String testvid = "https://www.youtube.com/watch?v=uE-1RPDqJAY&t=3s";

	public static Logger log;
	
	private ProcessBuilder pb;
	private Process ezstream;
	AudioEncoder audioEncoder;

	private UpNextPlacer upNextPlacer;
	private TrackProcessor trackProcessor;
	private boolean stop;
	private Thread shutdownHook;

	/**
	 * @throws IOException
	 * 
	 */
	public EssenceRuntime() throws IOException {

		stop = false;
		LOG_DIR.mkdirs();
		
		log  = Logger.getLogger("EssenceRuntime");
		log.addHandler(new FileHandler(LOG_DIR.getAbsolutePath() + "/EssenceRuntime.log"));
		
		
		shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {stop();}
		});

		TRACK_DIR.mkdirs();
		UPLOADS_DIR.mkdirs();

		File log = new File(LOG_DIR,
				"ezstream-" + (new SimpleDateFormat("YYYY_MM_dd_HH-mm").format(Date.from(Instant.now()))) + ".log");
		if (!log.exists()) {
			log.createNewFile();
		}

		pb = new ProcessBuilder("ezstream", "-c", STREAM_CONF);
		pb.redirectErrorStream(true);
		pb.redirectOutput(log);
		pb.directory(null);

		upNextPlacer = new UpNextPlacer(this);
		trackProcessor = new TrackProcessor(this);

		audioEncoder = new AudioEncoder(FFMPEG_PATH, FFPROBE_PATH);
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

		new Thread(upNextPlacer).start();
		new Thread(trackProcessor).start();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (!stop) {
			
			try {
			while(!br.ready() && !stop)
				Thread.sleep(500);
			
			if(br.ready())
				handleCommand(br.readLine());
			} catch (InterruptedException e) {
			} catch (IOException e) {
				log.severe(e.getMessage());
			}
		}
		
	}

	private void handleCommand(String line) {
		
		log.info("Received command: " + line);
		
		if(line.equals("stop"))
			stop();
		
	}

	public void stop() {
		
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch(IllegalStateException e) {
		}

		stop = true;

		log.info("Killing ezstream...");
		ezstream.destroy();

		log.info("Killing UpNextPlacer...");
		upNextPlacer.stopAndWait();

		log.info("Killing TrackProcessor...");
		trackProcessor.stopAndWait();
	}

	/**
	 * Gets the next track that should be played
	 * 
	 * @return
	 */
	public File getNextTrack() {
		// TODO make not random

		File[] tracks = TRACK_DIR.listFiles(new NoFolderFilter());

		return tracks.length < 1 ? null : tracks[0];

	}

}

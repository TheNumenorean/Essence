/**
 * 
 */
package net.thenumenorean.essence;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Scanner;

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
	
	
	
	private ProcessBuilder pb;
	private Process ezstream;
	AudioEncoder audioEncoder;
	
	private UpNextPlacer upNextPlacer;
	private TrackProcessor trackProcessor;
	private boolean stop;

	/**
	 * @throws IOException 
	 * 
	 */
	public EssenceRuntime() throws IOException {
		
		stop = false;

		TRACK_DIR.mkdirs();
		UPLOADS_DIR.mkdirs();
		
		
		LOG_DIR.mkdirs();
		File log = new File(LOG_DIR, "ezstream-" + (new SimpleDateFormat("YYYY_MM_dd_HH-mm").format(Date.from(Instant.now()))) + ".log");
		if(!log.exists()) {
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
		
		
		try {
			ezstream = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		

		new Thread(upNextPlacer).start();
		new Thread(trackProcessor).start();
		
		Scanner scan = new Scanner(System.in);
		while(!stop) {
			if(scan.nextLine().equals("stop"))
				stop();
		}
		scan.close();
	}

	public void stop() {
		
		stop = true;
		
		System.out.println("Killing ezstream...");
		ezstream.destroy();

		System.out.println("Killing UpNextPlacer...");
		upNextPlacer.stopAndWait();

		System.out.println("Killing TrackProcessor...");
		trackProcessor.stopAndWait();
	}
	
	
	/**
	 * Gets the next track that should be played
	 * @return
	 */
	public File getNextTrack() {
		// TODO make not random
		
		File[] tracks = TRACK_DIR.listFiles(new NoFolderFilter());
		
		return tracks.length < 1 ? null : tracks[0];
		
	}

}

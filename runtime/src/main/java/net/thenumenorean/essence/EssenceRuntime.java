/**
 * 
 */
package net.thenumenorean.essence;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {
	
	static final String STREAM_CONF = "ezstream.xml";
	static final String EZSTREAM_LOG = "log/exstream.log";

	static final File TRACK_DIR = new File("tracks/");
	static final File OUT_FILE = new File(TRACK_DIR, "current/next.mp3");

	static final String testvid = "https://www.youtube.com/watch?v=uE-1RPDqJAY&t=3s";

	/**
	 * 
	 */
	public EssenceRuntime() {

		TRACK_DIR.mkdir();
	}

	@Override
	public void run() {
		
		ProcessBuilder pb = new ProcessBuilder("ezstream -c " + STREAM_CONF);
		pb.redirectErrorStream(true);
		pb.redirectOutput(new File(EZSTREAM_LOG));
		
		Thread upNextPlacer = new Thread(new UpNextPlacer(this));
		
		try {
			Process ezstream = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		

		upNextPlacer.start();
	}

	private void findNewTracks() {

	}

	
	
	/**
	 * Gets the next track that should be played
	 * @return
	 */
	public File getNextTrack() {
		// TODO make not random
		
		File[] tracks = TRACK_DIR.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
			
		});
		
		return tracks.length < 1 ? null : tracks[0];
		
	}



}

/**
 * 
 */
package net.thenumenorean.essence;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {
	
	static final String STREAM_CONF = "ezstream.xml";
	static final String EZSTREAM_LOG = "log/exstream.log";

	static final File TRACK_DIR = new File("tracks/");
	static final File UPLOADS_DIR = new File(TRACK_DIR, "uploads/");
	static final File OUT_FILE = new File(TRACK_DIR, "current/next.mp3");

	static final String testvid = "https://www.youtube.com/watch?v=uE-1RPDqJAY&t=3s";
	
	
	
	private ProcessBuilder pb;
	private Thread upNextPlacer;
	Process ezstream;

	/**
	 * 
	 */
	public EssenceRuntime() {

		TRACK_DIR.mkdir();
		
		pb = new ProcessBuilder("ezstream -c " + STREAM_CONF);
		pb.redirectErrorStream(true);
		pb.redirectOutput(new File(EZSTREAM_LOG));
		

		upNextPlacer = new Thread(new UpNextPlacer(this));
	}

	@Override
	public void run() {
		
		
		try {
			ezstream = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		

		upNextPlacer.start();
	}

	private void findNewTracks() {
		
		File[] uploads = UPLOADS_DIR.listFiles(new NoFolderFilter());
		
		

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
	
	private class NoFolderFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			return pathname.isFile();
		}
		
	}

}

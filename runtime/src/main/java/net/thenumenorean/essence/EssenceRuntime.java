/**
 * 
 */
package net.thenumenorean.essence;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import com.google.common.io.Files;

import net.thenumenorean.essence.utils.RepeatingRunnable;

/**
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {

	static final File TRACK_DIR = new File("songs/");
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
		
		Thread upNextPlacer = new Thread(new UpNextPlacer());
		
		upNextPlacer.start();
	}

	private void findNewTracks() {

	}

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

	private class UpNextPlacer extends RepeatingRunnable {

		private static final int DEFAULT_WAIT = 10000;

		public UpNextPlacer() {
			this(DEFAULT_WAIT);
		}

		public UpNextPlacer(int wait) {
			super(wait);
		}

		@Override
		public void runOnce() {

			if (!OUT_FILE.exists()) {

				File next = EssenceRuntime.this.getNextTrack();

				if (next != null) {
					try {
						Files.move(next, OUT_FILE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EssenceRuntime rt = new EssenceRuntime();

		rt.run();

	}

}

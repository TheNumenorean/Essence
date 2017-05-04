/**
 * 
 */
package net.thenumenorean.essence;

import java.io.File;

import net.thenumenorean.essence.utils.RepeatingRunnable;

/**
 * @author Francesco Macagno
 *
 */
public class TrackProcessor extends RepeatingRunnable {

	private final EssenceRuntime essenceRuntime;
	private static final int DEFAULT_WAIT = 10000;

	public TrackProcessor(EssenceRuntime essenceRuntime) {
		this(essenceRuntime, DEFAULT_WAIT);
	}

	public TrackProcessor(EssenceRuntime essenceRuntime, int wait) {
		super(wait);
		this.essenceRuntime = essenceRuntime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.thenumenorean.essence.utils.RepeatingRunnable#runOnce()
	 */
	@Override
	public void runOnce() {
		File[] uploads = EssenceRuntime.UPLOADS_DIR.listFiles(new NoFolderFilter());

		for (File f : uploads) {

			try {
				File to = new File(EssenceRuntime.TRACK_DIR,
						f.getName().substring(0, f.getName().indexOf('.')) + ".mp3");
				essenceRuntime.audioEncoder.convert(f, to);
				f.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}

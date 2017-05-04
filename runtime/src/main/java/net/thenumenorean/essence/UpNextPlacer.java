package net.thenumenorean.essence;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import net.thenumenorean.essence.utils.RepeatingRunnable;

class UpNextPlacer extends RepeatingRunnable {

	/**
	 * 
	 */
	private final EssenceRuntime essenceRuntime;
	private static final int DEFAULT_WAIT = 30000;

	public UpNextPlacer(EssenceRuntime essenceRuntime) {
		this(essenceRuntime, DEFAULT_WAIT);
	}

	public UpNextPlacer(EssenceRuntime essenceRuntime, int wait) {
		super(wait);
		this.essenceRuntime = essenceRuntime;
	}

	@Override
	public void runOnce() {
		
		EssenceRuntime.log.info("Checking if new song needed...");

		if (!EssenceRuntime.OUT_FILE.exists()) {

			File next = this.essenceRuntime.getNextTrack();

			if (next != null) {
				EssenceRuntime.log.info("adding " + next.getName() + " as next");
				try {
					Files.move(next, EssenceRuntime.OUT_FILE);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

}
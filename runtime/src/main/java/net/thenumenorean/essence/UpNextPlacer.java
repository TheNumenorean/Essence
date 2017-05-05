package net.thenumenorean.essence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

import net.thenumenorean.essence.utils.RepeatingRunnable;

class UpNextPlacer extends RepeatingRunnable {

	/**
	 * 
	 */
	private final EssenceRuntime essenceRuntime;
	private File nextOutput;
	private static final int DEFAULT_WAIT = 30000;

	public UpNextPlacer(EssenceRuntime essenceRuntime, File nextOutput) {
		this(essenceRuntime, nextOutput, DEFAULT_WAIT);
	}

	public UpNextPlacer(EssenceRuntime essenceRuntime, File nextOutput, int wait) {
		super(wait);
		this.essenceRuntime = essenceRuntime;
		this.nextOutput = nextOutput;
	}

	@Override
	public void loop() {
		
		EssenceRuntime.log.info("Checking if new song needed...");

		if (nextOutput.length() == 0) {

			String next = essenceRuntime.getNextTrack();

			if (next != null) {
				EssenceRuntime.log.info("adding " + Files.getNameWithoutExtension(next) + " as next");
				try {
					Files.write(next, nextOutput, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

}
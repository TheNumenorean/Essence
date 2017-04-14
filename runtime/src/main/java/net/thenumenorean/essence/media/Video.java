/**
 * 
 */
package net.thenumenorean.essence.media;

import java.io.File;

/**
 * @author Francesco Macagno
 *
 */
public abstract class Video {

	protected abstract File getRawVideo();
	
	
	public void writeReencodedTo(File f, AudioEncoder encoder) {
		encoder.convert(getRawVideo(), f);
	}
}

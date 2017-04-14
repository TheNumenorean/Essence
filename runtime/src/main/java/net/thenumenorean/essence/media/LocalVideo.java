/**
 * 
 */
package net.thenumenorean.essence.media;

import java.io.File;

/**
 * @author Francesco
 *
 */
public class LocalVideo extends Video {

	private File file;

	/**
	 * 
	 */
	public LocalVideo(File f) {
		this.file = f;
	}

	@Override
	protected File getRawVideo() {
		return file;
	}

}

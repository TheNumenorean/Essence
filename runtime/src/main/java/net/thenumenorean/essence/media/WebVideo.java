/**
 * 
 */
package net.thenumenorean.essence.media;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import com.github.axet.vget.VGet;

/**
 * @author administrator
 *
 */
public class WebVideo extends Video {

	private static String DEFAULT_DOWNLOADS_DIR = "songs/downloads/";
	
	/**
	 * 
	 */
	public WebVideo(String url) {
		try {
			File tmp = new File(DEFAULT_DOWNLOADS_DIR + (new Random().nextInt()));
			tmp.mkdir();
			
			VGet get = new VGet(new URL(url), tmp);
			get.download();
			
			System.out.println(tmp.listFiles());
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.thenumenorean.essence.media.Video#getRawVideo()
	 */
	@Override
	protected File getRawVideo() {
		return null;
	}

}

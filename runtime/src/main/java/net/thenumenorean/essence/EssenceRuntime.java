/**
 * 
 */
package net.thenumenorean.essence;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.github.axet.vget.VGet;

/**
 * @author Francesco
 *
 */
public class EssenceRuntime implements Runnable {
	
	static final String testvid = "https://www.youtube.com/watch?v=uE-1RPDqJAY&t=3s";

	/**
	 * 
	 */
	public EssenceRuntime() {
		// TODO Auto-generated constructor stub
	}
	


	@Override
	public void run() {
		try {
			VGet get = new VGet(new URL(testvid), new File("."));
			get.download();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public File retrieveAndDownloadVideo()
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EssenceRuntime rt = new EssenceRuntime();
		
		rt.run();

	}

}

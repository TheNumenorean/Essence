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
	
	static final File OUT_FILE = new File("songs/current/next.mp3");
	
	static final String testvid = "https://www.youtube.com/watch?v=uE-1RPDqJAY&t=3s";

	/**
	 * 
	 */
	public EssenceRuntime() {
		// TODO Auto-generated constructor stub
	}
	


	@Override
	public void run() {
		
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EssenceRuntime rt = new EssenceRuntime();
		
		rt.run();

	}

}

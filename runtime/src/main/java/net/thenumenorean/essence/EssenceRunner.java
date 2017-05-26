/**
 * 
 */
package net.thenumenorean.essence;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author administrator
 *
 */
public class EssenceRunner {
	
	private static final String DEFAULT_PROP_FILE = "essence.properties";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Properties p = new Properties();
		
		if(args.length > 0)
			p.load(new FileInputStream(args[0]));
		else
			p.load(new FileInputStream(DEFAULT_PROP_FILE));
		
		
		EssenceRuntime rt = new EssenceRuntime(p);

		rt.run();

	}

}

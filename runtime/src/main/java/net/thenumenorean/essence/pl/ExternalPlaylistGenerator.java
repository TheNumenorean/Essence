/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.bson.Document;

import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco
 *
 */
public class ExternalPlaylistGenerator extends PlaylistGenerator {
	
	private static File MODULE_DIR = new File("modules");
	private static String MODULE_START_SCRIPT_NAME = "run.sh";
	
	private File modRunScript;
	

	/**
	 * @param md
	 * @param name 
	 */
	public ExternalPlaylistGenerator(MongoDriver md, String name) throws FileNotFoundException {
		super(md);
		
		modRunScript = new File(MODULE_DIR, name + File.pathSeparatorChar + MODULE_START_SCRIPT_NAME);
		
		if(!modRunScript.exists() || modRunScript.isDirectory())
			throw new FileNotFoundException("Couldnt find module at " + modRunScript.getPath());
		
	}

	/* (non-Javadoc)
	 * @see net.thenumenorean.essence.pl.PlaylistGenerator#generatePlaylist(java.util.List, java.util.List)
	 */
	@Override
	public List<Document> generatePlaylist(List<Document> currentPlaylist, List<Document> requests) {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.bson.Document;

import net.thenumenorean.essence.MongoDriver;

/**
 * This playlist generator uses an external script or program to calculate what
 * the new playlist should be. Data is passed using json and stdin and stdout.
 * 
 * @author Francesco
 *
 */
public class ExternalPlaylistGenerator extends PlaylistGenerator {

	private static File MODULE_DIR = new File("modules");
	private static String MODULE_START_SCRIPT_NAME = "run.sh";

	private File modRunScript;

	/**
	 * Create a new generator with the given name of the module to run
	 * 
	 * @param md
	 *            MongoDriver to get data from
	 * @param name
	 *            The name of the module to run. It will be looked for in
	 *            modules/"name"/run.sh
	 */
	public ExternalPlaylistGenerator(MongoDriver md, String name) throws FileNotFoundException {
		super(md);

		modRunScript = new File(MODULE_DIR, name + File.pathSeparatorChar + MODULE_START_SCRIPT_NAME);

		if (!modRunScript.exists() || modRunScript.isDirectory())
			throw new FileNotFoundException("Couldnt find module at " + modRunScript.getPath());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.thenumenorean.essence.pl.PlaylistGenerator#generatePlaylist(java.util
	 * .List, java.util.List)
	 */
	@Override
	public List<Document> generatePlaylist(List<Document> currentPlaylist, List<Document> requests) {

		ProcessBuilder pb = new ProcessBuilder("sh", modRunScript.getPath());
		pb.redirectErrorStream(false);
		pb.redirectInput(Redirect.PIPE);

		Process p;
		try {
			p = pb.start();

			PlaylistGenerator.writeDocumentsToStream(requests, p.getOutputStream());

			return PlaylistGenerator.readDocumentsFromStream(p.getInputStream());
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}

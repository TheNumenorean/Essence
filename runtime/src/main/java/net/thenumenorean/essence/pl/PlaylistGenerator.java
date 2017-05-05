/**
 * 
 */
package net.thenumenorean.essence.pl;

import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco
 *
 */
public abstract class PlaylistGenerator {

	/**
	 * 
	 */
	public PlaylistGenerator() {
		// TODO Auto-generated constructor stub
	}
	
	public abstract void regeneratePlaylist(MongoDriver md);

}

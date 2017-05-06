/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.util.List;

import org.bson.Document;

import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco Macagno
 *
 */
public abstract class PlaylistGenerator {

	protected MongoDriver md;

	/**
	 * 
	 */
	public PlaylistGenerator(MongoDriver md) {
		this.md = md;
	}
	
	public abstract List<Document> generatePlaylist(final List<Document> currentPlaylist, List<Document> requests);

}

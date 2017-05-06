/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;

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
	
	public abstract List<Document> generatePlaylist(final FindIterable<Document> currentPlaylist,  FindIterable<Document> requests);

}

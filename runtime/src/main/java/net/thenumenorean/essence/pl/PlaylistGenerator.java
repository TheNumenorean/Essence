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
	
	protected Document createFromRequest(Document d, int rank) {
		return new Document("rank", rank).append("track_id", d.getObjectId("track_id"))
				.append("req_id", d.getObjectId("_id")).append("user", d.getString("user"))
				.append("timestamp", d.getInteger("timestamp"));
	}

}

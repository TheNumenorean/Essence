/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import net.thenumenorean.essence.EssenceRuntime;
import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco Macagno
 *
 */
public class InsertOrderPlaylist extends PlaylistGenerator {


	public InsertOrderPlaylist(MongoDriver md) {
		super(md);
	}

	@Override
	public List<Document> generatePlaylist(List<Document> currentPlaylist, List<Document> requests) {
		List<Document> docs = new ArrayList<Document>();

		int rank = 0;
		for (Document d : requests) {

			Document trck = md.getTrack(d.getObjectId("track_id"));
			if (trck == null) {
				EssenceRuntime.log.severe("Request references nonexistent track!");
				continue;
			}

			// Only add to playlist if it hhas been processed
			if (trck.getBoolean("processed"))
				docs.add(new Document("rank", rank++).append("track_id", d.getObjectId("track_id"))
						.append("req_id", d.getObjectId("_id")).append("user", d.getString("user"))
						.append("timestamp", d.getInteger("timestamp")));
		}
		
		return docs;
	}

}

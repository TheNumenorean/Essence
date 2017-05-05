/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco Macagno
 *
 */
public class InsertOrderPlaylist extends PlaylistGenerator {

	/**
	 * 
	 */
	public InsertOrderPlaylist() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.thenumenorean.essence.pl.PlaylistGenerator#regeneratePlaylist(net.
	 * thenumenorean.essence.MongoDriver)
	 */
	@Override
	public void regeneratePlaylist(MongoDriver md) {

		// clear the existing playlist to populate it
		md.getPlaylistColection().deleteMany(Filters.ne("rank", -1));

		List<Document> docs = new ArrayList<Document>();

		int rank = 0;
		for (Document d : md.getRequestColection().find().sort(Sorts.ascending("timestamp"))) {
			docs.add(new Document("rank", rank++)
					.append("track_id", d.getObjectId("song_id"))
					.append("req_id", d.getObjectId("_id"))
					.append("user", d.getString("user"))
					.append("timestamp", d.getInteger("timestamp")));
		}
		
		if(!docs.isEmpty())
			md.getPlaylistColection().insertMany(docs);
	}

}

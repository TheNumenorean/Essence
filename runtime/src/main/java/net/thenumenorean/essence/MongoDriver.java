/**
 * 
 */
package net.thenumenorean.essence;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * @author Francesco
 *
 */
public class MongoDriver {
	
	public static final String DB_NAME  = "essence";
	
	private MongoClient mongo;
	private MongoDatabase mongodb;
	private MongoCollection<Document> playlistcol;
	private MongoCollection<Document> trackscol;
	private MongoCollection<Document> requestscol;

	/**
	 * 
	 */
	public MongoDriver() {
		

		mongo = new MongoClient();
		mongodb = mongo.getDatabase(DB_NAME);
		playlistcol = mongodb.getCollection("playlist");
		trackscol = mongodb.getCollection("tracks");
		requestscol = mongodb.getCollection("requests");
	}
	
	public MongoCollection<Document> getPlaylistColection() {
		return playlistcol;
	}
	
	public MongoCollection<Document> getTrackColection() {
		return trackscol;
	}
	
	public Document getTrack(long id) {
		return trackscol.find(Filters.eq("_id", id)).first();
	}

	public MongoCollection<Document> getRequestColection() {
		return requestscol;
	}

}

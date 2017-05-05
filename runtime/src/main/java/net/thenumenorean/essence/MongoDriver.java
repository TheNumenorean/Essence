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
	
	public static final String DB_NAME  = "Essence";
	
	private MongoClient mongo;
	private MongoDatabase mongodb;
	private MongoCollection<Document> playlistcol;
	private MongoCollection<Document> trackscol;

	/**
	 * 
	 */
	public MongoDriver() {
		

		mongo = new MongoClient();
		mongodb = mongo.getDatabase(DB_NAME);
		playlistcol = mongodb.getCollection("playlist");
		trackscol = mongodb.getCollection("tracks");
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

}

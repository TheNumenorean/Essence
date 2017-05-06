/**
 * 
 */
package net.thenumenorean.essence;

import org.bson.Document;
import org.bson.types.ObjectId;

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
	private MongoCollection<Document> playlistcol, trackscol, requestscol, historycol;

	/**
	 * Creates the default MongoDB connection
	 */
	public MongoDriver() {
		mongo = new MongoClient();
		init();
	}
	
	/**
	 * Creates a new MongoDB connection using the given connection parameters.
	 * @param host
	 * @param port
	 */
	public MongoDriver(String host, int port) {
		mongo = new MongoClient(host, port);
		init();
	}
	
	/**
	 * Initializes local helper instances
	 */
	private void init() {
		mongodb = mongo.getDatabase(DB_NAME);
		playlistcol = mongodb.getCollection("playlist");
		trackscol = mongodb.getCollection("tracks");
		requestscol = mongodb.getCollection("requests");
		historycol = mongodb.getCollection("histoy");
	}
	
	/**
	 * Get the collection which contains the Essence playlist
	 * @return A valid collection
	 */
	public MongoCollection<Document> getPlaylistColection() {
		return playlistcol;
	}
	
	/**
	 * Get the collection which contains the Essence stored tracks
	 * @return A valid collection
	 */
	public MongoCollection<Document> getTrackColection() {
		return trackscol;
	}

	/**
	 * Get the collection which contains the Essence track requests
	 * @return A valid collection
	 */
	public MongoCollection<Document> getRequestColection() {
		return requestscol;
	}

	/**
	 * Get the collection which contains the history of tracks played
	 * @return A valid collection
	 */
	public MongoCollection<Document> getHistoryColection() {
		return historycol;
	}
	
	/**
	 * Helper method to get the document for a given track
	 * @param id The id of the track
	 * @return A document representing the track, or null if it doesn't exist
	 */
	public Document getTrack(ObjectId id) {
		return trackscol.find(Filters.eq("_id", id)).first();
	}

}

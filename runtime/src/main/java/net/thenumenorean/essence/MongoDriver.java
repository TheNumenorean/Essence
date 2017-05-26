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
 * This class serves as a connection to a MongoDB instance in order to allow
 * data manipulation for essence.
 * 
 * @author Francesco
 *
 */
public class MongoDriver {

	public static final String DEFAULT_DB_NAME = "essence";
	public static final String PLAYLIST_COL_NAME = "playlist";
	public static final String TRACKS_COL_NAME = "tracks";
	public static final String REQUESTS_COL_NAME = "requests";
	public static final String HISTORY_COL_NAME = "history";

	private MongoClient mongo;
	private MongoDatabase mongodb;

	/**
	 * Creates the default MongoDB connection
	 */
	public MongoDriver() {
		mongo = new MongoClient();
		mongodb = mongo.getDatabase(DEFAULT_DB_NAME);
	}

	/**
	 * Creates a new MongoDB connection using the given connection parameters.
	 * 
	 * @param host
	 * @param port
	 */
	public MongoDriver(String host, int port, String essenceDBName) {
		mongo = new MongoClient(host, port);
		mongodb = mongo.getDatabase(essenceDBName);
	}

	/**
	 * Get the collection which contains the Essence playlist
	 * 
	 * @return A valid collection
	 */
	public MongoCollection<Document> getPlaylistColection() {
		return mongodb.getCollection(PLAYLIST_COL_NAME);
	}

	/**
	 * Get the collection which contains the Essence stored tracks
	 * 
	 * @return A valid collection
	 */
	public MongoCollection<Document> getTrackColection() {
		return mongodb.getCollection(TRACKS_COL_NAME);
	}

	/**
	 * Get the collection which contains the Essence track requests
	 * 
	 * @return A valid collection
	 */
	public MongoCollection<Document> getRequestColection() {
		return mongodb.getCollection(REQUESTS_COL_NAME);
	}

	/**
	 * Get the collection which contains the history of tracks played
	 * 
	 * @return A valid collection
	 */
	public MongoCollection<Document> getHistoryColection() {
		return mongodb.getCollection(HISTORY_COL_NAME);
	}

	/**
	 * Helper method to get the document for a given track
	 * 
	 * @param id
	 *            The id of the track
	 * @return A document representing the track, or null if it doesn't exist
	 */
	public Document getTrack(ObjectId id) {
		return getTrackColection().find(Filters.eq("_id", id)).first();
	}

	/**
	 * Kill the connection to the MongoDB server
	 */
	public void close() {
		mongo.close();
	}

}

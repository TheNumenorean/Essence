package net.thenumenorean.essence.pl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import net.thenumenorean.essence.EssenceRuntime;
import net.thenumenorean.essence.MongoDriver;
import net.thenumenorean.essence.utils.RepeatingRunnable;

public class PlaylistGeneratorService extends RepeatingRunnable {
	
	
	private static final int DEFAULT_WAIT = 5000;
	
	private PlaylistGenerator pg;

	private MongoDriver mongoDriver;

	public PlaylistGeneratorService(MongoDriver mongoDriver, String generator) {
		this(mongoDriver, generator, DEFAULT_WAIT);
	}
	
	public PlaylistGeneratorService(MongoDriver mongoDriver, String generator, int wait) {
		super(wait);
		this.mongoDriver = mongoDriver;
		loadGenerator(generator);
	}

	@Override
	public void loop() {

		

		// Go through all the requests and only use ones that point to a track that has been processed
		// Also sort the documents by timestamp low to high so that the are in order of oldest to newest
		List<Document> requests = new ArrayList<>();
		for(Document req : mongoDriver.getRequestColection().find().sort(Sorts.ascending("timestamp"))) {
			Document tr = mongoDriver.getTrack(req.getObjectId("track_id"));
			if(tr == null) {
				EssenceRuntime.log.severe("Request refrences nonexistent track!");
				continue;
			} else if(tr.getBoolean("processed"))
				requests.add(req);
		}
		
		if(requests.isEmpty())
			return;
		
		EssenceRuntime.log.info("Running playlist generation on " + requests.size() + " tracks");
		
		List<Document> docs = pg.generatePlaylist(mongoDriver.getPlaylistColection().find().into(new ArrayList<>()), requests);
		
		// Sort the results by rank in order to ensure they are in the right order
		docs.sort(new Comparator<Document>() {

			@Override
			public int compare(Document arg0, Document arg1) {
				return arg1.getInteger("rank") - arg0.getInteger("rank");
			}
			
		});
		
		// Prevent changes to playlist in the middle of operations
		synchronized(mongoDriver.getPlaylistColection()) {
			
			long remainingTracks = mongoDriver.getPlaylistColection().count();
			
			// Add only as many as gets us to the total allowed playlist size, since these are now set in stone.
			for(int i = 0; i < docs.size() && i < EssenceRuntime.MAX_PLAYLIST_SIZE - remainingTracks; i++) {
				Document newTrack = docs.get(i);
				newTrack.put("rank", remainingTracks - 1 + i); // Set the relevant rank relative to the prexisting playlist

				mongoDriver.getPlaylistColection().insertOne(newTrack);
				mongoDriver.getRequestColection().deleteOne(Filters.eq("_id", newTrack.getObjectId("req_id")));
			}
			
		}
		
	}
	
	void loadGenerator(String name) {
		
		if(name.equalsIgnoreCase("InsertOrder")) {
			pg = new InsertOrderPlaylist(mongoDriver);
		} else if(name.equalsIgnoreCase("Random")) {
			pg = new RandomPlaylist(mongoDriver);
		}
		
	}
	
}
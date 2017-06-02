package net.thenumenorean.essence.pl;

import java.io.FileNotFoundException;
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

	public PlaylistGeneratorService(MongoDriver mongoDriver, String generator) throws FileNotFoundException {
		this(mongoDriver, generator, DEFAULT_WAIT);
	}
	
	public PlaylistGeneratorService(MongoDriver mongoDriver, String generator, int wait) throws FileNotFoundException {
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
			
			mongoDriver.getPlaylistColection().deleteMany(Filters.eq("temporary", true));
			
			long remainingTracks = mongoDriver.getPlaylistColection().count();
			int trackNum = 0;
			// Add only as many as gets us to the total allowed playlist size, since these are now set in stone.
			for(; trackNum < docs.size(); trackNum++) {
				Document newTrack = docs.get(trackNum);
				newTrack.put("rank", remainingTracks - 1 + trackNum); // Set the relevant rank relative to the prexisting playlist

				if(trackNum < EssenceRuntime.MAX_PLAYLIST_SIZE - remainingTracks) {
					mongoDriver.getRequestColection().deleteOne(Filters.eq("_id", newTrack.getObjectId("req_id")));
				} else {
					newTrack.put("temporary", true);
				}
				
				mongoDriver.getPlaylistColection().insertOne(newTrack);
			}
			
			
			for(; trackNum < docs.size(); trackNum++) {
				
			}
		}
		
	}
	
	void loadGenerator(String name) throws FileNotFoundException {
		
		if(name.equalsIgnoreCase("InsertOrder")) {
			pg = new InsertOrderPlaylist(mongoDriver);
		} else if(name.equalsIgnoreCase("Random")) {
			pg = new RandomPlaylist(mongoDriver);
		} else {
			
			pg = new ExternalPlaylistGenerator(mongoDriver, name);

		}
		
	}
	
}
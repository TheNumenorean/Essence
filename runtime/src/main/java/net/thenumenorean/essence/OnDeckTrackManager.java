package net.thenumenorean.essence;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.bson.Document;

import com.google.common.io.Files;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.thenumenorean.essence.utils.RepeatingRunnable;

class OnDeckTrackManager extends RepeatingRunnable {

	/**
	 * 
	 */
	private final MongoDriver mongoDriver;
	private File nextOutput;
	private static final int DEFAULT_WAIT = 30000;

	public OnDeckTrackManager(MongoDriver mongoDriver, File nextOutput) {
		this(mongoDriver, nextOutput, DEFAULT_WAIT);
	}

	public OnDeckTrackManager(MongoDriver mongoDriver, File nextOutput, int wait) {
		super(wait);
		this.mongoDriver = mongoDriver;
		this.nextOutput = nextOutput;
	}

	@Override
	public void loop() {

		EssenceRuntime.log.info("Checking if new song needed...");

		if (nextOutput.length() == 0) {

			String next = getNextTrack();

			if (next != null) {
				EssenceRuntime.log.info("adding " + Files.getNameWithoutExtension(next) + " as next");
				try {
					Files.write(next, nextOutput, StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * Gets the next track that should be played
	 * 
	 * @return
	 */
	public String getNextTrack() {

		Document next = mongoDriver.getPlaylistColection().find(Filters.eq("rank", 0)).first();
		if (next == null) {
			EssenceRuntime.log.warning("getNextTrack found no next track!");
			return null;
		}

		// There is a track, so proceed

		synchronized (mongoDriver.getPlaylistColection()) {
			// Remove the just played songe from the playlist
			Document justPlayed = mongoDriver.getPlaylistColection().findOneAndDelete(Filters.eq("rank", -1));
			mongoDriver.getHistoryColection().insertOne(justPlayed);

			// Update playlist for meantime accesses
			// Move the up-next song (at rank 0) to playing (-1)
			mongoDriver.getPlaylistColection().updateMany(Filters.exists("rank"), Updates.inc("rank", -1));
		}

		Document nextTrack = mongoDriver.getTrack(next.getObjectId("track_id"));

		if (nextTrack == null) {
			EssenceRuntime.log.severe("Couldnt find track with id " + next.getObjectId("track_id"));
			return null;
		}

		return nextTrack.getString("location");
	}

}
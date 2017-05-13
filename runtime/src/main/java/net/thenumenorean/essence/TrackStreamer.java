package net.thenumenorean.essence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.bson.Document;

import com.gmail.kunicins.olegs.libshout.Libshout;
import com.google.common.io.Files;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.thenumenorean.essence.utils.RepeatingRunnable;

class TrackStreamer extends RepeatingRunnable {

	/**
	 * 
	 */
	private final MongoDriver mongoDriver;
	private Libshout icecast;
	private static final int DEFAULT_WAIT = 2000;

	private InputStream track;

	public TrackStreamer(MongoDriver mongoDriver) throws IOException {
		this(mongoDriver, DEFAULT_WAIT);
	}

	public TrackStreamer(MongoDriver mongoDriver, int wait) throws IOException {
		super(wait);
		this.mongoDriver = mongoDriver;

		icecast = new Libshout();
		icecast.setHost("localhost");
		icecast.setPort(8000);
		icecast.setProtocol(Libshout.PROTOCOL_HTTP);
		icecast.setPassword("SpaceMining");
		icecast.setMount("/stream");
		icecast.setFormat(Libshout.FORMAT_MP3);
		icecast.setName("Essence");
		icecast.setDescription("Essence music stream");
		icecast.setUrl("http://essence.caltech.edu:8000/stream");
		icecast.setGenre("All");
	}

	@Override
	public void runBefore() {
		try {
			icecast.open();
		} catch (IOException e) {
			e.printStackTrace();
			super.stop();
		}
	}

	@Override
	public void runAfter() {
		icecast.close();
	}

	@Override
	public void loop() {

		if (track == null) {

			String next = getNextTrack();
			if (next != null) {
				EssenceRuntime.log.info("Found next track at " + Files.getNameWithoutExtension(next));
				try {
					track = new BufferedInputStream(new FileInputStream(new File(next)));
				} catch (FileNotFoundException e) {
					EssenceRuntime.log.info("Error reading file!");
					e.printStackTrace();
				}
			} else {
				EssenceRuntime.log.info("No next track available!");
				return;
			}
		}

		try {
			if (!icecast.isConnected())
				icecast.open();
			byte[] buffer = new byte[1024];
			int read = track.read(buffer);
			while (read > 0 && !super.stoppedCalled()) {
				icecast.send(buffer, read);
				read = track.read(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				track.close();
			} catch (IOException e) {
			}

			track = null;
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
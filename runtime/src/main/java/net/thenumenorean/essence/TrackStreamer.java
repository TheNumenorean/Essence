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
	private static final int DEFAULT_WAIT = 2000;
	
	private IcecastStream iceStream;

	public TrackStreamer(MongoDriver mongoDriver) throws IOException {
		this(mongoDriver, DEFAULT_WAIT);
	}

	public TrackStreamer(MongoDriver mongoDriver, int wait) throws IOException {
		super(wait);
		this.mongoDriver = mongoDriver;

		iceStream = new IcecastStream();
	}
	
	@Override
	public void runBefore() {
		new Thread(iceStream).start();
	}
	
	@Override
	public void runAfter() {
		iceStream.stopAndWait();
	}

	@Override
	public void loop() {
		
		

		if (iceStream.track == null) {

			String next = getNextTrack();
			if (next != null) {
				EssenceRuntime.log.info("Found next track at " + Files.getNameWithoutExtension(next));
				try {
					iceStream.track = new BufferedInputStream(new FileInputStream(new File(next)));
				} catch (FileNotFoundException e) {
					EssenceRuntime.log.info("Error reading file!");
					e.printStackTrace();
				}
			} else {
				EssenceRuntime.log.info("No next track available!");
				return;
			}
		}

	}

	private class IcecastStream extends RepeatingRunnable {

		private Libshout ice;

		private byte[] silence, buffer;

		public InputStream track;

		public IcecastStream() throws IOException {
			super(0);
			track = null;
			
			ice = new Libshout();
			ice.setHost("localhost");
			ice.setPort(8000);
			ice.setProtocol(Libshout.PROTOCOL_HTTP);
			ice.setPassword("SpaceMining");
			ice.setMount("/stream");
			ice.setFormat(Libshout.FORMAT_MP3);
			ice.setName("Essence");
			ice.setDescription("Essence music stream");
			ice.setUrl("http://essence.caltech.edu:8000/stream");
			ice.setGenre("All");

			silence = new byte[1024];
			buffer = new byte[1024];

			InputStream tmp = new BufferedInputStream(new FileInputStream(new File("silence.mp3")));
			if (tmp.read(silence) < silence.length) {
				tmp.close();
				throw new IOException("silence file isnt large enough!");
			}
			tmp.close();
		}

		@Override
		public void runBefore() {
			try {
				ice.open();
			} catch (IOException e) {
				EssenceRuntime.log.severe("Error connecting to icecast!");
				e.printStackTrace();
				
				// Prevent the stream from continuing
				stop(); 
				TrackStreamer.this.stop();
			}
		}

		@Override
		public void runAfter() {
			ice.close();
		}

		@Override
		public void loop() {

			try {
				if (track == null) {
					ice.send(silence, silence.length);
				} else {

					int read = track.read(buffer);
					if (read > 0)
						ice.send(buffer, read);
					else {
						track.close();
						track = null;
					}
				}

			} catch (IOException e) {
				e.printStackTrace(); // Dont fail, it may be temporary
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
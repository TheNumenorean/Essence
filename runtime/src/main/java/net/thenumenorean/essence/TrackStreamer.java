package net.thenumenorean.essence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bson.Document;

import com.gmail.kunicins.olegs.libshout.Libshout;
import com.google.common.io.Files;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.thenumenorean.essence.utils.RepeatingRunnable;

/**
 * This class is used to send data to an Icecast server. This is complicated
 * because the icecast server must be continually receiving data or the
 * connection will irreperably die.
 * 
 * While this can also be fixed by creating a new connection whenever we want to
 * send a file, that results in receiving clients loosing the stream and needing
 * to be manualy restarted.
 * 
 * @author Francesco
 *
 */
class TrackStreamer extends RepeatingRunnable {

	public static final String SILENCE_FILE = "dat/silence.mp3";

	private TrackFetchService tFetch;
	private File track, silence;
	private Libshout ice;

	/**
	 * Create a new TrackStreamer with the given mongodriver to get new tracks from.
	 * The connection to icecast is not made until the thread is started.
	 * @param mongoDriver The driver to retrieve data with
	 * @param p 
	 * @throws IOException
	 */
	public TrackStreamer(MongoDriver mongoDriver, Properties p) throws IOException {

		// We want basically no delay between tracks in order to keep the
		// connection live
		super(200);

		track = null;

		ice = new Libshout();
		ice.setHost(p.getProperty("icecastHost"));
		ice.setPort(Integer.parseInt(p.getProperty("icecastPort")));
		ice.setProtocol(Libshout.PROTOCOL_HTTP);
		ice.setPassword(p.getProperty("icecastPasswd"));
		ice.setMount(p.getProperty("icecastMount"));
		ice.setFormat(Libshout.FORMAT_MP3);
		ice.setName(p.getProperty("icecastName"));
		ice.setDescription(p.getProperty("icecastDesc"));
		ice.setUrl(p.getProperty("icecastUrl"));
		ice.setGenre(p.getProperty("icecastGenre"));

		silence = new File(SILENCE_FILE);

		tFetch = new TrackFetchService(mongoDriver);
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
			return;
		}
		

		// We need a track to start with
		new Thread(tFetch).start();

	}

	@Override
	public void runAfter() {

		ice.close();

		// Make sure the fetch service has stopped
		tFetch.stopAndWait();
	}

	@Override
	public void loop() {

		// Every loop, decide to either send silence or a track
		try {
			if (track != null) {
				
				sendFile(track);
				
				track = null;

				// Make sure the last one has fully finished before starting a
				// new one
				tFetch.stopAndWait();

				// Get the next track
				new Thread(tFetch).start();

			} else {
				sendFile(silence);
			}

		} catch (IOException e) {
			e.printStackTrace(); // Dont fail, it may be temporary
		}

	}

	/**
	 * Send a file to Icecast as a bytestream. If the TrackStreamer is stopped,
	 * this method will stop sending data and return.
	 * 
	 * @param f
	 *            The file to send (must be correct format6)
	 * @throws IOException
	 */
	private void sendFile(File f) throws IOException {
		byte[] buffer = new byte[1024];

		InputStream tmp = new BufferedInputStream(new FileInputStream(f));
		int read = tmp.read(buffer);
		while (read > 0 && !this.stoppedCalled()) {
			ice.send(buffer, read);
			read = tmp.read(buffer);
		}
		tmp.close();
	}

	/**
	 * A service to retrieve the next track concurrecntly so that the connection
	 * to icecast can be maintained open by the main thread. Once run, the
	 * object continues checking for a next track until it finds one, puts it
	 * into the correct location, and then dies.
	 * 
	 * @author Francesco
	 *
	 */
	private class TrackFetchService extends RepeatingRunnable {

		private MongoDriver md;

		// Delay 1 second between each check to see if there is a new track
		private static final int DELAY = 1000;

		/**
		 * Creates a TrackFetchSerivce which uses the given MongoDriver to
		 * retrieve the next track
		 * 
		 * @param md
		 *            The mongodriver to use
		 * @throws IOException
		 */
		public TrackFetchService(MongoDriver md) throws IOException {
			super(DELAY);
			this.md = md;
		}

		@Override
		public void loop() {

			String nextAddr = getNextTrack(md);
			if (nextAddr != null) {
				EssenceRuntime.log.info("Found next track at " + Files.getNameWithoutExtension(nextAddr));
				TrackStreamer.this.track = new File(nextAddr);
				this.stop();
			}

		}

	}

	/**
	 * Gets the next track that should be played
	 * 
	 * @param md
	 * 
	 * @return
	 */
	public String getNextTrack(MongoDriver md) {

		Document next = md.getPlaylistColection().find(Filters.eq("rank", 0)).first();
		if (next == null) {
			return null;
		}

		// There is a track, so proceed

		synchronized (md.getPlaylistColection()) {
			// Remove the just played songe from the playlist
			Document justPlayed = md.getPlaylistColection().findOneAndDelete(Filters.eq("rank", -1));
			md.getHistoryColection().insertOne(justPlayed);

			// Update playlist for meantime accesses
			// Move the up-next song (at rank 0) to playing (-1)
			md.getPlaylistColection().updateMany(Filters.exists("rank"), Updates.inc("rank", -1));
		}

		Document nextTrack = md.getTrack(next.getObjectId("track_id"));

		if (nextTrack == null) {
			EssenceRuntime.log.severe("Couldnt find track with id " + next.getObjectId("track_id"));
			return null;
		}

		return nextTrack.getString("location");
	}

}
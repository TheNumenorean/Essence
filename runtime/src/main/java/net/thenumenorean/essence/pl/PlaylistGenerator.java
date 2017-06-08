/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco Macagno
 *
 */
public abstract class PlaylistGenerator {
	

	public static String DOC_HEADER = "RequestDocumentJSON:";

	protected MongoDriver md;

	/**
	 * 
	 */
	public PlaylistGenerator(MongoDriver md) {
		this.md = md;
	}

	public abstract List<Document> generatePlaylist(final List<Document> currentPlaylist, List<Document> requests);

	public static Document createFromRequest(Document d, int rank) {
		return new Document("rank", rank).append("track_id", d.getObjectId("track_id"))
				.append("req_id", d.getObjectId("_id")).append("user", d.getString("user"))
				.append("timestamp", d.getInteger("timestamp"));
	}

	public static void writeDocumentsToStream(List<Document> docs, OutputStream os) {
		PrintStream out = new PrintStream(os);

		for (Document d : docs) {
			out.println(DOC_HEADER);
			out.println(d.toJson());
		}
	}

	public static List<Document> readDocumentsFromStream(InputStream is) throws IOException {
		List<Document> out = new ArrayList<Document>();
		
		StringBuilder sb = null;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String tmp;
		while((tmp = reader.readLine()) != null) {
			if(tmp.startsWith(DOC_HEADER)) {
				if(sb != null) {
					out.add(Document.parse(sb.toString()));
				}
				
				sb = new StringBuilder();
			} else {
				
				if(sb == null)
					throw new RuntimeException("Input did not start with a valid header!");
				
				sb.append(tmp);
				
			}
		}
		
		
		return out;
	}

}

/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco Macagno
 *
 */
public class InsertOrderPlaylist extends PlaylistGenerator {


	public InsertOrderPlaylist(MongoDriver md) {
		super(md);
	}

	@Override
	public List<Document> generatePlaylist(List<Document> currentPlaylist, List<Document> requests) {
		List<Document> docs = new ArrayList<Document>();

		int rank = 0;
		for (Document d : requests)
			docs.add(createFromRequest(d, rank++));
		
		return docs;
	}

}

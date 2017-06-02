/**
 * 
 */
package net.thenumenorean.essence.pl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.Document;

import net.thenumenorean.essence.MongoDriver;

/**
 * @author Francesco Macagno
 *
 */
public class RandomPlaylist extends PlaylistGenerator {


	public RandomPlaylist(MongoDriver md) {
		super(md);
	}

	@Override
	public List<Document> generatePlaylist(List<Document> currentPlaylist, List<Document> requests) {
		List<Document> docs = new ArrayList<Document>();
		
		Collections.shuffle(requests);

		int rank = 0;
		for (Document d : requests)
			docs.add(createFromRequest(d, rank++));
		
		return docs;
	}

}

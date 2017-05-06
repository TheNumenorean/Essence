




<div id="queueContainerLocal" class="container-fluid">
    
   <h2>Queue</h2>
	<ul class="list-group">
	<?php
	
	require '/vendor/autoload.php';
	try {
		// get the current queue
		$connection = new MongoDB\Client();
		$tracks = $connection->essence->tracks;
		$playlist = $connection->essence->playlist;
		// find the documents, return items in cursor
		$cursor = $playlist->find([], ['sort'=> ['rank' => 1],]);

		// for each line, add a row
		foreach ($cursor as $obj) {
			if ($obj["rank"] == -1) {
				echo '<li class="list-group-item list-group-item-success">';
			} else {
				echo '<li class="list-group-item">';
			}
			echo '<p>', $obj["rank"], ' ', $tracks->findOne(['_id' => $obj["track_id"]])["title"], '</p>';
			echo '</li>';
		}
	} catch (Exception $e) {
		echo 'Uh-oh! Exception! ', $e->getMessage(), "\n";
	}		
	?>	
	</div>
    
    <button class="btn btn-success" onclick="loadQueue($('#queueContainerLocal').parent())">Refresh</button>


</div>


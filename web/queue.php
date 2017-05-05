




<div id="queueContainerLocal" class="container-fluid">
    
   <h2>Queue</h2>
	<div class="list-group">
	<?php
	
	require '/vendor/autoload.php';
	try {
		// get collection containing info
		$coll = (new MongoDB\Client())->test->sample;
		// find the documents, return items in cursor
		$cursor = $coll->find([], ['sort'=> ['_id' => -1],]);
		
		// for each line, add a row
		foreach ($cursor as $obj) {
			echo '<div class="list-group-item">';
			echo '<span class=glyphicon glyphicon-music><p>', $obj["name"], ', added ', (time() - $obj["_id"]), ' seconds ago', '</p></span>';
			echo '</div>';
		}
	} catch (Exception $e) {
		echo 'Uh-oh! Exception! ', $e->getMessage(), "\n";
	}		
	?>	
	</div>
    
    <button class="btn btn-success" onclick="loadQueue($('#queueContainerLocal').parent())">Refresh</button>


</div>


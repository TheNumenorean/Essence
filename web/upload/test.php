


<?php

require '/vendor/autoload.php';

	
	// create connection to the database
	echo "1attempting connection to db";
	echo extension_loaded("mongodb") ? "loaded\n" : "not loaded\n";

	// $connection = new MongoDB\Driver\Manager();
	$connection = new MongoDB\Client("mongodb://127.0.0.1:27017");
	echo "connected to db";
	//$echo $connection->getServers();
	echo "connected";
	// $dbname = $connection->selectDB('test');
	// $client = new MongoDB\Client("mongodb://localhost:27017");
	$collection = $connection->selectDB('test')->addTo;
	echo "got collection";

	$target_dir = "../../tracks/uploads/";
	$fileType = pathinfo($_FILES["musicFile"]["name"],PATHINFO_EXTENSION);
	
	$newName = basename($_POST["musicTitle"]);
	if(empty($newName))
		$newName = basename($_FILES["musicFile"]["name"]);
		
	$target_file = $target_dir . $newName . '.' . $fileType;

	// insert the file into the database
	echo "attempting insertion";
	$result = $collection->insert(array("name" => $newName, "filetype" => $fileType, "target_file" => $target_file,));
	echo "inserted with object id: '{$result->getInsertedId()}'";

	if(substr($_FILES["musicFile"]["mime_type"], 0, 7) === "audio/"){
		exit("Invalid file type");
	}
	// Check if file already exists
	if (file_exists($target_file)) {
		exit("file exists");
	}
	// Check file size
	if ($_FILES["musicFile"]["size"] > 100000000)
		exit("File too large");

	if($_FILES["musicFile"]["error"] != 0)
		echo "error " . $_FILES["musicFile"]["error"];

	if (move_uploaded_file($_FILES["musicFile"]["tmp_name"], $target_file)) {
	   echo "Success";
	} else {
	   echo "Failed";
	}
?>

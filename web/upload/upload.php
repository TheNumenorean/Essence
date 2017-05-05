


<?php

require '/vendor/autoload.php';

if(isset($_POST["fileUpload"])) {
	
	// create connection to the database
	$connection = new MongoDB\Client();
	$collection = $connection->test->sample;

	$target_dir = "../../tracks/uploads/";
	$fileType = pathinfo($_FILES["musicFile"]["name"],PATHINFO_EXTENSION);
	
	$newName = basename($_POST["musicTitle"]);
	if(empty($newName))
		$newName = basename($_FILES["musicFile"]["name"]);
		
	$target_file = $target_dir . $newName . '.' . $fileType;

	$finfo = new finfo(FILEINFO_MIME_TYPE);
	if(substr( $finfo->file($_FILES['musicFile']['tmp_name']), 0, 6) != "audio/"){
        exit('Invalid file format: ' . $finfo->file($_FILES['musicFile']['tmp_name']));
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

	// use current time (secs since unix epoch) as the unique id value:
	if (! $collection->insertOne(["_id" => time(), "name" => $newName, "filetype" => $fileType, "target_file" => $target_file,]))
		echo "failure to record in db";

	if (move_uploaded_file($_FILES["musicFile"]["tmp_name"], $target_file)) {
	   echo "Success";
	} else {
	   echo "Failed";
	}
}
?>

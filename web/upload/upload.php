


<?php

require '/vendor/autoload.php';

// create connection to the database
$connection = new MongoDB\Client();
$music = $connection->essence->tracks;
$requests = $connection->essence->requests;

// get the user's username
$username = $_SERVER['PHP_AUTH_USER'];

if(isset($_POST["fileUpload"])) {
	$target_dir = "tracks/uploads/";
	$fileType = pathinfo($_FILES["musicFile"]["name"],PATHINFO_EXTENSION);
	
	// get name of the song 	
	$newName = $_POST["musicTitle"];
	if(empty($newName))
		$newName = pathinfo($_FILES["musicFile"]["name"],PATHINFO_FILENAME);
	
	$target_file = $target_dir . $newName . '.' . $fileType;

	$finfo = new finfo(FILEINFO_MIME_TYPE);
	if(substr( $finfo->file($_FILES['musicFile']['tmp_name']), 0, 6) != "audio/"){
        exit('Invalid file format: ' . $finfo->file($_FILES['musicFile']['tmp_name']));
	}
	
	// Check if file already exists
	if (file_exists($target_file))
		exit("file exists");
	
	// Check file size
	if ($_FILES["musicFile"]["size"] > 100000000)
		exit("File too large");

	// check if there's some other error
	if($_FILES["musicFile"]["error"] != 0)
		echo "error " . $_FILES["musicFile"]["error"];

	// check for any errors in moving the file
	if (! move_uploaded_file($_FILES["musicFile"]["tmp_name"], "../../" . $target_file))
		exit("Unable to move file");

	// allow a uniquely assigned object id
	// attempt an insertion to the database

	$dbResult = $music->insertOne(["processed" => false, "location" => $target_file, "title" => $newName, "format" => $fileType, "add_time" => time(), "users_req" => [$username,],]);
	$requests->insertOne(["track_id" => $dbResult->getInsertedId(), "user" => $username, "timestamp" => time(),]);

	echo "Success";

} elseif(isset($_POST["webUpload"])) {
	// get name of the song
	$link = $_POST["webLink"];
	if(empty($link))
		exit("No link provided!!!");
	$newName = basename($_POST["musicTitle"]);
	if(empty($newName))
		$newName = basename($link);
	
	// allow a uniquely assigned object id
	// attempt an insertion to the database
	$dbResult = $music->insertOne(["processed" => false, "webaddress" => $link, "title" => $newName, "add_time" => time(), "users_req" => [$username,],]);
	$requests->insertOne(["track_id" => $dbResult->getInsertedId(), "user" => $username, "timestamp" => time()]);
	echo "Success";

}
?>

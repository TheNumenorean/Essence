


<?php

if(isset($_POST["fileUpload"])) {

	$target_dir = "../../tracks/uploads/";
	$fileType = pathinfo($_FILES["musicFile"]["name"],PATHINFO_EXTENSION);
	$target_file = $target_dir . basename($_POST["musicTitle"]) . '.' . $fileType;



	if(substr($_FILES["musicFile"]["mime_type"], 0, 7) === "audio/"){
		exit("Invalid file type");
	}
	// Check if file already exists
	if (file_exists($target_file)) {
		exit("file exists");
	}
	// Check file size
	if ($_FILES["musicFile"]["size"] > 50000000)
		exit("File too large");

	if($_FILES["musicFile"]["error"] != 0)
		echo "error " . $_FILES["musicFile"]["error"];

	if (move_uploaded_file($_FILES["musicFile"]["tmp_name"], $target_file)) {
	   echo "Success";
	} else {
	   echo "Failed";
	}
}
?>

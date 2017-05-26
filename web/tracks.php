<?PHP 

?>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous">


<title>Essence</title>

<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.0/js/tether.min.js" integrity="sha384-DztdAPBWPRXSA/3eYEEUWrWCy7G5KFbe8fFjk5JAIxUYHKkDx6Qin1DkWx51bBrb" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/js/bootstrap.min.js" integrity="sha384-vBWWzlZJ8ea9aCX4pEW3rVHjgjt7zpkNpZk+02D9phzyeVkE+jo0ieGizqPLForn" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.form/4.2.1/jquery.form.min.js" integrity="sha384-tIwI8+qJdZBtYYCKwRkjxBGQVZS3gGozr3CtI+5JF/oL1JmPEHzCEnIKbDbLTCer" crossorigin="anonymous"></script>
<script src="../queue.js"></script>


</head>

<body>
<nav class="navbar navbar-toggleable-md navbar-inverse bg-inverse">
  <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>
  <a class="navbar-brand" href="http://essence.caltech.edu/">Essence</a>
  <div class="collapse navbar-collapse" id="navbarNavDropdown">
    <ul class="navbar-nav">
      <li class="nav-item">
        <a class="nav-link" href="/">Home</a>
      </li>
      <li class="nav-item active">
        <a class="nav-link" href="/tracks.php">Tracks<span class="sr-only">(current)</span></a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="/settings">Settings</a>
      </li>
      <li class="nav-item dropdown">
        <a class="nav-link dropdown-toggle" href="/users" id="navbarDropdownMenuLink" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
          User Management
        </a>
        <div class="dropdown-menu" aria-labelledby="navbarDropdownMenuLink">
          <a class="dropdown-item" href="/users/passwd">Change Password</a>
          <a class="dropdown-item" href="/users/create">Create Account</a>
          <a class="dropdown-item" href="/users/whoami.php">Who Am I?</a>
        </div>
      </li>
    </ul>
  </div>
</nav>




<div class="container-fluid">
	
  <div class="row">
    <div class="col-md-8">
   
    <h2>All Tracks</h2> 
	<table class="table table-stripped">
	<?php

	require '/vendor/autoload.php';
	try {
		// get all tracks ever
		$conn = new MongoDB\Client();
		$tracks = $conn->essence->tracks;

		// find all the tracks ever
		$cursor = $tracks->find([], ['sort' => ['add_time' => -1],]);
		
		// for each line, add a row
		foreach($cursor as $obj) {
			echo '<tr>';
			echo '<td>' . $obj["title"] . '</td>';
			echo '<td><button type="button" class="btn btn-primary" onClick="requestTrack(\'' . $obj["_id"] . '\')">Request</button></td>';
			echo '</tr>';
		}
	} catch (Exception $e) {
		exit ($e->getMessage());
	}
	?>
	
    </table>
 
    </div>

    <div class="col-md-4">
    <h2>Player</h2>
    <!-- BEGINS: AUTO-GENERATED MUSES RADIO PLAYER CODE -->
<script type="text/javascript" src="https://hosted.muses.org/mrp.js"></script>
<script type="text/javascript">
MRP.insert({
'url':'http://essence.caltech.edu:8000/stream',
'codec':'mp3',
'volume':100,
'autoplay':false,
'buffering':0,
'title':'Essence',
'bgcolor':'#000000',
'skin':'darkconsole',
'width':190,
'height':62
});
</script>
<!-- ENDS: AUTO-GENERATED MUSES RADIO PLAYER CODE -->


    <div id="queueCont"></div>
    </div>
    
  </div>

</div>


<script>
$( document ).ready(function() {
	loadQueue($('#queueCont'));

});

function requestTrack(id) {

    $.post('../upload/upload.php',{
		'trackRequest':'true',
		'track_id': id
	}, function(resp){
		alert(resp);
	});

};
</script>





</body>
</html>

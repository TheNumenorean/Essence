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
<script src="queue.js"></script>


</head>

<body>
<nav class="navbar navbar-toggleable-md navbar-inverse bg-inverse">
  <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>
  <a class="navbar-brand" href="http://essence.caltech.edu/">Essence</a>
  <div class="collapse navbar-collapse" id="navbarNavDropdown">
    <ul class="navbar-nav">
      <li class="nav-item active">
        <a class="nav-link" href="/">Home <span class="sr-only">(current)</span></a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="/queue">Queue</a>
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
          <a class="dropdown-item" href="/users/whoami">Who Am I?</a>
        </div>
      </li>
    </ul>
  </div>
</nav>




<div class="container-fluid">
	
  <div class="row">
    <div class="col-md-8">
    
    <div class="jumbotron">
    
    	<h2>Add a song from the web</h2>
	<form id="webUpload" method="post" action="upload/upload.php" enctype="multipart/form-data">
	<input type="hidden" value="link" id="webUpload" name="webUpload"/>
	 <div class="form-group">
          <label for="musicTitle">Song Title</label>
          <input type="text" class="form-control" id="musicTitle" name="musicTitle" placeholder="Ride of the Valkyries">
        </div>

        <div class="form-group">
          <label for="webLink">Youtube Link</label>
          <input type="text" class="form-control" id="webLink" name="webLink">
        </div>
        <input type="submit" class="btn btn-success" value="Submit"/>
        <div class="alert" role="alert"></div>
       </form>
	
        
    </div>
        
        
    <div class="jumbotron">
        <h2>Add a song from your computer</h2>
       <form id="localUpload" method="post" action="upload/upload.php" enctype="multipart/form-data">
       <input type="hidden" value="file" id="fileUpload" name="fileUpload"/>
        <div class="form-group">
          <label for="musicTitle">Song Title</label>
          <input type="text" class="form-control" id="musicTitle" name="musicTitle" placeholder="Taking the Hobbbits to Isengard">
        </div>
        <div class="form-group">
          <label for="musicFile">Music File</label>
          <input type="file" name="musicFile" id="musicFile">
          <p class="help-block">Can be most formats.</p>
        </div>
        <input type="submit" class="btn btn-success" value="Upload"/>
        <div class="alert" role="alert"></div>
       </form>
	</div>
    
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
  
  $('#localUpload').ajaxForm({
	  url: 'upload/upload.php',
	  method: 'POST',
	  success: function(dat, stat, j) {
		  alert(dat);
	  },
	  error: function(j, stat, err) {
		  alert(err);
	  },
	  clearForm: true
  });

     $('#webUpload').ajaxForm({
	  url: 'upload/upload.php',
	  method: 'POST',
	  success: function(dat, stat, j) {
		  alert(dat);
	  },
	  error: function(j, stat, err) {
		  alert(err);
	  },
	  clearForm: true
  });


});
</script>





</body>
</html>

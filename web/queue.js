
function loadQueue(el) {
	
	console.log("Getting new track queue");
	
	if(!window.jQuery) alert('No jquery');
	
	jQuery.ajax('queue.php', {
		type: 'GET',
		success: function(dat, stat,j){
			$(el).html(dat);
		},
		error: function(j, stat, err){
			console.error('Error getting queue: ' + stat + ', ' + err);
		}
		
	});
	
}

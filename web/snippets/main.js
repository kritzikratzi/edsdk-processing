$( function(){
	$( "#snippets dd" ).each( function(i,e){
		var text = e.textContent; 
		text = text.replace( /{{([a-z]+)}}/g, "<span class='var-$1'>$1</span>" ); 
		e.innerHTML = text; 
	} ); 
	
	$( "#val-cam" ).keyup( function(){
		$( ".var-cam" ).html( this.value ); 
	}); 
})
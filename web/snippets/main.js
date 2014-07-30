$( function(){
	var list = $( "#select-snippet" ); 
	$( "#snippets dt" ).each( function(i,e){
		if( e.textContent.trim() == "" ) return; 
		list.append( "<option value=" + i + ">" + e.textContent + "</option>" ); 
	}); 
	
	
	$( "#snippets dd" ).each( function(i,e){
		var text = e.textContent; 
		text = text.replace( /{{([a-z]+)}}/g, "<span class='var-$1'>$1</span>" ); 
		e.innerHTML = text; 
	} ); 
	
	$( "#val-cam" ).keyup( function(){
		$( ".var-cam" ).html( this.value ); 
	}); 
	
	$( "#select-snippet" ).change( function(){
		var i = this.selectedIndex; 
		if( i > 0 ){ 
			$( "dd" ).hide(); 
			$( "dt" ).hide();
			$( "dd" ).eq( i-1 ).show(); 
			$( "dt" ).eq( i-1 ).show(); 
		}
		else{
			$( "dd" ).show(); 
			$( "dt" ).show();
		}
	}); 
})
package probe;

import java.io.File;

import processing.core.PApplet;
import processing.core.PImage;
import edsdk.bindings.*;
import edsdk.api.*;
import edsdk.api.commands.*;
import edsdk.utils.*;
import edsdk.utils.CanonConstants.EdsImageQuality;
import edsdk.processing.*; 

public class HansiTest extends PApplet{

	public static void main(String[] args) {
		PApplet.main( "probe.HansiTest" ); 
	}


	ProcessingCanonCamera cam; 
	PImage lastImage = null; 
	String lastPath = null; 


	public void setup(){
	  size( 800, 600 ); 
	  frameRate( 30 ); 
	  cam = new ProcessingCanonCamera( this ); 
	  
	  printArray(cam.getAvailableImageQualities()); 
	  cam.setImageQuality( EdsImageQuality.EdsImageQuality_MJN ); 
	  
	  cam.setDeleteAfterDownload(false); 
	  // automatically create small thumbnails for every jpeg that was downloaded
	  cam.setThumbnailWidth( 300 ); 
	  
	  // a background thread will fetch the liveview when it's on 
	  // alternatively you can call cam.read() inside the draw() loop.  
	  cam.setAutoUpdateLiveView( true ); 

	  // start the liveview
	  cam.beginLiveView();
	  

	}

	public void draw(){
	  background( 0 ); 
	  if( lastImage != null ){
		image( lastImage, 0, 0, width, lastImage.height*width/lastImage.width ); 
	    fill( 255 ); 
	    text( lastPath + " - " + lastImage.width + " x " + lastImage.height + " px", 10, height-10 ); 
	  }
	  
	  // read doesn't do much if liveview is off 
	  // cam.read(); 
	  if( cam.isLiveViewOn() ){
	    PImage live = cam.liveViewImage(); 
	    image( live, 0, 0, 400, live.height*400/live.width ); 
	  }
	  
	  fill( 255 ); 
	  text( frameCount + " "  + round(frameRate) + "fps", 10, 30 ); 
	 
	}

	public void keyPressed(){
	  // takeImage is non blocking! 
	  // use the callbacks (below) to be notified about incoming images. 
	  if( key == ' ' ){
	    cam.takeImage(); 
	  }
	  
	  if( key == 'a' ){
	    cam.beginLiveView(); 
	  }
	  
	  if( key == 's' ){
	    cam.endLiveView(); 
	  }
	}

	// jpeg callback from edsdk 
	public void imageTaken( File file, File thumbnail ){
	  System.out.println( "Image taken: " + file.getAbsolutePath() ); 
	  lastPath = file.getAbsolutePath(); 
	  lastImage = loadImage( thumbnail.getAbsolutePath() ); 
	  System.out.println( lastImage.width + " x " + lastImage.height );
	}

	// raw callback from edsdk
	public void imageTakenRaw( File file ){
	  System.out.println( "Found raw file: " + file.getAbsolutePath() ); 
	}
}

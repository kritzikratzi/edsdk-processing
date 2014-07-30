import edsdk.bindings.*;
import edsdk.api.*;
import edsdk.api.commands.*;
import edsdk.utils.*;
import edsdk.processing.*; 


ProcessingCanonCamera cam; 
PImage lastImage = null; 
String lastPath = null; 


void setup(){
  size( 800, 600 ); 
  cam = new ProcessingCanonCamera( this ); 
  
  printArray(cam.getAvailableImageQualities()); 
  cam.setImageQuality( EdsImageQuality.EdsImageQuality_MJN ); 
  
  // a background thread will fetch the liveview when it's on 
  // (you don't need to turn this on/off all the time! )
  // instead you could also do this manually by calling cam.read()   
  cam.setAutoUpdateLiveView( true ); 

  // start the liveview
  cam.beginLiveView();

}

void draw(){
  background( 0 ); 
  if( lastImage != null ){
    image( lastImage, 0, 0, width, lastImage.height*width/lastImage.width ); 
    fill( 255 ); 
    text( lastPath + " - " + lastImage.width + " x " + lastImage.height + " px", 10, height-10 ); 
  }
  
  
  // show the live view image
  if( cam.isLiveViewOn() ){
    PImage live = cam.liveViewImage(); 
    image( live, 0, 0, 400, live.height*400/live.width ); 
  }
  
  fill( 255 ); 
  text( frameCount + " "  + round(frameRate) + "fps", 10, 30 ); 
 
}

void keyPressed(){
  // takeImage is non blocking! 
  // use the callbacks (at the end of this file) to be notified about incoming images. 
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
public void imageTaken( File file ){
  System.out.println( "Image taken: " + file.getAbsolutePath() ); 
  lastPath = file.getAbsolutePath(); 
  lastImage = loadImage( file.getAbsolutePath() ); 
  System.out.println( lastImage.width + " x " + lastImage.height ); 
}

// raw callback from edsdk
// only used if enabled using setQuality()
public void imageTakenRaw( File file ){
  System.out.println( "Found raw file: " + file.getAbsolutePath() ); 
}
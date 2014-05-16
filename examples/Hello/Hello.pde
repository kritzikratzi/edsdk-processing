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
  cam.beginLiveView(); 
}

void draw(){
  background( 0 ); 
  if( lastImage != null ){
    image( lastImage, 0, 0, width, lastImage.height*width/lastImage.width ); 
    fill( 255 ); 
    text( lastPath + " - " + lastImage.width + " x " + lastImage.height + " px", 10, height-10 ); 
  }
  
  
  // read doesn't do much if liveview is off 
  cam.read(); 
  if( cam.isLiveViewOn() ){
    PImage live = cam.liveViewImage(); 
    image( live, 0, 0, 400, live.height*400/live.width ); 
  }
  
  fill( 255 ); 
  text( round(frameRate) + "fps", 10, 30 ); 
 
}

void keyPressed(){
  // takeImage is non blocking! 
  // use File file = cam.takeImage().get() 
  // to wait for the result (your sketch will look like it's crashing for a few seconds!)
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
public void imageTakenRaw( File file ){
  System.out.println( "Found raw file: " + file.getAbsolutePath() ); 
}

import edsdk.bindings.*;
import edsdk.api.*;
import edsdk.processing.*; 
import edsdk.utils.CanonConstants.*; 

ProcessingCanonCamera cam; 
PImage lastImage = null; 
String lastPath = null; 


void setup(){
  size( 800, 600 ); 
  cam = new ProcessingCanonCamera( this ); 
}

void draw(){
  background( 0 ); 
  fill( 255 ); 
  if( lastImage != null ){
    image( lastImage, 0, 0, width, lastImage.height*width/lastImage.width ); 
    text( lastPath + " - " + lastImage.width + " x " + lastImage.height + " px", 10, height-10 ); 
  }
  else{
    text( "Press spacebar to take an image", 10, height - 10 ); 
  }
}

void keyPressed(){
  // takeImage is non blocking! 
  // use the callbacks (at the end of this file) to be notified about incoming images. 
  if( key == ' ' ){
    cam.takeImage(); 
  }
}


// jpeg callback from edsdk 
public void imageTaken( File file ){
  System.out.println( "Image taken: " + file.getAbsolutePath() ); 
  lastPath = file.getAbsolutePath(); 
  lastImage = loadImage( file.getAbsolutePath() ); 
  System.out.println( lastImage.width + " x " + lastImage.height ); 
}

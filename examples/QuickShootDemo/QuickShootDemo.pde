import edsdk.bindings.*;
import edsdk.api.*;
import edsdk.api.commands.*;
import edsdk.utils.*;
import edsdk.processing.*; 

import static edsdk.utils.CanonConstants.*; 

ProcessingCanonCamera cam; 

PImage images[] = new PImage[6]; 
int isos[] = { ISO_200, ISO_400, ISO_800, ISO_1600, ISO_3200, ISO_6400 }; 
int aperatures[] = { Av_4_5, Av_6_3, Av_8, Av_10, Av_18, Av_25 }; 

boolean show = false; 

int NUM_COLS = 3; 

void setup(){
  size( 800, 600 ); 
  cam = new ProcessingCanonCamera( this ); 
  runSequence(); 
}

void draw(){
  background( 0 ); 
  if( show ){
    float imgWidth = width/NUM_COLS; 
    float imgHeight = images[0].height*imgWidth/images[0].width; 
    
    for( int i = 0; i < images.length; i++ ){
      int ix = i%NUM_COLS; 
      int iy = i/NUM_COLS; 
      float x = imgWidth * ix; 
      float y = imgHeight * iy; 
      image( images[i], x, y, imgWidth, imgHeight ); 
    }
  }
}

void keyPressed(){
  if( key == ' ' ){
    runSequence(); 
  }
}

void runSequence(){    
  // with this manager you can take a bunch of images, and maybe change settings in between 
  // if timing is important this is faster than waiting for the images to download 
  // (we do that later) 
  QuickShootManager mgr = new QuickShootManager( cam ); 
  for( int i = 0; i < images.length; i++ ){
    // change settings, take image
    cam.setISO( isos[i] ); 
    cam.setAv( aperatures[i] ); 
    mgr.trigger(); 
  }
  
  // the QuickShootManager doesn't call the imageTaken callback!  
  ArrayList<File> files = mgr.downloadAll().get(); 
  for( int i = 0; i < images.length; i++ ){
    System.out.println( "Load file: " + files.get(i).getAbsolutePath() ); 
    images[i] = loadImage( files.get(i).getAbsolutePath() ); 
  }
  show = true; 
}

// callback from edsdk 
public void imageTaken( File file ){
  // Note that the QuickShootManager doesn't invoke the imageTaken callback! 
  // Why not? It would make it's code much less elegant. 
  // If the current way downloadAll().get() is not good enough for you let me know...
  System.out.println( "I'm so lonely :(" ); 
}


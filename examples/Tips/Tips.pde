import edsdk.bindings.*;
import edsdk.api.*;
import edsdk.api.commands.*;
import edsdk.utils.*;
import edsdk.processing.*; 


ProcessingCanonCamera cam; 

// This file doesn't really do anything, it 
// shows how to use the less important party of the api. 

void setup(){
  size( 800, 600 ); 
  cam = new ProcessingCanonCamera( this ); 
  
  // delete/keep images after download
  // there is also setDeleteRawAfterDownload and 
  // setDeleteJpgAfterDownload for more fine grained control. 
  cam.setDeleteAfterDownload( false ); 
  
  // list all the image qualities this camera supports
  printArray(cam.getAvailableImageQualities()); 
  
  // set image quality to medium jpeg
  cam.setImageQuality( EdsImageQuality.EdsImageQuality_MJN ); 
  
  // enable thumbnails 
  // use 0 to disable them again. 
  cam.setThumbnailWidth( 300 ); 
  
  // change destination directory for downloaded images
  cam.setDirectory( "images" ); 
  
}

void draw(){
  background( 0 ); 
}

////////////////////////////////
// CALLBACKS                  //
// Use all the ones you like  //
////////////////////////////////


// jpeg callback from edsdk 
public void imageTaken( File file ){
  System.out.println( "Image taken: " + file.getAbsolutePath() ); 
  lastPath = file.getAbsolutePath(); 
  lastImage = loadImage( file.getAbsolutePath() ); 
  System.out.println( lastImage.width + " x " + lastImage.height ); 
}

// if you want to use the automatic thumbnails (enable with cam.setThumbnailWidth(300)) 
// then use this callback instead: 
public void imageTaken( File file, File thumbnail ){
  System.out.println( "Image taken: " + file.getAbsolutePath() ); 
  lastPath = file.getAbsolutePath(); 
  lastImage = loadImage( thumbnail.getAbsolutePath() ); 
  System.out.println( lastImage.width + " x " + lastImage.height ); 
}

// raw callback from edsdk
// only used if enabled using setQuality()
public void imageTakenRaw( File file ){
  System.out.println( "Found raw file: " + file.getAbsolutePath() ); 
}
/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright © 2014 Hansi Raber <super@superduper.org>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package edsdk.processing;


import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

import processing.core.PApplet;
import processing.core.PImage;

import com.sun.jna.NativeLong;

import edsdk.api.CanonCamera;
import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.bindings.EdSdkLibrary.EdsVoid;
import edsdk.bindings.EdSdkLibrary.__EdsObject;
import edsdk.utils.CanonConstants;
import edsdk.utils.CanonUtils;

/**
 * This is a template class and can be used to start a new processing library or tool.
 * Make sure you rename this class as well as the name of the example package 'template' 
 * to your own library or tool naming convention.
 * 
 * @example Hello 
 * 
 * (the tag @example followed by the name of an example included in folder 'examples' will
 * automatically include the example in the javadoc.)
 *
 */

public class ProcessingCanonCamera extends CanonCamera {
	
	// myParent is a reference to the parent sketch
	PApplet parent;
	PImage liveView; 
	Method imageTaken;
	Method imageTakenRaw;
	boolean liveViewOn; 
	long lastLiveViewStatusChecked; 
	SimpleDateFormat dateFormat = new SimpleDateFormat( "YYYY-MM-dd_HH-mm.ss.SSS" ); 

	public File dir; 
	public final static String VERSION = "##library.prettyVersion##";
	

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 * @return 
	 */
	public ProcessingCanonCamera(PApplet theParent) {
		parent = theParent;
		dir = parent.saveFile( "canon-images" ); 
		liveView = parent.createImage( 1, 1, PImage.RGB ); 
		welcome();
		openSession(); 
		
		parent.registerMethod( "dispose", this );
		
		try{
			imageTaken = parent.getClass().getMethod( 
				"imageTaken", 
				new Class[]{
					File.class
				}
			); 
		}
		catch( Exception e ){
			System.out.println( "void imageTaken( File file ){} not found. " ); 
			System.out.println( "Add this method to be notified of jpeg file downloads from the camera" ); 
		}
		
		try{
			imageTakenRaw = parent.getClass().getMethod( 
				"imageTakenRaw", 
				new Class[]{
					File.class
				}
			); 
		}
		catch( Exception e ){
			System.out.println( "void imageTakenRaw( File file ){} not found. " ); 
			System.out.println( "Add this method to be notified of raw file downloads from the camera" ); 
		}
		
		setupObjectListener(); 
	}
	
	public File setDirectory( String name ){
		return dir = parent.saveFile( name ); 
	}
	
	
	private void welcome() {
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
		System.out.println( "Running on Java " +
			System.getProperty( "java.version") + "/" + 
			System.getProperty( "java.vendor" ) + 
			" (" + System.getProperty("sun.arch.data.model") + "bit)"
		); 
	}
	
	private void setupObjectListener(){
		this.addObjectEventHandler( new EdsObjectEventHandler(){
			@Override
			public NativeLong apply( NativeLong inEvent, __EdsObject inRef, EdsVoid inContext ) {
				if( inEvent.intValue() == EdSdkLibrary.kEdsObjectEvent_DirItemCreated ){
					if( !dir.exists() ) dir.mkdirs();
					
					File res = CanonUtils.download( inRef, dir, true );
					String name = res.getName().toLowerCase();
					try{
						if( name.endsWith( ".jpg" ) || name.endsWith( ".jpeg" ) ){
							if( imageTaken != null ){
								imageTaken.invoke( parent, new Object[]{ res });
							}
						}
						else if( name.endsWith( ".raw" ) || name.endsWith( ".cr2" ) ){
							if( imageTakenRaw != null ){
								imageTakenRaw.invoke( parent, new Object[]{ res } );
							}
						}
					}
					catch( InvocationTargetException e ){
						e.printStackTrace(); 
					}
					catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
				
				return null;
			}
		});
	}
	
	
	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}
	
	
	/**
	 * Updates the live view status returned by cam.isLiveViewOn() and 
	 * reads the new image from the live view if it's enabled. 
	 */
	public void read(){
		
		if( !checkLiveViewStatus().get() ){
			return; 
		}
		
		
		BufferedImage img = downloadLiveView().get();
		
		if( img == null ){
			return; 
		}
		
		if( liveView == null ){
			liveView = parent.createImage( img.getWidth(), img.getHeight(), PImage.RGB ); 
		}
		else if( liveView.width != img.getWidth() || liveView.height != img.getHeight() ){
			liveView.resize( img.getWidth(), img.getHeight() );
		}
		
		if( liveView.pixels == null ){
			liveView.loadPixels(); 
		}
		
		PixelGrabber pg = new PixelGrabber(img, 0, 0, liveView.width, liveView.height, liveView.pixels, 0, liveView.width );
		try {
			pg.grabPixels();
			liveView.updatePixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isLiveViewOn(){
		if( System.currentTimeMillis() - lastLiveViewStatusChecked > 1000 ){
			checkLiveViewStatus(); 
			lastLiveViewStatusChecked = System.currentTimeMillis(); 
		}
		return liveViewOn; 
	}
	
	private CanonCommand<Boolean> checkLiveViewStatus(){
		CanonCommand<Boolean> cmd = execute( new CanonCommand<Boolean>() {
			@Override
			public void run() {
				lastLiveViewStatusChecked = System.currentTimeMillis(); 
				liveViewOn = CanonUtils.isLiveViewEnabled(edsCamera); 
				setResult( liveViewOn ); 
			}
		} ); 
		return cmd; 
	}
	
	public PImage liveViewImage(){
		return liveView; 
	}
	
	public void draw( float x, float y ){
		if( liveView != null ){
			parent.image( liveView, x, y ); 
		}
	}
	
	/**
	 * Takes an image and calls imageTaken( File file ) in your sketch 
	 * to notify you when the image is available. 
	 * 
	 * @return
	 */
	public void takeImage(){
		liveViewOn = false; 
		execute( new TakeImageCommand() ); 
	}
	
	
	
	/**
	 * Called by processing when the sketch is disposed
	 */
	public void dispose(){
		System.out.println( "Dispose party!" ); 
		executeNow( new CanonCommand<Void>() {
			@Override
			public void run() {
			}
		});
		closeSession();
		close(); 
	}
	
	

	public static class TakeImageCommand extends CanonCommand<Void>{
		private boolean oldEvfMode;
		
		public TakeImageCommand(){
		}
		
		@Override
		public void run() {
			int result = -1; 
			oldEvfMode = CanonUtils.isLiveViewEnabled( edsCamera ); 
			if( oldEvfMode ) CanonUtils.endLiveView( edsCamera );  
			while( result != EdSdkLibrary.EDS_ERR_OK ){
				result = sendCommand( EdSdkLibrary.kEdsCameraCommand_TakePicture, 0 ); 
				try {
					Thread.sleep( 1000 );
				}
				catch( InterruptedException e ){
					e.printStackTrace();
				}
			}
			if( oldEvfMode ) CanonUtils.beginLiveView( edsCamera ); 
		}
	}

}

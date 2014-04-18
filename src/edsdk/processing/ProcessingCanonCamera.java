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
import java.lang.reflect.Method;

import processing.core.PApplet;
import processing.core.PImage;
import edsdk.api.CanonCamera;
import edsdk.api.CanonCommand;
import edsdk.api.CanonCommandListener;
import edsdk.api.commands.LiveViewCommand;
import edsdk.api.commands.ShootCommand;
import edsdk.api.commands.LiveViewCommand.Begin;
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
	boolean liveViewOn; 
	long lastLiveViewStatusChecked; 
	
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
			System.out.println( "Add this method to be notified of file downloads from the camera" ); 
		}
	}
	
	
	private void welcome() {
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
		System.out.println( "Running on Java " +
			System.getProperty( "java.version") + "/" + 
			System.getProperty( "java.vendor" ) + 
			" (" + System.getProperty("sun.arch.data.model") + "bit)"
		); 
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
	public ShootCommand takeImage(){
		liveViewOn = false; 
		ShootCommand cmd = shoot(); 
		cmd.whenDone( new CanonCommandListener<File>() {
			@Override
			public void success( File f ){
				if( imageTaken != null ){
					try {
						imageTaken.invoke( parent, new Object[]{
							f
						});
					}
					catch (Exception e){
						e.printStackTrace();
					}
					
					checkLiveViewStatus(); 
				}
			}
		});
		
		return cmd; 
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
}

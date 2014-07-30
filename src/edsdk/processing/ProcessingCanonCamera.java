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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import processing.core.PApplet;
import processing.core.PImage;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import edsdk.api.CanonCamera;
import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsDirectoryItemRef;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.utils.CanonConstants;
import edsdk.utils.CanonConstants.EdsCameraCommand;
import edsdk.utils.CanonConstants.EdsError;
import edsdk.utils.CanonUtils;

/**
 * edsdk-p5 - An API for Canon cameras based on the edsdk4j. 
 * Basic features included live view and taking images. 
 * 
 * @example Hello 
 */

public class ProcessingCanonCamera extends CanonCamera {
	
	// myParent is a reference to the parent sketch
	PApplet parent;
	PImage liveView; 
	Method imageTaken;
	Method imageTakenWithThumbnail; 
	Method imageTakenRaw;
	boolean liveViewOn; 
	long lastLiveViewStatusChecked; 
	SimpleDateFormat dateFormat = new SimpleDateFormat( "YYYY-MM-dd_HH-mm.ss.SSS" ); 
	Thread liveViewThread; 
	boolean autoUpdateLiveView = false; 
	int thumbnailWidth; 
	boolean exiting = false; 
	boolean deleteJpgAfterDownload = true; 
	boolean deleteRawAfterDownload = true; 
	boolean autoDownload = true; 
	public File dir; 
	public final static String VERSION = "##library.prettyVersion##";
	
	LinkedBlockingQueue<Object[]> imageTakenQueue; 
	LinkedBlockingQueue<Object[]> imageTakenRawQueue; 
	
	LinkedBlockingQueue<EdsDirectoryItemRef> pendingDownloads; 
	
	ReentrantLock lock; 
	
	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 * @return 
	 */
	public ProcessingCanonCamera(PApplet theParent) {
		imageTakenQueue = new LinkedBlockingQueue<Object[]>();
		imageTakenRawQueue = new LinkedBlockingQueue<Object[]>(); 
		pendingDownloads = new LinkedBlockingQueue<EdsDirectoryItemRef>(); 
		lock = new ReentrantLock(); 
		
		parent = theParent;
		dir = parent.saveFile( "canon-images" ); 
		liveView = parent.createImage( 1, 1, PImage.RGB ); 
		welcome();
		if( !openSession() ){
			throw new RuntimeException( "Failed to find camera" ); 
		}
		
		parent.registerMethod( "pre", this );
		parent.registerMethod( "dispose", this );
		
		try{
			imageTaken = parent.getClass().getMethod( 
				"imageTaken", 
				new Class<?>[]{
					File.class 
				}
			); 
		}
		catch( Exception e ){
		}
		try{
			imageTakenWithThumbnail = parent.getClass().getMethod( 
				"imageTaken", 
				new Class<?>[]{
					File.class, 
					File.class
				}
			); 
		}
		catch( Exception e ){
		}
		
		if( imageTaken == null && imageTakenWithThumbnail == null ){
			System.out.println( "edsdk-p5: You don't have a callback for JPEG images taken from the camera. " );
			System.out.println( "You can add one of the following: " );
			System.out.println( "  void imageTaken( File image, File thumb ){} // with thumbnails " );
			System.out.println( "  void imageTaken( File image ){} // without thumbnails" );
		}
		
		try{
			imageTakenRaw = parent.getClass().getMethod( 
				"imageTakenRaw", 
				new Class<?>[]{
					File.class
				}
			); 
		}
		catch( Exception e ){
			System.out.println( "edsdk-p5: You don't have a callback for RAW images taken from the camera. " );
			System.out.println( "You can add one of the following: " );
			System.out.println( "  void imageTakenRaw( File imageRaw ){}" );
		}
		
		setupObjectListener(); 
	}
	
	/**
	 * Sets the destination directory for images 
	 * coming from the camera. 
	 * @param name
	 * @return
	 */
	public File setDirectory( String name ){
		return dir = parent.saveFile( name ); 
	}
	
	/**
	 * Sets the deleteAfterDownload option for jpg and raw images. 
	 * @param del True to delete images automatically. 
	 */
	public void setDeleteAfterDownload( boolean del ){
		deleteRawAfterDownload = del;
		deleteJpgAfterDownload = del; 
	}

	/**
	 * The status of the deleteAfterDownload option. 
	 * @return True if both, jpgs and raws, are to be deleted automatically. 
	 */
	public boolean getDeleteAfterDownload(){
		return deleteRawAfterDownload && deleteJpgAfterDownload; 
	}
	
	/**
	 * Sets the deleteAfterDownload option for jpg images. 
	 * @param del True to delete jpgs automatically
	 */
	public void setDeleteJpgAfterDownload( boolean del ){
		deleteJpgAfterDownload = del; 
	}
	
	/**
	 * The status of the deleteAfterDownload option for jpegs. 
	 * @return True if images are to be deleted automatically
	 */
	public boolean getDeleteJpgAfterDownload(){
		return deleteJpgAfterDownload; 
	}
	
	/**
	 * Sets the deleteAfterDownload option for raw images. 
	 * @param del True to delete raw images automatically
	 */
	public void setDeleteRawAfterDownload( boolean del ){
		deleteRawAfterDownload = del; 
	}
	
	/**
	 * The status of the deleteAfterDownload option for raw images. 
	 * @return
	 */
	public boolean getDeleteRawAfterDownload(){
		return deleteRawAfterDownload; 
	}
	
	/**
	 * Warning: Experimental!!! 
	 * Enables and disables auto downloading of images. 
	 * If autoDownload is turned back on the pending images will be download. 
	 * @param autoDownload True if images should automatically be downloaded as they come in. 
	 */
	public void setAutoDownload( boolean autoDownload ){
		this.autoDownload = autoDownload; 
		
		if( autoDownload && !pendingDownloads.isEmpty() ){
			new Thread(){
				public void run(){
					try{
						while( !pendingDownloads.isEmpty() ){
							EdsDirectoryItemRef inRef = pendingDownloads.take(); 
							handleDownload( inRef ); 
						}
					}
					catch( InterruptedException e ){
						e.printStackTrace(); 
					}
				}
			}.start(); 
		}
	}
	
	/**
	 * The status of the auto download option. 
	 * @param autoDownload
	 * @return
	 */
	public boolean getAutodownload(){
		return autoDownload; 
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
			public NativeLong apply( NativeLong inEvent, EdsBaseRef inRef, Pointer inContext ) {
				if( inEvent.intValue() == EdSdkLibrary.kEdsObjectEvent_DirItemCreated ){
					if( autoDownload ){
						handleDownload( new EdsDirectoryItemRef(inRef.getPointer()) ); 
					}
					else{
						pendingDownloads.add( new EdsDirectoryItemRef(inRef.getPointer()) ); 
					}
				}
				
				return null;
			}
		});
	}
	
	// only call this on the edsdk thread! 
	private void handleDownload( EdsDirectoryItemRef inRef ){
		if( !dir.exists() ) dir.mkdirs();
		
		File res = CanonUtils.download( inRef, dir, false );
		String name = res.getName().toLowerCase();
		if( name.endsWith( ".jpg" ) || name.endsWith( ".jpeg" ) ){
			if( deleteJpgAfterDownload ){
				CanonCamera.EDSDK.EdsDeleteDirectoryItem( inRef ); 
			}
			File thumb = null; 
			if( thumbnailWidth > 0 && imageTakenWithThumbnail != null ){
				thumb = makeThumbnail( res, thumbnailWidth );
			}
			
			if( imageTaken != null || imageTakenWithThumbnail != null ){
				imageTakenQueue.add( new Object[]{ res, thumb });
			}
		}
		else if( name.endsWith( ".raw" ) || name.endsWith( ".cr2" ) ){
			if( deleteRawAfterDownload ){
				CanonCamera.EDSDK.EdsDeleteDirectoryItem( inRef ); 
			}
			if( imageTakenRaw != null ){
				imageTakenRawQueue.add( new Object[]{ res } );
			}
		}
	}
	
	/**
	 * Convenience method. Loads a jpg and saves a thumbnail
	 * under the same name with "_small.jpg" as suffix. 
	 * @param imageTaken The image you want to resize. 
	 * @param width The width of the thumbnail 
	 * @return The path to the thumbnail 
	 */
	public File makeThumbnail( File imageTaken, int width ){
		if( width <= 0 ) return null; 
		
		BufferedImage img;
		try {
			img = ImageIO.read( imageTaken );
		}
		catch ( IOException e ){
			e.printStackTrace();
			return null; 
		} 
		
		int h = img.getHeight()*width/img.getWidth(); 
		BufferedImage thumb = new BufferedImage( width, h, BufferedImage.TYPE_INT_RGB ); 
		thumb.getGraphics().drawImage( img, 0, 0, width, h, null );
		
		String path = imageTaken.getAbsolutePath(); 
		path = path.substring( 0, path.length() - 4 ) + "_small.jpg"; 
		File dest = new File( path ); 
		try {
			ImageIO.write( thumb, "jpg", dest );
		}
		catch (IOException e) {
			e.printStackTrace();
			return null; 
		} 
		finally{
			img.flush(); 
			thumb.flush();
		}
		
		return dest; 
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
	 * 
	 * It is preferred to use {@link #setAutoDownload(boolean)} instead. 
	 */
	public void read(){
		if( isLiveViewOn() ){
			grabLiveViewPixels();
			if( liveView != null ) liveView.updatePixels();
		}
	}
	
	/**
	 * The first time this must should be called on the draw thread.
	 * It MUST be called on the draw thread if opengl is used.  
	 * @return
	 */
	private PixelGrabber grabLiveViewPixels(){
		if( !isLiveViewOn() ){
			return null; 
		}
		
		
		BufferedImage img = downloadLiveView();
		
		if( img == null ){
			return null; 
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
			lock.lock(); 
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			lock.unlock(); 
		}
		
		return pg; 
	}
	
	/**
	 * Queries the live view status of the camera. 
	 * This is done in the background to not interrupted your sketch. 
	 * The correct live view status might lag up to one second behind 
	 * this result. 
	 * @return The live view status
	 */
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
				liveViewOn = CanonUtils.isLiveViewEnabled(camera.getEdsCamera(),false); 
				setResult( liveViewOn ); 
			}
		} ); 
		return cmd; 
	}
	
	/**
	 * Returns a reference to the live view images. 
	 * @return
	 */
	public PImage liveViewImage(){
		return liveView; 
	}
	
	/**
	 * DON'T CALL THIS. 
	 * Processing does. 
	 */
	public void pre(){
		try {
			if( !imageTakenQueue.isEmpty() ){
				Object[] obj = imageTakenQueue.take();
				
				if( obj[1] != null ){
					if( imageTakenWithThumbnail != null ) imageTakenWithThumbnail.invoke( parent, obj ); 
					else if( imageTaken != null ) imageTaken.invoke( parent, obj[0] ); 
				}
				else{
					if( imageTaken != null ) imageTaken.invoke( parent, obj[0] ); 
					else if( imageTakenWithThumbnail != null ) imageTakenWithThumbnail.invoke( parent, obj ); 
				}
			}
			if( !imageTakenRawQueue.isEmpty() ){
				Object[] obj = imageTakenQueue.take(); 
				imageTakenRaw.invoke( parent, obj ); 
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Convenience method to draw the live view image
	 * @param x X coordinate
	 * @param y Y coordinate. 
	 */
	public void draw( float x, float y ){
		if( liveView != null ){
			parent.image( liveView, x, y ); 
		}
	}
	
	/**
	 * If enabled and the live view is on, then the live view images will  
	 * automatically download background. 
	 * @param auto Yea, set it to true to enable it. 
	 */
	public void setAutoUpdateLiveView( boolean auto ){
		this.autoUpdateLiveView = auto; 
		if( liveViewThread == null && auto ){
			liveViewThread = new LiveViewUpdater(); 
			liveViewThread.start(); 
		}
	}
	
	/**
	 * Sets the width of the automatically created thumbnails. 
	 * @param width 0 to disable this feature, a width of the thumbnail otherwise. 
	 */
	public void setThumbnailWidth( int width ){
		this.thumbnailWidth = width; 
	}
	
	
	/**
	 * Takes an image and calls 
	 * <code>
	 * 		imageTaken( File image, File thumb ) and/or
	 * 		imageTaken( File image ) and/or
	 * 		imageTakeRaw( File raw )
	 * </code>
	 * in your sketch 
	 * to notify you when the image is available. 
	 * 
	 * @return
	 */
	public void takeImage(){
		liveViewOn = false; 
		execute( new TakeImageCommand() ); 
	}
	
	
	/**
	 * DON'T CALL THIS! 
	 * Called by processing when the sketch is disposed. 
	 */
	public void dispose(){
		System.out.println( "Dispose party!" );
		exiting = true; 
		if( liveViewThread != null ){
			try {
				liveViewThread.interrupt(); 
				liveViewThread.join( 5000 );
			}
			catch (InterruptedException e) {
				System.err.println( "edsdk-p5: Couldn't end live view auto updater :(" ); 
				e.printStackTrace();
			}
		}
		executeNow( new CanonCommand<Void>() {
			@Override
			public void run() {
			}
		});
		closeSession();
		close(); 
	}
	
	

	private class TakeImageCommand extends CanonCommand<Void>{
		private boolean oldEvfMode;
		
		public TakeImageCommand(){
		}
		
		@Override
		public void run() {
			EdsError result = EdsError.EDS_ERR_UNKNOWN_COMMAND; 
			oldEvfMode = CanonUtils.isLiveViewEnabled( edsCamera, false ); 
			//if( oldEvfMode ) CanonUtils.endLiveView( edsCamera );  
			liveViewOn = false; 
			lastLiveViewStatusChecked = System.currentTimeMillis(); 
			while( result != EdsError.EDS_ERR_OK ){
				result = sendCommand( EdsCameraCommand.kEdsCameraCommand_TakePicture, 0 ); 
				try {
					Thread.sleep( 500 );
				}
				catch( InterruptedException e ){
					e.printStackTrace();
				}
			}
			if( oldEvfMode ){
				// CanonUtils.beginLiveView( edsCamera ); 
				liveViewOn = false; 
				lastLiveViewStatusChecked = System.currentTimeMillis(); 
			}
		}
	}

	/**
	 * Used internally only. 
	 * @author hanxi
	 *
	 */
	protected class LiveViewUpdater extends Thread{
		boolean wasUpdated = false; 
		boolean wantsRead = false;  
		
		public LiveViewUpdater() {
			parent.registerMethod( "pre", this );
		}
		
		@Override
		public void run() {
			while( !exiting ){
				if( wantsRead && isLiveViewOn()){
					PixelGrabber pg = grabLiveViewPixels(); 
					if( pg != null ) wasUpdated = true; 
				}
				
				try{ Thread.sleep( 1 ); }
				catch( InterruptedException e ){
					System.err.println( "edsdk-p5: Auto liveview updater stopped" ); 
				}
			}
		}
		
		public void pre(){
			if( wasUpdated ){
				try{
					lock.lock(); 
					liveView.updatePixels();
				}
				finally{
					wasUpdated = false; 
					lock.unlock(); 
				}
			}
			wantsRead = autoUpdateLiveView; 
		}		
	} 
}

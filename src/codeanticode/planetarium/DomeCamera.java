package codeanticode.planetarium;

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGL;

/**
 * Enables interaction (e.g. changing settings) with Dome renderer. 
 */
public class DomeCamera {
  public final static int POSITIVE_X = 0;
  public final static int NEGATIVE_X = 1;  
  public final static int POSITIVE_Y = 2;
  public final static int NEGATIVE_Y = 3;
  public final static int POSITIVE_Z = 4;
  public final static int NEGATIVE_Z = 5;
  
  protected Dome renderer;
  
  public void enable() {
    renderer.domeRendering(true);
  }
  
  public void disable() {
    renderer.domeRendering(false);
  }
  
  public void ortho(float left, float right,
                    float bottom, float top) {
    renderer.setOrthoParams(left, right, bottom, top);    
  }
  
  public void translate(float tx, float ty, float tz) {
    renderer.setOrigin(tx, ty, tz);
  }
  
  public void scale(float s) {
    renderer.setScale(s);
  }
  
  /**
   * Default constructor.
   * 
   *  @param		parent	the current sketch. Must have have already been initialized 
   *  					with "Dome.RENDERER" as renderer (by calling setup()). 
   *  					Supposed to have a single instance per sketch.
   *  @return			a new DomeCamera object.
   */
  public DomeCamera(PApplet parent) {
    if (!(parent.g instanceof Dome)) {
      throw new RuntimeException("You need to use the Dome renderer");
    }
    
    renderer = (Dome)parent.g;
  }
  
  /**
   * Sets rendering of the reference grid.
   * 
   *  @param		mode		accepts two values: Dome.NORMAL and Dome.GRID.
   */
  public void setMode(int mode) {
    if (mode == Dome.NORMAL) {
      renderer.renderGrid(false);
    } else if (mode == Dome.GRID) {
      renderer.renderGrid(true);
    }
  }
  
  /**
   * Sets the Dome aperture, i.e. the radial (in cartographic terms, latitudinal) coverage of the image.
   * 
   *  @param		theAperture	is a normalized value. 1f is regular, fulldome aperture, 
   *  						equals 90° from center to border or 180° across the image.
   */
  public void setDomeAperture(float theAperture) {
	  renderer.setDomeAperture(theAperture);
  }

  /**
   * Renders into a separate PGraphics object, which is displayed after the execution of the draw() call,
   * thus rendering "on top" of everything else. For performance reasons, should be placed inside the pre() method.
   * Its contents are cleared each frame.
   * 
   * @param	theImage		the image to be drawn.
   * @param	x0			the x coordinate of the upper left corner of the image.
   * @param	y0			the y coordinate of the upper left corner of the image.
   * @param	w			the width of the image, to which it should be scaled.
   * @param	h			the height of the image, to which it should be scaled.
   */  
  public void imageToForeground(PImage theImage, int x0, int y0, int w, int h) {
	  renderer.imageToForeground(theImage, x0, y0, w, h);
  }
  
  /* TODO: figure out a way for this to work
  public void imageToBackground(PImage theImage, int x0, int y0, int w, int h) {
	  renderer.imageToBackground(theImage, x0, y0, w, h);
  }
  */
  
  
  /**
   * Returns the currently rendered face index.
   * 
   * @return		0 == DomeCamera.POSITIVE_X
   * 			1 == DomeCamera.NEGATIVE_X
   *   			2 == DomeCamera.POSITIVE_Y
   * 			3 == DomeCamera.NEGATIVE_Y
   *       		4 == DomeCamera.POSITIVE_Z
   * 			5 == DomeCamera.NEGATIVE_Z
   */  
  public int getFace() {
    return renderer.getCurrentFace() - PGL.TEXTURE_CUBE_MAP_POSITIVE_X;
  }
  
  /**
   * Toggles the rendering into a cubemap face by index.
   * 
   * @param	theFace	0 == DomeCamera.POSITIVE_X
   * 				1 == DomeCamera.NEGATIVE_X
   *   				2 == DomeCamera.POSITIVE_Y
   * 				3 == DomeCamera.NEGATIVE_Y
   *       			4 == DomeCamera.POSITIVE_Z
   * 				5 == DomeCamera.NEGATIVE_Z
   */  
  public void toggleFaceDraw(int theFace) {
	  renderer.setFaceDraw(theFace, !renderer.getFaceDraw(theFace));
  }
  
  /**
   * Sets the rendering into a cubemap face by index.
   * 
   * @param	theFace	0 == DomeCamera.POSITIVE_X
   * 				1 == DomeCamera.NEGATIVE_X
   *   				2 == DomeCamera.POSITIVE_Y
   * 				3 == DomeCamera.NEGATIVE_Y
   *       			4 == DomeCamera.POSITIVE_Z
   * 				5 == DomeCamera.NEGATIVE_Z
   * @param doDraw	true or false
   */  
  public void setFaceDraw(int theFace, boolean doDraw) {
	  renderer.setFaceDraw(theFace, doDraw);
  }
  
  /**
   * Informs if a given face is being rendered into.
   * 
   * @param	theFace	0 == DomeCamera.POSITIVE_X
   * 				1 == DomeCamera.NEGATIVE_X
   *   				2 == DomeCamera.POSITIVE_Y
   * 				3 == DomeCamera.NEGATIVE_Y
   *       			4 == DomeCamera.POSITIVE_Z
   * 				5 == DomeCamera.NEGATIVE_Z
   * @return 		true if the given camera is being updated.
   */   
  public boolean getFaceDraw(int theFace) {
	return renderer.getFaceDraw(theFace); 
  }
  
  /**
   * Sets the cubemap size, i.e. the resolution of each(!) of the (potentially) 6 cameras we are rendering into. 
   * 
   * @param	theSize	the desired cubemap resolution. Will be rounded up to the next power of two 
   * 				smaller than the maximum texture size supported by the graphics card.
   * 
   */  
  public void setCubemapSize(int theSize) {
	  renderer.setCubemapSize(theSize);
  }
}

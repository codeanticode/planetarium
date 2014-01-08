package codeanticode.planetarium;

import processing.core.PApplet;
import processing.opengl.PGL;

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
  
  public void translate(float tx, float ty, float tz) {
    renderer.setOrigin(tx, ty, tz);
  }
  
  public void scale(float s) {
    renderer.setScale(s);
  }
  
  public DomeCamera(PApplet parent) {
    if (!(parent.g instanceof Dome)) {
      throw new RuntimeException("You need to use the Dome renderer");
    }
    
    renderer = (Dome)parent.g;
  }
  
  public void setMode(int mode) {
    if (mode == Dome.NORMAL) {
      renderer.renderGrid(false);
    } else if (mode == Dome.GRID) {
      renderer.renderGrid(true);
    }
  }
  
  public int getFace() {
    return renderer.getCurrentFace() - PGL.TEXTURE_CUBE_MAP_POSITIVE_X;
  }
}

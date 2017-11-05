// The planetarium library is designed to create real-time projections on 
// spherical domes. It is based on the FullDome project by Christopher 
// Warnow (ch.warnow@gmx.de):
// https://github.com/mphasize/FullDome
//
// A brief descrition on how it works: a 360° view of the scene is generated
// by rendering the scene 6 times from each direction: positive x, negative x, 
// positive y, and so on. The output of each rendering is stored inside a cube map 
// texture, which is then rendered on to a quad using a raytraced fisheye lens.
//
// Hence, the library calls the draw() method 6 times per frame in order to update  
// the corresponding side of the cube map texture (in reality, only 5 times since  
// the bottom side of the cube map is not invisible on the dome). 
// Now you can manually set which face should render using domeCamera's toggleFaceDraw() method.
//
// So, it is important to keep in mind that if you need to perform some calculation
// only one time per frame, then the code for those calculations should be put inside
// the pre() method.

import codeanticode.planetarium.*;

float cubeX, cubeY, cubeZ;

DomeCamera dc;
int gridMode = Dome.NORMAL;

void setup() {
  // For the time being, only use square windows  
  size(1024, 1024, Dome.RENDERER);
  //initial default camera, i.e. interface to interact with the renderer.
  dc = new DomeCamera(this);
  //Set the aperture, or radial coverage of the dome. 
  //1 is default, 2 would show the contents of an entire sphere.
  dc.setDomeAperture(2f);
  //we enable the sixth side, sothat we see what is happenning
  dc.setFaceDraw(DomeCamera.NEGATIVE_Z, true);
  //This method unfortunately doesn't work yet. 
  //dc.setCubemapSize(2048);
}

// Called one time per frame.
void pre() {
  // The dome projection is centered at (0, 0), so the mouse coordinates
  // need to be offset by (width/2, height/2)
  cubeX += ((mouseX - width * 0.5)*2 - cubeX) * 0.2;
  cubeY += ((mouseY - height * 0.5)*2 - cubeY) * 0.2;
}

// Called five times per frame.
void draw() {
  background(0, 0, 0, 0);

  pushMatrix();

  translate(width/2, height/2, -300);
  lights();

  stroke(0);  
  fill(150);
  pushMatrix();
  translate(cubeX, cubeY, cubeZ);  
  box(50);
  popMatrix();

  stroke(255);
  int linesAmount = 10;
  for (int i = 0; i < linesAmount; i++) {
    float ratio = (float)i/(linesAmount-1);
    line(0, 0, cos(ratio*TWO_PI) * 50, sin(ratio*TWO_PI) * 50);
  }
  popMatrix();
}

void mouseDragged() {
  //exaggerating dome aperture. 1f <=> 180°
  dc.setDomeAperture(map(mouseY, 0, height, 0.1f, 2f));
}

void keyPressed() {
  if (key == CODED) {
    if (keyCode == UP) cubeZ -= 5;
    else if (keyCode == DOWN) cubeZ += 5;
  }
  switch(key) {
  case ' ':
    gridMode = gridMode == Dome.GRID ? Dome.NORMAL : Dome.GRID;
    //enables rendering of a reference grid (happens inside the shader)
    dc.setMode(gridMode);
    break;
  case 'e':
    //fulldome-conform rendering
    dc.enable();
    break;
  case 'd':
    //rendering only into a single, conventional camera
    dc.disable();
    break;
  case '0':
    //toggles rendering into the X+ side of the cubemap
    dc.toggleFaceDraw(0);
    break;
  case '1':
    //toggles rendering into the X- side of the cubemap
    dc.toggleFaceDraw(1);
    break;
  case '2':
    //toggles rendering into the Y+ side of the cubemap
    dc.toggleFaceDraw(2);
    break;
  case '3':
    //toggles rendering into the Y- side of the cubemap
    dc.toggleFaceDraw(3);
    break;
  case '4':
    //toggles rendering into the Z+ side of the cubemap
    dc.toggleFaceDraw(4);
    break;
  case '5':
    //toggles rendering into the Z- side of the cubemap
    dc.toggleFaceDraw(5);
    break;
  }
}
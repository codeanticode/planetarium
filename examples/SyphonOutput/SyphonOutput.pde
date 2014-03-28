// Example showing how to use the syphon library to export frames
// to a Syphon client.

import codeanticode.planetarium.*;
import codeanticode.syphon.*;

SyphonServer server;
float cubeX, cubeY, cubeZ;

void setup() {
  // For the time being, only use square windows  
  size(600, 600, Dome.RENDERER);
  
  server = new SyphonServer(this, "Planetarium server");
}

// Called one time per frame.
void pre() {
  // The dome projection is centered at (0, 0), so the mouse coordinates
  // need to be offset by (width/2, height/2)
  cubeX += ((mouseX - width * 0.5) - cubeX) * 0.2;
  cubeY += ((mouseY - height * 0.5) - cubeY) * 0.2;
}

// Called five times per frame.
void draw() {
  background(0);
  
  pushMatrix();  
  translate(width/2, height/2, 300);
  
  lights();
  
  stroke(0);  
  fill(150);
  pushMatrix();
  translate(cubeX, cubeY, cubeZ);  
  box(50);
  popMatrix();

  stroke(255);
  int linesAmount = 10;
  for (int i = 0; i < linesAmount;i++) {
    float ratio = (float)i/(linesAmount-1);
    line(0, 0, cos(ratio*TWO_PI) * 50, sin(ratio*TWO_PI) * 50);
  }
  popMatrix();
}

// This function is called after the dome has been rendererd, so
// it will send the final image out through syphon
void screen() {
  server.sendImage(g);
}

void keyPressed() {
  if (key == CODED) {
    if (keyCode == UP) cubeZ -= 5;
    else if (keyCode == DOWN) cubeZ += 5;
  }  
}

import processing.core.*; 
import processing.xml.*; 

import crayolon.portamod.*; 

import java.applet.*; 
import java.awt.*; 
import java.awt.image.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class portaSpread extends PApplet {

/** This sketch shows dynamic adjustment of stereo separation spread
* in Amiga MODs. Amiga computers had four audio channels - they 
* panned 1 and 4 to the left, 100%, and 2 and 3 to the right. This
* is rather unpleasant to listen to on heaphones, but some afficionados
* still like to recreate it. 
*
* Values of between 10 and 20 offer just a bit of separation, while 0
* gives none and 100 gives Amiga-style hardpanning
*
* Click/drag the mouse up and down.
*
*/



PortaMod mymod;
NoteData incoming;
int[] spreadsquares;
int spread = 10;

public void setup() {
  size(200,200);
  mymod = new PortaMod(this);
  mymod.interpolation = 0;
  mymod.doModLoad("goto80-ter4.mod", true, 64);
  mymod.setStereosep(10);
  spreadsquares = new int[12];
  for (int i=0; i < spreadsquares.length; i++ ) {
     spreadsquares[i] = i*16; 
  }
}

public void draw() {
  background(0, 60);
  fill(255);
    
  for (int k=spreadsquares.length-1-spread; k>0; k--) {
    rect((width/2)-(k*width/16), (height/2)-20-spreadsquares[k]/2, width/16, spreadsquares[k]);
  }
  for (int j=0; j < spreadsquares.length-spread ; j++) {
    rect((width/2)+(j*width/16) -(width/16), (height/2)-20-spreadsquares[j]/2, width/16, spreadsquares[j]);
  }
  
  rect(0,height-20, ((width/mymod.numpatterns)*mymod.sequencecounter),20);
  
}

public void mouseDragged() {
  if (mouseY < height-20 && mouseY > 1) {
    mymod.setStereosep((int)map(mouseY, height-20, 0, 0, 100));
    spread = (int)map(mouseY, height-20, 0, 100, 0)/10;
  }
}

public void mouseClicked() {
  if (mouseY < height-20  && mouseY > 1) {
    mymod.setStereosep((int)map(mouseY, height-20, 0, 0, 100));
    spread = (int)map(mouseY, height-20, 0, 100, 0)/10;
  }
}

public void grabNewdata(PortaMod n) {
  incoming = n.localnotes;
  if (incoming.currentrow == 0) {
    println(mymod.getCurrent_sequence_index());
  } 
}

public void stop()
{
  mymod.stop();
  super.stop();
} 


  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#f3f2f5", "portaSpread" });
  }
}

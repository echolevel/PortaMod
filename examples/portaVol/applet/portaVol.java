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

public class portaVol extends PApplet {

/** 
* This sketch uses the chanVol command to set a new forced maximum
* volume for each separate channel. This overrides CXX and volume-col
* commands throughout and may be buggy (feedback appreciated!).
*
* Click/drag across all channels to do a sweeping volume-adjustment,
* or play with individual channels. Click and drag the progress bar
* at the bottom to seek through the song.
*
* NOTE: the white bars express the current relative volume level, from
* 0 to 64, according to the commands in the module. It's not supposed 
* to be an FFT spectrum representation! ;)
*/



PortaMod mymod;
NoteData incoming;
int[] customvol;

public void setup() {
  size(200,200);
  mymod = new PortaMod(this);
  mymod.setInterpolation(0);
  mymod.doModLoad("shaman-heaven7.xm", true, 64);
  mymod.setStereosep(10);
  customvol = new int[mymod.numchannels];
  for (int i=0; i < customvol.length; i++ ) {
   customvol[i] = mymod.getChanvol(i); 
  }
}

public void draw() {
  background(0, 60);
  fill(255);
  
  for (int c=0; c< mymod.numchannels; c++ ) {
    rect((width/mymod.numchannels)*c, height-20, width/mymod.numchannels, map(mymod.getChanvol(c), 0, 64, 0, -height+20));
  }
  
  rect(0,height-20, ((width/mymod.numpatterns)*mymod.sequencecounter),20);
  
}

public void mouseDragged() {
  for (int c=0; c < mymod.numchannels; c++ ) {
   if (mouseX > (width/mymod.numchannels)*c && mouseX < (width/mymod.numchannels)*c + width/mymod.numchannels && mouseY < height-20) {
     mymod.setChanvol(c, (int)map(mouseY, height-20, 0, 0, 63));
   }
  }
}

public void mouseClicked() {
  for (int c=0; c < mymod.numchannels; c++ ) {
   if (mouseX > (width/mymod.numchannels)*c && mouseX < (width/mymod.numchannels)*c + width/mymod.numchannels && mouseY < height-20) {
     mymod.setChanvol(c, (int)map(mouseY, height-20, 0, 0, 63));
   }
  }
  if (mouseY > height-20) {
    mymod.setNext_sequence_index((int)map(mouseX, 0, width, 0, mymod.numpatterns),0);
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
    PApplet.main(new String[] { "--bgcolor=#f3f2f5", "portaVol" });
  }
}

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

public class portaReplace extends PApplet {

/**
* This sketch shows off the sample replacement feature of PortaMod
* by replacing the song's perfectly acceptable samples with some 
* perfectly appalling ones :) Click the boxes to experiment...
*/



// Declare an instance of our library
PortaMod mymod;
// Declare a NoteData object to hold incoming module information
NoteData incoming;
boolean[] replaced = new boolean[5];

 public void setup() {
  size(200,200);
  mymod = new PortaMod(this);
  mymod.interpolation = 0;
  mymod.doModLoad("shaman-heaven7.xm", true, 64);
  mymod.setStereosep(10);
  for (int i=0;i<replaced.length;i++){
    replaced[i] = false;
  }
}

 public void draw() {
  background(0, 60);

  noStroke();

  for (int i=0; i<5; i++) {
    if (mouseX > 5 + (40*i) && mouseX < 35 + (40*i) && mouseY > width/2-30 && mouseY < width/2) {
      fill(0xffff6666);
      if (replaced[i]) { 
        fill(0xffff2222);
      }
      if (i==4) {
        fill(0xff00ff33); 
      }
      rect (5 + (40 * i), width/2-30, 30, 30);
    } else {
      fill(255);
      if (replaced[i]) { fill(0xffff2222); }
          rect (5 + (40 * i), width/2-30, 30, 30);
    }

  }

  fill(255);
  // Draw the song's progress (rounded to the nearest pattern)
  rect(0,height-20, ((width/mymod.numpatterns)*mymod.sequencecounter),20);

}

public void keyPressed() {
  
  if (keyCode == RIGHT) {
    mymod.setNext_sequence_index(mymod.getCurrent_sequence_index()+1, 1);
  }
  if (keyCode == LEFT) {
    mymod.setNext_sequence_index(mymod.getCurrent_sequence_index()-1, 1);
  }
  
}

public void mousePressed() {

    if (mouseX > 5 && mouseX < 35 && mouseY > width/2-30 && mouseY < width/2) {
      if (!replaced[0]) {
            // '1' is the instrument we want to change, 
            // "SlapBass" is an 8bit/mono/22050/RAW sample in 'data',
            // -3 is the pitch offset, to tune the new sample (not 
            // very accurate)
            mymod.sampleSwap(1, "SlapBass", -3);
        replaced[0] = true;   
      } else {
              // We can restore the song's original sample for this 
              // instrument number...
              mymod.sampleRestore(1);
        replaced[0] = false;
      }
    }
    if (mouseX > 45 && mouseX < 75 && mouseY > width/2-30 && mouseY < width/2) {
      if (!replaced[1]) {
            mymod.sampleSwap(5, "Karate", 3);
        replaced[1] = true;   
      } else {
              mymod.sampleRestore(5);
        replaced[1] = false;
      }
    }
    if (mouseX > 80 && mouseX < 110 && mouseY > width/2-30 && mouseY < width/2) {
      if (!replaced[2]) {
            replaced[2] = true;   
            mymod.sampleSwap(10, "EPiano", -4);

      } else {
            replaced[2] = false;
            mymod.sampleRestore(10);

      }
    }
    if (mouseX > 115 && mouseX < 145 && mouseY > width/2-30 && mouseY < width/2) {
      if (!replaced[3]) {
            mymod.sampleSwap(11, "Claps", -4);
        replaced[3] = true;   
      } else {
              mymod.sampleRestore(11);
        replaced[3] = false;
      }
    }    
  if (mouseX > 165 && mouseX < 195 && mouseY > width/2-30 && mouseY < width/2) {
    for (int i=0; i< mymod.numinstruments; i++ ) {
      mymod.sampleRestore(i);
      for (int j=0; j<replaced.length;j++){
        replaced[j] = false;
      }
    }
  }

}


public void grabNewdata(PortaMod n) {
  // Every time the player plays a new row of notes, it sends us
  // all of the current row's data in one NoteData object.
  incoming = n.localnotes;
}

public void stop()
{
  mymod.stop();
  super.stop();
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#f3f2f5", "portaReplace" });
  }
}

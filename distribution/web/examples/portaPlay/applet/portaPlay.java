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

public class portaPlay extends PApplet {

/**
* This is the quickest way to get a MOD/XM/S3M module playing in
* your sketch. The tune is 'olden days', a 4-channel Amiga MOD by 
* Syphus.
*/



// Instantiate the library
PortaMod mymod;

 public void setup() {
  size(200,200);
  // Initialise the library, passing our PApplet as 'this'
  mymod = new PortaMod(this);
  // Use the filepath (with the module in the sketch's 'data'
  // directory), 'true' to autostart playback and 6.020f as
  // the initial volume (maximum)
  mymod.doModLoad("syphus-oldendays.mod", true, 64);

}

 public void draw() {
  background(0);  
}

public void stop()
{
  mymod.stop();
  super.stop();
} 


  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#f3f2f5", "portaPlay" });
  }
}

/**
* This is the quickest way to get a MOD/XM/S3M module playing in
* your sketch. The tune is 'olden days', a 4-channel Amiga MOD by 
* Syphus.
* This example also shows the use of the NoteData callback to 
* get notifications of module pattern-data as it's processed
* by the replayer engine. 
*/

import crayolon.portamod.*;

// Instantiate the library
PortaMod mymod;

public NoteData incoming;

 void setup() {
  size(200,200);
  // Initialise the library, passing our PApplet as 'this'
  mymod = new PortaMod(this);
  // Use the filepath (with the module in the sketch's 'data'
  // directory), 'true' to autostart playback and 6.020f as
  // the initial volume (maximum)
  mymod.doModLoad("syphus-oldendays.mod", true, 64);

}

 void draw() {
  background(0);  
}

void stop()
{
  mymod.stop();
  super.stop();
} 

public void grabNewdata(PortaMod b) {				
  incoming = b.localnotes;
  /* Available from NoteData objects:
  channel, currentrealrow, currentrow, currentseq, effect,
  effparam, inst, note, seqlength, timestamp, vol
  */
    if(incoming.channel == 0) {
      println(incoming.currentrealrow + "   " + mymod.noteConvert(incoming.note));
    }
}

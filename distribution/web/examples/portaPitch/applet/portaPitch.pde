/**
*  This sketch exemplifies the transpose and BPM adjustment features of PortaMod.
*  Drag the left slider up and down to tranpose and the right slider to increase/decrease speed in beats per minute.
*
*/

import crayolon.portamod.*;

PortaMod mymod;
NoteData incoming;

void setup() {
  size(200,200);
  mymod = new PortaMod(this);
  mymod.interpolation = 0;
  mymod.doModLoad("syphus-save_a_prayer.xm", true, 64);
  mymod.setStereosep(5);

}

void draw() {
  background(0, 60);
  fill(255);
  stroke(255);
  
  line(20, height-(map(mymod.transpose,-12,12,0,height)), width/2-20, height-(map(mymod.transpose,-12,12,0,height)));
  
  line(width/2+20, height-(map(mymod.bpmvalue, 32, 255, 0, height)), width-20, height-(map(mymod.bpmvalue, 32, 255, 0, height)) );
}

public void mouseDragged() {
  if (mouseX < width/2) {
    int val = (int)map(mouseY, 0, height, 12, -12);
    mymod.setTranspose(-1,val);
    println("Transpose offset: " + val);
  } else {
    int val = (int)map(mouseY, 0, height, 255, 32);
    mymod.setTempo(val);
    println("New tempo: "+val+" BPM");
  }
}

public void mouseClicked() {

}

public void grabNewdata(PortaMod n) {
  incoming = n.localnotes;
  if (incoming.currentrow == 0) {
    println(mymod.getCurrent_sequence_index());
  } 
}



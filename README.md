# PortaMod

A MOD/XM/S3M tracker module replayer and manipulation library for Processing by Brendan Ratliff (aka Syphus^UpRough). Based on the IBXM replayer by Martin Cameron.

Fundamentally, PortaMod is a Processing wrapper for IBXM so that the Processing community can benefit from the advantages that oldschool 'tracker' formats offer.

Compared to WAV/MP3/OGG, these advantages include small file-sizes, the ability to synchronise visuals and other triggered events to events in the music (e.g. syncing a screen-flash to a particular note, by a particular instrument, at a particular time and volume) and the possibility for extensive live interaction with the music itself.

Compared to MIDI, tracked formats use instruments based on samples which sound the same on any replayer system, rather than being dependent on varying MIDI-synths. Though oldschool 4-channel chiptunes can be as small as 3 kilobytes, complex and high-quality music can be arranged in an XM of up to 32 channels, often matching mp3 quality but with a reduced filesize.

PortaMod builds quite heavily on IBXM in the control features it offers, while retaining IBXM's efficiency and reliable handling of these formats and their idiosyncrasies. Its diverse range of methods is intended to give the Processing community a very granular relationship with tracked audio data, which can either be found amongst the gigabytes of free modules available online or created with trackers such as ProTracker (Amiga), FastTracker (MS-DOS), ScreamTracker (MS-DOS), or the modern-day and very cross-platform MilkyTracker - www.milkytracker.org

### Notes (2019)

I made this library over 10 years ago and while I did share it at the time (to understandably little effect considering its niche appeal), I noticed today that it appears to have dropped offline. Thanks, Google Code. Anyway, I just tested it with Processing 3 and, surprisingly, it works fine! All the examples run properly and sound as they should. As well as being a standalone library, it's also the backbone of [Chipdisco](https://github.com/echolevel/chipdisco), which I also added to GitHub today. I even did a pure-Java version of the library designed for headless use on Raspberry Pi (which I'd forgotten until someone found a video and emailed me about it today); I'll share this too because, while the ARM JVM struggled with PortaMod on my original hardware in the early days of RasPi, I'm sure it'll run very quickly on today's hardware and JVM.

As for building the library...well, you can try your luck in Eclipse. I'm still on Eclipse Juno (2013) and if you look in the .classpath file you'll see that it wants a core.jar from Processing in its build path. I can't really remember the build process and don't have the time/inclination to re-learn it, but I'm content that PortaMod is at least usable as a library, as-is. I'll add the Processing-generated distribution release to the GitHub release section. Have fun!

_~ Brendan Ratliff, 2019_

### Usage

In a Processing sketch:

```
import crayolon.portamod.*;

// Instantiate the library:
PortaMod mymod;

void setup() {
  size(200, 200)
  // Init the lib, passing our PApplet as 'this'
  mymod = new PortaMod(this);
  // Use the filepath (with the module in the sketch's data directory), 'true' to autostart playback and 64 as the initial volume (maximum)
  mymod.doModLoad("syphus-oldendays.mod", true, 64)
}

void draw() {
  background(0);
}

void stop() {
  mymod.stop();
  super.stop();
}
```

### Methods

`doModLoad(String filepath, boolean autostart, int startVol)` - accepts a valid path, autostart true/false, and an int between 0 and 64 for initial volume (start at 0 if you plan to fade in later)

`play()` - play

`pause()` - pause

`stop()` - stop

`getCurrent_sequence_index()` - returns the current sequence position as an int

`getCurrent_row()` - returns the current row of the current sequence position as an int

`getNext_sequence_index()` - returns the next sequence position as an int

`getNext_row()` - returns the next row as an int

`setNext_sequence_index(int newposition, int behaviour)` - takes the desired new sequence position and an int 0 or 1 to choose between 'continuous' pattern-skip behaviour (where the row in the new pattern follows smoothly from the row in the old pattern) and 'play from start' behaviour, where the new pattern plays from row 0. Do the logic in your sketch using, for instance, getCurrent_sequence_index, to determine how to skip back or forward in your chosen increments.

`setNext_row(int newrow)` - set the next row to be played - good for creating sub-pattern loops in conjunction with getCurrent_row()

`getTitle()` - returns a string containing the module's title (if present)

`getVol()` - returns current overall volume as a float between -40.0f and 6.020f

`setVol()` - sets overall volume between -40.0f and 6.020f (checks limits to avoid Java Sound crashes)

`mute()` - mutes all audio (playback continues)

`setTranspose(int chan, int trans)` - shifts playback key up or down by up to 12 semitones while retaining current tempo. If chan is set to -1, all channels are transposed

`getTempo()` - returns an int between 32 and 255 reflecting module's current BPM tempo

`setTempo(int tempo)` - sets tempo between 32 and 255 bpm

`getPanning()` - returns current panning as a float between -1.0f and 1.0f, where 0 is the centre position

`setPanning(float panvalue)` - sets panning between -1.0f (hard left) and 1.0f (hard right). 0f is centre

`getChanmute(int chan)` - returns mute status of a channel as a boolean - true for muted, false for unmuted

`setChanmute(int chan, boolean mutestatus)` - set a channel's mute status

`setSongloop(boolean loopstatus)` - switch looping on/off. mymod.looping (where mymod is a PortaMod instance) returns current looping status as a boolean.

`getChanvol(int chan)` - returns a channel's current volume as an int between 0 and 64. This is the final volume given to the mixer engine after volume command/tremolo/envelope/global levels have been calculated.

`setChanvol(int chan, int vol)` - sets a channel's volume to an int between 0 and 64. This may interfere unexpectedly with some modules' volume-change effect commands or volumeslide commands... Use with caution and/or a sense of adventure.

`setGlobvol()` - set global volume between 0 and 64

`getGlobvol()` - get the global volume as an int between 0 and 64

`setGlobvolOverride` - set a strict override for global volume between 0 and 64

`getGlobvolOverride` - get a global override, if there is one

`setStereosep(int percentage)` - set a percentage of stereo spread on MOD files. 100% gives full Amiga-style hard-panning (ew), 0 centres all channels collapsing the overall mix to mono, whereas 10% gives a slight, pleasing separation. Only works on 4 or 8 channel files that are verified as being MODs; has no effect on XM or S3M files!

`setInterpolation(int interp)` - sets mixing interpolation to none (0), linear (1) or sinc (2). Default is none (0).

`getSeek()` - returns current song position in milliseconds as an int.

`setSeek(int newpos)` - sets current position of the song in milliseconds, from an int

`setOverridetempo()` - toggles tempo override. Many modules have tempo commands scattered throughout which can reset the BPM - sometimes it's desirable to avoid this when trying to run a module at a custom BPM. Use this in conjunction with bpm().

`sampleSwap(int sampleindex, String inputpath, int offset)` - Replace a sample in the loaded module with another sample - ideally under 25kb, in RAW/IFF/8SVX format, and 8bit/mono/<32khz. This can be done on the fly, during playback.

`sampleRestore(int sampleindex)` - takes the index of a sample you've replaced and restores the original sample

`headerCheck(String filepath)` - returns true if the module is a valid MOD/S3M/XM file and updates the internal modtype var accordingly.

`customkeyDown(int note, int instrument, String volumecommand, String effectcommand, String effectparam)` - injects notes in the currently playing module on the fly with custom patterndata. If instrument, effectcommand or effectparameter are 0, they aren't used; volumecommand must always be set (between 0 and 40 hex). Does not override existing notes; these notes are played in temporary 'virtual' channels

`customkeyUp(int note, int instrument)` - flips a previously triggered customkeyDown to off. Use -1 as the note parameter to disable all customkeyDowns

`effector(int channel, String effectcommand, String effectparam)` - EXPERIMENTAL: injects effect commands into a channel's playback on the fly. Supports E9X retrigger, AXX volumeslide, 0XX arpeggio, 2XX porta down and 1XX porta up, to wildly varying degrees of success. Other commands are ignored for now.

`loopStart(int instrument, int newloopstart)` - EXPERIMENTAL: adjusts the loop start point of a sample in the loaded module, including on the fly during playbck

`loopLength(int instrument, int newlooplength)` - EXPERIMENTAL: adjusts the loop length of a sample in the loaded module, including on the fly during playback

`loopReset(int instrument)` - resets loop start/length values to their originals

`sampleDump()` - writes a module's sampledata to individual files with the extension .raw

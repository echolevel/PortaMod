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

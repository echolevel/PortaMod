package crayolon.portamod;

import processing.core.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class Player {
	private Thread play_thread;
	public IBXM ibxm;
	public Module module;
	public int song_duration, play_position;
	public boolean running, loop;
	public byte[] output_buffer;
	public SourceDataLine output_line, source;
	public int receivedbuffersize;
	public int rate = 1024;
	public AudioFormat af; 
	Mixer.Info[] brenmix;
	Line.Info[] brenlines;
	Mixer brenmixer;
	// Mono panning: 0 = off, 1 = left, 2 = right
	public static int mono = 0;

	/**
		Simple command-line test player.
	*/
	/*
	public static void main( String[] args ) throws Exception {
		if( args.length < 1 ) {
			System.err.println( "Usage: java ibxm.Player <module file>" );
			System.exit( 0 );
		}
		FileInputStream file_input_stream = new FileInputStream( args[ 0 ] );
		Player player = new Player();
		player.set_module( Player.load_module( file_input_stream ) );
		//file_input_stream.close();
		player.play();
	}
	*/
	
	/**
		Decode the data in the specified InputStream into a Module instance.
		@param input an InputStream containing the module file to be decoded.
		@throws IllegalArgumentException if the data is not recognised as a module file.
	*/
	public static Module load_module( InputStream input ) throws IllegalArgumentException, IOException {
  		DataInputStream data_input_stream = new DataInputStream( input );
		/* Check if data is in XM format.*/
		byte[] xm_header = new byte[ 60 ];
		data_input_stream.readFully( xm_header );
		if( FastTracker2.is_xm( xm_header ) )
			return FastTracker2.load_xm( xm_header, data_input_stream );
		/* Check if data is in ScreamTracker 3 format.*/	
		byte[] s3m_header = new byte[ 96 ];
		System.arraycopy( xm_header, 0, s3m_header, 0, 60 );
		data_input_stream.readFully( s3m_header, 60, 36 );
		if( ScreamTracker3.is_s3m( s3m_header ) )
			return ScreamTracker3.load_s3m( s3m_header, data_input_stream );
		/* Check if data is in ProTracker format.*/
		byte[] mod_header = new byte[ 1084 ];
		System.arraycopy( s3m_header, 0, mod_header, 0, 96 );
		data_input_stream.readFully( mod_header, 96, 988 );
			return ProTracker.load_mod( mod_header, data_input_stream );
	}

	
	/**
		Instantiate a new Player.
	*/
	public Player(int interp, int mon) throws LineUnavailableException {
		mono = mon;
		ibxm = new IBXM( 48000, interp, mono );
		set_loop( true );
		System.out.println(AudioSystem.getMixerInfo());
		brenmix = AudioSystem.getMixerInfo();
		//this chooses the mixer I'm going to use. 0 should usually be system default...ChipdiscoDJ will need a second, separate mixer for headphone cue output
		brenmixer = AudioSystem.getMixer(brenmix[0]);
		brenlines = brenmixer.getSourceLineInfo();

		af = new AudioFormat((float)48000,16,2,true,true);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,af);
		output_line = AudioSystem.getSourceDataLine( new AudioFormat( 48000, 16, 2, true, true ), brenmixer.getMixerInfo() );
		output_buffer = new byte[ rate * 4];
	}

	/**
		Set the Module instance to be played.
	*/
	public void set_module( Module m ) {
		if( m != null ) module = m;
		stop();
		ibxm.set_module( module );
		song_duration = ibxm.calculate_song_duration();
	}

	/**
	
        	syphus' edit - user seek!
	*/
	public void seek( int s ) {
		int sample_position = s;
		ibxm.seek( sample_position );
                play_position = sample_position;
	}

        /**
                syphus' edit - return updated position after seek
        */
        public int get_seek_return() {
                return play_position;
        }
        
        public int get_bpm() {
                return module.default_tempo;
        }
        
	/**
	
        	syphus' edit - global volume! (out of 64)
	*/
	public void set_global_volume( int vol ) {
		int volume = vol;
		ibxm.set_global_volume( volume );
	}

        // Syphus' edit - speed:
       	public void set_speed( int speed ) {
		if( speed > 0 && speed < 256 ) {
			ibxm.set_speed(speed);
		}
	}
        // Syphus' edit - tempo
	public void set_tempo( int bpm ) {
		if( bpm > 31 && bpm < 256 ) {
			ibxm.set_tempo(bpm);
		}
	}


        // Syphus' edit - get some channel data
        public int get_num_channels() {
           int num_channels = module.get_num_channels();
         return num_channels; 
        }
	
	// Syphus' edit - get song length in patterns
        public int get_sequence_length() {
        return	module.get_sequence_length();
	}
        //Syphus' edit - SET current pattern ? Nope, not sure what this does...
	public void set_sequence( int sequence_index, int pattern_index ) {
		if( sequence_index >= 0 && sequence_index < module.get_sequence_length() ) {
                        module.set_sequence( sequence_index, pattern_index);
		}
	}

        //syphus' edit - get song title
        public String get_title() {
          return module.song_title;
        }
        
        //syphus - how many instruments?
        public int get_num_instruments() {
          int numinst = module.get_num_instruments();
          return numinst;
        }
        
        // syphus' edit - get instrument text
        public String[] infotext() {
          String[] infotext = null;
          for (int i=0; i < (module.get_num_instruments() - 1); i++) {
            try {
              if (module.instruments[i].name == null) {
                infotext[i] = "";
              } else {
              infotext[i] = module.instruments[i].name;
              }
            } catch (Exception e) {

            }
          }
          return infotext;
        }

        // syphus - try at least one instrument name
        public String ins_name(int ins_num) {
          String first_ins_name = module.instruments[ins_num].name;
          return first_ins_name;
        }

	/**
		If loop is true, playback will continue indefinitely,
		otherwise the module will play through once and stop.
	*/
	public void set_loop( boolean loop ) {
		this.loop = loop;
	}
	/**
	 * Syphus' edit - pass a custom buffersize to the class
	 * 
	 */
	public void receivebuffer(int buffersize){
		receivedbuffersize = buffersize;
	}
	/**
		Open the audio device and begin playback.
		If a module is already playing it will be restarted.
	*/
	public void play() {
		stop();
		play_thread = new Thread( new Driver() );
		play_thread.start();
		
	}
	
	/**
		Stop playback and close the audio device.
	*/
	public void stop() {
		running = false;
		if( play_thread != null ) {
			try {
				play_thread.join();
			} catch( InterruptedException ie ) {}
		}
	}
	
	public class Driver implements Runnable {
		
		public void run() {
			if( running ) return;
			try {
				//syphus: specified audioformat and BUFFERSIZE, to override the default of 96000! This helps sync enormously in a non-interactive presentation...
				output_line.open(new AudioFormat( 48000, 16, 2, true, true ), 11025);
				output_line.start();
				play_position = 0;
				running = true;
				while( running ) {
					int frames = song_duration - play_position;
					if( frames > rate ) frames = rate;
					ibxm.get_audio( output_buffer, frames );	
					output_line.write( output_buffer, 0, frames * 4 );
					play_position += frames;
					
					// Looping only seems to work if position is altered by a fraction of song_duration - doesn't work with pattern or note skipping
					if( play_position >= song_duration ) {
						play_position = 0;
						if( !loop ) running = false;
					}
				}
				// syphus: flush replaces drain - suggested by Martin for cleaner stopping/pausing.
				output_line.flush();
				output_line.close();
			} catch( LineUnavailableException lue ) {
				lue.printStackTrace();
			}
		}
	}
}

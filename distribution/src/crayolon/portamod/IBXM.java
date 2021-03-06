package crayolon.portamod;
import java.util.ArrayList;

//is also event notifier
public class IBXM {
	public static final String VERSION = "ibxm alpha 51 (c)2008 mumart@gmail.com";

	public static final int FP_SHIFT = 15;
	public static final int FP_ONE = 1 << FP_SHIFT;
	public static final int FP_MASK = FP_ONE - 1;

	private int sampling_rate, resampling_quality, volume_ramp_length;
	public int tick_length_samples, current_tick_samples;
	private int[] mixing_buffer, volume_ramp_buffer;

	public Module module;
	public Channel[] channels;
	public int[] global_volume, note;
	public ArrayList[] allnotes;
	public int[] notegrab;
	public int current_sequence_index, next_sequence_index;
	public int current_row, next_row;
	private int tick_counter, ticks_per_row;
	private int pattern_loop_count, pattern_loop_channel;
	public int[] row_data;

	//syphus
	public Channel[] jamchans;
	public boolean keydown[];
	public boolean effector[];
	public CustomNote customnote = new CustomNote();
	public NoteData notedata;
	public int rowcount = 0;
	public static PortaMod mc;
	public int thenum = 356;
	public int total_rows;
	public boolean[] chanmute;
	public int interpolation = 0;
	public boolean overridetempo;
	public byte[] waveform;
	public ArrayList<byte[]> waveformer = new ArrayList(); 
	public int mono = 0;
	
	public static void SetMC(PortaMod foo) {
		mc = foo;
	}
	

 

	
	public IBXM( int sample_rate, int interp, int mon) {
		
		mono = mon;
		System.out.println( VERSION );
		if( sample_rate < 8000 ) {
			sample_rate = 8000;
		}
		sampling_rate = sample_rate;
		volume_ramp_length = sampling_rate >> 10;
		volume_ramp_buffer = new int[ volume_ramp_length * 2 ];
		mixing_buffer = new int[ sampling_rate / 6 ];
		global_volume = new int[ 1 ];
		note = new int[ 5 ];
		set_module( new Module() );
		// 0 = no interpolation ('nearest'), 1 = linear, 2 = sinc 
		set_resampling_quality( interp );
	}

	public void set_module( Module m ) {
		int channel_idx;
		module = m;
		channels = new Channel[ module.get_num_channels() ];
		jamchans = new Channel[ module.get_num_channels() ];
                //notedata = new NoteData[module.get_num_channels()];
		for( channel_idx = 0; channel_idx < channels.length; channel_idx++ ) {
			channels[ channel_idx ] = new Channel( module, sampling_rate, global_volume, mono );
			jamchans[ channel_idx] = new Channel( module, sampling_rate, global_volume, mono);
			notedata = new NoteData();
                        
		}
		
		set_sequence_index( 0, 0 );
		
		//syphus' edit        
		chanmute = new boolean[channels.length];
		keydown = new boolean[channels.length];
		effector = new boolean[channels.length];
		for (int i = 0; i<keydown.length;i++){
			chanmute[i] = false;
			keydown[i] = false;
			effector[i] = false;
		}
	}

	public void set_resampling_quality( int quality ) {
		resampling_quality = quality;
	}
	
	public int calculate_song_duration() {
		int song_duration;
		waveform = new byte[4096];
		set_sequence_index( 0, 0 );
		next_tick();
		song_duration = tick_length_samples;
		while( !next_tick() ) {
			song_duration += tick_length_samples;

		}
		
		// Attempt to precalc a waveform of the entire song...so far a failure, but could work in future.
		/*
		System.out.println("Trying waveform thing...");
		int play_position = 0;
		int frames = song_duration - play_position;
		if( frames > 1024 ) frames = 1024;
		while(play_position < song_duration){
			get_audio(waveform, frames);
			System.out.println("Wavefrm: " + (byte)((waveform[1] >> 8) & 0xff));
			waveformer.add(waveform);
			play_position += frames;
		}
		*/

		set_sequence_index( 0, 0 );
		System.out.println("Calculated song duration!");
		return song_duration;
		
	}
	
	public void set_sequence_index( int sequence_index, int row ) {
		int channel_idx;
		global_volume[ 0 ] = 64;
		for( channel_idx = 0; channel_idx < channels.length; channel_idx++ ) {
			channels[ channel_idx ].reset();
			jamchans[ channel_idx].reset();
			channels[ channel_idx ].set_panning( module.get_initial_panning( channel_idx ) );
			jamchans[ channel_idx ].set_panning( module.get_initial_panning( channel_idx ) );
		}
		
		set_global_volume( module.global_volume );
		set_speed( 6 );
		set_speed( module.default_speed );
		set_tempo( 125 );
		set_tempo( module.default_tempo );
		pattern_loop_count = -1;
		next_sequence_index = sequence_index;
		next_row = row;
		tick_counter = 0;
		current_tick_samples = tick_length_samples;
		clear_vol_ramp_buffer();
	}

	public void seek( int sample_position ) {
		int idx;
		set_sequence_index( 0, 0 );
		next_tick();
		while( sample_position > tick_length_samples ) {
			sample_position -= tick_length_samples;
			next_tick();
		}
		mix_tick();
		current_tick_samples = sample_position;
	}

	public void get_audio( byte[] output_buffer, int frames ) {
		int output_idx, mix_idx, mix_end, count, amplitude;
		output_idx = 0;
		while( frames > 0 ) {
			count = tick_length_samples - current_tick_samples;
			if( count > frames ) {
				count = frames;
			}
			mix_idx = current_tick_samples << 1;
			mix_end = mix_idx + ( count << 1 ) - 1;
			while( mix_idx <= mix_end ) {
				amplitude = mixing_buffer[ mix_idx ];
				if( amplitude > 32767 ) {
					amplitude = 32767;
				}
				if( amplitude < -32768 ) {
					amplitude = -32768;
				}
                                try {
				output_buffer[ output_idx     ] = ( byte ) ( amplitude >> 8 );

				output_buffer[ output_idx + 1 ] = ( byte ) ( amplitude & 0xFF );
                                } catch (Exception e) {}
				output_idx += 2;
				mix_idx += 1;
			}
			current_tick_samples = mix_idx >> 1;
			frames -= count;
			if( frames > 0 ) {
				next_tick();
				mix_tick();
				current_tick_samples = 0;
			}
		}
	}

	private void mix_tick() {
		int channel_idx, mix_idx, mix_len;
		mix_idx = 0;
		mix_len = tick_length_samples + volume_ramp_length << 1;
		while( mix_idx < mix_len ) {
			mixing_buffer[ mix_idx ] = 0;
			mix_idx += 1;
		}
		for( channel_idx = 0; channel_idx < channels.length; channel_idx++ ) {
			//Syphus' edit - only mix the channel if the user hasn't muted it
			if (chanmute[channel_idx] == false){
				mix_len = tick_length_samples + volume_ramp_length;
				channels[ channel_idx ].resample( mixing_buffer, 0, mix_len, resampling_quality );
				
				
			}
			//but mix the jamchans no matter what!
			jamchans[ channel_idx ].resample( mixing_buffer, 0, mix_len, resampling_quality );
		}
		volume_ramp();
	}

	private boolean next_tick() {
		int channel_idx;
		boolean song_end;
		for( channel_idx = 0; channel_idx < channels.length; channel_idx++ ) {
			channels[ channel_idx ].update_sample_idx( tick_length_samples );
			jamchans[ channel_idx ].update_sample_idx( tick_length_samples );
		}
		tick_counter -= 1;
		if( tick_counter <= 0 ) {
			tick_counter = ticks_per_row;
			song_end = next_row();
		} else {
			for( channel_idx = 0; channel_idx < channels.length; channel_idx++ ) {
				//syphus' edit - this hijacks the currently playing note/instrument/etc with one of our own choosing
				if (keydown[ channel_idx ] && customnote.channel == channel_idx) {
					if (customnote.note > 0) {
						jamchans[ channel_idx].row(customnote.note, customnote.inst, customnote.vol, customnote.effect, customnote.effparam);
					}
					if (effector[ channel_idx ]) {
						/* Arpeggio */
						if (customnote.effect == 0) {
							switch( channels[ customnote.channel ].effect_tick % 3 ) {
							case 1:
								channels[ customnote.channel ].key_add = ( customnote.effparam & 0xF0 ) >> 4;
								break;
							case 2:
								channels[ customnote.channel ].key_add = customnote.effparam & 0x0F;
								break;
							}
							
						}
						/* Tone Porta */
						if (customnote.effect == 3) {
							channels[ customnote.channel ].tone_portamento();
						}
						// If E9X, do a retrigger on selected channel
						if ((customnote.effect == 14) && (((customnote.effparam & 0xf0) >> 4) == 9)) {
							channels[ customnote.channel ].set_retrig_param(customnote.effparam & 0x0f);
							channels[ customnote.channel ].retrig_volume_slide();
						}
						/* Volume Slide.*/
						if (customnote.effect == 10) {
							channels[ customnote.channel ].set_volume_slide_param( customnote.effparam );
							channels[ customnote.channel ].volume_slide();	
							
						}
						/* Porta Down */
						if (customnote.effect == 2) {
							channels[ customnote.channel ].set_portamento_param( customnote.effparam );
							channels[ customnote.channel ].portamento_down();
						}
						/* Porta Up */
						if (customnote.effect == 1) {
							channels[ customnote.channel ].set_portamento_param( customnote.effparam );
							channels[ customnote.channel ].portamento_up();
						}
						
					}
					channels[ customnote.channel ].calculate_amplitude();
					channels[ customnote.channel ].calculate_frequency();
					effector[channel_idx] = false;
					keydown[ channel_idx ] = false;
				} else {
					channels[ channel_idx ].tick();
					//jamchans[ channel_idx].row(97, customnote.inst, customnote.vol, customnote.effect, customnote.effparam);
					jamchans[ channel_idx ].tick();
				}
				
			}

			
			song_end = false;
		}
		return song_end;
	}


	public ArrayList<CurrentPattern>[] all_rows(){
		Pattern pattern = module.get_pattern_from_sequence( current_sequence_index );
		int channel_idx;
		notegrab = new int[5];
		if (allnotes != null) {
			for (int i=0; i<channels.length;i++){
				allnotes[i] = null;
			}
		} 
		
		allnotes = new ArrayList[channels.length];
		
		
		try {
			for (channel_idx=0; channel_idx < channels.length; channel_idx++){
				//start afresh with our ArrayList for this channel
				allnotes[ channel_idx ] = new ArrayList<CurrentPattern>();
				for (int rowcount=0; rowcount<pattern.num_rows; rowcount++){
					pattern.get_note(notegrab, rowcount*channels.length+channel_idx);
					
					//can't remember wtf's happening here...
					//channels[ channel_idx ].pattern_notes( allnotes[channel_idx][rowcount][ 0 ], allnotes[channel_idx][rowcount][ 1 ], allnotes[channel_idx][rowcount][ 2 ], allnotes[channel_idx][rowcount][ 3 ], allnotes[channel_idx][rowcount][ 4 ], rowcount, pattern.num_rows );
					channels[ channel_idx ].pattern_notes(notegrab[0], notegrab[1], notegrab[2], notegrab[3], notegrab[4], rowcount, pattern.num_rows);
					
					//build a CurrentPattern object with the fresh note
					CurrentPattern currentpatt = new CurrentPattern(notegrab);
					
					//hoy it in the ArrayList for this channel
					allnotes[ channel_idx ].add(currentpatt);

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allnotes;
	
		//this is presumably how I'd access allnotes...
		//CurrentPattern tempitem = (CurrentPattern)allnotes.get();
		
	}
	
	private boolean next_row() {
		int channel_idx, effect, effect_param;
		boolean song_end;
		Pattern pattern;
		song_end = false;

		if( next_sequence_index < 0 ) {
			/* Bad next sequence index.*/
			next_sequence_index = 0;
			next_row = 0;
		}
		if( next_sequence_index >= module.get_sequence_length() ) {
			/* End of sequence.*/
			song_end = true;
			next_sequence_index = module.restart_sequence_index;
			if( next_sequence_index < 0 ) {
				next_sequence_index = 0;
			}
			if( next_sequence_index >= module.get_sequence_length() ) {
				next_sequence_index = 0;
			}
			next_row = 0;
			
		}
		if( next_sequence_index < current_sequence_index ) {
			/* Jump to previous pattern. */
			song_end = true;
		}
		if( next_sequence_index == current_sequence_index ) {
			if( next_row <= current_row ) {
				if( pattern_loop_count < 0 ) {
					/* Jump to previous row in the same pattern, but not a pattern loop. */
					song_end = true;
				}
			}
		}
		current_sequence_index = next_sequence_index;
		pattern = module.get_pattern_from_sequence( current_sequence_index );
		if( next_row < 0 || next_row >= pattern.num_rows ) {
			/* Bad next row.*/
			next_row = 0;
		}
		current_row = next_row;
		next_row = current_row + 1;
		if( next_row >= pattern.num_rows ) {
			next_sequence_index = current_sequence_index + 1;
			next_row = 0;
		}
		
		for( channel_idx = 0; channel_idx < channels.length; channel_idx++ ) {
			
			pattern.get_note( note, current_row * channels.length + channel_idx );
			effect = note[ 3 ];
			effect_param = note[ 4 ];

			channels[ channel_idx ].row( note[ 0 ], note[ 1 ], note[ 2 ], effect, effect_param );
			
                        //syphus' note-data printing extravaganza, hooray
                          
                        	  try {
								if(channel_idx <= channels.length) {
									  total_rows = pattern.num_rows;
									  notedata.currentseq = current_sequence_index;
									  notedata.seqlength = pattern.num_rows;
									  notedata.currentrow = module.get_pattern_from_sequence(current_sequence_index).note_index;
									  notedata.currentrealrow = current_row;
									  notedata.channel = channel_idx;
									  notedata.note = note[0];
									  notedata.inst = note[1];
									  notedata.vol = note[2];
									  notedata.effect = note[3];
									  notedata.effparam = note[4];
									  notedata.timestamp = System.currentTimeMillis();
									  if (mc != null){
										  mc.doIt(this, notedata);
									  } 
									  
								  }
								  if(channel_idx == channels.length -1) {
									  //System.out.println(" ");
								  }

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        	  
                          
                          
                    
                          
                          // syphus - finished getting notedata...
                           
                          	

			switch( effect ) {
				case 0x0B:
					/* Pattern Jump.*/
					if( pattern_loop_count < 0 ) {
						next_sequence_index = effect_param;
						next_row = 0;
					}
					break;
				case 0x0D:
					/* Pattern Break.*/
					if( pattern_loop_count < 0 ) {
						next_sequence_index = current_sequence_index + 1;
						next_row = ( effect_param >> 4 ) * 10 + ( effect_param & 0x0F );
					}
					break;
				case 0x0E:
					/* Extended.*/
					switch( effect_param & 0xF0 ) {
						case 0x60:
							/* Pattern loop.*/
							if( ( effect_param & 0x0F ) == 0 ) {
								/* Set loop marker on this channel. */
								channels[ channel_idx ].pattern_loop_row = current_row;
							}
							if( channels[ channel_idx ].pattern_loop_row < current_row ) {
								/* Marker and parameter are valid. Begin looping. */
								if( pattern_loop_count < 0 ) {
									/* Not already looping, begin. */
									pattern_loop_count = effect_param & 0x0F;
									pattern_loop_channel = channel_idx;
								}
								if( pattern_loop_channel == channel_idx ) {
									/* Loop in progress on this channel. Next iteration. */
									if( pattern_loop_count == 0 ) {
										/* Loop finished. */
										/* Invalidate current marker. */
										channels[ channel_idx ].pattern_loop_row = current_row + 1;
									} else {
										/* Count must be higher than zero. */
										/* Loop and cancel any breaks on this row. */
										next_row = channels[ channel_idx ].pattern_loop_row;
										next_sequence_index = current_sequence_index;
									}
									pattern_loop_count -= 1;
								}
							}
							break;
						case 0xE0:
							/* Pattern delay.*/
							tick_counter += ticks_per_row * ( effect_param & 0x0F );
							break;
					}
					break;
				case 0x0F:
					/* Set Speed/Tempo.*/
					if( effect_param < 32 ) {
						set_speed( effect_param );
						tick_counter = ticks_per_row;
					} else {
						if (!overridetempo) {
							set_tempo( effect_param );
						}
					}
					break;
				case 0x25:
					/* S3M Set Speed.*/
					set_speed( effect_param );
					tick_counter = ticks_per_row;
					break;
			}

                   

		}
		
		return song_end;
	}
	
	
	public void set_global_volume( int volume ) {
		if( volume < 0 ) {
			volume = 0;
		}
		if( volume > 64 ) {
			volume = 64;
		}
		global_volume[ 0 ] = volume;
	}

	public void set_speed( int speed ) {
		if( speed > 0 && speed < 256 ) {
			ticks_per_row = speed;
		}
	}

	public void set_tempo( int bpm ) {
		if( bpm > 31 && bpm < 256 ) {
			tick_length_samples = ( sampling_rate * 5 ) / ( bpm * 2 );
		}
	}	

	private void volume_ramp() {
		int ramp_idx, next_idx, ramp_end;
		int volume_ramp_delta, volume, sample;
		sample = 0;
		volume_ramp_delta = FP_ONE / volume_ramp_length;
		volume = 0;
		ramp_idx = 0;
		next_idx = 2 * tick_length_samples;
		ramp_end = volume_ramp_length * 2 - 1;
		while( ramp_idx <= ramp_end ) {
			sample = volume_ramp_buffer[ ramp_idx ] * ( FP_ONE - volume ) >> FP_SHIFT;
			mixing_buffer[ ramp_idx ] = sample + ( mixing_buffer[ ramp_idx ] * volume >> FP_SHIFT );
			volume_ramp_buffer[ ramp_idx ] = mixing_buffer[ next_idx + ramp_idx ];
			sample = volume_ramp_buffer[ ramp_idx + 1 ] * ( FP_ONE - volume ) >> FP_SHIFT;
			mixing_buffer[ ramp_idx + 1 ] = sample + ( mixing_buffer[ ramp_idx + 1 ] * volume >> FP_SHIFT );
			volume_ramp_buffer[ ramp_idx + 1 ] = mixing_buffer[ next_idx + ramp_idx + 1 ];
			volume += volume_ramp_delta;
			ramp_idx += 2;
		}
	}
	
	private void clear_vol_ramp_buffer() {
		int ramp_idx, ramp_end;
		ramp_idx = 0;
		ramp_end = volume_ramp_length * 2 - 1;
		while( ramp_idx <= ramp_end ) {
			volume_ramp_buffer[ ramp_idx ] = 0;
			ramp_idx += 1;
		}
	}
	
	
}

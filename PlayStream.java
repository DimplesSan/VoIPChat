/*
 * PlayStream.java
 *
 * Version:
 *     1.0
 *
 * Revisions:
 *     0
 */
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

import javax.sound.sampled.*;




/**
 * Class used to create the instance of a task to play the audio segment
 * that was received over the network. This task is then supplied to a thread for 
 * concurrent execution.
 *
 * @author      Siddharth Kalluru
 * @author      Vijaykumar Chandrashekar
 */
public class PlayStream implements Runnable{

	boolean stopPlayback;
	
	DatagramSocket objStreamSock;
	
	AudioFormat objAudioFormat;
	

	
	
	/**
	 * Constructor to initialize the instance of HostUI
	 *
	 * @param       objStreamSock    Datagramsocket to receive the voice stream 
	 * 
	 */
	public PlayStream(DatagramSocket objStreamSock) {
		
		this.objStreamSock = objStreamSock;
		stopPlayback = false;
		
		//Format of audio to played - Sampled @ 16Khz, SampleSize(PCM), Mono
		objAudioFormat = new AudioFormat(16000.0f, 16, 1, true, true);
	}
	
	
	
		
	@Override
	public void run() {
	

		
		try{
				
				DatagramPacket recPkt;
				
				InputStream ipStreamFrmDataPkt;
				AudioInputStream audioIpStreamFrmDataPkt;
				
				DataLine.Info objLineInfo;
				SourceDataLine objSpeaker;
				
				int i=0;
				System.out.println("Stream playback begun.");
				
				//Repeat till stop flag is set
				while(!stopPlayback){
					
						//Receive packet - blocks till the packet is received
						recPkt = new DatagramPacket(new byte[3000], 3000);
						this.objStreamSock.receive(recPkt);
						
						System.out.println("Stream packet "+ ++i + "received");
						
						//Extract data into a stream from the received pkt
				        ipStreamFrmDataPkt = new ByteArrayInputStream(recPkt.getData());
				        
				        //Wrap the stream into an Audio Stream
				        audioIpStreamFrmDataPkt = new AudioInputStream(ipStreamFrmDataPkt,
				        										objAudioFormat, 
				        										recPkt.getData().length / objAudioFormat.getFrameSize());
				        
				        objAudioFormat = audioIpStreamFrmDataPkt.getFormat();
				        
				        objLineInfo = new DataLine.Info(SourceDataLine.class, objAudioFormat);
	
				        int cnt = 0;
				        byte tempBuffer[] = new byte[10000];
				        try {
				        	
				            objSpeaker = (SourceDataLine) AudioSystem.getLine(objLineInfo);
				            objSpeaker.open(objAudioFormat);
				            objSpeaker.start();
				            
				            while ((cnt = audioIpStreamFrmDataPkt.read(tempBuffer, 0,tempBuffer.length)) != -1) {
				                if (cnt > 0) {
				                	
				                	//Write into the buffer of the speaker, that'll 
				                	//eventually be played
				                    objSpeaker.write(tempBuffer, 0, cnt);
				                }
				            }
				            
				            //Empty the buffer of the speaker
				            objSpeaker.drain();
				            objSpeaker.close();
					
				        }
				        catch(LineUnavailableException | IOException e1){
				        	e1.printStackTrace();
			            }
			
			
				}
				System.out.println("Stream playback halted.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		

	}
	
	
}

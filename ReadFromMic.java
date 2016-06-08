/*
 * ReadFromMic.java
 *
 * Version:
 *     1.0
 *
 * Revisions:
 *     0
 */
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;




/**
 * Class used to create the instance of a task to read the input
 * from the microphone. This task is then supplied to a thread for 
 * concurrent execution.
 *
 * @author      Siddharth Kalluru
 * @author      Vijaykumar Chandrashekar
 */
public class ReadFromMic implements Runnable{

	boolean readFlag;
	
	InetAddress ipOfServer;
	int portOfStreamOnServer;
	
	DatagramSocket objStreamSock;
	
	ByteArrayOutputStream bytArrOpStream;
	
	AudioFormat objAudioFormat;
	
	
	
	
	/**
	 * Constructor to initialize the instance of HostUI
	 *
	 * @param       objStreamSock    Datagramsocket to stream voice to remote host
	 * 
	 * @param       ip    ip address of the remote host
	 * 
	 * @param		portToSendStream		data-gram socket to stream voice 
	 * 										to remote host 
	 * 
	 * @exception 	UnknownHostException
	 */
	ReadFromMic(DatagramSocket objStreamSock, InetAddress ip, int portToSendStream) 
			throws UnknownHostException{
		
		ipOfServer = ip;
		this.portOfStreamOnServer = portToSendStream;
		
		this.objStreamSock = objStreamSock;
		
		readFlag = true;
		bytArrOpStream = new ByteArrayOutputStream();
		
		//Format of audio recorded - Sampled @ 16Khz, SampleSize(PCM), Mono
		objAudioFormat = new AudioFormat(16000.0f, 16, 1, true, true); 
	}
	
	
	
	
	
	@Override
	public void run() {

		
		TargetDataLine objMic;
		
		try{
			
			objMic = AudioSystem.getTargetDataLine(objAudioFormat);
			DataLine.Info objInfo = new DataLine.Info(TargetDataLine.class, objAudioFormat);
			
			objMic = (TargetDataLine) AudioSystem.getLine(objInfo);
			
			objMic.open(objAudioFormat,16000); //Prep the buffer to accept data from mic
			
			int packetSize = 3000;
			System.out.println("Mic Buffer size: " + objMic.getBufferSize());
			byte [] arrBuffer = new byte[objMic.getBufferSize() / 5];
			int  numOfBytesRead = 0, totalBytesRead = 0;
			objMic.start();	//start Recording
			
			
			
			int i=0;
			System.out.println("Mic recording begun.");
			
			//Read till read flag is set to false
			while(readFlag){
				
				//Read bytes from buffer of Mic
				//equal to the packet size
				numOfBytesRead = objMic.read(arrBuffer, 0, packetSize);
				totalBytesRead = totalBytesRead + numOfBytesRead;
				
                bytArrOpStream.write(arrBuffer, 0, numOfBytesRead);	//Writing to op stream
                
                //Add to data gram packet and send it
                objStreamSock.send(new DatagramPacket(arrBuffer, arrBuffer.length, 
                								      ipOfServer, portOfStreamOnServer));
                
                System.out.println("Stream packet : "+ ++i + "sent");
			}
			
			
			objMic.drain();
			objMic.close();
			
			System.out.println("Recording stopped.");
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

		
	}

}

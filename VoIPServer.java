/*
 * VoIPServer.java
 *
 * Version:
 *     1.0
 *
 * Revisions:
 *     0
 */
import java.io.IOException;

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;




/**
 * Class used to create the instance of the server side of the VoIP
 * application.
 *
 * @author      Siddharth Kalluru
 * @author      Vijaykumar Chandrashekar
 */
public class VoIPServer{
	
	
	
	
	int tcpPort, streamPort;
	
	ServerSocket objServSock;
	DatagramSocket objUDPSock;
	
	
	
	
	/**
	 * Constructor to initialize the instance of VoIPServer
	 *
	 * @param       servPort    Port of Server to be used for chat and exchange
	 * 							of control messages
	 * 
	 * @param       streamPort    Port to be used for streaming of voice
	 */
	public VoIPServer(int servPort, int streamPort) throws IOException{
		
		this.tcpPort = servPort;
		this.streamPort = streamPort;
		
		//Create server obj and bind to a port
		objServSock = new ServerSocket(tcpPort);
		
		//Create a DatagramSocket for the streams
		objUDPSock = new DatagramSocket(streamPort);
	}
	
	
	
	
  /**
   * The main program.
   *
   * @param    args    Port Number to start the VoIP Server
   */
	public static void main(String [] args){
		
		if(args.length == 1){
			try{
				
				int servPort = Integer.parseInt(args[0]);
				int streamPort = servPort+1;
				
				try{
					
					VoIPServer objVoIPSever = new VoIPServer(servPort, streamPort);
					System.out.println("Server is waiting for connections on port: " + servPort); 
					
					Socket objSock = objVoIPSever.objServSock.accept();
					System.out.println("Client connected.");
					
					//TO DO: Make client multi-threaded, if needed
					handleClient(objSock, objVoIPSever.objUDPSock);	
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			catch(NumberFormatException nfe){
				
				System.out.println("NFE - Usage: Please run the server side application as follows\n"+
						  "java VoIPServer <port of server>");
			}

		}
		else
			System.out.println("Usage: Please run the server side application as follows\n"+
					  "java VoIPServer <port of server>");
		
		
	}
	
	
	
	
	/**
	 * A description of what the method does
	 *
	 * @param       objSock    A socket connection to the connected client
	 * @param       objUDPSock    A UDP socket to be used for streaming voice to 
	 * 							  the connected client
	 * 
	 * @exception   IOException    
	 * @exception   LineUnavailableException    
	 * @exception   ClassNotFoundException
	 * @exception   UnsupportedAudioFileException    
	 */
	public static void handleClient(Socket objSock, DatagramSocket objUDPSock) throws IOException, 
														   LineUnavailableException, 
														   UnsupportedAudioFileException, ClassNotFoundException{
		
		HostUI clientUI =  new HostUI("Server", objSock, objUDPSock, 
				               objSock.getInetAddress(), objUDPSock.getLocalPort());
		
		//Draw the UI
		clientUI.drawUI("Connected to host.\n");
	}
	
	
	
	
}

/*
 * VoIPClient.java
 *
 * Version:
 *     1.0
 *
 * Revisions:
 *     0
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;




/**
 * Class used to create the instance of the client side of the VoIP
 * application.
 *
 * @author      Siddharth Kalluru
 * @author      Vijaykumar Chandrashekar
 */
public class VoIPClient{
	
	
	
	
	public Socket objSock;	//Socket to establish connection with server
	public DatagramSocket objDGSock; //Socket to stream the voice
	
	//Server Details 
	public InetAddress ipAddrOfServer;	
	public int portOfServer;
	public int streamPort;
	
	//I/O parameters
	public BufferedInputStream objBIS;
	public BufferedOutputStream objBOS;
	
	
	AudioFormat objAudioFormat;
	
	
	
	
	/**
	 * Constructor to initialize the instance of VoIPClient
	 *
	 * @param       ipOfServer    IP address of Server
	 * 
	 * @param       portOfServer    Port of Server to be used for chat and
	 * 								exchange of control messages
	 * 
	 * @param       streamPort    Port to be used for streaming of voice
	 */
	public VoIPClient(String ipOfServer, int portOfServer,
					  int streamPort) throws UnknownHostException, IOException{
		
		this.ipAddrOfServer = InetAddress.getByName(ipOfServer);
		this.portOfServer = portOfServer;
		this.streamPort = streamPort;
		
		//Create client socket to connect server  
		objSock = 	new Socket(ipOfServer, portOfServer);
		
		//Bind the client to the stream port to receive and stream voice
		objDGSock = new DatagramSocket(streamPort);
		
		//Get the channels to send data
		objBIS = new BufferedInputStream(objSock.getInputStream());
		objBOS = new BufferedOutputStream(objSock.getOutputStream());
	}
	
	
	
	
  /**
   * The main program.
   *
   * @param    args    IPaddress of Server and Port of Server
   */
	public static void main(String [] args){

		
		if(args.length == 2){
			
			String ipOfServer = args[0];
			int portOfServer = Integer.parseInt(args[1]);
			int streamPort = portOfServer+1;
			
			try{
				
				//Create the client and connect to the server
				VoIPClient objClient = new VoIPClient(ipOfServer, portOfServer, streamPort);
				HostUI clientUI =  new HostUI("Client", objClient.objSock, objClient.objDGSock, 
						                      objClient.ipAddrOfServer, streamPort);
				
				//Draw the UI
				clientUI.drawUI("Connected to host.\n");	
		
			}catch(Exception e){
				System.out.println(" Couldn't connect to host with IP address: "+ipOfServer + 
						           " and port :" + portOfServer );
			}
			
		}
		else
			System.out.println("Usage: Please run the client side application as follows\n"+
							  "java VoIPClient <ip address of server> <port of server>");
	}
	

	
	
}

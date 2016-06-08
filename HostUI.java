/*
 * HostUI.java
 *
 * Version:
 *     1.0
 *
 * Revisions:
 *     0
 */
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;




/**
 * Class used to create the UI for the hosts of VoIP application.
 *
 * @author      Siddharth Kalluru
 * @author      Vijaykumar Chandrashekar
 */
public class HostUI {
	
	
	
	
	Socket objSock;
	DatagramSocket objStreamSock;
	InetAddress ipOfServer;
	int portOfStreamOnServer;
	
	
	ReadFromMic recordAndStreamSpeech;
	PlayStream receivedStream;
	
	String title;
	PrintWriter objPW;
	
	JFrame objFrame;
	JPanel objMainPanel;
	
	JTextField textBox;
	JTextArea textArea;
	JButton sendBtn, startCallBtn, endCallBtn;
	
	
	
	
	/**
	 * Constructor to initialize the instance of HostUI.
	 *
	 * @param       title    title of the window
	 * 
	 * @param       objSock    socket connection to the remote host
	 * 
	 * @param		objStreamSock		data-gram socket to stream voice 
	 * 									to remote host 
	 * 
	 * @param		ipOfServer			IP address of remote host
	 * 
	 * @param		portOfStreamOnServer		TCP Port of remote host
	 * 
	 */
	public HostUI(String title, Socket objSock, DatagramSocket objStreamSock,
				  InetAddress ipOfServer, int portOfStreamOnServer) throws UnknownHostException{
		
		this.title  = title;
		
		this.objSock = objSock;
		this.objStreamSock = objStreamSock;
		this.ipOfServer = ipOfServer;
		this.portOfStreamOnServer = portOfStreamOnServer;
		
		try{
			objPW = new PrintWriter(objSock.getOutputStream());
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
	 * A function that draws the UI for the host
	 *
	 * @param       defMsg    Initial Message that'll be displayed 
	 * 						  on the UI
	 * 
	 */
	public void drawUI(String defMsg){
		 
		objFrame  =new JFrame(title);
		objMainPanel = new JPanel();
		
		
		//Draw the text area to display the incoming 
		//messages and is non editable 
		textArea = new JTextArea(15,50);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setText(defMsg);
		textArea.setEditable(false);
		
		//Make the text area scrollable
		JScrollPane objScroller = new JScrollPane(textArea);
		objScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		objScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//Have a text box to receive input
		textBox = new JTextField(20);
		
		//Add Send, Start Call, End Call buttons
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(new SendBtnListener());
		
		startCallBtn = new JButton("Start Call");
		startCallBtn.addActionListener(new BeginCallBtnListener());
		
		endCallBtn = new JButton("End Call");
		endCallBtn.addActionListener(new EndCallBtnListener());
		endCallBtn.setEnabled(false); //Initially false
		
		//Add all the components to the Main Panel
		objMainPanel.add(objScroller);
		objMainPanel.add(textBox);
		objMainPanel.add(sendBtn);
		objMainPanel.add(startCallBtn);
		objMainPanel.add(endCallBtn);
		
		//Set the attributes of the frame
		objFrame.getContentPane().add(BorderLayout.CENTER, objMainPanel);
		objFrame.setSize(600, 300);
		objFrame.setResizable(false);	//Disable maximize
		objFrame.setVisible(true);	//Display the frame
		
		//Cleanup on close
		objFrame.addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		         //Send message to End Session
		    	objPW.println("<EndSession>");
		    	objPW.flush();
		    	
		    }
		});

		
		//Start a thread to populate the txt area with the messages 
		// received from the other host
		try{
			new Thread(new getMessages(objSock, textArea, this)).start();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
	 * Inner class to listen to the click event on 'End Call' button
	 *
	 * @author      Siddharth Kalluru
	 * @author      Vijaykumar Chandrashekar
	 */
	class EndCallBtnListener implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			//End own call 
			endCall();
			
			//Ask the host to end the call
			objPW.println("<EndCall>");
			objPW.flush();
		}
		
	}
	
	
	
	
	/**
	 * Inner class to listen to the click event on 'Begin Call' button
	 *
	 * @author      Siddharth Kalluru
	 * @author      Vijaykumar Chandrashekar
	 */
	class BeginCallBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			//Ask the host to initate a call
			objPW.println("<InitateCall?>");
			objPW.flush();
		}
		
	}
	
	
	
	
	/**
	 * Inner class to listen to the click event on 'Send' button
	 *
	 * @author      Siddharth Kalluru
	 * @author      Vijaykumar Chandrashekar
	 */
	class SendBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			objPW.println(textBox.getText());
			objPW.flush();
			//
			textBox.setText("");
			textBox.requestFocus();
		}
		
	}
	
	
	
	
	/**
	 * A function that is called to begin the voice call if any of the two 
 	 * hosts decides to begin the voice call
	 */
	public void endSession(){
		
    	try {
    		
	    	//Close the network connections
	    	objStreamSock.close();
			objSock.close();
			
			//Hide the frame
			objFrame.setVisible(false);
			
			//Destroy the UI
			objFrame.dispose();
			//
			System.exit(0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * A function that is called to end the voice call if any of the two 
 	 * hosts decides to end the voice call
	 */
	public void endCall(){
		
		//Kill the threads
		recordAndStreamSpeech.readFlag = false;
		receivedStream.stopPlayback = true;
		
		//Disable the end call button and enable the start call button
		endCallBtn.setEnabled(false);
		startCallBtn.setEnabled(true);
		
	}
	
	
	
	
	/**
	 * A function that is called to set up the voice call if the remote 
	 * host has replied positively to the query of a voice call
	 *
	 *	@param		dgs		Datagram socket to stream voice
	 *	
	 *	@param		ipAddrOfServer		IP address of the remote host
	 *
	 *	@param		streamPort		port of the remote host
	 * 
	 */
	public void beginCall(DatagramSocket  dgs, InetAddress ipAddrOfServer, int streamPort){
		
		try {
			
			//Create the tasks to read from the mic and write out to the speaker
			recordAndStreamSpeech = new ReadFromMic(dgs, ipAddrOfServer, streamPort);
			receivedStream = new PlayStream(dgs);
	
			//start the threads
			new Thread(recordAndStreamSpeech).start();
			new Thread(receivedStream).start();
		
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
	}

	
	

}





/**
 * Class to handle the receipt of Control messages and plain messages
 * Plain messages are redirected to the text area and displayed in the UI
 * 
 * Actions are taken based on the type of control messages. The 3 main control
 * messages are 
 * 		1) <InitiateCall?>	-	Indicates that the remote host wants to start a voice call
 * 			a) <Yes>		-	Response indicating that the local host is willing to take part
 * 								in the voice call
 * 			b) <No>			-	Response indicating that the local host isn't willing to take part
 * 								in the voice call
 * 
 * 		2) <EndCall>	-	Indicates that the streaming and the reception of the audio stream
 * 							needs to be stopped
 * 
 *		3) <EndSession>	- 	Indicates that the session has come to an end and the application
 *							needs to be closed
 * @author      Siddharth Kalluru
 * @author      Vijaykumar Chandrashekar
 */
class getMessages implements Runnable{
	
	String msgs;
	BufferedReader objBR;
	HostUI hostUI;
	JTextArea msgBoard;
	
	public getMessages(Socket objSock, JTextArea textArea, HostUI hostUI) throws IOException{
		
		objBR = new BufferedReader(new InputStreamReader(objSock.getInputStream()));
		msgBoard = textArea;
		this.hostUI= hostUI;
	}
	
	
	@Override
	public void run(){
		
		try{
			
			while((msgs = objBR.readLine()) != null){
				
				//If <Initiate Call> Control Message then 
				//fire a dialog
				if(msgs.equalsIgnoreCase("<InitateCall?>")){
					
					//If OK Clicked on the dialog then 
					//being call
				    int respFromUser = JOptionPane.showConfirmDialog(null, 
				    								"Host wants to have a voice call. Do you want to continue?", "Initate Call?",
				    								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				    
				    if(respFromUser == JOptionPane.YES_OPTION){
				    	
				    	//Send the response first
//				    	synchronized (hostUI.objPW) {
					    	hostUI.objPW.println("<Yes>");
					    	hostUI.objPW.flush();							
//						}
				    	hostUI.beginCall(hostUI.objStreamSock, hostUI.ipOfServer, hostUI.portOfStreamOnServer);
				    	
						//Disable the start button
						hostUI.startCallBtn.setEnabled(false);
						hostUI.endCallBtn.setEnabled(true);
						
				    }
				    else{
				    	hostUI.objPW.println("<No>");
				    	hostUI.objPW.flush();
				    }
				    	
				}
				else if(msgs.equalsIgnoreCase("<Yes>")){
					
					hostUI.beginCall(hostUI.objStreamSock,hostUI.ipOfServer,hostUI.portOfStreamOnServer);
					
					//Disable the start button
					hostUI.startCallBtn.setEnabled(false);
					hostUI.endCallBtn.setEnabled(true);
				}
				else if(msgs.equalsIgnoreCase("<No>")){
					
					//Dialog that host declined the call
					JOptionPane.showMessageDialog(hostUI.objFrame, "Host declined call");
					
					hostUI.startCallBtn.setEnabled(true);
					hostUI.endCallBtn.setEnabled(false);
				}
				else if(msgs.equalsIgnoreCase("<EndCall>")){
					
					hostUI.endCall();
				}
				else if(msgs.equalsIgnoreCase("<EndSession>")){
					break;
				}
				else
					//Print messages to the text area
					msgBoard.append(msgs + "\n");
				
			}
			
			System.out.println("Input Stream closed");
			
			//End Session
			hostUI.endSession();
			
			
		}catch(IOException e)
		{
//			e.printStackTrace();
		}
		
	}
	
	
}









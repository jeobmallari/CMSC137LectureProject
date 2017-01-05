/*
	Mallari, Jeob Ervin N.
	CMSC 137 Lecture Project 1
	Client

	Type: java Client <data to send> to run
*/
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.lang.Thread;
import java.util.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;

public class Client extends Thread implements Runnable{
	
	DatagramSocket socket;
	DatagramPacket packet;
	String toSend;
	boolean isConnected = false;
	boolean endreached = false;
	String[] parameters;
	int index = 0;
	int winSize;
	int current;
	int timeout;

	public final String approved = "SYNACK";
	public final String established = "EST";
	public final String ack = "CONTINUE";
	public final String close = "CLOSE";
	public final String last = "LAST";

	public Client(String[] params) throws SocketException{
		socket = new DatagramSocket(4444);
		socket.setSoTimeout(5000);
		this.parameters = params;
		this.setParameters();
	}

	public void setParameters(){	// function to retrieve the arguments in execution.
		// if parameters are more than 1, this means there are spaces in the messasge
		// that is why we add space every after word appended to "toSend"
		this.toSend = "";
		for(int i=0;i<parameters.length;i++){
			this.toSend += parameters[i];
			this.toSend += " ";
		}
	}

	public void sendMsg(String message){		// sends specified messages using the UDP protocol
		byte[] msg = message.getBytes();
		InetAddress ip = null;
		try{
			ip = InetAddress.getByName("127.0.0.1");
		} catch(UnknownHostException e){}
		packet = new DatagramPacket(msg, msg.length, ip, 8080);
		try{
			socket.send(packet);
		} catch(IOException e){
			System.out.println("Error in sending.");
		}
	}

	public String getNextBit(int window, int curr){		// this function calculates the next window frame of the data
		if((curr+window) < this.toSend.length()){		// and returns the string to be sent.
			this.current = curr+window;
			return this.toSend.substring(curr, curr+window);
		}
		else {
			this.endreached = true;
			return this.toSend.substring(curr, this.toSend.length());
		}
	}

	public boolean parseData(String string){			// received data is processed here.
		string = string.trim();
		// System.out.println("String: "+string);
		if(string.startsWith(this.approved)){
			// client wants to connect
			System.out.println("Client has reached a server.");
			System.out.println("Attempting to Connect...");
			String msg = "ack";
			sendMsg(msg);
			System.out.println("Connection acknowledgement sent.");
			return true;
		}
		else if(string.startsWith(this.established)){
			System.out.println("Connection has been established!");
			System.out.println("Now sending data according to received window size...");
			String[] decrypts = string.split("|");
			
			this.winSize = Integer.parseInt(decrypts[decrypts.length-1]);
			String nowSending = getNextBit(this.winSize, this.current);
			String msg = "data|"+nowSending;
			System.out.println("Now Sending: "+nowSending);
			sendMsg(msg);
			return true;
		}
		else if(string.startsWith(this.ack) && !this.endreached){
			System.out.println("\nSending continuation...");
			String[] decrypts = string.split("|");
			this.winSize = Integer.parseInt(decrypts[decrypts.length-1]);
			String nowSending = getNextBit(this.winSize, this.current);
			String msg = "data|"+nowSending;
			System.out.println("Now Sending: "+nowSending);
			sendMsg(msg);
			return true;
		}
		else if(string.startsWith(this.ack) && this.endreached){
			// client has no more data to send.
			// client will now send the fin bit
			System.out.println("End of data reached. Will now terminate the connection.");
			String msg = "dc|2";	// 4-way handshake disconnection counterpart of active closing
										/// FIN_WAIT_1
			System.out.println("Active closing...");
			sendMsg(msg);
			return true;
		}
		else if(string.equals(this.close)){
			try{
				this.sleep(2000);			/// FIN_WAIT_2
			} catch(InterruptedException e){
				System.out.println("Error in sleeping. FIN_WAIT_2");
			}
			return true;
		}
		else if(string.startsWith(this.last)){
			String[] decrypts = string.split("|");
			this.timeout = Integer.parseInt(decrypts[decrypts.length-1]);
			String msg = "end";
			sendMsg(msg);
			System.out.println("Shutting off...");
			return false;
		}
		return true;
	}

	public void run(){
		System.out.println("The client has started.");
		// Client is the connection initiator.
		// Client has to send first.
		while(true){
			try{
				this.sleep(2000);
			} catch(InterruptedException e){

			}
			if(isConnected){
				byte[] databyte = new byte[1024];
				packet = new DatagramPacket(databyte, databyte.length);			
				try{
					socket.receive(packet);
				} catch (Exception e) {
					System.out.println("Client Error in packet reception: "+e);
					e.printStackTrace();
				}
				String data = new String(packet.getData());
				if(!parseData(data)){
					break;
				}
			}

			else{			// this else part is used to start the conversation between the server and the client.
				this.isConnected = true;
				sendMsg("conn");
			}
		}
	}

	public static void main(String[] args){
		if(args.length < 1){
			System.out.println("How to run: java Client <message to send>");	// in case the parameter is not indicated
		}
		else{
			try{
				String[] params = args;
				Client c = new Client(params);
				c.start();
			} catch(SocketException e){
				System.out.println("Error in starting up.");
			}
		}
	}
}
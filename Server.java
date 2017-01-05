import java.lang.Thread;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.net.UnknownHostException;


public class Server extends Thread implements Runnable{
	
	String wholeMessage = "";
	DatagramSocket socket;
	DatagramPacket packet;
	int syncNum;
	int winSize = 3;
	int timeout;

	public final String connection = "conn";
	public final String data = "data";
	public final String end = "dc";
	public final String ack = "ack";
	public final String done = "end";

	public Server() throws SocketException{
		socket = new DatagramSocket(8080);		// server socket initialization
		socket.setSoTimeout(5000);
	}

	public void sendMsg(String message){		// this function sends the message using the UDP protocol
		byte[] msg = message.getBytes();
		InetAddress ip = null;
		try{
			ip = InetAddress.getByName("127.0.0.1");
			packet = new DatagramPacket(msg, msg.length, ip, 4444);
		} catch(UnknownHostException e){
			System.out.println("Tried to send: "+message+", "+message.length()+", "+ip+", 4444 but failed.");
		}
		try{
			// System.out.println("Tried to send: "+message+", "+message.length()+", "+ip+", 4444.");
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Error in line socket.send(packet)");
		}
	}

	public boolean parseData(String string){		// received data is processed here.
		string = string.trim();
		if(string.startsWith(connection)){
			// client wants to connect
			System.out.println("A client has arrived.");
			System.out.println("Sending synchronization acknowledgement...");
			String msg = "SYNACK";
			sendMsg(msg);
			System.out.println("Synchronization acknowledgement sent.\n-------------");
			return true;
		}
		else if(string.startsWith(ack)){
			// client has established connection
			System.out.println("A connection with the client has been established.\n-------------");
			String established = "EST|"+this.winSize;								// this sets the window size to be used by the sender. also acts as the first ack message
			sendMsg(established);
			return true;
		}
		else if(string.startsWith(data)){
			// client is sending data
			System.out.println("Client is now sending data.");
			String[] decrypts = string.split("|");
			String newmsg = "";
			for(int i=0;i<decrypts.length;i++){
				// System.out.print(decrypts[i]);
				if(i>4) newmsg += decrypts[i];
			}
			this.wholeMessage += newmsg;
			System.out.println("\nThis is what we have received so far: "+this.wholeMessage);
			System.out.println("Telling client that the message is received...");
			String msg = "CONTINUE|"+this.wholeMessage.length();							// this acts as an (ACK + n) for the window size
			// System.out.println("MSG: "+msg);
			sendMsg(msg);
			System.out.println("The message has been sent.\n-------------");
			return true;
		}
		else if(string.startsWith(end)){
			// client ends connection
			System.out.println("Server is active closing. Will give it permission.");
			String[] decrypts = string.split("|");
			this.timeout = Integer.parseInt(decrypts[decrypts.length-1]);
			String msg = "CLOSE";														// 4-way handshake disconnection counterpart for passive closing
			// sending ACK
			sendMsg(msg);
			try{
				this.sleep(1000);
			} catch(InterruptedException e){
				System.out.println("Error in this.sleep(1000)");
			}
			//	now sending the last ACK
			msg = "LAST|4";
			sendMsg(msg);
			return true;
		}
		else if(string.equals(this.done)){
			return false;
			// try{
			// 	this.join();	/// join() ends a thread
			// 	return false;
			// } catch(InterruptedException e){
			// 	System.out.println("Error in this.join()");
			// }
		}
		return true;
	}

	public void run(){
		System.out.println("The server has started.\nNow waiting for connection...\n---------------");
		while(true){
			try{
				this.sleep(2000);
			} catch(InterruptedException e){

			}
			byte[] databyte = new byte[1024];
			packet = new DatagramPacket(databyte, databyte.length);
			try{
				socket.receive(packet);
			} catch (Exception e) {
				System.out.println("Server Error in packet reception: "+e);
				e.printStackTrace();
			}
			String data = new String(packet.getData());
			if(!parseData(data)){
				break;
			}
		}
	}

	public static void main(String[] args){
		try{
			Server s = new Server();
			s.start();
		} catch(SocketException e){
			System.out.println("Problem in starting up");
		}
	}
}
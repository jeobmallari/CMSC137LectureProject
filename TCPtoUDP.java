/*
	tcp = readUTF/writeUTF
	udp = datagram packets
*/

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.InetAddress;

public class TCPtoUDP implements Runnable{
	Socket s;
	DataInputStream dis;
	DataOutputStream dos;
	Thread t = new Thread(this);
	Scanner sc = new Scanner(System.in);
	DatagramSocket socket = null;
	InetAddress serverAdd;
	String receivedString;
	int syncNum = 15231;
	int ackNum = 1253;
	String bit = "001";
	int windowSize = 3021;

	String toSend;
	public final static int PORT=8080;

	public TCPtoUDP(String server){
		try{
			this.serverAdd = InetAddress.getByName(server);
			socket = new DatagramSocket();
			socket.setSoTimeout(300);
			t.start();
		}catch(Exception e){
			System.out.println("Error in creating instance.");
			e.printStackTrace();
		}
	}

	public void send(String msg){
		try{
			msg += syncNum+","+ackNum+","+bit+","+windowSize+","+msg;
			byte[] buf = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, this.serverAdd, PORT);
			socket.send(packet);
		}catch(Exception e){
			System.out.println("Sending packet error.");
		}
	}

	public void run(){
		boolean flag = false;
		try{
			socket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] incomingData = new byte[1024];
            String msg = syncNum+","+ackNum+","+bit+","+windowSize+","+"This is a message from client.";
            byte[] data = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
            socket.send(sendPacket);
            System.out.println("Message sent from client");
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);
            String response = new String(incomingPacket.getData());
            System.out.println("Response from server:" + response);
            System.out.println("Acknowledgement received.");
		}
		catch(Exception e){
			//
		}
	}

	public static void main(String[] args){
		if (args.length != 1){
			System.out.println("Usage: java TCPtoUDP <server>");
			System.exit(1);
		}

		new TCPtoUDP(args[0]);
	}

}
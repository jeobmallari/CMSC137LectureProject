/*
	tcp = readUTF/writeUTF
	udp = datagram packets
*/

import java.net.Socket;
import java.net.ServerSocket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.InetAddress;
import java.io.IOException;

public class Project1Server implements Runnable{
	DatagramSocket socket;
	Thread t = new Thread(this);
	ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
	ArrayList<Integer> ports = new ArrayList<Integer>();
	String receivedData;

	public final static int PORT=4412;

	public Project1Server(){
		try{
			socket = new DatagramSocket(PORT);
			socket.setSoTimeout(1000);
			this.t.start();
		}catch(IOException ioe){
			System.err.println("Could not listen to port: "+PORT);
			System.exit(-1);
		}catch(Exception e){
			System.err.println("Error in creating server...");
		}
	}

	public void broadcast(String msg){
		for(int i=0;i<addresses.size();i++){
			send(addresses.get(i), ports.get(i), msg);
		}
	}

	public void send(InetAddress recipient, int port, String msg){
		DatagramPacket packet;
		byte[] buf = msg.getBytes();
		packet = new DatagramPacket(buf, buf.length, recipient, port);
	}

	public boolean alreadyExists(InetAddress input, int port){
		for(int i=0;i<addresses.size();i++){
			if(addresses.get(i).equals(input)){
				if(ports.get(i).equals(port)){
					return true;
				}
			}
		}
		return false;
	}

	public void run(){
		try {
		    socket = new DatagramSocket(9876);
		    byte[] incomingData = new byte[1024];
	        while (true) {
		        DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
	            socket.receive(incomingPacket);
	            String message = new String(incomingPacket.getData());
	            String[] values = message.split(",");
	            System.out.println("Sequence number:"+values[0]);
				System.out.println("Acknowledgement number:"+values[1]);
				System.out.println("ACK/SYN/FIN BIT:"+values[2]);
				System.out.println("Window size:"+values[3]);
	            System.out.println("Received message from client at address "+incomingPacket.getAddress()+" at port "+incomingPacket.getPort()+": " + values[4]);
	            InetAddress IPAddress = incomingPacket.getAddress();
	            int port = incomingPacket.getPort();
	            String reply = "Acknowledgement for the message";
	            byte[] data = reply.getBytes();
	            DatagramPacket replyPacket = new DatagramPacket(data, data.length, IPAddress, port);
	            socket.send(replyPacket);
	            Thread.sleep(2000);
	            socket.close();
	        }
		} catch (Exception e) {
		    //e.printStackTrace();
		}
	}

	public static void main(String[] args){
		if(args.length < 1){
			System.out.println("To run: java Project1Server <IP Address>");
		}
		else{
			new Project1Server();
		}
	}

}
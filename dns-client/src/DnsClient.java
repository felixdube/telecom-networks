/**
 * DnsClient.java
 * java DnsClient [-t timeout] [-r max-retries] [-p port] [-mx|-ns] @servername
 * 
 * @author Felix Dube, Ali Lashari
 * @since 2016-09-29
 *
 */

import java.io.*;
import java.net.*;

public class DnsClient {
	
	static byte[] server;
	static String name;
	static String recordType = "A";
	static Integer timeout = 5;
	static Integer maxRetries = 3;
	static Integer port = 53;
	
	public static void main(String[] args) throws Exception {
		
		//Parse user input
		for(String s: args){
			switch(s.substring(0, 1)){
				case "@":
					String serverName[] = s.split(" ");
					server = serverName[0].getBytes();
					name = serverName[1];
					break;
				
				case "-":
					switch(s.substring(1,3)){
					case "t ":
						timeout = Integer.parseInt(s.substring(3));
						break;
					
					case "r ":
						maxRetries =  Integer.parseInt(s.substring(3));
						break;
						
					case "p ":
						port = Integer.parseInt(s.substring(3));
						break;
						
					case "mx":
						recordType = s.substring(3);
						break;
						
					case "ns":
						recordType = s.substring(3);
						break;
						
					default:
						System.out.println("Invalid input!");
						return;
					}	
					break;
				
				default:
					System.out.println("Invalid input!");
					return;
			
			}
		}
		
		//TODO: create the data to be send according to the DNS request specification
		
		//create client socket
		DatagramSocket clientSocket = new DatagramSocket();
		
		InetAddress IPAddress = InetAddress.getByAddress(server);
		
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		
		//create datagram
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		
		//send datagram
		clientSocket.send(sendPacket);
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		//read datagram from server
		clientSocket.receive(receivePacket);
		
		String modifiedSentence = new String(receivePacket.getData());
		
		//TODO: parse packet received accoding to the lab specification
		
		
		
		
		
	}
}

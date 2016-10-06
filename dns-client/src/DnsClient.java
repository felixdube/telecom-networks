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
import java.util.Random;

public class DnsClient {
	

	static byte[] server;
	static String sn;
	static String name;
	static String recordType = "A";
	static Integer timeout = 5;
	static Integer maxRetries = 3;
	static Integer port = 53;
	static Integer i=0;
	static Integer p=1;
	static String[] labels; 
	static String[] serverS;
	static byte[] serverB;
	
	static int sendDataIndex = 0;
	static byte[] sendData = new byte[1024];
	static byte[] receiveData = new byte[1024];
	
	
	public static void main(String[] args) throws Exception {
		
		/************************************/
		/******** USER INPUT PARSING ********/
		/************************************/
		
		for(String s: args){
			i++;
			if (p==1){
			switch(s.substring(0, 1)){
				case "@":
					server = args[i-1].substring(1).getBytes();
					name = args[i];
					System.out.println(server);
					System.out.println(name);
					labels = name.split("\\.");
					for (int z=0; z<4; z++){
						serverB[z]=(byte)Integer.parseInt(serverS[z]);
						}
					break;
				
				case "-":
					switch(s.substring(1)){
					case "t":
						if (isNumeric(args[i])){
						timeout = Integer.parseInt(args[i]);
						//System.out.println(args[i]);
						}
						else{
							//System.out.println("Incorrect Input");
							p=0;
						}
						break;
					
					case "r":
						if (isNumeric(args[i])){
						maxRetries =  Integer.parseInt(args[i]);
						//System.out.println(maxRetries);
					}
					else{
						//System.out.println("Incorrect Input");
						p=0;
					}
						break;
						
					case "p ":
						if (isNumeric(args[i])){
						port = Integer.parseInt(args[i]);
						//System.out.println(port);
					}
					else{
						//System.out.println("Incorrect Input");
						p=0;
					}
						
						break;
						
					case "mx":
						recordType = "MX";
						//System.out.println(recordType);
						break;
						
					case "ns":
						recordType = "NS";
						//System.out.println(recordType);
						break;
						
					case "A":
						recordType = "A";
						//System.out.println(recordType);
						break;
						
					default:
					return;
					}	
					break;
					
				
				default:
					//System.out.println("Invalid input!");
					//return;
			
			}
			}
			else{
				break;
			}
		}
		
		
		/*************************/
		/********** HEADER *******/
		/*************************/
		/*
		 * 	Header 1
		 * 	ID			identifier						random	16bit
		 */
		byte[] ID = new byte[2];	//random
		Random r = new Random();
		r.nextBytes(ID);
		
		sendData[sendDataIndex++] = ID[0];
		sendData[sendDataIndex++] = ID[1];
		
		
		/*	Header 2
		 * 	QR			query (0) or response (1)?		0		1bit
		 * 	Opcode		kind of query? Standard (0)		0		1bit
		 * 	AA			authoritative response?			0		4bit
		 * 	TC			truncated?						0		1bit
		 * 	RD			recursion?						1		1bit
		 * 	RA			server support recursion?		0		1bit
		 * 	Z			for future use...				0		3bit
		 * 	RCODE		error at the server?			0		4bit
		 */
		byte[] H2 = {(byte) 1, 0};
		
		sendData[sendDataIndex++] = H2[0];
		sendData[sendDataIndex++] = H2[1];
		
		/*
		 * 	Header 3
		 * 	QDCOUNT		number of entries				1		16bit
		 */
		byte[] QDCOUNT = {0, (byte) 1};
		
		sendData[sendDataIndex++] = QDCOUNT[0];
		sendData[sendDataIndex++] = QDCOUNT[1];
		
		/*
		 * 	Header 4
		 * 	ANCOUNT		# resource records in answer	0		16bit
		 */
		byte[] ANCOUNT = {0,0};
		
		sendData[sendDataIndex++] = ANCOUNT[0];
		sendData[sendDataIndex++] = ANCOUNT[1];
		
		/*
		 * 	Header 5
		 * 	NSCOUNT		# name server resource records	0		16bit
		 */
		byte[] NSCOUNT = {0,0};
		
		sendData[sendDataIndex++] = NSCOUNT[0];
		sendData[sendDataIndex++] = NSCOUNT[1];
		
		/*
		 * 	Header 6
		 * 	ARCOUNT		# resource Additional records	0		16bit
		 */
		byte[] ARCOUNT = {0,0};
		
		sendData[sendDataIndex++] = ARCOUNT[0];
		sendData[sendDataIndex++] = ARCOUNT[1];
		
		
		
		
		
		/**************************/
		/******** QUESTION ********/
		/**************************/
		
		
		/*
		 * 	Question 1
		 * 	QNAME		domain name 				see below	N*16bit
		 */
		for(String s: labels){
			int labelLength = s.length();
			
			sendData[sendDataIndex++] = (byte) labelLength;
			
			for(int i = 0; i< labelLength; i++){
				sendData[sendDataIndex++] = (byte) s.charAt(i);
			}
			
			sendData[sendDataIndex++] = 0;
		}
		
		//TODO
		
		/*
		 * 	Question 2
		 * 	QTYPE		query type					see below	16bit
		 * 
		 * 	0x0001		type-A
		 * 	0x0002		type-NS
		 * 	0x000f		type-MX
		 */
		byte[] QTYPE = new byte[2];
		switch(recordType){
			case "A" :
				QTYPE[0] = 0;
				QTYPE[1] = (byte) 1;
				break;
			
			case "NS" :
				QTYPE[0] = 0;
				QTYPE[1] = (byte) 2;
				break;
			
			case "MX" :
				QTYPE[0] = 0;
				QTYPE[1] = (byte) 15;
				break;
		}
		
		sendData[sendDataIndex++] = QTYPE[0];
		sendData[sendDataIndex++] = QTYPE[1];
		
		/*
		 *	Question 3
		 *	QCLASS 		class of the query				1		16bit
		 *	
		 *	0x0001 means Internet address
		 */
		byte[] QCLASS = {0, (byte) 1};
		
		sendData[sendDataIndex++] = QCLASS[0];
		sendData[sendDataIndex++] = QCLASS[1];
		
		
		
		
		/*********************************/
		/******** SENDING PACKETS ********/
		/*********************************/
		
		//create client socket
		DatagramSocket clientSocket = new DatagramSocket();
		
		InetAddress IPAddress = InetAddress.getByAddress(server);
		
		//create datagram
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		
		//send datagram
		clientSocket.send(sendPacket);
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		//read datagram from server
		clientSocket.receive(receivePacket);
		
		String modifiedSentence = new String(receivePacket.getData());
		
		System.out.println(modifiedSentence);
		
		
		/** QUERY SUMMARY **/
		
		
		
		
		
		
		
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
}

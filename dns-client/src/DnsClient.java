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
	static byte[] serverB = new byte[4];
	
	static int sendDataIndex = 0;
	static byte[] sendData = new byte[1024];
	static byte[] receiveData = new byte[1024];
	static int Active=0;
	
	
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
					serverS= args[i-1].substring(1).split("\\.");
					name = args[i];
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
						}
						else{
							p=0;
						}
						break;
					
					case "r":
						if (isNumeric(args[i])){
						maxRetries =  Integer.parseInt(args[i]);
					}
					else{
						p=0;
					}
						break;
						
					case "p ":
						if (isNumeric(args[i])){
						port = Integer.parseInt(args[i]);
					}
					else{
						p=0;
					}
						
						break;
						
					case "mx":
						recordType = "MX";
						break;
						
					case "ns":
						recordType = "NS";
						break;
						
					case "A":
						recordType = "A";
						break;
						
					default:
					return;
					}	
					break;
					
				
				default:
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
			
		}
		
		sendData[sendDataIndex++] = 0;
		
		
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
		
		InetAddress IPAddress = InetAddress.getByAddress(serverB);
		
		//create datagram
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		
		//send datagram
		clientSocket.send(sendPacket);
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		
		//read datagram from server
		clientSocket.receive(receivePacket);
		
		//String modifiedSentence = new String(receivePacket.getData());
		
		//System.out.println(modifiedSentence);
		decode(receivePacket.getData());
		
		/** QUERY SUMMARY **/
		
				
	}
	
	
	public static void decode(byte[] temp){
		int Active=0;
	    
		StringBuilder output = new StringBuilder();
		
		//Extract packet ID
		StringBuilder IDs = new StringBuilder();
	    byte ID[]={temp[0],temp[1]};
		for (byte b : ID) {
	        IDs.append(String.format("%02X ", b));
	    }
		
		//Decode Packet Header
		int QR= getBit(7,temp[2],1);
		int OPCODE = getBit(3,temp[2],4);
		int AA = getBit(2,temp[2],1);
		String AAs;
		if (AA==1){
			AAs = "auth";
		} else{ AAs = "nonauth";}
		int TC = getBit(1,temp[2],1);
		int RD = getBit(0,temp[2],1);
		int RA = getBit(7,temp[3],1);
		int Z = getBit(4,temp[3],3);
		int RCODE = getBit(0,temp[3],4);
		int QDCOUNT = (temp[4] & 0xff << 8) + temp[5];
		int ANCOUNT = (temp[6] & 0xff << 8) + temp[7];
		int NSCOUNT = (temp[8] & 0xff << 8) + temp[9];
		int ARCOUNT = (temp[10] & 0xff << 8) + temp[11];
		
		//Decode Question
		Active=12;
		String[] QNAMEtemp = domExtract(temp,Active,'n');
		String QNAME = QNAMEtemp[0];
		Active= Integer.parseInt(QNAMEtemp[1]);
		int QTYPE= (temp[Active] & 0xff << 8) + temp[Active+1];
		Active+=2;
		int QCLASS = (temp[Active] & 0xff << 8) + temp[Active+1];
		Active+=2;

		
		//decode Answers
		if (ANCOUNT>0){
		output.append("***Answer Section (" + ANCOUNT+ " records)***"+ '\n');

		}
		for (int i =0; i<ANCOUNT; i++){
		String[] ttemp = domExtract(temp,Active,'n');
		String tempt= ttemp[0];
		Active = Integer.parseInt(ttemp[1]);
		int TYPE = (temp[Active] << 8) + temp[Active+1];
		Active+=2;
		int CLASS = (temp[Active] << 8) + temp[Active+1];
		Active+=2;
		long TTL = (temp[Active] & 0xFF << 24) + (temp[Active+1] & 0xFF << 16) +(temp[Active+2] & 0xFF << 8) + temp[Active+3] & 0xFF;	
		Active+=4;
		int RDLENGTH = (temp[Active] << 8) + temp[Active+1];
		Active+=2;
		if (TYPE==1){
			StringBuilder IP = new StringBuilder();
			for (int j =0; j<RDLENGTH;j++ ){
				IP.append((int) temp[Active+j] & 0xFF);
				IP.append(".");
			}
			Active+=RDLENGTH;
			output.append("IP" + '\t' + IP.deleteCharAt(IP.lastIndexOf(".")).toString() + '\t' + TTL + '\t' + AAs + '\n');

		}
		
		if (TYPE==2){
			ttemp = domExtract(temp,Active,'n');
			tempt= ttemp[0];
			output.append("NS" + '\t' + tempt +'\t' + TTL+'\t' + AAs+'\n');
			Active+=RDLENGTH;	
		}
		
		if (TYPE==15){
			int PREF = (temp[Active] & 0xFF << 8) + (temp[Active+1] & 0xFF);
			Active+=2;
			ttemp = domExtract(temp,Active,'n');
			tempt= ttemp[0];
			output.append("MX" + '\t' + tempt +'\t'+ PREF +'\t' + TTL+'\t' + AAs+'\n');
			Active+=RDLENGTH-2;
			}
		
		if(TYPE==5){
			ttemp = domExtract(temp,Active,'n');
			tempt= ttemp[0];
			output.append("CNAME" + '\t' + tempt +'\t' + TTL+'\t' + AAs+'\n');
			Active+=RDLENGTH;	
		}
		
		
		
		
		}
		System.out.println(output.toString());
		

		
	}
	
	
	public static int getBit(int position, byte SB, int Length){
		return  ((SB & 0xFF) >> position) & ((int) Math.pow(2, Length) - 1);
	}
	
	public static String[] domExtract (byte temp[],int Activet, char mode){
		
		StringBuilder domName = new StringBuilder();
		
		for (int i=Activet; i>1; i++){
			int c = (int) temp[i];
			if (c!=0){
				if ((c & 0xC0) == 0xC0){
					String t = domExtract(temp, (int) ((temp[i] & 0x3f)  << 8) + temp[i+1], 'p')[0];
					domName.append(t);
					if (mode=='n'){
						Active=i+2;
					}
					i=0;
				}
				else{
					for (int j=0; j<c; j++){
					domName.append((char) temp[i+j+1] ) ;
				}
			i=i+c;
				if (mode=='n'){
				Active=i+2;
				}
				domName.append(".");
				}
				
			}
			else{
				i=0;
			}
		}
		
		while ((domName.lastIndexOf(".")==domName.length()-1)){
			domName.deleteCharAt(domName.length()-1);
		}
String tempS[] = {domName.toString(),Integer.toString(Active)};
		return tempS;
		
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

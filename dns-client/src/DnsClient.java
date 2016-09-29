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
	
	static String server;
	static String name;
	static String recordType = "A";
	static Integer timeout = 5;
	static Integer maxRetries = 3;
	static Integer port = 53;
	
	public static void main(String[] args) {
		
		//Parse user input
		for(String s: args){
			switch(s.substring(0, 1)){
				case "@":
					String serverName[] = s.split(" ");
					server = serverName[0];
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
		
	
		
	}
}

package me.kutrumbos.examples;

import java.net.URISyntaxException;
import java.util.Observer;

import me.kutrumbos.DdpClient;

/**
 * Simple example of DDP client use-case that just involves 
 * 		making a connection to a locally hosted Meteor server
 * @author peterkutrumbos
 *
 */
public class DdpMultiClientTestExample {

	public static void main(String[] args) {
		
		// specify location of Meteor server (assumes it is running locally) 
		String meteorIp = "localhost";
		Integer meteorPort = 3000;

		try {
			
			// create DDP client instance
			DdpClient ddp = new DdpClient(meteorIp, meteorPort);
			
			// create DDP client observer
			Observer obs = new SimpleDdpClientObserver();
			
			// add observer
			ddp.addObserver(obs);
						
			// make connection to Meteor server
			ddp.connect();
			
			Thread.sleep(1000);
			
			ddp.subscribe("allPositions", new Object[]{});
					
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	
	
}

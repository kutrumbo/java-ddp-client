package me.kutrumbos.examples;

import java.util.Observable;
import java.util.Observer;

/**
 * Sample observer class that simply prints any responses from the Meteor server to the console
 * @author peterkutrumbos
 *
 */
public class SimpleDdpClientObserver implements Observer {

	@Override
	public void update(Observable client, Object msg) {

		if (msg instanceof String) {
			System.out.println("Received response: "+msg);
		}
		
	}

}

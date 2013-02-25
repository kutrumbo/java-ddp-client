package me.kutrumbos;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;

/**
 * A web-socket based Meteor DDP client
 * @author peterkutrumbos
 *
 */
public class DdpClient extends Observable {

	private int currentId;
	private final Map<Integer,String> identifiers;
	private final WebSocketClient wsClient;
	private final Gson gson = new Gson();
	
	private final static String DDP_PROTOCOL_VERSION = "pre1";
	
	/**
	 * Field names supported in the DDP protocol
	 *
	 */
	public enum DdpMessageField {
		MSG("msg"), ID("id"), METHOD("method"), PARAMS("params"), NAME("name"), SERVER_ID("server_id"),
		SESSION("session"), VERSION("version"), SUPPORT("support");
		
		private String fieldId;
		
		private DdpMessageField(String fieldId){
			this.fieldId = fieldId;
		}
		
		public String toString() {
			return this.fieldId;
		}
	}
	
	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port
	 *   (note: running Meteor locally will typically have a port of 3000 but 
	 *   	port 80 is the typical default for publicly deployed servers)
	 * @param meteorServerIp - IP of Meteor server
	 * @param meteorServerPort - Port of Meteor server, if left null it will default to 3000
	 * @throws URISyntaxException
	 */
	public DdpClient(String meteorServerIp, Integer meteorServerPort) throws URISyntaxException{
		if(meteorServerPort == null) meteorServerPort = 3000;
		String meteorServerAddress = "ws://"+meteorServerIp+":"+meteorServerPort.toString()+"/websocket";
		this.currentId = 0;
		this.identifiers = new HashMap<Integer,String>();
		this.wsClient = new WebSocketClient(new URI(meteorServerAddress)) {
			
			@Override
			public void onOpen(ServerHandshake handshakedata) {
				connectionOpened();
			}
			
			@Override
			public void onMessage(String message) {
				received(message);
			}
			
			@Override
			public void onError(Exception ex) {
				handleError(ex);
			}
			
			@Override
			public void onClose(int code, String reason, boolean remote) {
				connectionClosed(code, reason, remote);
			}
		};
	}
	
	/**
	 * Ran on initial web-socket connection, sends back a connection confirmation message to the Meteor server
	 */
	private void connectionOpened() {
		// reply to Meteor server with connection confirmation message ({"msg": "connect"})
		System.out.println("WebSocket connection opened");
		Map<DdpMessageField,Object> connectMsg = new HashMap<DdpMessageField,Object>();
		connectMsg.put(DdpMessageField.MSG, "connect");
		connectMsg.put(DdpMessageField.VERSION, DDP_PROTOCOL_VERSION);
		connectMsg.put(DdpMessageField.SUPPORT, new String[]{DDP_PROTOCOL_VERSION});
		send(connectMsg);
	}
	
	/**
	 * Ran when connection is closed
	 * @param code
	 * @param reason
	 * @param remote
	 */
	private void connectionClosed(int code, String reason, boolean remote) {
		// is this how onClose messages should be handled??
		String closeMsg = "WebSocketClient connection closed:"+code+", "+reason+", "+remote;
		System.out.println(closeMsg);
		received(closeMsg);
	}
	
	/**
	 * Error handling for any errors over the web-socket connection
	 * @param ex
	 */
	private void handleError(Exception ex) {
		String errorMsg = "WebSocketClient error: "+ex.getMessage();
		ex.printStackTrace();
		received(errorMsg);
	}
	
	/**
	 * Increments and returns the client's current ID
	 * @return
	 */
	private int nextId() {
		return ++currentId;
	}
	
	/**
	 * Registers a client DDP message by storing it in the identifiers map
	 * @param identifier
	 * @return
	 */
	private String registerIdentifier(String identifier) {
		Integer id = nextId();
		identifiers.put(id, identifier);
		return id.toString();
	}
	
	/**
	 * Initiate connection to meteor server
	 */
	public void connect() {
		this.wsClient.connect();
	}
	
	/**
	 * Call a meteor method with the supplied parameters
	 * @param method - name of corresponding Meteor method
	 * @param args - arguments to be passed to the Meteor method
	 */
	public void call(String method, Object[] params){
		Map<DdpMessageField,Object> callMsg = new HashMap<DdpMessageField,Object>();
		callMsg.put(DdpMessageField.MSG, "method");
		callMsg.put(DdpMessageField.METHOD, method);
		callMsg.put(DdpMessageField.PARAMS, params);
		
		String id = registerIdentifier("method,"+method+","+Arrays.toString(params));
		
		callMsg.put(DdpMessageField.ID, id);
		
		send(callMsg);
	}
	
	/**
	 * Subscribe to a Meteor record set with the supplied parameters
	 * @param name - name of the corresponding Meteor subscription
	 * @param params - arguments corresponding to the Meteor subscription
	 */
	public void subscribe(String name, Object[] params) {
		Map<DdpMessageField,Object> subMsg = new HashMap<DdpMessageField,Object>();
		subMsg.put(DdpMessageField.MSG, "sub");
		subMsg.put(DdpMessageField.NAME, name);
		subMsg.put(DdpMessageField.PARAMS, params);
		
		String id = registerIdentifier("sub,"+name+","+Arrays.toString(params));
		
		subMsg.put(DdpMessageField.ID, id);
		
		send(subMsg);
	}
	
	/**
	 * Un-subscribe to a record set
	 * @param name - name of the corresponding Meteor subscription
	 */
	public void unsubscribe(String name) {
		Map<DdpMessageField,Object> unsubMsg = new HashMap<DdpMessageField,Object>();
		unsubMsg.put(DdpMessageField.MSG, "unsub");
		unsubMsg.put(DdpMessageField.NAME, name);
		
		String id = registerIdentifier("unsub,"+name);
		
		unsubMsg.put(DdpMessageField.ID, id);
		
		send(unsubMsg);
	}
	
	/**
	 * Converts DDP-formatted message to JSON and sends over web-socket
	 * @param msg
	 */
	public void send(Map<DdpMessageField,Object> msg) {
		String json = gson.toJson(msg);
		this.wsClient.send(json);
	}

	/**
	 * Notifies observers of this DDP client of messages received from the Meteor server 
	 * @param msg
	 */
	public void received(String msg) {
		this.setChanged();
		this.notifyObservers(msg);
	}
	
}

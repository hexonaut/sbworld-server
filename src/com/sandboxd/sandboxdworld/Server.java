package com.sandboxd.sandboxdworld;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Server extends WebSocketServer {
	
	private static final String REQ_LOGIN = "login";
	private static final String REQ_ECHO = "echo";
	
	private static final String RESP_USERLIST = "users";
	private static final String RESP_USERJOIN = "userjoin";
	private static final String RESP_USERLEAVE = "userleave";
	private static final String RESP_ECHO = "echo";
	
	private final ConcurrentHashMap<WebSocket, User> users = new ConcurrentHashMap<WebSocket, User>();
	
	public Server (InetSocketAddress host) {
		super(host);
	}

	@Override
	public void onOpen (WebSocket ws, ClientHandshake handshake) {
		users.put(ws, new User(ws));
	}

	@Override
	public void onClose (WebSocket ws, int code, String reason, boolean remoteClosed) {
		User user = users.remove(ws);
		user.name = null;
		
		//Send a user leave notification
		String resp = RESP_USERLEAVE + "\n" + user.id;
		for (User i : users.values()) {
			if (i.name != null) i.ws.send(resp);
		}
	}

	@Override
	public void onError (WebSocket ws, Exception e) {
		
	}

	@Override
	public void onMessage (WebSocket ws, String message) {
		try {
			User user = users.get(ws);
			String[] data = message.split("\n");
			
			if (data[0].equals(REQ_LOGIN)) {
				user.name = data[1];
				user.male = data[2].equals("1");
				
				//Send user all other users
				String resp = RESP_USERLIST;
				resp += "\n" + user.id + "\n" + user.name + "\n" + (user.male ? "1" : "0");
				for (User i : users.values()) {
					if (i.name != null && i != user) resp += "\n" + i.id + "\n" + i.name + "\n" + (i.male ? "1" : "0");
				}
				ws.send(resp);
				
				//Send all other users a userjoin message
				resp = RESP_USERJOIN + "\n" + user.id + "\n" + user.name + "\n" + (user.male ? "1" : "0");
				for (User i : users.values()) {
					if (i.name != null && i != user) i.ws.send(resp);
				}
			} else if (data[0].equals(REQ_ECHO)) {
				for (User i : users.values()) {
					String resp = RESP_ECHO + "\n" + user.id;
					for (int o = 1; o < data.length; o++) {
						resp += "\n" + data[o];
					}
					if (i != user && i.name != null) {
						i.ws.send(resp);
					}
				}
			}
		} catch (Exception e) {
			//Unknown message
		}
	}
	
	public static void main (String[] args) {
		new Server(new InetSocketAddress(args[0], Integer.parseInt(args[1]))).run();
	}

}

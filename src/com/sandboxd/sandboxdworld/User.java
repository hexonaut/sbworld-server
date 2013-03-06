package com.sandboxd.sandboxdworld;

import java.util.concurrent.atomic.AtomicInteger;

import org.java_websocket.WebSocket;

public class User {
	
	private static final AtomicInteger NEXT_UID = new AtomicInteger(0);
	
	public final int id;
	public final WebSocket ws;
	public String name;
	public boolean male;
	
	public User (WebSocket ws) {
		this.id = NEXT_UID.getAndIncrement();
		this.ws = ws;
	}
	
}

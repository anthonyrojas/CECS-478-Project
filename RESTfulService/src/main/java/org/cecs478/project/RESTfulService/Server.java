package org.cecs478.project.RESTfulService;
import java.util.*;
public class Server {
	
	public Server(){
		
	}
	
	public List<Message> getAllMessages(){
		Message m1 = new Message("Hello World!");
		Message m2 = new Message("Goodbye!");
		List <Message> list = new ArrayList <Message>();
		list.add(m1);
		list.add(m2);
		return list;
	}
}

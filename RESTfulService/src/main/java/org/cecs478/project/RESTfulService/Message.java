package org.cecs478.project.RESTfulService;
import java.util.*;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message {
	private String message;
	
	public Message(){
		
	}
	public Message(String m){
		this.message=m;
	}
	
	public void setMessage(String message){
		this.message=message;
	}
	
	public String getMessage(){
		return message;
	}
}

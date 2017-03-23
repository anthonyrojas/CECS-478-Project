package org.cecs478.project.RESTfulService;
import java.util.*;
import java.io.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("messages")
public class MessageResource {
	
	Server msgService = new Server();
	
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public List<Message> getMessages(){
		return msgService.getAllMessages();
	}
}

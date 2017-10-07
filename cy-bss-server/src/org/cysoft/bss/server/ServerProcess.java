package org.cysoft.bss.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.model.Server;
import org.cysoft.bss.core.service.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class ServerProcess {
	private static final Logger logger = LoggerFactory.getLogger(ServerProcess.class);
	
	@Autowired @Lazy
	private ServerService serverService;
	
	private String nodeId;
	private Server server=null;
	
	private void init() throws CyBssException {
		logger.info("NodeId="+nodeId);
		
		server=serverService.getServer(nodeId);
		if (server==null){
			String msg="Server not found";
			logger.error(msg);
			throw new CyBssException(msg);
		}
		
		try {
			InetAddress inetAddr = InetAddress.getLocalHost();
			byte[] addr = inetAddr.getAddress();
	        String ipAddr = "";
	        for (int i = 0; i < addr.length; i++) 
	        	 ipAddr += (i>0?".":"")+(addr[i] & 0xFF);
	        
	        logger.info("IP Address:" + ipAddr);
	    
	        String hostname = inetAddr.getHostName();
	        logger.info("Machine:" + hostname);
		    
	        server.setIp(ipAddr);
	        server.setMachine(hostname);
	        serverService.update(server.getId(), server);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new CyBssException(e.getMessage());
		}
	}
	
	public void run(String... args) throws CyBssException{
		logger.info("ServerProcess.run() >>>");
		
		if (args.length!=1){
			logger.error("Number of parameters is wrong -> aspected NodeId");
			return;
		}
		
		nodeId=args[0];
		init();
		serverService.changeStatus(server.getId(), Server.STATUS_RUNNING);
		
		
		serverService.changeStatus(server.getId(), Server.STATUS_STOPPED);
		
		logger.info("ServerProcess.run() <<<");
	}
	
	
}

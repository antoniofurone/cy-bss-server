package org.cysoft.bss.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.common.CyBssUtility;
import org.cysoft.bss.core.model.Purchase;
import org.cysoft.bss.core.model.Sale;
import org.cysoft.bss.core.model.Server;
import org.cysoft.bss.core.model.ServerCommand;
import org.cysoft.bss.core.model.ServerQueueItem;
import org.cysoft.bss.core.service.CompanyService;
import org.cysoft.bss.core.service.InvoiceService;
import org.cysoft.bss.core.service.ObjectService;
import org.cysoft.bss.core.service.PersonService;
import org.cysoft.bss.core.service.PurchaseService;
import org.cysoft.bss.core.service.SaleService;
import org.cysoft.bss.core.service.ServerService;
import org.cysoft.bss.server.batch.Batch;
import org.cysoft.bss.server.batch.impl.SianOlio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class ServerProcess {
	private static final Logger logger = LoggerFactory.getLogger(ServerProcess.class);
	
	private static final String COMMAND_SHUTDOWN="shutdown";
	private static final String COMMAND_PAUSE="pause";
	private static final String COMMAND_RESTART="restart";
	
	private static final String RESULT_COMMAND_OK="OK";
	private static final String RESULT_COMMAND_NOK="NOK";
	
	@Autowired @Lazy
	private ServerService serverService;
	public ServerService getServerService(){
		return serverService;
	}
	
	@Autowired @Lazy
	private ObjectService objectService;
	public ObjectService getObjectService(){
		return objectService;
	}
	
	@Autowired @Lazy
	private CompanyService companyService;
	public CompanyService getCompanyService(){
		return companyService;
	}
	
	@Autowired @Lazy
	private PersonService personService;
	public PersonService getPersonService(){
		return personService;
	}
	
	@Autowired @Lazy
	private PurchaseService purchaseService;
	public PurchaseService getPurchaseService(){
		return purchaseService;
	}
	
	@Autowired @Lazy
	private SaleService saleService;
	public SaleService getSaleService(){
		return saleService;
	}
	
	@Autowired @Lazy
	private InvoiceService invoiceService;
	public InvoiceService getInvoiceService(){
		return invoiceService;
	}
	
	private String nodeId;
	
	private Server server=null;
	public Server getServer(){
		return server;
	}
	
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
		server.setStatus(Server.STATUS_RUNNING);
		serverService.changeStatus(server.getId(), Server.STATUS_RUNNING);
		
		ForkJoinPool qiPool = new ForkJoinPool(6);
		
		while (true){
			
			String now=CyBssUtility.dateToString(CyBssUtility.getCurrentDate(),CyBssUtility.DATE_yyyy_MM_dd_HH_mm_ss);
			List<ServerCommand> commands=serverService.getCommands(server.getId(),ServerCommand.STATUS_PENDING, 
					null, null, null, now);
			
			if (!commands.isEmpty())
				execCommand(commands.get(0));
			
			if (server.getStatus().equals(Server.STATUS_STOPPING)) {
				qiPool.isShutdown();
				while (qiPool.isTerminated()){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						serverService.changeStatus(server.getId(), Server.STATUS_STOPPED);
						logger.info("ServerProcess.run() <<<");
						return;
					}
				}
				break;
			}
			
			if (server.getStatus().equals(Server.STATUS_RUNNING)){
				now=CyBssUtility.dateToString(CyBssUtility.getCurrentDate(),CyBssUtility.DATE_yyyy_MM_dd_HH_mm_ss);
				List<ServerQueueItem> items=serverService.getQueueItems(0, ServerCommand.STATUS_PENDING, null, null, null, now);
				
				for(ServerQueueItem item:items){
					
					if (item.getObjectName().equals(Sale.ENTITY_NAME)){
						qiPool.execute(new SaleExecutor(this,item));
					}
					
					if (item.getObjectName().equals(Purchase.ENTITY_NAME)){
						qiPool.execute(new PurchaseExecutor(this,item));
					}
					
				}
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				serverService.changeStatus(server.getId(), Server.STATUS_STOPPED);
				logger.info("ServerProcess.run() <<<");
				return;
			}
		}
		
		serverService.changeStatus(server.getId(), Server.STATUS_STOPPED);
		logger.info("ServerProcess.run() <<<");
	}
	
	private void execCommand(ServerCommand command) throws CyBssException {
		
		logger.info("Exec ->"+command.getCommand());
		
		String result=RESULT_COMMAND_OK; 
		
		serverService.startCommand(command.getId());
		
		if (command.getCommand().equals(COMMAND_SHUTDOWN)){
			server.setStatus(Server.STATUS_STOPPING);
			serverService.changeStatus(server.getId(), Server.STATUS_STOPPING);
		}
		else
			if (command.getCommand().equals(COMMAND_PAUSE)){
				server.setStatus(Server.STATUS_PAUSE);
				serverService.changeStatus(server.getId(), Server.STATUS_PAUSE);
			}
			else
				if (command.getCommand().equals(COMMAND_RESTART)){
					server.setStatus(Server.STATUS_RUNNING);
					serverService.changeStatus(server.getId(), Server.STATUS_RUNNING);
				}
				else
					result=RESULT_COMMAND_NOK+": Invalid Command";
		
		serverService.endCommand(command.getId(),result);
	}
	
	
	private static String BATCH_SIAN_OLIO="SianOlio";
	
	public void runBatch(String... args) throws CyBssException{
		logger.info("ServerProcess.runBatch() >>>");
		
		if (args[1].equalsIgnoreCase((BATCH_SIAN_OLIO))){
			Batch batch=new SianOlio(this);
			batch.exec();
		}
		else
			logger.error("Invalid batch:"+args[1]);
		
		logger.info("ServerProcess.runBatch() <<<");
	}
}

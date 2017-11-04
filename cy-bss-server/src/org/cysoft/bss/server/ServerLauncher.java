package org.cysoft.bss.server;

import org.cysoft.bss.core.common.CyBssDataSource;
import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.dao.AppDao;
import org.cysoft.bss.core.dao.BillableDao;
import org.cysoft.bss.core.dao.CityDao;
import org.cysoft.bss.core.dao.CompanyDao;
import org.cysoft.bss.core.dao.ContactDao;
import org.cysoft.bss.core.dao.CountryDao;
import org.cysoft.bss.core.dao.CyBssAuthDao;
import org.cysoft.bss.core.dao.CyBssServiceDao;
import org.cysoft.bss.core.dao.FileDao;
import org.cysoft.bss.core.dao.InvoiceDao;
import org.cysoft.bss.core.dao.LanguageDao;
import org.cysoft.bss.core.dao.LocationDao;
import org.cysoft.bss.core.dao.MetricDao;
import org.cysoft.bss.core.dao.ObjectDao;
import org.cysoft.bss.core.dao.PersonDao;
import org.cysoft.bss.core.dao.PriceDao;
import org.cysoft.bss.core.dao.ProductDao;
import org.cysoft.bss.core.dao.PurchaseDao;
import org.cysoft.bss.core.dao.SaleDao;
import org.cysoft.bss.core.dao.ServerDao;
import org.cysoft.bss.core.dao.TicketDao;
import org.cysoft.bss.core.dao.UserDao;
import org.cysoft.bss.core.dao.mysql.AppMysql;
import org.cysoft.bss.core.dao.mysql.BillableCostMysql;
import org.cysoft.bss.core.dao.mysql.BillableRevenueMysql;
import org.cysoft.bss.core.dao.mysql.CityMysql;
import org.cysoft.bss.core.dao.mysql.CompanyMysql;
import org.cysoft.bss.core.dao.mysql.ContactMysql;
import org.cysoft.bss.core.dao.mysql.CountryMysql;
import org.cysoft.bss.core.dao.mysql.CyBssAuthMysql;
import org.cysoft.bss.core.dao.mysql.CyBssServiceMysql;
import org.cysoft.bss.core.dao.mysql.FileMysql;
import org.cysoft.bss.core.dao.mysql.InvoiceMysql;
import org.cysoft.bss.core.dao.mysql.LanguageMysql;
import org.cysoft.bss.core.dao.mysql.LocationMysql;
import org.cysoft.bss.core.dao.mysql.MetricMysql;
import org.cysoft.bss.core.dao.mysql.ObjectMysql;
import org.cysoft.bss.core.dao.mysql.PassiveInvoiceMysql;
import org.cysoft.bss.core.dao.mysql.PersonMysql;
import org.cysoft.bss.core.dao.mysql.PriceMysql;
import org.cysoft.bss.core.dao.mysql.ProductMysql;
import org.cysoft.bss.core.dao.mysql.PurchaseMysql;
import org.cysoft.bss.core.dao.mysql.SaleMysql;
import org.cysoft.bss.core.dao.mysql.ServerMysql;
import org.cysoft.bss.core.dao.mysql.TicketMysql;
import org.cysoft.bss.core.dao.mysql.UserMysql;
import org.cysoft.bss.core.message.CyBssMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@PropertySource("classpath:cy-bss-server.properties")
@ComponentScan({"org.cysoft.bss.core.service.impl"})
@EnableScheduling
public class ServerLauncher implements CommandLineRunner{
	
	private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
	
	private static final String OPTION_B="-b";
	private static final String OPTION_BATCH="-batch";
	
	@Autowired
	Environment environment;
	
	@Bean
	@Description("Message Source")
	public CyBssMessageSource messageSource(){
		logger.info("ServerLancher.messageSource() >>>");
		CyBssMessageSource ms=new CyBssMessageSource();
		logger.info("ServerLancher.messageSource() <<<");
		return ms;
	}
	
	@Bean
	@Description("Bss Auth Dao")
	public CyBssAuthDao bssAuthDao(){
	 	CyBssAuthDao authDao=new CyBssAuthMysql();
		return authDao;
	 }
	
	@Bean
	@Description("User Dao")
	public UserDao userDao(){
	 	UserDao userDao=new UserMysql();
		return userDao;
	}
	
	@Bean
	@Description("Bss Service Dao")
	public CyBssServiceDao bssServiceDao(){
	 	CyBssServiceDao serviceDao=new CyBssServiceMysql();
		return serviceDao;
	 }
	
	@Bean
	@Description("File Dao")
	public FileDao fileDao(){
	 	FileDao fileDao=new FileMysql();
		return fileDao;
	}
	
	@Bean
	@Description("Language Dao")
	public LanguageDao languageDao(){
	 	LanguageDao langDao=new LanguageMysql();
		return langDao;
	}
	
	@Bean
	@Description("App Dao")
	public AppDao appDao(){
	 	AppDao appDao=new AppMysql();
		return appDao;
	 }
	
	@Bean
	@Description("City Dao")
	public CityDao cityDao(){
	 	CityDao cityDao=new CityMysql();
		return cityDao;
	 }
	
	@Bean
	@Description("Company Dao")
	public CompanyDao companyDao(){
	 	CompanyDao companyDao=new CompanyMysql();
		return companyDao;
	 }
	
	@Bean
	@Description("Person Dao")
	public PersonDao personDao(){
	 	PersonDao personDao=new PersonMysql();
		return personDao;
	 }
	
	@Bean
	@Description("Contact Dao")
	public ContactDao contactDao(){
	 	ContactDao contactDao=new ContactMysql();
		return contactDao;
	 }
	
	@Bean
	@Description("Object Dao")
	public ObjectDao objectDao(){
	 	ObjectDao objectDao=new ObjectMysql();
		return objectDao;
	 }
	
	@Bean
	@Description("Location Dao")
	public LocationDao locationDao(){
	 	LocationDao locationDao=new LocationMysql();
		return locationDao;
	}
	
	@Bean
	@Description("Metric Dao")
	public MetricDao metricDao(){
	 	MetricDao metricDao=new MetricMysql();
		return metricDao;
	}
	
	@Bean
	@Description("Price Dao")
	public PriceDao priceDao(){
	 	PriceDao priceDao=new PriceMysql();
		return priceDao;
	 }
	
	@Bean
	@Description("Product Dao")
	public ProductDao productDao(){
	 	   ProductDao productDao=new ProductMysql();
		   return productDao;
	}
	 
	
	@Bean
	@Description("Country Dao")
	public CountryDao countryDao(){
	 	CountryDao countryDao=new CountryMysql();
		return countryDao;
	 }
	
	@Bean
	@Description("Purchase Dao")
	public PurchaseDao purchaseDao(){
	 	PurchaseDao purchaseDao=new PurchaseMysql();
		return purchaseDao;
	 }
	 
	 @Bean
	 @Description("Sale Dao")
	 public SaleDao saleDao(){
		 	SaleDao saleDao=new SaleMysql();
			return saleDao;
		 }
	 
	 
	 @Bean
	 @Description("Ticket Dao")
	 public TicketDao ticketDao(){
		 	TicketDao ticketDao=new TicketMysql();
			return ticketDao;
		 }
	 
	 @Bean
	 @Description("BillableCost Dao")
	 public BillableDao billableCostDao(){
		 	BillableDao billableCostDao=new BillableCostMysql();
			return billableCostDao;
		 }
	
	 
	 @Bean
	 @Description("BillableRevenue Dao")
	 public BillableDao billableRevenueDao(){
		 	BillableDao billableRevenueDao=new BillableRevenueMysql();
			return billableRevenueDao;
		 }
	
	
	@Bean
	@Description("Invoice Dao")
	public InvoiceDao invoiceDao(){
	 	InvoiceDao invoiceDao=new InvoiceMysql();
		return invoiceDao;
	}
	
	@Bean
	@Description("PassiveInvoice Dao")
	public InvoiceDao passiveInvoiceDao(){
	 	InvoiceDao passiveInvoiceDao=new PassiveInvoiceMysql();
		return passiveInvoiceDao;
	}
	
	@Bean
	@Description("Server Dao Rest")
	public ServerDao serverDao(){
	 	ServerDao serverDao=new ServerMysql();
		return serverDao;
	 }
	 
	
	
	@Bean
	@Description("MySql Data")
	public CyBssDataSource mySqlDS() {
		 CyBssDataSource mySqlDs = new CyBssDataSource(environment);
		 return mySqlDs;
	 }
	
	 @Bean
	 @Description("Transaction Manager")
	 public DataSourceTransactionManager transactionManager() {
		 logger.info("Server.transactionManager() >>>");
		 DataSourceTransactionManager transactionManager=new DataSourceTransactionManager(mySqlDS());
		 logger.info("Server.transactionManager() <<<");
 		 return transactionManager;
	 }
	
	@Bean
 	@Description("Server")
	public ServerProcess serverProcess(){
		ServerProcess serverProcess=new ServerProcess();
		return serverProcess;
	}
	
	@Autowired
	ServerProcess serverProcess;
	
	@Override
	public void run(String... args) {
		try {
			
			if (args.length!=1 && args.length!=2){
				String msgError="Number of parameters is wrong -> aspected <NodeId> or <-b/-batch> and <Batch Name>";
				logger.error(msgError);
				throw new CyBssException(msgError);
			}
			
			if (args.length==1)
				serverProcess.run(args);
			else
				{
				if (args.length==2){
					if (args[0].equals(OPTION_B)||args[0].equals(OPTION_BATCH)){
						serverProcess.runBatch(args);
					}
				}
			}
			
		} catch (CyBssException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("ServerLauncher.start():"+e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		logger.info("Start Server >>>");
		
		SpringApplication.run(ServerLauncher.class, args);
	   	logger.info("End Server <<<");
	}
	
}



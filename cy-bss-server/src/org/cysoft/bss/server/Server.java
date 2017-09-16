package org.cysoft.bss.server;

import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.common.CyBssUtility;
import org.cysoft.bss.core.model.Company;
import org.cysoft.bss.core.model.PriceComponent;
import org.cysoft.bss.core.model.Sale;
import org.cysoft.bss.core.service.CompanyService;
import org.cysoft.bss.core.service.PriceService;
import org.cysoft.bss.core.service.SaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public class Server {
	private static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	@Autowired @Lazy
	private CompanyService companyService;
	
	@Autowired @Lazy
	private SaleService saleService;
	
	@Autowired @Lazy
	private PriceService priceService;
	
	public void run(String... args) throws CyBssException{
		logger.info("Server.run() >>>");
		
		Thread t1=new Thread(new ServerThread("a",1));
		Thread t2=new Thread(new ServerThread("b",1));
		Thread t3=new Thread(new ServerThread("c",1));
		Thread t4=new Thread(new ServerThread("d",1));
		Thread t5=new Thread(new ServerThread("e",1));
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		
			
		logger.info("Server.run() <<<");
	}

	
	class ServerThread implements Runnable {

		private String id;
		private int size;
		
		public ServerThread(String id, int size){
			this.id=id;
			this.size=size;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			for(int i=0;i<size;i++){
				Company company=new Company();
				company.setCode(id+i);
				company.setName("Company "+id+" "+i);
				
				long companyId=0;
				try {
					companyId=companyService.add(company);
				} catch (CyBssException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Sale sale=new Sale();
				sale.setCompanyId(1);
				sale.setProductId(1);
				sale.setCompanyId(1);
				sale.setCustomerId(companyId);
				sale.setCurrencyId(1);
				sale.setVat(4);
				sale.setQty(i);
				sale.setPrice(1*size);
				sale.setComponentId(3);
				
				PriceComponent component=priceService.getPriceComponent(sale.getComponentId());
				if (component==null){
					logger.error("Componente non trovato !");
					return;
				}	
				
				
				sale.setTacitRenewal(Sale.TACIT_RENEWAL_NO);
				sale.setTransactionType(Sale.TRANSACTION_TYPE_BILLABLE);
				
				sale.setDate(CyBssUtility.dateToString(CyBssUtility.getCurrentDate(),CyBssUtility.DATE_yyyy_MM_dd));
				sale.calcAmounts();
				
				try {
					saleService.add(sale);
				} catch (CyBssException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				sale=new Sale();
				sale.setCompanyId(1);
				sale.setProductId(1);
				sale.setCompanyId(1);
				sale.setCustomerId(companyId);
				sale.setCurrencyId(1);
				sale.setVat(4);
				sale.setQty(i);
				sale.setPrice(1*size);
				sale.setComponentId(1);
				
				component=priceService.getPriceComponent(sale.getComponentId());
				if (component==null){
					logger.error("Componente non trovato !");
					return;
				}
				
				sale.setTacitRenewal(Sale.TACIT_RENEWAL_NO);
				sale.setTransactionType(Sale.TRANSACTION_TYPE_BILLABLE);
				
				sale.setDate(CyBssUtility.dateToString(CyBssUtility.getCurrentDate(),CyBssUtility.DATE_yyyy_MM_dd));
				sale.calcAmounts();
				
				try {
					saleService.add(sale);
				} catch (CyBssException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				sale=new Sale();
				sale.setCompanyId(1);
				sale.setProductId(1);
				sale.setCompanyId(1);
				sale.setCustomerId(companyId);
				sale.setCurrencyId(1);
				sale.setVat(4);
				sale.setQty(i);
				sale.setPrice(1*size);
				sale.setComponentId(2);
				
				component=priceService.getPriceComponent(sale.getComponentId());
				if (component==null){
					logger.error("Componente non trovato !");
					return;
				}
				
				sale.setTacitRenewal(Sale.TACIT_RENEWAL_NO);
				sale.setTransactionType(Sale.TRANSACTION_TYPE_BILLABLE);
				
				sale.setDate(CyBssUtility.dateToString(CyBssUtility.getCurrentDate(),CyBssUtility.DATE_yyyy_MM_dd));
				sale.calcAmounts();
				
				try {
					saleService.add(sale);
				} catch (CyBssException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			
		}
		
	}
	
	
	
}

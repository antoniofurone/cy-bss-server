package org.cysoft.bss.server.batch.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.common.CyBssUtility;
import org.cysoft.bss.core.model.Billable;
import org.cysoft.bss.core.model.Company;
import org.cysoft.bss.core.model.Invoice;
import org.cysoft.bss.core.model.Purchase;
import org.cysoft.bss.core.model.Sale;
import org.cysoft.bss.core.model.PriceComponent;
import org.cysoft.bss.server.ServerProcess;
import org.cysoft.bss.server.batch.BatchAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SianOlio extends BatchAdapter{
	
	private static final Logger logger = LoggerFactory.getLogger(SianOlio.class);
	
	private static final String FIELD_SEP=";";
	private static String ROW_SEP=";";
	private static final String CR="\n";
	
	private static final String PURCHASE_TRACE_PREFIX="Acquisto";
	private static final String SALE_TRACE_PREFIX="Vendita";
	
	private final long PURCHASE_OFFSET=1000000; 
	private final long SALE_OFFSET=2000000; 
	
	public SianOlio(ServerProcess parentProcess) {
		super(parentProcess);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exec() throws CyBssException {
		// TODO Auto-generated method stub
		logger.info("SianOlio.exec() >>>");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			
			String homeDir=System.getenv("CYBUSINESS_HOME");
			if (homeDir==null || homeDir.equals(""))
				homeDir=System.getProperty("user.home"); 
			System.out.println("CyBusiness Home ->"+homeDir);
			
			String sOutputDir=homeDir+File.separator+this.getClass().getSimpleName();
			System.out.println("Output Directory ->"+sOutputDir);
			
			File outputDir=new File(sOutputDir);
			if (!outputDir.exists())
				outputDir.mkdir();
			
			
			System.out.println("Inserisci l'id della tua azienda [obbligatorio] ->");
			long companyId = Long.parseLong(br.readLine());
			
			System.out.println("Id Prodotto [obbligatorio] ->");
			long productId = Long.parseLong(br.readLine());
			
			System.out.println("Id Azienda Cliente/Fornitore ->");
			String sCustSupplId=br.readLine();
			long custSupplId=0; 
			if (sCustSupplId!=null && !sCustSupplId.equals(""))
				custSupplId=Long.parseLong(sCustSupplId);
			
			System.out.println("Id Persona ->");
			String sPersonId=br.readLine();
			long personId=0; 
			if (sPersonId!=null && !sPersonId.equals(""))
				personId=Long.parseLong(sPersonId);
			
			System.out.println("Data Inizio [obbligatoria;formato dd/mm/yyyy] ->");
			String sDataStart=br.readLine();
			
			System.out.println("Data Fine [obbligatoria;formato dd/mm/yyyy] ->");
			String sDataEnd=br.readLine();
			
			System.out.println("Progressivo File [obbligatorio] ->");
			long progFile = Long.parseLong(br.readLine());
		
			System.out.println("Id Stabilimento ->");
			String sFactoryId=br.readLine();
			long factoryId=0; 
			if (sFactoryId!=null && !sFactoryId.equals(""))
				factoryId=Long.parseLong(sFactoryId);
			
			System.out.println("CR Fine Riga [Y,N] ->");
			String sCR=br.readLine();
			if (sCR!=null && sCR.equalsIgnoreCase("Y"))
				ROW_SEP+="\n";
			
			Company company=parentProcess.getCompanyService().getManaged(companyId);
			if (company==null)
				throw new CyBssException("Azienda con id <"+companyId+"> non trovata !");
			
			
			String currentDate=CyBssUtility.dateToString(CyBssUtility.getCurrentDate(), CyBssUtility.DATE_ddMMyyyy);
			String sProgFile=String.format("%05d",progFile);
			String companyFiscalId=((company.getFiscalCode()==null||company.getFiscalCode().equals(""))?company.getVatCode():company.getFiscalCode());
			
			String fileNameReg=companyFiscalId+"_"+currentDate+"_"+sProgFile+"_OPERREGI.TXT";
			String fileNameTrace=companyFiscalId+"_"+currentDate+"_"+sProgFile+"_TRACE.TXT";
			
			System.out.println("File di Carico/Scarico -> "+fileNameReg);
			System.out.println("File di Trace -> "+fileNameTrace);
			
			Writer bufTrace = 
					new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(sOutputDir+File.separator+fileNameTrace), 
					StandardCharsets.UTF_8));
			
			bufTrace.write("Parametri di input -> companyId:"+companyId+";productId="+productId
					+";custSupplId="+custSupplId
					+";personId="+personId
					+";dataStart="+sDataStart
					+";dataEnd="+sDataEnd
					+";progFile="+progFile
					+";factoryId="+factoryId
					+CR
					);
			
			bufTrace.write("File di Carico/Scarico -> "+fileNameReg+CR);
			bufTrace.write("...>"+CR);
					
			
			Writer bufReg = 
					new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(sOutputDir+File.separator+fileNameReg), 
					StandardCharsets.UTF_8));
			
			
			// Purchases
			List<Purchase> purchases=parentProcess.getPurchaseService().find(companyId, productId,"",
					custSupplId, "", "", 
					personId, "", "", 
					"", "", sDataStart, sDataEnd);
			
			for(Purchase purchase:purchases){
				
				if (!purchase.getComponentCode().equals(PriceComponent.CODE_USG_QXP)){
					bufTrace.write(PURCHASE_TRACE_PREFIX+"<"+purchase.getId()+">: Componente <> da "+ PriceComponent.CODE_USG_QXP);
					bufTrace.write(CR);
					continue;
				}
				
				List<Billable> billables=parentProcess.getPurchaseService().getBillables(purchase.getId());
				long idInvoice=getIdInvoice(billables);
				if (idInvoice==0){
					bufTrace.write(PURCHASE_TRACE_PREFIX+"<"+purchase.getId()+">: Non ancora fatturato");
					bufTrace.write(CR);
					continue;
				}
				
				Invoice invoice=parentProcess.getInvoiceService().get("P", idInvoice);
				
				
				bufReg.write(String.format("%-16s",companyFiscalId ));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(String.format("%010d",factoryId ));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(String.format("%010d",PURCHASE_OFFSET+purchase.getId()));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(purchase.getDate()),
						CyBssUtility.DATE_ddMMyyyy));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(String.format("%-10s",invoice.getNumber() ));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(invoice.getDate()),
						CyBssUtility.DATE_ddMMyyyy));
				bufReg.write(FIELD_SEP);
				
				// codice operazione
				bufReg.write(String.format("%-10s","A4"));
				bufReg.write(FIELD_SEP);
				
				
				bufReg.write(ROW_SEP);
				
				bufTrace.write(PURCHASE_TRACE_PREFIX+"<"+purchase.getId()+">: OK");
				bufTrace.write(CR);
				
			}
			
			
			//Sales
			List<Sale> sales=parentProcess.getSaleService().find(companyId, productId,"",
					custSupplId, "", "", 
					personId, "", "", 
					"", "", sDataStart, sDataEnd);
			
			for(Sale sale:sales){
				
				if (!sale.getComponentCode().equals(PriceComponent.CODE_USG_QXP)){
					bufTrace.write(SALE_TRACE_PREFIX+"<"+sale.getId()+">: Componente <> da "+ PriceComponent.CODE_USG_QXP);
					bufTrace.write(CR);
					continue;
				}
				
				List<Billable> billables=parentProcess.getSaleService().getBillables(sale.getId());
				long idInvoice=getIdInvoice(billables);
				if (idInvoice==0){
					bufTrace.write(SALE_TRACE_PREFIX+"<"+sale.getId()+">: Non ancora fatturato");
					bufTrace.write(CR);
					continue;
				}
				
				Invoice invoice=parentProcess.getInvoiceService().get("A", idInvoice);
				
				bufReg.write(String.format("%-16s",companyFiscalId ));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(String.format("%010d",factoryId ));
				bufReg.write(FIELD_SEP);
		
				bufReg.write(String.format("%010d",SALE_OFFSET+sale.getId()));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(sale.getDate()),
						CyBssUtility.DATE_ddMMyyyy));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(String.format("%-10s",invoice.getNumber() ));
				bufReg.write(FIELD_SEP);
				
				bufReg.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(invoice.getDate()),
						CyBssUtility.DATE_ddMMyyyy));
				bufReg.write(FIELD_SEP);
				
				// codice operazione
				bufReg.write(String.format("%-10s","A9"));
				bufReg.write(FIELD_SEP);
				
				
				bufReg.write(ROW_SEP);
				
				bufTrace.write(SALE_TRACE_PREFIX+"<"+sale.getId()+">: OK");
				bufTrace.write(CR);
				
			}
			
			
			bufTrace.write("<..."+CR);
			bufTrace.close();
			
			bufReg.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		
		System.out.println("Premi un tasto per continuare ...");
		try {
			System.in.read();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		logger.info("SianOlio.exec() <<<");
		
	}

	private long getIdInvoice(List<Billable> billables){
		
		long idInvoice=0;
		for (Billable billable:billables){
			if (billable.isBilled() && billable.getInvoiceId()>idInvoice)
				idInvoice=billable.getInvoiceId();
		}
		
		return idInvoice;
	}
	
	
}

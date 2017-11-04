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
import java.text.ParseException;
import java.util.List;

import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.common.CyBssUtility;
import org.cysoft.bss.core.model.Billable;
import org.cysoft.bss.core.model.CommercialTransaction;
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
	
	private final long PERSON_OFFSET=3000000; 
	private final long COMPANY_OFFSET=4000000; 
	
	private final String KG="kg";
	
	private String companyFiscalId;
	private long factoryId=0;
	
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
			companyFiscalId=((company.getFiscalCode()==null||company.getFiscalCode().equals(""))?company.getVatCode():company.getFiscalCode());
			
			String fileNameReg=companyFiscalId+"_"+currentDate+"_"+sProgFile+"_OPERREGI.TXT";
			String fileNameCF=companyFiscalId+"_"+currentDate+"_"+sProgFile+"_ANAGFCTO.TXT";
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
			 
			Writer bufAnagCF = 
					new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(sOutputDir+File.separator+fileNameCF), 
					StandardCharsets.UTF_8));
			
			
			// Purchases
			List<Purchase> purchases=parentProcess.getPurchaseService().find(companyId, productId,"",
					custSupplId, "", "", 
					personId, "", "", 
					"", "", sDataStart, sDataEnd);
			
			for(Purchase purchase:purchases){
				if (!writeOperation(bufReg, bufTrace, purchase))
					continue;
			}
			
			//Sales
			List<Sale> sales=parentProcess.getSaleService().find(companyId, productId,"",
					custSupplId, "", "", 
					personId, "", "", 
					"", "", sDataStart, sDataEnd);
			
			for(Sale sale:sales){
				if (!writeOperation(bufReg, bufTrace, sale))
					continue;
			}
			
			
			bufTrace.write("<..."+CR);
			bufTrace.close();
			
			bufAnagCF.close();
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
	
	private boolean writeOperation(Writer writerOp,Writer tracer,CommercialTransaction trans) 
			throws IOException, CyBssException, ParseException{
		
		boolean isPurchase=false;
		String TRACE_PREFIX=SALE_TRACE_PREFIX;
		
		if (trans instanceof Purchase){
			isPurchase=true;
			TRACE_PREFIX=PURCHASE_TRACE_PREFIX;
		}
		
		
		if (!trans.getComponentCode().equals(PriceComponent.CODE_USG_QXP)){
			tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Componente <> da "+ PriceComponent.CODE_USG_QXP);
			tracer.write(CR);
			return false;
		}
		
		List<Billable> billables=null;
		if (isPurchase)
			billables=parentProcess.getPurchaseService().getBillables(trans.getId());
		else
			billables=parentProcess.getSaleService().getBillables(trans.getId());
		
		long idInvoice=getIdInvoice(billables);
		if (idInvoice==0){
			tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Non ancora fatturato");
			tracer.write(CR);
			return false;
		}
		
		Invoice invoice=parentProcess.getInvoiceService().get(isPurchase?"P":"A", idInvoice);
		if (!trans.getQtyUmSimbol().equalsIgnoreCase(KG)){
			tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Um <> "+KG);
			tracer.write(CR);
			return false;
		}
		
		writerOp.write(String.format("%-16s",companyFiscalId ));
		writerOp.write(FIELD_SEP);
		
		writerOp.write(String.format("%010d",factoryId));
		writerOp.write(FIELD_SEP);
		
		writerOp.write(String.format("%010d",isPurchase?PURCHASE_OFFSET+trans.getId():SALE_OFFSET+trans.getId()));
		writerOp.write(FIELD_SEP);
		
		writerOp.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(trans.getDate()),
				CyBssUtility.DATE_ddMMyyyy));
		writerOp.write(FIELD_SEP);
		
		writerOp.write(String.format("%-10s",invoice.getNumber() ));
		writerOp.write(FIELD_SEP);
		
		writerOp.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(invoice.getDate()),
				CyBssUtility.DATE_ddMMyyyy));
		writerOp.write(FIELD_SEP);
		
		// codice operazione
		writerOp.write(String.format("%-10s", isPurchase?"A4":"A9"));
		writerOp.write(FIELD_SEP);
		
		// cliente/fornitore
		long idCF=0;
		if (trans.getPersonId()==0)
			idCF=COMPANY_OFFSET+(isPurchase?((Purchase)trans).getSupplierId():
				((Sale)trans).getCustomerId());
		else
			idCF=PERSON_OFFSET+trans.getPersonId();
		writerOp.write(String.format("%010d",idCF));
		writerOp.write(FIELD_SEP);
		
		// committente
		writerOp.write(String.format("%010d",0));
		writerOp.write(FIELD_SEP);
		
		// quantitativo carico
		if (isPurchase)
			writerOp.write(String.format("%013d",(long)Math.round(trans.getQty()*1000)));
		else
			writerOp.write(String.format("%013d",0));
		
		
		writerOp.write(ROW_SEP);
		
		tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: OK");
		tracer.write(CR);

		return true;
	}
	
	
	/*
	I campi sempre obbligatori, per qualsiasi operazione, sono :
		Identificativo dell'impresa (campo con progressivo 1)
		Identificativo dello stabilimento (campo con progressivo 2)
		Numero operazione (campo con progressivo 3)
		Data operazione (campo con progressivo 4)
		Codice operazione (campo con progressivo 7)
		Tipo record (campo con progressivo 49)
	
	A4 - Carico di olive da ditta italiana
	
	obbligatori ->
	5-numero documento giustificativo;
	6-data documento giustificativo;
	8-codice soggetto fornitore/cliente;
	10-carico olive;
	17-origine macroarea(18-origine specifica);
	
	facoltativi - >
	9-codice soggetto committente;
	18-origine specifica;
	29-note;
	35-flag biologico o 37-flag in conversione;
	41-data/ora raccolta olive;
	43-annata;
	I campi DOP da valorizzare solo se previsti dal relativo disciplinare altrimenti sono non richiesti.
	
	
	A9 - Scarico/vendita di olive a ditta italiana
	
	5-numero documento giustificativo;
	6-data documento giustificativo;
	8-codice soggetto fornitore/cliente;
	11-scarico olive;
	17-origine macroarea(18-origine specifica);
	
	facoltativi ->
	9-codice soggetto committente;
	18-origine specifica;
	29-note;
	35-flag biologico o 37-flag in conversione;
	41-data/ora raccolta olive;
	43-annata;
	I campi DOP da valorizzare solo se previsti dal relativo disciplinare altrimenti sono non richiesti.
	
	*/
}

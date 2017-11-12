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
import java.util.ArrayList;
import java.util.List;

import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.common.CyBssUtility;
import org.cysoft.bss.core.model.Attribute;
import org.cysoft.bss.core.model.Billable;
import org.cysoft.bss.core.model.CommercialTransaction;
import org.cysoft.bss.core.model.Company;
import org.cysoft.bss.core.model.CyBssObject;
import org.cysoft.bss.core.model.Invoice;
import org.cysoft.bss.core.model.Person;
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
	
	private final String INVOICE_NUMBER_ATTR="Numero Fattura";
	private final String INVOICE_DATE_ATTR="Data Fattura";
	private final String SIAN_ID_ATTR="Sian Id";
	
	private final String KG="kg";
	
	private String companyFiscalId;
	private long factoryId=0;
	
	List<Long> lCustSuppliers=null;
	
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
			
			lCustSuppliers=new ArrayList<Long>();
			
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
				if (!writeOperation(bufReg, bufAnagCF, bufTrace, purchase))
					continue;
			}
			
			//Sales
			List<Sale> sales=parentProcess.getSaleService().find(companyId, productId,"",
					custSupplId, "", "", 
					personId, "", "", 
					"", "", sDataStart, sDataEnd);
			
			for(Sale sale:sales){
				if (!writeOperation(bufReg, bufAnagCF, bufTrace, sale))
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
	
	private boolean writeOperation(Writer writerOp, Writer writerCF, Writer tracer,CommercialTransaction trans) 
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
		
	
		
		String sInvoiceNumber="";
		String sInvoiceDate="";
		
		if (trans.getTransactionType().equalsIgnoreCase(CommercialTransaction.TRANSACTION_TYPE_BILLABLE))
		{
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
			if (invoice==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Fattura <"+idInvoice+"> non trovata");
				tracer.write(CR);
				return false;
			}
			
			sInvoiceNumber=Integer.toString(invoice.getNumber());
			sInvoiceDate=invoice.getDate();
		}
		else
		{
			CyBssObject cyObject=parentProcess.getObjectService().getByEntity(isPurchase?Purchase.ENTITY_NAME:Sale.ENTITY_NAME);
			if (cyObject==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Object <"+(isPurchase?Purchase.ENTITY_NAME:Sale.ENTITY_NAME)+"> non trovato");
				tracer.write(CR);
				return false;
			}
			
			Attribute attrInvoiceNum=parentProcess.getObjectService().getAttributeByName(cyObject.getId(), INVOICE_NUMBER_ATTR);
			if (attrInvoiceNum==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Attribute <"+INVOICE_NUMBER_ATTR+"> non trovato");
				tracer.write(CR);
				return false;
			}
			
			Attribute attrInvoiceDate=parentProcess.getObjectService().getAttributeByName(cyObject.getId(), INVOICE_DATE_ATTR);
			if (attrInvoiceDate==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Attribute <"+INVOICE_DATE_ATTR+"> non trovato");
				tracer.write(CR);
				return false;
			}
			
			attrInvoiceNum=parentProcess.getObjectService().getAttributeValue(cyObject.getId(), attrInvoiceNum.getId());
			if (attrInvoiceNum==null || attrInvoiceNum.getValue()==null || attrInvoiceNum.getValue().equals("")){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Attribute <"+INVOICE_NUMBER_ATTR+"> non trovato o non valorizzato");
				tracer.write(CR);
				return false;
			}
			else
				sInvoiceNumber=attrInvoiceNum.getValue();
			
			attrInvoiceDate=parentProcess.getObjectService().getAttributeValue(cyObject.getId(), attrInvoiceDate.getId());
			if (attrInvoiceDate==null || attrInvoiceDate.getValue()==null || attrInvoiceDate.getValue().equals("")){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Attribute <"+INVOICE_DATE_ATTR+"> non trovato o non valorizzato");
				tracer.write(CR);
				return false;
			}
			else
				sInvoiceDate=attrInvoiceDate.getValue();
		}
		
		
		long idCustomerSupplier=0;
		Company custSuppl=new Company(); 
		if (trans.getPersonId()==0){
			custSuppl=parentProcess.getCompanyService().get((isPurchase?((Purchase)trans).getSupplierId():
				((Sale)trans).getCustomerId()));
			
			if (custSuppl==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Cliente/Fornitore <"+(isPurchase?((Purchase)trans).getSupplierId():
					((Sale)trans).getCustomerId())+"> non trovato");
				tracer.write(CR);
				return false;
			}
			
			idCustomerSupplier=COMPANY_OFFSET+custSuppl.getId();
			
			CyBssObject cyObject=parentProcess.getObjectService().getByEntity(Company.ENTITY_NAME);
			if (cyObject==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Object <"+Company.ENTITY_NAME+"> non trovato");
				tracer.write(CR);
				return false;
			}
			
			Attribute attrSianId=parentProcess.getObjectService().getAttributeByName(cyObject.getId(), SIAN_ID_ATTR);
			if (attrSianId!=null){
				attrSianId=parentProcess.getObjectService().getAttributeValue(cyObject.getId(), attrSianId.getId());
				if (attrSianId!=null && attrSianId.getValue()!=null && attrSianId.getValue().equals(""))
					idCustomerSupplier=Long.parseLong(attrSianId.getValue());
					
			}
			
			
			
		}
		else
		{
			
			Person person=parentProcess.getPersonService().get(trans.getPersonId());
			
			if (person==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Persona <"+trans.getPersonId()+"> non trovato o non valorizzato");
				tracer.write(CR);
				return false;
			}
			
			idCustomerSupplier=PERSON_OFFSET+person.getId();
			
			CyBssObject cyObject=parentProcess.getObjectService().getByEntity(Person.ENTITY_NAME);
			if (cyObject==null){
				tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Object <"+Person.ENTITY_NAME+"> non trovato");
				tracer.write(CR);
				return false;
			}
			
			custSuppl.setName(person.getSecondName()+" "+person.getFirstName());
			custSuppl.setAddress(person.getAddress());
			custSuppl.setCity(person.getCity());
			custSuppl.setFiscalCode(person.getFiscalCode());
			custSuppl.setZipCode(person.getZipCode());
			
			Attribute attrSianId=parentProcess.getObjectService().getAttributeByName(cyObject.getId(), SIAN_ID_ATTR);
			if (attrSianId!=null){
				attrSianId=parentProcess.getObjectService().getAttributeValue(cyObject.getId(), attrSianId.getId());
				if (attrSianId!=null && attrSianId.getValue()!=null && attrSianId.getValue().equals(""))
					idCustomerSupplier=Long.parseLong(attrSianId.getValue());
			}
			
			
		}
		
		
		if (!trans.getQtyUmSimbol().equalsIgnoreCase(KG)){
			tracer.write(TRACE_PREFIX+"<"+trans.getId()+">: Um <> "+KG);
			tracer.write(CR);
			return false;
		}
		
		
		//1. CUAA (Codice fiscale o partita IVA) dell'impresa
		writerOp.write(String.format("%-16s",companyFiscalId ));
		writerOp.write(FIELD_SEP);
		
		//2. Identificativo dello stabilimento/deposito
		writerOp.write(String.format("%010d",factoryId));
		writerOp.write(FIELD_SEP);
		
		//3. Numero dell'operazione
		writerOp.write(String.format("%010d",isPurchase?PURCHASE_OFFSET+trans.getId():SALE_OFFSET+trans.getId()));
		writerOp.write(FIELD_SEP);
		
		//4. Data dell'operazione
		writerOp.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(trans.getDate()),
				CyBssUtility.DATE_ddMMyyyy));
		writerOp.write(FIELD_SEP);
		
		//5. Numero documento giustificativo
		writerOp.write(String.format("%-10s",sInvoiceNumber));
		writerOp.write(FIELD_SEP);
		
		//6. Data documento giustificativo
		writerOp.write(CyBssUtility.dateToString(CyBssUtility.tryStringToDate(sInvoiceDate),
				CyBssUtility.DATE_ddMMyyyy));
		writerOp.write(FIELD_SEP);
		
		//7. codice operazione
		writerOp.write(String.format("%-10s", isPurchase?"A4":"A9"));
		writerOp.write(FIELD_SEP);
		
		//8. cliente/fornitore
		writerOp.write(String.format("%010d",idCustomerSupplier));
		writerOp.write(FIELD_SEP);
		
		//9. committente
		writerOp.write(String.format("%010d",0));
		writerOp.write(FIELD_SEP);
		
		//10. quantitativo carico
		if (isPurchase)
			writerOp.write(String.format("%013d",(long)Math.round(trans.getQty()*1000)));
		else
			writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//11. quantitativo scarico
		if (!isPurchase)
			writerOp.write(String.format("%013d",(long)Math.round(trans.getQty()*1000)));
		else
			writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//12. identificativo recipiente di stoccaggio
		writerOp.write(String.format("%-10s"," "));
		writerOp.write(FIELD_SEP);
		
		//13. identificativo recipiente di stoccaggio di destinazione
		writerOp.write(String.format("%-10s"," "));
		writerOp.write(FIELD_SEP);
		
		//14. identificativo stabilimento di provenienza
		writerOp.write(String.format("%010d",0));
		writerOp.write(FIELD_SEP);
		
		//15. descrione categoria dell'olio
		writerOp.write(String.format("%02d",0));
		writerOp.write(FIELD_SEP);
		
		//16. descrione categoria dell'olio a fine operazione
		writerOp.write(String.format("%02d",0));
		writerOp.write(FIELD_SEP);
		
		//17. descrizione origine olio/olive per macro-aree
		// 1 ITA
		writerOp.write(String.format("%02d",1));
		writerOp.write(FIELD_SEP);
		
		//18. Descrizione origine olive/olio specifica
		writerOp.write(String.format("%-80s"," "));
		writerOp.write(FIELD_SEP);
		
		//19. Descrizione origine olive/olio per macroarea a fine operazione
		writerOp.write(String.format("%02d",0));
		writerOp.write(FIELD_SEP);
	
		//20. Descrizione origine olive/olio specifica a fine operazione
		writerOp.write(String.format("%-80s"," "));
		writerOp.write(FIELD_SEP);
		
		//21. Quantita' carico sansa (Kg)
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//22. Quantita' scarico sansa (Kg)
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//23. Quantita' carico di olio sfuso (kg)
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//24. Quantita' scarico di olio sfuso (kg)
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//25. Quantita' carico di olio confezionato (litri)
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//26. Quantita' scarico di olio confezionato (litri)
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
				
		//27. Quantita' perdite o cali di lavoro (kg)
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//28. Lotto di appartenenza dell'olio
		writerOp.write(String.format("%-20s"," "));
		writerOp.write(FIELD_SEP);
		
		//29. Descrizione note
		writerOp.write(String.format("%-300s"," "));
		writerOp.write(FIELD_SEP);
		
		//30. Flag lavoro per conto terzi
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
				
		//31. Flag indicazione prima spremitura a freddo
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//32. Flag indicazione prima spremitura a freddo fine operazione
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//33. Flag indicazione estratto a freddo
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//34. Flag indicazione estratto a freddo fine operazione
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//35. Flag Biologico
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//36. Flag Biologico fine operazione
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//37. Flag in conversione
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//38. Flag in conversione fine operazione
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//39. Flag non etichettato
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//40. Flag non etichettato fine operazione
		writerOp.write(" ");
		writerOp.write(FIELD_SEP);
		
		//41. Data e ora di raccolta delle olive
		writerOp.write(String.format("%-17s"," "));
		writerOp.write(FIELD_SEP);
		
		//42. Data e ora di molitura delle olive
		writerOp.write(String.format("%-17s"," "));
		writerOp.write(FIELD_SEP);
		
		//43. Annata
		writerOp.write(String.format("%04d",0));
		writerOp.write(FIELD_SEP);
		
		//44. Serie collarini dal
		writerOp.write(String.format("%-10s"," "));
		writerOp.write(FIELD_SEP);
		
		//45. Serie collarini al
		writerOp.write(String.format("%-10s"," "));
		writerOp.write(FIELD_SEP);
		
		//46. Capacità confezione
		writerOp.write(String.format("%013d",0));
		writerOp.write(FIELD_SEP);
		
		//47. Data certificato
		writerOp.write(String.format("%-8s"," "));
		writerOp.write(FIELD_SEP);
		
		//48. Numero certificato
		writerOp.write(String.format("%010d",0));
		writerOp.write(FIELD_SEP);
		
		//49. Tipo record inviato
		// I=Inserimento;C=Cancellazione
		writerOp.write("I");
		
		writerOp.write(ROW_SEP);
		
		// Customer/Supplier
		if (!lCustSuppliers.contains(idCustomerSupplier)){
			
			//1. CUAA (Codice fiscale o partita IVA) dell'impresa
			writerCF.write(String.format("%-16s",companyFiscalId ));
			writerCF.write(FIELD_SEP);
			
			//2. Stato ditta
			writerCF.write("IT");
			writerCF.write(FIELD_SEP);
			
			//3. Identificativo Fiscale soggetto
			writerCF.write(String.format("%-16s",
					custSuppl.getFiscalCode()!=null && !custSuppl.getFiscalCode().equals("")?
							custSuppl.getFiscalCode():custSuppl.getVatCode() 
					));
			writerCF.write(FIELD_SEP);
			
			//4. Codice soggetto
			writerCF.write(String.format("%010d",idCustomerSupplier));
			writerCF.write(FIELD_SEP);
			
			//5. Denominazione soggetto
			writerCF.write(String.format("%-150s",custSuppl.getName()));
			writerCF.write(FIELD_SEP);
			
			//6. Indirizzo soggetto
			writerCF.write(String.format("%-150s",custSuppl.getAddress()));
			writerCF.write(FIELD_SEP);
			
			//7. Codice iso nazione
			writerCF.write("  ");
			writerCF.write(FIELD_SEP);
			
			//8. Codice istat provincia
			writerCF.write("???");
			writerCF.write(FIELD_SEP);
			
			//9. Codice istat comune
			writerCF.write("???");
			
			writerCF.write(ROW_SEP);
			
			lCustSuppliers.add(idCustomerSupplier);
		}
		
		
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

package com.vpcp.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;


import com.vpcp.services.AgencyServiceImp;
import com.vpcp.services.FileConfig;
import com.vpcp.services.KnobstickServiceImp;
import com.vpcp.services.VnptProperties;
import com.vpcp.services.model.DeleteAgencyResult;
import com.vpcp.services.model.GetAgenciesResult;
import com.vpcp.services.model.GetChangeStatusResult;
import com.vpcp.services.model.GetEdocResult;
import com.vpcp.services.model.GetReceivedEdocResult;
import com.vpcp.services.model.GetSendEdocResult;
import com.vpcp.services.model.GetSynchronizeUnit;
import com.vpcp.services.model.Knobstick;
import com.vpcp.services.model.MessageType;
import com.vpcp.services.model.RegisterAgencyResult;
import com.vpcp.services.model.SendEdocResult;


public class ServiceTest {

	public static void main(String args[]) {
		VnptProperties vnptProperties = null;
		int init = 2;
		if(init==1) {
			//cau hinh bang file property
			FileConfig.setFileConfig("./resources/collaboration.properties");
			vnptProperties = new VnptProperties(FileConfig.getCollaborationPF());
		}else {
			//cau hinh bang tham so, server configuration
			String endpoint = "";
			//ma trung tam lien thong
			String systemId = "";
			//secret key
			String secret ="";
			String  storePathDir = "d:/test";
			int maxConnection = 10, retry = 3;
			vnptProperties = new VnptProperties(endpoint, systemId, secret, storePathDir, maxConnection, retry);
		}
		  AgencyServiceImp agencyService = new AgencyServiceImp(vnptProperties);
		  KnobstickServiceImp knobstickService = new KnobstickServiceImp(vnptProperties);
		  ServiceTest serviceTest = new ServiceTest();
		  String pstart = "---------";
		ServiceTest.run = 1;
		
		switch (run) {
			case 1:
				// lay danh sach cac don vi lien thong
				GetAgenciesResult getAgenciesResult = agencyService.getAgenciesList(serviceTest.getJsonHeader());
				if(getAgenciesResult!=null) {
					System.out.println(pstart+"status:"+getAgenciesResult.getStatus());
					System.out.println(pstart+"Desc:"+getAgenciesResult.getErrorDesc());
					System.out.println(pstart+"Size:"+getAgenciesResult.getAgencies().size());
				}
				break;
			case 2:
				// dang ky va cap nhat don vi lien thong
				String data = "{\"id\":\"\",\"pcode\":\"00.22.W00\",\"code\":\"999.010.01.H22\",\"name\":\"Hà giang test V4 \",\"mail\":cc\"\",\"mobile\":333\"\",\"fax\":333\"\"}";
				RegisterAgencyResult registerAgencyResult = agencyService.registerAgency(serviceTest.getJsonHeader(), data);
				if(registerAgencyResult!=null) {
					System.out.println(pstart+"status:"+registerAgencyResult.getStatus());
					System.out.println(pstart+"Desc:"+registerAgencyResult.getErrorDesc());
				}
				break;
			case 3:
				// huy dang ky don vi lien thong
				DeleteAgencyResult deleteAgencyResult = agencyService.deleteAgency(serviceTest.getJsonHeader());
				if(deleteAgencyResult!=null) {
					System.out.println(pstart+"status:"+deleteAgencyResult.getStatus());
					System.out.println(pstart+"Desc:"+deleteAgencyResult.getErrorDesc());
				}
				break;
			case 4:
				// lay thong tin cac van ban den
				GetReceivedEdocResult getReceivedEdocResult = knobstickService.getReceivedEdocList(serviceTest.getJsonHeader());
				if(getReceivedEdocResult!=null) {
					System.out.println(pstart+"status:"+getReceivedEdocResult.getStatus());
					System.out.println(pstart+"Desc:"+getReceivedEdocResult.getErrorDesc());
					System.out.println(pstart+"Size:"+getReceivedEdocResult.getKnobsticks().size());
					if(getReceivedEdocResult.getKnobsticks().size()>0){
						for(Knobstick item : getReceivedEdocResult.getKnobsticks()){
							try{
								System.out.println("docId: "+item.getId());
									String json2 = createHeaderJsonGetDoc(item.getId());
									GetEdocResult getEdocResult = knobstickService.getEdoc(json2);
									if(getEdocResult!=null) {
										System.out.println(pstart+"status:"+getEdocResult.getStatus());
										System.out.println(pstart+"Desc:"+getEdocResult.getErrorDesc());
										System.out.println(pstart+"Data:"+getEdocResult.getData());
										System.out.println(pstart+"file:"+getEdocResult.getFilePath());
											//prase du lieu tu edxml
											/*File file = new File(getEdocResult.getFilePath());
											InputStream inputStream = new FileInputStream(file);
											// goi tin status
											if(serviceTest.getJsonHeader().matches("status")){
												Status ed =  StatusXmlParser.parse(inputStream);
												//lay thong tin trong header 
												MessageStatus messageHeader2 = (MessageStatus) ed.getHeader().getMessageHeader();
											}
											//goi tin edoc
											else{
												Ed ed =  EdXmlParser.getInstance().parse(inputStream);
												//lay thong tin trong header
												MessageHeader messageHeader = (MessageHeader) ed.getHeader().getMessageHeader();
												//lay thong tin file dinh kem
												List<Attachment> attachment = ed.getAttachments();
												//lay thong tin trong body
												Body body = ed.getBody();
											}*/
										
										if(getEdocResult.getStatus().equals("OK")){
											String json = createHeaderJsonUpdate(item.getId(),"done") ;
											GetChangeStatusResult  GetChangeStatusResult2 = knobstickService.updateStatus(json);
											System.err.println(item.getId());
										}
										else{
											String json = createHeaderJsonUpdate(item.getId(),"fail") ;
											GetChangeStatusResult  GetChangeStatusResult2 = knobstickService.updateStatus(json);
											System.err.println("errrr : " + item.getId());
										}
										
									}
							}catch(Exception ex){
								System.out.println(ex.getMessage());
								break;
							}
						}
					}
				}
				break;
			case 5:
				// lay trang thai gui nhan van ban
				GetSendEdocResult getSendEdocResult = knobstickService.getSentEdocList(serviceTest.getJsonHeader());
				//System.err.println("getSendEdocResult" + getSendEdocResult);
				if(getSendEdocResult!=null) {
					System.out.println(pstart+"status:"+getSendEdocResult.getStatus());
					System.out.println(pstart+"Desc:"+getSendEdocResult.getErrorDesc());
					System.out.println(pstart+"Size:"+getSendEdocResult.getStatusResult().size());
					
					System.err.println("getSendEdocResult :" + getSendEdocResult.getStatusResult().get(0));
					System.err.println("getSendEdocResult :" + getSendEdocResult.getStatusResult().get(0).getFromCode());
				}
				break;
			case 6:
				// lay thong tin 1 van ban
				GetEdocResult getEdocResult = knobstickService.getEdoc(serviceTest.getJsonHeader());
				if(getEdocResult!=null) {
					System.out.println(pstart+"status:"+getEdocResult.getStatus());
					System.out.println(pstart+"Desc:"+getEdocResult.getErrorDesc());
					System.out.println(pstart+"Data:"+getEdocResult.getData());
					System.out.println(pstart+"file:"+getEdocResult.getFilePath());
				}
				break;
			case 7:
				// gui van ban
				String edXMLFileLocation = "./resources/edoc.edxml";
				SendEdocResult sendEdocResult = knobstickService.sendEdoc(serviceTest.getJsonHeader(), edXMLFileLocation);
				if(sendEdocResult!=null) {
					System.out.println(pstart+"status:"+sendEdocResult.getStatus());
					System.out.println(pstart+"Desc:"+sendEdocResult.getErrorDesc());
					System.out.println(pstart+"DocID:"+sendEdocResult.getDocID());
				}
				break;
			case 8:
				// thay doi trang thai van ban nhan
				GetChangeStatusResult  GetChangeStatusResult= knobstickService.updateStatus(serviceTest.getJsonHeader());
				if(GetChangeStatusResult!=null) {
					System.out.println(pstart+"status:"+GetChangeStatusResult.getStatus());
					System.out.println(pstart+"Desc:"+GetChangeStatusResult.getErrorDesc());
					System.out.println(pstart+"status:"+GetChangeStatusResult.getErrorCode());
				}
				break;
			case 9:
				// dong bo don vi
				GetSynchronizeUnit GgetSynchronizeUnit = agencyService.getSynchronizeUnit(serviceTest.getJsonHeader());
				if(GgetSynchronizeUnit!=null) {
					System.out.println(pstart+"status:"+GgetSynchronizeUnit.getStatus());
					System.out.println(pstart+"Desc:"+GgetSynchronizeUnit.getErrorDesc());
					System.out.println(pstart+"Size:"+GgetSynchronizeUnit.getUnitSynchronize().size());
				}
				System.out.println(pstart+"getCenterCode:"+GgetSynchronizeUnit.getUnitSynchronize().get(0));
				break;
			default:
				break;
		}
	}
	public  String getJsonHeader() {
		String json  ="";
		StringBuffer stringBuffer = new StringBuffer();
		switch (run) {
			case 1:
				json = "{}";
				break;
			case 2:
				json = "{}";
				break;
			case 3:
				String agencyCode = "99.99.99.G21";
				json = "{\"AgencyCode\":\""+agencyCode+"\"}";
				break;
			case 4:
				stringBuffer = new StringBuffer();
				String servicetype = "eDoc";
				stringBuffer.append("{"); 
				stringBuffer.append("\"servicetype\":\""+servicetype+"\"");
				stringBuffer.append(",\"messagetype\":\""+ MessageType.edoc +"\"");
				stringBuffer.append("}"); 
				json = stringBuffer.toString();
			
				break;
			case 5:
				stringBuffer = new StringBuffer();
				servicetype = "eDoc";
				String docId = "5bebcfeff858e3e0cc154762";
				stringBuffer.append("{"); 
				stringBuffer.append("\"servicetype\":\""+servicetype+"\"");
				stringBuffer.append(",\"messagetype\":\""+ MessageType.edoc +"\"");
				stringBuffer.append(",\"docId\":\""+ docId +"\"");
				stringBuffer.append("}"); 
				json = stringBuffer.toString();
				break;
			case 6:
				stringBuffer = new StringBuffer();
				String filePath = "xml";
				docId = "8af226c8-7cde-4fbe-88fe-e15a768b49cb";
				stringBuffer.append("{"); 
				stringBuffer.append("\"filePath\":\""+filePath+"\"");
				stringBuffer.append(",\"docId\":\""+ docId +"\"");
				stringBuffer.append("}"); 
				json = stringBuffer.toString();
				break;
			case 7:
				stringBuffer = new StringBuffer();
				servicetype = "eDoc";
				String from = "777.77.04.D77";
				stringBuffer.append("{"); 
				stringBuffer.append("\"from\":\""+from+"\"");
				stringBuffer.append(",\"servicetype\":\""+servicetype+"\"");
				stringBuffer.append(",\"messagetype\":\""+ MessageType.edoc +"\"");
				stringBuffer.append("}"); 
				json = stringBuffer.toString();
				break;
			case 8:
				stringBuffer = new StringBuffer();
				servicetype = "status";
		    	String status = "done"  ;//"processing"; // "fail" , "done"
		    	docId = "0dbcb61d-e30e-45e9-8c49-d2819759e3f4";
				stringBuffer.append("{"); 
				stringBuffer.append("\"status\":\""+ status +"\"");
				stringBuffer.append(",\"docid\":"+ docId );
				stringBuffer.append("}"); 
				json = stringBuffer.toString();
				break;
			case 9:
				json = "{}";
				break;
			default:
				break;
			}
		return json;
	}
	public static int run = 1;
	public  String username;
	public  String password;
	public ServiceTest(String username, String password) {
		this.username = username;
		this.password = password;
	}
	public static String createHeaderJsonUpdate(String docId, String status){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{"); 
		stringBuffer.append("\"status\":\""+ status +"\"");
		stringBuffer.append(",\"docId\":"+ docId );
		stringBuffer.append("}"); 
		String	json = stringBuffer.toString();
		return json;
	}
	public static String createHeaderJsonGetDoc(String docId){
		StringBuffer stringBuffer = new StringBuffer();
		String filePath = "xml";
		stringBuffer.append("{"); 
		stringBuffer.append("\"filePath\":\""+filePath+"\"");
		stringBuffer.append(",\"docId\":\""+ docId +"\"");
		stringBuffer.append("}"); 
		String json = stringBuffer.toString();
		return json;
	}
	public ServiceTest() {
		// TODO Auto-generated constructor stub
	}
}

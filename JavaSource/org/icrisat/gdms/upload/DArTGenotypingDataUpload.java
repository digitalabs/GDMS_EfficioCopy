package org.icrisat.gdms.upload;

import java.io.File;
import java.io.FileInputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import jxl.Sheet;
import jxl.Workbook;

import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.HibernateSessionFactory;
import org.icrisat.gdms.common.MaxIdValue;


public class DArTGenotypingDataUpload {
	String str="";
	private Session session;
	
	private Transaction tx;
	
	HttpServletRequest request;
	
	java.sql.Connection conn;
	java.sql.Connection con;
	
	//static Map<Integer, ArrayList<String>> hashMap = new HashMap<Integer,  ArrayList<String>>();
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	Properties prop=new Properties();
	public String setDArTData(HttpServletRequest request, String fname) throws SQLException{
		ManagerFactory factory =null;
		try{
			//String crop=request.getSession().getAttribute("crop").toString();
			session = HibernateSessionFactory.currentSession();
			tx=session.beginTransaction();
			String notMatchingDataDB="";
			prop.load(new FileInputStream(request.getSession().getServletContext().getRealPath("//")+"//WEB-INF//classes//DatabaseConfig.properties"));
		    String host=prop.getProperty("central.host");
		    String port=prop.getProperty("central.port");
		    String url = "jdbc:mysql://"+host+":"+port+"/";
		    String dbName = prop.getProperty("central.dbname");
		    String driver = "com.mysql.jdbc.Driver";
		    String userName = prop.getProperty("central.username"); 
		    String password = prop.getProperty("central.password");
		    
		    Class.forName(driver).newInstance();
		    conn = DriverManager.getConnection(url+dbName,userName,password);
		    Statement stCen=conn.createStatement();
		    
		    
		    String hostL=prop.getProperty("local.host");
		    String portL=prop.getProperty("local.port");
		    String urlL = "jdbc:mysql://"+hostL+":"+portL+"/";
		    String dbNameL = prop.getProperty("local.dbname");
		    //String driver = "com.mysql.jdbc.Driver";
		    String userNameL = prop.getProperty("local.username"); 
		    String passwordL = prop.getProperty("local.password");
		    
		    Class.forName(driver).newInstance();
		    con = DriverManager.getConnection(urlL+dbNameL,userNameL,passwordL);
		    Statement stLoc=con.createStatement();
			

			//DatabaseConnectionParameters local = new DatabaseConnectionParameters("localhost", "3306", "ivis", "root", "root");
			DatabaseConnectionParameters local = new DatabaseConnectionParameters("DatabaseConfig.properties", "local");
			DatabaseConnectionParameters central = new DatabaseConnectionParameters("DatabaseConfig.properties", "central");
			//DatabaseConnectionParameters central = new DatabaseConnectionParameters("localhost", "3306", "ibdb_ivis", "root", "root");
			factory = new ManagerFactory(local, central);
			GermplasmDataManager manager = factory.getGermplasmDataManager();
			
			String dataset_type="DArT";
			String datatype="int";
			//int intDataOrderIndex = 1;
			String strSource="",strDatalist="";
			String strGIDslist="";
			int mid=0;
			String markerId="";
			String germplasmName="";
			String gids1="";
			//int gidsCount=0;
			int m=0;
			int g=0;
			String ErrMsg ="";
			
			List genoDataMarkers = new ArrayList();
			String alertGN="no";
	        String alertGID="no";
	        String notMatchingData="";
	        String notMatchingGIDS="";
	        String notMatchingDataExists="";
	        int MarkerId=0;
	        ArrayList result1=new ArrayList();
	        
			DatasetBean ub=new DatasetBean();
			GenotypeUsersBean usb=new GenotypeUsersBean();	
			//ConditionsBean ubConditions=new ConditionsBean();
					
			
			
			Workbook workbook=Workbook.getWorkbook(new File(fname));
			String[] strSheetNames=workbook.getSheetNames();
			
			ExcelSheetValidations fv = new ExcelSheetValidations();
			//System.out.println("******************************");
			String strFv=fv.validation(workbook, request,"DArT");
			//System.out.println("Valid="+strFv);
			if(!strFv.equals("valid"))
				return strFv;
			
			
			for (int i=0;i<strSheetNames.length;i++){				
				if(strSheetNames[i].equalsIgnoreCase("DArT_Source"))
					strSource = strSheetNames[i];
				
				if(strSheetNames[i].equalsIgnoreCase("DArT_Data"))
					strDatalist = strSheetNames[i];	
				
				if(strSheetNames[i].equalsIgnoreCase("DArT_GIDs"))
					strGIDslist = strSheetNames[i];					
			}

			//////Retrieve the maximum column id from the database
			MaxIdValue uptMId=new MaxIdValue();
			int intDatasetId=uptMId.getMaxIdValue("dataset_id","gdms_dataset",session);
			//int dataset_id=intDatasetId+1;
			int dataset_id=intDatasetId-1;
			
			int size=0;
			String mon="";
			Calendar cal = new GregorianCalendar();
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			if(month>=9) 
				mon=String.valueOf(month+1);
			else 
				mon="0"+(month+1);
			  
			 String curDate=year+ "-" + mon + "-" +day;		 
			
			Sheet sheetSource = workbook.getSheet(strSource);			
			/** reading from Data sheet of template **/
			Sheet sheetData = workbook.getSheet(strDatalist);				
			int rowCount=sheetData.getRows();
			int colCount=sheetData.getColumns();
			//System.out.println(dataset_id+"   "+sheetSource.getCell(1,4).getContents().trim()+"  "+dataset_type+"   "+sheetSource.getCell(1,2).getContents().trim()+"   "+sheetSource.getCell(1,3).getContents().trim()+"     "+curDate+"      "+datatype);

			/** retrieving user id from 'users' table **/
			//int user_id=uptMId.getUserId("userid", "users", "uname", session,sheetSource.getCell(1,1).getContents().trim());
			
			String username=request.getSession().getAttribute("user").toString();
			int user_id=uptMId.getUserId("userid", "users", "uname", session,username);
				
			
			/** retrieving maximum marker id from 'marker' table of database **/
			int maxMarkerId=uptMId.getMaxIdValue("marker_id","gdms_marker",session);
			
			/** retrieving maximum ad_id from 'dart_values' table of database **/
			int maxad_Id=uptMId.getMaxIdValue("ad_id","gdms_dart_values",session);
			
			
			int intDataOrderIndex =uptMId.getMaxIdValue("an_id","gdms_allele_values",session);
			
			
			for (int a=7;a<colCount;a++){				
				germplasmName=germplasmName+sheetData.getCell(a,0).getContents().trim()+",";				
			}
			String str1="";
			//int strCount=0;
			ArrayList gidsDataSheet=new ArrayList();
			ArrayList gidsSheet =new ArrayList();
			/** appending all germplasm names to a variable **/
			for(int g1=7;g1<colCount;g1++){
				str1=str1+sheetData.getCell(g1,0).getContents().trim()+"!~!";
				if(!gidsDataSheet.contains(sheetData.getCell(g1,0).getContents().trim())){
					gidsDataSheet.add(sheetData.getCell(g1,0).getContents().trim());
					//strCount=strCount+1;	
				}			
			}
			
			/** reading gids & germplasm name from gids sheet of template and inserting to 'germplasm_temp' table **/
			Sheet sheetGIDs=workbook.getSheet(strGIDslist);			
			int rows=sheetGIDs.getRows();
			for(int r=1;r<rows;r++){					
				gids1=gids1+sheetGIDs.getCell(0,r).getContents().trim()+"!~!"+sheetGIDs.getCell(1,r).getContents().trim()+",";
				if(!gidsSheet.contains(sheetGIDs.getCell(1,r).getContents().trim())){
					gidsSheet.add(sheetGIDs.getCell(1,r).getContents().trim());
					//gidsCount=gidsCount+1;
				}
			}
			if(gidsSheet.size()!=gidsDataSheet.size()){
				//System.out.println("NOT Matching");
				ErrMsg = "Germplasms in DArT_GIDs sheet doesnot match with the Germplasm in DArT_Data sheet.";
				request.getSession().setAttribute("indErrMsg", ErrMsg);
				return "ErrMsg";
			}
			//System.out.println("gidsCount="+gidsCount+"     strCount="+strCount);
			int s=0;
			//String fGids="";
			String gNames="";
			ArrayList fGids=new ArrayList();
			ArrayList fGNames=new ArrayList();
			//String gidsRet="";
			ArrayList gidsRet=new ArrayList();
			HashMap<Integer, String> GIDsMap = new HashMap<Integer, String>();
			HashMap<String, Integer> GIDsMap1 = new HashMap<String, Integer>();
			ArrayList gidNamesList=new ArrayList();
			/** arranging gid's with respect to germplasm name in order to insert into allele_values table */
			if(gidsSheet.size()==gidsDataSheet.size()){					
				String[] arg1=gids1.split(",");
				String[] str2=str1.split("!~!");
				for(int a=0;a<arg1.length;a++){
					String[] arg2=arg1[a].split("!~!");
					if(str2[s].equals(arg2[1])){
						//gidsRet=gidsRet+arg2[0]+",";
						//if(!gidsRet.contains(Integer.parseInt(arg2[0])))
							gidsRet.add(Integer.parseInt(arg2[0]));
						gNames=gNames+"'"+arg2[1]+"',";
						if(!fGids.contains(arg2[0]))
							fGids.add(arg2[0]);
						
						if(!fGNames.contains(arg2[1]))
							fGNames.add(arg2[1]);
						
						if(!gidNamesList.contains(Integer.parseInt(arg2[0])))
							gidNamesList.add(Integer.parseInt(arg2[0])+","+arg2[1]);
						
						GIDsMap.put(Integer.parseInt(arg2[0]), arg2[1]);
						GIDsMap1.put(arg2[1], Integer.parseInt(arg2[0]));
					}
					s++;	
				}
			}
			
			//System.out.println("GIDsMap="+GIDsMap);
			//System.out.println("*******************"+gidsRet);
			Map<Object, String> sortedMap = new TreeMap<Object, String>(GIDsMap);
			SortedMap mapG = new TreeMap();
            List lstgermpName = new ArrayList();
			ArrayList lstGIDs=new ArrayList();
			//ArrayList lstGIDs=uptMId.getGIds("gid, nval", "names", "gid", session, gidsRet.substring(0,gidsRet.length()-1));
			
			List<Name> names = null;
			/*for(int n=0;n<fGids.size();n++){
				names = manager.getNamesByGID(Integer.parseInt(gidsRet.get(n).toString()), null, null);
				for (Name name : names) {					
					 lstgermpName.add(name.getGermplasmId());
					 mapG.put(name.getGermplasmId(), name.getNval());	
					 addValues(name.getGermplasmId(), name.getNval().toLowerCase());	
		        }
			}*/
			ArrayList gidsDBList = new ArrayList();
			ArrayList gNamesDBList = new ArrayList();
			hashMap.clear();
			for(int n=0;n<fGNames.size();n++){
				List<Germplasm> germplasmList = manager.getGermplasmByName(fGNames.get(n).toString(), 0, new Long(manager.countGermplasmByName(fGNames.get(n).toString(), Operation.EQUAL)).intValue(), Operation.EQUAL);
				for (Germplasm g1 : germplasmList) {
		        	if(!(gidsDBList.contains(g1.getGid()))){
		        		gidsDBList.add(g1.getGid());
		        		gNamesDBList.add(fGNames.get(n).toString());
		        		addValues(fGNames.get(n).toString(), g1.getGid());					        		
		        	}				        	
		           //System.out.println("  " + g.getGid());
		        }
		        //System.out.println(n+":"+gnamesList.get(n).toString()+"   "+hashMap.get(gnamesList.get(n).toString()));
			}
			/*//System.out.println
            
            for(int w=0;w<lstGIDs.size();w++){
                 Object[] strMareO= (Object[])lstGIDs.get(w);
                 lstgermpName.add(strMareO[0]);
                 String strMa123 = (String)strMareO[1];
                 mapG.put(strMareO[0], strMa123);
                 
            }*/
            //Iterator iterator = mapG.keySet().iterator();
	        Iterator iterator1 = sortedMap.keySet().iterator();
           //System.out.println("map=:"+map.size());
           if(gNamesDBList.size()==0){
        	   alertGID="yes";
        	   size=0;
        	   while (iterator1.hasNext()) {
        		   Object key1 = iterator1.next();
        		   notMatchingGIDS=notMatchingGIDS+key1+"   "+sortedMap.get(key1)+",\t";
        	   }
           }
            
          
           int gidToCompare=0;
           String gNameToCompare="";
           //String gNameFromMap="";
           ArrayList gNameFromMap=new ArrayList();
           //System.out.println("gidNamesList="+gidNamesList);
           if(gNamesDBList.size()>0){
	           /*for(int gi=0;gi<gidNamesList.size();gi++){
	        	   String arrP[]=new String[3];
					 StringTokenizer stzP = new StringTokenizer(gidNamesList.get(gi).toString(), ",");
					 int iP=0;
					 while(stzP.hasMoreTokens()){
						 arrP[iP] = stzP.nextToken();
						 iP++;
					 }	
	        	   gidToCompare=Integer.parseInt(arrP[0].toString());
	        	   gNameToCompare=arrP[1].toString();
	        	   //System.out.println("...."+gidToCompare+"   "+lstgermpName.contains(gidToCompare));
	        	   if(lstgermpName.contains(gidToCompare)){
	        		   //gNameFromMap=mapG.get(gidToCompare).toString();
	        		   gNameFromMap=hashMap.get(gidToCompare);
	        		   //System.out.println("...."+gNameToCompare+"   "+map.get(gidToCompare).equals(gNameToCompare)+"  from map: "+map.get(gidToCompare));
	        		   //if(!(gNameFromMap.toLowerCase().equals(gNameToCompare.toLowerCase()))){
	        		   if(!(gNameFromMap.contains(gNameToCompare.toLowerCase()))){
	        			   notMatchingData=notMatchingData+gidToCompare+"   "+hashMap.get(gidToCompare)+"\n\t";
	        			   alertGN="yes"; 
	        		   }			        			   
	        	   }else{
	        		   alertGID="yes";
	        		   size=sortedMap.size();
	        		   notMatchingGIDS=notMatchingGIDS+gidToCompare+", ";
	        	   }
	           }*/
	           for(int n=0;n<fGNames.size();n++){
        		   if(gNamesDBList.contains(fGNames.get(n))){
        			   if(!(hashMap.get(fGNames.get(n).toString()).contains(GIDsMap1.get(fGNames.get(n).toString())))){
        				   notMatchingData=notMatchingData+fGNames.get(n)+"   "+GIDsMap1.get(fGNames.get(n).toString())+"\n\t";
        				   notMatchingDataDB=notMatchingDataDB+fGNames.get(n)+"="+hashMap.get(fGNames.get(n))+"\t";
		        		   alertGN="yes";
        			   }
        		   }else{
        			   //int gid=GIDsMap.get(gnamesList.get(n).toString());
        			   alertGID="yes";
	        		   size=hashMap.size();
	        		   notMatchingGIDS=notMatchingGIDS+fGNames.get(n).toString()+", ";
        		   }
        	   }
           }
           if((alertGN.equals("yes"))&&(alertGID.equals("no"))){
        	   //String ErrMsg = "GID(s) ["+notMatchingGIDS.substring(0,notMatchingGIDS.length()-1)+"] of Germplasm(s) ["+notMatchingData.substring(0,notMatchingData.length()-1)+"] being assigned to ["+notMatchingDataExists.substring(0,notMatchingDataExists.length()-1)+"] \n Please verify the template ";
        	   ErrMsg = "Please verify the name(s) provided \t "+notMatchingData+" which do not match the GID(s) present in the database"+notMatchingDataDB;
        	   request.getSession().setAttribute("indErrMsg", ErrMsg);
        	   return "ErrMsg";	 
           }
           if((alertGID.equals("yes"))&&(alertGN.equals("no"))){	        	   
        	  if(size==0){
        		   //ErrMsg = "The GIDs provided do not exist in the database. \n Please upload the relevant germplasm information to the GMS ";
        		   ErrMsg = "The Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook ";
        	   }else{
        		   ErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS;
        		   //ErrMsg = "Please verify the GID/Germplasm(s) provided as some of them do not exist in the database. \n Please upload germplasm information into GMS ";
        	   }	        	   
        	   //ErrMsg = "Please verify the following GID/Germplasm(s) doesnot exists. \n Upload germplasm Information into GMS \n\t"+notMatchingGIDS;
        	   request.getSession().setAttribute("indErrMsg", ErrMsg);
        	   return "ErrMsg";
           }
		
           if((alertGID.equals("yes"))&&(alertGN.equals("yes"))){
        	   ErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please upload the relevant germplasm information through the Fieldbook \n \t"+notMatchingGIDS+" \n Please verify the name(s) provided "+notMatchingData+" which do not match the GIDS(s) present in the database "+notMatchingDataDB;
        	   request.getSession().setAttribute("indErrMsg", ErrMsg);
        	   return "ErrMsg";	 
           }
			//System.out.println("fGids="+fGids);
			String dname=sheetSource.getCell(1,2).getContents().trim();
			
			//Query rsDatasetNames=session.createQuery("from DatasetBean where dataset_name ='"+dname+"'");				
			//List result1= rsDatasetNames.list();
			
			ResultSet rsLoc=stLoc.executeQuery("select dataset_name from gdms_dataset where dataset_name='"+dname+"'");
			while(rsLoc.next()){
				result1.add(rsLoc.getString(1));						
			}

			ResultSet rsCen=stCen.executeQuery("select dataset_name from gdms_dataset where dataset_name='"+dname+"'");
			while(rsCen.next()){
				result1.add(rsCen.getString(1));	
			}
			
			//System.out.println(".............:"+result1.size());
			if(result1.size()>0){
				ErrMsg = "Dataset Name already exists.";
				request.getSession().setAttribute("indErrMsg", ErrMsg);							
				return "ErrMsg";
			}
			
			
			if(dname.length()>30){
				ErrMsg = "Dataset Name value exceeds max char size.";
				request.getSession().setAttribute("indErrMsg", ErrMsg);							
				return "ErrMsg";
			}
			//** Writing from source sheet of Template to 'dataset' table  **//*
			ub.setDataset_id(dataset_id);
			ub.setDataset_name((String)sheetSource.getCell(1,2).getContents().trim());
			ub.setDataset_desc((String)sheetSource.getCell(1,3).getContents().trim());
			ub.setDataset_type(dataset_type);
			ub.setGenus(sheetSource.getCell(1,4).getContents().trim());
			ub.setSpecies(sheetSource.getCell(1,5).getContents().trim());
			ub.setUpload_template_date(curDate);					
			ub.setDatatype(datatype);
			
			ub.setInstitute(sheetSource.getCell(1,0).getContents().trim());
			ub.setPrincipal_investigator(sheetSource.getCell(1,1).getContents().trim());
			
			
			//ub.setMissing_data(missing_data);
			session.save(ub);
			
			
			//************* inserting into 'dataset_users' table  *************//*
			usb.setDataset_id(dataset_id);
			usb.setUser_id(user_id);			
			session.save(usb);
			
			SortedMap mapN = new TreeMap();
			//System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
			ArrayList finalList =new ArrayList();
			ArrayList gidL=new ArrayList();
					
			/*
			 * getting nids with gid and nval for inserting into gdms_acc_metadataset table			
			*/
			Name name = null;
			for(int n=0;n<fGids.size();n++){
				/*name = manager.getNameByGIDAndNval(Integer.parseInt(fGids.get(n).toString()), fGNames.get(n).toString());
				if(!gidL.contains(name.getGermplasmId()))
	            	gidL.add(name.getGermplasmId());
	            mapN.put(name.getGermplasmId(), name.getNid());*/
	            //name = manager.getNameByGIDAndNval(Integer.parseInt(fGids.get(n).toString()), fGNames.get(n).toString(), GetGermplasmByNameModes.STANDARDIZED);
				name = manager.getNameByGIDAndNval(Integer.parseInt(fGids.get(n).toString()), fGNames.get(n).toString(), GetGermplasmByNameModes.STANDARDIZED);
				if(name==null){
					name=manager.getNameByGIDAndNval(Integer.parseInt(fGids.get(n).toString()), fGNames.get(n).toString(), GetGermplasmByNameModes.NORMAL);
				}
				if(!gidL.contains(name.getGermplasmId()))
	            	gidL.add(name.getGermplasmId());
	            mapN.put(name.getGermplasmId(), name.getNid());
				
			}
			
	       
	        for(int a=0;a<fGids.size();a++){
	        	int gid1=Integer.parseInt(fGids.get(a).toString());
	        	if(gidL.contains(gid1)){
	        		finalList.add(gid1+"~!~"+mapN.get(gid1));	
	        	}
	        }
            //System.out.println("******************  "+finalList);
	        
	        /** writing to acc_metadataset table  **/
			/*for(int r=1;r<rows;r++){					
				AccessionMetaDataBean amdb=new AccessionMetaDataBean();					
				//******************   GermplasmTemp   *********************//*	
				amdb.setDataset_id(dataset_id);
				amdb.setGid(Integer.parseInt(sheetGIDs.getCell(0,r).getContents().trim()));
				
				session.save(amdb);
				
				
				if (r % 1 == 0){
                    session.flush();
                    session.clear();
				}
				
			}*/
            
            for(int i=0;i<finalList.size();i++){	
            	String[] strList=finalList.get(i).toString().split("~!~");
            	AccessionMetaDataBean amdb=new AccessionMetaDataBean();					
				//******************   GermplasmTemp   *********************//*	
				amdb.setDataset_id(dataset_id);
				amdb.setGid(Integer.parseInt(strList[0].toString()));
				amdb.setNid(Integer.parseInt(strList[1].toString()));
				
				session.save(amdb);
				
				if (i % 1 == 0){
					session.flush();
					session.clear();
				}
            
            }
            
            String markersForQuery="";
            HashMap<String, Object> markersMap = new HashMap<String, Object>();
            
            
            List lstMarkers = new ArrayList();
			//** checking whether marker exists in the database if exists using the marker id other wise inserting to the 'marker' table **//*
			String markersList="";
			//HashMap<String, Object> map = new HashMap<String, Object>();
			for (int r=1;r<rowCount;r++){
				markersList = markersList +"'"+ sheetData.getCell(1,r).getContents().trim().toString()+"',";
			}
			markersForQuery=markersList.substring(0, markersList.length()-1);
			ResultSet rsML=stLoc.executeQuery("select distinct marker_id, marker_name from gdms_marker where Lower(marker_name) in ("+markersForQuery.toLowerCase()+")");
            ResultSet rsMC=stCen.executeQuery("select distinct marker_id, marker_name from gdms_marker where Lower(marker_name) in ("+markersForQuery.toLowerCase()+")");
            
            while(rsMC.next()){
            	//lstMarIdNames.add(rsMC.getString(2)+":"+rsMC.getString(1));
            	lstMarkers.add(rsMC.getString(2));
            	markersMap.put(rsMC.getString(2), rsMC.getInt(1));		
            }
            while(rsML.next()){	            		
            	if(!lstMarkers.contains(rsML.getString(2))){
            		lstMarkers.add(rsML.getString(2));
            		//lstMarIdNames.add(rsML.getString(2)+":"+rsML.getString(1));
            	}
            	markersMap.put(rsML.getString(2), rsML.getInt(1));	
            }
			
			
			/* ArrayList lstMarIdNames=uptMId.getMarkerIds("marker_id, marker_name", "gdms_marker", "marker_name", session, markersList.substring(0, markersList.length()-1));
	          
	            List lstMarkers = new ArrayList();
	            for(int w=0;w<lstMarIdNames.size();w++){
	                 Object[] strMareO= (Object[])lstMarIdNames.get(w);
	                 lstMarkers.add(strMareO[1]);
	                 String strMa123 = (String)strMareO[1];
	                 map.put(strMa123, strMareO[0]);
	                 
	            }*/
			ArrayList mids=new ArrayList();
			//System.out.println("map"+markersMap);
			//System.out.println("lstMarkers="+lstMarkers);
			//**  inserting data from data sheet of template to database  **//*		
			//maxad_Id=maxad_Id+1;
			maxad_Id=maxad_Id-1;
			for (int im=1;im<rowCount;im++){
				DArTDetailsBean dartBean=new DArTDetailsBean();
				MarkerInfoBean mib=new MarkerInfoBean();
				String marker=sheetData.getCell(1,im).getContents().trim().toString();
				if(lstMarkers.contains(marker)){
					MarkerId=Integer.parseInt(markersMap.get(marker).toString());
					if(!(mids.contains(MarkerId)))
						mids.add(MarkerId);
				}else{
					//MarkerId=maxMarkerId+1;
					MarkerId=maxMarkerId-1;
					if(!(mids.contains(MarkerId)))
						mids.add(MarkerId);
					mib.setMarkerId(MarkerId);
					mib.setMarker_type(dataset_type);
					mib.setMarker_name(sheetData.getCell(1,im).getContents().trim().toString());
					//mib.setCrop(sheetSource.getCell(1,3).getContents().trim());
					mib.setSpecies(sheetSource.getCell(1,5).getContents().trim());
					session.saveOrUpdate(mib);
				}
				//mid=uptMId.getUserId("marker_id", "marker", "marker_name", session,sheetData.getCell(1,im).getContents().trim());
				/*if(mid==0){
					maxMarkerId=maxMarkerId+1;
					mib.setMarkerId(maxMarkerId);
					mib.setMarker_type(dataset_type);
					mib.setMarker_name(sheetData.getCell(1,im).getContents().trim().toString());
					mib.setCrop(sheetSource.getCell(1,3).getContents().trim());
					session.saveOrUpdate(mib);
				}else{
					maxMarkerId= mid;
				}*/
				markerId=markerId+MarkerId+",";
				//** inserting into 'dart_details' table **//*
				dartBean.setAd_id(maxad_Id);
				dartBean.setDataset_id(dataset_id);
				dartBean.setMarker_id(MarkerId);
				dartBean.setClone_id(Integer.parseInt(sheetData.getCell(0,im).getContents().trim()));
				dartBean.setQvalue(Float.parseFloat(sheetData.getCell(2,im).getContents().trim()));
				dartBean.setReproducibility(Float.parseFloat(sheetData.getCell(3,im).getContents().trim()));
				dartBean.setCall_rate(Float.parseFloat(sheetData.getCell(4,im).getContents().trim()));
				dartBean.setPic_value(Float.parseFloat(sheetData.getCell(5,im).getContents().trim()));
				dartBean.setDiscordance(Float.parseFloat(sheetData.getCell(6,im).getContents().trim()));
				session.save(dartBean);
				if (im % 1 == 0){
					session.flush();
					session.clear();
				}
				//maxad_Id++;
				maxad_Id--;
				//m++;
				//maxMarkerId++;
				maxMarkerId--;
			}			
			
			
			String[] markers=markerId.split(",");
			//String[] accessions=germplasmName.split(",");		
			
			int kk=0;
			//intDataOrderIndex=intDataOrderIndex+1;
			intDataOrderIndex=intDataOrderIndex-1;
			//** inserting data into 'allele_values' table **//*
			for(int i=1;i<rowCount;i++){	
				//String[] insGids=fGids.split(",");
				for(int j=7;j<colCount;j++){
					IntArrayBean intB=new IntArrayBean();
					IntArrayCompositeKey cack = new IntArrayCompositeKey();
					cack.setDataset_id(dataset_id);
					//cack.setDataorder_index(intDataOrderIndex);
					cack.setAn_id(intDataOrderIndex);
					intB.setComKey(cack);
					//intB.setGid(Integer.parseInt(insGids[kk]));
					intB.setGid(Integer.parseInt(gidsRet.get(kk).toString()));
					intB.setMarker_id(Integer.parseInt(markers[m]));
					//chb.setAllele_raw_value((String)sheetData.getCell(j,i).getContents().trim());
					intB.setAllele_bin_value((String)sheetData.getCell(j,i).getContents().trim());
					kk++;
					g++;
					//intDataOrderIndex++;
					intDataOrderIndex--;
					session.save(intB);
					if (g % 1 == 0){
						session.flush();
						session.clear();
					}
					
				}
				kk=0;
				m++;
				g=0;
			}	
			//System.out.println("DONE");
			for(int m1=0;m1<mids.size();m1++){					
				//System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
				MarkerMetaDataBean mdb=new MarkerMetaDataBean();					
				//******************   GermplasmTemp   *********************//*	
				mdb.setDataset_id(dataset_id);
				mdb.setMarker_id(Integer.parseInt(mids.get(m1).toString()));
				
				session.save(mdb);
				if (m1 % 1 == 0){
                    session.flush();
                    session.clear();
				}			
			}	
			tx.commit();
			str="inserted";
			/*if(rsCen!=null)rsCen.close();
			if(rsLoc!=null) rsLoc.close();
			if(rsML!=null) rsML.close(); if(rsMC!=null)rsMC.close();
			if(stCen!=null) stCen.close(); if(stLoc!=null ) stLoc.close();*/
			con.close();conn.close();
			
		}catch(Exception e){
			tx.rollback();
			session.clear();
			e.printStackTrace();
		}finally{	
			con.close(); conn.close();
			factory.close();
			session.clear();	
			session.disconnect();
		}
		return str;
	}
	private static void addValues(String key, Integer value){
		ArrayList<Integer> tempList = null;
		if(hashMap.containsKey(key)){
			tempList=hashMap.get(key);
			if(tempList == null)
				tempList = new ArrayList<Integer>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMap.put(key,tempList);
	}

}

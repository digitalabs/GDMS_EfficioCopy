<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ page import="java.util.*" %>
<html:html>
	<head>
		<title>GDMS</title>
		<link rel="stylesheet" type="text/css" href="<html:rewrite forward='GDMSStyleSheet'/>">
	</head>
	<body>
		<html:form action="/export.do" method="post">
		<div class="heading" align="center">Genotyping Data Retrieval</div>
		<!--<table width='35%' border=0 align=right>
				<tr><td align=left nowrap class="displayBoldText" width="20%">Species</td><td><b>:</b></td><td align=left nowrap class="displayBoldText"><%=session.getAttribute("crop") %></td></tr>		
			</table>
		--><br><br>
			<%String[] maps=session.getAttribute("mapsSTR").toString().split(";;");
			session.setAttribute("type","map");
			%>
	  			
	  			<table width="65%" align="center" border=0 cellpadding=2 cellspacing=2>
	  			<tr class="displayText"><td>Please select the desired map for creating export format of CMTV</td></tr>
	  			<tr><td>
	  			<table align="left" width="80%" border=0>
	  			<%for(int m=0;m<maps.length;m++){
	  				String[] str=maps[m].split("!~!");
	  		%>
	  			<tr class="displayText">
	  				<td width="45%"><input type="checkbox" name="maps" value='<%=str[1]%>'>&nbsp;<%=str[1]%></td>
	  				<td> Markers : <%=str[0] %></td>
	  				<%if(str[3].equals("genetic")){ %>
	  					<td>Map Length : <%=str[2] %> cM</td>
	  				<%}else{ %>
	  					<td>Map Length : <%=str[2] %> bp</td>
	  				<%} %>
	  			</tr>
	  		<%} 
	  			%>
	  			</table>
	  			</td>
	  			</tr>
	  			<tr><td>&nbsp;</td></tr>
	  			<tr><td>&nbsp;</td></tr>
	  			<tr><td>&nbsp;</td></tr>
	  			<tr align="center"><td><html:button property="export" value="Submit" onclick="sub()"/></td></tr>
	  			</table>
	  			<html:hidden property="selMaps"/>
	  			<html:hidden property="opType"/>
		</html:form>
	
	</body>

</html:html>
<script>
function sub(){		
	var temp="";
	document.forms[0].opType.value="map";
	var c=0;	
	 for (var i=0; i<document.forms[0].elements.length; i++){
		    obj = document.forms[0].elements[i];
		    if (obj.type == "checkbox" && obj.checked) {
		    	//countn="true";	
		    	c++;
				temp=temp+"'"+obj.value+"',"; 
		    }          
	    }		
	//alert("temp="+temp);
	document.forms[0].selMaps.value=temp;		
	document.forms[0].submit();

}

</script>


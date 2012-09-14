<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib prefix="liferay-portlet" uri="http://liferay.com/tld/portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%@ page import="com.liferay.portal.kernel.util.Validator" %>
<%@ page import="com.liferay.portlet.PortletPreferencesFactoryUtil" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="java.util.Map" %>
<portlet:defineObjects />

<form action="<liferay-portlet:actionURL portletConfiguration="true" />" method="post" name="<portlet:namespace />fm"> 

<input name="<portlet:namespace /><%=Constants.CMD%>" type="hidden" value="<%=Constants.UPDATE%>" /> 
<%
PortletPreferences preferences = renderRequest.getPreferences();

String portletResource = ParamUtil.getString(request, "portletResource");
if (Validator.isNotNull(portletResource)) {
	preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);
}
String eadDisplayFriendlyUrl = preferences.getValue("eadDisplayFriendlyUrl", "");
String eadDisplayPortletName = preferences.getValue("eadDisplayPortletName", "");

%>
<table class="lfr-table">

<tr>
	<td>EAD Display Friendly Url:</td>
	<td><input  type="text" name="<portlet:namespace />preferences--eadDisplayFriendlyUrl--" value="<%=eadDisplayFriendlyUrl %>"/></td>
</tr>
<tr>
	<td>EAD Display Portlet name:</td>
	<td><input  type="text" name="<portlet:namespace />preferences--eadDisplayPortletName--" value="<%=eadDisplayPortletName %>"/> </td>
</tr>
</table>  
<input type="button" value="Save" onClick="submitForm(document.<portlet:namespace />fm);" /> </form>
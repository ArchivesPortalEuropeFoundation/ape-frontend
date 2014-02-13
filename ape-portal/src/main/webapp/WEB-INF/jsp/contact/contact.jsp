<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="portal" uri="http://portal.archivesportaleurope.eu/tags"%>
<%@ taglib prefix="liferay-portlet" uri="http://liferay.com/tld/portlet"%>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>

<%-- reCaptcha--%>
<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>

<portlet:actionURL var="contactUrl">
    <portlet:param name="myaction" value="contact" />
</portlet:actionURL>

<script type='text/javascript'>
	$(document).ready(function() {
		initContactForm();
	});
</script>

<script type="text/javascript">
	 var RecaptchaOptions = {
	    theme : 'white'
	 };
</script>

<div align="center">
    <h2><fmt:message key="contact.form.title" /></h2>
</div>

<form:form id="contactForm" name="contactForm" commandName="contact" method="post" action="${contactUrl}">
    <table class="contactForm">
        <tr>
            <td class="tdLabel">
                <label for="contact_email" class="label"><fmt:message key="label.email.contact" /><span class="required">*</span>:</label>
            </td>
            <td>
                <form:input path="email" type="text" name="email" size="50" id="contact_email" />
            </td>
            <td>
                <form:errors path="email" cssClass="error" />
            </td>
        </tr>
        <tr>
            <td class="tdLabel">
                <label for="contact_topicSubject" class="label"><fmt:message key="label.email.subject" /><span class="required">*</span>:</label>
            </td>
            <td>
                <form:select path="type" name="topicSubject" id="contact_topicSubject">
                    <option value="-1">--- <fmt:message key="label.contact.items.select" /> ---</option>
                    <option value="1"><fmt:message key="label.contact.item.issues" /></option>
                    <option value="2"><fmt:message key="label.contact.item.contribute" /></option>
                    <option value="3"><fmt:message key="label.contact.item.suggestions" /></option>
                    <option value="4"><fmt:message key="label.contact.item.feedback" /></option>
                </form:select>
            </td>
            <td>
                <form:errors path="type" cssClass="error" />
            </td>
        </tr>
        <tr>
            <td class="tdLabel">
                <label for="contact_feedbackText" class="label"><fmt:message key="label.feedback.comments" /><span class="required">*</span>:</label>
            </td>
            <td>
                <form:textarea path="feedback" name="feedbackText" cols="" rows="" id="contact_feedbackText" />
            </td>
            <td>
                <form:errors path="feedback" cssClass="error" />
            </td>
        </tr>
        <c:if test="${!empty loggedIn}"><!-- you are logged -->
        	<!-- no need for captcha -->
        	<script type="text/javascript">
        		document.getElementById('contact_email').value = '${eMail}';
        	</script>
        </c:if>
        <c:if test="${empty loggedIn}"><!-- then you are not logged -->     
	        <tr>
	            <td colspan="2">
					<script type="text/javascript" src="http://api.recaptcha.net/challenge?k='${contact.recaptchaPubKey}'"></script>
				</td>
	           <td>
	                <form:errors path="captcha" cssClass="error" />
	            </td>
	        </tr> 
        </c:if>
        <tr>
            <td colspan="3">
                <fmt:message key="feedbackText.info.tips" />
            </td>
        </tr>
        <tr>
            <td colspan="3">
                <table>
                    <tr>
                        <td class="leftBtn">
                            <input type="submit" id="contact_label_feedback_send" value="<fmt:message key="label.feedback.send" />" class="mainButton"/>
                        </td>
                        <td class="rightBtn">
                            <input type="reset" name="label.reset" value="<fmt:message key="label.reset" />"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</form:form>

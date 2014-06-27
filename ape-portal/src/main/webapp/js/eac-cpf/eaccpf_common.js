
/**
 * Function to delete the data not necessary 
 */
function eraseData(){
	eraseComma();
	eraseNameTitle();
	eraseLocationPlace();
}
/**
 * Function to delete the first comma in the dates when there is not characters before it 
 */
function eraseComma(){
	$("div#eaccpfcontent span.nameEtryDates").each(function(){
		var stringDates = $(this).text();
		var firstCharacter=stringDates.charAt(0);
		if (firstCharacter == ','){
			stringDates=$.trim(stringDates.substring(1));
		}
		$(this).text(stringDates);
	});
}
/**
 * Function to delete the name show in title form the alternatives names.
 */
function eraseNameTitle() {
	var titleName = $.trim($("div#eaccpfcontent span#nameTitle").text());
	var found = false;
	$("div#alternativeName").children().each(function() {
		if ($.trim($(this).text()) == titleName && !found){
			$(this).remove();
			found = true;
		}
	});
	var textRightColumn = $.trim($("div#alternativeName").find("p").text());
	if (textRightColumn == ''){
		$("div#titleAlternativeName").remove();
	}
}
/**
 * Function to delete the location if there is'nt nothing to show.
 */
function eraseLocationPlace(){
	$("div#eaccpfcontent .locationPlace").each(function(){
		var textRightColumn = $.trim($(this).find(".rightcolumn p").text());
		if (textRightColumn == ''){
			$(this).remove();
		}
	});
}
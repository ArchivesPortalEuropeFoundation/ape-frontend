
/**
 * Function to delete the data not necessary 
 */
function eraseData(){
	eraseEmptyRow();
	eraseDuplicatedArchivalLi();
	eraseComma();
	eraseNameTitle();
	eraseLocationPlace();
	eraseList();
	eraseEmptyLi();
	eraseEmptyTitleSection();
	eraseExistDates();
}

function eraseDuplicatedArchivalLi(){
	var targetTexts = new Array();
	var targetCodes = new Array();
	var i = 0;
	$("div#archives").find("li").each(function(){
		if($(this).find("a").length>0){
			if(i++==0){
				targetCodes.push($(this).find("a[href]").attr("href"));
				targetTexts.push($(this).text());
			}else{
				var found = false;
				for(var x=0;!found && x<targetCodes.length;x++){
					if(targetCodes[x]==$(this).find("a[href]").attr("href")){
						found = true;
						$(this).remove();
					}
				}
				if(!found){
					targetCodes.push($(this).find("a[href]").attr("href"));
					targetTexts.push($(this).text());
				}
			}
		}else{
			var deleted = false;
			for(var x=0;!deleted && x<targetTexts.length;x++){
				if(targetTexts[x]==$(this).text()){
					$(this).remove();
					deleted = true;
				}
			}
			if(!deleted){
				targetTexts.push($(this).text());
			}
		}
	});
	
	targetTexts = new Array();
	$("div#archives").find("li").find("a").each(function(){
		var texts = $(this).text();
		targetTexts.push(texts);
	});
	
	$("div#archives").find("li").not(":has(a)").each(function(){
		for(var i=0;i<targetTexts.length;i++){
			if($(this).text()==targetTexts[i]){
				$(this).remove();
			}
		}
	});
	
	var textToBeChanged = $("div#archives .boxtitle").find("span.text").text();
	if (textToBeChanged.indexOf("(") != -1 && textToBeChanged.indexOf(")") != -1) {
		textToBeChanged = textToBeChanged.substring(0, textToBeChanged.indexOf("("));
		textToBeChanged += "("+$("div#archives").find("li").length+")";
		$("div#archives .boxtitle").find("span.text").html(textToBeChanged);
	}
}

function eraseEmptyLi(){
	eraseEmptyLiByName("material");
	eraseEmptyLiByName("persons");
	eraseEmptyLiByName("archives");
	eraseEmptyLiByName("alternative");
}

function eraseEmptyLiByName(name){
	var counter = 0;
	$("#"+name).find("li:empty").each(function(){
		$(this).remove();
		counter++;
	});
	if(counter>0){
		var target = $("#"+name).find(".boxtitle").find(".text");
		var text = target.html();
		var figure = parseInt(text.substring(text.indexOf("(")+1,text.indexOf(")")));
		figure-=counter;
		text = text.substring(0,text.indexOf("("))+"("+figure+")";
		target.html(text);
	}
}
/**
 * Function to delete the first comma in the dates when there is not characters before it 
 */
function eraseComma(){
	$("div#eaccpfcontent span.nameEtryDates").each(function(){
		var stringDates = $(this).html();
		var startStringDates = "";
		var endStringDates = "";
		//strip span tags
		if(stringDates.length > 0 && stringDates.indexOf(">")!==undefined && stringDates.lastIndexOf("<")){
			startStringDates = stringDates.substring(0,stringDates.indexOf(">")+1);
			endStringDates = stringDates.substring(stringDates.lastIndexOf("<"),stringDates.length);
			stringDates = stringDates.substring(stringDates.indexOf(">")+1,stringDates.lastIndexOf("<"));
		}
		var firstCharacter=stringDates.charAt(0);
		if (firstCharacter == ','){
			stringDates=$.trim(stringDates.substring(1));
		}
		$(this).html(startStringDates+stringDates+endStringDates);
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
 * Function to delete the location if there isn't nothing to show.
 */
function eraseLocationPlace(){
	$("div#eaccpfcontent .locationPlace").each(function(){
		var textRightColumn = $.trim($(this).find(".rightcolumn p").text());
		if (textRightColumn == ''){
			$(this).remove();
		}
	});
}
/**
 * Function to delete the <ul class="level"> that has not <li class="item">
 */
function eraseList(){
	$("div#eaccpfcontent ul.level").each(function(){
		var textRightColumn = $.trim($(this).find("li.item").text());
		if (textRightColumn == ''){
			$(this).remove();
		}
	});
}
/**
 * Function to delete the titles when there aren't sections to display
 * 
 */
function eraseEmptyTitleSection(){
	
	$(".blockPlural").find(".blockSingular:empty").each(function(){
		$(this).remove();
	});
	
	$(".blockPlural").each(function(){
		if($(this).children().length==0){
			$(this).remove();
		}
	});
	
	$(".section").each(function(){
		if($(this).children().length==0){
			var before = $(this).prev();
			if(before.is("h2") && before.hasClass("title")){
				before.remove();
			}
			$(this).remove();
		}
	});
}
/**
 * This function must to delete the existDates if it's appear in the nameEntry
 */
function eraseExistDates(){
	var pattern = /[\d]{4}/;
	var noPattern = /[^\d{4}]/;	
	var titleName = $.trim($("div#eaccpfcontent h1 span#nameTitle").text());
	var name = titleName.split(noPattern); 
	var existDate = $.trim($("div#eaccpfcontent  h1 span.existDates").text());
	existDate = existDate.split(noPattern);
	var nameArray = new Array();
	var dateArray = new Array();
	nameArray = makeArray(name, nameArray, pattern);
	dateArray = makeArray(existDate, dateArray, pattern);
	var found = false;
	$.each(dateArray, function(k,value){
		if ($.inArray(dateArray[k], nameArray)!=-1 && !found){
			found = true; 
		}
	});
	
	if(found){
		var title = $("div#eaccpfcontent h1 span#nameTitle").html();
		$("div#eaccpfcontent  h1 span.nameEtryDates").remove();
		$("div#eaccpfcontent  h1 span#nameTitle").remove();
		$("div#eaccpfcontent h1").html(title+'<span id="nameTitle" class="hidden">'+title+'</span>');
	}
}
function makeArray(dates, arrayDates, pattern){
	$.each(dates, function(i,val){
		if (dates[i].match(pattern) != null){
			if (dates[i].length < 5){
				arrayDates.push(dates[i]); 
			}
		}
	}); 
	return arrayDates;
}
/**
 * Function to delete the row if there isn't nothing to show.
 */
function eraseEmptyRow(){
	$("div#eaccpfcontent .row").each(function(){
		if($(this).find(".rightcolumn p").length > 0){
			var textRightColumn = $.trim($(this).find(".rightcolumn p").text()); 
			if (textRightColumn == ''){
				$(this).remove();
			}
		}
	});
}
function displayResult() {
	var result = document.getElementById('searchResult');

	var searchString = document.getElementById('searchName').value.toLowerCase();
	
	// Clear previous result
	while (result.hasChildNodes()) { 
		result.removeChild(result.lastChild);
	}
	// Find and display result
	var found = false;
	for (i = 0; i < searchValues.length; i++) {
		if (searchValues[i][0].match(searchString)) {
			var link = document.createElement("a");
			link.setAttribute("href", makeLinkToIndi(searchValues[i][1]));
			link.appendChild(document.createTextNode(searchValues[i][2]));
			result.appendChild(link);
			result.appendChild(document.createElement("br"));
			found = true;
		}
	}
	if (! found) {
		result.appendChild(document.createTextNode("{noSearchResults}"));
	}

	// Display it
	result.style.display='';
	return false; // Do not submit form...
}

function makeLinkToIndi(id) {
	var link = "";
	var numbers = 2;
	while (id > 99) {
		var curr = id % 100;
		id = (id - curr) / 100;
		numbers += 2;
		link = curr + "/" + link;
		if (curr < 10) link = "0" + link;
	}
	link = id + "/" + link;
	if (id < 10) link = "0" + link;
	return "indi" + numbers + "/" + link + "{indexFile}";
}

function displayAdvanced() {
	document.getElementById('searchAdvanced').style.display = '';
	document.getElementById('searchSimple').style.display = 'none';
}
function displaySimple() {
	document.getElementById('searchAdvanced').style.display = 'none';
	document.getElementById('searchSimple').style.display = '';
}
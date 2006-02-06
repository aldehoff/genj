<?php

	// read stdinput - check header
	$in = fopen("php://input", "rb");
	if (trim(fgets($in))!='GEOQ') die('PING');
	
	// connect to database
	$link = mysql_connect('mysql4-g', 'g46817admin', 'genjadmin')
    	or die('error:connect ' . mysql_error());
	mysql_select_db('g46817_geo') 
    	or die('error:db');

	// read stdinput - lines "city;state;country" by one
	while ( ($line=fgetcsv($in, 100, ";")) !== FALSE) {

		// grab city,jurisdiction,country
		$city = $line[0];
		$like = rtrim($city, "*");
		if (strlen($like)<3) continue;
		$jurisdiction = $line[1];
		$country = $line[2];
		
		// equals or LIKE?
		$op = "=";
		if ($like!=$city) {
			$city = "$like%";
			$op = " LIKE ";	
		}

		// prepare location query "city, jurisdiction name, country, lat, lon"
		$lquery = 
			"SELECT locations.city, jurisdictions.name, locations.country, locations.lat, locations.lon " .
			"FROM locations LEFT JOIN jurisdictions ON jurisdictions.jurisdiction=locations.jurisdiction AND jurisdictions.country=locations.country AND jurisdictions.preferred=1 " .
			"WHERE locations.city".$op."'$city'";
	  
		// try to lookup country?
		if (strlen($country)>0)
	  		$lquery = "$lquery AND locations.country = '$country'";
	  
		// try to lookup a jurisdiction?
		if (strlen($jurisdiction)>0) {

			// prepare query for matching names
			$jquery = "SELECT jurisdiction FROM jurisdictions WHERE name LIKE '$jurisdiction'";
		  
			// add country qualifier if available
			if (strlen($country)>0)
		  		$jquery = "$jquery AND country = '$country'";
	
			// query and apply jurisdiction - if exactly one!
			$rows = mysql_query($jquery);
			if (mysql_num_rows($rows)==1) {
		  		$row = mysql_fetch_row($rows);
		  		$lquery = "$lquery AND jurisdictions.jurisdiction = '$row[0]'";
			}
			mysql_free_result($rows);
		}
		
		// query and return rows
		$rows = mysql_query($lquery);
		for ($i=0 ; $row = mysql_fetch_row($rows) ; $i++) {
			if ($i>0) echo ";";
			echo "$row[0],$row[1],$row[2],$row[3],$row[4]";
		}
		echo "\n";

		// next
		mysql_free_result($rows);
	}
	fclose($in);

	// db cleanup
	mysql_close($link);

?>
    
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
		if (strlen($city)<3) continue;
		$jurisdiction = $line[1];
		$country = $line[2];

		// prepare location query		
	  $lquery = 
			"SELECT jurisdictions.name, locations.country, locations.lat, locations.lon " .
			"FROM locations LEFT JOIN jurisdictions ON locations.jurisdiction = jurisdictions.jurisdiction AND locations.country=jurisdictions.country " .
			"WHERE locations.city='$city'";
	  
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
	
			// query and use jurisdiction - if exactly one
		  $rows = mysql_query($jquery);
		  if (mysql_num_rows($rows)==1) {
		  	$row = mysql_fetch_row($rows);
		  	$lquery = "$lquery AND jurisdictions.jurisdiction = '$row[0]'";
		  }
			mysql_free_result($rows);
		}
		
	  // query and return rows
	  echo "$city:";
	  $rows = mysql_query($lquery);
		while ($row = mysql_fetch_row($rows)) 
		  echo "$row[0],$row[1],$row[2],$row[3];";
	  echo "\n";

		// next
		mysql_free_result($rows);
	}
	fclose($in);

	// db cleanup
	mysql_close($link);

?>
    
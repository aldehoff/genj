<?php

	/////////////////////////////////////////////////
	// Open database connection
	function openDB() {
		// read password
		$credentials = file("geoq.ini");
		if (!$credentials) die("error:credentials");
		$user = trim($credentials[0]);
		$pass = trim($credentials[1]);
		// connect to database
		mysql_connect("mysql4-g", $user, $pass)
			or die("error:connect " . mysql_error());
		mysql_select_db("g46817_geo") 
			or die("error:db");
		// done
	}

	////////////////////////////////////////////////
	// Track user request
	function track($token, $hits) {

		// lookup current ip
		$ip = $_SERVER['REMOTE_ADDR'];

		// update track
		mysql_query("INSERT INTO tracking VALUES (\"$ip\",1,NULL,$hits,\"$token\") ON DUPLICATE KEY UPDATE requests=requests+1, hits=hits+$hits, token=\"$token\"");

		// done
	}

	///////////////////////////////////////////////
	// Parse Input and Respond
	function parse($in) {

		$hits = 0;

		// read stdinput - lines "city,jurisdiction,jurisdiction,...,country" by one
		for ($l=0 ; ($tokens=fgetcsv($in, 100, ",")) !== FALSE ; $l++) {
	
			// newline?
			if ($l>0) echo "\n";

			// at least 3 tokens?
			if (count($tokens)<3) continue;

			// grab city (first token)
			$city = trim($tokens[0]);
			if (strstr($city, "\"")!=FALSE) continue;
			$like = rtrim($city, "*");
			if (strlen($like)<3) continue;
		
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
				"WHERE locations.city".$op."\"$city\"";
	  
			// try to lookup country? (last token)
			$country = trim($tokens[count($tokens)-1]);
			if (strstr($country, "\"")!=FALSE) continue;
			if (strlen($country)>0)
		  		$lquery = "$lquery AND locations.country = \"$country\"";
	  
			// check for well known jurisdictions? (tokens 1 to n-1)
			for ($j=1 ; $j<count($tokens)-1 ; $j++) {
				// stop at first empty jurisdiction	
				$jurisdiction = trim($tokens[$j]);
				if (strstr($country, "\"")!=FALSE) break;
				if ($jurisdiction=="") break;

				// prepare query for matching names
				$jquery = "SELECT jurisdiction FROM jurisdictions WHERE name LIKE \"$jurisdiction\"";
	
				// add country qualifier if available
				if (strlen($country)>0)
		  			$jquery = "$jquery AND country = \"$country\"";
	
				// look for first matching jurisdiction and add it to lquery
				$rows = mysql_query($jquery);
				if (!$rows) die("error:selectj");
	 			if (mysql_num_rows($rows)==1) {
		  			$row = mysql_fetch_row($rows);
		  			// need to always allow for jurisdiction 00 (null) in table "locations"
			  		$lquery = "$lquery AND (locations.jurisdiction='00' or locations.jurisdiction=\"$row[0]\")";
			  		$j = count($tokens);
				}
				mysql_free_result($rows);
			
				// try next jurisdiction
			}
		
			// query and return rows
			$rows = mysql_query($lquery);
			if (!$rows) die("error:selectl");
			if (mysql_num_rows($rows)==0) {
				echo "?";
			} else for ($i=0 ; $row = mysql_fetch_row($rows) ; $i++) {
				if ($i>0) echo ";";
				echo "$row[0],$row[1],$row[2],$row[3],$row[4]";
				$hits++;
			}
			mysql_free_result($rows);

			// next
		}
		// done
		return $hits;
	}

	///////////////////////////////////////////////
	// MAIN

	// read stdinput - check header
	$in = fopen("php://input", "rb");
       $header = explode(":", trim(fgets($in)), 2);
	if ($header[0]!="GEOQ") die("PING");

	// open database
	openDB();

	// parse input
	$hits = parse($in);

	// close input
	fclose($in);

	// track it
	track($header[1],$hits);

	// close database
	mysql_close();

?>
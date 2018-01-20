<?php
//script that handles retrival from the database
//for security it requests the email and password for any transaction

include_once 'inc_0700/Connection.php';

//connection object
$Connection = new Connection();
$pdo = $Connection->getConnection();


$email= $_POST['email'];
$userPW= $_POST['password'];
$retrieve = $_POST["retrieve"];
$rows = $_POST["rows"];


//retrieve password
$getUserInfoQuery =
$pdo->prepare('SELECT UserPW FROM sm16_users
					WHERE Email =:email');
if ($getUserInfoQuery->execute(array(':email'=>$email))) {
	
	$info = $getUserInfoQuery->fetch(PDO::FETCH_ASSOC);

	//if the db pass equals the provided pass
	if (password_verify($userPW, $info["UserPW"])) {

		//clears the varible info to be reused
		unset($info);

		//cleans the query to avoid a SELECT * FROM by replacing any * with nothing ""
		//no queries to the database require a select all from any table
		$retrieve = str_replace("*", "", $retrieve);
		
		//prepare query
		$retrieveInfoQuery =
		$pdo->prepare($retrieve);

		//if the query is executed
		if ($retrieveInfoQuery->execute()) {
			
			//the value of rows defines if the query is requesting one row or several rows
			//in this case 1 means many rows while anything else means one row
			if($rows == "1"){
				//fetch results
				$info = $retrieveInfoQuery->fetchAll(PDO::FETCH_ASSOC);
		
				$result = '';
				
				//the results are converted to a single string were separators are added
				//-c- means column separation   
				//e.g Israel-c-Santiago ; one row with two records firstname-c-lastname
				//-r- means row separation 
				//e.g Israel-c-Santiago-r-Abe-c-San ; two rows with two records each
				foreach ($info as $row){
					foreach ($row as $key => $value){
						$result .= $value . '-c-';
					}
					$result .= "-r-";
				}
			} else {//if the value of row is not 1 the script returns a single row
				//fetch
				$info = $retrieveInfoQuery->fetch(PDO::FETCH_ASSOC);
		
				$result = '';
				//formats the string as the previous but will only fetch one row and only one row
				foreach ($info as $value){
						$result .= $value . '-c-';
				}
			}
			//if something was actually fetched from the db echo the result
			if($info){
				echo $result;//returns results
			} else {
				echo "no records";//no records found
			}
		} else {
			echo "failed";//failed specifies a failed query
		}
		
	} else {
		echo "false";//false means that the either the password or the email are not right
	}
} else {
	echo "false";//false means that the either the password or the email are not right
}


//unset all variables
unset($email);
unset($userPW);
unset($pdo);
unset($Connection);
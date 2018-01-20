<?php

include_once 'inc_0700/Connection.php';
include_once 'inc_0700/functions.php';


//connection object
$Connection = new Connection();
$pdo = $Connection->getConnection();

$email= $_POST['email'];
$userPW= $_POST['password'];
$hhID = $_POST['hhID'];

$getUserInfoQuery =
$pdo->prepare('SELECT UserPW FROM sm16_users
					WHERE Email =:email');
if ($getUserInfoQuery->execute(array(':email'=>$email))) {

	$info = $getUserInfoQuery->fetch(PDO::FETCH_ASSOC);

	if (password_verify($userPW, $info["UserPW"])) {

		unset($info);
		
		if((int)$hhID != 0){
			if(areAllUsersDone($hhID, $pdo)){
				mailMonthlySpreadsheet($hhID, $pdo);
				echo 'mailing attempted...';
			} else {
				echo 'users not ready';
			}
		} else {
			echo 'not valid id';
		}

	} else {
		echo "false";
	}//password authetication
} else {
	echo "false";
}//password retrival

//unset all variables
unset($email);
unset($userPW);
unset($pdo);
unset($Connection);
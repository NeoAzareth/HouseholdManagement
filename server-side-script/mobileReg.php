<?php
//script that handles registration

include_once 'inc_0700/Connection.php';
include_once 'inc_0700/functions.php';

//connection object
$Connection = new Connection();
$pdo = $Connection->getConnection();

//expects a code that the app passes to the script in order to ensure users are registered
//through the app
$keyCode = $_POST['keyCode'];
$verify = $_POST['verify'];
$reg = $_POST['reg'];
$sendEmail = $_POST['sendEmail'];
$newPW = $_POST['newPassword'];
$email = $_POST['Email'];

if (password_verify($keyCode, $Connection->getCode())){
	
	if ($verify == "true"){//verifying info
		$verifyEmail =
		$pdo->prepare($reg);
		
		if ($verifyEmail->execute()){
			$info = $verifyEmail->fetchAll(PDO::FETCH_ASSOC);
			if(empty($info)){
				echo "false";
			} else {
				echo "true";
			}
		} else {
			echo "failed";
		}
	} else if ($sendEmail == 'true' && $newPW != null){//query for reset password feature
			$newHashedPW=password_hash($newPW,PASSWORD_DEFAULT);
				
			$transaction =
			$pdo->prepare("UPDATE sm16_users SET UserPW = :pw WHERE Email = :em");
				
			$result = "";
				
			if ($transaction->execute(array(':pw'=>$newHashedPW,':em'=>$email))) {
				sendPassword($email, $newPW);
				echo "completed";//transaction completed
			} else {
				echo "failed";//transaction failed
			}
	} else {//assume registration
		//variables expected from the app
		$lastName = $_POST['LastName'];
		$firstName = $_POST['FirstName'];
		$email = $_POST['Email'];
		$pw = $_POST['UserPW'];
		//hash the incoming user password
		$pwHash = password_hash($pw, PASSWORD_DEFAULT);
		
		//prepare the query to register
		$registerNewUser =
		$pdo->prepare('INSERT INTO sm16_users
			VALUES (NULL,
			:lastName,
			:firstName,
			:email,
			:userPW,
			:userLevel,
			:userStatus,
			NULL)');
		
		//execute the query
		if ($registerNewUser->execute(array (
				':lastName' => $lastName,
				':firstName' => $firstName,
				':email' => $email,
				':userPW' => $pwHash,
				':userLevel' => 'member',
				':userStatus' => 'not in'
		))) {
			//echos a success message if completed
			echo 'success';
		} else {
			//otherwise a failed message if not
			echo 'failed to insert';
		}	
	}
} else {
	//page loads nothing if the keycode does not matches
	echo "";
}

//destroy all variables
unset($keyCode);
unset($lastName);
unset($firstName);
unset($email);
unset($pw);
unset($pwHash);
unset($pdo);
unset($Connection);
<?php


include_once 'inc_0700/Connection.php';

//connection object
$Connection = new Connection();
$pdo = $Connection->getConnection();

$email= $_POST['email'];
$userPW= $_POST['password'];
$query = $_POST['query'];
$newPW=$_POST['newPassword'];
$isRegHH = $_POST['RegHH'];

//retrieve password
$getUserInfoQuery =
$pdo->prepare('SELECT UserPW FROM sm16_users
					WHERE Email =:email');
if ($getUserInfoQuery->execute(array(':email'=>$email))) {
	
	$info = $getUserInfoQuery->fetch(PDO::FETCH_ASSOC);

	if (password_verify($userPW, $info["UserPW"])) {

		unset($info);
		
		if($newPW != null){//query for update password feature
			$newHashedPW=password_hash($newPW,PASSWORD_DEFAULT);
			
			$transaction =
			$pdo->prepare("UPDATE sm16_users SET UserPW = :pw WHERE Email = :em");
			
			$result = "";
			
			if ($transaction->execute(array(':pw'=>$newHashedPW,':em'=>$email))) {
				$result = "completed";//transaction completed
			} else {
				$result = "failed";//transaction failed
			} 
		}  else if ($isRegHH){//register household
			$transaction =
			$pdo->prepare($query);
			
			if ($transaction->execute()){
				$hhID = $pdo->lastInsertId();
				$level = 'admin';
				$status = 'not done';
				$updateUserHHID =
				$pdo->prepare("UPDATE sm16_users 
						SET HouseholdID = :hhId, 
						UserLevel = :level,
						UserStatus = :status
						WHERE Email = :email");
				if($updateUserHHID->execute(array (
						':hhId' => $hhID,
						':email' => $email,
						':level' => $level,
						':status' => $status
				))){
					$result = "completed";//transaction completed
				} else {
					$result = "failed";//updating using info failed
				}
				
			} else {
				$result = "failed";//failed register
			}
		
		} else {//any other query
			
			$transaction =
			$pdo->prepare($query);
			
			$result = "";
			
			if ($transaction->execute()){
				$result = "completed";//query completed
			} else {
				$result = "failed";//query failed
			}
		}
		
		echo $result;
		
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
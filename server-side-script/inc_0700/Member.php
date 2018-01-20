<?php
/**
 * Member class; former User class
 * A class that creates member objects,
 * @author Israel Santiago
 * @package projectv2
 * @link neoazareth@gmail.com
 * @version 2.0
 */
Class Member{
	//instance fields
	Private $lastName;
	Private $firstName;
	Private $email;
	Private $userID;
	Private $userLevel;
	Private $userBillIDs = [];
	Private $userBills = [];
	Private $userStatus;
	
	public function __construct()
	{
		//gets the arguments passed an stores them in the $a variable
		$a = func_get_args();
	
		//counts the arguments passed an stores the number in the $i variable
		//very important to name the other constructor with the number of
		//arguments they take
		$i = func_num_args();
	
		//calls the appropiate constructor based on the number of arguments
		if (method_exists($this,$f='__construct'.$i)) {
			call_user_func_array(array($this,$f),$a);
		}
	}
	
	function __construct1($id){
		$this->userID = $id;
	}
	
	function __construct2($firstName, $lastName){
		$this->firstName = $firstName;
		$this->lastName = $lastName;
	}
	
	/**
	 * Construtor
	 * @param string $userName, used to construct the object from the DB
	 * @param unknown $month, month is used to pull the current month bills
	 * @param unknown $pdo, a pdo object connection
	 */
	function __construct3($userID, $month, $pdo){
		//prepare statement
		$getUserInfo =
		$pdo->prepare('SELECT UserID, LastName, FirstName, Email, UserLevel, UserStatus
							FROM sm16_users
							WHERE UserID =:userid');
		//execute statement
		$getUserInfo->execute(array('userid'=>"$userID"));
		//pull info
		$info = $getUserInfo->fetchAll(PDO::FETCH_ASSOC);
		$info = $info[0];
	
		$this->lastName = $info["LastName"];
		$this->firstName = $info["FirstName"];
		$this->email = $info["Email"];
		$this->userID = $info["UserID"];
		$this->userLevel = $info["UserLevel"];
		$this->userStatus = $info["UserStatus"];
	
		$this->getUserBills($month, $pdo);
	}
	
	/**
	 * gets the user bills from the DB
	 * @param String $month, the current month
	 * @param object $pdo, a pdo connection object
	 * @param boolean $saveInSession, used to save the bills in the session
	 */
	function getUserBills($month,$pdo){
		
		$month .= '%';
			
		$getUserBills =
		$pdo->prepare('SELECT BillID, BillAmount, BillDesc, BillCategory, BillDate 
				FROM sm16_bills WHERE UserID = :userid AND BillDate LIKE :month');
		$getUserBills->execute(array(':userid' => $this->userID,':month' => "$month"));
		$info = $getUserBills->fetchAll(PDO::FETCH_ASSOC);
	
		//this simultaneously saves an array with ids and  and array of bills objects
		// as properties
		$bills = [];
		$billIDs = [];
		foreach ($info as $key => $value) {
			$obj = new Bill($value['BillID'], $value['BillDesc'],
					$value['BillCategory'], $value['BillAmount'], $value['BillDate']);
			array_push($bills, $obj);
			array_push($billIDs,$value['BillID']);
		}
	
		$this->userBills = $bills;
		$this->userBillIDs = $billIDs;
	}
	
	/**
	 * retrieves 
	 * @param object $pdo, a pdo connection
	 */
	function getUserContactOptions(){
		$config = new Validate();
		$pdo = $config->pdoConnection();
		$userID = $this->userID;
		$getContactOptions =
		$pdo->prepare('SELECT ContactOptionID, ContactOpType, ContactOpDesc
				FROM sp16_user_contact_options
				WHERE UserID = :userid
				');
		$getContactOptions->execute(array(':userid'=>$userID));
		$info = $getContactOptions->fetchAll(PDO::FETCH_ASSOC);
		
		$this->userContactOptions = $info;
	}
	
	/**
	 * getter for user id
	 */
	function getUserID(){
		return $this->userID;
	}
	
	/**
	 * getter for the user level
	 */
	function getUserLevel(){
		return $this->userLevel;
	}
	
	/**
	 * getter for the user firstname
	 */
	function getUserFirstName(){
		return $this->firstName;
	}
	
	function getUserEmail(){
		return $this->email;
	}
	
	/**
	 * getter for user status
	 */
	function getUserStatus(){
		return $this->userStatus;
	}
	
	/**
	 * gets the user's full name
	 * @return string
	 */
	function getUserFullName(){
		return $this->firstName . ' ' . $this->lastName;
	}
	
	function getBills(){
		return $this->userBills;
	}
	
	/**
	 * getter for the userbillsids array
	 */
	function getUserBillsIDs(){
		return $this->userBillIDs;
	}
	
	function getContactOptions(){
		return $this->userContactOptions;
	}
	
	function getPrefContactID(){
		return $this->prefContactId;
	}
	
	function getUserEmailAddress(){
		foreach ($this->userContactOptions as $option){
			if ($option['ContactOpType'] == 'email'){
				return $option['ContactOpDesc'];
			}
		}
	}
	
	/**
	 * setter preferred contact id
	 * @param string $id
	 */
	function setPrefContactId($id){
		$this->prefContactId = $id;
	}
	
	function billsDistribution(){
		$amountsArray= array('food'=>0.0,
				'utility'=>0.0,
				'maintenance'=>0.0,
				'other'=>0.0,
				'total'=>0.0);
		foreach ($this->userBills as $bill) {
			$key = $bill->getBillCategory();
			$amount = $bill->getBillAmount();
			$amountsArray["$key"] += round($amount,2);
			$amountsArray['total'] += round($amount,2);
		}
		
		return $amountsArray;
	}
	
	/**
	 *creates a list of user contacts ids for reference
	 */
	function listOfContactIDs(){
		$array = [];
		foreach ($this->userContactOptions as $option){
			$id = $option['ContactOptionID'];
			array_push($array, $id);
		}
		return $array;
	}
	
	/**
	 * displays a table with the current user's bills
	 * @return string
	 */
	function displayUserBills(){
		//if empty returns the following string
		if (empty($this->userBills)) {
			$table = '<h4 class="text-warning">There is nothing to show...</h4>';
		} else {
			$table = '<table class="table table-striped table-hover">
				<tHead>
					<tr>
						<th>Description</th><th>Amount</th><th>Category</th><th>Date</th>
					</tr>
				</tHead>
				<tBody>
				';
			//it calls the getbill as row method of the bill class
			foreach ($this->userBills as $key => $obj) {
				$table .= $obj->getBillAsRow();
			}
			$table .= '</tBody></table>';
		}
		return $table;
	}
	
	/**
	 * gets the user status formated as a row for a table
	 * @return string
	 */
	function getUserStatusAsRow(){
		$row = '<tr>
				<td>'. $this->getUserFullName() .'</td>
				<td>'. ucfirst($this->userStatus) .'</td>
				<td>'. ucfirst($this->userLevel) .'</td>
				</tr>';
		return $row;
	}
	
	/**
	 * Changes the user status: "done" to "not done"
	 * @param unknown $id, a number associated with user
	 * @param unknown $urlString, a url to which the page is going to redirect upon success
	 * or failure
	 * @param unknown $statusString, the status to change the user to
	 * @return string, a javascript string to redirect user
	 */
	function changeUserStatus($url,$status){
		$config = new Validate();
		$pdo = $config->pdoConnection();
		//prepare pdo statement
		$changeStatus =
		$pdo->prepare('UPDATE sp16_users SET UserStatus = :status
				WHERE UserID = :id');
		//execute the statement while checking if successful
		if ($changeStatus->execute(array(':id'=> $this->userID,':status'=> $status))) {
			
			$result = '<script>
			alert("Status Updated");
			window.location.href="'. $url .'";
			</script>';
			
			$id = $_SESSION['user']['householdid'];
			if ($status == 'done'){
				if ($config->areAllUsersDone($id)) {
					$currentMonth = $_SESSION['currentMonth'];
					$config->mailMonthlySpreadsheet($id, $currentMonth);
				} 
			}
			
		} else {
			$result = '<script>
			alert("Something went wrong...");
			window.location.href="'. $url .'";
			</script>';
		}
		return $result;
	}
	
	/**
	 * method that updates the user password on the DB
	 * @param string $newPass, the new password
	 * @param object $pdo, pdo connection
	 * @return boolean
	 */
	function updatePW($newPass,$pdo){
		$newPass = password_hash($newPass, PASSWORD_DEFAULT);
		$updatePass =
		$pdo->prepare('UPDATE sp16_users
				SET UserPW = :newpass
				WHERE UserID = :userid
				');
		if ($updatePass->execute(array(':newpass'=>$newPass,':userid'=>$this->userID))) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * udpates a contact option on the DB
	 * @param int $id
	 * @param string $desc
	 * @param string $type
	 * @param object $pdo
	 */
	function updateContactInfo($id,$desc,$type,$pdo ){
		$updateConctactOption =
		$pdo->prepare('UPDATE sp16_user_contact_options
				SET ContactOpType = :type,
				ContactOpDesc = :desc
				WHERE ContactOptionID = :id
				');
		if ($updateConctactOption->execute(array(
				':type'=>$type,
				':desc'=>$desc,
				':id'=>$id
		))){
			$result = 'Contact option updated';
			$_SESSION['editcontact'] = 0;
			$this->getUserContactOptions($pdo);
		} else {
			$result = 'Update failed';
		}
		return $result;
	}
	
	/**
	 * adds a contact option to the DB
	 * @param string $desc
	 * @param string $type
	 * @param object $pdo
	 */
	function addContactInfo($desc,$type,$pdo){
		$addContactInfo =
		$pdo->prepare('INSERT INTO sp16_user_contact_options
				VALUES(
				NULL,
				:type,
				:desc,
				:userid
				)');
		if ($addContactInfo->execute(array(
				':type'=> $type,
				':desc'=> $desc,
				':userid'=> $this->userID
		))) {
			$result = 'Contact option added';
			$this->getUserContactOptions($pdo);
		} else {
			$result = 'Addition failed';
		}
		return $result;
	}
	
	/**
	 * deletes a contact option on the DB
	 * @param int $id
	 * @param object $pdo, pdo connection
	 * @return string
	 */
	function deleteContactOption($id,$pdo){
		$result = '';
		$deleteContactOption =
		$pdo->prepare('
				DELETE FROM sp16_user_contact_options WHERE ContactOptionID = :id
				');
		if ($deleteContactOption->execute(array(':id'=>$id))){
			$result = 'Contact option deleted';
			$this->getUserContactOptions($pdo);
		} else {
			$result = 'Failed to delete option...';
		}
	
		return $result;
	}
	
	/**
	 * updates the preferred notification option on the DB
	 * @param int $id, id for reference
	 * @param object $pdo, pdo connection
	 * @return string
	 */
	function updatePrefNotOption($id,$pdo){
		$result = '';
		$updatePref =
		$pdo->prepare('
				UPDATE sp16_users SET PreferredNotID = :id
				WHERE UserID = :userid
				');
		if($updatePref->execute(array(':id'=>$id,':userid'=>$this->userID))){
			$result = 'Preference updated';
			$this->getUserContactOptions($pdo);
			$this->setPrefContactId($id);
		} else {
			$result = 'Update failed';
		}
	
		return $result;
	}
	
	/**
	 * Registers the user
	 * @param unknown $firstName
	 * @param unknown $lastName
	 * @param unknown $username
	 * @param unknown $email
	 * @param unknown $pw
	 * @return string
	 */
	function register($username,$email,$pw){
		$config = new Validate();
		$pdo = $config->pdoConnection();
		unset($config);
		$result ='';
		//hashes password
		$pw = password_hash($pw, PASSWORD_DEFAULT);
		//sets the household id to the id save in the session by the validate code method
		$householdID = $_SESSION['hhID'];
		//prepare register user in users table
		$registerUser =
		$pdo->prepare('INSERT INTO sp16_users
				VALUES (NULL,
				:lastname,
				:firstname,
				:username,
				:pass,
				:level,
				NULL,
				:status,
				:householdid
				)');
		//if successful registering user
		if($registerUser->execute(array(':lastname'=> $this->lastName,
				':firstname'=> $this->firstName,
				':username'=> $username,
				':pass'=> $pw,
				':level'=> 'member',
				':status'=> 'not done',
				':householdid'=>$householdID))){
				//prepares the statment to insert contact info into the respective table
		$userID = $pdo->lastInsertId();
		$insertEmail=
		$pdo->prepare('INSERT INTO sp16_user_contact_options
						VALUES(NULL,
						"email",
						:em,
						:userid
						)');
		//if successful insert of contact info
		if($insertEmail->execute(array(':em'=>$email,':userid'=>$userID))){
			//prepares to update the user preferred contact
			//to the just inserted email id
			$prefID = $pdo->lastInsertId();
			$updatePrefContactID=
			$pdo->prepare('UPDATE sp16_users
							SET PreferredNotID = :prefid
							WHERE UserID = :userid
							');
			//if successful
			if ($updatePrefContactID->execute(
					array(':prefid'=>$prefID,':userid'=>$userID))){
						//prepares to set the hashed code to invalid
						$code = $_SESSION['code'];
						$setCodeToInvalid =
						$pdo->prepare('UPDATE sp16_codes
								SET CodeValid = 0 WHERE CodeNum = :code');
						//if the code was successfully set to invalid
						if($setCodeToInvalid->execute(array(':code'=> $code))){
							$to  = $email;
	
							// subject
							$subject = 'HH Registration complete!';
	
							// message
							$message = '
								<html>
								<head>
								  <title>You have been successfully registered!</title>
								</head>
								<body>
								  <h3>
									Congratulations '. $firstName . ' ' . $lastName .'!
									</h3>
								  <p>You can now log in using your info,
								  		just click the link below</p>
								  <a href="http://neoazareth.comindex.php"
								  		target="_blank">HH log in</a>
								</body>
								</html>
							';
	
							// To send HTML mail, the Content-type header must be set
							$headers  = 'MIME-Version: 1.0' . "\r\n";
							$headers .= 'Content-type: text/html; charset=iso-8859-1'
									. "\r\n";
										
									//send an email to the user and redirects the user
									//to the login page
									if(mail($to, $subject, $message,$headers)){
										$result = '<script>
									alert("Registration complete!");
									window.location.href="index.php";
									</script>';
									} else {//the else are errors
										$result = 'Mail confirmation failed';
									}
						} else {
							$result = 'Code could not be deleted';
						}
	
			} else {
				$result = 'Update preferred notification failed';
			}
		} else {
			$result = 'Email registration failed';
		}
		} else {
			$result = 'Registration has failed';
		}
		return $result;
	}
}
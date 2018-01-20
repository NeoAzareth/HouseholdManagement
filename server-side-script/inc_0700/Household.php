<?php
/**
 * Household class
 * A class that creates household objects,
 * @author Israel Santiago
 * @package projectv2
 * @link neoazareth@gmail.com
 * @version 2.0
 */
Class Household{

	Private $householdName;
	Private $householdId;
	Private $hhRentAmount;
	Private $members = [];

	/**
	 * Constructor
	 * @param string $userName
	 * @param string $month, used to pull bills from current month
	 * @param object $pdo, pdo connection
	 */
	function __construct($hhID, $month, $pdo){

		$getHhInfo =
		$pdo->prepare('SELECT HouseholdName, HhRentAmount
							FROM sm16_households 
							WHERE HouseholdID =:householdid');
		$getHhInfo->execute(array(':householdid'=>"$hhID"));
		$info = $getHhInfo->fetch(PDO::FETCH_ASSOC);
		$info = $info;

		$this->householdId = $hhID;
		$this->householdName = $info['HouseholdName'];
		$this->hhRentAmount = $info['HhRentAmount'];
		$this->getHouseholdMembers($pdo,$month);
	}
	
	/**
	 * gets household members from db as User objects
	 * @param object $pdo, pdo connection
	 * @param boolean $saveInSession, if true saves the array in session
	 */
	function getHouseholdMembers($pdo,$month){
		$getMembers =
		$pdo->prepare('SELECT UserID FROM sm16_users WHERE HouseholdID =:householdid');
		$getMembers->execute(array('householdid'=>"$this->householdId"));
		$info = $getMembers->fetchAll(PDO::FETCH_COLUMN);
		$members =[];
		foreach ($info as $key => $id){
			$member = new Member($id,$month,$pdo);
			array_push($members, $member);
		}
		
		$this->members = $members;
	}
	
	/**
	 * getter for Householdid
	 */
	function getHhID(){
		return $this->householdId;
	}
	
	/**
	 * getter for the household name
	 */
	function getHhName(){
		return $this->householdName;
	}
	
	/**
	 * getter for the household rent
	 */
	function getHhRent(){
		return $this->hhRentAmount;
	}
	
	function getMembers(){
		return $this->members;
	}
	
	function setHhRent($amount){
		$this->hhRentAmount = $amount;
	}
	
	function getHouseholdAdmin(){
		$members = $this->members;
		foreach ($members as $member) {
			$level = $member->getUserLevel();
			if($level == 'admin'){
				$admin = $member;
				break;
			}
		}
		return $admin;
	}
	
	/**
	 * return a list of users associated with the houshold
	 */
	function listOfUsers(){
	
		$users = [];
		foreach ($this->members as $obj){
			$user = array('name'=> $obj->getUserFullName(),'id'=>$obj->getUserID());
			array_push($users, $user);
		}
		return $users;
	}
	
	function getNumberOfMembers(){
		$number = 0;
		foreach ($this->members as $obj){
			$number++;
		}
		return $number;
	}
	
	function getMonthTotal(){
		$total = $this->hhRentAmount;
		foreach ($this->members as $member){
			foreach ($member->getBills() as $bill){
				$total += $bill->getBillAmount();
			}
		}
		return $total;
	}
	
	function monthlyBalancedData(){
		$results= [];
		$results['total'] = $this->getMonthTotal();
		$results['fairAmount'] = $results['total']/$this->getNumberOfMembers();
		foreach ($this->members as $member) {
			$name = $member->getUserFirstName();
			$results['members'][$name] = $member->billsDistribution();
		}
		return $results;
	}
	
	function formatMonthlyBalanceReport(){
		$data = $this->monthlyBalancedData();
		$report = '<p>Period Total: $'. number_format($data['total'],2) .'.</br>
				Fair Amount: $'. number_format($data['fairAmount'],2) .'.</p>';
		
		foreach ($data['members'] as $name => $value) {
			$total = $value['total'];
			$toRent = round($data['fairAmount'] - $total,2);
			if ($total > $data['fairAmount']) {
				$report .= '<p>'. $name .' has exceeded the fair amount by $'. number_format(abs($toRent),2) .
				'; *see that he/she gets this amount</p>';
			} else {
				if(substr($name, strlen($name)-1,1) == 's'){
					$name = $name . '\'';
				} else {
					$name = $name . '\'s';
				}
				$report .= '<p>'. $name .' part of the rent is $'. number_format($toRent,2) .'.</p>';
			}
		}
		return $report;
	}
	
	function expensesDistribution($month){
		
		$overviewTitle = 'Current Month';
		if ($month != 'current'){
			$overviewTitle = 'Past Month';
			$config = new Validate();
			$pdo = $config->pdoConnection();
			$this->getHouseholdMembers($pdo, $month);
		}
		
		if ($this->getMonthTotal() == $this->getHhRent()){
			return '<div>
					<h3 class="text-center">'. $overviewTitle .'</h3>
					<h4 class="text-danger text-left">There are no results...</h4>
					</div>';
		}
		$total  = $this->getMonthTotal();
		$fairAmount = round($total/$this->getNumberOfMembers(),2);
		
		$chart = '<div>
				<h3 class="text-center">'. $overviewTitle.'</h3>
				<h4 class="text-info text-left">Hover the mouse over the segments to see details</h4>
				</div>
				<div class="background-custom">';
		foreach ($this->members as $member){
			$cats = $member->billsDistribution();
			$catsPercent = [];
			
			foreach ($cats as $key => $value){
				if ($cats['total']> $fairAmount){
					$catsPercent["$key"] = $this->calculatePercent($value, $cats['total']);
					$overLimit = 'Over fair amount: $'. ($cats['total'] - $fairAmount);
				} else {
					$catsPercent["$key"] = $this->calculatePercent($value, $fairAmount);
					$overLimit = '';
				}
			}
			$rent = round($fairAmount - $cats['total'],2);
			$rentPercent = 100 - $catsPercent['total'];
			$chart .='<div>
					<div class="row">
					<div class="col-lg-6">
					<h4>'. $member->getUserFullName() .'</h4>
					</div>
					<div class="col-lg-6">		
							<h4 class="text-right text-custom">'. $overLimit .'</h4>
					</div>		
					</div>
					<div class="progress">
						  <span title="Food: $'. $cats['food'] .'">
						  		<div class="progress-bar progress-bar-info" style="width: '
						  		. $catsPercent['food'] .'%"><p class="text-center-ver">'. round($catsPercent['food']) .'%</p></div>
						  </span>
						  <span title="Maintenance: $'. $cats['maintenance'] .'">
						  		<div class="progress-bar progress-bar-warning" style="width: '
						  		. $catsPercent['maintenance'] .'%"><p class="text-center-ver">'. round($catsPercent['maintenance']) .'%</p></div>
						  </span>
						  <span title="Utility: $'. $cats['utility'] .'">
						  		<div class="progress-bar progress-bar-danger" style="width: '
						  		. $catsPercent['utility'] .'%"><p class="text-center-ver">'. round($catsPercent['utility']) .'%</p></div>
						  </span>
						  <span title="Other: $'. $cats['other'] .'">
						  		<div class="progress-bar progress-bar-success" style="width: '
						  		. $catsPercent['other'] .'%"><p class="text-center-ver">'. round($catsPercent['other']) .'%</p></div>
						  </span>
						  <span title="Rent: $'. $rent .'">
						  <div class="progress-bar" style="width: '. $rentPercent .'%"><p class="text-center-ver">'. round($rentPercent) .'%</p></div>
						  </span>
					</div>
					</div>';
		}
		$chart .= '<h4>Period Total: <em class="text-custom">$'. $this->getMonthTotal().'</em></h4>';
		$chart .= '<h4>Fair Amount: <em class="text-success">$'. $fairAmount.'</em></h4>
				</div>';
		return $chart;
	}
	
	function calculatePercent($number,$hundred){
		$result = (100 * $number)/$hundred;
		return $result;
	}
	
	/**
	 * returns a list of users' ids
	 */
	function listOfUserIDs(){
		$ids = [];
		foreach ($this->members as $obj){
			$id = $obj->getUserID();
			array_push($ids, $id);
		}
		return $ids;
	}
	
	/**
	 * displays a table with the household user's status
	 * @return string
	 */
	function showUsersStatus($user){
		$table = '<table class="table table-striped table-hover">
				<tHead>
				<tr>
				<th>Name</th><th>Status</th><th>Level</th>
				</tr>
				</tHead>
				<tBody>';
		foreach ($this->members as $member){
			if($member->getUserID() != $user->getUserID()){
				$table .= $member->getUserStatusAsRow();
			}
		}
		$table .= '
				</tBody></table>';
		return $table;
	}
	
	/**
	 * displays custom report
	 * @param unknown $postArray
	 * @param unknown $pdo
	 * @return string
	 */
	function retrieveReportData($postArray, $pdo){
		$id = $this->householdId;
		$date = $postArray['date'];
		$name = '';
		$category = '';
	
		$constrainUser = '';
		$constrainCategory = '';
	
		$tokensArray = array(':hhID'=> $id, ':date'=> $date);
	
		if($postArray['name'] != 'all'){
			$constrainUser = ' AND u.UserID = :userid ';
			$name = $postArray['name'];
			$tokensArray[':userid'] = $name;
		}
		if($postArray['category'] != 'all'){
			$constrainCategory = ' AND BillCategory = :category';
			$category = $postArray['category'];
			$tokensArray[':category'] = $category;
		}
	
	
		$getCustomReport =
		$pdo->prepare('
				SELECT LastName, FirstName, BillDesc, BillAmount, BillCategory, BillDate
				FROM sp16_users u
				INNER JOIN sp16_bills b
				ON u.UserID = b.UserID
				WHERE b.HouseholdID = :hhID AND (LOCATE(:date,BillDate)!=0)
				'. $constrainUser .'
				'. $constrainCategory .'
				');
		$getCustomReport->execute($tokensArray);
		$info = $getCustomReport->fetchAll(PDO::FETCH_ASSOC);
	
		if(empty($info)){
			$table = '<h4 class="text-danger">
					There are no results with those parameters...</h4>';
			return $table;
		} else {
			return $this->formatReport($info, 'Query results:');
		}
	}
	
	/**
	 * formats report
	 * @param unknown $array
	 * @param unknown $string
	 * @return string
	 */
	function formatReport($array,$string){
	
		$total = 0.0;
		$table = '<h3 class="text-success">'. $string .'</h3>
				<table class="table table-striped table-hover">
				<tHead>
				<tr>
				<th>Name</th>
				<th>Bill Description</th>
				<th>Bill Amount</th>
				<th>Bill Category</th>
				<th>Bill Date</th>
				</tr>
				</tHead>
				<tBody>';
		foreach ($array as $value){
			$total += floatval($value['BillAmount']);
			$shortDate = explode(" ", $value['BillDate']);
			$shortDate = $shortDate[0];
			$table .= '<tr>
					<td>'. $value['FirstName'].' '.$value['LastName'] .'</td>
					<td>'. $value['BillDesc'] .'</td>
					<td>'. $value['BillAmount'] .'</td>
					<td>'. $value['BillCategory'] .'</td>
					<td>'. $shortDate .'</td>
					</tr>';
		}
		$table .= '
				<tr class="danger">
				<td></td>
				<th class="text-right">Total:</th><th>$'.$total.'</th><td></td><td></td>
				</tr>
				</tBody></table>';
	
		return $table;
	}
}
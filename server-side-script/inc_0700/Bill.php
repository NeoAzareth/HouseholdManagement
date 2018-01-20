<?php
/**
 * Bill class
* A class that creates bill objects,
* @author Israel Santiago
* @package itc285project
* @link neoazareth@gmail.com
* @version 1.0
*/
Class Bill{

	Private $billID;
	Private $billDesc;
	Private $billCategory;
	Private $billAmount;
	Private $billDate;

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
	
	function __construct1($billID){
		$this->billID = $billID;
	}
	
	/**
	 * constructor
	 * @param unknown $billID
	 * @param unknown $billDesc
	 * @param unknown $billCategory
	 * @param unknown $billAmount
	 * @param unknown $billDate
	 */
	function __construct5($billID, $billDesc, $billCategory, $billAmount,$billDate){

		$this->billID = $billID;
		$this->billDesc = $billDesc;
		$this->billCategory = $billCategory;
		$this->billAmount = $billAmount;
		$this->billDate = $billDate;
	}

	function getBillAmount(){
		return $this->billAmount;
	}
	
	function getBillCategory(){
		return $this->billCategory;
	}
	
	function getBillDescription(){
		return $this->billDesc;
	}
	
	/**
	 * returns a string with the properties fomatted as a table row
	 * @return string
	 */
	function getBillAsRow(){
		$row = '<tr>
				<td>'. $this->billDesc .'</td>
				<td>$'. $this->billAmount .'</td>
				<td>'. $this->billCategory .'</td>
				<td>'. $this->billDate .'</td>
				</tr>';
		return $row;
	}

	/**
	 * Returns the bills properties as a form to edit
	 * @return string
	 */
	function getBillAsForm(){
		$f = ''; $m = ''; $u = ''; $o = ''; $s = 'selected';
		switch ($this->billCategory){
			case 'food':
				$f = $s;
				break;
			case 'maintenance':
				$m = $s;
				break;
			case 'utility':
				$u = $s;
				break;
			case 'other':
				$o = $s;
				break;
		}

		$form = '
				<td>
				<input type="text" name="desc" value="'. $this->billDesc .'"
			    		class="form-control" id="inputDesc">
				</td>
				<td>
			    <input type="text" name="amount" value="'. $this->billAmount .'"
			    		class="form-control" id="inputAmount">
			    </td>
			    <td>
			    <select name="category" class="form-control" id="inputCat">
				<option value="food" '. $f .'>Food</option>
				<option value="utility" '. $u .'>Utility</option>
				<option value="maintenance" '. $m .'>Maintenance</option>
				<option value="other" '. $o .'>Other</option>
				</select>
				</td>
				';
		return $form;
	}

	/**
	 * getter for the bill id
	 * @return unknown
	 */
	function getBillID(){
		return $this->billID;
	}
	
	/**
	 * adds bill to the DB
	 * @param object $pdo, pdo connnection
	 * @return boolean true on DB insertion
	 */
	function addBill($pdo,$userID,$hhID){
		//sanitize the values
		$householdID = $hhID;
		//prepare statement
		$addBill =
		$pdo->prepare('INSERT INTO sp16_bills
				VALUES (NULL, :amount, :desc, :category, NOW(), :hhID, :userid)');
		//if executes return true
		if ($addBill->execute(array(':amount'=> $this->billAmount,
				':desc'=> $this->billDesc,
				':category'=> $this->billCategory,
				':hhID'=> $hhID,
				':userid' => $userID))) {
				return true;
		} else {
			return false;
		}
	}
	
	/**
	 * updates bill on DB
	 * @param string $array, contains the bill details
	 * @param object $pdo, pdo connection
	 * @return boolean
	 */
	function updateBill($array, $pdo){
		$desc = filter_var($array['desc'], FILTER_SANITIZE_STRING);
		$amount = filter_var($array['amount'], FILTER_SANITIZE_STRING);
		$category = filter_var($array['category'], FILTER_SANITIZE_STRING);
		
		$addBill =
		$pdo->prepare('UPDATE sp16_bills SET
				BillAmount = :amount,
				BillDesc = :desc,
				BillCategory = :category,
				BillDate = NOW()
				WHERE BillID = :billid');
		if ($addBill->execute(array(
				':amount'=> $amount,
				':desc'=> $desc,
				':category'=> $category,
				':billid' => $this->billID)))
		{
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * deletes a bill from the DB
	 * @param object $pdo, a pdo connection
	 */
	function deleteBill($pdo){
		$deleteBill =
		$pdo->prepare("DELETE FROM sp16_bills WHERE BillID = :id");
		$deleteBill->execute(array(':id' => $this->billID));
	}
}
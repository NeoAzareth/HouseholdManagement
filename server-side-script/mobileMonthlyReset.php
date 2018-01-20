<?php
include_once 'inc_0700/Connection.php';

//connection object
$Connection = new Connection();
$pdo = $Connection->getConnection();

$day= date('j');

$currentDecimalTime = (float)date('G') + ((float)date('i') * (100/60) * .01);

//this condition allows the reset of all the members only the first of
//every month in a range of 0:00 to 2:00 am
if((int)$day == 1 && ($currentDecimalTime >= 0.00 && $currentDecimalTime <= 2.00)){
	$statement = "UPDATE sm16_users SET UserStatus = 'not done' WHERE UserStatus = 'done'";
	
	$resetAllStatus =
	$pdo->prepare($statement);
	
	$resetAllStatus->execute();
	
}

unset($pdo);
unset($Connection);
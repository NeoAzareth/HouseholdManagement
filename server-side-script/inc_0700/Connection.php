<?php
include_once 'Config.php';

Class Connection{
	
	private $pdo = NULL;
    //this key must be the same as the pass on the config class in order for the connection to work
	private $key = '';
    //this is another layer of protection that allows some transactions with 
    //the use of this hashed code passed by the app
	private $code = '$2y$10$FEESC1PeRcmWjvY.9Xhkvu.NJUCfMxpWI5jTCXRc8AMV9XMLBao5i';
	
	
	public function __construct(){
		$config = new Config($this->key);
		$pdo = new PDO($config->getDB()
				,$config->getUsername()
				,$config->getPassword());
		$this->pdo = $pdo;
	}
	
	public function getConnection(){
		return $this->pdo;
	}
	
	public function getCode(){
		return $this->code;
	}
	
}
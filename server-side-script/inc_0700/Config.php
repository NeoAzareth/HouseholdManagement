<?php
Class Config{
	
    //db credentials
    //the $db variable is set like: 'mysql:host<<server name>>;dbname=<<database name>>'
    //replace the <<sever name>> and <<database name>> with yours
	private $db ='';
    //user name
	private $username = '';
    //password name
	private $password = '';
    
	//this is a layer of protection that ensures the only class that can access this configuration class is the
    //connection class
    //set this variable to be the same as the one in the connection class
    private $pass = '';
    //on instantiation this variable is set to be compared with the one above
	private $conPass = '';
	
	public function __construct($string){
		$this->conPass = $string;
	}
	
	public function getDB(){
		if($this->pass == $this->conPass){
			return $this->db;
		} else {
			return '';
		}
	}
	
	public function getUsername(){
		if($this->pass == $this->conPass){
			return $this->username;
		} else {
			return '';
		}
	}
	
	public function getPassword(){
		if($this->pass == $this->conPass){
			return $this->password;
		} else {
			return '';
		}
	}
}
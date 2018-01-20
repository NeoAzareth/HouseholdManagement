<?php

include_once 'inc_0700/Household.php';
include_once 'inc_0700/Member.php';
include_once 'inc_0700/Bill.php';

function areAllUsersDone($hhID,$pdo){

	$status =
	$pdo->prepare('SELECT FirstName FROM sm16_users WHERE UserStatus = "not done" AND HouseholdID = :id');
	$status->execute(array(':id'=>$hhID));
	$info = $status->fetchAll(PDO::FETCH_COLUMN);
	if (empty($info)) {
		return true;
	} else {
		return false;
	}
}

function sendPassword($userEmail,$pass){
		
	require_once 'PHPMailer/PHPMailerAutoload.php';

	$monthAndYear = date('F, Y');

	$message = '<html>
				<body>
				  <h3>Hello HHMobileApp user,</h3>
				  <p>You have requested a new password through the reset password feature.</p>
				<p>Your new password: '. $pass .'</p>
				<p>We recommend you to change your password on the in-app update password feature.</p>
				  <p>HHManagement mobile app.</p>
				</body>
				</html>';
		
		
	$email = new PHPMailer();
	$email->From = 'isrsan2@rainbow.dreamhost.com';
	$email->FromName = 'Household Management Mobile App';
	$email->Subject = 'New temp password.';
	$email->msgHTML($message);
	$email->addAddress( $userEmail );
	$email->Send();
}

function createSpreadsheet($id,$pdo){
	
	$currentMonth = date('Y-m');
	
	$household = new Household($id, $currentMonth, $pdo);
	unset($pdo);


	error_reporting(E_ALL);
	ini_set('display_errors', TRUE);
	ini_set('display_startup_errors', TRUE);

	define('EOL',(PHP_SAPI == 'cli') ? PHP_EOL : '<br />');

	/** Include PHPExcel */
	//require_once dirname(__FILE__) . '/../Classes/PHPExcel.php';
	require_once 'Classes/PHPExcel.php';

	PHPExcel_Cell::setValueBinder( new PHPExcel_Cell_AdvancedValueBinder() );

	// Create new PHPExcel object
	$objPHPExcel = new PHPExcel();
	$hhName = $household->getHhName();
	$year = date('Y');
	$month = date('F');
	$title = $hhName . ' ' . $month.'-'.$year;

	// Set document properties
	$objPHPExcel->getProperties()->setCreator("HhManagement")
	->setLastModifiedBy("HhManagement")
	->setTitle($title)
	->setSubject($month . ' ' . $year);

	//default styles
	$objPHPExcel->getDefaultStyle()->getFont()->setName('Calibri');
	$objPHPExcel->getDefaultStyle()->getFont()->setSize(12);


	//styles....

	//fonts
	//font red bold italic centered
	$fontRedBoldItalicCenter = array (
			'font' => array (
					'bold' => true,
					'italic' => true,
					'color' => array(
							'argb' => 'FFF40202',
					)
			),
			'alignment' => array (
					'vertical' => PHPExcel_Style_Alignment::VERTICAL_CENTER,
					'horizontal' => PHPExcel_Style_Alignment::HORIZONTAL_CENTER
			)
	);

	//font red bold
	$fontRedBold = array (
			'font' => array (
					'bold' => true,
					'color' => array(
							'argb' => 'FFF40202',
					)
			)
	);

	//font red
	$fontRed = array (
			'font' => array (
					'color' => array(
							'argb' => 'FFF40202',
					)
			)
	);

	//font Green
	$fontGreen = array (
			'font' => array (
					'color' => array(
							'argb' => '0008B448',
					)
			)
	);

	//font Bold Italic
	$fontBoldItalic = array (
			'font' => array (
					'bold' => true,
					'italic' => true,
			)
	);

	//font Bold Italic Centered
	$fontBoldItalicCenter = array (
			'font' => array (
					'bold' => true,
					'italic' => true,
			),
			'alignment' => array (
					'vertical' => PHPExcel_Style_Alignment::VERTICAL_CENTER,
					'horizontal' => PHPExcel_Style_Alignment::HORIZONTAL_CENTER
			)
	);

	//background fillings
	//fill red
	$fillRed = array (
			'fill' => array(
					'type' => PHPExcel_Style_Fill::FILL_SOLID,
					'startcolor' => array(
							'argb' => 'FFF40202',
					),
			),
	);

	//fill yellow
	$fillYellow = array (
			'fill' => array(
					'type' => PHPExcel_Style_Fill::FILL_SOLID,
					'startcolor' => array(
							'argb' => 'FFF2E500',
					),
			),
	);

	//fill green
	$fillGreen = array (
			'fill' => array(
					'type' => PHPExcel_Style_Fill::FILL_SOLID,
					'startcolor' => array(
							'argb' => 'FF92D050',
					),
			),
	);

	//fill gray
	$fillGray = array (
			'fill' => array(
					'type' => PHPExcel_Style_Fill::FILL_SOLID,
					'startcolor' => array(
							'argb' => 'FFD9D9D9',
					),
			),
	);

	//fill cream
	$fillCream = array (
			'fill' => array(
					'type' => PHPExcel_Style_Fill::FILL_SOLID,
					'startcolor' => array(
							'argb' => 'FFC4BD97',
					),
			),
	);

	//sets the heading for the first table
	$objPHPExcel->getActiveSheet()->getStyle('A1')->applyFromArray($fillCream);
	$objPHPExcel->getActiveSheet()->setCellValue('B1','Equal AMT');
	$objPHPExcel->getActiveSheet()->getStyle('B1')->applyFromArray($fontRedBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->getStyle('B1')->applyFromArray($fillCream);
	$objPHPExcel->getActiveSheet()->setCellValue('C1','Ind. bills');
	$objPHPExcel->getActiveSheet()->getStyle('C1')->applyFromArray($fontRedBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->getStyle('C1')->applyFromArray($fillCream);
	$objPHPExcel->getActiveSheet()->setCellValue('D1','To rent');
	$objPHPExcel->getActiveSheet()->getStyle('D1')->applyFromArray($fontRedBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->getStyle('D1')->applyFromArray($fillCream);

	$numberOfMembers = $household->getNumberOFMembers();
	$monthTotal = $household->getMonthTotal();
	$rent = $household->getHhRent();
	$col = 65;//starts at column A
	$row = 2;//the table starts at row 2

	//array used to associate the bills with the respective user
	$array =[];

	//sets the members names fair amount and value
	$members = $household->getMembers();
	foreach ($members as $member) {
		$name = $member->getUserFirstName();
		$cellName = chr($col) . $row;
		$objPHPExcel->getActiveSheet()->setCellValue($cellName,$name);
		$objPHPExcel->getActiveSheet()->getStyle($cellName)->applyFromArray($fontBoldItalic);

		$cellInd = chr($col+2) . $row;
		$objPHPExcel->getActiveSheet()->setCellValue($cellInd,'0.0');
		$objPHPExcel->getActiveSheet()->getStyle($cellInd)->getNumberFormat()
		->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
		$objPHPExcel->getActiveSheet()->getStyle($cellInd)->applyFromArray($fillRed);

		$cellFair = chr($col+1) . $row;
		$objPHPExcel->getActiveSheet()->getStyle($cellFair)->applyFromArray($fontRed);
		$objPHPExcel->getActiveSheet()->getStyle($cellFair)->getNumberFormat()
		->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
		$objPHPExcel->getActiveSheet()->getStyle($cellFair)->applyFromArray($fillGray);

		$cellRent = chr($col+3) . $row;
		$objPHPExcel->getActiveSheet()->setCellValue($cellRent,'=SUM('. $cellFair .'-'. $cellInd .')');
		$objPHPExcel->getActiveSheet()->getStyle($cellRent)->applyFromArray($fontGreen);
		$objPHPExcel->getActiveSheet()->getStyle($cellRent)->getNumberFormat()
		->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
		$objPHPExcel->getActiveSheet()->getStyle($cellInd)->applyFromArray($fillYellow);

		$array[$name]['cell'] = $cellInd;
		$row++;
	}

	//inserts the sum of the fair amounts to compare to the one below
	$endCell = chr($col+1) . ($row-1);
	$cell = chr($col+1) . $row;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'=SUM(B2:'.$endCell.')');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontRed);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->getNumberFormat()
	->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillGray);

	//insert the rent check values
	$cell = chr($col+2) .$row;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'Rent');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontBoldItalic);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillYellow);
	$cell = chr($col+3) . $row;
	$endCell = chr($col+3) .($row-1);
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'=SUM(D2:'.$endCell.')');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontRedBold);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->getNumberFormat()
	->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillYellow);

	//inserts the bill and amount labels
	$row += 2;
	$cellMergeEnd = chr($col+1) . $row;
	$cell = chr($col) . $row++;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'House bills');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->mergeCells($cell.':'.$cellMergeEnd);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillRed);

	$cell = chr($col) . $row;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'Bill');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillGreen);

	$cell = chr($col+1) . $row++;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'Amount');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillGreen);


	//inserts the bills
	$startCell = chr($col+1) . $row;
	foreach ($members as $member) {
		$name = $member->getUserFirstName();
		$col = 65;
		$bills = $member->getBills();
		$array[$name]['bills'] = [];
		foreach ($bills as $bill) {
			$desc = $bill->getBillDescription();
			$amount = $bill->getBillAmount();
			$objPHPExcel->getActiveSheet()->setCellValue(chr($col) . $row,$desc);
			$amountCell = chr($col+1) . $row++;
			$objPHPExcel->getActiveSheet()->setCellValue($amountCell,$amount);
			$objPHPExcel->getActiveSheet()->getStyle($amountCell)->getNumberFormat()
			->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
			array_push($array[$name]['bills'], $amountCell);
				
		}
	}

	$col = 65;

	//inserts rent
	$cell = chr($col) .$row;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'Rent');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillYellow);

	$cell = chr($col+1) . $row++;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,$rent);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->getNumberFormat()
	->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillYellow);
	$endCell = chr($col+1) . ($row-1);

	//inserts the total of bills
	$col = 65;
	$cell = chr($col) .$row;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'Total H-B');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontRedBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillCream);

	$cell = chr($col+1) .$row++;
	$objPHPExcel->getActiveSheet()->setCellValue($cell, '=SUM('. $startCell .':'. $endCell .')');
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fontRedBoldItalicCenter);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->getNumberFormat()
	->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillCream);

	//inserts the fair amount
	$cell = chr($col) .$row;
	$objPHPExcel->getActiveSheet()->setCellValue($cell,'Fair Amount if ' . $numberOfMembers);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillGray);
	$cell = chr($col+1) .$row;
	$objPHPExcel->getActiveSheet()
	->setCellValue($cell,'='. chr($col+1) .($row-1) . '/' . $numberOfMembers);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->getNumberFormat()
	->setFormatCode(PHPExcel_Style_NumberFormat::FORMAT_CURRENCY_USD_SIMPLE);
	$objPHPExcel->getActiveSheet()->getStyle($cell)->applyFromArray($fillGray);
	$fairAmountCell = chr($col+1) . $row;

	$row = 2;
	foreach ($members as $member) {
		$col = 66;
		$objPHPExcel->getActiveSheet()->setCellValue(chr($col) . $row++,'='. $fairAmountCell);
	}

	//inserts the individual bills
	foreach ($array as $value){
		$cell = $value['cell'];
		$sumOfBills = '';
			
		if (isset($value['bills'])) {
			$bills = $value['bills'];
			$counter = 1;
			foreach ($bills as $bill){
				if ($counter == 1){
					$sumOfBills .= $bill;
				} else {
					$sumOfBills .= '+' . $bill;
				}
				$counter++;
			}
		}
		$objPHPExcel->getActiveSheet()->setCellValue($cell,'=SUM('. $sumOfBills . ')');
	}

	$objPHPExcel->getActiveSheet()->getColumnDimension('A')->setAutoSize(true);
	$objPHPExcel->getActiveSheet()->getColumnDimension('B')->setAutoSize(true);
	$objPHPExcel->getActiveSheet()->getColumnDimension('C')->setAutoSize(true);
	$objPHPExcel->getActiveSheet()->getColumnDimension('D')->setAutoSize(true);

	// Rename worksheet
	$objPHPExcel->getActiveSheet()->setTitle($title);

	// Set active sheet index to the first sheet, so Excel opens this as the first sheet
	$objPHPExcel->setActiveSheetIndex(0);

	$objWriter = PHPExcel_IOFactory::createWriter($objPHPExcel, 'Excel2007');
	$objWriter->setPreCalculateFormulas(true);
	//$objWriter->save(str_replace('.php', '.xlsx', __FILE__));
	$objWriter->save(str_replace('.php', '.xlsx', 'spreadsheets/'. $title .'.php'));

	return $title . '.xlsx';
}

function mailMonthlySpreadsheet($id,$pdo){
	
	$currentMonth = date('Y-m');
		
	$household = new Household($id, $currentMonth, $pdo);
		
	require_once 'PHPMailer/PHPMailerAutoload.php';
	$admin = $household->getHouseholdAdmin();
	$adminName = $admin->getUserFullName();
	//$adminEmail = $admin->getUserEmailAddress();
		
	$members = $household->getMembers();
	$sheetName = createSpreadsheet($id,$pdo);
		
	$monthAndYear = date('F, Y');
	$hhName = $household->getHhName();
		
	$subject = str_replace('.xlsx', '', $sheetName) . ' closing';
		
	$monthSummary = $household->formatMonthlyBalanceReport();
		
	$message = '<html>
				<body>
				  <h3>Hello <<name>>,</h3>
				  <p>This is an automated email to notify you that '. $monthAndYear .' </br>
				  		period of the '. $hhName .' has been
				  		closed and balanced!</p>
				  <p>Here are the results: </br></p>
				  				'.$monthSummary.'
				  <p>HHManagement web app.</p>
				</body>
				</html>';
	$file_to_attach = 'spreadsheets/'.$sheetName;
		
	/*
	//$userEmail = $member->getUserEmailAddress();
	//$userName = $member->getUserFirstName();
	$email = new PHPMailer();
	$email->From = 'isrsan2@rainbow.dreamhost.com';
	$email->FromName = $adminName . '(HhManage Admin)';
	$email->Subject = $subject;
	//$email->Subject = 'Yet, another test... ignore';
	$email->msgHTML(str_replace('<<name>>', 'Israel', $message));
	$email->addAddress( "neoazareth@gmail.com");
	$email->addAttachment( $file_to_attach , $sheetName );
	$email->Send();
	*/
		
	
	 foreach ($members as $member) {
		 $userEmail = $member->getUserEmail();
		 $userName = $member->getUserFirstName();
		 $email = new PHPMailer();
		 $email->From = 'isrsan2@rainbow.dreamhost.com';
		 $email->FromName = $adminName . '(HhManage Admin)';
		 $email->Subject = $subject;
		 //$email->Subject = 'Yet, another test... ignore';
		 $email->msgHTML(str_replace('<<name>>', $userName, $message));
		 $email->addAddress( $userEmail);
		 $email->addAttachment( $file_to_attach , $sheetName );
		 $email->Send();
	 }
	 
	 unlink('spreadsheets/'.$sheetName);
}
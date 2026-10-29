package cl.bcs.custodia.pojo;

import java.util.HashMap;
import java.util.Map;

public class ConstantesCustodia {

	public final Map<String, String> codigoErroresBlockChain = getCodigoErroresBlockChain();
	
	private Map<String, String> getCodigoErroresBlockChain()
	{
		Map<String,String> myMap = new HashMap<String,String>();
		
		myMap.put("Add Lending Client not on Schedule",													"BC_501");
		myMap.put("Create Lending Agreement not on Schedule",											"BC_502");
		myMap.put("Trying to access an Lending Agreement that doesnt exist yet",						"BC_503");
		myMap.put("timeout expired while executing transaction",										"BC_504");
		myMap.put("Proposed data is invalid Symbol",													"BC_505");
		myMap.put("Proposed data is invalid BorrowingBrokerageHouseID",									"BC_506");
		myMap.put("Proposed data is invalid LoanType",													"BC_507");
		myMap.put("Lending Client already exists in Storage",											"BC_508");
		myMap.put("New clients Lending Quantity surpasses set total quantity for Lending Agreement",	"BC_509");
		myMap.put("Requesting Brokerage House ID does not match Borrower",								"BC_510");
		myMap.put("Matching Route not found",															"BC_511");
		myMap.put("Lending Qty surpasses Loans Qty To Be Assigned",										"BC_512");
		myMap.put("MSA not found or not Approved for the Borrowing Client",								"BC_513");
		myMap.put("Requesting Brokerage House ID does not match Lender",								"BC_514");
		myMap.put("Lending Agreement not on the allowed date to modify.",								"BC_515");
		myMap.put("Collateral is Mandatory for non Related Third clients.",								"BC_516");
		myMap.put("No matching Short Sale found for data",												"BC_517");
		myMap.put("Missing field: Arbitrator1",															"BC_518");
		myMap.put("Lending Agreement not on Schedule for Termination",									"BC_519");
		myMap.put("Successfully sent invoke transaction to the orderer.",								"BC_520");
		myMap.put("Invalid Symbol for this type of Lending Agreement",									"BC_521");
		myMap.put("is NOT multiple of LAN minimum cut",													"BC_522");
		myMap.put("No matching Loan found for data",													"BC_523");
		
		myMap.put("Proposed data is invalid ClientRUT, ",												"BC_524");
		myMap.put("Missing field: ClientAddress",														"BC_525");
		myMap.put("Missing field: ClientPhone",															"BC_526");
		myMap.put("Proposed data is invalid Email, ",													"BC_527");
		myMap.put("Missing field: ClientName",															"BC_528");
		myMap.put("Missing field: Arbitrator1",															"BC_529");
		myMap.put("Missing field: Arbitrator2",															"BC_530");
		myMap.put("Successfully Added Lending Client",													"BC_531");
		myMap.put("Lending Agreement not on the allowed date to modify",								"BC_532");
		myMap.put("New client's Lending Quantity surpasses set total quantity for Lending Agreement",	"BC_533");
		myMap.put("Lending Clients not on Schedule for modification",									"BC_534");
		myMap.put("Collateral is Mandatory for this LendingAgreementType",								"BC_535");
		myMap.put("Lending Agreement not on Schedule for modification", 								"BC_536");
		myMap.put("Yield is not valid, value must be between 0 and 100", 								"BC_537");
		
		myMap.put("CREATED", 																			"BC_538");
		myMap.put("ACTIVE", 																			"BC_539");
		myMap.put("TERMINATED", 																		"BC_540");
		myMap.put("MATURED", 																			"BC_541");
		
		return myMap;
	}	
}
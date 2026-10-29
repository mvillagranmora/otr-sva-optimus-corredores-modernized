package cl.bcs.contabilidad.pojo;

import cl.bcs.utils.Utils;

public class BusinessPartner {
	
	public String CardCode;
    public String CardName;
    public String CardType;
    public String FederalTaxID;//rut
	public String Address="Adress"; 
	public String City="City";
	public String County="County";
	public String GroupCode;//":100,
	public String Currency="##";
    
    
    public BusinessPartner(String CardCode,String Rut,String CardName,String CardType){
    	this.CardCode = CardCode;
    	this.CardName = CardName;
    	this.CardType = CardType;
    	this.GroupCode = CLIENTE;
    	this.FederalTaxID = Rut;
    	
    }
    
    public BusinessPartner(String CardCode,String Rut,String CardName,String CardType,String GroupCode){
    	this.CardCode = CardCode;
    	this.CardName = CardName;
    	this.CardType = CardType;
    	this.GroupCode = GroupCode;
    	this.FederalTaxID = Rut;
    	
    }
    
    public String toString() {
		return Utils.printJson(this, false);
	}
  
    public static final String CONTRAPARTE = "100";
    public static final String PROVEEDOR = "101";
    public static final String CLIENTE = "100";
    

}

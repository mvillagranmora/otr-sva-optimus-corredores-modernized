package cl.bcs.contabilidad.pojo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;



public class LoginSAP {
	
	private String UserName;
	private String Password;
	private String CompanyDB;
	
	
	public LoginSAP(String userName, String password, String companyDB) {
		super();
		this.UserName = userName;
		this.Password = password;
		this.CompanyDB = companyDB;
	}

	public String getUserName() {
	return UserName;
	}
	
	public void setUserName(String userName) {
	this.UserName = userName;
	}
	
	public String getPassword() {
	return Password;
	}
	
	public void setPassword(String password) {
	this.Password = password;
	}
	
	public String getCompanyDB() {
	return CompanyDB;
	}
	
	public void setCompanyDB(String companyDB) {
	this.CompanyDB = companyDB;
	}

}
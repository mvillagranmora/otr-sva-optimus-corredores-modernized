package cl.bcs.plataforma.com.pojo;

import java.math.BigDecimal;
import java.util.List;

import cl.bcs.utils.Utils;

public class CategoriaInversionista {
	public String IdCategoriaInversionista;
	public String NombreCategoriaInversionista;
	public Long IdEmpresa;
	public Long Jerarquia;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}



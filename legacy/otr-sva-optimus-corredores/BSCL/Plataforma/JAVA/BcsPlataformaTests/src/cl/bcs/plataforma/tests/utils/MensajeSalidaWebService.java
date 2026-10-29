package cl.bcs.plataforma.tests.utils;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.Utils;

public class MensajeSalidaWebService {
	public Long IdInterno;
	public String TipoMensaje;
	public String Codigo;
	public String Descripcion;
	
	public String toString() {
		return Utils.printJson(this, false);
	}	
}

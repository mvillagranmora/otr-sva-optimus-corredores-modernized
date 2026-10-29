package cl.bcs.plataforma.tests.utils;

import java.util.ArrayList;
import java.util.List;

public class WebServiceResponse {
	public Long ContadorErrores;
	public List<MensajeSalidaWebService> Mensajes;
	
	public WebServiceResponse() {
		Mensajes = new ArrayList<>();
	}
}

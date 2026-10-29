package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import cl.bcs.plataforma.tests.utils.WebServiceResponse;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.Utils;

public class TestWebServicesPlataforma {
	
	Gson gson = Utils.getGsonBuilder();
	List<String> endpoints = Arrays.asList("Clientes/ConsultarClientes","");
	
	@Test
    void myFirstTest() {
        assertEquals(2, 1 + 1);
    }
	
	@Test
	void testNoAutorizadoTokenInexistente() throws Exception {
		
	}
	
	@Test
	void testNoAutorizadoTokenInvalido() throws Exception {
		
	}
	
	@Test
	void testNoAutorizadoUsuarioClaveIncorrecta() throws Exception {
		CallRestService c = new CallRestService("http://desaservicios.optimuscb.cl/lb4/", "Clientes/consultarClientes", "x", "x", false, true);
		String res = c.callRestPost("");
		ConsultarClienteResponse response = gson.fromJson(res, ConsultarClienteResponse.class);
		assertEquals(response.Mensajes.get(0).TipoMensaje,"ERROR");
	}
	
	public static class ConsultarClienteResponse extends WebServiceResponse{
		
	}
}

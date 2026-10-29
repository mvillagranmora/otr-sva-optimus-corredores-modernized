package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.sql.SQLException;

import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public class AutorizacionEjemplo implements BcsAutorizaciones {

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
		
		SalidaAutorizacion s = new SalidaAutorizacion();
		
		s.P_OUT_ERRORESCONTADOR = 0L;
		s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"ORDEN_AUTORIZADA","Orden autorizada con exito"));
		
		ResultadoAutorizacion res = new ResultadoAutorizacion();
		res.evento = 1L;
		res.resultado = EXITO_AUTORIZACION;
		s.P_OUT_RESULTADO.add(res);
		
		return s;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
		
		SalidaAutorizacion s = new SalidaAutorizacion();
		
		s.P_OUT_ERRORESCONTADOR = 0L;
		s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"ORDEN_RECHAZADA","Orden RECHAZADA con exito"));
		
		ResultadoAutorizacion res = new ResultadoAutorizacion();
		res.evento = 1L;
		res.resultado = EXITO_AUTORIZACION;
		s.P_OUT_RESULTADO.add(res);
		
		return s;
	}
	
	public static void main (String [] args) throws SQLException {
		
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AutorizacionEjemplo e = new AutorizacionEjemplo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuario = 0L;
			
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "1111";
			entrada.p_Lista.add(evento);
			e.autorizarEvento(conn, entrada);
			
			
		}
		
	}

}

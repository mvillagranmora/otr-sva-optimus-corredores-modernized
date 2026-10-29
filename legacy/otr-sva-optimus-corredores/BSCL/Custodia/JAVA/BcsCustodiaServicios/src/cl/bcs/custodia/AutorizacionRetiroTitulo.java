package cl.bcs.custodia;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.custodia.ConfirmarAutorizacionRetiroTitulos.EntradaConfirmarAutorizacionRetiroTitulos;
import cl.bcs.custodia.ConfirmarAutorizacionRetiroTitulos.SalidaConfirmarAutorizacionRetiroTitulos;
import cl.bcs.custodia.RechazoAutorizacionRetiroTitulos.EntradaRechazoAutorizacionRetiroTitulos;
import cl.bcs.custodia.RechazoAutorizacionRetiroTitulos.SalidaRechazoAutorizacionRetiroTitulos;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public class AutorizacionRetiroTitulo implements BcsAutorizaciones {

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO

		SalidaAutorizacion s = new SalidaAutorizacion();		
		ConfirmarAutorizacionRetiroTitulos confirmar = new ConfirmarAutorizacionRetiroTitulos();
		EntradaConfirmarAutorizacionRetiroTitulos entradaConfirmar = new EntradaConfirmarAutorizacionRetiroTitulos();		
		List<Long> listaMovimientosEnTransito = new ArrayList<>();
		HashMap<Long,String> hashSalidaConfirmar = new HashMap<>();
		
		
		for(Evento evento : entrada.p_Lista) {	
			listaMovimientosEnTransito.add(Long.parseLong(evento.NumeroOperacionConcepto));
			entradaConfirmar.p_IdEmpresa = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo = evento.IdModulo;
			entradaConfirmar.p_IdUsuario = evento.IdUsuario;
		}
		
		try {
			entradaConfirmar.IdMovimientoCarteraEnTransito = listaMovimientosEnTransito;
			SalidaConfirmarAutorizacionRetiroTitulos salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);
			
			for(MensajeSalida salida : salidaConfirmacion.P_OUT_MENSAJES) {
				hashSalidaConfirmar.put(salida.id, salida.tipo);
			}
			
			for(Evento evento : entrada.p_Lista) {
				if(hashSalidaConfirmar.containsKey(evento.NumeroOperacionConcepto) && !hashSalidaConfirmar.get(evento.NumeroOperacionConcepto).equals("ERROR")) {
					s.P_OUT_ERRORESCONTADOR = 0L;
					s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"OPERACION_AUTORIZADA","Operacion autorizada con exito"));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					s.P_OUT_RESULTADO.add(res);
				}else {
					s.P_OUT_ERRORESCONTADOR = salidaConfirmacion.P_OUT_ERRORESCONTADOR;
					s.P_OUT_MENSAJES = salidaConfirmacion.P_OUT_MENSAJES;
				}		
			}
		} catch (Exception e1) {
			s.P_OUT_ERRORESCONTADOR = 1L;
		}
		return s;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		
		entrada.p_Lista = entrada.p_Lista; // aqui vienen los eventos por autorizar de la tabla AUT.EVENTO
		
		SalidaAutorizacion s = new SalidaAutorizacion();
		RechazoAutorizacionRetiroTitulos rechazo = new RechazoAutorizacionRetiroTitulos();
		
		for(Evento evento : entrada.p_Lista) {
			EntradaRechazoAutorizacionRetiroTitulos entradaRechazar = new EntradaRechazoAutorizacionRetiroTitulos();
			List<Long> listaMovimientosEnTransito = new ArrayList<>();
			listaMovimientosEnTransito.add(Long.parseLong(evento.NumeroOperacionConcepto));
			
			entradaRechazar.IdMovimientoCarteraEnTransito = listaMovimientosEnTransito;
			entradaRechazar.p_IdEmpresa = evento.IdEmpresa;
			entradaRechazar.p_IdModulo = evento.IdModulo;
			entradaRechazar.p_IdUsuario = evento.IdUsuario;
			try {
				SalidaRechazoAutorizacionRetiroTitulos salidaRechazo = rechazo.logica(conn, entradaRechazar);
				if(salidaRechazo.P_OUT_ERRORESCONTADOR != 0) {
					s.P_OUT_ERRORESCONTADOR = 0L;
					s.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"OPERACION_AUTORIZADA","Operacion autorizada con exito"));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					s.P_OUT_RESULTADO.add(res);
				}else {
					s.P_OUT_ERRORESCONTADOR = salidaRechazo.P_OUT_ERRORESCONTADOR;
					s.P_OUT_MENSAJES = salidaRechazo.P_OUT_MENSAJES;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return s;
	}
	
	public static void main (String [] args) throws SQLException {
		
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			AutorizacionRetiroTitulo e = new AutorizacionRetiroTitulo();
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

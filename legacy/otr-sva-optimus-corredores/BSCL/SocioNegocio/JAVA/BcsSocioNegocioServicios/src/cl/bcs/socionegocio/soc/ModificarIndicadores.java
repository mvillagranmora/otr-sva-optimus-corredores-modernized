package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.pojo.IndicadorPersonaCliente;
import cl.bcs.utils.Fecha;
import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.RegistroAtributo;
import cl.bcs.plataforma.ind.ModificarIndicador.SalidaModificarIndicador;

public class ModificarIndicadores {
	
	static final Logger logger = LogManager.getLogger(ModificarIndicadores.class);
	
	public static class EntradaModificarIndicadores {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoEntidad;
		public Long p_IdEntidad;
		public String p_ModeloCliente;
		public List<IndicadorPersonaCliente> p_Indicador;
		
		public EntradaModificarIndicadores() {
			this.p_Indicador = new ArrayList<>();
		}
	}
	
	public static class SalidaModificarIndicadores {
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		
		public SalidaModificarIndicadores() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;	
		}
	}
	
	public SalidaModificarIndicadores logica(Connection conn, EntradaModificarIndicadores entrada) throws Exception{
		
		logger.info("INICIO::ModificarIndicadores");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarIndicadores out = new SalidaModificarIndicadores();
		SalidaModificarIndicador salidaModificarIndicador = new SalidaModificarIndicador();
		salidaModificarIndicador = modificarIndicador(conn, entrada);
		
		if(!salidaModificarIndicador.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarIndicador.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarIndicador.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarIndicadores", finServicio - inicioServicio);
		
		return out;	
	}
	
	
	private SalidaModificarIndicador modificarIndicador(Connection conn, EntradaModificarIndicadores in) throws Exception {
		
		logger.info("INICIO::ModificarIndicadores.modificarIndicador");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarIndicador salida = new SalidaModificarIndicador();
		
		List<RegistroAtributo> indicadores = obtenerIndicadores(in.p_Indicador);
		
		if(!indicadores.isEmpty()) {
	
			ModificarIndicador modificarIndicador = new ModificarIndicador();
			EntradaModificarIndicador entrada= new EntradaModificarIndicador();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_IdTipoEntidad = in.p_IdTipoEntidad;
			entrada.p_IdEntidad = in.p_IdEntidad;
			entrada.p_ModeloCliente = in.p_ModeloCliente;
			entrada.p_RegistroAtributo = indicadores;
			
			return modificarIndicador.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarIndicadores.modificarIndicador", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private List<RegistroAtributo> obtenerIndicadores(List<IndicadorPersonaCliente> indicadores) {
		
		logger.info("INICIO::ModificarIndicadores.obtenerIndicadores");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroAtributo> registroAtributo = new ArrayList<RegistroAtributo>();
		
		for(IndicadorPersonaCliente indicador: indicadores) {
			RegistroAtributo registro = new RegistroAtributo();
			registro.IdIndicador	= indicador.IdIndicador;
			registro.IdTipoIndicador = indicador.CodigoTipoIndicador;
			registro.ValorIndicador	= indicador.ValorIndicador;
			registro.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
			registroAtributo.add(registro);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarIndicadores.obtenerIndicadores", finServicio - inicioServicio);
		
		return registroAtributo;
	}

}

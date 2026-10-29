package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.SalidaModificarIndicador;
import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.Indicador;
import cl.bcs.socionegocio.enr.pojo.Portafolio;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AdministrarEnrolamientoIndicador implements JavaCall  {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoIndicador.class);

	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/AdministrarEnrolamientoIndicador_SelectIndicadoresPortafolioPorIdPortafolio.sql");
	
	public List<DatosClienteDatosGenerales> modificarIndicadoresPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoIndicador.modificarIndicadoresPersona");
		long inicio = System.currentTimeMillis();
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<Indicador> indicadores = borradorCliente.Indicadores
					.parallelStream()
					.filter(indicador->"PERSONA".equals(indicador.IdTipoEntidad))
					.collect(Collectors.toList());
			
			EntradaModificarIndicador entradaModificarIndicador = new EntradaModificarIndicador();
			entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarIndicador.p_IdModulo = entrada.p_IdModulo;
			entradaModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			
			entradaModificarIndicador.p_IdTipoEntidad = "PERSONA";
			entradaModificarIndicador.p_IdEntidad = borradorCliente.IdPersona;
			
			for(Indicador indicador : indicadores) {
				entradaModificarIndicador.p_ModeloCliente = indicador.ModeloCliente;
				ModificarIndicador.RegistroAtributo registroAtributo = new ModificarIndicador.RegistroAtributo();
				registroAtributo.IdIndicador = indicador.IdIndicadorPersona;
				registroAtributo.IdTipoIndicador = indicador.IdTipoIndicador;
				registroAtributo.ValorIndicador = indicador.ValorIndicador;
				registroAtributo.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
				entradaModificarIndicador.p_RegistroAtributo.add(registroAtributo);
			}
			
			ModificarIndicador modificarIndicador = new ModificarIndicador();
			SalidaModificarIndicador salidaModificarIndicador = modificarIndicador.logica(con, entradaModificarIndicador);
			borradorCliente.MensajesSalida.addAll(salidaModificarIndicador.P_OUT_MENSAJES);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoIndicador.modificarIndicadoresPersona", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarIndicadoresCliente(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoIndicador.modificarIndicadoresCliente");
		long inicio = System.currentTimeMillis();
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<Indicador> indicadores = borradorCliente.Indicadores
					.parallelStream()
					.filter(indicador->"CLIENTE".equals(indicador.IdTipoEntidad))
					.collect(Collectors.toList());
			
			EntradaModificarIndicador entradaModificarIndicador = new EntradaModificarIndicador();
			entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarIndicador.p_IdModulo = entrada.p_IdModulo;
			entradaModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			
			entradaModificarIndicador.p_IdTipoEntidad = "CLIENTE";
			entradaModificarIndicador.p_IdEntidad = borradorCliente.Cliente.IdCliente;
			
			for(Indicador indicador : indicadores) {
				entradaModificarIndicador.p_ModeloCliente = indicador.ModeloCliente;
				ModificarIndicador.RegistroAtributo registroAtributo = new ModificarIndicador.RegistroAtributo();
				registroAtributo.IdIndicador = indicador.IdIndicadorPersona;
				registroAtributo.IdTipoIndicador = indicador.IdTipoIndicador;
				registroAtributo.ValorIndicador = indicador.ValorIndicador;
				registroAtributo.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");;
				entradaModificarIndicador.p_RegistroAtributo.add(registroAtributo);
			}
			
			ModificarIndicador modificarIndicador = new ModificarIndicador();
			SalidaModificarIndicador salidaModificarIndicador = modificarIndicador.logica(con, entradaModificarIndicador);
			borradorCliente.MensajesSalida.addAll(salidaModificarIndicador.P_OUT_MENSAJES);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoIndicador.modificarIndicadoresCliente", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarIndicadoresPortafolio(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoIndicador.modificarIndicadoresPortafolio");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		SQL sql = new SQL();
		List<Long> idsPortafolio = new ArrayList<Long>();
		HashMap<Long,List<Indicador>> indicadoresPorPortafolio = new HashMap<Long,List<Indicador>>();
		
		
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			for(Portafolio portafolio : borradorCliente.Portafolio) {
				idsPortafolio.add(portafolio.IdPortafolio);
				parametros.put("IdsPortafolio", idsPortafolio);
				List<Indicador> indicadoresPortafolio = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, parametros, Indicador.class);
				
				idsPortafolio.stream().forEach( idPort->{
					List<Indicador> lstIndicadores = indicadoresPortafolio.stream().filter(indic-> indic.IdInterno.equals(idPort)).collect(Collectors.toList());
					indicadoresPorPortafolio.put(idPort, lstIndicadores);
				});
			
			

					List<Indicador> indicadores = borradorCliente.Indicadores
							.parallelStream()
							.filter(indicador->"PORTAFOLIO".equals(indicador.IdTipoEntidad))
							.collect(Collectors.toList());
					
					EntradaModificarIndicador entradaModificarIndicador = new EntradaModificarIndicador();
					entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
					entradaModificarIndicador.p_IdModulo = entrada.p_IdModulo;
					entradaModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
					
					entradaModificarIndicador.p_IdTipoEntidad = "PORTAFOLIO";
					entradaModificarIndicador.p_IdEntidad = portafolio.IdPortafolio;
					List<Indicador> indicadoresPort = indicadoresPorPortafolio.get(portafolio.IdPortafolio);
					
					for(Indicador indicador : indicadores) {
						entradaModificarIndicador.p_ModeloCliente = indicador.ModeloCliente;
						ModificarIndicador.RegistroAtributo registroAtributo = new ModificarIndicador.RegistroAtributo();
						Long idIndicador = indicadoresPort.stream().filter(ind -> ind.IdTipoIndicador.equals(indicador.IdTipoIndicador)).count() == 1 ? indicadoresPortafolio.stream().filter(ind -> ind.IdTipoIndicador.equals(indicador.IdTipoIndicador)).findFirst().get().IdIndicador : 0L;
						registroAtributo.IdIndicador = idIndicador > 0 ? idIndicador : indicador.IdIndicadorPersona;
						registroAtributo.IdTipoIndicador = indicador.IdTipoIndicador;
						registroAtributo.ValorIndicador = indicador.ValorIndicador;
						registroAtributo.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
						entradaModificarIndicador.p_RegistroAtributo.add(registroAtributo);
					}
					
					ModificarIndicador modificarIndicador = new ModificarIndicador();
					SalidaModificarIndicador salidaModificarIndicador = modificarIndicador.logica(con, entradaModificarIndicador);
					borradorCliente.MensajesSalida.addAll(salidaModificarIndicador.P_OUT_MENSAJES);
				
			};
		};
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoIndicador.modificarIndicadoresPortafolio", fin - inicio);
		return borradoresCliente;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}

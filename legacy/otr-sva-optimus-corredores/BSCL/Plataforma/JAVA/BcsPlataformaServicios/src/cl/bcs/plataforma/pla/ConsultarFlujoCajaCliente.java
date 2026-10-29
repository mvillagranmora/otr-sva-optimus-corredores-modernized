package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarFlujoCajaCliente implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ConsultarFlujoCaja.class);
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarFlujoCaja jin = gson.fromJson(in.toString(), EntradaConsultarFlujoCaja.class);
		SalidaConsultarFlujoCaja out = logica(conn, jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public SalidaConsultarFlujoCaja logica(Connection con, EntradaConsultarFlujoCaja entrada) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio ConsultarFlujoCajaCliente");
		
		SalidaConsultarFlujoCaja salidaConsultarFlujoCaja = new SalidaConsultarFlujoCaja();
		
		ConsultarFlujoCaja ConsultarFlujoCaja = new ConsultarFlujoCaja();
		salidaConsultarFlujoCaja = ConsultarFlujoCaja.logica(con, entrada);		
		
		if (entrada.p_PortafolioAConsultar.size() > 1 && salidaConsultarFlujoCaja.P_OUT_ERRORESCONTADOR == 0) {	
			
			List<CalculoEntrada> calculosParidad = salidaConsultarFlujoCaja.P_OUT_FLUJOCAJA.stream()
				    .map(flujoCaja -> {
				        CalculoEntrada calculoParidad = new CalculoEntrada();
				        calculoParidad.IdTrabajo = (long) (Math.random() * 10000000000L);
				        calculoParidad.IdInterno = 0L;
				        calculoParidad.Fecha = flujoCaja.FechaSaldo;
				        calculoParidad.IdMonedaOrigen = flujoCaja.IdMoneda;
				        calculoParidad.IdMonedaDestino = "CLP";
				        calculoParidad.MontoOrigen = flujoCaja.Saldo;
				        calculoParidad.IdUso = flujoCaja.NombreUso;
				        return calculoParidad;
				    })
				    .collect(Collectors.toList());
	        
			CalcularParidadEntrada entradaCalcularParidad = new CalcularParidadEntrada();
			entradaCalcularParidad.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCalcularParidad.p_IdTrabajo = (long) (Math.random()*10000000000L);
			entradaCalcularParidad.p_CalculosEntrada = calculosParidad;

			
			CalcularParidad calcularParidad = new CalcularParidad();			
			CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, entradaCalcularParidad);
			
			List<ObjFLUJOCAJA> flujosCajaParidad = new ArrayList<>();
			
			if (calcularParidadSalida.P_OUT_ERRORESCONTADOR > 0) {
				salidaConsultarFlujoCaja.P_OUT_ERRORESCONTADOR += calcularParidadSalida.P_OUT_ERRORESCONTADOR;
				String codigoError = "ERROR_PARIDAD";
				MensajeSalida mensajeError = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoError).msg());
				salidaConsultarFlujoCaja.P_OUT_MENSAJES.add(mensajeError);
			} else {
				for( CalculoSalida calculoSalida : calcularParidadSalida.p_out_CalculosSalida) {
					
					PortafolioAConsultar portafolioSeleccionado = entrada.p_PortafolioAConsultar.stream().filter(port -> port.IdMoneda.equalsIgnoreCase(calculoSalida.IdMonedaOrigen)).findFirst().get();
					
					ObjFLUJOCAJA flujoCajaParidad = new ObjFLUJOCAJA();
					flujoCajaParidad.FechaSaldo = entrada.p_FechaDesde;
					flujoCajaParidad.IdMoneda = calculoSalida.IdMonedaDestino;
					flujoCajaParidad.IdPortafolio = portafolioSeleccionado.IdPortafolio;
					flujoCajaParidad.NombreUso = calculoSalida.IdUso;
					flujoCajaParidad.Saldo = calculoSalida.MontoDestino;
					
					flujosCajaParidad.add(flujoCajaParidad);
				}
				
				List<ObjFLUJOCAJA> flujosCajaAgrupado = flujosCajaParidad.stream()
				        .collect(Collectors.toMap(
				            flujoCaja -> flujoCaja.NombreUso,
				            flujoCaja -> {
				                ObjFLUJOCAJA nuevoFlujo = new ObjFLUJOCAJA();
				                nuevoFlujo.FechaSaldo = flujoCaja.FechaSaldo;
				                nuevoFlujo.NombreUso = flujoCaja.NombreUso;
				                nuevoFlujo.IdMoneda = flujoCaja.IdMoneda;
				                nuevoFlujo.IdPortafolio = flujoCaja.IdPortafolio;
				                nuevoFlujo.Saldo = flujoCaja.Saldo;
				                return nuevoFlujo;
				            },
				            (flujoExistente, flujoNuevo) -> { 
				                flujoExistente.Saldo = flujoExistente.Saldo.add(flujoNuevo.Saldo);
				                return flujoExistente;
				            }
				        ))
				        .values()
				        .stream()
				        .collect(Collectors.toList());	
				
				salidaConsultarFlujoCaja.P_OUT_FLUJOCAJA.clear();
				salidaConsultarFlujoCaja.P_OUT_FLUJOCAJA = flujosCajaAgrupado;
			}
		} 
		
		SalidaConsultarFlujoCaja out = new SalidaConsultarFlujoCaja();
		out.P_OUT_ERRORESCONTADOR = salidaConsultarFlujoCaja.P_OUT_ERRORESCONTADOR;
		out.P_OUT_FLUJOCAJA = salidaConsultarFlujoCaja.P_OUT_FLUJOCAJA;
		out.P_OUT_FLUJOCAJADETALLADO = salidaConsultarFlujoCaja.P_OUT_FLUJOCAJADETALLADO;
		out.P_OUT_MENSAJES = salidaConsultarFlujoCaja.P_OUT_MENSAJES;
		
		logger.info("Termino del servicio ConsultarFlujoCajaCliente, tiempo:" + (System.currentTimeMillis() - init));		
		return out;
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("== TEST =="); 
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_UsaReglasNegocio\":\"S\",\"p_IdModulo\":\"CTA\",\"p_FechaDesde\":\"2025-01-13\",\"p_FechaHasta\":\"2025-01-13\",\"p_AgruparPorOrigen\":\"S\",\"p_DesplegarEnUI\":\"S\",\"p_PortafolioAConsultar\":[{\"IdMoneda\":\"CLP\",\"IdPortafolio\":327621},{\"IdMoneda\":\"EUR\",\"IdPortafolio\":327621},{\"IdMoneda\":\"USD\",\"IdPortafolio\":327621}]}";
			
			EntradaConsultarFlujoCaja ObjIn = gson.fromJson(json, EntradaConsultarFlujoCaja.class);
			ConsultarFlujoCajaCliente demo = new ConsultarFlujoCajaCliente();
			try {
				SalidaConsultarFlujoCaja ObjOut = demo.logica(con, ObjIn);
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
			} finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}

	

}

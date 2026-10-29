package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.GenerarCartola.NominaPaso;
import cl.bcs.normativa.cartola.GenerarCartola.ServicioEjecutado;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InfoCarteraMaster implements BcsCartola{
	
	public final Log log = new Log("NOR.InfoCarteraMaster");
	private final static Logger logger = LogManager.getLogger(InfoCarteraMaster.class);
	
	private final String SQL_SELECT_INFORMACION_CLIENTE = loadResourceToString("cl/bcs/normativa/cartola/sql/InfoCarteraMaster_SelectInformacionCliente.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	private final String SQL_SELECT_NOMINA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectNominaPaso.sql");
	
	private final String SQL_SELECT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectServicioEjecutado.sql");
	private final String SQL_INSERT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertServicioEjecutado.sql");
	private final String SQL_INSERT_INFO_CARTERA_MASTER_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/InfoCarteraMaster_InsertOutServicioEjecutado.sql");
	private final String SQL_SELECT_INFO_CARTERA_MASTER_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/InfoCarteraMaster_SelectOUTServicioEjecutado.sql");

	private static final List<String> MONEDAS = Arrays.asList("UF","USD","EUR");
	
	public static class InfoInformacionFinanciera{
		public BigDecimal Valor_UF;
		public BigDecimal Valor_USD;
		public BigDecimal Valor_EUR;
		public BigDecimal ValorMonedaAdicional;
		
		public InfoInformacionFinanciera(){
			this.Valor_UF = BigDecimal.ZERO;
			this.Valor_USD = BigDecimal.ZERO;
			this.Valor_EUR = BigDecimal.ZERO;
			this.ValorMonedaAdicional = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionCliente{
		public Long IdCliente;
		public Long IdPortafolio;
		public String NombreCliente;
		public String TipoPortafolio;
		public String DireccionPrincipal;
		public String Direccion;
		public String ComunaPrincipal;
		public String Comuna;
		public String PaisPrincipal;
		public String Pais;
		public String DocumentoIdentidad;
		public String TipoCliente;
		public String FechaDesde;
		public String FechaHasta;
		public String FechaEmision;
		public String DocumentoIdentidadCavali;
		public String nombre;
		public String rut;
		public String cuenta;
		public String direccionPrincipal;
		public String direccion;
		public String comunaPrincipal;
		public String comuna;
		public String paisPrincipal;
		public String pais;
		public String agente;
		public String Tipo_Cuenta;
		public String codigoPostal;
		public Timestamp desde;
		public Timestamp hasta;
		public Timestamp fecha_emision;
		public String tel;
		public Timestamp fecha;
		public BigDecimal Valor_UF;
		public BigDecimal Valor_USD;
		public BigDecimal Valor_EUR;
		public BigDecimal ValorMonedaAdicional;	
		public String tieneAdministracionCartera;
		public Integer numeroFicha;
		public String PaisNombreDivisionPaisPrincipal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionClientePaso extends InformacionCliente{
		public Long IdNomina;
		public Long IdTrabajo;
        
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public InformacionClientePaso() {
			
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::InfoCarteraMaster");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		
		Gson gson = new Gson();	
		NominaPaso nomina = new NominaPaso();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<NominaPaso> nominas = new ArrayList();
		Boolean primeraEjecucion = false;
		Long idTrabajoEjecutado = 0L;
		try {
			nominas = sql.query(con, SQL_SELECT_NOMINA_PASO, new HashMap<>(), NominaPaso.class);
		} catch (Exception e) {
			logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_NOMINA_PASO "+e);
		}
		if(!nominas.isEmpty()) {
			nomina = nominas.get(0);
			if(nomina.IdNomina.compareTo(0L) != 0L) {
				JsonParser parser = new JsonParser();
				JsonObject jaison = parser.parse(gson.toJson(entrada)).getAsJsonObject();
				String jeisonString = jaison.toString();
				String clase = this.getClass().getSimpleName();
				HashMap<String,Object> paramsJson = new HashMap<String,Object>();
				List<ServicioEjecutado> serviciosEjecutados = new ArrayList();
				List<InformacionClientePaso> outGuardado = new ArrayList();
				List<InformacionCliente> listOutExistente = new ArrayList();
				paramsJson.put("IdNomina", nomina.IdNomina);
				paramsJson.put("Recurso", clase);
				try {
					serviciosEjecutados = sql.query(con, SQL_SELECT_SERVICIOS_EJECUTADOS, paramsJson, ServicioEjecutado.class);
				} catch (Exception e) {
					logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
				}
				
				if(serviciosEjecutados.isEmpty()) {
					List<ServicioEjecutado> portObj = new ArrayList<>();
					ServicioEjecutado se = new ServicioEjecutado();
					se.IdNomina = nomina.IdNomina;
					se.IdTrabajo = idTrabajo;
					se.Entrada = jeisonString;
					se.Recurso = clase;
					portObj.add(se);
					try {
						sql.batch(con, SQL_INSERT_SERVICIOS_EJECUTADOS, portObj, ServicioEjecutado.class);
					} catch (Exception e) {
						logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
					}
					primeraEjecucion = true;
				}
				else
				{
					HashMap<String,Object> paramsOut = new HashMap<String,Object>();
					paramsOut.put("IdNomina", nomina.IdNomina);
					try {
						outGuardado = sql.query(con, SQL_SELECT_INFO_CARTERA_MASTER_OUT,paramsOut, InformacionClientePaso.class);
					} catch (Exception e) {
						logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_OUT "+e);
					}
					for(ServicioEjecutado se : serviciosEjecutados) {
						if(se.Entrada.equals(jeisonString) && se.IdNomina.compareTo(nomina.IdNomina) == 0L && se.Recurso.equals(clase)) {
							idTrabajoEjecutado = se.IdTrabajo;
							primeraEjecucion = false;
							break;
						}
						else {
							primeraEjecucion = true;
						}
					}
					if(primeraEjecucion == false) {
						
						listOutExistente = parseaObjetoDePaso(outGuardado,idTrabajoEjecutado);
						if(listOutExistente.size() > 0) {
							List<HashMap<String,Object>> ResultadoinfoClientes = Utils.List2ListHashGenericObject(listOutExistente);
							out.P_OUT_SALIDA.put("informacionClientes", ResultadoinfoClientes);
							return out;
						}
					}
				}
			}
			else {
				primeraEjecucion = false;
			}
		}
		
		
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String monedaAdicional = null;
		
		//El Metodo arroja error cuando no encuentra parametro
		try{
			monedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		}catch(Exception e){
			log.write(e);
		}
		
		if(monedaAdicional == null || monedaAdicional.isEmpty()){
			monedaAdicional = "USD"; //Cuando no existe moneda es USD
		}
		
		List<Long> portafolios = new ArrayList<Long>();
		
		List<Portafolio> portObj = new ArrayList<>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
		}
		
		CalcularParidad calcularParidad = new CalcularParidad();
		
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
		Long i = 1L;
		for(String moneda : InfoCarteraMaster.MONEDAS){
			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 1L;
			calculoEntrada.IdInterno = i;
			calculoEntrada.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHastaOrigen);
			calculoEntrada.IdMonedaOrigen = moneda;
			calculoEntrada.IdMonedaDestino = monedaSistema;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen = BigDecimal.ONE;
			calculoEntrada.IdUso = "CARTOLA";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
			i++;
		}
		
		//Agregamos moneda adicional
		CalculoEntrada calculoEntrada = new CalculoEntrada();
		calculoEntrada.IdTrabajo = 1L;
		calculoEntrada.IdInterno = i;
		calculoEntrada.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHastaOrigen);
		calculoEntrada.IdMonedaOrigen = monedaAdicional;
		calculoEntrada.IdMonedaDestino = monedaSistema;
		calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
		calculoEntrada.MontoOrigen = BigDecimal.ONE;
		calculoEntrada.IdUso = "CARTOLA";
		calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
		
		CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
		
		InfoInformacionFinanciera infoInformacionFinanciera = new InfoInformacionFinanciera();
		
		for(CalculoSalida salida : calculoSalida.p_out_CalculosSalida){
			if("UF".equals(salida.IdMonedaOrigen)){
				infoInformacionFinanciera.Valor_UF = salida.MontoDestino;
			}
			if("USD".equals(salida.IdMonedaOrigen)){
				infoInformacionFinanciera.Valor_USD = salida.MontoDestino;
			}
			if("EUR".equals(salida.IdMonedaOrigen)){
				infoInformacionFinanciera.Valor_EUR = salida.MontoDestino;
			}
			if(monedaAdicional.equals(salida.IdMonedaOrigen)){
				infoInformacionFinanciera.ValorMonedaAdicional = salida.MontoDestino;
			}
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);

		
		
		List<InformacionCliente> informacionClientes = sql.query(con, SQL_SELECT_INFORMACION_CLIENTE, params, InformacionCliente.class);
		
		for(InformacionCliente info : informacionClientes){
			info.Valor_UF = infoInformacionFinanciera.Valor_UF;
			info.Valor_USD = infoInformacionFinanciera.Valor_USD;
			info.Valor_EUR = infoInformacionFinanciera.Valor_EUR;
			info.ValorMonedaAdicional = infoInformacionFinanciera.ValorMonedaAdicional;
			info.FechaDesde = Fecha.stringToSqlDate(entrada.p_FechaDesde);
			info.FechaHasta = Fecha.stringToSqlDate(entrada.p_FechaHastaOrigen);
			info.desde = Fecha.toTimeStamp(Fecha.stringToSqlDate(entrada.p_FechaDesde) + " 00:00:00");
			info.hasta = Fecha.toTimeStamp(Fecha.stringToSqlDate(entrada.p_FechaHastaOrigen) + " 00:00:00");
			info.fecha = Fecha.toTimeStamp(Fecha.stringToSqlDate(entrada.p_FechaHastaOrigen) + " 00:00:00");
		}
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));
		
		if(primeraEjecucion) {
			try {
				List<InformacionClientePaso> listOutPaso =  new ArrayList();
				for(InformacionCliente salida : informacionClientes) {
					InformacionClientePaso outPaso = new InformacionClientePaso();
					outPaso.IdNomina = nomina.IdNomina;
					outPaso.IdTrabajo = idTrabajo;
					outPaso.IdCliente = salida.IdCliente;
					outPaso.IdPortafolio = salida.IdPortafolio;
					outPaso.NombreCliente = salida.NombreCliente;
					outPaso.TipoPortafolio = salida.TipoPortafolio;
					outPaso.DireccionPrincipal = salida.DireccionPrincipal;
					outPaso.Direccion = salida.Direccion;
					outPaso.ComunaPrincipal = salida.ComunaPrincipal;
					outPaso.Comuna = salida.Comuna;
					outPaso.PaisPrincipal = salida.PaisPrincipal;
					outPaso.Pais = salida.Pais;
					outPaso.DocumentoIdentidad = salida.DocumentoIdentidad;
					outPaso.TipoCliente = salida.TipoCliente;
					outPaso.FechaDesde = salida.FechaDesde;
					outPaso.FechaHasta = salida.FechaHasta;
					outPaso.FechaEmision = salida.FechaEmision;
					outPaso.DocumentoIdentidadCavali = salida.DocumentoIdentidadCavali;
					outPaso.nombre = salida.nombre;
					outPaso.rut = salida.rut;
					outPaso.cuenta = salida.cuenta;
					outPaso.direccionPrincipal = salida.direccionPrincipal;
					outPaso.direccion = salida.direccion;
					outPaso.comunaPrincipal = salida.comunaPrincipal;
					outPaso.comuna = salida.comuna;
					outPaso.paisPrincipal = salida.paisPrincipal;
					outPaso.pais = salida.pais;
					outPaso.agente = salida.agente;
					outPaso.Tipo_Cuenta = salida.Tipo_Cuenta;
					outPaso.codigoPostal = salida.codigoPostal;
					outPaso.desde = salida.desde;
					outPaso.hasta = salida.hasta;
					outPaso.fecha_emision = salida.fecha_emision;
					outPaso.tel = salida.tel;
					outPaso.fecha = salida.fecha;
					outPaso.Valor_UF = salida.Valor_UF;
					outPaso.Valor_USD = salida.Valor_USD;
					outPaso.Valor_EUR = salida.Valor_EUR;
					outPaso.ValorMonedaAdicional = salida.ValorMonedaAdicional;	
					outPaso.tieneAdministracionCartera = salida.tieneAdministracionCartera;
					outPaso.numeroFicha = salida.numeroFicha;
					outPaso.PaisNombreDivisionPaisPrincipal = salida.PaisNombreDivisionPaisPrincipal;
					listOutPaso.add(outPaso);
				}
				sql.batch(con, SQL_INSERT_INFO_CARTERA_MASTER_OUT, listOutPaso, InformacionClientePaso.class);
			} catch (Exception e) {
				logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_OUT_SERVICIO_EJECUTADO "+e);
			}
		}
		List<HashMap<String,Object>> ResultadoinfoClientes = Utils.List2ListHashGenericObject(informacionClientes);
		
		out.P_OUT_SALIDA.put("informacionClientes", ResultadoinfoClientes);
		
		log.write("FIN::InfoCarteraMaster");
		return out;
	}
	
	private List<InformacionCliente> parseaObjetoDePaso(List<InformacionClientePaso> listObjPaso,Long idTrabajo){
		List<InformacionCliente> listSalidaCartera = new ArrayList<>();
		for(InformacionClientePaso p : listObjPaso) {
			if(p.IdTrabajo.compareTo(idTrabajo) == 0L) {
				InformacionCliente outExistente = new InformacionCliente();
				outExistente.IdCliente = p.IdCliente;
				outExistente.IdPortafolio = p.IdPortafolio;
				outExistente.NombreCliente = p.NombreCliente;
				outExistente.TipoPortafolio = p.TipoPortafolio;
				outExistente.DireccionPrincipal = p.DireccionPrincipal;
				outExistente.Direccion = p.Direccion;
				outExistente.ComunaPrincipal = p.ComunaPrincipal;
				outExistente.Comuna = p.Comuna;
				outExistente.PaisPrincipal = p.PaisPrincipal;
				outExistente.Pais = p.Pais;
				outExistente.DocumentoIdentidad = p.DocumentoIdentidad;
				outExistente.TipoCliente = p.TipoCliente;
				outExistente.FechaDesde = p.FechaDesde;
				outExistente.FechaHasta = p.FechaHasta;
				outExistente.FechaEmision = p.FechaEmision;
				outExistente.DocumentoIdentidadCavali = p.DocumentoIdentidadCavali;
				outExistente.nombre = p.nombre;
				outExistente.rut = p.rut;
				outExistente.cuenta = p.cuenta;
				outExistente.direccionPrincipal = p.direccionPrincipal;
				outExistente.direccion = p.direccion;
				outExistente.comunaPrincipal = p.comunaPrincipal;
				outExistente.comuna = p.comuna;
				outExistente.paisPrincipal = p.paisPrincipal;
				outExistente.pais = p.pais;
				outExistente.agente = p.agente;
				outExistente.Tipo_Cuenta = p.Tipo_Cuenta;
				outExistente.codigoPostal = p.codigoPostal;
				outExistente.desde = p.desde;
				outExistente.hasta = p.hasta;
				outExistente.fecha_emision = p.fecha_emision;
				outExistente.tel = p.tel;
				outExistente.fecha = p.fecha;
				outExistente.Valor_UF = p.Valor_UF;
				outExistente.Valor_USD = p.Valor_USD;
				outExistente.Valor_EUR = p.Valor_EUR;
				outExistente.ValorMonedaAdicional = p.ValorMonedaAdicional;	
				outExistente.tieneAdministracionCartera = p.tieneAdministracionCartera;
				outExistente.numeroFicha = p.numeroFicha;
				outExistente.PaisNombreDivisionPaisPrincipal = p.PaisNombreDivisionPaisPrincipal;
				listSalidaCartera.add(outExistente);
			}
		}
		return listSalidaCartera;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-07-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-07-31");
		Dato dato = new Dato();
		dato.IdCliente = 123569L;
		dato.IdPortafolio = 34991L;
		Objin.p_Datos.add(dato);
		InfoCarteraMaster  demo= new InfoCarteraMaster();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}

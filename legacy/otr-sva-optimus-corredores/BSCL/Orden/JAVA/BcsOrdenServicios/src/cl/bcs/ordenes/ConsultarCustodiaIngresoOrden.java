package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaEntrada;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaSalida;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarCustodiaIngresoOrden implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarCustodiaIngresoOrden.class);
	
	
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarCustodiaIngresoOrden_SelectPotafolioCliente.sql");
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	public static class ServiceEntrada {
		public Long				P_IDEMPRESA;
		public Long				P_IDUSUARIO;
		public String			P_IDMODULO;
		public String			P_FECHASALDO;
		public Long				P_DIASPROYECCION;
		public List<ObjORDENES>	P_ORDENES;
		public String 			P_VALORIZARFLUJO = "S";
		public BarraProgreso	p_BarraProgreso;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}



	
	public static class ServiceSalida {
		public List<ObjSALIDA>			P_OUT_SALIDA;
		public List<ObjERRORESDETALLE>	P_OUT_ERRORESDETALLE;

		public ServiceSalida() {
			P_OUT_SALIDA = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*
	 * 
	 * Clases utilitarias que representan objetos de procesamiento
	 * 
	 */

	public static class ObjORDENES {
		public Long		IdPortafolio;
		public Long		IdInstrumento;
		public String	IdTipoOperacion;
		public String	IdBoveda;
		public String	UsoTipoOperacion;//No usar desde fuera
		public String	CumpleParaVentaDescubierta = "N";   //Si orden cumple requisitos para una posible venta descubierta(S: consulta 2 usos)
		public String 	IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		@Override 
		public boolean equals(Object o) {
			ObjORDENES ord = (ObjORDENES)o;
			return ord.IdBoveda == this.IdBoveda && ord.IdPortafolio == this.IdPortafolio && ord.IdTipoOperacion == this.IdTipoOperacion  && ord.IdInstrumento == this.IdInstrumento;
		}
		
	}

	public static class ObjSALIDA {
		public Long			IdPortafolio;
		public String		IdTipoOperacion;
		public String		FechaSaldo;
		public Long			IdInstrumento;
		public String		IdBoveda;
		public String		IdClasificacionTipoInstrumento;
		public String		EsFungible;
		public String		IdEmisor;
		public Date			FechaVencimiento;
		public String		IdTipoReajuste;
		public BigDecimal	Cantidad = BigDecimal.ZERO;
		public BigDecimal	Precio = BigDecimal.ZERO;
		public BigDecimal	Monto = BigDecimal.ZERO;
		public BigDecimal	ValorRescate = BigDecimal.ZERO;
		public String		UsoTipoOperacion;   // Cuando es venta descubierta devuelve 2 usos    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjERRORESDETALLE {
		public String	IdTipoOperacion;
		public Long		IdPortafolio;
		public String	Codigo;
		public String	Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class Portafolio {
		public Long				IdPortafolio;
		public String			CodigoPortafolio;
		public String			NombreCompleto;
		
		
		public String			DocumentoIdentidad;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ServiceEntrada in = gson.fromJson(jin.toString(), ServiceEntrada.class);
		ServiceSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/*
	 * Logica Servicio
	 * 
	 */
	@Trace
	public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		ServiceSalida out = new ServiceSalida();
		logger.info("Inicio servicio ConsultarCustodiaIngresoOrden");
	
		SQL _sql = new SQL();
		if (in.p_BarraProgreso == null) {
            // No habilitado para servicio principal
            
        } else {
            in.p_BarraProgreso.actualizar(1L, "PROCESANDO", "Inicio consultar custodia orden");
        }
		
		
		List<Long> idsPortafolio = new ArrayList<Long>();		
		for (ObjORDENES ord : in.P_ORDENES)
		{
			if (!idsPortafolio.contains(ord.IdPortafolio))
				idsPortafolio.add(ord.IdPortafolio);
		}
		
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdsPortafolio", idsPortafolio);
		List<Portafolio> lstPortafolio = _sql.query(con, SQL_SELECT_PORTAFOLIO, params, Portafolio.class);
		HashMap<Long, Object> hashPortafolio = new HashMap<Long, Object>();
		hashPortafolio = Utils.List2Hash(lstPortafolio, "IdPortafolio");
		
		// ------------------------------
		// Lee tablas temporales globales
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.P_IDEMPRESA);
		Long idPaisEmpresa = empresa.IdPais;
		
		List<FlujoUso> Uso = new ArrayList<>();
		for (ObjORDENES ord : in.P_ORDENES){
			FlujoUso uso = new FlujoUso();
			switch (ord.IdTipoOperacion) {
			case "VENSIM":
				uso.UsoTipoOperacion = ord.IdTipoOperacion;
				ord.UsoTipoOperacion = ord.IdTipoOperacion;
				uso.IdCliente = null;
				uso.IdPortafolio = ord.IdPortafolio;
				uso.BusquedaEspecifica = "SOLO_SUBYACENTE";
				uso.IdTipoInstrumentoEspecifico = "SIM";
				break;
			case "VENRRV":
				uso.UsoTipoOperacion = "FLJREP";
				ord.UsoTipoOperacion = "FLJREP";
				uso.IdCliente = null;
				uso.IdPortafolio = ord.IdPortafolio;
				uso.BusquedaEspecifica = "SOLO_SUBYACENTE";
				uso.IdTipoInstrumentoEspecifico = "RRV";
				break;
			case "VENRRF":
				uso.UsoTipoOperacion = "FLJREP";
				ord.UsoTipoOperacion = "FLJREP";
				uso.IdCliente = null;
				uso.IdPortafolio = ord.IdPortafolio;
				uso.BusquedaEspecifica = "SOLO_SUBYACENTE";
				uso.IdTipoInstrumentoEspecifico = "RRF";
				break;
			default:
				uso.UsoTipoOperacion = "FLJORD";
				ord.UsoTipoOperacion = "FLJORD";
				uso.IdCliente = null;
				uso.IdPortafolio = ord.IdPortafolio;
				uso.BusquedaEspecifica = "";
				uso.IdTipoInstrumentoEspecifico = null;
				break;
			}
			Uso.add(uso);
			//Si orden cumple requisitos para una posible venta descubierta se consulta además el Flujo VENDES (Venta Descubierta)
			if (ord.CumpleParaVentaDescubierta.equalsIgnoreCase("S")) {
				FlujoUso usoVD = new FlujoUso();					
				usoVD.UsoTipoOperacion = "VENDES";
				usoVD.IdCliente = null;
				usoVD.IdPortafolio = ord.IdPortafolio;
				usoVD.BusquedaEspecifica = "";
				usoVD.IdTipoInstrumentoEspecifico = null;					
				Uso.add(usoVD);
			}
			
			if (ord.IdBoveda.equalsIgnoreCase(null)){
				ord.IdBoveda = "9XA";
			}
		}
		List<FlujoFiltrosBusqueda> filtroBusqueda = new ArrayList<>();
		for (ObjORDENES ord : in.P_ORDENES){
			FlujoFiltrosBusqueda filtro = new FlujoFiltrosBusqueda();
			filtro.IdClasificacionTipoInstrumento = null; // L = 3
			filtro.IdTipoInstrumento = null; // L = 3
			filtro.IdInstrumento = null;
			filtro.IdEmisor = null; // L = 40
			filtro.FechaVencimiento = null;
			filtro.IdTipoReajuste = null; // L = 3
			filtro.EsFungible = null; // L = 1
			filtro.IdBoveda = ord.IdBoveda; // L = 3
			filtroBusqueda.add(filtro);
		}
		List<FlujoEsAgrupadoPor> EsAgrupadoPor = new ArrayList<>();
		FlujoEsAgrupadoPor Agrupa = new FlujoEsAgrupadoPor();
		Agrupa.ClasificacionTipoInstrumento = "S"; // L = 1
		Agrupa.TipoInstrumento = null; // L = 1
		Agrupa.Instrumento = "S"; // L = 1
		Agrupa.Emisor = "S"; // L = 1
		Agrupa.Nemotecnico = null; // L = 1
		Agrupa.NemoSVS = null; // L = 1
		Agrupa.FechaEmisionInstrumento = null; // L = 1
		Agrupa.FechaVencimientoInstrumento = "S"; // L = 1
		Agrupa.FechaLiquidacionInstrumento = null; // L = 1
		Agrupa.TipoReajuste = "S"; // L = 1
		Agrupa.EsFungible = "S"; // L = 1
		Agrupa.CodigoIFRS = null; // L = 1
		Agrupa.Boveda = "S"; // L = 1
		Agrupa.Bolsa = null; // L = 1
		Agrupa.TipoOperacion = null; // L = 1
		Agrupa.Mesa = null; // L = 1
		Agrupa.FolioRegistro = null; // L = 1
		Agrupa.ItemOrigen = null;
		EsAgrupadoPor.add(Agrupa);
		
		FlujoCustodiaEntrada inFlujo = new FlujoCustodiaEntrada();  
		inFlujo.p_IdEmpresa = in.P_IDEMPRESA;
		inFlujo.p_IdUsuario = in.P_IDUSUARIO;
		inFlujo.p_IdModulo = in.P_IDMODULO; //L = 3
		inFlujo.p_FechaSaldo = in.P_FECHASALDO; //L =  10
		inFlujo.p_IdPais = idPaisEmpresa; //-- DEFAULT -1
		inFlujo.p_DiasProyeccion = in.P_DIASPROYECCION.intValue(); //Cantidad de dias a la q se proyecta el saldo consultado -- DEFAULT  0
		inFlujo.p_DiaHabil = "S"; //L = 1 -- DEFAULT 'S'
		inFlujo.p_ValorizarFlujo = in.P_VALORIZARFLUJO; //L = 1 -- DEFAULT 'N'
		inFlujo.p_Uso.addAll(Uso);
		inFlujo.p_FiltrosBusqueda.addAll(filtroBusqueda);
		inFlujo.p_EsAgrupadoPor.addAll(EsAgrupadoPor);
		if(in.p_BarraProgreso != null) {
			in.p_BarraProgreso.actualizar(5L,"PROCESANDO","Preparando consulta custodia");
			BarraProgreso subBarraFlujoCustodia = in.p_BarraProgreso.subrango(5, 90);
			inFlujo.p_BarraProgreso = subBarraFlujoCustodia;
		}		
		ConsultarFlujoCustodia flujoCus = new ConsultarFlujoCustodia();
		FlujoCustodiaSalida flujoCustOut = flujoCus.logica(con, inFlujo);
		
		if(in.p_BarraProgreso != null) {
			in.p_BarraProgreso.actualizar(91L,"PROCESANDO","Agrupando informacion flujo custodia");
			
		}
		for (FlujoCustodia flu: flujoCustOut.p_out_FlujoTotalizado){
			
			Portafolio infoPortafolio = (Portafolio) hashPortafolio.get(flu.IdPortafolio);
			
			
			ObjSALIDA objIF = null;
			ObjSALIDA objNoIF = null;
			if (flu.IdBoveda == null || flu.IdBoveda.equals("")) continue;
			for (ObjORDENES ord: in.P_ORDENES){
				if (ord.IdPortafolio.compareTo(flu.IdPortafolio)==0 && ord.UsoTipoOperacion.equalsIgnoreCase(flu.UsoTipoOperacion)){
					if (!(flu.IdClasificacionTipoInstrumento == null) && (ord.IdClasificacionTipoInstrumento == null || flu.IdClasificacionTipoInstrumento.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento))
							&& (ord.IdInstrumento == null || ord.IdInstrumento.equals(flu.IdInstrumento) || ord.IdTipoOperacion.equals(_c.TIPO_OPERACION_VENSIM))){
						if (!flu.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
							if (objNoIF == null)
								objNoIF = new ObjSALIDA();
								objNoIF.Cantidad  = BigDecimal.ZERO;
								objNoIF.IdPortafolio = ord.IdPortafolio;
								objNoIF.IdTipoOperacion = ord.IdTipoOperacion;
								objNoIF.FechaSaldo = Fecha.toFormat(flu.FechaSaldo, "yyyy-MM-dd");
								objNoIF.IdInstrumento = flu.IdInstrumento;
								objNoIF.IdClasificacionTipoInstrumento = flu.IdClasificacionTipoInstrumento;
								objNoIF.EsFungible = "N";
								objNoIF.IdEmisor = "";
								objNoIF.FechaVencimiento = Fecha.toDate("0001-01-01");
								objNoIF.IdTipoReajuste = "";
								BigDecimal cantidad = BigDecimal.ZERO;
								BigDecimal monto = BigDecimal.ZERO;
								if (ord.IdBoveda.equalsIgnoreCase("9XA") || ord.IdBoveda.isEmpty() || ord.IdBoveda.equalsIgnoreCase(flu.IdBoveda)){
									cantidad = flu.Cantidad;
									monto = flu.MontoAMercadoMonedaSistema;
									objNoIF.IdBoveda = ord.IdBoveda.equalsIgnoreCase("9XA")? null:ord.IdBoveda;
								}
								else {
									cantidad = BigDecimal.ZERO;
								}
								objNoIF.Cantidad = objNoIF.Cantidad.add(cantidad);
								objNoIF.Precio = flu.PrecioMercado;
								objNoIF.Monto = objNoIF.Monto.add(monto);
								objNoIF.ValorRescate = BigDecimal.ZERO;
								objNoIF.UsoTipoOperacion = flu.UsoTipoOperacion;
						}else {//es IF
							if (objIF == null)
								objIF = new ObjSALIDA();
							objIF.Cantidad = BigDecimal.ZERO;
							objIF.ValorRescate = BigDecimal.ZERO;
							objIF.IdPortafolio = ord.IdPortafolio;
							objIF.IdTipoOperacion = ord.IdTipoOperacion;
							objIF.FechaSaldo = Fecha.toFormat(flu.FechaSaldo, "yyyy-MM-dd");
							objIF.IdClasificacionTipoInstrumento = flu.IdClasificacionTipoInstrumento;
							objIF.IdInstrumento = flu.IdInstrumento;
							objIF.Precio = flu.PrecioMercado;
							objIF.EsFungible = flu.EsFungible;
							objIF.FechaVencimiento = flu.FechaVencimientoInstrumento;
							objIF.IdEmisor = flu.IdEmisor;
							objIF.IdTipoReajuste = flu.IdTipoReajuste;
							BigDecimal cantidad = BigDecimal.ZERO;
							BigDecimal monto = BigDecimal.ZERO;
							BigDecimal valorRescate = BigDecimal.ZERO;
							
							if (ord.IdBoveda.equalsIgnoreCase("9XA") || ord.IdBoveda.isEmpty() || ord.IdBoveda.equalsIgnoreCase(flu.IdBoveda)){
								cantidad = flu.Cantidad;
								monto = flu.MontoAMercadoMonedaSistema;
								valorRescate = flu.ValorRescate;
								objIF.IdBoveda = ord.IdBoveda.equalsIgnoreCase("9XA")? null:ord.IdBoveda;
							}
							else {
								cantidad = BigDecimal.ZERO;
							}
							objIF.Cantidad = objIF.Cantidad.add(cantidad);
							objIF.Monto = objIF.Monto.add(monto);
							objIF.ValorRescate = objIF.ValorRescate.add(valorRescate);
							objIF.UsoTipoOperacion = flu.UsoTipoOperacion;
						}
					}else if (flu.IdClasificacionTipoInstrumento == null){						
						
						if (objNoIF == null)
							objNoIF = new ObjSALIDA();
							objNoIF.IdPortafolio = ord.IdPortafolio;
							objNoIF.IdTipoOperacion = ord.IdTipoOperacion;
							objNoIF.FechaSaldo = Fecha.toFormat(flu.FechaSaldo, "yyyy-MM-dd");
							objNoIF.IdInstrumento = (flu.IdInstrumento != null) ? flu.IdInstrumento : 0L;
							objNoIF.IdClasificacionTipoInstrumento = flu.IdClasificacionTipoInstrumento != null ? flu.IdClasificacionTipoInstrumento : "";
							objNoIF.EsFungible = "N";
							objNoIF.IdEmisor = "";
							objNoIF.FechaVencimiento = Fecha.toDate("0001-01-01");
							objNoIF.IdTipoReajuste = "";

							
							if (ord.IdBoveda.equalsIgnoreCase("9XA") || ord.IdBoveda.isEmpty() || ord.IdBoveda.equalsIgnoreCase(flu.IdBoveda)){
								objNoIF.IdBoveda = ord.IdBoveda.equalsIgnoreCase("9XA")? null:ord.IdBoveda;
							}
							else {
								objNoIF.IdBoveda = flu.IdBoveda != null ? flu.IdBoveda : null;
							}
							
							objNoIF.Cantidad = objNoIF.Cantidad.add(flu.Cantidad != null ? flu.Cantidad  : BigDecimal.ZERO);
							objNoIF.Precio = flu.PrecioMercado != null ? flu.PrecioMercado  : BigDecimal.ZERO;
							objNoIF.Monto = objNoIF.Monto.add(flu.MontoAMercadoMonedaSistema != null ? flu.MontoAMercadoMonedaSistema  : BigDecimal.ZERO);
									
									
							objNoIF.ValorRescate = BigDecimal.ZERO;
							objNoIF.UsoTipoOperacion = flu.UsoTipoOperacion;
							

						
						
						
					}
				}
			}
			if (objIF != null)
				out.P_OUT_SALIDA.add(objIF);
			if (objNoIF != null)
				out.P_OUT_SALIDA.add(objNoIF);
			//Si orden cumple requisitos de venta descubierta, se devuelve ademÃ¡ el Flujo VENDES (Venta Descubierta)
				
			ObjSALIDA objFlujoVD = null;
			for (ObjORDENES ord: in.P_ORDENES){
				if (ord.CumpleParaVentaDescubierta.equalsIgnoreCase("S"))
				{	
					if (ord.IdPortafolio.compareTo(flu.IdPortafolio)==0 && "VENDES".equalsIgnoreCase(flu.UsoTipoOperacion)){
						objFlujoVD = new ObjSALIDA();
						objFlujoVD.IdPortafolio = ord.IdPortafolio;
						objFlujoVD.IdTipoOperacion = ord.IdTipoOperacion;
						objFlujoVD.FechaSaldo = Fecha.toFormat(flu.FechaSaldo, "yyyy-MM-dd");
						objFlujoVD.IdInstrumento = flu.IdInstrumento;
						objFlujoVD.IdClasificacionTipoInstrumento = flu.IdClasificacionTipoInstrumento;
						objFlujoVD.EsFungible = "N";
						objFlujoVD.IdEmisor = "";
						objFlujoVD.FechaVencimiento = Fecha.toDate("0001-01-01");
						objFlujoVD.IdTipoReajuste = "";
						BigDecimal cantidad = BigDecimal.ZERO;
						BigDecimal monto = BigDecimal.ZERO;
						if (ord.IdBoveda.equalsIgnoreCase("9XA") || ord.IdBoveda.isEmpty() || ord.IdBoveda.equalsIgnoreCase(flu.IdBoveda)){
							cantidad = flu.Cantidad;
							monto = flu.MontoAMercadoMonedaSistema;
							objFlujoVD.IdBoveda = ord.IdBoveda.equalsIgnoreCase("9XA")? null:ord.IdBoveda;
						}
						else {
							cantidad = BigDecimal.ZERO;
						}
						objFlujoVD.Cantidad = objFlujoVD.Cantidad.add(cantidad);
						objFlujoVD.Precio = flu.PrecioMercado;
						objFlujoVD.Monto = objFlujoVD.Monto.add(monto);
						objFlujoVD.ValorRescate = BigDecimal.ZERO;
						objFlujoVD.UsoTipoOperacion = flu.UsoTipoOperacion;
				
						out.P_OUT_SALIDA.add(objFlujoVD);
					}
				}
			}
		}
		if(in.p_BarraProgreso != null) {
			in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Fin consulta custodia");
			
		}
		
		logger.info("Termino del servicio ConsultarCustodiaIngresoOrden, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
}

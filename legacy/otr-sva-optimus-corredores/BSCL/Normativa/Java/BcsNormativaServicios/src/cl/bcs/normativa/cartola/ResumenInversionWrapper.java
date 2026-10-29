package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJADETALLADO;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class ResumenInversionWrapper implements JavaCall, BcsCartola{
	
	public final Log log = new Log("NOR.ResumenInversionBaseWrapper");
	
	public static class EntradaResumenInversionWrapper{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
	    public String p_FechaDesde;
	    public String	p_FechaHasta;
	    public List<Dato> p_Datos;
	    public String p_IdProductoAgrupador;
	    public String p_IdTipoCanal;
	    public String p_ObtenerPactos;
	    
	    public EntradaResumenInversionWrapper() {
	    	this.p_Datos = new ArrayList<Dato>();
	    	this.p_ObtenerPactos = "N";
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	
	private class SalidaBalanceGeneral extends BcsPojo {
		private Integer Orden;
		private Boolean AgruparMercado;
		private String Grupo;
		private String SubGrupo;
		private String Descripcion;
		private Integer Nivel;
		private String Item;
		private String IdMoneda;
		private BigDecimal MontoMonedaSistema;
		
	    private SalidaBalanceGeneral() {
			Nivel = 1;
			Orden = 2;				// orden por defecto
			AgruparMercado = false;	// agrupa y totaliza mercado en js
			IdMoneda = "CLP";
			MontoMonedaSistema = BigDecimal.ZERO;
		}

	    private Integer getOrden() {
			return Orden;
		}
	    
	    private String getDescripcion() {
			return Descripcion;
		}		    
	    
	    private Integer getNivel() {
			return Nivel;
		}	  
	    
	    private String getItem() {
			return Item;
		}
	    
		public Boolean getAgruparMercado() {
			return AgruparMercado;
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaResumenInversionWrapper jin = gson.fromJson(in.toString(), EntradaResumenInversionWrapper.class);
		SalidaCartola out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCartola logica(Connection con, EntradaResumenInversionWrapper entrada) throws Exception {
		log.write("INICIO::ResumenInversionWrapper");
		EntradaCartola entradaCartola = new EntradaCartola();
		entradaCartola.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCartola.p_IdUsuario = entrada.p_IdUsuario;
		entradaCartola.p_IdModulo = entrada.p_IdModulo;
		entradaCartola.p_IdTipoCanal =entrada.p_IdTipoCanal;
		entradaCartola.p_FechaDesde = Fecha.toDate(entrada.p_FechaDesde);
		entradaCartola.p_FechaHasta = Fecha.toDate(entrada.p_FechaHasta);
		entradaCartola.p_Datos = entrada.p_Datos;
		ResumenInversion resumen = new ResumenInversion();
		resumen.obtenerPactos = "S".equals(entrada.p_ObtenerPactos);
		SalidaCartola out = resumen.procesar(con, entradaCartola);

		// salida de Balance General para Resumen de Cliente
		@SuppressWarnings("unchecked") 
		List<HashMap<String,Object>> salidaResumenInversion = (List<HashMap<String, Object>>) out.P_OUT_SALIDA.get("resumenInversion");		
		
		List<SalidaBalanceGeneral> salidaBalanceGeneral = procesarBalanceGeneral(con, entrada, salidaResumenInversion);
		out.P_OUT_SALIDA.put("resumenBalanceGeneral", salidaBalanceGeneral);
		
		log.write("FIN::ResumenInversionWrapper");
		return out;
	}
	
	private List<SalidaBalanceGeneral> procesarBalanceGeneral (Connection con, EntradaResumenInversionWrapper entrada, List<HashMap<String,Object>> resumenInversion) throws Exception {
		// procedimiento para calcular la seccion Balance General en Resumen cliente de Optimus Cliente

		log.write("INICIO::ResumenInversionWrapper::ResumenBalanceGeneral");
		List<SalidaBalanceGeneral> listaBalanceGeneral = new ArrayList<SalidaBalanceGeneral>();
		
		List<SalidaBalanceGeneral> listaResumenInversion = new ArrayList<SalidaBalanceGeneral>();
		for (HashMap<String,Object> resumenItem : resumenInversion) {
			SalidaBalanceGeneral intemResumenInversior = new SalidaBalanceGeneral();
			for (String key : resumenItem.keySet()) {
				intemResumenInversior.Orden = 3;
				intemResumenInversior.AgruparMercado = true;
				switch (key) {
					case "Item":	// flag para identificar en el js si es activo o pasivo
						intemResumenInversior.Item = resumenItem.get(key).toString();
						break;
					case "Grupo":	// totaliza montos por descripcion en el js
						intemResumenInversior.Grupo = resumenItem.get(key).toString();
						intemResumenInversior.SubGrupo = resumenItem.get(key).toString();
						intemResumenInversior.Descripcion = resumenItem.get(key).toString();
						break;
					case "MontoMonedaSistema":
						intemResumenInversior.MontoMonedaSistema = (BigDecimal)(resumenItem.get(key));
						break;
					case "MonedaResumenInversion":	
						intemResumenInversior.IdMoneda = resumenItem.get(key).toString();
						break;						
				}
			}
			listaResumenInversion.add(intemResumenInversior);
		}
		//CGO
		// obtener items de Operaciones por Liquidar
		List<SalidaBalanceGeneral> cajaOpl = new ArrayList<SalidaBalanceGeneral>();
		cajaOpl = listaResumenInversion.stream()
									   .filter(s -> s.Grupo.equals("OPL") && s.IdMoneda.equals("CLP"))
									   .collect(Collectors.toList());

		// obtener Saldo Retenido y Fondos en Canje CLP pertenecientes al grupo de Operaciones a liquidar
		if (!cajaOpl.isEmpty()) {
			for (int i=0; i < cajaOpl.size(); i++){
				SalidaBalanceGeneral itemBalanceGeneral = new SalidaBalanceGeneral();
				String descripcion = cajaOpl.get(i).Item;
				if (descripcion!=null) {
					switch (descripcion.toUpperCase()) {
					case "FONDOS EN CANJE CLP":
						descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_ABONOS_RETIROS_FONDOS_EN_TRANSITO").msg();
						break;
					case "SALDO RETENIDO CLP":
						descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_SALDO_OPERACIONES_POR_LIQUIDAR").msg();
						break;
					}	
				}
				itemBalanceGeneral.Item	= cajaOpl.get(i).Item;
				itemBalanceGeneral.Descripcion	= descripcion;
				itemBalanceGeneral.MontoMonedaSistema= cajaOpl.get(i).MontoMonedaSistema;
				itemBalanceGeneral.AgruparMercado = false;
				itemBalanceGeneral.Grupo = "CAJA_DISPONIBLE";
				itemBalanceGeneral.SubGrupo = cajaOpl.get(i).Item.contains("Saldo Retenido") ? "CAJA_EFECTIVA" : "CAJA_DISPONIBLE";
				itemBalanceGeneral.Nivel = 2;
				listaBalanceGeneral.add(itemBalanceGeneral);
			}
		}
		
		// consultar Flujo de Caja para obtener saldo cuenta inversion para la fechaDesde 
		// y ordenes de compra para el saldo en 2 dias habiles a la fechaHasta
		
		// agregar 2 dias habiles a la fechaHasta para considerar los items de ordenes de compra 
		String fechaHastaFlujo = obtenerDiaSiguienteHabil(con, entrada.p_FechaHasta, 2L, entrada.p_IdEmpresa);
		
		log.write("INICIO::ConsultarFlujoCaja");
		ConsultarFlujoCaja llamadoFlujoCaja = new ConsultarFlujoCaja();
		EntradaConsultarFlujoCaja entradaFlujoCaja = new EntradaConsultarFlujoCaja();
		entradaFlujoCaja.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujoCaja.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujoCaja.p_IdModulo = entrada.p_IdModulo;
		entradaFlujoCaja.p_FechaDesde = entrada.p_FechaDesde;
		entradaFlujoCaja.p_FechaHasta = fechaHastaFlujo;
		
		List<ConsultarFlujoCaja.PortafolioAConsultar> portafoliosConsultar = entrada.p_Datos.parallelStream()
															.map(idPortafolio -> {
																ConsultarFlujoCaja.PortafolioAConsultar portafolio = new ConsultarFlujoCaja.PortafolioAConsultar();
																portafolio.IdMoneda = "CLP";
																portafolio.IdPortafolio = entrada.p_Datos.get(0).IdPortafolio;
																return portafolio;
															 })
															.collect(Collectors.toList());		
		entradaFlujoCaja.p_PortafolioAConsultar = portafoliosConsultar;
		
		SalidaConsultarFlujoCaja salidaFlujoCaja = llamadoFlujoCaja.logica(con, entradaFlujoCaja);
		List<ObjFLUJOCAJADETALLADO> flujoCajaDetallado = salidaFlujoCaja.P_OUT_FLUJOCAJADETALLADO;
		List<ObjFLUJOCAJADETALLADO> saldoCuentaInversion = salidaFlujoCaja.P_OUT_FLUJOCAJADETALLADO;
		
		// obtener saldo cuenta inversion para la fechaDesde de la entrada
		saldoCuentaInversion = flujoCajaDetallado.stream()
											 	 .filter(s -> s.ItemCalculo.equals("SALDO_CUENTA_INVERSION") && 
											 			 	  s.NombreUso.equals("ORDENES_DE_COMPRA") && 
											 			 	  s.FechaSaldo.equals(entrada.p_FechaDesde))
											 	 .collect(Collectors.toList());
		if (!saldoCuentaInversion.isEmpty()) {			
			for (int i=0; i < saldoCuentaInversion.size(); i++){
				SalidaBalanceGeneral itemBalanceGeneral = new SalidaBalanceGeneral();
				itemBalanceGeneral.Item	= Diccionario.getMensaje(con, entrada.p_IdEmpresa, "SALDO_CUENTA_INVERSION").msg();
				itemBalanceGeneral.Descripcion	= itemBalanceGeneral.Item;
				itemBalanceGeneral.MontoMonedaSistema= saldoCuentaInversion.get(i).Monto;
				itemBalanceGeneral.AgruparMercado = false;
				itemBalanceGeneral.Grupo = "CAJA_DISPONIBLE";
				itemBalanceGeneral.SubGrupo = "CAJA_EFECTIVA";
				itemBalanceGeneral.Nivel = 2;
				listaBalanceGeneral.add(itemBalanceGeneral);
			}
		}		

		// obtener saldo de ordenes de compra 2 dias habiles a la fechaHasta
		List<ObjFLUJOCAJADETALLADO> saldoOrdenesCompra = salidaFlujoCaja.P_OUT_FLUJOCAJADETALLADO;
		saldoOrdenesCompra = flujoCajaDetallado.stream()
											   .filter(s -> ((s.ItemCalculo.equals("MOVIMIENTOS_FACTURAR_COMPRAS") && s.NombreUso.equals("ORDENES_DE_COMPRA") && s.FechaSaldo.equals(fechaHastaFlujo)) ||
													   		 (s.ItemCalculo.equals("ORDENES_EN_TRANSITO_COMPRAS") && s.NombreUso.equals("ORDENES_DE_COMPRA") && s.FechaSaldo.equals(fechaHastaFlujo)) ||
													   		 (s.ItemCalculo.equals("ASIGNACIONES_COMPRAS") && s.NombreUso.equals("ORDENES_DE_COMPRA") && s.FechaSaldo.equals(fechaHastaFlujo))))
											   .collect(Collectors.toList());
		if (!saldoOrdenesCompra.isEmpty()) {
			SalidaBalanceGeneral itemBalanceGeneral = new SalidaBalanceGeneral();
			itemBalanceGeneral.Item = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_ORDENES").msg();
			itemBalanceGeneral.Descripcion = itemBalanceGeneral.Item;
			itemBalanceGeneral.AgruparMercado = false;
			itemBalanceGeneral.Grupo = "CAJA_DISPONIBLE";
			itemBalanceGeneral.SubGrupo = "CAJA_DISPONIBLE";
			itemBalanceGeneral.Nivel = 2;
			// calcula MontoMonedaSistema 
			for (int i=0; i < saldoOrdenesCompra.size(); i++){							
				itemBalanceGeneral.MontoMonedaSistema = itemBalanceGeneral.MontoMonedaSistema.add(saldoOrdenesCompra.get(i).Monto);
			}
			listaBalanceGeneral.add(itemBalanceGeneral);
		}
		log.write("FIN::ConsultarFlujoCaja");
		
		// Obtener Caja efectiva
		if (!listaBalanceGeneral.isEmpty()) {
			SalidaBalanceGeneral itemBalanceGeneral = new SalidaBalanceGeneral();
			itemBalanceGeneral.Item	= Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CAJA_EFECTIVA").msg();
			itemBalanceGeneral.Descripcion = itemBalanceGeneral.Item;
			itemBalanceGeneral.AgruparMercado = false;
			itemBalanceGeneral.Grupo = "CAJA_EFECTIVA";
			itemBalanceGeneral.SubGrupo = "CAJA_EFECTIVA";
			itemBalanceGeneral.Orden = 1;	// primero en la lista
			itemBalanceGeneral.Nivel = 1;	
			// calcula MontoMonedaSistema 
			for (int i=0; i < listaBalanceGeneral.size(); i++){
				if (listaBalanceGeneral.get(i).Nivel.equals(2) && (listaBalanceGeneral.get(i).SubGrupo.equals("CAJA_EFECTIVA"))){										
					itemBalanceGeneral.MontoMonedaSistema = itemBalanceGeneral.MontoMonedaSistema.add(listaBalanceGeneral.get(i).MontoMonedaSistema);
				}
			}
			listaBalanceGeneral.add(itemBalanceGeneral);
		}
				
		// obtener Caja disponible compra
		if (!listaBalanceGeneral.isEmpty()) {
			SalidaBalanceGeneral itemBalanceGeneral = new SalidaBalanceGeneral();
			itemBalanceGeneral.Item = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CAJA_DISPONIBLE_PARA_COMPRA").msg();
			itemBalanceGeneral.Descripcion = itemBalanceGeneral.Item;
			itemBalanceGeneral.AgruparMercado = false;
			itemBalanceGeneral.Grupo = "CAJA_DISPONIBLE";
			itemBalanceGeneral.SubGrupo = "CAJA_DISPONIBLE";
			itemBalanceGeneral.Nivel = 1;	
			// calcula MontoMonedaSistema 
			for (int i=0; i < listaBalanceGeneral.size(); i++){
				if (listaBalanceGeneral.get(i).Nivel.equals(2) && (listaBalanceGeneral.get(i).Grupo.equals("CAJA_DISPONIBLE"))){										
					itemBalanceGeneral.MontoMonedaSistema = itemBalanceGeneral.MontoMonedaSistema.add(listaBalanceGeneral.get(i).MontoMonedaSistema);
				}
			}
			listaBalanceGeneral.add(itemBalanceGeneral);
		}
		//obtener ffmm 
		if (!listaBalanceGeneral.isEmpty()) {
			SalidaBalanceGeneral itemBalanceGeneral = new SalidaBalanceGeneral();
			itemBalanceGeneral.Item = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_FONDOS_MUTUOS").msg();
			itemBalanceGeneral.Descripcion = itemBalanceGeneral.Item;
			itemBalanceGeneral.AgruparMercado = false;
			itemBalanceGeneral.Grupo = "FM";
			itemBalanceGeneral.SubGrupo = "FM";
			itemBalanceGeneral.Nivel = 1;	
			// calcula MontoMonedaSistema 
			for (int i=0; i < listaBalanceGeneral.size(); i++){
				if (listaBalanceGeneral.get(i).Nivel.equals(2) && (listaBalanceGeneral.get(i).Grupo.equals("FM"))){										
					itemBalanceGeneral.MontoMonedaSistema = itemBalanceGeneral.MontoMonedaSistema.add(listaBalanceGeneral.get(i).MontoMonedaSistema);
				}
			}
			listaBalanceGeneral.add(itemBalanceGeneral);
		}
		
		
		// agregar listado de resumen de inversion excluyendo caja y operaciones ya implicitos en Caja efectiva y Caja disponible para compra
		List<SalidaBalanceGeneral> listaResumen = listaResumenInversion.stream()
															 		   .filter(s -> (!s.Grupo.equals("CAJA") && !s.Grupo.equals("OPL")) && s.IdMoneda.equals("CLP"))
																	   .collect(Collectors.toList());
																		
		listaBalanceGeneral.addAll(listaResumen);
		
		log.write("FIN::ResumenInversionWrapper::ResumenBalanceGeneral");
		
		// ordenar salida por grupos, nivel e item
		return listaBalanceGeneral.stream()
								  .sorted(Comparator.comparing(SalidaBalanceGeneral::getOrden)
								  .thenComparing(SalidaBalanceGeneral::getNivel)										  
								  .thenComparing(SalidaBalanceGeneral::getDescripcion)
								  .thenComparing(SalidaBalanceGeneral::getItem)
								  .thenComparing(SalidaBalanceGeneral::getAgruparMercado))
								  .collect(Collectors.toList());
	}
	
	private String obtenerDiaSiguienteHabil(Connection con, String fecha, Long dias, Long IdEmpresa) throws Exception {
		//obtener configuraciones
		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, IdEmpresa);
		long idPais = Long.valueOf(configuraciones.get("PLATAFORMA").get("PLA.PaisSistema"));
		List<FechasIn> fechasInList = new ArrayList<ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn>();

		FechasIn fechas = new FechasIn();
		fechas.fechaInicial = fecha;
		fechas.numeroDias = dias;	// cantidad de dias habiles a calcular
		fechas.pais = idPais;
		fechasInList.add(fechas);
		
		ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente diaSgteIn = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		diaSgteIn.p_idPais = idPais;
		diaSgteIn.p_diaHabil = "S";
		diaSgteIn.p_fechas = fechasInList;
		
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente salDiaSgte = diaSgte.logica(con, diaSgteIn);
		return salDiaSgte.p_out_fechas.get(0).fechaCalculada;		
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","c:/optimus-corredores/connections.properties")) {
		EntradaResumenInversionWrapper Objin = new EntradaResumenInversionWrapper();
        Objin.p_IdEmpresa = 1L;
        Objin.p_IdModulo = "SOC";
        Objin.p_IdUsuario = 9815L;
        //Fecha.toDate(d
        Objin.p_FechaDesde = "2026-03-01";
        Objin.p_FechaHasta = "2026-03-23";
        Objin.p_IdProductoAgrupador = null;
        Dato dato = new Dato();
        dato.IdCliente    = 6953L;
        dato.IdPortafolio = 2101L;
        
		Objin.p_Datos.add(dato);
		ResumenInversionWrapper  demo= new ResumenInversionWrapper();				
		try {
			long inicio = System.currentTimeMillis();
			SalidaCartola ObjOut=demo.logica(con, Objin);
			long fin = System.currentTimeMillis();
			System.out.println("TOTAL:resumenInversionWrapper:" + (fin - inicio));
			con.rollback();
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

	@Override
	public SalidaCartola procesar(Connection con, EntradaCartola entrada) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String loadResourceToString(String path) {
		// TODO Auto-generated method stub
		return BcsCartola.super.loadResourceToString(path);
	}
}

package cl.bcs.ordenes;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjERRORESDETALLE;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjORDENES;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ServiceEntrada;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ServiceSalida;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ObjSALIDA;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada_DatosInstrumentos;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarCustodiaSubyacenteSimultanea implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConsultarCustodiaSubyacenteSimultanea.class);
	private final String SQL_SELECT_INSTRUMENTO  	= loadResourceToString("cl/bcs/ordenes/sql/ConsultarCustodiaSubyacenteSimultanea_SelectInstrumento.sql");
	             
	
	// CLASES DE ENTRADA
	// --------------------------------------------------------------------------
	public static class ConsultarCustodiaSubyacenteSimultanea_Entrada {		
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public Long			p_IdPais;		
		public Long			p_IdPortafolio;
		public String		p_IdBolsa;
		public Long			p_plazoDias;
		public String		p_IdBoveda;		
		public BigDecimal	p_Tasa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	
	
	// CLASES DE SALIDA
	// --------------------------------------------------------------------------	
	public static class ConsultarCustodiaSubyacenteSimultanea_Salida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Custodia>  		P_CUSTODIA;
				
		public ConsultarCustodiaSubyacenteSimultanea_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();			
			this.P_CUSTODIA = new ArrayList<Custodia>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	public static class Custodia {
		public Long             IdInterno;
	    public Long				IdInstrumento;
	    public String 			Instrumento;
	    public String 			IdTipoInstrumento;
	    public BigDecimal 		Precio;
	    public BigDecimal 		CantidadDisponible;
	    public BigDecimal 		MontoDisponible;
		public String 			IdMonedaInstrumento;
		public String 			ValorClasificacionRiesgo;
		public BigDecimal		PrecioVaR;
		public String 			IdClasificacionTipoInstrumento;
		public BigDecimal	    PorcentajeVaR;		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarCustodiaSubyacenteSimultanea_Entrada in = gson.fromJson(jin.toString(), ConsultarCustodiaSubyacenteSimultanea_Entrada.class);
		ConsultarCustodiaSubyacenteSimultanea_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	
	// LOGICA
	// --------------------------------------------------------------------------	
	public ConsultarCustodiaSubyacenteSimultanea_Salida logica(Connection conn,  ConsultarCustodiaSubyacenteSimultanea_Entrada in) throws Exception {
		Long init = System.currentTimeMillis();
		Long idInterno = 0L;
		SQL _sql = new SQL();
		System.out.println("Inicio servicio Consultar Custodia Subyacente Simultanea");
		ConsultarCustodiaSubyacenteSimultanea_Salida out = new ConsultarCustodiaSubyacenteSimultanea_Salida();
		logger.info("Inicio servicio Consultar Custodia Subyacente Simultanea");
		
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String listasAyBSimultanea = "N";
		
		try		
		{
			listasAyBSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "ListasAyBSimultanea");
		}
		catch(Exception e)
		{
			listasAyBSimultanea = "N";
		}
		
		
		// Calcula fecha plazo
		//-----------------------------------------------------------
		
		ObtenerDiaSiguiente servicioObtenerDiaSiguiente = new ObtenerDiaSiguiente();
		
		List<FechasIn> lstFechasObtenerDiaSiguiente = new ArrayList<FechasIn>();
		FechasIn objFecha 		= new FechasIn();
		objFecha.fechaInicial 	= fechaSistema;
		objFecha.numeroDias		= in.p_plazoDias;
		objFecha.pais 			= in.p_IdPais;
		lstFechasObtenerDiaSiguiente.add(objFecha);
		
		InObtenerDiaSiguiente entradaObtenerDiaSiguiente  = new InObtenerDiaSiguiente();
		entradaObtenerDiaSiguiente.p_idPais   = in.p_IdPais;
		entradaObtenerDiaSiguiente.p_diaHabil = "S";
		entradaObtenerDiaSiguiente.p_fechas   = lstFechasObtenerDiaSiguiente;
		
		OutObtenerDiaSiguiente salidaObtenerDiaSiguiente   = servicioObtenerDiaSiguiente.logica(conn, entradaObtenerDiaSiguiente);		
		String fechaCalculada = (salidaObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada); 
		
		
		
		// Consulta custodia para venta de simultaneas (ojo es el FLUJO VENSIM no disponible)
		//-----------------------------------------------------------
		
		List<ObjORDENES> lstOrdenes = new ArrayList<ObjORDENES>();		
		ObjORDENES objOrdenes = new ObjORDENES();		
		objOrdenes.IdPortafolio 				= in.p_IdPortafolio;
		objOrdenes.IdTipoOperacion 				= "VENSIM";
		objOrdenes.IdBoveda 					= in.p_IdBoveda;
		objOrdenes.CumpleParaVentaDescubierta 	= "N";		
		lstOrdenes.add(objOrdenes);
		
		
		
		
		ConsultarCustodiaIngresoOrden servicioConsultarCustodiaIngresoOrden = new ConsultarCustodiaIngresoOrden();
		ServiceEntrada entradaConsultarCustodiaIngresoOrden = new ServiceEntrada();				
		entradaConsultarCustodiaIngresoOrden.P_IDEMPRESA 		= in.p_IdEmpresa;
		entradaConsultarCustodiaIngresoOrden.P_IDUSUARIO 		= in.p_IdUsuario;
		entradaConsultarCustodiaIngresoOrden.P_IDMODULO 		= in.p_IdModulo;
		entradaConsultarCustodiaIngresoOrden.P_FECHASALDO 		= fechaCalculada;
		entradaConsultarCustodiaIngresoOrden.P_DIASPROYECCION 	= 0L;
		entradaConsultarCustodiaIngresoOrden.P_ORDENES 			= lstOrdenes;
		entradaConsultarCustodiaIngresoOrden.P_VALORIZARFLUJO 	= "S";
		
		ServiceSalida salidaConsultarCustodiaIngresoOrden = new ServiceSalida();
		salidaConsultarCustodiaIngresoOrden = servicioConsultarCustodiaIngresoOrden.logica(conn, entradaConsultarCustodiaIngresoOrden);
		
		
		//Retornar posibles errores de consulta custodia
		List<ObjERRORESDETALLE> erroresSaldoCustodia = salidaConsultarCustodiaIngresoOrden.P_OUT_ERRORESDETALLE;
		if (erroresSaldoCustodia.size()>0)
		{	
			for (ObjERRORESDETALLE err : erroresSaldoCustodia) 
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,err.Codigo,err.Descripcion));
			}
			return out;
		}
		
		salidaConsultarCustodiaIngresoOrden.P_OUT_SALIDA = salidaConsultarCustodiaIngresoOrden.P_OUT_SALIDA.parallelStream().filter(custodia->custodia.IdInstrumento != null && custodia.IdInstrumento > 0).collect(Collectors.toList());
		
		if (salidaConsultarCustodiaIngresoOrden.P_OUT_SALIDA.size() == 0)
		{	
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SIN_CUSTODIA_DISPONIBLE",null,conn,in.p_IdEmpresa));			
			return out;
		}
		

		// Busca datos extras del instrumento
		//-----------------------------------------------
		
		List<Long> lstIdInstrumentos = new ArrayList<Long>();
				
		for(ObjSALIDA salidaCustodia: salidaConsultarCustodiaIngresoOrden.P_OUT_SALIDA)
		{
			lstIdInstrumentos.add(salidaCustodia.IdInstrumento);
		}
		

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdBolsa", in.p_IdBolsa);
		params.put("IdInstrumentos", lstIdInstrumentos);
		
		List<Custodia> lstDatosInstrumento = _sql.query(conn, SQL_SELECT_INSTRUMENTO, params, Custodia.class);
		HashMap<String, Object> hashDatosInstrumento = Utils.List2Hash(lstDatosInstrumento, "IdInstrumento");
		
		//OBTENCION DE PRECIOS
		EntregarPreciosTasasUI consultarPrecioVector = new EntregarPreciosTasasUI();
		EntregarPreciosTasasUI_Entrada entrada_consultarPrecioVector = new EntregarPreciosTasasUI_Entrada();

		entrada_consultarPrecioVector.p_IdEmpresa =  in.p_IdEmpresa;
		entrada_consultarPrecioVector.p_IdModulo = "CUS";
		entrada_consultarPrecioVector.p_IdPais = in.p_IdPais;
		entrada_consultarPrecioVector.p_IdUsuario = in.p_IdUsuario;
		
				
		for(Custodia precio : lstDatosInstrumento){
			EntregarPreciosTasasUI_Entrada_DatosInstrumentos entradaPrecioOrden = new EntregarPreciosTasasUI_Entrada_DatosInstrumentos();
			entradaPrecioOrden.FechaConsulta = fechaSistema;
			entradaPrecioOrden.IdRegistro = 0L;
			entradaPrecioOrden.TipoCartera = "0";
			entradaPrecioOrden.IdUso = "ORDEN";
			entradaPrecioOrden.IdTipoCriterioTasa = "0";
			entradaPrecioOrden.IdClasificacionTipoInstrumento = precio.IdClasificacionTipoInstrumento;
			entradaPrecioOrden.IdTipoInstrumento = precio.IdTipoInstrumento;
			entradaPrecioOrden.Nemotecnico = precio.Instrumento;
			entradaPrecioOrden.IdInstrumento = precio.IdInstrumento;
			entradaPrecioOrden.IdMoneda = precio.IdMonedaInstrumento;
			entradaPrecioOrden.FechaConsulta = fechaSistema;
			entradaPrecioOrden.PlazoResidual = 0L;
			entradaPrecioOrden.IdTrabajo = 0L;	
			entrada_consultarPrecioVector.p_DatosInstrumentos.add(entradaPrecioOrden);
			
			EntregarPreciosTasasUI_Entrada_DatosInstrumentos entradaPrecioGarantia = new EntregarPreciosTasasUI_Entrada_DatosInstrumentos();
			entradaPrecioGarantia.FechaConsulta = fechaSistema;
			entradaPrecioGarantia.IdRegistro = 1L;
			entradaPrecioGarantia.TipoCartera = "0";
			entradaPrecioGarantia.IdUso = "GARANTIA";
			entradaPrecioGarantia.IdTipoCriterioTasa = "0";
			entradaPrecioGarantia.IdClasificacionTipoInstrumento = precio.IdClasificacionTipoInstrumento;
			entradaPrecioGarantia.IdTipoInstrumento = precio.IdTipoInstrumento;
			entradaPrecioGarantia.Nemotecnico = precio.Instrumento;
			entradaPrecioGarantia.IdInstrumento = precio.IdInstrumento;
			entradaPrecioGarantia.IdMoneda = precio.IdMonedaInstrumento;
			entradaPrecioGarantia.FechaConsulta = fechaSistema;
			entradaPrecioGarantia.PlazoResidual = 0L;
			entradaPrecioGarantia.IdTrabajo = 1L;
			entrada_consultarPrecioVector.p_DatosInstrumentos.add(entradaPrecioGarantia);
		}	

		EntregarPreciosTasas_Salida salidaPrecio = consultarPrecioVector.logica(conn, entrada_consultarPrecioVector);		
		HashMap<String, Object> hashDatosPrecios = new HashMap<String, Object>();
		HashMap<String, Object> hashDatosPreciosVar = new HashMap<String, Object>();
		
		 for (EntregarPreciosTasas_Salida_PreciosTasas sal: salidaPrecio.P_OUT_PRECIOSTASAS) {
			 if (sal.IdRegistro == 0L) {
				 hashDatosPrecios.put(sal.IdInstrumento.toString(), sal);
			 }
			 if (sal.IdRegistro == 1L) {
				 hashDatosPreciosVar.put(sal.IdInstrumento.toString(), sal);
			 }
		 }
		// PROCESAR SALIDA FINAL
		//-----------------------------------------------------
		
			BigDecimal bg1 = new BigDecimal(1);
			BigDecimal bg2 = new BigDecimal(2);
			BigDecimal bg30 = new BigDecimal(30);
			BigDecimal bg100 = new BigDecimal(100);
		 
		for(ObjSALIDA salidaCustodia: salidaConsultarCustodiaIngresoOrden.P_OUT_SALIDA)
		{	
			Custodia custodiaSimultanea = new Custodia();
			Custodia infoInstrumento  = (Custodia) hashDatosInstrumento.get(salidaCustodia.IdInstrumento);			
			EntregarPreciosTasas_Salida_PreciosTasas infoPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) hashDatosPrecios.get(salidaCustodia.IdInstrumento.toString());
			EntregarPreciosTasas_Salida_PreciosTasas infoPrecioVar = (EntregarPreciosTasas_Salida_PreciosTasas) hashDatosPreciosVar.get(salidaCustodia.IdInstrumento.toString());
			
			custodiaSimultanea.IdInterno            = idInterno;
			custodiaSimultanea.IdInstrumento 		=  salidaCustodia.IdInstrumento;
			custodiaSimultanea.Instrumento 			= infoInstrumento.Instrumento;
			custodiaSimultanea.IdTipoInstrumento 	= infoInstrumento.IdTipoInstrumento;
			custodiaSimultanea.Precio 				= infoPrecio.Precio;
			custodiaSimultanea.CantidadDisponible 	= salidaCustodia.Cantidad;
			custodiaSimultanea.MontoDisponible 		= salidaCustodia.Monto;
			custodiaSimultanea.IdMonedaInstrumento 	= infoInstrumento.IdMonedaInstrumento;
			custodiaSimultanea.ValorClasificacionRiesgo = infoInstrumento.ValorClasificacionRiesgo;
			custodiaSimultanea.PrecioVaR = infoPrecioVar.Precio;			
			
			BigDecimal valor1 = new BigDecimal(0);
			BigDecimal valor2 = new BigDecimal(0);			
			
			valor1 = bg2.subtract((infoPrecioVar.Precio.divide(infoPrecio.Precio,20,RoundingMode.HALF_UP)));
			valor2 = bg1.divide(bg1.add(in.p_Tasa.divide(bg100).multiply(BigDecimal.valueOf(in.p_plazoDias).divide(bg30,20,RoundingMode.HALF_UP))),20,RoundingMode.HALF_UP);
			custodiaSimultanea.PorcentajeVaR = valor1.subtract(valor2).multiply(bg100);

			if (listasAyBSimultanea.equalsIgnoreCase("S")){
				if (custodiaSimultanea.ValorClasificacionRiesgo.trim().equalsIgnoreCase("A") || custodiaSimultanea.ValorClasificacionRiesgo.trim().equalsIgnoreCase("B")){
					out.P_CUSTODIA.add(custodiaSimultanea);
					idInterno++;
				}
			}
			else{
				out.P_CUSTODIA.add(custodiaSimultanea);
				idInterno++;
			}
			
		}


		logger.info("Termino del servicio Consultar Custodia Subyacente Simultanea, tiempo:" + (System.currentTimeMillis() - init));
		return out;		
	}

	
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------------------------------
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBIConnection()) {
			try {
					Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

					String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_IdBolsa\":\"BCS\",\"p_IdPortafolio\":1451800,\"p_plazoDias\":7,\"p_IdBoveda\":\"DCV\",\"p_IdPais\":44,\"p_Tasa\":5}";

					ConsultarCustodiaSubyacenteSimultanea_Entrada Objin = gson.fromJson(json, ConsultarCustodiaSubyacenteSimultanea_Entrada.class);				
					ConsultarCustodiaSubyacenteSimultanea demo = new ConsultarCustodiaSubyacenteSimultanea();				
		 			System.out.println("ENTRADA");
		 			System.out.println(Objin.toString());
		 			
		 			ConsultarCustodiaSubyacenteSimultanea_Salida ObjOut = demo.logica(con, Objin);	 			
			    	System.out.println(ObjOut.toString());	
			    	con.commit();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}	
}

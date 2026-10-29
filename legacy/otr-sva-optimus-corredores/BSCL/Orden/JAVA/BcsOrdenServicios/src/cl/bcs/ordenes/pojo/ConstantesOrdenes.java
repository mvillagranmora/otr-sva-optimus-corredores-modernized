package cl.bcs.ordenes.pojo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantesOrdenes {
	
	public final String CODIGO_COBRO_IVA = "IVA";
	public final String CODIGO_COBRO_ALL_INCLUSIVE = "COAI";
	public final String TIPO_EVENTO_BITACORA_ORDEN_RECHAZADA = "ORDEN_RECHAZADA";
	public final String TIPO_EVENTO_BITACORA_EN_NEGOCIACION = "EN_NEGOCIACION";
	public final String TIPO_EVENTO_BITACORA_ROUTED_TO_XBCL = "ORDEN_EN_BEC";
	public final String TIPO_EVENTO_BITACORA_ENVIADA_NEGOCIACION = "ENVIADA_NEGOCIACION";
	public final String TIPO_EVENTO_BITACORA_EJECUTADA_PARCIALMENTE = "EJECUTADA_PARCIALMENTE";
	public final String TIPO_EVENTO_BITACORA_EJECUTADA_COMPLETAMENTE = "EJECUTADA_COMPLETAMENTE";
	public final String TIPO_EVENTO_BITACORA_ENVIADA_ANULAR = "ENVIADA_ANULAR";
	public final String TIPO_EVENTO_BITACORA_ORDEN_ANULADA = "ORDEN_ANULADA";
	public final String TIPO_EVENTO_BITACORA_ORDEN_ANULADA_PARCIALMENTE = "ORDEN_ANULADA_PARCIALMENTE";
	
	public final String ESTADO_FIX_NEWS = "-1";
	public final String ESTADO_FIX_CALCE_PARCIAL = "1";
	public final String ESTADO_FIX_CALCE_COMPLETO = "2";
	public final String ESTADO_FIX_ORDEN_ANULADA = "4";
	public final String ESTADO_FIX_ORDEN_RECHAZADA = "8";
	public final String ESTADO_FIX_EN_NEGOCIACION = "0";
	public final String ESTADO_FIX_ROUTED_XBCL = "A";
	public final String ESTADO_FIX_CANCELADA = "C";
	
	public final String OE = "OE";
	public final String OD = "OD";
	public final String TIPO_MENSAJE_BEC_PALO_SIM = "BEC_PALO_SIM";
	public final String TIPO_MENSAJE_BEC_PALO_IF = "BEC_PALO_IF";
	public final String TIPO_MENSAJE_BEC_PALO_RF = "BEC_PALO_RF";
	public final String TIPO_MENSAJE_BEC_PALO_RV = "BEC_PALO_RV";
	public final List<String> nemosBolson = Arrays.asList(new String[]{"XERO","CERO","ZERO"});
	public final List<String> cuponesEmision = Arrays.asList(new String[]{"XERO","CERO","ZERO"});
	public final List<String> bonoReconocimiento = Arrays.asList(new String[]{"BR","AE","A3","B1","B2","B3","BA","BC","BD","BE","BL","BX","CB3","CBR","D3","DE","E1","E2","E3","F3"});
	
	public final Map<String, String> codigosAgnoEmisionBonoReconocimiento = getCodigoAgnoEmisionBonoReconocimiento();
    
    public final Map<String, String> codigosMesEmisionBonoReconocimiento = getCodigoMesEmisionBonoReconocimiento();
	
	public final String TIPO_EVENTO_AUTORIZACION_INGRESAR_ASIGNACION = "LIMASG";
    	
	public final String ESTADO_ASIGNACION_PENDIENTE_AUTORIZACION = "PDA";
	public final String ESTADO_ASIGNACION_ANULADA = "ANU";
	public final String TIPO_CANAL_INGRESO_AGENTE = "AGE";
	public final String TIPO_CANAL_INGRESO_TRADER = "TRA";
	public final String TIPO_CANAL_INGRESO_OWP = "OWP";
	public final String TIPO_CANAL_INGRESO_CLI = "CLI";
	public final String TIPO_CANAL_INGRESO_WS = "WBS";
	public final String TIPO_CANAL_INGRESO_APP = "APP";
	
	public final String CLASIFICACION_IF = "IF";
	public final String CLASIFICACION_RF = "RF";
	public final String CLASIFICACION_RV = "RV";
	public final String CLASIFICACION_DV = "DV";
	public final String CLASIFICACION_RE = "RE";
	
	public final String SI = "S";
	public final String NO = "N";
	
	public final String ESTADO_ORDEN_INGRESADA = "ING";
	public final String ESTADO_ORDEN_PENDIENTE_AUTORIZAR = "PDA";
	public final String ESTADO_ORDEN_OFERTADA = "OFE";
	public final String ESTADO_ORDEN_ANULADA = "ANU";
	public final String ESTADO_ORDEN_RECHAZADA = "RZD";
	public final String ESTADO_ORDEN_ANULADA_PARCIAL = "ANP";
	public final String ESTADO_ORDEN_NO_AUTORIZADA = "NAU";
	public final String ESTADO_ORDEN_EN_RUTEO = "ENR";
	public final String ESTADO_ORDEN_AUTORIZADA = "AUT";
	public final String ESTADO_ORDEN_VENCIDA = "VCD";
	
	public final String ESTADO_CONFIRMACION_ORDEN_EN_PLAZO = "CEP";
	public final String ESTADO_CONFIRMACION_ORDEN_FUERA_PLAZO = "CFP";
	public final String ESTADO_CONFIRMACION_ORDEN_NO = "NCF";
	
	public final String ID_MODULO_ORDENES = "ORD";
	
	public final String TIPO_CANAL_DISTRIBUCION_RUTEO_RETAIL = "RUR";
	public final String TIPO_CANAL_DISTRIBUCION_RUTEO_AGENTE = "RUA";
	
	public final String ESTADO_ASIGNACION_NO_ENVIADA = "NEF";
	
	public final String ESTADO_MODULO_CERRADO = "C";
	
	public final String TIPO_PRECIO_LIMITE = "L";
	public final String TIPO_PRECIO_MERCADO = "M";
	
	public final String ACCION_COMPRA = "COMPRA";
	public final String ACCION_VENTA = "VENTA";
	
	public final String TIPO_INSTRUMENTO_SIM = "SIM";
	public final String TIPO_INSTRUMENTO_RRF = "RRF";
	public final String TIPO_INSTRUMENTO_RRV = "RRV";
	
	public final String TIPO_PRODUCTO_RERF = "RERF";
	public final String TIPO_PRODUCTO_RERV = "RERV";
	
	
	public final String ESTADO_TRANSACCION_INGRESADA = "ING";
	public final String ESTADO_TRANSACCION_EN_PROCESO = "EPR";
	public final String ID_CUALQUIER_BOLSA = "0";
	
	public final String ORIGEN_REFACTURACION = "REFACTURACION";
	public final String ORIGEN_REASIGNACION = "REASIGNACION";
	public final String TIPO_VIGENCIA_SOLO_HOY = "SH";
	public final String TIPO_VIGENCIA_SEMANAL = "SE";
	public final String TIPO_VIGENCIA_FECHA_FIJA = "FF";
	public final String TIPO_VIGENCIA_PERMANENTE = "OP";
	
	public final String TIPO_CANAL_DISTRIBUCION_MANUAL = "MAN";
	
	public final String TIPO_OPERACION_VENFUT = "VENFUT";
	public final String TIPO_OPERACION_COMFUT = "COMFUT";
	
	public final String FACTURA_ELECTRONICA = "FAE";
	public final String POLIZA = "POL";
	
	public final String MEDIO_PAGO_CUENTA_INVERSION = "CIN";
	public final String ESTADO_ASIGNACION_ENVIADA_FACTURAR = "EAF";
	public final String INTERES_SIMPLE = "S";
	public final String INTERES_COMPUESTO = "C";
	
	//OPERACION SPOT
	public final String Dolar = "USD";
	public final String ModuloSpot = "SPT";
	public final String TipoCanalIngreso = "MEB";
	public final String CanalPunta = "Punta";
	public final String SpotCompra = "COMSPT";
	public final String SpotVenta = "VENSPT";
	public final String SpotProducto = "MEME";
	public final String SpotTipoComprobanteFacturacion ="FEE";
	public final String SpotBenOur="BEN";
	public final String SpotEstadoVigente = "VIG";
	public final String SpotEstadoRechazado = "RECH";
	public final String SpotCalculaMargen="S";
	public final String SpotError="ERROR";
	public final String SpotTipoInstrumento="SPT";
	public final String SpotNemo="DISTRI";
	public final Long SpotCodigoComercio=(long)300206;
	public final String SpotTipoMovimientoIngreso="MOVING";
	public final String SpotTipoMovimientoEgreso="MOVEGR";
	public final String SpotformaDePagoEntregamosVenta_0="VENSPT_CIN_USD_0";
	public final String SpotformaDePagoRecibimosVenta_0="COMSPT_CIN_USD_0";
	public final String SpotformaDePagoEntregamosCompra_0="VENSPT_CIN_CLP_0";
	public final String SpotformaDePagoRecibimosCompra_0="COMSPT_CIN_CLP_0";
	public final String SpotformaDePagoEntregamosVenta_1="VENSPT_CIN_USD_1";
	public final String SpotformaDePagoRecibimosVenta_1="COMSPT_CIN_USD_1";
	public final String SpotformaDePagoEntregamosCompra_1="VENSPT_CIN_CLP_1";
	public final String SpotformaDePagoRecibimosCompra_1="COMSPT_CIN_CLP_1";
	public final String SpotformaDePagoEntregamosVenta_2="VENSPT_CIN_USD_2";
	public final String SpotformaDePagoRecibimosVenta_2="COMSPT_CIN_USD_2";
	public final String SpotformaDePagoEntregamosCompra_2="VENSPT_CIN_CLP_2";
	public final String SpotformaDePagoRecibimosCompra_2="COMSPT_CIN_CLP_2";
	
	public final String SpotValutaDefectoFormaDePago="0";
	
	//OPERACIONES DE REPORTE
	public final String TIPO_OPERACION_COMRRV = "COMRRV";
	public final String TIPO_OPERACION_COMRRF = "COMRRF";
	public final String TIPO_OPERACION_VENRRF = "VENRRF";
	public final String TIPO_OPERACION_VENRRV = "VENRRV";
	
	//OPERACIONES DE SIMULTANEA
	public final String TIPO_OPERACION_COMSIM = "COMSIM";
	public final String TIPO_OPERACION_VENSIM = "VENSIM";
	
	public final String TIPO_OPERACION_VENTA_CORTA_RF	= "VCRF";

	public final String ESTADO_LIBRO_CERRADO = "CER";
	
	public final String MECANISMO_CAPTACION_OTROS_MEDIOS = "OM";
	public final String MECANISMO_CAPTACION_ORDEN_ESCRITA = "OE";
	public final String MECANISMO_CAPTACION_ORDEN_VERBAL = "OV";
	
	public final String GLOSA_CARGA_MASIVA = "Orden por Carga Masiva";
	public final String GLOSA_ORDEN_POSTRERA = "Orden Postrera";
	
	//TODO CAMBIAR POR TABLAS
	public final List<String> tipoLibroOfertasPublicasPeruRV = Arrays.asList(new String[]{"OPARV","OPIRV","OPCRV","OPVRV"});
	public final List<String> tipoLibroOfertasPublicasPeruRF = Arrays.asList(new String[]{"OPIRF","OPVRF"});
	public final List<String> tipoLibroColocaciones = Arrays.asList(new String[]{"COLORV","COLORF"});
	
	private Map<String, String> getCodigoAgnoEmisionBonoReconocimiento()
    {
        Map<String,String> myMap = new HashMap<String,String>();
        myMap.put("1", "1981");
        myMap.put("2", "1982");
        myMap.put("3", "1983");
        myMap.put("4", "1984");
        myMap.put("5", "1985");
        myMap.put("6", "1986");
        myMap.put("7", "1987");
        myMap.put("8", "1988");
        myMap.put("9", "1989");
        myMap.put("0", "1990");
        myMap.put("A", "1991");
        myMap.put("B", "1992");
        myMap.put("C", "1993");
        myMap.put("D", "1994");
        myMap.put("E", "1995");
        myMap.put("F", "1996");
        myMap.put("G", "1997");
        myMap.put("H", "1998");
        myMap.put("I", "1999");
        myMap.put("J", "2000");
        
        return myMap;
    }
	
	private Map<String, String> getCodigoMesEmisionBonoReconocimiento()
    {
        Map<String,String> myMap = new HashMap<String,String>();
        myMap.put("1", "01");
        myMap.put("2", "02");
        myMap.put("3", "03");
        myMap.put("4", "04");
        myMap.put("5", "05");
        myMap.put("6", "06");
        myMap.put("7", "07");
        myMap.put("8", "08");
        myMap.put("9", "09");
        myMap.put("0", "10");
        myMap.put("A", "11");
        myMap.put("B", "12");
        return myMap;
    }
	
	
	public String[] getModalidadMercadoSMV(String idBolsa, String idClasificacion,String idTipoInstrumento,String idSistemaBursatil,String idTipoLibroOrdenes) {
		
		String modalidadSMV = "";
		String mercadoSMV = "";
		
		//RV BVL
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("BVL") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "21";
			mercadoSMV = "1";
		}
		//RV FRD
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("FRD") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "21";
			mercadoSMV = "2";
		}
		//RV OUT
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("OUT") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "21";
			mercadoSMV = "3";
		}
		//RV MILA
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("BCS") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "21";
			mercadoSMV = "3";
		}
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("BVC") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "21";
			mercadoSMV = "3";
		}
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("BMV") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "21";
			mercadoSMV = "3";
		}
		//RV BVL COLOCACION
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("BVL") && tipoLibroColocaciones.contains(idTipoLibroOrdenes)) {
			modalidadSMV = "21";
			mercadoSMV = "6";
		}
		//RV FRD COLOCACION
		if (idClasificacion.equalsIgnoreCase("RV") && idBolsa.equalsIgnoreCase("FRD") && tipoLibroColocaciones.contains(idTipoLibroOrdenes)) {
			modalidadSMV = "21";
			mercadoSMV = "7";
		}//RV BVL OFERTAS PUBLICAS
		if (idBolsa.equalsIgnoreCase("BVL") && idClasificacion.equalsIgnoreCase("RV") && tipoLibroOfertasPublicasPeruRV.contains(idTipoLibroOrdenes)) {
			modalidadSMV = "31";
			mercadoSMV = "1";
		}
		//RF BVL COLOCACION
		if (idClasificacion.equalsIgnoreCase("RF") && idBolsa.equalsIgnoreCase("BVL") && tipoLibroColocaciones.contains(idTipoLibroOrdenes)) {
			modalidadSMV = "22";
			mercadoSMV = "6";
		}
		//RV FRD COLOCACION
		if (idClasificacion.equalsIgnoreCase("RF") && idBolsa.equalsIgnoreCase("FRD") && tipoLibroColocaciones.contains(idTipoLibroOrdenes)) {
			modalidadSMV = "22";
			mercadoSMV = "7";
		}
		//RF FRD
		if (idClasificacion.equalsIgnoreCase("RF") && idBolsa.equalsIgnoreCase("FRD") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "22";
			mercadoSMV = "2";
		}
		//RF BVL MERCADO DEL DINERO
		if (idClasificacion.equalsIgnoreCase("RF") && idBolsa.equalsIgnoreCase("BVL") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL") && 
				idSistemaBursatil != null &&  idSistemaBursatil.equalsIgnoreCase("MERCADODELDINERO")) {
			modalidadSMV = "25";
			mercadoSMV = "1";
		}
		//RF BVL NEGOCIACION CONTINUA
		if (idClasificacion.equalsIgnoreCase("RF") && idBolsa.equalsIgnoreCase("BVL") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL") && 
				idSistemaBursatil != null && idSistemaBursatil.equalsIgnoreCase("NEGOCIACIONCONTINUA")) {
			modalidadSMV = "22";
			mercadoSMV = "1";
		}
		//RF OUT
		if (idClasificacion.equalsIgnoreCase("RF") && idBolsa.equalsIgnoreCase("OUT") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "22";
			mercadoSMV = "3";
		}
		//RF BVL OFERTAS PUBLICAS
		if (idClasificacion.equalsIgnoreCase("RF") && idBolsa.equalsIgnoreCase("BVL") && tipoLibroOfertasPublicasPeruRF.contains(idTipoLibroOrdenes)) {
			modalidadSMV = "32";
			mercadoSMV = "1";
		}
		//REPORTES RV
		if (idClasificacion.equalsIgnoreCase("RE") && idTipoInstrumento.equalsIgnoreCase("RRV") && idBolsa.equalsIgnoreCase("BVL") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "41";
			mercadoSMV = "1";
		}
		//REPORTES RF
		if (idClasificacion.equalsIgnoreCase("RE") && idTipoInstrumento.equalsIgnoreCase("RRF") && idBolsa.equalsIgnoreCase("BVL") && idTipoLibroOrdenes.equalsIgnoreCase("NORMAL")) {
			modalidadSMV = "42";
			mercadoSMV = "1";
		}
		//OPCIONES EXTRANJERA
		if (idClasificacion.equalsIgnoreCase("DV") && idBolsa.equalsIgnoreCase("OUT") && idTipoInstrumento.equalsIgnoreCase("OPC")) {
			modalidadSMV = "21";
			mercadoSMV = "3";
		}
		//OPCIONES BVL
		if (idClasificacion.equalsIgnoreCase("DV") && idBolsa.equalsIgnoreCase("BVL") && idTipoInstrumento.equalsIgnoreCase("OPC")) {
			modalidadSMV = "21";
			mercadoSMV = "1";
		}
		
		String resp[] = new String[2];
		resp[0] = modalidadSMV;
		resp[1] = mercadoSMV;
		return resp;
	}
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}

	//OPCIONES
	public final List<String> tipoInstrumentoOpciones = Arrays.asList(new String[]{"IOA","IOE","IOI","IOM","IOB","IOC","IOU","IOL","IOD"});	
}

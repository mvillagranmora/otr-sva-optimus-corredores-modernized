
package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Entrada;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Salida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularPrestamo implements JavaCall {

	private final Log log = new Log("CUS.AnularPrestamo");

	private final String SQL_SELECT_PRESTAMOS				= loadResourceToString("cl/bcs/custodia/sql/prt/AnularPrestamo_SelectPrestamos.sql");
	private final String SQL_SELECT_PRESTAMISTAS			= loadResourceToString("cl/bcs/custodia/sql/prt/AnularPrestamo_SelectPrestamistas.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	private final String SQL_INSERT_LOG_PROCESO_BLOCKCHAIN 		= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_InsertLogProcesoPrestamoBlockChain.sql");

	public static class Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdPais;
		public String	p_FechaAnulacion;
		public Long 	p_FolioPrestamo;
		public String 	p_VieneDesdeBlockChain = "N"; 	// si esto esta en S
														// significa que debe
														// saltarse validar en
														// BlockChain e ir a anular a BlockChain

		
		public String toString() {
			return Utils.printJson(this, false);
		}

		
	}

	public static class Salida {
		public Long 		P_OUT_ERRORESCONTADOR;
		public Long 		P_OUT_ADVERTENCIASCONTADOR;
		public String 		P_OUT_DESCRIPCION;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_DESCRIPCION = "";
			
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	public static class Prestamo {
		public Long 			IdCartera;
		public Long 			FolioPrestamo;
		public java.sql.Date 	FechaPrestamo;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
		public String 			IdTipoProducto;		
		public String 			IdBolsa;
		public String 			IdTipoInstrumento;
		public String 			IdEmisor;         
		public String 			NemoSubyacente;
		public Long 			IdInstrumentoSubyacente;
		public String 			IdTipoInstrumentoSubyacente;
		public String 			IdClasificacionTipoInstrumentoSubyacente;
		public String 			IdTipoReajusteSubyacente;
		public String 			IdEmisorSubyacente;
		public String 			IdMonedaSubyacente;
		public BigDecimal 		INGCantidad;
		public BigDecimal 		INGValorRescate;
		public BigDecimal 		INGMontoInstrumento;
		public Long 			FolioSubyacente;
		public Long 			IdInstrumentoPrestamo;
		public BigDecimal 		INGMontoPrima;
		public BigDecimal 		PrecioInstrumento;
		public java.sql.Date 	FechaVencimientoSubyacente;
		public String 			EsFungibleSubyacente;
		public Long 			IdSubyacente;
		public String 			IdTipoPrestamo;
		public String 			IdModulo;
		public String 			FolioSistemaExterno;
		public String			IdEstado;
		
		public Prestamo() {
			this.IdEstado = "";
		}
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjRegistroPrestamo {
		public Long 			IdCartera;
		public Long				IdOperacion;
		public Long 			FolioCartera;
		public String 			Nemotecnico;
		public String 			EsPagaderoEn;
		public BigDecimal 		INGMontoATasaCompraActual;
		public BigDecimal 		INGMontoCompraActualMonedaInstrumento;
		public BigDecimal 		INGMontoAPlazo;
		public String 			IdClasificacionTipoInstrumento;
		public String 			IdTipoInstrumento;
		public Long 			IdInstrumento;
		public Long 			IdPortafolio;
		public String 			IdMonedaInstrumento;
		public String 			IdTipoProducto;
		public String			IdTipoOperacion;
		public String 			OperaContraCuentaInversion;
		public Long 			IdPersona;
		public Long 			IdCliente;
		public java.sql.Date 	FechaOperacion;
		public String 			IdConcepto;
		public String 			IdTipoCanalIngreso;
		public Long 			IdUsuario;
		public Long 			IdSucursal;
		public Long 			IdAgente;
		public String 			Glosa;
		public String 			IdBolsa;
		public BigDecimal 		Cantidad;
		public BigDecimal 		CorteMinimo;
		public BigDecimal 		MontoCompraMonedaInstrumento;
		public BigDecimal 		PrecioAPlazo;
		public BigDecimal 		MontoAPlazo;
		public java.sql.Date 	FechaComprobante;
		public String 			IdMonedaSistema; // se usa como IdMonedaOperacion
		public String 			IdCondicionLiquidacion; // se esta usando T0
		public BigDecimal 		INGCantidad; // usado como Cantidad?
		public BigDecimal 		INGValorRescate;
		public BigDecimal 		ValorRescate;
		public BigDecimal 		INGTasaCompra; // es la tasa
		public BigDecimal 		PrecioCompra;
		public BigDecimal 		INGPrecioCompra;
		public String 			AccionOperacion; // lo usa la simultanea
		public Long 			IdInstrumentoSubyacente;
		public Long 			FolioSubyacente;
		public Long 			FolioPrestamo; // para que el emprestito lo referencie
		public String 			FechaLiquidacion;
		public Long 			FolioComprobante;
		public String 			IdTipoComprobante;
		public String 			CobraPrimaAlInicio;
		public String 			TablaAfecta;
		public String 			IdTipoComprobanteBusqueda;
		public Long 			FolioComprobanteBusqueda;
		public java.sql.Date 	FechaLiquidacionDate;
		public BigDecimal 		INGMontoPrima;     // Monto prima de Prestamos (se usa para enviar a facturar prestamos) 
		public BigDecimal 		INGMontoPremio;    // Monto premio de Emprestitos (se usa para enviar a facturar emprestitos)                    
		public String 			IdEmisor;
		public String 			TieneCorteDeCupon;
		public String 			IdTipoReajuste;
		public String 			IdMonedaEmision;
		
		public BigDecimal 		CantidadAsignada;
		public BigDecimal		CantidadPorAsignar;
		public String			IdTipoPrestamo;
		public String			PrestamoAsignadoTotal = "S";
		
		public String 			FolioSistemaExterno = ""; //Valor que tiene asociado un prestamo (FolioBlockChain) cuando esta registrado en el sistema externo llamado BlockChain
		public String 			EsAnticipoParcial = "N";
		public java.sql.Date 	FechaVencimientoPrestamo;
		public String 			IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Prestamista {
		public Long 			IdCartera;
		public Long 			FolioCartera;
		public Long 			FolioCarteraRelacionada;
	
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class LogProcesoPrestamoBlockChain extends BcsPojo {
	public Long 	IdEmpresa;
	public Long 	FolioCartera;
	public String 	FechaProceso;
	public String 	Descripcion;
	public String 	OrigenProcesoClase;
	public String 	OrigenProcesoMetodo;
	public String 	CreadorParametro;
	
	public LogProcesoPrestamoBlockChain(Long idEmpresa, Long folioCartera, String fechaProceso, String descripcion, String origenProcesoClase, String origenProcesoMetodo, String creadoPor) {
		this.IdEmpresa 				= idEmpresa;
		this.FolioCartera 			= folioCartera;
		this.FechaProceso 			= fechaProceso;
		this.Descripcion 			= descripcion;
		this.OrigenProcesoClase 	= origenProcesoClase;
		this.OrigenProcesoMetodo 	= origenProcesoMetodo;
		this.CreadorParametro 		= creadoPor;
	}
}
	
	/*******************
	 * metodo principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		log.write("*** Inicio de la ejecucion ***");
		Gson gson = new Gson();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		log.write("*** Fin de la ejecucion ***");
		return dataout;
	}

	
	/**********************
	 * logica del programa
	 **********************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		log.write("INICIO::AnularPrestamo");
		SQL sql = new SQL();
		Salida salida = new Salida();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		log.write("Inicia ejecucion de Anulacion Prestamos con datos de entrada: " + entrada);
		
		
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		
		
		// --------------------------------------------------------------
		// BUSCA MAS DATOS DEL PRESTAMO
		
		HashMap<String, Object> paramPrestamos = new HashMap<String, Object>();
		paramPrestamos.put("IdEmpresa",       entrada.p_IdEmpresa);
		paramPrestamos.put("FoliosPrestamos", entrada.p_FolioPrestamo);

		List<ObjRegistroPrestamo> lstPrestamos = new ArrayList<ObjRegistroPrestamo>();		
		lstPrestamos = sql.query(con, SQL_SELECT_PRESTAMOS, paramPrestamos, ObjRegistroPrestamo.class);		
		List<RetiroCustodia.ObjDatosEntrada> lstRetiros = new ArrayList<>();
		

		if (lstPrestamos.size() > 0)
		{
			//---------------------------------------------------------------------------------------------
			// Valida que la anulacion corresponde al dia del ingreso de la operacion
			HashMap<Long, Object> hashPrestamos = Utils.List2Hash(lstPrestamos, "FolioCartera");
			ObjRegistroPrestamo infoPrestamos = (ObjRegistroPrestamo) hashPrestamos.get(entrada.p_FolioPrestamo);
			
			Date fechaInicial   = infoPrestamos.FechaOperacion;
			Date fechaFinal 	= dateFormat.parse(entrada.p_FechaAnulacion);
			int diferenciaDias 	= (int) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
			
			if (diferenciaDias != 0)
			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ANULACION_PRESTAMO_SOLO_DIA_INGRESO",null,con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;
			}
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE LOS PRESTAMISTAS ASOCIADOS
			
			HashMap<String,Object> paramPresta = new HashMap<String,Object>();
			paramPresta.put("FolioPrestamo", entrada.p_FolioPrestamo);
			paramPresta.put("IdEmpresa",     entrada.p_IdEmpresa);
			List<Prestamista> lstPrestamistas = sql.query(con, SQL_SELECT_PRESTAMISTAS, paramPresta, Prestamista.class);
			
			
			if (lstPrestamistas.size() > 0)
			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PRESTAMO_CON_PRESTAMISTAS_VIGENTES",null,con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;				
			}
			
			if ((infoPrestamos.IdTipoPrestamo.equalsIgnoreCase("PRTVTC") || infoPrestamos.IdTipoPrestamo.equalsIgnoreCase("PRVCRF")) && infoPrestamos.IdModulo.equalsIgnoreCase("FAC"))
			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PRESTAMO_VENTA_CORTA_NEGOCIO_NO_SE_ANULA",null,con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;				
			}
		}
		else
		{
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PRESTAMO_NO_SE_ENCUENTRA_VIGENTE",null,con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
			return salida;				
		}
				
		// --------------------------------------------------------------
		// SOLICITA ANULACION DE PRESTAMO (CARTERA Y SUBYACENTE)

		VencerPrestamos objVencimientoPrt = new VencerPrestamos();
		VencerPrestamos.Entrada paramsVencPrt = new VencerPrestamos.Entrada();
		paramsVencPrt.p_Fecha 						= entrada.p_FechaAnulacion;
		paramsVencPrt.p_IdEmpresa 					= entrada.p_IdEmpresa;
		paramsVencPrt.p_IdModulo 					= entrada.p_IdModulo;
		paramsVencPrt.p_IdUsuario 					= entrada.p_IdUsuario;
		paramsVencPrt.p_IdPais 						= entrada.p_IdPais;
		paramsVencPrt.p_OrigenLiquidacion 			= "ANULACION";
		paramsVencPrt.p_VieneDesdeVencerEmprestito 	= "N";
		
		for (ObjRegistroPrestamo reg : lstPrestamos){
			VencerPrestamos.ObjRegistroPrestamo newReg = new VencerPrestamos.ObjRegistroPrestamo();
			Utils.copyPojo(reg, newReg );
			paramsVencPrt.p_lstPrestamos.add(newReg);
		}

		VencerPrestamos.Salida resulVencPrt = objVencimientoPrt.logica(con, paramsVencPrt);
		salida.P_OUT_ERRORESCONTADOR = resulVencPrt.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resulVencPrt.P_OUT_MENSAJES);	
		
		if (salida.P_OUT_ERRORESCONTADOR == 0L) {
			List<MensajeSalida> salidaInformarAnulacionBlockChain = informarAnulacionPrestamoEnBlockChain(con, entrada, lstPrestamos.get(0));
			salida.P_OUT_MENSAJES.addAll(salidaInformarAnulacionBlockChain);
		}

		log.write("FIN::AnularPrestamo");
		return salida;
	}
	
	public List<MensajeSalida> informarAnulacionPrestamoEnBlockChain(Connection con, Entrada entrada, ObjRegistroPrestamo prestamo) throws Exception {
		log.write("INICIO::informarAnulacionPrestamoEnBlockChain");
		
		List<MensajeSalida> salida 			= new ArrayList<>();
		SQL sql 							= new SQL();
		HashMap<String,Object> parametros 	= new HashMap<String,Object>();
		
		if(entrada.p_VieneDesdeBlockChain.equals("S")) {
			return salida;
		}
		
		/*** TIPO DOCUMENTO BLOCKCHAIN HABILITADO ***/
		parametros.put("p_IdTipoDocumento", prestamo.IdTipoPrestamo.equalsIgnoreCase("PRTVTC") || prestamo.IdTipoPrestamo.equalsIgnoreCase("PRVCRF") ? 
				ConstantesBCK.CONTRATOPRESTAMOANEXO42 : ConstantesBCK.CONTRATOPRESTAMOANEXO48);
		List<TipoDocumento> arrTipoDocumento = sql.query(con, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametros, TipoDocumento.class);
		parametros.clear();
		
		if (arrTipoDocumento.size() == 0) {
			return salida;
		}
		
		/*** INFORMACION CORREDORA ***/
		ConsultaDataCorredoraBlockChain consultaDataCorredora = new ConsultaDataCorredoraBlockChain();
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora = new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
		entradaConsultaDataCorredora.p_IdEmpresa = entrada.p_IdEmpresa;
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(con, entradaConsultaDataCorredora);
		
		if (salidaConsultaDataCorredora.p_Out_ContadorError > 0) {
			salida.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA", null, con, entrada.p_IdEmpresa));
			return salida;
		}
		
		if (prestamo.FolioSistemaExterno != null && !"".equals(prestamo.FolioSistemaExterno) && !"0".equals(prestamo.FolioSistemaExterno)) {
			log.write("SI TIENE CONTRATO FIRMADO");
					
			ModificaPrestamoBlockChain modificaPrestamoBlockChain = new ModificaPrestamoBlockChain();
			ModificaPrestamoBlockChain_Entrada ModificaPrestamoBlockChainEntrada = new  ModificaPrestamoBlockChain_Entrada();
			ModificaPrestamoBlockChainEntrada.p_IdEmpresa 			= entrada.p_IdEmpresa;
			ModificaPrestamoBlockChainEntrada.p_codCorrOpe 			= salidaConsultaDataCorredora.p_Out_BrokerageID;
			ModificaPrestamoBlockChainEntrada.p_cantidad 			= "0";
			ModificaPrestamoBlockChainEntrada.p_fechaLiq 			= "";
			ModificaPrestamoBlockChainEntrada.p_tipoModificacion 	= cl.bcs.socionegocio.pojo.ConstantesBCK.ACTION_MODIFY;
			ModificaPrestamoBlockChainEntrada.p_rutAnular 			= "";
			ModificaPrestamoBlockChainEntrada.p_tipoGarantia 		= "";
			ModificaPrestamoBlockChainEntrada.p_origenModificacion 	= cl.bcs.socionegocio.pojo.ConstantesBCK.ANULAR_APTMO;
			ModificaPrestamoBlockChainEntrada.p_tasa 				= "0";
			ModificaPrestamoBlockChainEntrada.p_id 					= prestamo.FolioSistemaExterno;
			ModificaPrestamoBlockChain_Salida ModificaPrestamoBlockChainSalida = modificaPrestamoBlockChain.logica(con, ModificaPrestamoBlockChainEntrada);
			
			salida.addAll(ModificaPrestamoBlockChainSalida.P_OUT_MENSAJES);
			
			if (ModificaPrestamoBlockChainSalida.p_Out_ContadorError > 0) {
				List<LogProcesoPrestamoBlockChain> logProcesoCrearEmprestitoBlockChain = new ArrayList<>();
				logProcesoCrearEmprestitoBlockChain.add(new LogProcesoPrestamoBlockChain(entrada.p_IdEmpresa, entrada.p_FolioPrestamo, entrada.p_FechaAnulacion, ModificaPrestamoBlockChainSalida.p_Out_MensajeError, "AnularPrestamo", "Logica",entrada.p_IdUsuario.toString())); 
											
				if (!logProcesoCrearEmprestitoBlockChain.isEmpty()) {
					sql.batch(con, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoCrearEmprestitoBlockChain, LogProcesoPrestamoBlockChain.class);	
				}
			}
		}
		else {
			salida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FOLIO_PRESTAMOBC", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
		}
		
		log.write("FIN::informarAnulacionPrestamoEnBlockChain");
		
		return salida;
	}

	//------------------------
	//-- metodos del programa
	//------------------------


	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":50,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaAnulacion\":\"2023-07-11\",\"p_FolioPrestamo\":65445,\"p_IdPais\":44}";

			Entrada Objin = gson.fromJson(json, Entrada.class);

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			AnularPrestamo ven = new AnularPrestamo();
			try {
				Salida resp = ven.logica(con, Objin);
				//con.rollback();
				con.commit();
				con.close();
				System.out.println(resp);
			} catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
			
			System.out.println("== END ==");
		}
	}
}

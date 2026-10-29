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

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain;
import cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.SelectDocumentoIdentidad;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Entrada;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Salida;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.Prestamista;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularEmprestito implements JavaCall {
	ConstantesBCK _ConstantesBCK = new ConstantesBCK();

	private final Log log = new Log("CUS.AnularEmprestito");
	
	private final String SQL_BuscaDatosEmprestito 			= loadResourceToString("cl/bcs/custodia/sql/AnularEmprestito_SelectEmprestito.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD		= loadResourceToString("cl/bcs/socionegocio/bck/sql/ConsultarEstadoContratoBlockchain_SelectDocumentoIdentidad.sql");
	private final String SQL_INSERT_LOG_PROCESO_BLOCKCHAIN 	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_InsertLogProcesoPrestamoBlockChain.sql");

	
	
	public static class ObjRegistroEmprestito{
		public Long FolioEmprestito;
	}
	

	public static class Entrada {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdEmpleadoDigitador;
		public Long 	p_IdPais;
		public String 	p_FechaAnulacion;
		public Long 	p_FolioEmprestito; // FolioCartera del emprestito
		public String 	p_VieneDesdeBlockChain = "N"; 	// si esto esta en S
														// significa que debe
														// saltarse validar en
														// BlockChain e ir a anular a BlockChain
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
		public Entrada(){
			this.p_FechaAnulacion = "";
			this.p_FolioEmprestito = 0L;
		}
		
	}

	public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public String P_OUT_DESCRIPCION;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<LogEjecucion> P_OUT_LOGEJECUCION;

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

	public static class LogEjecucion {
		public Long TimeMillis;
		public String Mensaje;
		public String EsError;
		public String EsInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjRegistroEmprestitoDetalle {
		public Long IdCartera;
		public Long IdSubyacente;
		public Long FolioSubyacente;
		public Long FolioEmprestito;
		public String FolioSistemaExterno;
		public Long IdCarteraRetirada;
		public java.sql.Date FechaVencimientoEmprestito;
		public java.sql.Date FechaEmprestito;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public Long FolioPrestamo;
		public Long IdClientePrestamo;
		public Long IdPortafolioPrestamo;
		public java.sql.Date FechaPrestamo;
		public Long IdInstrumentoEmprestito;
		public String IdTipoInstrumentoEmprestito;
		public String IdEmisorEmprestito;
		public String NemoSubyacente;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoSubyacente;
		public String IdTipoReajusteSubyacente;
		public String IdEmisorSubyacente;
		public String IdMonedaSubyacente;
		public BigDecimal INGCantidad;
		public BigDecimal INGTasaEmprestito;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoInstrumento;
		public BigDecimal INGMontoTotalEmprestito;
		public BigDecimal INGMontoPremio;
		public String ConCustodiaPrestamista;
		public String ConCustodiaPrestatario;
		public String IdBolsa;
		public String OperaContraCuentaInversionPrestamista;
		public String OperaContraCuentaInversionPrestatario;
		public BigDecimal PrecioInstrumento;
		public Long PlazoAlVencimiento;
		public Long EmpleadoDigitador;
		public Long IdMovimientoCarteraEnTransito;
		
		public java.sql.Date FechaVencimientoSubyacente;
		public String EsFungibleSubyacente;
		public String IdMonedaEmisionSubyacente;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjRegistroCarteraPrestatarioDetalle {
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdMonedaInstrumento;
		public String IdEmisor;
		public BigDecimal PrecioCompra;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public BigDecimal MontoCompraMonedaInstrumento;
		public String IdMonedaEmision;
		public java.sql.Date FechaVencimientoInstrumento;
		public String IdTipoInstrumento;
		public String EsFungible;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	public static class ObjRegistroCarteraRetiradaEmprestito {
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;		
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	public static class ObjFolioCarteraVigente {
		public Long IdCartera;
		public Long FolioCartera;
		public Long FolioEmprestito;
		public Long IdCliente;
		public Long IdPortafolio;
		public java.sql.Date FechaLiquidacion;
		public Long IdInstrumento;
		public String IdEmisor;
		public BigDecimal CantidadEmprestito;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
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
		log.write("INICIO::AnularEmprestito");
		SQL sql = new SQL();
		Salida salida = new Salida();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		salida.P_OUT_LOGEJECUCION = new ArrayList<LogEjecucion>();
		
		List<ObjRegistroEmprestitoDetalle> lstEmprestitos = new ArrayList<ObjRegistroEmprestitoDetalle>();
		HashMap<String, Object> prmBuscaEmprestitos = new HashMap<String, Object>();
		prmBuscaEmprestitos.put("p_IdEmpresa", entrada.p_IdEmpresa);
		prmBuscaEmprestitos.put("lstFolioEmprestito", entrada.p_FolioEmprestito);
		lstEmprestitos = sql.query(con, SQL_BuscaDatosEmprestito, prmBuscaEmprestitos,ObjRegistroEmprestitoDetalle.class);
		
		List<Long> ListClientes = new ArrayList<>();
		
		// Valida que la anulacion no corresponde al mismo dia del del vencimiento
		if (lstEmprestitos.size() > 0)
		{	
			HashMap<Long, Object> hashEmprestitos = Utils.List2Hash(lstEmprestitos, "FolioEmprestito");
			ObjRegistroEmprestitoDetalle infoEmprestitos = (ObjRegistroEmprestitoDetalle) hashEmprestitos.get(entrada.p_FolioEmprestito);
			HashMap<String,String> valores = new HashMap<String,String>();
	
			Date fechaInicial   = dateFormat.parse(fechaSistema); 
			Date fechaFinal 	= infoEmprestitos.FechaVencimientoEmprestito;			
			int diferenciaDias 	= (int) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
			
			if (diferenciaDias <= 0)
			{	
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PRESTAMISTA_NO_SE_PUEDE_DESASIGNAR_POR_FECHA_VCTO",valores,con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;
			}			
			
			VencerEmprestitos ObjVencimiento = new VencerEmprestitos();
			VencerEmprestitos.Entrada paramsVenc = new VencerEmprestitos.Entrada();
			paramsVenc.p_Fecha 				= fechaSistema;
			paramsVenc.p_IdEmpresa 			= entrada.p_IdEmpresa;
			paramsVenc.p_IdModulo 			= entrada.p_IdModulo;
			paramsVenc.p_IdPais 			= entrada.p_IdPais;
			paramsVenc.p_IdUsuario 			= entrada.p_IdUsuario;
			paramsVenc.p_RebajaPrestamo 	= "S";
			paramsVenc.p_OrigenLiquidacion 	= "ANULACION";
			
			for (ObjRegistroEmprestitoDetalle reg : lstEmprestitos){
				
				ListClientes.add(reg.IdCliente);
				
				VencerEmprestitos.EmprestitoAnticipando regAnt = new VencerEmprestitos.EmprestitoAnticipando();
				regAnt.Cantidad 				= reg.INGCantidad;
				regAnt.FolioEmprestito 			= reg.FolioEmprestito;
				
				paramsVenc.p_lstEmprestitos.add(reg.FolioEmprestito);
				paramsVenc.p_lstAnticipos.add(regAnt);
			}

			VencerEmprestitos.Salida resulVencimiento = ObjVencimiento.logica(con, paramsVenc);
			salida.P_OUT_ERRORESCONTADOR = resulVencimiento.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll(resulVencimiento.P_OUT_MENSAJES);		
			
			List<MensajeSalida> anulacionEmprestitoEnBlockChain = informarAnulacionEmprestitoEnBlockChain(con, entrada, ListClientes, lstEmprestitos);
			salida.P_OUT_MENSAJES.addAll(anulacionEmprestitoEnBlockChain);
		}
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		log.write("FIN::AnularEmpresitito");
		return salida;
	}
	
	public List<MensajeSalida> informarAnulacionEmprestitoEnBlockChain(Connection con, Entrada entrada, List<Long> clientes, List<ObjRegistroEmprestitoDetalle> emprestitos) throws Exception {
		log.write("INICIO::informarAnulacionEmprestitoEnBlockChain");
		
		List<MensajeSalida> salida 			= new ArrayList<>();
		SQL sql 							= new SQL();
		HashMap<String,Object> parametros 	= new HashMap<String,Object>();
		
		if(entrada.p_VieneDesdeBlockChain.equals("S")) {
			return salida;
		}
		
		/*** TIPO DOCUMENTO BLOCKCHAIN HABILITADO ***/
		parametros.put("p_IdTipoDocumento", ConstantesBCK.CONTRATOPRESTAMOANEXO43);
		List<TipoDocumento> arrTipoDocumento = sql.query(con, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametros, TipoDocumento.class);
		parametros.clear();

		if(arrTipoDocumento.size() == 0) {
			return salida;
		}
		
		/*** INFORMACION CORREDORA ***/
		ConsultaDataCorredoraBlockChain consultaDataCorredora = new ConsultaDataCorredoraBlockChain();
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora = new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
		entradaConsultaDataCorredora.p_IdEmpresa = entrada.p_IdEmpresa;
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(con, entradaConsultaDataCorredora);
		
		/*** INFORMACION PRESTAMISTA ***/
		parametros.put("p_IdCliente", clientes);
		List<SelectDocumentoIdentidad> SelectPrestamista = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, SelectDocumentoIdentidad.class);
		
		if (SelectPrestamista.size() == 0) {
			salida.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"NO_SE_PUDO_OBTENER_INFORMACION_PRESTAMISTA", null, con, entrada.p_IdEmpresa));
			return salida;
		}
			
		List<Prestamista> ListPrestamistas = new ArrayList<>();
			
		for (ObjRegistroEmprestitoDetalle reg : emprestitos) {
			for (SelectDocumentoIdentidad Cliente : SelectPrestamista) {
				if (!reg.IdCliente.equals(Cliente.IdCliente )) continue;
					
				Prestamista p_prestamista = new Prestamista();
				p_prestamista.rut       = Cliente.DocumentoIdentidad.trim();
				p_prestamista.cantidad  = "0";
				ListPrestamistas.add(p_prestamista); 
					
				if ( reg.FolioSistemaExterno != null && !"".equals(reg.FolioSistemaExterno) ) {
					ModificaPrestamoBlockChain modificaPrestamoBlockChain 					= new ModificaPrestamoBlockChain();
					ModificaPrestamoBlockChain_Entrada modificaPrestamoBlockChainEntrada 	= new ModificaPrestamoBlockChain_Entrada();
					modificaPrestamoBlockChainEntrada.p_IdEmpresa          = entrada.p_IdEmpresa;
					modificaPrestamoBlockChainEntrada.p_codCorrOpe         = salidaConsultaDataCorredora.p_Out_BrokerageID;
					modificaPrestamoBlockChainEntrada.p_cantidad           = "0";
					modificaPrestamoBlockChainEntrada.p_fechaLiq           = entrada.p_FechaAnulacion;
					modificaPrestamoBlockChainEntrada.p_tipoModificacion   = ConstantesBCK.ACTION_MODIFY; 
					modificaPrestamoBlockChainEntrada.p_prestamistas       = ListPrestamistas;
					modificaPrestamoBlockChainEntrada.p_rutAnular          = Cliente.DocumentoIdentidad.trim();
					modificaPrestamoBlockChainEntrada.p_tipoGarantia       = "";
					modificaPrestamoBlockChainEntrada.p_origenModificacion = ConstantesBCK.ANULAR_APTMTA;
					modificaPrestamoBlockChainEntrada.p_tasa               = reg.INGTasaEmprestito.toString();
					modificaPrestamoBlockChainEntrada.p_id                 = reg.FolioSistemaExterno.toString();		
					ModificaPrestamoBlockChain_Salida modificaPrestamoBlockChainSalida = modificaPrestamoBlockChain.logica(con, modificaPrestamoBlockChainEntrada);
					
					salida.addAll( modificaPrestamoBlockChainSalida.P_OUT_MENSAJES );
				
					if ( modificaPrestamoBlockChainSalida.p_Out_ContadorError > 0 ) {
						List<LogProcesoPrestamoBlockChain> logProcesoCrearEmprestitoBlockChain = new ArrayList<>();
						logProcesoCrearEmprestitoBlockChain.add( new LogProcesoPrestamoBlockChain(entrada.p_IdEmpresa, entrada.p_FolioEmprestito, entrada.p_FechaAnulacion, modificaPrestamoBlockChainSalida.p_Out_MensajeError, "AnularEmprestito", "Logica", entrada.p_IdUsuario.toString()) ); 
							
						if ( !logProcesoCrearEmprestitoBlockChain.isEmpty() ) {
							sql.batch(con, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoCrearEmprestitoBlockChain, LogProcesoPrestamoBlockChain.class);	
						}
							
						log.write("==NO ASIGNAMOS PRESTAMISTA ==" + modificaPrestamoBlockChainSalida.p_Out_Status + "--" + modificaPrestamoBlockChainSalida.p_Out_ContadorError);
					}
				} else {
					salida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FOLIO_PRESTAMOBC", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
				}
			}
		}
		
		log.write("FIN::informarAnulacionEmprestitoEnBlockChain");
		
		return salida;
	}

	//------------------------
	//-- metodos del programa
	//------------------------

	public void logEjecucion(List<LogEjecucion> logej, String mensaje, String esError, String esInterno) {
		log.write("Tipo=EMP => logEjecucion[esError=" + esError + "]: " + mensaje);
		LogEjecucion aux = new LogEjecucion();
		aux.TimeMillis = System.currentTimeMillis();
		aux.Mensaje = mensaje;
		aux.EsError = esError;
		aux.EsInterno = esInterno;
		logej.add(aux);
	}


	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoDigitador\":7305,\"p_UsaReglasNegocio\":\"S\",\"p_IdPais\":44,\"p_FechaAnulacion\":\"2023-03-22\",\"p_FolioEmprestito\":20706806}";
			
			Entrada Objin = gson.fromJson(json, Entrada.class);

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			AnularEmprestito ven = new AnularEmprestito();
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

package cl.bcs.socionegocio.bck;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.custodia.prt.ModificarPrestamo.ClienteCustodia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Entrada;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Salida;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Entrada;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Salida;
import cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Entrada;
import cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Salida;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Entrada;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Salida;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain;
import cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Entrada;
import cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Salida;
import cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.SelectDocumentoIdentidad;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ConsultaEstadoCntBC;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReintentoPrestamosBlockChain  implements JavaCall{
	private final Log log = new Log("CUS.ReintentoPrestamoBlockChain"); 
	private final String SQL_SELECT_PRESTAMO_VIGENTES 		=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectPrestamosVigentes.sql");
	private final String SQL_UPDATE_FOLIOBLOCKCHAIN			=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_UpdateCarteraConFolioBlockChain.sql");
	private final String SQL_SELECT_PRESTAMOS_ASIGNADO 		=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectAsignacionesVigentes.sql");
	private final String SQL_SELECT_PRESTAMOS_ANTICIPADO 	=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectAnticipaciones.sql");
	private final String SQL_SELECT_IDCARTERAS 				= 	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectIdCarteras.sql");
	private final String SQL_UPDATE_ESTADO_ANTICIPO 		= 	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_UpdateEstadoAnticipo.sql");
	private final String SQL_SELECT_INGCANTIDAD 			=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectIngCantidad.sql");
	private final String SQL_SELECT_PRESTAMOS_ANULADO 		=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectPrestamosAnulado.sql");
	private final String SQL_SELECT_PRESTAMOS_DESASIGNADO	=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectPrestamosDesasignado.sql");
	private final String SQL_SELECT_PRESTAMOS_MODIFICADO	=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectPrestamosModificado.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD		= 	loadResourceToString("cl/bcs/socionegocio/bck/sql/ConsultarEstadoContratoBlockchain_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_CLIENTE_CUSTODIA		= 	loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectClienteCustodia.sql");
	private final String SQL_SELECT_TIPO_DOCUMENTO_ES_BLOCKCHAIN	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectTipoDocumento.sql");
    
	//clase utilitarias
	public static class TipoDocumento extends BcsPojo {
		public Long 	IdClase = 0L;
		public String 	TipoDocumentoNormativa;
	}

	public static class Prestamo extends BcsPojo {
		public Long 		FolioPrestamo = 0L;
		public String 		FechaPrestamo;
		public String 		NemoSubyacente = "";
		public String 		IdClasificacionTipoInstrumento = "";
		public String 		TipoPrestamoSistemaExterno = "";
		public String 		CodigoCondicionLiquidacion = "";
		public BigDecimal 	Cantidad; 
		public BigDecimal 	TasaPrestamo; 
		public String 		FechaVencimientoPrestamo;
		public Long 		IdCliente;
		public String 		IdTipoPrestamo = "";
		public String 		DocumentoIdentidad = "";
		public String 		TipoCustodia;
	}
	
	public static class Cartera extends BcsPojo {
		public Long			p_IdEmpresa		= 0L;
		public Long			p_FolioCartera  = 0L;
		public String		p_FolioSistemaExterno  = "";
	}
	
	public static class ReintentoPrestamosBlockChain_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		
		public ReintentoPrestamosBlockChain_Entrada() {
			this.p_IdEmpresa = 1L;
		}
	}
	
	public static class ReintentoPrestamosBlockChain_Salida extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ReintentoPrestamosBlockChain_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Asignacion extends BcsPojo {
		public Long 		IdEmpresa;
		public String 		TipoCustodia;
		public String 		ExigirGarantia;
		public Long 		IdClientePrestamo;
		public Long 		IdClientePrestamista;
		public BigDecimal 	Cantidad; 
		public String 		FolioSistemaExterno;
		public Long 		FolioEmprestito;
	}
	
	public static class Anticipos extends BcsPojo {
		public Long 		IdEmpresa;
		public String 		TipoCustodia;
		public String 		ExigirGarantia;
		public Long 		IdCliente;
		public Long 		IdClienteRelacionado;
		public BigDecimal 	Cantidad; 
		public BigDecimal 	EGRCantidad; 
		public String 		FolioCarteraRelacionada;
		public Long 		FolioCartera;
		public String 		IdTipoOperacion;
		public Date 		FechaLiquidacion;
		public Date 		FechaOperacion;
		public Long 		IdCartera;
		public String 		FolioSistemaExterno;
		public String		DocumentoIdentidad;
	}
	
	public static class Anular extends BcsPojo {
		public String FolioSistemaExterno;
	}
	
	public static class Desasignar extends BcsPojo {
		public String 	FolioSistemaExterno;
		public Long 	IdCliente;
	}
	
	public static class Modificar extends BcsPojo {
		public String 		FolioSistemaExterno;
		public BigDecimal 	TasaOperacion;
		public String 		FechaVencimiento;
		public String 		IdTipoPrestamo;
		public Long			IdCliente;
	}
	
	public static class SelectIdCartera extends BcsPojo {
		public Long IdCartera;
		public Long FolioCartera;
	}
	
	public static class UpdateEstadoAnticipo extends BcsPojo {
		public Long p_IdCartera;
		
		public UpdateEstadoAnticipo(Long idCartera) {
			this.p_IdCartera = idCartera;
		}
	}
	
	public static class SelectDocumentoIdentidadPrestatario extends BcsPojo {
		public String DocumentoIdentidad;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		return null;
	}
	
	public int[] updateFolioBlockchain(Connection conn, List<Cartera> carterasPrestamoActualizar) throws SQLException {
		SQL sql 	= new SQL();
		int[] rows 	= sql.batch(conn, SQL_UPDATE_FOLIOBLOCKCHAIN, carterasPrestamoActualizar, Cartera.class);
		return rows;
	}
	
	public ReintentoPrestamosBlockChain_Salida logica(Connection conn, ReintentoPrestamosBlockChain_Entrada entrada) throws Exception { 
		ReintentoPrestamosBlockChain_Salida salida 	= new ReintentoPrestamosBlockChain_Salida();
        SQL sql 									= new SQL();
        HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
        List<TipoDocumento> tiposDocumento			= new ArrayList<>();
        List<String> tiposDocumentoConsulta			= new ArrayList<>();
		String fechaSistema 						= Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa);
	    
		tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO42);
		tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO43);
		tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO48);
		
		parametroEntrada.put( "p_IdEmpresa"		, entrada.p_IdEmpresa );
		parametroEntrada.put( "p_TipoDocumento"	, tiposDocumentoConsulta );
		tiposDocumento = sql.query(conn, SQL_SELECT_TIPO_DOCUMENTO_ES_BLOCKCHAIN, parametroEntrada, TipoDocumento.class);
		parametroEntrada.clear();
		
		if ( tiposDocumento.size() < 3 ) {
			return salida;
		}
		
		/*** INFORMACION CORREDORA ***/
		ConsultaDataCorredoraBlockChain consultaDataCorredora 													= new ConsultaDataCorredoraBlockChain();
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora 	= new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
		entradaConsultaDataCorredora.p_IdEmpresa = entrada.p_IdEmpresa;
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(conn, entradaConsultaDataCorredora);
			
		ReintentoPrestamosBlockChain_Salida salidaPendientesPorCrear  = prestamosPendientesPorCrear(conn, salidaConsultaDataCorredora.p_Out_BrokerageID, entrada, fechaSistema);
		salida.P_OUT_MENSAJES.addAll( salidaPendientesPorCrear.P_OUT_MENSAJES );
			
		ReintentoPrestamosBlockChain_Salida salidaAsignacionesPendientes = asignacionesPendientes(conn, salidaConsultaDataCorredora.p_Out_BrokerageID);
		salida.P_OUT_MENSAJES.addAll( salidaAsignacionesPendientes.P_OUT_MENSAJES );
			 
		ReintentoPrestamosBlockChain_Salida salidaAnticiposPendientes = anticiposPendientes(conn, entrada);
		salida.P_OUT_MENSAJES.addAll( salidaAnticiposPendientes.P_OUT_MENSAJES );
		
		ReintentoPrestamosBlockChain_Salida salidaDesasignacionesPendientes = desasignacionesPendientes(conn, entrada, salidaConsultaDataCorredora.p_Out_BrokerageID, fechaSistema);
		salida.P_OUT_MENSAJES.addAll( salidaDesasignacionesPendientes.P_OUT_MENSAJES );
		
		ReintentoPrestamosBlockChain_Salida salidaAnulacionesPendientes = anulacionesPendientes(conn, salidaConsultaDataCorredora.p_Out_BrokerageID, entrada, fechaSistema);
		salida.P_OUT_MENSAJES.addAll( salidaAnulacionesPendientes.P_OUT_MENSAJES );
		
		ReintentoPrestamosBlockChain_Salida salidaModificacionesPendientes = modificacionesPendientes(conn, entrada, salidaConsultaDataCorredora.p_Out_BrokerageID, fechaSistema);
		salida.P_OUT_MENSAJES.addAll( salidaModificacionesPendientes.P_OUT_MENSAJES );
			
		return salida;
	}
	
	public ReintentoPrestamosBlockChain_Salida anticiposPendientes(Connection conn, ReintentoPrestamosBlockChain_Entrada entrada) throws Exception{
		ReintentoPrestamosBlockChain_Salida salida 						= new ReintentoPrestamosBlockChain_Salida();
		AnticiparPrestamoBlockChain AnticiparPrestamoBlockchain 		= new AnticiparPrestamoBlockChain();
		AnticiparPrestamistaBlockChain AnticiparPrestamistaBlockchain 	= new AnticiparPrestamistaBlockChain();
		SQL sql 														= new SQL();
		List<Anticipos> anticipos 										= new ArrayList<>();
		List<SelectIdCartera> idsCartera 								= new ArrayList<>();
		HashMap<String,Object> parametroEntrada 						= new HashMap<String,Object>();
		List<UpdateEstadoAnticipo> carterasEstadoActualizar 			= new ArrayList<>();
		List<Anticipos> cantidadesPorFolioCartera 						= new ArrayList<>();
		String esAnticipoParcial 										= "N";
		
		anticipos = sql.query(conn, SQL_SELECT_PRESTAMOS_ANTICIPADO, parametroEntrada, Anticipos.class);
		parametroEntrada.clear();
		
		parametroEntrada.put("p_FolioCartera", anticipos.stream().map( anticipo -> anticipo.FolioCartera ).collect( Collectors.toList() ));
		idsCartera = sql.query(conn, SQL_SELECT_IDCARTERAS, parametroEntrada, SelectIdCartera.class);
		parametroEntrada.clear();
		
		
		parametroEntrada.put("p_IdEmpresa"		, entrada.p_IdEmpresa);
		parametroEntrada.put("p_FolioPrestamo"	, anticipos.stream().map( anticipo -> anticipo.FolioCartera ).collect( Collectors.toList() ) );
		cantidadesPorFolioCartera = sql.query(conn, SQL_SELECT_INGCANTIDAD, parametroEntrada, Anticipos.class);
		parametroEntrada.clear();
		
		for (Anticipos anticipo : anticipos) {
			
			List<SelectIdCartera> idsCarteraAnticipo = idsCartera.stream()
					.filter( id_cartera -> id_cartera.IdCartera.equals(anticipo.FolioCartera) )
					.collect( Collectors.toList() );
			
			if (anticipo.IdTipoOperacion == "EMPRES") {
				
				AnticiparPrestamistaBlockChain_Entrada AnticiparPrestamistaBlockChainEntrada = new AnticiparPrestamistaBlockChain_Entrada();
				AnticiparPrestamistaBlockChainEntrada.CantidadPrestamo				=	anticipo.Cantidad;
				AnticiparPrestamistaBlockChainEntrada.p_IdEmpresa					=	anticipo.IdEmpresa;
				AnticiparPrestamistaBlockChainEntrada.p_Fecha						=	anticipo.FechaOperacion.toString();
				AnticiparPrestamistaBlockChainEntrada.p_rutPrestamista				=	anticipo.DocumentoIdentidad;
				AnticiparPrestamistaBlockChainEntrada.p_cantidadPrestamista			=	anticipo.Cantidad;
				AnticiparPrestamistaBlockChainEntrada.p_FoliosPrestamoBlockChain.add(anticipo.FolioSistemaExterno.toString());	
				AnticiparPrestamistaBlockChain_Salida AnticiparPrestamistaBlockChainSalida = AnticiparPrestamistaBlockchain.logica(conn, AnticiparPrestamistaBlockChainEntrada);
				
				if (AnticiparPrestamistaBlockChainSalida.P_OUT_ERRORESCONTADOR > 0) {
					log.write("Error al informar anticipo de emprestito a blockchain");
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, AnticiparPrestamistaBlockChainSalida.P_OUT_MENSAJES.get(0).descripcion, null, conn, AnticiparPrestamistaBlockChainEntrada.p_IdEmpresa));
				} else {
					if ( idsCarteraAnticipo.size() == 0 ) {
						carterasEstadoActualizar.addAll( idsCarteraAnticipo.stream()
								.map( cartera_estado -> new UpdateEstadoAnticipo( cartera_estado.IdCartera ) )
								.collect( Collectors.toList() ) );
					}
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, AnticiparPrestamistaBlockChainSalida.P_OUT_MENSAJES.get(0).descripcion, null, conn, AnticiparPrestamistaBlockChainEntrada.p_IdEmpresa));
				}
			} else {
				
				List<Anticipos> cantidadFolioCartera = cantidadesPorFolioCartera.stream()
						.filter( cantidad_cartera -> cantidad_cartera.FolioCartera.equals( anticipo.FolioCartera ) )
						.collect( Collectors.toList() );
				
				if ( cantidadFolioCartera.size() == 0 ) continue;
				
				if (anticipo.Cantidad != cantidadFolioCartera.get(0).EGRCantidad) {
					esAnticipoParcial = "S";
				}
				
				AnticiparPrestamoBlockChain_Entrada AnticiparPrestamoBlockChainEntrada = new AnticiparPrestamoBlockChain_Entrada();
				if ( esAnticipoParcial.equals("S") ) {
					AnticiparPrestamoBlockChainEntrada.p_DocumentoIdentidadPrestamista = anticipo.DocumentoIdentidad;
				}
				AnticiparPrestamoBlockChainEntrada.esAnticipoParcial		 	= 	esAnticipoParcial;
				AnticiparPrestamoBlockChainEntrada.Cantidad						=	anticipo.Cantidad;
				AnticiparPrestamoBlockChainEntrada.p_IdEmpresa					=	anticipo.IdEmpresa;
				AnticiparPrestamoBlockChainEntrada.p_Fecha						=	anticipo.FechaOperacion.toString();
				AnticiparPrestamoBlockChainEntrada.p_FechaLiquidacion			=	anticipo.FechaLiquidacion.toString();
				FolioPrestamoBlockChain folio = new FolioPrestamoBlockChain();
				folio.FolioPrestamoBlockChain = anticipo.FolioSistemaExterno.toString();
				AnticiparPrestamoBlockChainEntrada.p_FoliosPrestamoBlockChain.add(folio);
				AnticiparPrestamoBlockChain_Salida AnticiparPrestamoBlockChainSalida = AnticiparPrestamoBlockchain.logica(conn, AnticiparPrestamoBlockChainEntrada);
				
				if (AnticiparPrestamoBlockChainSalida.P_OUT_ERRORESCONTADOR > 0) {
					log.write("Error al informar anticipo de prestamo a blockchain");
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, AnticiparPrestamoBlockChainSalida.P_OUT_MENSAJES.get(0).descripcion, new HashMap<String, String>(), conn, AnticiparPrestamoBlockChainEntrada.p_IdEmpresa));
				} else {
					if ( idsCarteraAnticipo.size() == 0 ) {
						carterasEstadoActualizar.addAll( idsCarteraAnticipo.stream()
								.map( cartera_estado -> new UpdateEstadoAnticipo( cartera_estado.IdCartera ) )
								.collect( Collectors.toList() ) );
					}
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, AnticiparPrestamoBlockChainSalida.P_OUT_MENSAJES.get(0).descripcion, new HashMap<String, String>(), conn, AnticiparPrestamoBlockChainEntrada.p_IdEmpresa));
				}
			}
		}
		
		actualizarEstadoAnticipos(conn, carterasEstadoActualizar);
		
		return salida;
	}
	
	public ReintentoPrestamosBlockChain_Salida asignacionesPendientes(Connection conn, String codigoCorredora) throws Exception{
		ReintentoPrestamosBlockChain_Salida salida 			= new ReintentoPrestamosBlockChain_Salida();
		SQL sql 											= new SQL();
		List<Asignacion> asignacionesPendientesEnBlockChain = new ArrayList<>();
		HashMap<String,Object> parametroEntrada 			= new HashMap<String,Object>();
		List<Cartera> carterasPrestamo 						= new ArrayList<>();
		
		asignacionesPendientesEnBlockChain = sql.query(conn, SQL_SELECT_PRESTAMOS_ASIGNADO, parametroEntrada, Asignacion.class);
		
		for ( Asignacion objAsignacion : asignacionesPendientesEnBlockChain ) {
			
			AsignaPrestamistaBlockchain asignarPrestamista 					= new AsignaPrestamistaBlockchain();
			AsignaPrestamistaBlockchain_Entrada asignarPrestamistaEntrada 	= new AsignaPrestamistaBlockchain_Entrada();
			
			asignarPrestamistaEntrada.p_IdEmpresa 					= objAsignacion.IdEmpresa;
			asignarPrestamistaEntrada.p_TipoCustodia 				= objAsignacion.TipoCustodia;
			asignarPrestamistaEntrada.p_TipoGarantia 				= objAsignacion.ExigirGarantia;
			asignarPrestamistaEntrada.p_CodigoCorredorPrestamista 	= codigoCorredora;
			asignarPrestamistaEntrada.p_Cantidad 					= objAsignacion.Cantidad.toString();
			asignarPrestamistaEntrada.p_IdClientePrestatario 		= objAsignacion.IdClientePrestamo.toString();
			asignarPrestamistaEntrada.p_IdClientePrestamista 		= objAsignacion.IdClientePrestamista.toString();
			asignarPrestamistaEntrada.p_CantidadPrestamista 		= "";
			asignarPrestamistaEntrada.p_Folio 						= objAsignacion.FolioSistemaExterno;
			
			AsignaPrestamistaBlockchain_Salida AsignarPrestamistaBlockChainFirmado_Salida = asignarPrestamista.logica(conn, asignarPrestamistaEntrada);
		
			salida.P_OUT_MENSAJES.addAll(AsignarPrestamistaBlockChainFirmado_Salida.P_OUT_MENSAJES);
			
			if ( AsignarPrestamistaBlockChainFirmado_Salida.p_Out_ContadorError == 0 ) {
				Cartera carteraPrestamo 				= new Cartera();
				carteraPrestamo.p_IdEmpresa 			= objAsignacion.IdEmpresa;
				carteraPrestamo.p_FolioCartera 			= objAsignacion.FolioEmprestito;
				carteraPrestamo.p_FolioSistemaExterno 	= objAsignacion.FolioSistemaExterno;
				carterasPrestamo.add(carteraPrestamo);
			}
		}
		
		updateFolioBlockchain(conn, carterasPrestamo);
		
		return salida;
	}
	
	public ReintentoPrestamosBlockChain_Salida prestamosPendientesPorCrear(Connection conn, String codigoCorredora, ReintentoPrestamosBlockChain_Entrada entrada, String fechaSistema) throws Exception {
		
		ReintentoPrestamosBlockChain_Salida salida 	= new ReintentoPrestamosBlockChain_Salida();
		SQL sql 									= new SQL();
		HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
		List<Prestamo> prestamosSinFolioBlockChain	= new ArrayList<>();
		List<Cartera> carterasPrestamo 				= new ArrayList<>();
		
		parametroEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parametroEntrada.put("p_FechaOperacion", fechaSistema);	
		prestamosSinFolioBlockChain = sql.query(conn, SQL_SELECT_PRESTAMO_VIGENTES, parametroEntrada, Prestamo.class);
		
		String EsBlockChain 	= null;
		String ContratoFirmado 	= null;
		
		for ( Prestamo objPrestamo : prestamosSinFolioBlockChain ) {
			
			ContratoEsBlockChainFirmado ContratoEsBlockChain 				= new ContratoEsBlockChainFirmado();
			ContratoEsBlockChainFirmado_Entrada ContratoEsBlockChainEntrada = new ContratoEsBlockChainFirmado_Entrada();
			ContratoEsBlockChainEntrada.p_IdEmpresa 	= entrada.p_IdEmpresa;
			ContratoEsBlockChainEntrada.p_IdUsuario 	= entrada.p_IdUsuario;
			ContratoEsBlockChainEntrada.p_ClaveUsuario 	= "";
			
			ConsultaEstadoCntBC ConsultaEstadoCntBC = new ConsultaEstadoCntBC();
			ConsultaEstadoCntBC.p_IdTipoDocumento 				= (objPrestamo.IdTipoPrestamo.equals("PRTVTC")) ? ConstantesBCK.IDCONTRATOPRESTAMOANEXO42 : ConstantesBCK.IDCONTRATOPRESTAMOANEXO48;
			ConsultaEstadoCntBC.p_IdCliente 					= Long.toString(objPrestamo.IdCliente);
			ConsultaEstadoCntBC.p_BrokerageRUT 					= objPrestamo.DocumentoIdentidad;
			ConsultaEstadoCntBC.p_IdContrato 					= (objPrestamo.IdTipoPrestamo.equals("PRTVTC")) ? ConstantesBCK.CONTRATOPRESTAMOANEXO42 : ConstantesBCK.CONTRATOPRESTAMOANEXO48;
			ContratoEsBlockChainEntrada.p_ConsultaEstadoCntBC 	= ConsultaEstadoCntBC;
			ContratoEsBlockChainFirmado_Salida ContratoEsBlockChainFirmado_Salida = ContratoEsBlockChain.logica(conn,ContratoEsBlockChainEntrada);

			if ( ContratoEsBlockChainFirmado_Salida.P_OUT_ERRORESCONTADOR > 0 ) {
				log.write("Error al buscar el contrato firmado");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, ContratoEsBlockChainFirmado_Salida.P_OUT_MENSAJES.get(0).descripcion, new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
			}
			
			for ( ContratoEsBlockChainFirmado.Resultado ResultadoBC : ContratoEsBlockChainFirmado_Salida.P_OUT_RESULTADOPROCESO ) {
				EsBlockChain 	= ResultadoBC.EsBlockChain;
				ContratoFirmado = ResultadoBC.ContratoFirmado;
				break;
			}
			
			if ( EsBlockChain.equals(ConstantesBCK.SI) && ContratoFirmado.equals(ConstantesBCK.SI) ) {
				
				IngresoPrestamosBlockchain IngresoPrestamo 							= new IngresoPrestamosBlockchain();
				IngresoPrestamosBlockchain_Entrada IngresoPrestamoBlockChainEntrada = new  IngresoPrestamosBlockchain_Entrada();
				IngresoPrestamoBlockChainEntrada.p_IdEmpresa 					= entrada.p_IdEmpresa;
				IngresoPrestamoBlockChainEntrada.p_Tasa 						= objPrestamo.TasaPrestamo.toString();
				IngresoPrestamoBlockChainEntrada.p_FechaOperacion 				= objPrestamo.FechaPrestamo;		
				IngresoPrestamoBlockChainEntrada.p_Nemo 						= objPrestamo.NemoSubyacente;
				IngresoPrestamoBlockChainEntrada.p_Mercado 						= objPrestamo.IdClasificacionTipoInstrumento;
				IngresoPrestamoBlockChainEntrada.p_TipoPrestamo 				= objPrestamo.TipoPrestamoSistemaExterno;  
				IngresoPrestamoBlockChainEntrada.p_TipoGarantia 				= "T";
				IngresoPrestamoBlockChainEntrada.p_CondicionLiquidacion			= objPrestamo.CodigoCondicionLiquidacion;
				IngresoPrestamoBlockChainEntrada.p_CodigoCorredorPrestamista	= codigoCorredora;
				IngresoPrestamoBlockChainEntrada.p_Cantidad 					= objPrestamo.Cantidad.toBigInteger().toString();
				IngresoPrestamoBlockChainEntrada.p_FechaVencimiento 			= objPrestamo.FechaVencimientoPrestamo;
				IngresoPrestamoBlockChainEntrada.p_IdClientePrestatario 		= objPrestamo.IdCliente.toString();
				IngresoPrestamoBlockChainEntrada.p_IdClientePrestamista 		= "0";
				IngresoPrestamoBlockChainEntrada.p_CantidadPrestamista			= "0";						
				IngresoPrestamosBlockchain_Salida IngresoPrestamoBlockChainFirmado_Salida = IngresoPrestamo.logica(conn, IngresoPrestamoBlockChainEntrada);
				
				salida.P_OUT_MENSAJES.addAll( IngresoPrestamoBlockChainFirmado_Salida.P_OUT_MENSAJES );
				
				if ( IngresoPrestamoBlockChainFirmado_Salida.p_Out_ContadorError == 0 ) {
					log.write("Se crea prestamo en BlockChain folio --" + IngresoPrestamoBlockChainFirmado_Salida.p_Out_FolioExterno);
					Cartera carteraPrestamo 				= new Cartera();
					carteraPrestamo.p_IdEmpresa 			= entrada.p_IdEmpresa;
					carteraPrestamo.p_FolioCartera 			= objPrestamo.FolioPrestamo;
					carteraPrestamo.p_FolioSistemaExterno 	= IngresoPrestamoBlockChainFirmado_Salida.p_Out_FolioExterno;
					carterasPrestamo.add(carteraPrestamo);
				}
			}
		}
		
		updateFolioBlockchain(conn, carterasPrestamo);
		
		return salida;
	}
	
	public ReintentoPrestamosBlockChain_Salida anulacionesPendientes(Connection conn, String codigoCorredora, ReintentoPrestamosBlockChain_Entrada entrada, String fechaSistema) throws Exception {
		
		ReintentoPrestamosBlockChain_Salida salida 	= new ReintentoPrestamosBlockChain_Salida();
		SQL sql 									= new SQL();
		HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
		List<Anular> prestamosAnulado				= new ArrayList<>();
		
		parametroEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parametroEntrada.put("p_FechaOperacion", fechaSistema);	
		prestamosAnulado = sql.query(conn, SQL_SELECT_PRESTAMOS_ANULADO, parametroEntrada, Anular.class);
		
		if ( prestamosAnulado.size() == 0 ) {
			return salida;
		}
		
		ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
		consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= entrada.p_IdEmpresa;
		consultaPrestamoPorLAIDEntrada.p_FolioExterno 	= prestamosAnulado.parallelStream()
				.map( prestamo_anulado -> new ConsultaListaPorFolioBlockChain.Folio( prestamo_anulado.FolioSistemaExterno ) )
				.collect( Collectors.toList() );
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
		
		for ( Anular prestamoAnulado : prestamosAnulado ) {
			
			if ( prestamoAnulado.FolioSistemaExterno.isEmpty() || prestamoAnulado.FolioSistemaExterno.equals("0") ) continue;
			
			List<PrestamosBlockChain> prestamoBlockChain = consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
			.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equalsIgnoreCase( prestamoAnulado.FolioSistemaExterno ) )
			.collect( Collectors.toList() );
			
			if ( prestamoBlockChain.size() == 0 ) continue;
			if ( new BigDecimal( prestamoBlockChain.get(0).laDetails.LendingQty ).compareTo( new BigDecimal("0") ) == 0 ) continue;
			
			ModificaPrestamoBlockChain modificaPrestamoBlockChain 					= new ModificaPrestamoBlockChain();
			ModificaPrestamoBlockChain_Entrada ModificaPrestamoBlockChainEntrada 	= new ModificaPrestamoBlockChain_Entrada();
			ModificaPrestamoBlockChainEntrada.p_IdEmpresa 			= entrada.p_IdEmpresa;
			ModificaPrestamoBlockChainEntrada.p_codCorrOpe 			= codigoCorredora;
			ModificaPrestamoBlockChainEntrada.p_cantidad 			= "0";
			ModificaPrestamoBlockChainEntrada.p_fechaLiq 			= "";
			ModificaPrestamoBlockChainEntrada.p_tipoModificacion 	= cl.bcs.socionegocio.pojo.ConstantesBCK.ACTION_MODIFY;
			ModificaPrestamoBlockChainEntrada.p_rutAnular 			= "";
			ModificaPrestamoBlockChainEntrada.p_tipoGarantia 		= "";
			ModificaPrestamoBlockChainEntrada.p_origenModificacion 	= cl.bcs.socionegocio.pojo.ConstantesBCK.ANULAR_APTMO;
			ModificaPrestamoBlockChainEntrada.p_tasa 				= "0";
			ModificaPrestamoBlockChainEntrada.p_id 					= prestamoAnulado.FolioSistemaExterno;
			ModificaPrestamoBlockChain_Salida ModificaPrestamoBlockChainSalida = modificaPrestamoBlockChain.logica(conn, ModificaPrestamoBlockChainEntrada);
			
			salida.P_OUT_MENSAJES.addAll( ModificaPrestamoBlockChainSalida.P_OUT_MENSAJES );
			
		}
		
		return salida;
	}
	
	public ReintentoPrestamosBlockChain_Salida desasignacionesPendientes(Connection conn, ReintentoPrestamosBlockChain_Entrada entrada, String codigoCorredora, String fechaSistema) throws Exception {
		
		ReintentoPrestamosBlockChain_Salida salida 	= new ReintentoPrestamosBlockChain_Salida();
		SQL sql 									= new SQL();
		HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
		List<Desasignar> prestamosDesasignado		= new ArrayList<>();
		
		parametroEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parametroEntrada.put("p_FechaOperacion", fechaSistema);	
		prestamosDesasignado = sql.query(conn, SQL_SELECT_PRESTAMOS_DESASIGNADO, parametroEntrada, Desasignar.class);
		parametroEntrada.clear();
		
		if ( prestamosDesasignado.size() == 0 ) {
			return salida;
		}
		
		ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
		consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= entrada.p_IdEmpresa;
		consultaPrestamoPorLAIDEntrada.p_FolioExterno 	= prestamosDesasignado.parallelStream()
				.map( prestamo_desasignado -> new ConsultaListaPorFolioBlockChain.Folio( prestamo_desasignado.FolioSistemaExterno ) )
				.collect( Collectors.toList() );
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
		
		/*** INFORMACION PRESTAMISTA ***/
		parametroEntrada.put("p_IdCliente", prestamosDesasignado.parallelStream()
				.map( prestamo_desasignado -> prestamo_desasignado.IdCliente )
				.collect( Collectors.toList() ) );
		List<SelectDocumentoIdentidad> prestamistasDatos = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroEntrada, SelectDocumentoIdentidad.class);
		parametroEntrada.clear();
		
		if ( prestamistasDatos.size() == 0 ) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"NO_SE_PUDO_OBTENER_INFORMACION_PRESTAMISTA", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		
		for ( Desasignar prestamoDesasignado : prestamosDesasignado ) {
			
			if ( prestamoDesasignado.FolioSistemaExterno.isEmpty() || prestamoDesasignado.FolioSistemaExterno.equals("0") ) continue;
			
			List<PrestamosBlockChain> prestamoBlockChain = consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
			.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equalsIgnoreCase( prestamoDesasignado.FolioSistemaExterno ) )
			.collect( Collectors.toList() );
			
			if ( prestamoBlockChain.size() == 0 ) continue;
			
			List<SelectDocumentoIdentidad> prestamistaDatos = prestamistasDatos.stream()
					.filter( prestamista -> prestamista.IdCliente.equals(prestamoDesasignado.IdCliente) )
					.collect( Collectors.toList() );
			
			if ( prestamistaDatos.size() == 0 ) continue;
			
			if ( !prestamoBlockChain.get(0).lendingClients
					.stream()
					.anyMatch( prestamo_cliente -> prestamo_cliente.LendingClientRUT.trim().equalsIgnoreCase( prestamistaDatos.get(0).DocumentoIdentidad.trim() ) ) ) continue;
			
			ModificaPrestamoBlockChain modificaPrestamoBlockChain 					= new ModificaPrestamoBlockChain();
			ModificaPrestamoBlockChain_Entrada modificaPrestamoBlockChainEntrada 	= new ModificaPrestamoBlockChain_Entrada();
			modificaPrestamoBlockChainEntrada.p_IdEmpresa          = entrada.p_IdEmpresa;
			modificaPrestamoBlockChainEntrada.p_codCorrOpe         = codigoCorredora;
			modificaPrestamoBlockChainEntrada.p_cantidad           = "0";
			modificaPrestamoBlockChainEntrada.p_fechaLiq           = fechaSistema;
			modificaPrestamoBlockChainEntrada.p_tipoModificacion   = ConstantesBCK.ACTION_MODIFY; 
			modificaPrestamoBlockChainEntrada.p_rutAnular          = prestamistaDatos.get(0).DocumentoIdentidad.trim();
			modificaPrestamoBlockChainEntrada.p_tipoGarantia       = "";
			modificaPrestamoBlockChainEntrada.p_origenModificacion = ConstantesBCK.ANULAR_APTMTA;
			modificaPrestamoBlockChainEntrada.p_id                 = prestamoDesasignado.FolioSistemaExterno;		
			ModificaPrestamoBlockChain_Salida modificaPrestamoBlockChainSalida = modificaPrestamoBlockChain.logica(conn, modificaPrestamoBlockChainEntrada);
			
			salida.P_OUT_MENSAJES.addAll( modificaPrestamoBlockChainSalida.P_OUT_MENSAJES );
			
		}
		
		return salida;
	}
	
	public ReintentoPrestamosBlockChain_Salida modificacionesPendientes(Connection conn, ReintentoPrestamosBlockChain_Entrada entrada, String codigoCorredora, String fechaSistema) throws Exception {
		
		ReintentoPrestamosBlockChain_Salida salida 	= new ReintentoPrestamosBlockChain_Salida();
		SQL sql 									= new SQL();
		HashMap<String,Object> parametroEntrada 	= new HashMap<String,Object>();
		List<Modificar> prestamosModificado			= new ArrayList<>();
		
		parametroEntrada.put("p_IdEmpresa"		, entrada.p_IdEmpresa);
		parametroEntrada.put("p_FechaOperacion"	, fechaSistema);	
		prestamosModificado = sql.query(conn, SQL_SELECT_PRESTAMOS_MODIFICADO, parametroEntrada, Modificar.class);
		parametroEntrada.clear();
		
		if ( prestamosModificado.size() == 0 ) {
			return salida;
		}
		
		ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
		consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= entrada.p_IdEmpresa;
		consultaPrestamoPorLAIDEntrada.p_FolioExterno 	= prestamosModificado.parallelStream()
				.map( prestamo_modificado -> new ConsultaListaPorFolioBlockChain.Folio( prestamo_modificado.FolioSistemaExterno ) )
				.collect( Collectors.toList() );
		ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(conn, consultaPrestamoPorLAIDEntrada);
		
		Boolean clienteOperaCustodiaExterna = false;
		parametroEntrada.put("p_IdCliente", 	prestamosModificado.stream()
				.map( prestamo_modificado -> prestamo_modificado.IdCliente )
				.collect( Collectors.toList() ));
		List<ClienteCustodia> clientesCustodia = sql.query(conn, SQL_SELECT_CLIENTE_CUSTODIA, parametroEntrada, ClienteCustodia.class);
		parametroEntrada.clear();
		
		for ( Modificar prestamoModificado : prestamosModificado ) {
			
			if ( prestamoModificado.FolioSistemaExterno.isEmpty() || prestamoModificado.FolioSistemaExterno.equals("0") ) continue;
			
			List<PrestamosBlockChain> prestamoBlockChain = consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
					.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equalsIgnoreCase( prestamoModificado.FolioSistemaExterno ) )
					.collect( Collectors.toList() );
					
			if ( prestamoBlockChain.size() == 0 ) continue;
			
			if ( prestamoBlockChain.get(0).laDetails.MaturityDate.equalsIgnoreCase( prestamoModificado.FechaVencimiento ) && 
					new BigDecimal(prestamoBlockChain.get(0).laDetails.Yield).compareTo( prestamoModificado.TasaOperacion ) == 0 ) continue;
			
			List<ClienteCustodia> clienteCustodia = clientesCustodia.parallelStream()
					.filter( cliente_custodia -> cliente_custodia.IdCliente.equals( prestamoModificado.IdCliente ) )
					.collect( Collectors.toList() );
			
			if ( clienteCustodia.size() > 0 ) {
				clienteOperaCustodiaExterna = clienteCustodia.get(0).TieneContrato.equalsIgnoreCase(ConstantesBCK.NO) ? true : false;
			} else {
				clienteOperaCustodiaExterna = false;
			}
			
			ModificaPrestamoBlockChain_Entrada modificaPrestamoBlockChainEntrada 	= new ModificaPrestamoBlockChain_Entrada();
			ModificaPrestamoBlockChain_Salida modificaPrestamoBlockChainSalida 		= new ModificaPrestamoBlockChain_Salida();
			ModificaPrestamoBlockChain modificaPrestamoBlockChain 					= new ModificaPrestamoBlockChain();
			
			modificaPrestamoBlockChainEntrada.p_IdEmpresa 			= entrada.p_IdEmpresa;
			modificaPrestamoBlockChainEntrada.p_tasa 				= prestamoModificado.TasaOperacion.toString();
			modificaPrestamoBlockChainEntrada.p_origenModificacion	= ConstantesBCK.ANULAR_MPTMO; 
			modificaPrestamoBlockChainEntrada.p_tipoModificacion 	= ConstantesBCK.ACTION_MODIFY;
			modificaPrestamoBlockChainEntrada.p_id 					= prestamoModificado.FolioSistemaExterno;
			modificaPrestamoBlockChainEntrada.p_codCorrOpe  		= codigoCorredora;
			// Prestamo Administrativo (se puede modificar Precio, Prima y Plazo el dia del prestamo o el dia siguiente) 
			if ( prestamoModificado.IdTipoPrestamo.equalsIgnoreCase("PRTADM") || (prestamoModificado.IdTipoPrestamo.equalsIgnoreCase("PRTVTC") && clienteOperaCustodiaExterna) ) {
				modificaPrestamoBlockChainEntrada.p_FechaVencimiento = prestamoModificado.FechaVencimiento;
			}
			modificaPrestamoBlockChainSalida = modificaPrestamoBlockChain.logica(conn, modificaPrestamoBlockChainEntrada);
			
			salida.P_OUT_MENSAJES.addAll( modificaPrestamoBlockChainSalida.P_OUT_MENSAJES );
		}
		
		return salida;
	}
	
	public void actualizarEstadoAnticipos(Connection conn, List<UpdateEstadoAnticipo> carterasActualizar) throws SQLException {
		if (carterasActualizar.size() == 0) return;
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_ESTADO_ANTICIPO, carterasActualizar, UpdateEstadoAnticipo.class);
	}

	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){

			try {
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6}";

				ReintentoPrestamosBlockChain_Entrada in = gson.fromJson(json, ReintentoPrestamosBlockChain_Entrada.class);
						
				ReintentoPrestamosBlockChain demo = new ReintentoPrestamosBlockChain();
				
				ReintentoPrestamosBlockChain_Salida out = demo.logica(con, in);
				con.commit();
			
		}catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}		

		}	
	}

}

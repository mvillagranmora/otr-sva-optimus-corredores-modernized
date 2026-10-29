package cl.bcs.socionegocio.bck;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.prt.AnticiparPrestamistaDesdeBlockChain;
import cl.bcs.custodia.prt.AnticiparPrestamistaDesdeBlockChain.AnticiparPrestamistaDesdeBlockChainEntrada;
import cl.bcs.custodia.prt.AnticiparPrestamistaDesdeBlockChain.AnticiparPrestamistaDesdeBlockChainSalida;
import cl.bcs.custodia.prt.AnticiparPrestamoDesdeBlockChain;
import cl.bcs.custodia.prt.AnticiparPrestamoDesdeBlockChain.AnticiparPrestamoDesdeBlockChain_Entrada;
import cl.bcs.custodia.prt.AnticiparPrestamoDesdeBlockChain.AnticiparPrestamoDesdeBlockChain_Salida;
import cl.bcs.custodia.prt.AnularEmprestitoDesdeBlockChain;
import cl.bcs.custodia.prt.AnularEmprestitoDesdeBlockChain.AnularEmprestitoDesdeBlockChainEntrada;
import cl.bcs.custodia.prt.AnularEmprestitoDesdeBlockChain.AnularEmprestitoDesdeBlockChainSalida;
import cl.bcs.custodia.prt.AnularPrestamoDesdeBlockChain;
import cl.bcs.custodia.prt.AnularPrestamoDesdeBlockChain.AnularPrestamoDesdeBlockChainEntrada;
import cl.bcs.custodia.prt.AnularPrestamoDesdeBlockChain.AnularPrestamoDesdeBlockChainSalida;
import cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain;
import cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain.CrearEmprestitoDesdeBlockChain_Entrada;
import cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain.CrearEmprestitoDesdeBlockChain_Salida;
import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain;
import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain.CrearPrestamoDesdeBlockChain_Entrada;
import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain.CrearPrestamoDesdeBlockChain_Salida;
import cl.bcs.custodia.prt.ModificarPrestamoDesdeBlockChain;
import cl.bcs.custodia.prt.ModificarPrestamoDesdeBlockChain.ModificarPrestamoDesdeBlockChainEntrada;
import cl.bcs.custodia.prt.ModificarPrestamoDesdeBlockChain.ModificarPrestamoDesdeBlockChainSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ActualizaNroBloqueLogBlockChain.ActualizaNroBloqueLogBlockChain_Entrada;
import cl.bcs.socionegocio.bck.ActualizaNroBloqueLogBlockChain.ActualizaNroBloqueLogBlockChain_Salida;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.ConsultaLogBlockChain_Entrada;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.ConsultaLogBlockChain_Salida;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamistaAnticipado;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoAnticipado;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoAsignado;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoCreado;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoModificado;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoPrestamistaDesasignado;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ProcesaLogBlockChain implements JavaCall 
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_INSERT_LOG_PROCESO_BLOCKCHAIN 						= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_InsertLogProcesoPrestamoBlockChain.sql");
	private final String SQL_UPDATE_VENCIMIENTOS_HISTORICO_PROCESO_BLOCKCHAIN 	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_UpdateVencimientosHistorico.sql");
	private final String SQL_SELECT_TIPO_DOCUMENTO_ES_BLOCKCHAIN				= loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectTipoDocumento.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	
	public static class ProcesaLogBlockChain_Entrada extends BcsPojo {
		public Long		p_IdEmpresa	;
		public Long		p_IdUsuario	;
		public String	p_IdModulo	;
		
		public ProcesaLogBlockChain_Entrada()
		{
			//asume que es el modulo custodia
			this.p_IdModulo = "CUS";
		}
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ProcesaLogBlockChain_Salida extends BcsPojo {
		public List<MensajeSalida>			P_OUT_MENSAJES			;
		public Long							P_OUT_ERRORESCONTADOR	;
		public List<String>					P_OUT_RESULTADO			;
		
		public ProcesaLogBlockChain_Salida()
		{	
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR	= 0L;
			this.P_OUT_RESULTADO = new ArrayList<>();
		}
	}
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class PrestamosOptimus extends BcsPojo {
		public String FolioSistemaExterno	;
		public Long FolioPrestamo			;
		public String FechaVencimiento		;
	}
	
	public static class Instrumento extends BcsPojo {
		public Long		IdInstrumento;
		public String	Nemotecnico;
		public String 	IdTipoInstrumento;
		public String	IdClasificacionTipoInstrumento;
		public String	CodigoClasificacionTipoInstrumento;
		public String 	RazonSocial;
	}

	public static class Indicador extends BcsPojo {
		public Long		IdIndicador;
		public Long 	IdTipoIndicador;
		public String	NombreTipoIndicador;
		public String	ClaseDato;
		public String	CodigoTipoIndicador;
		public String	ValorIndicador;
		public String	IdEstadoAutorizacion;
		public String	GrupoIndicador;
		public String	IdUso;
		public Long		CantidadDecimales;
		public Long		LongitudMinima;
		public Long		LongitudMaxima;
		public String	IdTipoEntidad;
	  	public Long		IdEntidad;
	  	public String	ModeloCliente;
	  	public String	IdTipoPersona;
	}
	
	public static class GrupoIndicador extends BcsPojo {
		public String	GrupoIndicador;
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
	
	public static class VencimientoHisPrestamoBlockChain extends BcsPojo {
		public String 	EstadoLiquidacionBC;
		public String 	FolioSistemaExterno;
		
		public VencimientoHisPrestamoBlockChain(String estadoLiquidacionBC, String folioSistemaExterno) {
			this.EstadoLiquidacionBC 	= estadoLiquidacionBC;
			this.FolioSistemaExterno 	= folioSistemaExterno;
		}
	}
	
	public static class TipoDocumento extends BcsPojo {
		public Long 	IdClase = 0L;
		public String 	TipoDocumentoNormativa;
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ProcesaLogBlockChain_Salida logica(Connection conn, ProcesaLogBlockChain_Entrada in) throws Exception 
	{
		ProcesaLogBlockChain_Salida salida 						= new ProcesaLogBlockChain_Salida();
		ConsultaLogBlockChain_Entrada objConsulLogBC_Entrada 	= new  ConsultaLogBlockChain_Entrada();
		ConsultaLogBlockChain objConsultaLogBC 					= new ConsultaLogBlockChain();
		List<Integer> ejecucionesResultado 						= new ArrayList<>();
		List<TipoDocumento> tiposDocumento						= new ArrayList<>();
        List<String> tiposDocumentoConsulta						= new ArrayList<>();
        HashMap<String,Object> parametroEntrada 				= new HashMap<String,Object>();
        SQL sql 												= new SQL();
		String fechaSistema 									= Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		
		tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO42);
		tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO43);
		tiposDocumentoConsulta.add(ConstantesBCK.CONTRATOPRESTAMOANEXO48);
		
		parametroEntrada.put( "p_IdEmpresa"		, in.p_IdEmpresa );
		parametroEntrada.put( "p_TipoDocumento"	, tiposDocumentoConsulta );
		tiposDocumento = sql.query(conn, SQL_SELECT_TIPO_DOCUMENTO_ES_BLOCKCHAIN, parametroEntrada, TipoDocumento.class);
		parametroEntrada.clear();
		
		if ( tiposDocumento.size() < 3 ) {
			return salida;
		}
		
		objConsulLogBC_Entrada.p_IdEmpresa 	= in.p_IdEmpresa;
		//objConsulLogBC_Entrada.p_Bloque 	= "2108"; // TODO: DESPUES COMENTAR
		ConsultaLogBlockChain_Salida consultaLogBlockChainSalida = objConsultaLogBC.logica(conn, objConsulLogBC_Entrada);
		
		salida.P_OUT_RESULTADO.add( consultaLogBlockChainSalida.P_OUT_SECUENCIA );
		
		List<PrestamoCreado> prestamoBlockChainCreado							= consultaLogBlockChainSalida.P_OUT_RESULTADO.PrestamosCreados;
		List<PrestamoModificado> prestamosModificados							= consultaLogBlockChainSalida.P_OUT_RESULTADO.PrestamosModificado;
		List<PrestamoAsignado> prestamoBlockChainAsignado 						= consultaLogBlockChainSalida.P_OUT_RESULTADO.Prestamistas;
		List<String> prestamosAnulados											= consultaLogBlockChainSalida.P_OUT_RESULTADO.PrestamosAnulados;
		List<PrestamoPrestamistaDesasignado> prestamosPrestamistasDesasignados 	= consultaLogBlockChainSalida.P_OUT_RESULTADO.PrestamistasDesasignado;
		List<PrestamoAnticipado> prestamosAnticipado							= consultaLogBlockChainSalida.P_OUT_RESULTADO.PrestamosAnticipados;
		List<PrestamistaAnticipado> prestamistasAnticipados						= consultaLogBlockChainSalida.P_OUT_RESULTADO.PrestamistasAnticipado;
		List<String> prestamosVencido											= consultaLogBlockChainSalida.P_OUT_RESULTADO.PrestamosVencidos;
		
		ejecucionesResultado.add( CrearPrestamoOptimus( conn, in, prestamoBlockChainCreado, salida, fechaSistema ) );
		ejecucionesResultado.add( AsignarPrestamistaEnOptimus( conn, in, prestamoBlockChainAsignado, salida, fechaSistema ) );
		ejecucionesResultado.add( PrestamoOptimusAnulados( conn, in, prestamosAnulados, new ArrayList<String>(), salida, fechaSistema ) );
		ejecucionesResultado.add( PrestamoPrestamistasDesasignados( conn, in, prestamosPrestamistasDesasignados, salida, fechaSistema ) );
		ejecucionesResultado.add( PrestamosModificados( conn, in, prestamosModificados, salida, fechaSistema ) );
		ejecucionesResultado.add( PrestamoOptimusAnticipados( conn, in, prestamosAnticipado, salida, fechaSistema ) );
		ejecucionesResultado.add( PrestamistaAnticipado( conn, in, prestamistasAnticipados, salida, fechaSistema ) );
		ejecucionesResultado.add( PrestamoOptimusVencimientos(conn, in, prestamosVencido, salida) );
		
		//si todos los retornos fueron 1, entonces actualiza el nro de block leido desde Log BlockChain
		
		if ( ejecucionesResultado.stream().reduce( 1, Integer::min ) == 1 ) {
			
			ActualizaNroBloqueLogBlockChain_Entrada actualizaBloqueLogBlockChainEntrada = new  ActualizaNroBloqueLogBlockChain_Entrada();
			actualizaBloqueLogBlockChainEntrada.p_IdEmpresa 	= in.p_IdEmpresa;
			actualizaBloqueLogBlockChainEntrada.p_ValorBlock 	= consultaLogBlockChainSalida.P_OUT_SECUENCIA;
			
			//si viene valor para actualizar
			if ( actualizaBloqueLogBlockChainEntrada.p_ValorBlock != null && !actualizaBloqueLogBlockChainEntrada.p_ValorBlock.isEmpty() ) {
				ActualizaNroBloqueLogBlockChain actualizaBloqueLogBlockChain = new 	ActualizaNroBloqueLogBlockChain();
				ActualizaNroBloqueLogBlockChain_Salida objOut = actualizaBloqueLogBlockChain.logica(conn, actualizaBloqueLogBlockChainEntrada);
				
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR == 1 ? 1 : objOut.P_OUT_ERRORESCONTADOR; 
				salida.P_OUT_MENSAJES.addAll( objOut.P_OUT_MENSAJES );
				
			}
			
				
		}
		return salida;
	}

	//-------------------------------------------------------------------------------------------
	// CREACION DE PRESTAMOS EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public Integer CrearPrestamoOptimus( Connection conn, ProcesaLogBlockChain_Entrada in, List<PrestamoCreado> prestamosCreado, ProcesaLogBlockChain_Salida salida, String fechaSistema ) {
		
		CrearPrestamoDesdeBlockChain objCrearPrestamoDesdeBlockChain 	= new CrearPrestamoDesdeBlockChain();
		
		try {
			
			CrearPrestamoDesdeBlockChain_Entrada objCrearPrestamoDesdeBlockChain_Entrada = new CrearPrestamoDesdeBlockChain_Entrada();
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdEmpresa							= in.p_IdEmpresa;
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdUsuario							= in.p_IdUsuario;
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdModulo							= in.p_IdModulo;
			objCrearPrestamoDesdeBlockChain_Entrada.p_PrestamosCreado					= prestamosCreado;
			
			CrearPrestamoDesdeBlockChain_Salida objCrearPrestamoDesdeBlockChain_Salida	= objCrearPrestamoDesdeBlockChain.logica(conn, objCrearPrestamoDesdeBlockChain_Entrada);
			
			if ( objCrearPrestamoDesdeBlockChain_Salida.P_OUT_PRESTAMOBLOCKCHAIN != null && objCrearPrestamoDesdeBlockChain_Salida.P_OUT_ERRORESCONTADOR > 0 ) {
				//entonces agrego al log para reprocesar
				objCrearPrestamoDesdeBlockChain.AgregaATablaPrestamosPendientesPorCargar( conn, objCrearPrestamoDesdeBlockChain_Salida.P_OUT_PRESTAMOBLOCKCHAIN, "PRT", objCrearPrestamoDesdeBlockChain_Salida.P_OUT_MENSAJES, fechaSistema );
			}
		
			return 1;
			
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			return 0;
		}
		
	}
	//-------------------------------------------------------------------------------------------
	// ASIGNAR PRESTAMISTAS DESDE BLOCKCHAIN A PRESTAMOS EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public Integer AsignarPrestamistaEnOptimus( Connection conn, ProcesaLogBlockChain_Entrada in, List<PrestamoAsignado> lstPrestamosBC, ProcesaLogBlockChain_Salida salida, String fechaSistema )
	{
		
		CrearEmprestitoDesdeBlockChain objCrearEmprestitoDesdeBlockChain = new CrearEmprestitoDesdeBlockChain();
		
		try {
		
			CrearEmprestitoDesdeBlockChain_Entrada objCrearEmprestitoDesdeBlockChain_Entrada = new CrearEmprestitoDesdeBlockChain_Entrada();
			
			objCrearEmprestitoDesdeBlockChain_Entrada.p_IdEmpresa			= in.p_IdEmpresa;
			objCrearEmprestitoDesdeBlockChain_Entrada.p_IdUsuario			= in.p_IdUsuario;
			objCrearEmprestitoDesdeBlockChain_Entrada.p_FoliosBlockChain 	= lstPrestamosBC; 
			objCrearEmprestitoDesdeBlockChain_Entrada.p_IdModulo			= in.p_IdModulo;
			CrearEmprestitoDesdeBlockChain_Salida objCrearEmprestitoDesdeBlockChain_Salida = objCrearEmprestitoDesdeBlockChain.logica(conn, objCrearEmprestitoDesdeBlockChain_Entrada);
			
			if ( objCrearEmprestitoDesdeBlockChain_Salida.P_OUT_PRESTAMOBLOCKCHAIN != null && objCrearEmprestitoDesdeBlockChain_Salida.P_OUT_ERRORESCONTADOR > 0 ) {
				CrearPrestamoDesdeBlockChain objCrearPrestamoDesdeBlockChain = new CrearPrestamoDesdeBlockChain();
				//entonces agrego al log para reprocesar
				objCrearPrestamoDesdeBlockChain.AgregaATablaPrestamosPendientesPorCargar( conn, objCrearEmprestitoDesdeBlockChain_Salida.P_OUT_PRESTAMOBLOCKCHAIN, "EMP", objCrearEmprestitoDesdeBlockChain_Salida.P_OUT_MENSAJES, fechaSistema );
			}
			
			return 1;
			
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return 0;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------
	// ANULA PRESTAMOS EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public Integer PrestamoOptimusAnulados( Connection conn, ProcesaLogBlockChain_Entrada in, List<String> lstPrestamosBC, List<String> lstPrestamistaBC, ProcesaLogBlockChain_Salida salida, String fechaSistema ) throws Exception {
		
		AnularPrestamoDesdeBlockChain objAnularPrestamoDesdeBlockChain 	= new AnularPrestamoDesdeBlockChain();
		SQL sql 														= new SQL(); 
		
		try {
			
			AnularPrestamoDesdeBlockChainEntrada objAnularPrestamoDesdeBlockChain_Entrada = new AnularPrestamoDesdeBlockChainEntrada();
				
			objAnularPrestamoDesdeBlockChain_Entrada.p_IdEmpresa				= in.p_IdEmpresa;
			objAnularPrestamoDesdeBlockChain_Entrada.p_IdUsuario				= in.p_IdUsuario;
			objAnularPrestamoDesdeBlockChain_Entrada.p_FoliosBlockChain 		= lstPrestamosBC;
			objAnularPrestamoDesdeBlockChain_Entrada.p_PrestamistasBlockChain	= lstPrestamistaBC; 
			objAnularPrestamoDesdeBlockChain_Entrada.p_FechaAnulacion	 		= fechaSistema;
			AnularPrestamoDesdeBlockChainSalida objAnularPrestamoDesdeBlockChainSalida = objAnularPrestamoDesdeBlockChain.logica(conn, objAnularPrestamoDesdeBlockChain_Entrada);
			if (objAnularPrestamoDesdeBlockChainSalida.P_OUT_ERRORESCONTADOR > 1) { 
				salida.P_OUT_MENSAJES.addAll(objAnularPrestamoDesdeBlockChainSalida.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = 1L;
			} else { 	
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_PRESTAMO_ANULADO_OK", new HashMap<>(), conn, in.p_IdEmpresa));

			}
			
			List<LogProcesoPrestamoBlockChain> logProcesoAnulacionesDesdeBlockChain = objAnularPrestamoDesdeBlockChainSalida.P_OUT_MENSAJES.stream()
					.filter(mensaje -> mensaje.tipo.equals(MensajeSalida.MENSAJE_ERROR))
					.map(mensaje -> {
						return new LogProcesoPrestamoBlockChain(in.p_IdEmpresa, mensaje.id, fechaSistema, mensaje.descripcion, "ProcesaLogBlockChain", "PrestamoOptimusAnulados", "JOB");
						})
					.collect(Collectors.toList());
			
			if (!logProcesoAnulacionesDesdeBlockChain.isEmpty()) {
				sql.batch(conn, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoAnulacionesDesdeBlockChain, LogProcesoPrestamoBlockChain.class);	
			}
			return 1;
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return 0;
		}
		
	}
	//-------------------------------------------------------------------------------------------
	// ANTICIPA PRESTAMOS EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public Integer PrestamoOptimusAnticipados( Connection conn, ProcesaLogBlockChain_Entrada in, List<PrestamoAnticipado> prestamosAnticipado, ProcesaLogBlockChain_Salida salida, String fechaSistema ) throws Exception 
	{
		
		AnticiparPrestamoDesdeBlockChain objAnticiparPrestamoDesdeBlockChain 	= new AnticiparPrestamoDesdeBlockChain();
		SQL sql 																= new SQL(); 
		
		try {
		
			AnticiparPrestamoDesdeBlockChain_Entrada objAnticiparPrestamoDesdeBlockChain_Entrada = new AnticiparPrestamoDesdeBlockChain_Entrada();
			
			objAnticiparPrestamoDesdeBlockChain_Entrada.p_IdEmpresa				= in.p_IdEmpresa;
			objAnticiparPrestamoDesdeBlockChain_Entrada.p_IdUsuario				= in.p_IdUsuario;
			objAnticiparPrestamoDesdeBlockChain_Entrada.p_PrestamosAnticipado	= prestamosAnticipado;
			objAnticiparPrestamoDesdeBlockChain_Entrada.p_FechaAnticipo			= fechaSistema;
			
			AnticiparPrestamoDesdeBlockChain_Salida objAnticiparPrestamoDesdeBlockChain_Salida = objAnticiparPrestamoDesdeBlockChain.logica(conn, objAnticiparPrestamoDesdeBlockChain_Entrada);
			
			List<LogProcesoPrestamoBlockChain> logProcesoAnulacionesDesdeBlockChain = objAnticiparPrestamoDesdeBlockChain_Salida.P_OUT_MENSAJES.stream()
					.filter(mensaje -> mensaje.tipo.equals(MensajeSalida.MENSAJE_ERROR))
					.map(mensaje -> {
						return new LogProcesoPrestamoBlockChain(in.p_IdEmpresa, mensaje.id, fechaSistema, mensaje.descripcion, "ProcesaLogBlockChain", "PrestamoOptimusAnticipados", "JOB");
						})
					.collect(Collectors.toList());
			
			if (!logProcesoAnulacionesDesdeBlockChain.isEmpty()) {
				sql.batch(conn, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoAnulacionesDesdeBlockChain, LogProcesoPrestamoBlockChain.class);	
			}
			
			return 1;
		
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return 0;
		}
		
	}
	
	public Integer PrestamistaAnticipado( Connection conn, ProcesaLogBlockChain_Entrada in, List<PrestamistaAnticipado> prestamistasAnticipados, ProcesaLogBlockChain_Salida salida, String fechaSistema ) {
		
		SQL sql	= new SQL(); 
		
		try {
			
			AnticiparPrestamistaDesdeBlockChain anticiparPrestamistaDesdeBlockChain 				= new AnticiparPrestamistaDesdeBlockChain();
			AnticiparPrestamistaDesdeBlockChainEntrada anticiparPrestamistaDesdeBlockChainEntrada 	= new AnticiparPrestamistaDesdeBlockChainEntrada();
			AnticiparPrestamistaDesdeBlockChainSalida anticiparPrestamistaDesdeBlockChainSalida		= new AnticiparPrestamistaDesdeBlockChainSalida();
			
			anticiparPrestamistaDesdeBlockChainEntrada.p_IdEmpresa 					= in.p_IdEmpresa;
			anticiparPrestamistaDesdeBlockChainEntrada.p_IdUsuario					= in.p_IdUsuario;
			anticiparPrestamistaDesdeBlockChainEntrada.p_IdModulo					= in.p_IdModulo;
			anticiparPrestamistaDesdeBlockChainEntrada.p_FechaAnticipo				= fechaSistema;
			anticiparPrestamistaDesdeBlockChainEntrada.p_PrestamistasAnticipados	= prestamistasAnticipados;
			
			anticiparPrestamistaDesdeBlockChainSalida	= anticiparPrestamistaDesdeBlockChain.logica( conn, anticiparPrestamistaDesdeBlockChainEntrada );
			
			List<LogProcesoPrestamoBlockChain> logProcesoAnulacionesDesdeBlockChain = anticiparPrestamistaDesdeBlockChainSalida.P_OUT_MENSAJES.stream()
					.filter(mensaje -> mensaje.tipo.equals(MensajeSalida.MENSAJE_ERROR))
					.map(mensaje -> {
						return new LogProcesoPrestamoBlockChain(in.p_IdEmpresa, mensaje.id, fechaSistema, mensaje.descripcion, "ProcesaLogBlockChain", "PrestamistaAnticipado", "JOB");
						})
					.collect(Collectors.toList());
			
			if (!logProcesoAnulacionesDesdeBlockChain.isEmpty()) {
				sql.batch(conn, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoAnulacionesDesdeBlockChain, LogProcesoPrestamoBlockChain.class);	
			}
			
			return 1;
			
		} catch ( Exception e ) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return 0;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------
	// AGREGA PRESTAMOS VENCIDOS EN HISTORICO EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public Integer PrestamoOptimusVencimientos( Connection conn, ProcesaLogBlockChain_Entrada in, List<String> lstPrestamosBC, ProcesaLogBlockChain_Salida salida ) throws Exception {
			
		SQL sql = new SQL();
			
			try {
				
				List<VencimientoHisPrestamoBlockChain> listVencimientoHisPrestamoBlockChain = new ArrayList<>();
				
				listVencimientoHisPrestamoBlockChain = lstPrestamosBC.stream()
						.map( folio_blockchain -> new VencimientoHisPrestamoBlockChain( ConstantesBCK.ESTADO_VENCIMIENTO, folio_blockchain )  )
						.collect( Collectors.toList() );
				
				if ( listVencimientoHisPrestamoBlockChain.size() > 0 ) {
					sql.batch(conn, SQL_UPDATE_VENCIMIENTOS_HISTORICO_PROCESO_BLOCKCHAIN, listVencimientoHisPrestamoBlockChain, VencimientoHisPrestamoBlockChain.class);
				}
				
				return 1;
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return 0;
			}
			
	}

	public Integer PrestamoPrestamistasDesasignados( Connection conn, ProcesaLogBlockChain_Entrada in, List<PrestamoPrestamistaDesasignado> prestamoPrestamistasDesasignados, ProcesaLogBlockChain_Salida salida, String fechaSistema ) throws Exception {
		
		SQL sql		= new SQL(); 
		
		try 
		{
		
			AnularEmprestitoDesdeBlockChain anularEmprestitoDesdeBlockChain 				= new AnularEmprestitoDesdeBlockChain();
			AnularEmprestitoDesdeBlockChainEntrada anularEmprestitoDesdeBlockChainEntrada 	= new AnularEmprestitoDesdeBlockChainEntrada();
			AnularEmprestitoDesdeBlockChainSalida anularEmprestitoDesdeBlockChainSalida 	= new AnularEmprestitoDesdeBlockChainSalida();
			
			anularEmprestitoDesdeBlockChainEntrada.p_IdEmpresa 					= in.p_IdEmpresa;
			anularEmprestitoDesdeBlockChainEntrada.p_IdUsuario					= in.p_IdUsuario;
			anularEmprestitoDesdeBlockChainEntrada.p_FechaAnulacion				= fechaSistema;
			anularEmprestitoDesdeBlockChainEntrada.p_PrestamistasDesasignado	= prestamoPrestamistasDesasignados;
			
			anularEmprestitoDesdeBlockChainSalida = anularEmprestitoDesdeBlockChain.logica( conn, anularEmprestitoDesdeBlockChainEntrada );
			
			List<LogProcesoPrestamoBlockChain> logProcesoAnulacionesDesdeBlockChain = anularEmprestitoDesdeBlockChainSalida.P_OUT_MENSAJES.stream()
					.filter( mensaje -> mensaje.tipo.equals( MensajeSalida.MENSAJE_ERROR ) )
					.map( mensaje -> {
						return new LogProcesoPrestamoBlockChain( in.p_IdEmpresa, mensaje.id, fechaSistema, mensaje.descripcion, "ProcesaLogBlockChain", "PrestamoPrestamistasDesasignados", "JOB" );
						} )
					.collect( Collectors.toList() );
			
			if ( !logProcesoAnulacionesDesdeBlockChain.isEmpty() ) {
				sql.batch( conn, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoAnulacionesDesdeBlockChain, LogProcesoPrestamoBlockChain.class );	
			}
			
			return 1;
		
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			return 0;
		}
		
	}
	
	public Integer PrestamosModificados( Connection conn, ProcesaLogBlockChain_Entrada in, List<PrestamoModificado> prestamosModificados, ProcesaLogBlockChain_Salida salida, String fechaSistema ) {
		
		SQL sql		= new SQL(); 
		
		try 
		{
		
			ModificarPrestamoDesdeBlockChain modificarPrestamoDesdeBlockChain 					= new ModificarPrestamoDesdeBlockChain();
			ModificarPrestamoDesdeBlockChainEntrada modificarPrestamoDesdeBlockChainEntrada 	= new ModificarPrestamoDesdeBlockChainEntrada();
			ModificarPrestamoDesdeBlockChainSalida modificarPrestamoDesdeBlockChainSalida 		= new ModificarPrestamoDesdeBlockChainSalida();
			
			modificarPrestamoDesdeBlockChainEntrada.p_IdEmpresa				= in.p_IdEmpresa;
			modificarPrestamoDesdeBlockChainEntrada.p_IdUsuario				= in.p_IdUsuario;
			modificarPrestamoDesdeBlockChainEntrada.p_FechaOperacion		= fechaSistema;
			modificarPrestamoDesdeBlockChainEntrada.p_PrestamosModificado	= prestamosModificados;
			
			modificarPrestamoDesdeBlockChainSalida = modificarPrestamoDesdeBlockChain.logica( conn, modificarPrestamoDesdeBlockChainEntrada );
			
			
			List<LogProcesoPrestamoBlockChain> logProcesoAnulacionesDesdeBlockChain = modificarPrestamoDesdeBlockChainSalida.P_OUT_MENSAJES.stream()
					.filter( mensaje -> mensaje.tipo.equals( MensajeSalida.MENSAJE_ERROR ) )
					.map( mensaje -> {
						return new LogProcesoPrestamoBlockChain( in.p_IdEmpresa, mensaje.id, fechaSistema, mensaje.descripcion, "ProcesaLogBlockChain", "PrestamosModificados", "JOB" );
						} )
					.collect( Collectors.toList() );
			
			if ( !logProcesoAnulacionesDesdeBlockChain.isEmpty() ) {
				sql.batch( conn, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoAnulacionesDesdeBlockChain, LogProcesoPrestamoBlockChain.class );	
			}
			
			return 1;
		
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR += 1L;
			return 0;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ProcesaLogBlockChain_Entrada in = gson.fromJson(jin.toString(), ProcesaLogBlockChain_Entrada.class);
		ProcesaLogBlockChain_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
			
			String json = "{\"p_IdEmpresa\":1, \"p_IdUsuario\":9}";
			ProcesaLogBlockChain_Entrada Objin = gson.fromJson(json, ProcesaLogBlockChain_Entrada.class);
			ProcesaLogBlockChain demo = new ProcesaLogBlockChain();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				
 				ProcesaLogBlockChain_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				//con.rollback();
 				
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
}

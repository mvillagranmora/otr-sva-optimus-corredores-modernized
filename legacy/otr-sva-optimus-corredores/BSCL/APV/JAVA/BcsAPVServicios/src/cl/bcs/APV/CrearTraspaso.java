package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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


public class CrearTraspaso implements JavaCall   {

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_CREAR_TRASPASO			 = loadResourceToString("cl/bcs/APV/sql/Traspaso_Insert.sql");
	public String SQL_CREAR_DETALLE_ORIGEN_TRASPASO = loadResourceToString("cl/bcs/APV/sql/DetalleOrigenTraspaso_Insert.sql");
	

	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class Traspaso{
		public Long 	IdTraspaso;
		public Long 	IdContratoSAAPV;
		public Long 	IdEmpresa;
		public String	TipoMovimiento;
		public String 	RazonRetiro;
		public String 	FechaIngreso;
		public String 	FechaEgreso;
		public String 	FechaModificacion;
		public String 	CodigoTipoTraspaso;
		public String 	Descripcion;
		public Long 	IdCliente;
		public Long 	IdPersona;
		public String 	CodigoTipoAhorro;	
		public String 	NombreEntidad;
		public String 	DocumentoEntidad;
		public String	Canal;
		public String 	IdEstado;
		public String  CierreSolicitud;
		
		public Traspaso() {
			this.Canal = "OPT";
			this.CierreSolicitud="0001-01-01";
		}
	};
		
	public static class DetalleOrigenTraspaso{
		public Long   	IdDetalleOrigenTraspaso;
		public Long   	IdTraspaso;
		public String 	CodigoTipoAhorro;
		public Long 	IdEntidadRecaudadora;
		public String 	NombreEntidad;
		public String 	DocumentoEntidad;
		public String 	Fondo;
		public double 	MontoTraspaso;
		public double 	UFTraspaso;
		public double 	PorcentajeTraspaso;
		public String 	IdEstado;
		public Long 	IdInstrumento;			
		public Long 	IdPortafolio;					
		public String 	CodigoRegimenTributario;					
		
		public Long IdInterno;
	    public DetalleOrigenTraspaso() {
	    	IdInterno = 0L;
	    }
	};
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	Traspaso					p_Traspaso;
		public	List<DetalleOrigenTraspaso>	p_DetalleOrigenTraspaso;
		
		public ParametrosEntradaServicio(){
			p_DetalleOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_IDTRASPASO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Metodos Basicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		Long count = 1L;
		Traspaso traspaso = in.p_Traspaso;
		
		try{
			List<Long> IdTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdTraspaso", 1L);				
			Traspaso tra = new Traspaso();
			tra.IdTraspaso = IdTraspaso.get(0);
			tra.IdContratoSAAPV = traspaso.IdContratoSAAPV;
			tra.IdEmpresa = in.p_IdEmpresa;
			tra.TipoMovimiento = "EGRESO";
			tra.RazonRetiro = traspaso.RazonRetiro;
			tra.FechaIngreso = fechaSistema;
			tra.FechaEgreso = "0001-01-01";
			tra.FechaModificacion = "0001-01-01";
			tra.CodigoTipoTraspaso = traspaso.CodigoTipoTraspaso;
			tra.Descripcion = traspaso.Descripcion;
			tra.IdCliente = traspaso.IdCliente;
			tra.IdPersona = traspaso.IdPersona;
			tra.CodigoTipoAhorro = traspaso.CodigoTipoAhorro;	
			tra.NombreEntidad = traspaso.NombreEntidad;
			tra.DocumentoEntidad = traspaso.DocumentoEntidad;
			tra.IdEstado = "PDI";
			tra.Canal = traspaso.Canal;
			tra.CierreSolicitud = "0001-01-01";
			List<Traspaso> lstTraspaso = new ArrayList<Traspaso>();
			lstTraspaso.add(tra);
			int[] resultado = sql.batch(con, SQL_CREAR_TRASPASO,lstTraspaso, Traspaso.class);
			if(resultado != null && resultado.length > 0 && resultado[0] == 1){
				MensajeSalida msg = new MensajeSalida();
			    msg.id = count;
			    msg.tipo = "INFORMACION";
			    msg.codigo = "CREACION_TRASPASO_OK";
			    msg.descripcion = "Creacion del traspaso: " + IdTraspaso.get(0).toString() + " exitosa.";
				salida.P_OUT_MENSAJES.add(msg);
				
				DetalleOrigenTraspaso dtOrigenTraspaso = new DetalleOrigenTraspaso();
				List<DetalleOrigenTraspaso> lstOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
				for(DetalleOrigenTraspaso row : in.p_DetalleOrigenTraspaso){
					count++;

					List<Long> IdDetalleOrigenTraspaso = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::TRA.IdDetalleOrigenTraspaso", 1L);
					dtOrigenTraspaso = new DetalleOrigenTraspaso();
					dtOrigenTraspaso.IdDetalleOrigenTraspaso = IdDetalleOrigenTraspaso.get(0);
					dtOrigenTraspaso.IdTraspaso = IdTraspaso.get(0);
					dtOrigenTraspaso.CodigoTipoAhorro = row.CodigoTipoAhorro;
					dtOrigenTraspaso.IdEntidadRecaudadora = row.IdEntidadRecaudadora;
					dtOrigenTraspaso.NombreEntidad = row.NombreEntidad;
					dtOrigenTraspaso.DocumentoEntidad = row.DocumentoEntidad;
					dtOrigenTraspaso.Fondo = row.Fondo;
					dtOrigenTraspaso.MontoTraspaso = row.MontoTraspaso;
					dtOrigenTraspaso.UFTraspaso = row.UFTraspaso;
					dtOrigenTraspaso.PorcentajeTraspaso = row.PorcentajeTraspaso;
					dtOrigenTraspaso.IdEstado = "VIG";
					dtOrigenTraspaso.IdInstrumento = 0L;
					dtOrigenTraspaso.IdPortafolio = 0L;
					dtOrigenTraspaso.CodigoRegimenTributario = "";

					lstOrigenTraspaso.add(dtOrigenTraspaso);
					
					if(lstOrigenTraspaso.size() > 0 ) {
						int[] result = sql.batch(con, SQL_CREAR_DETALLE_ORIGEN_TRASPASO,lstOrigenTraspaso, DetalleOrigenTraspaso.class);
						if(result != null && result.length > 0 && resultado[0] == 1){
							msg = new MensajeSalida();
						    msg.id = count;
						    msg.tipo = "INFORMACION";
						    msg.codigo = "CREACION_ORIGEN_TRASPASO_OK";
						    msg.descripcion = "Creacion del Origen traspaso: " + IdDetalleOrigenTraspaso.get(0).toString() + " exitosa.";
						    salida.P_OUT_MENSAJES.add(msg);
						    salida.P_OUT_IDTRASPASO = IdTraspaso.get(0);
						}	
					}
				}
			}
		
			return salida;
			
		} catch (Exception er){
			er.printStackTrace();
			con.rollback();
			
			MensajeSalida msg = new MensajeSalida();
			msg.id = count;//1L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR";
			msg.descripcion = er.getMessage();
			salida.P_OUT_MENSAJES.add(msg);
			
			return salida;
		}
	}
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				//SimpleDateFormat FormatoFechaSistema = new SimpleDateFormat("yyyy-MM-dd hh:mmm:ss.SSS");
				//Long time = System.currentTimeMillis();
				//java.sql.Date FechaSistema = Fecha.toDate("1978-01-01T03:00:00.000Z");
				//Timestamp Fechatime	= new java.sql.Timestamp(FechaSistema.getTime());

				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"I\",\"p_Parametros\":[{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":66,\"IdPlan\":1,\"NombrePlan\":\"ABIERTO\",\"FechaSuscripcion\":\"2018-02-21\",\"FechaCreacion\":\"2018-02-22\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"Folio\":\"\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"IdPersona\":null,\"IdCliente\":null,\"IdEmpleador\":0,\"IdEnrolamiento\":null,\"IdPortafolio\":null,\"NombreCliente\":\"JAIME ALEXIS\",\"ApellidoPaternoCliente\":\"ARAVENA\",\"ApellidoMaternoCliente\":\"GONZALEZ\",\"DocumentoIdentidadCliente\":\"113459198-6\",\"FechaNacimientoCliente\":\"1979-10-06\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"M\",\"IdPaisResidenciaCliente\":44,\"IdComunaCliente\":null,\"DireccionCliente\":null,\"NumeroDireccionCliente\":null,\"IdTipoCargoPersona\":null,\"TelefonoCliente\":null,\"TelefonoComercialCliente\":null,\"TelefonoCelularCliente\":null,\"EmailCliente\":\"PRUEBA_CORREO@GMAIL.COM\",\"RazonSocialEmpleador\":\"SAVORY\",\"DocumentoIdentidadEmpleador\":\"80537000-5\",\"IdPaisEmpleador\":0,\"DireccionComercialEmpleador\":null,\"NumeroDireccionEmpleador\":null,\"IdCiudadEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"562239968900\",\"NombreEncargado\":null,\"EmailEncargado\":null,\"TelefonoEncargado\":null,\"IdMedioRecaudacionTrabajador\":1,\"IdEntidadRecaudadora\":0,\"CodigoPrevision\":0,\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":1,\"Institucion\":0,\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":7312,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[],\"p_ParametrosModificados\":0}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"I\",\"p_Parametros\":[{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"\",\"IdPlan\":1,\"NombrePlan\":\"A. ABIERTO\",\"FechaSuscripcion\":\"0001-01-01\",\"FechaCreacion\":\"2018-04-25\",\"FechaAprobacion\":\"0001-01-01\",\"FechaNotificacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01\",\"CodigoTipoSolicitud\":\"TRA\",\"Folio\":\"\",\"IdCategoriaInversionista\":\"BALANCEADO\",\"IdPerfilComercial\":\"BALANCEADO APV INV. ACTIVA\",\"IdPersona\":0,\"IdCliente\":0,\"IdEmpleador\":0,\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"INGRESO CLIENTE NUEVO TRASPASO APV 2\",\"ApellidoPaternoCliente\":\"A\",\"ApellidoMaternoCliente\":\"S\",\"DocumentoIdentidadCliente\":\"10301339-9 \",\"FechaNacimientoCliente\":\"2004-06-09\",\"EstadoCivil\":\"SO\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":2931,\"IdComunaCliente\":327,\"DireccionCliente\":\"ASDASDASD\",\"NumeroDireccionCliente\":\"123\",\"IdTipoCargoPersona\":0,\"TelefonoFijo\":\"\",\"TelefonoMovil\":\"56965779493\",\"TelefonoLaboral\":\"\",\"EmailCliente\":\"ASDASD@GMAIL.COM\",\"RazonSocialEmpleador\":\"\",\"DocumentoIdentidadEmpleador\":\"\",\"IdPaisEmpleador\":0,\"DireccionComercialEmpleador\":\"\",\"NumeroDireccionEmpleador\":0,\"IdCiudadEmpleador\":0,\"IdComunaEmpleador\":0,\"TelefonoEmpleador\":\"\",\"NombreEncargado\":\"\",\"EmailEncargado\":\"\",\"TelefonoEncargado\":\"\",\"IdMedioRecaudacionTrabajador\":0,\"IdEntidadRecaudadora\":0,\"CodigoPrevision\":0,\"Pensionado\":\"NO\",\"Trabajador\":\"\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"DiaDescuento\":0,\"Institucion\":0,\"ComisionTransferencia\":0,\"TipoMovimiento\":\"EGRESO\",\"RazonRetiro\":\"TRA\",\"NombreEntidadOrigen\":\"AFP PLANVITAL\",\"RutEntidadOrigen\":\"98001200-K\",\"IdPersonaOrigen\":1765331,\"TipoDeAhorro\":\"\",\"FondoDeOrigen\":\"\",\"TipoTraspaso\":\"TOT\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"Descripcion\":\"\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":11126,\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\"}],\"p_DistribucionInversion\":[],\"p_ParametrosModificados\":0,\"p_DetalleOrigenTraspaso\":[{\"IdDetalleOrigenTraspaso\":0,\"IdTraspaso\":0,\"IdEmpresa\":1,\"CodigoTipoAhorro\":\"APV\",\"IdEntidadRecaudadora\":5,\"NombreEntidad\":\"AFP PLANVITAL\",\"DocumentoEntidad\":\"98001200-K\",\"Fondo\":\"A\",\"MontoTraspaso\":0,\"UFTraspaso\":0,\"PorcentajeTraspaso\":0,\"IdEstado\":\"VIG\"}]}";
				ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				CrearTraspaso demo = new CrearTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
	 				con.commit();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					SQL.exec(con, "DROP TABLE #RegistroAtributo");
					con.rollback();
				}
	 			
		} catch (Exception e) {

			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
}
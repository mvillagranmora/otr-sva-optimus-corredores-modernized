package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspaso;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspasoDestino;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspasoOrigen;




public class CrearTraspaso implements JavaCall   {

	private final Log log = new Log("FM.CrearTraspaso");
	
	public String SQL_TRASPASO_INSERT			 	 = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Insert.sql");
	public String SQL_TRASPASO_DETALLEORIGEN_INSERT  = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleOrigen.sql");
	public String SQL_TRASPASO_DETALLEDESTINO_INSERT = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleDestino.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");

	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class InTraspaso{
		public Long 	IdInterno;
		public Long 	IdCliente;
		public Long 	IdPortafolio;
		public String	IdEmisor;
		public String	IdMotivoTraspaso;
		public String	IdTipoMovimientoTraspaso;
		public String 	IdTipoTraspaso;
		public String 	Glosa;
		public String 	FechaHoraSuscripcion;
		public Long 	IdDivisionPais;
		public Long 	FolioTraspaso;
		public BigDecimal 	Monto;
		public Long 	FolioMLT;
		public Long 	FolioCSC;
		public Long 	IdEmpleadoEjecutivo;
		public Long		IdEntidadRecaudadora;
		
		
	    private String  EstadoProceso;
	    
	    public InTraspaso() {
	    	this.IdEmisor = "";
	    	this.FolioMLT = 0L;
	    	this.FolioCSC = 0L;
	    	this.IdEmpleadoEjecutivo = 0L;
	    	this.IdEntidadRecaudadora = 0L;
	    	this.EstadoProceso = "";
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	

	
	public static class InTraspasoDetalleOrigen{
		public String 	IdSerieFondo;
		public double 	Cantidad;
		public String 	IdRegimenTributario="";	
		public Long IdInterno;
	    public InTraspasoDetalleOrigen() {
	    	IdInterno = 0L;
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    
	};
	public static class InTraspasoDetalleDestino{

		public String 	IdSerieFondo;
		public double 	Porcentaje;
		public String	IdRegimenFondoMutuo;	
		public Long IdInterno;
		public String RunFFMM;
		public String NombreFFMM;
	    public InTraspasoDetalleDestino() {
	    	IdInterno = 0L;
	    	RunFFMM = "";
			NombreFFMM="";
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	};
	
	//  ENTRADA
	public static class EntradaCrearTraspaso {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InTraspaso>					p_Traspaso;
		public	List<InTraspasoDetalleOrigen>		p_TraspasoDetalleOrigen;
		public	List<InTraspasoDetalleDestino>		p_TraspasoDetalleDestino;
		
		
		public EntradaCrearTraspaso(){
			p_Traspaso 					= new ArrayList<InTraspaso>();
			p_TraspasoDetalleOrigen 	= new ArrayList<InTraspasoDetalleOrigen>();
			p_TraspasoDetalleDestino 	= new ArrayList<InTraspasoDetalleDestino>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SALIDA

	public static class SalidaCrearTraspaso{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearTraspaso(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearTraspaso jin = gson.fromJson(in.toString(), EntradaCrearTraspaso.class);
		SalidaCrearTraspaso out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaCrearTraspaso logica(Connection con, EntradaCrearTraspaso entrada) throws Exception{
	
		log.write("INICIO::CrearTraspaso");
		SalidaCrearTraspaso out = new SalidaCrearTraspaso();
		SQL sql=new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}
		
		
		if(entrada.p_Traspaso.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InTraspaso mov: entrada.p_Traspaso) {
			if(mov.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdInterno);
			}
		}
		
		
		List<InTraspaso> solicitudProcesar = new ArrayList<InTraspaso>();
		List<InTraspaso> solicitudInvalida = new ArrayList<InTraspaso>();
		
		//VALIDAMOS CUSTODIA
		ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
		EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
		entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
		entradaFlujo.p_IdModulo = entrada.p_IdModulo;
		entradaFlujo.p_IdPais = idPaisEmpresa;
		entradaFlujo.p_FechaSaldo = fechaSistema;
		entradaFlujo.p_DiasProyeccion = 0;
		List<Long> fPortafolio = new ArrayList<Long>();
		List<Long> fInstrumento = new ArrayList<Long>();
		List<String> fRegimen = new ArrayList<String>();
		
		List<String> series = new ArrayList<String>();
		for(InTraspasoDetalleOrigen DetalleOrigen : entrada.p_TraspasoDetalleOrigen){
			if(!series.contains(DetalleOrigen.IdSerieFondo)) series.add(DetalleOrigen.IdSerieFondo);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		params.clear();
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		
		
		//validaciones del Traspaso
		for(InTraspaso solicitud : entrada.p_Traspaso){
			solicitud.EstadoProceso = _c.EstadoPendiente;
			
			
			if(_c.EstadoPendiente.equalsIgnoreCase(solicitud.EstadoProceso)){
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(entrada.p_TraspasoDetalleOrigen.get(0).IdSerieFondo);
				if(!fPortafolio.contains(solicitud.IdPortafolio)){
					fPortafolio.add(solicitud.IdPortafolio);
					ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
					cartera.IdCliente = solicitud.IdCliente;
					cartera.IdPortafolio = solicitud.IdPortafolio;
					entradaFlujo.p_Carteras.add(cartera);
				}
				if(!fInstrumento.contains(serie.IdInstrumento) || (fInstrumento.contains(serie.IdInstrumento) &&  (!fRegimen.contains(entrada.p_TraspasoDetalleOrigen.get(0).IdRegimenTributario)))){
					fInstrumento.add(serie.IdInstrumento);
					fRegimen.add(entrada.p_TraspasoDetalleOrigen.get(0).IdRegimenTributario);
					ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
					instrumento.IdInstrumento = serie.IdInstrumento;
					instrumento.IdRegimenFondoMutuo = entrada.p_TraspasoDetalleOrigen.get(0).IdRegimenTributario;
					entradaFlujo.p_Instrumentos.add(instrumento);
				}
			}
	
			SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = flujo.logica(con, entradaFlujo);
			
			for(InTraspasoDetalleOrigen DetalleOrigen2 : entrada.p_TraspasoDetalleOrigen){
				if(_c.EstadoRechazado.equalsIgnoreCase(solicitud.EstadoProceso)) continue;
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(DetalleOrigen2.IdSerieFondo);
				
				if(montoEsDecimalParaMonedaCLP(solicitud.Monto,serie.IdMoneda)) {
					HashMap<String,String> parametrosError = new HashMap<String,String>(1);
					parametrosError.put("IdSerieFondo", serie.IdSerieFondo);
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_SERIEFONDO_SOLO_PERMITE_MONTO_ENTERO",parametrosError,con,entrada.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					break;
				}
				
				BigDecimal custodia = BigDecimal.ZERO;
				
				for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado salida : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
					
					if(salida.IdCliente.equals(solicitud.IdCliente) && salida.IdPortafolio.equals(solicitud.IdPortafolio) && salida.IdInstrumento.equals(serie.IdInstrumento) && salida.IdRegimenTributario.equals(DetalleOrigen2.IdRegimenTributario)){
						custodia = salida.CantidadDisponible;
						break;
					}
				}
				
				//VALIDAMOS CUSTODIA, SI NO HAY ERROR Y SALIMOS.
				BigDecimal CantidadDisponible = BigDecimal.valueOf(DetalleOrigen2.Cantidad);
				
				if(CantidadDisponible.compareTo(custodia) == 1){
					solicitud.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
					parametrosMensaje.put("Nemotecnico", DetalleOrigen2.IdSerieFondo);
					parametrosMensaje.put("CantidadRetiro", CantidadDisponible.toPlainString());
					parametrosMensaje.put("CantidadCustodia",custodia.toPlainString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FM_CLIENTE_SIN_CUSTODIA",parametrosMensaje,con,entrada.p_IdEmpresa));
					continue;
				}
			}
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				return out;
			}
			
		
			//if(_c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)){
				int contOrigen=0;
				for(InTraspasoDetalleOrigen DetalleOrigen : entrada.p_TraspasoDetalleOrigen){
					if ((long)DetalleOrigen.IdInterno==(long)solicitud.IdInterno)
					{
						contOrigen++;
					}
					
				}
				if(contOrigen == 0){
					solicitud.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_TRASPASO_SIN_ORIGEN",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				
			//}
			
			int contDestino=0;
			for(InTraspasoDetalleDestino DetalleDestino : entrada.p_TraspasoDetalleDestino){
				if ((long)DetalleDestino.IdInterno==(long)solicitud.IdInterno)
				{
					contDestino++;
				}
				
			}
			if(contDestino == 0  && _c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)){
				solicitud.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_TRASPASO_SIN_DESTINO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				continue;
			}
		   
			if( !(_c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)) &&  !(_c.TraspasoMovimientoIngreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)) ){
				solicitud.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_TIPO_TRASPASO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				continue;
			}
		
			if (_c.EstadoRechazado.equalsIgnoreCase(solicitud.EstadoProceso)){
				solicitudInvalida.add(solicitud);
			}else{
				
				solicitudProcesar.add(solicitud);	
			};
					
		}	//fin Validaiones
		
		
		//INSERTAMOS TRASPASOS SIN ERRORES
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::Traspaso", new Long(solicitudProcesar.size()));
		
		List<RegistroTraspaso> crearTraspaso = new ArrayList<RegistroTraspaso>();
		List<RegistroTraspasoOrigen> crearTraspasoOrigen = new ArrayList<RegistroTraspasoOrigen>();
		List<RegistroTraspasoDestino> crearTraspasoDestino = new ArrayList<RegistroTraspasoDestino>();
		Calendar calendario = Calendar.getInstance();
		
		int i = 0;
		for(InTraspaso solicitud : solicitudProcesar){
			//TRASPASO
			RegistroTraspaso reg = new RegistroTraspaso();
			reg.IdTraspaso= secuencias.get(i);

			if (fechaSistema.equals(solicitud.FechaHoraSuscripcion.substring(0,10))) {
				reg.FechaHoraSuscripcion= solicitud.FechaHoraSuscripcion.substring(0,10) + " " +calendario.get(Calendar.HOUR_OF_DAY) + ":" +calendario.get(Calendar.MINUTE) + ":"+calendario.get(Calendar.SECOND);
				
			}else {
				reg.FechaHoraSuscripcion= solicitud.FechaHoraSuscripcion.substring(0,10);
			}
			
			reg.IdDivisionPais=solicitud.IdDivisionPais;
			reg.FechaIngreso= Fecha.toDate(fechaSistema);
			reg.FolioTraspaso=solicitud.FolioTraspaso;
			reg.Glosa=solicitud.Glosa;
			reg.IdCliente=solicitud.IdCliente;
			reg.IdEmisor=solicitud.IdEmisor;
			reg.IdEmpresa=entrada.p_IdEmpresa;
			reg.IdEstado=_c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)? _c.TraspasoEstadoIngresado :_c.TraspasoEstadoPendiente;
			//reg.IdEstado=_c.TraspasoEstadoIngresado;
			reg.IdInterno=solicitud.IdInterno;
			reg.IdModulo=entrada.p_IdModulo;
			reg.IdMotivoTraspaso=solicitud.IdMotivoTraspaso;
			reg.IdTipoMovimientoTraspaso=	solicitud.IdTipoMovimientoTraspaso;
			reg.IdPortafolio=solicitud.IdPortafolio;
			reg.IdTipoTraspaso=solicitud.IdTipoTraspaso;
			reg.IdUsuario=entrada.p_IdUsuario;
			reg.Monto=solicitud.Monto;
			reg.FolioCSC=solicitud.FolioCSC;
			reg.FolioMLT=solicitud.FolioMLT;
			reg.IdEmpleadoEjecutivo=solicitud.IdEmpleadoEjecutivo;
			reg.IdEntidadRecaudadora = solicitud.IdEntidadRecaudadora;
			
			
			crearTraspaso.add(reg);	//Incluir registro Traspaso
			//if(_c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)){
				
				for(InTraspasoDetalleOrigen origen : entrada.p_TraspasoDetalleOrigen)
				{
					//ORIGEN
					RegistroTraspasoOrigen regOrigen =new RegistroTraspasoOrigen();
					if (origen.IdInterno==reg.IdInterno){
						regOrigen.Cantidad=origen.Cantidad;
						regOrigen.IdRegimenTributario=origen.IdRegimenTributario;
						regOrigen.FechaIngreso=reg.FechaIngreso;
						regOrigen.IdEstado=_c.TraspasoDetalleActivo;
						regOrigen.IdInterno=origen.IdInterno;
						regOrigen.IdSerieFondo=origen.IdSerieFondo;
						regOrigen.IdTraspaso=reg.IdTraspaso;
						crearTraspasoOrigen.add(regOrigen);	//Incluir registro Traspaso Origen
					}
					
				}
			//}
			
			for(InTraspasoDetalleDestino destino : entrada.p_TraspasoDetalleDestino)
			{
				//DESTINO
				RegistroTraspasoDestino regDestino =new RegistroTraspasoDestino();
				if (destino.IdInterno==reg.IdInterno){
					regDestino.FechaIngreso=reg.FechaIngreso;
					regDestino.IdEstado=_c.TraspasoDetalleActivo;
					regDestino.IdInterno=destino.IdInterno;
					regDestino.IdSerieFondo=destino.IdSerieFondo==null?"":destino.IdSerieFondo;
					regDestino.IdTraspaso=reg.IdTraspaso;
					regDestino.Porcentaje=destino.Porcentaje;
					regDestino.IdRegimenFondoMutuo=destino.IdRegimenFondoMutuo;
					regDestino.RunFFMM= destino.RunFFMM;
					regDestino.NombreFFMM= destino.NombreFFMM;
					crearTraspasoDestino.add(regDestino);	//Incluir registro Traspaso Origen
				}
				
			}
			
			i++;
		};
		
		if(crearTraspaso.size() > 0){
			sql.batch(con, SQL_TRASPASO_INSERT, crearTraspaso, RegistroTraspaso.class);
			
		}
		if(crearTraspasoOrigen.size() > 0){
			sql.batch(con, SQL_TRASPASO_DETALLEORIGEN_INSERT, crearTraspasoOrigen, RegistroTraspasoOrigen.class);
		}
		if(crearTraspasoDestino.size() > 0){
			sql.batch(con, SQL_TRASPASO_DETALLEDESTINO_INSERT, crearTraspasoDestino, RegistroTraspasoDestino.class);
		}
		
		log.write("FIN::CrearTraspaso");
		return out;
	}
	
	private boolean montoEsDecimalParaMonedaCLP(BigDecimal monto, String IdMoneda) {
		log.write("INICIO::CrearTraspaso.montoEsDecimalParaMonedaCLP");
		BigDecimal montoFloor = monto.setScale(0, RoundingMode.FLOOR);
		
		log.write("FIN::CrearTraspaso.montoEsDecimalParaMonedaCLP");
		return (monto.compareTo(montoFloor) == 1) && (_c.Pesos).equals(IdMoneda);
	}

	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_Traspaso\":[{\"IdInterno\":1,\"IdCliente\":1423325,\"IdPortafolio\":1453512,\"IdMotivoTraspaso\":\"BAJCOM\",\"IdTipoMovimientoTraspaso\":\"TRAEGR\",\"IdTipoTraspaso\":\"PARCIAL\",\"Glosa\":\"\",\"FechaHoraSuscripcion\":\"2021-12-13T03:00:00.000Z\",\"IdDivisionPais\":0,\"FolioTraspaso\":0,\"Monto\":44408}],\"p_TraspasoDetalleOrigen\":[{\"IdInterno\":1,\"IdSerieFondo\":\"CUSTODIACHILE-A\",\"IdRegimenTributario\":\"18Q\",\"Cantidad\":61}],\"p_TraspasoDetalleDestino\":[{\"IdInterno\":1,\"RunFFMM\":\"9994-5\",\"NombreFFMM\":\"AMERIS LIQUIDEZ FONDO MUTUO\",\"Porcentaje\":100}]}";
			

				EntradaCrearTraspaso Objin_json = gson.fromJson(json, EntradaCrearTraspaso.class);
				
				CrearTraspaso demo = new CrearTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				SalidaCrearTraspaso ObjOut = demo.logica(con, Objin_json);
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					con.rollback();
				}
	 			
		} catch (Exception e) {

			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
}
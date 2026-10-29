package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;


import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspaso;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspasoDestino;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspasoOrigen;




public class ModificarTraspaso implements JavaCall   {

	private final Log log = new Log("FM.ModificarTraspaso");
	
	public String SQL_TRASPASO_UPDATE			 	 = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Update.sql");
	public String SQL_TRASPASO_DETALLEORIGEN_UPDATE  = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_UpdateDetalleOrigen.sql");
	public String SQL_TRASPASO_DETALLEDESTINO_UPDATE = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_UpdateDetalleDestino.sql");
	public String SQL_TRASPASO_DETALLEORIGEN_INSERT  = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleOrigen.sql");
	public String SQL_TRASPASO_DETALLEDESTINO_INSERT = loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_InsertDetalleDestino.sql");
	

	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class InTraspaso{
		public Long 	IdTraspaso;
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
		public Long 	IdEntidadRecaudadora;
		
		public String	IdEstado ="";
		
		
		
	    private String  EstadoProceso ="";
	    
	    public InTraspaso() {
	    	this.IdEmisor = "";
	    	this.FolioMLT = 0L;
	    	this.FolioCSC = 0L;
	    	this.IdEmpleadoEjecutivo = 0L;
	    	this.IdEntidadRecaudadora = 0L;
	    	this.IdEstado ="";
	    	this.EstadoProceso ="";
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	

	
	public static class InTraspasoDetalleOrigen{
		public String 	IdSerieFondo;
		public double 	Cantidad;
		public String 	IdRegimenTributario="";		
		public Long IdTraspaso;
		public Long IdDetalleTraspaso;
	    public InTraspasoDetalleOrigen() {
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    
	};
	public static class InTraspasoDetalleDestino{

		public String 	IdSerieFondo;
		public double 	Porcentaje;
		public String	IdRegimenFondoMutuo;	
		public Long IdTraspaso;
		public Long IdDetalleTraspaso;
		public String RunFFMM;
		public String NombreFFMM;
	    public InTraspasoDetalleDestino() {
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
	    	RunFFMM="";
			NombreFFMM="";
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	};
	
	//  ENTRADA
	public static class EntradaModificarTraspaso {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InTraspaso>					p_Traspaso;
		public	List<InTraspasoDetalleOrigen>		p_TraspasoDetalleOrigen;
		public	List<InTraspasoDetalleDestino>		p_TraspasoDetalleDestino;
		
		
		public EntradaModificarTraspaso(){
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
		EntradaModificarTraspaso jin = gson.fromJson(in.toString(), EntradaModificarTraspaso.class);
		SalidaCrearTraspaso out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaCrearTraspaso logica(Connection con, EntradaModificarTraspaso entrada) throws Exception{
	
		log.write("INICIO SalidaModificarTraspaso");
		SalidaCrearTraspaso out = new SalidaCrearTraspaso();
		SQL sql=new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}
		
		
		if(entrada.p_Traspaso.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MODIFICACIONES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InTraspaso mov: entrada.p_Traspaso) {
			if(mov.IdTraspaso.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdTraspaso)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdTraspaso);
			}
		}
		
		
		List<InTraspaso> solicitudProcesar = new ArrayList<InTraspaso>();
		List<InTraspaso> solicitudInvalida = new ArrayList<InTraspaso>();
		
		//validaciones del Traspaso
		for(InTraspaso solicitud : entrada.p_Traspaso){
			solicitud.EstadoProceso = _c.EstadoPendiente;
			if(_c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)){
				int contOrigen=0;
				for(InTraspasoDetalleOrigen DetalleOrigen : entrada.p_TraspasoDetalleOrigen){
					if ((long)DetalleOrigen.IdTraspaso==(long)solicitud.IdTraspaso)
					{
						contOrigen++;
					}
					
				}
			
				if(contOrigen == 0){
					solicitud.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_TRASPASO_SIN_ORIGEN",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
			
				
			}
			
			int contDestino=0;
			for(InTraspasoDetalleDestino DetalleDestino : entrada.p_TraspasoDetalleDestino){
				if ((long)DetalleDestino.IdTraspaso==(long)solicitud.IdTraspaso)
				{
					contDestino++;
				}
				
			}
			if(contDestino == 0 && _c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)){
				solicitud.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_TRASPASO_SIN_DESTINO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				continue;
			}
		   
			if( !(_c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)) &&  !(_c.TraspasoMovimientoIngreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)) ){
				solicitud.EstadoProceso = _c.EstadoRechazado;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_TIPO_TRASPASO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				continue;
			}
		
			if (_c.EstadoRechazado.equalsIgnoreCase(solicitud.EstadoProceso)){
				solicitudInvalida.add(solicitud);
			}else{
				
				solicitudProcesar.add(solicitud);	
			};
						
		}	//fin Validaiones
		
		
		//MODIFICAMOS TRASPASOS SIN ERRORES
		//List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::Traspaso", new Long(solicitudProcesar.size()));
		
		List<RegistroTraspaso> actualizarTraspaso = new ArrayList<RegistroTraspaso>();
		List<RegistroTraspasoOrigen> actualizarTraspasoOrigen = new ArrayList<RegistroTraspasoOrigen>();
		List<RegistroTraspasoDestino> actualizarTraspasoDestino = new ArrayList<RegistroTraspasoDestino>();
		
		List<RegistroTraspasoOrigen> crearTraspasoOrigen = new ArrayList<RegistroTraspasoOrigen>();
		List<RegistroTraspasoDestino> crearTraspasoDestino = new ArrayList<RegistroTraspasoDestino>();
				
		
		//int i = 0;
		for(InTraspaso solicitud : solicitudProcesar){
			//TRASPASO
			RegistroTraspaso reg = new RegistroTraspaso();
			reg.IdTraspaso= solicitud.IdTraspaso;
			reg.FechaHoraSuscripcion=solicitud.FechaHoraSuscripcion;
			reg.IdDivisionPais=solicitud.IdDivisionPais;
			reg.FechaModificacion= Fecha.toDate(fechaSistema);
			reg.FechaIngreso= Fecha.toDate(fechaSistema);
			reg.FolioTraspaso=solicitud.FolioTraspaso;
			reg.Glosa=solicitud.Glosa;
			reg.IdCliente=solicitud.IdCliente;
			reg.IdEmisor=solicitud.IdEmisor;
			reg.IdEmpresa=entrada.p_IdEmpresa;
			//reg.IdEstado=_c.TraspasoEstadoIngresado;
			reg.IdEstado= "".equalsIgnoreCase(solicitud.IdEstado)? _c.TraspasoEstadoIngresado :solicitud.IdEstado;
			reg.IdTraspaso=solicitud.IdTraspaso;
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
			reg.IdEntidadRecaudadora=solicitud.IdEntidadRecaudadora;		
			actualizarTraspaso.add(reg);	//Incluir registro Traspaso
			//if(_c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso)){
				
				for(InTraspasoDetalleOrigen origen : entrada.p_TraspasoDetalleOrigen)
				{
					//ORIGEN
					RegistroTraspasoOrigen regOrigen =new RegistroTraspasoOrigen();
					if ((long)origen.IdTraspaso==(long)reg.IdTraspaso){
						regOrigen.Cantidad=origen.Cantidad;
						regOrigen.FechaModificacion=reg.FechaModificacion;
						regOrigen.FechaIngreso=reg.FechaIngreso;
						regOrigen.IdTraspaso=origen.IdTraspaso;
						regOrigen.IdSerieFondo=origen.IdSerieFondo;
						regOrigen.IdTraspaso=reg.IdTraspaso;
						regOrigen.IdDetalleTraspaso=origen.IdDetalleTraspaso;
						regOrigen.IdRegimenTributario=origen.IdRegimenTributario;
						if (!(regOrigen.Cantidad>0) && _c.TraspasoMovimientoEgreso.equalsIgnoreCase(solicitud.IdTipoMovimientoTraspaso) ){regOrigen.IdEstado=_c.TraspasoDetalleInactivo;}else{regOrigen.IdEstado=_c.TraspasoDetalleActivo;};
						//Incluir registro Traspaso Origen
						if (regOrigen.IdDetalleTraspaso>0){actualizarTraspasoOrigen.add(regOrigen);}else{crearTraspasoOrigen.add(regOrigen);};
							
					}
					
				}
			//}
			
			for(InTraspasoDetalleDestino destino : entrada.p_TraspasoDetalleDestino)
			{
				//DESTINO
				RegistroTraspasoDestino regDestino =new RegistroTraspasoDestino();
				if ((long)destino.IdTraspaso==(long)reg.IdTraspaso){
					regDestino.FechaModificacion=reg.FechaModificacion;
					regDestino.FechaIngreso=reg.FechaIngreso;
					regDestino.IdTraspaso=destino.IdTraspaso;
					regDestino.IdSerieFondo=destino.IdSerieFondo==null?"":destino.IdSerieFondo;
					regDestino.IdTraspaso=reg.IdTraspaso;
					regDestino.Porcentaje=destino.Porcentaje;
					regDestino.IdDetalleTraspaso=destino.IdDetalleTraspaso;
					regDestino.IdRegimenFondoMutuo=destino.IdRegimenFondoMutuo;
					regDestino.RunFFMM= destino.RunFFMM;
					regDestino.NombreFFMM= destino.NombreFFMM;
					if (regDestino.Porcentaje>0){regDestino.IdEstado=_c.TraspasoDetalleActivo;}else{regDestino.IdEstado=_c.TraspasoDetalleInactivo;};
					//Incluir registro Traspaso Origen
					if (regDestino.IdDetalleTraspaso>0){actualizarTraspasoDestino.add(regDestino);}else{crearTraspasoDestino.add(regDestino);};
				}
				
			}
			
			//i++;
		};
		
		if(actualizarTraspaso.size() > 0){
			sql.batch(con, SQL_TRASPASO_UPDATE, actualizarTraspaso, RegistroTraspaso.class);
			
		}
		if(actualizarTraspasoOrigen.size() > 0){
			sql.batch(con, SQL_TRASPASO_DETALLEORIGEN_UPDATE, actualizarTraspasoOrigen, RegistroTraspasoOrigen.class);
		}
		if(actualizarTraspasoDestino.size() > 0){
			sql.batch(con, SQL_TRASPASO_DETALLEDESTINO_UPDATE, actualizarTraspasoDestino, RegistroTraspasoDestino.class);
		}
		if(crearTraspasoOrigen.size() > 0){
			sql.batch(con, SQL_TRASPASO_DETALLEORIGEN_INSERT, crearTraspasoOrigen, RegistroTraspasoOrigen.class);
		}
		if(crearTraspasoDestino.size() > 0){
			sql.batch(con, SQL_TRASPASO_DETALLEDESTINO_INSERT, crearTraspasoDestino, RegistroTraspasoDestino.class);
		}
		

		return out;
	}
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"FMM\",\"p_Traspaso\":[{\"IdTraspaso\":198,\"IdCliente\":249194,\"IdPortafolio\":1450263"
						+ ",\"IdEmisor\":\"CREDICORPAGF\",\"IdMotivoTraspaso\":\"BAJCOM\",\"IdTipoMovimientoTraspaso\":\"TRAEGR\",\"IdTipoTraspaso\":\"PARCIAL\",\"Glosa\":\"xx\",\"FechaHoraSuscripcion\":\"2018-01-01 08:40:10\","
						+ "\"IdDivisionPais\":5,\"FolioTraspaso\":800,\"Monto\":500}],\"p_TraspasoDetalleOrigen\":[{\"IdTraspaso\":198,\"IdSerieFondo\":\"FM_APC1_A\",\"Cantidad\":8000,\"IdDetalleTraspaso\":192}] ,"
						+ "\"p_TraspasoDetalleDestino\":[{\"IdTraspaso\":198,\"IdSerieFondo\":\"FM_APC1_A\",\"Porcentaje\":44,\"IdDetalleTraspaso\":47}]}";
				
				
				
				EntradaModificarTraspaso Objin_json = gson.fromJson(json, EntradaModificarTraspaso.class);
				
				ModificarTraspaso demo = new ModificarTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				SalidaCrearTraspaso ObjOut = demo.logica(con, Objin_json);
	 				con.commit();
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
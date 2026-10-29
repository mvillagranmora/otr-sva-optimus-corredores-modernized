package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Fatca;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearFatca implements JavaCall{
	static final Logger logger = LogManager.getLogger(CrearFatca.class);
	
	public String SQL_INSERT_FATCA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearFatca_InsertFatca.sql");
	
	private final String BORRADO = "N";
	private final String ID_ESTADO = "ACT";
	
	public static class EntradaCrearFatca extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<Fatca> p_Fatcas;
		
		public EntradaCrearFatca() {
			this.p_Fatcas = new ArrayList<Fatca>();
		}
	}
	
	public static class SalidaCrearFatca extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoCrearFatca> P_OUT_REFID;
		
		public SalidaCrearFatca(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REFID =  new ArrayList<ResultadoCrearFatca>();
		}
	}
	
	public class ResultadoCrearFatca extends BcsPojo{
		public Long IdInterno;
		public Long IdEntidad;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearFatca in = gson.fromJson(jin.toString(), EntradaCrearFatca.class);
		SalidaCrearFatca out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearFatca logica(Connection con, EntradaCrearFatca entrada) throws Exception {
		logger.trace("INICIO::CrearFatca");
		long inicio = System.currentTimeMillis();
		SalidaCrearFatca out = new SalidaCrearFatca();
		
		List<Fatca> fatcas = obtenerEntidadesFatca(con, entrada);
		
		insertarFatcas(con, fatcas);
		
		out.P_OUT_REFID = obtenerIdsInsertados(fatcas);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearFatca", fin - inicio);
		return out;
	}
	
	private List<Fatca> obtenerEntidadesFatca(Connection con, EntradaCrearFatca entradaFatca ) {
		logger.trace("INICIO::CrearFatca.obtenerEntidadesFatca");
		long inicio = System.currentTimeMillis();
		
		List<Fatca> entidadesFatcas = entradaFatca.p_Fatcas.parallelStream().map(entidadFatca->{
			Fatca fatca = new Fatca();
			fatca.IdInterno = entidadFatca.IdInterno;
			fatca.IdPersona = entidadFatca.IdPersona;
			fatca.IdClasificacionFatca = entidadFatca.IdClasificacionFatca;
			fatca.IdClasificacionEmpresaFatca = entidadFatca.IdClasificacionEmpresaFatca;
			fatca.NumeroGIIN = entidadFatca.NumeroGIIN;
			fatca.FechaFirmaFormulario = entidadFatca.FechaFirmaFormulario;
			fatca.AutorizacionInformarOrganismo = entidadFatca.AutorizacionInformarOrganismo;
			fatca.ExcepcionResidente = entidadFatca.ExcepcionResidente;
			fatca.ExcepcionNacimiento = entidadFatca.ExcepcionNacimiento;
			fatca.ExcepcionDomicilio = entidadFatca.ExcepcionDomicilio;
			fatca.ExcepcionTelefono = entidadFatca.ExcepcionTelefono;
			fatca.ExcepcionOrdenPermanente = entidadFatca.ExcepcionOrdenPermanente;
			fatca.ExcepcionFirma = entidadFatca.ExcepcionFirma;
			fatca.ParticipacionSociedadesUSA = entidadFatca.ParticipacionSociedadesUSA;
			fatca.OrdenPermanente = entidadFatca.OrdenPermanente;
			fatca.NACEEUU = entidadFatca.NACEEUU;
			fatca.NACEEUUExplicacion = entidadFatca.NACEEUUExplicacion;
			fatca.EntidadFinanciera = entidadFatca.EntidadFinanciera;
			fatca.TipoInstitucionFinanciera = entidadFatca.TipoInstitucionFinanciera;
			fatca.EntidadGestionada = entidadFatca.EntidadGestionada;
			fatca.IngresosPasivos = entidadFatca.IngresosPasivos;
			fatca.GenerarIngresosPasivos = entidadFatca.GenerarIngresosPasivos;
			fatca.CategoriaEntidad = entidadFatca.CategoriaEntidad;
			fatca.GIINEntidad = entidadFatca.GIINEntidad;
			fatca.RazonSocial = entidadFatca.RazonSocial;
			fatca.FechaLiquidacion = entidadFatca.FechaLiquidacion;
			fatca.NombreMercado = entidadFatca.NombreMercado;
			fatca.FechaConstitucion = entidadFatca.FechaConstitucion;
			fatca.ObligacionesFiscales = entidadFatca.ObligacionesFiscales;
			fatca.ObligacionesFiscalesExplicacion = entidadFatca.ObligacionesFiscalesExplicacion;
			fatca.IndividuoControl = entidadFatca.IndividuoControl;
			fatca.Borrado = BORRADO;
			fatca.IdUsuarioCreador = entradaFatca.p_IdUsuario;
			fatca.IdUsuarioModificador = 0L;
			fatca.IdCarga = 0L;		
			fatca.IdEstado = ID_ESTADO;	
			return fatca;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearFatca.obtenerEntidadesFatca", fin - inicio);
		return entidadesFatcas;
	}
	
	private void insertarFatcas(Connection con, List<Fatca> fatcas) throws Exception {
		logger.debug("INICIO::CrearFatca.insertarFatcas");
		long inicio = System.currentTimeMillis();		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_FATCA, fatcas, Fatca.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearFatca.insertarFatcas", fin - inicio);
	}
	
	private List<ResultadoCrearFatca> obtenerIdsInsertados(List<Fatca> fatcas){
		logger.trace("INICIO::CrearFatca.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoCrearFatca> idsInsertados = fatcas.parallelStream().map(fatca->{
			ResultadoCrearFatca resultadoCreacionFatca = new ResultadoCrearFatca();
			resultadoCreacionFatca.IdInterno = fatca.IdInterno;
			resultadoCreacionFatca.IdEntidad = fatca.IdPersona;
			return resultadoCreacionFatca;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearFatca.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "";
			EntradaCrearFatca Objin = gson.fromJson(json, EntradaCrearFatca.class);
			
			CrearFatca demo=new CrearFatca();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaCrearFatca ObjOut=demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
	    }catch (SQLException e) {
	    	e.printStackTrace();
	    }
		System.out.println("== END ==");
	}

}

package cl.bcs.socionegocio.jobs;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class BloquearClientePorFechaVencimientoJob implements JavaCall {
	private final Log log = new Log("SOC.BloquearClientePorFechaVencimientoJob"); 
	FileLoader loader          = new FileLoader();
	private final String SQL_SELECT_DOCUMENTOS = loader.loadResourceToString("cl/bcs/socionegocio/jobs/sql/BloquearClientePorFechaVencimientoDocumentos_Select.sql");
	private final String SQL_UPDATE_ANEXOS = loader.loadResourceToString("cl/bcs/socionegocio/jobs/sql/BloquearClientePorFechaVencimientoAnexos_Update.sql");
	private final String SQL_UPDATE_CLIENTES = loader.loadResourceToString("cl/bcs/socionegocio/jobs/sql/BloquearClientePorFechaVencimientoClientes_Update.sql");

	public static class EntradaBloquearClientePorFechaVencimientoJob {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
	
	}
	
	public static class Documentos extends BcsPojo
	{
		public Long			IdCliente;
		public Long			IdAnexo;
		public String		IdEstado;
		public String 		BloquearAlVencer;
		public Timestamp	FechaVigencia;
		public Date	FechaModificacion;
	}

	public static class Anexo extends BcsPojo
	{
		public Long			IdAnexo;
	
	}
	
	public static class DatosActualizar extends BcsPojo
	{
		public Long		IdCliente;
		public String 	IdTipoBloqueo;
		public String	ComentarioBloqueo;
	
	}
	
	public static class SalidaBloquearClientePorFechaVencimientoJob{
		public Long 			P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Long> 		P_OUT_LISTA_CLIENTES;
		
		public SalidaBloquearClientePorFechaVencimientoJob(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}

	}	
	
    public SalidaBloquearClientePorFechaVencimientoJob logica(Connection con, EntradaBloquearClientePorFechaVencimientoJob entrada) throws Exception  {
    	log.write("*Bloquear Cliente Por Fecha de Vencimiento* Inicio de Job");
    	SalidaBloquearClientePorFechaVencimientoJob salida = new SalidaBloquearClientePorFechaVencimientoJob();
 
		try{
			Long parIdEmpresa = 1L;
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,parIdEmpresa,"PLATAFORMA","PLA.FechaSistema");
		
		
			salida.P_OUT_ERRORESCONTADOR = 1L;
			SQL sql = new SQL();
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
		
			List<Documentos> SelectDocumentos = sql.query(con, SQL_SELECT_DOCUMENTOS, parametroEntrada, Documentos.class);
			calcularFechaVencimiento(con, parIdEmpresa, SelectDocumentos);
			log.write("Ejecutar Bloqueo Automatico de Clientes"); 
			
			log.write("BLOQUEO POR CONFIGURACION AUTOMATICA: ");

			List<Anexo> IdAnexos = SelectDocumentos.stream().filter(doc -> Fecha.toStringDate(doc.FechaVigencia).equals(confFechaSistema)).map(doc -> {
				Anexo anexo = new Anexo();
				anexo.IdAnexo = doc.IdAnexo;
				return anexo;
			}).collect(Collectors.toList());
		
			parametroEntrada.put("IdAnexo", IdAnexos);
			sql.batch(con,SQL_UPDATE_ANEXOS , IdAnexos,Anexo.class);
			
			
			List<DatosActualizar> datosActualizar = SelectDocumentos.stream().filter(doc -> Fecha.toStringDate(doc.FechaVigencia).equals(confFechaSistema) && "S".equals(doc.BloquearAlVencer)).map(doc -> {
				DatosActualizar datoActualizar = new DatosActualizar();
				datoActualizar.IdCliente = doc.IdCliente;
				datoActualizar.IdTipoBloqueo="BAUTOM"; //Crear Tipo de Bloqueo!
				datoActualizar.ComentarioBloqueo="Cliente bloqueado automaticamente por fecha de vencimiento";
				return datoActualizar;
			}).collect(Collectors.toList());
			
			if(datosActualizar.size()>0){
		
				sql.batch(con,SQL_UPDATE_CLIENTES , datosActualizar,DatosActualizar.class);
		
				salida.P_OUT_LISTA_CLIENTES = datosActualizar.stream().map(id -> id.IdCliente).collect(Collectors.toList());
				
			}else{
				//no hay clientes que bloquear
			}
			
	      
		}catch(Exception e){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR Bloquear Cliente Por Fecha de Vencimiento", new HashMap<String, String>(1), con, entrada.p_IdEmpresa));
			log.write(e);
			log.write("***** Error  Bloquear Cliente Por Fecha de Vencimiento *****");
    		throw e;	
		}
		
		log.write( "*Bloquear Cliente Por Fecha de Vencimiento* Fin ejecucion de servicio");
        return salida;
		
	}
    
    
    @Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		EntradaBloquearClientePorFechaVencimientoJob in=gson.fromJson(jin.toString() ,EntradaBloquearClientePorFechaVencimientoJob.class);
		SalidaBloquearClientePorFechaVencimientoJob out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
    	
	private void calcularFechaVencimiento(Connection con, Long idEmpresa, List<Documentos> documentos) throws Exception {
	
		String numeroDiasVencimiento = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA","PLA.NumeroDiasFechaVencimiento");
		
		int diasVencimiento = Integer.valueOf(numeroDiasVencimiento);
		documentos.stream().filter(documento -> documento.FechaModificacion != null).forEach(documento -> {
			if(!"PEN".equals(documento.IdEstado)) {
				documento.FechaVigencia = new Timestamp((Fecha.addDays(documento.FechaModificacion, diasVencimiento).getTime()));
					
			}
		});
		
	}
	
	public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getDESAConnection();
    	BloquearClientePorFechaVencimientoJob p = new BloquearClientePorFechaVencimientoJob();
    	EntradaBloquearClientePorFechaVencimientoJob entrada = new EntradaBloquearClientePorFechaVencimientoJob();
		entrada.p_IdEmpresa=1L;
		entrada.p_IdUsuario=37L;
		entrada.p_IdModulo="SOC";

        try {
            p.logica(con,entrada);
           // con.commit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
    
}
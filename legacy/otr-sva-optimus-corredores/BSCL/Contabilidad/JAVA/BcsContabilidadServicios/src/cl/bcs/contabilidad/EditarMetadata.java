package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.AgregarCuentasPorMetadata.CuentasFiltros;
import cl.bcs.contabilidad.AgregarCuentasPorMetadata.Evento;
import cl.bcs.contabilidad.AgregarCuentasPorMetadata.InAgregarCuentasPorMetadata;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EditarMetadata implements JavaCall{
	
	private final Log log = new Log("CON.AgregarMetadata");
	
	private final String SQL_UPDATE_METADATA_CUENTA_CONTABLE = loadResourceToString("cl/bcs/contabilidad/sql/EditarMetadata_UpdateMetadataCuentaContable.sql");
	private final String SQL_INSERT_FILTROS_METADATA = loadResourceToString("cl/bcs/contabilidad/sql/EditarMetadata_InsertMetadataFiltros.sql");
	private final String SQL_SELECT_FILTROS_METADATA = loadResourceToString("cl/bcs/contabilidad/sql/EditarMetadata_SelectMetadataFiltros.sql");
	private final String SQL_BORRAR_FILTROS_METADATA = loadResourceToString("cl/bcs/contabilidad/sql/EditarMetadata_BorrarMetadataFiltros.sql");
	private final String SQL_SELECT_COMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/EditarMetadata_SelectComprobante.sql");
	private final String SQL_SELECT_EVENTOCOMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/EditarMetadata_SelectEventoComprobante.sql");
	
	public static class InEditarMetadata{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Long 	p_IdMetadataCuentaContable;
		public String	p_DescripcionMetadata;
		public List<InFiltro> p_Filtros;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InFiltro{
		public String	CuentaOptimus;
		public String	IdTipoDatoEvento;
		public String	Condicion;
		public String	Valor; 
	}
	
	public static class OutEditarMetadata{
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class Metadata{
		public Long IdMetadataCuentaContable;
		public String DescripcionMetadata;
	}
	
	public static class TipoContabilidad{
		public Long IdTipoContabilidad;
		public String DebeHaber;
		public Long Ejercicio;
		public String Formato;
	}
		
	public static class Filtro{
		public Long 	IdMetadataCuentaContable;
		public String	CuentaOptimus;
		public String	IdTipoDatoEvento;
		public String	Condicion;
		public String	Valor;
	}
	
	public static class FiltroExistente{
		public Long		IdMetadataFiltros;
		public Long 	IdMetadataCuentaContable;
		public String	CuentaOptimus;
		public String	IdTipoDatoEvento;
		public String	Condicion;
		public String	Valor;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InEditarMetadata in=gson.fromJson(jin.toString() ,InEditarMetadata.class);
		OutEditarMetadata out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutEditarMetadata logica(Connection con, InEditarMetadata entrada) throws Exception {
		OutEditarMetadata out = new OutEditarMetadata();
		try{
			
			SQL sql=new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			Metadata metadata = new Metadata();
			metadata.IdMetadataCuentaContable = entrada.p_IdMetadataCuentaContable;
			metadata.DescripcionMetadata = entrada.p_DescripcionMetadata;
			params.put("IdMetadataCuentaContable", metadata.IdMetadataCuentaContable);
			List<Metadata> metadatas = new ArrayList<Metadata>();
			metadatas.add(metadata);
			List<FiltroExistente> filtrosExistentes = sql.query(con,SQL_SELECT_FILTROS_METADATA,params,FiltroExistente.class);
			
			List<FiltroExistente> filtrosEliminar = new ArrayList<FiltroExistente>();
			List<Filtro> filtrosInsertar = new ArrayList<Filtro>();
			List<Filtro> filtrosCuentaNuevaInsertar = new ArrayList<Filtro>();
			
			for(InFiltro p_filtro : entrada.p_Filtros){
				boolean existe = false;
				boolean cuentaNueva = false;
				for(FiltroExistente existente : filtrosExistentes){
					if(existente.CuentaOptimus.equalsIgnoreCase(p_filtro.CuentaOptimus) && existente.IdTipoDatoEvento.equalsIgnoreCase(p_filtro.IdTipoDatoEvento)){
						existe = true;
					}
					if(existente.CuentaOptimus.equalsIgnoreCase(p_filtro.CuentaOptimus)){
						cuentaNueva = true;
					}
				}
				if(!existe){
					Filtro filtro = new Filtro();
					filtro.IdMetadataCuentaContable = metadata.IdMetadataCuentaContable;
					filtro.CuentaOptimus = p_filtro.CuentaOptimus;
					filtro.Condicion = p_filtro.Condicion;
					filtro.IdTipoDatoEvento = p_filtro.IdTipoDatoEvento;
					filtro.Valor = p_filtro.Valor;
					filtrosInsertar.add(filtro);
				}
				
				if(!cuentaNueva){
					Filtro filtro = new Filtro();
					filtro.IdMetadataCuentaContable = metadata.IdMetadataCuentaContable;
					filtro.CuentaOptimus = p_filtro.CuentaOptimus;
					filtro.Condicion = p_filtro.Condicion;
					filtro.IdTipoDatoEvento = p_filtro.IdTipoDatoEvento;
					filtro.Valor = p_filtro.Valor;
					filtrosCuentaNuevaInsertar.add(filtro);
				}
			}
			
			for(FiltroExistente existente : filtrosExistentes){
				boolean existe = false;
				for(InFiltro p_filtro : entrada.p_Filtros){
					if(existente.CuentaOptimus.equalsIgnoreCase(p_filtro.CuentaOptimus) && existente.IdTipoDatoEvento.equalsIgnoreCase(p_filtro.IdTipoDatoEvento)){
						existe = true;
					}
				}
				if(!existe){
					filtrosEliminar.add(existente);
				}
			}
			
			
			sql.batch(con, SQL_UPDATE_METADATA_CUENTA_CONTABLE, metadatas, Metadata.class);
			sql.batch(con, SQL_INSERT_FILTROS_METADATA, filtrosInsertar, Filtro.class);
			sql.batch(con, SQL_BORRAR_FILTROS_METADATA, filtrosEliminar, FiltroExistente.class);
			
			if(filtrosCuentaNuevaInsertar.size() > 0){
				params.clear();
				params.put("IdMetadataCuentaContable", metadata.IdMetadataCuentaContable);
				List<TipoContabilidad> tipoContabilidades = sql.query(con,SQL_SELECT_COMPROBANTE,params,TipoContabilidad.class);
				
				for(TipoContabilidad tipoContabilidad : tipoContabilidades){
					params.clear();
					params.put("IdTipoContabilidad", tipoContabilidad.IdTipoContabilidad);
					params.put("IdMetadataCuentaContable", metadata.IdMetadataCuentaContable);
					List<Evento> evento = sql.query(con,SQL_SELECT_EVENTOCOMPROBANTE,params,Evento.class);
					AgregarCuentasPorMetadata agregar = new AgregarCuentasPorMetadata();
					InAgregarCuentasPorMetadata in = new InAgregarCuentasPorMetadata(); 
					in.p_IdEmpresa = entrada.p_IdEmpresa;
					in.p_IdUsuario = entrada.p_IdUsuario;
					in.p_IdModulo = entrada.p_IdModulo;
					in.p_Eventos = evento;
					in.p_IdMetadataCuentaContable = metadata.IdMetadataCuentaContable;
					in.p_IdTipoContabilidad = tipoContabilidad.IdTipoContabilidad;
					in.p_DebeHaber = tipoContabilidad.DebeHaber;
					in.p_Ejercicio = String.valueOf(tipoContabilidad.Ejercicio);
					in.p_Formato = tipoContabilidad.Formato;
					in.p_CuentasFiltros = new ArrayList<CuentasFiltros>();
					for(Filtro filtro : filtrosCuentaNuevaInsertar){
						CuentasFiltros cuentaFiltro = new CuentasFiltros();
						cuentaFiltro.IdTipoDatoEvento = filtro.IdTipoDatoEvento;
						cuentaFiltro.CuentaOptimus = filtro.CuentaOptimus;
						cuentaFiltro.Condicion = filtro.Condicion;
						cuentaFiltro.Valor = filtro.Valor;
						in.p_CuentasFiltros.add(cuentaFiltro);
					}
					agregar.logica(con, in);
				}
			}
			
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_MENSAJE = "UI_EXITO";
			
		} catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "UI_ERROR";
			log.write("Error",e);
		}
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdMetadataCuentaContable\":13,\"p_DescripcionMetadata\":\"TEST 68\",\"p_Filtros\":[{\"CuentaOptimus\":\"110101001002\",\"IdTipoDatoEvento\":\"AliasBancoCaja\",\"Condicion\":\"=\",\"Valor\":\"TESTEST\"},{\"CuentaOptimus\":\"110101001002\",\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BCS\"},{\"CuentaOptimus\":\"110101002001\",\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BCS\"},{\"CuentaOptimus\":\"110101002001\",\"IdTipoDatoEvento\":\"AliasBancoCaja\",\"Condicion\":\"=\",\"Valor\":\"TESTESTT\"},{\"CuentaOptimus\":\"11010101\",\"IdTipoDatoEvento\":\"AliasBancoCaja\",\"Condicion\":\"=\",\"Valor\":\"WEWE\"},{\"CuentaOptimus\":\"110102001001\",\"IdTipoDatoEvento\":\"AliasBancoCaja\",\"Condicion\":\"=\",\"Valor\":\"TETETE\"},{\"CuentaOptimus\":\"110102001001\",\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BEC\"}]}";
			InEditarMetadata Objin = gson.fromJson(json, InEditarMetadata.class);
			
			EditarMetadata demo=new EditarMetadata();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				OutEditarMetadata ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}

}

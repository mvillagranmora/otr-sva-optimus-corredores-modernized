package cl.bcs.mesa.SPT;

import java.sql.Connection;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.math.BigDecimal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.GrabarContratoSAAPV.DistribucionInversion;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosContrato;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosEntradaServicio;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosSalidaServicio;
import cl.bcs.APV.GrabarContratoSAAPV.PortafoliosModificados;
import cl.bcs.normativa.interfaces.GenerarInterfazC40CarteraNormal.IdClientes;
import cl.bcs.APV.GrabarContratoSAAPV.DetalleOrigenTraspaso;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaArchivoOperacionSpot implements JavaCall{	
	
	private final Log log = new Log("CargaMasivaOperacionSpot");
	
	private final String SQL_INSERT_CARGA_ARCHIVO = loadResourceToString("cl/bcs/mesa/SPT/sql/CargaMasivaArchivoSpot_Insert.sql");
	private final String SQL_INSERT_CARGA_ARCHIVO_DETALLE = loadResourceToString("cl/bcs/mesa/SPT/sql/CargaMasivaArchivoSpotDetalle_Insert.sql");
	private final String SQL_EXISTE_CLIENTE = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarExisteCliente.sql");
	private static final DateFormat formatoEntrada = new SimpleDateFormat("dd-MM-yyyy");
	private static final DateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd");
	
	
	public static class CargaMasivaArchivo_Entrada extends BcsPojo {
	    public Long							p_IdEmpresa;
	    public Long							p_IdUsuario;
	    public String						p_IdModulo;
	    public String						p_FechaCarga;
	    public List<CargaArchivo> 			p_Archivo;
		public List<DetalleCargaMasiva>  	p_DetalleCargaMasiva;
	    
	    public CargaMasivaArchivo_Entrada(){
	    	p_Archivo = new ArrayList<CargaArchivo>();
	    	p_DetalleCargaMasiva = new ArrayList<DetalleCargaMasiva>();
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CargaMasivaArchivo_Salida extends BcsPojo{		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_IDSCARGAMASIVA;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CargaMasivaArchivo_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_IDSCARGAMASIVA = new ArrayList<Long>();
			P_OUT_ERRORESCONTADOR = 0L;
		}		
	};
	
	
	public static class CargaArchivo extends BcsPojo{
		public Long 	IdCargaMasiva;
		public String 	FechaCarga;
		public Long 	CantidadRegistros; 
		public Long 	IdEmpresa;
		public Long 	IdUsuario;
		public String 	IdEstado;
		public String 	NombreArchivo;
		public Long 	IdTipoCargaMasiva;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class DetalleCargaMasiva extends BcsPojo{
		public Long 		IdCargaMasivaDetalle;
		public String 		DocumentoIdentidad;
		public String 		CodigoPortafolio;
		public String 		ProductoAgrupador;
		public String 		TipoOperacion;
		public String 		FechaOperacion;
		public String 		HoraOperacion;
		public String 		CanalIngreso;
		public BigDecimal 	MontoMonedaPrincipal;
		public BigDecimal 	MontoMonedaSecundaria;
		public BigDecimal 	TipoCambioCierre;
		public BigDecimal 	TipoCambioCosto;
		public BigDecimal 	PuntaCompra;
		public BigDecimal 	PuntaVenta;
		public String 		FormaPagoEntregamos;
		public String	 	FormaPagoRecibimos;
		public String 		Observacion;
		public String 		AfectoADerivado;
		public String 		DolarObservado;
		public Long 		CodigoComercio;
		public String 		DetalleValidacion;
		public Long 		IdCargaMasiva;
		public Long 		IdTipoCargaMasiva;
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public String 		TipoComprobanteFacturacion;
		

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ExisteCliente extends BcsPojo{		
		public String DocumentoIdentidad;
		public Long IdCliente;
		public Long IdPortafolio;
		public String CodigoPortafolio;
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CargaMasivaArchivo_Entrada jin = gson.fromJson(in.toString() ,CargaMasivaArchivo_Entrada.class);
		CargaMasivaArchivo_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};	
	
	public CargaMasivaArchivo_Salida logica(Connection con, CargaMasivaArchivo_Entrada in) throws Exception {
		CargaMasivaArchivo_Salida out = new CargaMasivaArchivo_Salida();
		SQL sql = new SQL();
		MensajeSalida msg;
		HashMap<String, Object> param;
		Long Count = 0L;
		
		
	if(in.p_Archivo.size() > 0){
		try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			List<Long> idsCargaArchivo = Utils.getSecuencia(con, "BSCL.Mesa.BD.Secuencia::SPT.IdCargaMasiva", (long)in.p_Archivo.size());
			CargaArchivo cargaMasiva = new CargaArchivo();
			cargaMasiva.IdCargaMasiva = idsCargaArchivo.get(0);
			cargaMasiva.FechaCarga = fechaSistema;
			cargaMasiva.CantidadRegistros = (long) in.p_DetalleCargaMasiva.size();
			cargaMasiva.IdEmpresa = in.p_IdEmpresa;
			cargaMasiva.IdUsuario = in.p_IdUsuario;
			cargaMasiva.IdEstado = "ING";
			cargaMasiva.NombreArchivo = in.p_Archivo.get(0).NombreArchivo;
			cargaMasiva.IdTipoCargaMasiva = in.p_Archivo.get(0).IdTipoCargaMasiva;
			
			List<CargaArchivo> cargaArchivos = Arrays.asList(cargaMasiva);
			List<DetalleCargaMasiva> listaCargaDetalle = new ArrayList<>();
			
			param = new HashMap<>();
			List<String> documentosIdentidad = new ArrayList<>();
            for(DetalleCargaMasiva Detalle : in.p_DetalleCargaMasiva) {
            	documentosIdentidad.add(Detalle.DocumentoIdentidad);
            }
            param.put("DocumentoIdentidad", documentosIdentidad);	
			List<ExisteCliente> existeCliente = sql.query(con, SQL_EXISTE_CLIENTE, param, ExisteCliente.class);

			
			
			for (DetalleCargaMasiva Detalle : in.p_DetalleCargaMasiva) {
				List<Long> IdCargaMasivaDetalle = Utils.getSecuencia(con, "BSCL.Mesa.BD.Secuencia::SPT.IdCargaMasivaDetalle", (long)in.p_Archivo.size());
				Count ++;
				Date fecha = formatoEntrada.parse(Detalle.FechaOperacion);
	            String FechaOperacion = formatoSalida.format(fecha);
	            				
				Long existePortafolio = 0L;
				Long iDCliente = 0L;
				for (ExisteCliente ex : existeCliente) {
					if(Detalle.CodigoPortafolio.equals(ex.CodigoPortafolio) && (ex.DocumentoIdentidad.equals(Detalle.DocumentoIdentidad))) {
						iDCliente = ex.IdCliente;
						existePortafolio = ex.IdPortafolio;
					}
				}

	            
				DetalleCargaMasiva cargaDetalle = new DetalleCargaMasiva();
				cargaDetalle.IdCargaMasivaDetalle   	=IdCargaMasivaDetalle.get(0)    	;
				cargaDetalle.DocumentoIdentidad     	=Detalle.DocumentoIdentidad     	;
				cargaDetalle.CodigoPortafolio       	=Detalle.CodigoPortafolio       	;//si es un APV, ProductoAgrupador es obligatorio
				cargaDetalle.ProductoAgrupador      	=Detalle.ProductoAgrupador      	;
				cargaDetalle.TipoOperacion          	=Detalle.TipoOperacion          	;
				cargaDetalle.FechaOperacion         	=FechaOperacion						;
				cargaDetalle.HoraOperacion          	=Detalle.HoraOperacion          	;
				cargaDetalle.CanalIngreso           	=Detalle.CanalIngreso           	;
				cargaDetalle.MontoMonedaPrincipal   	=Detalle.MontoMonedaPrincipal   	;
				cargaDetalle.MontoMonedaSecundaria  	=Detalle.MontoMonedaSecundaria  	;
				cargaDetalle.TipoCambioCierre       	=Detalle.TipoCambioCierre       	;
				cargaDetalle.TipoCambioCosto        	=Detalle.TipoCambioCosto        	;
				cargaDetalle.PuntaCompra            	=Detalle.PuntaCompra            	;
				cargaDetalle.PuntaVenta             	=Detalle.PuntaVenta             	;
				cargaDetalle.FormaPagoEntregamos    	=Detalle.FormaPagoEntregamos    	;
				cargaDetalle.FormaPagoRecibimos     	=Detalle.FormaPagoRecibimos     	;
				cargaDetalle.Observacion            	=Detalle.Observacion            	;
				cargaDetalle.AfectoADerivado        	=Detalle.AfectoADerivado        	;
				cargaDetalle.DolarObservado         	=Detalle.DolarObservado         	;
				cargaDetalle.CodigoComercio         	=Detalle.CodigoComercio         	;
				cargaDetalle.DetalleValidacion      	=Detalle.DetalleValidacion      	;
				cargaDetalle.IdCliente					=iDCliente							;
				cargaDetalle.IdPortafolio			 	=existePortafolio					;
				cargaDetalle.TipoComprobanteFacturacion =Detalle.TipoComprobanteFacturacion ;
				cargaDetalle.IdCargaMasiva          	=cargaMasiva.IdCargaMasiva      	;
				cargaDetalle.IdTipoCargaMasiva      	=cargaMasiva.IdTipoCargaMasiva  	;
				
				//VALIDACIONES
				
				if(!("COMSPT".equalsIgnoreCase(cargaDetalle.TipoOperacion)||"VENSPT".equalsIgnoreCase(cargaDetalle.TipoOperacion))) {
					msg = new MensajeSalida();
					msg.id = Count;
					msg.tipo = "ERROR";
					msg.codigo = "ERROR";
					msg.descripcion = "Los tipos de operacion validos solo son COMSPT o VENSPT";
					out.P_OUT_MENSAJES.add(msg);
					out.P_OUT_ERRORESCONTADOR++;
				}
				listaCargaDetalle.add(cargaDetalle);
			}
			if(out.P_OUT_ERRORESCONTADOR == 0){
				
					int[] resD = sql.batch(con, SQL_INSERT_CARGA_ARCHIVO_DETALLE, listaCargaDetalle, DetalleCargaMasiva.class);
					int[] res = sql.batch(con, SQL_INSERT_CARGA_ARCHIVO, cargaArchivos, CargaArchivo.class);
					if(res!=null && res.length>0 && res[0]==1
							&& resD!=null && resD.length>0 && resD[0]==1){
						
						
						msg = new MensajeSalida();
						msg.id = 1L;
						msg.tipo = "INFORMACION";
						msg.codigo = "CARGA_MASIVA_OK";
						msg.descripcion = "Carga masiva de Operaciones SPOT exitosa.";
						out.P_OUT_MENSAJES.add(msg);
						out.P_IDSCARGAMASIVA.add(idsCargaArchivo.get(0));
						
						
					}else{
						msg = new MensajeSalida();
						msg.id = 1L;
						msg.tipo = "ERROR";
						msg.codigo = "CARGA_MASIVA_ERROR";
						msg.descripcion = "Carga masiva de Operaciones SPOT con problemas de ingreso.";
						out.P_OUT_MENSAJES.add(msg);
						out.P_OUT_ERRORESCONTADOR++;
					}
				}	
			}
			catch(Exception error){
				msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_CONECCION";
				msg.descripcion = error.getMessage();
				out.P_OUT_MENSAJES.add(msg);
				out.P_OUT_ERRORESCONTADOR = (long)out.P_OUT_MENSAJES.size(); 
			}
		
		}
		return out;
	}

	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
//			try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			
				String json = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 0,\r\n"
						+ "    \"p_IdModulo\": \"SPT\",\r\n"
						+ "    \"p_FechaCarga\": \"2023-04-13\",\r\n"
						+ "    \"p_Archivo\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdCargaMasiva\": 1,\r\n"
						+ "            \"IdTipoCargaMasiva\": 1,\r\n"
						+ "            \"NombreArchivo\": \"CargaMasivaSpot.txt\"\r\n"
						+ "        }\r\n"
						+ "    ],\r\n"
						+ "    \"p_DetalleCargaMasiva\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdCargaMasivaDetalle\": 1,\r\n"
						+ "            \"DocumentoIdentidad\": \"18695177-8\",\r\n"
						+ "            \"CodigoPortafolio\": \"APV-A\",\r\n"
						+ "            \"ProductoAgrupador\": \"\",\r\n"
						+ "            \"TipoOperacion\": \"COMSPT\",\r\n"
						+ "            \"FechaOperacion\": \"18-12-2023\",\r\n"
						+ "            \"HoraOperacion\": \"10:06:00\",\r\n"
						+ "            \"CanalIngreso\": \"MES\",\r\n"
						+ "            \"MontoMonedaPrincipal\": 2.5,\r\n"
						+ "            \"MontoMonedaSecundaria\": 2000,\r\n"
						+ "            \"TipoCambioCierre\": 801,\r\n"
						+ "            \"TipoCambioCosto\": 801.5,\r\n"
						+ "            \"PuntaCompra\": 803,\r\n"
						+ "            \"PuntaVenta\": 803.3,\r\n"
						+ "            \"FormaPagoEntregamos\": \"VENSPT_TRA_CLP_0\",\r\n"
						+ "            \"FormaPagoRecibimos\": \"COMSPT_TRA_USD_0\",\r\n"
						+ "            \"Observacion\": \"COMENTARIOS 4MPWX26\",\r\n"
						+ "            \"AfectoADerivado\": \"S\",\r\n"
						+ "            \"DolarObservado\": \"N\",\r\n"
						+ "            \"CodigoComercio\": 10000,\r\n"
						+ "            \"TipoComprobanteFacturacion\": \"BEE\"\r\n"
						+ "        },\r\n"
						+ "        {\r\n"
						+ "            \"IdCargaMasivaDetalle\": 1,\r\n"
						+ "            \"DocumentoIdentidad\": \"18696301-6\",\r\n"
						+ "            \"CodigoPortafolio\": \"0\",\r\n"
						+ "            \"ProductoAgrupador\": \"\",\r\n"
						+ "            \"TipoOperacion\": \"COMSPT\",\r\n"
						+ "            \"FechaOperacion\": \"18-12-2023\",\r\n"
						+ "            \"HoraOperacion\": \"10:06:00\",\r\n"
						+ "            \"CanalIngreso\": \"MES\",\r\n"
						+ "            \"MontoMonedaPrincipal\": 2.5,\r\n"
						+ "            \"MontoMonedaSecundaria\": 2000,\r\n"
						+ "            \"TipoCambioCierre\": 802,\r\n"
						+ "            \"TipoCambioCosto\": 802.5,\r\n"
						+ "            \"PuntaCompra\": 804,\r\n"
						+ "            \"PuntaVenta\": 804.3,\r\n"
						+ "            \"FormaPagoEntregamos\": \"VENSPT_TRA_CLP_0\",\r\n"
						+ "            \"FormaPagoRecibimos\": \"COMSPT_TRA_USD_0\",\r\n"
						+ "            \"Observacion\": \"COMENTARIOS 4MPWX26\",\r\n"
						+ "            \"AfectoADerivado\": \"S\",\r\n"
						+ "            \"DolarObservado\": \"N\",\r\n"
						+ "            \"CodigoComercio\": 10000,\r\n"
						+ "            \"TipoComprobanteFacturacion\": \"BEE\"\r\n"
						+ "        },\r\n"
						+ "        {\r\n"
						+ "            \"IdCargaMasivaDetalle\": 1,\r\n"
						+ "            \"DocumentoIdentidad\": \"18695177-8\",\r\n"
						+ "            \"CodigoPortafolio\": \"0\",\r\n"
						+ "            \"ProductoAgrupador\": \"\",\r\n"
						+ "            \"TipoOperacion\": \"COMSPT\",\r\n"
						+ "            \"FechaOperacion\": \"18-12-2023\",\r\n"
						+ "            \"HoraOperacion\": \"10:06:00\",\r\n"
						+ "            \"CanalIngreso\": \"MES\",\r\n"
						+ "            \"MontoMonedaPrincipal\": 2.5,\r\n"
						+ "            \"MontoMonedaSecundaria\": 2000,\r\n"
						+ "            \"TipoCambioCierre\": 803,\r\n"
						+ "            \"TipoCambioCosto\": 803.5,\r\n"
						+ "            \"PuntaCompra\": 805,\r\n"
						+ "            \"PuntaVenta\": 805.3,\r\n"
						+ "            \"FormaPagoEntregamos\": \"VENSPT_TRA_CLP_0\",\r\n"
						+ "            \"FormaPagoRecibimos\": \"COMSPT_TRA_USD_0\",\r\n"
						+ "            \"Observacion\": \"COMENTARIOS 4MPWX26\",\r\n"
						+ "            \"AfectoADerivado\": \"S\",\r\n"
						+ "            \"DolarObservado\": \"N\",\r\n"
						+ "            \"CodigoComercio\": 10000,\r\n"
						+ "            \"TipoComprobanteFacturacion\": \"BEE\"\r\n"
						+ "        }\r\n"
						+ "    ]\r\n"
						+ "}";
				
				CargaMasivaArchivo_Entrada Objin = gson.fromJson(json, CargaMasivaArchivo_Entrada.class);
				
				CargaMasivaArchivoOperacionSpot demo = new CargaMasivaArchivoOperacionSpot();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				CargaMasivaArchivo_Salida ObjOut = demo.logica(con, Objin);
	 				if(ObjOut.P_OUT_ERRORESCONTADOR != null){
	 					con.commit();
	 				}
	 				else {
	 					con.rollback();	
	 				}
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
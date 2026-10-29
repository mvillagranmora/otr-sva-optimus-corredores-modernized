package cl.bcs.normativa.circulares;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class GenerarArchivoCircular1720 implements JavaCall{	
	
	private final String SQL_INSERT_TABLAPARAMETROS = loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarArchivoCircular1720_InsertTablaParametros.sql");
	private final String SQL_SELECT_EMPRESA = loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarArchivoCircular1720_SelectEmpresa.sql");
			
	private final Log log = new Log("NOR.GenerarArchivoCircular1720");
	
	public static class InformacionEmpresa {
		public Long IdEmpresa;
		public String DocumentoIdentidad;
		public String RazonSocial;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	public static class TablaParametros_Entrada {
		public String  TipoParametro;
		public String  Valor;
	
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjTablaSalida {
		public Long			IdInterno; 
		public java.sql.Timestamp	FechaCuadraturaCustodia; 
		public String		Nemotecnico; 
		public String       NemotecnicoCMF;
		public String  		NombreEmisor;
		public String       TipoInstrumento;
		public BigDecimal   CantidadUnidadesNominal;
		public String       TipoUnidades; 
		public BigDecimal   Valorizacion;
		public BigDecimal   CarteraPropia;
		public BigDecimal   CustodiaTerceros;
		public BigDecimal   CustodiaPropiaDCV;
		public BigDecimal   CustodiaTercerosDCV;
		public BigDecimal   CustodiaSociedadEmisora;
		public BigDecimal   CustodiaIntermediario;
		public BigDecimal   CustodiaBolsas;
		public BigDecimal   CustodiaOtros;
		public java.sql.Date  FechaRegistro;
		public Long         IdCuadratura;
		public String       Tipo;
		public String		Habilitado; 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjResultadoProceso {
		public String   NombreParametro; 
		public String  	Valor;		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class GenerarArchivoCircular1720_Entrada{
		public Long                          p_IdEmpresa;
		public String                        p_FechaConsulta;
		public Long							 p_IdSolicitudCircular;
		public String						 p_IdTipoDocumento;
		public List<TablaParametros_Entrada> P_TABLAPARAMETROS;
		
		public GenerarArchivoCircular1720_Entrada (){			
			P_TABLAPARAMETROS	= new ArrayList<TablaParametros_Entrada>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class GenerarArchivoCircular1720_Salida{
		public List<ObjTablaSalida>	         P_OUT_TABLASALIDA;
		public String  P_OUT_ARCHIVO;
		
		public GenerarArchivoCircular1720_Salida (){
			P_OUT_TABLASALIDA      = new ArrayList<ObjTablaSalida>();			
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	@Trace
	public GenerarArchivoCircular1720_Salida logica(Connection con, GenerarArchivoCircular1720_Entrada in) throws Exception {
		log.write("Inicio GenerarArchivoCircular1720");
		log.write(in.toString());
		StringBuilder archivo = new StringBuilder();
				
		GenerarArchivoCircular1720_Salida out = new GenerarArchivoCircular1720_Salida();
		SQL sql = new SQL();
		
		//Recupera valores de empresa
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		List<InformacionEmpresa> empresa = sql.query(con, SQL_SELECT_EMPRESA, params, InformacionEmpresa.class);
		
		try {			
			List<TablaParametros_Entrada> lstParametros = new ArrayList<>();
			
			for (TablaParametros_Entrada parametro : in.P_TABLAPARAMETROS) {	
				TablaParametros_Entrada param = new TablaParametros_Entrada();
				
				param.TipoParametro = parametro.TipoParametro;
				param.Valor = parametro.Valor;
				
				lstParametros.add(param);	
			}		
						
			sql.batch(con, SQL_INSERT_TABLAPARAMETROS, lstParametros, TablaParametros_Entrada.class);
			
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_IdSolicitudCircular", in.p_IdSolicitudCircular);
			parmsIn.put("p_IdTipoDocumento", in.p_IdTipoDocumento);
			
			HashMap<String,Class> parmsOut =new HashMap<String,Class>();
			parmsOut.put("p_out_TablaSalida", ObjTablaSalida.class);
			
			String queryCall = "Call \"BSCL.Normativa.BD.Servicios.NOR::CIR1720\"(p_IdSolicitudCircular=>:p_IdSolicitudCircular,p_IdTipoDocumento=>:p_IdTipoDocumento,p_TablaParametros=> \"BSCL.Normativa.BD::GBL.Circular1720TablaParametro\",p_out_TablaSalida=>$p_out_TablaSalida)";
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIn, parmsOut);
			
			out.P_OUT_TABLASALIDA = (List<ObjTablaSalida>) rpt.get("p_out_TablaSalida");
			Long cantidad = 0L;
			
			if (out.P_OUT_TABLASALIDA.size() > 0) {	        	
	        	// -------------------------------------------------------------
			    // GENERA REGISTRO 01 (Registro de encabezamiento del Archivo)
			    // -------------------------------------------------------------
	        	archivo.append("1");
	        	archivo.append(String.format("%-9s", '0' + empresa.get(0).DocumentoIdentidad.split("-")[0]).replace(' ', '0'));
	        	archivo.append(String.format("%-1s", empresa.get(0).DocumentoIdentidad.substring(empresa.get(0).DocumentoIdentidad.lastIndexOf("-")+1, empresa.get(0).DocumentoIdentidad.length())).replace(' ', ' '));
	        	archivo.append(String.format("%-8s", in.p_FechaConsulta.split("-")[0]+in.p_FechaConsulta.substring(in.p_FechaConsulta.indexOf("-")+1,in.p_FechaConsulta.lastIndexOf("-"))+in.p_FechaConsulta.substring(in.p_FechaConsulta.lastIndexOf("-")+1,in.p_FechaConsulta.length())).replace(' ', '0'));	        	
	        	archivo.append(String.format("%-60s", empresa.get(0).RazonSocial).replace(' ', ' '));
	        	archivo.append(String.format("%-183s", " ").replace(' ', ' '));
	        	archivo.append("\n");
	        	
	        	// -------------------------------------------------------------
			    // GENERA REGISTRO 02 (Registro de detalle del Archivo)
			    // -------------------------------------------------------------
	        	for(ObjTablaSalida registros : out.P_OUT_TABLASALIDA) {
					if(registros.Habilitado.equals("S")) {
						archivo.append("2");
						archivo.append(String.format("%-20s", registros.NemotecnicoCMF.length() > 20?registros.NemotecnicoCMF.substring(0,20): registros.NemotecnicoCMF).replace(' ', ' '));
						archivo.append(String.format("%-30s", registros.NombreEmisor.length() > 30?registros.NombreEmisor.substring(0,30):registros.NombreEmisor).replace(' ', ' '));
						archivo.append(String.format("%-10s", registros.TipoInstrumento.length() > 10?registros.TipoInstrumento.substring(0,10):registros.TipoInstrumento).replace(' ', ' '));
						archivo.append(String.format("%19s", onlyNumbers(registros.CantidadUnidadesNominal, true)).replace(' ', '0'));
						archivo.append(String.format("%15s", registros.TipoUnidades).replace(' ', ' '));
						archivo.append(String.format("%15s", onlyNumbers(registros.Valorizacion, false)).replace(' ', '0'));
						archivo.append(String.format("%19s", onlyNumbers(registros.CarteraPropia, true)).replace(' ', '0')); 
						archivo.append(String.format("%19s", onlyNumbers(registros.CustodiaTerceros, true)).replace(' ', '0'));
						archivo.append(String.format("%19s", onlyNumbers(registros.CustodiaPropiaDCV, true)).replace(' ', '0'));
						archivo.append(String.format("%19s", onlyNumbers(registros.CustodiaTercerosDCV, true)).replace(' ', '0'));
						archivo.append(String.format("%19s", onlyNumbers(registros.CustodiaSociedadEmisora, true)).replace(' ', '0'));
						archivo.append(String.format("%19s", onlyNumbers(registros.CustodiaIntermediario, true)).replace(' ', '0'));
						archivo.append(String.format("%19s", onlyNumbers(registros.CustodiaBolsas, true)).replace(' ', '0'));
						archivo.append(String.format("%19s", onlyNumbers(registros.CustodiaOtros, true)).replace(' ', '0'));
						archivo.append("\n");
						cantidad++;
					}
				}
	        	
	        	// -------------------------------------------------------------
			    // GENERA REGISTRO 03 (Registro de totales del Archivo)
			    // -------------------------------------------------------------
	        	archivo.append("3");
	        	archivo.append(String.format("%10s", cantidad + 2).replace(' ', '0'));
	        	archivo.append(String.format("%251s", ' ').replace(' ', ' '));
	        	
	        	out.P_OUT_ARCHIVO = archivo.toString();
			}
		
		}
		finally {
		   //con.close();	
		}
				
		return out;
	};
			
	private String onlyNumbers(BigDecimal label, boolean decimal) {
		if(decimal == true) {
			label = label.setScale(4);
			return label.toPlainString().replaceAll("[^0-9]", "");
		}else {
			label = label.setScale(0);
			return label.toPlainString().replaceAll("[^0-9]", "");
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarArchivoCircular1720_Entrada jin=gson.fromJson(in.toString() ,GenerarArchivoCircular1720_Entrada.class);
		GenerarArchivoCircular1720_Salida out=logica(conn,jin);
		//out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();					
			    String json ="{\"p_IdEmpresa\":1,\"p_FechaConsulta\":\"2020-07-27\",\"p_IdSolicitudCircular\":0,\"p_IdTipoDocumento\":\"CIR1720\",\"P_TABLAPARAMETROS\":[{\"TipoParametro\":\"IdEmpresa\",\"Valor\":1},{\"TipoParametro\":\"fechaHasta\",\"Valor\":\"2020-07-27\"}]}";
								
				GenerarArchivoCircular1720_Entrada Objin = gson.fromJson(json, GenerarArchivoCircular1720_Entrada.class);			
				GenerarArchivoCircular1720 generarCirc1720 = new GenerarArchivoCircular1720();
				GenerarArchivoCircular1720_Salida Objout = generarCirc1720.logica(con, Objin);				
				System.out.println(Objout.toString());
			//	con.commit();
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};
};

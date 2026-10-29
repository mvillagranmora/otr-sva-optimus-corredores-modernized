package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public  class CrearCargaMasiva implements JavaCall{
	private final String SQL_INSERT_CREA_CABECERA              = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargaMasiva_InsertCreaCabecera.sql");
	private final String SQL_INSERT_CREA_DETALLE               = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargaMasiva_InsertCreaDetalle.sql");
	private final String SQL_SELECT_OBTIENE_IDCONCEPTO         = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargaMasiva_SelectIdConcepto.sql");
	
	/******************************************************************************
	 * 							CLASES
	 *******************************************************************************/
    public static class EntradaCargaMasiva{
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaCarga;
		public String p_TipoCargaSeleccionada;
		public List<DetalleCargaMasiva> p_CargaMasiva;
  	    public List<DetalleTransancciones>p_CargaMasivaDetalle;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
    public static class DetalleCargaMasiva{		
		public Long 	IdInterno;			  
		public String   CodigoTipoCargaMasiva;
		public String 	NombreArchivo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    public static class DetalleTransancciones{
    	 public Long IdInterno;
    	 public Long IdRegistro;
    	 public String RutCliente;
    	 public String Portafolio;    	 
		 public String Moneda;
		 public String TipoMovimiento;
		 public String IdConcepto;
		 public BigDecimal Monto;
		 public String Descripcion;
		 public String Plan;
		 public String ProductoAgrupador;
		// public String IdEstado;
		// public String ObservacionValidacion;
		 public String FechaGeneracion;
		 public String FechaInicioServ;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    
    public static class DetalleTransancciones2{
   	
		 public String IdentServicio;
		 public String tipo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
     
	public static class MensajeTransaccion	
	{	
	
		public static String		codBanco;     			

		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}		
	public static class SalidaCarga{
	
		public SalidaCarga() {
			super();
		}
		public String	 mensaje;
		
	}
	public static class SalidaCargaMasiva{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
		
			public SalidaCargaMasiva(){
				this.P_OUT_ERRORESCONTADOR = 0L;
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
	}	
	
	public static class ConsultaSecuencia{
		public Long	 IdCargaMasivaP;
		
	}
	
	 public static class SelectCabecera{
	     public Long IdCargaMasiva;
	     public String CodigoTipoCargaMasiva;
	     public Long IdEmpresa;
	     public Long IdUsuario;
	     public Integer CantidadRegistros;
	     public BigDecimal MontoTotal;
	     public String NombreArchivo;
	     public String FechaCarga;
	     public String FechaProceso;
	     public String IdMoneda;
	     public String IdConcepto;
	     public String toString() {
				return Utils.printJson(this, false);
			}
	 }
		 public static class SelectDetalle{
			 public Long IdCargaMasivaDetalle;
			 public Long IdCargaMasiva;
			 public String DocumentoIdentidad;
			 public String IdPortafolio;
			 public String IdMoneda;
			 public String IdTipoMovimiento;
			 public String IdConcepto;
			 public BigDecimal Monto;
			 public String Descripcion;
			 public String Plan;
			 public String ProductoAgrupador;
			 public String IdEstado;
			 public String ObservacionValidacion;			
		     public String toString() {
					return Utils.printJson(this, false);
				}
    
	 }

			public static class ConsultaIdConcepto{
				public String	IdConcepto;
				
			}
			
	public long SecuenciaCab;
	public long SecuenciaDet;
		/******************************************************************************
		 * 						OBJETO DE TRANSPORTE
		 ******************************************************************************/
		@Override
		public JsonObject call(Connection conn, JsonObject jin) throws Exception {
			Gson gson = new Gson();		
			EntradaCargaMasiva in = gson.fromJson(jin.toString() ,EntradaCargaMasiva.class);
			
			//RecaudacionBCP_Entrada in = gson.fromJson(jin.toString(), RecaudacionBCP_Entrada.class);
			SalidaCargaMasiva out = logica(conn, in);
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			JsonParser parser = new JsonParser();
			JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		/******************************************************************************
		 * 							LOGICA
		 ******************************************************************************/
		
		@SuppressWarnings("unlikely-arg-type")
		public SalidaCargaMasiva logica(Connection conn, EntradaCargaMasiva entrada) {
			
			SQL sql = new SQL();
			SalidaCargaMasiva Salida = new  SalidaCargaMasiva();
			Salida.P_OUT_ERRORESCONTADOR= 0L;
			Long contador = 0L;	
			try {						
				 for(DetalleTransancciones Detalle : entrada.p_CargaMasivaDetalle) {
					 if  (!Detalle.TipoMovimiento.equals(entrada.p_TipoCargaSeleccionada))
					 {
						    
							Salida.P_OUT_ERRORESCONTADOR= 1L;
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR","Existe un tipo movimiento que no corresponde al tipo de carga seleccionado"));
							return Salida;
							
					 }
				 }
						
				//---------------													
							String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

							//Obtener secuencia cabecera
							HashMap<String,Object> paramsec = new HashMap<String,Object>();
							List<ConsultaSecuencia> ParamIdCargaMasiva = new ArrayList<ConsultaSecuencia>();							

							List<Long> secuencias = Utils.getSecuencia(conn, "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasiva", new Long(entrada.p_CargaMasiva.size()));
							SecuenciaCab=secuencias.get(0);
							
												  					        
							//Agrega cabecera
							List<SelectCabecera> paramInsertMensajeTransaccionU2 = new ArrayList<SelectCabecera>();	
							
							for(DetalleCargaMasiva cabecera : entrada.p_CargaMasiva) {
								SelectCabecera DetSelectCabecera = new SelectCabecera ();
								DetSelectCabecera.IdCargaMasiva=SecuenciaCab;
								DetSelectCabecera.CodigoTipoCargaMasiva=cabecera.CodigoTipoCargaMasiva; 
								DetSelectCabecera.IdEmpresa=entrada.p_IdEmpresa;
								DetSelectCabecera.IdUsuario=entrada.p_IdUsuario;
								DetSelectCabecera.CantidadRegistros=entrada.p_CargaMasivaDetalle.size();
								DetSelectCabecera.MontoTotal= BigDecimal.valueOf(0) ; //Se deja en cero por que son multiples monedas
								DetSelectCabecera.NombreArchivo=cabecera.NombreArchivo;
								DetSelectCabecera.FechaCarga=entrada.p_FechaCarga;
								DetSelectCabecera.FechaProceso=fechaSistema;
								DetSelectCabecera.IdMoneda="";			
							
								HashMap<String, Object> params = new HashMap<>();
								params.put("p_CodigoTipoCargaMasiva",cabecera.CodigoTipoCargaMasiva);
								List<ConsultaIdConcepto> lstConcepto = new ArrayList<ConsultaIdConcepto>();
								
								lstConcepto=sql.query(conn, SQL_SELECT_OBTIENE_IDCONCEPTO, params, ConsultaIdConcepto.class);
								if (lstConcepto.size()>0) {
									DetSelectCabecera.IdConcepto=lstConcepto.get(0).IdConcepto;
								}
								paramInsertMensajeTransaccionU2.add(DetSelectCabecera);									
							}										
						    sql.batch(conn, SQL_INSERT_CREA_CABECERA, paramInsertMensajeTransaccionU2, SelectCabecera.class);			    
						  
						  //Registrar  Detalle
							List<SelectDetalle> paramInsertMensajeTransaccionDet = new ArrayList<SelectDetalle>();							
						    for(DetalleTransancciones Detalle : entrada.p_CargaMasivaDetalle) {
						    	
								secuencias = Utils.getSecuencia(conn, "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasivaDetalle", 1L);
								SecuenciaDet=secuencias.get(0);								
						    	SelectDetalle DetSelectDetalle = new SelectDetalle ();
						    	DetSelectDetalle.IdCargaMasivaDetalle=SecuenciaDet;
						    	DetSelectDetalle.IdCargaMasiva = SecuenciaCab;
					    		DetSelectDetalle.DocumentoIdentidad= Detalle.RutCliente;						    			
						    	//System.out.println(DetSelectDetalle.DocumentoIdentidad);
					    		DetSelectDetalle.IdPortafolio= Detalle.Portafolio;						    	 
						    	DetSelectDetalle.IdTipoMovimiento= Detalle.TipoMovimiento;
						    	DetSelectDetalle.IdMoneda= Detalle.Moneda;
						    	DetSelectDetalle.IdConcepto= Detalle.IdConcepto;
						    	DetSelectDetalle.Monto=Detalle.Monto; 
						    	DetSelectDetalle.Descripcion= Detalle.Descripcion;
						    	DetSelectDetalle.Plan  = Detalle.Plan;
						    	DetSelectDetalle.ProductoAgrupador  = Detalle.ProductoAgrupador;
						    	//System.out.println(DetSelectDetalle.Descripcion);
						    	DetSelectDetalle.IdEstado= "OK";
						    	DetSelectDetalle.ObservacionValidacion= "";
						    	paramInsertMensajeTransaccionDet.add(DetSelectDetalle);
						    	contador++;
						    	
						    }
							
					    	sql.batch(conn, SQL_INSERT_CREA_DETALLE, paramInsertMensajeTransaccionDet, SelectDetalle.class);
					    	Salida.P_OUT_ERRORESCONTADOR= 0L;
							Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO","Archivo Cargado con exito, se cargaron "+ contador +" registros."));
				}
							
			catch (Exception e)
			{
				System.out.println(e);
				Salida.P_OUT_ERRORESCONTADOR= 1L;
				Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR","Archivo presenta problemas en la linea : "+ contador));
				e.printStackTrace();
				
			}
			
			return Salida;
		}


		
		/******************************************************************************
		 * 							MAIN
		 ******************************************************************************/
		public static void main(String[] args){
			
			try (Connection con = PlataformaUtils.getBOLSAConnection())
			{
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
//OK			String jsonUniverso = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2019-06-07\",\"p_CargaMasiva\":[{\"IdInterno\":1,\"NombreArchivo\":\"ArchivoPrueba.txt\",\"CodigoTipoCargaMasiva\":\"CARGA_ABONO\"}],\"p_CargaMasivaDetalle\":[{\"IdInterno\":1,\"RutCliente\":\"15731790-3\",\"Portafolio\":\"1\",\"Moneda\":\"CLP\",\"TipoMovimiento\":\"MOVABN\",\"IdConcepto\":\"AAAAAA\",\"Monto\":\"1000\",\"Descripcion\":\"REGISTRO_1\"},{\"IdInterno\":2,\"RutCliente\":\"15731790-3\",\"Portafolio\":\"1\",\"Moneda\":\"CLP\",\"TipoMovimiento\":\"MOVABN\",\"IdConcepto\":\"AAAAAA\",\"Monto\":\"2000\",\"Descripcion\":\"REGISTRO_2\"},{\"IdInterno\":3,\"RutCliente\":\"15731790-3\",\"Portafolio\":\"1\",\"Moneda\":\"CLP\",\"TipoMovimiento\":\"MOVABN\",\"IdConcepto\":\"AAAAAA\",\"Monto\":\"3000\",\"Descripcion\":\"REGISTRO_3\"}]}";
				String jsonUniverso = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_TipoCargaSeleccionada\":\"MOVCAR\",\"p_FechaCarga\":\"2021-09-29\",\"p_CargaMasiva\":[{\"IdInterno\":1,\"NombreArchivo\":\"CUENTA INVERSION.txt\",\"CodigoTipoCargaMasiva\":\"CARGA_CARGO\"}],\"p_CargaMasivaDetalle\":[{\"IdInterno\":1,\"RutCliente\":\"15443645-6\",\"Portafolio\":\"APV-B\",\"Moneda\":\"CLP\",\"TipoMovimiento\":\"MOVCAR\",\"IdConcepto\":\"CARGCI\",\"Monto\":\"10000\",\"Descripcion\":\"CLP\",\"Plan\":\"ACTIVO\",\"ProductoAgrupador\":\"ACCIONES\"},{\"IdInterno\":2,\"RutCliente\":\"15443645-6\",\"Portafolio\":\"0\",\"Moneda\":\"CLP\",\"TipoMovimiento\":\"MOVCAR\",\"IdConcepto\":\"CARGCI\",\"Monto\":\"10000\",\"Descripcion\":\"CLP\",\"Plan\":\"null\",\"ProductoAgrupador\":\"null\"}]}";
				EntradaCargaMasiva entrada = gson.fromJson(jsonUniverso, EntradaCargaMasiva.class);
				CrearCargaMasiva demo = new CrearCargaMasiva();				
				SalidaCargaMasiva salida = demo.logica(con, entrada);				
				System.out.println("salida --> "+salida.toString());
				//con.commit();//--rollback();
				
			}catch(Exception e){
				e.printStackTrace(); 
			}
			
			System.out.println("== FIN PRUEBA LOCAL ==");
		}

}

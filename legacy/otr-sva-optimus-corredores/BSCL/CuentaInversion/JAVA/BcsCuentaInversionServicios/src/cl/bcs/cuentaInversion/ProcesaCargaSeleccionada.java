package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargaMasiva.ConsultaIdConcepto;
import cl.bcs.cuentaInversion.CrearCargaMasiva.SelectCabecera;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjAdvertenciasDetalle;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjErroresDetalle;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjInformacionDetalle;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjProcesamientoCargoAbonoAGenerar;
import cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos.InFondos;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public  class ProcesaCargaSeleccionada implements JavaCall{
	
	private final String SQL_SELECT_OBTIENE_DETALLE             = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectObtieneDetalleCarga.sql");
	private final String SQL_UPDATE_ACTUALIZA_ESTADO_CABECERA   = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_UpdateActualizaEstadoCabecera.sql");
	private final String SQL_UPDATE_ACTUALIZA_DETALLE           = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_UpdateActualizaDetalle.sql");

	/******************************************************************************
	 * 							CLASES
	 *******************************************************************************/

    public static class EntradaProcesamientoMasivo{
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaCarga;
		public String p_IdCargaMasiva;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class ObjDetalleCargoAbonoRegistrado {				
		public Long IdCargaMasivaDetalle;
		public Long IdCargaMasiva;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String IdMoneda;
		public String IdTipoMovimiento;
		public String IdConcepto;
		public BigDecimal Monto;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
		
	public static class MensajeTransaccion	
	{		
		public static String		codBanco;     	
		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}		
	
	public static class SalidaProcesamiento{
	
		public SalidaProcesamiento() {
			super();
		}
		public String	 mensaje;
		
	}
	public static class SalidaProcesamientoMasivo{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
		
			public SalidaProcesamientoMasivo(){
				this.P_OUT_ERRORESCONTADOR = 0L;
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
	}	
	
	 public static class SelectCabecera{
	     public Long IdCargaMasiva;
	     public String CodigoTipoCargaMasiva;
	     public Long IdEmpresa;
	     public Long IdUsuario;
	     public Integer CantidadRegistros;
	     public Double MontoTotal;
	     public String NombreArchivo;
	     public String FechaCarga;
	     public String FechaProceso;
	     public String IdMoneda;
	     public String IdConcepto;
	     public String toString() {
				return Utils.printJson(this, false);
			}
	 }
		
	 
	 public static class ConsultaCargaMasiva{
		    public String	p_IdEstado;
			public String	p_IdCargaMasiva;
					
		}

	 public static class ProcesaSalidaCargoAbono{
	     public Long   p_IdCargaMasivaDetalle;
	     public String p_IdEstado;
	     public String p_ObservacionValidacion;
	     public String p_Descripcion;
	   
	     public String toString() {
				return Utils.printJson(this, false);
			}
	 }
	 
	 public List<ProcesaSalidaCargoAbono>	SalidaCargaAbono;
		 
		public long NumeroCargoAbono;
		public long ContadorErroresDetalle=0;
		
		/******************************************************************************
		 * 						OBJETO DE TRANSPORTE
		 ******************************************************************************/
		@Override
		public JsonObject call(Connection conn, JsonObject jin) throws Exception {
			Gson gson = new Gson();		
			EntradaProcesamientoMasivo in = gson.fromJson(jin.toString() ,EntradaProcesamientoMasivo.class);
			
			SalidaProcesamientoMasivo out = logica(conn, in);
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			JsonParser parser = new JsonParser();
			JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		/******************************************************************************
		 * 							LOGICA
		 * @throws SQLException 
		 ******************************************************************************/
		
		@SuppressWarnings("unlikely-arg-type")
		public SalidaProcesamientoMasivo logica(Connection conn, EntradaProcesamientoMasivo entrada) throws SQLException {
			
			SQL sql = new SQL();
			SalidaProcesamientoMasivo Salida = new  SalidaProcesamientoMasivo();
			Salida.P_OUT_ERRORESCONTADOR= 0L;
			Long contador = 0L;	
			try {		
				
							String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");							
							HashMap<String,Object> parmsIn =new HashMap<String,Object>();
							parmsIn.put("p_IdCargaMasivaDetalle", entrada.p_IdCargaMasiva);							
							List<ObjDetalleCargoAbonoRegistrado> p_out_DetalleCargaGenerada = sql.query(conn, SQL_SELECT_OBTIENE_DETALLE,parmsIn,ObjDetalleCargoAbonoRegistrado.class);							
							List<ObjCargoAbonoAGenerar> listagen = new ArrayList<>();
							
							for(ObjDetalleCargoAbonoRegistrado DetalleCarga : p_out_DetalleCargaGenerada) 
							{								
								ObjCargoAbonoAGenerar CampoDetalle = new ObjCargoAbonoAGenerar(); 
								CampoDetalle.NumeroCargoAbono= DetalleCarga.IdCargaMasivaDetalle;           
								CampoDetalle.IdPortafolio= DetalleCarga.IdPortafolio;                
								CampoDetalle.IdMoneda= DetalleCarga.IdMoneda;                    
								CampoDetalle.IdTipoMovimiento= DetalleCarga.IdTipoMovimiento;            
								CampoDetalle.IdConcepto= DetalleCarga.IdConcepto;                  
								CampoDetalle.Descripcion= DetalleCarga.Descripcion;
								CampoDetalle.IdEjecutivo= 0L;
								CampoDetalle.Monto= DetalleCarga.Monto;

								CampoDetalle.IdCondicionDeLiquidacion= "T0";
								CampoDetalle.FechaCondicionDeLiquidacion= fechaSistema;                    
								CampoDetalle.IdSucursal= 0L;                  
								CampoDetalle.IdTipoProducto= "";              
								CampoDetalle.IdTipoOperacion= "";            
								CampoDetalle.IdOperacion= 0L;                 
								CampoDetalle.FechaOperacion= "0001-01-01";             
								CampoDetalle.FechaLiquidacionOperacion= "0001-01-01"; 
								CampoDetalle.FechaVencimientoOperacion= null;
								CampoDetalle.IdMonedaOperacion= "";       
								CampoDetalle.MontoOperacion=  null;          
								CampoDetalle.TipoCambioOperacion= new BigDecimal(0);     
								CampoDetalle.IdContraparte= null;               
								CampoDetalle.IdTipoComprobante= null;           
								CampoDetalle.IdComprobante= null;            
								CampoDetalle.FolioComprobante= null;          
								CampoDetalle.IdComprobanteDetalle= null;      
								CampoDetalle.IdComprobanteFormaDePago= null;  
								CampoDetalle.IdTipoMovimientoAsociado= null;    
								CampoDetalle.IdMovimientoAsociado=  null;      
								CampoDetalle.FechaComprobante= null;          
								CampoDetalle.IdPersonaPagoCobro= null;          
								CampoDetalle.IdInstrumento= null;               
								CampoDetalle.IdTipoInstrumento= null;           
								CampoDetalle.Cantidad= new BigDecimal(0);             
								CampoDetalle.Nemo= "";                        
								CampoDetalle.EsIndicadorBilateral= "N";      
								CampoDetalle.EsCargoAbonoPorTraspaso= "N";  
								CampoDetalle.IdPortafolioTraspaso= null;
								CampoDetalle.GeneroContraparteEnTesoreria= "N";
								CampoDetalle.CertificadoAutomatico= "N";
								
								listagen.add(CampoDetalle);
  						}
					CrearCargoAbono GeneraCargoAbonoInt = new CrearCargoAbono();								
					CrearCargoAbonoEntrada CreaEntrada = new CrearCargoAbonoEntrada();
					long startTime = System.currentTimeMillis();
					CreaEntrada.p_IdEmpresa	= entrada.p_IdEmpresa;
					CreaEntrada.p_IdUsuario = entrada.p_IdUsuario;
					CreaEntrada.p_IdModulo = "CTA";
					CreaEntrada.p_simularOperacion  = "N"; 
					CreaEntrada.p_UsaReglasNegocio = "S";
					CreaEntrada.p_FechaSistema = fechaSistema;
					CreaEntrada.p_EsIngresoManual	= "S";								
					CreaEntrada.p_CargoAbonoAGenerar = listagen;
					CrearCargoAbonoSalida Salida_ObjCargoAbonoAGenerar = GeneraCargoAbonoInt.logica(conn, CreaEntrada);
					Salida.P_OUT_ERRORESCONTADOR += Salida_ObjCargoAbonoAGenerar.P_OUT_ERRORESCONTADOR;
					
					SalidaCargaAbono= new ArrayList<>();
					List<String> descripcionesError = new ArrayList<>();

					for(MensajeSalida item : Salida_ObjCargoAbonoAGenerar.P_OUT_MENSAJES )
					{	
						ProcesaSalidaCargoAbono VarIngreso = new  ProcesaSalidaCargoAbono();
						VarIngreso.p_IdCargaMasivaDetalle=item.id;
						if (item.tipo.equals("ERROR"))
						{
							VarIngreso.p_IdEstado="INV";
					        descripcionesError.add(item.descripcion);
						}
						else
						{
							VarIngreso.p_IdEstado="OK";
					        Salida.P_OUT_MENSAJES.add(item);
						}
						
						VarIngreso.p_ObservacionValidacion=item.codigo;
						VarIngreso.p_Descripcion=item.descripcion;
						SalidaCargaAbono.add(VarIngreso);
						if (item.tipo.equals("ERROR"))
						{
							ContadorErroresDetalle++;
						}
					}
					
					HashMap<Long, Object> mapaIdsProcesados = Utils.List2Hash(Salida_ObjCargoAbonoAGenerar.P_OUT_MENSAJES, "id");
					    
					descripcionesError.addAll(
					    p_out_DetalleCargaGenerada.stream()
					        .filter(detalle -> !mapaIdsProcesados.containsKey(detalle.IdCargaMasivaDetalle))
					        .map(detalle -> detalle.DocumentoIdentidad)
					        .collect(Collectors.toList())
					);

					if (!descripcionesError.isEmpty())
					{
					    HashMap<String, String> valores = new HashMap<String, String>();
					    valores.put("cantidad", String.valueOf(descripcionesError.size()));
					    Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_CARGAS_NO_PROCESADAS", valores, conn, entrada.p_IdEmpresa));
					}
					
					sql.batch(conn, SQL_UPDATE_ACTUALIZA_DETALLE, SalidaCargaAbono, ProcesaSalidaCargoAbono.class);
					
					List<ConsultaCargaMasiva> paramIdCargaMasiva = new ArrayList<ConsultaCargaMasiva>();
					ConsultaCargaMasiva ObjEntradaProcesamientoMasivo = new ConsultaCargaMasiva();
				
					if (ContadorErroresDetalle==0) 
					{
						ObjEntradaProcesamientoMasivo.p_IdEstado ="PRO";
						ObjEntradaProcesamientoMasivo.p_IdCargaMasiva= entrada.p_IdCargaMasiva;
						paramIdCargaMasiva.add(ObjEntradaProcesamientoMasivo);
						sql.batch(conn, SQL_UPDATE_ACTUALIZA_ESTADO_CABECERA, paramIdCargaMasiva, ConsultaCargaMasiva.class);	
					}
					else 
					{
						ObjEntradaProcesamientoMasivo.p_IdEstado ="INV";
						ObjEntradaProcesamientoMasivo.p_IdCargaMasiva= entrada.p_IdCargaMasiva;
						paramIdCargaMasiva.add(ObjEntradaProcesamientoMasivo);
						sql.batch(conn, SQL_UPDATE_ACTUALIZA_ESTADO_CABECERA, paramIdCargaMasiva, ConsultaCargaMasiva.class);	
					}
					
					conn.commit();
					
				}
							
			catch (Exception e)
			{
				System.out.println(e);
				Salida.P_OUT_ERRORESCONTADOR= 1L;
				Salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR"," Error en procesamiento de carga de archivo cargado, verifique!"));
				e.printStackTrace();
				
			}
			
			 return Salida;		
		}


		
		/******************************************************************************
		 * 							MAIN
		 ******************************************************************************/
		public static void main(String[] args){
			
			try (Connection con = PlataformaUtils.getMBIConnection())
			{
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String jsonUniverso = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2019-04-30\",\"p_IdCargaMasiva\":2}";
				EntradaProcesamientoMasivo entrada = gson.fromJson(jsonUniverso, EntradaProcesamientoMasivo.class);
				ProcesaCargaSeleccionada demo = new ProcesaCargaSeleccionada();				
				SalidaProcesamientoMasivo salida = demo.logica(con, entrada);				
				System.out.println("salida --> "+salida.toString());
				//con.commit();//--rollback();
				
			}catch(Exception e){
				e.printStackTrace(); 
			}
			
			System.out.println("== FIN PRUEBA LOCAL ==");
		}

}
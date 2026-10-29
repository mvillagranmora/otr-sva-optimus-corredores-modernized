package cl.bcs.custodia;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public  class CrearCargaMasivaISFUT implements JavaCall{
	private final String SQL_SELECT_EVENTO_CAPITAL = "SELECT EC.\"TasaEfectiva\" FROM \"BSCL.Custodia.BD::CUS.EventoCapital\" EC,BSCL.\"BSCL.Custodia.BD::CUS.EventoCapitalCliente\" ECC WHERE EC.\"IdEmpresa\" = :p_IdEmpresa AND ECC.\"IdEventoCapital\"=EC.\"IdEventoCapital\" AND ECC.\"IdEventoCapitalCliente\" IN (:IdEventoCapitalCliente)";
	private final String SQL_UPDATE_CARGA_MASIVA = loadResourceToString("cl/bcs/custodia/sql/CrearCargaMasivaISFUT_UpdateCargaMasiva.sql");
	private final String SQL_INSERT_CARGA_MASIVA = loadResourceToString("cl/bcs/custodia/sql/CrearCargaMasivaISFUT_InsertCargaMasiva.sql");
	private final String SQL_INSERT_CARGA_MASIVA_DETALLE = loadResourceToString("cl/bcs/custodia/sql/CrearCargaMasivaISFUT_InsertCargaMasivaDetalle.sql");
	
		
		/******************************************************************************
		 * 							CLASES
		******************************************************************************/
    public static class EntradaCargaMasivaISFUT{
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaCarga;
		public List<CargaMasiva> p_CargaMasiva;
		public List<DetalleCargaMasiva>p_CargaMasivaDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    public static class CargaMasiva{		
		public Long IdCargaMasiva;
		public Long IdEmpresa;
		public Long IdUsuario;
		public Long CantidadRegistros;
		public String NombreArchivo;
		public String FechaCarga;
		public String CodigoTipoCargaMasiva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    
    public static class CargaMasiva2{		
		public Long IdCargaMasiva;
	}
    
    public static class DetalleCargaMasiva{
    	public Long IdCargaMasivaDetalleISFUT;
    	public Long IdCargaMasivaISFUT;
    	public Long IdEventoCapitalCliente;
    	public String IdTipoEventoCapital;
    	public String NumeroDividendo;
    	public String Secuencia;
    	public String TipoSocioNegocio;
    	public String CodigoPortafolio;
    	public String IdClasificacionTipoInstrumento;
    	public String Nemo;
    	public String ISIN;
    	public String NemoNuevo;
    	public String IdConceptoGarantia;
    	public String TipoCartera;
    	public String DocumentoIdentidad;
    	public String NombreCliente;
    	public String ConfiguracionNotificacion;
    	public String TienePreferenciaDePago;
    	public String MontoCapital;
    	public String Precio;
    	public String Factor;
    	public String FactorNuevo;
    	public String PrecioNuevo;
    	public String CantidadNueva;
    	public String MontoInteres;
    	public String MontoReajuste;
    	public String CantidadDerecho;
    	public String Total;
    	public String MontoRetencion;
    	public String CantidadDerechoArchivo;
    	public String AccionesDisponiblesArchivo;
    	public String DiferenciaCantidadDerecho;
    	public String DiferenciaAccionesCantidadNueva;
    	public String MontoBeneficioArchivo;
    	public String DiferenciaMontoBeneficio;
    	public String MontoInteresArchivo;
    	public String MontoCapitalArchivo;
    	public String DiferenciaMontoInteres;
    	public String DiferenciaMontoCapital;
    	public String MontoRetencionArchivo;
    	public String DiferenciaMontoRetencion;
    	public String IdMoneda;
    	public String FechaPago;
    	public String CodigoUsuarioAgente;
    	public String NombreAgente;
    	public String IdBoveda;
    	public String IdEstado;
    	public String Origen;
    	public String EstadoNotificacion;
    	public String NombreMedioDePago;
    	public String TipoCuentaBoveda;
    	public String NumeroCuentaBoveda;
    	public String PagaClienteCorredor;
    	public String IdTipoReajuste;
    	public String INGMontoInteresCorteCupon;
    	public String Amortizacion;
    	public String AfectoaClearing;
    	public String ProcesadoClearing;
    	public String IdRegimenFondoMutuo;
    	public String ISFUT;
    	public String RetencionISFUT;
	public BigDecimal MontoRetencionISFUT;
    	public String Estado;
    	public String TipoArchivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

    public static class DetalleTransanccionesUni2{
   	
		 public String IdentServicio;
		 public String tipo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
   
	public static class SalidaCarga{
	
		public SalidaCarga() {
			super();
		}
		public String	 mensaje;
		
	}
	public static class SalidaCargaMasivaISFUT{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
		
			public SalidaCargaMasivaISFUT(){
				this.P_OUT_ERRORESCONTADOR = 0L;
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
	}			
	public static class EventoExiste {
                public BigDecimal        TasaEfectiva;                
                            
            public String toString() {
                        return Utils.printJson(this, false);
                }
        }
		/******************************************************************************
		 * 						OBJETO DE TRANSPORTE
		 ******************************************************************************/
		@Override
		public JsonObject call(Connection conn, JsonObject jin) throws Exception {
			Gson gson = new Gson();		
			EntradaCargaMasivaISFUT in = gson.fromJson(jin.toString() ,EntradaCargaMasivaISFUT.class);
			
			//RecaudacionBCP_Entrada in = gson.fromJson(jin.toString(), RecaudacionBCP_Entrada.class);
			SalidaCargaMasivaISFUT out = logica(conn, in);
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			JsonParser parser = new JsonParser();
			JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		/******************************************************************************
		 * 							LOGICA
		 ******************************************************************************/
		
		public SalidaCargaMasivaISFUT logica(Connection conn, EntradaCargaMasivaISFUT entrada) {
			
			SQL sql = new SQL();
			SalidaCargaMasivaISFUT Salida = new  SalidaCargaMasivaISFUT();
			Salida.P_OUT_ERRORESCONTADOR= 0L;
			HashMap<String, Object> params = new HashMap<String, Object>();
                        List<EventoExiste> TasaEfectiva = new ArrayList<EventoExiste>();

			try {		
				
				//---------------
				for (CargaMasiva carga0 : entrada.p_CargaMasiva){
					CargaMasiva _mensajeCargaMasiva = new CargaMasiva();
					_mensajeCargaMasiva.CodigoTipoCargaMasiva = carga0.CodigoTipoCargaMasiva;
					
					//**********************************************************************************
					if(_mensajeCargaMasiva.CodigoTipoCargaMasiva.equals("CARGA_CLIENTES_ISFUT")){

							List<DetalleCargaMasiva> paramInsertMensajeTransaccionISFUT = new ArrayList<DetalleCargaMasiva>();
							Long contador = (long) 0;
							List<Long> secuencias = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdCargaMasivaISFUT", new Long(1));
							
							for (DetalleCargaMasiva carga : entrada.p_CargaMasivaDetalle){
								
								DetalleCargaMasiva _mensajeTransaccionP = new DetalleCargaMasiva();  
								_mensajeTransaccionP.IdCargaMasivaDetalleISFUT = carga.IdCargaMasivaDetalleISFUT;
								_mensajeTransaccionP.IdCargaMasivaISFUT = secuencias.get(0);
								_mensajeTransaccionP.IdEventoCapitalCliente = carga.IdEventoCapitalCliente;
								_mensajeTransaccionP.IdTipoEventoCapital = carga.IdTipoEventoCapital;
								_mensajeTransaccionP.NumeroDividendo = carga.NumeroDividendo;
								_mensajeTransaccionP.Secuencia = carga.Secuencia;
								_mensajeTransaccionP.TipoSocioNegocio = carga.TipoSocioNegocio;
								_mensajeTransaccionP.CodigoPortafolio = carga.CodigoPortafolio;
								_mensajeTransaccionP.IdClasificacionTipoInstrumento = carga.IdClasificacionTipoInstrumento;
								_mensajeTransaccionP.Nemo = carga.Nemo;
								_mensajeTransaccionP.ISIN = carga.ISIN;
								_mensajeTransaccionP.NemoNuevo = carga.NemoNuevo;
								_mensajeTransaccionP.IdConceptoGarantia = carga.IdConceptoGarantia;
								_mensajeTransaccionP.TipoCartera = carga.TipoCartera;
								_mensajeTransaccionP.DocumentoIdentidad = carga.DocumentoIdentidad;
								_mensajeTransaccionP.NombreCliente = carga.NombreCliente;
								_mensajeTransaccionP.ConfiguracionNotificacion = carga.ConfiguracionNotificacion;
								_mensajeTransaccionP.TienePreferenciaDePago = carga.TienePreferenciaDePago;
								_mensajeTransaccionP.MontoCapital = carga.MontoCapital;
								_mensajeTransaccionP.Precio = carga.Precio;
								_mensajeTransaccionP.Factor = carga.Factor;
								_mensajeTransaccionP.FactorNuevo = carga.FactorNuevo;
								_mensajeTransaccionP.PrecioNuevo = carga.PrecioNuevo;
								_mensajeTransaccionP.CantidadNueva = carga.CantidadNueva;
								_mensajeTransaccionP.MontoInteres = carga.MontoInteres;
								_mensajeTransaccionP.MontoReajuste = carga.MontoReajuste;
								_mensajeTransaccionP.CantidadDerecho = carga.CantidadDerecho;
								_mensajeTransaccionP.Total = carga.Total;
								_mensajeTransaccionP.MontoRetencion = carga.MontoRetencion;
								_mensajeTransaccionP.CantidadDerechoArchivo = carga.CantidadDerechoArchivo;
								_mensajeTransaccionP.AccionesDisponiblesArchivo = carga.AccionesDisponiblesArchivo;
								_mensajeTransaccionP.DiferenciaCantidadDerecho = carga.DiferenciaCantidadDerecho;
								_mensajeTransaccionP.DiferenciaAccionesCantidadNueva = carga.DiferenciaAccionesCantidadNueva;
								_mensajeTransaccionP.MontoBeneficioArchivo = carga.MontoBeneficioArchivo;
								_mensajeTransaccionP.DiferenciaMontoBeneficio = carga.DiferenciaMontoBeneficio;
								_mensajeTransaccionP.MontoInteresArchivo = carga.MontoInteresArchivo;
								_mensajeTransaccionP.MontoCapitalArchivo = carga.MontoCapitalArchivo;
								_mensajeTransaccionP.DiferenciaMontoInteres = carga.DiferenciaMontoInteres;
								_mensajeTransaccionP.DiferenciaMontoCapital = carga.DiferenciaMontoCapital;
								_mensajeTransaccionP.MontoRetencionArchivo = carga.MontoRetencionArchivo;
								_mensajeTransaccionP.DiferenciaMontoRetencion = carga.DiferenciaMontoRetencion;
								_mensajeTransaccionP.IdMoneda = carga.IdMoneda;
								_mensajeTransaccionP.FechaPago = carga.FechaPago;
								_mensajeTransaccionP.CodigoUsuarioAgente = carga.CodigoUsuarioAgente;
								_mensajeTransaccionP.NombreAgente = carga.NombreAgente;
								_mensajeTransaccionP.IdBoveda = carga.IdBoveda;
								_mensajeTransaccionP.IdEstado = carga.IdEstado;
								_mensajeTransaccionP.Origen = carga.Origen;
								_mensajeTransaccionP.EstadoNotificacion = carga.EstadoNotificacion;
								_mensajeTransaccionP.NombreMedioDePago = carga.NombreMedioDePago;
								_mensajeTransaccionP.TipoCuentaBoveda = carga.TipoCuentaBoveda;
								_mensajeTransaccionP.NumeroCuentaBoveda = carga.NumeroCuentaBoveda;
								_mensajeTransaccionP.PagaClienteCorredor = carga.PagaClienteCorredor;
								_mensajeTransaccionP.IdTipoReajuste = carga.IdTipoReajuste;
								_mensajeTransaccionP.INGMontoInteresCorteCupon = carga.INGMontoInteresCorteCupon;
								_mensajeTransaccionP.Amortizacion = carga.Amortizacion;
								_mensajeTransaccionP.AfectoaClearing = carga.AfectoaClearing;
								_mensajeTransaccionP.ProcesadoClearing = carga.ProcesadoClearing;
								_mensajeTransaccionP.IdRegimenFondoMutuo = carga.IdRegimenFondoMutuo;
								_mensajeTransaccionP.ISFUT = carga.ISFUT;
								_mensajeTransaccionP.RetencionISFUT = carga.RetencionISFUT;
								_mensajeTransaccionP.MontoRetencionISFUT=BigDecimal.ZERO;
                                                                if (_mensajeTransaccionP.IdEventoCapitalCliente > 0){
                                                                        params = new HashMap<>();
                                                                        params.put("IdEventoCapitalCliente", _mensajeTransaccionP.IdEventoCapitalCliente);
                                                                        
                                                                        params.put("p_IdEmpresa", entrada.p_IdEmpresa);
                                                                        params.put("IdEventoCapitalCliente", _mensajeTransaccionP.IdEventoCapitalCliente);
                                                                        
                                                                        TasaEfectiva = sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoExiste.class);
                                                                        
                                                                        if (TasaEfectiva.size()>0) {
                                                                        if( (_mensajeTransaccionP.ISFUT.equalsIgnoreCase("S") || _mensajeTransaccionP.RetencionISFUT.equalsIgnoreCase("S") ) &&
                                                                                        (TasaEfectiva.get(0).TasaEfectiva.compareTo(BigDecimal.ZERO) == 1))
                                                                        {
                                                                                _mensajeTransaccionP.MontoRetencionISFUT = TasaEfectiva.get(0).TasaEfectiva.multiply(new BigDecimal(_mensajeTransaccionP.Total), MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128);
                                                                                
                                                                        }
                                                                        }                                                                        
                                                                }
								_mensajeTransaccionP.Estado = carga.Estado;
								_mensajeTransaccionP.TipoArchivo = carga.TipoArchivo;
								paramInsertMensajeTransaccionISFUT.add(_mensajeTransaccionP);
								
								contador++;
								
							}
							sql.batch(conn, SQL_INSERT_CARGA_MASIVA_DETALLE, paramInsertMensajeTransaccionISFUT, DetalleCargaMasiva.class);
							
							
							
							List<CargaMasiva> paramInsertMensajeTransaccionISFUT2 = new ArrayList<CargaMasiva>();		
							for (CargaMasiva carga : entrada.p_CargaMasiva){
								
								CargaMasiva _mensajeTransaccionP2 = new CargaMasiva();  
								_mensajeTransaccionP2.IdEmpresa = carga.IdEmpresa;
								_mensajeTransaccionP2.IdUsuario = carga.IdUsuario;
								_mensajeTransaccionP2.CantidadRegistros = contador;
								_mensajeTransaccionP2.NombreArchivo = carga.NombreArchivo;
								_mensajeTransaccionP2.FechaCarga = carga.FechaCarga;
								_mensajeTransaccionP2.CodigoTipoCargaMasiva = carga.CodigoTipoCargaMasiva;
								_mensajeTransaccionP2.IdCargaMasiva = secuencias.get(0);
								paramInsertMensajeTransaccionISFUT2.add(_mensajeTransaccionP2);

							}
							sql.batch(conn, SQL_INSERT_CARGA_MASIVA, paramInsertMensajeTransaccionISFUT2, CargaMasiva.class);
							
							
//							HashMap<String,Object> params2 = new HashMap<String,Object>();
							List<CargaMasiva2> ListaArchivo = new ArrayList<CargaMasiva2>();
								
							CargaMasiva2 _datos = new CargaMasiva2();
							_datos.IdCargaMasiva = secuencias.get(0);
							ListaArchivo.add(_datos);
								
							sql.batch(conn, SQL_UPDATE_CARGA_MASIVA, ListaArchivo, CargaMasiva2.class);
					}
				}
					
					Salida.P_OUT_ERRORESCONTADOR= 0L;
					Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO","Archivo Cargado con exito"));
					    
				
			}catch (Exception e){
				e.printStackTrace();
				Salida.P_OUT_ERRORESCONTADOR= 1L;
				Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"Error","Error en el sistema"));
				
			}
			
			return Salida;
		}

		
		/******************************************************************************
		 * 							MAIN
		 ******************************************************************************/
		public static void main(String[] args){
			
			try (Connection con = PlataformaUtils.getDESAConnection())
			{
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
//				String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2022-08-01\",\"p_CargaMasiva\":[{\"IdCargaMasiva\":1,\"IdEmpresa\":1,\"IdUsuario\":1,\"CantidadRegistros\":1,\"NombreArchivo\":\"PRUEBA CARGA MASIVA.txt\",\"FechaCarga\":\"06-06-2022\",\"CodigoTipoCargaMasiva\":\"CARGA_CLIENTES_ISFUT\"}],\"p_CargaMasivaDetalle\":[{\"IdEventoCapitalCliente\":\"103093\",\"IdCargaMasivaISFUT\":\"1\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"1\",\"Secuencia\":\"103454\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"001\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"COPEC\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"CO\",\"IdConceptoGarantia\":\"001\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"122352218\",\"NombreCliente\":\"MIGUEL SANDOVAL H\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"0\",\"FactorNuevo\":\"0\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"500\",\"Total\":\"3333333\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"0\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"ABC\",\"TipoCuentaBoveda\":\"ABC\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"ABC\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"A\",\"ISFUT\":\"S\",\"RetencionISFUT\":\"S\"}]}";
//				String json3 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2022-09-05\",\"p_CargaMasiva\":[{\"IdCargaMasiva\":1,\"IdEmpresa\":1,\"IdUsuario\":6,\"NombreArchivo\":\"4.txt\",\"FechaCarga\":\"2022-09-05\",\"CodigoTipoCargaMasiva\":\"CARGA_CLIENTES_ISFUT\"}],\"p_CargaMasivaDetalle\":[{\"IdEventoCapitalCliente\":\"103093\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"1\",\"Secuencia\":\"103454\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"001\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"COPEC\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"CO\",\"IdConceptoGarantia\":\"001\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"122352218\",\"NombreCliente\":\"MIGUEL SANDOVAL H\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"0\",\"FactorNuevo\":\"0\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"500\",\"Total\":\"3333333\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"0\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"ABC\",\"TipoCuentaBoveda\":\"ABC\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"ABC\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"A\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"}]}";
//				String json4 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2022-09-05\",\"p_CargaMasiva\":[{\"IdEmpresa\":1,\"IdUsuario\":6,\"NombreArchivo\":\"4.txt\",\"FechaCarga\":\"2022-09-05\",\"CodigoTipoCargaMasiva\":\"CARGA_CLIENTES_ISFUT\"}],\"p_CargaMasivaDetalle\":[{\"IdEventoCapitalCliente\":\"103093\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"1\",\"Secuencia\":\"103454\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"001\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"COPEC\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"CO\",\"IdConceptoGarantia\":\"001\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"122352218\",\"NombreCliente\":\"MIGUEL SANDOVAL H\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"\",\"MontoCapital\":\"\",\"Precio\":\"0\",\"Factor\":\"0\",\"FactorNuevo\":\"0\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"500\",\"Total\":\"3333333\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"0\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"ABC\",\"TipoCuentaBoveda\":\"ABC\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"ABC\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"A\",\"ISFUT\":\"S\",\"RetencionISFUT\":\"S\"}]}";
//				String json5 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2022-09-09\",\"p_CargaMasiva\":[{\"IdEmpresa\":1,\"IdUsuario\":6,\"NombreArchivo\":\"9.txt\",\"FechaCarga\":\"2022-09-09\",\"CodigoTipoCargaMasiva\":\"CARGA_CLIENTES_ISFUT\"}],\"p_CargaMasivaDetalle\":[{\"IdEventoCapitalCliente\":\"102562\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"16895045-4\",\"NombreCliente\":\"JUANJO URIBE VARGAS\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"500\",\"Total\":\"3888500\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102560\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"16895045-4\",\"NombreCliente\":\"JUANJO URIBE VARGAS\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"7651\",\"Total\":\"59501827\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102559\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"NOM\",\"DocumentoIdentidad\":\"13189373-6\",\"NombreCliente\":\"CRISTIAN GUZMAN OYARCE\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"-200\",\"Total\":\"-1555400\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"AG300\",\"NombreAgente\":\"ANDERS CESPEDES CATALAN\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102563\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"18576992-5\",\"NombreCliente\":\"SERGIO MEDRANO FLORES\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"1000\",\"Total\":\"7777000\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"MBI1\",\"NombreAgente\":\"AGENTE MBI PRIMERO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102561\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"13189373-6\",\"NombreCliente\":\"CRISTIAN GUZMAN OYARCE\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"920\",\"Total\":\"7154840\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"AG300\",\"NombreAgente\":\"ANDERS CESPEDES CATALAN\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102565\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CNT\",\"CodigoPortafolio\":\"\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"NOM\",\"DocumentoIdentidad\":\"96502820-K\",\"NombreCliente\":\"CHILE MARKET CORREDORES DE BOLSA SPA    \",\"ConfiguracionNotificacion\":\"N\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"200\",\"Total\":\"1555400\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"AG300\",\"NombreAgente\":\"ANDERS CESPEDES CATALAN\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102566\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CNT\",\"CodigoPortafolio\":\"\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"96666140-2\",\"NombreCliente\":\"DEPOSITO CENTRAL DE VALORES\",\"ConfiguracionNotificacion\":\"N\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"-12571\",\"Total\":\"-97764667\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102564\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"12235221-8\",\"NombreCliente\":\"MIGUEL SANDOVAL H\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"2500\",\"Total\":\"19442500\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"MSH\",\"NombreAgente\":\"MIGUEL SANDOVAL H\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"}]}";
//				String json6 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2022-09-09\",\"p_CargaMasiva\":[{\"IdEmpresa\":1,\"IdUsuario\":6,\"NombreArchivo\":\"9.txt\",\"FechaCarga\":\"2022-09-09\",\"CodigoTipoCargaMasiva\":\"CARGA_CLIENTES_ISFUT\"}],\"p_CargaMasivaDetalle\":[{\"IdEventoCapitalCliente\":\"102562\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"VA\",\"IdConceptoGarantia\":\"1\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"16895045-4\",\"NombreCliente\":\"JUANJO URIBE VARGAS\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"500\",\"Total\":\"3888500\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"0\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"2022-06-06\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"AABC\",\"TipoCuentaBoveda\":\"AABC\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"AABC\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"N\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"}]}";
				String json7 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2022-09-09\",\"p_CargaMasiva\":[{\"IdEmpresa\":1,\"IdUsuario\":6,\"NombreArchivo\":\"sera.csv\",\"FechaCarga\":\"2022-09-09\",\"CodigoTipoCargaMasiva\":\"CARGA_CLIENTES_ISFUT\"}],\"p_CargaMasivaDetalle\":[{\"IdEventoCapitalCliente\":\"102562\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"16895045-4\",\"NombreCliente\":\"JUANJO URIBE VARGAS\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"500\",\"Total\":\"3888500\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102560\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"16895045-4\",\"NombreCliente\":\"JUANJO URIBE VARGAS\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"7651\",\"Total\":\"59501827\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102559\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"NOM\",\"DocumentoIdentidad\":\"13189373-6\",\"NombreCliente\":\"CRISTIAN GUZMAN OYARCE\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"-200\",\"Total\":\"-1555400\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"AG300\",\"NombreAgente\":\"ANDERS CESPEDES CATALAN\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102563\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"18576992-5\",\"NombreCliente\":\"SERGIO MEDRANO FLORES\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"N\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"1000\",\"Total\":\"7777000\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MBI1\",\"NombreAgente\":\"AGENTE MBI PRIMERO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102561\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"13189373-6\",\"NombreCliente\":\"CRISTIAN GUZMAN OYARCE\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"920\",\"Total\":\"7154840\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"AG300\",\"NombreAgente\":\"ANDERS CESPEDES CATALAN\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102565\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CNT\",\"CodigoPortafolio\":\"\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"NOM\",\"DocumentoIdentidad\":\"96502820-K\",\"NombreCliente\":\"CHILE MARKET CORREDORES DE BOLSA SPA    \",\"ConfiguracionNotificacion\":\"N\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"200\",\"Total\":\"1555400\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"AG300\",\"NombreAgente\":\"ANDERS CESPEDES CATALAN\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102566\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CNT\",\"CodigoPortafolio\":\"\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"96666140-2\",\"NombreCliente\":\"DEPOSITO CENTRAL DE VALORES\",\"ConfiguracionNotificacion\":\"N\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"-12571\",\"Total\":\"-97764667\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MBI2\",\"NombreAgente\":\"AGENTE MBI SEGUNDO\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"},{\"IdEventoCapitalCliente\":\"102564\",\"IdTipoEventoCapital\":\"DV\",\"NumeroDividendo\":\"777\",\"Secuencia\":\"7777\",\"TipoSocioNegocio\":\"CLI\",\"CodigoPortafolio\":\"1\",\"IdClasificacionTipoInstrumento\":\"RV\",\"Nemo\":\"VAPORES\",\"ISIN\":\"CLP3064M1019\",\"NemoNuevo\":\"\",\"IdConceptoGarantia\":\"\",\"TipoCartera\":\"CUSTODIA\",\"DocumentoIdentidad\":\"12235221-8\",\"NombreCliente\":\"MIGUEL SANDOVAL H\",\"ConfiguracionNotificacion\":\"S\",\"TienePreferenciaDePago\":\"S\",\"MontoCapital\":\"0\",\"Precio\":\"0\",\"Factor\":\"7777\",\"FactorNuevo\":\"\",\"PrecioNuevo\":\"0\",\"CantidadNueva\":\"0\",\"MontoInteres\":\"0\",\"MontoReajuste\":\"0\",\"CantidadDerecho\":\"2500\",\"Total\":\"19442500\",\"MontoRetencion\":\"0\",\"CantidadDerechoArchivo\":\"0\",\"AccionesDisponiblesArchivo\":\"0\",\"DiferenciaCantidadDerecho\":\"0\",\"DiferenciaAccionesCantidadNueva\":\"0\",\"MontoBeneficioArchivo\":\"0\",\"DiferenciaMontoBeneficio\":\"0\",\"MontoInteresArchivo\":\"0\",\"MontoCapitalArchivo\":\"0\",\"DiferenciaMontoInteres\":\"0\",\"DiferenciaMontoCapital\":\"0\",\"MontoRetencionArchivo\":\"0\",\"DiferenciaMontoRetencion\":\"\",\"IdMoneda\":\"CLP\",\"FechaPago\":\"06-06-2022\",\"CodigoUsuarioAgente\":\"MSH\",\"NombreAgente\":\"MIGUEL SANDOVAL H\",\"IdBoveda\":\"DCV\",\"IdEstado\":\"PRO\",\"Origen\":\"SISTEMA\",\"EstadoNotificacion\":\"N\",\"NombreMedioDePago\":\"\",\"TipoCuentaBoveda\":\"\",\"NumeroCuentaBoveda\":\"PECO\",\"PagaClienteCorredor\":\"CLIENTE\",\"IdTipoReajuste\":\"\",\"INGMontoInteresCorteCupon\":\"0\",\"Amortizacion\":\"0\",\"AfectoaClearing\":\"N\",\"ProcesadoClearing\":\"N\",\"IdRegimenFondoMutuo\":\"\",\"ISFUT\":\"N\",\"RetencionISFUT\":\"N\"}]}";
				EntradaCargaMasivaISFUT entrada = gson.fromJson(json7, EntradaCargaMasivaISFUT.class);
				CrearCargaMasivaISFUT demo = new CrearCargaMasivaISFUT();
				
				SalidaCargaMasivaISFUT salida = demo.logica(con, entrada);
				
				
				System.out.println("salida --> "+salida.toString());
				con.commit();//--rollback();
				
			}catch(Exception e){
				e.printStackTrace(); 
			}
			
			System.out.println("== FIN PRUEBA LOCAL ==");
		}



		
	
}

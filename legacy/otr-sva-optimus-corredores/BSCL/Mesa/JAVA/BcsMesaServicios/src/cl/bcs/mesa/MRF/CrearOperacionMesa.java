package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.FWD.CrearOperacionForward.EventoAutorizacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.MRF.AnularOperacionMesa.Operacion;
import cl.bcs.mesa.MRF.IngresarCompraVentaPacto.IngresarCompraVentaPacto_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.IngresarCompraVentaPacto.IngresarCompraVentaPacto_FormaPagoOperacion_IN;
import cl.bcs.mesa.MRF.IngresarCompraVentaPacto.IngresarCompraVentaPacto_IN;
import cl.bcs.mesa.MRF.IngresarCompraVentaPacto.IngresarCompraVentaPacto_OUT;
import cl.bcs.mesa.MRF.IngresarCompraVentaPacto.IngresarCompraVentaPacto_Operacion_IN;
import cl.bcs.mesa.MRF.IngresarCompraVentaTermino.IngresarCompraVentaTermino_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.IngresarCompraVentaTermino.IngresarCompraVentaTermino_FormaPagoOperacion_IN;
import cl.bcs.mesa.MRF.IngresarCompraVentaTermino.IngresarCompraVentaTermino_IN;
import cl.bcs.mesa.MRF.IngresarCompraVentaTermino.IngresarCompraVentaTermino_OUT;
import cl.bcs.mesa.MRF.IngresarCompraVentaTermino.IngresarCompraVentaTermino_Operacion_IN;
import cl.bcs.mesa.MRF.TomarDepositoDesembolso.CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN;
import cl.bcs.mesa.MRF.TomarDepositoDesembolso.CrearOperacionDepositoDesembolso_Operacion_IN;
import cl.bcs.mesa.MRF.TomarDepositoDesembolso.TomarDepositoDesembolso_IN;
import cl.bcs.mesa.MRF.TomarDepositoDesembolso.TomarDepositoDesembolso_OUT;
import cl.bcs.mesa.MRF.pojo.CacheMRF;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.lim.pojo.Validacion;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.InsertarLogServicio;
import cl.bcs.plataforma.pla.InsertarLogServicio.RegistroLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.CacheServicio;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato.Resultado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.IngresarMandatoPAC.EntradaIngresoMandatoPAC;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.*;

public class CrearOperacionMesa implements JavaCall {

	private static final Logger logger = LogManager.getLogger(CrearOperacionMesa.class);

	private final String SQL_SELECT_OPERACION			= loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaRF_SelectOperacion.sql");
	private final String SQL_SELECT_CONCEPTOS_MESA		= loadResourceToString("cl/bcs/mesa/sql/ConsultarConceptosMesa.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO 	= loadResourceToString("cl/bcs/mesa/sql/ConsultarTipoMovimientoMesa.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_CORREDOR = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaRF_SelectFormaPagoPortafolio.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_CLIENTE = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaRF_SelectFormaPagoPortafolioCliente.sql");
	private final String SQL_SELECT_CONDICION_LIQUIDACION = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaRF_SelectCondicionLiquidacion.sql");	
	private final String SQL_SELECT_SUCURSAL_EMPLEADO 	  = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaRF_SelectSucursalEmpleado.sql");
	private final String SQL_SELECT_CLIENTE 			  = loadResourceToString("cl/bcs/mesa/MRF/sql/CrearOperacionMesaRF_SelectCliente.sql");
	private final String SQL_SELECT_ID_CARTERA_RESERVA    = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesa_SelectIdCartera.sql");
	private final String SQL_SELECT_MODALIDAD_DE_PAGO     = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesa_SelectModalidaPago.sql");
	private final String SQL_INSERT_OPERACION_MESA     	  = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesa_SelectOperacionMesa.sql");
	private final String SQL_SELECT_PARAMETROS_CONFIGURACION = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesa_SelectParametroConfiguracion.sql");
	private final String SQL_SELECT_MESA = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesa_SelectNombreMesa.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CORREDOR = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormasDePagoDefecto.sql");
	private final String SQL_INSERT_FECHAS_CONSULTA = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaRF_InsertFechaConsulta.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CLIENTE = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormasDePagoDefectoCliente.sql");
	private final String SQL_SELECT_FECHA_HABIL = loadResourceToString("cl/bcs/mesa/sql/CrearOperacionMesaRF_ValidarFechaHabil.sql");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	// --------------------------------------------------------------
	// CLASES DE ENTRADA.
	// --------------------------------------------------------------
	
	public static class FechaEsHabil {
		public Long IdTrabajo;
		public String FechaVencimiento;
		public String TipoDia;

		public FechaEsHabil() {
		}

		public FechaEsHabil(Long IdTrabajo, String fecha) {
			this.IdTrabajo = IdTrabajo;
			this.FechaVencimiento = fecha;
		}
	}
	
	public static class CrearOperacionMesa_Operacion_IN extends BcsPojo{
		
		public BigDecimal 	TasaAnualizada;
		public String	 	CostoFondo;
		public BigDecimal 	UtilidadPerdida;
		public BigDecimal 	UtilidadFinal;
		
		public String Accion;
		public Long IdInternoOperacion;
		public Long IdOperacion;
		public Long FolioOperacion;
		public Long FolioCartera;// para pactos y depositos y desembolso
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public String Intermesa;
		public Long IdMesaOrigen;
		public Long IdMesaDestino;
		public String AfectaCustodia;
		public String IdTipoProducto;// 'MERF'
		public String IdTipoOperacion;// COMTER,VENTER,COMPAC,VENPAC
		public String IdTipoReajuste;// "USD","UF"
		public Long Plazo;
		public BigDecimal Monto;
		public BigDecimal MontoMoneda;
		public String IdMoneda;// CLP "USD","UF" moneda local
		public String IdMonedaLiquidacion; // CLP "USD","UF"
		public BigDecimal MontoLiquidacion;
		public BigDecimal Tasa;
		public BigDecimal ValorFinal;
		public BigDecimal ValorFinalMoneda;
		public BigDecimal TasaAgente;
		public String FechaOperacion;
		public String FechaVencimiento;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public Long IdAgente;
		public Long IdOperador;
		public String IdEstado;
		public String Glosa;
		public String IdTipoCanalIngreso;
		public String TipoDocumento;
		public Long IdContacto;
		public String RequiereAutorizacion="N";
		// Nuevo deposito desembolso
		public String IdEmisor = "N";
		public String GuardandoCabecera = "N";
		public String ValorizacionManual;
		

		public CrearOperacionMesa_Operacion_IN() {
			this.ValorizacionManual="N";			
		}
	}

	public static class CrearOperacionMesa_DetalleOperacion_IN {
		public Long IdInternoDetalle;
		public Long IdInternoOperacion;
		public Long IdReserva;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdMesa;
		public String IdTipoProducto; // 'MERF'
		public String IdTipoOperacion; // COMTER,VENTER,COMPAC,VENPAC
		public Long IdInstrumento;
		public String Nemotecnico;
		public String EsFungible;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCartera;
		public Long FolioCartera;
		public String CodigoIFRS;
		public String IdEmisor;
		public String IdMoneda;
		public String IdTipoReajuste;
		public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
		public String IdMonedaLiquidacionOperacion;
		public BigDecimal CorteMinimo;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal PrecioMercado;
		public BigDecimal Tasa;
		public BigDecimal TasaMercado;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoLiquidacionOperacion;
		public BigDecimal ValorRescate;
		public String FechaVencimiento;
		public BigDecimal MontoValorizadoReserva;
		public String IdClasificacionTipoInstrumento;
		public String TipoCarteraCorredor;
		public BigDecimal MontoFinalCarteraMonedaSistema;
		public BigDecimal MontoFinalCarteraMonedaInstrumento;
		public BigDecimal INGMontoAPlazoDetalle;
		public BigDecimal INGMontoAPlazoMonedaInstrumentoDetalle;

		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	public static class CrearOperacionMesa_FormaPagoOperacion_IN {
		public Long IdInternoPagoOperacion;
		public Long IdInternoOperacion;
		public Long IdComprobantePagoOperacion;
		public String IdFormaDePago;
		public String IdMoneda;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		// public String IdMonedaLiquidacion;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String AfectarTesoreria;
		public Long IdCuentaBancaria;
		public boolean EsCliente;
		public String FechaLiquidacion;
		public String NombreFormaDePago;
		public String NombreBanco = "" ;
		public String NombreBancoTarjeta = "" ;
		public String NumeroTarjeta = "" ;
		public String CuentaBancaria = "" ;

		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	public static class CrearOperacionMesa_IN extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;		
		public List<CrearOperacionMesa_Operacion_IN> p_Operacion;
		public List<CrearOperacionMesa_DetalleOperacion_IN> p_DetalleOperacion;
		public List<CrearOperacionMesa_FormaPagoOperacion_IN> p_FormasDePago;

		public CrearOperacionMesa_IN() {
			this.p_Operacion = new ArrayList<CrearOperacionMesa_Operacion_IN>();
			this.p_DetalleOperacion = new ArrayList<CrearOperacionMesa_DetalleOperacion_IN>();
			this.p_FormasDePago = new ArrayList<CrearOperacionMesa_FormaPagoOperacion_IN>();			
		}
		
	};
	
	public static class VerificarCartera  extends BcsPojo  {
		public Long FolioCartera;
		public Long IdCartera;
		public String IdEstado;
		
	}

	// --------------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------------
	public static class CrearOperacionMesa_Operacion_OUT {
		public String Accion;
		public Long IdInternoOperacion;
		public Long IdOperacion;
		public Long FolioOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdOperacionRelacionada;
		public String Intermesa;
		public Long IdMesaOrigen;
		public Long IdMesaDestino;
		public String AfectaCustodia;
		public String IdTipoProducto; // 'MERF'
		public String IdTipoOperacion; // COMTER,VENTER,COMPAC,VENPAC
		public String IdTipoReajuste; // "USD","UF"
		public Long Plazo;
		public BigDecimal Monto;
		public BigDecimal MontoMoneda;
		public String IdMoneda;// CLP "USD","UF" moneda local
		public String IdMonedaLiquidacion; // CLP "USD","UF"
		public BigDecimal Tasa;
		public BigDecimal ValorFinal;
		public BigDecimal ValorFinalMoneda;
		public BigDecimal TasaAgente;
		public Date FechaOperacion;
		public Date FechaVencimiento;
		public String IdCondicionLiquidacion;
		public Date FechaLiquidacion;
		public Long IdAgente;
		public Long IdOperador;
		public String IdEstado;
		public String NombreEstado;
		public String Glosa;
		public String IdTipoCanalIngreso;
		// Nuevo deposito desembolso
		public String IdEmisor;

		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	public static class CrearOperacionMesa_DetalleOperacion_OUT {
		public Long IdInternoDetalle;
		public Long IdInternoOperacion;
		public Long IdReserva;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdMesa;
		public String IdTipoProducto;// 'MERF'
		public String IdTipoOperacion;// COMTER,VENTER,COMPAC,VENPAC
		public Long IdInstrumento;
		public String EsFungible;
		public String IdBolsa;
		public String IdBoveda;
		public Long FolioCartera;
		public String CodigoIFRS;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
		public String IdMonedaLiquidacionOperacion;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoLiquidacionOperacion;
		public BigDecimal ValorRescate;
		public Date FechaVencimiento;
		public String TipoCarteraCorredor;
		public BigDecimal MontoFinalCarteraMonedaSistema;
		public BigDecimal MontoFinalCarteraMonedaInstrumento;

		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	public static class CrearOperacionMesa_FormaPagoOperacion_OUT {
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public String IdFormaDePago;
		public String IdMoneda;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String NombreFormaDePago;
		public String NombreMoneda;
		public String IdMedioDePago;
		public String AfectarTesoreria;
		public Long IdCuentaBancaria;
		public String NombreBanco;
		public String CuentaBancaria;

		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	public static class CrearOperacionMesa_OUT {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public List<CrearOperacionMesa_Operacion_OUT> P_OUT_OPERACION;
		public List<CrearOperacionMesa_DetalleOperacion_OUT> P_OUT_DETALLEOPERACION;
		public List<CrearOperacionMesa_FormaPagoOperacion_OUT> P_OUT_FORMASDEPAGO;

		public CrearOperacionMesa_OUT() {
			this.P_OUT_OPERACION = new ArrayList<CrearOperacionMesa_Operacion_OUT>();
			this.P_OUT_DETALLEOPERACION = new ArrayList<CrearOperacionMesa_DetalleOperacion_OUT>();
			this.P_OUT_FORMASDEPAGO = new ArrayList<CrearOperacionMesa_FormaPagoOperacion_OUT>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// --------------------------------------------------------------
	// CLASES COMPLEMENTARIAS
	// --------------------------------------------------------------

	public static class FormaDePagoMesa {
		public String IdFormaDePago;
		public String NombreFormaDePago;
		public Long Dias;
		public String PorDefecto;
		public String RecibePagaPortafolioDesc;
		public String IdEstado;
		public String NombreEstado;
		public String IdPreferenciaPago;
		public String IdTipoOperacion;
		public String IdMedioDePago;
		public String IdMoneda;
		public String UsaPersonaRelacionada;
		public Date FechaCreacion;
		public Date FechaModificacion;
		public Long IdUsuarioCreador;
		public Long IdEmpresa;
		public String NombreTipoOperacion;
		public String IdTipoProducto;
		public String NombreTipoProducto;
		public String RecibePagaPortafolio;
		public String NombreMedioDePago;
		public String NombreMoneda;
		public String UsaCuentaBancaria;
		public String UsaTarjetaBancaria;
		public String IdEstadoPreferenciaPago;
		public String NombreEstadoPreferenciaPago;
		public String IdModalidadDePago;

		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	public static class TipoMovimientoMesa {
		public String TipoMov;
		public String RecibePaga;
		public String IdTipoMovimiento;

		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	public static class Concepto {
		public String IdConcepto;
		public String NombreConcepto;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String Uso;
		public String IdConceptoRelacionado;
		public String NombreConceptoRelacionado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConfiguracionMesa {
		public Long IdEmpresa;
		public String IdTipoProducto;
		public String CodigoParametro;
		public String Descripcion;
		public String Valor;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Sucursal {
		public Long IdSucursal;
		public Long IdEmpleado;
		public Long IdUsuario;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Mesa {
		public Long IdCliente; 
		public Long IdMesa;
		public String Nombre;
		public Long IdPortafolio;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Persona {
		public Long IdCliente;
		public Long IdPersona;
		public Long IdEmpresa;
		public String IdEstado;
		public String RazonSocial;
		public String CodigoPortafolio;

		public String DocumentoIdentidad = "";
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ModalidadDePago {
		public Long IdMedioDePagoYModalidadDePago;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PreferenciaPagoPortafolio {
		public Long IdPortafolio;
		public String IdPreferenciaPago;
		public Long IdCuentaBancaria;
		public Long IdBancoCuentaBancaria;
		public String NombreBancoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdTarjetaBancaria;
		public String IdTipoTarjetaBancaria;
		public String NombreBancoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public String IdModalidadDePago;
		public String IdMedioDePago;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosOperacion {
		public Long IdOperacion;
		public String IdEstado;
		public String FechaOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPortafolioMesa;
		public String IdTipoOperacion;
		public Long PlazoEnDias;
		public Long IdUsuarioCreacion;
		public BigDecimal MontoOperacionInicial;
		public BigDecimal TotalDetalle;
		public BigDecimal TotalPagos;
		public Long FolioOperacion;
		public String NombreTipoOperacion;
		public String Agente;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreProducto;
		public String IdTipoReajuste;
		public Long IdDireccionElectronica;
		public Long IdPersona;
		public String NombreEstado;
		public BigDecimal MontoOperacionFinal;
		public BigDecimal MontoOperacionMoneda;
		public String FechaVencimiento;
		public BigDecimal Tasa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ParametrosConfiguracion {
		public String CodigoParametro;
		public String Valor;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjCondicionLiquidacion {
		public String IdCondicionDeLiquidacion;
		public int PlazoEnDias;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class DatosValidacionFormaPago{
		public BigDecimal montoFormaPagoCorregido;
		public String RecibePagoPortafolio;
		public String IdTipoOperacion;
		public String ModificaMonto;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosOperacionDinamico{
		public Long FolioOperacion;
		public Long IdOperacion;
		public Long IdOperacionRelacionada;
		
		public String IdEstado;
		public String NombreEstado;
		
		public String Intermesa;
		public String Liquidado;
		
		public Long IdMesaOrigen;
		public String NombreMesaOrigen;
		public Long IdMesaDestino;
		public String NombreMesaDestino;
		
		public Long IdCliente;
		public String RazonSocial;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String DocumentoIdentidad;
		
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public String DocumentoIdentidadMesa;
		public String ConfirmaAdendum;
		
		public String CodigoUsuario;
		public BigDecimal CostoFondo;
		public Long PlazoEnDias;
		
		
		public BigDecimal Tasa;
		public BigDecimal TasaAgente;
		public BigDecimal TasaAnticipo;

		public String TipoDocumento;
		public String TipoInformeGenerado;
		
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionAnticipo;
		public BigDecimal MontoOperacionFinal;
		public BigDecimal MontoOperacionFinalAnticipo;
		public BigDecimal MontoOperacionFinalMoneda;
		public BigDecimal MontoOperacionFinalMonedaAnticipo;
		
		public BigDecimal MontoOperacionInicial;
		public BigDecimal MontoOperacionInicialAnticipo;
		public BigDecimal MontoOperacionInicialMoneda;
		public BigDecimal MontoOperacionInicialMonedaAnticipo;
		public BigDecimal MontoOperacionMoneda;
		public BigDecimal MontoOperacionMonedaAnticipo;
		
		public Date FechaOperacion;
		public Date FechaVencimiento;
		public Date FechaLiquidacion;
		public Date FechaAnticipo;
		public Date FechaCondicionLiquidacionAnticipo;
		public Date FechaCondicionLiquidacion;
		public Date FechaCreacion;
		public String FechaLiquidacionString;
		public String FechaVencimientoUI;
		
		public String IdCondicionLiquidacion;
		public Long IdModalidadLiquidacion;
		public String IdCondicionLiquidacionAnticipo;
		public Long IdEmpresa;
		
		public Long IdAgente;
		public Long IdSucursalEjecutivo;
		
		public String IdTipoCanalIngreso;
		public String	IdTipoInstrumento;
		public String	IdTipoOperacion;
		public String	IdTipoProducto;
		public String	IdTipoReajuste;
		
		public Long IdUsuarioConfirma;
		public Long IdUsuarioCreacion;
		
		public String ParametroConfirmacion;
		public String ConfirmaMediosPago;
		public Long UsuarioConfirma;
		public String Glosa;
		public String TipoOperacion;
		public String AfectaCustodia;
		public String Anticipado;
		
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public String IdMonedaLiquidacionAnticipo;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String NombreOperacion;
		public Long UtilidadFinal;
		public Long UtilidadPerdida;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

// ------------------------------------------------------------
// LOGICA
// ------------------------------------------------------------

	public CrearOperacionMesa_OUT logica(Connection conn, CrearOperacionMesa_IN in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		Long MontoMinimoPacto = 0L;
		BigDecimal MontoCLP_UFMinimo_Pacto = BigDecimal.ZERO;
		logger.info("Inicio servicio MRF.CrearOperacionMesa");
		InsertarLogServicio insertarLogService = new InsertarLogServicio();

		SQL _sql = new SQL();
		CrearOperacionMesa_OUT out = new CrearOperacionMesa_OUT();

		Long Id = 0L;
		out.P_OUT_ERRORESCONTADOR = 0L;

		HashMap<String, String> mapMsg = new HashMap<>();

		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisSistema = empresa.IdPais;

		logger.debug("[CrearOperacionMesa] Inicio Obtiene parametros Configuracion");
		startTime = System.nanoTime();
		String monedaSistema = "", fechaSistema = "", localizacionSistema = "";

		HashMap<String, Object> paramCongif = new HashMap<>();
		paramCongif.put("IdEmpresa", in.p_IdEmpresa);
		// No se usa ConfiguracionSistema.getParametroConfiguracionString por
		// rendimiento
		List<ParametrosConfiguracion> lstParametrosConfiguracion = _sql.query(conn, SQL_SELECT_PARAMETROS_CONFIGURACION, paramCongif, ParametrosConfiguracion.class);

		
		if (lstParametrosConfiguracion.size() > 0)
		{
			for (ParametrosConfiguracion config : lstParametrosConfiguracion)
			{
				if (config.CodigoParametro.equalsIgnoreCase("PLA.MonedaSistema"))       { monedaSistema = config.Valor; }
				if (config.CodigoParametro.equalsIgnoreCase("PLA.FechaSistema"))        { fechaSistema = config.Valor; }
				if (config.CodigoParametro.equalsIgnoreCase("PLA.LocalizacionSistema")) { localizacionSistema = config.Valor; }
				if (config.CodigoParametro.equalsIgnoreCase("MONTO_MINIMO_PACTO_EN_UF")){ MontoMinimoPacto = Long.parseLong(config.Valor); }
			}
		}
		

		// Paridad UF (chequeo monto minimo Pacto)
		// --------------------------------------------------------------------------------------
		if (MontoMinimoPacto.compareTo(0L) != 0) { 
			CalcularParidad par = new CalcularParidad();
			CalcularParidad.CalcularParidadEntrada parin = new CalcularParidad.CalcularParidadEntrada();
			parin.p_IdEmpresa 	= in.p_IdEmpresa;		
			parin.p_IdTrabajo 	= (long)Math.random()*10000;
			CalcularParidad.CalculoEntrada auxmon = new CalcularParidad.CalculoEntrada();			
			auxmon.IdInterno 		= 1L;
			auxmon.IdTrabajo 		= parin.p_IdTrabajo;
			auxmon.IdUso 			= "VALORIZADOR";
			auxmon.Fecha 			= fechaSistema;
			auxmon.IdMonedaOrigen 	= "UF";
			auxmon.MontoOrigen 		= BigDecimal.valueOf(MontoMinimoPacto);
			auxmon.ValorCambioLocalModificado = BigDecimal.ZERO;
			auxmon.IdMonedaDestino 	= "CLP";
			parin.p_CalculosEntrada.add(auxmon);
			
			CalcularParidad.CalcularParidadSalida parout = par.logica(conn, parin);
			for (  CalculoSalida parItem : parout.p_out_CalculosSalida  ) {
				MontoCLP_UFMinimo_Pacto = parItem.MontoDestino;
			}
		}
		
		List<String> IdTipoOperacion = new ArrayList<String>();
		List<String> IdTipoProducto = new ArrayList<String>();
		HashMap<String, Object> paramConcep = new HashMap<>();
		HashMap<Long, CrearOperacionMesa_Operacion_IN> v_IdOperacionHash = new HashMap<>();

		// Recorre entrada y asigna variables para consultas
		// --------------------------------------------------------------------------------------

		HashMap<String, Object> parPrefPor = new HashMap<>();
		HashMap<String, Object> parPrefPorCor = new HashMap<>();
		HashMap<String, Object> params = new HashMap<>();

		List<Object> pc = new ArrayList<>();
		List<Object> parPre = new ArrayList<>();
		List<Object> parPreCor = new ArrayList<>();
		List<Object> pcDinamico = new ArrayList<>();
		List<DatosOperacion> lstIdsOperacion = new ArrayList<DatosOperacion>();
		DatosOperacion objIdOperacion;
		String guardandoCabecera = "";
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		
		List<FechaEsHabil> fechasConsulta = in.p_Operacion.stream()
				.map(ope -> ope.FechaVencimiento)//Obtengo fechas de vencimiento de las operaciones
				.distinct() //me quedo solo con los distintos
				.map(ope -> new FechaEsHabil(idTrabajo, ope))//creo objeto FechaEsHabil (en este punto es un string comun y silvestre)
				.collect(Collectors.toList()); //obtengo la lista
		_sql.batch(conn, SQL_INSERT_FECHAS_CONSULTA, fechasConsulta, FechaEsHabil.class);
		
		HashMap<String, Object> paramsDia = new HashMap<String, Object>(2);
		paramsDia.put("IdEmpresa", 1);
		paramsDia.put("IdTrabajo", idTrabajo);
		List<FechaEsHabil> resultadoFechas = _sql.query(conn, SQL_SELECT_FECHA_HABIL, paramsDia, FechaEsHabil.class);
		
		HashMap<String, Object> fechaHabil = Utils.List2Hash(resultadoFechas, "FechaVencimiento");
		
		
		
		for (CrearOperacionMesa_Operacion_IN ope : in.p_Operacion) {
			pc.add(ope.IdCliente);
			pc.add(ope.IdClienteCorredor);
			pcDinamico.add(ope.IdCliente);
			IdTipoOperacion.add(ope.IdTipoOperacion);
			IdTipoProducto.add(ope.IdTipoProducto);
			params.put("IdPortafolio", ope.IdPortafolio);
			guardandoCabecera = ope.GuardandoCabecera;
			v_IdOperacionHash.put(ope.IdInternoOperacion, ope);
			parPre.add(ope.IdPortafolio);
			parPreCor.add(ope.IdPortafolioCorredor);
			
			if (ope.IdOperacion != 0) {
				objIdOperacion = new DatosOperacion();
				objIdOperacion.IdOperacion = ope.IdOperacion;

				if (!lstIdsOperacion.contains(objIdOperacion)) {
					lstIdsOperacion.add(objIdOperacion);
				}
			}
		}
		//valida formas de pago
		Boolean sonFormaPagoDefecto = false;
		if(in.p_FormasDePago.size()==0 && "S".equals(guardandoCabecera)) {
			in.p_FormasDePago = crearFormaDePagoPorDefecto(conn,in.p_Operacion);
			sonFormaPagoDefecto= true;
			
		}

		// Consulta datos Cliente
		HashMap<String, Object> parCli = new HashMap<>();
		parCli.put("IdCliente", pc);
		parCli.put("IdPortafolio", parPre);
		List<Persona> PersonaList = _sql.query(conn, SQL_SELECT_CLIENTE, parCli, Persona.class);
		HashMap<String, Object> PersonaHash = Utils.List2Hash(PersonaList, "IdCliente");

		// Validaciones de la Mesa
		HashMap<Long, Object> hashDatosOperacion = new HashMap<Long, Object>();
		List<DatosOperacion> lstDatosOperacion = new ArrayList<DatosOperacion>();

		if (lstIdsOperacion.size() > 0) {
			_sql.batch(conn, SQL_INSERT_OPERACION_MESA, lstIdsOperacion, DatosOperacion.class);
			lstDatosOperacion = _sql.query(conn, SQL_SELECT_OPERACION, new HashMap<>(), DatosOperacion.class);
			hashDatosOperacion = Utils.List2Hash(lstDatosOperacion, "IdOperacion");
		}
		List<RegistroLog> lstDatosLog = new ArrayList<RegistroLog>();
		HashMap<Long, DatosValidacionFormaPago> listaValidacionFormaPagos = new HashMap<>();
		for (CrearOperacionMesa_Operacion_IN ope : in.p_Operacion) {
			DatosOperacion DatosOpe = (DatosOperacion) hashDatosOperacion.get(ope.IdOperacion);
			BigDecimal monto = BigDecimal.ZERO;
			BigDecimal montoFinal = BigDecimal.ZERO;

			if ((ope.IdTipoOperacion.equals("COMPAC") || ope.IdTipoOperacion.equals("VENPAC"))) {
				FechaEsHabil fechaVencimientoOperacion = (FechaEsHabil) fechaHabil.get(ope.FechaVencimiento);
				if (fechaVencimientoOperacion != null && !"HABIL".equals(fechaVencimientoOperacion.TipoDia)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "FECHA_VENCIMIENTO_HABIL",
							mapMsg, conn, in.p_IdEmpresa));
					break;
				}

				if (MontoMinimoPacto.compareTo(0L) != 0 && ope.Monto.compareTo(MontoCLP_UFMinimo_Pacto) < 0) {
					mapMsg.put("MontoMinimoUF", MontoMinimoPacto.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR,
							"SRV_MRF_OPERACION_PACTO_INFERIOR_MONTO_UF", mapMsg, conn, in.p_IdEmpresa));
					break;
				}
			}

			
			if (ope.Accion.equals("CERRAR")) {
			// Montos Formas de Pago.

				for (CrearOperacionMesa_FormaPagoOperacion_IN Pagos : in.p_FormasDePago) {
					if (ope.IdInternoOperacion.equals(Pagos.IdInternoOperacion)) {
						if (ope.IdTipoOperacion.equals("COMPAC")) {
							if (Pagos.RecibePagaPortafolio.equals("P")) {
								monto = monto.add(Pagos.MontoFormaDePago, MathContext.DECIMAL128);
							}
							else
							{
								montoFinal = montoFinal.add(Pagos.MontoFormaDePago, MathContext.DECIMAL128).setScale(0, RoundingMode.HALF_UP);
							}
						} else if (ope.IdTipoOperacion.equals("VENPAC")) {
							if (Pagos.RecibePagaPortafolio.equals("P")) {
								monto = monto.add(Pagos.MontoFormaDePago, MathContext.DECIMAL128);
							}
							else
							{
								montoFinal = montoFinal.add(Pagos.MontoFormaDePago, MathContext.DECIMAL128).setScale(0, RoundingMode.HALF_UP);
							}
						} else {
							monto = monto.add(Pagos.MontoFormaDePago, MathContext.DECIMAL128);
						}
					}
				}

				if (DatosOpe != null) {
					if ((DatosOpe.IdTipoOperacion.equals("COMPAC") || DatosOpe.IdTipoOperacion.equals("VENPAC"))
							&& DatosOpe.PlazoEnDias.equals(0L)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR,"SRV_MRF_OPERACION_PACTO_SIN_PLAZO", mapMsg, conn, in.p_IdEmpresa));
						break;
					}

					if ("CL".equals(localizacionSistema)) {
						if ((DatosOpe.TotalDetalle == null || DatosOpe.TotalDetalle.equals(BigDecimal.ZERO))) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_OPERACION_SIN_PAPELES", mapMsg, conn, in.p_IdEmpresa));
							break;
						}

						if (ope.MontoLiquidacion.intValue() != monto.intValue()) {
							out.P_OUT_MENSAJES
									.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_OPERACION_PAGO_INICIAL_DISTINTO_PAPELES", mapMsg, conn, in.p_IdEmpresa));
							break;
						}
						if (ope.ValorFinalMoneda.setScale(0, RoundingMode.HALF_UP).intValue() != montoFinal.setScale(0, RoundingMode.HALF_UP).intValue() && !("VENTER".equals(ope.IdTipoOperacion) || "COMTER".equals(ope.IdTipoOperacion)) && !"UF".equals(ope.IdTipoReajuste)) {
							out.P_OUT_MENSAJES
									.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_OPERACION_PAGO_FINAL_DISTINTO_PAPELES", mapMsg, conn, in.p_IdEmpresa));
							break;
						}
						//si la cabecera del pacto tiene IdTipoReajuste en uf, los montos de FP son en CLP
						if (ope.ValorFinal.compareTo(montoFinal) != 0 && !("VENTER".equals(ope.IdTipoOperacion) || "COMTER".equals(ope.IdTipoOperacion)) && "UF".equals(ope.IdTipoReajuste)) {
							out.P_OUT_MENSAJES
									.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_OPERACION_PAGO_FINAL_DISTINTO_PAPELES", mapMsg, conn, in.p_IdEmpresa));
							break;
						}
					}

					if (DatosOpe.IdEstado.equals("VIG")) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_OPERACION_YA_ESTA_VIGENTE", mapMsg, conn, in.p_IdEmpresa));
						break;
					}
				}

				if ("PE".equals(localizacionSistema)) {
					ope.MontoLiquidacion = ope.Monto;
				}

				//Valida la parte entera de los montos liquidacion y forma de pago. La parte decimal queda como rango de holgura para operaciones usd
				if (ope.MontoLiquidacion.setScale(0, RoundingMode.FLOOR).compareTo(monto.setScale(0, RoundingMode.FLOOR)) != 0)
				{
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR,
							"SRV_MRF_OPERACION_MONTO_DISTINTO_PAGOS", mapMsg, conn, in.p_IdEmpresa));
					break;
				}
			}

		}
		if(lstDatosLog.size() >= 1) {
			insertarLogService.logica(conn,lstDatosLog);
		}
		// Busca conceptos usados en mesa
		// --------------------------------------------------------------------------------------
		paramConcep.put("IdTipoProducto", "MERF");
		List<Concepto> selectConcepto = _sql.query(conn, SQL_SELECT_CONCEPTOS_MESA, paramConcep, Concepto.class);
		List<Concepto> selectConceptoIngresoOperacion = new ArrayList<>();
		List<Concepto> selectConceptoIngresoReservaOperacion = new ArrayList<>();
		List<Concepto> selectConceptoRetiroReservaOperacion = new ArrayList<>();
		HashMap<String, Object> selectConceptoIngresoOperacionHash = new HashMap<>();
		HashMap<String, Object> selectConceptoIngresoReservaOperacionHash = new HashMap<>();
		HashMap<String, Object> selectConceptoRetiroReservaOperacionHash = new HashMap<>();
		for (Concepto conce : selectConcepto) {
			switch (conce.Uso) {
			case "INGRESO":
				selectConceptoIngresoOperacion.add(conce);
				break;
			case "INGRESO.RESERVA":
				selectConceptoIngresoReservaOperacion.add(conce);
				break;
			case "RETIRO.RESERVA":
				selectConceptoRetiroReservaOperacion.add(conce);
				break;
			default:
				break;
			}
		}
		selectConceptoIngresoOperacionHash = Utils.List2Hash(selectConceptoIngresoOperacion, "IdTipoOperacion");
		selectConceptoIngresoReservaOperacionHash = Utils.List2Hash(selectConceptoIngresoReservaOperacion, "IdTipoOperacion");
		selectConceptoRetiroReservaOperacionHash = Utils.List2Hash(selectConceptoRetiroReservaOperacion, "IdTipoOperacion");

		// Busca TipoMovimiento usados en mesa

		List<TipoMovimientoMesa> tipoMov = _sql.query(conn, SQL_SELECT_TIPO_MOVIMIENTO, new HashMap<>(), TipoMovimientoMesa.class);

		Map<String, String> tipoMovTes = new HashMap<>();
		Map<String, String> tipoMovCtaCli = new HashMap<>();
		Map<String, String> tipoMovCtaCor = new HashMap<>();
		for (TipoMovimientoMesa tipo : tipoMov) {
			switch (tipo.TipoMov) {
			case "TES":
				tipoMovTes.put(tipo.RecibePaga, tipo.IdTipoMovimiento);
				break;
			case "CTAINVCLI":
				tipoMovCtaCli.put(tipo.RecibePaga, tipo.IdTipoMovimiento);
				break;
			case "CTAINVCOR":
				tipoMovCtaCor.put(tipo.RecibePaga, tipo.IdTipoMovimiento);
				break;
			}
		}

		// Busca sucursal empleado
		HashMap<String, Object> parEmp = new HashMap<>();
		parEmp.put("p_IdUsuario", in.p_IdUsuario);
		parEmp.put("p_IdEmpresa", in.p_IdEmpresa);
		List<Sucursal> idSucursal = _sql.query(conn, SQL_SELECT_SUCURSAL_EMPLEADO, parEmp, Sucursal.class);

		/*********************************
		 * VALIDACIONES FORMAS DE PAGO
		 *********************************/
		// Busca Modalidad de pago //NO VA MAS
		List<ModalidadDePago> modalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_DE_PAGO, new HashMap<>(), ModalidadDePago.class);
		HashMap<String, Object> modalidadPagoHash = Utils.List2Hash(modalidadPago, "IdMedioDePago");

		// Consultar preferencia Pago portafolio
		// --------------------------------------------------------------------------------------
		parPrefPor.put("IdPortafolio", parPre);
		List<PreferenciaPagoPortafolio> prefPagoPortafolio = _sql.query(conn, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_CLIENTE, parPrefPor, PreferenciaPagoPortafolio.class);
		Map<String, List<PreferenciaPagoPortafolio>> groupPreferencias = prefPagoPortafolio.stream().collect(Collectors.groupingBy(asds -> asds.IdPreferenciaPago));
		
		// Consultar preferencia Pago portafolio
		// --------------------------------------------------------------------------------------
		parPrefPorCor.put("IdPortafolio", parPreCor);
		List<PreferenciaPagoPortafolio> prefPagoPortafolioCor = _sql.query(conn, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_CORREDOR, parPrefPorCor, PreferenciaPagoPortafolio.class);
		Map<String, List<PreferenciaPagoPortafolio>> groupPreferenciasCor = prefPagoPortafolioCor.stream().collect(Collectors.groupingBy(asds -> asds.IdPreferenciaPago));
		// Busca formas de pago de la mesa

		HashMap<String, Object> selectFormaPagoMesaIN = new HashMap<>();
		selectFormaPagoMesaIN.put("IdTipoProducto", IdTipoProducto);
		List<FormaDePagoMesa> FormaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, selectFormaPagoMesaIN, FormaDePagoMesa.class);
		
		for(CrearOperacionMesa_FormaPagoOperacion_IN fpagEntrada : in.p_FormasDePago) {
			boolean existeFPM =  FormaDePagoMesaList.stream().filter(fpm -> fpm.IdPreferenciaPago.equals(fpagEntrada.IdFormaDePago)).findFirst().isPresent();
			if(!existeFPM) {
				HashMap<String, String> parametros = new HashMap<>(2);
				parametros.put("NombreFormaDePago",fpagEntrada.NombreFormaDePago);
				parametros.put("IdMoneda",fpagEntrada.IdMoneda);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MESA_RF_NO_TIENE_PREFERENCIA_PAGO", parametros, conn, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				break;
			}
		}
		
		HashMap<String, Object> FormaDePagoMesaHash = Utils.List2Hash(FormaDePagoMesaList, "IdFormaDePago");

		List<ObjCondicionLiquidacion> ListaCondicionLiquidacion = _sql.query(conn, SQL_SELECT_CONDICION_LIQUIDACION, new HashMap<>(), ObjCondicionLiquidacion.class);
		HashMap<String, Object> HashCondicionLiquidacion = Utils.List2Hash(ListaCondicionLiquidacion, "IdCondicionDeLiquidacion");

		// Consulta paridad y tipo cambio Forma de pago
		// --------------------------------------------------------------------------------------
		ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente datDia = new InObtenerDiaSiguiente();
		datDia.p_idPais = idPaisSistema;
		datDia.p_diaHabil = "S";

		OutObtenerDiaSiguiente salDiaSgte = new OutObtenerDiaSiguiente();
		{
			for (CrearOperacionMesa_FormaPagoOperacion_IN inComp : in.p_FormasDePago) {

				FechasIn fechas = new FechasIn();
				fechas.fechaInicial = ((CrearOperacionMesa_Operacion_IN) v_IdOperacionHash.get(inComp.IdInternoOperacion)).FechaLiquidacion;
				fechas.numeroDias = 0L;// ((FormaDePagoMesa) FormaDePagoMesaHash.get(inComp.IdFormaDePago)).Dias;
										// //calculo de dias deacuerdo a la condicion de liquidacion
				datDia.p_fechas.add(fechas);
			}
			salDiaSgte = diaSgte.logica(conn, datDia);
		}

		// TODO:: validaciones
		for (CrearOperacionMesa_Operacion_IN ope : in.p_Operacion) {
			if (selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion) == null) {
				// mapMsg.put("IdOperacion", op.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_CONCEPTOS_MESA_NO_CONFIGURADO", mapMsg, conn, in.p_IdEmpresa));
				Id++;
				break;
			}
		}
		for (CrearOperacionMesa_DetalleOperacion_IN det : in.p_DetalleOperacion) {
			if (selectConceptoIngresoReservaOperacionHash.get(det.IdTipoOperacion) == null
					|| selectConceptoRetiroReservaOperacionHash.get(det.IdTipoOperacion) == null) {
				// mapMsg.put("IdOperacion", op.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_CONCEPTOS_MESA_NO_CONFIGURADO", mapMsg, conn, in.p_IdEmpresa));
				Id++;
				break;
			}
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			return out;
		}
		
	
		//LIMITES CUENTA PROPIA
		ValidarLimiteCuentaPropiaIFRF.SalidaValidarLimiteCuentaPropiaIFRF salidaValidarLimiteCuentaPropia = validarLimiteCuentaPropia(conn, in);
		out.P_OUT_MENSAJES.addAll(salidaValidarLimiteCuentaPropia.P_OUT_MENSAJES);
		
		List<EventoAutorizacion> evtList = new ArrayList<>();
		Map<Long, Validacion> validacionPorIdInternoOperacion = salidaValidarLimiteCuentaPropia.P_OUT_VALIDACIONES.stream().collect(Collectors.toMap(u -> u.IdInternoOperacion, u -> u));
		Set<Long> idsInternosOperacionesRechazadas = new HashSet<>(in.p_Operacion.size());
		in.p_Operacion.stream().filter((o) -> "MODIFICAR".equals(o.Accion) == false).forEach(operacion -> {
			
			Optional<Validacion> maybeValidacion = Optional.ofNullable(validacionPorIdInternoOperacion.get(operacion.IdInternoOperacion-1));
			boolean esAutorizacion = maybeValidacion.map(u -> _cl.TIPO_VALIDACION_AUTORIZACION.equals(u.TipoComportamiento)).orElse(false);
			if (esAutorizacion) {
				operacion.IdEstado = "PEN";
				operacion.RequiereAutorizacion = "S";
				EventoAutorizacion eventoAutorizacion = new EventoAutorizacion();
				eventoAutorizacion.IdTipoEvento = "LIMECP";
				eventoAutorizacion.NumeroOperacion = operacion.IdOperacion;
				eventoAutorizacion.Glosa = "FUERA_LIM_CTA_PROPIA";
				evtList.add(eventoAutorizacion);
			}
			boolean esOperacionRechazada = maybeValidacion.map(u -> _cl.TIPO_VALIDACION_RECHAZAR.equals(u.TipoComportamiento)).orElse(false);
			boolean esGrupoRechazado = salidaValidarLimiteCuentaPropia.p_out_IdInternoGruposRechazados.contains(operacion.IdInternoOperacion);
			if (esOperacionRechazada || esGrupoRechazado) {
				idsInternosOperacionesRechazadas.add(operacion.IdInternoOperacion);
			}
		});
		
		in.p_Operacion = in.p_Operacion.stream().filter(u -> idsInternosOperacionesRechazadas.contains(u.IdInternoOperacion) == false).collect(Collectors.toList());
		in.p_DetalleOperacion = in.p_DetalleOperacion.stream().filter(u -> idsInternosOperacionesRechazadas.contains(u.IdInternoOperacion) == false).collect(Collectors.toList());
		in.p_FormasDePago = in.p_FormasDePago.stream().filter(u -> idsInternosOperacionesRechazadas.contains(u.IdInternoOperacion) == false).collect(Collectors.toList());
		
		Id += evtList.size();

		if(in.p_Operacion.isEmpty()) {
			return out;
		}
		
		if(evtList.size() > 0) {
			CrearEventoAutorizacionMasivo eventoAutorizacion = new CrearEventoAutorizacionMasivo();
			CrearEventoAutorizacionMasivo.ServiceEntrada entradaEventoAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
			entradaEventoAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
			entradaEventoAutorizacion.P_IDMODULO = in.p_IdModulo;
			entradaEventoAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
			entradaEventoAutorizacion.P_SIMULAROPERACION = "N";
			entradaEventoAutorizacion.P_USAREGLASNEGOCIO = "N";			
			
			Map<Long,Validacion> validacionLimCtaPropiaPorIdInterno = salidaValidarLimiteCuentaPropia.P_OUT_VALIDACIONES.stream()
					.collect(Collectors.toMap(u -> u.IdInternoGrupoOperacion, u -> u));
			
			Map<Long,CrearOperacionMesa_DetalleOperacion_IN> detallesOperacionPorIdInternoDetalle = in.p_DetalleOperacion.stream()
					.collect(Collectors.toMap(u -> u.IdInternoDetalle, u -> u));
			
			for(EventoAutorizacion evt: evtList) {
				for(CrearOperacionMesa_Operacion_IN ope: in.p_Operacion) {
					
					if(evt.NumeroOperacion.equals(ope.IdOperacion)) {
						
						CrearOperacionMesa_DetalleOperacion_IN detalleParaAutorizar = detallesOperacionPorIdInternoDetalle.get(validacionLimCtaPropiaPorIdInterno.get(ope.IdInternoOperacion).IdInternoOperacion);
						
						CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
						evento.IdTipoEvento = evt.IdTipoEvento;
						evento.IdModuloSolicita = in.p_IdModulo;
						evento.IdUsuarioSolicita = in.p_IdUsuario;
						evento.Fecha = detalleParaAutorizar.FechaVencimiento;
						evento.IdTipoOperacion = detalleParaAutorizar.IdTipoOperacion;
						evento.IdConcepto = "LIMEIR"; //TODO: Revisar valor correcto. no encuentro uso real del campo
						evento.NumeroOperacionConcepto = ope.IdOperacion.toString();
						evento.FechaOperacionConcepto = "";
						evento.IdCliente = detalleParaAutorizar.IdCliente;
						evento.IdFuncionario = 0L;
						evento.IdProveedor = 0L;
						evento.IdTipoProducto = detalleParaAutorizar.IdTipoProducto;
						evento.IdInstrumento = detalleParaAutorizar.IdInstrumento;
						evento.IdClasificacionTipoInstrumento = detalleParaAutorizar.IdClasificacionTipoInstrumento;
						//evento.IdTipoInstrumento = "PAC"; //TODO: Revisar el valor correcto que debe ir, hasta ahora no se encuentra un uso real del campo al autorizar
						evento.Nemo = detalleParaAutorizar.Nemotecnico;
						evento.Cantidad = 0L;
						evento.Monto = MonedaFormato.formatear(conn, detalleParaAutorizar.Monto, "MONTO", detalleParaAutorizar.IdMoneda).valor;
						evento.Precio = detalleParaAutorizar.Precio;
						evento.Tasa = detalleParaAutorizar.Tasa; // O tasa mercado?
						evento.IdMoneda = detalleParaAutorizar.IdMoneda;
						evento.Glosa = evt.Glosa;
						
						entradaEventoAutorizacion.P_TABLAEVENTOS.add(evento);
						
					}															
				}
			}
			CrearEventoAutorizacionMasivo.ServiceSalida salidaEventoAutorizacion = eventoAutorizacion.logica(conn, entradaEventoAutorizacion);
			for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error: salidaEventoAutorizacion.P_OUT_ERRORESDETALLE) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,error.Codigo,mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			
		}
			
		for (CrearOperacionMesa_Operacion_IN ope : in.p_Operacion) {			
			String concepto = "";
			switch (ope.IdTipoOperacion) {
			case "COMTER":
			case "VENTER":
				// actualizo valor concepto operacion
				IngresarCompraVentaTermino_Operacion_IN opeTer = copyPojoOperacionATermino(ope);
				opeTer.Concepto = (Concepto) selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion);
				opeTer.Persona = (Persona) PersonaHash.get(ope.IdCliente);
				opeTer.IdEmpleado = idSucursal.get(0).IdEmpleado;
				opeTer.RequiereAutorizacion = ope.RequiereAutorizacion;


				opeTer.DocumentoIdentidadCorredor = ((Persona) PersonaHash.get(ope.IdClienteCorredor)).DocumentoIdentidad;
				
				IngresarCompraVentaTermino_IN inTer = new IngresarCompraVentaTermino_IN();
				inTer.p_IdEmpresa = in.p_IdEmpresa;
				inTer.p_IdModulo = in.p_IdModulo;
				inTer.p_IdUsuario = in.p_IdUsuario;
				inTer.p_IdPais = idPaisSistema;
				inTer.p_FechaSistema = fechaSistema;
				inTer.p_IdSucursal = idSucursal.get(0).IdSucursal;
				inTer.p_MonedaSistema = monedaSistema;
				inTer.p_UsaReglasNegocio = in.p_UsaReglasNegocio;

				inTer.p_Operacion.add(opeTer);
				// actualizo concepto Detalle operacion
				List<IngresarCompraVentaTermino_DetalleOperacion_IN> opeDet = new ArrayList<>();
				for (CrearOperacionMesa_DetalleOperacion_IN det : in.p_DetalleOperacion) {

					IngresarCompraVentaTermino_DetalleOperacion_IN opeDetTer = copyPojoOperacionATerminoDetalle(det);
					if (opeDetTer.Cantidad.equals(BigDecimal.ZERO)) { // es un retiro de reserva
						concepto = ((Concepto) selectConceptoRetiroReservaOperacionHash
								.get(opeDetTer.IdTipoOperacion)).IdConcepto;
					} else {
						concepto = ((Concepto) selectConceptoIngresoReservaOperacionHash
								.get(opeDetTer.IdTipoOperacion)).IdConcepto;
					}
					opeDetTer.Concepto = concepto;
					opeDet.add(opeDetTer);
				}
				inTer.p_DetalleOperacion = opeDet;
				if(!sonFormaPagoDefecto) {
					// filtro las formas de pago por operacion
					List<IngresarCompraVentaTermino_FormaPagoOperacion_IN> fPagTerIn = new ArrayList<>();
					for (CrearOperacionMesa_FormaPagoOperacion_IN fPag : in.p_FormasDePago) {
						if (ope.IdInternoOperacion.equals(fPag.IdInternoOperacion)) {
							String fechaCondicionLiquidacion = null;
							for (FechasOut fec : salDiaSgte.p_out_fechas) {
								if (fec.fechaInicial.equals(ope.FechaLiquidacion)) {
									fechaCondicionLiquidacion = fec.fechaCalculada; 
								}
							}
	
							IngresarCompraVentaTermino_FormaPagoOperacion_IN fPagoDep = new IngresarCompraVentaTermino_FormaPagoOperacion_IN();
							fPagoDep.IdInternoPagoOperacion = fPag.IdInternoPagoOperacion;
							fPagoDep.IdInternoOperacion = fPag.IdInternoOperacion;
							fPagoDep.IdComprobantePagoOperacion = fPag.IdComprobantePagoOperacion;
							fPagoDep.IdFormaDePago = fPag.IdFormaDePago;
							fPagoDep.IdMoneda = fPag.IdMoneda;
							fPagoDep.IdOperacion = fPag.IdOperacion;
							fPagoDep.RecibePagaPortafolio = fPag.RecibePagaPortafolio;
							fPagoDep.MontoFormaDePago = fPag.MontoFormaDePago;
							fPagoDep.AfectarTesoreria = fPag.AfectarTesoreria;
							fPagoDep.IdTipoMovimientoTes = ope.IdTipoOperacion.equalsIgnoreCase("COMTER") ? tipoMovTes.get("P") : tipoMovTes.get("R");// tipoMovTes.get(fPag.RecibePagaPortafolio);
							fPagoDep.IdTipoMovimientoCtaCli = ope.IdTipoOperacion.equalsIgnoreCase("COMTER") ? tipoMovCtaCli.get("P") : tipoMovCtaCli.get("R");// tipoMovCtaCli.get(fPag.RecibePagaPortafolio);
							fPagoDep.IdTipoMovimientoCtaCorr = ope.IdTipoOperacion.equalsIgnoreCase("COMTER") ? tipoMovCtaCor.get("P") : tipoMovCtaCor.get("R");// tipoMovCtaCor.get(fPag.RecibePagaPortafolio);
							fPagoDep.FormaDePago = seleccionaFormaPagoMesa(FormaDePagoMesaList, fPag.IdFormaDePago , fPag.RecibePagaPortafolio);
							fPagoDep.IdModalidadDePago = fPagoDep.FormaDePago.IdModalidadDePago;
	
							List<PreferenciaPagoPortafolio> prefPagoPortafolioFiltrada = groupPreferencias.get(fPagoDep.IdFormaDePago);
							List<PreferenciaPagoPortafolio> prefPagoPortafolioCorrFiltrada = groupPreferenciasCor.get(fPagoDep.IdFormaDePago);
							
							if (prefPagoPortafolioFiltrada != null) {
								for (PreferenciaPagoPortafolio pref : prefPagoPortafolioFiltrada) {
									if (pref.IdCuentaBancaria.compareTo(fPag.IdCuentaBancaria) == 0) {
										fPagoDep.PreferenciaDePago = pref;
										fPagoDep.IdCuentaBancaria = pref.IdCuentaBancaria;
										break;
									}
								}
							}
							
							if (prefPagoPortafolioCorrFiltrada != null) {
								for (PreferenciaPagoPortafolio pref : prefPagoPortafolioCorrFiltrada) {
									if (pref.IdCuentaBancaria.compareTo(fPag.IdCuentaBancaria) == 0) {
										fPagoDep.PreferenciaDePago = pref;
										fPagoDep.IdCuentaBancaria = pref.IdCuentaBancaria;
										break;
									}
								}
							}
	
							fPagoDep.FechaLiquidacionFormaDePago = fechaCondicionLiquidacion;// " DATE
							fPagoDep.IdEstado = ope.IdEstado;
							fPagTerIn.add(fPagoDep);
						}
					}
					inTer.p_FormasDePago = fPagTerIn;
				}

				IngresarCompraVentaTermino CompraVentaTermino = new IngresarCompraVentaTermino();
				IngresarCompraVentaTermino_OUT opeTerOut = CompraVentaTermino.logica(conn, inTer);
				logger.debug("[CrearOperacionMesa] (" + (System.nanoTime() - startTime) / 1000000
						+ ") Fin llamada CompraVentaTermino");

				out.P_OUT_MENSAJES.addAll(opeTerOut.P_OUT_MENSAJES);
				out.P_OUT_OPERACION.addAll(opeTerOut.P_OUT_OPERACION);
				out.P_OUT_DETALLEOPERACION.addAll(opeTerOut.P_OUT_DETALLEOPERACION);
				if(!sonFormaPagoDefecto) {
					out.P_OUT_FORMASDEPAGO.addAll(opeTerOut.P_OUT_FORMASDEPAGO);
				}else
				{
					List<CrearOperacionMesa_FormaPagoOperacion_OUT>  salidaComprobantes = this.crearComprobanteFormaDePago(conn,in,sonFormaPagoDefecto,opeTerOut.P_OUT_OPERACION);
					out.P_OUT_FORMASDEPAGO.addAll(salidaComprobantes);	
				}
				

				break;
			case "COMPAC":
			case "VENPAC":
				IngresarCompraVentaPacto_IN inPac = new IngresarCompraVentaPacto_IN();
				IngresarCompraVentaPacto CompraVentaPacto = new IngresarCompraVentaPacto();
				// actualizo valor concepto operacion
				Concepto concepo = (Concepto) selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion);				
				Persona persona = (Persona) PersonaHash.get(ope.IdCliente);
				String documentoIdentidadCorredor = ((Persona) PersonaHash.get(ope.IdClienteCorredor)).DocumentoIdentidad;
				Long idEmpleado = idSucursal.get(0).IdEmpleado;
				
				//creaa cabecera
				IngresarCompraVentaPacto_Operacion_IN opePac = this.crearCabeceraPacto(ope,concepo,persona,documentoIdentidadCorredor,idEmpleado);
				//crea detalles
				List<IngresarCompraVentaPacto_DetalleOperacion_IN> opePacDetalle=this.crearDetallePacto(in, selectConceptoRetiroReservaOperacionHash, selectConceptoIngresoReservaOperacionHash); 
				
				
				inPac.p_IdEmpresa = in.p_IdEmpresa;
				inPac.p_IdModulo = in.p_IdModulo;
				inPac.p_IdUsuario = in.p_IdUsuario;
				inPac.p_IdPais = idPaisSistema;
				inPac.p_FechaSistema = fechaSistema;
				inPac.p_IdSucursal = idSucursal.get(0).IdSucursal;
				inPac.p_MonedaSistema = monedaSistema;
				inPac.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				inPac.p_Operacion.add(opePac);
				//crea formas de pago 
				if(!sonFormaPagoDefecto) {
					List<IngresarCompraVentaPacto_FormaPagoOperacion_IN> opePacFormaPago = this.crearFormaDePagoPacto(in, ope, tipoMovTes, FormaDePagoMesaList, salDiaSgte, tipoMovCtaCli, tipoMovCtaCor, groupPreferencias, groupPreferenciasCor);
					inPac.p_FormasDePago.addAll(opePacFormaPago);
							
				}
				
				List<IngresarCompraVentaPacto_DetalleOperacion_IN> listaDetallesVerificados = this.verificarIdCarteraVigente(conn, opePacDetalle, out, in.p_IdEmpresa, ope.AfectaCustodia);			 
				inPac.p_DetalleOperacion.addAll(listaDetallesVerificados); 
				
				if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) { 
					return out; 
				} 
				
			
				IngresarCompraVentaPacto_OUT opePacOut = CompraVentaPacto.logica(conn, inPac);
				if(sonFormaPagoDefecto) {
					List<CrearOperacionMesa_FormaPagoOperacion_OUT>  salidaComprobantes = this.crearComprobanteFormaDePago(conn,in,sonFormaPagoDefecto,opePacOut.P_OUT_OPERACION);
					out.P_OUT_FORMASDEPAGO.addAll(salidaComprobantes);
				}
				else
				{
					out.P_OUT_FORMASDEPAGO.addAll(opePacOut.P_OUT_FORMASDEPAGO);
				}
				out.P_OUT_MENSAJES.addAll(opePacOut.P_OUT_MENSAJES);
				out.P_OUT_OPERACION.addAll(opePacOut.P_OUT_OPERACION);
				out.P_OUT_DETALLEOPERACION.addAll(opePacOut.P_OUT_DETALLEOPERACION);
					
				break;
			case "TDEPLA":
			case "TAUTDI":
				// actualizo valor concepto operacion

				CrearOperacionDepositoDesembolso_Operacion_IN opeDep = copyPojoOperacionDepositoDesembolso(ope);
				opeDep.Concepto = (Concepto) selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion);
				opeDep.Persona = (Persona) PersonaHash.get(ope.IdCliente);

				TomarDepositoDesembolso_IN inDep = new TomarDepositoDesembolso_IN();
				inDep.p_IdEmpresa = in.p_IdEmpresa;
				inDep.p_IdModulo = in.p_IdModulo;
				inDep.p_IdUsuario = in.p_IdUsuario;
				inDep.p_IdPais = idPaisSistema;
				inDep.p_FechaSistema = fechaSistema;
				inDep.p_IdSucursal = idSucursal.get(0).IdSucursal;
				inDep.p_MonedaSistema = monedaSistema;
				inDep.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				inDep.p_Operacion.add(opeDep);
				// filtro las formas de pago por operacion
				List<CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN> fPagDepAutIn = new ArrayList<>();
				for (CrearOperacionMesa_FormaPagoOperacion_IN fPag : in.p_FormasDePago) {
					if (ope.IdInternoOperacion.equals(fPag.IdInternoOperacion)) {
						String fechaCondicionLiquidacion = null;
						for (FechasOut fec : salDiaSgte.p_out_fechas) {
							if (fec.fechaInicial.equals(ope.FechaLiquidacion) && fec.numeroDiasInicial
									.equals(((FormaDePagoMesa) FormaDePagoMesaHash.get(fPag.IdFormaDePago)).Dias)) {
								fechaCondicionLiquidacion = fec.fechaCalculada;
							}
						}

						CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN fPagoDep = new CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN();

						fPagoDep.IdInternoPagoOperacion = fPag.IdInternoPagoOperacion;
						fPagoDep.IdInternoOperacion = fPag.IdInternoOperacion;
						fPagoDep.IdComprobantePagoOperacion = fPag.IdComprobantePagoOperacion;
						fPagoDep.IdFormaDePago = fPag.IdFormaDePago;
						fPagoDep.IdMoneda = fPag.IdMoneda;
						fPagoDep.IdOperacion = fPag.IdOperacion;
						fPagoDep.RecibePagaPortafolio = fPag.RecibePagaPortafolio;
						fPagoDep.MontoFormaDePago = fPag.MontoFormaDePago;
						fPagoDep.AfectarTesoreria = fPag.AfectarTesoreria;

						fPagoDep.IdTipoMovimientoTes = tipoMovTes.get(fPag.RecibePagaPortafolio);
						fPagoDep.IdTipoMovimientoCtaCli = tipoMovCtaCli.get(fPag.RecibePagaPortafolio);
						fPagoDep.IdTipoMovimientoCtaCorr = tipoMovCtaCor.get(fPag.RecibePagaPortafolio);
						fPagoDep.IdModalidadDePago = ((ModalidadDePago) modalidadPagoHash
								.get(((FormaDePagoMesa) FormaDePagoMesaHash
										.get(fPag.IdFormaDePago)).IdMedioDePago)).IdModalidadDePago;
						fPagoDep.FormaDePago = ((FormaDePagoMesa) FormaDePagoMesaHash.get(fPag.IdFormaDePago));

						List<PreferenciaPagoPortafolio> prefPagoPortafolioFiltrada = groupPreferencias.get(fPagoDep.IdFormaDePago);
						if (prefPagoPortafolioFiltrada != null) {
							for (PreferenciaPagoPortafolio pref : prefPagoPortafolioFiltrada) {
								if (fPagoDep.FormaDePago.IdPreferenciaPago.equals(pref.IdPreferenciaPago)) {
									fPagoDep.PreferenciaDePago = pref;
								}
							}
						}
						// fPagoDep.MontoLiquidacionFormaDePago = ((CalculoSalida)
						// paridadFpagoSalidaHash.get(fPag.IdInternoPagoOperacion)).MontoDestino.compareTo(BigDecimal.ZERO)
						// > 0?((CalculoSalida)
						// paridadFpagoSalidaHash.get(fPag.IdInternoPagoOperacion)).MontoDestino:((CalculoSalida)
						// paridadFpagoSalidaHash.get(fPag.IdInternoPagoOperacion)).MontoOrigen;//BigDecimal.ZERO;//"
						// DECIMAL(26,8)
						fPagoDep.FechaLiquidacionFormaDePago = fechaCondicionLiquidacion;// " DATE
						fPagoDep.IdEstado = ope.IdEstado;
						fPagDepAutIn.add(fPagoDep);
					}
				}
				inDep.p_FormasDePago = fPagDepAutIn;

				TomarDepositoDesembolso dep = new TomarDepositoDesembolso();
				logger.debug("[CrearOperacionMesa] Inicio llamada TomarDepositoDesembolso");
				startTime = System.nanoTime();
				TomarDepositoDesembolso_OUT depAutDepOut = dep.logica(conn, inDep);
				logger.debug("[CrearOperacionMesa] (" + (System.nanoTime() - startTime) / 1000000
						+ ") Fin llamada TomarDepositoDesembolso");
				out.P_OUT_MENSAJES.addAll(depAutDepOut.P_OUT_MENSAJES);
				out.P_OUT_OPERACION.addAll(depAutDepOut.P_OUT_OPERACION);
				out.P_OUT_FORMASDEPAGO.addAll(depAutDepOut.P_OUT_FORMASDEPAGO);
				break;

			default:
				break;
			}
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			return out;
		}

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		// Consulta datosMesa Origen
		HashMap<String, Object> MesOrigen = new HashMap<String, Object>();
		MesOrigen.put("IdMesa", in.p_Operacion.get(0).IdMesaOrigen);
		List<Mesa> MesaListOrigen = _sql.query(conn, SQL_SELECT_MESA, MesOrigen, Mesa.class);
		// Consulta datosMesa Origen
		HashMap<String, Object> MesDest = new HashMap<String, Object>();
		MesDest.put("IdMesa", in.p_Operacion.get(0).IdMesaDestino);
		List<Mesa> MesaListDestino = _sql.query(conn, SQL_SELECT_MESA, MesDest, Mesa.class);
		
		// Consulta datos Cliente
		HashMap<String, Object> parCliDinamico = new HashMap<String, Object>();
		parCliDinamico.put("IdCliente", pcDinamico);
		parCliDinamico.put("IdPortafolio", parPre);
		List<Persona> PersonaListDinamico = _sql.query(conn, SQL_SELECT_CLIENTE, parCliDinamico, Persona.class);
		
		DatosOperacionDinamico objtDatosDinamico = new DatosOperacionDinamico();
		//preparamos los datos a enviar
		for(Persona persona:PersonaListDinamico) {
			objtDatosDinamico.DocumentoIdentidad = persona.DocumentoIdentidad;
			objtDatosDinamico.RazonSocial = persona.RazonSocial;
			objtDatosDinamico.CodigoPortafolio=persona.CodigoPortafolio;
		}
		
		for(Mesa mesaOrigen:MesaListOrigen) {
			objtDatosDinamico.IdMesaOrigen = mesaOrigen.IdMesa;
			objtDatosDinamico.NombreMesaOrigen = mesaOrigen.Nombre;
		}
		for(Mesa mesaDestino:MesaListDestino) {
			objtDatosDinamico.IdMesaDestino = mesaDestino.IdMesa;
			objtDatosDinamico.NombreMesaDestino = mesaDestino.Nombre;
			objtDatosDinamico.IdClienteCorredor = mesaDestino.IdCliente;
			objtDatosDinamico.IdPortafolioCorredor =mesaDestino.IdPortafolio;
		}
		
		String tipoOperacion ="";
		for(CrearOperacionMesa_Operacion_OUT operacion:out.P_OUT_OPERACION) {
			objtDatosDinamico.FolioOperacion=operacion.FolioOperacion;
			objtDatosDinamico.IdOperacion=operacion.IdOperacion;
			objtDatosDinamico.IdEstado= operacion.IdEstado;
			objtDatosDinamico.Intermesa= operacion.Intermesa;
			objtDatosDinamico.TipoOperacion=operacion.IdTipoOperacion;
			objtDatosDinamico.Tasa=operacion.Tasa;
			objtDatosDinamico.PlazoEnDias=operacion.Plazo;
			tipoOperacion =operacion.Accion;
			objtDatosDinamico.IdTipoReajuste=operacion.IdTipoReajuste;
			
			objtDatosDinamico.MontoOperacion=operacion.Monto;
			objtDatosDinamico.MontoOperacionAnticipo=BigDecimal.ZERO;
			objtDatosDinamico.MontoOperacionFinal=operacion.ValorFinal;
			objtDatosDinamico.MontoOperacionFinalAnticipo=BigDecimal.ZERO;;
			objtDatosDinamico.MontoOperacionFinalMoneda=operacion.ValorFinalMoneda;
			objtDatosDinamico.MontoOperacionFinalMonedaAnticipo=BigDecimal.ZERO;;
			objtDatosDinamico.MontoOperacionInicial = operacion.Monto;
			objtDatosDinamico.MontoOperacionInicialAnticipo = BigDecimal.ZERO;;
			objtDatosDinamico.MontoOperacionInicialMoneda = operacion.MontoMoneda;
			objtDatosDinamico.MontoOperacionInicialMonedaAnticipo=BigDecimal.ZERO;;
			objtDatosDinamico.MontoOperacionMoneda=operacion.MontoMoneda;
			objtDatosDinamico.MontoOperacionMonedaAnticipo=BigDecimal.ZERO;
			
			objtDatosDinamico.NombreEstado=operacion.NombreEstado;
				
			objtDatosDinamico.IdMoneda = operacion.IdMoneda;
			objtDatosDinamico.IdAgente = operacion.IdAgente;
			objtDatosDinamico.IdUsuarioCreacion = in.p_IdUsuario;
			objtDatosDinamico.IdUsuarioConfirma = 0L;
			
			objtDatosDinamico.IdTipoProducto = operacion.IdTipoProducto;
			objtDatosDinamico.AfectaCustodia = operacion.AfectaCustodia;
			
			objtDatosDinamico.ParametroConfirmacion ="N";
			objtDatosDinamico.ConfirmaMediosPago =operacion.IdEstado.equals("PEN")?"NCF":"CNF";
			objtDatosDinamico.UsuarioConfirma = 0L;
			objtDatosDinamico.Glosa=operacion.Glosa;
			
			objtDatosDinamico.IdTipoCanalIngreso =operacion.IdTipoCanalIngreso;
			objtDatosDinamico.IdTipoOperacion =operacion.IdTipoOperacion;
			objtDatosDinamico.IdTipoProducto = operacion.IdTipoProducto;
			objtDatosDinamico.IdTipoProducto = operacion.IdTipoProducto;
			objtDatosDinamico.NombreOperacion = operacion.IdTipoOperacion.equals("COMPAC")?"COMPRA CON PACTO": operacion.IdTipoOperacion.equals("VENPAC")?"VENTA CON PACTO":operacion.IdTipoOperacion.equals("VENTER")?"VENTA A TERMINO": "COMPRA A TERMINO";
			
			objtDatosDinamico.FechaOperacion=operacion.FechaOperacion;
			objtDatosDinamico.FechaVencimiento = operacion.FechaVencimiento;
			objtDatosDinamico.FechaLiquidacion = operacion.FechaOperacion;
			objtDatosDinamico.FechaLiquidacionString = operacion.FechaOperacion.toString();
			objtDatosDinamico.FechaVencimientoUI = operacion.FechaVencimiento.toString();
			objtDatosDinamico.FechaAnticipo = operacion.FechaOperacion;
			objtDatosDinamico.FechaCondicionLiquidacion = operacion.FechaOperacion;
			objtDatosDinamico.FechaCreacion = operacion.FechaOperacion;
			
			objtDatosDinamico.Anticipado ="N";
			objtDatosDinamico.CodigoUsuario ="";

		
		}
		Gson gson = new Gson();
		String mensajeDinamico = gson.toJson(objtDatosDinamico);
		/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS MESARF *****/
		CacheServicio.invalidarPorTag(CacheMRF.TAG_POSICION);
		AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, tipoOperacion.equals("MODIFICAR")?"MODMRF":"ACTMRF", mensajeDinamico);
		
		
		
		agregarLogSistema(conn,out, fechaSistema, in.p_IdUsuario, in.p_IdEmpresa, in.p_IdModulo);
		// Notificaciones
		// -----------------------------------------
		if (out.P_OUT_ERRORESCONTADOR.equals(0L) && lstDatosOperacion.size() > 0) {
			AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
			notifIn.P_IDEMPRESA = in.p_IdEmpresa;
			notifIn.P_IDUSUARIO = in.p_IdUsuario;
			notifIn.P_IDMODULO = in.p_IdModulo;

			List<ObjEVENTOS> eventosNotificacion = new ArrayList<>();
			List<ObjIDDIRECCIONES> direccionesNotificacion = new ArrayList<>();

			for (DatosOperacion movs : lstDatosOperacion) {

				ObjEVENTOS evento = new ObjEVENTOS();
				evento.CodigoTipoEvento = "NOTIFICACION_MESA_RF";
				evento.FechaCreacionSistema = fechaSistema;
				evento.IdPersonaDestinatario = movs.IdPersona;
				evento.IdPersonaRemitente = movs.IdPersona;
				evento.IdPortafolio = 0L;
				evento.IdSecuencia = movs.FolioOperacion;
				eventosNotificacion.add(evento);

				Resultado Monto = MonedaFormato.formatear(conn, movs.MontoOperacionFinal, MonedaFormato.MONTO,
						movs.IdTipoReajuste, null, null);
				Resultado MontoComprometido = MonedaFormato.formatear(conn, movs.MontoOperacionMoneda,
						MonedaFormato.MONTO, movs.IdTipoReajuste, null, null);

				// Valores generales
				notifIn.addValor(movs.FolioOperacion, "FOLIOOPERACION", movs.FolioOperacion.toString())
						.addValor(movs.FolioOperacion, "NOMBRETIPOOPERACION", movs.NombreTipoOperacion)
						.addValor(movs.FolioOperacion, "AGENTE", movs.Agente)
						.addValor(movs.FolioOperacion, "DOCUMENTOIDENTIDAD", movs.DocumentoIdentidad)
						.addValor(movs.FolioOperacion, "PORTAFOLIO", movs.CodigoPortafolio)

						.addValor(movs.FolioOperacion, "FECHAOPERACION",
								Fecha.toFormat(Fecha.toDate(fechaSistema), "dd-MM-yyyy"))
						.addValor(movs.FolioOperacion, "IDOPERACION", movs.IdOperacion.toString())
						.addValor(movs.FolioOperacion, "ESTADO", movs.NombreEstado)

						.addValor(movs.FolioOperacion, "FOLIOOPERACION", movs.FolioOperacion.toString())
						.addValor(movs.FolioOperacion, "PRODUCTO", movs.NombreProducto)
						.addValor(movs.FolioOperacion, "OPERACION", movs.IdOperacion.toString())
						.addValor(movs.FolioOperacion, "MONTOOPERACIONMONEDA", Monto.toString())
						.addValor(movs.FolioOperacion, "TIPOREAJUSTE", movs.IdTipoReajuste)
						.addValor(movs.FolioOperacion, "TASA", movs.Tasa == null ? "" : movs.Tasa.toString())
						.addValor(movs.FolioOperacion, "MONTOCOMPROMETIDO", MontoComprometido.toString())
						.addValor(movs.FolioOperacion, "FECHAVENCIMIENTO",
								Fecha.toFormat(Fecha.toDate(movs.FechaVencimiento), "dd-MM-yyyy"));

				ObjIDDIRECCIONES dir1 = new ObjIDDIRECCIONES();
				dir1.IdDireccion = movs.IdDireccionElectronica;

				dir1.IdSecuencia = movs.FolioOperacion;
				dir1.TipoDestinatario = "CC";

				ObjIDDIRECCIONES dir2 = new ObjIDDIRECCIONES();
				dir2.IdDireccion = movs.IdDireccionElectronica;
				dir2.IdSecuencia = movs.FolioOperacion;
				dir2.TipoDestinatario = "TO";

				direccionesNotificacion.add(dir1);
				direccionesNotificacion.add(dir2);
			}

			notifIn.P_EVENTOS = eventosNotificacion;
			notifIn.P_IDDIRECCIONES = direccionesNotificacion;

			if (notifIn.P_EVENTOS.size() > 0) {
				AgregarEvento notif = new AgregarEvento();

				logger.debug("[CrearOperacionMesa--] Inicio llamada AgregarEvento Notificacion");
				startTime = System.nanoTime();
				AgregarEvento.ServiceSalida notifOut = notif.logica(conn, notifIn);
				logger.debug("[CrearOperacionMesa--] (" + (System.nanoTime() - startTime) / 1000000
						+ ") Fin  llamada AgregarEvento Notificacion");

			}
		}

		logger.info("[CrearOperacionMesa] Termino del servicio CrearOperacionMesa, tiempo: "
				+ (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init) / 1000 + " seg");
		
		return out;
	};
	private void agregarLogSistema(Connection con, CrearOperacionMesa_OUT out, String fechaSistema, Long IdUsuario, Long IdEmpresa, String IdModulo) throws Exception {
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		if(out.P_OUT_OPERACION.size()>0) {
			for(CrearOperacionMesa_Operacion_OUT OPIN : out.P_OUT_OPERACION) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = OPIN.IdOperacion;
				log.IdTipoLog = OPIN.Accion.equalsIgnoreCase("CREAR") ? "INGRESO" : "MODIFICACION" ;
				log.IdModulo = IdModulo;
				log.IdUsuario = IdUsuario;
				log.IdEmpresa = IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = IdUsuario;
				log.IdCliente =  OPIN.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio =  OPIN.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = OPIN.Accion.equalsIgnoreCase("CREAR") ? "INGRESO" : "MODIFICACION" ;
				log.EstadoRegistro = "";
				log.IdTipoOperacion = "";
				log.Folio =  OPIN.IdOperacion;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa =  OPIN.Tasa;
				log.Monto =  OPIN.Monto;
				log.IdMonedaOrigen =  OPIN.IdTipoReajuste;
				log.IdMonedaLiquidacion = OPIN.IdMonedaLiquidacion;
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.NombreProceso = OPIN.Accion.equalsIgnoreCase("CREAR") ? "INGRESO" : "MODIFICACION"  + "MRF.CrearOperacionMesa"; 						
				entradaAgregarLog.p_Log.add(log);
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
			}
		}
	}
	

	private List<IngresarCompraVentaPacto_DetalleOperacion_IN> verificarIdCarteraVigente(Connection conn,List<IngresarCompraVentaPacto_DetalleOperacion_IN> opePacDetalle,  CrearOperacionMesa_OUT out, Long idEmpresa, String afectaCustodia) throws Exception {
		logger.debug("INICIO::CrearOperacionMesa.verificarIdCarteraVigente");
		long inicioMetodo = System.currentTimeMillis();

		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> listaFolios = opePacDetalle.stream().map(detalle -> detalle.FolioCartera).collect(Collectors.toList());
		params.put("ListaFolioCartera", listaFolios);

		Map<Long,VerificarCartera> mapCarterasVigentes =  sql.query(conn, SQL_SELECT_ID_CARTERA_RESERVA, params, VerificarCartera.class)
												.stream().collect(Collectors.toMap(cartera -> cartera.FolioCartera, cartera-> cartera));

		List<IngresarCompraVentaPacto_DetalleOperacion_IN> detallesActualizados = new ArrayList<>();
		for (IngresarCompraVentaPacto_DetalleOperacion_IN detalle : opePacDetalle) {
			VerificarCartera carteraVerificada = mapCarterasVigentes.get(detalle.FolioCartera);
			if (detalle.FolioCartera != 0 && carteraVerificada != null && carteraVerificada.IdCartera.compareTo(detalle.IdCartera) != 0) {
				detalle.IdCartera = carteraVerificada.IdCartera;
			}

			if (carteraVerificada == null && afectaCustodia.equals("S") && detalle.IdCartera.compareTo(0L) != 0) {
				HashMap<String, String> mapMsg = new HashMap<>();
				mapMsg.put("FolioCartera", detalle.FolioCartera.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_CARTERA_NO_TIENE_CUSTODIA", mapMsg, conn, idEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
				break;
			} else {
			  detallesActualizados.add(detalle);
			}

		}



		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionMesa.verificarIdCarteraVigente :" , finMetodo- inicioMetodo);

		return detallesActualizados;

	}

	// ------------------------------------------------------------
	// METODOS UTILITARIOS
	// ------------------------------------------------------------
	
	public IngresarCompraVentaPacto_Operacion_IN crearCabeceraPacto (CrearOperacionMesa_Operacion_IN ope,Concepto concepto,Persona persona ,String documentoIdentidadCorredor ,Long idEmpleado) {
		
		IngresarCompraVentaPacto_Operacion_IN opePac = copyPojoOperacionPacto(ope);
		opePac.Concepto = concepto;
		opePac.Persona = persona;
		opePac.DocumentoIdentidadCorredor = documentoIdentidadCorredor;
		opePac.IdEmpleado = idEmpleado;
				
		opePac.TasaAnualizada 	= ope.TasaAnualizada;
		opePac.CostoFondo	  	= ope.CostoFondo != null ? new BigDecimal(ope.CostoFondo.replace(",",".")): BigDecimal.ZERO;
		opePac.UtilidadPerdida	= ope.UtilidadPerdida;
		opePac.UtilidadFinal	= ope.UtilidadFinal;
		opePac.ValorizacionManual = ope.ValorizacionManual;		
		
		
		return opePac;
	}
	
	public List<IngresarCompraVentaPacto_DetalleOperacion_IN> crearDetallePacto (CrearOperacionMesa_IN in,HashMap<String, Object> selectConceptoRetiroReservaOperacionHash,HashMap<String, Object> selectConceptoIngresoReservaOperacionHash) {			
		
		List<IngresarCompraVentaPacto_DetalleOperacion_IN> opeDetPac = new ArrayList<>();
		String concepto= "";
		
		for (CrearOperacionMesa_DetalleOperacion_IN det : in.p_DetalleOperacion) {
			IngresarCompraVentaPacto_DetalleOperacion_IN opeDetPacto = copyPojoOperacionPactoDetalle(det);

			if (("RF").equals(det.IdClasificacionTipoInstrumento) && opeDetPacto.Cantidad.compareTo(BigDecimal.ZERO)==0  ||("IF").equals(det.IdClasificacionTipoInstrumento) && opeDetPacto.ValorRescate.compareTo(BigDecimal.ZERO)==0 ) { // es un retiro de reserva
				concepto = ((Concepto) selectConceptoRetiroReservaOperacionHash.get(opeDetPacto.IdTipoOperacion)).IdConcepto;
			} else {
				concepto = ((Concepto) selectConceptoIngresoReservaOperacionHash
						.get(opeDetPacto.IdTipoOperacion)).IdConcepto;
			}
			opeDetPacto.Concepto = concepto;

			// Se calcula el ValorFinal para COMPAC y VENPAC
			if (opeDetPacto.IdTipoOperacion.contentEquals("COMPAC") || opeDetPacto.IdTipoOperacion.contentEquals("VENPAC")) {	

				BigDecimal uno = new BigDecimal(1);
				BigDecimal cien = new BigDecimal(100);
				BigDecimal plazo = new BigDecimal(in.p_Operacion.get(0).Plazo);
				BigDecimal base = new BigDecimal(30);

				BigDecimal tasaCien = in.p_Operacion.get(0).Tasa.divide(cien, MathContext.DECIMAL128);
				BigDecimal plazoBase = plazo.divide(base, MathContext.DECIMAL128);
				BigDecimal mult = tasaCien.multiply(plazoBase);
				BigDecimal sum = uno.add(mult);

				opeDetPacto.ValorFinal = opeDetPacto.Monto.multiply(sum).setScale(0, RoundingMode.HALF_UP);
			}

			opeDetPac.add(opeDetPacto);
		}
		
		return opeDetPac;
	}
	

	public List<IngresarCompraVentaPacto_FormaPagoOperacion_IN> crearFormaDePagoPacto (CrearOperacionMesa_IN in,CrearOperacionMesa_Operacion_IN ope,Map<String, String> tipoMovTes,List<FormaDePagoMesa> FormaDePagoMesaList  
			,OutObtenerDiaSiguiente salDiaSgte,Map<String, String> tipoMovCtaCli ,Map<String, String> tipoMovCtaCor,Map<String, List<PreferenciaPagoPortafolio>> groupPreferencias,Map<String, List<PreferenciaPagoPortafolio>> groupPreferenciasCor) {
		
		
		// filtro las formas de pago por operacion
		List<IngresarCompraVentaPacto_FormaPagoOperacion_IN> fPagPacIn = new ArrayList<>();
		for (CrearOperacionMesa_FormaPagoOperacion_IN fPag : in.p_FormasDePago) {
			if (ope.IdInternoOperacion.equals(fPag.IdInternoOperacion)) {
				String fechaCondicionLiquidacion = fPag.FechaLiquidacion;
				if (fechaCondicionLiquidacion != null) {
					for (FechasOut fec : salDiaSgte.p_out_fechas) {
						if (fec.fechaInicial.equals(ope.FechaLiquidacion) && fec.numeroDiasInicial.equals(0L)) { // ((FormaDePagoMesa)
							fechaCondicionLiquidacion = fec.fechaCalculada;
						}
					}
				}

				IngresarCompraVentaPacto_FormaPagoOperacion_IN fPagoPac = new IngresarCompraVentaPacto_FormaPagoOperacion_IN();

				fPagoPac.IdInternoPagoOperacion = fPag.IdInternoPagoOperacion;
				fPagoPac.IdInternoOperacion = fPag.IdInternoOperacion;
				fPagoPac.IdComprobantePagoOperacion = fPag.IdComprobantePagoOperacion;
				fPagoPac.IdFormaDePago = fPag.IdFormaDePago;
				fPagoPac.IdMoneda = fPag.IdMoneda;
				fPagoPac.IdOperacion = fPag.IdOperacion;
				fPagoPac.RecibePagaPortafolio = fPag.RecibePagaPortafolio;
				fPagoPac.MontoFormaDePago = fPag.MontoFormaDePago;
				fPagoPac.AfectarTesoreria = fPag.AfectarTesoreria;
				if("VENPAC".equals(ope.IdTipoOperacion)) {
					fPagoPac.IdTipoMovimientoTes = fPag.RecibePagaPortafolio.equalsIgnoreCase("P") ? tipoMovTes.get("R") : tipoMovTes.get("P");//P(forma pago inicial) debe generar ingreso (R) en tesoreria
					fPagoPac.IdTipoMovimientoCtaCli = fPag.RecibePagaPortafolio.equalsIgnoreCase("P") ? tipoMovCtaCli.get("R") : tipoMovCtaCli.get("P");// tipoMovCtaCli.get(fPag.RecibePagaPortafolio);
					fPagoPac.IdTipoMovimientoCtaCorr = fPag.RecibePagaPortafolio.equalsIgnoreCase("P") ? tipoMovCtaCor.get("R") : tipoMovCtaCor.get("P");// tipoMovCtaCor.get(fPag.RecibePagaPortafolio);
				}
				else {
					fPagoPac.IdTipoMovimientoTes = fPag.RecibePagaPortafolio.equalsIgnoreCase("P") ? tipoMovTes.get("P") : tipoMovTes.get("R");//P(forma pago inicial) debe generar egreso (P) en tesoreria
					fPagoPac.IdTipoMovimientoCtaCli = fPag.RecibePagaPortafolio.equalsIgnoreCase("P") ? tipoMovCtaCli.get("P") : tipoMovCtaCli.get("R");// tipoMovCtaCli.get(fPag.RecibePagaPortafolio);
					fPagoPac.IdTipoMovimientoCtaCorr = fPag.RecibePagaPortafolio.equalsIgnoreCase("P") ? tipoMovCtaCor.get("P") : tipoMovCtaCor.get("R");// tipoMovCtaCor.get(fPag.RecibePagaPortafolio);
				}
				fPagoPac.FormaDePago = 	seleccionaFormaPagoMesa(FormaDePagoMesaList, fPag.IdFormaDePago,  fPag.RecibePagaPortafolio);// ((FormaDePagoMesa)																		// FormaDePagoMesaHash.get(fPag.IdFormaDePago));
				fPagoPac.IdModalidadDePago = fPagoPac.FormaDePago.IdModalidadDePago;// ((ModalidadDePago) modalidadPagoHash.get(((FormaDePagoMesa)
																					// FormaDePagoMesaHash.get(fPag.IdFormaDePago)).IdMedioDePago)).IdModalidadDePago;
				
				List<PreferenciaPagoPortafolio> prefPagoPortafolioFiltrada = groupPreferencias.get(fPagoPac.IdFormaDePago);						
				List<PreferenciaPagoPortafolio> prefPagoPortafolioCorrFiltrada = groupPreferenciasCor.get(fPagoPac.IdFormaDePago);
				
				if (prefPagoPortafolioFiltrada != null) {
					for (PreferenciaPagoPortafolio pref : prefPagoPortafolioFiltrada) {
						if (pref.IdCuentaBancaria.compareTo(fPag.IdCuentaBancaria) == 0) {
							fPagoPac.PreferenciaDePago = pref;
							fPagoPac.IdCuentaBancaria = pref.IdCuentaBancaria;
							break;
						}
					}
				}
				if (prefPagoPortafolioCorrFiltrada != null) {
					for (PreferenciaPagoPortafolio pref : prefPagoPortafolioCorrFiltrada) {
						if (pref.IdCuentaBancaria.compareTo(fPag.IdCuentaBancaria) == 0) {
							fPagoPac.PreferenciaDePago = pref;
							fPagoPac.IdCuentaBancaria = pref.IdCuentaBancaria;
							break;
						}
					}
				}
				
				fPagoPac.IdCuentaBancaria = fPag.IdCuentaBancaria;
				fPagoPac.FechaLiquidacionFormaDePago = ope.FechaLiquidacion;
				fPagoPac.IdEstado = ope.IdEstado;
				fPagPacIn.add(fPagoPac);
			}
		}
		
		return fPagPacIn;
		
	}
	
	private FormaDePagoMesa seleccionaFormaPagoMesa(List<FormaDePagoMesa> FormaDePagoMesaList, String preferenciaPago, String RecibePagaPortafolio) {				
		
		FormaDePagoMesa fp = FormaDePagoMesaList.stream().filter(c -> c.IdPreferenciaPago.equals(preferenciaPago)).findFirst().orElse(new FormaDePagoMesa());
		FormaDePagoMesa formaPagoRespuesta = new FormaDePagoMesa();
		
		formaPagoRespuesta.IdFormaDePago = fp.IdFormaDePago ;                             
		formaPagoRespuesta.NombreFormaDePago =fp.NombreFormaDePago;                          
		formaPagoRespuesta.Dias =fp.Dias;                                       
		formaPagoRespuesta.PorDefecto =fp.PorDefecto;                                 
		formaPagoRespuesta.RecibePagaPortafolioDesc ="R".equals(RecibePagaPortafolio) ? "Recibimos":"Entregamos";                   
		formaPagoRespuesta.IdEstado =fp.IdEstado;                                   
		formaPagoRespuesta.NombreEstado =fp.NombreEstado;                               
		formaPagoRespuesta.IdPreferenciaPago =fp.IdPreferenciaPago;                          
		formaPagoRespuesta.IdTipoOperacion =fp.IdTipoOperacion;                            
		formaPagoRespuesta.IdMedioDePago =fp.IdMedioDePago;                              
		formaPagoRespuesta.IdMoneda =fp.IdMoneda;                                   
		formaPagoRespuesta.UsaPersonaRelacionada =fp.UsaPersonaRelacionada;                      
		formaPagoRespuesta.FechaCreacion =fp.FechaCreacion;                              
		formaPagoRespuesta.FechaModificacion =fp.FechaModificacion;                          
		formaPagoRespuesta.IdUsuarioCreador =fp.IdUsuarioCreador;                           
		formaPagoRespuesta.IdEmpresa =fp.IdEmpresa;                                  
		formaPagoRespuesta.NombreTipoOperacion =fp.NombreTipoOperacion;                        
		formaPagoRespuesta.IdTipoProducto =fp.IdTipoProducto;                             
		formaPagoRespuesta.NombreTipoProducto =fp.NombreTipoProducto;                         
		formaPagoRespuesta.RecibePagaPortafolio =RecibePagaPortafolio;                       
		formaPagoRespuesta.NombreMedioDePago =fp.NombreMedioDePago;                          
		formaPagoRespuesta.NombreMoneda =fp.NombreMoneda;                               
		formaPagoRespuesta.UsaCuentaBancaria =fp.UsaCuentaBancaria;                          
		formaPagoRespuesta.UsaTarjetaBancaria =fp.UsaTarjetaBancaria;                         
		formaPagoRespuesta.IdEstadoPreferenciaPago =fp.IdEstadoPreferenciaPago;                    
		formaPagoRespuesta.NombreEstadoPreferenciaPago =fp.NombreEstadoPreferenciaPago;                
		formaPagoRespuesta.IdModalidadDePago =fp.IdModalidadDePago; 
		return formaPagoRespuesta;
	}

	private IngresarCompraVentaPacto_DetalleOperacion_IN copyPojoOperacionPactoDetalle(
			CrearOperacionMesa_DetalleOperacion_IN operacionDetalle) {

		IngresarCompraVentaPacto_DetalleOperacion_IN operacionDetallePacto = new IngresarCompraVentaPacto_DetalleOperacion_IN();

		operacionDetallePacto.IdInternoDetalle = operacionDetalle.IdInternoDetalle;
		operacionDetallePacto.IdInternoOperacion = operacionDetalle.IdInternoOperacion;
		operacionDetallePacto.IdReserva = operacionDetalle.IdReserva;
		operacionDetallePacto.IdCliente = operacionDetalle.IdCliente;
		operacionDetallePacto.IdPortafolio = operacionDetalle.IdPortafolio;
		operacionDetallePacto.IdClienteCorredor = operacionDetalle.IdClienteCorredor;
		operacionDetallePacto.IdPortafolioCorredor = operacionDetalle.IdPortafolioCorredor;
		operacionDetallePacto.IdMesa = operacionDetalle.IdMesa;
		operacionDetallePacto.IdTipoProducto = operacionDetalle.IdTipoProducto;
		operacionDetallePacto.IdTipoOperacion = operacionDetalle.IdTipoOperacion;
		operacionDetallePacto.IdInstrumento = operacionDetalle.IdInstrumento;
		operacionDetallePacto.Nemotecnico = operacionDetalle.Nemotecnico;
		operacionDetallePacto.EsFungible = operacionDetalle.EsFungible;
		operacionDetallePacto.IdBolsa = operacionDetalle.IdBolsa;
		operacionDetallePacto.IdBoveda = operacionDetalle.IdBoveda;
		operacionDetallePacto.IdCartera = operacionDetalle.IdCartera;
		operacionDetallePacto.FolioCartera = operacionDetalle.FolioCartera;
		operacionDetallePacto.CodigoIFRS = operacionDetalle.CodigoIFRS;
		operacionDetallePacto.IdEmisor = operacionDetalle.IdEmisor;
		operacionDetallePacto.IdMoneda = operacionDetalle.IdMoneda;
		operacionDetallePacto.IdTipoReajuste = operacionDetalle.IdTipoReajuste;
		operacionDetallePacto.IdMonedaEmisionInstrumento = operacionDetalle.IdMonedaEmisionInstrumento;
		operacionDetallePacto.IdMonedaLiquidacionInstrumento = operacionDetalle.IdMonedaLiquidacionInstrumento;
		operacionDetallePacto.IdMonedaLiquidacionOperacion = operacionDetalle.IdMonedaLiquidacionOperacion;
		operacionDetallePacto.CorteMinimo = operacionDetalle.CorteMinimo;
		operacionDetallePacto.Cantidad = operacionDetalle.Cantidad;
		operacionDetallePacto.Precio = operacionDetalle.Precio;
		operacionDetallePacto.PrecioMercado = operacionDetalle.PrecioMercado;
		operacionDetallePacto.Tasa = operacionDetalle.Tasa;
		operacionDetallePacto.TasaMercado = operacionDetalle.TasaMercado;
		operacionDetallePacto.Monto = operacionDetalle.Monto;
		operacionDetallePacto.MontoLiquidacion = operacionDetalle.MontoLiquidacion;
		operacionDetallePacto.MontoLiquidacionOperacion = operacionDetalle.MontoLiquidacionOperacion;
		operacionDetallePacto.ValorRescate = operacionDetalle.ValorRescate;
		operacionDetallePacto.FechaVencimiento = operacionDetalle.FechaVencimiento;
		operacionDetallePacto.MontoValorizadoReserva = operacionDetalle.MontoValorizadoReserva;
		operacionDetallePacto.TipoCarteraCorredor = operacionDetalle.TipoCarteraCorredor;
		operacionDetallePacto.MontoFinalCarteraMonedaSistema=operacionDetalle.MontoFinalCarteraMonedaSistema;
		operacionDetallePacto.MontoFinalCarteraMonedaInstrumento=operacionDetalle.MontoFinalCarteraMonedaInstrumento;
		operacionDetallePacto.INGMontoAPlazoDetalle=operacionDetalle.INGMontoAPlazoDetalle;
		operacionDetallePacto.INGMontoAPlazoMonedaInstrumentoDetalle=operacionDetalle.INGMontoAPlazoMonedaInstrumentoDetalle;
		return operacionDetallePacto;
	}

	private IngresarCompraVentaPacto_Operacion_IN copyPojoOperacionPacto(CrearOperacionMesa_Operacion_IN operacion) {

		IngresarCompraVentaPacto_Operacion_IN operacionPacto = new IngresarCompraVentaPacto_Operacion_IN();

		operacionPacto.Accion = operacion.Accion;
		operacionPacto.IdInternoOperacion = operacion.IdInternoOperacion;
		operacionPacto.IdOperacion = operacion.IdOperacion;
		operacionPacto.FolioOperacion = operacion.FolioOperacion;
		operacionPacto.FolioCartera = operacion.FolioCartera;
		operacionPacto.IdCliente = operacion.IdCliente;
		operacionPacto.IdPortafolio = operacion.IdPortafolio;
		operacionPacto.IdClienteCorredor = operacion.IdClienteCorredor;
		operacionPacto.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
		operacionPacto.Intermesa = operacion.Intermesa;
		operacionPacto.IdMesaOrigen = operacion.IdMesaOrigen;
		operacionPacto.IdMesaDestino = operacion.IdMesaDestino;
		operacionPacto.AfectaCustodia = operacion.AfectaCustodia;
		operacionPacto.IdTipoProducto = operacion.IdTipoProducto;
		operacionPacto.IdTipoOperacion = operacion.IdTipoOperacion;
		operacionPacto.IdTipoReajuste = operacion.IdTipoReajuste;
		operacionPacto.Plazo = operacion.Plazo;
		operacionPacto.Monto = operacion.Monto;
		operacionPacto.MontoMoneda = operacion.MontoMoneda;
		operacionPacto.IdMoneda = operacion.IdMoneda;
		operacionPacto.IdMonedaLiquidacion = operacion.IdMonedaLiquidacion;
		operacionPacto.Tasa = operacion.Tasa;
		operacionPacto.ValorFinal = operacion.ValorFinal;
		operacionPacto.ValorFinalMoneda = operacion.ValorFinalMoneda;
		operacionPacto.TasaAgente = operacion.TasaAgente;
		operacionPacto.FechaOperacion = operacion.FechaOperacion;
		operacionPacto.FechaVencimiento = operacion.FechaVencimiento;
		operacionPacto.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
		operacionPacto.FechaLiquidacion = operacion.FechaLiquidacion;
		operacionPacto.IdAgente = operacion.IdAgente;
		operacionPacto.IdOperador = operacion.IdOperador;
		operacionPacto.IdEstado = operacion.IdEstado;
		operacionPacto.Glosa = operacion.Glosa;
		operacionPacto.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
		operacionPacto.TipoDocumento = operacion.TipoDocumento;
		operacionPacto.IdContacto = operacion.IdContacto;

		return operacionPacto;
	}

	private IngresarCompraVentaTermino_DetalleOperacion_IN copyPojoOperacionATerminoDetalle(
			CrearOperacionMesa_DetalleOperacion_IN operacionDetalle) {

		IngresarCompraVentaTermino_DetalleOperacion_IN operacionDetalleTermino = new IngresarCompraVentaTermino_DetalleOperacion_IN();

		operacionDetalleTermino.IdInternoDetalle = operacionDetalle.IdInternoDetalle;
		operacionDetalleTermino.IdInternoOperacion = operacionDetalle.IdInternoOperacion;
		operacionDetalleTermino.IdReserva = operacionDetalle.IdReserva;
		operacionDetalleTermino.IdCliente = operacionDetalle.IdCliente;
		operacionDetalleTermino.IdPortafolio = operacionDetalle.IdPortafolio;
		operacionDetalleTermino.IdClienteCorredor = operacionDetalle.IdClienteCorredor;
		operacionDetalleTermino.IdPortafolioCorredor = operacionDetalle.IdPortafolioCorredor;
		operacionDetalleTermino.IdMesa = operacionDetalle.IdMesa;
		operacionDetalleTermino.IdTipoProducto = operacionDetalle.IdTipoProducto;
		operacionDetalleTermino.IdTipoOperacion = operacionDetalle.IdTipoOperacion;
		operacionDetalleTermino.IdInstrumento = operacionDetalle.IdInstrumento;
		operacionDetalleTermino.Nemotecnico = operacionDetalle.Nemotecnico;
		operacionDetalleTermino.EsFungible = operacionDetalle.EsFungible;
		operacionDetalleTermino.IdBolsa = operacionDetalle.IdBolsa;
		operacionDetalleTermino.IdBoveda = operacionDetalle.IdBoveda;
		operacionDetalleTermino.IdCartera = operacionDetalle.IdCartera;
		operacionDetalleTermino.FolioCartera = operacionDetalle.FolioCartera;
		operacionDetalleTermino.CodigoIFRS = operacionDetalle.CodigoIFRS;
		operacionDetalleTermino.IdEmisor = operacionDetalle.IdEmisor;
		operacionDetalleTermino.IdMoneda = operacionDetalle.IdMoneda;
		operacionDetalleTermino.IdTipoReajuste = operacionDetalle.IdTipoReajuste;
		operacionDetalleTermino.IdMonedaEmisionInstrumento = operacionDetalle.IdMonedaEmisionInstrumento;
		operacionDetalleTermino.IdMonedaLiquidacionInstrumento = operacionDetalle.IdMonedaLiquidacionInstrumento;
		operacionDetalleTermino.IdMonedaLiquidacionOperacion = operacionDetalle.IdMonedaLiquidacionOperacion;
		operacionDetalleTermino.CorteMinimo = operacionDetalle.CorteMinimo;
		operacionDetalleTermino.Cantidad = operacionDetalle.Cantidad;
		operacionDetalleTermino.Precio = operacionDetalle.Precio;
		operacionDetalleTermino.PrecioMercado = operacionDetalle.PrecioMercado;
		operacionDetalleTermino.Tasa = operacionDetalle.Tasa;
		operacionDetalleTermino.TasaMercado = operacionDetalle.TasaMercado;
		operacionDetalleTermino.Monto = operacionDetalle.Monto;
		operacionDetalleTermino.MontoLiquidacion = operacionDetalle.MontoLiquidacion;
		operacionDetalleTermino.MontoLiquidacionOperacion = operacionDetalle.MontoLiquidacionOperacion;
		operacionDetalleTermino.ValorRescate = operacionDetalle.ValorRescate;
		operacionDetalleTermino.FechaVencimiento = operacionDetalle.FechaVencimiento;
		operacionDetalleTermino.MontoValorizadoReserva = operacionDetalle.MontoValorizadoReserva;

		return operacionDetalleTermino;
	}

	private IngresarCompraVentaTermino_Operacion_IN copyPojoOperacionATermino(
			CrearOperacionMesa_Operacion_IN operacion) {
		IngresarCompraVentaTermino_Operacion_IN operacionTermino = new IngresarCompraVentaTermino_Operacion_IN();

		operacionTermino.Accion = operacion.Accion;
		operacionTermino.IdInternoOperacion = operacion.IdInternoOperacion;
		operacionTermino.IdOperacion = operacion.IdOperacion;
		operacionTermino.FolioOperacion = operacion.FolioOperacion;
		operacionTermino.FolioCartera = operacion.FolioCartera;
		operacionTermino.IdCliente = operacion.IdCliente;
		operacionTermino.IdPortafolio = operacion.IdPortafolio;
		operacionTermino.IdClienteCorredor = operacion.IdClienteCorredor;
		operacionTermino.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
		operacionTermino.Intermesa = operacion.Intermesa;
		operacionTermino.IdMesaOrigen = operacion.IdMesaOrigen;
		operacionTermino.IdMesaDestino = operacion.IdMesaDestino;
		operacionTermino.AfectaCustodia = operacion.AfectaCustodia;
		operacionTermino.IdTipoProducto = operacion.IdTipoProducto;
		operacionTermino.IdTipoOperacion = operacion.IdTipoOperacion;
		operacionTermino.IdTipoReajuste = operacion.IdTipoReajuste;
		operacionTermino.Plazo = operacion.Plazo;
		operacionTermino.Monto = operacion.Monto;
		operacionTermino.MontoMoneda = operacion.MontoMoneda;
		operacionTermino.IdMoneda = operacion.IdMoneda;
		operacionTermino.IdMonedaLiquidacion = operacion.IdMonedaLiquidacion;
		operacionTermino.Tasa = operacion.Tasa;
		operacionTermino.ValorFinal = operacion.ValorFinal;
		operacionTermino.ValorFinalMoneda = operacion.ValorFinalMoneda;
		operacionTermino.TasaAgente = operacion.TasaAgente;
		operacionTermino.FechaOperacion = operacion.FechaOperacion;
		operacionTermino.FechaVencimiento = operacion.FechaVencimiento;
		operacionTermino.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
		operacionTermino.FechaLiquidacion = operacion.FechaLiquidacion;
		operacionTermino.IdAgente = operacion.IdAgente;
		operacionTermino.IdOperador = operacion.IdOperador;
		operacionTermino.IdEstado = operacion.IdEstado;
		operacionTermino.Glosa = operacion.Glosa;
		operacionTermino.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
		operacionTermino.TipoDocumento = operacion.TipoDocumento;
		operacionTermino.IdContacto = operacion.IdContacto;
		operacionTermino.ValorizacionManual = operacion.ValorizacionManual;
		return operacionTermino;
	}

	private CrearOperacionDepositoDesembolso_Operacion_IN copyPojoOperacionDepositoDesembolso(
			CrearOperacionMesa_Operacion_IN operacion) {

		CrearOperacionDepositoDesembolso_Operacion_IN operacionDepositoDesembolso = new CrearOperacionDepositoDesembolso_Operacion_IN();

		operacionDepositoDesembolso.Accion = operacion.Accion;
		operacionDepositoDesembolso.IdInternoOperacion = operacion.IdInternoOperacion;
		operacionDepositoDesembolso.IdOperacion = operacion.IdOperacion;
		operacionDepositoDesembolso.FolioOperacion = operacion.FolioOperacion;
		operacionDepositoDesembolso.FolioCartera = operacion.FolioCartera;
		operacionDepositoDesembolso.IdCliente = operacion.IdCliente;
		operacionDepositoDesembolso.IdPortafolio = operacion.IdPortafolio;
		operacionDepositoDesembolso.IdClienteCorredor = operacion.IdClienteCorredor;
		operacionDepositoDesembolso.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
		operacionDepositoDesembolso.Intermesa = operacion.Intermesa;
		operacionDepositoDesembolso.IdMesaOrigen = operacion.IdMesaOrigen;
		operacionDepositoDesembolso.IdMesaDestino = operacion.IdMesaDestino;
		operacionDepositoDesembolso.AfectaCustodia = operacion.AfectaCustodia;
		operacionDepositoDesembolso.IdTipoProducto = operacion.IdTipoProducto;
		operacionDepositoDesembolso.IdTipoOperacion = operacion.IdTipoOperacion;
		operacionDepositoDesembolso.IdTipoReajuste = operacion.IdTipoReajuste;
		operacionDepositoDesembolso.Plazo = operacion.Plazo;
		operacionDepositoDesembolso.Monto = operacion.Monto;
		operacionDepositoDesembolso.MontoMoneda = operacion.MontoMoneda;
		operacionDepositoDesembolso.IdMoneda = operacion.IdMoneda;
		operacionDepositoDesembolso.IdMonedaLiquidacion = operacion.IdMonedaLiquidacion;
		operacionDepositoDesembolso.Tasa = operacion.Tasa;
		operacionDepositoDesembolso.ValorFinal = operacion.ValorFinal;
		operacionDepositoDesembolso.ValorFinalMoneda = operacion.ValorFinalMoneda;
		operacionDepositoDesembolso.TasaAgente = operacion.TasaAgente;
		operacionDepositoDesembolso.FechaOperacion = operacion.FechaOperacion;
		operacionDepositoDesembolso.FechaVencimiento = operacion.FechaVencimiento;
		operacionDepositoDesembolso.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
		operacionDepositoDesembolso.FechaLiquidacion = operacion.FechaLiquidacion;
		operacionDepositoDesembolso.IdAgente = operacion.IdAgente;
		operacionDepositoDesembolso.IdOperador = operacion.IdOperador;
		operacionDepositoDesembolso.IdEstado = operacion.IdEstado;
		operacionDepositoDesembolso.Glosa = operacion.Glosa;
		operacionDepositoDesembolso.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
		operacionDepositoDesembolso.IdEmisor = operacion.IdEmisor;

		return operacionDepositoDesembolso;
	}
	
	private ValidarLimiteCuentaPropiaIFRF.SalidaValidarLimiteCuentaPropiaIFRF validarLimiteCuentaPropia(Connection conn,CrearOperacionMesa_IN entrada) throws Exception{
		logger.info("INICIO::CrearOperacionMesa.validarLimiteCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimiteCuentaPropiaIFRF validarLimiteCuentaPropia = new ValidarLimiteCuentaPropiaIFRF();
		
		ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF entradaValidarLimiteCuentaPropia = new ValidarLimiteCuentaPropiaIFRF.EntradaValidarLimiteCuentaPropiaIFRF();
		
		Map<Long,CrearOperacionMesa_Operacion_IN> operacionPorIdInternoOperacion = entrada.p_Operacion.stream()
				.collect(Collectors.toMap(u -> u.IdInternoOperacion, u-> u));
		
		entradaValidarLimiteCuentaPropia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimiteCuentaPropia.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimiteCuentaPropia.p_IdModulo  = entrada.p_IdModulo;
		entradaValidarLimiteCuentaPropia.p_UsaReglasNegocio = "S"; //entrada.p_UsaReglasNegocio;
		entradaValidarLimiteCuentaPropia.p_IdTipoProceso = entrada.p_Operacion.stream().filter(u -> u.Accion.equals("MODIFICAR")).count() > 0 ? _cl.PROCESO_MODIFICACION_MESA :  _cl.PROCESO_INGRESAR;
		entradaValidarLimiteCuentaPropia.p_Operaciones = entrada.p_DetalleOperacion.stream()
				.filter(det -> _cl.TIPO_OPERACION_MESA_VALIDO.contains(det.IdTipoOperacion))
				.map(det -> {
					ValidarLimiteCuentaPropiaIFRF.Operacion o = new ValidarLimiteCuentaPropiaIFRF.Operacion();
					o.IdInterno = det.IdInternoDetalle;
					o.IdOperacion = det.IdInternoOperacion;
					o.IdInternoGrupo = det.IdInternoOperacion;
					o.Folio = operacionPorIdInternoOperacion.get(det.IdInternoOperacion).IdOperacion.toString();
					o.IdCliente = det.IdCliente;
					o.IdPortafolio = det.IdPortafolio;
					o.IdInstrumento = det.IdInstrumento;
					o.IdTipoCanalIngreso = operacionPorIdInternoOperacion.get(det.IdInternoOperacion).IdTipoCanalIngreso;
					o.IdTipoOperacion = det.IdTipoOperacion;
					o.IdMonedaOperacion = det.IdMonedaLiquidacionOperacion;
					o.Tasa = det.Tasa;
					o.MontoCompraMonedaSistema = det.Monto; 
					o.Cantidad = det.Cantidad;
					o.ValorRescate = det.ValorRescate;
					o.FechaVencimientoInstrumento = Fecha.toDate(det.FechaVencimiento);				
					return o;
				})
				.collect(Collectors.toList());
		
		ValidarLimiteCuentaPropiaIFRF.SalidaValidarLimiteCuentaPropiaIFRF salidaValidarLimiteCuentaPropia = validarLimiteCuentaPropia.logica(conn, entradaValidarLimiteCuentaPropia);
		
		long fin = System.currentTimeMillis();
		logger.info("INICIO::CrearOperacionMesa.validarLimiteCuentaPropia {}", fin-inicio);
		
		return salidaValidarLimiteCuentaPropia;
	}
	
	private List<CrearOperacionMesa_FormaPagoOperacion_IN> crearFormaDePagoPorDefecto(Connection conn,List<CrearOperacionMesa_Operacion_IN> operacionDetalle) throws SQLException {
		SQL sql = new SQL();
		
		Long idInternoOperacion = 0L;
		Long idPortafolioCli =0L;
		Long idPortafolioCor = 0L;
		String idTipoOperacion="";
		String idTipoOperacionCli ="";
		String idMoneda="";
		String fechaOperacion="";
		String fechaLiquidacion="";
		String fechaVencimiento="";
		Long idOperacion =0L;
		Long idInternoPagoOperacion =0L;
		BigDecimal monto = BigDecimal.ZERO;
		BigDecimal montoLiquidacion = BigDecimal.ZERO;

		String recibePagaPortafolioCli ="";
		String recibePagaPortafolioCor ="";
		
		BigDecimal montoAsignadofmpCli = BigDecimal.ZERO;
		BigDecimal montoAsignadofmpCor = BigDecimal.ZERO;
		HashMap<String, Object>paramsFmpCli = new HashMap<>(3);
		HashMap<String, Object>paramsFmpCor = new HashMap<>(3);
		
		for(CrearOperacionMesa_Operacion_IN op: operacionDetalle) {
			idInternoOperacion = op.IdInternoOperacion;
			idPortafolioCli = op.IdPortafolio;
			idPortafolioCor = op.IdPortafolioCorredor;
			idTipoOperacion = op.IdTipoOperacion;
			
			idMoneda=op.IdMoneda;
			fechaOperacion = op.FechaOperacion;
			fechaLiquidacion = op.FechaLiquidacion;
			fechaVencimiento = op.FechaVencimiento;
			idOperacion = op.IdOperacion;
			if(op.IdTipoReajuste.equals("UF")) {
				switch (idTipoOperacion) {
				
				case "VENTER":
					monto = op.Monto;
					montoLiquidacion =op.Monto;
					break;
				case "COMTER":
					monto = op.Monto;
					montoLiquidacion =op.Monto;
					break;	
					
				case "COMPAC":
					monto = op.ValorFinal;
					montoLiquidacion =op.Monto;
					break;
				case "VENPAC":
					monto = op.ValorFinal;
					montoLiquidacion =op.Monto;
					break;	
				default:
					break;
				}
			}else {
				monto = idTipoOperacion.equals("VENTER") || idTipoOperacion.equals("COMTER") ?op.MontoMoneda:op.ValorFinalMoneda;
				montoLiquidacion =op.MontoMoneda;
			}
			
			
		}
		
		if(idTipoOperacion.equals("VENPAC")) {
			paramsFmpCor.put("IdPortafolio",idPortafolioCor);
			paramsFmpCor.put("IdMoneda",idMoneda);
			paramsFmpCor.put("IdTipoOperacion","VENPAC");
			recibePagaPortafolioCor= "P";
			montoAsignadofmpCor =montoLiquidacion;
			
			
			paramsFmpCli.put("IdPortafolio",idPortafolioCli);
			paramsFmpCli.put("IdMoneda",idMoneda);
			paramsFmpCli.put("IdTipoOperacion","VENPAC");
			recibePagaPortafolioCli ="R";
			montoAsignadofmpCli = monto;
			
		}else if(idTipoOperacion.equals("COMPAC")){
			paramsFmpCor.put("IdPortafolio",idPortafolioCor);
			paramsFmpCor.put("IdMoneda",idMoneda);
			paramsFmpCor.put("IdTipoOperacion","VENPAC");
			recibePagaPortafolioCor ="R";
			montoAsignadofmpCor = monto;
			
			paramsFmpCli.put("IdPortafolio",idPortafolioCli);
			paramsFmpCli.put("IdMoneda",idMoneda);
			paramsFmpCli.put("IdTipoOperacion","VENPAC");
			recibePagaPortafolioCli= "P";
			montoAsignadofmpCli =montoLiquidacion;
		}else if(idTipoOperacion.equals("VENTER")) {
			paramsFmpCor.put("IdPortafolio",idPortafolioCor);
			paramsFmpCor.put("IdMoneda",idMoneda);
			paramsFmpCor.put("IdTipoOperacion","VENTER");
			recibePagaPortafolioCor= "P";
			montoAsignadofmpCor =montoLiquidacion;
		}else if(idTipoOperacion.equals("COMTER")) {
			paramsFmpCli.put("IdPortafolio",idPortafolioCli);
			paramsFmpCli.put("IdMoneda",idMoneda);
			paramsFmpCli.put("IdTipoOperacion","VENTER");
			recibePagaPortafolioCli= "P";
			montoAsignadofmpCli =montoLiquidacion;
		}
	
		List<CrearOperacionMesa_FormaPagoOperacion_IN> formasDePago = new ArrayList<CrearOperacionMesa_FormaPagoOperacion_IN>();
		
		if(paramsFmpCli.size()>0) {
			List<PreferenciaPagoPortafolio> fmpDefectoCli = sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CLIENTE,paramsFmpCli , PreferenciaPagoPortafolio.class);
			if(!(("VENTER").equals(idTipoOperacion) && "P".equals(recibePagaPortafolioCli)) || !(("COMTER").equals(idTipoOperacion) && "R".equals(recibePagaPortafolioCli))) {
			 	for(PreferenciaPagoPortafolio fmpCli:fmpDefectoCli) {
			 		CrearOperacionMesa_FormaPagoOperacion_IN fmp = new CrearOperacionMesa_FormaPagoOperacion_IN();
			 		idInternoPagoOperacion +=1;
			 		fmp.AfectarTesoreria = fmpCli.IdMedioDePago.equals("CIN")?"N":"S";
			 		fmp.FechaLiquidacion = "P".equals(recibePagaPortafolioCli)?fechaLiquidacion:fechaVencimiento;
			 		fmp.IdOperacion = idOperacion;
			 		fmp.IdInternoPagoOperacion = idInternoPagoOperacion;
			 		fmp.IdFormaDePago = fmpCli.IdPreferenciaPago;
			 		fmp.IdMoneda = idMoneda;
			 		fmp.RecibePagaPortafolio =recibePagaPortafolioCli;
			 		fmp.MontoFormaDePago = montoAsignadofmpCli;
			 		fmp.IdCuentaBancaria = fmpCli.IdCuentaBancaria;
			 		fmp.IdInternoOperacion = idInternoOperacion;
			 		fmp.IdComprobantePagoOperacion =0L;
			 		fmp.NombreBanco = fmpCli.NombreBancoCuentaBancaria;
			 		fmp.NombreBancoTarjeta = fmpCli.NombreBancoTarjetaBancaria;
			 		fmp.CuentaBancaria = fmpCli.NumeroCuentaBancaria;
			 		fmp.NumeroTarjeta = fmpCli.NumeroTarjetaBancaria;
			 		formasDePago.add(fmp);
			 	}
			 }
		}
		if(paramsFmpCor.size()>0) {
			List<PreferenciaPagoPortafolio> fmpDefectoCor = sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CORREDOR,paramsFmpCor , PreferenciaPagoPortafolio.class);
			if(!("COMTER").equals(idTipoOperacion)) {
		 		for(PreferenciaPagoPortafolio fmpCor:fmpDefectoCor) {
		 			CrearOperacionMesa_FormaPagoOperacion_IN fmp = new CrearOperacionMesa_FormaPagoOperacion_IN();
			 		idInternoPagoOperacion +=1;
			 		fmp.AfectarTesoreria = fmpCor.IdMedioDePago.equals("CIN")?"N":"S";
			 		fmp.FechaLiquidacion ="P".equals(recibePagaPortafolioCor)?fechaLiquidacion:fechaVencimiento;
			 		fmp.IdOperacion = idOperacion;
			 		fmp.IdInternoPagoOperacion = idInternoPagoOperacion;
			 		fmp.IdFormaDePago = fmpCor.IdPreferenciaPago;
			 		fmp.IdMoneda = idMoneda;
			 		fmp.RecibePagaPortafolio =recibePagaPortafolioCor;
			 		fmp.MontoFormaDePago = montoAsignadofmpCor;
			 		fmp.IdCuentaBancaria = fmpCor.IdCuentaBancaria;
			 		fmp.IdInternoOperacion = idInternoOperacion;
			 		fmp.IdComprobantePagoOperacion =0L;
			 		fmp.NombreBanco = fmpCor.NombreBancoCuentaBancaria;
			 		fmp.NombreBancoTarjeta = fmpCor.NombreBancoTarjetaBancaria;
			 		fmp.CuentaBancaria = fmpCor.NumeroCuentaBancaria;
			 		fmp.NumeroTarjeta = fmpCor.NumeroTarjetaBancaria;
			 		formasDePago.add(fmp);
			 	}
			}
		}
		
		return formasDePago;
	}
	private String modalidadDePago(String IdFormaDePago) {
		String modalidad = "MODDEP";
		
		if(IdFormaDePago.contains("CHE") || IdFormaDePago.contains("VVI")||IdFormaDePago.contains("EFE")||IdFormaDePago.contains("EBA")) {
			 modalidad = "MODCAJ";
		}
		return modalidad;
	}
	private List<CrearOperacionMesa_FormaPagoOperacion_OUT>  crearComprobanteFormaDePago(Connection conn,CrearOperacionMesa_IN in,Boolean sonFormaPagoDefecto,List<CrearOperacionMesa_Operacion_OUT> ope) throws Exception {
		logger.debug("INICIO::IngresarDetalleOperacionMesa.crearComprobanteFormaDePago");
		long inicioMetodo = System.currentTimeMillis();
		
		List<CrearOperacionMesa_FormaPagoOperacion_OUT> ListaFormaDePagoSalida = new ArrayList();
		CrearComprobantePagoOperacion_OUT comprobanteSalida = new CrearComprobantePagoOperacion_OUT();		
		CrearComprobantePagoOperacion_IN  comprobanteEntrada = new CrearComprobantePagoOperacion_IN();
		CrearComprobantePagoOperacion comprobantes = new CrearComprobantePagoOperacion();
		List<ComprobantePagoOperacionObj_IN> ListaformaDePago = new ArrayList();
		Long IdOperacion = 0L; 
		comprobanteEntrada.p_IdEmpresa= in.p_IdEmpresa;
		comprobanteEntrada.p_IdModulo= in.p_IdModulo;
		comprobanteEntrada.p_IdUsuario= in.p_IdUsuario;
		comprobanteEntrada.p_UsaReglasNegocio="N";
		comprobanteEntrada.p_FormaPagoNueva=sonFormaPagoDefecto?"S":"N";
		comprobanteEntrada.p_ActualizaFormaPago=sonFormaPagoDefecto?"N":"S";		
		
		for(CrearOperacionMesa_Operacion_OUT op: ope) {
			IdOperacion = op.IdOperacion;
		}
		
		for(CrearOperacionMesa_FormaPagoOperacion_IN formaPago: in.p_FormasDePago) {
			ComprobantePagoOperacionObj_IN fpIngreso = new ComprobantePagoOperacionObj_IN();
			fpIngreso.IdInternoPagoOperacion = formaPago.IdInternoOperacion;
			fpIngreso.IdComprobantePagoOperacion=formaPago.IdComprobantePagoOperacion;
			fpIngreso.IdEmpresa= in.p_IdEmpresa;
			fpIngreso.IdOperacion= IdOperacion;
			fpIngreso.IdIngresoEgreso= 0L;
			fpIngreso.IdAbono= 0L;
			fpIngreso.IdCargo= 0L;
			fpIngreso.IdIngresoEgresoAnticipo= 0L;
			fpIngreso.IdAbonoAnticipo= 0L;
			fpIngreso.IdCargoAnticipo= 0L;
			fpIngreso.IdTipoProducto= "MERF";
			fpIngreso.RecibePagaPortafolio= formaPago.RecibePagaPortafolio;
			fpIngreso.IdFormaDePago= formaPago.IdFormaDePago;
			fpIngreso.IdModalidadPago= this.modalidadDePago(formaPago.IdFormaDePago); // buscar validacion
			fpIngreso.IdMoneda= formaPago.IdMoneda;
			fpIngreso.Monto= formaPago.MontoFormaDePago;
			fpIngreso.FechaIngresoComprobantePagoOperacion= formaPago.FechaLiquidacion;
			fpIngreso.FechaLiquidacionComprobantePagoOperacion= formaPago.FechaLiquidacion;
			fpIngreso.IdEstado = "ACT";
			fpIngreso.AfectarTesoreria= formaPago.AfectarTesoreria;
			fpIngreso.IdCuentaBanco= formaPago.IdCuentaBancaria;
			
			ListaformaDePago.add(fpIngreso);
		}
		
		comprobanteEntrada.p_ComprobantePagoOperacion.addAll(ListaformaDePago);
		comprobanteSalida = comprobantes.logica(conn, comprobanteEntrada);
		
		comprobanteSalida.P_OUT_FORMADEPAGO.forEach(compSalida->{
			CrearOperacionMesa_FormaPagoOperacion_OUT fpSalida = new CrearOperacionMesa_FormaPagoOperacion_OUT();
			fpSalida.IdInternoPagoOperacion= compSalida.IdInternoPagoOperacion;
			fpSalida.IdComprobantePagoOperacion =compSalida.IdComprobantePagoOperacion;
			fpSalida.IdFormaDePago= compSalida.IdFormaDePago;
			fpSalida.IdMoneda= compSalida.IdMoneda;
			fpSalida.IdOperacion= compSalida.IdOperacion;
			fpSalida.RecibePagaPortafolio= compSalida.RecibePaga;
			fpSalida.MontoFormaDePago= compSalida.Monto;
			fpSalida.NombreFormaDePago= compSalida.NombreFormaDePago;
			fpSalida.NombreMoneda= compSalida.IdMoneda;
			fpSalida.IdMedioDePago= compSalida.NombreFormaDePago;
			fpSalida.AfectarTesoreria= compSalida.AfectarTesoreria;
			fpSalida.IdCuentaBancaria= compSalida.IdCuentaBancaria;
			fpSalida.NombreBanco= compSalida.NombreBancoCuentaBancaria;
			fpSalida.CuentaBancaria=compSalida.NumeroCuentaBancaria;
			
		
			ListaFormaDePagoSalida.add(fpSalida);
			
		});
	
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::IngresarDetalleOperacionMesa.crearComprobanteFormaDePago :" , finMetodo-inicioMetodo);
		return ListaFormaDePagoSalida;
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		CrearOperacionMesa_IN jin = gson.fromJson(in.toString(), CrearOperacionMesa_IN.class);
		CrearOperacionMesa_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

	// ------------------------------------------------------------
	// METODO PRINCIPAL PARA PRUEBAS LOCALES
	// ------------------------------------------------------------

	public static void main(String[] args) throws Exception, SQLException {
		Connection conn = null;
		Gson gson = new Gson();

		try {
			System.out.println("== BEGIN ==");
			//(conn = PlataformaUtils.getPRUEBAConnection();
			conn = PlataformaUtils.getPropertiesConnection("desa02", "c:/archivos/connections.properties");
		
			String json1 ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_IdEstadoConfirmacionPago\":\"NCF\",\"p_Operacion\":[{\"Accion\":\"CERRAR\",\"IdInternoOperacion\":1,\"IdOperacion\":150918,\"FolioOperacion\":0,\"IdCliente\":6182,\"IdPortafolio\":3894,\"IdClienteCorredor\":4846,\"IdPortafolioCorredor\":2225,\"Intermesa\":\"N\",\"IdMesaOrigen\":39,\"IdMesaDestino\":39,\"AfectaCustodia\":\"S\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdTipoReajuste\":\"CLP\",\"Plazo\":21,\"Monto\":15600300,\"MontoMoneda\":15600300,\"MontoLiquidacion\":15600300,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"Tasa\":6.3,\"ValorFinal\":16288273,\"ValorFinalMoneda\":16288273,\"TasaAgente\":0.12,\"FechaOperacion\":\"2024-02-20\",\"FechaVencimiento\":\"2024-03-12\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2024-02-20\",\"IdAgente\":5632,\"IdOperador\":97,\"IdEstado\":\"PEN\",\"Glosa\":\"\",\"IdTipoCanalIngreso\":\"AGE\",\"TipoDocumento\":\"\",\"IdContacto\":0,\"TasaAnualizada\":0,\"CostoFondo\":0.02,\"UtilidadPerdida\":293909.652,\"UtilidadFinal\":293909.652,\"GuardandoCabecera\":\"N\",\"ValorizacionManual\":\"S\"}],\"p_DetalleOperacion\":[{\"IdInternoDetalle\":0,\"IdInternoOperacion\":1,\"IdReserva\":16156,\"IdCliente\":6182,\"IdPortafolio\":3894,\"IdClienteCorredor\":4846,\"IdPortafolioCorredor\":2225,\"IdMesa\":39,\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdClasificacionTipoInstrumento\":\"RF\",\"IdInstrumento\":70102,\"Nemotecnico\":\"BARAU-F\",\"EsFungible\":\"N\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"FolioCartera\":542207,\"IdCartera\":542092,\"CodigoIFRS\":\"T\",\"IdEmisor\":\"ARAUCO\",\"IdMoneda\":\"CLP\",\"IdTipoReajuste\":\"UF\",\"IdMonedaEmisionInstrumento\":\"UF\",\"IdMonedaLiquidacionInstrumento\":\"CLP\",\"IdMonedaLiquidacionOperacion\":\"CLP\",\"CorteMinimo\":500,\"Cantidad\":500,\"Precio\":99.8592,\"PrecioMercado\":0,\"Tasa\":4.3,\"TasaMercado\":0,\"Monto\":15600300,\"MontoLiquidacion\":10145728,\"MontoLiquidacionOperacion\":15600300,\"ValorRescate\":0,\"FechaVencimiento\":\"2029-10-30\",\"MontoValorizadoReserva\":0,\"TipoCarteraCorredor\":\"\",\"INGMontoAPlazoDetalle\":16288273.23,\"INGMontoAPlazoMonedaInstrumentoDetalle\":16288273.23}],\"p_FormasDePago\":[{\"AfectarTesoreria\":\"S\",\"IdComprobantePagoOperacion\":1449378,\"IdCuentaBancaria\":4314,\"IdFormaDePago\":\"VENPAC_TRA_CLP\",\"IdInternoOperacion\":1,\"IdInternoPagoOperacion\":1,\"IdMoneda\":\"CLP\",\"IdOperacion\":150918,\"IdTipoCuentaBancaria\":\"\",\"MontoFormaDePago\":15600300,\"NombreCuentaBancaria\":\"\",\"RecibePagaPortafolio\":\"P\",\"NombreFormaDePago\":\"TRANSFERENCIA\",\"NumeroCuentaBancaria\":\"32970050352\",\"IdMedioDePago\":\"TRANSFERENCIA\"},{\"AfectarTesoreria\":\"S\",\"IdComprobantePagoOperacion\":1449379,\"IdCuentaBancaria\":1084,\"IdFormaDePago\":\"VENPAC_TRA_CLP\",\"IdInternoOperacion\":1,\"IdInternoPagoOperacion\":1,\"IdMoneda\":\"CLP\",\"IdOperacion\":150918,\"IdTipoCuentaBancaria\":\"\",\"MontoFormaDePago\":16288273,\"NombreCuentaBancaria\":\"\",\"RecibePagaPortafolio\":\"R\",\"NombreFormaDePago\":\"TRANSFERENCIA\",\"NumeroCuentaBancaria\":\"2012030\",\"IdMedioDePago\":\"TRANSFERENCIA\"}]}";
			CrearOperacionMesa_IN objIn = gson.fromJson(json1, CrearOperacionMesa_IN.class);

			CrearOperacionMesa demo = new CrearOperacionMesa();

			CrearOperacionMesa_OUT ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			conn.commit();
			//conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== END ==");

	};
}
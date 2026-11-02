// GENERATED DOUBLE of the legacy cl.bcs.mesa.SPT.CrearOperacionSpot by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;



import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

import java.util.*;

public class CrearOperacionSpot implements JavaCall{ 
			
	public static class OperacionObjIn{
		public String NumeroOperacion;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public Long IdCliente; 
		public Long IdPortafolio;
		public java.sql.Timestamp FechaOperacion;
		public String HoraOperacion;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		
		public BigDecimal TipoCambioCierreAntiguo; //Para log
		public BigDecimal TipoCambioCostoAntiguo; //Para log
		
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCierreForward;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCierreForward;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal MontoEquivalente;
		public BigDecimal MontoRentabilidad;
		public BigDecimal MontoRentabilidadCanal;
		public BigDecimal Margen;
		public BigDecimal Utilidad;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String SolicitudTipoCambio;
		public Long IdMesa;
		public String IdModulo;
		public Long IdTipoDolar;
		public String TipoComprobanteFacturacion;
		public Long IdAgente;
		public String BenOur;
		public BigDecimal EquivalenteConGasto;
		public BigDecimal TipoCambioCierreConGasto;
		public String Observacion;
		public Long CodigoComercio;
		public Long FolioPlanilla;
		public String FolioInterfaz;
		public String OperacionFicticia;
		public String IdTipoCanalIngreso;
		public String IdDatatec;
		public String Estado;
		public Long IdSolicitudAporteRescate=0L;
		public String TipoSolicitud= "";
		public String TipoDocumento = "";
		public long	IdContacto = 0L;
		public String IdSerieFondo;
		public String ValidaFlujoCaja = "S";
		public String AfectoADerivado = "N";
		public String DolarObservado = "N";
		public String ProductoAgrupador ="";
		public String CodigoPortafolio ="";
		public String Confirmar = ""; //WS
		public String IdPlataformaNegociacion = "OTC";
		public String CodigoEjecutivoDigitador = "";
		public Long IdEmpleadoDigitador = 0L;
		public String FacturacionAutomatica;
		public String EstadoCaja;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		public OperacionObjIn() {
			this.ProductoAgrupador = "";
			this.CodigoPortafolio = "";
			this.Confirmar = "";
			this.FacturacionAutomatica = "N";
			this.EstadoCaja = "NRE";
		}
	}
	
	public static class FormaPagoObjIn{
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public String IdTipoMovimiento;
		public String IdFormaDePago;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String AfectarTesoreria;
		public Date FechaLiquidacion;
		public Long IdCuentaBancaria = 0L;		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearOperacionSpotEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CalculaMargen;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<OperacionObjIn> p_DatosOperacion;
		public List<FormaPagoObjIn> p_DatosFormaPago;
		public String p_PermiteOperarProspectos;
		public String p_EsCargaMasiva;
		public String p_EsIngresoWS;
		
		public CrearOperacionSpotEntrada() {
			this.p_DatosOperacion = new ArrayList<OperacionObjIn>();
			this.p_DatosFormaPago = new ArrayList<FormaPagoObjIn>();
			this.p_SimularOperacion = "N"; //Se deja N como por defecto dado que antes no tenia este parametro y para no modificar las otras piezas.
			this.p_PermiteOperarProspectos = "N";
			this.p_EsCargaMasiva = "N";
			this.p_EsIngresoWS = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionObjOut {
		public String IdInterno;
		public Long IdOperacionSpot;
		public String IdDatatec;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearOperacionSpotSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionObjOut>	P_OUT_IDOPERACIONSPOT;
		
		public CrearOperacionSpotSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpot{
		public Long IdInterno;
		public String NumeroOperacion;
		public Long IdOperacionSpot;
		public Long IdEmpresa;
		public Long IdCliente; 
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public java.sql.Timestamp FechaOperacion;
		public Date FechaLiquidacion;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public String IdMonedaLocal;
		public String IdMonedaEquivalente;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaPrincipalEquivalente;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal TipoCambioCierreForward;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCierreForward;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal MontoEquivalente;
		public BigDecimal MontoRentabilidad;
		public BigDecimal MontoRentabilidadCanal;
		public BigDecimal ResultadoMonedaLocalPuntas;
		public BigDecimal ResultadoMonedaLocalBidAsk;
		public BigDecimal ResultadoMonedaLocalDO;
		public BigDecimal ResultadoMonedaLocalTCSpot;
		public BigDecimal ResultadoMonedaLocalTCCierreMesa = BigDecimal.ZERO; //IMPCREDOPT-2549 campo ResultadoMonedaLocalTCCierreMesa se debe actualizar una vez cerrada la mesa
		public BigDecimal ResultadoMonedaLocalConsolidado;
		public BigDecimal Margen;
		public BigDecimal MargenBidAsk;
		public BigDecimal Utilidad;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal PuntaBid;
		public BigDecimal PuntaAsk;
		public BigDecimal ParidadBid;
		public BigDecimal ParidadAsk;
		public BigDecimal ValorDolarObservado;
		public BigDecimal ValorTCSpot;
		public String SolicitudTipoCambio;
		public Long IdEvento;
		public Long IdMesa;
		public String IdModulo;
		public Long IdTipoDolar;
		public String TipoComprobanteFacturacion;
		public Long IdComprobanteFacturacion;
		public Long IdComprobanteTesoreriaIngreso;
		public Long IdComprobanteTesoreriaEgreso;
		public Long IdAgente;
		public String BenOur;
		public BigDecimal EquivalenteConGasto;
		public BigDecimal TipoCambioCierreConGasto;
		public String Observacion;
		public Long CodigoComercio;
		public Long FolioPlanilla;
		public String FolioInterfaz;
		public String OperacionFicticia;
		public String IdTipoCanalIngreso;
		public String IdDatatec;
		public String Estado;
		public Long IdSolicitudAporteRescate = 0L;
		public Long IdUsuario;
		public Long IdUsuarioModifica;
		public java.sql.Timestamp FechaModifica;
		public java.sql.Timestamp FechaReproceso;
		public java.sql.Timestamp FechaIngresoRegistro;
		public String EstadoConfirmacion;
		public Long IdOperacionSpotReferencia = 0L;
		public Long FolioOrigenFW = 0L;
		public Long IdOperacionSpotOrigen;
		public Long IdOperacionSpotVigente;
		
		public String MultiplicaODivide;
		public BigDecimal ParidadMercado;
		public BigDecimal CierreIngresadoPosMonPri;
		public BigDecimal CierreIngresadoPosMonEqui;
		public String Nemotecnico;
		public BigDecimal TipoCambioUSD;
		public BigDecimal TipoCambioMonedaSecundaria;
		
		public String NombreCompleto = "";
		public String DocumentoIdentidad = "";
		
		public BigDecimal MontoComision = BigDecimal.ZERO;
		public String IdTipoMovimientoFondoMutuo = "";
		public String IdEmisorOrigen = "";
		public String FechaCertificacion = "0001-01-01";
		public Long   IdInstrumentoFFMM = 0L;
		public String NemotecnicoFFMM = "";
		public String IdEmisorFFMM = "";
		
		public String TipoDocumento;
		public long	IdContacto;

		public String AfectoADerivado = "N";
		public String DolarObservado = "N";
		
		public String IdTipoSpot = "";
		public String CodigoOperador = "";
		public String Confirmar ="";
		public String IdPlataformaNegociacion = "OTC";
		public String IdTipoInstrumento ="";
		public Long IdEmpleadoDigitador = 0L;
		
		public String FacturacionAutomatica = "N";
		public String EstadoCaja = "NRE";
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpotFormaDePago{
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public Long IdAbono;
		public Long IdCargo;
		public Long IdIngresoEgresoAnticipo;
		public Long IdAbonoAnticipo;
		public Long IdCargoAnticipo;
		public String IdTipoProducto;
		public String IdTipoMovimiento;
		public String RecibePagaPortafolio;
		public String IdFormaDePago;
		public String IdModalidadPago;
		public String IdMedioDePago;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public String FechaIngresoComprobantePagoOperacion;
		public String FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public String AfectarTesoreria;
		public Long IdCuentaBancaria;
		public String IdTipoOperacion = "";
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntasCalcularMargen{
		public String NumeroOperacion;
		public Long IdEmpresa;
		public String IdTipoOperacion;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String MonedaLocal;
		public String CheckPrecioPorTramo;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
		public BigDecimal ParidadCosto;
		public BigDecimal ParidadMercado;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal ValorCompra;
		public BigDecimal ValorVenta;
		public BigDecimal Margen;
		

		public BigDecimal TipoCambioCosto;
		
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}

	public static class Puntas{
		public String NumeroOperacion;
		public String IdTipoOperacion;
		public Long IdInstrumento;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal TipoCambioCierre;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String IdTipoCanalIngreso;
		public BigDecimal ValorCompra;
		public BigDecimal ValorVenta;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class RequisitosCrearOperacionSpot{
		public String NumeroOperacion;
		public Timestamp FechaOperacion;
		public String IdMonedaPrincipal;
		public Long IdMesa;
		public String IdEstadoMesa;
		public String IdEstadoModulo;
		public String TipoEstadoModulo;
		public String PautaEnEjecucion;
		public Timestamp UltimoProcesoCambioFecha;
		public String EstadoMesaSpot;
		public String EstadoFondo;
		public String EstadoProcesoCambioFecha;
		public String IdTipoComprobante;
		public String IdEstadoFacturacion;
		public BigDecimal TipoCambioUSD;
		public BigDecimal TipoCambioPrincipal;
		public BigDecimal ParidadPrincipal;
		public Long IdCliente;
		public String IdEstadoCliente;
		public String ClienteEsEnrolado;
		public Long IdPortafolio;
		public String IdEstadoPortafolio;
		public String IdEstadoAutorizacion;
		public Long IdSolicitudAporteRescate;
		public String CodigoPortafolio;
		public String DocumentoIdentidad;
		public String IdEstadoCuentaInversionMonedaPrincipal;
		public String IdEstadoCuentaInversionMonedaSecundaria;
		public String ResultadoPerfilProducto;
		public String MensajePerfilProducto;
		public String PlantillaEmpleadoProductoHabilitado;
		public String Nemotecnico;
		public String EsClienteNuevoProspecto = "N";
		public Long IdEmpleadoDigitador;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class LimitesParidadYTipoCambio{
		public String NumeroOperacion;
		public String FechaOperacion;//
		public String IdTipoOperacion;//
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
		public BigDecimal TipoCambioCosto;
		public BigDecimal ParidadCosto;
		public BigDecimal ParidadMercado;
		public BigDecimal ParidadBID;
		public BigDecimal ParidadASK;
		public BigDecimal LimiteSuperiorParidad;
		public BigDecimal LimiteInferiorParidad;
		public BigDecimal LimiteSuperiorPAR;
		public BigDecimal LimiteInferiorPAR;
		public BigDecimal TipoCambioMercado;
		public BigDecimal LimiteSuperiorTipoCambio;
		public BigDecimal LimiteInferiorTipoCambio;
		public BigDecimal LimiteSuperiorTC;
		public BigDecimal LimiteInferiorTC;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafoliosFlujoCaja{
		public String NumeroOperacion;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormaDePagoMesa {
		public String IdFormaDePago;
	    public String NombreFormaDePago;
	    public Long	Dias;
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
	    public String IdCondicionDeLiquidacion;
	    public String CodigoCondicionDeLiquidacion;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ModalidadDePago {
		public Long IdMedioDePagoYModalidadDePago;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdEstado;
	}
	
	public static class OperacionSpotEventoAutorizacion{
		public String NumeroOperacion;
		public String IdTipoEvento;
		public String Glosa;
	}
	public static class PreferenciasPagoSpot{
		public String  IdPreferenciaPago;
		public String IdMedioDePago;
	}
	
	public static class PlataformaNegociacion extends BcsPojo{
		public String Codigo;
	}
	

	public static class Empleado extends BcsPojo{
		public Long IdEmpleadoDigitador;
		public String CodigoEmpleado;
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearOperacionSpot", "call", new Object[]{con, jin}, JsonObject.class); }
	
	public boolean validarIngreso1(String estadoFondo,String estadoMesa,String estadoPauta) { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.validarIngreso1: not part of the double"); }
	
	//considerando feriados de USA
	public String calculaFechaLiquidacion(Connection conn, String fechaEntrada, Long numeroDias, Long pais , Long idPaisSistema) throws Exception { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.calculaFechaLiquidacion: not part of the double"); }
	
	public static BigDecimal convertirCentavosAPesosChilenos(BigDecimal centavos) { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.convertirCentavosAPesosChilenos: not part of the double"); }


	public CrearOperacionSpotSalida logica(Connection conn, CrearOperacionSpotEntrada in) throws Exception { return cl.bcs.oracle.Doubles.answer("CrearOperacionSpot", "logica", new Object[]{conn, in}, CrearOperacionSpotSalida.class); }
	
	public Timestamp horasConvertidasTimestamp(Fecha fecha,Timestamp time,String hour) { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.horasConvertidasTimestamp: not part of the double"); }
	
	public boolean validarHora(String hora) { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.validarHora: not part of the double"); }
	 
	public String corregirHora(String hora) { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.corregirHora: not part of the double"); }
	
	public ArrayList<OperacionSpot> ModificarHora(List<OperacionSpot> operacionesSpot, CrearOperacionSpotEntrada in, Timestamp fechaHoraSistemaTimestamp) { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.ModificarHora: not part of the double"); }
	
	public static void main(String[] args) throws Exception { throw new UnsupportedOperationException("oracle double CrearOperacionSpot.main: not part of the double"); }
}
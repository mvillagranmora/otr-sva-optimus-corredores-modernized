package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.custodia.prt.AnticiparEmprestito.AnticiparEmprestito_Entrada;
import cl.bcs.custodia.prt.AnticiparEmprestito.AnticiparEmprestito_Entrada_DatosEmprestito;
import cl.bcs.custodia.prt.AnticiparEmprestito.AnticiparEmprestito_Salida;
import cl.bcs.custodia.prt.ConsultarResumenPrestamoVigente.ConsultarResumenPrestamoVigente_Entrada;
import cl.bcs.custodia.prt.ConsultarResumenPrestamoVigente.ConsultarResumenPrestamoVigente_Salida;
import cl.bcs.custodia.prt.ConsultarResumenPrestamoVigente.PrestamoVigente;
import cl.bcs.custodia.prt.CrearPrestamo.CrearPrestamo_Entrada;
import cl.bcs.custodia.prt.CrearPrestamo.CrearPrestamo_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AnticipoEmprestitoSinCustodia  implements JavaCall {
	
	private final String SQL_SELECT_PRESTAMOS_ORIGEN 				= loadResourceToString("cl/bcs/custodia/sql/AnticiparEmprestitoSinCustodia_SelectPrestamo.sql");	
	private final String SQL_UPDATE_PRESTAMOS_ORIGEN			    = loadResourceToString("cl/bcs/custodia/sql/AnticiparEmprestitoSinCustodia_UpdatePrestamoOriginal.sql");
	static final Logger logger								= LogManager.getLogger(AnticipoEmprestitoSinCustodia.class);
	
	public static class AnticipoEmprestitoSinCustodiaEntrada extends BcsPojo {
		public Long    	p_IdEmpresa;
		public Long     p_IdUsuario;
		public String   p_IdModulo;	
		public Long		p_FolioPrestamosOriginal;	
		public String   p_Accion; // CREAR /ANTICIPAR
		public List <AnticiparEmprestito_Entrada_DatosEmprestito> p_Empresito;
		
	} 
	
	public static class AnticipoEmprestitoSinCustodiaSalida extends BcsPojo {	
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;		
		public PrestamoSalida           P_OUT_PRESTAMO;
		//public Long                     P_OUT_ID_PRESTAMO_ORIGINAL;
		
		public AnticipoEmprestitoSinCustodiaSalida() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES	 		=  new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Emprestito extends BcsPojo {	
		
		public Long FolioEmprestito;
		public String FechaEmprestito;
		public BigDecimal PrecioInstrumento;
		public Long Cantidad;
		public BigDecimal MontoInstrumento;
		public BigDecimal TasaPremio; 		
		public BigDecimal MontoPremio; 		
		public BigDecimal MontoAPlazo; 		
		public String FechaAnticipo; 		
		public Long PlazoAlVencimiento;		
		public Long IdPais; 				
		public String IdMonedaLiquidacion; 
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion; 		
		public String DevolverCustodia= "S";
		public Long IdEmpleadoDigitador;
		public Long FolioPrestamo;	
		
	}
	
	public static class PrestamoSalida extends PrestamoVigente{		
		public String FechaPrestamoVencimiento;
		public String FechaLiquidacionPrestamo;
		public String FechaPrestamoNuevo;
		
	}
	
	public static class FolioPrestamo extends BcsPojo{		
		public Long FolioPrestamo;
		public BigDecimal Cantidad;
		public BigDecimal MontoPrestamo;
	}
	
	public AnticipoEmprestitoSinCustodiaSalida logica(Connection con, AnticipoEmprestitoSinCustodiaEntrada entrada) throws Exception {
		logger.info("INICIO::AnticipoEmprestitoSinCustodia");
		long inicioServicio = System.currentTimeMillis();		
		
		AnticipoEmprestitoSinCustodiaSalida salida = new AnticipoEmprestitoSinCustodiaSalida();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa,"PLATAFORMA", "PLA.PaisSistema");
		
		
		if("CREAR".equals(entrada.p_Accion)) {
			
			CrearPrestamo prestamoNuevo = new CrearPrestamo();
			CrearPrestamo_Entrada EntradaPrestamoNuevo = new CrearPrestamo_Entrada();
			CrearPrestamo_Salida SalidaPrestamoNuevo = new CrearPrestamo_Salida();

			List<CrearPrestamo_Entrada> ListaPrestamosOrigen = this.BuscarPrestamoOrigen(con,entrada);			
			
			
			if(!ListaPrestamosOrigen.isEmpty()) {
				EntradaPrestamoNuevo = ListaPrestamosOrigen.get(0);
								
				EntradaPrestamoNuevo.p_Cantidad = entrada.p_Empresito.get(0).Cantidad;
				EntradaPrestamoNuevo.p_FechaLiquidacion = fechaSistema;
				EntradaPrestamoNuevo.p_FechaMovimiento= fechaSistema;				
				
				SalidaPrestamoNuevo = prestamoNuevo.logica(con, EntradaPrestamoNuevo);
				salida.P_OUT_ERRORESCONTADOR= SalidaPrestamoNuevo.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_MENSAJES = SalidaPrestamoNuevo.P_OUT_MENSAJES;
				
				if(!SalidaPrestamoNuevo.P_OUT_FOLIOCARTERA.equals(null)) {
					
					ConsultarResumenPrestamoVigente_Entrada entradaConsultaPrestamoNuevo = new ConsultarResumenPrestamoVigente_Entrada ();
					ConsultarResumenPrestamoVigente_Salida salidaConsultaPrestamoNuevo = new ConsultarResumenPrestamoVigente_Salida ();
					ConsultarResumenPrestamoVigente consultarPrestamo = new ConsultarResumenPrestamoVigente();
					
					entradaConsultaPrestamoNuevo.p_IdEmpresa =entrada.p_IdEmpresa;
					entradaConsultaPrestamoNuevo.p_FechaConsulta = EntradaPrestamoNuevo.p_FechaMovimiento;
					entradaConsultaPrestamoNuevo.p_ConsultaPor = "OPERACION";
					entradaConsultaPrestamoNuevo.p_FolioCartera = SalidaPrestamoNuevo.P_OUT_FOLIOCARTERA;
					entradaConsultaPrestamoNuevo.p_IdClasificacionTipoInstrumento = EntradaPrestamoNuevo.p_IdMercado;
					entradaConsultaPrestamoNuevo.p_IdCliente = EntradaPrestamoNuevo.p_IdClienteSolicita;
					
					salidaConsultaPrestamoNuevo = consultarPrestamo.logica(con, entradaConsultaPrestamoNuevo);
					
					salida.P_OUT_PRESTAMO = this.copyPrestamoSalida(salidaConsultaPrestamoNuevo.P_OUT_PRESTAMOS_VIGENTES.get(0));				
					
				}
								
				return salida;
				
			}
			
		}else if ("ANTICIPAR".equals(entrada.p_Accion)) {
			
			AnticiparEmprestito_Entrada entradaAnticipo = new AnticiparEmprestito_Entrada();
			AnticiparEmprestito_Salida salidaAnticipo= new AnticiparEmprestito_Salida();
			AnticiparEmprestito anticipoEmprestitos = new AnticiparEmprestito();
			
			entradaAnticipo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnticipo.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnticipo.p_IdModulo = entrada.p_IdModulo;
			entradaAnticipo.p_UsaReglasNegocio = "S";
			entradaAnticipo.p_IdPais = idPaisSistema;
			entradaAnticipo.p_FechaAnticipo = fechaSistema;	
			entradaAnticipo.p_RebajaPrestamo = "S";
			entradaAnticipo.p_AnticiparPrestamoAsociado = "N";
			entradaAnticipo.p_DatosEmprestitos = entrada.p_Empresito;					
			
			salidaAnticipo = anticipoEmprestitos.logica(con, entradaAnticipo);
			
			salida.P_OUT_ERRORESCONTADOR = salidaAnticipo.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES = salidaAnticipo.P_OUT_MENSAJES;
			
			//update prestamos original
			if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L)==0) {
				
				List<CrearPrestamo_Entrada> ListaPrestamosOrigen = this.BuscarPrestamoOrigen(con,entrada);					
				
				this.updatePretamosOriginal(con,entrada,salida,ListaPrestamosOrigen);
				
			}						
			return salida;		
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.info("FIN::AnticipoEmprestitoSinCustodia :" , finMetodo- inicioServicio);	
		
		return salida;
	}


	private void updatePretamosOriginal(Connection con, AnticipoEmprestitoSinCustodiaEntrada entrada,AnticipoEmprestitoSinCustodiaSalida salida ,List<CrearPrestamo_Entrada> ListaPrestamosOrigen) {
		logger.debug("INICIO::AnticipoEmprestitoSinCustodia.updatePretamosOriginal");
		long inicioMetodo = System.currentTimeMillis();
		SQL sql = new SQL();
		
		BigDecimal cantidadPrestamo = ListaPrestamosOrigen.get(0).p_Cantidad.subtract(entrada.p_Empresito.get(0).Cantidad);
		BigDecimal montoPrestamo = cantidadPrestamo.multiply(ListaPrestamosOrigen.get(0).p_Precio, MathContext.DECIMAL128);
		
		List<FolioPrestamo> listaPrestamosActulizar = new ArrayList<FolioPrestamo>(); 	
		
		FolioPrestamo actualizarPrestamo = new FolioPrestamo();
		actualizarPrestamo.FolioPrestamo = entrada.p_FolioPrestamosOriginal;
		actualizarPrestamo.Cantidad = cantidadPrestamo;
		actualizarPrestamo.MontoPrestamo = montoPrestamo;	
		
		listaPrestamosActulizar.add(actualizarPrestamo);
		
		try {
			sql.batch(con, SQL_UPDATE_PRESTAMOS_ORIGEN, listaPrestamosActulizar, FolioPrestamo.class);
		} catch (SQLException e) {
			MensajeSalida mensajeSalida = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "NO_SE_PUDO_ACTUALIZAR_PRESTAMO","No se pudo actualizar prestamo original");					
			salida.P_OUT_MENSAJES.add(mensajeSalida);
		}
				
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnticipoEmprestitoSinCustodia.updatePretamosOriginal: " , finMetodo- inicioMetodo);
		
	}


	private List<CrearPrestamo_Entrada> BuscarPrestamoOrigen(Connection con,AnticipoEmprestitoSinCustodiaEntrada entrada) throws SQLException {
		logger.debug("INICIO::AnticipoEmprestitoSinCustodia.BuscarPrestamoOrigen");
		long inicioMetodo = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("FolioPrestamo", entrada.p_FolioPrestamosOriginal);
					
		List<CrearPrestamo_Entrada> ListaPrestamosOrigen = sql.query(con, SQL_SELECT_PRESTAMOS_ORIGEN, parametros, CrearPrestamo_Entrada.class);
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::AnticipoEmprestitoSinCustodia.BuscarPrestamoOrigen: " , finMetodo- inicioMetodo);
		return ListaPrestamosOrigen;
	}


	private PrestamoSalida copyPrestamoSalida(PrestamoVigente prestamoNuevo) {
		logger.trace("INICIO::AnticipoEmprestitoSinCustodia.copyPrestamoSalida");
		long inicioMetodo = System.currentTimeMillis();		
		
		PrestamoSalida presSalida  = new PrestamoSalida();
		
		presSalida.IdInstrumentoSubyacente= prestamoNuevo.IdInstrumentoSubyacente;
		presSalida.FolioSubyacente= prestamoNuevo.FolioSubyacente;
		presSalida.IdEmisorSubyacente= prestamoNuevo.IdEmisorSubyacente;
		presSalida.NemoSubyacente= prestamoNuevo.NemoSubyacente;
		presSalida.NemoSVSSubyacente= prestamoNuevo.NemoSVSSubyacente;
		presSalida.IdTipoInstrumentoSubyacente= prestamoNuevo.IdTipoInstrumentoSubyacente;
		presSalida.NombreTipoInstrumentoSubyacente= prestamoNuevo.NombreTipoInstrumentoSubyacente;
		presSalida.IdClasificacionTipoInstrumentoSubyacente= prestamoNuevo.IdClasificacionTipoInstrumentoSubyacente;
		presSalida.CantidadAsignada= prestamoNuevo.CantidadAsignada;
		presSalida.CantidadPorAsignar= prestamoNuevo.CantidadPorAsignar;
		presSalida.IdMonedaSubyacente= prestamoNuevo.IdMonedaSubyacente;
		presSalida.IdTipoReajusteSubyacente= prestamoNuevo.IdTipoReajusteSubyacente;
		presSalida.IdSubyacente= prestamoNuevo.IdSubyacente;
		presSalida.IdMonedaEmisionSubyacente= prestamoNuevo.IdMonedaEmisionSubyacente;
		presSalida.NombreMercado= prestamoNuevo.NombreMercado;
		presSalida.NombreEmisorSubyacente= prestamoNuevo.NombreEmisorSubyacente;
		presSalida.IdBovedaSubyacente= prestamoNuevo.IdBovedaSubyacente;
		presSalida.IdCartera= prestamoNuevo.IdCartera;
		presSalida.IdEmpresa= prestamoNuevo.IdEmpresa;
		presSalida.FolioPrestamo= prestamoNuevo.FolioPrestamo;
		presSalida.FolioCarteraRelacionada= prestamoNuevo.FolioCarteraRelacionada;
		presSalida.IdEstado= prestamoNuevo.IdEstado;
		presSalida.NombreEstado= prestamoNuevo.NombreEstado;
		presSalida.IdMonedaSistema= prestamoNuevo.IdMonedaSistema;
		presSalida.IdMonedaInstrumento= prestamoNuevo.IdMonedaInstrumento;
		presSalida.IdOperacion= prestamoNuevo.IdOperacion;
		presSalida.IdTipoOperacion= prestamoNuevo.IdTipoOperacion;
		presSalida.NombreTipoOperacion= prestamoNuevo.NombreTipoOperacion;
		presSalida.IdBoveda= prestamoNuevo.IdBoveda;
		presSalida.IdTipoReajuste= prestamoNuevo.IdTipoReajuste;
		presSalida.IdTipoProducto= prestamoNuevo.IdTipoProducto;
		presSalida.IdSucursal= prestamoNuevo.IdSucursal;
		presSalida.IdAgente= prestamoNuevo.IdAgente;
		presSalida.IdBolsa= prestamoNuevo.IdBolsa;
		presSalida.NombreBolsa= prestamoNuevo.NombreBolsa;
		presSalida.IdConcepto= prestamoNuevo.IdConcepto;
		presSalida.NombreConcepto= prestamoNuevo.NombreConcepto;
		presSalida.IdCondicionLiquidacion= prestamoNuevo.IdCondicionLiquidacion;
		presSalida.NombreCondicionLiquidacion= prestamoNuevo.NombreCondicionLiquidacion;
		presSalida.IdModulo= prestamoNuevo.IdModulo;
		presSalida.IdCliente= prestamoNuevo.IdCliente;
		presSalida.DocumentoIdentidad= prestamoNuevo.DocumentoIdentidad;
		presSalida.IdPortafolio= prestamoNuevo.IdPortafolio;
		presSalida.CodigoPortafolio= prestamoNuevo.CodigoPortafolio;
		presSalida.DescripcionPortafolio= prestamoNuevo.DescripcionPortafolio;
		presSalida.RazonSocial= prestamoNuevo.RazonSocial;
		presSalida.NemotecnicoPrestamo= prestamoNuevo.NemotecnicoPrestamo;
		presSalida.NemoSVS= prestamoNuevo.NemoSVS;
		presSalida.IdInstrumentoPrestamo= prestamoNuevo.IdInstrumentoPrestamo;
		presSalida.IdClasificacionTipoInstrumento= prestamoNuevo.IdClasificacionTipoInstrumento;
		presSalida.IdTipoInstrumento= prestamoNuevo.IdTipoInstrumento;
		presSalida.TipoInstrumento= prestamoNuevo.TipoInstrumento;
		presSalida.IdEmisor= prestamoNuevo.IdEmisor;
		presSalida.FechaPrestamo= prestamoNuevo.FechaPrestamo;
		presSalida.IdTipoComprobante= prestamoNuevo.IdTipoComprobante;
		presSalida.FolioComprobante= prestamoNuevo.FolioComprobante;
		presSalida.FechaComprobante= prestamoNuevo.FechaComprobante;
		presSalida.Cantidad= prestamoNuevo.Cantidad;
		presSalida.CorteMinimo= prestamoNuevo.CorteMinimo;
		presSalida.PrecioInstrumento= prestamoNuevo.PrecioInstrumento;
		presSalida.MontoInstrumento= prestamoNuevo.MontoInstrumento;
		presSalida.TasaPrestamo= prestamoNuevo.TasaPrestamo;
		presSalida.ValorRescate= prestamoNuevo.ValorRescate;
		presSalida.FechaVencimientoPrestamo= prestamoNuevo.FechaVencimientoPrestamo;
		presSalida.FechaEmisionInstrumento= prestamoNuevo.FechaEmisionInstrumento;
		presSalida.FechaVencimientoInstrumento= prestamoNuevo.FechaVencimientoInstrumento;
		presSalida.PlazoEnDiasOperacion= prestamoNuevo.PlazoEnDiasOperacion;
		presSalida.EsFungible= prestamoNuevo.EsFungible;
		presSalida.EsFungibleSubyacente= prestamoNuevo.EsFungibleSubyacente;
		presSalida.CodigoIFRS= prestamoNuevo.CodigoIFRS;
		presSalida.FechaIngresoRegistro= prestamoNuevo.FechaIngresoRegistro;
		presSalida.FechaModificacionRegistro= prestamoNuevo.FechaModificacionRegistro;
		presSalida.TasaAgente= prestamoNuevo.TasaAgente;
		presSalida.FechaLiquidacion= prestamoNuevo.FechaLiquidacion;
		presSalida.IdTipoPrestamo= prestamoNuevo.IdTipoPrestamo;
		presSalida.NombreTipoPrestamo= prestamoNuevo.NombreTipoPrestamo;
		presSalida.MinimoDiasAnticipo= prestamoNuevo.MinimoDiasAnticipo;
		presSalida.MaximoDiasPrestamo= prestamoNuevo.MaximoDiasPrestamo;
		presSalida.EsDiaHabilPlazoPrestamo= prestamoNuevo.EsDiaHabilPlazoPrestamo;
		presSalida.EsDiaHabilParaCalculoInteres= prestamoNuevo.EsDiaHabilParaCalculoInteres;
		presSalida.UsaGarantiaEspecifica= prestamoNuevo.UsaGarantiaEspecifica;
		presSalida.FolioEntidad= prestamoNuevo.FolioEntidad;
		presSalida.CobraPrimaAlInicio= prestamoNuevo.CobraPrimaAlInicio;
		presSalida.IdTipoCanalIngreso= prestamoNuevo.IdTipoCanalIngreso;
		presSalida.INGGlosa= prestamoNuevo.INGGlosa;
		presSalida.INGFechaOperacion= prestamoNuevo.INGFechaOperacion;
		presSalida.INGValorRescate= prestamoNuevo.INGValorRescate;
		presSalida.INGTasaPrestamo= prestamoNuevo.INGTasaPrestamo;
		presSalida.MontoPrimaOriginal= prestamoNuevo.MontoPrimaOriginal;
		presSalida.INGMontoTotalPrestamo= prestamoNuevo.INGMontoTotalPrestamo;
		presSalida.INGPrecioInstrumento= prestamoNuevo.INGPrecioInstrumento;
		presSalida.INGTasaInstrumento= prestamoNuevo.INGTasaInstrumento;
		presSalida.INGFechaLiquidacionPrestamo= prestamoNuevo.INGFechaLiquidacionPrestamo;
		presSalida.INGMontoInstrumento= prestamoNuevo.INGMontoInstrumento;
		presSalida.INGFechaPrecioTasaMercadoActual= prestamoNuevo.INGFechaPrecioTasaMercadoActual;
		presSalida.INGPrecioMercadoInstrumento= prestamoNuevo.INGPrecioMercadoInstrumento;
		presSalida.INGTasaMercadoActual= prestamoNuevo.INGTasaMercadoActual;
		presSalida.INGMontoMercadoInstrumento= prestamoNuevo.INGMontoMercadoInstrumento;
		presSalida.EGRCantidadPrestamo= prestamoNuevo.EGRCantidadPrestamo;
		presSalida.EGRFechaLiquidacionPrestamo= prestamoNuevo.EGRFechaLiquidacionPrestamo;
		presSalida.EGRFechaOperacion= prestamoNuevo.EGRFechaOperacion;
		presSalida.INGFechaValorizacionAnterior= prestamoNuevo.INGFechaValorizacionAnterior;
		presSalida.INGFechaValorizacionActual= prestamoNuevo.INGFechaValorizacionActual;
		presSalida.ValorTipoCambioCierre= prestamoNuevo.ValorTipoCambioCierre;
		presSalida.INGPrecioMercadoAnterior= prestamoNuevo.INGPrecioMercadoAnterior;
		presSalida.INGMontoPrecioPromedioCompra= prestamoNuevo.INGMontoPrecioPromedioCompra;
		presSalida.INGMontoATasaMercadoAnterior= prestamoNuevo.INGMontoATasaMercadoAnterior;
		presSalida.INGMontoATasaMercadoProximo= prestamoNuevo.INGMontoATasaMercadoProximo;
		presSalida.INGMontoATasaCompraAnterior= prestamoNuevo.INGMontoATasaCompraAnterior;
		presSalida.INGMontoInteresCompraProximo= prestamoNuevo.INGMontoInteresCompraProximo;
		presSalida.INGMontoInteresCorridoCompraActual= prestamoNuevo.INGMontoInteresCorridoCompraActual;
		presSalida.TipoValorizacion= prestamoNuevo.TipoValorizacion;
		presSalida.IdInstrumento= prestamoNuevo.IdInstrumento;
		presSalida.FolioSistemaExterno= prestamoNuevo.FolioSistemaExterno;
		presSalida.EstadoAnticipo= prestamoNuevo.EstadoAnticipo;
		presSalida.ExigirGarantia= prestamoNuevo.ExigirGarantia;
		presSalida.EstadoLiquidacion= prestamoNuevo.EstadoLiquidacion;
		presSalida.TipoCustodia= prestamoNuevo.TipoCustodia;
		presSalida.CodigoCondicionLiquidacion= prestamoNuevo.CodigoCondicionLiquidacion;
		presSalida.MinimoDiasPrestamo= prestamoNuevo.MinimoDiasPrestamo;
		presSalida.INGCantidad= prestamoNuevo.INGCantidad;
		presSalida.INGMontoATasaCompraActual= prestamoNuevo.INGMontoATasaCompraActual;
		presSalida.INGMontoAPlazo= prestamoNuevo.INGMontoAPlazo;
		presSalida.INGMontoPrima= prestamoNuevo.INGMontoPrima;
		presSalida.INGMontoCompraActualMonedaInstrumento= prestamoNuevo.INGMontoCompraActualMonedaInstrumento;
		presSalida.INGMontoAPlazoMonedaInstrumento= prestamoNuevo.INGMontoAPlazoMonedaInstrumento;
		presSalida.INGMontoPremioMonedaInstrumento= prestamoNuevo.INGMontoPremioMonedaInstrumento;
		presSalida.PrecioCompraInicial= prestamoNuevo.PrecioCompraInicial;
		presSalida.MontoCompraInicial= prestamoNuevo.MontoCompraInicial;
		presSalida.InteresAcumulado= prestamoNuevo.InteresAcumulado;
		presSalida.InteresDiario= prestamoNuevo.InteresDiario;
		presSalida.MontoPrecioMedio= prestamoNuevo.MontoPrecioMedio;
		presSalida.PrecioMedioInicial= prestamoNuevo.PrecioMedioInicial;
		presSalida.FechaPrestamoVencimiento= prestamoNuevo.FechaVencimientoPrestamo.toString();		
		presSalida.FechaLiquidacionPrestamo= prestamoNuevo.FechaLiquidacion.toString();	
		presSalida.FechaPrestamoNuevo= prestamoNuevo.FechaPrestamo.toString();
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::AnticipoEmprestitoSinCustodia.copyPrestamoSalida: " , finMetodo- inicioMetodo);		
		
		return presSalida;
	}





	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		AnticipoEmprestitoSinCustodiaEntrada in = gson.fromJson(jin.toString(), AnticipoEmprestitoSinCustodiaEntrada.class);
		AnticipoEmprestitoSinCustodiaSalida out = logica(conn,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties"))
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String emprestitoJson = "{\r\n"
					+ "	\"p_IdEmpresa\": 1,\r\n"
					+ "	\"p_IdUsuario\": 97,\r\n"
					+ "	\"p_IdModulo\": \"CUS\",\r\n"
					+ "	\"p_FolioPrestamosOriginal\": 1063747,\r\n"
					+ "	\"p_Accion\": \"ANTICIPAR\",\r\n"
					+ "	\"p_Empresito\": [{\r\n"
					+ "		\"FolioEmprestito\": 1063756,\r\n"
					+ "		\"FechaEmprestito\": \"2025-08-21\",\r\n"
					+ "		\"PrecioInstrumento\": 4814,\r\n"
					+ "		\"Cantidad\": 2000,\r\n"
					+ "		\"MontoInstrumento\": 9628000,\r\n"
					+ "		\"TasaPremio\": 3.6,\r\n"
					+ "		\"MontoPremio\": 11553.60000000149,\r\n"
					+ "		\"MontoAPlazo\": 9639553.600000001,\r\n"
					+ "		\"FechaAnticipo\": \"2025-08-22\",\r\n"
					+ "		\"PlazoAlVencimiento\": 14,\r\n"
					+ "		\"IdPais\": 44,\r\n"
					+ "		\"IdMonedaLiquidacion\": \"CLP\",\r\n"
					+ "		\"IdCondicionLiquidacion\": \"T0\",\r\n"
					+ "		\"FechaLiquidacion\": \"2025-08-22\",\r\n"
					+ "		\"DevolverCustodia\": \"S\",\r\n"
					+ "		\"IdEmpleadoDigitador\": 108,\r\n"
					+ "		\"FolioPrestamo\": 1063747\r\n"
					+ "	}]\r\n"
					+ "}";
			
			AnticipoEmprestitoSinCustodiaEntrada anticipoSinCustodia = gson.fromJson(emprestitoJson, AnticipoEmprestitoSinCustodiaEntrada.class);
			
			AnticipoEmprestitoSinCustodia demo = new AnticipoEmprestitoSinCustodia();
						
 			try
 			{
 				AnticipoEmprestitoSinCustodiaSalida ObjOut = demo.logica(con, anticipoSinCustodia);
 				System.out.println("SALIDA:" + ObjOut);
 				//con.commit();
 				con.rollback();
 			}
 			catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}

package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero.ClearingFinancieroDetalle;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero.EntradaClearingFinanciero;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero.SalidaClearingFinanciero;
import cl.bcs.tesoreria.pojo.Camara;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CentralizaClearingFinanciero implements BcsCentralizacion {

	//private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaClearingFinanciero_SelectMovimientos.sql");
	//private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaClearingFinanciero_SelectEventos.sql");
	//private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaClearingFinanciero_SelectDatosMovimiento.sql");
	
	private final Log log = new Log("CON.CentralizaClearingFinanciero");
	
	public static class MovimientoPasoClearingFinanciero{
		public Long IdMovimiento;
		public String Folio;
		public String FechaDocumento;
		public String CompraVenta;
		public String ClaseInstrumento;
		public String Camara;
		public String CondicionLiquidacion;
		public String Bolsa;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String EsContraparte;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaClearingFinanciero");
		SalidaCentralizacion out = new SalidaCentralizacion();

		//Consultar Clearing financiero
		EntradaClearingFinanciero entradaClearingFinanciero = new EntradaClearingFinanciero();
		entradaClearingFinanciero.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaClearingFinanciero.p_IdUsuario = entrada.p_IdUsuario;
		entradaClearingFinanciero.p_IdModulo = entrada.p_IdModulo;
		entradaClearingFinanciero.p_UsaReglasNegocio = "S";
		entradaClearingFinanciero.p_FechaLiquidacion = entrada.p_FechaContable;
		entradaClearingFinanciero.p_Camaras = Stream.of("PM","RV","SM","PH","HS").map(c -> new Camara(c)).collect(Collectors.toList());
				
		ConsultarClearingFinanciero consultarClearingFinanciero = new ConsultarClearingFinanciero();
		SalidaClearingFinanciero salidaClearingFinanciero = consultarClearingFinanciero.logica(con, entradaClearingFinanciero);
		List<ClearingFinancieroDetalle> detalleClearing = salidaClearingFinanciero.P_OUT_CLEARINGFINANCIERODETALLE;
		
		//MOVIMIENTOS
		out.P_TABMOVIMIENTOS = detalleClearing.stream().filter(Objects::nonNull).filter(detalle -> detalle.IdComprobanteTesoreria>0).map(detalle ->{
			TabMovimientos mov = new TabMovimientos();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.IdTipoMovimiento = "CLEARING";
			mov.IdEmpresa = entrada.p_IdEmpresa;
			mov.Numero = Long.parseLong(Optional.ofNullable(detalle.FolioTransaccion).orElse("0"));
			mov.Linea = 0L;
			mov.Fecha = entrada.p_FechaContable;
			mov.DocumentoIdentidad = detalle.RutContraparte;
			mov.IdPortafolio = 0L;
			mov.Ejercicio = Long.valueOf(Optional.ofNullable(LocalDate.parse(entrada.p_FechaContable, Fecha.YYYYMMDD).format(Fecha.YYYY)).orElse("0"));
			mov.Periodo = LocalDate.parse(entrada.p_FechaContable, Fecha.YYYYMMDD).format(DateTimeFormatter.ofPattern("MM-yy"));
			mov.IdMoneda = detalle.IdMonedaTransaccion;
			mov.Monto = (detalle.TotalVenta.compareTo(BigDecimal.ZERO) > 0) ? detalle.TotalVenta : detalle.TotalCompra;
			mov.Estado = "";
			return mov;
		}).collect(Collectors.toList());
		
		//EVENTOS
		out.P_TABEVENTOS = new ArrayList<>();
		detalleClearing.stream().filter(Objects::nonNull).filter(detalle -> detalle.IdComprobanteTesoreria>0).forEach(detalle -> {
			TabEventos evt = new TabEventos();
			evt.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			evt.IdMovimiento = detalle.IdTransaccion;
			evt.IdEvento = "TOTAL_VENTA";
			evt.Nombre = "Total Venta";
			evt.MontoEvento = Optional.ofNullable(detalle.TotalVenta).orElse(BigDecimal.ZERO);
			evt.CodigoMoneda = detalle.IdMonedaTransaccion;
			out.P_TABEVENTOS.add(evt);
			
			evt = new TabEventos();
			evt.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			evt.IdMovimiento = detalle.IdTransaccion;
			evt.IdEvento = "TOTAL_COMPRA";
			evt.Nombre = "Total Compra";
			evt.MontoEvento = Optional.ofNullable(detalle.TotalCompra).orElse(BigDecimal.ZERO);
			evt.CodigoMoneda = detalle.IdMonedaTransaccion;
			out.P_TABEVENTOS.add(evt);
		});
		
		//MOVIMIENTO DATOS
		out.P_TABDATOSMOVIMIENTO = new ArrayList<>();
		detalleClearing.stream().filter(Objects::nonNull).filter(detalle -> detalle.IdComprobanteTesoreria>0).forEach(detalle -> {
			TabDatosMovimiento mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "Folio";
			mov.ValorVarChar = detalle.FolioTransaccion;
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "FechaDocumento";
			mov.ValorVarChar = Optional.ofNullable(entrada.p_FechaContable).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "CompraVenta";
			mov.ValorVarChar = Optional.ofNullable(detalle.CompraVenta).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);

			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "ClaseInstrumento";
			mov.ValorVarChar = Optional.ofNullable(detalle.IdClasificacionTipoInstrumento).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "Camara";
			mov.ValorVarChar = Optional.ofNullable(detalle.NombreGrupoCamara).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "CondicionLiquidacion";
			mov.ValorVarChar = Optional.ofNullable(detalle.IdCondicionDeLiquidacion).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "Bolsa";
			mov.ValorVarChar = Optional.ofNullable(detalle.IdBolsa).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "DocumentoIdentidad";
			mov.ValorVarChar = Optional.ofNullable(detalle.RutContraparte).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "NombreCliente";
			mov.ValorVarChar = Optional.ofNullable(detalle.RazonSocialContraparte).orElse("");
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
			
			mov = new TabDatosMovimiento();
			mov.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			mov.IdMovimiento = detalle.IdTransaccion;
			mov.TipoDato = "EsContraparte";
			mov.ValorVarChar = "SI";
			mov.ValorDecimal = BigDecimal.ZERO;
			out.P_TABDATOSMOVIMIENTO.add(mov);
		});		
		
		out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
		
		return out;
	}

}

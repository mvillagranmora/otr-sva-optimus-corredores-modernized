package cl.bcs.plataforma.tests.utils.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import cl.bcs.mesa.MRF.CrearOperacionMesa;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresarPactoBuilder {
	
	@Disabled
	@Test void ingresarPacto() throws Exception {
		try(Connection innerConn = PlataformaUtils.getQAConnection()){
			String fechaSistema = Utils.getFechaSistema(innerConn, Utils.getSchema(), 1);
			LocalDate ld = LocalDate.parse(fechaSistema, Fecha.YYYYMMDD);			
			CrearOperacionMesa_OUT res = new IngresarPactoBuilder()
					.setFechaOperacion(fechaSistema)
					.setFechaVencimiento(ld.plusDays(7).format(Fecha.YYYYMMDD))
					.setFechaLiquidacion(fechaSistema)
					.build(innerConn);
			
			innerConn.rollback();
			assertNotNull(res);
			assertEquals(res.P_OUT_DETALLEOPERACION.get(0).Monto.doubleValue(), entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).Monto.doubleValue(), 0.5);
		}
	}

	private EntradaCumpleRequisitos entradaCumpleRequisitos;
	private CrearOperacionMesa_IN entradaCrearOperacionMesa1;
	private CalcularParidadEntrada entradaCalcularParidad;
	private CrearOperacionMesa_IN entradaCrearOperacionMesa2;
	private CrearOperacionMesa_IN entradaCrearOperacionMesa3;
	
	public static class EntradaCumpleRequisitos{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Datos> P_DATOS;
		
		public static class Datos{
			public Long IdPortafolio;
			public String IdTipoOperacion;
			public String IdMoneda;
			public String IdCondicionDeLiquidacion;
			public String FechaOperacion;
			public String FechaLiquidacion;
			public BigDecimal MontoOperacion;
		}
	}
	
	public IngresarPactoBuilder() {
		Gson gson = Utils.getGsonBuilder();
		String in1 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"P_DATOS\":[{\"IdPortafolio\":100549,\"IdTipoOperacion\":\"COMPAC\",\"IdMoneda\":\"CLP\",\"IdCondicionDeLiquidacion\":\"T0\",\"FechaOperacion\":\"2018-08-28\",\"FechaLiquidacion\":\"2018-08-29\",\"MontoOperacion\":1000}]}";
		this.entradaCumpleRequisitos = gson.fromJson(in1, EntradaCumpleRequisitos.class);
		//obtener nuevamente los cuatro request con el isntrumento BCU0300118
		String in2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"N\",\"p_Operacion\":[{\"Accion\":\"CREAR\",\"IdInternoOperacion\":1,\"IdOperacion\":0,\"FolioOperacion\":0,\"IdCliente\":100024,\"IdPortafolio\":100549,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"Intermesa\":\"N\",\"IdMesaOrigen\":3,\"IdMesaDestino\":3,\"AfectaCustodia\":\"N\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdTipoReajuste\":\"CLP\",\"Plazo\":7,\"Monto\":1000,\"MontoMoneda\":1000,\"MontoLiquidacion\":0,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"Tasa\":1.2,\"ValorFinal\":1003,\"ValorFinalMoneda\":1003,\"TasaAgente\":0.25,\"FechaOperacion\":\"2018-08-28\",\"FechaVencimiento\":\"2018-08-29\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-08-29\",\"IdAgente\":20180,\"IdOperador\":25,\"IdEstado\":\"\",\"Glosa\":\"\",\"IdTipoCanalIngreso\":\"AGE\"}],\"p_DetalleOperacion\":[],\"p_FormasDePago\":[]}";
		this.entradaCrearOperacionMesa1 = gson.fromJson(in2, CrearOperacionMesa_IN.class);
		
		String in3 = "{\"p_IdEmpresa\":1,\"p_IdTrabajo\":1315,\"p_CalculosEntrada\":[{\"IdTrabajo\":1315,\"IdInterno\":1,\"Fecha\":\"2018-08-21\",\"IdMonedaOrigen\":\"CLP\",\"IdMonedaDestino\":\"CLP\",\"ValorCambioLocalModificado\":0,\"MontoOrigen\":\"13830404\",\"IdUso\":\"MESA\"}]}";
		this.entradaCalcularParidad=gson.fromJson(in3 ,CalcularParidadEntrada.class);
		
		String in4 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"N\",\"p_Operacion\":[{\"Accion\":\"MODIFICAR\",\"IdInternoOperacion\":1,\"IdOperacion\":11092,\"FolioOperacion\":0,\"IdCliente\":100024,\"IdPortafolio\":100549,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"Intermesa\":\"N\",\"IdMesaOrigen\":3,\"IdMesaDestino\":3,\"AfectaCustodia\":\"N\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdTipoReajuste\":\"CLP\",\"Plazo\":7,\"Monto\":1000,\"MontoMoneda\":1000,\"MontoLiquidacion\":13830404,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"Tasa\":1.2,\"ValorFinal\":1003,\"ValorFinalMoneda\":1003,\"TasaAgente\":0.25,\"FechaOperacion\":\"2018-08-28\",\"FechaVencimiento\":\"2018-08-29\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-08-29\",\"IdAgente\":20180,\"IdOperador\":25,\"IdEstado\":\"PEN\",\"Glosa\":\"\",\"IdTipoCanalIngreso\":\"AGE\"}],\"p_DetalleOperacion\":[{\"IdInternoDetalle\":0,\"IdInternoOperacion\":1,\"IdReserva\":0,\"IdCliente\":100024,\"IdPortafolio\":100549,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"IdMesa\":3,\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdInstrumento\":81833,\"Nemotecnico\":\"BCU0301018\",\"EsFungible\":\"N\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"FolioCartera\":0,\"IdCartera\":0,\"CodigoIFRS\":\"T\",\"IdEmisor\":\"BCU\",\"IdMoneda\":\"CLP\",\"IdTipoReajuste\":\"UF\",\"IdMonedaEmisionInstrumento\":\"UF\",\"IdMonedaLiquidacionInstrumento\":\"CLP\",\"IdMonedaLiquidacionOperacion\":\"CLP\",\"CorteMinimo\":500,\"Cantidad\":500,\"Precio\":100.33,\"PrecioMercado\":100.28,\"Tasa\":0,\"TasaMercado\":0.43,\"Monto\":13830404,\"MontoLiquidacion\":13830404,\"MontoLiquidacionOperacion\":13830404,\"ValorRescate\":0,\"FechaVencimiento\":\"2018-10-01\"}],\"p_FormasDePago\":[{\"IdInternoPagoOperacion\":1,\"IdInternoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdFormaDePago\":\"COMPAC_CIN_CLP_0\",\"IdOperacion\":11092,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"CLP\",\"MontoFormaDePago\":13830404,\"AfectarTesoreria\":\"N\"}]}";
		this.entradaCrearOperacionMesa2 = gson.fromJson(in4, CrearOperacionMesa_IN.class);
		
		String in5 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operacion\":[{\"Accion\":\"CERRAR\",\"IdInternoOperacion\":1,\"IdOperacion\":11092,\"FolioOperacion\":0,\"IdCliente\":100024,\"IdPortafolio\":100549,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"Intermesa\":\"N\",\"IdMesaOrigen\":3,\"IdMesaDestino\":3,\"AfectaCustodia\":\"N\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdTipoReajuste\":\"CLP\",\"Plazo\":7,\"Monto\":13830404,\"MontoMoneda\":13830404,\"MontoLiquidacion\":13830404,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"Tasa\":1.2,\"ValorFinal\":13869129,\"ValorFinalMoneda\":13869129,\"TasaAgente\":0.25,\"FechaOperacion\":\"2018-08-28\",\"FechaVencimiento\":\"2018-08-29\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-08-29\",\"IdAgente\":20180,\"IdOperador\":25,\"IdEstado\":\"PEN\",\"Glosa\":\"\",\"IdTipoCanalIngreso\":\"AGE\"}],\"p_DetalleOperacion\":[{\"IdInternoDetalle\":0,\"IdInternoOperacion\":1,\"IdReserva\":679,\"IdCliente\":100024,\"IdPortafolio\":100549,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"IdMesa\":3,\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdInstrumento\":81833,\"Nemotecnico\":\"BCU0301018\",\"EsFungible\":\"N\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"FolioCartera\":0,\"IdCartera\":0,\"CodigoIFRS\":\"T\",\"IdEmisor\":\"BCU\",\"IdMoneda\":\"CLP\",\"IdTipoReajuste\":\"UF\",\"IdMonedaEmisionInstrumento\":\"UF\",\"IdMonedaLiquidacionInstrumento\":\"CLP\",\"IdMonedaLiquidacionOperacion\":\"CLP\",\"CorteMinimo\":500,\"Cantidad\":500,\"Precio\":100.33,\"PrecioMercado\":100.28,\"Tasa\":0,\"TasaMercado\":0.43,\"Monto\":13830404,\"MontoLiquidacion\":13830404,\"MontoLiquidacionOperacion\":13830404,\"ValorRescate\":0,\"FechaVencimiento\":\"2018-10-01\"}],\"p_FormasDePago\":[{\"IdInternoPagoOperacion\":1,\"IdInternoOperacion\":1,\"IdComprobantePagoOperacion\":1605,\"IdFormaDePago\":\"COMPAC_CIN_CLP_0\",\"IdOperacion\":11092,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"CLP\",\"MontoFormaDePago\":13830404,\"AfectarTesoreria\":\"N\"}]}";
		this.entradaCrearOperacionMesa3 = gson.fromJson(in5, CrearOperacionMesa_IN.class);
	}
	
	public IngresarPactoBuilder setIdEmpresa(Long idEmpresa) {
		this.entradaCumpleRequisitos.p_IdEmpresa = idEmpresa;
		this.entradaCrearOperacionMesa1.p_IdEmpresa = idEmpresa;
		this.entradaCalcularParidad.p_IdEmpresa = idEmpresa;
		this.entradaCrearOperacionMesa2.p_IdEmpresa = idEmpresa;
		this.entradaCrearOperacionMesa3.p_IdEmpresa = idEmpresa;
		return this;
	}
	public IngresarPactoBuilder setIdCliente(Long idCliente) {
		this.entradaCrearOperacionMesa1.p_Operacion.get(0).IdCliente = idCliente;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).IdCliente = idCliente;
		this.entradaCrearOperacionMesa2.p_DetalleOperacion.get(0).IdCliente = idCliente;
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).IdCliente = idCliente;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).IdCliente = idCliente;
		return this;
	} 
	public IngresarPactoBuilder setIdPortafolio(Long idPortafolio) {
		this.entradaCumpleRequisitos.P_DATOS.get(0).IdPortafolio = idPortafolio;
		this.entradaCrearOperacionMesa1.p_Operacion.get(0).IdPortafolio = idPortafolio;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).IdPortafolio = idPortafolio;
		this.entradaCrearOperacionMesa2.p_DetalleOperacion.get(0).IdCliente = idPortafolio;
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).IdPortafolio = idPortafolio;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).IdCliente = idPortafolio;
		return this;
	} 
	
	public IngresarPactoBuilder setIdClienteCorredor(Long idClienteCorredor) {
		this.entradaCrearOperacionMesa1.p_Operacion.get(0).IdClienteCorredor = idClienteCorredor;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).IdClienteCorredor = idClienteCorredor;
		this.entradaCrearOperacionMesa2.p_DetalleOperacion.get(0).IdClienteCorredor = idClienteCorredor;
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).IdClienteCorredor = idClienteCorredor;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).IdClienteCorredor = idClienteCorredor;
		return this;
	}
	
	public IngresarPactoBuilder setIdPortafolioCorredor(Long idPortafolioCorredor) {
		this.entradaCrearOperacionMesa1.p_Operacion.get(0).IdPortafolioCorredor = idPortafolioCorredor;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).IdPortafolioCorredor = idPortafolioCorredor;
		this.entradaCrearOperacionMesa2.p_DetalleOperacion.get(0).IdPortafolioCorredor = idPortafolioCorredor;
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).IdPortafolioCorredor = idPortafolioCorredor;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).IdPortafolioCorredor = idPortafolioCorredor;
		return this;
	}
	
	public IngresarPactoBuilder setIdTipoOperacion(String idTipoOperacion) {
		this.entradaCumpleRequisitos.P_DATOS.get(0).IdTipoOperacion = idTipoOperacion;
		this.entradaCrearOperacionMesa1.p_Operacion.get(0).IdTipoOperacion = idTipoOperacion;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).IdTipoOperacion = idTipoOperacion;
		this.entradaCrearOperacionMesa2.p_DetalleOperacion.get(0).IdTipoOperacion = idTipoOperacion;
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).IdTipoOperacion = idTipoOperacion;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).IdTipoOperacion = idTipoOperacion;
		return this;
	}
	public IngresarPactoBuilder setFechaOperacion(String fechaOperacion) {
		this.entradaCumpleRequisitos.P_DATOS.get(0).FechaOperacion = fechaOperacion;

		this.entradaCrearOperacionMesa1.p_Operacion.get(0).FechaOperacion = fechaOperacion;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).FechaOperacion = fechaOperacion;
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).FechaOperacion = fechaOperacion;
		return this;
	}
	
	public IngresarPactoBuilder setFechaLiquidacion(String fechaLiquidacion) {
		this.entradaCumpleRequisitos.P_DATOS.get(0).FechaLiquidacion = fechaLiquidacion;
		this.entradaCrearOperacionMesa1.p_Operacion.get(0).FechaLiquidacion = fechaLiquidacion;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).FechaLiquidacion = fechaLiquidacion;		
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).FechaLiquidacion = fechaLiquidacion;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).FechaVencimiento = fechaLiquidacion;
		return this;
	}
	
	public IngresarPactoBuilder setFechaVencimiento(String fechaVencimiento) {
		this.entradaCrearOperacionMesa1.p_Operacion.get(0).FechaVencimiento = fechaVencimiento;
		this.entradaCrearOperacionMesa2.p_Operacion.get(0).FechaVencimiento = fechaVencimiento;		
		this.entradaCrearOperacionMesa3.p_Operacion.get(0).FechaVencimiento = fechaVencimiento;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).FechaVencimiento = fechaVencimiento;
		return this;
	}
	
	public IngresarPactoBuilder setIdInstrumento(Long idInstrumento) {
		this.entradaCrearOperacionMesa2.p_DetalleOperacion.get(0).IdInstrumento = idInstrumento;
		this.entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).IdInstrumento = idInstrumento;
		return this;
	}
	
	
	private String INSERT_CUMPLE_REQUISITOS = "INSERT INTO \"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_IN\" (\"IdPortafolio\",\"IdTipoOperacion\",\"IdMoneda\",\"IdCondicionDeLiquidacion\",\"FechaOperacion\",\"FechaLiquidacion\",\"MontoOperacion\") VALUES (:IdPortafolio, :IdTipoOperacion, :IdMoneda, :IdCondicionDeLiquidacion, :FechaOperacion, :FechaLiquidacion, :MontoOperacion) ;";
	private String CALL_CUMPLE_REQUISITOS = "CALL \"BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosClienteIngresoOperacionRF\"(p_IdEmpresa => :p_IdEmpresa,p_IdUsuario => :p_IdUsuario,p_IdModulo => :p_IdModulo,p_UsaReglasNegocio => :p_UsaReglasNegocio,p_out_AdvertenciasContador => @p_out_AdvertenciasContador,p_out_ErroresContador => @p_out_ErroresContador)";
	private String SELECT_ERRORES_CUMPLE_REQUISITOS = "SELECT \"Codigo\", \"Descripcion\" FROM \"BSCL.Mesa.BD::PAR.ServicioCumpleRequisitosClienteIngresoOperacionRF_Errores_OUT\"";
	
	public CrearOperacionMesa_OUT build(Connection conn) throws Exception {
		SQL sql = new SQL();
		boolean cumpleRequisitos = cumpleRequisitosIngresoOperacion(conn);
		if(cumpleRequisitos) {

			CrearOperacionMesa operacionMesa = new CrearOperacionMesa();
			CrearOperacionMesa_OUT salida1 = operacionMesa.logica(conn, entradaCrearOperacionMesa1);
			boolean mesa1 = salida1.P_OUT_MENSAJES.stream().map(det -> det.tipo).filter(det -> !det.equalsIgnoreCase("ok") ).collect(Collectors.toList()).isEmpty();
			if(mesa1) {
				CalcularParidad paridad = new CalcularParidad();
				CalcularParidadSalida salidaCalcularParidad = paridad.logica(conn, entradaCalcularParidad);
				
				entradaCrearOperacionMesa2.p_Operacion.get(0).IdOperacion = salida1.P_OUT_OPERACION.get(0).IdOperacion;
				entradaCrearOperacionMesa2.p_FormasDePago.get(0).IdOperacion = salida1.P_OUT_OPERACION.get(0).IdOperacion;
				entradaCrearOperacionMesa2.p_FormasDePago.get(0).MontoFormaDePago = salidaCalcularParidad.p_out_CalculosSalida.get(0).MontoDestino;
				entradaCrearOperacionMesa2.p_FormasDePago.get(0).IdMoneda = salidaCalcularParidad.p_out_CalculosSalida.get(0).IdMonedaDestino;
				
				
				CrearOperacionMesa_OUT salida2 = operacionMesa.logica(conn, entradaCrearOperacionMesa2);
				boolean mesa2 = salida2.P_OUT_MENSAJES.stream().map(det -> det.tipo).filter(det -> !det.equalsIgnoreCase("ok") ).collect(Collectors.toList()).isEmpty();
				if(mesa2) {
					
					entradaCrearOperacionMesa3.p_Operacion.get(0).IdOperacion = salida2.P_OUT_OPERACION.get(0).IdOperacion;
					entradaCrearOperacionMesa3.p_FormasDePago.get(0).IdOperacion = salida2.P_OUT_FORMASDEPAGO.get(0).IdOperacion;
					entradaCrearOperacionMesa3.p_FormasDePago.get(0).IdComprobantePagoOperacion = salida2.P_OUT_FORMASDEPAGO.get(0).IdComprobantePagoOperacion;
					entradaCrearOperacionMesa3.p_FormasDePago.get(0).MontoFormaDePago = salida2.P_OUT_FORMASDEPAGO.get(0).MontoFormaDePago;
					entradaCrearOperacionMesa3.p_FormasDePago.get(0).IdMoneda = salida2.P_OUT_FORMASDEPAGO.get(0).IdMoneda;
					entradaCrearOperacionMesa3.p_FormasDePago.get(0).RecibePagaPortafolio = salida2.P_OUT_FORMASDEPAGO.get(0).RecibePagaPortafolio;
					
					entradaCrearOperacionMesa3.p_DetalleOperacion.get(0).IdReserva = salida2.P_OUT_DETALLEOPERACION.get(0).IdReserva;
					CrearOperacionMesa_OUT salida3 = operacionMesa.logica(conn, entradaCrearOperacionMesa3);
										
					System.out.println(salida1);
					System.out.println(salidaCalcularParidad);
					System.out.println(salida2);
					System.out.println(salida3);
					
					return salida3;
				}else {
					return salida2;
				}
			}else {
				return salida1;
			}
		}else {
			List<ErrorDetalle> errores = sql.query(conn, SELECT_ERRORES_CUMPLE_REQUISITOS, new HashMap<String, Object>(), ErrorDetalle.class);
			System.out.println(errores);
		}
		return null;
	}

	private boolean cumpleRequisitosIngresoOperacion(Connection conn) throws SQLException {
		SQL sql = new SQL();
		//servicio requisitos cliente ingreso operacion rf
		sql.batch(conn, INSERT_CUMPLE_REQUISITOS, this.entradaCumpleRequisitos.P_DATOS, EntradaCumpleRequisitos.Datos.class);
		HashMap<String, Object> paramsIn = new HashMap<>();
		paramsIn.put("p_IdEmpresa", 1);
		paramsIn.put("p_IdUsuario", 25);
		paramsIn.put("p_IdModulo", "MRF");
		paramsIn.put("p_UsaReglasNegocio", "S");
		
		HashMap<String, Class> paramsOut = new HashMap<>();
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		
		HashMap<String, Object> result = sql.call(conn, CALL_CUMPLE_REQUISITOS, paramsIn, paramsOut);
		return result.get("p_out_ErroresContador").equals(0L);
	}
	
}

package cl.bcs.plataforma.tests.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.utils.Utils;

public class CrearIngresoEgresoBuilder {
	@Disabled	
	@Test
	void validarCrearIngresoEgreso() throws Exception{
		try(Connection conn = PlataformaUtils.getQAConnection()){
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), 1);
			CrearIngresoEgreso_Salida builder = new CrearIngresoEgresoBuilder().build(conn);
			conn.rollback();
			assertNotNull(builder);
			assertEquals(builder.P_OUT_ERRORESCONTADOR, 0, 0L);
			assertNotNull(builder.P_OUT_INGRESOEGRESOGENERADO.get(0));
			assertNotNull(builder.P_OUT_INGRESOEGRESOGENERADO.get(0).IdIngresoEgreso);
			assertTrue(builder.P_OUT_INGRESOEGRESOGENERADO.get(0).IdIngresoEgreso != 0);
		}
	}
	
	private CrearIngresoEgreso_Entrada entradaCrearOperacion;
	
	public CrearIngresoEgresoBuilder() {
		Gson gson = Utils.getGsonBuilder();

		String inOperacion = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FormaDePago\":[{\"IdMedioDePago\":\"EFE\",\"IdModalidadDePago\":\"MODCAJ\",\"IdMoneda\":\"CLP\",\"Monto\":200000,\"IdCuentaBancariaCorredora\":0,\"IdTarjetaBancariaCorredora\":0,\"IdChequera\":0,\"NumeroDocumentoCorredora\":0,\"FechaDocumentoPagoCorredora\":\"0001-01-01\",\"IdCuentaBancariaSocio\":0,\"IdBancoSocio\":0,\"NombreCuentaBancariaSocio\":\"\",\"NumeroCuentaBancariaSocio\":\"\",\"IdTipoCuentaBancariaSocio\":\"\",\"NombreTipoCuentaBancariaSocio\":\"\",\"IdTarjetaBancariaSocio\":0,\"IdTipoTarjetaBancariaSocio\":\"\",\"NombreTipoTarjetaBancariaSocio\":\"\",\"NumeroTarjetaBancariaSocio\":\"\",\"NumeroSerieSocio\":\"\",\"NumeroDocumentoSocio\":0,\"FechaDocumentoPagoSocio\":\"2018-08-28\",\"NumeroReferencia\":1,\"NumeroReferenciaFormaDePago\":1}],\"p_IngresoEgresoGenerar\":[{\"IdTipoMovimiento\":\"MOVING\",\"IdTipoPersona\":\"CLI\",\"IdPersona\":20180,\"IdPortafolio\":100039,\"IdMoneda\":\"CLP\",\"Monto\":200000,\"FechaLiquidacion\":\"2018-08-28\",\"IdConcepto\":\"AJCTCO\",\"IdTipoPersonaTitularPago\":\"CLI\",\"IdPersonaTitularPago\":20180,\"IdPortafolioTitularPago\":100039,\"IdCaja\":0,\"SecuenciaAgrupacion\":\"\",\"SecuenciaRelacionada\":null,\"Glosa\":\"\",\"IdEmpleadoDigitador\":192,\"IdSucursalEmpleadoDigitador\":0,\"IdTipoComprobante\":\"\",\"IdComprobante\":0,\"FechaComprobante\":\"0001-01-01\",\"FechaLiquidacionComprobante\":\"0001-01-01\",\"FolioCartera\":0,\"IdInstrumento\":0,\"IdTipoInstrumento\":\"\",\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"2018-08-28\",\"IdCondicionLiquidacion\":\"\",\"IdBolsa\":\"\",\"IdMonedaOperacion\":\"\",\"MontoOperacion\":0,\"IdTipoReajuste\":\"\",\"FechaParidad\":\"0001-01-01\",\"ValorParidad\":0,\"Bilateral\":\"N\",\"GeneraComprobante\":\"N\",\"AgruparEnComprobante\":\"N\",\"MueveCuentaInversion\":\"S\",\"TitularPago\":\"N\",\"NumeroReferencia\":1}],\"p_EsIngresoAutomatico\":\"N\"}";
		this.entradaCrearOperacion = gson.fromJson(inOperacion, CrearIngresoEgreso_Entrada.class);		
	}
	
	public CrearIngresoEgreso_Salida build(Connection conn) throws Exception{
		CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Salida salida = crearIngresoEgreso.logica(conn, this.entradaCrearOperacion);
		return salida;
	}
	
	public CrearIngresoEgresoBuilder setIdEmpresa(Long p_IdEmpresa){
		this.entradaCrearOperacion.p_IdEmpresa = p_IdEmpresa;
		return this;
	}
		
	public CrearIngresoEgresoBuilder setIdPortafolio(Long IdPortafolio){
		this.entradaCrearOperacion.p_IngresoEgresoGenerar.get(0).IdPortafolio = IdPortafolio;
		return this;
	}
	
	public CrearIngresoEgresoBuilder setIdMoneda(String IdMoneda){
		this.entradaCrearOperacion.p_IngresoEgresoGenerar.get(0).IdMoneda = IdMoneda;
		return this;
	}
	
	public CrearIngresoEgresoBuilder setIdTipoMovimiento(String IdTipoMovimiento){
		this.entradaCrearOperacion.p_IngresoEgresoGenerar.get(0).IdTipoMovimiento = IdTipoMovimiento;
		return this;
	}
	
	public CrearIngresoEgresoBuilder setIdConcepto(String IdConcepto){
		this.entradaCrearOperacion.p_IngresoEgresoGenerar.get(0).IdConcepto = IdConcepto;
		return this;
	}
	
	public CrearIngresoEgresoBuilder setMonto(BigDecimal Monto){
		this.entradaCrearOperacion.p_IngresoEgresoGenerar.get(0).Monto = Monto;
		return this;
	}
	
	public CrearIngresoEgresoBuilder setFechaLiquidacion(String FechaLiquidacion){
		this.entradaCrearOperacion.p_IngresoEgresoGenerar.get(0).FechaLiquidacion = FechaLiquidacion;
		return this;
	}
	
	public CrearIngresoEgresoBuilder setIdPersona(Long IdPersona){
		this.entradaCrearOperacion.p_IngresoEgresoGenerar.get(0).IdPersona = IdPersona;
		return this;
	}
	

}

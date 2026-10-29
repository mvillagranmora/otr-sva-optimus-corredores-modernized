package cl.bcs.plataforma.tests.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Utils;

public class CrearCargoAbonoBuilder {
	@Disabled
	@Test
	void validarCrearCargoAbono() throws Exception{
		try(Connection conn = PlataformaUtils.getQAConnection()){
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), 1);
			CrearCargoAbonoSalida builder = new CrearCargoAbonoBuilder().setFechaCondicionDeLiquidacion(fechaSistema).build(conn);
			conn.rollback();
			assertNotNull(builder);
			assertEquals(builder.P_OUT_ERRORESCONTADOR, 0, 0L);
			assertNotNull(builder.P_OUT_CARGOABONOGENERADO.get(0));
			assertNotNull(builder.P_OUT_CARGOABONOGENERADO.get(0).IdCargoAbonoMovimiento);
			assertTrue(builder.P_OUT_CARGOABONOGENERADO.get(0).IdCargoAbonoMovimiento != 0);
		}
	}
	
	private CrearCargoAbonoEntrada entradaCrearOperacion;
	
	public CrearCargoAbonoBuilder() {
		Gson gson = Utils.getGsonBuilder();

		String inOperacion = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CTA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaSistema\":\"2018-08-21\",\"p_CargoAbonoAGenerar\":[{\"NumeroCargoAbono\":1,\"IdPortafolio\":100039,\"IdMoneda\":\"CLP\",\"IdTipoMovimiento\":\"MOVABN\",\"IdConcepto\":\"APORCI\",\"Descripcion\":\"\",\"IdCondicionDeLiquidacion\":\"T0\",\"FechaCondicionDeLiquidacion\":\"2018-08-21\",\"Monto\":100000,\"IdEjecutivo\":0,\"IdSucursal\":0,\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"0001-01-01\",\"FechaLiquidacionOperacion\":\"0001-01-01\",\"FechaVencimientoOperacion\":null,\"IdMonedaOperacion\":\"\",\"MontoOperacion\":null,\"TipoCambioOperacion\":0,\"IdContraparte\":null,\"IdTipoComprobante\":null,\"IdComprobante\":null,\"FolioComprobante\":null,\"IdComprobanteDetalle\":null,\"IdComprobanteFormaDePago\":null,\"IdTipoMovimientoAsociado\":null,\"IdMovimientoAsociado\":null,\"FechaComprobante\":null,\"IdPersonaPagoCobro\":null,\"IdInstrumento\":null,\"IdTipoInstrumento\":null,\"Nemo\":null,\"EsIndicadorBilateral\":\"N\",\"EsCargoAbonoPorTraspaso\":\"N\",\"IdPortafolioTraspaso\":null,\"GeneroContraparteEnTesoreria\":\"N\",\"CertificadoAutomatico\":\"N\"}],\"p_EsIngresoManual\":\"S\"}";
		this.entradaCrearOperacion = gson.fromJson(inOperacion, CrearCargoAbonoEntrada.class);		
	}
	
	public CrearCargoAbonoBuilder setIdEmpresa(Long p_IdEmpresa){
		this.entradaCrearOperacion.p_IdEmpresa = p_IdEmpresa;
		return this;
	}
	
	public CrearCargoAbonoBuilder setIdPortafolio(Long IdPortafolio){
		this.entradaCrearOperacion.p_CargoAbonoAGenerar.get(0).IdPortafolio = IdPortafolio;
		return this;
	}
	
	public CrearCargoAbonoBuilder setIdMoneda(String IdMoneda){
		this.entradaCrearOperacion.p_CargoAbonoAGenerar.get(0).IdMoneda = IdMoneda;
		return this;
	}
	
	public CrearCargoAbonoBuilder setFechaCondicionDeLiquidacion(String FechaCondicionDeLiquidacion){
		this.entradaCrearOperacion.p_CargoAbonoAGenerar.get(0).FechaCondicionDeLiquidacion = FechaCondicionDeLiquidacion;
		return this;
	}
	
	public CrearCargoAbonoBuilder setMonto(BigDecimal Monto){
		this.entradaCrearOperacion.p_CargoAbonoAGenerar.get(0).Monto = Monto;
		return this;
	}
	
	public CrearCargoAbonoBuilder setCertificadoAutomatico(String CertificadoAutomatico){
		this.entradaCrearOperacion.p_CargoAbonoAGenerar.get(0).CertificadoAutomatico = CertificadoAutomatico;
		return this;
	}
	
	public CrearCargoAbonoBuilder setIdTipoMovimiento(String IdTipoMovimiento){
		this.entradaCrearOperacion.p_CargoAbonoAGenerar.get(0).IdTipoMovimiento = IdTipoMovimiento;
		return this;
	}
	
	public CrearCargoAbonoBuilder setIdConcepto(String IdConcepto){
		this.entradaCrearOperacion.p_CargoAbonoAGenerar.get(0).IdConcepto = IdConcepto;
		return this;
	}
	
	public CrearCargoAbonoSalida build(Connection conn) throws Exception{
		CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
		CrearCargoAbonoSalida salida = crearCargoAbono.logica(conn, this.entradaCrearOperacion);
		return salida;
	}
	
}

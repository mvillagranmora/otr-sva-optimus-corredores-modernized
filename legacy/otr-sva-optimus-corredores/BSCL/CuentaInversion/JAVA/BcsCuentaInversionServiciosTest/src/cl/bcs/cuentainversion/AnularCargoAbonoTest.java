package cl.bcs.cuentainversion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tests.utils.CrearCargoAbonoBuilder;
import cl.bcs.plataforma.tests.utils.builders.ClienteBuilder;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;

@TestInstance(Lifecycle.PER_CLASS)
class AnularCargoAbonoTest {
	private Connection conn;

	private String perfilComercial = "MODELO COMERCIAL ARRIEGADO";
	private Long idEmpresa = 1L;
	private Long idEmpleadoEjecutivo = 192L;
	private String fechaMovimiento = "2018-10-10";
	private SalidaServicioCrearCliente cliente;
	private CrearCargoAbonoSalida cargoAbono;

	@BeforeAll
	void setUp() {
		try {
			// 0 - preparar conexion
			this.conn = PlataformaUtils.getQAConnection();
			// 1 - Crear Cliente
			this.cliente = new ClienteBuilder().setIdEmpresa(idEmpresa).setIdPerfilComercial(perfilComercial)
					.setIdEmpleadoEjecutivo(idEmpleadoEjecutivo).build(conn);
			// 2 - Ingresar Cargo Abono
			this.fechaMovimiento = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA","PLA.FechaSistema");
			this.cargoAbono = new CrearCargoAbonoBuilder().setIdEmpresa(idEmpresa)
					.setIdPortafolio(cliente.P_OUT_REFIDPORTAFOLIO).setMonto(BigDecimal.valueOf(100000L))
					.setFechaCondicionDeLiquidacion(fechaMovimiento).setIdMoneda("CLP").build(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterAll
	void tearApart() {
		try {
			if (conn != null)
				conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	void evaluarCreacionCargoAbono() {
		assertTrue(cargoAbono.P_OUT_CARGOABONOGENERADO.size() > 0, "Existe un cargo abono creado?");
	}

	@Test
	void anularCargoAbono() {
		AnularCargoAbono anularCargoAbono = new AnularCargoAbono();
		AnularCargoAbono_Entrada in = new AnularCargoAbono_Entrada();
		try {
			in.p_IdEmpresa = idEmpresa;
			in.p_IdUsuario = 25L;
			in.p_IdModulo = "CTA";
			in.p_simularOperacion = "N";
			in.p_UsaReglasNegocio = "S";
			in.p_FechaMovimiento = fechaMovimiento;
			in.p_EsAnulacionManual = "N";
			in.p_CargoAbonoAAnular = Optional.ofNullable(cargoAbono.P_OUT_CARGOABONOGENERADO).orElse(new ArrayList<>())
					.stream().map(cb -> {
						CargoAbono_Entrada cargoAbonoTemp = new CargoAbono_Entrada();
						cargoAbonoTemp.IdCargoAbonoMovimiento = cb.IdCargoAbonoMovimiento;
						cargoAbonoTemp.GlosaAnulacion = "ANULACION PU";
						return cargoAbonoTemp;
					}).collect(Collectors.toList());

			AnularCargoAbono_Salida salida = anularCargoAbono.logica(conn, in);
			System.out.println(salida);
			assertTrue(in.p_CargoAbonoAAnular.get(0).IdCargoAbonoMovimiento.equals(salida.P_OUT_MENSAJES.get(0).id));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

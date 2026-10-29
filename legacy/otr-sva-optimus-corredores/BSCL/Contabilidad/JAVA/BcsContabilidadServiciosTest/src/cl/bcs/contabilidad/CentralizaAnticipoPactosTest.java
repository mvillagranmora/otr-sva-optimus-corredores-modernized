package cl.bcs.contabilidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import cl.bcs.contabilidad.BcsCentralizacion.EntradaCentralizacion;
import cl.bcs.contabilidad.BcsCentralizacion.EntradaCentralizacionInfoAdicional;
import cl.bcs.contabilidad.BcsCentralizacion.SalidaCentralizacion;
import cl.bcs.mesa.MRF.AnticiparOperacion.AnticiparOperacionMesa_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tests.utils.builders.AnticiparPactoBuilder;
import cl.bcs.plataforma.tests.utils.facades.IngresoPactosFacade;
import cl.bcs.utils.Utils;

@TestInstance(Lifecycle.PER_CLASS)
class CentralizaAnticipoPactosTest {
	protected static Connection conn;
	protected CrearOperacionMesa_OUT pacto;
	private AnticiparOperacionMesa_OUT anticipo;
	protected EntradaCentralizacionInfoAdicional infoAdicional;
	
	@BeforeAll void prepararAmbiente(){
		try {
			//0 - Variables prueba
			conn = PlataformaUtils.getQAConnection();
			String perfilComercial = "CLIENTES BALANCEADO";
			Long idInstrumento = 81833L;
			Long idEmpresa = 1L;
			
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, idEmpresa);
			
			infoAdicional = new  EntradaCentralizacionInfoAdicional();
			infoAdicional.p_Empresa = empresa;
			infoAdicional.p_FechaSistema = fechaSistema;
			infoAdicional.p_LocalizacionSistema = localizacionSistema;
			infoAdicional.p_IdMonedaSistema = idMonedaSistema;
			
			//1 - FACADE Ingreso compra pacto cuenta propia
			IngresoPactosFacade ingresoPactos = new IngresoPactosFacade(conn, idEmpresa);
			this.pacto = ingresoPactos.ingresarPactoCompra(perfilComercial, idInstrumento);
						
			CrearOperacionMesa_Operacion_OUT orig = this.pacto.P_OUT_OPERACION.get(0);
			
			//2 - Anticipar pacto compra
			this.anticipo = new AnticiparPactoBuilder(idEmpresa)
					.setOperacion(orig)
					.build(conn);
			
			System.out.println(this.anticipo);
			assertNotNull(this.anticipo);
			assertNotNull(this.anticipo.P_OUT_OPERACION);
			assertTrue(this.anticipo.P_OUT_OPERACION.size()>0);
			assertNotEquals(this.anticipo.P_OUT_OPERACION.get(0).IdOperacion.longValue(), 0L);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	@AfterAll void terminarPrueba() {
		try {
			conn.rollback();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test void entradaVacia() throws SQLException {
		CentralizaAnticipoPactos anticipoPactos = new CentralizaAnticipoPactos();
		assertThrows(Exception.class, () -> {
			anticipoPactos.centralizar(conn, new EntradaCentralizacion(),infoAdicional);
	    });	
	}
	
	
	@Test void centralizacionSinDatos() throws Exception {
		CentralizaAnticipoPactos anticipoPactos = new CentralizaAnticipoPactos();
		
		String entradaDatos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdTipoCentralizacion\":242,\"p_FechaContable\":\"2010-08-20\",\"p_Ejercicio\":2010,\"p_SimulaOperacion\":\"N\"}";
		EntradaCentralizacion entrada = Utils.getGsonBuilder().fromJson(entradaDatos, EntradaCentralizacion.class);
		SalidaCentralizacion salida = anticipoPactos.centralizar(conn, entrada,infoAdicional);
		System.out.println(salida);
		
		assertNotNull(salida);
		assertEquals(0L, salida.P_OUT_ERRORESCONTADOR,0.1);
		assertEquals(0L, salida.P_TABMOVIMIENTOS.size(),0.1);
		assertEquals(0L, salida.P_TABEVENTOS.size(),0.1);
		assertEquals(0L, salida.P_TABDATOSMOVIMIENTO.size(),0.1);
		assertEquals(0L, salida.P_TABDATOSEVENTO.size(),0.1);
		assertEquals(0L, salida.P_OUT_RESULTADO.size(),0.1);
	}
	
	@Test void centralizacionConDatos() throws Exception {
						
		CentralizaAnticipoPactos anticipoPactos = new CentralizaAnticipoPactos();
		
		String entradaDatos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdTipoCentralizacion\":242,\"p_FechaContable\":\"2018-08-21\",\"p_Ejercicio\":2018,\"p_SimulaOperacion\":\"N\"}";
		EntradaCentralizacion entrada = Utils.getGsonBuilder().fromJson(entradaDatos, EntradaCentralizacion.class);
		SalidaCentralizacion salida = anticipoPactos.centralizar(conn, entrada,infoAdicional);
		System.out.println(salida);
		
		assertNotNull(salida);
		assertEquals(0L, salida.P_OUT_ERRORESCONTADOR,0.1);
		assertEquals(1L, salida.P_OUT_RESULTADO.size(), 0.1);
		assertEquals(1L, salida.P_TABMOVIMIENTOS.size(),0.1);
		assertEquals(salida.P_TABMOVIMIENTOS.get(0).IdMovimiento, this.anticipo.P_OUT_OPERACION.get(0).IdOperacion);
	}

}

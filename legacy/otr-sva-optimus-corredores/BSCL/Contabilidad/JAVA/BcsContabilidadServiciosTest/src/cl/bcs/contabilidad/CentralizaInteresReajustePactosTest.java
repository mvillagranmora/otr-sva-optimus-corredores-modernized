package cl.bcs.contabilidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.google.gson.Gson;

import cl.bcs.contabilidad.BcsCentralizacion.EntradaCentralizacion;
import cl.bcs.contabilidad.BcsCentralizacion.EntradaCentralizacionInfoAdicional;
import cl.bcs.contabilidad.BcsCentralizacion.SalidaCentralizacion;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tests.utils.facades.IngresoPactosFacade;
import cl.bcs.utils.Utils;

@TestInstance(Lifecycle.PER_CLASS)
class CentralizaInteresReajustePactosTest {
		
	protected Connection conn;
	private CrearOperacionMesa_OUT pacto;
	private Long idEmpresa;
	protected EntradaCentralizacionInfoAdicional infoAdicional;
	
	@BeforeAll void prepararAmbiente() {
		try{
			//0 - Variables prueba
			conn = PlataformaUtils.getQAConnection();
			String perfilComercial = "CLIENTES BALANCEADO";
			Long idInstrumento = 81833L;
			this.idEmpresa = 1L;
			
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
			
			
			//1 - Facade Ingresar pacto compra
			IngresoPactosFacade ingresarPactosFacade = new IngresoPactosFacade(conn, idEmpresa);
			this.pacto = ingresarPactosFacade.ingresarPactoCompra(perfilComercial, idInstrumento);
			
			System.out.println(this.pacto);
			assertNotNull(this.pacto);
			assertTrue(this.pacto.P_OUT_ERRORESCONTADOR==0);
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}

	@AfterAll void terminarPrueba() throws SQLException {
		try {
			conn.rollback();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test void centralizacionVacia() {

		try {
			CentralizaInteresReajustePactos centralizacion = new CentralizaInteresReajustePactos();
			Gson gson = Utils.getGsonBuilder();
			String entradaVacia = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdTipoCentralizacion\":240,\"p_FechaContable\":\"2018-08-21\",\"p_Ejercicio\":2018,\"p_SimulaOperacion\":\"N\"}";
			EntradaCentralizacion entrada = gson.fromJson(entradaVacia, EntradaCentralizacion.class);
			entrada.p_FechaContable = "2010-01-01";
			
			SalidaCentralizacion salida = centralizacion.centralizar(this.conn, entrada,infoAdicional);
			
			System.out.println(salida);
			assertTrue(salida.P_OUT_ERRORESCONTADOR.equals(0L));
			
			assertTrue(salida.P_TABMOVIMIENTOS.size() == 0);
			assertTrue(salida.P_TABEVENTOS.size() == 0);
			assertTrue(salida.P_TABDATOSMOVIMIENTO.size() == 0);
			assertTrue(salida.P_TABDATOSEVENTO.size() == 0);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error al centralizar");
		}
	}
	
	@Test void centralizacionConValores() {
		
		try {
			CentralizaInteresReajustePactos servicioCentralizacion = new CentralizaInteresReajustePactos();
			Gson gson = Utils.getGsonBuilder();
			String entradaDatos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_IdTipoCentralizacion\":240,\"p_FechaContable\":\"2018-08-28\",\"p_Ejercicio\":2018,\"p_SimulaOperacion\":\"N\"}";
			EntradaCentralizacion entrada = gson.fromJson(entradaDatos, EntradaCentralizacion.class);
			
			if(this.conn==null) {
				this.conn = PlataformaUtils.getQAConnection();
				this.pacto = Utils.getGsonBuilder().fromJson("{\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"BLOQUEO_OPERACION_ACTUALIZADA\",\"descripcion\":\"BLOQUEO_OPERACION_ACTUALIZADA sin traduccion\"},{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"OPERACION_ACTUALIZADA\",\"descripcion\":\"OPERACION_ACTUALIZADA sin traduccion\"},{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"RESERVA_ACTUALIZADA\",\"descripcion\":\"RESERVA_ACTUALIZADA sin traduccion\"},{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"INGRESO_TRANSITO_CUSTODIA_OK\",\"descripcion\":\"INGRESO_TRANSITO_CUSTODIA_OK sin traduccion\"},{\"id\":1,\"tipo\":\"OK\",\"codigo\":\"INGRESO_TRANSITO_CUSTODIA_OK\",\"descripcion\":\"INGRESO_TRANSITO_CUSTODIA_OK sin traduccion\"},{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"UI_INGRESO_TITULO_OK\",\"descripcion\":\"Se ha ingresado exitosamente el título, con Folio Cartera número 51713\"},{\"id\":1,\"tipo\":\"OK\",\"codigo\":\"UI_INGRESO_TITULO_OK\",\"descripcion\":\"Se ha ingresado exitosamente el título, con Folio Cartera número 44693\"},{\"id\":2,\"tipo\":\"OK\",\"codigo\":\"UI_INGRESO_TITULO_OK\",\"descripcion\":\"Se ha ingresado exitosamente el título, con Folio Cartera número 51714\"},{\"id\":2,\"tipo\":\"OK\",\"codigo\":\"INGRESO_CUSTODIA_OK\",\"descripcion\":\"INGRESO_CUSTODIA_OK sin traduccion\"},{\"id\":3,\"tipo\":\"OK\",\"codigo\":\"INGRESO_CUSTODIA_OK\",\"descripcion\":\"INGRESO_CUSTODIA_OK sin traduccion\"},{\"id\":4,\"tipo\":\"OK\",\"codigo\":\"INGRESO_CUSTODIA_OK\",\"descripcion\":\"INGRESO_CUSTODIA_OK sin traduccion\"},{\"id\":6336,\"tipo\":\"OK\",\"codigo\":\"UI_CARGO_CREADO\",\"descripcion\":\"Cargo  N°6336  creado\"},{\"id\":6337,\"tipo\":\"OK\",\"codigo\":\"UI_ABONO_CREADO\",\"descripcion\":\"Abono N°6337  creado\"},{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"COMPROBANTE_PAGO_ELIMINADO\",\"descripcion\":\"COMPROBANTE_PAGO_ELIMINADO sin traduccion\"},{\"id\":1,\"tipo\":\"OK\",\"codigo\":\"COMPROBANTE_PAGO_ACTUALIZADO\",\"descripcion\":\"COMPROBANTE_PAGO_ACTUALIZADO sin traduccion\"}],\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_OPERACION\":[{\"Accion\":\"CERRAR\",\"IdInternoOperacion\":1,\"IdOperacion\":11108,\"FolioOperacion\":2082,\"IdCliente\":100024,\"IdPortafolio\":100549,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"IdOperacionRelacionada\":0,\"Intermesa\":\"N\",\"IdMesaOrigen\":3,\"IdMesaDestino\":3,\"AfectaCustodia\":\"N\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdTipoReajuste\":\"CLP\",\"Plazo\":7,\"Monto\":13830404,\"MontoMoneda\":13830404,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"Tasa\":1.2,\"ValorFinal\":13869129,\"ValorFinalMoneda\":13869129,\"TasaAgente\":0.25,\"FechaOperacion\":\"2018-08-28 00:00:00.000\",\"FechaVencimiento\":\"2018-08-29 00:00:00.000\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2018-09-04 00:00:00.000\",\"IdAgente\":20180,\"IdOperador\":25,\"IdEstado\":\"VIG\",\"NombreEstado\":\"VIGENTE\",\"Glosa\":\"\",\"IdTipoCanalIngreso\":\"AGE\",\"IdEmisor\":\"OPTIMUS\"}],\"P_OUT_DETALLEOPERACION\":[{\"IdInternoDetalle\":0,\"IdInternoOperacion\":1,\"IdReserva\":694,\"IdCliente\":100024,\"IdPortafolio\":100549,\"IdClienteCorredor\":100022,\"IdPortafolioCorredor\":100037,\"IdMesa\":3,\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"COMPAC\",\"IdInstrumento\":81833,\"EsFungible\":\"N\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"FolioCartera\":0,\"CodigoIFRS\":\"T\",\"IdEmisor\":\"BCU\",\"IdTipoReajuste\":\"UF\",\"IdMonedaEmisionInstrumento\":\"UF\",\"IdMonedaLiquidacionInstrumento\":\"CLP\",\"IdMonedaLiquidacionOperacion\":\"CLP\",\"Cantidad\":500,\"Precio\":100.33,\"Tasa\":0,\"Monto\":13830404,\"MontoLiquidacion\":13830404,\"MontoLiquidacionOperacion\":13830404,\"ValorRescate\":0,\"FechaVencimiento\":null}],\"P_OUT_FORMASDEPAGO\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":1605,\"IdFormaDePago\":\"COMPAC_CIN_CLP_0\",\"IdMoneda\":\"CLP\",\"IdOperacion\":11108,\"RecibePagaPortafolio\":\"R\",\"MontoFormaDePago\":13830404}]}", CrearOperacionMesa_OUT.class);
			}
			
			String fechaSistema = Utils.getFechaSistema(this.conn, Utils.getSchema(), this.idEmpresa);
			entrada.p_FechaContable = fechaSistema;

			SalidaCentralizacion salida = servicioCentralizacion.centralizar(this.conn, entrada,infoAdicional);
			
			System.out.println(salida);
			assertNotNull(this.pacto);
			assertTrue(salida.P_OUT_ERRORESCONTADOR.equals(0L));
			//Obtener el movimiento
			HashMap<Object, Object> movientosCentralizacion = Utils.List2Hash(salida.P_TABMOVIMIENTOS, "IdMovimiento");
			this.pacto.P_OUT_OPERACION.forEach(operacion -> {
				TabMovimientos centralizacion = (TabMovimientos) movientosCentralizacion.get(operacion.IdOperacion);
				
				assertNotNull(centralizacion);
				assertEquals(operacion.IdPortafolioCorredor, centralizacion.IdPortafolio);
				assertEquals(operacion.Monto.doubleValue(), centralizacion.Monto.doubleValue(), 0.5);
				assertEquals(operacion.IdMoneda, centralizacion.IdMoneda);
				
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error al centralizar");
		}
	}

}

package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.ValidarExistenciaCuentaBancaria.EntradaValidarExistenciaBorradorCuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.CuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorCuentaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorDireccionFisica.class);
	
	private String SQL_UPDATE_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorCuentaBancaria_UpdateCuentaBancaria.sql");
	
	public static class EntradaModificarBorradorCuentaBancaria extends EntradaAdministrarBorrador{
		public List<CuentaBancaria> p_CuentasBancarias;
		
		public EntradaModificarBorradorCuentaBancaria(){
			this.p_CuentasBancarias = new ArrayList<CuentaBancaria>();
		}
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorCuentaBancaria jin = gson.fromJson(in.toString(), EntradaModificarBorradorCuentaBancaria.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_CuentasBancarias, CuentaBancaria.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_CuentasBancarias, "CuentasBancarias", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		ValidarExistenciaCuentaBancaria validarExistenciaCuentaBancaria = new ValidarExistenciaCuentaBancaria();
		EntradaValidarExistenciaBorradorCuentaBancaria entradaValidarExistenciaBorradorCuentaBancaria = new EntradaValidarExistenciaBorradorCuentaBancaria();
		entradaValidarExistenciaBorradorCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorCuentaBancaria.p_CuentasBancarias = entrada.p_CuentasBancarias;
		
		List<Validacion> validacionesExistenciaCuentasBancarias = validarExistenciaCuentaBancaria.logica(con, entradaValidarExistenciaBorradorCuentaBancaria).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaCuentasBancarias);
				
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		actualizarCuentasBancarias(con, entrada.p_CuentasBancarias);
		
		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_CuentasBancarias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorCuentaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private void actualizarCuentasBancarias(Connection con, List<CuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::ModificarBorradorCuentaBancaria.actualizarCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CUENTA_BANCARIA, cuentasBancarias, CuentaBancaria.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorCuentaBancaria.actualizarCuentasBancarias", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<CuentaBancaria> cuentasBancarias){
		logger.trace("INICIO::ModificarBorradorCuentaBancaria.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = cuentasBancarias.parallelStream().map(cuentaBancaria->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = cuentaBancaria.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = cuentaBancaria.IdCuentaBancaria;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorCuentaBancaria.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\"p_CuentasBancarias\":[{\"IdInterno\":1,\"IdCuentaBancaria\":0,\"IdTipoBanco\":\"NAC\",\"IdBanco\":12,\"IdTipoCuentaBancaria\":\"CUCO\",\"IdMoneda\":\"CLP\",\"NumeroCuentaBancaria\":\"123456\",\"IdEnrolamiento\":456,\"IdPlaza\":765,\"IdTipoIdentificadorCuenta\":\"\",\"IdentificadorCuenta\":\"\",\"IdPersonaRelacionada\":0,\"CodigoCuentaInterbancario\":\"\",\"CodigoAba\":\"\",\"OtroCodigoBancoExtranjero\":\"\",\"CodigoIban\":\"\",\"OtroCodigoCuentaExtranjero\":\"\",\"CrearPreferenciaPago\":\"N\",\"EsPac\":\"N\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"CUENTA_BANCARIA_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\"}\n";
			EntradaModificarBorradorCuentaBancaria entradaCrearBorradorCuentaBancaria = gson.fromJson(jsonEntrada, EntradaModificarBorradorCuentaBancaria.class);
			ModificarBorradorCuentaBancaria crearBorradorCuentaBancaria = new ModificarBorradorCuentaBancaria();
			try {
				SalidaAdministrarBorrador salidaAdministrarBorrador = crearBorradorCuentaBancaria.logica(con, entradaCrearBorradorCuentaBancaria);
				if (salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
					con.rollback();
				}
				System.out.println(salidaAdministrarBorrador);
			} catch (Exception er) {
				er.printStackTrace();
			} finally {
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}

package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.CuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.DatosPreferenciaPago;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearBorradorCuentaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorCuentaBancaria.class);
	
	private String SQL_INSERT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorCuentaBancaria_InsertCuentaBancaria.sql");
	private String SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorCuentaBancaria_InsertPreferenciaPagoPortafolio.sql");
	private String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorCuentaBancaria_UpdatePreferenciaPagoPortafolio.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorCuentaBancaria_UpdateIdCuentaBancariaPrincipal.sql");
	private String SQL_SELECT_EXISTENCIA_BANCO = "SELECT BANCO.\"IdBanco\" AS \"Existencia_Banco\" FROM \"BSCL.Plataforma.BD::BAN.Banco\" AS BANCO WHERE BANCO.\"IdBanco\"=:p_idBanco";

	
	public static class EntradaCrearBorradorCuentaBancaria extends EntradaAdministrarBorrador{
		public List<CuentaBancaria> p_CuentasBancarias;
		
		public EntradaCrearBorradorCuentaBancaria(){
			this.p_CuentasBancarias = new ArrayList<CuentaBancaria>();
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorCuentaBancaria jin = gson.fromJson(in.toString(), EntradaCrearBorradorCuentaBancaria.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::CrearBorradorCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_CuentasBancarias, CuentaBancaria.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_CuentasBancarias,"CuentasBancarias", out);
		out = validarExistenciaBanco(con, entrada,out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<CuentaBancaria> cuentasBancarias = obtenerCuentasBancarias(con, entrada.p_CuentasBancarias);
		
		insertarCuentasBancarias(con, cuentasBancarias);
		asignarPreferenciaPagoPortafolioPorDefecto(con, entrada, cuentasBancarias);
		out.P_OUT_REFID = obtenerIdsInsertados(cuentasBancarias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorCuentaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private List<CuentaBancaria> obtenerCuentasBancarias(Connection con, List<CuentaBancaria> borradoresCuentasBancarias) throws Exception{
		logger.trace("INICIO::CrearBorradorCuentaBancaria.obtenerCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorCuentaBancaria", Long.valueOf(borradoresCuentasBancarias.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<CuentaBancaria> cuentasBancarias = borradoresCuentasBancarias.parallelStream().map(borradorCuentaBancaria->{
			Long idCuentaBancaria = secuencias.get(index.getAndIncrement());
			CuentaBancaria cuentaBancaria = obtenerEntidadCuentaBancaria(borradorCuentaBancaria, idCuentaBancaria);
			return cuentaBancaria;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorCuentaBancaria.obtenerCuentasBancarias", fin - inicio);
		return cuentasBancarias;
	}
	
	private CuentaBancaria obtenerEntidadCuentaBancaria(CuentaBancaria borradorCuentaBancaria, Long idCuentaBancaria) {
		logger.trace("INICIO::CrearBorradorCuentaBancaria.obtenerEntidadCuentaBancaria");
		long inicio = System.currentTimeMillis();
		
		CuentaBancaria cuentaBancaria = new CuentaBancaria();
		cuentaBancaria.IdInterno = borradorCuentaBancaria.IdInterno;
		cuentaBancaria.IdCuentaBancaria = idCuentaBancaria;
		cuentaBancaria.IdBanco = borradorCuentaBancaria.IdBanco;
		cuentaBancaria.IdTipoCuentaBancaria = borradorCuentaBancaria.IdTipoCuentaBancaria;
		cuentaBancaria.IdMoneda = borradorCuentaBancaria.IdMoneda;
		cuentaBancaria.NumeroCuentaBancaria = borradorCuentaBancaria.NumeroCuentaBancaria;
		cuentaBancaria.IdEnrolamiento = borradorCuentaBancaria.IdEnrolamiento;
		cuentaBancaria.IdPlaza = borradorCuentaBancaria.IdPlaza;
		cuentaBancaria.IdTipoIdentificadorCuenta = borradorCuentaBancaria.IdTipoIdentificadorCuenta;
		cuentaBancaria.IdentificadorCuenta = borradorCuentaBancaria.IdentificadorCuenta;
		cuentaBancaria.IdPersonaRelacionada = borradorCuentaBancaria.IdPersonaRelacionada;
		cuentaBancaria.CodigoCuentaInterbancario = borradorCuentaBancaria.CodigoCuentaInterbancario;
		cuentaBancaria.CodigoAba = borradorCuentaBancaria.CodigoAba;
		cuentaBancaria.OtroCodigoBancoExtranjero = borradorCuentaBancaria.OtroCodigoBancoExtranjero;
		cuentaBancaria.CodigoIban = borradorCuentaBancaria.OtroCodigoBancoExtranjero;
		cuentaBancaria.OtroCodigoCuentaExtranjero = borradorCuentaBancaria.OtroCodigoCuentaExtranjero;
		cuentaBancaria.EsPac = borradorCuentaBancaria.EsPac;
		cuentaBancaria.Predeterminado = borradorCuentaBancaria.Predeterminado;
		cuentaBancaria.IdCuentaBancariaPersona = borradorCuentaBancaria.IdCuentaBancariaPersona;
		cuentaBancaria.CrearPreferenciaPago = borradorCuentaBancaria.CrearPreferenciaPago;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorCuentaBancaria.obtenerEntidadCuentaBancaria", fin - inicio);
		return cuentaBancaria;
	}
	
	private void insertarCuentasBancarias(Connection con, List<CuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::CrearBorradorCuentaBancaria.insertarCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CUENTA_BANCARIA, cuentasBancarias, CuentaBancaria.class);
		sql.batch(con, SQL_UPDATE_CUENTA_BANCARIA_PRINCIPAL, cuentasBancarias, CuentaBancaria.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::CrearBorradorCuentaBancaria.insertarCuentasBancarias", fin - inicio);
	}
	
	private SalidaAdministrarBorrador validarExistenciaBanco(Connection con,EntradaCrearBorradorCuentaBancaria entrada , SalidaAdministrarBorrador salidaCrearBorrador) throws Exception {
		for(CuentaBancaria cuenta:entrada.p_CuentasBancarias) {
			System.out.println(cuenta);
			SQL sql = new SQL();
			HashMap<String,Object> parametros = new HashMap<String,Object>(2);
			parametros.put("p_idBanco",cuenta.IdBanco);
			HashMap<String, String> params = new HashMap<>(2);
			params.put("IdBanco", ""+cuenta.IdBanco);
			Long existeCuenta = sql.countQuery(con, SQL_SELECT_EXISTENCIA_BANCO, parametros);
			if(existeCuenta.equals((long)0)) {
				salidaCrearBorrador.P_OUT_ERRORESCONTADOR ++;
				salidaCrearBorrador.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_BANCO", params, con, entrada.p_IdEmpresa));				
			}			
		}
		return salidaCrearBorrador;		
	}
	
	private void asignarPreferenciaPagoPortafolioPorDefecto(Connection con, EntradaCrearBorradorCuentaBancaria entrada, List<CuentaBancaria> cuentasBancarias) throws Exception {
		
		logger.debug("INICIO::CrearBorradorCuentaBancaria.asignarPreferenciaPagoPortafolioPorDefecto");
		Long inicio = System.currentTimeMillis();
		
		List<CuentaBancaria> cuentasBancariasConPreferenciaPago = cuentasBancarias.parallelStream()
																.filter(cuenta -> esCreacionPreferenciaPago(cuenta.CrearPreferenciaPago, cuenta.IdEnrolamiento))
																.collect(Collectors.toList());
		
		List<DatosPreferenciaPago> preferenciasDePago = cuentasBancariasConPreferenciaPago.parallelStream().map( cuenta -> {
			DatosPreferenciaPago datosPreferenciaPago = new DatosPreferenciaPago();
			datosPreferenciaPago.IdEnrolamiento = cuenta.IdEnrolamiento;
			datosPreferenciaPago.IdMoneda = cuenta.IdMoneda;
			datosPreferenciaPago.IdEmpresa = entrada.p_IdEmpresa;
			datosPreferenciaPago.IdCuentaBancaria = cuenta.IdCuentaBancaria;
			datosPreferenciaPago.IdUsuario = entrada.p_IdUsuario;
			datosPreferenciaPago.IdPersonaRelacionada = cuenta.IdPersonaRelacionada;
			return datosPreferenciaPago;
			}).collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasDePago, DatosPreferenciaPago.class);
		sql.batch(con, SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasDePago, DatosPreferenciaPago.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorCuentaBancaria.asignarPreferenciaPagoPortafolioPorDefecto", fin - inicio);
		
	}
	
	private Boolean esCreacionPreferenciaPago(String crearPreferenciaPago, Long idCliente) {
		logger.trace("INICIO::CrearBorradorCuentaBancaria.esCreacionPreferenciaPago");
		long inicio = System.currentTimeMillis();
		Boolean esCreacionPreferenciaPago = "S".equals(crearPreferenciaPago) && idCliente != null && idCliente.longValue() != 0L;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorCuentaBancaria.esCreacionPreferenciaPago", fin - inicio);
		return esCreacionPreferenciaPago;
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<CuentaBancaria> cuentasBancarias){
		logger.trace("INICIO::CrearBorradorCuentaBancaria.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = cuentasBancarias.parallelStream().map(cuentaBancaria->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = cuentaBancaria.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = cuentaBancaria.IdCuentaBancaria;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorCuentaBancaria.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = 
					"{\"p_CuentasBancarias\":"
					+ "[{\"IdInterno\":1"
					//+ ",\"IdCuentaBancaria\":0"
					+ ",\"IdTipoBanco\":\"NAC\""
					+ ",\"IdBanco\":12"
					+ ",\"IdTipoCuentaBancaria\":\"CUCO\""
					+ ",\"IdMoneda\":\"CLP\""
					+ ",\"NumeroCuentaBancaria\":\"00023456\""
					+ ",\"IdEnrolamiento\":1"
					+ ",\"IdPlaza\":765"
					+ ",\"IdTipoIdentificadorCuenta\":\"\""
					+ ",\"IdentificadorCuenta\":\"\""
					+ ",\"IdPersonaRelacionada\":0"
					+ ",\"CodigoCuentaInterbancario\":\"\""
					+ ",\"CodigoAba\":\"\""
					+ ",\"OtroCodigoBancoExtranjero\":\"\""
					+ ",\"CodigoIban\":\"\""
					+ ",\"OtroCodigoCuentaExtranjero\":\"\""
					+ ",\"CrearPreferenciaPago\":\"N\""
					+ ",\"EsPac\":\"N\""
					+ ",\"Predeterminado\":\"S\"}]"
					
					+ ",\"p_IdEmpresa\":1"
					+ ",\"p_IdUsuario\":4"
					+ ",\"p_IdModulo\":\"SOC\""
					+ ",\"p_UsaReglasNegocio\":\"S\""
					+ ",\"p_CodigoFormulario\":\"CUENTA_BANCARIA_FULL_JURIDICA\""
					+ ",\"p_IdTipoCanal\":\"OPT\"}\n";
			EntradaCrearBorradorCuentaBancaria entradaCrearBorradorCuentaBancaria = gson.fromJson(jsonEntrada, EntradaCrearBorradorCuentaBancaria.class);
			CrearBorradorCuentaBancaria crearBorradorCuentaBancaria = new CrearBorradorCuentaBancaria();				
			try {
				SalidaAdministrarBorrador salidaAdministrarBorrador = crearBorradorCuentaBancaria.logica(con, entradaCrearBorradorCuentaBancaria);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println(salidaAdministrarBorrador.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				con.close();
			}			
    	}
		System.out.println("== END ==");
	}
}

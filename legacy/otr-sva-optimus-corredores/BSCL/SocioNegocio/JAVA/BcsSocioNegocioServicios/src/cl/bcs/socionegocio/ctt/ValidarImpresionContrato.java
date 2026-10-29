package cl.bcs.socionegocio.ctt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.ConsultarFichaSocioNegocio;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull;
import cl.bcs.socionegocio.soc.ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio;
import cl.bcs.socionegocio.soc.ConsultarFichaSocioNegocio.SalidaConsultarFichaSocioNegocio;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull.EntradaValidarRequisitosEnrolamientoFull;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull.SalidaValidarRequisitosEnrolamientoFull;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidarImpresionContrato implements JavaCall {
	static final Logger logger = LogManager.getLogger(ValidarImpresionContrato.class);
	
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_SelectClienteDatosGenerales.sql");
	
	public static class EntradaValidarImpresionContrato extends BcsPojo{
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public Long IdCliente;
	}
	
	public static class SalidaValidarImpresionContrato extends BcsPojo{
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
        public List<Validacion> P_OUT_VALIDACIONES;
        public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;
		
		public SalidaValidarImpresionContrato() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_VALIDACIONES = new ArrayList<Validacion>();
			this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<RequisitoEnrolamientoFull>();
		}	
	}
	
	public SalidaValidarImpresionContrato logica(Connection con, EntradaValidarImpresionContrato entrada) throws Exception {
		logger.info("INICIO::ValidarImpresionContrato");
		
		SalidaValidarImpresionContrato salida = new SalidaValidarImpresionContrato();
		
		List<DatosClienteDatosGenerales> clienteDatosGenerales = obtenerDatosCliente(con, entrada.IdCliente);
		
		ConsultarFichaSocioNegocio consultarFichaSocioNegocio = new ConsultarFichaSocioNegocio();
		
		EntradaConsultarFichaSocioNegocio entradaConsultarFichaSocioNegocio = new EntradaConsultarFichaSocioNegocio();
		entradaConsultarFichaSocioNegocio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarFichaSocioNegocio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarFichaSocioNegocio.entidades = clienteDatosGenerales.stream().map(datoCliente -> {
			ConsultarFichaSocioNegocio.ClienteEntidad clienteEntidad = new ConsultarFichaSocioNegocio.ClienteEntidad();
			clienteEntidad.IdCliente = datoCliente.IdCliente;
			return clienteEntidad;
		}).collect(Collectors.toList());
				
		SalidaConsultarFichaSocioNegocio salidaConsultarFichaSocioNegocio = consultarFichaSocioNegocio.logica(con, entradaConsultarFichaSocioNegocio);

		EntradaValidarRequisitosEnrolamientoFull entradaValidarRequisitosEnrolamientoFull = new EntradaValidarRequisitosEnrolamientoFull();
		entradaValidarRequisitosEnrolamientoFull.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarRequisitosEnrolamientoFull.p_IdModulo = entrada.p_IdModulo;
		entradaValidarRequisitosEnrolamientoFull.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarRequisitosEnrolamientoFull.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaValidarRequisitosEnrolamientoFull.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarRequisitosEnrolamientoFull.clientes = salidaConsultarFichaSocioNegocio.P_OUT_RESULTADOPROCESO;
		
		ValidarRequisitosEnrolamientoFull validarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull();
		SalidaValidarRequisitosEnrolamientoFull salidaValidarRequisitosEnrolamientoFull = validarRequisitosEnrolamientoFull.logica(con, entradaValidarRequisitosEnrolamientoFull);
		salida.P_OUT_ERRORESCONTADOR = salidaValidarRequisitosEnrolamientoFull.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES = salidaValidarRequisitosEnrolamientoFull.P_OUT_MENSAJES;
		salida.P_OUT_REQUISITOS_ENR_FULL = salidaValidarRequisitosEnrolamientoFull.P_OUT_REQUISITOS_ENR_FULL;
		salida.P_OUT_VALIDACIONES = salidaValidarRequisitosEnrolamientoFull.P_OUT_VALIDACIONES;
		
		logger.info("FIN::ValidarImpresionContrato");
		
		return salida;
	}
	
	private List<DatosClienteDatosGenerales> obtenerDatosCliente(Connection con, Long IdCliente) throws Exception {
		logger.debug("INICIO::ValidarImpresionContrato.obtenerDatosCliente");
		long inicio = System.currentTimeMillis();
	
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdCliente", IdCliente);
	
		List<DatosClienteDatosGenerales> cliente = sql.query(con, SQL_SELECT_CLIENTE, params, DatosClienteDatosGenerales.class);
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarImpresionContrato.obtenerDatosCliente", fin - inicio);
	
		return cliente;
    }
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaValidarImpresionContrato jin = gson.fromJson(in.toString(), EntradaValidarImpresionContrato.class);
		SalidaValidarImpresionContrato out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String args[]) {
		System.out.println("== INICIO TEST ==");
		
		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\": \"OPT\",\"IdCliente\":\"1424629\"}";
        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarImpresionContrato entrada = gson.fromJson(jsonRequest, EntradaValidarImpresionContrato.class);
            ValidarImpresionContrato validarImpresionContrato = new ValidarImpresionContrato();

            try {
            	SalidaValidarImpresionContrato salida = validarImpresionContrato.logica(conn, entrada);
                System.out.println(salida.P_OUT_ERRORESCONTADOR);
                System.out.println(salida.P_OUT_MENSAJES);
                System.out.println(salida.P_OUT_REQUISITOS_ENR_FULL);
                System.out.println(salida.P_OUT_VALIDACIONES);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.rollback();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
	}

}

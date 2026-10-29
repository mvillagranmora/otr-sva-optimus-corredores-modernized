package cl.bcs.socionegocio.soc;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ValidarCamposObligatoriosCliente implements JavaCall {
	static final Logger logger = LogManager.getLogger(ValidarCamposObligatoriosCliente.class);

	public static class CampoFaltante extends BcsPojo {
		public Long IdInterno;
		public String Mensaje;
		public String Ubicacion;
	}

	public static class EntradaValidarCamposObligatoriosCliente extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<Long> p_ClienteIds;

		public EntradaValidarCamposObligatoriosCliente() {
			p_ClienteIds = new ArrayList<>();
		}
	}

	public static class SalidaValidarCamposObligatoriosCliente extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<CampoFaltante> P_OUT_REQUISITOS_FALTANTES;

		public SalidaValidarCamposObligatoriosCliente() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_REQUISITOS_FALTANTES = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaValidarCamposObligatoriosCliente entrada = gson.fromJson(in, EntradaValidarCamposObligatoriosCliente.class);
		SalidaValidarCamposObligatoriosCliente salida = logica(conn, entrada);
		return new JsonParser().parse(gson.toJson(salida)).getAsJsonObject();
	}

	private SalidaValidarCamposObligatoriosCliente logica(Connection conn, EntradaValidarCamposObligatoriosCliente entrada) throws Exception {
		logger.info("INICIO::ValidarCamposObligatoriosCliente");
		long inicio = System.currentTimeMillis();

		SalidaValidarCamposObligatoriosCliente salida = new SalidaValidarCamposObligatoriosCliente();
		ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio entradaConsultarFichaSocioNegocio = new ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio();
		entradaConsultarFichaSocioNegocio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarFichaSocioNegocio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarFichaSocioNegocio.entidades = entrada.p_ClienteIds.stream().map(clienteId -> {
			ConsultarFichaSocioNegocio.ClienteEntidad clienteEntidad = new ConsultarFichaSocioNegocio.ClienteEntidad();
			clienteEntidad.IdCliente = clienteId;
			return clienteEntidad;
		}).collect(Collectors.toList());

		ConsultarFichaSocioNegocio.SalidaConsultarFichaSocioNegocio salidaConsultarFichaSocioNegocio = new ConsultarFichaSocioNegocio().logica(conn, entradaConsultarFichaSocioNegocio);
		salida.P_OUT_ERRORESCONTADOR += salidaConsultarFichaSocioNegocio.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(salidaConsultarFichaSocioNegocio.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		ValidarRequisitosEnrolamientoFull.EntradaValidarRequisitosEnrolamientoFull entradaValidarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull.EntradaValidarRequisitosEnrolamientoFull();
		entradaValidarRequisitosEnrolamientoFull.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarRequisitosEnrolamientoFull.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarRequisitosEnrolamientoFull.p_IdModulo = entrada.p_IdModulo;
		entradaValidarRequisitosEnrolamientoFull.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarRequisitosEnrolamientoFull.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaValidarRequisitosEnrolamientoFull.clientes = salidaConsultarFichaSocioNegocio.P_OUT_RESULTADOPROCESO;

		ValidarRequisitosEnrolamientoFull.SalidaValidarRequisitosEnrolamientoFull salidaValidarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull().logica(conn, entradaValidarRequisitosEnrolamientoFull);
		salida.P_OUT_ERRORESCONTADOR += salidaValidarRequisitosEnrolamientoFull.P_OUT_ERRORESCONTADOR;
		AtomicLong idInterno = new AtomicLong(1);
		salida.P_OUT_REQUISITOS_FALTANTES = salidaValidarRequisitosEnrolamientoFull.P_OUT_REQUISITOS_ENR_FULL.stream().map(requisito -> {
			CampoFaltante campoFaltante = new CampoFaltante();
			campoFaltante.IdInterno = idInterno.getAndIncrement();
			campoFaltante.Mensaje = requisito.Descripcion;
			campoFaltante.Ubicacion = requisito.Ubicacion;
			return campoFaltante;
		}).collect(Collectors.toList());

		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarCamposObligatoriosCliente {}", fin - inicio);

		return salida;
	}
}

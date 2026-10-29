package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.TarjetaBancaria;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorTarjetaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorTarjetaBancaria.class);
	
	private String SQL_INSERT_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorTarjetaBancaria_InsertTarjetaBancaria.sql");
	
	public static class EntradaCrearBorradorTarjetaBancaria extends EntradaAdministrarBorrador{
		public List<TarjetaBancaria> p_TarjetasBancarias;
		
		public EntradaCrearBorradorTarjetaBancaria(){
			this.p_TarjetasBancarias = new ArrayList<TarjetaBancaria>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaCrearBorradorTarjetaBancaria.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::CrearBorradorTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_TarjetasBancarias, TarjetaBancaria.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_TarjetasBancarias, "TarjetasBancarias", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<TarjetaBancaria> tarjetasBancarias = obtenerTarjetasBancarias(con, entrada.p_TarjetasBancarias);
		
		insertarTarjetasBancarias(con, tarjetasBancarias);
		
		out.P_OUT_REFID = obtenerIdsInsertados(tarjetasBancarias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorTarjetaBancaria", finServicio - inicioServicio);
		return out;
		
	}
	
	private List<TarjetaBancaria> obtenerTarjetasBancarias(Connection con, List<TarjetaBancaria> borradoresTarjetasBancarias) throws Exception{
		logger.trace("INICIO::CrearBorradorTarjetaBancaria.obtenerTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorTarjetaBancaria", Long.valueOf(borradoresTarjetasBancarias.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<TarjetaBancaria> tarjetasBancarias = borradoresTarjetasBancarias.parallelStream().map(borradorTarjetaBancaria->{
			Long idTarjetaBancaria = secuencias.get(index.getAndIncrement());
			TarjetaBancaria tarjetaBancaria = obtenerEntidadTarjetaBancaria(borradorTarjetaBancaria, idTarjetaBancaria);
			return tarjetaBancaria;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorTarjetaBancaria.obtenerTarjetasBancarias", fin - inicio);
		return tarjetasBancarias;
	}
	
	private TarjetaBancaria obtenerEntidadTarjetaBancaria(TarjetaBancaria borradorTarjetaBancaria, Long idTarjetaBancaria) {
		logger.trace("INICIO::CrearBorradorTarjetaBancaria.obtenerEntidadTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		
		TarjetaBancaria tarjetaBancaria = new TarjetaBancaria();
		tarjetaBancaria.IdTarjetaBancaria = idTarjetaBancaria;
		tarjetaBancaria.IdInterno = borradorTarjetaBancaria.IdInterno;
		tarjetaBancaria.IdEnrolamiento = borradorTarjetaBancaria.IdEnrolamiento;
		tarjetaBancaria.IdBanco = borradorTarjetaBancaria.IdBanco;
		tarjetaBancaria.IdTipoTarjetaBancaria = borradorTarjetaBancaria.IdTipoTarjetaBancaria;
		tarjetaBancaria.NumeroTarjetaBancaria = borradorTarjetaBancaria.NumeroTarjetaBancaria;
		tarjetaBancaria.FechaVencimiento = borradorTarjetaBancaria.FechaVencimiento;
		tarjetaBancaria.CodigoVerificacion = borradorTarjetaBancaria.CodigoVerificacion;
		tarjetaBancaria.IdTarjetaBancariaPersona = borradorTarjetaBancaria.IdTarjetaBancariaPersona;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorTarjetaBancaria.obtenerEntidadTarjetaBancaria", fin - inicio);
		return tarjetaBancaria;
	}
	
	private void insertarTarjetasBancarias(Connection con, List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.debug("INICIO::CrearBorradorTarjetaBancaria.insertarTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_TARJETA_BANCARIA, tarjetasBancarias, TarjetaBancaria.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::CrearBorradorTarjetaBancaria.insertarTarjetasBancarias", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<TarjetaBancaria> tarjetasBancarias){
		logger.trace("INICIO::CrearBorradorTarjetaBancaria.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = tarjetasBancarias.parallelStream().map(tarjetaBancaria->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = tarjetaBancaria.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = tarjetaBancaria.IdTarjetaBancaria;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorTarjetaBancaria.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}

}

package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class CargarDiasInhabiles implements JavaCall{

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		EntradaCargarDiasInhabiles in = gson.fromJson(jin.toString(), EntradaCargarDiasInhabiles.class);
		SalidaCargarDiasInhabiles out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private static final Logger logger = LogManager.getLogger(CargarDiasInhabiles.class);
	
	private final String SQL_SELECT_DIAS_INHABILES = loadResourceToString("cl/bcs/plataforma/pla/sql/CargarDiasInhabiles_SelectDiasInhabiles.sql");
	private final String SQL_UPDATE_DIAS_INHABILES = loadResourceToString("cl/bcs/plataforma/pla/sql/CargarDiasInhabiles_UpdateDiasInhabiles.sql");
	private final String SQL_INSERT_DIAS_INHABILES = loadResourceToString("cl/bcs/plataforma/pla/sql/CargarDiasInhabiles_InsertDiasInhabiles.sql");
	
	private static final Map<Integer, String> DIAS_INHABILES = crearDiasInhabiles();

    private static Map<Integer, String> crearDiasInhabiles() {
        Map<Integer, String> tmp = new HashMap<>();
        tmp.put(5, "Sábado");
        tmp.put(6, "Domingo");
        return Collections.unmodifiableMap(tmp);
    }
	
	public static class EntradaCargarDiasInhabiles extends BcsPojo{
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
	}
	
	public static class SalidaCargarDiasInhabiles extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public SalidaCargarDiasInhabiles() {
        	this.P_OUT_MENSAJES = new ArrayList<>();
        	this.P_OUT_ERRORESCONTADOR = 0L;
        }
	}
	
	public static class DiaFeriado extends BcsPojo{
		public Long IdFeriado;
		public String Tipo;
	    public Integer Dia;
	    public Date Fecha;
	    public Long IdPais;
	    public String DescripcionDia;
	    public String IdEstado;
	    public Integer AgnoCarga;
	    public Long Numero;
	    public DiaFeriado() {
	    	this.Numero = 0L;
	    }
	}
		
	public SalidaCargarDiasInhabiles logica(Connection con, EntradaCargarDiasInhabiles in) throws Exception {
		logger.info("INICIO::CargarDiasInhabiles");
		
		SalidaCargarDiasInhabiles out = new SalidaCargarDiasInhabiles();
		
		SQL sql = new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Long idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		Integer agnoSiguiente;
		try {
			agnoSiguiente = LocalDate.parse(fechaSistema).getYear() + 1;
		}	catch (Exception e) {
	        logger.error("Error al calcular año siguiente", e);
	        out.P_OUT_ERRORESCONTADOR++;
	        out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_AGNO_SIGUIENTE", new HashMap<>(), con, in.p_IdEmpresa));
	        return out;
		}
		
		List<DiaFeriado> listaDiasInhabiles;
		
		try {
			listaDiasInhabiles = buscarDiasInhabiles(con, sql, agnoSiguiente, idPais);
		} catch (Exception e) {
			logger.error("Error al buscar días inhábiles", e);
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_CONSULTA_DIAS_INHABILES", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		List<DiaFeriado> listaFeriadosPorInsertar = new ArrayList<>();
		List<DiaFeriado> listaFeriadosPorActualizar = new ArrayList<>();
		
		if(listaDiasInhabiles.isEmpty()) {
			listaFeriadosPorInsertar.add(crearDiaInhabil(5, agnoSiguiente, idPais,1L));
			listaFeriadosPorInsertar.add(crearDiaInhabil(6, agnoSiguiente, idPais,2L));	
		}else {
			procesarDia(listaDiasInhabiles, listaFeriadosPorInsertar, listaFeriadosPorActualizar, 5, agnoSiguiente, idPais,1L);
			procesarDia(listaDiasInhabiles, listaFeriadosPorInsertar, listaFeriadosPorActualizar, 6, agnoSiguiente, idPais,2L);
		}
		
		if(!listaFeriadosPorInsertar.isEmpty()) {
			try {
				insertarDiasInhabiles(con, sql, listaFeriadosPorInsertar);
				logger.info(String.format("Se insertaron %d días inhábiles", listaFeriadosPorInsertar.size()));
		    } catch (Exception e) {
		        logger.error("Error al insertar días inhábiles", e);
		        out.P_OUT_ERRORESCONTADOR++;
		        out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_INSERTAR_DIAS_INHABILES", new HashMap<>(), con, in.p_IdEmpresa));
		    }
		}
		
		if(!listaFeriadosPorActualizar.isEmpty()) {
			try {
				actualizarDiasInhabiles(con, sql, listaFeriadosPorActualizar);
				logger.info(String.format("Se actualizaron %d días inhábiles", listaFeriadosPorActualizar.size()));
	        } catch (SQLException e) {
	            logger.error("Error al actualizar días inhábiles", e);
	            out.P_OUT_ERRORESCONTADOR++;
	            out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_ACTUALIZAR_DIAS_INHABILES", new HashMap<>(), con, in.p_IdEmpresa));
	        }
		}
		
		logger.info("FIN::CargarDiasInhabiles");
		return out;
		
	}
	
	private List<DiaFeriado> buscarDiasInhabiles(Connection con, SQL sql, Integer agnoCarga, Long idPais) throws SQLException{
		logger.debug("INICIO::CargarDiasInhabiles.buscarDiasInhabiles");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> param = new HashMap<>(2);
		param.put("agnoCarga", agnoCarga);
		param.put("idPais", idPais);
		List<DiaFeriado> listaDiasInhabiles = sql.query(con, SQL_SELECT_DIAS_INHABILES, param, DiaFeriado.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarDiasInhabiles.buscarDiasInhabiles tiempo: " + (fin - inicio));
		return listaDiasInhabiles;
	}
	
	private void insertarDiasInhabiles(Connection con, SQL sql, List<DiaFeriado> listaFeriadosPorInsertar) throws SQLException {
		logger.debug("INICIO::CargarDiasInhabiles.insertarDiasInhabiles");
		long inicio = System.currentTimeMillis();
		
		sql.batch(con, SQL_INSERT_DIAS_INHABILES, listaFeriadosPorInsertar, DiaFeriado.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarDiasInhabiles.insertarDiasInhabiles tiempo: " + (fin - inicio));
	}
	
	private void actualizarDiasInhabiles(Connection con, SQL sql, List<DiaFeriado> listaFeriadosPorActualizar) throws SQLException {
		logger.debug("INICIO::CargarDiasInhabiles.actualizarDiasInhabiles");
		long inicio = System.currentTimeMillis();
		
		sql.batch(con, SQL_UPDATE_DIAS_INHABILES, listaFeriadosPorActualizar, DiaFeriado.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarDiasInhabiles.actualizarDiasInhabiles tiempo: " + (fin - inicio));
	}
	
	private DiaFeriado crearDiaInhabil(Integer dia, Integer agnoCarga, Long idPais, Long numero) {
		logger.trace("INICIO::CargarDiasInhabiles.crearDiaInhabil");
		long inicio = System.currentTimeMillis();
		
		DiaFeriado diaInhabil = new DiaFeriado();
		diaInhabil.Dia = dia;
		diaInhabil.Tipo = "INHABIL";
		diaInhabil.DescripcionDia = DIAS_INHABILES.getOrDefault(dia, "");
		diaInhabil.Fecha = Fecha.toDate("0001-01-01");
		diaInhabil.IdEstado = "ACT";
		diaInhabil.IdPais = idPais;
		diaInhabil.AgnoCarga = agnoCarga;
		diaInhabil.Numero = numero;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CargarDiasInhabiles.crearDiaInhabil tiempo: " + (fin - inicio));
		return diaInhabil;
	}
		
	private void procesarDia(List<DiaFeriado> existentes, List<DiaFeriado> listaFeriadosPorInsertar, List<DiaFeriado> listaFeriadosPorActualizar, Integer diaNumero, Integer agnoSiguiente, Long idPais, Long numeroRegistro) {
		logger.trace("INICIO::CargarDiasInhabiles.procesarDia");
		long inicio = System.currentTimeMillis();
	    
		DiaFeriado dia = existentes.stream().filter(d -> d.Dia == diaNumero).findFirst().orElse(null);
	    if (dia == null) {
	    	listaFeriadosPorInsertar.add(crearDiaInhabil(diaNumero, agnoSiguiente, idPais, numeroRegistro));
	    } else if ("INA".equalsIgnoreCase(dia.IdEstado)) {
	    	listaFeriadosPorActualizar.add(dia);
	    }
	    
	    long fin = System.currentTimeMillis();
	    logger.trace("FIN::CargarDiasInhabiles.procesarDia tiempo: " + (fin - inicio));
	}

}

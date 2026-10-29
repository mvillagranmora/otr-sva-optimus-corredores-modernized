package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarInstrumento implements JavaCall {

	private final String SQL_CONSULTAR_INSTRUMENTO = loadResourceToString("cl/bcs/mesa/sql/ConsultarInstrumento.sql");
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<InformacionInstrumento> logica(Connection conn, List<Long> IdInstrumento) throws Exception {
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdInstrumento", IdInstrumento);
		
		List<InformacionInstrumento> instrumentos = _sql.query(conn, SQL_CONSULTAR_INSTRUMENTO, params, InformacionInstrumento.class);
		if (instrumentos.size() == 0)
			throw new Exception("instrumento no existe");
		return instrumentos;
	}
	
	public static class InformacionInstrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String ISIN;
		public BigDecimal CorteMinimo;
		public String EstaInscrito;
		public String EsDivisible;
		public String RazonSocial;
		public String Lista;
		public Date FechaEmision;
		public Date FechaVencimiento;
		public Long IdInstrumentoRelacionado;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String Reajustable;
		public String IdMoneda;
		public String IdEstado;
		public int DecimalesPrecio;
		public String IdTipoInstrumento;
		public String CodigoTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String CodigoClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String DocumentoIdentidadEmisor; //RutEmisor
		public String NombreCompletoEmisor;//RazonSocialEmisor
		public String IdTipoOperacion;
		public String Accion;
		public String NombreTipoOperacion;
		public Long Tamano;
		public String IdEntidadClasificadoraRiesgo;
		public String ValorClasificacionRiesgo;
		public String IdTipoClasificacionRiesgo;
		public String TipoValorizacion;
		public BigDecimal Tasa;
		public BigDecimal Precio;
		public String ValorizaPor;
		public String TieneCupon;
		public int NumeroCupones;
		public String IndicadorBolson;
	}
	
}
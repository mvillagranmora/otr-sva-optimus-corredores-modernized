package cl.bcs.normativa.carga;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public final class CargaDJ1Pagos implements JavaCall {
	
	private final String SQL_SELECT_PERIODO =  loadResourceToString("cl/bcs/normativa/carga/sql/CargaDJ1Pagos_SelectPeriodoComercial.sql");
	private final String SQL_SELECT_ANT =  loadResourceToString("cl/bcs/normativa/carga/sql/CargaDJ1Pagos_SelectAnticipados.sql");
	private final String SQL_SELECT_VCD =  loadResourceToString("cl/bcs/normativa/carga/sql/CargaDJ1Pagos_SelectVencidos.sql");
	private final String SQL_SELECT_DUP =  loadResourceToString("cl/bcs/normativa/carga/sql/CargaDJ1Pagos_SelectDuplicado.sql");
	private final String SQL_SELECT_REG =  loadResourceToString("cl/bcs/normativa/carga/sql/CargaDJ1Pagos_SelectRegistro.sql");
	private final String SQL_DELETE_PAGO =  loadResourceToString("cl/bcs/normativa/carga/sql/CargaDJ1Pagos_DeletePago.sql");
	private final String SQL_INSERT_PAGO =  loadResourceToString("cl/bcs/normativa/carga/sql/CargaDJ1Pagos_InsertDatos.sql");

	public static class CargaDJ1Pagos_Entrada
	{
		public Long idEmpresa;
		public Long idDocumento;
		public Long idUsuario;
		public String schema;
		public String grabar;
		public String reemplazar;
		public String aceptarFechasNull;
		public String tipoCarga;
		public String fechaDesde;
		public String fechaHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegistroDJ1
	{
		public Long ID;
		public Long IdDocumento;
		public String KRutCtraparte1;
		public Long KIdContrato;
		public String KFechaContrato;
		public String HoraContrato;
		public Long KIdOpeEstruc;
		public Long KS5_CorrPago;
		public String KS5_IdPago;
		public String S5_FecPago;
		public BigDecimal S5_MontoPago;
		public String S5_TipoPago;
		public String S5_CodMoneda;
		public String kProducto;
		public String kTipoArchivo;
		public String kPeriodoComercial;
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class VersionDJ1
	{
		public String Periodo;
		public String FechaDesde;
		public String FechaHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratoDJ1
	{
		public Long ID;
		public Long IdContrato;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public boolean logica(Connection con, CargaDJ1Pagos_Entrada in) throws Exception {
		
		final Logger log = Logger.getLogger(CargaDJ1Pagos.class.getName());
		
		log.info("== INICIO CARGA DJ1 PAGOS  ==");
		log.info("ENTRADA > " + in.toString());
		
		List<VersionDJ1> Periodo = new ArrayList<>();
		HashMap<String,Object> params = new HashMap<>();
		params.put("idDocumento", in.idDocumento);
		
		SQL _sql = new SQL();
		
		// obtener periodo desde idDocumento
		Periodo = _sql.query(con, SQL_SELECT_PERIODO, params, VersionDJ1.class);
		final String periodo = Periodo.get(0).Periodo;
		
		if (!in.fechaDesde.equals("0001-01-01")) {
			Periodo.get(0).FechaDesde = in.fechaDesde;
		}
		if (!in.fechaHasta.equals("0001-01-01")) {
			Periodo.get(0).FechaHasta = in.fechaHasta;
		}
		
		List<RegistroDJ1> RegistroDJ1_Ant = new ArrayList<>();
		List<RegistroDJ1> RegistroDJ1_Vcd = new ArrayList<>();
		params.clear();
		params.put("periodo", periodo);
		params.put("FechaDesde", Periodo.get(0).FechaDesde);
		params.put("FechaHasta", Periodo.get(0).FechaHasta);
		
		RegistroDJ1_Ant = _sql.query(con, SQL_SELECT_ANT, params, RegistroDJ1.class);
		List<RegistroDJ1> Anticipados =  RegistroDJ1_Ant.stream()
				.map(e-> {
				    e.IdDocumento = in.idDocumento;
				    return e;
				}).collect(Collectors.toList());
		
		RegistroDJ1_Vcd = _sql.query(con, SQL_SELECT_VCD, params, RegistroDJ1.class);
		List<RegistroDJ1> Vencidos =  RegistroDJ1_Vcd.stream()
				.map(e-> {
				    e.IdDocumento = in.idDocumento;
				    return e;
				}).collect(Collectors.toList());
		
		// Busca pagos ya insertados
		List<ContratoDJ1> ContratoDJ1_Pago = new ArrayList<>();
		ContratoDJ1_Pago = _sql.query(con, SQL_SELECT_DUP, params, ContratoDJ1.class);
		
		List<Long> IdsContrato =  new ArrayList<>();
		ContratoDJ1_Pago.forEach(e -> {
			if (!IdsContrato.contains(e.IdContrato))
				IdsContrato.add(e.IdContrato);
		});
		
		List<RegistroDJ1> DatosPago = Stream.concat(Anticipados.stream(), Vencidos.stream()).collect(Collectors.toList());
		if (IdsContrato.size()>0){
			_sql.batch(con, SQL_DELETE_PAGO, ContratoDJ1_Pago, ContratoDJ1.class);
		}
		IdsContrato.clear();
		
		// Busca registros actuales del periodo
		params.clear();
		params.put("IdDocumento", in.idDocumento);
		params.put("FechaDesde", Periodo.get(0).FechaDesde);
		params.put("FechaHasta", Periodo.get(0).FechaHasta);
		List<ContratoDJ1> ContratoDJ1_Reg = new ArrayList<>();
		ContratoDJ1_Reg = _sql.query(con, SQL_SELECT_REG, params, ContratoDJ1.class);
		ContratoDJ1_Reg.forEach(e -> {
			if (!IdsContrato.contains(e.IdContrato))
				IdsContrato.add(e.IdContrato);
		});
		
		// Evita insertar pagos a contratos no cargados
		DatosPago = DatosPago.stream()
			.filter(e-> IdsContrato.contains(e.KIdContrato))
			.collect(Collectors.toList());
		
		for (int i=0; i<DatosPago.size(); i++) {
			DatosPago.get(i).ID = (long)i;
		}
		
		if (DatosPago.size()>0) {
			_sql.batch(con, SQL_INSERT_PAGO, DatosPago, RegistroDJ1.class);	
		}
		
		log.info("== TERMINO CARGA DJ1 PAGOS ==");
		
		return true;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
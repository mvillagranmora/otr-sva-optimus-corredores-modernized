package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FlujoPatrimonial implements BcsCartola{
	
	public final Log log = new Log("NOR.FlujoPatrimonial");
	
	private final String SQL_SELECT_TESORERIA = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoPatrimonial_SelectTesoreria.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class SalidaFlujoPatrimonial{
		public Long IdCliente;
		public Long IdPortafolio;
		public Timestamp fecha;
		public BigDecimal retirosClp;
		public BigDecimal abonosClp;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::FlujoPatrimonial");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		List<Portafolio> portObj = new ArrayList<>();

		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Long> portafolios = new ArrayList<Long>();
		for(Dato dato : entrada.p_Datos){
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
			
		}
		
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("IdMoneda", monedaSistema);
		List<SalidaFlujoPatrimonial> salidas = sql.query(con, SQL_SELECT_TESORERIA, params, SalidaFlujoPatrimonial.class);
		
		List<HashMap<String,Object>> resultadoFlujoPatrimonial = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("flujoPatrimonial", resultadoFlujoPatrimonial);
		
		log.write("FIN::FlujoPatrimonial");
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-09-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-09-13");
		Dato dato = new Dato();
		dato.IdCliente = 100350L;
		dato.IdPortafolio = 100586L;
		Objin.p_Datos.add(dato);
		FlujoPatrimonial  demo= new FlujoPatrimonial();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}

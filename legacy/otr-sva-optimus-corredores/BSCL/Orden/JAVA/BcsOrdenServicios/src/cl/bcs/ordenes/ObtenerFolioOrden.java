package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;

public class ObtenerFolioOrden {
	
	public static FileLoader loader = new FileLoader();
	private static final String SQL_SELECT_ORDENES = loader.loadResourceToString("cl/bcs/ordenes/sql/ObtenerFolioOrden_SelectOrdenes.sql");
	public static HashMap<String,FolioDia> ultimosFolios = new HashMap<>();;
	
	public static class FolioDia {
		public Long FormadorMercado; //6
		public Long OrdenEspecial;	//5
		public Long OrdenMila;		//4
		//public Long OrdenRuteo2;	//3 no se usa
		public Long OrdenRuteo1;	//2
		public Long OrdenOrdinaria;	//1
	}
	
	public static synchronized FolioDia actualizarFolios(Connection conn,Long idEmpresa,String fechaOrden) throws Exception {
		
		Long localOffset = ConfiguracionSistema.getParametroConfiguracionLong(conn, idEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
		
		Fecha.setLocalOffSet(localOffset.intValue());
		
		java.sql.Timestamp date1 = Fecha.toDateTime(fechaOrden + " 00:00:00");
		String fechaDesde = Fecha.toStringDatetime(Fecha.toUTCTimeStamp(date1));
		java.sql.Timestamp fechaDesdeTs = Fecha.toTimeStamp(fechaDesde);
		
		java.sql.Timestamp date2 = Fecha.addDays(fechaDesdeTs, 1);
		String fechaHasta = Fecha.toStringDatetime(date2);
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("fechaOperacionDesde", fechaDesde);
		params.put("fechaOperacionHasta", fechaHasta);
		List<Orden> ordenesDia = _sql.query(conn, SQL_SELECT_ORDENES, params , Orden.class);
		
		FolioDia f = new FolioDia();
		f.FormadorMercado = 600001L;
		f.OrdenEspecial = 500001L;
		f.OrdenMila = 400001L;
		f.OrdenRuteo1 = 200001L;
		f.OrdenOrdinaria = 100001L;
		
		ultimosFolios.put(fechaOrden, f);
		
		for (Orden ord : ordenesDia) {
			
			if (ord.Folio != null && !ord.Folio.equalsIgnoreCase("") && ord.Folio.length() == 6) {
				
				Long folioCompleto = -1L;
				try {
					folioCompleto = Long.parseLong(ord.Folio);
				}
				catch(Exception e) {
					
				}
				
				if (folioCompleto == -1L) {
					continue;
				}
				
				Long tipoFolio = folioCompleto / 100000;
				
				if (tipoFolio == 6) {
					f.FormadorMercado = folioCompleto + 1;
				}
				else if (tipoFolio == 5) {
					f.OrdenEspecial = folioCompleto + 1;
				}
				else if (tipoFolio == 4) {
					f.OrdenMila = folioCompleto + 1;
				}
				else if (tipoFolio == 2) {
					f.OrdenRuteo1 = folioCompleto + 1;
				}
				else if (tipoFolio == 1) {
					f.OrdenOrdinaria = folioCompleto + 1;
				}
				else {
					//ERROR
				}
				
			}
		}
		
		return f;
		
		
	}
	
	public synchronized String Obtener(Connection conn,Long idEmpresa,Orden orden) throws Exception {
		
		String fecha = Fecha.toStringDate(orden.FechaOperacion);
		
		FolioDia folios = ultimosFolios.get(fecha);
		if (folios == null) {
			folios = actualizarFolios(conn,idEmpresa,fecha);
			
		}
		
		Long base = 0L;
		if("TRA".equalsIgnoreCase(orden.IdTipoCanalIngreso) && "S".equalsIgnoreCase(orden.FormadorMercado)){ 
			base = folios.FormadorMercado++;
		}
		else if("S".equalsIgnoreCase(orden.EsOrdenEspecial)){
			base = folios.OrdenEspecial++;
		}
		else if("BCS".equalsIgnoreCase(orden.IdBolsa) ||"BVC".equalsIgnoreCase(orden.IdBolsa) ||"BMV".equalsIgnoreCase(orden.IdBolsa)) {
			base = folios.OrdenMila++;
		}
		else if(!"MAN".equalsIgnoreCase(orden.IdTipoCanalDistribucion)){
			base = folios.OrdenRuteo1++;
		}
		else {
			base = folios.OrdenOrdinaria++;
		}
		
		return base+"";
	}
	
		
	public static void main(String[] args)  throws Exception {
		try (Connection con = PlataformaUtils.getMBPEConnection()) {
			ObtenerFolioOrden f= new ObtenerFolioOrden();
			Orden o = new Orden();
			o.FechaOperacion = Fecha.toDateTime("2018-04-10 01:00:00");
			f.Obtener(con, 1L, o);
		 	
			System.out.println("== END ==");
		}
	};
}

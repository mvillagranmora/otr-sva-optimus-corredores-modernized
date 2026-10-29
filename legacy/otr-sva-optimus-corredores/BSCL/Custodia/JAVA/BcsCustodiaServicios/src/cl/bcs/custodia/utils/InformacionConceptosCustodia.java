package cl.bcs.custodia.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.SQL;

public class InformacionConceptosCustodia {	
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SelectConceptosCustodia = loader.loadResourceToString("cl/bcs/custodia/sql/InformacionConceptosCustodia_SEL_ConceptoCustodia.sql");
	
	
	public static class ObjDatosEntrada {
		public String	IdConcepto;
		public Long		IdTipoOperacion;
		public Long		IdTipoInstrumento;
		public Long		IdClasificacionTipoInstrumento;
		public Long		IdInstrumento;
		public String	IdNombreEntidad;
		
		public ObjDatosEntrada() {
			IdNombreEntidad = "CUS.INGRESORETIROTITULO";//se deja por defecto...pero deberia ser enviado por el que llama esta clase
		}
				
	};
	public static class ObjDatosSalida {
		public String	IdConcepto;
		public String	ConCertificadoAutomatico; 
		public String	GeneraMovimientoEnTransito;
		public String	EsTransferenciaBoveda;
		public String	EsTransferenciaCliente;
		public String	EsTransferenciaPortafolio;
		public String	EsGarantiaPrendaria;		
		public String	RetieneDividendos;
		public String	RetieneAmortizacion;
		public String	RetieneIntereses;
		public String	FolioObligatorio;
	};

	public static List<ObjDatosSalida> informacionConcepto(Connection conn,List<ObjDatosEntrada> conceptosConsulta) throws SQLException{
		List<ObjDatosSalida> ConceptosCustodia = new ArrayList<ObjDatosSalida>();
		SQL sql = new SQL();
		
		try {
			HashMap<String,Object> params = new HashMap<>();
			List<String> IdConceptos = new ArrayList<String>();
			List<Long> IdTipoOperacion = new ArrayList<Long>();	
			List<Long> IdTipoInstrumento = new ArrayList<Long>();
			List<Long> IdClasificacionTipoInstrumento = new ArrayList<Long>();
			List<Long> IdInstrumento = new ArrayList<Long>();
			List<String> IdNombreEntidad= new ArrayList<String>();
			
			for (ObjDatosEntrada det: conceptosConsulta){
				if (!IdConceptos.contains(det.IdConcepto)) {		//PARA QUE NO SE REPITAN
					IdConceptos.add( det.IdConcepto);
				};
				if (det.IdTipoOperacion != null && !IdTipoOperacion.contains(det.IdTipoOperacion)) {
					IdTipoOperacion.add( det.IdTipoOperacion);
				};
				if (det.IdTipoInstrumento != null && !IdTipoInstrumento.contains(det.IdTipoInstrumento)) {
					IdTipoInstrumento.add( det.IdTipoInstrumento);
				};
				if (det.IdClasificacionTipoInstrumento != null && !IdClasificacionTipoInstrumento.contains(det.IdClasificacionTipoInstrumento)) {
					IdClasificacionTipoInstrumento.add( det.IdClasificacionTipoInstrumento);
				};
				if (det.IdInstrumento != null && !IdInstrumento.contains(det.IdInstrumento)) {
					IdInstrumento.add( det.IdInstrumento);
				};
				if (det.IdNombreEntidad != null && !IdNombreEntidad.contains(det.IdNombreEntidad)) {
					IdNombreEntidad.add( det.IdNombreEntidad);
				};
			};
			
			if (IdConceptos.size() > 0 ){				
				params.put("FiltroTipoOperacionValido", 0);
				params.put("FiltroTipoInstrumentoValido", 0);
				params.put("FiltroClasificacionTipoInstrumentoValido", 0);
				params.put("FiltroInstrumentoValido", 0);
				
				if (IdTipoOperacion.size() == 0) {
					IdTipoOperacion.add(-1L); 
					params.put("FiltroTipoOperacionValido", 1);
				};
				if (IdTipoInstrumento.size() == 0) {
					IdTipoInstrumento.add(-1L); 
					params.put("FiltroTipoInstrumentoValido", 1);
				};				
				if (IdClasificacionTipoInstrumento.size() == 0) {
					IdClasificacionTipoInstrumento.add(-1L); 
					params.put("FiltroClasificacionTipoInstrumentoValido", 1);
				};
				if (IdInstrumento.size() == 0) {
					IdInstrumento.add(-1L); 
					params.put("FiltroInstrumentoValido", 1);
				};
								
				params.put("IdConcepto", IdConceptos);
				params.put("IdTipoOperacion", IdTipoOperacion);
				params.put("IdTipoInstrumento", IdTipoInstrumento);
				params.put("IdClasificacionTipoInstrumento", IdClasificacionTipoInstrumento);
				params.put("IdInstrumento", IdInstrumento);
				params.put("IdNombreEntidad",IdNombreEntidad);
				ConceptosCustodia = sql.query(conn, SQL_SelectConceptosCustodia, params, ObjDatosSalida.class);
			};
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return ConceptosCustodia;
	};	
};

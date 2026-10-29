package cl.bcs.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import datadog.trace.api.Trace;

public class AgregarNotificacion {
	
	private Logger log = LogManager.getLogger(AgregarNotificacion.class);
	
	public static class EntradaAgregarNotificacion {
		public Long P_IDEMPRESA;
		public Long P_IDUSUARIO;
		public String P_IDMODULO;
		public List<Evento> P_EVENTOS;
		public List<ValorEvento> P_VALORES;
		public List<ArchivoAdjuntoEvento> P_ARCHIVOSADJUNTOS;
		public List<DireccionEvento> P_IDDIRECCIONES;

		public EntradaAgregarNotificacion() {
			P_ARCHIVOSADJUNTOS = new ArrayList<>();
			P_IDDIRECCIONES = new ArrayList<>();
			P_VALORES = new ArrayList<>();
			P_EVENTOS = new ArrayList<>();
		}

		public EntradaAgregarNotificacion addValor(Long idSecuencia, String llave, String valor) {
			ValorEvento obj = new ValorEvento();
			obj.IdSecuencia = idSecuencia;
			obj.Llave = llave;
			obj.Valor = valor;
			this.P_VALORES.add(obj);
			return this;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaAgregarNotificacion {
		public List<IdentificadorEvento> P_OUT_IDEVENTO;

		public SalidaAgregarNotificacion() {
			P_OUT_IDEVENTO = new ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Evento {
		public String CodigoTipoEvento;
		public Long IdSecuencia;
		public String FechaCreacionSistema;
		public Long IdPersonaRemitente;
		public Long IdPersonaDestinatario;
		public Long IdPortafolio;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ValorEvento {
		public Long IdSecuencia;
		public String Llave;
		public String Valor;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ArchivoAdjuntoEvento {
		public Long IdSecuencia;
		public Byte[] ArchivoAdjunto;
		public String Nombre;
		public String Contrasenia;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DireccionEvento {
		public Long IdSecuencia;
		public Long IdDireccion;
		public String TipoDestinatario;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class IdentificadorEvento {
		public Long IdEvento;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Trace
	public SalidaAgregarNotificacion logica(Connection con, EntradaAgregarNotificacion in) throws Exception {
		Long init = System.currentTimeMillis();
		SQL sql=new SQL();				
		SalidaAgregarNotificacion out=new SalidaAgregarNotificacion();		
		out.P_OUT_IDEVENTO = new ArrayList<>();
		log.info("Inicio servicio AgregarEvento");
		
		try {
			//------------------------------
			//Crea las tablas temporales
			
			SQL.exec(con,"CREATE LOCAL TEMPORARY TABLE #AgregarEvento_P_EVENTOS (\"CodigoTipoEvento\" NVARCHAR(40),\"IdSecuencia\" BIGINT,\"FechaCreacionSistema\" SECONDDATE,\"IdPersonaRemitente\" BIGINT,\"IdPersonaDestinatario\" BIGINT,\"IdPortafolio\" BIGINT)");
			SQL.exec(con,"CREATE LOCAL TEMPORARY TABLE #AgregarEvento_P_VALORES (\"IdSecuencia\" BIGINT,\"Llave\" NVARCHAR(40),\"Valor\" NVARCHAR(5000))");
			SQL.exec(con,"CREATE LOCAL TEMPORARY TABLE #AgregarEvento_P_ARCHIVOSADJUNTOS (\"IdSecuencia\" BIGINT,\"ArchivoAdjunto\" BLOB,\"Nombre\" NVARCHAR(520),\"Contrasenia\" NVARCHAR(520))");
			SQL.exec(con,"CREATE LOCAL TEMPORARY TABLE #AgregarEvento_P_IDDIRECCIONES (\"IdSecuencia\" BIGINT,\"IdDireccion\" BIGINT,\"TipoDestinatario\" NVARCHAR(6))");

			//------------------------------
			//Inserta tablas temporales globales
			sql.batch(con,"INSERT INTO #AgregarEvento_P_EVENTOS (\"CodigoTipoEvento\",\"IdSecuencia\",\"FechaCreacionSistema\",\"IdPersonaRemitente\",\"IdPersonaDestinatario\",\"IdPortafolio\") VALUES (:CodigoTipoEvento,:IdSecuencia,:FechaCreacionSistema,:IdPersonaRemitente,:IdPersonaDestinatario,:IdPortafolio)", in.P_EVENTOS,Evento.class);
			sql.batch(con,"INSERT INTO #AgregarEvento_P_VALORES (\"IdSecuencia\",\"Llave\",\"Valor\") VALUES (:IdSecuencia,:Llave,:Valor)", in.P_VALORES,ValorEvento.class);
			sql.batch(con,"INSERT INTO #AgregarEvento_P_IDDIRECCIONES (\"IdSecuencia\",\"IdDireccion\",\"TipoDestinatario\") VALUES (:IdSecuencia,:IdDireccion,:TipoDestinatario)", in.P_IDDIRECCIONES,DireccionEvento.class);
			
			// INSERTAR ARCHIVOS ADJUNTOS - INICIO
			String insertarAdjuntosTemp = "INSERT INTO #AgregarEvento_P_ARCHIVOSADJUNTOS (\"IdSecuencia\",\"ArchivoAdjunto\",\"Nombre\",\"Contrasenia\") VALUES (?, ?, ?, ?)";
			PreparedStatement pstmt = con.prepareStatement(insertarAdjuntosTemp);
			for (ArchivoAdjuntoEvento adj: in.P_ARCHIVOSADJUNTOS) {
				pstmt.setLong(1, adj.IdSecuencia);
				pstmt.setBytes(2, ArrayUtils.toPrimitive(adj.ArchivoAdjunto));
				pstmt.setNString(3, adj.Nombre);
				pstmt.setNString(4, adj.Contrasenia);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			pstmt.close();
			// INSERTAR ARCHIVOS ADJUNTOS - FIN
			
			//Ejecuta el SP
			HashMap<String,Object> parmsIN=new HashMap<>();
			
			parmsIN.put("P_IDEMPRESA", in.P_IDEMPRESA);
			parmsIN.put("P_IDUSUARIO", in.P_IDUSUARIO);
			parmsIN.put("P_IDMODULO", in.P_IDMODULO);
			parmsIN.put("P_EVENTOS", in.P_EVENTOS);
			parmsIN.put("P_VALORES", in.P_VALORES);
			parmsIN.put("P_ARCHIVOSADJUNTOS", in.P_ARCHIVOSADJUNTOS);
			parmsIN.put("P_IDDIRECCIONES", in.P_IDDIRECCIONES);
			
			HashMap<String,Class> parmsOUT=new HashMap<>();
			parmsOUT.put("P_OUT_IDEVENTO",IdentificadorEvento.class);
			
			String queryCall="call \"BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,#AgregarEvento_P_EVENTOS,#AgregarEvento_P_VALORES,#AgregarEvento_P_ARCHIVOSADJUNTOS,#AgregarEvento_P_IDDIRECCIONES,'',$P_OUT_IDEVENTO)";
			HashMap<String,Object> rpt=sql.call(con, queryCall, parmsIN, parmsOUT);
			
			
			out.P_OUT_IDEVENTO = (List<IdentificadorEvento>) rpt.get("P_OUT_IDEVENTO");
		
		}finally {
			//Elimina tablas temporales locales			
			
			try { SQL.exec(con,"DROP TABLE #AgregarEvento_P_EVENTOS"); } catch (Exception er) { }
			try { SQL.exec(con,"DROP TABLE #AgregarEvento_P_VALORES"); } catch (Exception er) { }
			try { SQL.exec(con,"DROP TABLE #AgregarEvento_P_ARCHIVOSADJUNTOS"); } catch (Exception er) { }
			try { SQL.exec(con,"DROP TABLE #AgregarEvento_P_IDDIRECCIONES"); } catch (Exception er) { }
		}
		
		log.info("Termino del servicio AgregarEvento, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}

}

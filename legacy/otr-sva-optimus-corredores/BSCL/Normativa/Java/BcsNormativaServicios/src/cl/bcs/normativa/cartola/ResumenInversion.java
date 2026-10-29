package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.normativa.cartola.ResumenInversionBase.CarteraResumenInversion;
import cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion;
import cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.Totales;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ResumenInversion implements BcsCartola{
	
	private static final Logger logger = LogManager.getLogger(ResumenInversion.class);
	private final String SQL_SELECT_CLASIFICACION_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversion_SelectClasificacionTipoInstrumento.sql");
	private final String SQL_SELECT_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversion_SelectTipoInstrumento.sql");
	
	
	public boolean obtenerPactos;
	
	
	public ResumenInversion() {
		obtenerPactos = false;
	}
	
	public static class ResumenInversionSalida{
		public Long IdCliente;
		public Long IdPortafolio;
		public String Grupo;
		public String DescripcionGrupo;
		public String Item;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public BigDecimal TotalGrupoMonedaSistema;
		public BigDecimal TotalMonedaSistema;
		public String IdMonedaAdicional;
		public BigDecimal MontoMonedaAdicional;
		public BigDecimal TotalGrupoMonedaAdicional;
		public BigDecimal TotalMonedaAdicional;
		public String MonedaResumenInversion;
		public String IdMonedaOperacion;
		public BigDecimal MontoMonedaOperacion;
		public BigDecimal TotalGrupoMonedaOperacion;
		public BigDecimal TotalMonedaOperacion;
//Formato
//		public String roundMont;		
		
		
		public ResumenInversionSalida() {
			this.MontoMonedaSistema = BigDecimal.ZERO;
			this.TotalGrupoMonedaSistema = BigDecimal.ZERO;
			this.TotalMonedaSistema = BigDecimal.ZERO;
			this.MontoMonedaAdicional = BigDecimal.ZERO;
			this.TotalGrupoMonedaAdicional = BigDecimal.ZERO;
			this.TotalMonedaAdicional = BigDecimal.ZERO;
			this.MontoMonedaOperacion = BigDecimal.ZERO;
			this.TotalGrupoMonedaOperacion = BigDecimal.ZERO;
			this.TotalMonedaOperacion = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClasificacionTipoInstrumento {
		public String IdClasificacionTipoInstrumento;
		public String Nombre;
	}
	
	public static class TipoInstrumento {
		public String IdTipoInstrumento;
		public String Nombre;
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::ResumenInversion");
		SalidaCartola out = new SalidaCartola();
		
		Map<String, ClasificacionTipoInstrumento> clasificacionInstrumentosPorIdClasificacionInstrumento = obtenerClasificacionInstrumentosPorIdClasificacionInstrumento(con);
		Map<String, TipoInstrumento> tipoInstrumentosPorIdTipoInstrumento = obtenerTipoInstrumentosPorIdTipoInstrumento(con);
		
		ResumenInversionBase resumenInversionBase = new ResumenInversionBase();
		EntradaResumenInversionBase entradaResumenInversionBase = new EntradaResumenInversionBase();
		entradaResumenInversionBase.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaResumenInversionBase.p_IdUsuario = entrada.p_IdUsuario;
		entradaResumenInversionBase.p_FechaDesde = entrada.p_FechaDesde;
		entradaResumenInversionBase.p_FechaHasta = entrada.p_FechaHasta;
		entradaResumenInversionBase.p_IdModulo = entrada.p_IdModulo;
		entradaResumenInversionBase.p_Datos = entrada.p_Datos;
		entradaResumenInversionBase.p_IdTipoCanal = entrada.p_IdTipoCanal;
		
		SalidaResumenInversionBase salidaResumenInversionBase = resumenInversionBase.logica(con, entradaResumenInversionBase);
		
		if(obtenerPactos) {
			salidaResumenInversionBase.p_out_ResumenInversion = agregarPactosYSubyacentes(salidaResumenInversionBase.p_out_ResumenInversion);
		}
		
		if("NOR".equals(entrada.p_IdModulo)) {
			for(OutResumenInversion res : salidaResumenInversionBase.p_out_ResumenInversion) {
				if("FM".equals(res.Grupo)) {
					res.Item = WordUtils.capitalizeFully(res.Item, new char[]{' '});
					res.Item = res.Item +  ' ' + res.IdMonedaOperacion;
				}
			}
		}
		HashMap<String,ResumenInversionSalida> hSalidaResumenInversion = new HashMap<String,ResumenInversionSalida>();
		HashMap<String,List<OutResumenInversion>> hSalidaResumenesInversion = new HashMap<String,List<OutResumenInversion>>();
		
		for(OutResumenInversion res : salidaResumenInversionBase.p_out_ResumenInversion) {
			String id = res.IdCliente + "_" + res.IdPortafolio + "_" + res.Grupo + "_" + res.Item;
			String idResumenes = res.IdCliente + "_" + res.IdPortafolio;
			ResumenInversionSalida resumenInversionSalida = hSalidaResumenInversion.get(id);
			if(resumenInversionSalida == null) {
				resumenInversionSalida = new ResumenInversionSalida();
				resumenInversionSalida.IdCliente = res.IdCliente;
				resumenInversionSalida.IdPortafolio = res.IdPortafolio;
				resumenInversionSalida.Grupo = res.Grupo;
				resumenInversionSalida.DescripcionGrupo = obtenerDescripcionGrupo(resumenInversionSalida.Grupo, clasificacionInstrumentosPorIdClasificacionInstrumento, tipoInstrumentosPorIdTipoInstrumento);
				resumenInversionSalida.Item = res.Item;
				resumenInversionSalida.IdMonedaSistema = res.IdMonedaSistema;
				resumenInversionSalida.IdMonedaAdicional = res.IdMonedaAdicional;
				resumenInversionSalida.IdMonedaOperacion = res.IdMonedaOperacion;
			}
			resumenInversionSalida.MonedaResumenInversion = res.MonedaResumenInversion;
			
			resumenInversionSalida.MontoMonedaSistema = resumenInversionSalida.MontoMonedaSistema.add(res.MontoMonedaSistema, MathContext.DECIMAL128);
			resumenInversionSalida.MontoMonedaAdicional = resumenInversionSalida.MontoMonedaAdicional.add(res.MontoMonedaAdicional, MathContext.DECIMAL128);
			resumenInversionSalida.MontoMonedaOperacion = resumenInversionSalida.MontoMonedaOperacion.add(res.MontoMonedaOperacion, MathContext.DECIMAL128);

			hSalidaResumenInversion.put(id, resumenInversionSalida);
			
			List<OutResumenInversion> resumenesInversionClientePortafolio = hSalidaResumenesInversion.get(idResumenes);
			if(resumenesInversionClientePortafolio == null) {
				resumenesInversionClientePortafolio = new ArrayList<>();
			}
			resumenesInversionClientePortafolio.add(res);
			hSalidaResumenesInversion.put(idResumenes, resumenesInversionClientePortafolio);
		}
		
		List<ResumenInversionSalida> salidas = new ArrayList<ResumenInversionSalida>();
		for(Map.Entry<String, ResumenInversionSalida> obj : hSalidaResumenInversion.entrySet()){
			ResumenInversionSalida rInv = obj.getValue();
			List<OutResumenInversion> resumenesInversionClientePortafolio = hSalidaResumenesInversion.get(rInv.IdCliente + "_" + rInv.IdPortafolio);
			for(OutResumenInversion res : resumenesInversionClientePortafolio) {
				rInv.TotalMonedaSistema = rInv.TotalMonedaSistema.add(res.MontoMonedaSistema, MathContext.DECIMAL128);
				rInv.TotalMonedaAdicional = rInv.TotalMonedaAdicional.add(res.MontoMonedaAdicional, MathContext.DECIMAL128);
				rInv.TotalMonedaOperacion = rInv.TotalMonedaOperacion.add(res.MontoMonedaOperacion, MathContext.DECIMAL128);
				if(rInv.Grupo.equals(res.Grupo)) {
					rInv.TotalGrupoMonedaSistema = rInv.TotalGrupoMonedaSistema.add(res.MontoMonedaSistema, MathContext.DECIMAL128);
					rInv.TotalGrupoMonedaAdicional = rInv.TotalGrupoMonedaAdicional.add(res.MontoMonedaAdicional, MathContext.DECIMAL128);
					rInv.TotalGrupoMonedaOperacion = rInv.TotalGrupoMonedaOperacion.add(res.MontoMonedaOperacion, MathContext.DECIMAL128);
				}
			}
			salidas.add(rInv);
		}
		
		salidas.sort(new Comparator<ResumenInversion.ResumenInversionSalida>() {
		    @Override
		    public int compare(ResumenInversion.ResumenInversionSalida mov1, ResumenInversion.ResumenInversionSalida mov2) {

		        int cmpGrupo = mov1.Grupo.compareTo(mov2.Grupo);
		        if (cmpGrupo != 0) {
		            return cmpGrupo;
		        }

		        return mov1.Item.compareTo(mov2.Item);
		    }
		});
		
		List<HashMap<String,Object>> resultadoResumenInversion = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("resumenInversion", resultadoResumenInversion);
		logger.trace(out);
		logger.info("FIN::ResumenInversion");
		
		return out;
	}
	
	private List<OutResumenInversion> agregarPactosYSubyacentes(List<OutResumenInversion> p_out_ResumenInversion) {
		Map<String, List<OutResumenInversion>> pactos = p_out_ResumenInversion.stream()
			    .filter(r -> r.IdTipoInstrumento != null && "PAC".equalsIgnoreCase(r.IdTipoInstrumento))
			    .collect(Collectors.groupingBy(data -> data.IdCliente + "_" + data.IdPortafolio + "_" + data.IdMonedaOperacion));
		
		for (Map.Entry<String, List<OutResumenInversion>> entry : pactos.entrySet()) {

		    List<OutResumenInversion> registros = entry.getValue();
		    OutResumenInversion base = registros.get(0);

		    BigDecimal montoOperacionPositivo = BigDecimal.ZERO;
		    BigDecimal montoSistemaPositivo   = BigDecimal.ZERO;
		    BigDecimal montoAdicionalPositivo = BigDecimal.ZERO;

		    BigDecimal montoOperacionNegativo = BigDecimal.ZERO;
		    BigDecimal montoSistemaNegativo   = BigDecimal.ZERO;
		    BigDecimal montoAdicionalnegativo = BigDecimal.ZERO;

		    for (OutResumenInversion r : registros) {
		        if (r.MontoMonedaOperacion.compareTo(BigDecimal.ZERO) >= 0) {
		        	montoOperacionPositivo = montoOperacionPositivo.add(r.MontoMonedaOperacion);
		        	montoSistemaPositivo   = montoSistemaPositivo.add(r.MontoMonedaSistema);
		        	montoAdicionalPositivo = montoAdicionalPositivo.add(r.MontoMonedaAdicional);
		        } else {
		        	montoOperacionNegativo = montoOperacionNegativo.add(r.MontoMonedaOperacion);
		        	montoSistemaNegativo   = montoSistemaNegativo.add(r.MontoMonedaSistema);
		        	montoAdicionalnegativo = montoAdicionalnegativo.add(r.MontoMonedaAdicional);
		        }
		        
		        OutResumenInversion pactoSub = new OutResumenInversion();
		        pactoSub.IdCliente = r.IdCliente;
		        pactoSub.IdPortafolio = r.IdPortafolio;
		        pactoSub.Grupo = "PACTOS_SUB";
		        pactoSub.IdMonedaAdicional = r.IdMonedaAdicional;
		        pactoSub.IdMonedaOperacion = r.IdMonedaOperacion;
		        pactoSub.IdMonedaSistema = r.IdMonedaSistema;
		        pactoSub.IdTipoInstrumento = r.IdTipoInstrumento;
		        pactoSub.Item = String.format("%s %s", "RF".equals(r.Grupo)?"Renta Fija":"Intermediación Financiera", r.IdMonedaOperacion);
		        pactoSub.MontoMonedaAdicional = r.MontoMonedaAdicional;
		        pactoSub.MontoMonedaOperacion = r.MontoMonedaOperacion;
		        pactoSub.MontoMonedaSistema = r.MontoMonedaSistema;
		        pactoSub.MonedaResumenInversion = r.IdMonedaOperacion;
		        
		        p_out_ResumenInversion.add(pactoSub);
		    }

		    if (montoSistemaPositivo.compareTo(BigDecimal.ZERO) != 0) {
		    	OutResumenInversion venta = new OutResumenInversion();
		        venta.IdCliente = base.IdCliente;
		        venta.IdPortafolio = base.IdPortafolio;
		        venta.Grupo = "PACTOS";
		        venta.Item = String.format("%s %s", "Compra con Pacto", base.IdMonedaOperacion);
		        venta.IdMonedaOperacion = base.IdMonedaOperacion;
		        venta.IdTipoInstrumento = base.IdTipoInstrumento;
		        venta.IdMonedaSistema = base.IdMonedaSistema;
		        venta.IdMonedaAdicional = base.IdMonedaAdicional;
		        venta.MontoMonedaOperacion = montoOperacionPositivo;
		        venta.MontoMonedaSistema = montoSistemaPositivo;
		        venta.MontoMonedaAdicional = montoAdicionalPositivo;
		        venta.MonedaResumenInversion = base.IdMonedaOperacion;

		        p_out_ResumenInversion.add(venta);
		    }

		    if (montoSistemaNegativo.compareTo(BigDecimal.ZERO) != 0) {
		    	OutResumenInversion compra = new OutResumenInversion();
		        compra.IdCliente = base.IdCliente;
		        compra.IdPortafolio = base.IdPortafolio;
		        compra.Grupo = "PACTOS";
		        compra.Item = String.format("%s %s", "Venta con Pacto", base.IdMonedaOperacion);
		        compra.IdMonedaOperacion = base.IdMonedaOperacion;
		        compra.IdTipoInstrumento = base.IdTipoInstrumento;
		        compra.IdMonedaSistema = base.IdMonedaSistema;
		        compra.IdMonedaAdicional = base.IdMonedaAdicional;
		        compra.MontoMonedaOperacion = montoOperacionNegativo;
		        compra.MontoMonedaSistema = montoSistemaNegativo;
		        compra.MontoMonedaAdicional = montoAdicionalnegativo;
		        compra.MonedaResumenInversion = base.IdMonedaOperacion;

		        p_out_ResumenInversion.add(compra);
		    }
		    
		    if(montoSistemaPositivo.compareTo(BigDecimal.ZERO) != 0 && "RF".equals(base.IdTipoInstrumento)) {
		    	
		    }
		    
		}
		
		
		return p_out_ResumenInversion.stream().filter(data -> !(("PAC".equalsIgnoreCase(data.IdTipoInstrumento)) && ("RF".equalsIgnoreCase(data.Grupo) || "IF".equalsIgnoreCase(data.Grupo))))
											   .collect(Collectors.toList());
	}

	private Map<String, ClasificacionTipoInstrumento> obtenerClasificacionInstrumentosPorIdClasificacionInstrumento(Connection con) throws Exception{
		SQL sql = new SQL();
		List<ClasificacionTipoInstrumento> clasificacionInstrumentos = sql.query(con, SQL_SELECT_CLASIFICACION_TIPO_INSTRUMENTO, new HashMap<String, Object>(2), ClasificacionTipoInstrumento.class);
		Map<String, ClasificacionTipoInstrumento> clasificacionInstrumentosPorIdClasificacionInstrumento = clasificacionInstrumentos.stream().collect(Collectors.toMap(clasificacion->clasificacion.IdClasificacionTipoInstrumento, clasificacion->clasificacion));
		return clasificacionInstrumentosPorIdClasificacionInstrumento;
	}
	
	private Map<String, TipoInstrumento> obtenerTipoInstrumentosPorIdTipoInstrumento(Connection con) throws Exception{
		SQL sql = new SQL();
		List<TipoInstrumento> tipoInstrumentos = sql.query(con, SQL_SELECT_TIPO_INSTRUMENTO, new HashMap<String, Object>(2), TipoInstrumento.class);
		Map<String, TipoInstrumento> tipoInstrumentosPorIdTipoInstrumento = tipoInstrumentos.stream().collect(Collectors.toMap(tipoInstrumento->tipoInstrumento.IdTipoInstrumento, tipoInstrumento->tipoInstrumento));
		return tipoInstrumentosPorIdTipoInstrumento;
	}
	
	private String obtenerDescripcionGrupo(String grupo, Map<String, ClasificacionTipoInstrumento> clasificacionInstrumentosPorIdClasificacionInstrumento, Map<String, TipoInstrumento> tipoInstrumentosPorIdTipoInstrumento) {
		if("CAJA".equals(grupo)) {
			return "CAJA";
		}
		if("OPL".equals(grupo)) {
			return "OPERACIONES POR LIQUIDAR";
		}
		if(clasificacionInstrumentosPorIdClasificacionInstrumento.get(grupo) != null) {
			return clasificacionInstrumentosPorIdClasificacionInstrumento.get(grupo).Nombre;
		}
		if(tipoInstrumentosPorIdTipoInstrumento.get(grupo) != null) {
			return tipoInstrumentosPorIdTipoInstrumento.get(grupo).Nombre;
		}
		return grupo;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBCOESTADOConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		/*Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-07-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-08-01");
		Dato dato = new Dato();
		dato.IdCliente = 249199L;
		dato.IdPortafolio = 153970L;*/
		
		Objin.p_IdUsuario = 106138L;
		Objin.p_FechaDesde = Fecha.toDate("2020-06-01");
		Objin.p_FechaHasta = Fecha.toDate("2020-06-02");
		Dato dato = new Dato();
		dato.IdCliente    = 104497L;
		dato.IdPortafolio = 105166L;
		
		Objin.p_Datos.add(dato);
		ResumenInversion  demo= new ResumenInversion();				
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

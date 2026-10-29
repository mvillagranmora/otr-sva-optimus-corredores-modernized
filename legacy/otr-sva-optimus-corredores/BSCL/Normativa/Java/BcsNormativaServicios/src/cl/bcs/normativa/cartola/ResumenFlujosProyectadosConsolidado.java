package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ResumenFlujosProyectadosConsolidado implements BcsCartola{
	
	static final Logger logger = LogManager.getLogger(ResumenFlujosProyectadosConsolidado.class);
	
	private final String[] TIPO_FLUJO_PROYECTADO = {"Forwards", "Futuros", "Pactos", "Simultaneas"};
	
	private final String SQL_SELECT_MOVIMIENTOS_30_DIAS = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenFlujosProyectadosConsolidado_SelectMovimientos30Dias.sql");
	private final String SQL_SELECT_MOVIMIENTOS_90_DIAS = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenFlujosProyectadosConsolidado_SelectMovimientos90Dias.sql");
	private final String SQL_SELECT_MOVIMIENTOS_30_DIAS_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenFlujosProyectadosConsolidado_SelectMovimientos30DiasHistorico.sql");
	private final String SQL_SELECT_MOVIMIENTOS_90_DIAS_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenFlujosProyectadosConsolidado_SelectMovimientos90DiasHistorico.sql");
	
	private final String SQL_INSERT_CLIENTES_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertClientesLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class DistribucionProyeccion{
		public Long IdCliente;
		public Date FechaLiquidacion;
		public String Item;
		public String IdMonedaSistema;
		public BigDecimal Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoResumenFlujosProyectadosConsolidado{
		
		public Long IdCliente;
		public Long IdPortafolio;
		public String item;
		public String tipoDiv;
		public BigDecimal P30;
		public BigDecimal P30_90;
		
		public ResultadoResumenFlujosProyectadosConsolidado() {
			this.P30 = BigDecimal.ZERO;
			this.P30_90 = BigDecimal.ZERO;
			this.tipoDiv = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Cliente {
		public Long IdCliente;
		public Long IdTrabajo;
	}
	
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.debug("INICIO::ResumenFlujosProyectadosConsolidado");
		SQL sql = new SQL();
		
		SalidaCartola out = new SalidaCartola();
		Long idTrabajo =  Double.valueOf(Math.random()*10000000000L).longValue();
		
		try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			
			FechasIn inFecha30Dias = new FechasIn();
			inFecha30Dias.fechaInicial = Fecha.stringToSqlDate(entrada.p_FechaHasta);
			inFecha30Dias.numeroDias = 30L;
			FechasIn inFecha90Dias = new FechasIn();
			inFecha90Dias.fechaInicial = Fecha.stringToSqlDate(entrada.p_FechaHasta);
			inFecha90Dias.numeroDias = 90L;
			
			inObtenerDiaSiguiente.p_fechas.add(inFecha30Dias);
			inObtenerDiaSiguiente.p_fechas.add(inFecha90Dias);
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
			
			String fecha30Dias = null;
			String fecha90Dias = null;
			
			for(FechasOut fechaOut : outObtenerDiaSiguiente.p_out_fechas) {
				if(fechaOut.numeroDiasInicial.equals(30L)) {
					fecha30Dias = fechaOut.fechaCalculada;
				}
				if(fechaOut.numeroDiasInicial.equals(90L)) {
					fecha90Dias = fechaOut.fechaCalculada;
				}
			}
			
			List<Long> clientes = new ArrayList<Long>();
			
			for(Dato dato : entrada.p_Datos) {
				if(!clientes.contains(dato.IdCliente)) clientes.add(dato.IdCliente);
			}
			
			List<Cliente> clientesBusqueda = clientes.stream()
	        		.map(cliente->{
	        			Cliente clienteBusqueda = new Cliente();
	        			clienteBusqueda.IdCliente = cliente;
	        			clienteBusqueda.IdTrabajo = idTrabajo;
	        			return clienteBusqueda;
	        		})
	        		.collect(Collectors.toList());
	        
	        sql.batch(con, SQL_INSERT_CLIENTES_LEGACY, clientesBusqueda, Cliente.class);
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdTrabajo", idTrabajo);
			params.put("FechaProyectadaDesde", fecha30Dias);
			List<DistribucionProyeccion> distribucionProyeccion30Dias = new ArrayList<DistribucionProyeccion>();
			List<DistribucionProyeccion> distribucionProyeccion90Dias = new ArrayList<DistribucionProyeccion>();
			if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
				distribucionProyeccion30Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_30_DIAS, params, DistribucionProyeccion.class);
			}else {
				distribucionProyeccion30Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_30_DIAS_HISTORICO, params, DistribucionProyeccion.class);
			}
			
			params.put("FechaProyectadaHasta", fecha90Dias);
			if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
				distribucionProyeccion90Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_90_DIAS, params, DistribucionProyeccion.class);
			}else {
				distribucionProyeccion90Dias = sql.query(con, SQL_SELECT_MOVIMIENTOS_90_DIAS_HISTORICO, params, DistribucionProyeccion.class);
			}
			
			List<ResultadoResumenFlujosProyectadosConsolidado> salidas = new ArrayList<ResultadoResumenFlujosProyectadosConsolidado>();
			for(Dato dato : entrada.p_Datos) {
				for(String tipoFlujo : TIPO_FLUJO_PROYECTADO) {
					ResultadoResumenFlujosProyectadosConsolidado resultadoFlujoProyectado = new ResultadoResumenFlujosProyectadosConsolidado();
					resultadoFlujoProyectado.IdCliente = dato.IdCliente;
					resultadoFlujoProyectado.IdPortafolio = 0L;
					resultadoFlujoProyectado.item = tipoFlujo;
					
					//P30
					for(DistribucionProyeccion distribucion : distribucionProyeccion30Dias) {
						if(dato.IdCliente.equals(distribucion.IdCliente) && tipoFlujo.equalsIgnoreCase(distribucion.Item)) {
							resultadoFlujoProyectado.P30 = resultadoFlujoProyectado.P30.add(distribucion.Monto);
						}
					}
					
					//P30_90
					for(DistribucionProyeccion distribucion : distribucionProyeccion90Dias) {
						if(dato.IdCliente.equals(distribucion.IdCliente) && tipoFlujo.equalsIgnoreCase(distribucion.Item)) {
							resultadoFlujoProyectado.P30_90 = resultadoFlujoProyectado.P30_90.add(distribucion.Monto);
						}
					}
					salidas.add(resultadoFlujoProyectado);
				}
			}
			
			List<HashMap<String,Object>> resultadoFlujoProyectado = Utils.List2ListHashGenericObject(salidas);
			
			out.P_OUT_SALIDA.put("resumenFlujosProyectadosConsolidado", resultadoFlujoProyectado);
		} catch (Exception e) {
			logger.error("ERROR::ResumenFlujosProyectadosConsolidado", e);
			throw e;
		} finally {
			Cliente cliente = new Cliente();
			cliente.IdTrabajo = idTrabajo;
			List<Cliente> clientes = Arrays.asList(cliente);
			sql.batch(con, SQL_DELETE_PORTAFOLIOS_LEGACY, clientes, Cliente.class);
		}

		logger.debug("FIN::ResumenFlujosProyectadosConsolidado");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-07-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-07-31");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 512341L;
		Objin.p_Datos.add(dato);
		ResumenFlujosProyectadosConsolidado  demo= new ResumenFlujosProyectadosConsolidado();				
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

package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal;
import cl.bcs.plataforma.com.ConsultarCobrosPortafolio;
import cl.bcs.plataforma.com.ConsultarCobrosPortafolio.ConsultarCobros_Entrada;
import cl.bcs.plataforma.com.ConsultarCobrosPortafolio.ConsultarCobros_Salida;
import cl.bcs.plataforma.com.ConsultarCobrosPortafolio.LineaCobroProceso;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarCobrosOrdenes implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConsultarCobrosOrdenes.class);
	
	//private final String SQL_SELECT_COBROS_CORRESPONSAL = loadResourceToString("cl/bcs/ordenes/sql/ConsultarCobrosOrdenes_SelectCobrosCorresponsal.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Trace
	public ConsultarCobrosOrdenes_Salida logica(Connection conn,ConsultarCobrosOrdenes_Entrada ent) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ConsultarCobrosOrdenes");
		
		ConsultarCobrosOrdenes_Salida salida = new ConsultarCobrosOrdenes_Salida();
		List<ConsultarCobrosPortafolio.InformacionOperacion> ports = new ArrayList<>();
		for (InformacionOperacion obj : ent.p_Operaciones) {
			ConsultarCobrosPortafolio.InformacionOperacion aux = new ConsultarCobrosPortafolio.InformacionOperacion();
			//Utils.copyPojo(obj, aux);
			aux.IdInterno = obj.IdInterno;
			aux.IdPortafolio = obj.IdPortafolio;
			aux.IdTipoProducto = obj.IdTipoProducto;
			aux.IdTipoOperacion = obj.IdTipoOperacion;
			aux.IdTipoCanalIngreso = obj.IdTipoCanalIngreso;
			aux.IdBolsa = obj.IdBolsa;
			ports.add(aux);
		}
		
		//ORIGEN PORTAFOLIO
		ConsultarCobrosPortafolio serviceConsultarCobros = new ConsultarCobrosPortafolio();
		ConsultarCobrosPortafolio.ConsultarCobros_Entrada entradaConsultarCobros = new ConsultarCobros_Entrada();
		entradaConsultarCobros.p_IdEmpresa = ent.p_IdEmpresa;
		entradaConsultarCobros.p_IdModulo = ent.p_IdModulo;
		entradaConsultarCobros.p_IdUsuario = ent.p_IdUsuario;
		entradaConsultarCobros.p_Operaciones = ports;
		ConsultarCobros_Salida salidaCobros = serviceConsultarCobros.logica(conn, entradaConsultarCobros);
		for (LineaCobroProceso linea : salidaCobros.p_out_MatrizOperacional) {
			//Utils.copyPojo(linea, cobro);
			LineaMatrizOperacional cobro = copyPojoLineaMatrizOperacional(linea);
			cobro.IdOrigenCobro="PORTAFOLIO";
			salida.p_out_MatrizOperacional.add(cobro);
		}
		
		//ORIGEN CORRESPONSAL
		{
			cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal.ConsultarCobros_Entrada entradaCorr = new cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal.ConsultarCobros_Entrada();
			
			for (InformacionOperacion info : ent.p_Operaciones) {
				if (info.IdCorredoraCorresponsal != null && info.IdCorredoraCorresponsal != 0L) {
					cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal.InformacionOperacion arg0 = new cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal.InformacionOperacion();
					arg0.IdBolsa = info.IdBolsa;
					arg0.IdCorredorCorresponsal = info.IdCorredoraCorresponsal;
					arg0.IdInterno = info.IdInterno;
					arg0.IdTipoCanalIngreso = info.IdTipoCanalIngreso;
					arg0.IdTipoOperacion = info.IdTipoOperacion;
					arg0.IdTipoProducto = info.IdTipoProducto;
					entradaCorr.p_Operaciones.add(arg0);
				}
			}
			
			if (entradaCorr.p_Operaciones.size() > 0) {
				entradaCorr.p_IdEmpresa = ent.p_IdEmpresa;
				entradaCorr.p_IdModulo = ent.p_IdModulo;
				entradaCorr.p_IdUsuario = ent.p_IdUsuario;
				
				ConsultarCobrosCorredorCorresponsal corr = new ConsultarCobrosCorredorCorresponsal();
				cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal.ConsultarCobros_Salida salidaCorr = corr.logica(conn, entradaCorr );
				
				for (InformacionOperacion obj : ent.p_Operaciones) {
					for (cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal.LineaCobroProceso linea : salidaCorr.p_out_MatrizOperacional) {
						if (obj.IdCorredoraCorresponsal.equals(linea.IdCorredorCorresponsal)) {
							//Utils.copyPojo(linea, cobro);
							LineaMatrizOperacional cobro = copyPojoLineaMatrizOperacional(linea);
							
							cobro.IdOrigenCobro="CORRESPONSAL";
							cobro.IdBolsa = obj.IdBolsa;
							cobro.IdCorredoraCorresponsal = obj.IdCorredoraCorresponsal;
							cobro.IdInterno = obj.IdInterno;
							cobro.IdPortafolio = obj.IdPortafolio;
							cobro.IdTipoCanalIngreso = obj.IdTipoCanalIngreso;
							cobro.IdTipoOperacion = obj.IdTipoOperacion;
							cobro.IdTipoProducto = obj.IdTipoProducto;
							salida.p_out_MatrizOperacional.add(cobro);	
						}
					}
				}
			}
		}
		
		logger.info("Termino del servicio ConsultarCobrosOrdenes, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	public static class ConsultarCobrosOrdenes_Salida {
		public List<LineaMatrizOperacional> p_out_MatrizOperacional;
		public List<MensajeSalida> p_out_Mensajes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarCobrosOrdenes_Salida() {
			p_out_MatrizOperacional = new ArrayList<>();
			p_out_Mensajes = new ArrayList<>();
		}
	}
	
	public static class InPortafolio {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdCanalIngreso;
		public String IdBolsa;
		public Long IdCorredorCorresponsal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarCobrosOrdenes_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InformacionOperacion> p_Operaciones;
		
		public ConsultarCobrosOrdenes_Entrada() {
			this.p_Operaciones = new ArrayList<InformacionOperacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//entrada
	public static class InformacionOperacion {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public Long IdCorredoraCorresponsal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//salida
	public static class LineaMatrizOperacional {
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public Long IdCorredoraCorresponsal;
		public String IdCobro;
		public String IdOrigenCobro;
		public BigDecimal ValorCobro;
		public String FormaCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public String NombreCobro;
		public Long IdCobroClasificacion;
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private LineaMatrizOperacional copyPojoLineaMatrizOperacional(LineaCobroProceso linea) {
		LineaMatrizOperacional cobro = new LineaMatrizOperacional();
		cobro.IdInterno = linea.IdInterno;			   
		cobro.IdPortafolio = linea.IdPortafolio;
		cobro.IdTipoProducto = linea.IdTipoProducto;
		cobro.IdTipoOperacion = linea.IdTipoOperacion;
		cobro.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
		cobro.IdBolsa = linea.IdBolsa;
		//cobro.IdCorredoraCorresponsal = linea.IdCorredoraCorresponsal;
		cobro.IdCobro = linea.IdCobro;
		//cobro.IdOrigenCobro = linea.IdOrigenCobro;
		cobro.ValorCobro = linea.ValorCobro;
		cobro.FormaCobro = linea.FormaCobro;
		cobro.ValorTopeMinimoOrden = linea.ValorTopeMinimoOrden;
		cobro.IdMoneda = linea.IdMoneda;
		cobro.NombreCobro = linea.NombreCobro;
		cobro.IdCobroClasificacion = linea.IdCobroClasificacion;
		cobro.CodigoCobroClasificacion = linea.CodigoCobroClasificacion;
		cobro.PermiteCambiarValor = linea.PermiteCambiarValor;
		cobro.IdEmpresa = linea.IdEmpresa;
		return cobro;
	}
	
	private LineaMatrizOperacional copyPojoLineaMatrizOperacional(cl.bcs.plataforma.com.ConsultarCobrosCorredorCorresponsal.LineaCobroProceso linea) {
		LineaMatrizOperacional cobro = new LineaMatrizOperacional();
		cobro.IdInterno = linea.IdInterno;			   
		//cobro.IdPortafolio = linea.IdPortafolio;
		cobro.IdTipoProducto = linea.IdTipoProducto;
		cobro.IdTipoOperacion = linea.IdTipoOperacion;
		cobro.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
		cobro.IdBolsa = linea.IdBolsa;
		//cobro.IdCorredoraCorresponsal = linea.IdCorredoraCorresponsal;
		cobro.IdCobro = linea.IdCobro;
		//cobro.IdOrigenCobro = linea.IdOrigenCobro;
		cobro.ValorCobro = linea.ValorCobro;
		cobro.FormaCobro = linea.FormaCobro;
		cobro.ValorTopeMinimoOrden = linea.ValorTopeMinimoOrden;
		cobro.IdMoneda = linea.IdMoneda;
		cobro.NombreCobro = linea.NombreCobro;
		cobro.IdCobroClasificacion = linea.IdCobroClasificacion;
		cobro.CodigoCobroClasificacion = linea.CodigoCobroClasificacion;
		cobro.PermiteCambiarValor = linea.PermiteCambiarValor;
		cobro.IdEmpresa = linea.IdEmpresa;
		return cobro;
	}
	
	
	public static void main(String[] args) throws SQLException, Exception {
		ConsultarCobrosOrdenes s = new ConsultarCobrosOrdenes();
		ConsultarCobrosOrdenes_Entrada ent = new ConsultarCobrosOrdenes_Entrada();
		ent.p_IdEmpresa = 1L;
		ent.p_IdModulo = "ORD";
		ent.p_IdUsuario = 4L;
		InformacionOperacion info = new InformacionOperacion();
		info.IdBolsa = "BCS";
		info.IdCorredoraCorresponsal = 0L;
		info.IdInterno = 0L;
		info.IdPortafolio = 343710L;
		info.IdTipoCanalIngreso = "TRA";
		info.IdTipoOperacion = "COMACN";
		info.IdTipoProducto = "INAC";
		ent.p_Operaciones.add(info);
		ConsultarCobrosOrdenes_Salida aaasas = s.logica(PlataformaUtils.getDESAConnection(), ent);
		System.out.println(aaasas.toString());
	}
}
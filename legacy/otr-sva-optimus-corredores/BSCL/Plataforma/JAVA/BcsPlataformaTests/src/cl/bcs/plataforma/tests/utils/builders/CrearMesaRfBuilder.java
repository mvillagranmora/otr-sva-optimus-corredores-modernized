package cl.bcs.plataforma.tests.utils.builders;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearMesaRfBuilder {
	private final String SQL_CREAR_MESA_CALL = "CALL \"BSCL.Mesa.BD.Servicios::CrearMesa\"(	p_IdEmpresa => :p_IdEmpresa,	p_IdUsuario => :p_IdUsuario,	p_IdModulo => :p_IdModulo,	p_UsaReglasNegocio => :p_UsaReglasNegocio,	p_Nombre => :p_Nombre,	p_Codigo => :p_Codigo,	p_IdCliente => :p_IdCliente,	p_IdPortafolio => :p_IdPortafolio,	p_IdTipoProducto => :p_IdTipoProducto,	p_ValorRazonable => :p_ValorRazonable,	p_IdEstadoRegistro => :p_IdEstadoRegistro,	p_CodigoIFRS => :p_CodigoIFRS,	p_PorDefecto => :p_PorDefecto,    p_out_RefId => @p_out_RefId,        p_out_AdvertenciasContador  => @p_out_AdvertenciasContador,    p_out_AdvertenciasDetalle   => $p_out_AdvertenciasDetalle,    p_out_ErroresContador       => @p_out_ErroresContador,    p_out_ErroresDetalle        => $p_out_ErroresDetalle);";
	
	private HashMap<String, Object> paramsIn = new HashMap<>();
	private HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
	
	public class SalidaCrearMesaRfBuilder extends SalidaServicios{
		public Long IdMesa;

		public SalidaCrearMesaRfBuilder(){
			super();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public CrearMesaRfBuilder(){
		
		paramsIn.put("p_IdEmpresa",  1L);
		paramsIn.put("p_IdUsuario",  0L);
		paramsIn.put("p_IdModulo",  "MES");
		paramsIn.put("p_UsaReglasNegocio",  "N");
		paramsIn.put("p_Nombre",  "MESATEST");
		paramsIn.put("p_Codigo",  "MESATEST");
		paramsIn.put("p_IdCliente",  100024);
		paramsIn.put("p_IdPortafolio",  100039);
		paramsIn.put("p_IdTipoProducto",  "MERF");
		paramsIn.put("p_ValorRazonable",  "VALORMERCADO");
		paramsIn.put("p_IdEstadoRegistro",  "ACT");
		paramsIn.put("p_CodigoIFRS",  "T");
		paramsIn.put("p_PorDefecto",  "N");
		
		//parametros de salida
		
		paramsOut.put("p_out_RefId", Long.class);
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		paramsOut.put("p_out_ErroresDetalle", ErrorDetalle.class);
		paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);

	}
	
	public CrearMesaRfBuilder setIdEmpresa(Object IdEmpresa) {
		paramsIn.put("p_IdEmpresa", IdEmpresa);
		return this;
	}
	public CrearMesaRfBuilder setIdUsuario(Object p_IdUsuario) {
		paramsIn.put("p_IdUsuario", p_IdUsuario);
		return this;
	}
	public CrearMesaRfBuilder setIdCliente(Object p_IdCliente) {
		paramsIn.put("p_IdCliente", p_IdCliente);
		return this;
	}
	public CrearMesaRfBuilder setIdPortafolio(Object p_IdPortafolio) {
		paramsIn.put("p_IdPortafolio", p_IdPortafolio);
		return this;
	}
	
	public SalidaCrearMesaRfBuilder build(Connection conn){
		SQL sql = new SQL();
		SalidaCrearMesaRfBuilder salida = new SalidaCrearMesaRfBuilder();
		HashMap<String, Object> resultSP;
		try {
			resultSP = sql.call(conn, SQL_CREAR_MESA_CALL, paramsIn, paramsOut);
			salida.IdMesa = (Long) resultSP.get("p_out_RefId");
			salida.P_OUT_ADVERTENCIASCONTADOR = (Long) resultSP.get("p_out_AdvertenciasContador");
			salida.P_OUT_ERRORESCONTADOR = (Long) resultSP.get("p_out_ErroresContador");
			salida.P_OUT_ERRORESDETALLE = (List<ErrorDetalle>) resultSP.get("p_out_ErroresDetalle");
			salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalle>) resultSP.get("p_out_AdvertenciasDetalle");
		} catch (SQLException e) {
			e.printStackTrace();
			salida.addError("UI_ERROR_CREAR_MESA", e.getMessage());
		}

		return salida;
	}
}

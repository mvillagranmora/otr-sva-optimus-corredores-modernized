package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cl.bcs.custodia.ConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.EntradaConsultarCustodiaMovimientos;
import cl.bcs.custodia.ConsultarCustodiaMovimientos.SalidaConsultarCustodiaMovimientos;
import cl.bcs.normativa.cartola.pojo.TipoOperacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MovimientosTitulosForward implements BcsCartola{
	
	public final Log log = new Log("NOR.MovimientosTitulosForward");

	private final String SQL_SELECT_TIPO_OPERACION = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectTipoOperacion.sql");
	
	public static class SalidaMovimientosTitulosForward{
		public Long IdCliente;
		public Long IdPortafolio;
		public Timestamp FEC_MVTO;
		public BigDecimal SEC_MOVTO;
		public String operacion;
		public String NEMO;
		public String PB;
		public BigDecimal CANTIDAD;
		public BigDecimal precioTir;
		public BigDecimal montoNeto;
		public BigDecimal COMISION;
		public BigDecimal DERECHOS;
		public BigDecimal GASTOS;
		public BigDecimal montoTotal;
		public String MERCADO;
		public String fecha_vencimiento;
		public String NemoMP;
		public String NemoMS;
		public BigDecimal folioOper;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::MovimientosTitulosForward");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		ConsultarCustodiaMovimientos consultarCustodiaMovimientos = new ConsultarCustodiaMovimientos();
		
		
		EntradaConsultarCustodiaMovimientos entradaConsultarCustodiaMovimientos = new EntradaConsultarCustodiaMovimientos();
		entradaConsultarCustodiaMovimientos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaMovimientos.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaMovimientos.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarCustodiaMovimientos.p_FechaInicio = Fecha.stringToSqlDate(entrada.p_FechaDesde);
		entradaConsultarCustodiaMovimientos.p_FechaTermino = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		
		for(Dato dato : entrada.p_Datos){
			ConsultarCustodiaMovimientos.VariableProceso variableProceso = new ConsultarCustodiaMovimientos.VariableProceso();
			variableProceso.IdCliente = dato.IdCliente;
			variableProceso.IdPortafolio = dato.IdPortafolio;
			variableProceso.FolioCartera = 0L;
			variableProceso.IdClasificacionTipoInstrumento = "DV";
			variableProceso.IdTipoInstrumento = "FWD";//Solo debe recuperar FWD y no DERIVADOS 
			variableProceso.IdInstrumento = 0L;
			entradaConsultarCustodiaMovimientos.p_VariablesProceso.add(variableProceso);
		}
		
		SalidaConsultarCustodiaMovimientos salidaConsultarCustodiaMovimientos = consultarCustodiaMovimientos.logica(con, entradaConsultarCustodiaMovimientos);
		
		List<String> idTiposOperaciones = new ArrayList<String>();
		
		for(ConsultarCustodiaMovimientos.SalidaMovimiento mov : salidaConsultarCustodiaMovimientos.P_OUT_TABLA){
			if(!idTiposOperaciones.contains(mov.IdTipoOperacion)) idTiposOperaciones.add(mov.IdTipoOperacion);
		}
		
		params.put("IdTipoOperacion", idTiposOperaciones);
		List<TipoOperacion> tipoOperaciones = sql.query(con, SQL_SELECT_TIPO_OPERACION, params, TipoOperacion.class);
		HashMap<String,Object> hTiposOperacion = Utils.List2Hash(tipoOperaciones, "IdTipoOperacion");
		
		
		salidaConsultarCustodiaMovimientos.P_OUT_TABLA.sort(new Comparator<ConsultarCustodiaMovimientos.SalidaMovimiento>() {
		    @Override
		    public int compare(ConsultarCustodiaMovimientos.SalidaMovimiento mov1, ConsultarCustodiaMovimientos.SalidaMovimiento mov2) {
		        if(mov1.FechaRegistro.compareTo(mov2.FechaRegistro) == 0){
		            return 0;
		        }
		        return mov1.FechaRegistro.compareTo(mov2.FechaRegistro) == 1 ? 1 : -1;
		     }
		});
		
		List<SalidaMovimientosTitulosForward> salidas = new ArrayList<SalidaMovimientosTitulosForward>();
		for(ConsultarCustodiaMovimientos.SalidaMovimiento mov : salidaConsultarCustodiaMovimientos.P_OUT_TABLA){
			TipoOperacion tipoOperacion = (TipoOperacion) hTiposOperacion.get(mov.IdTipoOperacion);
			SalidaMovimientosTitulosForward salidaMovimientosTitulos = new SalidaMovimientosTitulosForward();
			salidaMovimientosTitulos.IdCliente = mov.IdCliente;
			salidaMovimientosTitulos.IdPortafolio = mov.IdPortafolio;
			salidaMovimientosTitulos.FEC_MVTO = Fecha.toTimeStamp(Fecha.stringToSqlDate(mov.FechaOperacion) + " 00:00:00");
			salidaMovimientosTitulos.SEC_MOVTO = new BigDecimal(mov.FolioCartera);
			salidaMovimientosTitulos.operacion = tipoOperacion != null ? tipoOperacion.Nombre : mov.TipoOperacion;
			salidaMovimientosTitulos.NEMO = mov.Nemotecnico + " " + mov.IdMonedaPrincipal + "/" + mov.IdMonedaSecundaria;
			salidaMovimientosTitulos.PB = "";
			salidaMovimientosTitulos.CANTIDAD = mov.Cantidad;
			salidaMovimientosTitulos.precioTir = mov.ValorTipoCambioCierre;
			salidaMovimientosTitulos.montoNeto = mov.MontoMonedaSecundaria;
			salidaMovimientosTitulos.COMISION = BigDecimal.ZERO;
			salidaMovimientosTitulos.DERECHOS = BigDecimal.ZERO;
			salidaMovimientosTitulos.GASTOS = BigDecimal.ZERO;
			salidaMovimientosTitulos.montoTotal = BigDecimal.ZERO;
			salidaMovimientosTitulos.MERCADO = "";
			salidaMovimientosTitulos.fecha_vencimiento = Fecha.toFormat(mov.FechaVencimiento, "dd/MM/yyyy");
			salidaMovimientosTitulos.NemoMP = "";
			salidaMovimientosTitulos.NemoMS = "";
			salidaMovimientosTitulos.folioOper = new BigDecimal(mov.IdOperacion);
			salidas.add(salidaMovimientosTitulos);
		}
		
		List<HashMap<String,Object>> resultadoMovimientosTitulosForward = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("novimientosTitulosForward", resultadoMovimientosTitulosForward);
		
		log.write("FIN::MovimientosTitulosForward");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-09-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-09-28");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 512341L;
		Objin.p_Datos.add(dato);
		MovimientosTitulosForward demo= new MovimientosTitulosForward();				
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

package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * @author PRojo
 *
 */
public class ConsultarFormaPagoOperacion implements JavaCall{
	private final Log log = new Log("MESA_ConsultarFormaPagoOperacion");
	private final String SQL_FormaPagoOperacion = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormaPagoOperacion_SelFormaPagoOperacion.sql");
	
	public static class ObjFormaPagoOperacionIN {
		public Long		IdOperacion;

		public ObjFormaPagoOperacionIN(Long idOperacion) {
			super();
			IdOperacion = idOperacion;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	
	public static class ObjFormaPagoOperacionOUT {
		public Long		IdComprobantePagoOperacion;
		public Long		IdEmpresa;
		public Long		IdOperacion;
		public Long		IdIngresoEgreso;
		public Long		IdAbono;
		public Long		IdCargo;
		public String	IdTipoProducto;
		public String	IdFormaPago;
		public String	NombreFormaDePago;
		public Long		Dias;
		public String	IdPreferenciaPago;
		public String	IdMedioDePago;
		public String	NombreMedioDePago;
		public String	RecibePaga;
		public String	RecibePagaPortafolioDesc;
		public String	IdModalidadPago;
		public String	IdMoneda;
		public String	IdMonedaLiquidacion;
		public BigDecimal		Monto;
		public BigDecimal		MontoLiquidacion;
		public Date		FechaIngresoComprobantePagoOperacion;
		public Date		FechaLiquidacionComprobantePagoOperacion;
		public String	IdEstado;
		public String		FechaConsulta;
		public String		FechaProximaLiquidacionComprobantePagoOperacion;
		
				
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class FormaPagoOperacionIN {
		public Long		IdEmpresa;
		public Long		IdPais;
		public String	Fecha;
		public List<ObjFormaPagoOperacionIN>	Operaciones;
		public FormaPagoOperacionIN() {
			super();
			Operaciones = new ArrayList<ObjFormaPagoOperacionIN>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class FormaPagoOperacionOUT {
		public List<ObjFormaPagoOperacionOUT>	Operaciones;
		public FormaPagoOperacionOUT() {
			super();
			Operaciones = new ArrayList<ObjFormaPagoOperacionOUT>();
		}
		
		public FormaPagoOperacionOUT(List<ObjFormaPagoOperacionOUT> operaciones) {
			super();
			Operaciones = operaciones;
		}

		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public OutObtenerDiaSiguiente diaSiguienteFecha(Connection con, List<InObtenerDiaSiguiente.FechasIn> fechas, Long idPais) throws  Exception {
		ObtenerDiaSiguiente o = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente entrada = new InObtenerDiaSiguiente();
		entrada.p_idPais = idPais;
		entrada.p_diaHabil = "S";
		entrada.p_fechas = fechas;
		OutObtenerDiaSiguiente salida = o.logica(con, entrada);
		
		return salida;
	}
	
	public FormaPagoOperacionOUT logica(Connection con, FormaPagoOperacionIN in) throws Exception {
		SQL sql = new SQL();
		List<ObjFormaPagoOperacionOUT> listFormaPagoOperacion = new ArrayList<ObjFormaPagoOperacionOUT>();
		List<InObtenerDiaSiguiente.FechasIn> fechas = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
		try {
			log.write("Inicia Ejecucion Consultar Forma de Pago Operacion: "+in);
			HashMap<String,Object> paramsVacio = new HashMap<String,Object>();
			//parametro de entrada
			HashMap<String, Object> paramsIn = new HashMap<String, Object>(); 
			paramsIn.put("IdEmpresa", in.IdEmpresa);
			List<Long> dat = new ArrayList<Long>();
			for(ObjFormaPagoOperacionIN item : in.Operaciones){
			 	dat.add(item.IdOperacion);
			}
			paramsIn.put("Operaciones", dat);
			
			if (in.Operaciones.size() > 0){
				log.write("Consulta Forma Pago Operacion: "+in);
				listFormaPagoOperacion= sql.query(con, SQL_FormaPagoOperacion,paramsIn,ObjFormaPagoOperacionOUT.class);
				for (ObjFormaPagoOperacionOUT item :listFormaPagoOperacion){
					InObtenerDiaSiguiente.FechasIn data = new InObtenerDiaSiguiente.FechasIn();
					data.fechaInicial 	= in.Fecha;
					data.numeroDias 	=  item.Dias;
					fechas.add(data);
				}
				OutObtenerDiaSiguiente diasSgtes = diaSiguienteFecha(con, fechas, in.IdPais);
				System.out.println(diasSgtes);
				for(ObjFormaPagoOperacionOUT itemFP :listFormaPagoOperacion){
					for (OutObtenerDiaSiguiente.FechasOut itemDia :diasSgtes.p_out_fechas){
						if (itemFP.Dias.compareTo(itemDia.numeroDiasInicial) == 0){
							itemFP.FechaConsulta = itemDia.fechaInicial;
							itemFP.FechaProximaLiquidacionComprobantePagoOperacion = itemDia.fechaCalculada;
						}
					}
				}
			}
			log.write("Termina Consulta Forma Pago Operacion... " +listFormaPagoOperacion);
			
		}catch (Exception e){
			e.printStackTrace();
		}

		return new FormaPagoOperacionOUT(listFormaPagoOperacion);
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();		
		FormaPagoOperacionIN jin=gson.fromJson(in.toString() ,FormaPagoOperacionIN.class);
		FormaPagoOperacionOUT out=logica(con,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
        System.out.println("dataout:= "+dataout);
		return dataout;
	}
		
	public static void main(String[] args) throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection con = null;
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		
		try {
			System.out.println("== BEGIN ==");
			con = PlataformaUtils.getDESAConnection();
			//datos entrada
			FormaPagoOperacionIN entrada = new FormaPagoOperacionIN();
			entrada.IdEmpresa = new Long(1);
			entrada.Fecha = "2017-07-07";
			entrada.IdPais = new Long(44);
			entrada.Operaciones.add(new ObjFormaPagoOperacionIN(new Long(259)));
			entrada.Operaciones.add(new ObjFormaPagoOperacionIN(new Long(230)));
			
			JsonObject in = parser.parse(gson.toJson(entrada)).getAsJsonObject();
			ConsultarFormaPagoOperacion prueba = new ConsultarFormaPagoOperacion(); 
			FormaPagoOperacionOUT resp=gson.fromJson(prueba.call(con, in),FormaPagoOperacionOUT.class);
			//mientras pruebo hare el rollback
			System.out.println(resp);
			con.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			con.close();
		}
			System.out.println("== END ==");
	}
}

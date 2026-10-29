package cl.bcs.normativa.circulares;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import java.util.Collections;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarInversionistasRV13419 implements JavaCall{
	
	private final Log log = new Log("NOR.ConsultarInversionistasRV13419");
	
	private final String SQL_CONSULTA_INVERSIONISTAS_RV_13419 = loadResourceToString("cl/bcs/normativa/circulares/sql/InversionistasRV13419_SelectComprobantes.sql");
	//private final String SQL_CONSULTA_INVERSIONISTAS_CORREDORA_RV_13419 = loadResourceToString("cl/bcs/normativa/circulares/sql/InversionistasRV13419_SelectComprobantesCorredora.sql");
	private final String SQL_CONSULTA_EMPRESA = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarEmpresa_Select.sql");

	public static class EntradaConsultarInversionistasRV13419{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaInicio;
		public String p_FechaTermino;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaConsultarInversionistasRV13419{
		public List<SalidaInvertsionistasRV> P_OUT_TABLA;
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public SalidaConsultarInversionistasRV13419(){
			this.P_OUT_TABLA = new ArrayList<SalidaInvertsionistasRV>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosSeccion {
    	public BigDecimal MontoTotal;
    	public BigDecimal MontoCompras;
    	public BigDecimal MontoVentas;
    	public Long CantidadTotal;
    	public Long CantidadCompras;
    	public Long CantidadVentas;
    	public Long CantidadClientes;
    	
    	DatosSeccion(){
    		MontoTotal = new BigDecimal(0);
			MontoCompras = new BigDecimal(0);
			MontoVentas = new BigDecimal(0);
			CantidadTotal = 0L;
			CantidadCompras = 0L;
			CantidadVentas = 0L;
    	}
	}
	
	public static class Periodos {
    	public Timestamp Periodos;
	}
    
	public static class SalidaInvertsionistasRV{
    	public String Periodo;
    	public String NombreCorredor;
    	public String CodigoCorredor;
    	public BigDecimal ClientesExtranjerosNoMilaMontoTotal;
    	public BigDecimal ClientesExtranjerosNoMilaMontoCompras;
    	public BigDecimal ClientesExtranjerosNoMilaMontoVentas;
    	public Long ClientesExtranjerosNoMilaCantidadTotal;
    	public Long ClientesExtranjerosNoMilaCantidadCompras;
    	public Long ClientesExtranjerosNoMilaCantidadVentas;
    	public Long ClientesExtranjerosNoMilaCantidadClientes;
    	public BigDecimal ClientesExtranjerosMilaMontoTotal;
    	public BigDecimal ClientesExtranjerosMilaMontoCompras;
    	public BigDecimal ClientesExtranjerosMilaMontoVentas;
    	public Long ClientesExtranjerosMilaCantidadTotal;
    	public Long ClientesExtranjerosMilaCantidadCompras;
    	public Long ClientesExtranjerosMilaCantidadVentas;
    	public Long ClientesExtranjerosMilaCantidadClientes;
    	public BigDecimal ClientesInstitucionalesNacionalesMontoTotal;
    	public BigDecimal ClientesInstitucionalesNacionalesMontoCompras;
    	public BigDecimal ClientesInstitucionalesNacionalesMontoVentas;
    	public Long ClientesInstitucionalesNacionalesCantidadTotal;
    	public Long ClientesInstitucionalesNacionalesCantidadCompras;
    	public Long ClientesInstitucionalesNacionalesCantidadVentas;
    	public Long ClientesInstitucionalesNacionalesCantidadClientes;
    	public BigDecimal ClientesNoInstitucionalesNacionalesMontoTotal;
    	public BigDecimal ClientesNoInstitucionalesNacionalesMontoCompras;
    	public BigDecimal ClientesNoInstitucionalesNacionalesMontoVentas;
    	public Long ClientesNoInstitucionalesNacionalesCantidadTotal;
    	public Long ClientesNoInstitucionalesNacionalesCantidadCompras;
    	public Long ClientesNoInstitucionalesNacionalesCantidadVentas;
    	public Long ClientesNoInstitucionalesNacionalesCantidadClientes;
    	public BigDecimal CuentaPropiaYRelacionadosMontoTotal;
    	public BigDecimal CuentaPropiaYRelacionadosMontoCompras;
    	public BigDecimal CuentaPropiaYRelacionadosMontoVentas;
    	public Long CuentaPropiaYRelacionadosCantidadTotal;
    	public Long CuentaPropiaYRelacionadosCantidadCompras;
    	public Long CuentaPropiaYRelacionadosCantidadVentas;
    	public Long CuentaPropiaYRelacionadosCantidadClientes;
    	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InversionistasRV{
    	public BigDecimal MontoComprobante;
    	public String Accion;
    	public Long IdCliente;
    	public String IdTipoInstrumento;
    	public String EsExtranjero;
    	public Timestamp FechaComprobante;
    	public String IdTipoCliente;
    	public String CodigoTipoIndicador;
    	public String ValorIndicador;
    	public Long IdMovimientoComprobante;
    	public Long IdComprobante;
    	public String IdMoneda;
    	public String EsCorredor;
    	public String EsRelacionado;
    	
    	
    	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InvertsionistasRVCorredora{
    	public BigDecimal MontoComprobante;
    	public String Accion;
    	public Long IdCliente;
    	public Timestamp FechaComprobante;
    	public Long IdComprobante;
    	public Long IdMovimientoComprobante;
    	public String IdMoneda;
    	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarInversionistasRV13419 jin = gson.fromJson(in.toString(), EntradaConsultarInversionistasRV13419.class);
		SalidaConsultarInversionistasRV13419 out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarInversionistasRV13419 logica(Connection con, EntradaConsultarInversionistasRV13419 entrada) throws Exception {
		log.write("INICIO::ConsultarCustodiaMovimientos");
		SalidaConsultarInversionistasRV13419 out = new SalidaConsultarInversionistasRV13419();
		
		SQL sql=new SQL();
		
		try{
			List<SalidaInvertsionistasRV> datosSalida = new ArrayList<SalidaInvertsionistasRV>();
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			String paisSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
			HashMap<String,Object> paramsEmp = new HashMap<>();
			paramsEmp.put("IdEmpresa", entrada.p_IdEmpresa);
			
			List<InformacionEmpresa> empresa = sql.query(con, SQL_CONSULTA_EMPRESA, paramsEmp, InformacionEmpresa.class);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			
			java.util.Date dateInicio = format.parse(entrada.p_FechaInicio.substring(0,4) + entrada.p_FechaInicio.substring(4,7) + "-01");
			java.util.Date dateTermino = format.parse(entrada.p_FechaTermino);

		    Timestamp fInicio = new Timestamp(dateInicio.getTime());

		    LocalDate lastDayofMonthYearTermino = YearMonth.of(Integer.parseInt(entrada.p_FechaTermino.substring(0,4)),dateTermino.getMonth() + 1).atEndOfMonth();
		    Date fecTermino = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(lastDayofMonthYearTermino.toString()).getTime());
		    
		    
			HashMap<String,Object> params =new HashMap<String,Object>();
			
			params.put("p_Pais", paisSistema);
			params.put("p_FechaInicio", fInicio);
			params.put("p_FechaTermino", fecTermino);
			
			List<InversionistasRV> InversionistasRV13419 = sql.query(con, SQL_CONSULTA_INVERSIONISTAS_RV_13419, params, InversionistasRV.class);
			
			//Llevar a moneda sistema
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			for(InversionistasRV dato : InversionistasRV13419){
				if(!monedaSistema.equalsIgnoreCase(dato.IdMoneda)){
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.IdTrabajo = 1L;
					calculoEntrada.IdInterno = dato.IdMovimientoComprobante;
					calculoEntrada.Fecha = dato.FechaComprobante.toString().substring(0,10);
					calculoEntrada.IdMonedaOrigen =dato.IdMoneda;
					calculoEntrada.IdMonedaDestino = monedaSistema;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntrada.MontoOrigen =  dato.MontoComprobante;
					calculoEntrada.IdUso = "";
					calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
				}
			}
			
			CalcularParidadSalida calculoSalida = new CalcularParidadSalida();
			if(calcularParidadEntrada.p_CalculosEntrada.size() > 0 ){
				calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
			}
			
			//HashMap<Long,Object> paridadRV13419 = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
			
			
	        List<String> PeriodosConsulta = new ArrayList<String>();
	      
	        for(InversionistasRV seccion : InversionistasRV13419)
			{
	        	if(PeriodosConsulta.contains(seccion.FechaComprobante.toString().substring(0,7)) == false) {
	        		PeriodosConsulta.add(seccion.FechaComprobante.toString().substring(0,7));
	        	}
			}
	        
			HashMap<String,Object> paramsCor =new HashMap<String,Object>();
			paramsCor.put("p_IdEmpresa", entrada.p_IdEmpresa);
			paramsCor.put("p_FechaInicio", fInicio);
			paramsCor.put("p_FechaTermino", fecTermino);
			
			/*
			List<InvertsionistasRVCorredora> InversionistasRVCorredora13419 = sql.query(con, SQL_CONSULTA_INVERSIONISTAS_CORREDORA_RV_13419, paramsCor, InvertsionistasRVCorredora.class);
	
	        
			//Llevar a moneda sistema (corredor)
			CalcularParidad calcularParidadCorredor = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntradaCor = new CalcularParidadEntrada();
			calcularParidadEntradaCor.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntradaCor.p_IdTrabajo = 1L;
			for(InvertsionistasRVCorredora dato : InversionistasRVCorredora13419){
				if(!monedaSistema.equalsIgnoreCase(dato.IdMoneda)){
					CalculoEntrada calculoEntrada = new CalculoEntrada();
					calculoEntrada.IdTrabajo = 1L;
					calculoEntrada.IdInterno = dato.IdComprobante;
					calculoEntrada.Fecha = dato.FechaComprobante.toString();
					calculoEntrada.IdMonedaOrigen =dato.IdMoneda;
					calculoEntrada.IdMonedaDestino = monedaSistema;
					calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntrada.MontoOrigen =  dato.MontoComprobante;
					calculoEntrada.IdUso = "";
					calcularParidadEntradaCor.p_CalculosEntrada.add(calculoEntrada);
				}
			}
			
			CalcularParidadSalida calculoSalidaCorredor = new CalcularParidadSalida();
			if(calcularParidadEntradaCor.p_CalculosEntrada.size() > 0 ){
				calculoSalidaCorredor = calcularParidadCorredor.logica(con, calcularParidadEntrada);
			}
			
			HashMap<Long,Object> paridadRV13419Cor = Utils.List2Hash(calculoSalidaCorredor.p_out_CalculosSalida, "IdInterno");	        
	        
	        for(InvertsionistasRVCorredora seccion : InversionistasRVCorredora13419)
			{
	        	if(PeriodosConsulta.contains(seccion.FechaComprobante.toString().substring(0,7)) == false) {
	        		PeriodosConsulta.add(seccion.FechaComprobante.toString().substring(0,7));
	        	}
	        	if(!monedaSistema.equalsIgnoreCase(seccion.IdMoneda)){
        			CalculoSalida calculo = (CalculoSalida) paridadRV13419Cor.get(seccion.IdComprobante);
        			seccion.MontoComprobante=calculo.MontoDestino;
        		}
	        	
	        	
			}
	        
*/
	        Collections.sort(PeriodosConsulta, Collections.reverseOrder());
	        
	        for(String periodo : PeriodosConsulta) {
	        	
	        	Date fechInicio = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(periodo.concat("-01")).getTime()); 				    
			    LocalDate lastDayofMonthYear = YearMonth.of(Integer.parseInt(periodo.substring(0,4)),fechInicio.getMonth() + 1).atEndOfMonth();
			    Date fechTermino = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(lastDayofMonthYear.toString()).getTime());
			  		        
			    
			    DatosSeccion extranjerosNoMila = new DatosSeccion();			
				DatosSeccion extranjerosMila = new DatosSeccion();		
				DatosSeccion institucionalesNacionales = new DatosSeccion();
				DatosSeccion noInstitucionalesNacionales = new DatosSeccion();			
				DatosSeccion cuentaPropiaYRelacionados = new DatosSeccion();
				
		        List<Long> clientesNoMila = new ArrayList<Long>();
		        List<Long> clientesMila = new ArrayList<Long>();
		        List<Long> clientesInstitucionalesNacionales = new ArrayList<Long>(); 
		        List<Long> clientesNoInstitucionalesNacionales = new ArrayList<Long>(); 
		        List<Long> clientesCuentaPropiaYRelacionados = new ArrayList<Long>();
		        

				for(InversionistasRV seccion : InversionistasRV13419){

					if(seccion.FechaComprobante.compareTo(fechInicio) >= 0 && seccion.FechaComprobante.compareTo(fechTermino) <=0) {


						if(seccion.EsExtranjero.equals("S") == true) {
							//NO MILA
							if(seccion.IdTipoInstrumento.equals("MIL") == false && seccion.IdTipoInstrumento.equals("") == false && seccion.IdTipoInstrumento.isEmpty() == false) {
								extranjerosNoMila.MontoTotal = extranjerosNoMila.MontoTotal.add(seccion.MontoComprobante);
								extranjerosNoMila.MontoCompras = seccion.Accion.equals("COMPRA") == true ? extranjerosNoMila.MontoCompras.add(seccion.MontoComprobante) : extranjerosNoMila.MontoCompras;
								extranjerosNoMila.MontoVentas = seccion.Accion.equals("VENTA") == true ? extranjerosNoMila.MontoVentas.add(seccion.MontoComprobante) : extranjerosNoMila.MontoVentas;
								extranjerosNoMila.CantidadTotal = extranjerosNoMila.CantidadTotal + 1;
								extranjerosNoMila.CantidadCompras = seccion.Accion.equals("COMPRA") == true ? extranjerosNoMila.CantidadCompras + 1 : extranjerosNoMila.CantidadCompras;
								extranjerosNoMila.CantidadVentas = seccion.Accion.equals("VENTA") == true ? extranjerosNoMila.CantidadVentas + 1 : extranjerosNoMila.CantidadVentas;
								clientesNoMila.add(seccion.IdCliente);
							}
							//MILA
							if(seccion.IdTipoInstrumento.equals("MIL") == true) {
								extranjerosMila.MontoTotal = extranjerosMila.MontoTotal.add(seccion.MontoComprobante);
								extranjerosMila.MontoCompras = seccion.Accion.equals("COMPRA") == true ? extranjerosMila.MontoCompras.add(seccion.MontoComprobante) : extranjerosMila.MontoCompras;
								extranjerosMila.MontoVentas = seccion.Accion.equals("VENTA") == true ? extranjerosMila.MontoVentas.add(seccion.MontoComprobante) : extranjerosMila.MontoVentas;
								extranjerosMila.CantidadTotal = extranjerosNoMila.CantidadTotal + 1;
								extranjerosMila.CantidadCompras = seccion.Accion.equals("COMPRA") == true ? extranjerosMila.CantidadCompras + 1 : extranjerosMila.CantidadCompras;
								extranjerosMila.CantidadVentas = seccion.Accion.equals("VENTA") == true ? extranjerosMila.CantidadVentas + 1 : extranjerosMila.CantidadVentas;
								clientesMila.add(seccion.IdCliente);
							}
						}else {

							//INSTITUCIONAL
							if(seccion.IdTipoCliente.equals("INSTITUCIONAL") == true) {
								institucionalesNacionales.MontoTotal = institucionalesNacionales.MontoTotal.add(seccion.MontoComprobante);
								institucionalesNacionales.MontoCompras = seccion.Accion.equals("COMPRA") == true ? institucionalesNacionales.MontoCompras.add(seccion.MontoComprobante) : institucionalesNacionales.MontoCompras;
								institucionalesNacionales.MontoVentas = seccion.Accion.equals("VENTA") == true ? institucionalesNacionales.MontoVentas.add(seccion.MontoComprobante) : institucionalesNacionales.MontoVentas;
								institucionalesNacionales.CantidadTotal = institucionalesNacionales.CantidadTotal + 1;
								institucionalesNacionales.CantidadCompras = seccion.Accion.equals("COMPRA") == true ? institucionalesNacionales.CantidadCompras + 1 : institucionalesNacionales.CantidadCompras;
								institucionalesNacionales.CantidadVentas = seccion.Accion.equals("VENTA") == true ? institucionalesNacionales.CantidadVentas + 1 : institucionalesNacionales.CantidadVentas;
								clientesInstitucionalesNacionales.add(seccion.IdCliente);
							}else {
								//RELACIONADO
								if(seccion.EsCorredor.equals("S") == true || seccion.EsRelacionado.equals("S") == true  ) {
									
									cuentaPropiaYRelacionados.MontoTotal = cuentaPropiaYRelacionados.MontoTotal.add(seccion.MontoComprobante);
									cuentaPropiaYRelacionados.MontoCompras = seccion.Accion.equals("COMPRA") == true ? cuentaPropiaYRelacionados.MontoCompras.add(seccion.MontoComprobante) : cuentaPropiaYRelacionados.MontoCompras;
									cuentaPropiaYRelacionados.MontoVentas = seccion.Accion.equals("VENTA") == true ? cuentaPropiaYRelacionados.MontoVentas.add(seccion.MontoComprobante) : cuentaPropiaYRelacionados.MontoVentas;
									cuentaPropiaYRelacionados.CantidadTotal = cuentaPropiaYRelacionados.CantidadTotal + 1;
									cuentaPropiaYRelacionados.CantidadCompras = seccion.Accion.equals("COMPRA") == true ? cuentaPropiaYRelacionados.CantidadCompras + 1 : cuentaPropiaYRelacionados.CantidadCompras;
									cuentaPropiaYRelacionados.CantidadVentas = seccion.Accion.equals("VENTA") == true ? cuentaPropiaYRelacionados.CantidadVentas + 1 : cuentaPropiaYRelacionados.CantidadVentas;
									clientesCuentaPropiaYRelacionados.add(seccion.IdCliente);
									
								}else {

									noInstitucionalesNacionales.MontoTotal = noInstitucionalesNacionales.MontoTotal.add(seccion.MontoComprobante);
									noInstitucionalesNacionales.MontoCompras = seccion.Accion.equals("COMPRA") == true ? noInstitucionalesNacionales.MontoCompras.add(seccion.MontoComprobante) : noInstitucionalesNacionales.MontoCompras;
									noInstitucionalesNacionales.MontoVentas = seccion.Accion.equals("VENTA") == true ? noInstitucionalesNacionales.MontoVentas.add(seccion.MontoComprobante) : noInstitucionalesNacionales.MontoVentas;
									noInstitucionalesNacionales.CantidadTotal = noInstitucionalesNacionales.CantidadTotal + 1;
									noInstitucionalesNacionales.CantidadCompras = seccion.Accion.equals("COMPRA") == true ? noInstitucionalesNacionales.CantidadCompras + 1 : noInstitucionalesNacionales.CantidadCompras;
									noInstitucionalesNacionales.CantidadVentas = seccion.Accion.equals("VENTA") == true ? noInstitucionalesNacionales.CantidadVentas + 1 : noInstitucionalesNacionales.CantidadVentas;
									clientesNoInstitucionalesNacionales.add(seccion.IdCliente);

								}
							}
						}
					}
				}
				
				extranjerosNoMila.CantidadClientes = clientesNoMila.stream().distinct().count();
				extranjerosMila.CantidadClientes = clientesMila.stream().distinct().count();
				institucionalesNacionales.CantidadClientes = clientesInstitucionalesNacionales.stream().distinct().count();
				noInstitucionalesNacionales.CantidadClientes = clientesNoInstitucionalesNacionales.stream().distinct().count();
				cuentaPropiaYRelacionados.CantidadClientes = clientesCuentaPropiaYRelacionados.stream().distinct().count();

				
				SalidaInvertsionistasRV data = new SalidaInvertsionistasRV();

				data.Periodo = periodo.substring(5,7) + "-" + periodo.substring(0,4);
				data.NombreCorredor = empresa.get(0).NombreCompleto;
			    data.CodigoCorredor = empresa.get(0).Codigo;
				data.ClientesExtranjerosNoMilaMontoTotal = dividirMil(extranjerosNoMila.MontoTotal);
				data.ClientesExtranjerosNoMilaMontoCompras = dividirMil(extranjerosNoMila.MontoCompras);
				data.ClientesExtranjerosNoMilaMontoVentas = dividirMil(extranjerosNoMila.MontoVentas);
				data.ClientesExtranjerosNoMilaCantidadTotal = extranjerosNoMila.CantidadTotal;
				data.ClientesExtranjerosNoMilaCantidadCompras = extranjerosNoMila.CantidadCompras;
				data.ClientesExtranjerosNoMilaCantidadVentas = extranjerosNoMila.CantidadVentas;
				data.ClientesExtranjerosNoMilaCantidadClientes = extranjerosNoMila.CantidadClientes;
				data.ClientesExtranjerosMilaMontoTotal = dividirMil(extranjerosMila.MontoTotal);
				data.ClientesExtranjerosMilaMontoCompras = dividirMil(extranjerosMila.MontoCompras);
				data.ClientesExtranjerosMilaMontoVentas = dividirMil(extranjerosMila.MontoVentas);
				data.ClientesExtranjerosMilaCantidadTotal = extranjerosMila.CantidadTotal;
				data.ClientesExtranjerosMilaCantidadCompras = extranjerosMila.CantidadCompras;
				data.ClientesExtranjerosMilaCantidadVentas = extranjerosMila.CantidadVentas;
				data.ClientesExtranjerosMilaCantidadClientes = extranjerosMila.CantidadClientes;
				data.ClientesInstitucionalesNacionalesMontoTotal = dividirMil(institucionalesNacionales.MontoTotal);
				data.ClientesInstitucionalesNacionalesMontoCompras = dividirMil(institucionalesNacionales.MontoCompras);
				data.ClientesInstitucionalesNacionalesMontoVentas = dividirMil(institucionalesNacionales.MontoVentas);
				data.ClientesInstitucionalesNacionalesCantidadTotal = institucionalesNacionales.CantidadTotal;
				data.ClientesInstitucionalesNacionalesCantidadCompras = institucionalesNacionales.CantidadCompras;
				data.ClientesInstitucionalesNacionalesCantidadVentas = institucionalesNacionales.CantidadVentas;
				data.ClientesInstitucionalesNacionalesCantidadClientes = institucionalesNacionales.CantidadClientes;
				data.ClientesNoInstitucionalesNacionalesMontoTotal = dividirMil(noInstitucionalesNacionales.MontoTotal);
				data.ClientesNoInstitucionalesNacionalesMontoCompras = dividirMil(noInstitucionalesNacionales.MontoCompras);
				data.ClientesNoInstitucionalesNacionalesMontoVentas = dividirMil(noInstitucionalesNacionales.MontoVentas);
				data.ClientesNoInstitucionalesNacionalesCantidadTotal = noInstitucionalesNacionales.CantidadTotal;
				data.ClientesNoInstitucionalesNacionalesCantidadCompras = noInstitucionalesNacionales.CantidadCompras;
				data.ClientesNoInstitucionalesNacionalesCantidadVentas = noInstitucionalesNacionales.CantidadVentas;
				data.ClientesNoInstitucionalesNacionalesCantidadClientes = noInstitucionalesNacionales.CantidadClientes;
				data.CuentaPropiaYRelacionadosMontoTotal = dividirMil(cuentaPropiaYRelacionados.MontoTotal);
				data.CuentaPropiaYRelacionadosMontoCompras = dividirMil(cuentaPropiaYRelacionados.MontoCompras);
				data.CuentaPropiaYRelacionadosMontoVentas = dividirMil(cuentaPropiaYRelacionados.MontoVentas);
				data.CuentaPropiaYRelacionadosCantidadTotal = cuentaPropiaYRelacionados.CantidadTotal;
				data.CuentaPropiaYRelacionadosCantidadCompras = cuentaPropiaYRelacionados.CantidadCompras;
				data.CuentaPropiaYRelacionadosCantidadVentas = cuentaPropiaYRelacionados.CantidadVentas;
				data.CuentaPropiaYRelacionadosCantidadClientes = cuentaPropiaYRelacionados.CantidadClientes;
			    	
				datosSalida.add(data);
	        }
			out.P_OUT_TABLA = datosSalida;
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write("ERROR",e);
			throw e;
		}finally{
			
		}
		
		log.write("FIN.ConsultarInversionistasRV13419");
		return out;
	}
	public BigDecimal dividirMil(BigDecimal valor ) {
		return BigDecimal.valueOf(Math.round(valor.divide(BigDecimal.valueOf(1000L),MathContext.DECIMAL128).doubleValue()));
	}
	public static void main(String[] args) throws Exception{
		System.out.println("== DESA ==");
		Connection con = PlataformaUtils.getDESAConnection();
		try {
			EntradaConsultarInversionistasRV13419 entrada = new EntradaConsultarInversionistasRV13419();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdUsuario = 288L;
			entrada.p_FechaInicio =  "2022-09-23";
			entrada.p_FechaTermino = "2022-09-23";
			
			
			SalidaConsultarInversionistasRV13419 Salida = new SalidaConsultarInversionistasRV13419();
			ConsultarInversionistasRV13419 sp = new ConsultarInversionistasRV13419();
			Salida = sp.logica(con,entrada);
			System.out.println(Salida);
			con.rollback();
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
		}finally{
			con.close();
		}
		System.out.println("== END ==");
	}
}

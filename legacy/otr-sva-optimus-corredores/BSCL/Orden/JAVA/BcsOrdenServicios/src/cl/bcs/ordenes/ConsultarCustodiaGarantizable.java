package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaParaGarantia;
import cl.bcs.custodia.ConsultarCustodiaParaGarantia.ConsultarCustodiaParaGarantiaEntrada;
import cl.bcs.custodia.ConsultarCustodiaParaGarantia.ConsultarCustodiaParaGarantiaSalida;
import cl.bcs.custodia.ConsultarCustodiaParaGarantia.Obj_p_out_CustodiaGarantia;
import cl.bcs.ordenes.ConsultarCustodiaSubyacenteSimultanea.Custodia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasasUI.EntregarPreciosTasasUI_Entrada_DatosInstrumentos;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * @author PRojo
 *
 */
public class ConsultarCustodiaGarantizable implements JavaCall{
	
	
	private final String CONSULTAR_ENTIDAD_COBERTURA = loadResourceToString("cl/bcs/ordenes/sql/ConsultarEntidadCobertura.sql");
	
	public static class outCustodiaGarantia {
		public Long 			IdInstrumento;
		public String 			Instrumento;
		public String 			IdTipoInstrumento;
		public String 			IdClasificacionTipoInstrumento;
		public BigDecimal		CantidadDisponibleGarantia;
		public BigDecimal		MontoDisponibleGarantia;
		public String 			IdMonedaInstrumento;
		public Date				FechaLiquidacion;
		public String 			IdEmisor;
		public Date				FechaEmisionInstrumento;	
		public Date				FechaVencimientoInstrumento;
		public String 			NemoSVS;
		public String 			IdTipoReajuste;
		public Long				PlazoEnDias;
		public String			EsFungible;					  					
		public BigDecimal		MontoCoberturaEntidad;
		public BigDecimal		MontoCoberturaCorredora;
		public BigDecimal		MontoCobertura;
		public BigDecimal		Precio;
		public String			ValorClasificacionRiesgo;
		public BigDecimal		CantidadAIngresar;
		public BigDecimal		MontoAGarantizar;
		public String			IdMonedaSistema;
		public BigDecimal		PrecioVaR;
		public BigDecimal		    PorcentajeVaR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class outAdvertenciasDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class outErroresDetalle {
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class utilData {
		public Long Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CustodiaGarantiaEntrada {
		public Long 		p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public Long			p_IdCliente;
		public Long 		p_IdPortafolio;
		public String		p_IdBolsa;
		public Integer		p_DiasLiquidacion;
		public Long			p_IdInstrumentoAGarantizar;
		public String		p_IdEntidad;					//nuevo a enviar de la interfaz
		public String		p_IdTipoInstrumentoAGarantizar;	//nuevo a enviar de la interfaz
		public String		p_IdTipoOperacion;				//nuevo a enviar de la interfaz

		public Long			p_plazoDias;		
		public BigDecimal	p_Tasa;
		
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CustodiaGarantiaSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public List<outCustodiaGarantia> 	P_OUT_CUSTODIAGARANTIA;
		
		public CustodiaGarantiaSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_CUSTODIAGARANTIA = new ArrayList<outCustodiaGarantia>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	
	
	public CustodiaGarantiaSalida logica(Connection conn, CustodiaGarantiaEntrada in) throws Exception {
		SQL sql = new SQL();
		CustodiaGarantiaSalida out = new CustodiaGarantiaSalida();
		
		try {
			
			HashMap<String,Object> param = new HashMap<String,Object>();
			HashMap<String,Object> param2 = new HashMap<String,Object>();
						
			//obtengo idPais
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		    Long idPaisEmpresa = empresa.IdPais;
		    String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		    
			//obtengo Id entidad cobertura
			param2.put("p_IdEntidad", in.p_IdEntidad);
			List<utilData> IdEntidadCobertura = sql.query(conn, CONSULTAR_ENTIDAD_COBERTURA, param2, utilData.class);
			if (IdEntidadCobertura.isEmpty())
				return out;
			
			ConsultarCustodiaParaGarantia custodiaParaGarantia = new ConsultarCustodiaParaGarantia();
			//parametro de entrada
			ConsultarCustodiaParaGarantiaEntrada paramsIn = new ConsultarCustodiaParaGarantiaEntrada(); 
			paramsIn.p_IdEmpresa						= in.p_IdEmpresa; 
			paramsIn.p_IdUsuario						= in.p_IdUsuario;
			paramsIn.p_IdModulo							= in.p_IdModulo;
			paramsIn.p_UsaReglasNegocio					= "S";
			paramsIn.p_IdPais							= idPaisEmpresa;
			paramsIn.p_PlazoDias						= in.p_DiasLiquidacion;
			paramsIn.p_IdCliente						= in.p_IdCliente; 
			paramsIn.p_IdPortafolio						= in.p_IdPortafolio;
			paramsIn.p_IdTipoOperacion					= in.p_IdTipoOperacion;
			paramsIn.p_IdTipoInstrumentoAGarantizar		= in.p_IdTipoInstrumentoAGarantizar;
			paramsIn.p_IdInstrumentoAGarantizar			= in.p_IdInstrumentoAGarantizar;
			//paramsIn.p_IdTipoInstrumentoBusqueda		= null;
			//paramsIn.p_IdInstrumentoBusqueda;
			paramsIn.p_IdEntidadCobertura				= (Long) IdEntidadCobertura.get(0).Valor;
			paramsIn.p_ValorizarFlujo					= "S";
			paramsIn.p_IdBolsa							= in.p_IdBolsa;
			
			ConsultarCustodiaParaGarantiaSalida respCustodiaParaGarantia = custodiaParaGarantia.logica(conn, paramsIn);
			
			out.P_OUT_MENSAJES = respCustodiaParaGarantia.P_OUT_MENSAJES;
			
			//PRECIOS ORDENES
			EntregarPreciosTasasUI consultarPrecioVector = new EntregarPreciosTasasUI();
			EntregarPreciosTasasUI_Entrada entrada_consultarPrecioVector = new EntregarPreciosTasasUI_Entrada();

			entrada_consultarPrecioVector.p_IdEmpresa =  in.p_IdEmpresa;
			entrada_consultarPrecioVector.p_IdModulo = "CUS";
			entrada_consultarPrecioVector.p_IdPais = idPaisEmpresa;
			entrada_consultarPrecioVector.p_IdUsuario = in.p_IdUsuario;

					
			for(Obj_p_out_CustodiaGarantia resp :respCustodiaParaGarantia.p_out_CustodiaGarantia){
				EntregarPreciosTasasUI_Entrada_DatosInstrumentos entradaPrecioOrden = new EntregarPreciosTasasUI_Entrada_DatosInstrumentos();
				entradaPrecioOrden.FechaConsulta = fechaSistema;
				entradaPrecioOrden.IdRegistro = 0L;
				entradaPrecioOrden.TipoCartera = "0";
				entradaPrecioOrden.IdUso = "ORDEN";
				entradaPrecioOrden.IdTipoCriterioTasa = "0";
				entradaPrecioOrden.IdClasificacionTipoInstrumento = resp.IdClasificacionTipoInstrumento;
				entradaPrecioOrden.IdTipoInstrumento = resp.IdTipoInstrumento;
				entradaPrecioOrden.Nemotecnico = resp.Instrumento;
				entradaPrecioOrden.IdInstrumento = resp.IdInstrumento;
				entradaPrecioOrden.IdMoneda = resp.IdMonedaInstrumento;
				entradaPrecioOrden.FechaConsulta = fechaSistema;
				entradaPrecioOrden.PlazoResidual = 0L;
				entradaPrecioOrden.IdTrabajo = 0L;	
				entrada_consultarPrecioVector.p_DatosInstrumentos.add(entradaPrecioOrden);
				
				EntregarPreciosTasasUI_Entrada_DatosInstrumentos entradaPrecioGarantia = new EntregarPreciosTasasUI_Entrada_DatosInstrumentos();
				entradaPrecioGarantia.FechaConsulta = fechaSistema;
				entradaPrecioGarantia.IdRegistro = 1L;
				entradaPrecioGarantia.TipoCartera = "0";
				entradaPrecioGarantia.IdUso = "GARANTIA";
				entradaPrecioGarantia.IdTipoCriterioTasa = "0";
				entradaPrecioGarantia.IdClasificacionTipoInstrumento = resp.IdClasificacionTipoInstrumento;
				entradaPrecioGarantia.IdTipoInstrumento = resp.IdTipoInstrumento;
				entradaPrecioGarantia.Nemotecnico = resp.Instrumento;
				entradaPrecioGarantia.IdInstrumento = resp.IdInstrumento;
				entradaPrecioGarantia.IdMoneda = resp.IdMonedaInstrumento;
				entradaPrecioGarantia.FechaConsulta = fechaSistema;
				entradaPrecioGarantia.PlazoResidual = 0L;
				entradaPrecioGarantia.IdTrabajo = 1L;	
				entrada_consultarPrecioVector.p_DatosInstrumentos.add(entradaPrecioGarantia);				
			}	

			EntregarPreciosTasas_Salida salidaPrecio = consultarPrecioVector.logica(conn, entrada_consultarPrecioVector);		
			HashMap<String, Object> hashDatosPrecios = new HashMap<String, Object>();
			HashMap<String, Object> hashDatosPreciosVar = new HashMap<String, Object>();
			
			 for (EntregarPreciosTasas_Salida_PreciosTasas sal: salidaPrecio.P_OUT_PRECIOSTASAS) {
				 if (sal.IdRegistro == 0L) {
					 hashDatosPrecios.put(sal.IdInstrumento.toString(), sal);
				 }
				 if (sal.IdRegistro == 1L) {
					 hashDatosPreciosVar.put(sal.IdInstrumento.toString(), sal);
				 }
			 }		
			 
			 
			BigDecimal bg1 = new BigDecimal(1);
			BigDecimal bg2 = new BigDecimal(2);
			BigDecimal bg30 = new BigDecimal(30);
			BigDecimal bg100 = new BigDecimal(100);		 
			
			for(Obj_p_out_CustodiaGarantia resp :respCustodiaParaGarantia.p_out_CustodiaGarantia){
				if (!resp.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") && !resp.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") && resp.CantidadDisponibleGarantia.compareTo(BigDecimal.ZERO) == 1){
					outCustodiaGarantia dataCus = new outCustodiaGarantia();
					EntregarPreciosTasas_Salida_PreciosTasas infoPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) hashDatosPrecios.get(resp.IdInstrumento.toString());
					EntregarPreciosTasas_Salida_PreciosTasas infoPrecioVar = (EntregarPreciosTasas_Salida_PreciosTasas) hashDatosPreciosVar.get(resp.IdInstrumento.toString());
					
					dataCus.IdInstrumento 					= resp.IdInstrumento;
					dataCus.Instrumento						= resp.Instrumento;
					dataCus.IdTipoInstrumento 				= resp.IdTipoInstrumento;
					dataCus.IdClasificacionTipoInstrumento 	= resp.IdClasificacionTipoInstrumento;
					dataCus.CantidadDisponibleGarantia 		= resp.CantidadDisponibleGarantia;
					dataCus.MontoDisponibleGarantia 		= resp.MontoDisponibleGarantia;
					dataCus.IdMonedaInstrumento 			= resp.IdMonedaInstrumento;
					dataCus.FechaLiquidacion				= resp.FechaLiquidacion;
					dataCus.IdEmisor						= resp.IdEmisor;
					dataCus.FechaEmisionInstrumento			= resp.FechaEmisionInstrumento;	
					dataCus.FechaVencimientoInstrumento		= resp.FechaVencimientoInstrumento;
					dataCus.NemoSVS							= "";//resp.NemoSVS;
					dataCus.IdTipoReajuste					= resp.IdTipoReajuste;
					dataCus.PlazoEnDias						= resp.PlazoEnDias;
					dataCus.EsFungible						= resp.EsFungible;			  					
					dataCus.MontoCoberturaEntidad			= resp.MontoCoberturaEntidad;
					dataCus.MontoCoberturaCorredora			= resp.MontoCoberturaCorredora;
					dataCus.MontoCobertura					= resp.MontoCobertura;
					dataCus.Precio							= infoPrecio.Precio;
					dataCus.ValorClasificacionRiesgo		= resp.ValorClasificacionRiesgo;
					dataCus.CantidadAIngresar				= resp.CantidadAIngresar;
					dataCus.MontoAGarantizar				= resp.MontoAGarantizar;
					dataCus.PrecioVaR						= infoPrecioVar.Precio;
					dataCus.IdMonedaSistema					= idMonedaSistema;
					BigDecimal valor1 = new BigDecimal(0);
					BigDecimal valor2 = new BigDecimal(0);			
					
					valor1 = bg2.subtract((infoPrecioVar.Precio.divide(infoPrecio.Precio,20,RoundingMode.HALF_UP)));
					valor2 = bg1.divide(bg1.add(in.p_Tasa.divide(bg100).multiply(BigDecimal.valueOf(in.p_plazoDias).divide(bg30,20,RoundingMode.HALF_UP))),20,RoundingMode.HALF_UP);
					dataCus.PorcentajeVaR = valor1.subtract(valor2).multiply(bg100);
					
					out.P_OUT_CUSTODIAGARANTIA.add(dataCus);
				}
			}
					
			
		}catch (Exception e){
			e.printStackTrace();
		}

		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();		
		CustodiaGarantiaEntrada jin=gson.fromJson(in.toString() ,CustodiaGarantiaEntrada.class);
		CustodiaGarantiaSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_IdCliente\":116606,\"p_IdPortafolio\":20262,\"p_IdBolsa\":\"BVL\",\"p_DiasLiquidacion\":0,\"p_IdInstrumentoAGarantizar\":2253,\"p_IdEntidad\":\"CAV\",\"p_IdTipoInstrumentoAGarantizar\":\"RRV\",\"p_IdTipoOperacion\":\"VENRRV\"}";
			String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"ORD\",\"p_IdCliente\":1424224,\"p_IdPortafolio\":1451800,\"p_IdBolsa\":\"BCS\",\"p_DiasLiquidacion\":0,\"p_IdInstrumentoAGarantizar\":2249,\"p_IdEntidad\":\"BCS\",\"p_IdTipoInstrumentoAGarantizar\":\"SIM\",\"p_IdTipoOperacion\":\"VENSIM\",\"p_plazoDias\":5,\"p_Tasa\":5}";
			CustodiaGarantiaEntrada Objin = gson.fromJson(json2, CustodiaGarantiaEntrada.class);
			
			ConsultarCustodiaGarantizable demo=new ConsultarCustodiaGarantizable();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CustodiaGarantiaSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
}

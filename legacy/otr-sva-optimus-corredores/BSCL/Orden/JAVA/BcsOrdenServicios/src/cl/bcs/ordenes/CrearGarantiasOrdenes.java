package cl.bcs.ordenes;

/*
 * Clase generada automaticamente por Sp2Java
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresarGarantiaJava;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida.ObjResultado;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCuentaInversion;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCustodia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearGarantiasOrdenes implements JavaCall {

	private final Log log = new Log("ORD.CrearGarantiasOrdenes");
	private final String SQL_CONSULTAR_CONTRAPARTE = loadResourceToString("cl/bcs/ordenes/sql/CrearGarantiasOrdenes_ConsultarContraparte.sql");
	private final String SQL_CONSULTAR_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CrearGarantiasOrdenes_ConsultarInstrumentos.sql");
	private final String SQL_CONSULTAR_CONCEPTOS = loadResourceToString("cl/bcs/ordenes/sql/CrearGarantiasOrdenes_ConsultarConcepto.sql");
		
	public static class CrearGarantiasOrdenes_Entrada {

		public Long									p_IdEmpresa;
		public Long									p_IdUsuario;
		public String								p_IdModulo;
		public List<GarantiaCustodia_Entrada>		p_GarantiasCustodia;
		public List<GarantiaCtaInversion_Entrada>	p_GarantiasCuentaInversion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CrearGarantiasOrdenes_Salida {

		public List<GarantiaCustodia_Salida>		p_out_GarantiasCustodia;
		public List<GarantiaCtaInversion_Salida>	p_out_GarantiasCtaInversion;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>					p_out_Mensajes;
		
		public CrearGarantiasOrdenes_Salida() {
			p_out_GarantiasCustodia = new ArrayList();
			p_out_GarantiasCtaInversion = new ArrayList();
			p_out_Mensajes = new ArrayList();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class GarantiaCustodia_Entrada {
		public Long			IdInstrumento;
		public Long			IdInstrumentoGarantizado;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public BigDecimal	Cantidad;
		public String		IdBolsa;
		public BigDecimal	Precio;
		public BigDecimal	Monto;
		public String		IdTipoOperacion = "";//tipo operacion origen garantia
		public Long			PlazoEnDias;
		public Long			IdOperacion;
		public BigDecimal	Tasa;
		public Long			FolioCarteraGarantizada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class GarantiaCtaInversion_Entrada {
		public String		IdMoneda;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public BigDecimal	Monto;
		public Long			IdInstrumentoGarantizado;
		public String		IdTipoOperacion = "";//tipo operacion origen garantia
		public Long			IdOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class GarantiaCustodia_Salida {
		public Long			FolioGarantia;
		public String		IdMonedaInstrumento;
		public String		Instrumento;
		public BigDecimal	Cantidad;
		public BigDecimal	Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class GarantiaCtaInversion_Salida {
		public Long			FolioGarantia;
		public String		IdMoneda;
		public BigDecimal	Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearGarantiasOrdenes_Entrada in = gson.fromJson(jin.toString(), CrearGarantiasOrdenes_Entrada.class);
		CrearGarantiasOrdenes_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class GarantiaCustodia_Proceso {
		public Long			IdGenerarMovimientoCustodia; 
		public Long			IdInstrumentoCarteraGarantizada; 
		public Long			IdContraparteGarantia; 
		public String		RazonSocialContraparteGarantia; 
		public String		IdOrigenGarantia; 
		public Long			IdClienteIngreso; 
		public Long			IdPortafolioIngreso; 
		public String		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;
		public String 		Nemotecnico;
		public Long			IdInstrumento;
		public String		IdEmisor;
		public String		NemoSVS;
		public String		IdTipoReajuste;
		public java.sql.Date		FechaEmisionInstrumento;
		public java.sql.Date		FechaVencimientoInstrumento;
		public String		EsFungible;
		public String		IdMonedaSistema; 
		public String		IdConcepto; 
		public String		IdTipoOperacion; 
		public String		FechaOperacion; 
		public String		IdBolsa; 
		public String		IdCondicionLiquidacion; 
		public String		FechaLiquidacion; 
		public String		IdMonedaInstrumento; 
		public BigDecimal	Cantidad; 
		public BigDecimal	MontoCompra; 
		public BigDecimal	MontoCompraMonedaInstrumento;
		public BigDecimal PrecioCompra;
		public BigDecimal ValorRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContraparteGarantia_Proceso {
		public Long IdContraparteGarantia;
		public String RazonSocialContraparteGarantia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoGarantia_Proceso {
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoReajuste;
		public String IdEmisor;
		public java.sql.Date FechaEmision;
		public java.sql.Date FechaVencimiento;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ConceptoGarantia {
		public String IdConcepto;
		public String NombreConcepto;
		public String IdTipoOperacion;
		public String Uso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresoCustodia_Proceso {
		public Long FolioGarantia;
		public Long IdGenerarMovimientoCustodia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public CrearGarantiasOrdenes_Salida logica(Connection con, CrearGarantiasOrdenes_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		CrearGarantiasOrdenes_Salida out = new CrearGarantiasOrdenes_Salida();
		log.write("INICIO");
		try {
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			List<ContraparteGarantia_Proceso> contraparte = sql.query(con, SQL_CONSULTAR_CONTRAPARTE, new HashMap<>(), ContraparteGarantia_Proceso.class);
			
			if (contraparte.size() == 0) {
				MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CONTRAPARTE_GARANTIA_NO_CONFIGURADA","");
				String desc = Diccionario.getMensaje(con, entrada.p_IdEmpresa,msg.codigo, new HashMap());
				msg.descripcion = desc;
				out.p_out_Mensajes.add(msg);
				return out;
			}
			
			ArrayList<GarantiaCustodia_Proceso> garantiasCustodia = new ArrayList<GarantiaCustodia_Proceso>();
			Long i = 0L;
			HashMap<Long,IngresarGarantiaEntrada> garantiasCustodiaHash = new HashMap<Long,IngresarGarantiaEntrada>();
			List<ConceptoGarantia> concepto = sql.query(con, SQL_CONSULTAR_CONCEPTOS, new HashMap<>(), ConceptoGarantia.class);
			HashMap<String, Object> concepHash = Utils.List2Hash(concepto, "IdTipoOperacion");
			
			IngresarGarantiaJava garantia = new IngresarGarantiaJava();
			IngresarGarantiaEntrada garantiaIn = new IngresarGarantiaEntrada();
			
			garantiaIn.p_IdEmpresa = entrada.p_IdEmpresa;
			garantiaIn.p_IdUsuario = entrada.p_IdUsuario;
			garantiaIn.p_IdModulo = entrada.p_IdModulo;
			garantiaIn.p_UsaReglasNegocio = "S";
			garantiaIn.p_SimulaOperacion = "N";
			garantiaIn.p_Certifica = "N";  //S: si proviene desde Certificacion de movimiento en transito
			garantiaIn.p_FechaMovimiento = fechaSistema;
			
			
			ArrayList<Long> instrumentos = new ArrayList<Long>();
			for (GarantiaCustodia_Entrada gar: entrada.p_GarantiasCustodia) {
				instrumentos.add(gar.IdInstrumento);
			}
			HashMap<String,Object> params1 = new HashMap<String,Object>();
			params1.put("Instrumentos", instrumentos);
			List<InstrumentoGarantia_Proceso> infoInstrumentos = sql.query(con, SQL_CONSULTAR_INSTRUMENTOS, params1, InstrumentoGarantia_Proceso.class);
			
			
			HashMap<Long, Object> info = Utils.List2Hash(infoInstrumentos, "IdInstrumento");
			if (entrada.p_GarantiasCustodia.size() > 0) {
				
				
				for (GarantiaCustodia_Entrada gar: entrada.p_GarantiasCustodia) {
					
					ObjGarantiaCustodia garPro = new ObjGarantiaCustodia();
					garPro.IdInstrumento = gar.IdInstrumento; 
					garPro.IdMoneda = ((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumento)).IdMoneda;
					garPro.Cantidad = gar.Cantidad;;
					garPro.ValorRescate = BigDecimal.ZERO;
					garPro.MontoMonedaInstrumento  = gar.Monto;
					garPro.MontoMonedaSistema  = gar.Monto; 
					garPro.IdClasificacionTipoInstrumento =  ((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumento)).IdClasificacionTipoInstrumento; 
					garPro.IdTipoReajuste = ((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumento)).IdTipoReajuste; 
					garPro.NemoSVS = "";
					garPro.FechaEmisionInstrumento = Fecha.toFormat(((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumento)).FechaEmision,"YYYY-MM-dd"); 
					garPro.FechaVencimientoInstrumento = Fecha.toFormat(((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumento)).FechaVencimiento,"YYYY-MM-dd"); 
					garPro.IdEmisor = ((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumento)).IdEmisor; 
					garPro.PlazoEnDias = gar.PlazoEnDias; 
					garPro.EsFungible = "N"; 
					garPro.IdCliente = gar.IdCliente; 
					garPro.IdPortafolio = gar.IdPortafolio;
					garPro.IdTipoInstrumento = ((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumento)).IdTipoInstrumento; 
					garPro.IdBolsa = gar.IdBolsa; 
					garPro.IdInstrumentoGarantizado =  gar.IdInstrumentoGarantizado; 
					garPro.IdConcepto = ((ConceptoGarantia) concepHash.get(gar.IdTipoOperacion)).IdConcepto;//"INGGSI"; 
					garPro.IdContraparteGarantia = contraparte.get(0).IdContraparteGarantia; 
					garPro.IdTipoOperacion = "INGGTA"; 
					garPro.IdOperacion = gar.IdOperacion; 
					garPro.Precio = gar.Precio; 
					garPro.Tasa = gar.Tasa;
					garPro.FolioCarteraGarantizada = gar.FolioCarteraGarantizada;
					
					garantiaIn.p_GarantiaCustodia.add(garPro);
				}
			}
			
			
			ArrayList<ObjGarantiaCuentaInversion> garantiasCtaInversion = new ArrayList<>();
			
			for (GarantiaCtaInversion_Entrada gar: entrada.p_GarantiasCuentaInversion) {
				ObjGarantiaCuentaInversion garPro = new ObjGarantiaCuentaInversion();
				
				garPro.IdMoneda = gar.IdMoneda; 
				garPro.MontoMonedaInstrumento = gar.Monto;
				garPro.MontoMonedaSistema = gar.Monto; 
				garPro.IdCliente = gar.IdCliente; 
				garPro.IdPortafolio = gar.IdPortafolio; 
				garPro.IdTipoInstrumento =  info.get(gar.IdInstrumentoGarantizado) != null ?((InstrumentoGarantia_Proceso)info.get(gar.IdInstrumentoGarantizado)).IdTipoInstrumento: ""; 
				garPro.IdBolsa = null; 
				garPro.IdInstrumentoGarantizado = gar.IdInstrumentoGarantizado; 
				garPro.IdConcepto = ((ConceptoGarantia) concepHash.get(gar.IdTipoOperacion)).IdConcepto;
				garPro.IdContraparteGarantia = contraparte.get(0).IdContraparteGarantia;
				garPro.IdTipoOperacion = "INGGTA"; 
				garPro.IdOperacion = gar.IdOperacion; 
				
				garantiasCtaInversion.add(garPro);
			}
			IngresarGarantiaSalida garantiaOut = garantia.logica(con, garantiaIn);
			
			out.p_out_Mensajes.addAll(garantiaOut.P_OUT_MENSAJES);
			for (ObjResultado garOut: garantiaOut.P_OUT_RESULTADOPROCESO) {
				System.out.println(garOut.IdGenerarMovimientoCustodia);
				System.out.println(garOut.TablaOrigen);
				System.out.println(garOut.IdCustodia);
				System.out.println(garOut.FolioCustodia);
				if (garOut.TablaOrigen.equals("GARANTIA")){
					for (ObjGarantiaCustodia garIn : garantiaIn.p_GarantiaCustodia) {
						if (garIn.IdOperacion.equals(garOut.IdOperacion) && garIn.IdInstrumento.equals(garOut.IdInstrumento)){
							GarantiaCustodia_Salida gar = new GarantiaCustodia_Salida();
							gar.FolioGarantia = garOut.FolioCustodia;
							gar.IdMonedaInstrumento = garIn.IdMoneda;
							gar.Instrumento = ((InstrumentoGarantia_Proceso)info.get(garIn.IdInstrumento)).Nemotecnico;
							gar.Cantidad = garIn.Cantidad;
							gar.Monto = garIn.MontoMonedaInstrumento;
							out.p_out_GarantiasCustodia.add(gar);
						}
					}
					for (ObjGarantiaCuentaInversion garIn : garantiaIn.p_GarantiaCuentaInversion) {
						if (garIn.IdOperacion.equals(garOut.IdOperacion)){
							GarantiaCustodia_Salida gar = new GarantiaCustodia_Salida();
							gar.FolioGarantia = garOut.FolioCustodia;
							gar.IdMonedaInstrumento = garIn.IdMoneda;
							gar.Instrumento = "";
							gar.Cantidad = BigDecimal.ZERO;
							gar.Monto = garIn.MontoMonedaInstrumento;
							out.p_out_GarantiasCustodia.add(gar);
						}
					}
				}
			}
			
		} finally {
			

		}
		log.write("TERMINO");
		return out;

	}

}

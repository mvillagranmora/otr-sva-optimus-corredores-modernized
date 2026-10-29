package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.ConsultarCobrosPlantilla.ConsultarCobrosPlantilla_Entrada;
import cl.bcs.plataforma.com.ConsultarCobrosPlantilla.ConsultarCobrosPlantilla_Salida;
import cl.bcs.plataforma.com.ConsultarCobrosPlantilla.InformacionOperacion;
import cl.bcs.plataforma.com.ConsultarCobrosPlantillaPeru.ConsultarCobrosPlantillaPeru_Entrada;
import cl.bcs.plataforma.com.ConsultarCobrosPlantillaPeru.ConsultarCobrosPlantillaPeru_Salida;
import cl.bcs.plataforma.com.ConsultarProductosPlantilla.ConsultarProductosPlantilla_Entrada;
import cl.bcs.plataforma.com.ConsultarProductosPlantilla.ConsultarProductosPlantilla_Salida;
import cl.bcs.plataforma.com.ConsultarProductosPlantilla.DetallePerfilProducto;
import cl.bcs.plataforma.com.ConsultarProductosPlantilla.PerfilProductoEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMatrizOperacional implements JavaCall {
	
	private final String SQL_CONSULTAR_PLANTILLAS_MODELO_COMERCIAL = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarPlantillasModeloComercial.sql");
	private final String SQL_CONSULTAR_PLANTILLAS_PORTAFOLIO = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarPlantillasPortafolio.sql");
	private final String SQL_CONSULTAR_DATOS = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarDatos.sql");
	private final String SQL_CONSULTAR_MERCADOS = loadResourceToString("cl/bcs/plataforma/com/sql/ConsultarMatrizOperacional_ConsultarMercados.sql");
	
	
	public static class ConsultarMatrizOperacional_Entrada {
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_IdTipoEntidad = "PORTAFOLIO";	//PORTAFOLIO, MODELO, PREVISUALIZAR
		public String p_IdEntidad;						//IdPortafolio, IdPerfilComercial
		
		//CASO PREVISUALIZAR
		public String p_IdPlantillaCobros;
		public String p_IdPlantillaProductos;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LineaMatrizOperacional {
		public String 		IdBolsa;
		public String		NombreBolsa;
		public String 		IdTipoProducto;
		public String 		NombreTipoProducto;
		public String 		IdTipoOperacion;
		public String 		NombreTipoOperacion;
		public String 		IdTipoCanalIngreso;
		public String 		NombreTipoCanalIngreso;
		public String 		IdCobro;
		public BigDecimal 	ValorCobro;
		public String 		FormaCobro;
		public BigDecimal 	ValorTopeMinimoOrden;
		public String 		IdMoneda;
		public String 		NombreCobro;
		public Long 		IdCobroClasificacion;
		public String 		CodigoCobroClasificacion;
		public Long 		IdEmpresa;
		
		public String 		IdPerfilCobros;
		public String 		EsPerfilPersonalizado;
		public String 		IdEstadoPerfilCobros;
		public String 		IdClaseCobro;
		public String 		IdTipoCobro;
		public String 		NombreTipoCobro;
		public String 		CodigoDefinicionCobro;
		public String 		IdEstado;
		public String 		Descripcion;
		public String 		CheckCobroMonedaOperacion;
		public String 		IdMonedaCobroOtraMoneda;
		public BigDecimal 	MontoFijo_MontoCobro;
		public String 		MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal 	MontoFijo_ValorMontoOperacionMenor;
		public String 		MontoFijo_CheckSiNo;
		public BigDecimal 	MontoFijo_ValorMontoSiNo;
		public BigDecimal 	PorcentualFijo_ValorCobro;
		public String 		PorcentualFijo_CheckSobreMonto;
		public String 		PorcentualFijo_CheckSobreCantidad;
		public String 		PorcentualFijo_CheckMayorEntreDos;
		public String 		PorcentualFijo_CheckMenorEntreDos;
		public String		PorcentualFijo_CheckMontoMinimo;
		public BigDecimal 	PorcentualFijo_ValorMontoMinimo;
		public String 		PorcentualFijo_CheckMontoMaximo;
		public BigDecimal 	PorcentualFijo_ValorMontoMaximo;
		public String 		PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal 	PorcentualTasaPlazo_ValorCobro;
		public String 		Otros_CheckOperacionContado;
		public String 		Otros_CheckOperacionPlazo;
		public String 		Otros_CheckBaseTasa;
		public Long 		Otros_ValorBaseTasa;
		public String 		Otros_CheckInteresSimple;
		public String 		Otros_CheckInteresCompuesto;
		public String 		Otros_CheckAllIn;
		public BigDecimal 	Otros_ValorAllInMontoOperacionBase;
		public String 		Otros_CheckAfectoImpuesto;
		public String 		Otros_CheckAplicaPortafolio;
		public String 		Otros_CheckAplicaCorresponsal;
		public String 		Otros_CheckPermiteAumentarValor;
		public String 		Otros_CheckPermiteDisminuirValor;
		public String 		PorcentualRango_IdMonedaRangoMonto;
		public String 		PorcentualRango_IdTipoRangoCobro;
		public String 		Otros_IncluyeExcluyeEmisoresCheck;
		public String 		Otros_IncluyeExcluyeInstrumentosCheck;
		public String 		Otros_IncluyeExcluyeCorresponsalesCheck;
		public String 		Otros_IncluyeExcluyeEmisoresValor;
		public String 		Otros_IncluyeExcluyeInstrumentosValor;
		public String 		Otros_IncluyeExcluyeCorresponsalesValor;
		public BigDecimal	ValorCobroCombinado;
		public String 		ValorCobroMonedaPorcentaje;
		public String 		IdMonedaCobroUI;
		public String 		UsoMonedaFormato;
		
		public String 		IdClasificacionTipoInstrumento;
		public String 		NombreClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionMercado {
		public String 		IdTipoOperacion;
		public String 		IdClasificacionTipoInstrumento;
		public String 		NombreClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarMatrizOperacional_Salida {
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public List<LineaMatrizOperacional> P_OUT_MATRIZOPERACIONAL;

		public ConsultarMatrizOperacional_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_MATRIZOPERACIONAL =  new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PortafolioProceso {
		public Long IdPortafolio;
		public String IdPerfilCobros;
		public String IdPerfilCobrosExtra;
		public String IdPerfilProductos;
		public String IdPerfilProductosExtra;
		public String IdPerfilCobrosPeru;
		public String IdPerfilCobrosPeruExtra;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LineaMatrizProceso {
		public Long IdInterno;
		public String IdTipoProducto;
		public String NombreTipoProducto;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdTipoCanalIngreso;
		public String NombreTipoCanalIngreso;
		public String IdBolsa;
		public String NombreBolsa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarMatrizOperacional_Entrada in = gson.fromJson(jin.toString(), ConsultarMatrizOperacional_Entrada.class);
		ConsultarMatrizOperacional_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public ConsultarMatrizOperacional_Salida logica(Connection conn,ConsultarMatrizOperacional_Entrada entrada) throws Exception {
		ConsultarMatrizOperacional_Salida salida = new ConsultarMatrizOperacional_Salida();
		SQL _sql = new SQL();
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		List<LineaMatrizProceso> _lineas = _sql.query(conn, SQL_CONSULTAR_DATOS, new HashMap<String,Object>(), LineaMatrizProceso.class);
		List<OperacionMercado> mercs = _sql.query(conn, SQL_CONSULTAR_MERCADOS, new HashMap<String,Object>(), OperacionMercado.class);
		
		
		String idPlantillaCobros = null;
		String idPlantillaCobrosExtra = null;
		String idPlantillaProductos = null;
		String idPlantillaProductosExtra = null;
		
		if (entrada.p_IdTipoEntidad.equalsIgnoreCase("PORTAFOLIO")) {
			HashMap<String,Object> params1 = new HashMap<>();
			params1.put("IdEntidad", entrada.p_IdEntidad);
			List<PortafolioProceso> p1 = _sql.query(conn, SQL_CONSULTAR_PLANTILLAS_PORTAFOLIO, params1, PortafolioProceso.class);
			if (p1.size() == 0) {
				throw new Exception("NO SE ENCONTRO PORTAFOLIO");
			}
			else {
				if (localizacionSistema.equalsIgnoreCase("CL")) {
					idPlantillaCobros = p1.get(0).IdPerfilCobros;
					idPlantillaCobrosExtra = p1.get(0).IdPerfilCobrosExtra;
				}
				if (localizacionSistema.equalsIgnoreCase("PE")) {
					idPlantillaCobros = p1.get(0).IdPerfilCobrosPeru;
					idPlantillaCobrosExtra = p1.get(0).IdPerfilCobrosPeruExtra;
				}
				idPlantillaProductos = p1.get(0).IdPerfilProductos;
				idPlantillaProductosExtra = p1.get(0).IdPerfilProductosExtra;
				
			}
			
		}
		else if (entrada.p_IdTipoEntidad.equalsIgnoreCase("PREVISUALIZAR")) {
			idPlantillaCobros = entrada.p_IdPlantillaCobros;
			idPlantillaProductos = entrada.p_IdPlantillaProductos;
		}
		//MODELO
		else {
			HashMap<String,Object> params1 = new HashMap<>();
			params1.put("IdEntidad", entrada.p_IdEntidad);
			List<PortafolioProceso> p1 = _sql.query(conn, SQL_CONSULTAR_PLANTILLAS_MODELO_COMERCIAL, params1, PortafolioProceso.class);
			if (p1.size() == 0) {
				throw new Exception("NO SE ENCONTRO PORTAFOLIO");
			}
			else {
				
				if (localizacionSistema.equalsIgnoreCase("CL")) {
					idPlantillaCobros = p1.get(0).IdPerfilCobros;
					idPlantillaCobrosExtra = p1.get(0).IdPerfilCobrosExtra;
				}
				if (localizacionSistema.equalsIgnoreCase("PE")) {
					idPlantillaCobros = p1.get(0).IdPerfilCobrosPeru;
					idPlantillaCobrosExtra = p1.get(0).IdPerfilCobrosPeruExtra;
				}
				
				idPlantillaProductos = p1.get(0).IdPerfilProductos;
				idPlantillaProductosExtra = p1.get(0).IdPerfilProductosExtra;
				
			}
		}
		
		//productos
		List<DetallePerfilProducto> productos = null;
		{
			ConsultarProductosPlantilla_Entrada entradaConsultarProductosPlantilla = new ConsultarProductosPlantilla_Entrada();
			ConsultarProductosPlantilla serviceConsultarProductosPlantilla = new ConsultarProductosPlantilla();
			entradaConsultarProductosPlantilla.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarProductosPlantilla.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarProductosPlantilla.p_IdUsuario = entrada.p_IdUsuario;
			List<PerfilProductoEntrada> perfiles = new ArrayList<>();
			
			if (idPlantillaProductos != null) {
				PerfilProductoEntrada p1 = new PerfilProductoEntrada();
				p1.IdPerfilProductos = idPlantillaProductos;
				perfiles.add(p1);
			}
			
			if (idPlantillaProductosExtra != null) {
				PerfilProductoEntrada p2 = new PerfilProductoEntrada();
				p2.IdPerfilProductos = idPlantillaProductosExtra;
				perfiles.add(p2);
			}
			
			if (perfiles.size() > 0) {
				entradaConsultarProductosPlantilla.p_Perfiles = perfiles;
				ConsultarProductosPlantilla_Salida asdf = serviceConsultarProductosPlantilla.logica(conn, entradaConsultarProductosPlantilla);
				productos = asdf.p_out_Productos;
			}
			else {
				
			}
		}
		
		//filtramos por plantilla de productos
		int index = 0;
		List<LineaMatrizProceso> lineas = new ArrayList<>();
		{
			for (LineaMatrizProceso linea : _lineas) {

				for (DetallePerfilProducto producto : productos) {
					
					if ((linea.IdTipoProducto.equalsIgnoreCase(producto.IdTipoProducto) || producto.IdTipoProducto.equalsIgnoreCase("0"))
							&& (linea.IdTipoOperacion.equalsIgnoreCase(producto.IdTipoOperacion) || producto.IdTipoOperacion.equalsIgnoreCase("0"))
							&& (linea.IdTipoCanalIngreso.equalsIgnoreCase(producto.IdTipoCanalIngreso) || producto.IdTipoCanalIngreso.equalsIgnoreCase("0"))
							&& producto.IdEstadoDetallePerfilProducto.equalsIgnoreCase("ACT")
							) {
						linea.IdInterno = new Long(index++);
						lineas.add(linea);
						break;
					}
					
				}	
			}
		}
		
		
		if (localizacionSistema.equalsIgnoreCase("CL")) {
			ConsultarCobrosPlantilla serviceConsultarCobrosPlantilla = new ConsultarCobrosPlantilla();
			ConsultarCobrosPlantilla_Entrada entradaConsultarCobrosPlantilla = new ConsultarCobrosPlantilla_Entrada();
			entradaConsultarCobrosPlantilla.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarCobrosPlantilla.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarCobrosPlantilla.p_IdUsuario = entrada.p_IdUsuario;
			List<InformacionOperacion> operaciones = new ArrayList<>();
			for (LineaMatrizProceso linea : lineas) {
				InformacionOperacion ope = new InformacionOperacion();
				
				ope.IdInterno = linea.IdInterno;
				ope.IdBolsa = linea.IdBolsa;
				ope.IdPerfilCobros = idPlantillaCobros;
				ope.IdPerfilCobrosExtra = idPlantillaCobrosExtra;
				ope.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
				ope.IdTipoOperacion = linea.IdTipoOperacion;
				ope.IdTipoProducto = linea.IdTipoProducto;
				
				operaciones.add(ope);
			}
			entradaConsultarCobrosPlantilla.p_Operaciones = operaciones ;
			ConsultarCobrosPlantilla_Salida cobros = serviceConsultarCobrosPlantilla.logica(conn, entradaConsultarCobrosPlantilla);
			
			for (LineaMatrizProceso lin : lineas) {
				
				int cantCobros = 0;
				
				for (cl.bcs.plataforma.com.ConsultarCobrosPlantilla.LineaCobroProceso cobro  : cobros.p_out_MatrizOperacional) {
					if (lin.IdInterno.equals(cobro.IdInterno)) {
						LineaMatrizOperacional lx = new LineaMatrizOperacional();
						Utils.copyPojo(cobro, lx);
						lx.IdBolsa = lin.IdBolsa;
						lx.NombreBolsa = lin.NombreBolsa;
						lx.IdTipoProducto = lin.IdTipoProducto;
						lx.NombreTipoProducto = lin.NombreTipoProducto;
						lx.IdTipoOperacion = lin.IdTipoOperacion;
						lx.NombreTipoOperacion = lin.NombreTipoOperacion;
						lx.IdTipoCanalIngreso = lin.IdTipoCanalIngreso;
						lx.NombreTipoCanalIngreso = lin.NombreTipoCanalIngreso;
						
						salida.P_OUT_MATRIZOPERACIONAL.add(lx);
						cantCobros++;
					}
				}
				
				if (cantCobros == 0) {
					LineaMatrizOperacional lx = new LineaMatrizOperacional();
					lx.CodigoCobroClasificacion = "SIN COBROS DEFINIDOS";
					lx.IdBolsa = lin.IdBolsa;
					lx.NombreBolsa = lin.NombreBolsa;
					lx.IdTipoProducto = lin.IdTipoProducto;
					lx.NombreTipoProducto = lin.NombreTipoProducto;
					lx.IdTipoOperacion = lin.IdTipoOperacion;
					lx.NombreTipoOperacion = lin.NombreTipoOperacion;
					lx.IdTipoCanalIngreso = lin.IdTipoCanalIngreso;
					lx.NombreTipoCanalIngreso = lin.NombreTipoCanalIngreso;
					
					salida.P_OUT_MATRIZOPERACIONAL.add(lx);
				}
				
				
			}
		}
		
		
		//COBROS PERU
		if (localizacionSistema.equalsIgnoreCase("PE")) {
			ConsultarCobrosPlantillaPeru serviceConsultarCobrosPlantillaPeru = new ConsultarCobrosPlantillaPeru();
			ConsultarCobrosPlantillaPeru_Entrada entradaConsultarCobrosPlantillaPeru = new ConsultarCobrosPlantillaPeru_Entrada();
			entradaConsultarCobrosPlantillaPeru.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarCobrosPlantillaPeru.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarCobrosPlantillaPeru.p_IdUsuario = entrada.p_IdUsuario;
			List<cl.bcs.plataforma.com.ConsultarCobrosPlantillaPeru.InformacionOperacion> operaciones = new ArrayList<>();
			for (LineaMatrizProceso linea : lineas) {
				cl.bcs.plataforma.com.ConsultarCobrosPlantillaPeru.InformacionOperacion ope = new cl.bcs.plataforma.com.ConsultarCobrosPlantillaPeru.InformacionOperacion();
				
				ope.IdInterno = linea.IdInterno;
				ope.IdBolsa = linea.IdBolsa;
				ope.IdPerfilCobros = idPlantillaCobros;
				ope.IdPerfilCobrosExtra = idPlantillaCobrosExtra;
				ope.IdTipoCanalIngreso = linea.IdTipoCanalIngreso;
				ope.IdTipoOperacion = linea.IdTipoOperacion;
				ope.IdTipoProducto = linea.IdTipoProducto;
				
				operaciones.add(ope);
			}
			entradaConsultarCobrosPlantillaPeru.p_Operaciones = operaciones ;
			ConsultarCobrosPlantillaPeru_Salida cobros = serviceConsultarCobrosPlantillaPeru.logica(conn, entradaConsultarCobrosPlantillaPeru);
			
			for (LineaMatrizProceso lin : lineas) {
				for (cl.bcs.plataforma.com.ConsultarCobrosPlantillaPeru.DefinicionCobro cobro  : cobros.p_out_MatrizOperacional) {
					if (lin.IdInterno.equals(cobro.IdInterno)) {
						LineaMatrizOperacional lx = new LineaMatrizOperacional();
						Utils.copyPojo(cobro, lx);
						lx.IdBolsa = lin.IdBolsa;
						lx.NombreBolsa = lin.NombreBolsa;
						lx.IdTipoProducto = lin.IdTipoProducto;
						lx.NombreTipoProducto = lin.NombreTipoProducto;
						lx.IdTipoOperacion = lin.IdTipoOperacion;
						lx.NombreTipoOperacion = lin.NombreTipoOperacion;
						lx.IdTipoCanalIngreso = lin.IdTipoCanalIngreso;
						lx.NombreTipoCanalIngreso = lin.NombreTipoCanalIngreso;
						
						//TIENE QUE COINCIDIR CON LA VISTA CONSULTARDETALLEPERFILCOBROS EN COM_2
						if (cobro.IdTipoCobro.equalsIgnoreCase("MONTOFIJO")) {
							lx.ValorCobroCombinado = cobro.MontoFijo_MontoCobro;
							lx.UsoMonedaFormato = "Monto";
							if (cobro.CheckCobroMonedaOperacion.equalsIgnoreCase("S")) {
								lx.ValorCobroMonedaPorcentaje = "MONEDA OPERACION";
								lx.IdMonedaCobroUI = monedaSistema;
							} else {
								lx.ValorCobroMonedaPorcentaje = cobro.IdMonedaCobroOtraMoneda;
								lx.IdMonedaCobroUI = cobro.IdMonedaCobroOtraMoneda;
							}
						}
						else if (cobro.IdTipoCobro.equalsIgnoreCase("PORCENTAJEFIJO")) {
							lx.ValorCobroCombinado = cobro.PorcentualFijo_ValorCobro;
							lx.UsoMonedaFormato = "Tasa";
							lx.ValorCobroMonedaPorcentaje = "%";
							lx.IdMonedaCobroUI = monedaSistema;
						}
						else if (cobro.IdTipoCobro.equalsIgnoreCase("PORCENTAJERANGOS")) {
							lx.ValorCobroCombinado = null;
							lx.UsoMonedaFormato = "Monto";
							lx.ValorCobroMonedaPorcentaje = "RANGO";
							lx.IdMonedaCobroUI = monedaSistema;
						}
						else if (cobro.IdTipoCobro.equalsIgnoreCase("PORCENTAJETASAPLAZO")) {
							lx.ValorCobroCombinado = cobro.PorcentualTasaPlazo_ValorCobro;
							lx.UsoMonedaFormato = "Tasa";
							lx.ValorCobroMonedaPorcentaje = "%";
							lx.IdMonedaCobroUI = monedaSistema;
						}
						
							
						
						
						
						
						salida.P_OUT_MATRIZOPERACIONAL.add(lx);
					}
				}
			}
		}
		
		for (LineaMatrizOperacional lx : salida.P_OUT_MATRIZOPERACIONAL) {
			for (OperacionMercado merc : mercs) {
				if (lx.IdTipoOperacion.equals(merc.IdTipoOperacion)) {
					lx.IdClasificacionTipoInstrumento = merc.IdClasificacionTipoInstrumento;
					lx.NombreClasificacionTipoInstrumento = merc.NombreClasificacionTipoInstrumento;
				}
			}
		}
		
		return salida;
	}
	
	
	
	public static void main(String[] args) throws SQLException, Exception {
		
		ConsultarMatrizOperacional_Entrada ent = new ConsultarMatrizOperacional_Entrada();
		
		ent.p_IdEmpresa = 1L;
		//ent.p_IdEntidad = "1448570";
		ent.p_IdEntidad = "153929";
		ent.p_IdPlantillaCobros = "";
		ent.p_IdPlantillaProductos = "";
		ent.p_IdTipoEntidad = "PORTAFOLIO";
		
		//String json = "{\"p_IdEmpresa\":1,\"p_IdTipoEntidad\":\"PREVISUALIZAR\",\"p_IdEntidad\":\"\",\"p_IdPlantillaCobros\":\"PRUEBA JANY\",\"p_IdPlantillaProductos\":\"PRUEBA JANY\",\"p_IdPlantillaCobrosPeru\":\"\"}";
		//Gson gson = Utils.getGsonBuilder();
		//ConsultarMatrizOperacional_Entrada ent = gson.fromJson(json, ConsultarMatrizOperacional_Entrada.class);

		 //de fuera
		 Gson gson2 = new Gson();
		 String viene = gson2.toJson(ent);
		 
		 System.out.println(viene);
		 ConsultarMatrizOperacional_Entrada ento = gson2.fromJson(viene, ConsultarMatrizOperacional_Entrada.class);

		//System.out.println(ento+"\n");
		
		ConsultarMatrizOperacional serv = new ConsultarMatrizOperacional();
		ConsultarMatrizOperacional_Salida asdf = serv.logica(PlataformaUtils.getDESAConnection(), ento);
		
		System.out.println(asdf.toString());
		
		
		
	}

}

package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.IndiceRentabilidadAtributtionAnalysis.ClasificacionRentabilidad;
import cl.bcs.cuentaInversion.IndiceRentabilidadAtributtionAnalysis.EntradaClasificacionAtributtion;
import cl.bcs.cuentaInversion.IndiceRentabilidadAtributtionAnalysis.SalidaIndice;
import cl.bcs.custodia.ConsultarCarteras.Cartera;
import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.DatosCartolaSocioNegocio;
import cl.bcs.normativa.cartola.MovimientosCaja;
import cl.bcs.normativa.cartola.MovimientosTitulos;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InformeAtributtionAnalisys implements JavaCall {

	private final String SQL_SELECT_CLASIFICACION = loadResourceToString(
			"cl/bcs/cuentaInversion/sql/InformeAtributtionAnalysis_Select_RelacionClasificacion.sql");
	private final String SQL_SELECT_ARBOL = loadResourceToString(
			"cl/bcs/cuentaInversion/sql/InformeAtributtionAnalysis_Select_ArbolSinPadre.sql");
	

	private static final String rentaVariable = "RV";

	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class InformeAtributtionAnalisys_Entrada {
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public List<Cliente> p_Datos;
		public String p_Moneda;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
		private static class Cliente {
		public Long IdCliente;
		public Long IdPortafolio;

		public String toString() {
			return Utils.printJson(this, false);
		}

		public Long getIdCliente() {
			return IdCliente;
		}

		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}

		public Long getIdPortafolio() {
			return IdPortafolio;
		}

		public void setIdPortafolio(Long idPortafolio) {
			IdPortafolio = idPortafolio;
		}

	}
	

	public static class ArbolClasificacion {
		public Long IdPortafolio; 
		public Long IdCliente; 
		public Date INGFechaOperacion; 
		public Date FechaOperacion; 
		public String MonedaReajuste; 
		public String INGAccion;
		public String EGRAccion;
		public String Nemotecnico; 
		public BigDecimal MontoDiv;
		public BigDecimal MontoCup;
		public BigDecimal MontoVenc;
		public BigDecimal MontoCompra; 
		public String IdClasificacionTipoInstrumento; 
		public BigDecimal ValorInstrumento; 
		public String Mercado; 
		public String SubMercado; 

		public String EsSimultanea; 
		public String EsVentaCorta; 
		public String EsAnticipo; 
		public String EsVencimiento; 
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Date FechaVencimientoOperacion;
		public BigDecimal MontoCierreAnterior;
		public BigDecimal INGCantidad;
		public BigDecimal ValorInstrumentoEGR;
		public BigDecimal INGPrecio = BigDecimal.ZERO;
		public BigDecimal EGRPrecio = BigDecimal.ZERO;
		public String 	IdEstado;
		public Date 	EGRFechaLiquidacion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class NivelesClasificacion {
		public String Mercado;
		public String SubMercado;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PreciosNemoClass {
		public String Nemotecnico;
		public BigDecimal MontoCompra;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UtilidadPerdida {
		public BigDecimal Compras = BigDecimal.ZERO;
		public BigDecimal Ventas = BigDecimal.ZERO;
		public BigDecimal Cupones = BigDecimal.ZERO;
		public BigDecimal Dividendos = BigDecimal.ZERO;
		public BigDecimal Vencimientos = BigDecimal.ZERO;
		public BigDecimal Spread = BigDecimal.ZERO;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaFilas {

		public String nemotecnico;
		public String monedaReajuste;
		public BigDecimal valorInstrumento;
		public BigDecimal utilidadPerdida;
		public BigDecimal porcentajePortafoliodesde;
		public BigDecimal porcentajePortafoliohasta;
		public BigDecimal rentabilidadMonedaReajuste;
		public BigDecimal contribucionalPortafolio;
		public String tipoFila;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	public static class TransformacionMonedas {
		public String TipoMonto;
		public String fecha;
		public String monedaOrigen;
		public String monedaDestino;
		public BigDecimal monto;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
		
	}
	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	// -------------------------------------------------------------------------------------------

	public static class InformeAtributtionAnalisys_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public HashMap<String, List<HashMap<String, Object>>> P_OUT_SALIDA = new HashMap<>();

		public InformeAtributtionAnalisys_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------

	public InformeAtributtionAnalisys_Salida logica(Connection conn, InformeAtributtionAnalisys_Entrada in)
			throws Exception {



// DEFINICION E INICIALIZACION DE VARIABLES
		BigDecimal montoacumulado =BigDecimal.ZERO;
		
		BigDecimal totalPatrimonioDesde =BigDecimal.ZERO;
		BigDecimal totalPatrimonioHasta =BigDecimal.ZERO;
		
		
		InformeAtributtionAnalisys_Salida salida = new InformeAtributtionAnalisys_Salida();
		List<HashMap<String, Object>> filas = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> salidaFilas = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> salidaFilasClasificacion = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> salidaFilasClasificacionUnion = new ArrayList<HashMap<String, Object>>();

		
		SQL sql = new SQL();

		LocalDate date = LocalDate.parse(in.p_FechaDesde);
		LocalDate fechaPrimeroDeMesLocalDate = date.withDayOfMonth(1);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa infoEmpresa = consultarEmpresa.logica(conn, in.p_IdEmpresa);

		InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
		fechasIn.fechaInicial = in.p_FechaDesde;
		fechasIn.numeroDias = 1L;
		InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
		inObtenerDiaAnterior.fechas.add(fechasIn);
		inObtenerDiaAnterior.idPais = infoEmpresa.IdPais;
		inObtenerDiaAnterior.diaHabil = "S";

		BigDecimal utilidad = BigDecimal.ZERO;
		BigDecimal sumaUtilidad = BigDecimal.ZERO;
		BigDecimal sumaUtilidadContribucion = BigDecimal.ZERO;
		BigDecimal sumautilidadSub = BigDecimal.ZERO;
		BigDecimal sumaContribucionSub = BigDecimal.ZERO; 
		BigDecimal montoDiaDesde  = BigDecimal.ZERO;
		BigDecimal montoDiaDesdeSub  = BigDecimal.ZERO;

		BigDecimal sumaDeCompras = BigDecimal.ZERO;
		BigDecimal sumaDeVentas  = BigDecimal.ZERO;
		BigDecimal sumaDeVencimientos  = BigDecimal.ZERO;
		BigDecimal sumaDeCupones  = BigDecimal.ZERO;

		BigDecimal monto = BigDecimal.ZERO;
		BigDecimal montoInicial = BigDecimal.ZERO;
		BigDecimal montoVolatil =BigDecimal.ZERO;
		BigDecimal valorPortafolioSubCabecera = BigDecimal.ZERO;
		BigDecimal pocentajePortafolioHasta = BigDecimal.ZERO;
		BigDecimal pocentajePortafolioDesde   = BigDecimal.ZERO;

		BigDecimal porcentajePortafolioDesdeSub = BigDecimal.ZERO;
		
		BigDecimal sumaPorcentajePortafolioHasta=BigDecimal.ZERO;
		BigDecimal sumaPorcentajePortafolioDesde=BigDecimal.ZERO;
		
		BigDecimal sumaRentabilidadMonedaReajuste=BigDecimal.ZERO;
		BigDecimal sumaRentabilidadMonedaReajusteSub=BigDecimal.ZERO;
		
		
		BigDecimal contribucionAlPortafolio =BigDecimal.ZERO;
		
		BigDecimal sumaPonderacionTitulosAbonos =BigDecimal.ZERO;
		BigDecimal sumaPonderacionTitulosCargos =BigDecimal.ZERO;

		BigDecimal sumaPonderacionCajaAbonos =BigDecimal.ZERO;
		BigDecimal sumaPonderacionCajaCargos =BigDecimal.ZERO;
		
		BigDecimal cien = new BigDecimal(100);
		
		try {

// OBTIENE DATOS DEL SOCIO DE NEGOCIO
			EntradaCartola entradaCartola = new EntradaCartola();
			entradaCartola.p_IdEmpresa 	= in.p_IdEmpresa;
			entradaCartola.p_IdModulo	= in.p_IdModulo;
			entradaCartola.p_IdUsuario 	= in.p_IdUsuario;
			entradaCartola.p_FechaDesde = Fecha.toDate(in.p_FechaDesde);
			entradaCartola.p_FechaHasta = Fecha.toDate(in.p_FechaHasta);
			for (Cliente cliente : in.p_Datos) { // validar que tenga datos isempty
				Dato dato = new Dato();
				dato.IdCliente = cliente.IdCliente;
				dato.IdPortafolio = cliente.IdPortafolio;
				entradaCartola.p_Datos.add(new Dato(cliente.IdCliente, cliente.IdPortafolio));
			}

			salida.P_OUT_SALIDA.put("datosSocioNegocio", getDatosSocioNegocio(conn, entradaCartola));

			
// OBTIENE MOVIMIENTOS DE TITULO (CARTERA)

			SalidaCartola salidaCartolaTitulos = new SalidaCartola();
			MovimientosTitulos movTitulos = new MovimientosTitulos();
			salidaCartolaTitulos = movTitulos.procesar(conn, entradaCartola);
			
// OBTIENE MOVIMIENTOS Y SALDO DE CUENTA INVERSION (de caja (abonos cargos)
			SalidaCartola salidaCartolaCaja = new SalidaCartola();
			MovimientosCaja movCaja = new MovimientosCaja();
			salidaCartolaCaja = movCaja.procesar(conn, entradaCartola);

			/// Trae datos desde BD arbol y relacion clasificacion
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();

			// Considera todos los clientes y portafolios segun los parametros establecidos
			List<Long> portafolios = new ArrayList<Long>();
			List<Long> clientes = new ArrayList<Long>();

			for (Cliente dato : in.p_Datos) {
				clientes.add(dato.IdCliente); // Lista de Clientes
				portafolios.add(dato.IdPortafolio); // Lista de Portafolios
			}
			parametroEntrada.put("p_FechaDesde", in.p_FechaDesde);
			parametroEntrada.put("p_FechaHasta", in.p_FechaHasta);
			parametroEntrada.put("p_IdPortafolio", portafolios);
			parametroEntrada.put("p_IdCliente", clientes);
			parametroEntrada.put("p_FechaAnterior", fechaPrimeroDeMesLocalDate);
			List<ArbolClasificacion> arbol = sql.query(conn, SQL_SELECT_CLASIFICACION, parametroEntrada,
					ArbolClasificacion.class);
			
			//precio tasa para obtener valorizado a precio de mercado
			HashMap<String, Object> hashSalidaPrecioTasa = new HashMap<String,Object>();
			EntregarPreciosTasas precio = new EntregarPreciosTasas();
			EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas_Entrada();
			paramsPrecioTasa.p_IdEmpresa 	= in.p_IdEmpresa;
			paramsPrecioTasa.p_IdUsuario	= in.p_IdUsuario;
			paramsPrecioTasa.p_IdModulo 	= in.p_IdModulo;
			paramsPrecioTasa.p_IdPais 		= ConfiguracionSistema.getParametroConfiguracionLong(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
			
			List<ArbolClasificacion> carterasRV = arbol.parallelStream().filter(car->"RV".equalsIgnoreCase(car.IdClasificacionTipoInstrumento)).collect(Collectors.toList());
			
			List<Long> secuencias = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas",new Long(carterasRV.size()*2));
			
			AtomicInteger i = new AtomicInteger(0);
			
			paramsPrecioTasa.p_DatosInstrumentos = carterasRV.parallelStream().map(car -> {
				EntregarPreciosTasas_Entrada_DatosInstrumentos ins = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
				ins.IdRegistro 			= secuencias.get(i.getAndIncrement());
				ins.TipoCartera 		= "0";
				ins.IdUso				= "CARTERA";
				ins.IdTipoCriterioTasa 	= "0";
				ins.IdClasificacionTipoInstrumento = car.IdClasificacionTipoInstrumento;
				ins.IdTipoInstrumento	= car.IdTipoInstrumento;
				ins.IdInstrumento		= car.IdInstrumento;
				ins.Nemotecnico			= car.Nemotecnico;
				ins.IdMoneda			= car.MonedaReajuste;
				ins.FechaConsulta 		= Fecha.toDate(in.p_FechaHasta);
				ins.PlazoResidual		= 0L;
				return ins;
			}).collect(Collectors.toList());
			
			paramsPrecioTasa.p_DatosInstrumentos.addAll(carterasRV.parallelStream().map(car -> {
				EntregarPreciosTasas_Entrada_DatosInstrumentos ins = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
				ins.IdRegistro 			= secuencias.get(i.getAndIncrement());
				ins.TipoCartera 		= "0";
				ins.IdUso				= "CARTERA";
				ins.IdTipoCriterioTasa 	= "0";
				ins.IdClasificacionTipoInstrumento = car.IdClasificacionTipoInstrumento;
				ins.IdTipoInstrumento	= car.IdTipoInstrumento;
				ins.IdInstrumento		= car.IdInstrumento;
				ins.Nemotecnico			= car.Nemotecnico;
				ins.IdMoneda			= car.MonedaReajuste;
				ins.FechaConsulta 		= Fecha.toDate(in.p_FechaDesde);
				ins.PlazoResidual		= 0L;
				return ins;
			}).collect(Collectors.toList()));
			
			EntregarPreciosTasas_Salida precioSalida = precio.logica(conn, paramsPrecioTasa);
			for(EntregarPreciosTasas_Salida_PreciosTasas dato : precioSalida.P_OUT_PRECIOSTASAS){
				String fechaFuente = dato.FechaFuente.toString().substring(0,10);
				hashSalidaPrecioTasa.putIfAbsent(dato.Nemotecnico + "_" + fechaFuente, dato);
			}

			Map<String, Map<String, List<ArbolClasificacion>>> mercados = arbol.stream().collect(Collectors.groupingBy(arb -> arb.Mercado, Collectors.groupingBy(arb -> arb.SubMercado)));
			List<HashMap<String, Object>> mercadoTemporal = new ArrayList<HashMap<String, Object>>();

			String mercadoAux = "";
			for (String mercado : mercados.keySet()) {
				System.out.println("MERCADO: " + mercado);
				
				if(!mercadoAux.equals(mercado)) {
					sumaPorcentajePortafolioHasta = BigDecimal.ZERO;
					
					sumaRentabilidadMonedaReajuste = BigDecimal.ZERO;
					
					
					sumaPorcentajePortafolioDesde = BigDecimal.ZERO;
					montoacumulado = BigDecimal.ZERO;
					sumaUtilidad = BigDecimal.ZERO;
					sumaUtilidadContribucion = BigDecimal.ZERO;
				}
				
				//Agrega linea de cabecera en detalle del informe atributtions (A nivel de Mercado)
				List<HashMap<String, Object>> cabecera = agregarLineaSalidaMercado(mercado, BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
				salidaFilas.addAll(cabecera);
				List<HashMap<String, Object>> cabeceraSub = new ArrayList<HashMap<String, Object>>();
				List<HashMap<String, Object>> cabeceraDetalle = new ArrayList<HashMap<String, Object>>();
				// salida para el cuadro rentabilidad por nivel
				List<HashMap<String, Object>> cabeceraClasificacion = agregarLineaTablaClasificacion(mercado, BigDecimal.ZERO);
				salidaFilasClasificacion.addAll(cabeceraClasificacion);
				
				
				List<HashMap<String, Object>> cabeceraClasificacionSub = new ArrayList<HashMap<String, Object>>();
				
				for (String subMercado : mercados.get(mercado).keySet()) {

					System.out.println("\tSUBMERCADO: " + subMercado);
					
					
					
					// Si hay SubMercado, agrega linea de cabecera 
					if (!(subMercado == "")) {
						// Agrega linea de cabecera para cuadro de Clasificacion Rentabilidad (a nivel de SubMercado)
						cabeceraClasificacionSub = agregarLineaTablaClasificacion(subMercado, BigDecimal.ZERO);
						salidaFilasClasificacion.addAll(cabeceraClasificacionSub);
						
						// Agrega linea de cabecera en detalle del informe atributtions (A nivel de SubMercado)
						cabeceraSub  = agregarLineaSalidaSubMercado(subMercado, BigDecimal.ZERO, BigDecimal.ZERO,
								BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
						salidaFilas.addAll(cabeceraSub);
						
					}
					
					Map<String, List<ArbolClasificacion>> unificacion = mercados.get(mercado).get(subMercado)
							.parallelStream().collect(Collectors.groupingBy(arb -> arb.Nemotecnico));

					for (Map.Entry<String, List<ArbolClasificacion>> entry : unificacion.entrySet()) { /// instrumentos
						List<ArbolClasificacion> lista = entry.getValue();

						UtilidadPerdida utilidadperdida = new UtilidadPerdida();

				
						BigDecimal patrimonioIncial = BigDecimal.ZERO;
						
						
						for (ArbolClasificacion listaDetalle : lista) {
							
							if(listaDetalle.IdClasificacionTipoInstrumento.equals("RV")){
								EntregarPreciosTasas_Salida_PreciosTasas hashRegDesde = (EntregarPreciosTasas_Salida_PreciosTasas)hashSalidaPrecioTasa.get(listaDetalle.Nemotecnico + "_" + in.p_FechaDesde);
								int compareFechas = hashRegDesde.FechaFuente.toString().substring(0,10).compareTo(listaDetalle.FechaOperacion.toString());
								if(hashRegDesde.Precio != null && compareFechas == 0) {
									listaDetalle.INGPrecio = hashRegDesde.Precio;
									listaDetalle.ValorInstrumento = listaDetalle.INGCantidad.multiply((hashRegDesde.Precio));
								}
								EntregarPreciosTasas_Salida_PreciosTasas hashRegHasta = (EntregarPreciosTasas_Salida_PreciosTasas)hashSalidaPrecioTasa.get(listaDetalle.Nemotecnico + "_" + in.p_FechaHasta);
								if(hashRegHasta.Precio != null) {
									listaDetalle.EGRPrecio = "VIG".equalsIgnoreCase(listaDetalle.IdEstado)? hashRegHasta.Precio : BigDecimal.ZERO;
									listaDetalle.ValorInstrumentoEGR = listaDetalle.INGCantidad.multiply(listaDetalle.EGRPrecio);
								}
							}
							
							listaDetalle.ValorInstrumento=(listaDetalle.ValorInstrumento==null)?BigDecimal.ZERO:listaDetalle.ValorInstrumento;
							listaDetalle.MontoVenc=(listaDetalle.MontoVenc==null)?BigDecimal.ZERO:listaDetalle.MontoVenc;
							if(!listaDetalle.MonedaReajuste.equals(in.p_Moneda)){
								
								
								listaDetalle.ValorInstrumento = TransformadorMonedas(conn,in.p_FechaHasta.toString(),listaDetalle.MonedaReajuste, in.p_Moneda,(BigDecimal) listaDetalle.ValorInstrumento);
								listaDetalle.MontoVenc        = TransformadorMonedas(conn,listaDetalle.INGFechaOperacion.toString(),listaDetalle.MonedaReajuste, in.p_Moneda,(BigDecimal) listaDetalle.MontoVenc);
								listaDetalle.MontoCup         = TransformadorMonedas(conn,listaDetalle.INGFechaOperacion.toString(),listaDetalle.MonedaReajuste, in.p_Moneda,(BigDecimal) listaDetalle.MontoCup);
								listaDetalle.MontoDiv         = TransformadorMonedas(conn,listaDetalle.INGFechaOperacion.toString(),listaDetalle.MonedaReajuste, in.p_Moneda,(BigDecimal) listaDetalle.MontoDiv);
	
							}
							listaDetalle.ValorInstrumento=(listaDetalle.ValorInstrumento==null)?BigDecimal.ZERO:listaDetalle.ValorInstrumento;
							listaDetalle.MontoVenc=(listaDetalle.MontoVenc==null)?BigDecimal.ZERO:listaDetalle.MontoVenc;
							if ((listaDetalle.INGAccion.equals("COMPRA") || listaDetalle.EGRAccion.equals("COMPRA")) && listaDetalle.EsSimultanea.equals("N")
									&& listaDetalle.EsVentaCorta.equals("N") && listaDetalle.EsAnticipo.equals("N")
									&& listaDetalle.EsVencimiento.equals("N")) {
	
									utilidadperdida.Compras = utilidadperdida.Compras.add(listaDetalle.ValorInstrumento,MathContext.DECIMAL64);
									sumaDeCompras = utilidadperdida.Compras;
							}
							if ((listaDetalle.INGAccion.equals("VENTA") || listaDetalle.EGRAccion.equals("VENTA")) && listaDetalle.EsSimultanea.equals("N")
									&& listaDetalle.EsVentaCorta.equals("N") && listaDetalle.EsAnticipo.equals("N")
									&& listaDetalle.EsVencimiento.equals("N")) {
								
									utilidadperdida.Ventas = utilidadperdida.Ventas.add((listaDetalle.INGAccion.equals("COMPRA") && listaDetalle.EGRAccion.equals("VENTA"))?listaDetalle.ValorInstrumentoEGR:listaDetalle.ValorInstrumento,MathContext.DECIMAL64);
									sumaDeVentas = utilidadperdida.Ventas;
							}
							if (listaDetalle.EsVencimiento.equals("S")) {

									utilidadperdida.Vencimientos = utilidadperdida.Vencimientos.add(listaDetalle.ValorInstrumento,MathContext.DECIMAL64);
									sumaDeVencimientos = utilidadperdida.Ventas;
							}
							
							utilidadperdida.Cupones = listaDetalle.MontoCup;
							utilidadperdida.Dividendos = listaDetalle.MontoDiv;
							
							listaDetalle.MontoCup=(listaDetalle.MontoCup==null)?BigDecimal.ZERO:listaDetalle.MontoCup;
							sumaDeCupones    = sumaDeCupones.add(listaDetalle.MontoCup);
						
							if(listaDetalle.IdEstado.equalsIgnoreCase("VIG") || (!listaDetalle.IdEstado.equalsIgnoreCase("VIG") && listaDetalle.EGRFechaLiquidacion.toString().compareTo(in.p_FechaHasta) > 0)) {
								monto = monto.add(listaDetalle.ValorInstrumentoEGR,MathContext.DECIMAL64);
							}
							
							if(listaDetalle.FechaOperacion.toString().equals(in.p_FechaDesde)){
								montoInicial = montoInicial.add(listaDetalle.ValorInstrumento);
							}
							//lamar a la tabla de la caro pasando el dia anterior a el inicio del periodo consultado asi obtener el patrimonio inicial.
							
							if(!cabeceraSub.isEmpty()){
								montoVolatil = montoVolatil.add(listaDetalle.ValorInstrumento, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR);
							}
							
						}
						
						lista = lista.parallelStream().sorted(new Comparator<ArbolClasificacion>() {
							@Override
							public int compare(ArbolClasificacion mov1, ArbolClasificacion mov2) {
								if (mov1.INGFechaOperacion.compareTo(mov2.INGFechaOperacion) == 0) {
									return 0;
								}
								return mov2.INGFechaOperacion.compareTo(mov1.INGFechaOperacion) == 1 ? 1 : -1;
							}
						}).collect(Collectors.toList());

						if (!lista.isEmpty()) {
						
							ArbolClasificacion listaObtenerGetCero = lista.get(0);
							Date dateDesde = format.parse(in.p_FechaDesde);
							Date dateHasta = format.parse(in.p_FechaHasta);

							BigDecimal montoInicialCar = BigDecimal.ZERO;
							BigDecimal montoFinalCar = BigDecimal.ZERO;
							BigDecimal precioInicial = BigDecimal.ZERO;
							BigDecimal precioFinal = BigDecimal.ZERO;
							BigDecimal monedaRentabilidad = BigDecimal.ZERO;
							
							//****** INICIO CALCULO monedaRentabilidad
							if (!listaObtenerGetCero.IdClasificacionTipoInstrumento.equals(rentaVariable)){
								montoFinalCar = lista.get(lista.size() - 1).ValorInstrumentoEGR;
								precioFinal   = lista.get(lista.size() - 1).EGRPrecio;
								if(listaObtenerGetCero.FechaOperacion.compareTo(dateDesde) == 0) {
									montoInicialCar = listaObtenerGetCero.ValorInstrumento;
									precioInicial = listaObtenerGetCero.INGPrecio;
								}
								
								if (precioInicial.compareTo(BigDecimal.ZERO) != 0) {
									BigDecimal monedaRentabilidadp1 = precioFinal.subtract(precioInicial);
									BigDecimal monedaRentabilidadp2 = monedaRentabilidadp1.subtract(utilidadperdida.Compras);
									BigDecimal monedaRentabilidadp3 = monedaRentabilidadp2.add(utilidadperdida.Ventas).add(utilidadperdida.Cupones).add(utilidadperdida.Vencimientos);
									monedaRentabilidad = monedaRentabilidadp3.divide(precioInicial,BigDecimal.ROUND_CEILING).multiply(cien).setScale(2, RoundingMode.HALF_UP);
									if(!cabeceraSub.isEmpty()){
										cabeceraSub.get(0).put("utilidadPerdida",utilidad.setScale(2, RoundingMode.FLOOR));
									}
								} else {
									monedaRentabilidad = BigDecimal.ZERO;
								}
							} else {
								// Si el papel fue comprado antes del periodo seleccionado corresponde al valor
								// de cierre del mes anterior.

								montoFinalCar = lista.get(lista.size() - 1).ValorInstrumentoEGR;
								precioFinal   = lista.get(lista.size() - 1).EGRPrecio;
								if(listaObtenerGetCero.FechaOperacion.compareTo(dateDesde) == 0) {
									montoInicialCar = listaObtenerGetCero.ValorInstrumento;
									precioInicial = listaObtenerGetCero.INGPrecio;
								}
								
								if (dateDesde.after(listaObtenerGetCero.FechaOperacion)) {
									precioInicial = listaObtenerGetCero.MontoCierreAnterior;
									if(precioInicial.compareTo(BigDecimal.ZERO)==0) {
										precioInicial=BigDecimal.ONE;
									}
									monedaRentabilidad = precioFinal.subtract(precioInicial).divide(precioInicial,BigDecimal.ROUND_CEILING).multiply(cien);
								} else if ((dateDesde.before(listaObtenerGetCero.FechaOperacion) && dateHasta.after(listaObtenerGetCero.FechaOperacion))
										|| (dateDesde.equals(listaObtenerGetCero.FechaOperacion) || dateHasta.equals(listaObtenerGetCero.FechaOperacion))) {
									if (precioInicial.compareTo(BigDecimal.ZERO) != 0) {
										monedaRentabilidad = precioFinal.subtract(precioInicial).divide(precioInicial,BigDecimal.ROUND_CEILING).multiply(cien).setScale(2, RoundingMode.FLOOR);
									} else {
										monedaRentabilidad = BigDecimal.ZERO;
									}
								}
							}
							
							// validar si viene un solo registro y mantener si pasa el ultimo el Vfinal
							if(!listaObtenerGetCero.ValorInstrumento.toString().equals(lista.get(lista.size() - 1).ValorInstrumento.toString()) &&  cabeceraSub.isEmpty()) {
								utilidad = montoFinalCar.subtract(montoInicialCar).subtract(utilidadperdida.Compras).add(utilidadperdida.Ventas).add(utilidadperdida.Cupones).add(utilidadperdida.Dividendos).add(utilidadperdida.Vencimientos).setScale(2, RoundingMode.FLOOR);
								
								if(!cabeceraSub.isEmpty()){
									cabeceraSub.get(0).put("utilidadPerdida",utilidad.setScale(2, RoundingMode.FLOOR));
								}
							}else {
								utilidadperdida.Compras=(utilidadperdida.Compras==null)?BigDecimal.ZERO:utilidadperdida.Compras;
								utilidadperdida.Ventas=(utilidadperdida.Ventas==null)?BigDecimal.ZERO:utilidadperdida.Ventas;
								utilidadperdida.Cupones=(utilidadperdida.Cupones==null)?BigDecimal.ZERO:utilidadperdida.Cupones;
								utilidadperdida.Dividendos=(utilidadperdida.Dividendos==null)?BigDecimal.ZERO:utilidadperdida.Dividendos;
								utilidadperdida.Vencimientos=(utilidadperdida.Vencimientos==null)?BigDecimal.ZERO:utilidadperdida.Vencimientos;
								utilidad = montoFinalCar.subtract(montoInicialCar).subtract(utilidadperdida.Compras).add(utilidadperdida.Ventas).add(utilidadperdida.Cupones).add(utilidadperdida.Dividendos).add(utilidadperdida.Vencimientos).setScale(2, RoundingMode.FLOOR);
								
								if(!cabeceraSub.isEmpty()){
									cabeceraSub.get(0).put("utilidadPerdida",utilidad.setScale(2, RoundingMode.FLOOR));
								}
							}
							BigDecimal precioDiaDesde = lista.get(0).INGPrecio;
							BigDecimal precioDiaHasta = lista.get(lista.size() - 1).EGRPrecio;
							
							if(!cabeceraSub.isEmpty()){ 
								
								montoDiaDesdeSub = montoDiaDesde.add(precioDiaDesde).setScale(2,BigDecimal.ROUND_UP);
							}else {
								montoDiaDesde = montoDiaDesde.add(precioDiaDesde).setScale(2,BigDecimal.ROUND_UP);
							}

							BigDecimal valorPortafolioCabecera = (BigDecimal) cabecera.get(0).get("ValorInstrumento");
							if (!cabeceraSub.isEmpty()) {
								valorPortafolioSubCabecera = (BigDecimal) cabeceraSub.get(0).get("ValorInstrumento");

								cabeceraSub.get(0).put("ValorInstrumento",valorPortafolioSubCabecera.add(montoVolatil, MathContext.DECIMAL64));
								
							}

							cabecera.get(0).put("ValorInstrumento",
									valorPortafolioCabecera.add(monto, MathContext.DECIMAL64));

							cabecera.get(0).put("utilidadPerdida",
									valorPortafolioCabecera.add(utilidad, MathContext.DECIMAL64)); ////////

							cabecera.get(0).put("ValorInstrumento",
									valorPortafolioSubCabecera.add(monto, MathContext.DECIMAL64));
							
							/*
							if (monto.compareTo(BigDecimal.ZERO) == 0) {
								monto = BigDecimal.ONE;
							}*/


							if(monto.compareTo(BigDecimal.ZERO)==0) {
								pocentajePortafolioHasta = BigDecimal.ZERO;
							}else {
								pocentajePortafolioHasta = (precioDiaHasta.divide(monto, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR));
							}
							
							cabecera.get(0).put("porcentajePortafoliohasta",valorPortafolioSubCabecera.add(pocentajePortafolioHasta, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR));
							
							if(!cabeceraSub.isEmpty()){
								cabeceraSub.get(0).put("porcentajePortafoliodesde",valorPortafolioSubCabecera.add(pocentajePortafolioDesde, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR));
							}

							if(!cabeceraSub.isEmpty()){
							cabeceraSub.get(0).put("porcentajePortafoliohasta",valorPortafolioSubCabecera.add(pocentajePortafolioHasta, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR));
							}

							BigDecimal montoCaja = BigDecimal.ZERO;
							BigDecimal montoTitulos = BigDecimal.ZERO;
							SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
							BigDecimal ponderacionTitulosAbonos = BigDecimal.ZERO;
							BigDecimal ponderacionTitulosCargos = BigDecimal.ZERO;

							BigDecimal ponderacionCajaAbonos = BigDecimal.ZERO;
							BigDecimal ponderacionCajaCargos = BigDecimal.ZERO;

							// Corresponde a : Fecha final - Fecha inicial (B)
							Date fechaInicialB = dateFormat2.parse(in.p_FechaDesde);
							Date fechaFinalB = dateFormat2.parse(in.p_FechaHasta);
							long diasB = diferenciaEnDias(fechaFinalB, fechaInicialB);
							BigDecimal diferenciaDiasB = new BigDecimal(diasB);
							if(diferenciaDiasB.compareTo(BigDecimal.ZERO)==0)
							{
								diferenciaDiasB=BigDecimal.ONE;
							}

							// titulos
							List<HashMap<String, Object>> titulos = (List<HashMap<String, Object>>) salidaCartolaTitulos.P_OUT_SALIDA.get("movimientosTitulos");

							if (!titulos.isEmpty()) {
								for (HashMap<String, Object> itemTitulos : titulos) {
									if(!(itemTitulos.get("NombreTipoOperacion")==null) && !(itemTitulos.get("TipoConcepto")==null) && !(itemTitulos.get("Nemotecnico")==null)){

										if (itemTitulos.get("Nemotecnico").toString().equals(listaObtenerGetCero.Nemotecnico.toString())) {
											
											if (itemTitulos.get("Accion").equals("INGRESO")) {
												montoTitulos = (BigDecimal) itemTitulos.get("MontoCompraMonedaInstrumento");
												if(!itemTitulos.get("IdMonedaInstrumento").toString().equals(in.p_Moneda)) {
													montoTitulos = TransformadorMonedas(conn,itemTitulos.get("FechaOperacion").toString(),itemTitulos.get("IdMonedaInstrumento").toString(), in.p_Moneda,(BigDecimal) itemTitulos.get("MontoMercadoMonedaLocal"));
												}

												// Corresponde a: Fecha final - Fecha del Movimiento (A)
												Date fechaInicialA = dateFormat.parse(itemTitulos.get("FechaOperacion").toString());
												Date fechaFinalA = dateFormat2.parse(in.p_FechaHasta);
												long diasA = diferenciaEnDias(fechaFinalA, fechaInicialA);
												BigDecimal diferenciaDiasA = new BigDecimal(diasA);
												if(montoTitulos == null) {
													ponderacionTitulosAbonos = BigDecimal.ZERO;
												}
												else {
													ponderacionTitulosAbonos = montoTitulos.multiply(diferenciaDiasA).divide(diferenciaDiasB, BigDecimal.ROUND_CEILING).setScale(2, RoundingMode.FLOOR);
												}
												
												sumaPonderacionTitulosAbonos = sumaPonderacionTitulosAbonos.add(ponderacionTitulosAbonos);
											
											} else if (!itemTitulos.get("TipoConcepto").equals(null) && itemTitulos.get("Accion").equals("RETIRO")) {
												montoTitulos = (BigDecimal) itemTitulos.get("MontoMercadoMonedaLocal");
												if(!itemTitulos.get("IdMonedaInstrumento").toString().equals(in.p_Moneda)) {
													montoTitulos = TransformadorMonedas(conn,itemTitulos.get("FechaOperacion").toString(),itemTitulos.get("IdMonedaInstrumento").toString(), in.p_Moneda,(BigDecimal) itemTitulos.get("MontoMercadoMonedaLocal"));
												}
												
												// Corresponde a: Fecha final - Fecha del Movimiento (A)

												Date fechaInicialA = dateFormat.parse(itemTitulos.get("FechaOperacion").toString());
												Date fechaFinalA = dateFormat2.parse(in.p_FechaHasta);
												long diasA = diferenciaEnDias(fechaFinalA, fechaInicialA);
												BigDecimal diferenciaDiasA = new BigDecimal(diasA);

												if(montoTitulos == null) {
													ponderacionTitulosCargos = BigDecimal.ZERO;
												}
												else {
													ponderacionTitulosCargos = (montoTitulos.multiply(diferenciaDiasA).divide(diferenciaDiasB, BigDecimal.ROUND_CEILING).setScale(2, RoundingMode.FLOOR));
												}
												
												sumaPonderacionTitulosCargos = sumaPonderacionTitulosCargos.add(ponderacionTitulosCargos);
											}
										}
									}
								}
							}

							// mov caja
							List<HashMap<String, Object>> caja =  (List<HashMap<String, Object>>) salidaCartolaCaja.P_OUT_SALIDA.get("movimientosCaja");
							if (!caja.isEmpty()) {
								for (HashMap<String, Object> itemCaja : caja) {

									if (!listaObtenerGetCero.Nemotecnico.equals("") && !(itemCaja.get("Moneda")==null)) {

										if (itemCaja.get("Nemotecnico").equals(listaObtenerGetCero.Nemotecnico)) {

											if (itemCaja.get("AfectaRentabilidad").equals("S")
													&& itemCaja.get("NombreTipoMovimiento").equals("ABONO")
													&& itemCaja.get("Nemotecnico").equals(listaObtenerGetCero.Nemotecnico)) {

												if (itemCaja.get("Moneda").equals(in.p_Moneda)) {
													montoCaja = montoCaja.add((BigDecimal) itemCaja.get("Abono"));
												} else {
													montoCaja = montoCaja.add(TransformadorMonedas(conn,itemCaja.get("FechaOperacion").toString(),
															itemCaja.get("Moneda").toString(), in.p_Moneda,(BigDecimal) itemCaja.get("Abono")));
												}

												Date fechaInicialA = dateFormat.parse(itemCaja.get("FechaOperacion").toString());
												Date fechaFinalA = dateFormat2.parse(in.p_FechaHasta);
												long diasA = diferenciaEnDias(fechaFinalA, fechaInicialA);
												BigDecimal diferenciaDiasA = new BigDecimal(diasA);
												ponderacionCajaAbonos = montoCaja.multiply(diferenciaDiasA).divide(diferenciaDiasB,BigDecimal.ROUND_CEILING).setScale(2, RoundingMode.FLOOR);

												sumaPonderacionCajaAbonos = sumaPonderacionCajaAbonos.add(ponderacionCajaAbonos);
												
												} else if (itemCaja.get("AfectaRentabilidad").equals("S")
													&& itemCaja.get("NombreTipoMovimiento").equals("CARGO")
													&& itemCaja.get("Nemotecnico").equals(listaObtenerGetCero.Nemotecnico)) {
												
												if (itemCaja.get("Moneda").equals(in.p_Moneda)) {
													montoCaja = montoCaja.add((BigDecimal) itemCaja.get("Cargo"));
												} else {
													montoCaja = montoCaja.add(TransformadorMonedas(conn,itemCaja.get("FechaOperacion").toString(),
														itemCaja.get("Moneda").toString(), in.p_Moneda,(BigDecimal) itemCaja.get("Cargo")));
												}

												Date fechaInicialA = dateFormat.parse(itemCaja.get("FechaOperacion").toString());
												Date fechaFinalA = dateFormat2.parse(in.p_FechaHasta);
												long diasA = diferenciaEnDias(fechaFinalA, fechaInicialA);
												BigDecimal diferenciaDiasA = new BigDecimal(diasA);
												ponderacionCajaCargos = (montoCaja.multiply(diferenciaDiasA).divide(diferenciaDiasB,BigDecimal.ROUND_CEILING).setScale(2, RoundingMode.FLOOR));

												sumaPonderacionCajaCargos = sumaPonderacionCajaCargos.add(ponderacionCajaCargos);
											}
										}
									}
								}
							}
							montoacumulado= montoacumulado.add(monto);
							
							totalPatrimonioHasta= totalPatrimonioHasta.add(monto);
							if(listaObtenerGetCero.FechaOperacion.toString().compareTo(in.p_FechaDesde) <= 0) {
								totalPatrimonioDesde = totalPatrimonioDesde.add(montoInicial);
							}
							
							
							BigDecimal ponderacionAbonos = sumaPonderacionTitulosAbonos.add(sumaPonderacionCajaAbonos);
							BigDecimal ponderacionCargos = sumaPonderacionTitulosCargos.add(sumaPonderacionCajaCargos);
							
							if (patrimonioIncial.compareTo(BigDecimal.ZERO) == 0) {
								patrimonioIncial = BigDecimal.ONE;
							}
							BigDecimal dividendo = montoInicial.add(ponderacionAbonos).subtract(ponderacionCargos);
							if(!(dividendo.compareTo(BigDecimal.ZERO)==0)) {
								contribucionAlPortafolio = utilidad.divide(dividendo,2,RoundingMode.HALF_UP).multiply(cien);
							}else {
								contribucionAlPortafolio = BigDecimal.ZERO;
							}
							
						
							BigDecimal porcentajePortafolioDesde = BigDecimal.ZERO;
							
							if(listaObtenerGetCero.FechaOperacion.toString().compareTo(in.p_FechaDesde) <= 0) {
								porcentajePortafolioDesde = listaObtenerGetCero.ValorInstrumento; // Por ahora se guarda MontoCompra de fechaDesde, mas abajo se recalcula porcentajePortafolioDesde usando este mismo valor dividido por totalPatrimonio 
								sumaPorcentajePortafolioDesde= sumaPorcentajePortafolioDesde.add(porcentajePortafolioDesde);
							}
							
							sumaRentabilidadMonedaReajuste= sumaRentabilidadMonedaReajuste.add(monedaRentabilidad);
							if(!cabeceraSub.isEmpty()){
								porcentajePortafolioDesdeSub = porcentajePortafolioDesdeSub.add(porcentajePortafolioDesde);
								sumaRentabilidadMonedaReajusteSub = sumaRentabilidadMonedaReajusteSub.add(monedaRentabilidad);
								sumautilidadSub =sumautilidadSub.add(utilidad);
								sumaContribucionSub = sumaContribucionSub.add(contribucionAlPortafolio);
							}

							
							cabeceraDetalle=agregarLineaSalida(entry.getKey(), lista.get(0).MonedaReajuste, monto,utilidad, porcentajePortafolioDesde.setScale(2, RoundingMode.FLOOR), BigDecimal.ZERO, monedaRentabilidad,contribucionAlPortafolio, "detalle");
							
							
							salidaFilas.addAll(cabeceraDetalle);

							monto=BigDecimal.ZERO; 
							sumaUtilidad= sumaUtilidad.add(utilidad);
							sumaUtilidadContribucion = sumaUtilidadContribucion.add(contribucionAlPortafolio);
							utilidad =BigDecimal.ZERO;

						}
					}
				}
				
				cabecera.get(0).put("porcentajePortafoliodesde",sumaPorcentajePortafolioDesde);
				cabecera.get(0).put("porcentajePortafoliohasta",sumaPorcentajePortafolioHasta);
				cabecera.get(0).put("rentabilidadMonedaReajuste",sumaRentabilidadMonedaReajuste);
				if(!cabeceraSub.isEmpty()){
					cabeceraSub.get(0).put("porcentajePortafoliodesde",porcentajePortafolioDesdeSub);
					cabeceraSub.get(0).put("porcentajePortafoliohasta", BigDecimal.ZERO);
					cabeceraSub.get(0).put("contribucionalPortafolio",sumaContribucionSub);
					cabeceraSub.get(0).put("rentabilidadMonedaReajuste",sumaRentabilidadMonedaReajusteSub);
					cabeceraSub.get(0).put("utilidadPerdida",sumautilidadSub);
				}
				montoVolatil=BigDecimal.ZERO;
				
				cabecera.get(0).put("ValorInstrumento",(montoacumulado));
				cabecera.get(0).put("utilidadPerdida",(sumaUtilidad));
				cabecera.get(0).put("contribucionalPortafolio",(sumaUtilidadContribucion));
				
				
				// Total Porcentaje Rentabilidad de acuerdo a la columna (Rentabilidad (Porc) (Moneda Rejuste)) segun cada mercado
				if(cabeceraClasificacion.size()>0) {
					cabeceraClasificacion.get(0).put("MonedaReajuste", sumaRentabilidadMonedaReajuste);
				}
				
				if(cabeceraClasificacionSub.size()>0) {
					cabeceraClasificacionSub.get(0).put("MonedaReajuste", sumaRentabilidadMonedaReajusteSub);
				}
				
				
				Collections.reverse(mercadoTemporal);
				filas.addAll(mercadoTemporal);
				salidaFilasClasificacionUnion.addAll(salidaFilasClasificacion);
			}
		
			//System.out.println("totalPatrimonio >> " + totalPatrimonio);

			//REPASO DE PORCENTAJES
			List<HashMap<String, Object>> salidaFilasRepaso = new ArrayList<HashMap<String, Object>>();
			
			for (HashMap<String, Object> fila : salidaFilas)
			{
				//System.out.println(fila + "\n");
			
				HashMap<String, Object> estructuraFilas = new HashMap<>();
				estructuraFilas.put("nemotecnico", fila.get("nemotecnico"));
				estructuraFilas.put("MonedaReajuste", fila.get("MonedaReajuste"));
				estructuraFilas.put("ValorInstrumento", fila.get("ValorInstrumento"));
				estructuraFilas.put("utilidadPerdida", fila.get("utilidadPerdida"));
				

			

				if (totalPatrimonioDesde.compareTo(BigDecimal.ZERO) != 0) 
				{
					estructuraFilas.put("porcentajePortafoliodesde", (new BigDecimal(fila.get("porcentajePortafoliodesde").toString()).divide(totalPatrimonioDesde, MathContext.DECIMAL64)).multiply(new BigDecimal(100L)).setScale(2, RoundingMode.FLOOR));
										
				}
				else
				{
					estructuraFilas.put("porcentajePortafoliodesde", BigDecimal.ZERO);				
				}
				
				
				if (totalPatrimonioHasta.compareTo(BigDecimal.ZERO) != 0) 
				{
					estructuraFilas.put("porcentajePortafoliohasta", (new BigDecimal(fila.get("ValorInstrumento").toString()).divide(totalPatrimonioHasta, MathContext.DECIMAL64)).multiply(new BigDecimal(100L)).setScale(2, RoundingMode.FLOOR));
				}
				else
				{
					estructuraFilas.put("porcentajePortafoliohasta", BigDecimal.ZERO);
				}
				
				estructuraFilas.put("rentabilidadMonedaReajuste", fila.get("rentabilidadMonedaReajuste"));
				estructuraFilas.put("contribucionalPortafolio", fila.get("contribucionalPortafolio"));
				estructuraFilas.put("TipoFila", fila.get("TipoFila"));
				
				salidaFilasRepaso.add(estructuraFilas);
			}

			
			
			salida.P_OUT_SALIDA.put("RentabilidadClasificacion", salidaFilasClasificacion);
			
			salida.P_OUT_SALIDA.put("Atributtion", salidaFilasRepaso);
			
			
// *** INICIO CUADRO RENTABILIDAD INDICES
		
			HashMap<String, Object> entradaNivelesSinPadre = new HashMap<String, Object>();
			List<ClasificacionRentabilidad> nivelesSinPadre = sql.query(conn,SQL_SELECT_ARBOL,entradaNivelesSinPadre,ClasificacionRentabilidad.class);
			IndiceRentabilidadAtributtionAnalysis rIndices = new IndiceRentabilidadAtributtionAnalysis();
			EntradaClasificacionAtributtion       rEntradaIndices = new EntradaClasificacionAtributtion();
			rEntradaIndices.p_FechaDesde = in.p_FechaDesde;
			rEntradaIndices.p_FechaHasta = in.p_FechaHasta;
			rEntradaIndices.p_clasificacion =nivelesSinPadre;
			salida.P_OUT_SALIDA.put("RentabilidadIndices", rIndices.procesar(conn,rEntradaIndices ).P_OUT_SALIDA.get("rentabilidadindice"));
			
			HashMap<String, BigDecimal> indiceClasificacion = new HashMap<String, BigDecimal>();
			salida.P_OUT_SALIDA.get("RentabilidadIndices").forEach(indice -> {
				indiceClasificacion.put((String) indice.get("Descripcion"),(BigDecimal) indice.get("rentabilidadIndice"));
			});
			HashMap<String, String> indiceClasificacion2 = new HashMap<String, String>();
			salida.P_OUT_SALIDA.get("RentabilidadIndices").forEach(indice -> {
				indiceClasificacion2.put((String) indice.get("Descripcion"),(String) indice.get("Indice"));
			});
			
			List<SalidaIndice> indiceNDList = new ArrayList<>();
			for(HashMap<String,Object> hashRent : salida.P_OUT_SALIDA.get("RentabilidadClasificacion")) {
				String nemo = hashRent.get("nemotecnico").toString();
				BigDecimal indice = indiceClasificacion.get(nemo);
				String ind = indiceClasificacion2.get(nemo);
				SalidaIndice indiceND = new SalidaIndice();
				
				indiceND.Descripcion = nemo;
				if(indice != null && ind != null) {
					indiceND.Indice = ind;
					indiceND.rentabilidadIndice = indice;
				} else {
					HashMap<String,Object> addClasificacionIndice = new HashMap<String, Object>();
					salida.P_OUT_SALIDA.get("RentabilidadIndices").add(addClasificacionIndice);
					
					indiceND.Indice = "ND";
					indiceND.rentabilidadIndice = BigDecimal.ZERO;
				}
				indiceNDList.add(indiceND);
			}
			List<HashMap<String,Object>> resutadoIndiceND = Utils.List2ListHashGenericObject(indiceNDList);
			salida.P_OUT_SALIDA.replace("RentabilidadIndices",resutadoIndiceND);

// *** FIN RENTABILIDAD INDICES

			
			System.out.println("aSDASD");
		} catch (Exception e) {
			System.out.println(e);
			GeneraElementosError(conn, "ERROR", 1L, "UI_OPERACION_TIENE_ERRORES", salida.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			throw e;
		}
		System.out.println(salida);
		return salida;
	}


	
	// -------------------------------------------------
	// METODOS COMPLEMENTARIOS
	// -------------------------------------------------
	private List<HashMap<String, Object>> agregarLineaSalida(String nemotecnico, String MonedaReajuste,
			BigDecimal ValorInstrumento, BigDecimal utilidadPerdida, BigDecimal porcentajePortafoliodesde,
			BigDecimal porcentajePortafoliohasta, BigDecimal rentabilidadMonedaReajuste,
			BigDecimal contribucionalPortafolio, String TipoFila) {

		List<HashMap<String, Object>> salidaf = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> estructuraFilas = new HashMap<>();
		estructuraFilas.put("nemotecnico", nemotecnico);
		estructuraFilas.put("MonedaReajuste", MonedaReajuste);
		estructuraFilas.put("ValorInstrumento", ValorInstrumento); // Va en la fila mercado y submercado
		estructuraFilas.put("utilidadPerdida", utilidadPerdida);
		estructuraFilas.put("porcentajePortafoliodesde", porcentajePortafoliodesde);
		estructuraFilas.put("porcentajePortafoliohasta", porcentajePortafoliohasta);
		estructuraFilas.put("rentabilidadMonedaReajuste", rentabilidadMonedaReajuste);
		estructuraFilas.put("contribucionalPortafolio", contribucionalPortafolio);
		estructuraFilas.put("TipoFila", TipoFila);
		salidaf.add(estructuraFilas);
		return salidaf;
	}

	// OK :: Agrega linea de cabecera en detalle del informe atributtions
	private List<HashMap<String, Object>> agregarLineaSalidaMercado(String nemotecnico, BigDecimal ValorInstrumento,
			BigDecimal utilidadPerdida, BigDecimal porcentajePortafoliodesde, BigDecimal porcentajePortafoliohasta,
			BigDecimal contribucionalPortafolio) {

		List<HashMap<String, Object>> salidaf = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> estructuraFilas = new HashMap<>();
		estructuraFilas.put("nemotecnico", nemotecnico);
		estructuraFilas.put("MonedaReajuste", "");
		estructuraFilas.put("ValorInstrumento", ValorInstrumento); // Va en la fila mercado y submercado
		estructuraFilas.put("utilidadPerdida", utilidadPerdida);
		estructuraFilas.put("porcentajePortafoliodesde", porcentajePortafoliodesde);
		estructuraFilas.put("porcentajePortafoliohasta", porcentajePortafoliohasta);
		
		estructuraFilas.put("rentabilidadMonedaReajuste", BigDecimal.ZERO);
		
		estructuraFilas.put("contribucionalPortafolio", contribucionalPortafolio);
		estructuraFilas.put("TipoFila", "mercado");
		salidaf.add(estructuraFilas);
		return salidaf;
	}

	// OK :: Agrega linea de cabecera en detalle del informe atributtions (A nivel de SubMercado)
	private List<HashMap<String, Object>> agregarLineaSalidaSubMercado(String nemotecnico, BigDecimal ValorInstrumento,
			BigDecimal utilidadPerdida, BigDecimal porcentajePortafoliodesde, BigDecimal porcentajePortafoliohasta,
			BigDecimal contribucionalPortafolio) {

		List<HashMap<String, Object>> salidaf = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> estructuraFilas = new HashMap<>();
		estructuraFilas.put("nemotecnico", nemotecnico);
		estructuraFilas.put("MonedaReajuste", "");
		estructuraFilas.put("ValorInstrumento", ValorInstrumento); // Va en la fila mercado y submercado
		estructuraFilas.put("utilidadPerdida", utilidadPerdida);
		estructuraFilas.put("porcentajePortafoliodesde", porcentajePortafoliodesde);
		estructuraFilas.put("porcentajePortafoliohasta", porcentajePortafoliohasta);
		
		estructuraFilas.put("rentabilidadMonedaReajuste", BigDecimal.ZERO); 
		
		
		estructuraFilas.put("contribucionalPortafolio", contribucionalPortafolio);
		estructuraFilas.put("TipoFila", "submercado");
		salidaf.add(estructuraFilas);
		return salidaf;
	}

	
	// OK :: Agrega linea de cabecera para cuadro de Clasificacion Rentabilidad
	private List<HashMap<String, Object>> agregarLineaTablaClasificacion(String nemotecnico, BigDecimal ValorInstrumento) {

		List<HashMap<String, Object>> salidaf = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> estructuraFilas = new HashMap<>();
		estructuraFilas.put("nemotecnico", nemotecnico);
		estructuraFilas.put("MonedaReajuste", ValorInstrumento);
		salidaf.add(estructuraFilas);
		return salidaf;
	}


/* OK :: OBTIENE DATOS DE SOCIO DE NEGOCIO */
	public List<HashMap<String, Object>> getDatosSocioNegocio(Connection con, EntradaCartola entradaCartola)
			throws Exception {

		List<HashMap<String, Object>> dusuario = new ArrayList<HashMap<String, Object>>();
		DatosCartolaSocioNegocio dSocio = new DatosCartolaSocioNegocio();

		SalidaCartola salidaCartola = dSocio.procesar(con, entradaCartola);
		if (salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			dusuario = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("datosSocioNegocio");
		}
		return dusuario;
	}
	
	
	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		InformeAtributtionAnalisys_Entrada in = gson.fromJson(jin.toString(), InformeAtributtionAnalisys_Entrada.class);
		InformeAtributtionAnalisys_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getMBIPREConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":0,\"p_FechaDesde\":\"2016-01-04\",\"p_FechaHasta\":\"2021-05-24\",\"p_Datos\":[{\"IdCliente\":5477,\"IdPortafolio\":6169}],\"p_Moneda\":\"CLP\"}";

			InformeAtributtionAnalisys_Entrada Objin = gson.fromJson(jsonCL, InformeAtributtionAnalisys_Entrada.class);

			InformeAtributtionAnalisys demo = new InformeAtributtionAnalisys();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				InformeAtributtionAnalisys_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			//con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

	
	// -------------------------------------------------
	// METODOS COMPLEMENTARIOS
	// -------------------------------------------------
	public static int diferenciaEnDias(Date fechaMayor, Date fechaMenor) {
		long diferenciaEn_ms = fechaMayor.getTime() - fechaMenor.getTime();
		long dias = diferenciaEn_ms / (1000 * 60 * 60 * 24);
		return (int) dias;

	}

	public static BigDecimal TransformadorMonedas(Connection conn, String fechaMovimiento, String monedaOrigen,
			String monedaDestino, BigDecimal monto) throws Exception {
		BigDecimal montoTransformado = BigDecimal.ZERO;
		List<CalcularParidad.CalcularParidadSalida> listCalculaParidad = new ArrayList<CalcularParidad.CalcularParidadSalida>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalculoEntrada entradaCalcularParidad = new CalculoEntrada();
		SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");
		Date date2 = parseador.parse(fechaMovimiento);
		String fechaOP = formateador.format(date2);
		List<CalcularParidad.CalculoEntrada> listparidad = new ArrayList<CalcularParidad.CalculoEntrada>();
		entradaCalcularParidad.IdTrabajo = 0L;
		entradaCalcularParidad.IdInterno = 0L;
		entradaCalcularParidad.Fecha = fechaOP; // fecha OP
		entradaCalcularParidad.IdMonedaOrigen = monedaOrigen; // MONEDA QUE VIENE EJ USD
		entradaCalcularParidad.IdMonedaDestino = monedaDestino; // MONDEA A TRANSFORMAR
		entradaCalcularParidad.ValorCambioLocalModificado = BigDecimal.ZERO;
		entradaCalcularParidad.IdUso = "";
		listparidad.add(entradaCalcularParidad);
		CalcularParidad.CalcularParidadEntrada paridadEntrada = new CalcularParidad.CalcularParidadEntrada(); // ENTRADA
																												// FINAL
		paridadEntrada.p_IdEmpresa = 1L;
		paridadEntrada.p_IdTrabajo = 0L;
		paridadEntrada.p_CalculosEntrada = listparidad;
		entradaCalcularParidad.MontoOrigen = monto;
		listCalculaParidad.add(calcularParidad.logica(conn, paridadEntrada)); // dentro tengo el monto
																		// transformado
		montoTransformado = listCalculaParidad.get(0).p_out_CalculosSalida.get(0).MontoDestino; // Dato
		// transformado
		return montoTransformado; // a pesos CLP
	}

	public void GeneraElementosError(Connection con, String tipo, Long idEmpresa, String codMsg,
			List<MensajeSalida> P_OUT_MENSAJES) throws Exception {

		String msg = Diccionario.getMensaje(con, idEmpresa, codMsg).msg();
		if (tipo == "ERROR") {
			P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codMsg, msg));
		} else if (tipo == "ALERTA") {
			P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, codMsg, msg));
		} else {
			P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, codMsg, msg));
		}
	}
}

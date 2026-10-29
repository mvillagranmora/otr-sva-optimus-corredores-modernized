package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarAsignacionesPorDetallePerfilCobro implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarAsignacionesPorDetallePerfilCobro.class);

	private final String SQL_SELECT_DETALLE_PERFIL_COBROS = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Select.sql");
	private final String SQL_UPDATE_COBRO_ASIGNACION = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Update_cobroAsignacion.sql");
	private final String SQL_INSERT_COBRO_ASIGNACION = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Insert_cobroAsignacion.sql");
	private final String SQL_SELCT_COBRO_ASIGNACION = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Select_CobroAsignacion.sql");
	private final String SQL_SELCT_EXISTE = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Select_existe.sql");
	
	private final String SQL_SELCT_PORTAFOLIO = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_Portafolio.sql");
	private final String SQL_SELCT_COBRO_DEFAULT = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarOrdenesPorDetallePerfilCobro_Select_Cobro_Default.sql");
	private final String SQL_DELETE_ASIGNACION_COBRO = loadResourceToString(
			"cl/bcs/plataforma/com/sql/ModificarAsignacionesPorDetallePerfilCobro_Delete_AsignacionCobros.sql");

	public static class EntradaModificarAsignacionesPorDetallePerfilCobro extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Long> p_IdDetallePerfilCobros;

		public EntradaModificarAsignacionesPorDetallePerfilCobro() {
			this.p_IdDetallePerfilCobros = new ArrayList<>();
		}
	}

	public static class SalidaModificarAsignacionesPorDetallePerfilCobro extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaModificarAsignacionesPorDetallePerfilCobro() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class EntidadAsignacionesCobros extends BcsPojo {

		public Long IdDetallePerfilCobros;
		public Long IdPortafolio;
		public String IdMoneda;
		public String IdCobro;
		public String IdCobrocoborden;
		public Long IdOrden;		
		public Long IdAsignacion;
		public BigDecimal ValorCobroPerfil;
		public BigDecimal ValorCobro;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public BigDecimal ValorCobroFinal;
		public BigDecimal MontoAsignado;
		public BigDecimal MontoAsignadoLocal;
		public BigDecimal MontoAsignadoLiquidacion;
		public Long IdCobroAsignacion;
		public String CodigoCobroClasificacion;
		public String IdCobroAsig;
		public String Borrado;
		public String IdTipoProducto;
		public BigDecimal MontoMinimo;
		public BigDecimal MontoCobroCalculadoOrd;

	}

	public static class EntidadUpdateCobroAsignacion extends BcsPojo {

		public String IdMoneda;
		public BigDecimal ValorCobro;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal ValorCobroFinal;
		public BigDecimal MontoCobroCalculado;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public Long IdAsignacion;
		public String IdCobro;
		
	}

	public static class EntidadInsertCobroASignacion extends BcsPojo {

		public Long IdCobroAsignacion;
		public Long IdAsignacion;
		public String IdCobro;
		public Date FechaIngresoCobro;
		public Date FechaIngresoRegistro;
		public String FormaCobro;
		public BigDecimal ValorCobro;
		public BigDecimal ValorCobroAplicado;
		public BigDecimal MontoCobroCalculado;
		public Long IdCarga;
		public BigDecimal MontoCobroCalculadoLocal;
		public BigDecimal MontoCobroCalculadoLiquidacion;
		public String IdMoneda;
		public BigDecimal ValorCobroFinal;
		public String CodigoCobroClasificacion;
		public String CodigoCobroClasificacionBase;

	}

	public static class resultado {
		public Long existen;

		public resultado() {
			this.existen = 0l;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class EntidadCobroDefault extends BcsPojo {
		public Long IdPortafolio;
		public String IdCobro;
		public String IdTipoProducto;
		public String IdValorCobro;
		public String IdPerfilCobros;
		public BigDecimal ValorCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;

	}

	public static class EntidadBuscaFolio extends BcsPojo {
		public Long IdPortafolio;
		public String IdCobro;
		public String IdTipoProducto;

	}

	public static class EntidadDeleteCobroAsignacion extends BcsPojo {
		public Long IdAsignacion;
		public Long IdCobroAsignacion;
		public String IdCobro;

	}

//////////////////FUNCIONES	

	private List<EntidadAsignacionesCobros> obtenerDatosDetallePerfilAsignaciones(Connection conn,
			List<Long> IdDetallePerfilCobros) throws Exception {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro.obtenerDatosDetallePerfilAsignaciones");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdDetallePerfilCobros", IdDetallePerfilCobros);

		SQL sql = new SQL();
		List<EntidadAsignacionesCobros> detalle = sql.query(conn, SQL_SELECT_DETALLE_PERFIL_COBROS, parametros,
				EntidadAsignacionesCobros.class);
		logger.debug("DATA = " + detalle);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.obtenerDatosDetallePerfilAsignaciones",
				fin - inicio);
		return detalle;
	}

	private BigDecimal obtenerNuevosMontosComisionFija(Connection conn, EntidadAsignacionesCobros Orden) {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		try {
			MontoCobro = MonedaFormato.formatear(conn, Orden.ValorCobroPerfil, MonedaFormato.MONTO,
					Orden.IdMoneda).valor;
		} catch (Exception e) {
			logger.trace("ERROR::ModificarAsignacionesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", fin - inicio);
		return MontoCobro;
	}

	private BigDecimal obtenerNuevosMontosComisionVariable(Connection conn, EntidadAsignacionesCobros Orden) {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		BigDecimal porcentajeReal = Orden.ValorCobroPerfil.divide(new BigDecimal(100), MathContext.DECIMAL128);
		MontoCobro = Orden.MontoAsignado.multiply(porcentajeReal, MathContext.DECIMAL128);

		try {
			MontoCobro = MonedaFormato.formatear(conn, MontoCobro, MonedaFormato.MONTO, Orden.IdMoneda).valor;

		} catch (Exception e) {
			logger.trace("ERROR::ModificarAsignacionesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable",
				fin - inicio);
		return MontoCobro;
	}

	private void ModificaCobroAsignaciones(Connection conn, EntidadAsignacionesCobros OrdenCobroAsignacion,
			BigDecimal CobroComision) {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro.ModificaCobroAsignaciones");
		long inicio = System.currentTimeMillis();
		EntidadUpdateCobroAsignacion UpdateCobroAsignacion = new EntidadUpdateCobroAsignacion();
		List<EntidadUpdateCobroAsignacion> UpdateCobroAsignacionList = new ArrayList<>();
          
		try {
			UpdateCobroAsignacion.IdMoneda = OrdenCobroAsignacion.IdMoneda;
			UpdateCobroAsignacion.ValorCobro =  OrdenCobroAsignacion.ValorCobroPerfil.multiply(new BigDecimal(1), MathContext.DECIMAL128);
			UpdateCobroAsignacion.ValorCobroAplicado = OrdenCobroAsignacion.ValorCobroPerfil.multiply(new BigDecimal(1), MathContext.DECIMAL128);
			UpdateCobroAsignacion.ValorCobroFinal = OrdenCobroAsignacion.ValorCobroPerfil.multiply(new BigDecimal(1), MathContext.DECIMAL128);
			UpdateCobroAsignacion.MontoCobroCalculado = CobroComision;
			UpdateCobroAsignacion.MontoCobroCalculadoLocal = CobroComision;
			UpdateCobroAsignacion.MontoCobroCalculadoLiquidacion = CobroComision;
			UpdateCobroAsignacion.IdCobro = OrdenCobroAsignacion.IdCobro;
			UpdateCobroAsignacion.IdAsignacion = OrdenCobroAsignacion.IdAsignacion;

			UpdateCobroAsignacionList.add(UpdateCobroAsignacion);
			logger.debug("UPDATE:: UpdateCobro Asignaciones = " + UpdateCobroAsignacion.toString());
			SQL sql = new SQL();
			sql.batch(conn, SQL_UPDATE_COBRO_ASIGNACION, UpdateCobroAsignacionList, EntidadUpdateCobroAsignacion.class);

		} catch (Exception e) {

			logger.debug("ERROR::ModificarAsignacionesPorDetallePerfilCobro.ModificaCobroAsignaciones", e);
			logger.trace("ERROR::ModificarAsignacionesPorDetallePerfilCobro.ModificaCobroAsignaciones", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.ModificaCobroAsignaciones", fin - inicio);
	}

	private void AgregaCobroAsignaciones(Connection conn, EntidadAsignacionesCobros OrdenCobroAsignacion
			) {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro.AgregaCobroAsignaciones");
		long inicio = System.currentTimeMillis();

		try {

			List<resultado> Existen = new ArrayList<>();
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("IdAsignacion", OrdenCobroAsignacion.IdAsignacion);
			param.put("IdCobro", OrdenCobroAsignacion.IdCobrocoborden);

			SQL sql2 = new SQL();

			Existen = sql2.query(conn, SQL_SELCT_EXISTE, param, resultado.class);
			
			if (Existen.get(0).existen == 0) {

				List<EntidadInsertCobroASignacion> NoComision = new ArrayList<>();

				HashMap<String, Object> parametros = new HashMap<String, Object>();
				parametros.put("IdAsignacion", OrdenCobroAsignacion.IdAsignacion);

				SQL sql = new SQL();
				NoComision = sql.query(conn, SQL_SELCT_COBRO_ASIGNACION, parametros,
						EntidadInsertCobroASignacion.class);

				
				
				EntidadInsertCobroASignacion InsertCobroAsignacion = new EntidadInsertCobroASignacion();
				List<EntidadInsertCobroASignacion> InsertCobroAsignacionList = new ArrayList<>();

				InsertCobroAsignacion.IdAsignacion = OrdenCobroAsignacion.IdAsignacion;
				InsertCobroAsignacion.IdCobro = OrdenCobroAsignacion.IdCobro;
				InsertCobroAsignacion.ValorCobro = OrdenCobroAsignacion.ValorCobroPerfil;
				InsertCobroAsignacion.ValorCobroAplicado = OrdenCobroAsignacion.ValorCobroPerfil;
				InsertCobroAsignacion.MontoCobroCalculado = OrdenCobroAsignacion.MontoCobroCalculadoOrd;
				InsertCobroAsignacion.FormaCobro = NoComision.get(0).FormaCobro;
				InsertCobroAsignacion.FechaIngresoCobro = NoComision.get(0).FechaIngresoCobro;
				InsertCobroAsignacion.FechaIngresoRegistro = NoComision.get(0).FechaIngresoCobro;
				InsertCobroAsignacion.IdCarga = NoComision.get(0).IdCarga;
				InsertCobroAsignacion.MontoCobroCalculadoLocal = OrdenCobroAsignacion.MontoCobroCalculadoOrd;
				InsertCobroAsignacion.MontoCobroCalculadoLiquidacion = OrdenCobroAsignacion.MontoCobroCalculadoOrd;
				InsertCobroAsignacion.CodigoCobroClasificacion = "COMISION";
				InsertCobroAsignacion.IdMoneda = NoComision.get(0).IdMoneda;
				InsertCobroAsignacion.ValorCobroFinal = OrdenCobroAsignacion.ValorCobroPerfil;
				InsertCobroAsignacion.CodigoCobroClasificacion = "COMISION";
				InsertCobroAsignacion.CodigoCobroClasificacionBase = "COMISION";
				
				InsertCobroAsignacionList.add(InsertCobroAsignacion);
				
				sql.batch(conn, SQL_INSERT_COBRO_ASIGNACION, InsertCobroAsignacionList,
						EntidadInsertCobroASignacion.class);
				logger.debug("INSERT:: ADD= ",InsertCobroAsignacion.toString() );
			} else {

				logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.AgregaCobroAsignaciones", "ya existe");
			}

		} catch (Exception e) {

			logger.trace("ERROR::ModificarAsignacionesPorDetallePerfilCobro.AgregaCobroAsignaciones", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.AgregaCobroAsignaciones", fin - inicio);

	}

	private EntidadBuscaFolio obtenerPortfaolioDefault(Connection conn, List<Long> IdDetallePerfilCobros)
			throws Exception {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro.obtenerValoresDefault");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdDetallePerfilCobros", IdDetallePerfilCobros);
		SQL sql = new SQL();

		List<EntidadBuscaFolio> Folio = sql.query(conn, SQL_SELCT_PORTAFOLIO, parametros, EntidadBuscaFolio.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.obtenerValoresDefault", fin - inicio);

		return Folio.get(0);
	}

	private List<EntidadCobroDefault> obtenerCobrosDefault(Connection conn, EntidadBuscaFolio Folio) throws Exception {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro.obtenerValoresDefault");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("IdPortafolio", Folio.IdPortafolio);
		param.put("IdCobro", Folio.IdCobro);
		param.put("IdTipoProducto", Folio.IdTipoProducto);
		List<EntidadCobroDefault> CobrosDefault = sql.query(conn, SQL_SELCT_COBRO_DEFAULT, param,
				EntidadCobroDefault.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro.obtenerValoresDefault", fin - inicio);

		return CobrosDefault;
	}

	private BigDecimal obtenerNuevosMontosComisionFijaDefault(Connection conn, EntidadAsignacionesCobros Orden,
			EntidadCobroDefault Default) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		try {
			MontoCobro = MonedaFormato.formatear(conn, Default.ValorCobro, MonedaFormato.MONTO, Default.IdMoneda).valor;
		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionFija", fin - inicio);
		return MontoCobro;
	}

	private BigDecimal obtenerNuevosMontosComisionVariableDefault(Connection conn, EntidadAsignacionesCobros Orden,
			EntidadCobroDefault Default) {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable");
		long inicio = System.currentTimeMillis();
		BigDecimal MontoCobro = BigDecimal.ZERO;

		BigDecimal porcentajeReal = Default.ValorCobro.divide(new BigDecimal(100), MathContext.DECIMAL128);
		MontoCobro = Orden.MontoAsignado.multiply(porcentajeReal, MathContext.DECIMAL128);

		try {
			MontoCobro = MonedaFormato.formatear(conn, MontoCobro, MonedaFormato.MONTO, Orden.IdMoneda).valor;
			if (MontoCobro.compareTo(Default.ValorTopeMinimoOrden) >= 0) {
				MontoCobro = Default.ValorTopeMinimoOrden;
			}

		} catch (Exception e) {
			logger.trace("ERROR::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", e);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.obtenerNuevosMontosComisionVariable", fin - inicio);
		return MontoCobro;
	}

	private void EliminaCobroAsignacion(Connection conn, Long p_Asignacion, Long p_IdCobroAsignacion, String p_IdCobro)
			throws Exception {
		logger.debug("INICIO::ModificarOrdenesPorDetallePerfilCobro.EliminaCobroOrden");
		long inicio = System.currentTimeMillis();

		List<EntidadDeleteCobroAsignacion> DeleteAsignacionList = new ArrayList<>();
		EntidadDeleteCobroAsignacion DeleteCobroOrden = new EntidadDeleteCobroAsignacion();
		DeleteCobroOrden.IdAsignacion = p_Asignacion;
		DeleteCobroOrden.IdCobroAsignacion = p_IdCobroAsignacion;
		DeleteCobroOrden.IdCobro = p_IdCobro;
		DeleteAsignacionList.add(DeleteCobroOrden);
		logger.debug("DELETE:: = " + DeleteCobroOrden);
		SQL sql = new SQL();

		sql.batch(conn, SQL_DELETE_ASIGNACION_COBRO, DeleteAsignacionList, EntidadDeleteCobroAsignacion.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarOrdenesPorDetallePerfilCobro.EliminaCobroOrden", fin - inicio);

	}
//////////////////////////LOGICA /////////////////////////////

	public Long NumAsignacion;
	public EntidadBuscaFolio portafolio;
	public List<EntidadCobroDefault> CobroDefault;
	public Integer Cont;

	public SalidaModificarAsignacionesPorDetallePerfilCobro logica(Connection conn,
			EntradaModificarAsignacionesPorDetallePerfilCobro entrada) throws Exception {
		logger.debug("INICIO::ModificarAsignacionesPorDetallePerfilCobro");
		long inicio = System.currentTimeMillis();
		logger.debug("Entrada    " + entrada);
		SalidaModificarAsignacionesPorDetallePerfilCobro salida = new SalidaModificarAsignacionesPorDetallePerfilCobro();

		List<EntidadAsignacionesCobros> AsignacionesCobros = obtenerDatosDetallePerfilAsignaciones(conn,
				entrada.p_IdDetallePerfilCobros);

		if (AsignacionesCobros.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"UI_PERFIL_ASIGNACIONES_NO_EXISTE", new HashMap<String, String>(), conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_MENSAJES.stream()
					.filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count();
			return salida;
		}
		try {

			portafolio = obtenerPortfaolioDefault(conn, entrada.p_IdDetallePerfilCobros);

		} catch (Exception e) {
			logger.debug("ModificarOrdenesPorDetallePerfilCobro", "error ", e);
		}
		NumAsignacion = 0l;
		
		AsignacionesCobros.stream().forEach(Asignacion -> {
			Cont =0;
			if (Asignacion.IdAsignacion > 0l) {
				if(NumAsignacion == Asignacion.IdAsignacion) {
					Cont ++;
				}else {
					Cont=0;
				}
				if (Cont < 2) {
					
				if (Asignacion.IdAsignacion.compareTo(NumAsignacion) == 1
						|| Asignacion.IdCobroAsig.equalsIgnoreCase("COVA") == true
						|| Asignacion.IdCobroAsig.equalsIgnoreCase("COFI") == true) {
					NumAsignacion = Asignacion.IdAsignacion;
					
				
					if (Asignacion.IdCobro.equalsIgnoreCase("COVA") == true	|| Asignacion.IdCobro.equalsIgnoreCase("COFI") == true) {
						BigDecimal CobroComision = BigDecimal.ZERO;
							if (Asignacion.Borrado.equalsIgnoreCase("N") == true) {
								
								
								if (Asignacion.IdCobrocoborden.equalsIgnoreCase(Asignacion.IdCobro) == true) {
							        	if (Asignacion.IdCobro.equalsIgnoreCase("COVA")) {
								        	CobroComision = obtenerNuevosMontosComisionVariable(conn, Asignacion);
            								}
								        if (Asignacion.IdCobro.equalsIgnoreCase("COFI")) {
									        CobroComision = obtenerNuevosMontosComisionFija(conn, Asignacion);
							            	}
								        
								        AgregaCobroAsignaciones(conn, Asignacion);
								        ModificaCobroAsignaciones(conn, Asignacion, CobroComision);
							     	}

							} else {
								try {
								
									portafolio.IdCobro = Asignacion.IdCobro;
								
									CobroDefault = obtenerCobrosDefault(conn, portafolio);
								
								} catch (Exception e) {
									System.out.println("Error " + e);
								}
								if (CobroDefault.isEmpty()) {

									try {
								
										EliminaCobroAsignacion(conn, Asignacion.IdAsignacion,
												Asignacion.IdCobroAsignacion, Asignacion.IdCobro);

									} catch (Exception e) {
										System.out.println("Error " + e);
									}

								} else {
									
									
									if (Asignacion.IdCobroAsig.equalsIgnoreCase("COVA")) {
										CobroComision = obtenerNuevosMontosComisionVariableDefault(conn, Asignacion,
												CobroDefault.get(0));

									}
									if (Asignacion.IdCobroAsig.equalsIgnoreCase("COFI")) {

										CobroComision = obtenerNuevosMontosComisionFijaDefault(conn, Asignacion,
												CobroDefault.get(0));
									}
									
									
									EntidadAsignacionesCobros AsignacionCobroDefault = new EntidadAsignacionesCobros();
									AsignacionCobroDefault = Asignacion;
																
									AsignacionCobroDefault.MontoMinimo = CobroDefault.get(0).ValorTopeMinimoOrden.multiply(new BigDecimal(1), MathContext.DECIMAL128);
									AsignacionCobroDefault.ValorCobro = CobroDefault.get(0).ValorCobro.multiply(new BigDecimal(1), MathContext.DECIMAL128);
									AsignacionCobroDefault.ValorCobroPerfil =CobroDefault.get(0).ValorCobro.multiply(new BigDecimal(1), MathContext.DECIMAL128);
									AsignacionCobroDefault.IdOrden = Asignacion.IdOrden;
									AsignacionCobroDefault.IdCobro = Asignacion.IdCobro;

									ModificaCobroAsignaciones(conn, AsignacionCobroDefault, CobroComision);

								}
							}
					
					}
				}
			}}
		});

	if(salida.P_OUT_ERRORESCONTADOR==0)

	{
		MensajeSalida mensaje = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_MODIFICACION_INGRESO_EXITO",
				new HashMap<String, String>(), conn, entrada.p_IdEmpresa);
		salida.P_OUT_MENSAJES.add(mensaje);
	}

	long fin = System
			.currentTimeMillis();logger.debug("FIN::ModificarAsignacionesPorDetallePerfilCobro",fin-inicio);return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarAsignacionesPorDetallePerfilCobro in = gson.fromJson(jin.toString(),
				EntradaModificarAsignacionesPorDetallePerfilCobro.class);
		SalidaModificarAsignacionesPorDetallePerfilCobro out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();

				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDetallePerfilCobros\":[2902]}";

				EntradaModificarAsignacionesPorDetallePerfilCobro Objin = gson.fromJson(json,
						EntradaModificarAsignacionesPorDetallePerfilCobro.class);
				System.out.println(Objin.toString());
				ModificarAsignacionesPorDetallePerfilCobro modificarMovimientoComprobanteCobrosMasivo = new ModificarAsignacionesPorDetallePerfilCobro();
				SalidaModificarAsignacionesPorDetallePerfilCobro Objout = modificarMovimientoComprobanteCobrosMasivo
						.logica(con, Objin);

				System.out.println("____Salida_____");
				System.out.println(Objout);
				// con.rollback();
				 con.commit();

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
	}

}

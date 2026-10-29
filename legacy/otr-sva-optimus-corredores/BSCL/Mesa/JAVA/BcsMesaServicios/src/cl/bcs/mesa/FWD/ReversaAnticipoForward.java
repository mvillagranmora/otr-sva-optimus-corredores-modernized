package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.facturacion.AnularComprobanteFinal.Comprobante_Proceso;
import cl.bcs.facturacion.AnularComprobanteFinal.EntidadTesoreria_Proceso;
import cl.bcs.mesa.FWD.CrearOperacionForward.FormaDePagoMesa;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForwardObjOut;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraEntrada;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraSalida;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjIn;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjOut;
import cl.bcs.mesa.FWD.VencimientoOperacionForward.OperacionForwardObjIn;
import cl.bcs.mesa.FWD.RevisarRequisitosReversaForward.RevisarRequisitosReversaForwardSalida;
import cl.bcs.mesa.SPT.AnularOperacionSpot;
import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotEntrada;
import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.mesa.SPT.IngresoDatatec.DatosDatatecObjOut;
import cl.bcs.mesa.SPT.IngresoDatatec.OperacionSpotDatatec;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Salida;
import cl.bcs.tesoreria.AnularRegistros.Registro_Entrada;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.InObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReversaAnticipoForward implements JavaCall {

	private final Log log = new Log("FWD.ReversaAnticipoForward");

	private final String SQL_SELECT_OPERACIONES_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarOperacionesForward.sql");
	private final String SQL_SELECT_OPERACIONES_ANTICIPO_PARCIAL_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarOperacionesAnticipoParcialForward.sql");
	private final String SQL_SELECT_VALIDACIONES_REVERSA_ANTICIPO_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarValidacionesReversaAnticipoForward.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_OPERACION_REVERSADA = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarFormaDePagoOperacionReversada.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_TESORERIA = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarMovimientoComprobanteTesoreria.sql");
	private final String SQL_SELECT_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarOperacionSpot.sql");
	private final String SQL_SELECT_COMPROBANTE_CUENTAINVERSION = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarIdComprobanteCuentaInversion.sql");
	private final String SQL_UPDATE_OPERACIONES_MESA = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_UpdateOperacionesMesa.sql");
	private final String SQL_SELECT_INGRESOEGRESO_TESORERIA = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_SelectEstadoIngresoEgreso.sql");
	private final String SQL_SELECT_COMPROBANTE_TESORERIA = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_SelectEstadoComprobante.sql");
	private final String SQL_SELECT_COMPROBANTE_CUENTAINVERSION_TESORERIA = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarIdOperacionCuentaInversion.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarMonedasFeriadoMonedaPrincipal.sql");
	
	public static class ReversaAnticipoForwardEntrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdBolsa;
		public Long p_IdSucursal;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionForwardObjIn> p_tablaOperReversa;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ReversaAnticipoForwardSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionForwardObjOut>	P_OUT_TBLIDFORWARD;

		public ReversaAnticipoForwardSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosCarteraReversaForward {
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoProducto;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdAgente;
		public String IdTipoCanalIngreso;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String EGRIdTipoOperacion;
		public BigDecimal INGMontoMonedaPrincipal;
		public BigDecimal EGRMontoMonedaPrincipal;
		public BigDecimal INGMontoMonedaSecundaria;
		public BigDecimal EGRMontoMonedaSecundaria;
		public Date FechaOperacion;
		public BigDecimal ValorTipoCambioCierre;
		public BigDecimal ValorParidadCierre;
		public Long Plazo;
		public Date FechaVencimientoOperacion;
		public Date FechaLiquidacion;
		public Long IdModalidadCumplimiento;
		public String Modalidad;
		public String ModalidadLiquidacionValor;
		public Long IdPlaza;
		public BigDecimal ValorTipoCambioCosto;
		public String INGEsPuntos;
		public BigDecimal INGTasaPuntosMonedaPrincipal;
		public BigDecimal TasaCompra;
		public Long IdFixing;
		public Date FechaFixing;
		public Time HoraFixing;
		public Long IdMesa;
		public Long EGRIdSpotAsociado;
		public String Glosa;
		public String IdEstado;
		public BigDecimal EGRMontoARecibirAnticipo;
		public BigDecimal TipoCambioMercadoDO;
		public String MultiplicaODivide;
		public String TipoAnticipo;
		public String EGRIdTipoComprobante;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosValidacionesForwardReversar {
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public String IdEstado;
		public Long Modalidad;
		public String ModalidadLiquidacionValor;
		public String EGRIdTipoOperacion;
		public Long EGRIdSpotAsociado;
		public String IdEstadoCuentaInversion;
		public String IdEstadoTesoreria;
		public Long ComprobanteTesoreria;
		public String IdEstadoSpot;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FormaDePagoReversaForward {
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public Long IdAbono;
		public Long IdCargo;
		public String IdTipoProducto;
		public String RecibePaga;
		public String IdFormaDePago;
		public String IdModalidadPago;
		public String IdMoneda;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String IdMonedaLiquidacion;
		public BigDecimal Monto;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal MontoLiquidacion;
		public Date FechaIngresoComprobantePagoOperacion;
		public Date FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public String AfectarTesoreria;
		public Long IdCartera;
		public Long IdOperacionCartera;
		public Long FolioCartera;

		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class MovimientoComprobanteTesoreria {
		public Long IdComprobante;
		public Long IdIngresoEgreso;
		public String IdEstado;
	}
	
	public static class ComprobanteCuentaInversion {
		public Long IdCargoAbonoMovimiento;
	}
	
	public static class ObjFechaVencimiento{
		public Long IdCarteraFechaVencimiento;
		public Date FechaVencimiento;
		public Long IdModalidadLiquidacion;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ReversaAnticipoForwardEntrada in = gson.fromJson(jin.toString(), ReversaAnticipoForwardEntrada.class);
		ReversaAnticipoForwardSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("AUTORIZACION"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ReversaAnticipoForwardSalida logica(Connection conn, ReversaAnticipoForwardEntrada in) throws Exception {
		SQL _sql = new SQL();
		ReversaAnticipoForwardSalida out = new ReversaAnticipoForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		Long errCont = 0L;
		HashMap<String, String> mapMsg = new HashMap<>();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		log.write("INICIO :: FWD.ReversaAnticipoForward");

		try {
			if (in.p_tablaOperReversa.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if(MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0){
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
		
			RevisarRequisitosReversaForward validacionReversa = new RevisarRequisitosReversaForward();
			RevisarRequisitosReversaForwardSalida salidaValidacionReversa = validacionReversa.logica(conn, in);
			if (salidaValidacionReversa.p_out_Mensajes.size() > 0) {	
				out.P_OUT_MENSAJES.addAll(salidaValidacionReversa.p_out_Mensajes);
				return out;
			}

			/***** VARIABLES *****/
			Long cantOpeIn = 0L;;
			
			HashMap<String,Object> params = new HashMap<>();
			List<TablaForwardObjIn> operacionesReversa = new ArrayList<TablaForwardObjIn>();
			List<Long> idCartera = new ArrayList<Long>();
			List<Long> folioCarteraAnticipoParcial = new ArrayList<Long>();
			List<Long> folioCartera = new ArrayList<Long>();
			
			for(OperacionForwardObjIn ope: in.p_tablaOperReversa){
				idCartera.add(ope.IdCartera);
				folioCartera.add(ope.FolioCartera);
				cantOpeIn++;
			}
			
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdCartera", idCartera);
			
			//Obtiene operaciones Forward a Reversar
			List<DatosCarteraReversaForward> operacionesForward = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD, params, DatosCarteraReversaForward.class);
			
			List<String> idsTipoProducto = new ArrayList<String>();
			List<Long> idsInstrumento = new ArrayList<Long>();
			List<ObjFechaVencimiento> listaFechaVencimiento = new ArrayList<>();
			
			idCartera = new ArrayList<Long>();
			for(DatosCarteraReversaForward opFw: operacionesForward ){
				for(OperacionForwardObjIn opeFwdIn: in.p_tablaOperReversa){
					if(opFw.IdCartera.longValue() == opeFwdIn.IdCartera.longValue()){
						//Determina si es Reversa de un Anticipo Completo o Parcial
						opFw.TipoAnticipo = opFw.INGMontoMonedaPrincipal.compareTo(opFw.EGRMontoMonedaPrincipal) == 0 ? "COMPLETO" : "PARCIAL";
						
						if (!idsTipoProducto.contains(opFw.IdTipoProducto)) {
							idsTipoProducto.add(opFw.IdTipoProducto);
					    }
						if (!idsInstrumento.contains(opFw.IdInstrumento)) {
					        idsInstrumento.add(opFw.IdInstrumento);
					    }

						ObjFechaVencimiento objFV = new ObjFechaVencimiento();
						objFV.IdCarteraFechaVencimiento =  opeFwdIn.IdCartera;
						objFV.FechaVencimiento = opFw.FechaVencimientoOperacion;
						objFV.IdModalidadLiquidacion = opFw.IdModalidadCumplimiento;
					    listaFechaVencimiento.add(objFV);
						
						TablaForwardObjIn tfi = new TablaForwardObjIn();
						tfi.FolioInterno = opeFwdIn.IdCartera;
						tfi.IdOperacion = opeFwdIn.IdOperacion;
						tfi.FolioCartera = opeFwdIn.FolioCartera;
						tfi.IdTipoOperacionOriginal = opFw.IdTipoOperacion;
						tfi.IdTipoOperacionNuevo = "REVFWD";
						tfi.IdTipoInstrumento = opFw.IdTipoInstrumento;
						tfi.IdClasificacionTipoInstrumento = opFw.IdClasificacionTipoInstrumento;
						tfi.IdTipoProducto = opFw.IdTipoProducto;
						tfi.IdInstrumento = opFw.IdInstrumento;
						tfi.Nemotecnico = opFw.Nemotecnico;
						tfi.IdClienteOriginal = opFw.IdCliente;
						tfi.IdClienteNuevo = opFw.IdCliente;
						tfi.IdPortafolioOriginal = opFw.IdPortafolio;
						tfi.IdPortafolioNuevo = opFw.IdPortafolio;
						tfi.IdPersonaEjecutivoOriginal = opFw.IdAgente;
						tfi.IdPersonaEjecutivoNuevo = opFw.IdAgente;
						tfi.IdTipoCanalIngreso = opFw.IdTipoCanalIngreso;
						tfi.IdMonedaPrincipal = opFw.IdMonedaPrincipal;	
						tfi.IdMonedaSecundaria = opFw.IdMonedaSecundaria;
						tfi.PrecioVencimiento = BigDecimal.ZERO;
						if(opFw.TipoAnticipo.equals("PARCIAL")){
							tfi.MontoOperacionOriginal = opFw.EGRMontoMonedaPrincipal;
							tfi.MontoOperacionNuevo = BigDecimal.ZERO;
							tfi.MontoInicialOriginal = opFw.EGRMontoMonedaPrincipal;
							tfi.MontoInicialNuevo = BigDecimal.ZERO;
							tfi.MontoFinalOriginal = opFw.EGRMontoMonedaSecundaria;
							tfi.MontoFinalNuevo = BigDecimal.ZERO;
							
							folioCarteraAnticipoParcial.add(opeFwdIn.FolioCartera);
						} else {
							tfi.MontoOperacionOriginal = opFw.INGMontoMonedaPrincipal;
							tfi.MontoOperacionNuevo = opFw.EGRMontoMonedaPrincipal;
							tfi.MontoInicialOriginal = opFw.INGMontoMonedaPrincipal;
							tfi.MontoInicialNuevo = opFw.EGRMontoMonedaPrincipal;
							tfi.MontoFinalOriginal = opFw.INGMontoMonedaSecundaria;
							tfi.MontoFinalNuevo = opFw.EGRMontoMonedaSecundaria;
						}
						tfi.FechaOperacion = opFw.FechaOperacion;
						tfi.TCCierreOriginal = opFw.ValorTipoCambioCierre;
						tfi.TCCierreNuevo = opFw.ValorTipoCambioCierre;
						tfi.ParidadCierreOriginal = opFw.ValorParidadCierre;
						tfi.ParidadCierreNuevo = opFw.ValorParidadCierre;
						tfi.PlazoOriginal = opFw.Plazo;
						tfi.PlazoNuevo = opFw.Plazo;//TODO revisar
						tfi.FechaVencimientoOriginal = opFw.FechaVencimientoOperacion;
						tfi.FechaVencimientoNuevo = opFw.FechaVencimientoOperacion;//TODO revisar
						tfi.FechaPagoOriginal = opFw.FechaLiquidacion;
						tfi.FechaPagoNuevo = opFw.FechaLiquidacion;//TODO revisar
						tfi.IdModalidadLiquidacionOriginal = opFw.IdModalidadCumplimiento;
						tfi.IdModalidadLiquidacionNuevo = opFw.IdModalidadCumplimiento;
						tfi.IdReferenciaMercado = 0L;
						tfi.IdPlaza = opFw.IdPlaza;
						tfi.TCCostoOriginal = opFw.ValorTipoCambioCosto;
						tfi.TCCostoNuevo = opFw.ValorTipoCambioCosto;
						tfi.PuntosTasasForwardOriginal = opFw.INGEsPuntos;
						tfi.PuntosTasasForwardNuevo = opFw.INGEsPuntos;
						tfi.PuntosForwardOriginal = opFw.INGTasaPuntosMonedaPrincipal;
						tfi.PuntosForwardNuevo = opFw.INGTasaPuntosMonedaPrincipal;
						tfi.TCForwardOriginal = opFw.TasaCompra;
						tfi.TCForwardNuevo = opFw.TasaCompra;
						tfi.TasasMonedaPrincipalOriginal = BigDecimal.ZERO;
						tfi.TasasMonedaPrincipalNuevo = BigDecimal.ZERO;
						tfi.TasasMonedaSecundariaOriginal = BigDecimal.ZERO;
						tfi.TasasMonedaSecundariaNuevo = BigDecimal.ZERO;
						tfi.MontoDiferidoOriginal = BigDecimal.ZERO;
						tfi.MontoDiferidoNuevo = BigDecimal.ZERO;
						tfi.MontoMargenOriginal = BigDecimal.ZERO;
						tfi.MontoMargenNuevo = BigDecimal.ZERO;
						tfi.IdFixingOriginal = opFw.IdFixing;
						tfi.IdFixingNuevo = opFw.IdFixing;
						tfi.FechaFixingOriginal = opFw.FechaFixing;
						tfi.FechaFixingNuevo = opFw.FechaFixing;
						tfi.HoraFixingOriginal = Fecha.toStringTime(opFw.HoraFixing);
						tfi.HoraFixingNuevo = tfi.HoraFixingOriginal;
						tfi.IdMesa = opFw.IdMesa;
						tfi.IdSpotAsociado = opFw.EGRIdSpotAsociado;
						tfi.Observaciones = opFw.Glosa;
						tfi.IdEstado = opFw.IdEstado;
						tfi.TasaDescuento = BigDecimal.ZERO;
						tfi.TipoCambioAnticipo = BigDecimal.ZERO;
						tfi.MontoARecibirAnticipo = BigDecimal.ZERO;
						tfi.TipoCambioObservado = BigDecimal.ZERO;
						tfi.TipoAnticipo = opFw.TipoAnticipo;
						operacionesReversa.add(tfi);
						
						//Se obtiene IdCartera de las operaciones Forward para realizar las validaciones
						idCartera.add(opeFwdIn.IdCartera);
					}
				}
			}
			
			//Obtiene la operación forward Vigente de los Anticipos Parciales
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FolioCartera", folioCarteraAnticipoParcial);
			List<DatosCarteraReversaForward> operacionesAnticipoParcial = _sql.query(conn, SQL_SELECT_OPERACIONES_ANTICIPO_PARCIAL_FORWARD, params, DatosCarteraReversaForward.class);
			
			for(DatosCarteraReversaForward opFw: operacionesAnticipoParcial ){
				for(TablaForwardObjIn tfi: operacionesReversa){
					if(opFw.FolioCartera.longValue() == tfi.FolioCartera.longValue()){
						tfi.MontoOperacionNuevo = tfi.MontoOperacionOriginal.add(opFw.INGMontoMonedaPrincipal);
						tfi.MontoOperacionOriginal = opFw.INGMontoMonedaPrincipal;
						tfi.MontoInicialNuevo = tfi.MontoInicialOriginal.add(opFw.INGMontoMonedaPrincipal);
						tfi.MontoFinalNuevo = tfi.MontoFinalOriginal.add(opFw.INGMontoMonedaSecundaria);
						tfi.FolioInterno = opFw.IdCartera;
					}
				}
			}
			
			
			/***** VALIDACIONES *****/
			// Validacion movimiento de Tesoreria y de Cuenta Inversion deben estar ANULADOS
			// para Reversar operacion Forward Compensada
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdCartera", idCartera);

			List<DatosValidacionesForwardReversar> validacionesReversaForward = _sql.query(conn, SQL_SELECT_VALIDACIONES_REVERSA_ANTICIPO_FORWARD, params, DatosValidacionesForwardReversar.class);

			for (DatosValidacionesForwardReversar vfr : validacionesReversaForward) {
				// Validacion movimiento de Tesoreria y de Cuenta Inversion
				// para Reversar operacion Forward Compensada
				if (vfr.ModalidadLiquidacionValor.equals("COMPENSADA") && vfr.IdEstado.equals("LIQ") && vfr.IdEstadoTesoreria.equals("LIQ") && !mapMsg.containsValue(vfr.ComprobanteTesoreria.toString())) {
					mapMsg.put("Id",vfr.ComprobanteTesoreria.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "FWD_COMPROBANTE_TESORERIA_LIQUIDADO", mapMsg, conn, in.p_IdEmpresa));
				}
			}

			// Retorna si existen errores
			errCont = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errCont.equals(0L)) {
				return out;
			}
			
			/***** REVERSA DE FORWARD EN CARTERA *****/
			log.write("MESA FORWARD :: Llamada Ingresar Forward Cartera");
			IngresarForwardCartera ingresarForwardCartera = new IngresarForwardCartera();
			IngresarForwardCarteraEntrada ingresarForwardCarteraIn = new IngresarForwardCarteraEntrada();
			ingresarForwardCarteraIn.p_IdEmpresa = in.p_IdEmpresa;
			ingresarForwardCarteraIn.p_IdPais = in.p_IdPais;
			ingresarForwardCarteraIn.p_IdUsuario = in.p_IdUsuario;
			ingresarForwardCarteraIn.p_IdBolsa = in.p_IdBolsa;
			ingresarForwardCarteraIn.p_IdSucursal = in.p_IdSucursal;
			ingresarForwardCarteraIn.p_IdModulo = in.p_IdModulo;
			ingresarForwardCarteraIn.p_UsaReglasNegocio = "N";// En Reversa FWD no es necesario validar Reglas
			ingresarForwardCarteraIn.p_tablaForward = operacionesReversa;
			IngresarForwardCarteraSalida ingresarForwardCarteraOut = ingresarForwardCartera.logica(conn, ingresarForwardCarteraIn);

			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + ingresarForwardCarteraOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(ingresarForwardCarteraOut.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(ingresarForwardCarteraOut.P_OUT_MENSAJES) > 0) {
				return out;
			}

			// Salida del Servicio
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			out.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
			for (DatosCarteraReversaForward opFw : operacionesForward) {
				for (TablaForwardObjOut fwdCarOut : ingresarForwardCarteraOut.P_OUT_TBLIDFORWARDRETIROS) {
					if (opFw.IdCartera.longValue() == fwdCarOut.IdCartera.longValue()) {
						OperacionForwardObjOut opOut = new OperacionForwardObjOut();
						opOut.FolioInterno = fwdCarOut.FolioInterno;
						opOut.IdCartera = fwdCarOut.IdCartera;
						opOut.IdOperacion = fwdCarOut.IdOperacion == null ? opFw.IdOperacion : fwdCarOut.IdOperacion;
						opOut.FolioCartera = fwdCarOut.FolioCartera;
						opOut.IdEstado = opFw.IdEstado;
						out.P_OUT_TBLIDFORWARD.add(opOut);
					}
				}
			}

			/***** FORMAS DE PAGO OPERACION FORWARD *****/
			List<ComprobantePagoOperacionObj_IN> comprobantePagoProceso = new ArrayList<ComprobantePagoOperacionObj_IN>();
			long i = 0;

			// Obtiene FORMAS DE PAGO de operacion ANU
			params.put("FolioCartera", folioCartera);
			params.put("IdCartera", idCartera);
			List<FormaDePagoReversaForward> formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_OPERACION_REVERSADA, params, FormaDePagoReversaForward.class);
			
			//Formas de pago Mesa
			HashMap<String, Object> selectFormaPagoMesaIN = new HashMap<>();
			selectFormaPagoMesaIN.put("IdTipoProducto" , idsTipoProducto);
			List<FormaDePagoMesa> formaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, selectFormaPagoMesaIN, FormaDePagoMesa.class);
			HashMap<String, Object> formaDePagoMesaHash = Utils.List2Hash(formaDePagoMesaList, "IdFormaDePago");
			
			//Verificar si aplica feriado (caso modalidad entrega fisica) 
			Long aplicaFeriadoMonedaPrincipal = 0L;
			SQL sql = new SQL();	
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("IdInstrumento", idsInstrumento);
			parametros.put("IdMonedaPrincipal", "USD");
			parametros.put("IdMonedaSecundaria", "CLP");

			aplicaFeriadoMonedaPrincipal = sql.countQuery(conn, SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL, parametros);
			ArrayList<Long> listaPais= new 	ArrayList<>(); 
			listaPais.add(in.p_IdPais); 
			listaPais.add(223L); 
			
			//Se prepara entrada para servicios ObtenerDiaSiguienteMultiplesPaises y ObtenerDiaSiguiente
			OutObtenerDiaSiguienteMultiplesPaises outObtenerFechaHabil = null;
			ObtenerDiaSiguienteMultiplesPaises obtenerDiaSiguienteMulti = new ObtenerDiaSiguienteMultiplesPaises(); 
			InObtenerDiaSiguienteMultiplesPaises obtenerFechaConFeriado = new InObtenerDiaSiguienteMultiplesPaises();
			obtenerFechaConFeriado.p_diaHabil="S"; 
			obtenerFechaConFeriado.p_soloCaeDiaHabil ="S";
			
			OutObtenerDiaSiguiente salDiaSgte = null;
			ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente(); 
			InObtenerDiaSiguiente diaSgteIn = new InObtenerDiaSiguiente();
			diaSgteIn.p_idPais = in.p_IdPais;
			diaSgteIn.p_diaHabil = "S";
			
			HashMap<Long, Object> listaFechaVencimientoHash = Utils.List2Hash(listaFechaVencimiento, "IdCarteraFechaVencimiento");
			
			if (listaFechaVencimientoHash != null) {
				for(FormaDePagoReversaForward fpIn : formaDePago){
					ObjFechaVencimiento ObjLFV = (ObjFechaVencimiento) listaFechaVencimientoHash.get(fpIn.IdOperacion);
					//Si aplica feriado y modalidad liquidacion es entrega fisica llamamos al servicio ObtenerDiaSiguienteMultiplesPaises
					if(aplicaFeriadoMonedaPrincipal==1 && ObjLFV != null && ObjLFV.IdModalidadLiquidacion != null && ObjLFV.IdModalidadLiquidacion == 7) {
						cl.bcs.plataforma.pla.pojo.FechasIn fechaIn=new cl.bcs.plataforma.pla.pojo.FechasIn();
						fechaIn.idInterno = fpIn.IdComprobantePagoOperacion; 
						fechaIn.fechaInicial = Fecha.toStringDate(((ObjFechaVencimiento) listaFechaVencimientoHash.get(fpIn.IdOperacion)).FechaVencimiento);
						fechaIn.numeroDias = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias;
						fechaIn.paises=listaPais;
						obtenerFechaConFeriado.p_fechas.add(fechaIn);
					//Caso contrario llamaremos al servicio ObtenerDiaSiguiente	
					} else {
						FechasIn fechas = new FechasIn();
						fechas.IdInterno = fpIn.IdComprobantePagoOperacion; 
						fechas.fechaInicial = Fecha.toStringDate(((ObjFechaVencimiento) listaFechaVencimientoHash.get(fpIn.IdOperacion)).FechaVencimiento);
						fechas.numeroDias = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias;
						diaSgteIn.p_fechas.add(fechas);
					} 
				}
			}

			if(obtenerFechaConFeriado.p_fechas.size() > 0) {
				outObtenerFechaHabil = obtenerDiaSiguienteMulti.logica(conn, obtenerFechaConFeriado);
			}

			if(diaSgteIn.p_fechas.size() > 0) {
				salDiaSgte = diaSgte.logica(conn, diaSgteIn);
			}
			
			for(TablaForwardObjOut fwdCarOut: ingresarForwardCarteraOut.P_OUT_TBLIDFORWARDINGRESOS){
				for(FormaDePagoReversaForward fprf: formaDePago){
					if(fwdCarOut.IdCartera.longValue() == fprf.IdCartera.longValue() && fwdCarOut.FolioInterno.longValue() == fprf.IdOperacion){
						
						String fechaCondicionLiquidacion = null;
						
						//Se setea fecha liquidación con dia habil del servicio ObtenerDiaSiguienteMultiplesPaises (si aplica feriado y modalidad entrega fisica)
						HashMap<Long, Object> outObtenerFechaHabilHash = null;
						OutObtenerDiaSiguienteMultiplesPaises.FechasOut ObjFechaHabil = null;
						if(outObtenerFechaHabil != null && outObtenerFechaHabil.p_out_fechas != null) {
							outObtenerFechaHabilHash = Utils.List2Hash(outObtenerFechaHabil.p_out_fechas, "idInterno");
							ObjFechaHabil = (OutObtenerDiaSiguienteMultiplesPaises.FechasOut) outObtenerFechaHabilHash.get(fprf.IdComprobantePagoOperacion);
						}
						if(ObjFechaHabil != null && ObjFechaHabil.fechaCalculada != null && ObjFechaHabil.numeroDiasInicial.equals(((FormaDePagoMesa) formaDePagoMesaHash.get(fprf.IdFormaDePago)).Dias)){
							fechaCondicionLiquidacion = ObjFechaHabil.fechaCalculada;
						}
						
						//Se setea fecha liquidacion con dia habil del servicio ObtenerDiaSiguiente
						HashMap<Long, Object> salDiaSgteHash = null;
						OutObtenerDiaSiguiente.FechasOut ObjFechaDiaSgt = null;			
						if(salDiaSgte != null && salDiaSgte.p_out_fechas != null) {
							salDiaSgteHash = Utils.List2Hash(salDiaSgte.p_out_fechas, "IdInterno");
							ObjFechaDiaSgt = (OutObtenerDiaSiguiente.FechasOut) salDiaSgteHash.get(fprf.IdComprobantePagoOperacion);
						}
						if(ObjFechaDiaSgt != null && ObjFechaDiaSgt.fechaCalculada != null && ObjFechaDiaSgt.numeroDiasInicial.equals(((FormaDePagoMesa) formaDePagoMesaHash.get(fprf.IdFormaDePago)).Dias)){
							fechaCondicionLiquidacion = ObjFechaDiaSgt.fechaCalculada;
						}
						
						ComprobantePagoOperacionObj_IN cpoIn = new ComprobantePagoOperacionObj_IN();
						cpoIn.IdInternoPagoOperacion = i;
						cpoIn.IdComprobantePagoOperacion = 0L;
						cpoIn.IdEmpresa = in.p_IdEmpresa;
						cpoIn.IdOperacion = fprf.IdCartera;
						cpoIn.IdIngresoEgreso = fprf.IdIngresoEgreso;
						cpoIn.IdAbono = fprf.IdAbono;
						cpoIn.IdCargo = fprf.IdCargo;
						cpoIn.IdIngresoEgresoAnticipo = 0L;
						cpoIn.IdAbonoAnticipo = 0L;
						cpoIn.IdCargoAnticipo = 0L;
						cpoIn.IdTipoProducto = fprf.IdTipoProducto;
						cpoIn.RecibePagaPortafolio = fprf.RecibePaga;
						cpoIn.IdFormaDePago = fprf.IdFormaDePago;
						cpoIn.IdModalidadPago = fprf.IdModalidadPago;
						cpoIn.FechaIngresoComprobantePagoOperacion = Fecha.toStringDate(fprf.FechaIngresoComprobantePagoOperacion);
						cpoIn.FechaLiquidacionComprobantePagoOperacion = fechaCondicionLiquidacion != null ? fechaCondicionLiquidacion : Fecha.toStringDate(fprf.FechaLiquidacionComprobantePagoOperacion);
						cpoIn.IdEstado = fprf.IdEstado;
						cpoIn.AfectarTesoreria = fprf.AfectarTesoreria;
						
						if(fprf.IdMoneda.equals(fprf.IdMonedaPrincipal)){
							cpoIn.IdMoneda = fprf.IdMonedaPrincipal;
							cpoIn.Monto = fprf.MontoMonedaPrincipal;
						} else if (fprf.IdMoneda.equals(fprf.IdMonedaSecundaria)) {
							cpoIn.IdMoneda = fprf.IdMonedaSecundaria;
							cpoIn.Monto = fprf.MontoMonedaSecundaria;
						}
						//cpoIn.IdMonedaLiquidacion = cpoIn.IdMoneda;
						//cpoIn.MontoLiquidacion = cpoIn.Monto;
						
						comprobantePagoProceso.add(cpoIn);
						i++;
					}
				}
			}
			
			//Comprobante Pago Operacion de Mesa
			if(comprobantePagoProceso.size() > 0){
				log.write("MESA FORWARD :: Llamada Ingreso Comprobante Pago Operación");
				CrearComprobantePagoOperacion crearComPag = new CrearComprobantePagoOperacion();
				CrearComprobantePagoOperacion_IN crearComPagIn = new CrearComprobantePagoOperacion_IN();
				crearComPagIn.p_IdEmpresa = in.p_IdEmpresa;
				crearComPagIn.p_IdUsuario = in.p_IdUsuario;
				crearComPagIn.p_IdModulo = in.p_IdModulo;
				crearComPagIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				crearComPagIn.p_ComprobantePagoOperacion = comprobantePagoProceso;
				CrearComprobantePagoOperacion_OUT crearComPagOut = crearComPag.logica(conn, crearComPagIn);
				
				out.P_OUT_MENSAJES.addAll(crearComPagOut.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(crearComPagOut.P_OUT_MENSAJES) > 0){
					return out;
				}
			}
			
			/***************************** ANULACION DE COMPROBANTE EN TESORERIA **********************************/
			if(operacionesForward.get(0).IdEstado.equals("LIQ") && operacionesForward.get(0).EGRIdTipoComprobante.equals("TES")) {
				log.write("Llamada a Anular Registros Tesoreria");
				
				params.clear();
				params.put("p_ComprobanteOrigen", in.p_tablaOperReversa.get(0).FolioCartera);
				
				List<MovimientoComprobanteTesoreria> movimientoComprobanteTesoreria = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE_TESORERIA, params, MovimientoComprobanteTesoreria.class);
				
				AnularRegistros_Entrada entradaAnularTesoreria = new AnularRegistros_Entrada();
				entradaAnularTesoreria.p_FechaMovimiento = fechaSistema;
				entradaAnularTesoreria.p_IdEmpresa = in.p_IdEmpresa;
				entradaAnularTesoreria.p_IdModulo = in.p_IdModulo;
				entradaAnularTesoreria.p_IdUsuario = in.p_IdUsuario;
				entradaAnularTesoreria.p_simularOperacion = "N";
				entradaAnularTesoreria.p_UsaReglasNegocio = "S";
				
				List<Registro_Entrada> lista = new ArrayList<>();
				for (DatosCarteraReversaForward opFw : operacionesForward) {
					if(opFw.IdEstado.equals("LIQ")) {
						Registro_Entrada arg0 = new Registro_Entrada();
						arg0.Id = (long) 0;
						lista.add(arg0);
					}
				}
				entradaAnularTesoreria.p_RegistrosAProcesar = lista;
				
				List<String> entidades = Arrays.asList("TES.Comprobante","TES.IngresoEgreso");
				
				for(String ent : entidades) {

					entradaAnularTesoreria.p_RegistrosAProcesar.get(0).Entidad = ent;
					
					if(ent.contentEquals("TES.IngresoEgreso")) {
						params.clear();
						params.put("IdIngresoEgreso", movimientoComprobanteTesoreria.get(0).IdIngresoEgreso);
						List<MovimientoComprobanteTesoreria> IngresoEgresoTes = _sql.query(conn, SQL_SELECT_INGRESOEGRESO_TESORERIA, params, MovimientoComprobanteTesoreria.class);
						if (IngresoEgresoTes.get(0).IdEstado.equals("ANU"))
							continue;
						entradaAnularTesoreria.p_RegistrosAProcesar.get(0).Id = movimientoComprobanteTesoreria.get(0).IdIngresoEgreso;
					}else {
						params.clear();
						params.put("IdComprobante", movimientoComprobanteTesoreria.get(0).IdComprobante);
						List<MovimientoComprobanteTesoreria> ComprobanteTes = _sql.query(conn, SQL_SELECT_COMPROBANTE_TESORERIA, params, MovimientoComprobanteTesoreria.class);
						if (ComprobanteTes.get(0).IdEstado.equals("ANU"))
							continue;
						entradaAnularTesoreria.p_RegistrosAProcesar.get(0).Id = movimientoComprobanteTesoreria.get(0).IdComprobante;
					}
					
					AnularRegistros anularTesoreriaService = new AnularRegistros();
					AnularRegistros_Salida salidaAnularTesoreria = anularTesoreriaService.logica(conn, entradaAnularTesoreria);
					log.write("Salida AnularRegistrosTesoreria: " + salidaAnularTesoreria.toString());
					
					out.P_OUT_MENSAJES.addAll(salidaAnularTesoreria.P_OUT_MENSAJES);
					
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
						out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
						log.write("Fin");
						log.write(out);
						return out;
					}
					
					/***** ANULACION CUENTA INVERSION *****/
					params.clear();
					params.put("IdOperacion", in.p_tablaOperReversa.get(0).FolioCartera);
					List<ComprobanteCuentaInversion> idCuentaInversion = _sql.query(conn, SQL_SELECT_COMPROBANTE_CUENTAINVERSION_TESORERIA, params, ComprobanteCuentaInversion.class);
					
					// Anula el movimiento cta.inv contrario al de tesoreria
					AnularCargoAbono anularCargoAbono = new AnularCargoAbono();
					AnularCargoAbono_Entrada entrada = new AnularCargoAbono_Entrada();
					CargoAbono_Entrada cargoAbono = new CargoAbono_Entrada();
					cargoAbono.GlosaAnulacion = "REVERSA ANTICIPO FORWARD";
					cargoAbono.IdCargoAbonoMovimiento = idCuentaInversion.get(0).IdCargoAbonoMovimiento;
					
					try {
						entrada.p_IdEmpresa = in.p_IdEmpresa;
						entrada.p_IdUsuario = in.p_IdUsuario;
						entrada.p_IdModulo = "CTA";
						entrada.p_simularOperacion = "N";
						entrada.p_UsaReglasNegocio = "S";
						entrada.p_FechaMovimiento = fechaSistema;
						entrada.p_EsAnulacionManual = "N";
						entrada.p_EjecutaAutorizacion = "N";
						entrada.p_CargoAbonoAAnular.add(cargoAbono);

						AnularCargoAbono_Salida salida = anularCargoAbono.logica(conn, entrada);
						
						out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
						
						if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
							out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
							log.write("Fin");
							log.write(out);
							return out;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			/***************************** ANULACION DE OPERACION SPOT **********************************/
			if(operacionesForward.get(0).IdEstado.equals("LIQ") && operacionesForward.get(0).EGRIdTipoComprobante.equals("CUS")) {
				String fwdIntegradoConSpot = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "MESA", "FwdIntegradoConSpot");
				if (fwdIntegradoConSpot.equals("S")) {
					log.write("Llamada a Anular Operacion Spot");
					params.clear();
					params.put("FolioCartera", in.p_tablaOperReversa.get(0).FolioCartera);
					params.put("MontoMonedaPrincipal", operacionesForward.get(0).EGRMontoMonedaPrincipal);
					params.put("TipoCambioCierre", operacionesForward.get(0).ValorTipoCambioCierre);
					
					List<OperacionSpot> operacionesSpotAnu = _sql.query(conn, SQL_SELECT_OPERACION_SPOT, params, OperacionSpot.class);
					if (operacionesSpotAnu.size() > 0) {
						operacionesSpotAnu.get(0).NumeroOperacion = operacionesSpotAnu.get(0).IdOperacionSpot.toString();
						
						
						AnularOperacionSpot anularOperacionSpot = new AnularOperacionSpot();
						AnularOperacionSpot.AnularOperacionSpotEntrada anularOperacionSpotEntrada = new AnularOperacionSpot.AnularOperacionSpotEntrada();
						anularOperacionSpotEntrada.p_IdEmpresa = in.p_IdEmpresa;
						anularOperacionSpotEntrada.p_IdUsuario = in.p_IdUsuario;
						anularOperacionSpotEntrada.p_IdModulo = in.p_IdModulo;
						anularOperacionSpotEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
						anularOperacionSpotEntrada.p_DatosOperacion = operacionesSpotAnu;
						AnularOperacionSpot.AnularOperacionSpotSalida anularOperacionSpotSalida = anularOperacionSpot.logica(conn, anularOperacionSpotEntrada);
					
						out.P_OUT_MENSAJES.addAll(anularOperacionSpotSalida.P_OUT_MENSAJES);
						
						if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
							out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
							log.write("Fin");
							log.write(out);
							return out;
						}
					} else {
						log.write("Operacion Spot no encontrada");
					}
				}
			}
			
			/***************************** ANULACION DE CUENTA INVERSION **********************************/
			
			//CONSULTAR IdCargoAbonoMovimiento
			
			if(operacionesForward.get(0).IdEstado.equals("LIQ") && operacionesForward.get(0).EGRIdTipoComprobante.equals("CTA")) {
				AnularCargoAbono anularCargoAbono = new AnularCargoAbono();
				AnularCargoAbono_Entrada entrada = new AnularCargoAbono_Entrada();
				CargoAbono_Entrada cargoAbono = new CargoAbono_Entrada();
				
				
				params.clear();
				params.put("IdComprobante", folioCartera.get(0).intValue());
				params.put("Monto", operacionesForward.get(0).EGRMontoARecibirAnticipo.toBigInteger().abs());
				
				List<ComprobanteCuentaInversion> idComprobanteCuentaInversion = _sql.query(conn, SQL_SELECT_COMPROBANTE_CUENTAINVERSION, params, ComprobanteCuentaInversion.class);
				
				cargoAbono.GlosaAnulacion = "REVERSA ANTICIPO FORWARD";
				cargoAbono.IdCargoAbonoMovimiento = idComprobanteCuentaInversion.get(0).IdCargoAbonoMovimiento;
				
				try {
					entrada.p_IdEmpresa = in.p_IdEmpresa;
					entrada.p_IdUsuario = in.p_IdUsuario;
					entrada.p_IdModulo = "CTA";
					entrada.p_simularOperacion = "N";
					entrada.p_UsaReglasNegocio = "S";
					entrada.p_FechaMovimiento = fechaSistema;
					entrada.p_EsAnulacionManual = "N";
					entrada.p_EjecutaAutorizacion = "N";
					entrada.p_CargoAbonoAAnular.add(cargoAbono);

					AnularCargoAbono_Salida salida = anularCargoAbono.logica(conn, entrada);
					
					out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
					
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0) {
						out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
						log.write("Fin");
						log.write(out);
						return out;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
			/***************************** CAMBIA ESTADO OPERACION MESA **********************************/

			List<Long> idOperacion = new ArrayList<Long>();
			for(OperacionForwardObjIn ope: in.p_tablaOperReversa){
				idOperacion.add(ope.IdOperacion);
			}
			
			if (idOperacion.size()>0) {
				try {
					params.clear();
					params.put("IdEmpresa", in.p_IdEmpresa);
					params.put("IdOperacion", idOperacion);
					_sql.query(conn, SQL_UPDATE_OPERACIONES_MESA, params, ComprobanteCuentaInversion.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesaAntParc = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":97,\"p_IdBolsa\":1,\"p_IdSucursal\":1,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"N\",\"p_tablaOperReversa\":[{\"IdCartera\":569197,\"FolioCartera\":569344,\"IdOperacion\":150869,\"IdEstado\":\"LIQ\"}]}";
			ReversaAnticipoForwardEntrada Objin = gson.fromJson(jsonDesaAntParc, ReversaAnticipoForwardEntrada.class);
			
			ReversaAnticipoForward demo = new ReversaAnticipoForward();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ReversaAnticipoForwardSalida ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}
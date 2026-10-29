package cl.bcs.normativa.ils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.ils.ConsultarSaldosCuentasContables.ConsultarSaldosCuentasContables_Entrada;
import cl.bcs.normativa.ils.ConsultarSaldosCuentasContables.ConsultarSaldosCuentasContables_Salida;
import cl.bcs.normativa.ils.DetalleCoberturaILS.ObjDetalleCobertura;
import cl.bcs.normativa.ils.pojo.ConstantesILS;
import cl.bcs.normativa.ils.pojo.CuentaContable;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.PersonaRelacionaExistente;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GeneraReporteILS implements JavaCall{
	private static final Logger logger = LogManager.getLogger(GeneraReporteILS.class);
	//private final String SQL_SELECT_SALDO_CUENTA_CONTABLE = loadResourceToString("cl/bcs/normativa/ils/sql/ConsultarSaldosCuentasContables_SelectSaldosCuentasContables.sql");
	private final String SQL_SELECT_INFORME = loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarInforme.sql");
	private final String SQL_SELECT_INDICE = loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarIndice.sql");
	private final String SQL_SELECT_CONCEPTO = loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarConcepto.sql");
	private final String SQL_SELECT_ITEM = loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarItem.sql");
	private final String SQL_SELECT_DERECHO_RETROCOMPRA 		= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectDerechoRetroCompra.sql");
	private final String SQL_SELECT_DERECHO_MONEDAEXTRANJERA 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectDerechoMonedaExtranjera.sql");
	private final String SQL_SELECT_OBLIGACION_RETROVENTA 		= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectObligacionRetroVenta.sql");
	private final String SQL_SELECT_SIMULTANEA_DERECHOS 		= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectSimultaneaDerechos.sql");
	private final String SQL_SELECT_SIMULTANEA_GARANTIAS 		= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectSimultaneaGarantias.sql");
	private final String SQL_SELECT_SIMULTANEA_OBLIGACIONES 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectSimultaneaObligaciones.sql");
	
	private final String SQL_DELETE_INFORME_RESULTADO		 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultado.sql");
	private final String SQL_DELETE_INFORME_RESULTADO_INDICE 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultadoIndice.sql");
	private final String SQL_DELETE_INFORME_RESULTADO_CONCEPTO 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultadoConcepto.sql");
	private final String SQL_DELETE_INFORME_RESULTADO_ITEM	 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultadoItem.sql");
	
	private final String SQL_INSERT_INFORME_RESULTADO		 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultado.sql");
	private final String SQL_INSERT_INFORME_RESULTADO_INDICE 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoIndice.sql");
	private final String SQL_INSERT_INFORME_RESULTADO_CONCEPTO 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoConcepto.sql");
	private final String SQL_INSERT_INFORME_RESULTADO_ITEM	 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoItem.sql");
	private final String SQL_INSERT_INFORME_RESULTADO_ITEM_DETALLE 	= loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoItemDetalle.sql");
	
	private final String SQL_SELECT_FECHAS_REPORTES = loadResourceToString("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectFechasUltimosReportes.sql");
	
	public final ConstantesILS _c = new ConstantesILS();
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class GeneraReporteILS_Entrada {
		public Long					p_IdEmpresa;
		public Long					p_IdUsuario;
		public String				p_IdModulo;
		public String				p_UsaReglasNegocio;
		public String				p_FechaInforme;
		
		public List<InformeIn>		p_Informes;
	    
		
	    public GeneraReporteILS_Entrada() {
	    	p_Informes = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class GeneraReporteILS_Salida {	
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public GeneraReporteILS_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	
	public static class FechasInformes {
		public String 		FechaContable;
		public String		NombreReporte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformeIn {
		public Long 				p_IdTipoInforme;
/*		public Long 				p_IdIndice;
		public Long 				p_IdConcepto;
		public Long 				p_IdItem;
		public String 				p_FechaInforme;*/
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Informe {
		public Long			IdTipoInforme;
		public Long			IdEmpresa;
		public String		Nombre;
		public Long			IdIndice;
		public Integer		Orden;
		
		//calculado
		public BigDecimal	MontoCalculado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Indice {
		public Long			IdIndice;
		public String		Nombre;
		public String		NombreCorto;
		public Long			IdConcepto;
		public Long			IdEmpresa;
		public String		Posicion;
		public BigDecimal	Factor;
		public String		Operacion;
		public String		Tipo;//Puede ser %. si se definen mas hay que implementarlos
		
		//calculado
		public BigDecimal	MontoCalculado;
			
		public Indice() {
			// TODO Auto-generated constructor stub
			MontoCalculado = BigDecimal.ZERO;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Concepto {
		public Long			IdConcepto;
		public String		Nombre;
		public String		NombreCorto;
		public Long			IdConceptoGrupo;
		public Long			IdItem;
		public String		Operacion;
		public BigDecimal	Factor;
		public Long			IdIndice;
		public String		Posicion;
		
		//calculado
		public BigDecimal	MontoCalculado;
		
		public Concepto() {
			// TODO Auto-generated constructor stub
			MontoCalculado = BigDecimal.ZERO;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Item {
		public Long			IdItem;
		public Long			IdIndice;
		public Long			IdConceptoGrupo;
		public Long			IdConcepto;
		public String		IdTipoItem;
		public String		Nombre;
		public String		NombreCorto;
		public String		CuentaContable;
		public String		CuentaOptimus;
		public String		SaldoDebeHaber;
		public String		Condicion;
		public BigDecimal	Monto;
		public String		Operacion;
		public BigDecimal	Factor;
		public String		MayorACero;
		public String		MenorACero;
		public String		PermiteCero;
		public Long			IdProcesoFuncion;
		public String		ProcesoFuncion;
		public String		NombreFuncion;
		public String		OperacionGC;
		
		//calculado
		public BigDecimal	MontoCalculado;
		public BigDecimal	MontoContable;
		public BigDecimal	MontoDigitado;
		public BigDecimal	MontoProgramado;
		
		public Item() {
			// TODO Auto-generated constructor stub
			MontoCalculado = BigDecimal.ZERO;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ListaInforme {
		List<Informe>	informe;
		List<Indice>	indice;
		List<Concepto>	concepto;
		List<Item>		item;
		List<CuentaContable>	cuentaContable;
		
		BigDecimal		montoDerechosRetroCompra;
		BigDecimal		montoDerechosMonedaExtranjera;
		BigDecimal		montoObligacionesRetroVenta;
		BigDecimal		montoSimultaneasDerechos;
		BigDecimal		montoSimultaneasGarantias;
		BigDecimal		montoSimultaneasObligaciones;
		BigDecimal		montoCarteraObligaciones;
		
		public ListaInforme() {
			// TODO Auto-generated constructor stub
			informe = new ArrayList<GeneraReporteILS.Informe>();
			indice 	= new ArrayList<GeneraReporteILS.Indice>();
			concepto= new ArrayList<GeneraReporteILS.Concepto>();
			item	= new ArrayList<GeneraReporteILS.Item>();
			cuentaContable	= new ArrayList<CuentaContable>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SaldoOperacion {
		public BigDecimal		Monto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformeResultado {
		public Long			IdTipoInforme;
		public String 		FechaInforme;
		public String 		FechaActual;
		public String		HoraActual;
		public String		IdEstado;//'VIG'
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InformeResultadoIndice {
		public Long			IdResultadoIndice;
		public Long			IdTipoInforme;
		public String		FechaContable;
		public BigDecimal	Monto;
		public Long			IdIndice;
		public String		NombreIndice;
		public String		Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InformeResultadoConcepto {
		public Long			IdResultadoConcepto;
		public Long			IdResultadoIndice;
		public Long			IdTipoInforme;
		public String		FechaContable;
		public BigDecimal	Monto;
		public String		NombreConcepto;
		public Long			IdConcepto;
		//public Long			IdConceptoGrupo;
		public Long			IdIndice;
		public String		Posicion;
		public String		Operador;
		public BigDecimal	Factor;
		public BigDecimal	MontoCalculado;
		public String		Estado;
		
		public InformeResultadoConcepto() {
			MontoCalculado = BigDecimal.ZERO;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InformeResultadoItem {
		public Long			IdResultadoItem;
		public Long			IdResultadoConcepto;
		public String		IdTipoItem;
		public Long			IdTipoInforme;
		public String		FechaContable;
		public Long			IdItem;
		public Long			IdConcepto;
		public Long			IdConceptoGrupo;
		public String		NombreItem;
		public BigDecimal	Monto;
		public String		Operacion;
		public BigDecimal	Factor;
		public BigDecimal	MontoCalculado;
		public BigDecimal	MontoContable;
		public BigDecimal	MontoDigitado;
		public BigDecimal	MontoProgramado;
		public String		PosicionIndice;
		public String 		PositivoNegativo;
		
		
		public InformeResultadoItem() {
			MontoCalculado = BigDecimal.ZERO;
			MontoContable = BigDecimal.ZERO;
			MontoDigitado = BigDecimal.ZERO;
			MontoProgramado = BigDecimal.ZERO;
		}


		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InformeResultadoItemDetalle {
		public Long			IdResultadoItemDetalle;
		public Long			IdResultadoItem;
		public String		IdTipoItem;
		public BigDecimal	Monto;
		public Long			IdItem;
		public Long			IdConcepto;
		public Long			IdIndice;
		public Long			IdConceptoGrupo;
		public String		NombreItem;
		public String		CuentaContable;
		public String		SaldoDebeHaber;
		public String		Condicion;
		public BigDecimal	MontoCondicion;
		public String		Operacion;
		public BigDecimal	FactorItem;
		public BigDecimal	MontoCalculado;
		public BigDecimal	MontoContable;
		public BigDecimal	MontoProgramado;
		public BigDecimal	MontoDigitado;
		public String		PosicionIndice;
		public String		MayorACero;
		public String		MenorACero;
		public String		PermiteCero;
		public String			ProcesoFuncion;
	   
	   public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformeResultadoItems {
		public List<InformeResultadoItem>			infResGrupoConcepto;
		public List<InformeResultadoItemDetalle>			infResItemDet;
		
	   public String toString() {
			return Utils.printJson(this, false);
		}
	}
	//-------------------------------------------------
	// METODOS UTILES 
	//-------------------------------------------------	
	
	private BigDecimal getIndice (Long idIndice, ListaInforme lista) {
		List<Indice> indices = lista.indice.stream().filter(i -> i.IdIndice.equals(idIndice)).collect(Collectors.toList());
		BigDecimal montoIndice = BigDecimal.ZERO;
		System.out.println(indices.toString());
		BigDecimal numerador 	= BigDecimal.ONE;
		BigDecimal denominador 	= BigDecimal.ONE;
		String Tipo = "";
		
		for (Indice in: indices) {
			System.out.println("IdConcepto:::"+in.IdConcepto);
			
			in.MontoCalculado = getConcepto(in.IdConcepto, lista, in.IdIndice);
			in.MontoCalculado = in.MontoCalculado.multiply(in.Factor);
			if (in.Posicion.equalsIgnoreCase(_c.POSICION_NUMERADOR)) {
				if (in.Operacion.equals(_c.SIGNO_MENOS)) {
					numerador = numerador.subtract(in.MontoCalculado);		
				}
				else
				{
					numerador = numerador.add(in.MontoCalculado);		
				}
			}
			if (in.Posicion.equalsIgnoreCase(_c.POSICION_DENOMINADOR)) {
				if (in.Operacion.equals(_c.SIGNO_MENOS)) {
					denominador = denominador.subtract(in.MontoCalculado);
				}
				else {
					denominador = denominador.add(in.MontoCalculado);
				}
			}
			Tipo =in.Tipo;
		}
		//calculo del indice
		montoIndice = numerador.divide(denominador,20,RoundingMode.HALF_UP);
		if (denominador.compareTo(BigDecimal.ONE) == 0 ) {
			montoIndice = montoIndice.divide(new BigDecimal(1000),20,RoundingMode.HALF_UP);
		}
		
		if (Tipo.equals("%")) {
			montoIndice = montoIndice.multiply(new BigDecimal(100));
		}
		return montoIndice;
	}
	
	private BigDecimal getConcepto (Long idConcepto, ListaInforme lista, Long idIndice) {
		BigDecimal montoConceptos = BigDecimal.ZERO;
		//List<Concepto> conceptos= lista.concepto.stream().filter(c -> c.IdConcepto.equals(idConcepto)).collect(Collectors.toList());
		List<Concepto> conceptos = lista.concepto.stream().filter(c -> c.IdConcepto.equals(idConcepto) && c.IdIndice.equals(idIndice)).collect(Collectors.toList());
		System.out.println(conceptos.toString());
		for (Concepto con: conceptos) {			
			con.MontoCalculado = getItem(con.IdItem,con.IdConceptoGrupo,con.IdConcepto, con.IdIndice, lista);
			con.MontoCalculado = con.MontoCalculado.multiply(con.Factor);
			if (con.Operacion.equals(_c.SIGNO_MENOS)) {
				montoConceptos = montoConceptos.subtract(con.MontoCalculado);
			}
			else {
				montoConceptos = montoConceptos.add(con.MontoCalculado);
			}
		}
		return montoConceptos;
	}
	private BigDecimal getItem (Long idItem, Long IdConceptoGrupo, Long IdConcepto, Long IdIndice, ListaInforme lista) {
		BigDecimal montoItems = BigDecimal.ZERO;
		
		//Item item = lista.item.stream().filter(i -> i.IdItem.equals(idItem)).count() > 0?(Item) lista.item.stream().filter(i -> i.IdItem.equals(idItem)).findAny().get(): null;
		//List<Item> items = lista.item.stream().filter(i -> i.IdItem.equals(idItem)).collect(Collectors.toList());
		List<Item> items = lista.item.stream().filter(c -> c.IdItem.equals(idItem) && c.IdConceptoGrupo.equals(IdConceptoGrupo) && c.IdConcepto.equals(IdConcepto) && c.IdIndice.equals(IdIndice)).collect(Collectors.toList());
		System.out.println(items.toString());
		for (Item it: items) {
			
			//DIGITADO
			//Item itemDig = lista.item.stream().filter(c -> c.IdItem.equals(it.IdItem) && c.IdConceptoGrupo.equals(it.IdConceptoGrupo) && c.IdConcepto.equals(it.IdConcepto)).count() > 0?(Item) lista.item.stream().filter(c -> c.IdItem.equals(it.IdItem) && c.IdConceptoGrupo.equals(it.IdConceptoGrupo) && c.IdConcepto.equals(it.IdConcepto)).findAny().get(): null;
			//ToDo obtener de tabla ILS.InformeResultadoItemDig por IdTipoInforme, IdEmpresa, FechaContable, IdIndice, IdConcepto, IdItem
			if (it != null) {
				it.MontoDigitado = it.MontoCalculado;
			}
			else {
				it.MontoDigitado = BigDecimal.ZERO;
			}
			
			//CONTABLE
			CuentaContable ctaCon = lista.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equals(it.CuentaContable)).count() > 0?(CuentaContable) lista.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equals(it.CuentaContable)).findAny().get(): null;
			
			if (ctaCon != null) {
				System.out.println(ctaCon.toString());
									
				if (it.SaldoDebeHaber.equalsIgnoreCase(_c.TIPOSALDO_DEBE)) {
					//evaluar que sea ><=
					if ((it.Condicion.equals(_c.CONDICION_MAYOR) && !(ctaCon.MontoDebe.compareTo(it.Monto) > 0))
						|| (it.Condicion.equals(_c.CONDICION_MENOR) && !(ctaCon.MontoDebe.compareTo(it.Monto) < 0))
						|| (it.Condicion.equals(_c.CONDICION_IGUAL) && !(ctaCon.MontoDebe.compareTo(it.Monto) == 0))) {
						it.MontoContable = BigDecimal.ZERO;
					}else {
						it.MontoContable = ctaCon.MontoDebe.multiply(it.Factor);
					}
				}
				else if (it.SaldoDebeHaber.equalsIgnoreCase(_c.TIPOSALDO_HABER)) {
					//evaluar que sea ><=
					if ((it.Condicion.equals(_c.CONDICION_MAYOR) && !(ctaCon.MontoHaber.compareTo(it.Monto) > 0))
						|| (it.Condicion.equals(_c.CONDICION_MENOR) && !(ctaCon.MontoHaber.compareTo(it.Monto) < 0))
						|| (it.Condicion.equals(_c.CONDICION_IGUAL) && !(ctaCon.MontoHaber.compareTo(it.Monto) == 0))) {
						it.MontoContable = BigDecimal.ZERO;
					}else {
						it.MontoContable = ctaCon.MontoHaber.multiply(it.Factor);
					}
				}
				else if (it.SaldoDebeHaber.equalsIgnoreCase(_c.TIPOSALDO_SALDO)) {
					//evaluar que sea ><=
					if ((it.Condicion.equals(_c.CONDICION_MAYOR) && !(ctaCon.Saldo.compareTo(it.Monto) > 0))
						|| (it.Condicion.equals(_c.CONDICION_MENOR) && !(ctaCon.Saldo.compareTo(it.Monto) < 0))
						|| (it.Condicion.equals(_c.CONDICION_IGUAL) && !(ctaCon.Saldo.compareTo(it.Monto) == 0))) {
						it.MontoContable = BigDecimal.ZERO;
					}else {
						it.MontoContable = ctaCon.Saldo.multiply(it.Factor);
					}
					
				}
				else {
					it.MontoContable = BigDecimal.ZERO;
				}
			}else {
				it.MontoContable = BigDecimal.ZERO;
			}
				
			//PROGRAMADO
			//only if NombreFuncion is not null or empty
			if (it.NombreFuncion != null) {
				if (it.NombreFuncion.equals(_c.NOMBREFUNCION_DERECHORETROCOMPRA)){
					it.MontoProgramado = lista.montoDerechosRetroCompra.abs();
				}
				else if (it.NombreFuncion.equals(_c.NOMBREFUNCION_DERECHOMONEDAEXTRANJERA)){
					it.MontoProgramado = lista.montoDerechosMonedaExtranjera.abs();
				}
				else if (it.NombreFuncion.equals(_c.NOMBREFUNCION_OBLIGACIONESRETROVENTA)){
					it.MontoProgramado = lista.montoObligacionesRetroVenta.abs();
				}
				else if (it.NombreFuncion.equals(_c.NOMBREFUNCION_SIMULTANEADERECHO)){
					it.MontoProgramado = lista.montoSimultaneasDerechos.abs();
				}
				else if (it.NombreFuncion.equals(_c.NOMBREFUNCION_SIMULTANEAGARANTIA)){
					it.MontoProgramado = lista.montoSimultaneasGarantias.abs();
				}
				else if (it.NombreFuncion.equals(_c.NOMBREFUNCION_SIMULTANEAOBLIGACION)){
					it.MontoProgramado = lista.montoSimultaneasObligaciones.abs();
				}
				else if (it.NombreFuncion.equals(_c.NOMBREFUNCION_CARTERAOBLIGACION)){
					it.MontoProgramado = lista.montoCarteraObligaciones.abs();
				}
				else {
					it.MontoProgramado = BigDecimal.ZERO;
				}
			}else {
				it.MontoProgramado = BigDecimal.ZERO;
			}
			
			//Finalmente evaluar montocalculado
			System.out.println(it.IdTipoItem);
			if (it.IdTipoItem.equalsIgnoreCase(_c.TIPOITEM_CTACONTABLE)) {
				it.MontoCalculado = it.MontoContable;
			}else if (it.IdTipoItem.equalsIgnoreCase(_c.TIPOITEM_DIGITADO)) {
				it.MontoCalculado = it.MontoDigitado;
			}else if (it.IdTipoItem.equalsIgnoreCase(_c.TIPOITEM_PROGRAMADO)) {
				it.MontoCalculado = it.MontoProgramado;
			}
			System.out.println("it.MontoCalculado ::::" +it.MontoCalculado.toString());
			
			if (_c.SIGNO_MENOS.equals(it.Operacion)) {
			    montoItems = montoItems.subtract(it.MontoCalculado);
			} else {
			    montoItems = montoItems.add(it.MontoCalculado);
			}

		}
		
		return montoItems;
	}
	public List<InformeResultado> prepararDataInfResultado (String fecha, String fechaHoraSistema, ListaInforme listaInf) {
		List<InformeResultado> inf = new ArrayList<>();
		for (Informe lis: listaInf.informe) {
			if (inf.stream().filter(c -> c.IdTipoInforme.equals(lis.IdTipoInforme)).count() > 0) {
				continue;
			}
			InformeResultado res = new InformeResultado();
			res.IdTipoInforme	= lis.IdTipoInforme;
			res.FechaInforme	= fecha;
			res.FechaActual		= fechaHoraSistema.split(" ")[0];	
			res.HoraActual		= fechaHoraSistema.split(" ")[1];
			res.IdEstado		= "VIG";
			
			inf.add(res);
		}
		
		return inf;
	}
	public List<InformeResultadoIndice> prepararDataInfResultadoIndice (Connection conn, String fecha, ListaInforme listaInf) throws SQLException {
		List<InformeResultadoIndice> inf = new ArrayList<>();
		Long TotReg = new Long(listaInf.informe.size());
		List<Long> Id = Utils.getSecuencia(conn, "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoIndice", TotReg);
		for (Informe lis: listaInf.informe) {
			/*if (inf.stream().filter(c -> c.IdTipoInforme.equals(lis.IdTipoInforme)).count() > 0) {
				continue;
			}*/
			List<Indice> ind = listaInf.indice.stream().filter(c -> c.IdIndice.equals(lis.IdIndice)).collect(Collectors.toList());
			for (Indice i: ind) {
				if (inf.stream().filter(c -> c.IdIndice.equals(i.IdIndice)).count() > 0) {
					continue;
				}
				InformeResultadoIndice res = new InformeResultadoIndice();
				res.IdResultadoIndice 	= Id.get(listaInf.informe.indexOf(lis));
				res.IdTipoInforme		= lis.IdTipoInforme;
				res.FechaContable		= fecha;
				res.Monto				= lis.MontoCalculado;
				res.IdIndice			= lis.IdIndice;
				res.NombreIndice		= i.Nombre;
				res.Estado				= "VIG";

				inf.add(res);
			}
		}
		
		return inf;
	}
	public List<InformeResultadoConcepto> prepararDataInfResultadoConcepto (Connection conn, String fecha,List<InformeResultadoIndice> InfResIndice, ListaInforme listaInf) throws SQLException {
		List<InformeResultadoConcepto> inf = new ArrayList<>();
		Long TotReg = new Long(listaInf.indice.size());
		List<Long> Id = Utils.getSecuencia(conn, "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoConcepto", TotReg);
		for (Indice lis: listaInf.indice) {
			InformeResultadoIndice infRes = InfResIndice.stream().filter(c -> c.IdIndice.equals(lis.IdIndice)).count() > 0?(InformeResultadoIndice) InfResIndice.stream().filter(c -> c.IdIndice.equals(lis.IdIndice)).findAny().get(): null;
			List<Concepto> con = listaInf.concepto.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdIndice.equals(lis.IdIndice) && c.Posicion.equals(lis.Posicion)).collect(Collectors.toList());
			
			InformeResultadoConcepto res = new InformeResultadoConcepto();
			for (Concepto i: con) {
				/*if (inf.stream().filter(c -> c.IdConcepto.equals(i.IdConcepto) && c.IdConceptoGrupo.equals(i.IdConceptoGrupo)).count() > 0) {
					continue;
				}*/
				res.IdResultadoConcepto	= Id.get(listaInf.indice.indexOf(lis));
				res.IdResultadoIndice	= infRes.IdResultadoIndice;
				res.IdTipoInforme		= infRes.IdTipoInforme;
				res.FechaContable		= fecha;
				res.Monto				= i.MontoCalculado;//validar este campo
				res.NombreConcepto		= i.Nombre;
				res.IdConcepto			= i.IdConcepto;
				//res.IdConceptoGrupo		= i.IdConceptoGrupo;
				res.IdIndice			= i.IdIndice;
				res.Posicion			= lis.Posicion;
				res.Operador			= lis.Operacion;
				res.Factor				= lis.Factor;
				if (_c.SIGNO_MENOS.equals(i.Operacion)) {
				    res.MontoCalculado = res.MontoCalculado.subtract(i.MontoCalculado);
				} else {
				    res.MontoCalculado = res.MontoCalculado.add(i.MontoCalculado);
				}
				res.Estado				= "VIG";
				
			}
			inf.add(res);
		}
		
		return inf;
	}
	public InformeResultadoItems prepararDataInfResultadoItem (Connection conn,String fecha, List<InformeResultadoConcepto> InfResConcepto, ListaInforme listaInf) throws SQLException {
		InformeResultadoItems inf = new InformeResultadoItems();
		List<InformeResultadoItem> infGrupoConcepto = new ArrayList<>();
		List<InformeResultadoItemDetalle> infDetalle = new ArrayList<>();
		Long TotReg = new Long(listaInf.concepto.size());
		Long TotRegDetalle = new Long(listaInf.item.size());
		List<Long> Id = Utils.getSecuencia(conn, "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoItem", TotReg);
		List<Long> IdDetalle = Utils.getSecuencia(conn, "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoItemDetalle", TotRegDetalle);
		
		for (Concepto lis: listaInf.concepto) {
			InformeResultadoConcepto infResCon = InfResConcepto.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdIndice.equals(lis.IdIndice)).count() > 0?(InformeResultadoConcepto) InfResConcepto.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdIndice.equals(lis.IdIndice)).findAny().get(): null;
			List<Item> item = listaInf.item.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo)).collect(Collectors.toList());
			List<Item> itemDet = listaInf.item.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo) && c.IdItem.equals(lis.IdItem) && c.IdIndice.equals(lis.IdIndice)).collect(Collectors.toList());
			if (itemDet.size() == 0) {
				System.out.println(lis.toString());
				continue;
			}
			InformeResultadoItem res = new InformeResultadoItem();
			
			for (Item i: itemDet) {
				InformeResultadoItemDetalle resDetalle = new InformeResultadoItemDetalle();
				CuentaContable cuentaContable = listaInf.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equalsIgnoreCase(i.CuentaContable)).count() > 0?(CuentaContable) listaInf.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equalsIgnoreCase(i.CuentaContable)).findAny().get(): null;
				//info grupo concepto
				res.IdResultadoItem			= Id.get(listaInf.concepto.indexOf(lis));
				res.IdResultadoConcepto		= infResCon.IdResultadoConcepto;
				res.IdTipoInforme			= infResCon.IdTipoInforme;
				res.FechaContable			= fecha;
				res.IdTipoItem				= i.IdTipoItem;
				res.IdItem					= i.IdItem;
				res.IdConcepto				= lis.IdConcepto;
				res.IdConceptoGrupo			= lis.IdConceptoGrupo;
				res.NombreItem				= i.Nombre;
				res.Monto					= i.Monto;
				res.Operacion				= lis.Operacion;//grupo concepto
				res.Factor					= lis.Factor;//grupo concepto
				
				res.MontoDigitado			= (_c.SIGNO_MENOS.equals(i.Operacion)) ? res.MontoDigitado.subtract(i.MontoDigitado) : res.MontoDigitado.add(i.MontoDigitado);
				res.MontoProgramado			= (_c.SIGNO_MENOS.equals(i.Operacion)) ? res.MontoProgramado.subtract(i.MontoProgramado) : res.MontoProgramado.add(i.MontoProgramado);
				res.PosicionIndice			= lis.Posicion;//indice.Posicion;
				if(cuentaContable != null && cuentaContable.Saldo.compareTo(BigDecimal.ZERO)==-1) {
					res.PositivoNegativo = "N";
					BigDecimal montoContableAjustado = i.MontoContable.compareTo(BigDecimal.ZERO) == 1 && i.Factor.compareTo(BigDecimal.ZERO) >= 0 ? i.MontoContable.multiply(BigDecimal.ONE.negate()) : i.MontoContable;
					BigDecimal montoCalculadoAjustado = i.MontoCalculado.compareTo(BigDecimal.ZERO) == 1 && i.Factor.compareTo(BigDecimal.ZERO) >= 0 ? i.MontoCalculado.multiply(BigDecimal.ONE.negate()) : i.MontoCalculado;
					
					res.MontoContable = (_c.SIGNO_MENOS.equals(i.Operacion)) ? res.MontoContable.subtract(montoContableAjustado) : res.MontoContable.add(montoContableAjustado);
					res.MontoCalculado = (_c.SIGNO_MENOS.equals(i.Operacion)) ? res.MontoCalculado.subtract(montoCalculadoAjustado) : res.MontoCalculado.add(montoCalculadoAjustado);

				}else {
					res.PositivoNegativo = "S";
					res.MontoContable			= (_c.SIGNO_MENOS.equals(i.Operacion)) ? res.MontoContable.subtract(i.MontoContable) : res.MontoContable.add(i.MontoContable);
					res.MontoCalculado			= (_c.SIGNO_MENOS.equals(i.Operacion)) ? res.MontoCalculado.subtract(i.MontoCalculado) : res.MontoCalculado.add(i.MontoCalculado);
				}
				
				//info item detalle
				System.out.println("indexOf - " + listaInf.item.indexOf(i));
				System.out.println("Id.get - " + IdDetalle.get(listaInf.item.indexOf(i)));
				resDetalle.IdResultadoItemDetalle	= IdDetalle.get(listaInf.item.indexOf(i));
				resDetalle.IdResultadoItem			= res.IdResultadoItem;
				resDetalle.IdTipoItem				= i.IdTipoItem;
				if (cuentaContable == null && i.IdTipoItem.equals(_c.TIPOITEM_CTACONTABLE)) {
					System.out.println("CUENTA_CONT_NULL:::"+ i.toString());
				}
				resDetalle.Monto					= !i.IdTipoItem.equals(_c.TIPOITEM_CTACONTABLE) || cuentaContable == null || cuentaContable.Saldo == null?BigDecimal.ZERO:cuentaContable.Saldo;//i.Monto;//saldo de la cuenta contable
				resDetalle.IdItem					= i.IdItem;
				resDetalle.IdConceptoGrupo			= i.IdConceptoGrupo;
				resDetalle.IdConcepto				= i.IdConcepto;
				resDetalle.IdIndice				= i.IdIndice == null?0L:i.IdIndice;
				resDetalle.NombreItem				= i.Nombre;
				resDetalle.CuentaContable			= i.CuentaContable;
				resDetalle.SaldoDebeHaber			= i.SaldoDebeHaber;
				resDetalle.Condicion				= i.Condicion;
				resDetalle.MontoCondicion			= i.Monto;
				resDetalle.Operacion				= i.Operacion;
				resDetalle.FactorItem				= i.Factor;
				if(resDetalle.Monto.compareTo(BigDecimal.ZERO)==-1 && resDetalle.FactorItem.compareTo(BigDecimal.ZERO) >= 0) {
			
					resDetalle.MontoCalculado			= i.MontoCalculado.multiply(BigDecimal.ONE.negate());
					resDetalle.MontoContable			= i.MontoContable.multiply(BigDecimal.ONE.negate());
				}else {
				
					resDetalle.MontoCalculado			= i.MontoCalculado;
					resDetalle.MontoContable			= i.MontoContable;
				}
				resDetalle.MontoDigitado			= i.MontoDigitado;
				resDetalle.MontoProgramado			= i.MontoProgramado;
				resDetalle.PosicionIndice			= res.PosicionIndice;
				resDetalle.MayorACero				= i.MayorACero;
				resDetalle.MenorACero				= i.MenorACero;
				resDetalle.PermiteCero				= i.PermiteCero;
				resDetalle.ProcesoFuncion			= i.ProcesoFuncion;
				
				infDetalle.add(resDetalle);
			}
			
			infGrupoConcepto.add(res);
		}
		inf.infResGrupoConcepto = infGrupoConcepto;
		inf.infResItemDet = infDetalle;
		return inf;
	}
	public List<InformeResultadoItemDetalle> prepararDataInfResultadoItemDetalle (Connection conn,List<InformeResultadoItem> InfResItem, ListaInforme listaInf) throws SQLException {
		List<InformeResultadoItemDetalle> inf = new ArrayList<>();
		Long TotReg = new Long(listaInf.item.size());
		List<Long> Id = Utils.getSecuencia(conn, "BSCL.Normativa.BD.Secuencias::ILS.InformeResultadoItemDetalle", TotReg);
		
//		for (Concepto lis: listaInf.concepto) {
//			InformeResultadoItem infResItem = InfResItem.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo)).count() > 0?(InformeResultadoConcepto) InfResItem.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo)).findAny().get(): null;
//			List<Item> item = listaInf.item.stream().filter(c -> c.IdConcepto.equals(lis.IdConcepto) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo)).collect(Collectors.toList());
			
		for (InformeResultadoItem lis: InfResItem) {
			Item itemDetalle = listaInf.item.stream().filter(c -> c.IdItem.equals(lis.IdItem) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo) && c.IdConcepto.equals(lis.IdConcepto)).findAny().get();
			CuentaContable cuentaContable = listaInf.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equalsIgnoreCase(itemDetalle.CuentaContable)).count() > 0?(CuentaContable) listaInf.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equalsIgnoreCase(itemDetalle.CuentaContable)).findAny().get(): null;
			
			if (itemDetalle != null) {
				InformeResultadoItemDetalle res = new InformeResultadoItemDetalle();
				res.IdResultadoItemDetalle	= Id.get(listaInf.item.indexOf(itemDetalle));
				res.IdResultadoItem			= lis.IdResultadoItem;
				res.IdTipoItem				= itemDetalle.IdTipoItem;
				if (cuentaContable == null && itemDetalle.IdTipoItem.equals(_c.TIPOITEM_CTACONTABLE)) {
					System.out.println("CUENTA_CONT_NULL:::"+ itemDetalle.toString());
				}
				res.Monto					= !itemDetalle.IdTipoItem.equals(_c.TIPOITEM_CTACONTABLE) || cuentaContable == null || cuentaContable.Saldo == null?BigDecimal.ZERO:cuentaContable.Saldo;//i.Monto;//saldo de la cuenta contable
				res.IdItem					= itemDetalle.IdItem;
				res.IdConceptoGrupo			= itemDetalle.IdConceptoGrupo;
				res.IdConcepto				= itemDetalle.IdConcepto;
				res.IdIndice				= itemDetalle.IdIndice == null?0L:itemDetalle.IdIndice;
				res.NombreItem				= itemDetalle.Nombre;
				res.CuentaContable			= itemDetalle.CuentaContable;
				res.SaldoDebeHaber			= itemDetalle.SaldoDebeHaber;
				res.Condicion				= itemDetalle.Condicion;
				res.MontoCondicion			= itemDetalle.Monto;
				res.Operacion				= itemDetalle.Operacion;
				res.FactorItem				= itemDetalle.Factor;
				res.MontoCalculado			= itemDetalle.MontoCalculado;
				res.MontoContable			= itemDetalle.MontoContable;
				res.MontoDigitado			= itemDetalle.MontoDigitado;
				res.MontoProgramado			= itemDetalle.MontoProgramado;
				res.PosicionIndice			= lis.PosicionIndice;
				res.MayorACero				= itemDetalle.MayorACero;
				res.MenorACero				= itemDetalle.MenorACero;
				res.PermiteCero				= itemDetalle.PermiteCero;
				res.ProcesoFuncion			= itemDetalle.ProcesoFuncion;
				
				inf.add(res);
			}
		}
		
//			for (Item i: item) {
		/*
			for (Item lis: listaInf.item) {
				CuentaContable cuentaContable = listaInf.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equalsIgnoreCase(lis.CuentaContable)).count() > 0?(CuentaContable) listaInf.cuentaContable.stream().filter(c -> c.IdCodigoContabilidad.equalsIgnoreCase(lis.CuentaContable)).findAny().get(): null;
				InformeResultadoItem infResItem = InfResItem.stream().filter(c -> c.IdItem.equals(lis.IdItem) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo) && c.IdConcepto.equals(lis.IdConcepto)).count() > 0?(InformeResultadoItem) InfResItem.stream().filter(c -> c.IdItem.equals(lis.IdItem) && c.IdConceptoGrupo.equals(lis.IdConceptoGrupo) && c.IdConcepto.equals(lis.IdConcepto)).findAny().get(): null;
				if (infResItem != null) {
					InformeResultadoItemDetalle res = new InformeResultadoItemDetalle();
					res.IdResultadoItemDetalle	= Id.get(listaInf.item.indexOf(lis));
					res.IdResultadoItem			= infResItem.IdResultadoItem;
					res.IdTipoItem				= lis.IdTipoItem;
					if (cuentaContable == null && lis.IdTipoItem.equals(_c.TIPOITEM_CTACONTABLE)) {
						System.out.println("CUENTA_CONT_NULL:::"+ lis.toString());
					}
					res.Monto					= !lis.IdTipoItem.equals(_c.TIPOITEM_CTACONTABLE) || cuentaContable == null || cuentaContable.Saldo == null?BigDecimal.ZERO:cuentaContable.Saldo;//i.Monto;//saldo de la cuenta contable
					res.IdItem					= lis.IdItem;
					res.IdConceptoGrupo			= lis.IdConceptoGrupo;
					res.IdConcepto				= lis.IdConcepto;
					res.IdIndice				= lis.IdIndice == null?0L:lis.IdIndice;
					res.NombreItem				= lis.Nombre;
					res.CuentaContable			= lis.CuentaContable;
					res.SaldoDebeHaber			= lis.SaldoDebeHaber;
					res.Condicion				= lis.Condicion;
					res.MontoCondicion			= lis.Monto;
					res.Operacion				= lis.Operacion;
					res.FactorItem				= lis.Factor;
					res.MontoCalculado			= lis.MontoCalculado;
					res.MontoContable			= lis.MontoContable;
					res.MontoDigitado			= lis.MontoDigitado;
					res.MontoProgramado			= lis.MontoProgramado;
					res.PosicionIndice			= infResItem.PosicionIndice;
					res.MayorACero				= lis.MayorACero;
					res.MenorACero				= lis.MenorACero;
					res.PermiteCero				= lis.PermiteCero;
					res.ProcesoFuncion			= lis.ProcesoFuncion;
					
					inf.add(res);
				}
				
			}
			*/
//		}
		
		return inf;
	}
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------	
	public GeneraReporteILS_Salida logica(Connection conn, GeneraReporteILS_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		Long init = System.currentTimeMillis();
		Long id = 0L;;
		GeneraReporteILS_Salida salida = new GeneraReporteILS_Salida();
		HashMap<String, Object> params = new HashMap<>();
		HashMap<String,String> mapMsg = new HashMap<>();
		
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn,entrada.p_IdEmpresa);
		
		List<Informe> informes = new ArrayList<>();
		List<Indice> indices = new ArrayList<>();
		List<Concepto> conceptos = new ArrayList<>();
		List<Item> items = new ArrayList<>();
		List<SaldoOperacion> saldoOperacion = new ArrayList<>();
		ListaInforme listaInforme = new ListaInforme();
		List<InformeResultado> informeResultado = new ArrayList<>();
		List<InformeResultadoIndice> informeResultadoIndice = new ArrayList<>();
		List<InformeResultadoConcepto> informeResultadoConcepto = new ArrayList<>();
		List<InformeResultadoItem> informeResultadoItem = new ArrayList<>();
		List<InformeResultadoItemDetalle> informeResultadoItemDetalle = new ArrayList<>();
		InformeResultadoItems informeResultadoItems = new InformeResultadoItems();
		
		//Validacion
		
		List<FechasInformes> resultadoFechasInformes = new ArrayList<>();
		params = new HashMap<>();
		params.put("p_FechaConsulta", entrada.p_FechaInforme);
		resultadoFechasInformes = _sql.query(conn, SQL_SELECT_FECHAS_REPORTES, params, FechasInformes.class);
			
		Boolean HomologarCuentasContables = true;
		Boolean CoberturaPatrimonial = false;
		Boolean MonedaExtranjera = false;
		
		for(FechasInformes in: resultadoFechasInformes){
			if("HomologarCuentasContables".equals(in.NombreReporte)){
				HomologarCuentasContables = true;
			}
			if("CalcularObligacionesDerechosMonedaExtranjera".equals(in.NombreReporte)){
				MonedaExtranjera = true;
			}
			if("CoberturaPatrimonial".equals(in.NombreReporte)){
				CoberturaPatrimonial = true;
			}
		}
		
		if(HomologarCuentasContables.equals(true) && MonedaExtranjera.equals(true) && CoberturaPatrimonial.equals(true)){
			ConsultarSaldosCuentasContables saldo = new ConsultarSaldosCuentasContables();
			ConsultarSaldosCuentasContables_Entrada saldoIn = new ConsultarSaldosCuentasContables_Entrada();
			saldoIn.p_IdEmpresa = entrada.p_IdEmpresa;
			saldoIn.p_IdModulo = entrada.p_IdModulo;
			saldoIn.p_IdUsuario = entrada.p_IdUsuario;
			saldoIn.p_PresentacionMontos = "$";
			ConsultarSaldosCuentasContables_Salida saldosCuentas = saldo.logica(conn, saldoIn);
			
			listaInforme.cuentaContable.addAll(saldosCuentas.P_OUT_SALDOSCUENTASCONTABLES);
			//Informe
			List<Object> paramListInf = new ArrayList<>();
			for (InformeIn in: entrada.p_Informes) {
				paramListInf.add(in.p_IdTipoInforme);
			}
			params = new HashMap<>();
			params.put("p_IdTipoInforme", paramListInf);
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			informes = _sql.query(conn, SQL_SELECT_INFORME, params, Informe.class);
			listaInforme.informe.addAll(informes);
			//indice
			List<Object> paramListIndice = new ArrayList<Object>(); 
			for (Informe in: informes) {
				paramListIndice.add(in.IdIndice);
			}		
			params = new HashMap<>();
			params.put("p_IdIndice", paramListIndice);
			indices = _sql.query(conn, SQL_SELECT_INDICE, params, Indice.class);
			listaInforme.indice.addAll(indices);
			//concepto
			List<Object> paramListConcepto = new ArrayList<Object>(); 
			for (Indice in: indices) {
				paramListConcepto.add(in.IdConcepto);
			}		
			params = new HashMap<>();
			params.put("p_IdConcepto", paramListConcepto);
			params.put("p_IdIndice", paramListIndice);
			conceptos = _sql.query(conn, SQL_SELECT_CONCEPTO, params, Concepto.class);
			listaInforme.concepto.addAll(conceptos);
			//item + item digitado
			List<Object> paramListItem = new ArrayList<Object>(); 
			for (Concepto in: conceptos) {
				paramListItem.add(in.IdItem);
			}		
			params = new HashMap<>();
			params.put("p_IdItem", paramListItem);
			params.put("p_FechaContable", entrada.p_FechaInforme);
			items = _sql.query(conn, SQL_SELECT_ITEM, params, Item.class);
			listaInforme.item.addAll(items);
			
			//CALCULO DE FUNCIONES
			params = new HashMap<>();
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			params.put("p_FechaConsulta", entrada.p_FechaInforme);

			saldoOperacion = _sql.query(conn, SQL_SELECT_DERECHO_MONEDAEXTRANJERA, params, SaldoOperacion.class);
			listaInforme.montoDerechosMonedaExtranjera 	= saldoOperacion.size() > 0 && saldoOperacion.get(0).Monto != null ? saldoOperacion.get(0).Monto:BigDecimal.ZERO;
			
			saldoOperacion = _sql.query(conn, SQL_SELECT_DERECHO_RETROCOMPRA, params, SaldoOperacion.class);
			listaInforme.montoDerechosRetroCompra 		= saldoOperacion.size() > 0 && saldoOperacion.get(0).Monto != null ? saldoOperacion.get(0).Monto:BigDecimal.ZERO;
			 
			saldoOperacion = _sql.query(conn, SQL_SELECT_OBLIGACION_RETROVENTA, params, SaldoOperacion.class);
			listaInforme.montoObligacionesRetroVenta 	= saldoOperacion.size() > 0 && saldoOperacion.get(0).Monto != null ? saldoOperacion.get(0).Monto:BigDecimal.ZERO;
			 
			saldoOperacion = _sql.query(conn, SQL_SELECT_SIMULTANEA_DERECHOS, params, SaldoOperacion.class);
			listaInforme.montoSimultaneasDerechos	 	= saldoOperacion.size() > 0 && saldoOperacion.get(0).Monto != null ? saldoOperacion.get(0).Monto:BigDecimal.ZERO;
			 
			saldoOperacion = _sql.query(conn, SQL_SELECT_SIMULTANEA_OBLIGACIONES, params, SaldoOperacion.class);
			listaInforme.montoSimultaneasObligaciones 	= saldoOperacion.size() > 0 && saldoOperacion.get(0).Monto != null ? saldoOperacion.get(0).Monto:BigDecimal.ZERO;
			 
			saldoOperacion = _sql.query(conn, SQL_SELECT_SIMULTANEA_GARANTIAS, params, SaldoOperacion.class);
			listaInforme.montoSimultaneasGarantias	 	= saldoOperacion.size() > 0 && saldoOperacion.get(0).Monto != null ? saldoOperacion.get(0).Monto:BigDecimal.ZERO;
			
			DetalleCoberturaILS cob = new DetalleCoberturaILS ();
			List<ObjDetalleCobertura> listaCobertura = cob.calculaCoberturaCategoria(conn, entrada.p_IdEmpresa, entrada.p_FechaInforme);
			listaInforme.montoCarteraObligaciones = listaCobertura.get(listaCobertura.size()-1).MontoCobertura.multiply(new BigDecimal(1000));
			
			
			for (Informe in: listaInforme.informe) {
				System.out.println("IdIndice:::"+in.IdIndice);
				in.MontoCalculado = getIndice(in.IdIndice.longValue(), listaInforme);
				System.out.println("Termino Informe");
			}
			
			
			//comienza el insert del Informe
			//informe resultado
			informeResultado = prepararDataInfResultado(entrada.p_FechaInforme, fechaHoraSistema,listaInforme);
			_sql.batch(conn, SQL_DELETE_INFORME_RESULTADO, informeResultado, InformeResultado.class);				
			_sql.batch(conn, SQL_INSERT_INFORME_RESULTADO, informeResultado, InformeResultado.class);
			
			//informe resultado indice
			informeResultadoIndice = prepararDataInfResultadoIndice(conn,entrada.p_FechaInforme,listaInforme);
			_sql.batch(conn, SQL_DELETE_INFORME_RESULTADO_INDICE, informeResultadoIndice, InformeResultadoIndice.class);
			_sql.batch(conn, SQL_INSERT_INFORME_RESULTADO_INDICE, informeResultadoIndice, InformeResultadoIndice.class);
			
			//informe resultado concepto
			informeResultadoConcepto = prepararDataInfResultadoConcepto(conn, entrada.p_FechaInforme, informeResultadoIndice,listaInforme);
			_sql.batch(conn, SQL_DELETE_INFORME_RESULTADO_CONCEPTO, informeResultadoConcepto, InformeResultadoConcepto.class);
			_sql.batch(conn, SQL_INSERT_INFORME_RESULTADO_CONCEPTO, informeResultadoConcepto, InformeResultadoConcepto.class);
			
			//informe resultado items (grupo detalle + detalle items)
			informeResultadoItems = prepararDataInfResultadoItem(conn, entrada.p_FechaInforme, informeResultadoConcepto,listaInforme);
			
			//informe resultado item
			//informeResultadoItem = prepararDataInfResultadoItem(conn, entrada.p_FechaInforme, informeResultadoConcepto,listaInforme);
			_sql.batch(conn, SQL_DELETE_INFORME_RESULTADO_ITEM, informeResultadoItems.infResGrupoConcepto, InformeResultadoItem.class);
			_sql.batch(conn, SQL_INSERT_INFORME_RESULTADO_ITEM, informeResultadoItems.infResGrupoConcepto, InformeResultadoItem.class);
			
			//informe resultado item detalle
			//informeResultadoItemDetalle = prepararDataInfResultadoItemDetalle(conn, informeResultadoItem,listaInforme);
			_sql.batch(conn, SQL_INSERT_INFORME_RESULTADO_ITEM_DETALLE, informeResultadoItems.infResItemDet, InformeResultadoItemDetalle.class);
			
			mapMsg.clear();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_OK,"UI_REPORTE_ILS_OK", mapMsg, conn, entrada.p_IdEmpresa));
			id++;
			logger.info("Termino del servicio GeneraReporteILS, tiempo:" + (System.currentTimeMillis() - init));
			
		}else {
			
			if(HomologarCuentasContables.equals(false)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_ERROR,"UI_REPORTE_ILS_HOMOLOGACION", mapMsg, conn, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			if(MonedaExtranjera.equals(false)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_ERROR,"UI_REPORTE_ILS_MONEDAEXTRANJERA", mapMsg, conn, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			if(CoberturaPatrimonial.equals(false)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_ERROR,"UI_REPORTE_ILS_COBERTURAPATRIMONIAL", mapMsg, conn, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}	
		}
		
		return salida;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		GeneraReporteILS_Entrada in = gson.fromJson(jin.toString(), GeneraReporteILS_Entrada.class);
		GeneraReporteILS_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":50,\"p_IdModulo\":\"ILS\",\"p_UsaReglasNegocio\":\"N\",\"p_FechaInforme\":\"2023-03-24\",\"p_Informes\":[{\"p_IdTipoInforme\":1}]}";
			
			GeneraReporteILS_Entrada Objin = gson.fromJson(json, GeneraReporteILS_Entrada.class);
			GeneraReporteILS demo = new GeneraReporteILS();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				GeneraReporteILS_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}
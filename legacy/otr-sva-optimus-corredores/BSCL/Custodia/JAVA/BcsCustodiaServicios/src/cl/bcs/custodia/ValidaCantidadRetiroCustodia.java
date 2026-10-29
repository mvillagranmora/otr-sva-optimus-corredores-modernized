package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento;
import cl.bcs.custodia.prt.LiquidarCustodiaPrestamo.ObjIndicadorCliente;
import cl.bcs.custodia.prt.LiquidarCustodiaPrestamo.PrestamoVencer_Proceso;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ValidaCantidadRetiroCustodia implements JavaCall{
	
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO \"BSCL.Custodia.BD::GBL.ValidaCantidadRetiroCustodia_RegistrosAValidar\" (\"IdRegistro\",\"IdCliente\", \"IdPortafolio\", \"IdClasificacionTipoInstrumento\", \"IdInstrumento\", \"FechaOperacion\", \"FolioCartera\", \"IdEmpresa\", \"IdMoneda\", \"IdMovimientoEnTransito\", \"IdBoveda\") VALUES (:IdRegistro, :IdCliente, :IdPortafolio, :IdClasificacionTipoInstrumento, :IdInstrumento, :FechaOperacion, :FolioCartera, :IdEmpresa, :IdMoneda, :IdMovimientoEnTransito, :IdBoveda)";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM \"BSCL.Custodia.BD::GBL.ValidaCantidadRetiroCustodia_RegistrosAValidar\" WHERE 1=1";

	private final String INDICADOR_CUENTA_INVERSION = "GEN_OCCINV";
	private final String INDICADOR_SOBREGIRO_CUENTA_INVERSION = "GEN_SOBCTA";
	private final String CONSULTA_CARTERA_DISPONIBLE_SELECT = loadResourceToString("cl/bcs/custodia/sql/ValidaCantidadRetiroCustodia_ConsultaCarteraDisponible.sql");
	private final String CONSULTA_CARTERA_SELECT = loadResourceToString("cl/bcs/custodia/sql/ValidaCantidadRetiroCustodia_ConsultaCartera.sql");
	private final String CONSULTA_REQUISITO_OPERATIVO_SELECT = loadResourceToString("cl/bcs/custodia/sql/ValidaCantidadRetiroCustodia_ConsultaRequisitoOperativo.sql");
	private final String CONSULTA_INDICADOR_CUENTA_INVERSION_SELECT = loadResourceToString("cl/bcs/custodia/sql/ValidaCantidadRetiroCustodia_ConsultaIndicadorCuentaInversion.sql");
	private final String CONSULTA_SOBREGIRO_PORTAFOLIO_SELECT = loadResourceToString("cl/bcs/custodia/sql/ValidaCantidadRetiroCustodia_ConsultaSobreGiroPortafolio.sql");
	
	private final String SQL_SELECT_INDICADOR_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/LiquidarCustodiaPrestamoEmprestito_IndicadorCliente2.sql");
	
	public static class EntradaValidacionCustodia{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdCanalIngreso;
		
		public List<ObjInstrumento> p_Instrumentos;
		public List<Long> p_MovimientosEnTransito; 
		public boolean p_PorCartera;
		public Long p_FolioCartera;
		public String p_Estado; 
		
		public EntradaValidacionCustodia() {
			this.p_MovimientosEnTransito = new ArrayList<Long>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaValidacionCustodia{
		public int P_OUT_VALIDACIONCAJA;
		public String P_OUT_TIPOCOMPORTAMIENTO;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidacionCustodia(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TIPOCOMPORTAMIENTO = "OK";
			this.P_OUT_VALIDACIONCAJA = 0;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjCartera{
		public BigDecimal CantidadDisponible;
		public Long IdPortafolio;
		public Long IdRegistro;
		public Long FolioCartera;
		
		public ObjCartera(){
			this.CantidadDisponible=BigDecimal.ZERO;
			this.FolioCartera=0L;
		}
		public Long getIdPortafolio() {
		    return IdPortafolio;
		 }
		public Long getIdRegistro() {
		    return IdRegistro;
		 }
		public BigDecimal getSumaCantidadDisponible() {
		    return  CantidadDisponible;
		 }
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RequisitosOperativosRetiroTitulos{
		public String IdCondicionOperativa;
		public String IdTipoEtapa;
		public String IdTipoCanalIngreso;
		public String IdTipoComportamiento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Indicador{
		public String Codigo;
		public String Valor;
		public Long IdRegistro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegistrosAProcesar {
		public Long IdRegistro;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public Long IdInstrumento;
		public Long FolioCartera;
		public String FechaOperacion;
		public Long IdEmpresa;
		public String IdMoneda;
		public Long IdMovimientoEnTransito;
		public BigDecimal CantidadRetiro;
		public String	IdEstado;
		public String DebeCertificar;
		public String IdBoveda;
		public Long getId() {
			return IdPortafolio;
		}
		
		public RegistrosAProcesar() {
			this.DebeCertificar = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaValidacionCustodia in=gson.fromJson(jin.toString() ,EntradaValidacionCustodia.class);
		SalidaValidacionCustodia out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaValidacionCustodia logica (Connection con, EntradaValidacionCustodia entrada) throws Exception{
		
		SQL sql=new SQL();
		SalidaValidacionCustodia salida = new SalidaValidacionCustodia();
		
		Boolean sinCartera = false;
		Boolean ContratoCustodia = false;

		String conceptoNoValidaFlujoCustodia = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUSTODIA", "ConceptoNoValidaFlujoCustodia");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"); 

		/*
		 *  MOVIMIENTOS EN TRANSITO
		 */
		List<RegistrosAProcesar> listRegistros = new ArrayList<>();
		List<RegistrosAProcesar> registrosConCartera = new ArrayList<>();
		
		HashMap<Long, GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosEnTransitoHash = new HashMap<>();
		if(entrada.p_MovimientosEnTransito != null && !entrada.p_MovimientosEnTransito.isEmpty()) {
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, entrada.p_MovimientosEnTransito);
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movimientos : movimientosEnTransito) {
				if(movimientos.IdTipoOperacion.equals("RETTIT")){
					RegistrosAProcesar registros = new RegistrosAProcesar();
					registros.IdRegistro = movimientos.IdMovimientoCarteraEnTransito;
					registros.IdCliente = movimientos.IdCliente;
					registros.IdPortafolio = movimientos.IdPortafolio;
					registros.IdClasificacionTipoInstrumento = movimientos.IdClasificacionTipoInstrumento;
					registros.IdInstrumento = movimientos.IdInstrumento;
					registros.FechaOperacion = movimientos.FechaOperacion.toString();
					registros.IdEmpresa = movimientos.IdEmpresa;
					registros.IdMoneda = movimientos.IdMonedaInstrumento;
					registros.IdMovimientoEnTransito = movimientos.IdMovimientoCarteraEnTransito;
					registros.CantidadRetiro = "IF".equalsIgnoreCase(movimientos.IdClasificacionTipoInstrumento) ? movimientos.ValorRescate : movimientos.Cantidad;
					registros.IdEstado = movimientos.IdEstado;
					registros.IdBoveda = movimientos.IdBoveda;
					if("IF".equalsIgnoreCase(movimientos.IdClasificacionTipoInstrumento) || "RF".equalsIgnoreCase(movimientos.IdClasificacionTipoInstrumento)) {
						if(movimientos.FolioCustodia != null && movimientos.FolioCustodia >0) {
							registros.FolioCartera = movimientos.FolioCustodia;
							entrada.p_PorCartera= true;
						}
					}
					movimientos.Cantidad = "IF".equalsIgnoreCase(movimientos.IdClasificacionTipoInstrumento) ? movimientos.ValorRescate : movimientos.Cantidad;
					listRegistros.add(registros);
					movimientosEnTransitoHash.put(movimientos.IdMovimientoCarteraEnTransito, movimientos);
				}
			}
		} else {
			for(ObjInstrumento instrumento : entrada.p_Instrumentos) {
				if(instrumento.IdTipoOperacion.equals("RETTIT")) {
					RegistrosAProcesar registros = new RegistrosAProcesar();
					registros.IdRegistro = instrumento.IdGenerarMovimientoCustodia;
					registros.IdCliente = instrumento.IdCliente;
					registros.IdPortafolio = instrumento.IdPortafolio;
					registros.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
					registros.IdInstrumento = instrumento.IdInstrumento;
					registros.FechaOperacion = instrumento.FechaOperacion;
					registros.IdEmpresa = entrada.p_IdEmpresa;
					registros.IdMoneda = instrumento.IdMonedaInstrumento;
					registros.FolioCartera = (instrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF" )|| instrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") )? instrumento.FolioCartera : 0L;
					registros.IdMovimientoEnTransito = 0L;
					registros.CantidadRetiro = "IF".equalsIgnoreCase(instrumento.IdClasificacionTipoInstrumento) ? instrumento.ValorRescate : instrumento.Cantidad;
					registros.DebeCertificar = instrumento.DebeCertificar;
					registros.IdBoveda = instrumento.IdBoveda;
					listRegistros.add(registros);
					if (instrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF" )|| instrumento.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) entrada.p_PorCartera= true;
				}
			}
		}
		if (!listRegistros.isEmpty()) {
			sql.batch(con, SQL_INSERT_REGISTROS, listRegistros, RegistrosAProcesar.class);

			/*
			 * VALIDA CARTERAS DISPONIBLES
			 */
			List<ObjCartera> listaCarterasDisponibles = new ArrayList<ObjCartera>();
			if (entrada.p_PorCartera) {
				listaCarterasDisponibles = sql.query(con, CONSULTA_CARTERA_SELECT, new HashMap<>(), ObjCartera.class);
				List<Long> folioDisponibles = new ArrayList<>();
				folioDisponibles = listaCarterasDisponibles.stream().map(c -> c.FolioCartera).collect(Collectors.toList());
				
				for(RegistrosAProcesar registro: listRegistros) {
					if(registro.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")){
							if(folioDisponibles.contains(registro.FolioCartera)==false){
									salida.P_OUT_ERRORESCONTADOR = 1L;
									HashMap<String,String> valores = new HashMap<String,String>(1);
									valores.put("Folio", registro.FolioCartera.toString());
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registro.IdRegistro, MensajeSalida.MENSAJE_ERROR, "RETIRO_NO_ENCONTRO_FOLIO_CARTERA", valores ,con,entrada.p_IdEmpresa));
								}
							
								
					}
				}
				
				
			} else {
				HashMap<String,Object> params = new HashMap<>();
				params.put("FechaSistema", fechaSistema);
				listaCarterasDisponibles = sql.query(con, CONSULTA_CARTERA_DISPONIBLE_SELECT, params, ObjCartera.class);
			}			
			// VALIDACION PARA PRESTAMOS DE CLIENTES EXTERNOS
			
			//Consulta si tiene cartera
			if(listaCarterasDisponibles.isEmpty()) {
				sinCartera = true;
			}
			
			
			//evaluacion de si se posee contrato de custodia
			List<ObjIndicadorCliente> objIndicadorCliente  = new ArrayList<>();
			HashMap<String,Object> param = new HashMap<>();		
			Long idCliente = listRegistros.get(0).IdCliente;
			param.put("IdCliente", idCliente);				
			objIndicadorCliente = sql.query(con, SQL_SELECT_INDICADOR_CLIENTE, param, ObjIndicadorCliente.class);
			
			
			for (ObjIndicadorCliente indicador: objIndicadorCliente) {
				if (idCliente.compareTo(indicador.IdCliente) == 0 ) {
					ContratoCustodia = true;
				}
			}
			 
			if (!listaCarterasDisponibles.isEmpty()) {
				Map<Long, BigDecimal> sumaCarteras = listaCarterasDisponibles.stream().collect(Collectors.groupingBy(ObjCartera::getIdRegistro, Collectors.reducing(BigDecimal.ZERO, ObjCartera::getSumaCantidadDisponible, BigDecimal::add)));
				Map<String, BigDecimal> cantidadRetirada = new HashMap<>();
				for (RegistrosAProcesar registro : listRegistros) {				
				
					if(sumaCarteras.containsKey(registro.IdRegistro)) {
						int cantidadRetiroComparada = entrada.p_MovimientosEnTransito == null || entrada.p_MovimientosEnTransito.isEmpty() ? registro.CantidadRetiro.compareTo(sumaCarteras.get(registro.IdRegistro).subtract(cantidadRetirada.getOrDefault(registro.IdPortafolio.toString() + registro.IdInstrumento.toString() + registro.IdBoveda, BigDecimal.ZERO))) : movimientosEnTransitoHash.get(registro.IdRegistro).Cantidad.compareTo(sumaCarteras.get(registro.IdRegistro).subtract(cantidadRetirada.getOrDefault(registro.IdPortafolio.toString() + registro.IdInstrumento.toString() + registro.IdBoveda, BigDecimal.ZERO)));
						if (cantidadRetiroComparada > 0) {
							salida.P_OUT_ERRORESCONTADOR = 1L;
							salida.P_OUT_MENSAJES.add(new MensajeSalida(registro.IdRegistro, MensajeSalida.MENSAJE_ERROR, "RETIRO_CARTERA", registro.IdRegistro + ": Custodia insuficiente para realizar retiro"));
						}
						else{
							cantidadRetirada.put(registro.IdPortafolio.toString() + registro.IdInstrumento.toString() + registro.IdBoveda, entrada.p_MovimientosEnTransito == null || entrada.p_MovimientosEnTransito.isEmpty() ? registro.CantidadRetiro.add(cantidadRetirada.getOrDefault(registro.IdPortafolio.toString() + registro.IdInstrumento.toString() + registro.IdBoveda, BigDecimal.ZERO)) : movimientosEnTransitoHash.get(registro.IdRegistro).Cantidad.add(cantidadRetirada.getOrDefault(registro.IdPortafolio.toString() + registro.IdInstrumento.toString() + registro.IdBoveda, BigDecimal.ZERO)));
						}						
						registrosConCartera.add(registro);
					}else {
						salida.P_OUT_ERRORESCONTADOR = 1L;
						salida.P_OUT_MENSAJES.add(new MensajeSalida(registro.IdRegistro, MensajeSalida.MENSAJE_ERROR, "RETIRO_SIN_CARTERA", registro.IdRegistro + ": Custodia no contiene instrumento a retirar"));
					}
				}  
			}
			else {
			      if(entrada.p_Instrumentos != null) {
			    	  for(ObjInstrumento instrumento : entrada.p_Instrumentos) {
					    	 if(!conceptoNoValidaFlujoCustodia.contains(instrumento.IdConcepto)) {					    		 
					    	     salida.P_OUT_ERRORESCONTADOR += 1L;
					    	     salida.P_OUT_MENSAJES.add(new MensajeSalida(listRegistros.get(0).IdRegistro, MensajeSalida.MENSAJE_ERROR, "RETIRO_SIN_CARTERA", "Custodia no contiene instrumento a retirar"));
				              }
					     } 	
			      }
			      else
			      {
					for(Long IdMovimientoEnTransito: entrada.p_MovimientosEnTransito) {
						if(!conceptoNoValidaFlujoCustodia.contains(movimientosEnTransitoHash.get(IdMovimientoEnTransito).IdConcepto)) { 
							salida.P_OUT_ERRORESCONTADOR += 1L; 
							salida.P_OUT_MENSAJES.add(new MensajeSalida(IdMovimientoEnTransito, MensajeSalida.MENSAJE_ERROR, "RETIRO_SIN_CARTERA", IdMovimientoEnTransito + ": Custodia no contiene instrumento a retirar")); 
						} 
					}
			      }
			      
		     } 
		}
		/*
		 *  VALIDA CUENTA INVERSION NO SEA NEGATIVA SEGUN REQUIITOS
		 */
		 
		if (!registrosConCartera.isEmpty()) {// si tiene titulos para retirar
			//Busca indicadores de portafolio
			HashMap<Long,Indicador> indicadorCuentaInversion = new HashMap<>();
			HashMap<Long,Indicador> indicadorSobregiro = new HashMap<>();
			List<Indicador> indicadresPortafolio = sql.query(con, CONSULTA_INDICADOR_CUENTA_INVERSION_SELECT, new HashMap<>(), Indicador.class);//Busca indicadores de portafolio
			
			for(Indicador indicadorPortafolio : indicadresPortafolio) {
				if(indicadorPortafolio.Codigo.equals(INDICADOR_CUENTA_INVERSION) && !indicadorCuentaInversion.containsKey(indicadorPortafolio.IdRegistro)){
					indicadorCuentaInversion.put(indicadorPortafolio.IdRegistro, indicadorPortafolio);
				} 
				if(indicadorPortafolio.Codigo.equals(INDICADOR_SOBREGIRO_CUENTA_INVERSION) && !indicadorSobregiro.containsKey(indicadorPortafolio.IdRegistro)) {
					indicadorSobregiro.put(indicadorPortafolio.IdRegistro, indicadorPortafolio);
				}
			}
			//Busca limite de sobregiro del portafolio
			List<Indicador> limitesSobregiroPortafolio = sql.query(con, CONSULTA_SOBREGIRO_PORTAFOLIO_SELECT, new HashMap<>(), Indicador.class);//Busca limite de cuenta inversion
			
			//Busca Requisito Operativo 
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<RequisitosOperativosRetiroTitulos> requisitosOperativos = sql.query(con, CONSULTA_REQUISITO_OPERATIVO_SELECT, params, RequisitosOperativosRetiroTitulos.class);
			HashMap<String,RequisitosOperativosRetiroTitulos> requisitosOperativosHash = new HashMap<>();
			for (RequisitosOperativosRetiroTitulos requisito : requisitosOperativos) {
				requisitosOperativosHash.put(requisito.IdTipoCanalIngreso, requisito);
			}
			
			SQL.exec(con, SQL_DELETE_REGISTROS);
			Long contador = 0L;
			int validacionCaja = 0;
			String codigoMensajeSalida = "";
			String mensajeSalida = "";
			String tipoComportamiento = "";
			//Envia por portafolio a revisar si cuenta inversion es negativa
			for (RegistrosAProcesar registroAProcesar : registrosConCartera) {
				
				if(registroAProcesar.IdClasificacionTipoInstrumento.equals("RV")) {
					
				if (indicadorCuentaInversion.get(registroAProcesar.IdPortafolio).Valor.equals("S")) {// si indicador cuenta invercion encendido
					
					if (!requisitosOperativosHash.isEmpty() && requisitosOperativosHash.get(entrada.p_IdCanalIngreso).IdEstado.equals("ACT")) {// Requisito operativo

						ConsultarSaldoCuentaInversion saldoCuentaInversion = new ConsultarSaldoCuentaInversion();
						EntradaConsultarSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaConsultarSaldoCuentaInversion();
						SalidaConsultarSaldoCuentaInversion salidaCuentaInversion = new SalidaConsultarSaldoCuentaInversion();
						InCuentaInversionConsultar cuentaAConsultar = new InCuentaInversionConsultar();
						entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaSaldoCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
						entradaSaldoCuentaInversion.p_IdModulo = "CTA";
						entradaSaldoCuentaInversion.p_UsaReglasNegocio = "S";
						entradaSaldoCuentaInversion.p_IncluyeOtrosSaldos = "S";
						entradaSaldoCuentaInversion.p_FechaDesde = registroAProcesar.FechaOperacion;
						entradaSaldoCuentaInversion.p_FechaHasta = registroAProcesar.FechaOperacion;
						entradaSaldoCuentaInversion.p_IncluyeProductoApv = "S";
						entradaSaldoCuentaInversion.p_IdCliente = registroAProcesar.IdCliente;
						entradaSaldoCuentaInversion.p_IdPortafolio = registroAProcesar.IdPortafolio;
						

						cuentaAConsultar.IdEjecutivo = null;
						cuentaAConsultar.IdMoneda = registroAProcesar.IdMoneda;
						cuentaAConsultar.IdPortafolio = registroAProcesar.IdPortafolio;
						entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.add(cuentaAConsultar);
						salidaCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);

						if (salidaCuentaInversion.P_OUT_ERRORESCONTADOR == 0L && !salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.isEmpty()) {// si tiene cuenta inversion
							if(registroAProcesar.IdEstado==null) {
								registroAProcesar.IdEstado="";
							} 			
							if (salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.get(0).Saldo.compareTo(BigDecimal.ZERO) < 0 && !registroAProcesar.IdEstado.equals("MAU")) {//la cuenta inversion es negativa
								if (indicadorSobregiro.get(registroAProcesar.IdPortafolio).Valor.equals("N")) {// no tiene indicador sobregiro en cuenta inversion;
									//error	
									validacionCaja = salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.get(0).Saldo.compareTo(BigDecimal.ZERO);
									contador = contador + 1;
									codigoMensajeSalida = "SALDO_NEGATIVO_CUENTA_INVERSION";
									mensajeSalida = "Cliente tiene saldo negativo en Cuenta de Inversion";
									tipoComportamiento = requisitosOperativosHash.get(entrada.p_IdCanalIngreso).IdTipoComportamiento;
									
								}else { //tiene indicador sobregiro en cuenta inversion y tiene check cuenta inversion
									
										if(listRegistros.get(0).DebeCertificar.equals("N")) {
											validacionCaja = salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.get(0).Saldo.compareTo(BigDecimal.ZERO);
											codigoMensajeSalida = "RETIRO_CUSTODIA_OK";
											mensajeSalida = "Retiro Custodia OK";
											tipoComportamiento = "OK";
										}else {
											validacionCaja = salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.get(0).Saldo.compareTo(BigDecimal.ZERO);
											codigoMensajeSalida = "RETIRO_CUSTODIA_OK";
											mensajeSalida = "Pendiente de Certificacion";
											tipoComportamiento = "CERTIFICACION";
										}
																			
									
									if(tipoComportamiento.equals("")) {
										validacionCaja = salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.get(0).Saldo.compareTo(BigDecimal.ZERO);
										contador = contador + 1;
										codigoMensajeSalida = "NO_TIENE_LIMITE_SOBREGIRO";
										mensajeSalida = "No tiene limite Sobregiro";
										tipoComportamiento = requisitosOperativosHash.get(entrada.p_IdCanalIngreso).IdTipoComportamiento;
									}
								}
							} else {//cuenta inversion positiva
								validacionCaja = salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA.get(0).Saldo.compareTo(BigDecimal.ZERO);
								codigoMensajeSalida = "RETIRO_CUSTODIA_OK";
								mensajeSalida = "Retiro Custodia OK";
								tipoComportamiento = "OK";
							}
							
							if(tipoComportamiento.equals("RECHAZO")) {
								if (registroAProcesar.IdMovimientoEnTransito == 0L) {
									salida.P_OUT_ERRORESCONTADOR = contador;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_ERROR, codigoMensajeSalida, mensajeSalida));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}else {//mensaje para movimientos en transito optimus
									salida.P_OUT_ERRORESCONTADOR = contador;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdMovimientoEnTransito, MensajeSalida.MENSAJE_ERROR, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}
								
							}else if(tipoComportamiento.equals("AUTORIZACION")) {
								if (registroAProcesar.IdMovimientoEnTransito == 0L) {
									salida.P_OUT_ERRORESCONTADOR = 0L ;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_AUTORIZACION, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida + ", Operacion requiere autorizacion"));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}else {//mensaje para movimientos en transito agente
									salida.P_OUT_ERRORESCONTADOR = 0L;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdMovimientoEnTransito, MensajeSalida.MENSAJE_AUTORIZACION, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida + ", Operacion requiere autorizacion"));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}
							}else if(tipoComportamiento.equals("OK")) {
								if (registroAProcesar.IdMovimientoEnTransito == 0L) {
									salida.P_OUT_ERRORESCONTADOR = contador;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, mensajeSalida));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}else {//mensaje para movimientos en transito OK
									salida.P_OUT_ERRORESCONTADOR = contador;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdMovimientoEnTransito, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}
							}else if(tipoComportamiento.equals("CERTIFICACION")) {
								if (registroAProcesar.IdMovimientoEnTransito == 0L) {
									salida.P_OUT_ERRORESCONTADOR = contador;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, mensajeSalida));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}else {//mensaje para movimientos en transito OK
									salida.P_OUT_ERRORESCONTADOR = contador;
									salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, mensajeSalida));
									salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
									salida.P_OUT_VALIDACIONCAJA = validacionCaja;
								}
							}
						}
					}else 
						if (registroAProcesar.IdMovimientoEnTransito == 0L) {
							salida.P_OUT_ERRORESCONTADOR = contador;
							salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, mensajeSalida));
							salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
							salida.P_OUT_VALIDACIONCAJA = validacionCaja;
						}else {//mensaje para movimientos en transito OK
							salida.P_OUT_ERRORESCONTADOR = contador;
							salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdMovimientoEnTransito, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida));
							salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
							salida.P_OUT_VALIDACIONCAJA = validacionCaja;
					}
				}else if (registroAProcesar.IdMovimientoEnTransito == 0L) {
					salida.P_OUT_ERRORESCONTADOR = contador;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, mensajeSalida));
					salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
					salida.P_OUT_VALIDACIONCAJA = validacionCaja;
				}else {//mensaje para movimientos en transito OK
					salida.P_OUT_ERRORESCONTADOR = contador;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdMovimientoEnTransito, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida));
					salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
					salida.P_OUT_VALIDACIONCAJA = validacionCaja;
				}	
			}else 
				if (registroAProcesar.IdMovimientoEnTransito == 0L) {
					salida.P_OUT_ERRORESCONTADOR = contador;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, mensajeSalida));
					salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
					salida.P_OUT_VALIDACIONCAJA = validacionCaja;
				}else {//mensaje para movimientos en transito OK
					salida.P_OUT_ERRORESCONTADOR = contador;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdMovimientoEnTransito, MensajeSalida.MENSAJE_OK, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida));
					salida.P_OUT_TIPOCOMPORTAMIENTO = tipoComportamiento;
					salida.P_OUT_VALIDACIONCAJA = validacionCaja;
				}	
				
				if(ContratoCustodia) {
					if(sinCartera) {
						salida.P_OUT_ERRORESCONTADOR = contador;
						salida.P_OUT_MENSAJES.add(new MensajeSalida(registroAProcesar.IdRegistro, MensajeSalida.MENSAJE_AUTORIZACION, codigoMensajeSalida, registroAProcesar.IdPortafolio + ": " + mensajeSalida + ", Operacion requiere autorizacion"));
						salida.P_OUT_TIPOCOMPORTAMIENTO = "AUTORIZACION";
						salida.P_OUT_VALIDACIONCAJA = validacionCaja;
					}
				}
			
			}
		}
		return salida;
	}
	
	public static void main(String[] args) { 
		System.out.println("==COMIENZA==");
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json="{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"CUS\",\"p_IdCliente\":249194,\"p_IdPortafolio\":153964,\"p_IdMonedaInstrumento\":\"USD\",\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdInstrumento\":2,\"p_FechaConsulta\":\"2020-02-25\",\"p_CantidadRetiro\":10,\"p_IdCanalIngreso\":\"OPT\"}";
			
			EntradaValidacionCustodia entrada = gson.fromJson(json, EntradaValidacionCustodia.class);
			ValidaCantidadRetiroCustodia demo = new ValidaCantidadRetiroCustodia();
			
			System.out.println(demo.logica(con, entrada));
			
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("==TERMINA==");
	}
}

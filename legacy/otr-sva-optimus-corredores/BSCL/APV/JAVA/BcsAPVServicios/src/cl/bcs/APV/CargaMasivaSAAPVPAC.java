package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CargaMasivaArchivo.CargaArchivo;
import cl.bcs.APV.GrabarContratoSAAPV.DetalleOrigenTraspaso;
import cl.bcs.APV.GrabarContratoSAAPV.DistribucionInversion;
import cl.bcs.APV.GrabarContratoSAAPV.ParametrosContrato;
import cl.bcs.APV.GrabarContratoSAAPV.PortafoliosModificados;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.InCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.OutCuentaBancaria;
import cl.bcs.tesoreria.IngresarMandatoPAC;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaSAAPVPAC implements JavaCall{
	
	private final Log log = new Log("CargaMasivaSAAPVPAC");
	
	public final String SQL_CONSULTAR_CONTRATOSAAPV = loadResourceToString("cl/bcs/APV/sql/CargaMasivaSAAPVPAC_ConsultarContratoSAAPV.sql");
	public final String SQL_CONSULTAR_CUENTA_BANCARIA = loadResourceToString("cl/bcs/APV/sql/CargaMasivaSAAPVPAC_ConsultarCuentaBancaria.sql");
	
	
	public static class InContratoPAC{
		public String NumeroCuentaBancaria;
		public String CodigoBanco;
		public Long FolioSAAPV;
		public Long FolioPAC;
		public String Receptor;
		public Long NumeroMandato;
		public String Firma;
		public String IdentServicio;
		public Long IdInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CargaMasivaSAAPVPAC_Entrada {
	    public Long							p_IdEmpresa;
	    public Long							p_IdUsuario;
	    public String						p_IdModulo;
	    public String						p_NombreCarga;
	    public String						p_Origen;
	    public Long							p_IdEmpleado;
	    public List<CargaArchivo>			p_Archivo;
	    public List<ParametrosContrato>		p_ParametrosContratos;
		public List<DistribucionInversion> 	p_DistribucionInversion;
		public List<PortafoliosModificados> p_PortafoliosModificados;
		public List<DetalleOrigenTraspaso>  p_DetalleOrigenTraspaso;
		public List<InContratoPAC>			p_InContratoPAC;
	    
	    public CargaMasivaSAAPVPAC_Entrada(){
	    	p_Archivo = new ArrayList<CargaArchivo>();
	    	p_ParametrosContratos = new ArrayList<ParametrosContrato>();
	    	p_DistribucionInversion = new ArrayList<DistribucionInversion>();
	    	p_PortafoliosModificados = new ArrayList<PortafoliosModificados>();	
	    	p_DetalleOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
	    	p_InContratoPAC = new ArrayList<InContratoPAC>();
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CargaMasivaSAAPVPAC_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_IDSCARGAMASIVA;
		public List<ResultadoCargaMasiva> P_OUT_RESULTADOCARGAMASIVA;
		
		public CargaMasivaSAAPVPAC_Salida(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_IDSCARGAMASIVA = new ArrayList<Long>();
			this.P_OUT_RESULTADOCARGAMASIVA = new ArrayList<ResultadoCargaMasiva>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class DatosContratoSAAPV{
		public Long IdInterno;
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
	    public Long IdPersona; 
	    public Long IdEmpleador; 
	    public Long IdCliente;
	    public Long IdPortafolio;
	    public String FolioPAC;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosCuentaBancaria{
		public Long IdInterno;
		public Long IdPersona;
		public Long IdCliente;
		public Long IdBanco;
		public String CodigoBanco;
		public Long IdCuentaBancaria;
	    public String NumeroCuentaBancaria;
	    public String FolioPAC;
	    public String FolioSAAPV;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoCargaMasiva{
	    public Long IdInterno;
	    public Long IdCargaMasiva;
	    public String FolioSAAPVPAC;
	    public Long IdContratoSAAPV;
	    public String FolioSAAPV;
	    public Long IdMandatoPAC;
	    public String FolioPAC;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CargaMasivaSAAPVPAC_Entrada jin = gson.fromJson(in.toString(), CargaMasivaSAAPVPAC_Entrada.class);
		CargaMasivaSAAPVPAC_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
			
	public CargaMasivaSAAPVPAC_Salida logica(Connection con, CargaMasivaSAAPVPAC_Entrada in) throws Exception {
		log.write("INICIO :: APV.CargaMasivaSAAPVPAC");
		SQL _sql = new SQL();
		MensajeSalida msg;
		
		CargaMasivaSAAPVPAC_Salida out = new CargaMasivaSAAPVPAC_Salida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		try {
			if(in.p_Archivo.size() == 0){
				msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = MensajeSalida.MENSAJE_ERROR;
				msg.codigo = "CARGA_CONTRATOS_ERROR";
				msg.descripcion = "Carga archivo sin datos.";
				out.P_OUT_MENSAJES.add(msg);
				return out;
			}
			
			/***** Variables *****/
			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			
			HashMap<String,Object> params = new HashMap<>();
			List<Long> folioContratoSAAPVList = new ArrayList<Long>();
			List<Long> idPersonaList = new ArrayList<Long>();
			List<String> numeroCuentaBancariaList = new ArrayList<String>();
			List<String> codigoBancoList = new ArrayList<String>();
			
			for (InContratoPAC inContratoPAC: in.p_InContratoPAC) {
				if (!folioContratoSAAPVList.contains(inContratoPAC.FolioSAAPV)) {
					folioContratoSAAPVList.add(inContratoPAC.FolioSAAPV);
				}
				if (!numeroCuentaBancariaList.contains(inContratoPAC.NumeroCuentaBancaria)) {
					numeroCuentaBancariaList.add(inContratoPAC.NumeroCuentaBancaria);
				}
				if (!codigoBancoList.contains(inContratoPAC.CodigoBanco)) {
					codigoBancoList.add(inContratoPAC.CodigoBanco);
				}
				
				//Completa resultado salida
				ResultadoCargaMasiva resultadoCargaMasiva = new ResultadoCargaMasiva();
				resultadoCargaMasiva.IdInterno = inContratoPAC.IdInterno;
				resultadoCargaMasiva.FolioSAAPVPAC = inContratoPAC.FolioSAAPV + "_" + inContratoPAC.FolioPAC;
				resultadoCargaMasiva.FolioSAAPV = inContratoPAC.FolioSAAPV.toString();
				resultadoCargaMasiva.FolioPAC = inContratoPAC.FolioPAC.toString();
				out.P_OUT_RESULTADOCARGAMASIVA.add(resultadoCargaMasiva);
			}
			
			
			/***** Validacion Contratos SAAPV *****/
			//Verifica contratos SAAPV que existen pues la Carga SAAPV no lo indica por Folio (indica id de secuencia)
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FolioContratoSAAPV", folioContratoSAAPVList);
			List<DatosContratoSAAPV> contratoSAAPVExisten = _sql.query(con, SQL_CONSULTAR_CONTRATOSAAPV, params, DatosContratoSAAPV.class);
			
			//Para los contratos SAAPV que existen, setea IdInterno que debe remover
			for (DatosContratoSAAPV contratoExiste: contratoSAAPVExisten) {
				for (ResultadoCargaMasiva resultadoCargaMasiva: out.P_OUT_RESULTADOCARGAMASIVA) {
					if (contratoExiste.FolioContratoSAAPV.equals(resultadoCargaMasiva.FolioSAAPV)) {
						contratoExiste.IdInterno = resultadoCargaMasiva.IdInterno;
						
						out.P_OUT_ERRORESCONTADOR++;
						MensajeSalida msj = new MensajeSalida();
						msj.id = Long.parseLong(resultadoCargaMasiva.FolioSAAPV);
						msj.tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
						msj.codigo = "FOLIO_CONTRATO_EXISTE";
						msj.descripcion = "Salida SAAPV: " + "El Folio Contrato SAAPV ya existe.";
						out.P_OUT_MENSAJES.add(msj);
						
						//Completa resultado salida
						resultadoCargaMasiva.IdContratoSAAPV = contratoExiste.IdContratoSAAPV;
					}
				}
			}
			
			//Continua solo con los contratos que no existen
			for (DatosContratoSAAPV contratoExiste: contratoSAAPVExisten) {
				in.p_ParametrosContratos.removeIf(opeIn -> opeIn.IdInterno.longValue() == contratoExiste.IdInterno);
				in.p_DistribucionInversion.removeIf(opeIn -> opeIn.IdInterno.longValue() == contratoExiste.IdInterno);
				in.p_PortafoliosModificados.removeIf(opeIn -> opeIn.IdInterno.longValue() == contratoExiste.IdInterno);
				in.p_DetalleOrigenTraspaso.removeIf(opeIn -> opeIn.IdInterno.longValue() == contratoExiste.IdInterno);
			}
			folioContratoSAAPVList = new ArrayList<Long>();
			for (ParametrosContrato parametrosContrato: in.p_ParametrosContratos) {
				folioContratoSAAPVList.add(Long.parseLong(parametrosContrato.FolioContratoSAAPV));
			}
			
			
			/***** Carga SAAPV *****/
			CargaMasivaArchivo.CargaMasivaArchivo_Salida cargaSaapvOut = new CargaMasivaArchivo.CargaMasivaArchivo_Salida();
			List<DatosContratoSAAPV> contratoSAAPVCreados = new ArrayList<DatosContratoSAAPV>();
			
			if (in.p_ParametrosContratos.size() > 0) {
				log.write("APV.CargaMasivaSAAPVPAC :: Llamada CargaMasivaArchivo SAAPV");
				CargaMasivaArchivo cargaSaapv = new CargaMasivaArchivo();
				CargaMasivaArchivo.CargaMasivaArchivo_Entrada cargaSaapvIn = new CargaMasivaArchivo.CargaMasivaArchivo_Entrada();
				cargaSaapvIn.p_IdEmpresa = in.p_IdEmpresa;
				cargaSaapvIn.p_IdUsuario = in.p_IdUsuario;
				cargaSaapvIn.p_IdModulo = in.p_IdModulo;
				cargaSaapvIn.p_Archivo = in.p_Archivo;
				cargaSaapvIn.p_ParametrosContratos = in.p_ParametrosContratos;
				cargaSaapvIn.p_DistribucionInversion = in.p_DistribucionInversion;
				cargaSaapvIn.p_PortafoliosModificados = in.p_PortafoliosModificados;
				cargaSaapvIn.p_DetalleOrigenTraspaso = in.p_DetalleOrigenTraspaso;
				cargaSaapvOut = cargaSaapv.logica(con, cargaSaapvIn);
				
				out.P_IDSCARGAMASIVA = cargaSaapvOut.P_IDSCARGAMASIVA;
				if (MensajeSalida.getNumeroErrores(cargaSaapvOut.P_OUT_MENSAJES) > 0){
					out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(cargaSaapvOut.P_OUT_MENSAJES);
				}
				
				//Obtiene datos de los Contratos SAAPV creados
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("FolioContratoSAAPV", folioContratoSAAPVList);
				contratoSAAPVCreados = _sql.query(con, SQL_CONSULTAR_CONTRATOSAAPV, params, DatosContratoSAAPV.class);
				
				for (DatosContratoSAAPV contratoCreado: contratoSAAPVCreados) {
					if (!idPersonaList.contains(contratoCreado.IdPersona)) {
						idPersonaList.add(contratoCreado.IdPersona);
					}
					
					//Completa resultado salida
					for (ResultadoCargaMasiva resultadoCargaMasiva: out.P_OUT_RESULTADOCARGAMASIVA) {
						if (contratoCreado.FolioContratoSAAPV.equals(resultadoCargaMasiva.FolioSAAPV)) {
							resultadoCargaMasiva.IdContratoSAAPV = contratoCreado.IdContratoSAAPV;
							
							contratoCreado.IdInterno = resultadoCargaMasiva.IdInterno;
							contratoCreado.FolioPAC = resultadoCargaMasiva.FolioPAC;
						}
					}
					
					//Ajuste de id y descripcion de Mensaje Salida
					for (MensajeSalida mensajeSalida: cargaSaapvOut.P_OUT_MENSAJES) {
						mensajeSalida.descripcion = "Salida SAAPV: " + mensajeSalida.descripcion;
						if (contratoCreado.IdContratoSAAPV.longValue() == mensajeSalida.id.longValue()) {
							mensajeSalida.id = Long.parseLong(contratoCreado.FolioContratoSAAPV);
						}
					}
				}
				
				out.P_OUT_MENSAJES.addAll(cargaSaapvOut.P_OUT_MENSAJES);
				
				//Completa resultado salida
				for (ResultadoCargaMasiva resultadoCargaMasiva: out.P_OUT_RESULTADOCARGAMASIVA) {
					resultadoCargaMasiva.IdCargaMasiva = cargaSaapvOut.P_IDSCARGAMASIVA.get(0);
				}
			}
			
			
			/***** Crear Cuenta Bancaria (SocioNegocio) *****/
			//Verifica si existe Cuenta Bancaria Persona. Si no existe, se crea la Cuenta Bancaria a la Persona
			List<InCuentaBancaria> inCuentaBancariaList = new ArrayList<InCuentaBancaria>();
			
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdPersona", idPersonaList);
			params.put("NumeroCuentaBancaria", numeroCuentaBancariaList);
			params.put("CodigoBanco", codigoBancoList);
			List<DatosCuentaBancaria> datosCuentaBancariaList = _sql.query(con, SQL_CONSULTAR_CUENTA_BANCARIA, params, DatosCuentaBancaria.class);
			
			//Arma las Cuentas Bancarias que debe validar
			List<DatosCuentaBancaria> datosCuentaBancariaValidacion = new ArrayList<DatosCuentaBancaria>();
			for (DatosContratoSAAPV contratoSAAPV: contratoSAAPVCreados) {
				for (InContratoPAC inContratoPAC: in.p_InContratoPAC) {
					if (contratoSAAPV.IdInterno.longValue() == inContratoPAC.IdInterno.longValue()) {
						DatosCuentaBancaria cuentaBancaria = new DatosCuentaBancaria();
						cuentaBancaria.IdInterno = contratoSAAPV.IdInterno;
						cuentaBancaria.IdPersona = contratoSAAPV.IdPersona;
						cuentaBancaria.IdCliente = contratoSAAPV.IdCliente;
						cuentaBancaria.IdBanco = 0L;
						cuentaBancaria.CodigoBanco = inContratoPAC.CodigoBanco;
						cuentaBancaria.IdCuentaBancaria = 0L;
						cuentaBancaria.NumeroCuentaBancaria = inContratoPAC.NumeroCuentaBancaria;
						cuentaBancaria.FolioPAC = contratoSAAPV.FolioPAC;
						cuentaBancaria.FolioSAAPV = contratoSAAPV.FolioContratoSAAPV;
						
						for (DatosCuentaBancaria datosCuentaBancaria: datosCuentaBancariaList) {
							if (contratoSAAPV.IdPersona.longValue() == datosCuentaBancaria.IdPersona.longValue() && inContratoPAC.CodigoBanco.equals(datosCuentaBancaria.CodigoBanco)) {
								datosCuentaBancaria.IdCliente = contratoSAAPV.IdCliente;
								datosCuentaBancaria.IdInterno = contratoSAAPV.IdInterno;
								datosCuentaBancaria.FolioPAC = contratoSAAPV.FolioPAC;
								datosCuentaBancaria.FolioSAAPV = contratoSAAPV.FolioContratoSAAPV;
								
								cuentaBancaria.IdBanco = datosCuentaBancaria.IdBanco;
								cuentaBancaria.IdCuentaBancaria = datosCuentaBancaria.IdCuentaBancaria;
							}
						}
						
						datosCuentaBancariaValidacion.add(cuentaBancaria);
					}
				}
			}
			
			//Verifica las Cuentas Bancarias con error y las que debe crear
			for (DatosCuentaBancaria datosCuentaBancaria: datosCuentaBancariaValidacion) {
				//Valida Codigo Banco existe. Si no existe, registro queda marcado como ERROR
				if (datosCuentaBancaria.IdBanco.longValue() == 0) {
					out.P_OUT_ERRORESCONTADOR++;
					MensajeSalida msj = new MensajeSalida(Long.parseLong(datosCuentaBancaria.FolioSAAPV),MensajeSalida.MENSAJE_ERROR,"SIN_CONFIGURAR_INFORMACION_BANCO",new HashMap<String,String>(),con,in.p_IdEmpresa);
					msj.descripcion = "Cuenta Bancaria: " + msj.descripcion;
					out.P_OUT_MENSAJES.add(msj);
					break;
				}
				
				//Datos de la Cuenta Bancaria a crear
				if (datosCuentaBancaria.IdCuentaBancaria.longValue() == 0) {
					InCuentaBancaria inCuentaBancaria = new InCuentaBancaria();
					inCuentaBancaria.IdInterno = Long.parseLong(datosCuentaBancaria.FolioPAC);
					inCuentaBancaria.IdBanco = datosCuentaBancaria.IdBanco;
					inCuentaBancaria.IdTipoCuentaBancaria = "CUCO";//valor por definicion
					inCuentaBancaria.IdMoneda = monedaSistema;//valor por difinicion
					inCuentaBancaria.NumeroCuentaBancaria = datosCuentaBancaria.NumeroCuentaBancaria;
					inCuentaBancaria.IdPersona = datosCuentaBancaria.IdPersona;
					inCuentaBancaria.IdEstado = "ACT";
					inCuentaBancaria.IdPlaza = 0L;
					inCuentaBancaria.CrearPreferenciaPago = "N";
					inCuentaBancaria.IdCliente = datosCuentaBancaria.IdCliente;
					inCuentaBancaria.EsPac = "N";
					inCuentaBancaria.IdTipoIdentificadorCuenta = "";
					inCuentaBancaria.IdentificadorCuenta = "0";
					inCuentaBancaria.IdPersonaRelacionada = 0L;
					inCuentaBancaria.CodigoCuentaInterbancario = "";
					inCuentaBancaria.CodigoAba = "";
					inCuentaBancaria.OtroCodigoBancoExtranjero = "";
					inCuentaBancaria.CodigoIban = "";
					inCuentaBancaria.OtroCodigoCuentaExtranjero = "";
					inCuentaBancariaList.add(inCuentaBancaria);
				}
			}
			
			if (inCuentaBancariaList.size() > 0) {
				log.write("APV.CargaMasivaSAAPVPAC :: Llamada CrearCuentaBancaria");
				CrearPersonaCuentaBancaria crearCuentaBancaria = new CrearPersonaCuentaBancaria();
				EntradaCrearPersonaCuentaBancaria crearCuentaBancariaEntrada = new CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria();
				crearCuentaBancariaEntrada.p_IdEmpresa = in.p_IdEmpresa;
				crearCuentaBancariaEntrada.p_IdUsuario = in.p_IdUsuario;
				crearCuentaBancariaEntrada.p_IdModulo = "SOC";
				crearCuentaBancariaEntrada.p_UsaReglasNegocio = "S";
				crearCuentaBancariaEntrada.p_InCuentaBancaria = inCuentaBancariaList;
				CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria crearCuentaBancariaSalida = crearCuentaBancaria.logica(con, crearCuentaBancariaEntrada);
				
				for (DatosCuentaBancaria datosCuentaBancaria: datosCuentaBancariaValidacion) {
					for (OutCuentaBancaria outCuentaBancaria : crearCuentaBancariaSalida.P_OUT_CUENTABANCARIA) {
						if (datosCuentaBancaria.FolioPAC.equals(outCuentaBancaria.IdInterno.toString())) {
							datosCuentaBancaria.IdCuentaBancaria = outCuentaBancaria.IdCuentaBancaria;
						}
					}
				}
				
				for (MensajeSalida mensaje: crearCuentaBancariaSalida.P_OUT_MENSAJES) {
					mensaje.descripcion = "Crear Cuenta Bancaria: " + mensaje.descripcion;
					out.P_OUT_MENSAJES.add(mensaje);
				}
			}
			
			HashMap<String, Object> datosCuentaBancariaHash = Utils.List2Hash(datosCuentaBancariaValidacion, "FolioSAAPV");
			
			
			/***** Ingreso Mandato PAC (Tesoreria) *****/
			List<IngresarMandatoPAC.InIngresoPAC> inIngresoPACList = new ArrayList<IngresarMandatoPAC.InIngresoPAC>();
			for (DatosContratoSAAPV contratoSAAPV: contratoSAAPVCreados) {
				for (InContratoPAC inContratoPAC: in.p_InContratoPAC) {
					if (contratoSAAPV.FolioContratoSAAPV.equals(inContratoPAC.FolioSAAPV.toString())) {
						IngresarMandatoPAC.InIngresoPAC inIngresoPAC = new IngresarMandatoPAC.InIngresoPAC();
						inIngresoPAC.IdCliente = contratoSAAPV.IdCliente;
						inIngresoPAC.IdPortafolio = contratoSAAPV.IdPortafolio;
						inIngresoPAC.FolioCSC = "0";
						inIngresoPAC.CuentaCorriente = inContratoPAC.NumeroCuentaBancaria;
						inIngresoPAC.FolioSAAPV = contratoSAAPV.FolioContratoSAAPV;
						inIngresoPAC.FolioPACMandatoAgrupador = 1L;
						inIngresoPAC.Receptor = inContratoPAC.Receptor;
						inIngresoPAC.Fecha = Fecha.toDate(in.p_Archivo.get(0).FechaCarga);
						inIngresoPAC.NumeroMandato = inContratoPAC.NumeroMandato;
						inIngresoPAC.Firma = inContratoPAC.Firma;
						inIngresoPAC.IdentServicio = inContratoPAC.IdentServicio;
						inIngresoPAC.IdEmpresa = in.p_IdEmpresa;
						inIngresoPAC.IdCuentaBancaria = datosCuentaBancariaHash.get(inContratoPAC.FolioSAAPV.toString()) == null ? 0L : ((DatosCuentaBancaria) datosCuentaBancariaHash.get(inContratoPAC.FolioSAAPV.toString())).IdCuentaBancaria;
						inIngresoPAC.Canal = "OPTIMUS";
						inIngresoPAC.TipoPago = "N";//como es carga, se envia N
						inIngresoPACList.add(inIngresoPAC);
					}
				}
			}
			
			if (inIngresoPACList.size() > 0) {
				log.write("APV.CargaMasivaSAAPVPAC :: Llamada IngresarMandatoPAC");
				IngresarMandatoPAC ingresarMandatoPac = new IngresarMandatoPAC();
				IngresarMandatoPAC.EntradaIngresoMandatoPAC entradaIngresoMandatoPAC = new IngresarMandatoPAC.EntradaIngresoMandatoPAC();
				entradaIngresoMandatoPAC.p_IdEmpresa = in.p_IdEmpresa;
				entradaIngresoMandatoPAC.p_IdUsuario = in.p_IdUsuario;
				entradaIngresoMandatoPAC.p_IdModulo = "TES";
				entradaIngresoMandatoPAC.p_SimularOperacion = "N";
				entradaIngresoMandatoPAC.p_UsaReglasNegocio = "S";
				entradaIngresoMandatoPAC.p_IngresoPAC = inIngresoPACList;
				IngresarMandatoPAC.SalidaCrearIngresoPAC salidaCrearIngresoPAC = ingresarMandatoPac.logica(con, entradaIngresoMandatoPAC);
				
				//Completa resultado salida
				for (IngresarMandatoPAC.ResultadoIngresoPAC resultadoPAC: salidaCrearIngresoPAC.P_OUT_RESULTADO) {
					for (ResultadoCargaMasiva resultadoCargaMasiva: out.P_OUT_RESULTADOCARGAMASIVA) {
						resultadoCargaMasiva.IdMandatoPAC = resultadoPAC.IdMandatoPAC;
					}
				}
				
				out.P_OUT_MENSAJES.addAll(salidaCrearIngresoPAC.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(salidaCrearIngresoPAC.P_OUT_MENSAJES) > 0){
					out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(salidaCrearIngresoPAC.P_OUT_MENSAJES);
				}
			}
		}
		catch(Exception error){
			msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = MensajeSalida.MENSAJE_ERROR;
			msg.codigo = "CARGA_CONTRATOS_ERROR";
			msg.descripcion = error.getMessage();
			out.P_OUT_MENSAJES.add(msg);
			out.P_OUT_ERRORESCONTADOR = (long)out.P_OUT_MENSAJES.size();
			
			log.write(error);
			throw error;
		}
		
		return out;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"APV\",\"p_NombreCarga\":\"ContratoSAAPVContratoPAC.txt\",\"p_Origen\":\"PAC\",\"p_IdEmpleado\":7905,\"p_Archivo\":[{\"IdEmpresa\":1,\"IdCargaMasiva\":0,\"NombreCargaMasiva\":\"ContratoSAAPVContratoPAC.txt\",\"CantidadRegistros\":3,\"IdUsuario\":33,\"IdTipoCarga\":1,\"Descripcion\":\"Carga SAAPV+PAC\",\"FechaCarga\":\"2018-10-12\",\"IdEstado\":\"VIG\"}],\"p_ParametrosContratos\":[{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"100007\",\"IdPlan\":1,\"NombrePlan\":\"A. ABIERTO\",\"FechaSuscripcion\":\"2018-03-04\",\"FechaCreacion\":\"2018-02-16\",\"FechaAprobacion\":\"1900-01-01\",\"FechaNotificacion\":\"1900-01-01\",\"FechaRecepcion\":\"1900-01-01\",\"Folio\":\"0\",\"IdPersona\":null,\"IdEmpleador\":null,\"IdCliente\":null,\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"PRUEBA CLIENTE 100007\",\"ApellidoPaternoCliente\":\"TODOS\",\"ApellidoMaternoCliente\":\"LOS DATOS\",\"DocumentoIdentidadCliente\":\"17012355-7\",\"FechaNacimientoCliente\":\"2017-10-05\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":97,\"IdComunaCliente\":97,\"DireccionCliente\":\"MONEDA\",\"NumeroDireccionCliente\":\"365\",\"IdTipoCargoPersona\":1,\"TelefonoFijo\":\"56998877665\",\"TelefonoMovil\":\"56998877665\",\"TelefonoLaboral\":\"56998874000\",\"EmailCliente\":\"CARGA3@GMAIL.COM\",\"RazonSocialEmpleador\":\"CUROS CHANCHO\",\"DocumentoIdentidadEmpleador\":\"15127956-2\",\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"NATANIEL COX\",\"NumeroDireccionEmpleador\":\"68\",\"IdCiudadEmpleador\":97,\"IdComunaEmpleador\":97,\"TelefonoEmpleador\":\"56998877664\",\"NombreEncargado\":\"MARTIN MACHMAR\",\"EmailEncargado\":\"MARTIN.MACHMAR@GAT-BLAC.COM\",\"TelefonoEncargado\":\"56998877663\",\"IdMedioRecaudacionTrabajador\":1,\"IdEntidadRecaudadora\":1,\"CodigoPrevision\":1003,\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":2,\"DiaDescuento\":5,\"Institucion\":\"0\",\"CodigoTipoSolicitud\":\"\",\"TipoMovimiento\":\"\",\"RazonRetiro\":\"\",\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"IdPersonaOrigen\":0,\"TipoDeAhorro\":\"0\",\"FondoDeOrigen\":\"0\",\"TipoTraspaso\":\"0\",\"MontoTraspaso\":194,\"UFTraspaso\":470375,\"PorcentajeTraspaso\":0,\"Descripcion\":\"3001\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":\"11399\",\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\",\"IdInterno\":0},{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"100008\",\"IdPlan\":1,\"NombrePlan\":\"A. ABIERTO\",\"FechaSuscripcion\":\"2018-03-04\",\"FechaCreacion\":\"2018-02-16\",\"FechaAprobacion\":\"1900-01-01\",\"FechaNotificacion\":\"1900-01-01\",\"FechaRecepcion\":\"1900-01-01\",\"Folio\":\"0\",\"IdPersona\":null,\"IdEmpleador\":null,\"IdCliente\":null,\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"PRUEBA CLIENTE 100008\",\"ApellidoPaternoCliente\":\"TODOS\",\"ApellidoMaternoCliente\":\"LOS DATOS\",\"DocumentoIdentidadCliente\":\"17012355-7\",\"FechaNacimientoCliente\":\"2017-10-05\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":97,\"IdComunaCliente\":97,\"DireccionCliente\":\"MONEDA\",\"NumeroDireccionCliente\":\"365\",\"IdTipoCargoPersona\":1,\"TelefonoFijo\":\"56998877665\",\"TelefonoMovil\":\"56998877665\",\"TelefonoLaboral\":\"56998874000\",\"EmailCliente\":\"CARGA3@GMAIL.COM\",\"RazonSocialEmpleador\":\"CUROS CHANCHO\",\"DocumentoIdentidadEmpleador\":\"15127956-2\",\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"NATANIEL COX\",\"NumeroDireccionEmpleador\":\"68\",\"IdCiudadEmpleador\":97,\"IdComunaEmpleador\":97,\"TelefonoEmpleador\":\"56998877664\",\"NombreEncargado\":\"MARTIN MACHMAR\",\"EmailEncargado\":\"MARTIN.MACHMAR@GAT-BLAC.COM\",\"TelefonoEncargado\":\"56998877663\",\"IdMedioRecaudacionTrabajador\":1,\"IdEntidadRecaudadora\":1,\"CodigoPrevision\":1003,\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":2,\"DiaDescuento\":5,\"Institucion\":\"0\",\"CodigoTipoSolicitud\":\"\",\"TipoMovimiento\":\"\",\"RazonRetiro\":\"\",\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"IdPersonaOrigen\":0,\"TipoDeAhorro\":\"0\",\"FondoDeOrigen\":\"0\",\"TipoTraspaso\":\"0\",\"MontoTraspaso\":194,\"UFTraspaso\":470375,\"PorcentajeTraspaso\":0,\"Descripcion\":\"3001\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":\"11399\",\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\",\"IdInterno\":1},{\"IdContratoSAAPV\":0,\"IdEmpresa\":1,\"FolioContratoSAAPV\":\"100009\",\"IdPlan\":1,\"NombrePlan\":\"A. ABIERTO\",\"FechaSuscripcion\":\"2018-03-04\",\"FechaCreacion\":\"2018-02-16\",\"FechaAprobacion\":\"1900-01-01\",\"FechaNotificacion\":\"1900-01-01\",\"FechaRecepcion\":\"1900-01-01\",\"Folio\":\"0\",\"IdPersona\":null,\"IdEmpleador\":null,\"IdCliente\":null,\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"IdEnrolamiento\":0,\"IdPortafolio\":0,\"NombreCliente\":\"PRUEBA CLIENTE 100009\",\"ApellidoPaternoCliente\":\"TODOS\",\"ApellidoMaternoCliente\":\"LOS DATOS\",\"DocumentoIdentidadCliente\":\"17012355-7\",\"FechaNacimientoCliente\":\"2017-10-05\",\"EstadoCivil\":\"CA\",\"IdTipoGenero\":\"F\",\"IdPaisResidenciaCliente\":44,\"IdCiudadCliente\":97,\"IdComunaCliente\":97,\"DireccionCliente\":\"MONEDA\",\"NumeroDireccionCliente\":\"365\",\"IdTipoCargoPersona\":1,\"TelefonoFijo\":\"56998877665\",\"TelefonoMovil\":\"56998877665\",\"TelefonoLaboral\":\"56998874000\",\"EmailCliente\":\"CARGA3@GMAIL.COM\",\"RazonSocialEmpleador\":\"CUROS CHANCHO\",\"DocumentoIdentidadEmpleador\":\"15127956-2\",\"IdPaisEmpleador\":44,\"DireccionComercialEmpleador\":\"NATANIEL COX\",\"NumeroDireccionEmpleador\":\"68\",\"IdCiudadEmpleador\":97,\"IdComunaEmpleador\":97,\"TelefonoEmpleador\":\"56998877664\",\"NombreEncargado\":\"MARTIN MACHMAR\",\"EmailEncargado\":\"MARTIN.MACHMAR@GAT-BLAC.COM\",\"TelefonoEncargado\":\"56998877663\",\"IdMedioRecaudacionTrabajador\":1,\"IdEntidadRecaudadora\":1,\"CodigoPrevision\":1003,\"Pensionado\":\"NO\",\"Trabajador\":\"DEPENDIENTE\",\"OrigenAhorro\":\"\",\"MontoPesos\":0,\"MontoUF\":0,\"PorcentajeRegimenImponible\":0,\"Pago\":\"\",\"PeriodoInicio\":\"\",\"PeriodoTermino\":\"\",\"RegimenTributario\":\"\",\"FormaDePago\":\"DIRECTA\",\"IdMedioRecaudacionFormaPago\":2,\"DiaDescuento\":5,\"Institucion\":\"0\",\"CodigoTipoSolicitud\":\"\",\"TipoMovimiento\":\"\",\"RazonRetiro\":\"\",\"ComisionTransferencia\":0,\"NombreEntidadOrigen\":\"\",\"RutEntidadOrigen\":\"\",\"IdPersonaOrigen\":0,\"TipoDeAhorro\":\"0\",\"FondoDeOrigen\":\"0\",\"TipoTraspaso\":\"0\",\"MontoTraspaso\":194,\"UFTraspaso\":470375,\"PorcentajeTraspaso\":0,\"Descripcion\":\"3001\",\"IdEstadoEnvioNotificacion\":\"NEV\",\"IdEstadoSolicitud\":\"\",\"IdEstado\":\"PDI\",\"IdCargaMasiva\":null,\"IdEmpleado\":\"11399\",\"IdPropositoPortafolio\":\"\",\"IdMatrizAgrupadorAPV\":0,\"CodigoAgrupador\":\"\",\"IdInterno\":2}],\"p_DistribucionInversion\":[{\"FolioContratoSAAPV\":\"100007\",\"IdInstrumento\":\"421060\",\"RazonSocial\":\"ABNA15 &08\",\"Nemotecnico\":\"ABNA15 &08\",\"MontoPesos\":1000,\"Porcentaje\":20,\"IdEstado\":\"VIG\",\"IdInterno\":0},{\"FolioContratoSAAPV\":\"100008\",\"IdInstrumento\":\"421060\",\"RazonSocial\":\"ABNA15 &08\",\"Nemotecnico\":\"ABNA15 &08\",\"MontoPesos\":1000,\"Porcentaje\":20,\"IdEstado\":\"VIG\",\"IdInterno\":1},{\"FolioContratoSAAPV\":\"100009\",\"IdInstrumento\":\"421060\",\"RazonSocial\":\"ABNA15 &08\",\"Nemotecnico\":\"ABNA15 &08\",\"MontoPesos\":1000,\"Porcentaje\":20,\"IdEstado\":\"VIG\",\"IdInterno\":2}],\"p_PortafoliosModificados\":[{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupadorAPV\":2,\"OrigenAhorro\":\"APV\",\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2017-10\",\"MesTermino\":\"2017-10\",\"MontoPesos\":10001,\"MontoUF\":0,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"PorcentajeRegimenImponible\":0,\"Producto\":\"APV Fondos Mutuos\",\"Regimen\":\"APV-B\",\"IdInterno\":0},{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupadorAPV\":2,\"OrigenAhorro\":\"APV\",\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2017-10\",\"MesTermino\":\"2017-10\",\"MontoPesos\":10001,\"MontoUF\":0,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"PorcentajeRegimenImponible\":0,\"Producto\":\"APV Fondos Mutuos\",\"Regimen\":\"APV-B\",\"IdInterno\":1},{\"CodigoAgrupador\":\"GRUPO 1\",\"IdMatrizAgrupadorAPV\":2,\"OrigenAhorro\":\"APV\",\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"MesInicio\":\"2017-10\",\"MesTermino\":\"2017-10\",\"MontoPesos\":10001,\"MontoUF\":0,\"Pago\":\"IND\",\"PagoNombre\":\"INDEFINIDO\",\"PorcentajeRegimenImponible\":0,\"Producto\":\"APV Fondos Mutuos\",\"Regimen\":\"APV-B\",\"IdInterno\":2}],\"p_DetalleOrigenTraspaso\":[],\"p_InContratoPAC\":[{\"NumeroCuentaBancaria\":\"123123\",\"CodigoBanco\":\"111111\",\"FolioSAAPV\":\"100007\",\"FolioPAC\":200007,\"Receptor\":\"MMACHMAR\",\"NumeroMandato\":300007,\"Firma\":\"Si\",\"IdentServicio\":\"0000000001231231111110\",\"IdInterno\":0},{\"NumeroCuentaBancaria\":\"123123\",\"CodigoBanco\":\"110011\",\"FolioSAAPV\":\"100008\",\"FolioPAC\":200008,\"Receptor\":\"MMACHMAR\",\"NumeroMandato\":300008,\"Firma\":\"Si\",\"IdentServicio\":\"0000000001231231111110\",\"IdInterno\":1},{\"NumeroCuentaBancaria\":\"123123\",\"CodigoBanco\":\"1\",\"FolioSAAPV\":\"100009\",\"FolioPAC\":200009,\"Receptor\":\"MMACHMAR\",\"NumeroMandato\":300009,\"Firma\":\"Si\",\"IdentServicio\":\"0000000000000012312318\",\"IdInterno\":2}]}";
			CargaMasivaSAAPVPAC_Entrada Objin = gson.fromJson(json, CargaMasivaSAAPVPAC_Entrada.class);
			
			CargaMasivaSAAPVPAC demo = new CargaMasivaSAAPVPAC();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				CargaMasivaSAAPVPAC_Salida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
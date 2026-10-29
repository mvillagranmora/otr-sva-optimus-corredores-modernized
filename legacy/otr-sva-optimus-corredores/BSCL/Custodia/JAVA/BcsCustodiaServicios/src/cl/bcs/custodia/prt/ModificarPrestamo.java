package cl.bcs.custodia.prt;

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
import com.ibm.icu.text.SimpleDateFormat;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain;
import cl.bcs.socionegocio.bck.ExtenderPlazoPrestamoBlockChain;
import cl.bcs.socionegocio.bck.ExtenderPlazoPrestamoBlockChain.ExtenderPlazoPrestamoBlockChain_Entrada;
import cl.bcs.socionegocio.bck.ExtenderPlazoPrestamoBlockChain.ExtenderPlazoPrestamoBlockChain_Salida;
import cl.bcs.socionegocio.bck.ExtenderPlazoPrestamoBlockChain.DatosPrestamo;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Entrada;
import cl.bcs.socionegocio.bck.ModificaPrestamoBlockChain.ModificaPrestamoBlockChain_Salida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;
import cl.bcs.custodia.prt.ModificarEmprestito.ModificarEmprestito_Entrada;
import cl.bcs.custodia.prt.ModificarEmprestito.ModificarEmprestito_Salida;

public class ModificarPrestamo implements JavaCall
{	
	//-------------------------------------------------
	// LOG
	//-------------------------------------------------
	private final Log log = new Log("CUS.ModificarPrestamo");
	
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_SECUENCIA_IDCARTERA		= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectSecuenciaIdCartera.sql");
	private final String SQL_SELECT_SECUENCIA_IDSUBYACENTE	= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectSecuenciaIdSubyacente.sql");
	private final String SQL_INSERT_SELECT_CARTERA			= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_InsertSelectCartera.sql");
	private final String SQL_INSERT_SELECT_SUBYACENTE		= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_InsertSelectSubyacente.sql");
	private final String SQL_UPDATE_ESTADO_CARTERA			= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_UpdateEstadoCartera.sql");
	private final String SQL_UPDATE_ESTADO_SUBYACENTE		= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_UpdateEstadoSubyacente.sql");
	private final String SQL_SELECT_PRESTAMOS				= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectPrestamos.sql");
	private final String SQL_SELECT_IDSUBYACENTE			= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectIdSubyacente.sql");
	private final String SQL_SELECT_PRESTAMISTAS			= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectPrestamistas.sql");
	private final String SQL_SELECT_CLIENTE_CUSTODIA		= loadResourceToString("cl/bcs/custodia/sql/ModificarPrestamo_SelectClienteCustodia.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");

	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	
	public static class ModificarPrestamo_Entrada
	{
		public Long       		p_IdEmpresa;
		public Long       		p_IdUsuario;
		public String     		p_IdModulo;
		public Long				p_IdCartera;
		public Long				p_IdSubyacente;
		
		public String     		p_FechaVencimiento	= "0001-01-01";
		public BigDecimal		p_PrecioInstrumento	= new BigDecimal(0);
		public BigDecimal 		p_TasaInstrumento	= new BigDecimal(0);
		public BigDecimal 		p_Monto				= new BigDecimal(0);
		public BigDecimal		p_MontoMdaInstr		= new BigDecimal(0);
		public BigDecimal		p_MontoPrima		= new BigDecimal(0);
		public BigDecimal		p_TasaPrima			= new BigDecimal(0);
		public BigDecimal		p_MontoAPlazo		= new BigDecimal(0);
		public Long				p_PlazoEnDias		= 0L;
		
		public BigDecimal		p_InteresDiario 	= new BigDecimal(0);
		public BigDecimal		p_InteresAcumulado	= new BigDecimal(0);
		public String			p_FolioSistemaExterno	= "";

		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------	
	public static class ModificarPrestamo_Salida
	{	
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_NEWIDCARTERA;
		public Long							P_OUT_NEWIDSUBYACENTE;
		public Long							P_OUT_ERRORESCONTADOR = 0L; 
		public String						P_OUT_ERRCODE;
		public String						P_OUT_ERREXT;
		
		public ModificarPrestamo_Salida()
		{
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------

	
	public static class Subyacente
	{
		public Long		IdSubyacente;
		public String	IdEstado;
	}
	
	public static class Prestamista
	{	
		public Long				IdCartera;
		public Long				FolioCartera;
		public Long				FolioCarteraRelacionada;
		public BigDecimal       INGMontoATasaCompraActual;
		public Long				PlazoEnDiasOperacion;
		
		public Long				IdSubyacente;
		public Long				FolioSubyacente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Prestamo
	{	
		public Long				IdCartera;
		public String			IdEstado;		
		public Long				FolioPrestamo;
		public java.sql.Date	FechaPrestamo;
		public String			FolioSistemaExterno;
		public BigDecimal		TasaCompra;
		public String 			IdTipoPrestamo;
		public Long 			IdCliente;
		public java.sql.Date	FechaVencimientoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	public static class NewIdCartera
	{
		public Long NewIdCartera;
	}
	
	public static class NewIdSubyacente
	{
		public Long NewIdSubyacente;
	}
	
	public static class ParametroInsertCartera
	{
		public long			p_out_NewIdCartera;
		public String		p_FechaVencimiento;
		public long			p_PlazoEnDias;
		public long			p_IdUsuario;
		public BigDecimal	p_PrecioInstrumento;
		public BigDecimal	p_TasaPrima;
		public BigDecimal	p_Monto;
		public BigDecimal	p_MontoMdaInstr;
		public BigDecimal	p_MontoPrima;
		public BigDecimal	p_MontoAPlazo;
		public long			p_IdCartera;
		
		public BigDecimal	p_InteresDiario;
		public BigDecimal	p_InteresAcumulado;
		
	}
	
	public static class ParametroInsertSubyacente
	{
		public long			p_out_NewIdSubyacente;
		public BigDecimal	p_PrecioInstrumento;
		public BigDecimal	p_TasaPrima;
		public BigDecimal	p_TasaInstrumento;
		public BigDecimal	p_Monto;
		public BigDecimal	p_MontoMdaInstr;
		public long			p_IdSubyacente;
	}
	
	public static class ClienteCustodia extends BcsPojo {
		public String 	TieneContrato;
		public Long 	IdCliente;
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ModificarPrestamo_Salida logica(Connection con, ModificarPrestamo_Entrada entrada) throws Exception
	{
		SQL sql = new SQL();
		ModificarPrestamo_Salida salida = new ModificarPrestamo_Salida();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		log.write("== INICIO EJECUCION MODIFICAR PRESTAMO ==");
		
		String fechaSistema = Utils.getFechaSistema(con, "BSCL", entrada.p_IdEmpresa);
		String tasaPrestamoIgualTasaEmprestito = "N"; 
				
		try
		{
			tasaPrestamoIgualTasaEmprestito = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "CUSTODIA", "TasaPrestamoIgualTasaEmprestito");	
		}
		catch(Exception ex)
		{
			tasaPrestamoIgualTasaEmprestito = "N";
		}
		
		
		//---------------------------------------------------------------------------------------------
		// VALIDA SI CUMPLE REQUISITOS BASICOS
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) 
		{
			log.write("NO CUMPLE REQUISITOS BASICOS");
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		

		
		//---------------------------------------------------------------------------------------------
		// VALIDAR CARTERA Y SUBYACENTE
 
		HashMap<String,Object> paramPrestamos = new HashMap<String,Object>();
		paramPrestamos.put("IdCartera", 	entrada.p_IdCartera);
		paramPrestamos.put("IdEmpresa",     entrada.p_IdEmpresa);
		 
		List<Prestamo> lstPrestamos = sql.query(con, SQL_SELECT_PRESTAMOS, paramPrestamos, Prestamo.class);
		HashMap<Long, Object> hashPrestamos = Utils.List2Hash(lstPrestamos, "IdCartera");
		Prestamo infoPrestamo = (Prestamo) hashPrestamos.get(entrada.p_IdCartera);

		HashMap<String,Object> ParamIdSubyacente = new HashMap<String,Object>();
		ParamIdSubyacente.put("p_IdSubyacente", entrada.p_IdSubyacente);
		List<Subyacente> Subyacente = sql.query(con,SQL_SELECT_IDSUBYACENTE, ParamIdSubyacente,Subyacente.class);
		
		/*** INFORMACION CORREDORA ***/
		ConsultaDataCorredoraBlockChain consultaDataCorredora = new ConsultaDataCorredoraBlockChain();
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora = new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
		entradaConsultaDataCorredora.p_IdEmpresa = entrada.p_IdEmpresa;
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(con, entradaConsultaDataCorredora);
		
		if (lstPrestamos.size() == 0 && Subyacente.size() == 0)	
		{
			log.write("CARTERA Y SUBYACENTE NO ESTA VIGENTE");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CARTERA_Y_SUBYACENTE_NO_ESTA_VIGENTE", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
			//---------------------------------------------------------------------------------------------
			//                                     B L O C K C H A I N
			//---------------------------------------------------------------------------------------------
			
			/*** TIPO DOCUMENTO BLOCKCHAIN HABILITADO ***/
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("p_IdTipoDocumento", lstPrestamos.get(0).IdTipoPrestamo.equalsIgnoreCase(ConstantesBCK.PRESTAMO_TIPO_VENTA_CORTA) ? ConstantesBCK.CONTRATOPRESTAMOANEXO42 : ConstantesBCK.CONTRATOPRESTAMOANEXO48);
			List<TipoDocumento> arrTipoDocumento = sql.query(con, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametros, TipoDocumento.class);
			parametros.clear();
			
			if ( arrTipoDocumento.size() > 0 ) {
				ModificaPrestamoBlockChain_Salida objSalida_ModPrestamoBC = new ModificaPrestamoBlockChain_Salida();  
				if( !entrada.p_FolioSistemaExterno.equals( "" ) || !entrada.p_FolioSistemaExterno.equals( "0" ) )  {
					
					if ( dateFormat.parse( entrada.p_FechaVencimiento  ).after( infoPrestamo.FechaVencimientoOperacion ) ) {
						
						ExtenderPlazoPrestamoBlockChain extenderPlazoPrestamoBlockChain 				= new ExtenderPlazoPrestamoBlockChain();
						ExtenderPlazoPrestamoBlockChain_Entrada extenderPlazoPrestamoBlockChainEntrada 	= new ExtenderPlazoPrestamoBlockChain_Entrada();
						ExtenderPlazoPrestamoBlockChain_Salida extenderPlazoPrestamoBlockChainSalida	= new ExtenderPlazoPrestamoBlockChain_Salida();
						DatosPrestamo datosPrestamo 													= new DatosPrestamo();
						
						datosPrestamo.FolioSistemaExterno	= entrada.p_FolioSistemaExterno;
						datosPrestamo.FechaVencimiento		= entrada.p_FechaVencimiento;
						datosPrestamo.Tasa					= entrada.p_TasaPrima.toString();
						
						extenderPlazoPrestamoBlockChainEntrada.p_IdEmpresa		= entrada.p_IdEmpresa;
						extenderPlazoPrestamoBlockChainEntrada.p_IdUsuario		= entrada.p_IdUsuario;
						extenderPlazoPrestamoBlockChainEntrada.p_IdModulo		= entrada.p_IdModulo;
						extenderPlazoPrestamoBlockChainEntrada.p_FechaSolicitud	= fechaSistema;
						extenderPlazoPrestamoBlockChainEntrada.p_DatosPrestamo	= datosPrestamo;
						
						extenderPlazoPrestamoBlockChainSalida	= extenderPlazoPrestamoBlockChain.logica( con, extenderPlazoPrestamoBlockChainEntrada );
						
						salida.P_OUT_MENSAJES.addAll( extenderPlazoPrestamoBlockChainSalida.P_OUT_MENSAJES );
						
					} else {
						
						// verifica si cliente opera con custodia externa
						Boolean clienteOperaCustodiaExterna 				= false;
						List<Long> IdsCliente 								= new ArrayList<Long>();
						HashMap<String,Object> parametrosClienteCustodia 	= new HashMap<String,Object>();
						IdsCliente.add( infoPrestamo.IdCliente );
						parametrosClienteCustodia.put("p_IdCliente", 	IdsCliente);
						List<ClienteCustodia> clienteCustodia = sql.query(con, SQL_SELECT_CLIENTE_CUSTODIA, parametrosClienteCustodia, ClienteCustodia.class);
						if ( clienteCustodia.size() > 0 ) {
							clienteOperaCustodiaExterna = clienteCustodia.get(0).TieneContrato.equalsIgnoreCase(ConstantesBCK.NO) ? true : false;
						}
						
						ModificaPrestamoBlockChain_Entrada objEntrada_ModPrestamoBC = new ModificaPrestamoBlockChain_Entrada();
						ModificaPrestamoBlockChain objModPrestamoBC 				= new ModificaPrestamoBlockChain();
						
						objEntrada_ModPrestamoBC.p_IdEmpresa 			= entrada.p_IdEmpresa;
						objEntrada_ModPrestamoBC.p_tasa 				= entrada.p_TasaPrima.toString();
						objEntrada_ModPrestamoBC.p_origenModificacion	= ConstantesBCK.ANULAR_MPTMO; 
						objEntrada_ModPrestamoBC.p_tipoModificacion 	= ConstantesBCK.ACTION_MODIFY;
						objEntrada_ModPrestamoBC.p_id 					= infoPrestamo.FolioSistemaExterno;
						objEntrada_ModPrestamoBC.p_codCorrOpe  			= salidaConsultaDataCorredora.p_Out_BrokerageID;
						
						if ( infoPrestamo.IdTipoPrestamo.equalsIgnoreCase("PRTADM") || (infoPrestamo.IdTipoPrestamo.equalsIgnoreCase("PRTVTC") && clienteOperaCustodiaExterna) ) {
							objEntrada_ModPrestamoBC.p_FechaVencimiento	= entrada.p_FechaVencimiento;
						}
						objSalida_ModPrestamoBC 						= objModPrestamoBC.logica(con, objEntrada_ModPrestamoBC);
						
						if( objSalida_ModPrestamoBC.p_Out_ContadorError == 0 ) {
							log.write("PRESTAMO BLOCKCHAIN MODIFICADO CON EXITO");
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, objSalida_ModPrestamoBC.p_Out_MensajeError, new HashMap<String, String>(), con, entrada.p_IdEmpresa));
						}
						
					}	
				}
			}
			
					
				//---------------------------------------------------------------------------------------------
				// OBTIENE NEW ID CARTERA
				List<NewIdCartera> NewIdCartera = sql.query(con,SQL_SELECT_SECUENCIA_IDCARTERA, new HashMap<>(),NewIdCartera.class);
				
				if ( NewIdCartera.size() == 0 )
				{
					log.write("NO SE HA GENERADO ID CARTERA");
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_HA_GENERADO_ID_CARTERA", null, con, entrada.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				}
				else
				{
					salida.P_OUT_NEWIDCARTERA = NewIdCartera.get(0).NewIdCartera;
					log.write("DATOS ID CARTERA > " + NewIdCartera.get(0).NewIdCartera);
				}
	
				//---------------------------------------------------------------------------------------------
				// INGRESAR DATOS CARTERA
				List<ParametroInsertCartera> InsertCartera = new ArrayList<ParametroInsertCartera>();
				ParametroInsertCartera ParamCartera = new ParametroInsertCartera();
				ParamCartera.p_out_NewIdCartera = NewIdCartera.get(0).NewIdCartera;
				ParamCartera.p_FechaVencimiento = entrada.p_FechaVencimiento;
				ParamCartera.p_PlazoEnDias = entrada.p_PlazoEnDias;
				ParamCartera.p_IdUsuario = entrada.p_IdUsuario;
				ParamCartera.p_PrecioInstrumento = entrada.p_PrecioInstrumento;
				ParamCartera.p_TasaPrima = entrada.p_TasaPrima;
				ParamCartera.p_Monto = entrada.p_Monto;
				ParamCartera.p_MontoMdaInstr = entrada.p_MontoMdaInstr;
				ParamCartera.p_MontoPrima = entrada.p_MontoPrima;
				ParamCartera.p_MontoAPlazo = entrada.p_MontoAPlazo;
				ParamCartera.p_IdCartera = entrada.p_IdCartera;
				ParamCartera.p_InteresDiario = entrada.p_InteresDiario;
				ParamCartera.p_InteresAcumulado = entrada.p_InteresAcumulado;
				InsertCartera.add(ParamCartera);
				sql.batch(con, SQL_INSERT_SELECT_CARTERA, InsertCartera, ParametroInsertCartera.class);
				
				
				//---------------------------------------------------------------------------------------------
				// OBTIENE NEW ID SUBYACENTE
				List<NewIdSubyacente> NewIdSubyacente = sql.query(con,SQL_SELECT_SECUENCIA_IDSUBYACENTE, new HashMap<>(),NewIdSubyacente.class);
				
				if (NewIdSubyacente.size() == 0)
				{
					log.write("NO SE HA GENERADO ID SUBYACENTE");
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_HA_GENERADO_ID_SUBYACENTE", null, con, entrada.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				}
				else
				{
					salida.P_OUT_NEWIDSUBYACENTE = NewIdSubyacente.get(0).NewIdSubyacente;
					log.write("DATOS ID SUBYACENTE > " + NewIdSubyacente.get(0).NewIdSubyacente);
				}
				
				
				//---------------------------------------------------------------------------------------------
				// INGRESAR DATOS SUBYACENTE
				List<ParametroInsertSubyacente> InsertSubyacente = new ArrayList<ParametroInsertSubyacente>();
				ParametroInsertSubyacente ParamSubyacente = new ParametroInsertSubyacente();
				ParamSubyacente.p_out_NewIdSubyacente = NewIdSubyacente.get(0).NewIdSubyacente;
				ParamSubyacente.p_PrecioInstrumento = entrada.p_PrecioInstrumento;
				ParamSubyacente.p_TasaPrima = entrada.p_TasaPrima;
				ParamSubyacente.p_TasaInstrumento = entrada.p_TasaInstrumento;
				ParamSubyacente.p_Monto = entrada.p_Monto;
				ParamSubyacente.p_MontoMdaInstr = entrada.p_MontoMdaInstr;
				ParamSubyacente.p_IdSubyacente = entrada.p_IdSubyacente;
				InsertSubyacente.add(ParamSubyacente);
				sql.batch(con, SQL_INSERT_SELECT_SUBYACENTE, InsertSubyacente, ParametroInsertSubyacente.class);
				
				//---------------------------------------------------------------------------------------------
				// UPDATE ESTADO CARTERA
				sql.batch(con, SQL_UPDATE_ESTADO_CARTERA, InsertCartera, ParametroInsertCartera.class);
				
				//---------------------------------------------------------------------------------------------
				// UPDATE ESTADO SUBYACENTE
				sql.batch(con, SQL_UPDATE_ESTADO_SUBYACENTE, InsertSubyacente, ParametroInsertSubyacente.class);
				
				
				
				//Al modificar la tasa del prestamo afecta la tasa de los emprestitos asociados (y por ende se recualcula premio, monto a plazo, intereses, etc de los emprestitos)
				if (tasaPrestamoIgualTasaEmprestito.equalsIgnoreCase("S"))
				{
					//---------------------------------------------------------------------------------------------
					// OBTIENE DATOS DE LOS PRESTAMISTAS ASOCIADOS
					
					HashMap<String,Object> paramPresta = new HashMap<String,Object>();
					paramPresta.put("FolioPrestamo", infoPrestamo.FolioPrestamo);
					paramPresta.put("IdEmpresa",     entrada.p_IdEmpresa);				
					List<Prestamista> lstPrestamistas = sql.query(con, SQL_SELECT_PRESTAMISTAS, paramPresta, Prestamista.class);
					
					Date fechaInicial = dateFormat.parse(infoPrestamo.FechaPrestamo.toString());
					Date fechaFinal   = dateFormat.parse(fechaSistema);
					BigDecimal diasTranscurridos = new BigDecimal((fechaFinal.getTime()-fechaInicial.getTime())/86400000);					
					
					
					//---------------------------------------------------------------------------------------------
					// MODIFICACION DE EMPRESTITOS ASOCIADOS (al modificar la tasa del prestamo debe modificar los prestamistas asociados)

					ModificarEmprestito servicioModificarEmprestito = new ModificarEmprestito(); 
					ModificarEmprestito_Entrada entradaModificarEmprestito = new ModificarEmprestito_Entrada();
					
					if (lstPrestamistas.size() > 0)
					{
						
						for (Prestamista prestamista : lstPrestamistas)
						{		
							entradaModificarEmprestito.p_IdEmpresa		= entrada.p_IdEmpresa;
							entradaModificarEmprestito.p_IdUsuario		= entrada.p_IdUsuario;
							entradaModificarEmprestito.p_IdModulo		= entrada.p_IdModulo;
							entradaModificarEmprestito.p_IdCartera		= prestamista.IdCartera;
							entradaModificarEmprestito.p_IdSubyacente 	= prestamista.IdSubyacente;
							
							
							entradaModificarEmprestito.p_TasaPremio			= entrada.p_TasaPrima;
							entradaModificarEmprestito.p_MontoAPlazo		= calcularMontoAPlazo(prestamista.INGMontoATasaCompraActual, entrada.p_TasaPrima, new BigDecimal(prestamista.PlazoEnDiasOperacion) );
							entradaModificarEmprestito.p_MontoPremio		= entradaModificarEmprestito.p_MontoAPlazo.subtract(prestamista.INGMontoATasaCompraActual);
							entradaModificarEmprestito.p_InteresDiario		= calcularInteresDiario(prestamista.INGMontoATasaCompraActual, entrada.p_TasaPrima);
							entradaModificarEmprestito.p_InteresAcumulado	= entradaModificarEmprestito.p_InteresDiario.multiply(diasTranscurridos);
							
						}
						
						ModificarEmprestito_Salida salidaModificarEmprestito = servicioModificarEmprestito.logica(con, entradaModificarEmprestito);
						
						if (MensajeSalida.getNumeroErrores(salidaModificarEmprestito.P_OUT_MENSAJES) > 0) 
						{
							log.write("MODIFICACION DE PRESTAMISTAS ASOCIADOS CON ERROR");
							salida.P_OUT_MENSAJES.addAll(salidaModificarEmprestito.P_OUT_MENSAJES);
							return salida;
						}
					}
					
				} // if (tasaPrestamoAfectaTasaEmprestito.equalsIgnoreCase("S"))
				
			
				//---------------------------------------------------------------------------------------------
				// CALL AGREGAR LOG
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = format.parse(Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa));
				String fechaHoraSistema = format.format(date);
				
				AgregarLog agregarLogService = new AgregarLog();
				AgregarLog.InAgregarLog.Log agregarLog = new AgregarLog.InAgregarLog.Log();
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				
				entradaAgregarLog.p_IdTrabajo = 1L;
				agregarLog.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
				agregarLog.IdTipoLog = "MODIFICACION";
				agregarLog.IdModulo = "CUS";
				agregarLog.IdUsuario = entrada.p_IdUsuario;
				agregarLog.IdEmpresa = entrada.p_IdEmpresa;
				agregarLog.FechaSistemaLog = fechaHoraSistema;
				agregarLog.IdUsuarioAtendido = entrada.p_IdUsuario;
				agregarLog.IdTipoRegistro = "MODIFICARPRESTAMO";
				agregarLog.IdRegistro = NewIdCartera.get(0).NewIdCartera.toString();
				agregarLog.NombreProceso = "(62) PLUGIN_CUS.jar@cl.bcs.custodia.prt.ModificarPrestamo";
				agregarLog.EstadoProceso = "";
				agregarLog.Cantidad = new BigDecimal(0);
				agregarLog.Nemo = "";					
				entradaAgregarLog.p_Log.add(agregarLog);	
				agregarLogService.logica(con, entradaAgregarLog);
			
			
		
		
		log.write("PARAMETROS DE SALIDA : " + salida.toString());
		log.write("== TERMINO MODIFICAR PRESTAMO ==");
		
		return salida;
	}
	
	private BigDecimal calcularMontoAPlazo(BigDecimal monto, BigDecimal porcentajeTasa, BigDecimal diasPlazo ) throws Exception
	{   //montoAPlazo = (monto * ( 1 + (tasa/100) * (plazo /30) ))
		BigDecimal tasa 		= porcentajeTasa.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
		BigDecimal periodos 	= diasPlazo.divide(new BigDecimal(30),20,RoundingMode.HALF_UP);		
		BigDecimal factor 		= BigDecimal.ONE.add(tasa.multiply(periodos)); 						
		BigDecimal montoAPlazo  = monto.multiply(factor);                                  			

		return montoAPlazo;
	}

	private BigDecimal calcularInteresDiario(BigDecimal monto, BigDecimal porcentajeTasa) throws Exception
	{ 	//  monto * ((Tasa/100) * (1/30))
		BigDecimal tasa 		= (porcentajeTasa.divide( new BigDecimal(100)).multiply( new BigDecimal(1).divide(new BigDecimal(30), 20,RoundingMode.HALF_UP) ) );
		BigDecimal interesDiario = tasa.multiply(monto);
		
		return interesDiario;
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception
	{
		Gson gson = new Gson();
		ModificarPrestamo_Entrada in = gson.fromJson(jin.toString(), ModificarPrestamo_Entrada.class);
		ModificarPrestamo_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args) throws Exception
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_IdCartera\":1544538,\"p_IdSubyacente\":7013,\"p_FechaVencimiento\":\"2023-03-14\",\"p_PrecioInstrumento\":3670,\"p_TasaInstrumento\":1.05,\"p_Monto\":2205670,\"p_MontoMdaInstr\":0,\"p_MontoPrima\":3764.3434666669928,\"p_TasaPrima\":1.28,\"p_MontoAPlazo\":2209434.343466667,\"p_PlazoEnDias\":4,\"p_InteresDiario\":942,\"p_InteresAcumulado\":0}";
			ModificarPrestamo_Entrada Objin = gson.fromJson(json, ModificarPrestamo_Entrada.class);
			ModificarPrestamo demo = new ModificarPrestamo();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try
 			{
 				ModificarPrestamo_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 			}
 			catch(Exception er)
 			{
				er.printStackTrace();
				con.rollback();
			}
	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}

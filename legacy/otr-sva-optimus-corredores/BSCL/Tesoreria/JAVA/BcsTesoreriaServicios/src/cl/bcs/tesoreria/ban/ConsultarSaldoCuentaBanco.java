
package cl.bcs.tesoreria.ban;

/**
 * Objetivo : Consulta saldo(disponible, pendiente, contable, proyectado) de las cuentas bancarias de la corredora.
 * Autor    : BCS
 * Fecha    : 14-02-2019
 */
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import org.apache.commons.collections.map.MultiKeyMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarSaldoCuentaBanco implements JavaCall {
	
	// Log
	private final Log log = new Log("TES.ConsultarSaldoCuentaBanco");
	
	
	// Queries
	private final String SQL_INSERT_CuentasAConsultar		= loadResourceToString("cl/bcs/tesoreria/sql/ConsultarSaldoCuentaBanco_InsertCuentasAConsultar.sql");	
	private final String SQL_SELECT_CuentaBanco        		= loadResourceToString("cl/bcs/tesoreria/sql/ConsultarSaldoCuentaBanco_SelectCuentaBanco.sql");
	private final String SQL_INSERT_RangoFechaCuentaBanco   = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco.sql");	
	private final String SQL_SELECT_CalculaSaldoDisponible 	= loadResourceToString("cl/bcs/tesoreria/sql/ConsultarSaldoCuentaBanco_CalculaSaldoDisponible.sql");
	private final String SQL_SELECT_CalculaSaldoPendiente 	= loadResourceToString("cl/bcs/tesoreria/sql/ConsultarSaldoCuentaBanco_CalculaSaldoPendiente.sql");
	private final String SQL_SELECT_CalculaSaldoProyectado  = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarSaldoCuentaBanco_CalculaSaldoProyectado.sql");
	
	// Solo para revisar
	private final String SQL_SELECT_RangoFechaCuentaBanco   = "SELECT \"IdCuentaBanco\", \"FechaSaldo\" FROM BSCL.\"BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco\""; // solo para revisar en debug	
	

	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA ENTRADA
	//-------------------------------------------------------------------------------------------
	
	public static class ConsultarSaldoCuentaBanco_Entrada 
	{	
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String		p_UsaReglasNegocio;
		public String		p_FechaDesde;
		public String		p_FechaHasta;
		
		//SaldosPendientes : Saldo de Movimientos que aun no se han certificado/liquidado
		public String		p_IncluyeSaldosPendientes;     //S|N|null  // Se define por parametro de entrada al servicio. Si no se especifica(es decir vienen en null) se obtienen por parametro de configuracion en BD
		//SaldosContables = Saldos Disponible + Saldos Pendiente
		public String		p_IncluyeSaldosContables;     //S|N|null  // Se define por parametro de entrada al servicio. Si no se especifica(es decir vienen en null) se obtienen por parametro de configuracion en BD
		//Saldos proyectados a T1, T2 yT3
		public String		p_IncluyeSaldosFuturo;     //S|N|null  // Se define por parametro de entrada al servicio. Si no se especifica(es decir vienen en null) se obtienen por parametro de configuracion en BD
		
		
		public List<CuentasAConsultar_Entrada>	p_CuentasAConsultar;
		

		public ConsultarSaldoCuentaBanco_Entrada()
		{
			this.p_CuentasAConsultar 			  = new ArrayList<CuentasAConsultar_Entrada>();
		}		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class CuentasAConsultar_Entrada 
	{
		public Long 	IdCuentaBanco;
		public Long 	IdBanco;
		public String 	IdMoneda;

		public String toString(){
			return Utils.printJson(this, false);
		}
	}	

	

	
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class ConsultarSaldoCuentaBanco_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;		
		public List<SaldoCuentaBanco_Salida>		P_OUT_SALDOCUENTABANCO;    // Tabla con proyección de Saldo
		
		public ConsultarSaldoCuentaBanco_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_SALDOCUENTABANCO		 = new ArrayList<SaldoCuentaBanco_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class SaldoCuentaBanco_Salida 
	{
		public Long    		IdCuentaBanco;			
		public String  		NombreCuentaBanco;
		public Long    		IdBanco;
		public String  		NombreBanco;
		public String  		NumeroCuentaBancaria;
		public String  		IdMoneda;
		public String  		NombreMoneda;
		public String  		FechaSaldo;	    
		
		//Saldo Disponible (T0)): Todo aquello que afecto saldo en fecha consulta
		public BigDecimal   SaldoDisponibleInicial;  
		public BigDecimal   SaldoDisponibleIngreso;
		public BigDecimal   SaldoDisponibleEgreso;
		public BigDecimal   SaldoDisponible;

		//Saldo Pendiente: Todo aquello que no ha afectado saldo aun
		public BigDecimal   SaldoPendienteIngreso;    
		public BigDecimal   SaldoPendienteEgreso;
		public BigDecimal   SaldoPendiente;

		//Saldo Contable: Saldo disponible + Saldo Pendiente
		public BigDecimal   SaldoContableIngreso;
		public BigDecimal   SaldoContableEgreso;
		public BigDecimal   SaldoContable;

		//Saldo Proyectado (> T0) T1, T2, T3
		public String  		FechaT1; 
	    public BigDecimal   SaldoIngresoT1;
	    public BigDecimal   SaldoEgresoT1;
	    public BigDecimal   SaldoT1;
      
	    public String 		FechaT2;
	    public BigDecimal   SaldoIngresoT2;
	    public BigDecimal   SaldoEgresoT2;
	    public BigDecimal   SaldoT2;
      
	    public String 		FechaT3;
        public BigDecimal   SaldoIngresoT3;
        public BigDecimal   SaldoEgresoT3;
        public BigDecimal   SaldoT3; 
		
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	


	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------------------------------------------------
	
	
	public static class CuentaBanco 
	{	
		public Long 	IdCuentaBanco;
		public Long 	IdBanco;
		public String 	NombreBanco;
		public String 	NumeroCuentaBancaria;
		public String 	NombreCuentaBanco;
		public String 	IdMoneda;
		public String 	NombreMoneda;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}	
	
	
	public static class RangoFechaCuentaBanco
	{
		
		public Long 	IdCuentaBanco;
		public String 	FechaSaldo;
		public Integer 	Orden;
		
		public String toString(){
			return Utils.printJson(this, false);
		}		
	}
	

	public static class SaldoDisponible
	{	
		public Long 		IdCuentaBanco;		 		
		public String 		FechaSaldo;
 		public BigDecimal 	SaldoDisponibleInicial;
 		public BigDecimal 	SaldoDisponibleEgreso;
 		public BigDecimal 	SaldoDisponibleIngreso;
 		public BigDecimal 	SaldoDisponible;
		
		public String toString(){
			return Utils.printJson(this, false);
		}		
	}
	
	
	public static class SaldoPendiente
	{	
		public Long 		IdCuentaBanco;		 		
		public String 		FechaSaldo;
 		public BigDecimal 	SaldoPendienteEgreso;
 		public BigDecimal 	SaldoPendienteIngreso;
 		public BigDecimal 	SaldoPendiente;
 		
		public String toString(){
			return Utils.printJson(this, false);
		}		
	}
	
    		
			 		
	public static class SaldoContable
	{	
		public Long 		IdCuentaBanco;		 		
		public String 		FechaSaldo;
 		public BigDecimal 	SaldoContableEgreso;
 		public BigDecimal 	SaldoContableIngreso;
 		public BigDecimal 	SaldoContable;
 		
		public String toString(){
			return Utils.printJson(this, false);
		}		
	}
	

	public static class SaldoProyectado
	{	
		public Long 		IdCuentaBanco;		 		
		public String 		FechaSaldo;
 		public String 		FechaT1;
 		public BigDecimal 	SaldoEgresoT1;
 		public BigDecimal 	SaldoIngresoT1;
 		public BigDecimal 	SaldoT1;	
 		public String 		FechaT2;
 		public BigDecimal 	SaldoEgresoT2;
 		public BigDecimal 	SaldoIngresoT2;		 		 
 		public BigDecimal 	SaldoT2;	
 		public String 		FechaT3;
 		public BigDecimal 	SaldoEgresoT3;
 		public BigDecimal 	SaldoIngresoT3;
 		public BigDecimal 	SaldoT3;
		 
 		
		public String toString(){
			return Utils.printJson(this, false);
		}		
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		ConsultarSaldoCuentaBanco_Entrada in = gson.fromJson(jin.toString(), ConsultarSaldoCuentaBanco_Entrada.class);
		ConsultarSaldoCuentaBanco_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS
	//-------------------------------------------------------------------------------------------	

	/**
	 * Genera un rango de fechas entre las fechas indicadas
	 */
	public List<Date> generarRangoFechas(Date fechaInicio, Date fechaFin) 
	{
	    Calendar c1 = Calendar.getInstance();
	    c1.setTime(fechaInicio);
	    Calendar c2 = Calendar.getInstance();
	    c2.setTime(fechaFin);
	    List<Date> rangoFechas = new ArrayList<Date>();
	    while (!c1.after(c2)) {
	    	rangoFechas.add(c1.getTime());
	        c1.add(Calendar.DAY_OF_MONTH, 1);
	    }
	    return rangoFechas;
	}	

	/**
	 * Suma dias a una fecha
	 */
    public static Date agregarDiasFecha(Date fecha, int dias)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.add(Calendar.DATE, dias);
        return cal.getTime();
    }
 
    
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public ConsultarSaldoCuentaBanco_Salida logica(Connection con, ConsultarSaldoCuentaBanco_Entrada in) throws Exception {
		SQL _sql = new SQL();
		ConsultarSaldoCuentaBanco_Salida out = new ConsultarSaldoCuentaBanco_Salida();
		log.write("== INICIO CONSULTA SALDO CUENTA BANCO ==");		
		log.write("PARAMETROS DE ENTRADA > " + in.toString());
			
		HashMap<String,Object> params = new HashMap<>();
		MultiKeyMap mapSaldoPendiente  = new MultiKeyMap();
		MultiKeyMap mapSaldoContable   = new MultiKeyMap();
		MultiKeyMap mapSaldoProyectado = new MultiKeyMap();
		
		//Parsea fechas a tipo Date
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDesde   = formatter.parse(in.p_FechaDesde);
        Date fechaHasta   = formatter.parse(in.p_FechaHasta);

        
		//---------------------------------------------------------------------------------------------
		//VALIDA REQUISITOS BASICOS
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			log.write("NO CUMPLE REQUISITOS BASICOS");
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		
		//---------------------------------------------------------------------------------------------
		// OBTIENE PARAMETROS DE CONFIGURACION PARA LA CONSULTA 
		// Se defines por parametros de entrada al servicio. Si no se especifica(es decir vienen en null) se obtienen por parametro de configuracion en BD
		
		log.write("OBTIENE PARAMETROS DE CONFIGURACION DE CONSULTA");
		
		String IncluyeSaldosPendientes 	= in.p_IncluyeSaldosPendientes == null ? ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "CONSULTASALDOBANCOINCLUYESALDOSPENDIENTE") : in.p_IncluyeSaldosPendientes;		
		String IncluyeSaldosContables 	= in.p_IncluyeSaldosContables == null  ? ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "CONSULTASALDOBANCOINCLUYESALDOSCONTABLES") : in.p_IncluyeSaldosContables;		
		String IncluyeSaldosFuturos 	= in.p_IncluyeSaldosFuturo == null     ? ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "CONSULTASALDOBANCOINCLUYESALDOSFUTUROS") : in.p_IncluyeSaldosFuturo;
		
		
		log.write("PARAMETROS DE CONFIGURACION > " + "IncluyeSaldosPendientes: " + IncluyeSaldosPendientes + " IncluyeSaldosContables: " + IncluyeSaldosContables + " IncluyeSaldosFuturos: " + IncluyeSaldosFuturos);
		
		
		//---------------------------------------------------------------------------------------------
		// OBTIENE DATOS DE LAS CUENTAS BANCARIAS

		log.write("CONSULTANDO INFORMACION DE CUENTAS BANCARIAS");
		
		//Inserta en temporal datos de cuentas a consultar
		_sql.batch(con, SQL_INSERT_CuentasAConsultar, in.p_CuentasAConsultar, CuentasAConsultar_Entrada.class); 
			
		params.put("IdEmpresa", in.p_IdEmpresa);
		List<CuentaBanco> lstCuentaBanco = _sql.query(con, SQL_SELECT_CuentaBanco, params, CuentaBanco.class);
		
		if (lstCuentaBanco.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTEN_CUENTAS_BANCARIAS",null,con,in.p_IdEmpresa));
			return out;			
		}
		
		log.write("CUENTAS BANCARIAS > " + lstCuentaBanco);
		HashMap<Long,Object> hashCuentaBanco = Utils.List2Hash(lstCuentaBanco, "IdCuentaBanco");   // Transforma la lista en un hash		
 

		
		//---------------------------------------------------------------------------------------------
		// GENERA RANGO DE FECHAS PARA CUENTAS BANCARIAS

		log.write("GENERANDO RANGO DE FECHAS PARA CUENTAS BANCARIAS");
        
        List<Date> lstRangoFechas = generarRangoFechas(fechaDesde, fechaHasta);
        
        /*** Solo para ver y revisar
		for (Date fecha : lstRangoFechas) 
		{
			System.out.println(fecha);
			System.out.println(formatter.format(fecha));	    		
		}
		***/
		 
		List <RangoFechaCuentaBanco> lstRangoFechaCuentaBanco = new ArrayList<RangoFechaCuentaBanco>();
		RangoFechaCuentaBanco objRangoFechaCuentaBanco; 
		
  		for (CuentaBanco CBan : lstCuentaBanco)
  		{
    		for (Date fecha : lstRangoFechas) 
    		{
    			objRangoFechaCuentaBanco = new RangoFechaCuentaBanco();
    			objRangoFechaCuentaBanco.IdCuentaBanco = CBan.IdCuentaBanco;
    			objRangoFechaCuentaBanco.FechaSaldo = formatter.format(fecha);
    			objRangoFechaCuentaBanco.Orden = 0; //acá no se usa, se usa para saldos proyectados
    			lstRangoFechaCuentaBanco.add(objRangoFechaCuentaBanco);
    		}
  		}
  		
		_sql.batch(con, SQL_INSERT_RangoFechaCuentaBanco, lstRangoFechaCuentaBanco, RangoFechaCuentaBanco.class);
		log.write("RANGO DE FECHAS > " + lstRangoFechaCuentaBanco);
		
		
		/*** Solo para ver y revisar
		HashMap<String,Object> paramsVacio = new HashMap<>();  // falta llevar arriva
		List<RangoFechaCuentaBanco> lstRangoFechaCuentaBancoRevisar = _sql.query(con, SQL_SELECT_RangoFechaCuentaBanco, paramsVacio, RangoFechaCuentaBanco.class);
		log.write("RANGO DE FECHAS > " + lstRangoFechaCuentaBancoRevisar);
		***/
		
		
	    		
		
		
		
		//---------------------------------------------------------------------------------------------
		// CALCULA SALDO DISPONIBLE
		log.write("CALCULANDO SALDO DISPONIBLE (T0)");
		
		List<Long> paramsCBan = new ArrayList<Long>();
		for (CuentaBanco CBan : lstCuentaBanco)
  		{
			paramsCBan.add(CBan.IdCuentaBanco);
  		}
		
		params.clear();
		params.put("IdCuentaBanco", paramsCBan);
		params.put("FechaDesde", in.p_FechaDesde);
		
		List<SaldoDisponible> lstSaldoDisponible = _sql.query(con, SQL_SELECT_CalculaSaldoDisponible, params, SaldoDisponible.class);
		log.write("SALDOS DISPONIBLES > " + lstSaldoDisponible);
		
		
		
		//---------------------------------------------------------------------------------------------
		// CALCULA SALDO PENDIENTE (Suma movimientos y comprobantes en transito que aun no se han certificado/liquidado)
		
		if (IncluyeSaldosPendientes.equals("S"))
		{	
			log.write("CALCULANDO SALDOS PENDIENTES");
			
			params.clear();			
			List<SaldoPendiente> lstSaldoPendiente = _sql.query(con, SQL_SELECT_CalculaSaldoPendiente, params, SaldoPendiente.class);			
			log.write("SALDOS PENDIENTES > " + lstSaldoPendiente);
						
			for (SaldoPendiente saldoPen: lstSaldoPendiente)
			{
				mapSaldoPendiente.put(saldoPen.IdCuentaBanco, saldoPen.FechaSaldo, saldoPen);	
			}
			
		}

		
		
		//---------------------------------------------------------------------------------------------
		// CALCULA SALDO CONTABLE (Saldo disponible + Saldo Pendiente)
		
		if (IncluyeSaldosContables.equals("S"))
		{
			log.write("CALCULANDO SALDOS CONTABLES");
			
			List<SaldoContable> lstSaldoContable = new ArrayList<SaldoContable>(); 
			SaldoContable objSaldoContable;

			for (SaldoDisponible saldoDis : lstSaldoDisponible)
			{
				SaldoPendiente objSaldoPen = (SaldoPendiente) mapSaldoPendiente.get(saldoDis.IdCuentaBanco, saldoDis.FechaSaldo);
				
				objSaldoContable = new SaldoContable();
				
				objSaldoContable.IdCuentaBanco 			= saldoDis.IdCuentaBanco;
				objSaldoContable.FechaSaldo 			= saldoDis.FechaSaldo;
				objSaldoContable.SaldoContableEgreso 	= saldoDis.SaldoDisponibleEgreso.add(objSaldoPen.SaldoPendienteEgreso);
				objSaldoContable.SaldoContableIngreso 	= saldoDis.SaldoDisponibleIngreso.add(objSaldoPen.SaldoPendienteIngreso);
				objSaldoContable.SaldoContable 			= saldoDis.SaldoDisponible.add(objSaldoPen.SaldoPendiente);
				
				lstSaldoContable.add(objSaldoContable);
			}
			log.write("SALDOS CONTABLE > " + lstSaldoContable);
			
			
			for (SaldoContable saldoCon: lstSaldoContable)
			{
				mapSaldoContable.put(saldoCon.IdCuentaBanco, saldoCon.FechaSaldo, saldoCon);
			}
				
		}

		
		//---------------------------------------------------------------------------------------------
		// CALCULA SALDOS PROYECTADOS > T0 (T1, T2, T3)
		
		if (IncluyeSaldosFuturos.equals("S"))
		{
			
			log.write("CALCULANDO SALDOS PROYECTADOS (T1, T2, T3)");
			
			//Rango de fechas para proyección		
			
			Date fechaProyeccionDesde	= agregarDiasFecha(fechaDesde, 1);
			Date fechaProyeccionHasta	= agregarDiasFecha(fechaHasta, 5);
			
			
			List<Date> lstRangoFechasProyeccion = generarRangoFechas(fechaProyeccionDesde, fechaProyeccionHasta);
			
			List <RangoFechaCuentaBanco> lstRangoFechaProyeccionCuentaBanco = new ArrayList<RangoFechaCuentaBanco>();
			RangoFechaCuentaBanco objRangoFechaProyeccionCuentaBanco; 
			
			int orden;
			
	  		for (CuentaBanco CBan : lstCuentaBanco)
	  		{
	  			orden = 0;
	    		for (Date fecha : lstRangoFechasProyeccion) 
	    		{
	    			orden++;
	    			objRangoFechaProyeccionCuentaBanco = new RangoFechaCuentaBanco();
	    			objRangoFechaProyeccionCuentaBanco.IdCuentaBanco = CBan.IdCuentaBanco;
	    			objRangoFechaProyeccionCuentaBanco.FechaSaldo = formatter.format(fecha);
	    			objRangoFechaProyeccionCuentaBanco.Orden = orden;
	    			lstRangoFechaProyeccionCuentaBanco.add(objRangoFechaProyeccionCuentaBanco);
	    		}
	  		}
	  		
	  		_sql.batch(con, SQL_INSERT_RangoFechaCuentaBanco, lstRangoFechaProyeccionCuentaBanco, RangoFechaCuentaBanco.class);

	  		
			params.clear();			
			List<SaldoProyectado> lstSaldoProyectado = _sql.query(con, SQL_SELECT_CalculaSaldoProyectado, params, SaldoProyectado.class);			
			log.write("SALDOS PROYECTADO > " + lstSaldoProyectado);

			
			for (SaldoProyectado saldoPro: lstSaldoProyectado)
			{
				mapSaldoProyectado.put(saldoPro.IdCuentaBanco, saldoPro.FechaSaldo, saldoPro);	
			}
			
		}
		

		
		//---------------------------------------------------------------------------------------------
		// PREPARA LA SALIDA DEL SERVICIO
		
		List<SaldoCuentaBanco_Salida> salidaSaldoCuentaBanco = new ArrayList<SaldoCuentaBanco_Salida>();
		SaldoCuentaBanco_Salida objSaldoCuentaBanco;     
		
		for (SaldoDisponible saldoDis : lstSaldoDisponible)
		{
			
			CuentaBanco    	objCuentaBanco   	= (CuentaBanco) hashCuentaBanco.get(saldoDis.IdCuentaBanco);
			SaldoPendiente 	objSaldoPendiente 	= (SaldoPendiente) mapSaldoPendiente.get(saldoDis.IdCuentaBanco, saldoDis.FechaSaldo);
			SaldoContable  	objSaldoContable 	= (SaldoContable) mapSaldoContable.get(saldoDis.IdCuentaBanco, saldoDis.FechaSaldo);
			SaldoProyectado objSaldoProyectado 	= (SaldoProyectado) mapSaldoProyectado.get(saldoDis.IdCuentaBanco, saldoDis.FechaSaldo);
			
			
			objSaldoCuentaBanco = new SaldoCuentaBanco_Salida();
			
			objSaldoCuentaBanco.IdCuentaBanco 		 = objCuentaBanco.IdCuentaBanco;
			objSaldoCuentaBanco.NombreCuentaBanco 	 = objCuentaBanco.NombreCuentaBanco;
			objSaldoCuentaBanco.IdBanco 			 = objCuentaBanco.IdBanco;
			objSaldoCuentaBanco.NombreBanco 		 = objCuentaBanco.NombreBanco;
			objSaldoCuentaBanco.NumeroCuentaBancaria = objCuentaBanco.NumeroCuentaBancaria;
			objSaldoCuentaBanco.IdMoneda 			 = objCuentaBanco.IdMoneda;
			objSaldoCuentaBanco.NombreMoneda 		 = objCuentaBanco.NombreMoneda;
			objSaldoCuentaBanco.FechaSaldo 			 = saldoDis.FechaSaldo;
			
			//Saldo Disponible T0 (Real)
			objSaldoCuentaBanco.SaldoDisponibleInicial 	= saldoDis.SaldoDisponibleInicial;
			objSaldoCuentaBanco.SaldoDisponibleIngreso 	= saldoDis.SaldoDisponibleIngreso;
			objSaldoCuentaBanco.SaldoDisponibleEgreso 	= saldoDis.SaldoDisponibleEgreso;
			objSaldoCuentaBanco.SaldoDisponible 		= saldoDis.SaldoDisponible;
			
			//Saldo Pendiente (movimientos y comprobantes en transito aun no certificados/liquidados)
			objSaldoCuentaBanco.SaldoPendienteIngreso 	= objSaldoPendiente == null ? BigDecimal.ZERO : objSaldoPendiente.SaldoPendienteIngreso;
			objSaldoCuentaBanco.SaldoPendienteEgreso 	= objSaldoPendiente == null ? BigDecimal.ZERO : objSaldoPendiente.SaldoPendienteEgreso;
			objSaldoCuentaBanco.SaldoPendiente 			= objSaldoPendiente == null ? BigDecimal.ZERO : objSaldoPendiente.SaldoPendiente;
			
			//Saldo Contable (Saldo Disponible + Saldo Pendiente)
			objSaldoCuentaBanco.SaldoContableIngreso 	= objSaldoContable == null ? BigDecimal.ZERO : objSaldoContable.SaldoContableIngreso;
			objSaldoCuentaBanco.SaldoContableEgreso 	= objSaldoContable == null ? BigDecimal.ZERO : objSaldoContable.SaldoContableEgreso;
			objSaldoCuentaBanco.SaldoContable 			= objSaldoContable == null ? BigDecimal.ZERO : objSaldoContable.SaldoContable;
			
			//Saldo Proyectado > T0 (T1, T2, T3)
			objSaldoCuentaBanco.FechaT1 				= objSaldoProyectado == null ? "0001-01-01" : objSaldoProyectado.FechaT1;
			objSaldoCuentaBanco.SaldoIngresoT1			= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoIngresoT1;
			objSaldoCuentaBanco.SaldoEgresoT1			= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoEgresoT1;
			objSaldoCuentaBanco.SaldoT1					= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoT1;	      
			objSaldoCuentaBanco.FechaT2 				= objSaldoProyectado == null ? "0001-01-01" : objSaldoProyectado.FechaT2;
			objSaldoCuentaBanco.SaldoIngresoT2			= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoIngresoT2;
			objSaldoCuentaBanco.SaldoEgresoT2			= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoEgresoT2;
			objSaldoCuentaBanco.SaldoT2					= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoT2;	      
			objSaldoCuentaBanco.FechaT3 				= objSaldoProyectado == null ? "0001-01-01" : objSaldoProyectado.FechaT3;
			objSaldoCuentaBanco.SaldoIngresoT3			= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoIngresoT3;
			objSaldoCuentaBanco.SaldoEgresoT3			= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoEgresoT3;
			objSaldoCuentaBanco.SaldoT3					= objSaldoProyectado == null ? BigDecimal.ZERO : objSaldoProyectado.SaldoT3;
	        
			salidaSaldoCuentaBanco.add(objSaldoCuentaBanco);
		}
				
		out.P_OUT_SALDOCUENTABANCO = salidaSaldoCuentaBanco;
		
	
		log.write("PARAMETROS DE SALIDA > " + out);
		
		log.write("== TERMINO CONSULTA SALDO CUENTA BANCO ==");
		return out;
		
	}

	
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			Gson gson = Utils.getGsonBuilder();

			//String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaDesde\":\"2018-10-09\",\"p_FechaHasta\":\"2018-10-09\", \"p_CuentasAConsultar\":[{\"IdCuentaBanco\":null,\"IdBanco\":17,\"IdMoneda\":\"CLP\"}]}";
			//String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaDesde\":\"2018-10-09\",\"p_FechaHasta\":\"2018-10-09\",\"p_CuentasAConsultar\":[{\"IdCuentaBanco\":null,\"IdBanco\":null,\"IdMoneda\":null}]}";
			String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaDesde\":\"2018-10-12\",\"p_FechaHasta\":\"2018-10-12\",\"p_CuentasAConsultar\":[{\"IdCuentaBanco\":136,\"IdBanco\":17,\"IdMoneda\":\"CLP\"}]}";
			
			ConsultarSaldoCuentaBanco_Entrada Objin = gson.fromJson(test, ConsultarSaldoCuentaBanco_Entrada.class);
			ConsultarSaldoCuentaBanco demo = new ConsultarSaldoCuentaBanco();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{	
 				ConsultarSaldoCuentaBanco_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();
			}
 			
 			
 			con.close();
 			
	    }
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	

}
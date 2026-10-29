package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.prt.AnticiparPrestamo.AnticiparPrestamo_Entrada;
import cl.bcs.custodia.prt.AnticiparPrestamo.AnticiparPrestamo_Entrada_DatosPrestamo;
import cl.bcs.custodia.prt.AnticiparPrestamo.AnticiparPrestamo_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoAnticipado;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AnticiparPrestamoDesdeBlockChain implements JavaCall
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_PRESTAMO			= loadResourceToString("cl/bcs/custodia/sql/AnticiparPrestamoDesdeBlockChain_SelectPrestamo.sql");
		
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------

	public static class AnticiparPrestamoDesdeBlockChain_Entrada extends BcsPojo {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public List<PrestamoAnticipado>	p_PrestamosAnticipado;
		public String 					p_FechaAnticipo;
		
		public AnticiparPrestamoDesdeBlockChain_Entrada() {
			this.p_IdModulo = "CUS";
		}
	}
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class AnticiparPrestamoDesdeBlockChain_Salida extends BcsPojo	{
		public List<MensajeSalida>			P_OUT_MENSAJES			;
		public Long							P_OUT_ERRORESCONTADOR	;
		public List<String>					P_OUT_RESULTADO			; //retorna los FoliosBlockChain que se pudieron crear en Optimus
		
		public AnticiparPrestamoDesdeBlockChain_Salida()
		{	
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR	= 0L;
			this.P_OUT_RESULTADO = new ArrayList<>();
		}
	}
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class Prestamo
	{
		public Long			FolioPrestamo;
		public Long			FolioComprobante;
		public String		NombreEstado;
		public String		IdEstado;
		public String		IdTipoPrestamo;
		public String		NombreTipoPrestamo;
		public String		DocumentoIdentidad;
		public String		CodigoPortafolio;
		public String		RazonSocial;
		public String		IdBolsa;
		public String		NombreBolsa;
		public String		NemoSubyacente;
		public String		FechaPrestamo;
		public String		FechaLiquidacion;
		public String		FechaVencimientoPrestamo;
		public Long			PlazoEnDiasOperacion;
		public BigDecimal	INGCantidad;
		public BigDecimal	INGPrecioInstrumento;
		public BigDecimal	INGMontoInstrumento;
		public BigDecimal	INGPrecioMercadoInstrumento;
		public BigDecimal	INGMontoMercadoInstrumento;
		public BigDecimal	INGTasaPrestamo;
		public BigDecimal	MontoPrimaOriginal;
		public BigDecimal	INGMontoPrima;
		public BigDecimal	INGMontoTotalPrestamo;
		public BigDecimal	CantidadAsignada;
		public BigDecimal	CantidadPorAsignar;
		public String		INGFechaOperacion;
		public String		IdClasificacionTipoInstrumentoSubyacente;
		public BigDecimal	INGMontoInteresCorridoCompraActual;
		public BigDecimal	INGMontoInteresCompraProximo;
		public String		EstadoAnticipo;
		public String		EstadoLiquidacion;
		public String		IdCondicionLiquidacion;
		public Long			IdInstrumentoSubyacente;
		public BigDecimal	INGTasaInstrumento;
		public Long			IdAgente;
		public Long			IdInstrumento;
		public String		NemotecnicoPrestamo;
		public String		IdTipoInstrumento;
		public String 		IdTipoCanalIngreso;
		public String		IdMonedaSistema;
		public String		TipoCustodia;
		public String		ExigirGarantia;
		public String		IdTipoInstrumentoSubyacente;
		public String 		FolioSistemaExterno;
		public BigDecimal	PrecioCompra;
		public String 		EsDiaHabilPlazoPrestamo;
	}

	public static class TBLDETALLE
	{
		public Long 		FolioEmprestito;
		public BigDecimal	ValorPremio;
		public BigDecimal	TasaPremio;
		public Long 		Plazo;
	}
	
	//-------------------------------------------------------------------------------------------
	// ANTICIPAR PRESTAMOS EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public AnticiparPrestamoDesdeBlockChain_Salida logica(Connection conn, AnticiparPrestamoDesdeBlockChain_Entrada in) throws Exception 
	{
		AnticiparPrestamoDesdeBlockChain_Salida salida 	= new AnticiparPrestamoDesdeBlockChain_Salida();
		List<Prestamo> prestamos 						= new ArrayList<>();
		HashMap<String,String> mapMsg 					= new HashMap<>();
		SimpleDateFormat format 						= new SimpleDateFormat("yyyy-MM-dd");
		
		String usaFechaLiquidacionEnVencimiento = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		prestamos = ObtienePrestamos(conn, in.p_IdEmpresa, in.p_PrestamosAnticipado);
		
		for (PrestamoAnticipado prestamoAnticipadoItem : in.p_PrestamosAnticipado) {
			if (prestamos.stream().
					filter(prestamo -> prestamo.FolioSistemaExterno.equals(prestamoAnticipadoItem.FolioSistemaExterno))
					.collect(Collectors.toList()).size() == 0) {
				mapMsg.put("FolioSistemaExterno", prestamoAnticipadoItem.FolioSistemaExterno);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PRESTAMO_BLOCKCHAIN_NO_ENCONTRADO", mapMsg, conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR += 1L;
				mapMsg.clear();
			}
		}
		
		CrearPrestamoDesdeBlockChain objCrearPrestamo 	= new CrearPrestamoDesdeBlockChain();
		ConsultarEmpresa consultarEmpresaService 		= new ConsultarEmpresa();
		InformacionEmpresa empresa 	= consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long IdPaisEmpresa 			= empresa.IdPais;
		
		for (Prestamo prestamo : prestamos) {
			
			PrestamoAnticipado prestamoAnticipadoBlockChain = in.p_PrestamosAnticipado.stream()
					.filter(folio_bck -> folio_bck.FolioSistemaExterno.equals(prestamo.FolioSistemaExterno))
					.collect(Collectors.toList())
					.get(0);
			
			BigDecimal totalPremio 			= new BigDecimal("0");
			totalPremio 					= CalcularPremio(conn, in, IdPaisEmpresa, prestamo);
			BigDecimal cantidadBlockChain 	= new BigDecimal(prestamoAnticipadoBlockChain.Cantidad);
			BigDecimal tasaBlockChain 		= new BigDecimal(prestamoAnticipadoBlockChain.Tasa);
			String fechaDesdePlazo 			= usaFechaLiquidacionEnVencimiento.equalsIgnoreCase(ConstantesBCK.SI) && format.parse(prestamo.FechaLiquidacion).after(format.parse(in.p_FechaAnticipo)) ? prestamo.FechaLiquidacion : in.p_FechaAnticipo;
			String plazo 					= objCrearPrestamo.Plazo(conn, in.p_IdEmpresa, in.p_IdUsuario, fechaDesdePlazo, prestamo.FechaVencimientoPrestamo, IdPaisEmpresa, ConstantesBCK.CONDICION_DE_LIQUIDACION_T0, "S", prestamo.EsDiaHabilPlazoPrestamo);
			Long diasCobrados 				= prestamo.PlazoEnDiasOperacion - Long.parseLong(plazo);
			BigDecimal monto 				= prestamo.PrecioCompra.multiply(cantidadBlockChain);
			BigDecimal montoPlazo 			= objCrearPrestamo.calcularMontoPlazo(monto, diasCobrados.toString(), (tasaBlockChain.compareTo(new BigDecimal("0")) > 0 ? tasaBlockChain.toString() : prestamo.INGTasaPrestamo.toString()));
			BigDecimal montoPrima 			= montoPlazo.subtract(monto);
			
			
			AnticiparPrestamo objAnticiparPrestamo 								= new AnticiparPrestamo();
			AnticiparPrestamo_Entrada objAnticiparPrestamo_Entrada 				= new AnticiparPrestamo_Entrada();
			AnticiparPrestamo_Entrada_DatosPrestamo objEntrada_DatosPrestamo 	= new AnticiparPrestamo_Entrada_DatosPrestamo();
			
			objAnticiparPrestamo_Entrada.p_IdEmpresa 			= in.p_IdEmpresa;
			objAnticiparPrestamo_Entrada.p_IdUsuario 			= in.p_IdUsuario;
			objAnticiparPrestamo_Entrada.p_IdModulo 			= in.p_IdModulo;
			objAnticiparPrestamo_Entrada.p_UsaReglasNegocio 	= "S";
			objAnticiparPrestamo_Entrada.p_IdPais 				= IdPaisEmpresa;
			objAnticiparPrestamo_Entrada.p_UsaPrecioMedio 		= "N";
			objAnticiparPrestamo_Entrada.p_FechaAnticipo 		= in.p_FechaAnticipo;
			objAnticiparPrestamo_Entrada.p_VieneDesdeBlockChain = "S";
			
			
			objEntrada_DatosPrestamo.FolioPrestamo 			= prestamo.FolioPrestamo;
			objEntrada_DatosPrestamo.FechaAnticipo 			= in.p_FechaAnticipo;
			objEntrada_DatosPrestamo.IdPais 				= IdPaisEmpresa;
			objEntrada_DatosPrestamo.IdMonedaLiquidacion 	= prestamo.IdMonedaSistema;
			objEntrada_DatosPrestamo.IdCondicionLiquidacion = prestamo.IdCondicionLiquidacion;
			objEntrada_DatosPrestamo.FechaLiquidacion 		= in.p_FechaAnticipo;
			objEntrada_DatosPrestamo.DevolverCustodia 		= "S";
			objEntrada_DatosPrestamo.IdEmpleadoDigitador 	= in.p_IdUsuario;
			objEntrada_DatosPrestamo.Cantidad 				= prestamo.INGCantidad;
			objEntrada_DatosPrestamo.PrecioInstrumento 		= prestamo.PrecioCompra;
			objEntrada_DatosPrestamo.TasaAnticipo 			= prestamo.INGTasaPrestamo;
			objEntrada_DatosPrestamo.MontoPrima 			= montoPrima;
			objEntrada_DatosPrestamo.MontoPremio 			= totalPremio;
			objEntrada_DatosPrestamo.Monto 					= monto;
			objEntrada_DatosPrestamo.MontoAPlazo 			= montoPlazo;
			
			objAnticiparPrestamo_Entrada.p_DatosPrestamos.add(objEntrada_DatosPrestamo);
			
			AnticiparPrestamo_Salida objAnticiparPrestamo_Salida = objAnticiparPrestamo.logica(conn, objAnticiparPrestamo_Entrada);
			
			salida.P_OUT_MENSAJES.addAll(
					objAnticiparPrestamo_Salida.P_OUT_MENSAJES.stream()
					.map( mensaje -> new MensajeSalida( prestamo.FolioPrestamo, mensaje.tipo, mensaje.codigo, mensaje.descripcion ) )
					.collect( Collectors.toList() )
					);
			salida.P_OUT_ERRORESCONTADOR += objAnticiparPrestamo_Salida.P_OUT_ERRORESCONTADOR;
		}
		
		return salida; 
		
	}
	
	//-------------------------------------------------------------------------------------------
	// DEVUELVE DATOS DE PRESTAMO
	//-------------------------------------------------------------------------------------------
	private List<Prestamo> ObtienePrestamos(Connection conn, Long p_IdEmpresa, List<PrestamoAnticipado> p_PrestamosAnticipado) throws SQLException
	{
		SQL sql 							= new SQL();
		HashMap<String,Object> hparam 		= new HashMap<String,Object>();
		List<String> foliosSistemaExterno 	= p_PrestamosAnticipado.stream()
				.map(prestamo_anticipado -> prestamo_anticipado.FolioSistemaExterno)
				.collect(Collectors.toList());
		
		hparam.put("p_IdEmpresa"			, p_IdEmpresa );
		hparam.put("p_FolioSistemaExterno"	, foliosSistemaExterno );
		List<Prestamo> lstPrestamo = sql.query(conn, SQL_SELECT_PRESTAMO, hparam, Prestamo.class);
		
		return lstPrestamo;
	}

	public BigDecimal CalcularPremio(Connection conn, AnticiparPrestamoDesdeBlockChain_Entrada in, Long idPais, Prestamo prestamo) throws SQLException 
	{
		BigDecimal totalPremio 			= new BigDecimal("0");
		SQL sql 						= new SQL();
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
		BigDecimal cantidad 			= new BigDecimal(in.p_PrestamosAnticipado.stream()
				.filter(prestamo_anticipado -> prestamo_anticipado.FolioSistemaExterno.equals(prestamo.FolioSistemaExterno))
				.collect(Collectors.toList())
				.get(0).Cantidad);
		
		parmsIN.put("P_IDEMPRESA"		, in.p_IdEmpresa);
		parmsIN.put("P_IDUSUARIO"		, in.p_IdUsuario);
		parmsIN.put("P_IDMODULO"		, in.p_IdModulo);
		parmsIN.put("P_IDPAIS"			, idPais);
		parmsIN.put("P_FOLIOPRESTAMO"	, prestamo.FolioPrestamo );
		parmsIN.put("P_FECHAANTICIPO"	, in.p_FechaAnticipo);
		parmsIN.put("P_PRECIO"			, prestamo.PrecioCompra);
		parmsIN.put("P_CANTIDADANTICIPO", cantidad);
		
		parmsOUT.put("P_OUT_TOTALPRIMA"	, BigDecimal.class);
		parmsOUT.put("P_TBLDETALLE"		, TBLDETALLE.class);

		String queryCall 			= "call \"BSCL.Custodia.BD.Servicios::PRT.CalcularPremiosAnticipo\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,:P_IDPAIS,:P_FOLIOPRESTAMO,:P_FECHAANTICIPO,:P_PRECIO,:P_CANTIDADANTICIPO,@P_OUT_TOTALPRIMA,$P_TBLDETALLE)";
		HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);
		
		totalPremio 				=  (BigDecimal) rpt.get("P_OUT_TOTALPRIMA");
		List<TBLDETALLE> lstDetalle = (List<TBLDETALLE>) rpt.get("P_TBLDETALLE");
		
		return totalPremio;
	}

	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		AnticiparPrestamoDesdeBlockChain_Entrada in = gson.fromJson(jin.toString(), AnticiparPrestamoDesdeBlockChain_Entrada.class);
		AnticiparPrestamoDesdeBlockChain_Salida out = logica(con,in);
		
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

			String json = "{\"p_IdEmpresa\":1, \"p_IdUsuario\":54, \"p_FolioBlockChain\":\"61-0000000019756\" }";
			AnticiparPrestamoDesdeBlockChain_Entrada Objin = gson.fromJson(json, AnticiparPrestamoDesdeBlockChain_Entrada.class);
			AnticiparPrestamoDesdeBlockChain demo = new AnticiparPrestamoDesdeBlockChain();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				AnticiparPrestamoDesdeBlockChain_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				//con.rollback();
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

	
}

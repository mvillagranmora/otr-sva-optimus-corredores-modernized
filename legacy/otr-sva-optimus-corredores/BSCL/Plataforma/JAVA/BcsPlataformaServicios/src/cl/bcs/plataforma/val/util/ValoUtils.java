package cl.bcs.plataforma.val.util;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
*
* @author	: Jorge Garcés :: JG
* @date		: 03/11/2016
* @version	: 1.0
*/

public class ValoUtils {
	public static String SQL_SelectTareaValorizacion = "select \"IdTarea\",\"GrupoValorizacion\",\"FechaSolicitud\",\"IdEstado\",\"RutinaValorizacion\",\"OpcionValorizacion\","
			+ "\"TipoValorizacion\",\"TipoVal\",\"Nemo\",\"FechaVal\",\"FechaEmi\",\"FechaVcto\",\"NumeroDias\",\"TasaTir\",\"TasaTip\",\"TasaInteres\",\"ValRescateP\",\"ValRescateR\","
			+ "\"ValCaptacionP\",\"ValCaptacionR\",\"GlosaEmisor\",\"GlosaMensaje\",\"Cantidad\",\"UnidadMon\",\"Precio\",\"MontoPeso\",\"MontoUF\",\"MontoUS\",\"AnoVcto\",\"MesVcto\","
			+ "\"Duration\",\"ValorBase\",\"Convexidad\",\"ValorPar2\",\"ValorPar\",\"TipoTasa\",\"CalculoEst\",\"APlazo\",\"ValorRM\",\"ValorNom\",\"TipoMoneda\",\"CLiq\",\"Gestion\","
			+ "\"Estado\",\"Glosa\",\"Hora\",\"WsCallerCodSalida\",\"WsCallerDescSalida\",\"SoloTipificar\",\"TipoTarea\",\"FechaIni\",\"FechaFin\",\"CodTipoMod\",\"Plazo\","
			+ "\"CodigoEmisor\",\"FechaEmision\",\"FechaVencimiento\",\"TipoMod\",\"TipoUnidad\",\"ValorReferencial\",\"CuponAmortizacion\",\"CuponFactorAjuste\","
			+ "\"CuponFechaEfectiva\",\"CuponFechaHabil\",\"CuponFlujo\",\"CuponIntereses\",\"CuponSecuencia\""
			+ "from \"BSCL.Plataforma.BD::INS.TareaValorizacion\" WHERE \"IdEstado\" = 'PEN' and  \"GrupoValorizacion\" = :IdGrupo";
			
	public static String SQL_UpdateTareaValorizacion = "UPDATE \"BSCL.Plataforma.BD::INS.TareaValorizacion\" SET "
			+ "\"IdEstado\" = :IdEstado,\"TipoVal\" = :TipoVal,\"Nemo\" = :Nemo,\"FechaVal\" = :FechaVal,\"FechaEmi\" = :FechaEmi,"
			+ "\"FechaVcto\" = :FechaVcto,\"NumeroDias\" = :NumeroDias,\"TasaTir\" = :TasaTir,\"TasaTip\" = :TasaTip,"
			+ "\"TasaInteres\" = :TasaInteres,\"ValRescateP\" = :ValRescateP,\"ValRescateR\" = :ValRescateR,"
			+ "\"ValCaptacionP\" = :ValCaptacionP,\"ValCaptacionR\" = :ValCaptacionR,\"GlosaEmisor\" = :GlosaEmisor,"
			+ "\"GlosaMensaje\" = :GlosaMensaje,\"Cantidad\" = :Cantidad,\"UnidadMon\" = :UnidadMon,\"Precio\" = :Precio,"
			+ "\"MontoPeso\" = :MontoPeso,\"MontoUF\" = :MontoUF,\"MontoUS\" = :MontoUS,\"AnoVcto\" = :AnoVcto,\"MesVcto\" = :MesVcto,"
			+ "\"Duration\" = :Duration,\"ValorBase\" = :ValorBase,\"Convexidad\" = :Convexidad,\"ValorPar2\" = :ValorPar2,"
			+ "\"ValorPar\" = :ValorPar,\"TipoTasa\" = :TipoTasa,\"CalculoEst\" = :CalculoEst,\"APlazo\" = :APlazo,\"ValorRM\" = :ValorRM,"
			+ "\"ValorNom\" = :ValorNom,\"TipoMoneda\" = :TipoMoneda,\"CLiq\" = :CLiq,\"Estado\" = :Estado,\"Glosa\" = :Glosa,"
			+ "\"Hora\" = :Hora,\"WsCallerCodSalida\" = :WsCallerCodSalida,\"WsCallerDescSalida\" = :WsCallerDescSalida,"
			+ "\"RutinaValorizacion\" = :RutinaValorizacion,\"FechaIni\" = :FechaIni,\"FechaFin\" = :FechaFin,\"CodTipoMod\" = :CodTipoMod,"
			+ "\"Plazo\" = :Plazo,\"CodigoEmisor\" = :CodigoEmisor,\"FechaEmision\" = :FechaEmision,\"FechaVencimiento\" = :FechaVencimiento,"
			+ "\"TipoMod\" = :TipoMod,\"TipoUnidad\" = :TipoUnidad,\"ValorReferencial\" = :ValorReferencial,\"CuponAmortizacion\" = :CuponAmortizacion,"
			+ "\"CuponFactorAjuste\" = :CuponFactorAjuste,\"CuponFechaEfectiva\" = :CuponFechaEfectiva,\"CuponFechaHabil\" = :CuponFechaHabil,"
			+ "\"CuponFlujo\" = :CuponFlujo,\"CuponIntereses\" = :CuponIntereses,\"CuponSecuencia\" = :CuponSecuencia "
			+ "WHERE \"GrupoValorizacion\" = :GrupoValorizacion AND \"IdTarea\" = :IdTarea"; //TODO: Autonomous?
	
	//CLASES
	public static class TareaValorizacion {
		public Long					IdTarea; 
		public Long					GrupoValorizacion; 
		public java.sql.Timestamp	FechaSolicitud; 
		public String				IdEstado; 
		public String				RutinaValorizacion; 
		public String				OpcionValorizacion; 
		public String				TipoValorizacion; 
		public Long					TipoVal; 
		public String				Nemo; 
		public java.sql.Date		FechaVal; 
		public java.sql.Date		FechaEmi; 
		public java.sql.Date		FechaVcto; 
		public Long					NumeroDias; 
		public BigDecimal			TasaTir; 
		public BigDecimal			TasaTip; 
		public BigDecimal			TasaInteres; 
		public BigDecimal			ValRescateP; 
		public BigDecimal			ValRescateR; 
		public BigDecimal			ValCaptacionP; 
		public BigDecimal			ValCaptacionR; 
		public String				GlosaEmisor; 
		public String				GlosaMensaje; 
		public BigDecimal			Cantidad; 
		public String				UnidadMon; 
		public BigDecimal			Precio; 
		public BigDecimal			MontoPeso; 
		public BigDecimal			MontoUF; 
		public BigDecimal			MontoUS; 
		public Long					AnoVcto; 
		public Long					MesVcto; 
		public BigDecimal			Duration; 
		public BigDecimal			ValorBase; 
		public BigDecimal			Convexidad; 
		public BigDecimal			ValorPar2; 
		public BigDecimal			ValorPar; 
		public String				TipoTasa; 
		public String				CalculoEst; 
		public Long					APlazo; 
		public BigDecimal			ValorRM; 
		public BigDecimal			ValorNom; 
		public Long					TipoMoneda; 
		public String				CLiq; 
		public String				Gestion; 
		public String				Estado; 
		public String				Glosa; 
		public String				Hora; 
		public String				WsCallerCodSalida; 
		public String				WsCallerDescSalida; 
		public String				SoloTipificar; 
		public String				TipoTarea; 
		public java.sql.Date		FechaIni; 
		public java.sql.Date		FechaFin; 
		public String				CodTipoMod; 
		public Long					Plazo; 
		public String				CodigoEmisor; 
		public java.sql.Date		FechaEmision; 
		public java.sql.Date		FechaVencimiento; 
		public String				TipoMod; 
		public String				TipoUnidad; 
		public String				ValorReferencial; 
		public BigDecimal			CuponAmortizacion; 
		public BigDecimal			CuponFactorAjuste; 
		public java.sql.Date		CuponFechaEfectiva; 
		public java.sql.Date		CuponFechaHabil; 
		public BigDecimal			CuponFlujo; 
		public BigDecimal			CuponIntereses; 
		public Long					CuponSecuencia; 
	}
	
	//Conversor de formatos
	public static String dateToString(Date entrada){
		String salida = null;
		if(entrada!=null){
			DateFormat df = new SimpleDateFormat("yyyyMMdd");     
			salida = df.format(entrada);
		} return salida;
	}
	
	public static Date stringToDate(String entrada){
		Date fecha;
		try {
			DateFormat formatter = new SimpleDateFormat("yyyyMMdd"); 
			fecha = formatter.parse(entrada);
		}
		catch(Exception e) {
			fecha = null;
		} return fecha;
	}
	
	public static java.sql.Date stringToSqlDate(String entrada){
		java.sql.Date salida = null;
		if(entrada != null) {
			try {
				DateFormat formatter = new SimpleDateFormat("yyyyMMdd"); 
				Date fecha = formatter.parse(entrada);
				salida = new java.sql.Date(fecha.getTime());
			}
			catch(Exception e) {
				salida = null;
			}
		} return salida;
	}
}
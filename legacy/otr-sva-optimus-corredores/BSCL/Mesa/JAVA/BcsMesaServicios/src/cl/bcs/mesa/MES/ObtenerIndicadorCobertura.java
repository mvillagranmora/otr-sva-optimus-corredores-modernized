package cl.bcs.mesa.MES;

import java.math.BigDecimal;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class ObtenerIndicadorCobertura implements JavaCall{	
	static final Logger logger = LogManager.getLogger(ObtenerIndicadorCobertura.class);	
	private final String SQL_ConsultaOperacionesContrarias = loadResourceToString("cl/bcs/mesa/MES/sql/ObtenerIndicadorCobertura_SelectOperacionPacto.sql");
	
	public static class ObtenerIndicadorCoberturaEntrada {
				
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		
		public List<DetalleOperaciones>		p_Operaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class DetalleOperaciones{
		//Detalle
	    public Long IdOperacion;
	    public Long FolioOperacion;
		public String Nemotecnico;	
		public String IdEmisor;
		public String Mercado;
		public String IdTipoReajuste;
		public BigDecimal Tasa;
		public int IndicadorCobertura;
		public Date FechaVencimientoInstrumento;
		public String FechaVencimientoInstrumentoCadena;
		public BigDecimal Cantidad;
		public BigDecimal MontoSubyacente;
		public BigDecimal MontoCompromisoDetalle;
		//Operacion
	    public String FechaOperacion;
	    public String IdTipoOperacion;
	    public String NombreOperacion;
	    public String FechaLiquidacion;
	    public String FechaVencimiento;
	    public Date DateLiquidacion;
	    public Date DateVencimiento;
	    public Date FechaAnticipo;
	    public String Liquidado;
	    public Long FolioCartera;
	    public Long PlazoEnDias;
	    public String RelacionCorredora;
	    public String IdTipoInstrumento;
	    public Date FechaRespaldo;
		public String FlagDeterioro;
		public BigDecimal PorcentajeDeterioro;
		public BigDecimal Deterioro;
		public int Fase;
		public String IdMonedaInstrumento;
	    
	    public Long getFolioOperacion() {
	        return FolioOperacion;
	    }
	    
	    public Date getFechaRespaldo() {
	        return FechaRespaldo;
	    }
	    
	    public String getIdEmisor() {
	    	return IdEmisor;
	    }
	    
	};
	
	public static class ObtenerIndicadorCoberturaSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR = 0L;
		public List<DetalleOperaciones>		P_OUT_DETALLEOPERACIONES;
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public ObtenerIndicadorCoberturaSalida logica(Connection conn, ObtenerIndicadorCoberturaEntrada in) throws Exception {
		SQL sql = new SQL();	
		
		int indicadorCobertura = 0;
		int cubiertoEndeudamiento = 0;
		int cubiertoPatrimonial = 0;
		
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<DetalleOperaciones> lstDetalleOperaciones = new ArrayList<DetalleOperaciones>();

		/* 
		 * 	Valores posibles y descripcion Indicador Cobertura para carteras de Operaciones Pactos
		 *	“1”	Especial	No se usa (En cartera)
		 * 	“2”	Sin cobertura	Cubierto por Endeudamiento, pero No para Cobertura Patrimonial
		 *	“3”	Con cobertura 	Cubierto por Cobertura Patrimonial, pero no para Endeudamiento
		 *	“5”	Cubierto 	Cubierto para Endeudamiento y para Cobertura Patrimonial
		 *	“0”	Descubierto, cero	Valor por Omision, descubierto para ambos
		*/
		
		if(!in.p_Operaciones.isEmpty()) {

			List<String> idEmisores = in.p_Operaciones.stream()
                    .map(DetalleOperaciones::getIdEmisor)
                    .collect(Collectors.toList());
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdEmisor", idEmisores);
			List <DetalleOperaciones> lstOpContrarios = sql.query(conn, SQL_ConsultaOperacionesContrarias, params, DetalleOperaciones.class);
			
			
			for(DetalleOperaciones detalleOperacion : in.p_Operaciones) {
				DetalleOperaciones detalleOperaciones = new DetalleOperaciones();
				//cubierto Endeudamiento
				cubiertoEndeudamiento = verificarCubrimientoEndeudamiento(detalleOperacion, lstOpContrarios, FechaSistema);
				//cubierto Patrimonial 
				cubiertoPatrimonial = verificarCubrimientoPatrimonial(detalleOperacion, lstOpContrarios, FechaSistema);
				
				indicadorCobertura = cubiertoEndeudamiento + cubiertoPatrimonial;
				detalleOperacion.IndicadorCobertura = indicadorCobertura;
				
				detalleOperaciones.FolioCartera = detalleOperacion.FolioCartera;
				detalleOperaciones.Cantidad = detalleOperacion.Cantidad;
				detalleOperaciones.FechaVencimientoInstrumento = detalleOperacion.FechaVencimientoInstrumento;
				detalleOperaciones.FechaVencimientoInstrumentoCadena = detalleOperacion.FechaVencimientoInstrumentoCadena;
				detalleOperaciones.FolioOperacion = detalleOperacion.FolioOperacion;
				detalleOperaciones.IdEmisor = detalleOperacion.FechaVencimientoInstrumentoCadena;
				detalleOperaciones.IdTipoReajuste = detalleOperacion.FechaVencimientoInstrumentoCadena;
				detalleOperaciones.IndicadorCobertura = detalleOperacion.IndicadorCobertura;
				detalleOperaciones.Mercado = detalleOperacion.Mercado;
				detalleOperaciones.MontoCompromisoDetalle = detalleOperacion.MontoCompromisoDetalle;
				detalleOperaciones.MontoSubyacente = detalleOperacion.MontoSubyacente;
				detalleOperaciones.Nemotecnico = detalleOperacion.Nemotecnico;

				lstDetalleOperaciones.add(detalleOperaciones);
			}				
		}	
			
		
		ObtenerIndicadorCoberturaSalida out = new ObtenerIndicadorCoberturaSalida();
		out.P_OUT_DETALLEOPERACIONES = lstDetalleOperaciones;
		return out;
	}
	
	public int verificarCubrimientoEndeudamiento(DetalleOperaciones carteraOperacion, List<DetalleOperaciones> lstOpContrarios, String FechaSistema) {
	    int cubiertoEndeudamiento = 0;   
	    if(carteraOperacion.FechaVencimiento.equals(carteraOperacion.FechaVencimientoInstrumentoCadena)) cubiertoEndeudamiento = 2;	    	   
	    return cubiertoEndeudamiento;
	}
	
	public int verificarCubrimientoPatrimonial(DetalleOperaciones carteraOperacion, List<DetalleOperaciones> lstOpContrarios, String FechaSistema) {
	    int cubiertoPatrimonial = 0;		    
	    if(carteraOperacion.FechaVencimiento.equals(carteraOperacion.FechaVencimientoInstrumentoCadena) && carteraOperacion.RelacionCorredora.equalsIgnoreCase("Sin Relacion")) cubiertoPatrimonial = 3;
	    return cubiertoPatrimonial;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();		
		ObtenerIndicadorCoberturaEntrada jin=gson.fromJson(in.toString() ,ObtenerIndicadorCoberturaEntrada.class);
		ObtenerIndicadorCoberturaSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
		
	public static void main(String[] args) throws Exception,SQLException {
		Connection conn = null;		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_Operaciones\":[{\"IdOperacion\":null,\"FolioOperacion\":132659,\"Nemotecnico\":\"BBIC630515\",\"IdEmisor\":\"BBICE\",\"Mercado\":\"RF\",\"IdTipoReajuste\":\"USD\",\"Tasa\":180.65370000,\"IndicadorCobertura\":0,\"FechaVencimientoInstrumento\":\"2025-05-01 00:00:00.000\",\"FechaVencimientoInstrumentoCadena\":\"01-05-2025\",\"Cantidad\":7970000.00000000,\"MontoSubyacente\":1887516.53377717,\"MontoCompromisoDetalle\":1682202947.10786650,\"FechaOperacion\":\"26-09-2023\",\"IdTipoOperacion\":\"COMPAC\",\"NombreOperacion\":\"COMPRA CON PACTO\",\"FechaLiquidacion\":\"26-09-2023\",\"FechaVencimiento\":\"20-10-2023\",\"DateLiquidacion\":\"2023-09-26 00:00:00.000\",\"DateVencimiento\":\"2023-10-20 00:00:00.000\",\"FechaAnticipo\":\"2023-09-26 00:00:00.000\",\"Liquidado\":\"N\",\"FolioCartera\":535483,\"PlazoEnDias\":24,\"RelacionCorredora\":\"Relacionado\",\"IdTipoInstrumento\":\"PAC\",\"FechaRespaldo\":\"2023-04-15 00:00:00.000\"}]}" ;
		Gson gson = Utils.getGsonBuilder();
		ObtenerIndicadorCoberturaEntrada entrada = gson.fromJson(json, ObtenerIndicadorCoberturaEntrada.class);	
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa02","connections.properties");
			ObtenerIndicadorCobertura prueba = new ObtenerIndicadorCobertura(); 
			ObtenerIndicadorCoberturaSalida Salida = prueba.logica(conn, entrada);
			System.out.println(Salida);
			conn.rollback();
			
		}catch (Exception e){	
			System.out.println(e);	
			} finally {		conn.close();		}
		System.out.println("== END ==");
	}
}
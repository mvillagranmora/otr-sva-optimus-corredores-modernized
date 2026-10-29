package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarCtaCustodioDefectoPortafolio implements JavaCall {
	private final String SQL_SELECT_CUENTASCUSTODIO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarCtaCustodioDefectoPortafolio_SelectCtasCustodio.sql");
	
	public static class ConsultarCtaCustodioDefectoPortafolio_Entrada {

		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String 				p_IdModulo;
		
		public String 				p_IdBolsa;
		public String 				p_IdModeloCustodia;
		public String 				p_IdClasificacionTipoInstrumento;
		
		public List<Long>			p_Portafolios;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class CuentaCustodio {
		public Long		IdPortafolio                 ;
		public Long		IdCuentaCustodioPortafolio   ;
		public String 	IdModeloCustodia             ;
		public String 	NombreModeloCustodia         ;
		public String 	CuentaCustodioPortafolio	 ;
		public String 	IdEstadoAutorizacion         ;
		public String 	IdEstado                     ;
		public String 	IdBoveda                     ;
		public String 	NombreBoveda                 ;
		public String 	NombreBovedaCodigo           ;
		public String 	IdTipoCuentaCustodia         ;
		public String 	NombreTipoCuentaCustodia     ;
		public String 	Defecto						 ;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaCustodio_out {
		public Long		IdPortafolio                 ;
		public Long		IdCuentaCustodioPortafolio   ;
		public String 	IdModeloCustodia             ;
		public String 	NombreModeloCustodia         ;
		public String 	CuentaCustodioPortafolio	 ;
		public String 	IdEstadoAutorizacion         ;
		public String 	IdEstado                     ;
		public String 	IdBoveda                     ;
		public String 	NombreBoveda                 ;
		public String 	NombreBovedaCodigo           ;
		public String 	IdTipoCuentaCustodia         ;
		public String 	NombreTipoCuentaCustodia     ;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ConsultarCtaCustodioDefectoPortafolio_Salida {
		public int 							P_OUT_ERRORES_CONTADOR;
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public List<CuentaCustodio_out>		P_OUT_CUENTASCUSTODIO;
		
		
		public ConsultarCtaCustodioDefectoPortafolio_Salida() {
			P_OUT_ERRORES_CONTADOR = 0;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_CUENTASCUSTODIO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarCtaCustodioDefectoPortafolio_Entrada in = gson.fromJson(jin.toString(), ConsultarCtaCustodioDefectoPortafolio_Entrada.class);
		ConsultarCtaCustodioDefectoPortafolio_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ConsultarCtaCustodioDefectoPortafolio_Salida logica(Connection con, ConsultarCtaCustodioDefectoPortafolio_Entrada in) throws Exception {
		ConsultarCtaCustodioDefectoPortafolio_Salida out = new ConsultarCtaCustodioDefectoPortafolio_Salida();
		SQL _sql = new SQL();
		String IdBovedaDefecto = "";

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORES_CONTADOR = 1;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		/***** Determina cuál es la bóveda x defecto de acuerdo a la bolsa ****/
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String codigoParametro = "";
		
		HashMap<String,Object> params = new HashMap<>();
		
		if (localizacionSistema.equalsIgnoreCase("CL")) {
			codigoParametro = "IdBovedaDefecto";
		} else {
			if (in.p_IdModeloCustodia.equalsIgnoreCase("LOCAL")){
				codigoParametro = "IdBovedaDefectoLOCAL";
			}else if(in.p_IdModeloCustodia.equalsIgnoreCase("EXTERIOR") && in.p_IdClasificacionTipoInstrumento.equalsIgnoreCase("RV")){
				codigoParametro = "IdBovedaDefectoEXTRV";
			}else if(in.p_IdModeloCustodia.equalsIgnoreCase("EXTERIOR") && in.p_IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")){
				codigoParametro = "IdBovedaDefectoEXTRF";
			}else if(in.p_IdModeloCustodia.equalsIgnoreCase("EXTERIOR") && in.p_IdClasificacionTipoInstrumento.equalsIgnoreCase("DV")){
				codigoParametro = "IdBovedaDefectoEXTOPC";
			}else{
				codigoParametro = "IdBovedaDefectoLOCAL";	
			}
		}
		
		params.put("codigoParametro", codigoParametro);
		IdBovedaDefecto = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "ORDENES", codigoParametro);
		
		
		/***** busca las cuentas de custodio del (o los) portafolio(s) ****/
		List<CuentaCustodio> tblCtaCustodio = new ArrayList<>();
		params = new HashMap<>();
		params.put("idsPortafolios", in.p_Portafolios);
		params.put("modeloCustodia", in.p_IdModeloCustodia);
		params.put("bovedaSugerida", IdBovedaDefecto);
		tblCtaCustodio = _sql.query(con, SQL_SELECT_CUENTASCUSTODIO, params, CuentaCustodio.class);
		
		if (tblCtaCustodio.size() == 0){
			return out; //sale sin error, ya que la búsqueda de estas cuentas es sólo para sugerirlas
		}
		
		/**** toma la 1ra cuenta para cada portafolio, asumiendo q el select ya trajo ordenado los datos, dejando en 1er lugar
		 *  (por portafolio) una cuenta con boveda igual a la boveda x defecto para esa bolsa/mercado 
		 */
		long last = 0L;
		for (CuentaCustodio cta: tblCtaCustodio){
			if (last != cta.IdPortafolio){
				CuentaCustodio_out reg = new CuentaCustodio_out();
				if (localizacionSistema.equalsIgnoreCase("CL")) {
					reg.IdPortafolio                 = cta.IdPortafolio;
					reg.IdCuentaCustodioPortafolio   = 0L;
					reg.IdModeloCustodia             = "";
					reg.NombreModeloCustodia         = "";
					reg.CuentaCustodioPortafolio	 = "";
					reg.IdEstadoAutorizacion         = "";
					reg.IdEstado                     = "";
					reg.IdBoveda                     = IdBovedaDefecto;
					reg.NombreBoveda                 = "";
					reg.NombreBovedaCodigo           = "";
					reg.IdTipoCuentaCustodia         = "";
					reg.NombreTipoCuentaCustodia     = "";
				} else {
					reg.IdPortafolio                 = cta.IdPortafolio              ;
					reg.IdCuentaCustodioPortafolio   = cta.IdCuentaCustodioPortafolio;
					reg.IdModeloCustodia             = cta.IdModeloCustodia          ;
					reg.NombreModeloCustodia         = cta.NombreModeloCustodia      ;
					reg.CuentaCustodioPortafolio	 = cta.CuentaCustodioPortafolio	 ;
					reg.IdEstadoAutorizacion         = cta.IdEstadoAutorizacion      ;
					reg.IdEstado                     = cta.IdEstado                  ;
					reg.IdBoveda                     = cta.IdBoveda                  ;
					reg.NombreBoveda                 = cta.NombreBoveda              ;
					reg.NombreBovedaCodigo           = cta.NombreBovedaCodigo        ;
					reg.IdTipoCuentaCustodia         = cta.IdTipoCuentaCustodia      ;
					reg.NombreTipoCuentaCustodia     = cta.NombreTipoCuentaCustodia  ;
				}
				out.P_OUT_CUENTASCUSTODIO.add(reg);
				last = cta.IdPortafolio;
			}
		}
		
		return out;

	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_IdBolsa\":\"BVL\",\"p_IdModeloCustodia\":\"LOCAL\",\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_Portafolios\":[40739,44850,44858]}";
			
			ConsultarCtaCustodioDefectoPortafolio_Entrada adf = gson.fromJson(json, ConsultarCtaCustodioDefectoPortafolio_Entrada.class);
			ConsultarCtaCustodioDefectoPortafolio demo=new ConsultarCtaCustodioDefectoPortafolio();
			ConsultarCtaCustodioDefectoPortafolio_Salida dfasflj = demo.logica(con, adf);
			System.out.println(dfasflj.toString());
	    }
		System.out.println("== END ==");
	}
}

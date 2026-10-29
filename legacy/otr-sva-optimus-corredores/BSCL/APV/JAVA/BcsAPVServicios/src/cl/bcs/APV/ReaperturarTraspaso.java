package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.IngresoRecaudacionTraspaso.DetalleTraspaso;
import cl.bcs.APV.IngresoRecaudacionTraspaso.EntradaRecaudacion;
import cl.bcs.APV.IngresoRecaudacionTraspaso.Resultado;
import cl.bcs.APV.ModificarPortafolioTraspaso.DetalleTraspasoAEliminar;
import cl.bcs.APV.ModificarPortafolioTraspaso.ParametrosEntradaServicio;
import cl.bcs.APV.ModificarPortafolioTraspaso.ParametrosSalidaServicio;
import cl.bcs.APV.ModificarPortafolioTraspaso.PortafolioEliminar;
import cl.bcs.APV.ModificarPortafolioTraspaso.PortafoliosModificados;
import cl.bcs.APV.ModificarPortafolioTraspaso.Traspaso;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ReaperturarTraspaso implements JavaCall   {
	
	private final String SQL_SELECT_DETALLE_TRASPASOS = loadResourceToString("cl/bcs/APV/sql/IngresoRecaudacionTraspaso_SelectDetalleTraspasos.sql");

	public static class TraspasoReapertura{
		public Long 		IdTraspaso;
		public Long 		IdContratoSAAPV;
		public String		FolioContratoSAAPV;
		public String 		FechaIngreso;
		public String 		FechaModificacion;
		public Long			IdPersonaCliente;
		public Long 		IdCliente;
		public String 		DocumentoIdentidad;
		public String       IdPerfilComercial;
		
		//Datos Detalle Traspaso
		public Long			IdDetalleTraspaso;
		public Long 		IdPortafolio;
		public String		IdPropositoPortafolio;
		public Long 		IdPlanTipoInversion;
		public String		NombrePlan;
		public Long 		IdProductoAgrupador;
		public String		ProductoAgrupador;
		public Long 		IdRegimenTributario;
		public String		RegimenTributario;
		public String 		CodigoAgrupador;
		public BigDecimal 	MontoPeso;
		public BigDecimal 	MontoUF;
		public BigDecimal 	Porcentaje;
		public BigDecimal	MontoReapertura;
		
		public TraspasoReapertura () {
			this.IdTraspaso = 0L;
			this.IdContratoSAAPV = 0L;
			this.FolioContratoSAAPV = "";
			this.FechaIngreso = "";
			this.FechaModificacion = "";
			this.IdPersonaCliente = 0L;
			this.IdCliente = 0L;
			this.DocumentoIdentidad = "";
			this.IdPerfilComercial = "";
			this.IdDetalleTraspaso = 0L;
			this.IdPortafolio = 0L;
			this.IdPropositoPortafolio = "";
			this.IdPlanTipoInversion = 0L;
			this.NombrePlan = "";
			this.IdProductoAgrupador = 0L;
			this.ProductoAgrupador = "";
			this.IdRegimenTributario = 0L;
			this.RegimenTributario = "";
			this.CodigoAgrupador = "";
			this.MontoPeso = BigDecimal.ZERO;
			this.MontoUF = BigDecimal.ZERO;
			this.Porcentaje = BigDecimal.ZERO;
			this.MontoReapertura = BigDecimal.ZERO;
		}
	}
	
	public static class PortafolioSelect {
		public Long IdPortafolio;
	}

	public static class DatosRecaudacion {
		public String TipoDeposito;
	}
	
	public static class EstadoDetalleTraspasoReapertura {
		public String IdEstado;
	}
	
	public static class ParametrosEntradaServicio {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_Tipo;
		public List<TraspasoReapertura>		p_Traspasos;
		public List<PortafoliosModificados> p_PortafoliosModificados;
		public EntradaRecaudacion			p_Recaudacion;
		
		public ParametrosEntradaServicio(){
			p_Traspasos = new ArrayList<TraspasoReapertura>();	
			p_PortafoliosModificados = new ArrayList<PortafoliosModificados>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Resultado> 		P_OUT_IDRESULTADO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_IDRESULTADO = new ArrayList<Resultado>();
		}		
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
	    JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};


	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();		
		BigDecimal montoReapertura = BigDecimal.ZERO;
		SQL sql = new SQL();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdTraspaso", in.p_Traspasos.get(0).IdTraspaso);
		List<EstadoDetalleTraspasoReapertura> detalleTraspaso = sql.query(con, SQL_SELECT_DETALLE_TRASPASOS, parametros, EstadoDetalleTraspasoReapertura.class);
		List<Long> lstDetalleTraspasoReapertura = new ArrayList<Long>();
		
		if(!in.p_Traspasos.isEmpty() || detalleTraspaso.get(0).IdEstado.compareTo("COM") == 0) {
			List<ModificarPortafolioTraspaso.Traspaso> lstTraspasosReapertura = new ArrayList <ModificarPortafolioTraspaso.Traspaso>();
			//Ingreso Reapertura
			for(TraspasoReapertura fila : in.p_Traspasos ) {
				if(fila.MontoReapertura.compareTo(BigDecimal.ZERO) > 0) {
				
					ModificarPortafolioTraspaso.ParametrosEntradaServicio inModificaTraspaso = new ModificarPortafolioTraspaso.ParametrosEntradaServicio();
					ModificarPortafolioTraspaso.ParametrosSalidaServicio outModificaTraspaso = new ModificarPortafolioTraspaso.ParametrosSalidaServicio();
					List<ModificarPortafolioTraspaso.Traspaso> lstTraspasoMod = new ArrayList<ModificarPortafolioTraspaso.Traspaso>();
								
					//Se envia a agregar en la tabla detalle traspaso
					ModificarPortafolioTraspaso.Traspaso TraspasoMod = new ModificarPortafolioTraspaso.Traspaso();
					TraspasoMod.CodigoAgrupador = fila.CodigoAgrupador;
					TraspasoMod.DocumentoIdentidad = fila.DocumentoIdentidad;
					TraspasoMod.FechaIngreso = fila.FechaIngreso;
					TraspasoMod.FechaModificacion = fila.FechaModificacion;
					TraspasoMod.FolioContratoSAAPV = fila.FolioContratoSAAPV;
					TraspasoMod.IdCliente = fila.IdCliente;
					TraspasoMod.IdContratoSAAPV = fila.IdContratoSAAPV;
					TraspasoMod.IdDetalleTraspaso = 0L;
					TraspasoMod.IdPerfilComercial = fila.IdPerfilComercial;
					
					TraspasoMod.IdPersonaCliente = fila.IdPersonaCliente;
					TraspasoMod.IdPlanTipoInversion = fila.IdPlanTipoInversion;
					TraspasoMod.IdPortafolio = 0L;
					TraspasoMod.IdProductoAgrupador = fila.IdProductoAgrupador;
					TraspasoMod.IdPropositoPortafolio = fila.IdPropositoPortafolio;
					TraspasoMod.IdRegimenTributario = fila.IdRegimenTributario;
					TraspasoMod.IdTraspaso = fila.IdTraspaso;
					TraspasoMod.MontoPeso = fila.MontoReapertura;		//monto reapertura asignado en el monto peso
					TraspasoMod.MontoUF = fila.MontoUF;
					TraspasoMod.NombrePlan = fila.NombrePlan;
					TraspasoMod.Porcentaje = fila.Porcentaje;
					TraspasoMod.ProductoAgrupador = fila.ProductoAgrupador;
					TraspasoMod.RegimenTributario = fila.RegimenTributario;
					TraspasoMod.EsReapertura = "S";
						
					lstTraspasoMod.add(TraspasoMod);
					ModificarPortafolioTraspaso ingresoTraspasoReapertura = new ModificarPortafolioTraspaso();
					inModificaTraspaso.p_IdEmpresa = in.p_IdEmpresa;
					inModificaTraspaso.p_IdUsuario = in.p_IdUsuario;
					inModificaTraspaso.p_IdModulo = in.p_IdModulo;
					inModificaTraspaso.p_Tipo = "R";
					inModificaTraspaso.p_PortafoliosModificados.addAll(in.p_PortafoliosModificados);
					inModificaTraspaso.p_Traspasos = lstTraspasoMod;
					outModificaTraspaso = ingresoTraspasoReapertura.logica(con, inModificaTraspaso);

					if (outModificaTraspaso.P_OUT_ERRORESCONTADOR < 1) {
						lstDetalleTraspasoReapertura.add(outModificaTraspaso.P_OUT_IDRESULTADO.get(0));	
					}					
					
					salida.P_OUT_MENSAJES.add(outModificaTraspaso.P_OUT_MENSAJES.get(0));
					salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + outModificaTraspaso.P_OUT_ERRORESCONTADOR;
					montoReapertura = montoReapertura.add(fila.MontoReapertura);
				}
			}
			if(salida.P_OUT_ERRORESCONTADOR > 0) {
				msg = new MensajeSalida();
				msg.id = 0L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_MODIFICA_ESTADO";
				msg.descripcion = "Problema al reaperturar el Traspaso.";
				salida.P_OUT_MENSAJES.add(msg);
			}
			else {
				IngresoRecaudacionTraspaso.EntradaIngresoRecaudacionTraspaso in_recaudacionReapertura = new IngresoRecaudacionTraspaso.EntradaIngresoRecaudacionTraspaso();
				IngresoRecaudacionTraspaso.SalidaIngresoRecaudacionTraspaso out_recaudacionReapertura = new IngresoRecaudacionTraspaso.SalidaIngresoRecaudacionTraspaso();
				in_recaudacionReapertura.p_IdEmpresa = in.p_IdEmpresa;
				in_recaudacionReapertura.p_IdUsuario = in.p_IdUsuario;
				in_recaudacionReapertura.p_IdModulo = in.p_IdModulo;
				in_recaudacionReapertura.p_Recaudacion = in.p_Recaudacion;
				in_recaudacionReapertura.p_TipoTraspaso = "R";
				
				List<IngresoRecaudacionTraspaso.EntradaTraspaso> lstIdTraspaso = new ArrayList<IngresoRecaudacionTraspaso.EntradaTraspaso>();
				IngresoRecaudacionTraspaso.EntradaTraspaso inTraspaso = new IngresoRecaudacionTraspaso.EntradaTraspaso();
				inTraspaso.IdTraspaso = in.p_Traspasos.get(0).IdTraspaso;
				inTraspaso.MontoTraspaso = montoReapertura;
				inTraspaso.IdDetalleTraspaso.addAll(lstDetalleTraspasoReapertura);
				lstIdTraspaso.add(inTraspaso);	
				in_recaudacionReapertura.p_Traspasos = lstIdTraspaso;			
				
				IngresoRecaudacionTraspaso recaudacionTraspasoReapertura = new IngresoRecaudacionTraspaso();				
				out_recaudacionReapertura = recaudacionTraspasoReapertura.logica(con, in_recaudacionReapertura);
							
				if(out_recaudacionReapertura.P_OUT_ERRORESCONTADOR == 0) {
					msg = new MensajeSalida();
					msg.id = 0L;
					msg.tipo = "OK";
					msg.codigo = "REAPERTURAR_TRASPASO_OK";
					msg.descripcion = "Se crea reaperturacion correctamente.";
					salida.P_OUT_MENSAJES.add(msg);
					for(MensajeSalida msgRec : out_recaudacionReapertura.P_OUT_MENSAJES) {
						salida.P_OUT_MENSAJES.add(msgRec);
					}
					salida.P_OUT_IDRESULTADO.addAll(out_recaudacionReapertura.P_OUT_IDRESULTADO); 
				}
				else {

					msg = new MensajeSalida();
					msg.id = 0L;
					msg.tipo = "ERROR";
					msg.codigo = "ERROR_REAPERTURAR_TRASPASO";
					msg.descripcion = "Problema al reaperturar el Traspaso.";
					salida.P_OUT_MENSAJES.add(msg);
					salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + out_recaudacionReapertura.P_OUT_ERRORESCONTADOR;
					salida.P_OUT_IDRESULTADO.addAll(out_recaudacionReapertura.P_OUT_IDRESULTADO); 
				}	
			}
		}
		else {
			msg = new MensajeSalida();
			msg.id = 0L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR_MODIFICO_PORTAFOLIO_CONTRATO";
			msg.descripcion = "Problema al reaperturar el Traspaso.";
			salida.P_OUT_MENSAJES.add(msg);
			salida.P_OUT_ERRORESCONTADOR++;
		}

		return salida;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"I\",\"p_Fecha\":\"2023-09-14\",\"p_Traspasos\":[{\"IdTraspaso\":2991745,\"IdContratoSAAPV\":1013906,\"FechaIngreso\":\"2023-09-14\",\"FechaModificacion\":\"0001-01-01\",\"IdCliente\":85053,\"IdPersonaCliente\":117943,\"DocumentoIdentidad\":\"19263099-1\",\"IdPerfilComercial\":\"APV INV ACTIVA_ARRIESGADO\",\"IdDetalleTraspaso\":1930078,\"IdPortafolio\":487485,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":1,\"RegimenTributario\":\"APV-AN\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":370000,\"MontoUF\":0,\"Porcentaje\":0,\"MontoReapertura\":100000},{\"IdTraspaso\":2991745,\"IdContratoSAAPV\":1013906,\"FechaIngreso\":\"2023-09-14\",\"FechaModificacion\":\"0001-01-01\",\"IdCliente\":85053,\"IdPersonaCliente\":117943,\"DocumentoIdentidad\":\"19263099-1\",\"IdPerfilComercial\":\"APV INV ACTIVA_ARRIESGADO\",\"IdPortafolio\":0,\"IdPropositoPortafolio\":\"PROPOSITOAPV\",\"IdPlanTipoInversion\":2,\"NombrePlan\":\"I. ACTIVO\",\"IdProductoAgrupador\":2,\"ProductoAgrupador\":\"APV Acciones\",\"IdRegimenTributario\":6,\"RegimenTributario\":\"DC-R\",\"CodigoAgrupador\":\"GRUPO 2\",\"MontoPeso\":0,\"MontoUF\":0,\"Porcentaje\":0,\"MontoReapertura\":310000}],\"p_Recaudacion\":{\"CuentaCorriente\":\"4355700\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8}}";
				
				ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				ReaperturarTraspaso demo = new ReaperturarTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
	 				con.rollback();
	 				//con.commit();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					SQL.exec(con, "DROP TABLE #RegistroAtributo");
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
		
}
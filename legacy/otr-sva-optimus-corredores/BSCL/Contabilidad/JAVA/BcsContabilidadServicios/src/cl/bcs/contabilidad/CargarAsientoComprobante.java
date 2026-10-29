package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.AgregarCuentasPorMetadata.Evento;
import cl.bcs.contabilidad.AgregarCuentasPorMetadata.InAgregarCuentasPorMetadata;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarAsientoComprobante implements JavaCall{
	private final Log log = new Log("CON.CargarAsientoComprobante");
	
	private final String SQL_SELECT_COMPROBANTE = loadResourceToString("cl/bcs/contabilidad/sql/CargarAsientoComprobante_SelectCombrobante.sql");
	private final String SQL_ID_DATOS_CUENTA_CONTABLE = loadResourceToString("cl/bcs/contabilidad/sql/CargarAsientoComprobante_SelectIdCuentaContable.sql");
	private final String SQL_SELECT_METADATA = loadResourceToString("cl/bcs/contabilidad/sql/CargarAsientoComprobante_SelectMetadata.sql");
	private final String SQL_INSERT_CONTABILIDAD_DEFINICION_ASIENTO = loadResourceToString("cl/bcs/contabilidad/sql/CargarAsientoComprobante_InsertContabilidadDefinicionAsiento.sql");
	private final String SQL_INSERT_CONTABILIDAD_DEFINICION_TIPO_EVENTO = loadResourceToString("cl/bcs/contabilidad/sql/CargarAsientoComprobante_InsertContabilidadDefinicionTipoEvento.sql");
	private final String SQL_INSERT_CONTABILIDAD_TIPO_ASIENTO_CONDICION = loadResourceToString("cl/bcs/contabilidad/sql/CargarAsientoComprobante_InsertContabilidadTipoAsientoCondicion.sql");
	
	
	public static class InCargarAsientoComprobante{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public Asientos[] p_Asientos;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutCargarAsientoComprobante{
		public String 	P_OUT_MENSAJE;
		public Long 	P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public OutCargarAsientoComprobante(){
			this.P_OUT_MENSAJE = "";
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asientos{
		public String CACT_CONDICIONES;
		public String CACT_CUENTAOMETADATA;
		public String CACT_DEBEHABER;
		public String CACT_EVENTOS;
		public String CACT_FORMATO;
		public String CACT_NEMOTECNICO;
		public String MENSAJEVALIDACION;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Comprobante{
		public Long IdTipoContabilidad;
		public String Nemo;
		public Long Ejercicio;
	}
	
	public static class CuentasContableInsert{
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdCuentaContable;
		public Long Ejercicio;
		public Long Orden;
		public String DebeHaber;
		public String TipoEvento;
		public String Condicionado;
		public String OmiteCondicionCmbte;
		public String Estado;
		public String Formato;
		public Long IdMetadataCuentaContable;
	}
	
	public static class EventoInsert{
		public String IdTipoEvento;
		public Long IdDefinicionAsiento;
		public String Operador;
		public String Estado;
	}
	
	public static class CondicionInsert{
		public Long IdDefinicionAsiento;
		public Long IdTipoContabilidad;
		public String IdTipoDatoEvento;
		public String ValorVarchar;
		public BigDecimal ValorDecimal;
		public String Condicion;
		public String Conector;
		public String TipoCondicion;
		public String Estado;
		public String OmiteCondicionCmbte;
	}
	
	
	
	public static class Id{
		public Long Id;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCargarAsientoComprobante in=gson.fromJson(jin.toString() ,InCargarAsientoComprobante.class);
		OutCargarAsientoComprobante out= logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  OutCargarAsientoComprobante logica(Connection con, InCargarAsientoComprobante entrada) throws Exception {
		OutCargarAsientoComprobante out = new OutCargarAsientoComprobante();
		out.P_OUT_ERRORESCONTADOR = 0L;
		log.write("Inicio");
		try{
			
			SQL sql=new SQL();
			HashMap<String,Object> params = new HashMap<String,Object>();
			List<String> nemos = new ArrayList<String>();
			List<Comprobante> comprobantes = new ArrayList<Comprobante>();
			for(Asientos asiento : entrada.p_Asientos){
				if("OK".equalsIgnoreCase(asiento.MENSAJEVALIDACION)){
					nemos.add(asiento.CACT_NEMOTECNICO);
				}
			}
			if(nemos.size() > 0){
				params.put("nemos", nemos);
				params.put("IdEmpresa", entrada.p_IdEmpresa);
				comprobantes = sql.query(con, SQL_SELECT_COMPROBANTE, params, Comprobante.class);
			}
			
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_MENSAJE = "UI_EXITO";
			List<CuentasContableInsert> cuentasContableInsert = new ArrayList<CuentasContableInsert>();
			List<EventoInsert> eventosInsert = new ArrayList<EventoInsert>();
			List<CondicionInsert> condicionesInsert = new ArrayList<CondicionInsert>();
			HashMap<String,Long> orden = new HashMap<String,Long>();
			for(Asientos asiento : entrada.p_Asientos){
				if("OK".equalsIgnoreCase(asiento.MENSAJEVALIDACION) && asiento.CACT_CUENTAOMETADATA != null && !asiento.CACT_CUENTAOMETADATA.isEmpty()){
					if("&".equalsIgnoreCase(asiento.CACT_CUENTAOMETADATA.substring(0, 1))){
						if(asiento.CACT_CUENTAOMETADATA.length() > 2) {
							String metadata = asiento.CACT_CUENTAOMETADATA.substring(1, asiento.CACT_CUENTAOMETADATA.length()-1);
							params.clear();
							params.put("CodigoMetadata", metadata);
							log.write("Metadata::", metadata);
							List<Id> selectId = sql.query(con,SQL_SELECT_METADATA,params,Id.class);
							if(selectId.size() == 0) {
								HashMap<String,String> hMetadata = new HashMap<String,String>();
								hMetadata.put("IdMetadata", metadata);
								out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTE_METADATA",hMetadata,con,entrada.p_IdEmpresa));
								log.write(metadata + " no existe");
								continue;
							}
							AgregarCuentasPorMetadata agregarCuentasPorMetadata = new AgregarCuentasPorMetadata();
							InAgregarCuentasPorMetadata inAgregarCuentasPorMetadata = new InAgregarCuentasPorMetadata();
							inAgregarCuentasPorMetadata.p_IdEmpresa = entrada.p_IdEmpresa;
							inAgregarCuentasPorMetadata.p_IdUsuario = entrada.p_IdUsuario;
							inAgregarCuentasPorMetadata.p_IdModulo = entrada.p_IdModulo;
							inAgregarCuentasPorMetadata.p_IdMetadataCuentaContable = selectId.get(0).Id;
							inAgregarCuentasPorMetadata.p_Eventos = new ArrayList<Evento>();
							inAgregarCuentasPorMetadata.p_DebeHaber = asiento.CACT_DEBEHABER;
							for(Comprobante comprobante : comprobantes){
								if(asiento.CACT_NEMOTECNICO.equalsIgnoreCase(comprobante.Nemo)){
									inAgregarCuentasPorMetadata.p_IdTipoContabilidad = comprobante.IdTipoContabilidad;
									inAgregarCuentasPorMetadata.p_Ejercicio = String.valueOf(comprobante.Ejercicio);
									break;
								}
							}
							if(inAgregarCuentasPorMetadata.p_IdTipoContabilidad == null){
								HashMap<String,String> tipoContabilidad = new HashMap<String,String>();
								tipoContabilidad.put("IdTipoContabilidad", asiento.CACT_NEMOTECNICO);
								out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTE_CONTABILIDAD",tipoContabilidad,con,entrada.p_IdEmpresa));
								log.write(asiento.CACT_NEMOTECNICO + " no existe");
								continue;
							}
							inAgregarCuentasPorMetadata.p_Formato = asiento.CACT_FORMATO;
							String[] eventos = asiento.CACT_EVENTOS.split("\\|");
							HashMap<String,String> eventosExistentes = new HashMap<String,String>();
							for(String evento : eventos){
								if(evento != null && !evento.isEmpty()){
									Evento eventoSP = new Evento();
									eventoSP.IdTipoEvento = evento.substring(1, evento.length()).trim();
									eventoSP.Operador = evento.substring(0,1);
									if(eventosExistentes.get(eventoSP.IdTipoEvento) == null){
										eventosExistentes.put(eventoSP.IdTipoEvento, eventoSP.IdTipoEvento);
										inAgregarCuentasPorMetadata.p_Eventos.add(eventoSP);
									}else{
										HashMap<String,String> eventoContabilidad = new HashMap<String,String>();
										eventoContabilidad.put("AsientoEventoCuentaContable", asiento.CACT_NEMOTECNICO + "-" + metadata + "-" + eventoSP.IdTipoEvento);
										out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"YA_EXISTE_EVENTO_ASIENTO",eventoContabilidad,con,entrada.p_IdEmpresa));
										log.write(asiento.CACT_NEMOTECNICO + "-" + metadata + "-" + eventoSP.IdTipoEvento + " ya existe");
									}
								}			
							}
							agregarCuentasPorMetadata.logica(con, inAgregarCuentasPorMetadata);
						}
					}else{
						CuentasContableInsert cuentaContable = new CuentasContableInsert();
						params.clear();
						List<Id> selectId = sql.query(con,SQL_ID_DATOS_CUENTA_CONTABLE,params,Id.class);
						cuentaContable.IdDefinicionAsiento = selectId.get(0).Id;
						for(Comprobante comprobante : comprobantes){
							if(asiento.CACT_NEMOTECNICO.equalsIgnoreCase(comprobante.Nemo)){
								cuentaContable.IdTipoContabilidad = comprobante.IdTipoContabilidad;
								cuentaContable.Ejercicio = comprobante.Ejercicio;
								break;
							}
						}
						if(cuentaContable.IdTipoContabilidad == null){
							HashMap<String,String> tipoContabilidad = new HashMap<String,String>();
							tipoContabilidad.put("IdTipoContabilidad", asiento.CACT_NEMOTECNICO);
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"NO_EXISTE_CONTABILIDAD",tipoContabilidad,con,entrada.p_IdEmpresa));
							log.write(asiento.CACT_NEMOTECNICO + " no existe");
							continue;
						}
						cuentaContable.IdCuentaContable = asiento.CACT_CUENTAOMETADATA;
						if(orden.get(asiento.CACT_NEMOTECNICO) == null){
							orden.put(asiento.CACT_NEMOTECNICO, 1L);
						}else{
							orden.put(asiento.CACT_NEMOTECNICO, orden.get(asiento.CACT_NEMOTECNICO) + 1);
						}
						cuentaContable.Orden = orden.get(asiento.CACT_NEMOTECNICO);
						cuentaContable.DebeHaber = asiento.CACT_DEBEHABER;
						cuentaContable.TipoEvento = asiento.CACT_EVENTOS != null && asiento.CACT_EVENTOS.split("\\|").length > 1 ? "C" : "S";
						cuentaContable.Condicionado = "S";
						cuentaContable.OmiteCondicionCmbte = "N";
						cuentaContable.Estado = "V";
						cuentaContable.Formato = asiento.CACT_FORMATO;
						cuentasContableInsert.add(cuentaContable);
						String[] eventos = asiento.CACT_EVENTOS != null ? asiento.CACT_EVENTOS.split("\\|") : new String[0];
						HashMap<String,String> eventosExistentes = new HashMap<String,String>();
						for(String evento : eventos){
							if(evento != null && !evento.isEmpty()){
								EventoInsert eventoInsert = new EventoInsert();
								eventoInsert.IdDefinicionAsiento = cuentaContable.IdDefinicionAsiento;
								eventoInsert.IdTipoEvento = evento.substring(1, evento.length()).trim();
								eventoInsert.Operador = evento.substring(0,1);
								eventoInsert.Estado = "V";
								if(eventosExistentes.get(eventoInsert.IdTipoEvento) == null){
									eventosExistentes.put(eventoInsert.IdTipoEvento, eventoInsert.IdTipoEvento);
									eventosInsert.add(eventoInsert);
								}else{
									HashMap<String,String> eventoContabilidad = new HashMap<String,String>();
									eventoContabilidad.put("AsientoEventoCuentaContable", asiento.CACT_NEMOTECNICO + "-" + cuentaContable.IdCuentaContable + "-" + eventoInsert.IdTipoEvento);
									out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"YA_EXISTE_EVENTO_ASIENTO",eventoContabilidad,con,entrada.p_IdEmpresa));
									log.write(asiento.CACT_NEMOTECNICO + "-" + cuentaContable.IdCuentaContable + "-" + eventoInsert.IdTipoEvento + " ya existe");
								}
								
							}
						}
						String[] condiciones = asiento.CACT_CONDICIONES != null ? asiento.CACT_CONDICIONES.split("\\|") : new String[0];
						for(String condicion : condiciones){
							CondicionInsert condicionInsert = new CondicionInsert();
							String[] arrayCondicion = condicion.split("#");
							if(arrayCondicion.length == 3){
								condicionInsert.IdDefinicionAsiento = cuentaContable.IdDefinicionAsiento;
								condicionInsert.IdTipoContabilidad = cuentaContable.IdTipoContabilidad;
								condicionInsert.IdTipoDatoEvento = arrayCondicion[0].trim();
								condicionInsert.ValorVarchar = arrayCondicion[2].trim();
								try{
									condicionInsert.ValorDecimal = new BigDecimal(arrayCondicion[2]);
								}catch(Exception e){
									condicionInsert.ValorDecimal = BigDecimal.ZERO;
								}
								condicionInsert.Condicion = arrayCondicion[1];
								condicionInsert.Conector = "AND";
								condicionInsert.TipoCondicion = "A";
								condicionInsert.Estado = "V";
								condicionInsert.OmiteCondicionCmbte = "";
								if("=".equals(condicionInsert.Condicion) || ">".equals(condicionInsert.Condicion) || "<".equals(condicionInsert.Condicion) || "<>".equals(condicionInsert.Condicion)){
									condicionesInsert.add(condicionInsert);
								}else{
									HashMap<String,String> hcondicion = new HashMap<String,String>();
									hcondicion.put("Condicion", condicionInsert.Condicion);
									hcondicion.put("IdTipoContabilidad", asiento.CACT_NEMOTECNICO);
									hcondicion.put("Cuenta", asiento.CACT_CUENTAOMETADATA);
									out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"CONDICION_NO_VALIDA_DETERMINANTE_ASIENTO",hcondicion,con,entrada.p_IdEmpresa));
									log.write(condicionInsert.Condicion + " no valido");
								}
							}
							
						}
					}
				}
			}
			if(cuentasContableInsert.size() > 0){
				sql.batch(con, SQL_INSERT_CONTABILIDAD_DEFINICION_ASIENTO, cuentasContableInsert, CuentasContableInsert.class);
				if(eventosInsert.size() > 0){
					sql.batch(con, SQL_INSERT_CONTABILIDAD_DEFINICION_TIPO_EVENTO, eventosInsert, EventoInsert.class);
				}
				if(condicionesInsert.size() > 0){
					sql.batch(con, SQL_INSERT_CONTABILIDAD_TIPO_ASIENTO_CONDICION, condicionesInsert, CondicionInsert.class);
				}
			}
			
		} catch(Exception e){
			log.write("Error",e);
			throw e;
		}
		log.write("Termino");
		return out;
	}
	
	public static void main (String [] args) throws Exception {
  		try (Connection con = PlataformaUtils.getMBConnection()){
  				Gson gson = new GsonBuilder().create();
  				String json = "";
  				InCargarAsientoComprobante entrada = gson.fromJson(json, InCargarAsientoComprobante.class);
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				CargarAsientoComprobante demo= new CargarAsientoComprobante();
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
  			}
	}

}

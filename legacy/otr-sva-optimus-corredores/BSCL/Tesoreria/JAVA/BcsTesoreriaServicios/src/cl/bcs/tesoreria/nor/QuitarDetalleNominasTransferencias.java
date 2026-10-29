package cl.bcs.tesoreria.nor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.GirarComprobanteIngreso.Comprobantes;
import cl.bcs.tesoreria.RequisitosGirarComprobanteIngreso.Validar;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class QuitarDetalleNominasTransferencias implements JavaCall{
	
	private Logger log = LogManager.getLogger(QuitarDetalleNominasTransferencias.class);
	
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #QuitarDetalle_p_RegistrosAProcesar (\"IdComprobante\" BIGINT,\"IdNominaTransferencia\" BIGINT)";
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #QuitarDetalle_p_RegistrosAProcesar (\"IdComprobante\",\"IdNominaTransferencia\") VALUES (:IdComprobante,:IdNominaTransferencia)";
	private static final String DROP_TEMPORARY_TABLE = "DROP TABLE #QuitarDetalle_p_RegistrosAProcesar";
    private  final String SQL_UPDATE_QUITAR_DETALLE_COMPROBANTE_FORMA_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobanteFormaPago.sql");
    private  final String SQL_UPDATE_QUITAR_DETALLE_COMPROBANTE = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobante.sql");
    private  final String SQL_UPDATE_QUITAR_DETALLE_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateQuitarDetalleComprobanteDetalle.sql");
    private  final String SQL_DELETE_QUITAR_DETALLE_NOMINA_TRANSFERENCIA_DETALLE = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_DeleteQuitarDetalleNominaTransferenciaDetalle.sql");
    private  final String SQL_UPDATE_RECALCULAR_MONTO_NOMINA_TRANSFERENCIA = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateRecalcularMontoNominaTransferencia.sql");
    private  final String SQL_UPDATE_SET_MONTO_COMPROBANTE_PARTIDA_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateSetMontoComprobantePartidaEgreso.sql");
    private  final String SQL_UPDATE_SET_MONTO_COMPROBANTE_DETALLE_PARTIDA_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateSetMontoComprobanteDetallePartidaEgreso.sql");
    private  final String SQL_UPDATE_SET_MONTO_COMPROBANTE_FORMA_PAGO_PARTIDA_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_UpdateSetMontoComprobanteFormaPagoPartidaEgreso.sql");

	private final String SQL_SELECT_ANULAR_CARGO_ABONO = loadResourceToString("cl/bcs/tesoreria/sql/QuitarDetalleNominasTransferencias_SelectAnularCargosAbono.sql"); 
	
	public static class RegistrosQuitarDetalle extends BcsPojo {
	    public Long IdComprobante;
	    public Long IdNominaTransferencia;
	}
	public static class IdNominaTransferencia extends BcsPojo {
	    public Long IdNominaTransferencia;
	}

    public static class EntradaQuitarDetalle{
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        public List<RegistrosQuitarDetalle> p_RegistrosAProcesar;
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ResultadoQuitardetalle{
        public String Campo;
        public Long Id;
        public String Servicio;
        public String Resultado;
    }
	


    public static class SalidaQuitarDetalle extends SalidaServicios{
        public List<ResultadoQuitardetalle> p_out_ResultadoProceso;
        public SalidaQuitarDetalle() {
            super();
            this.p_out_ResultadoProceso = new ArrayList<>();
        }
        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }


    @Trace
    public SalidaQuitarDetalle logica(Connection conn, EntradaQuitarDetalle in) {
        log.info("INICIO SERVICIO");
        SalidaQuitarDetalle out = new SalidaQuitarDetalle();
        List<MensajeSalida> mensajesSalida = new ArrayList<>();
        SQL sql = new SQL();
        try {
            if(in.p_RegistrosAProcesar.isEmpty()) {
                mensajesSalida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_VIENEN_MOVIMIENTOS", "No vienen movimientos"));
                out.parseMensajeSalida(mensajesSalida);
                return out;
            }
            
            SQL.exec(conn, CREATE_TEMPORARY_TABLE);
            sql.batch(conn, SQL_INSERT_REGISTROS, in.p_RegistrosAProcesar, RegistrosQuitarDetalle.class);

                Set<Long> movimientosProcesar = new HashSet<>();
                in.p_RegistrosAProcesar.forEach(reg -> {
                    movimientosProcesar.add(reg.IdComprobante);
                    movimientosProcesar.add(reg.IdNominaTransferencia);
                });

                
                if(!movimientosProcesar.isEmpty()) {
                    log.debug("HAY MOVIMIENTOS, CONTINUAR");
                    
					List<CargoAbono_Entrada> cargosAbonoAnular = sql.query(conn, SQL_SELECT_ANULAR_CARGO_ABONO, new HashMap<>(), CargoAbono_Entrada.class);
					if(!cargosAbonoAnular.isEmpty()) {
						AnularCargoAbono_Entrada entradaAnularCargoAbono = new AnularCargoAbono_Entrada();
						entradaAnularCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
						entradaAnularCargoAbono.p_IdUsuario = in.p_IdUsuario;
						entradaAnularCargoAbono.p_IdModulo = in.p_IdModulo;
						entradaAnularCargoAbono.p_FechaMovimiento = in.p_FechaMovimiento;
						entradaAnularCargoAbono.p_simularOperacion = "N";
						entradaAnularCargoAbono.p_UsaReglasNegocio = "S";
						entradaAnularCargoAbono.p_EjecutaAutorizacion = "N";
						entradaAnularCargoAbono.p_OmiteRegistroSinAutorizar = "N";
						entradaAnularCargoAbono.p_EsAnulacionManual = "N";
						entradaAnularCargoAbono.p_CargoAbonoAAnular = cargosAbonoAnular;
						AnularCargoAbono anularCargoAbono = new AnularCargoAbono();
						AnularCargoAbono_Salida salidaAnularCargoAbono = anularCargoAbono.logica(conn, entradaAnularCargoAbono);
						out.P_OUT_ERRORESCONTADOR+= salidaAnularCargoAbono.P_OUT_ERRORESCONTADOR;
					}
                    
                    sql.batch(conn, SQL_DELETE_QUITAR_DETALLE_NOMINA_TRANSFERENCIA_DETALLE, in.p_RegistrosAProcesar, RegistrosQuitarDetalle.class);
                    sql.batch(conn, SQL_UPDATE_QUITAR_DETALLE_COMPROBANTE_FORMA_PAGO, in.p_RegistrosAProcesar, RegistrosQuitarDetalle.class);
                    sql.batch(conn, SQL_UPDATE_QUITAR_DETALLE_COMPROBANTE, in.p_RegistrosAProcesar, RegistrosQuitarDetalle.class);
                    sql.batch(conn, SQL_UPDATE_QUITAR_DETALLE_COMPROBANTE_DETALLE, in.p_RegistrosAProcesar, RegistrosQuitarDetalle.class);
        			List<IdNominaTransferencia> ListNominaTransferencia = new ArrayList<IdNominaTransferencia>();
        			IdNominaTransferencia NominaTransferencia = new IdNominaTransferencia();
        			NominaTransferencia.IdNominaTransferencia = in.p_RegistrosAProcesar.get(0).IdNominaTransferencia;
        			ListNominaTransferencia.add(NominaTransferencia);
                    sql.batch(conn, SQL_UPDATE_RECALCULAR_MONTO_NOMINA_TRANSFERENCIA, ListNominaTransferencia, IdNominaTransferencia.class);
                    sql.batch(conn, SQL_UPDATE_SET_MONTO_COMPROBANTE_PARTIDA_EGRESO, ListNominaTransferencia, IdNominaTransferencia.class);
                    sql.batch(conn, SQL_UPDATE_SET_MONTO_COMPROBANTE_DETALLE_PARTIDA_EGRESO, ListNominaTransferencia, IdNominaTransferencia.class);
                    sql.batch(conn, SQL_UPDATE_SET_MONTO_COMPROBANTE_FORMA_PAGO_PARTIDA_EGRESO, ListNominaTransferencia, IdNominaTransferencia.class);


                }else {
                    log.debug("NO HAY MOVIMIENTOS, TERMINAR");
                }
                
    			SQL.exec(conn, DROP_TEMPORARY_TABLE);

            	}catch (Exception e) {
                log.debug(e);
                e.printStackTrace();
            }
        	
            log.info("TERMINO SERVICIO");
            return out;
    }    

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaQuitarDetalle in = gson.fromJson(jin.toString(), EntradaQuitarDetalle.class);
		SalidaQuitarDetalle out = logica(conn, in);
		return new JsonParser().parse(gson.toJson(out)).getAsJsonObject();
	}
	
	public static void main(String[] args) {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			QuitarDetalleNominasTransferencias s = new QuitarDetalleNominasTransferencias();
			Gson gson = Utils.getGsonBuilder();
            String jsonComp = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2023-10-23\",\"p_RegistrosAProcesar\":[{\"IdComprobante\":1066459,\"IdNominaTransferencia\":90},{\"IdComprobante\":1066458,\"IdNominaTransferencia\":90}]}\r\n"
            		+ "";
			EntradaQuitarDetalle Objin = gson.fromJson(jsonComp, EntradaQuitarDetalle.class);
			SalidaQuitarDetalle salida = s.logica(conn, Objin );
			System.out.println(salida);
			//conn.rollback();
			conn.commit();
			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}
}    


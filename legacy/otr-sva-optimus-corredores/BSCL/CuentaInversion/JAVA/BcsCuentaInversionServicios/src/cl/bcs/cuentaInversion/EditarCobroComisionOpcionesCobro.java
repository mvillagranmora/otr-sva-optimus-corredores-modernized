package cl.bcs.cuentaInversion;

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

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EditarCobroComisionOpcionesCobro implements JavaCall {
    private static final Logger logger = LogManager.getLogger(EditarCobroComisionOpcionesCobro.class);

    private final String SQL_UPDATE_OPCION_COBRO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_UpdateRegistro.sql");
    private final String SQL_CREAR_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_CrearTablaPaso.sql");
    private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_InsertTablaPaso.sql");
    private final String SQL_SELECT_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_SelectOpcionesCobroTablaPaso.sql");
    private final String SQL_DROP_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_DropTablaPaso.sql");
    
    public static class EntradaEditarOpcionCobroComision extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<OpcionesCobro> p_Opciones;
        
        public EntradaEditarOpcionCobroComision() {
            this.p_IdEmpresa = 0L;
            this.p_IdUsuario = 0L;
            this.p_IdModulo = "CTA";
            this.p_UsaReglasNegocio = "N";
            this.p_Opciones = new ArrayList<OpcionesCobro>();

        }

    }
    
    public static class OpcionesCobro extends BcsPojo{
        public Long IdOpcionCobro;
        public Long OpcionCobro;
        public String PortafolioAPV;
        public String Mercado;
        public BigDecimal PorcentajeCobro;
        public String Estado;
        public Date FechaVigencia;
        public String FechaModificacion;
        public String FechaIngreso;
        
        public OpcionesCobro() {
            this.IdOpcionCobro = 0L;
            this.OpcionCobro = 0L;
            this.PortafolioAPV = "N";
            this.Mercado = "";
            this.PorcentajeCobro = BigDecimal.ZERO;
            this.Estado = "";
            this.FechaVigencia = Fecha.toDate("0001-01-01");
            this.FechaModificacion = "0001-01-01";
            this.FechaIngreso = "0001-01-01";
        }

    }

    public static class SalidaEditarOpcionCobroComision extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        
        public SalidaEditarOpcionCobroComision () {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_ADVERTENCIASCONTADOR = 0L;
        }
    }
    
    public static class BuscaOpciones {
        public Long Existe;
        
        public BuscaOpciones() {
            this.Existe = 0L;
        }
    }

    public SalidaEditarOpcionCobroComision logica(Connection conn, EntradaEditarOpcionCobroComision in) throws SQLException, Exception {
        logger.info("INICIO::EditarCobroComisionOpcionesCobro");
        long inicio = System.currentTimeMillis();
        
        SQL sql=new SQL();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA","PLA.FechaSistema");
        SalidaEditarOpcionCobroComision Salida = new SalidaEditarOpcionCobroComision();
        
        if(in.p_Opciones.isEmpty()) {
            Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_CONCEPTOS",new HashMap<String, String>(),conn,in.p_IdEmpresa));
            Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
            return Salida;
        }


        for(OpcionesCobro opcion : in.p_Opciones) {
            opcion.FechaIngreso = fechaSistema;
            if(opcion.PorcentajeCobro.compareTo(new BigDecimal("0.0")) < 0  || opcion.PorcentajeCobro.compareTo(new BigDecimal("1.0")) > 0) {
                HashMap<String, String> paramMensaje = new HashMap<String, String>(3);
                paramMensaje.put("Opcion", opcion.OpcionCobro.toString());
                paramMensaje.put("PortafolioAPV", opcion.PortafolioAPV);
                paramMensaje.put("IdClasificacionTipoInstrumento", opcion.Mercado);
                        
                logger.info("INFO::CrearCobroComisionOpcionesCobro :: Valor del PorcentajeCobro debe ser > 1 y < 0 en la opcion :: "+opcion.OpcionCobro+" "+opcion.PortafolioAPV+" "+opcion.Mercado);
                Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_PORCENTAJE_NO_VALIDO",paramMensaje,conn,in.p_IdEmpresa));
            }   
        }
        
        Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
        
        if(Salida.P_OUT_ERRORESCONTADOR > 0) {
            return Salida;
        }
        
        try {
            SQL.exec(conn, SQL_CREAR_TABLA_PASO);
            sql.batch(conn, SQL_INSERT_TABLA_PASO, in.p_Opciones, OpcionesCobro.class);
            List<OpcionesCobro> opcionesCobro = sql.query(conn, SQL_SELECT_TABLA_PASO, new HashMap<String, Object>(), OpcionesCobro.class);
            List<OpcionesCobro> opcionesCobroNoExistentes = opcionesCobro.stream().filter(op -> op.IdOpcionCobro.compareTo(0L) == 0).collect(Collectors.toList());
            HashMap<String, String> paramMensaje = new HashMap<String, String>(4);
            
            for( OpcionesCobro op : opcionesCobroNoExistentes) {
                paramMensaje.put("IdOpcion", op.IdOpcionCobro.toString());
                
                logger.info("INFO::EditarCobroComisionOpcionesCobro :: No existe opción requerida ");
                Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_OPCION_NO_ENCONTRADA",paramMensaje,conn,in.p_IdEmpresa));
                paramMensaje.clear();
            }
            
            Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
            
            if(Salida.P_OUT_ERRORESCONTADOR > 0) {
                return Salida;
            }
            
            List<OpcionesCobro> opcionesCobroExistentes = opcionesCobro.stream().filter(op -> op.IdOpcionCobro.compareTo(0L) > 0).collect(Collectors.toList());
            if(!opcionesCobroExistentes.isEmpty()) {
                logger.info("INFO::EditarCobroComisionOpcionesCobro Update de nueva opcion");
                try {
                    sql.batch(conn, SQL_UPDATE_OPCION_COBRO, opcionesCobroExistentes, OpcionesCobro.class);
                }catch(Exception error) {
                    logger.info("INFO::EditarCobroComisionOpcionesCobro :: Opcion Duplicada ");
                    
                    Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_ADVERTENCIA_OPCION_DUPLICADA",paramMensaje,conn,in.p_IdEmpresa));
                    Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
                    Salida.P_OUT_ADVERTENCIASCONTADOR++;
                }
                logger.info("INFO::EditarCobroComisionOpcionesCobro Opcion editada Exitosamente");
            }
        } catch (Error e) {
            logger.info("INFO::CrearCobroComisionOpcionesCobro :: Operacion Con errores: " + e);
        }finally {
            SQL.exec(conn, SQL_DROP_TABLA_PASO);
        }

        Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::EditarCobroComisionOpcionesCobro.", fin - inicio);
        
        return Salida;
    }
    
    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        Gson gson = getGsonBuilder();
        EntradaEditarOpcionCobroComision jin=gson.fromJson(in.toString() , EntradaEditarOpcionCobroComision.class);
        SalidaEditarOpcionCobroComision out = logica(con,jin);
        JsonParser parser = new JsonParser();
        JsonObject dataout  = parser.parse(gson.toJson(out)).getAsJsonObject();
        
        return dataout;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("== TEST ==");
        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"N\",\"p_Opciones\":[{\"IdOpcionCobro\":140,\"OpcionCobro\":132,\"PortafolioAPV\":\"S\",\"Mercado\":\"CI\",\"PorcentajeCobro\":0.0145,\"Estado\":\"ACT\",\"FechaVigencia\":\"2024-05-02T04:00:00.000Z\"}]}";
            EntradaEditarOpcionCobroComision Objin = gson.fromJson(json, EntradaEditarOpcionCobroComision.class);
            EditarCobroComisionOpcionesCobro demo = new EditarCobroComisionOpcionesCobro();
            try {
                SalidaEditarOpcionCobroComision salida = demo.logica(con, Objin);
                System.out.println(salida.toString());
                con.commit();
                //con.rollback();
            }
            catch(Exception er) {
                er.printStackTrace();   
                con.rollback();
            }
        }
        System.out.println("== END ==");
    }

}
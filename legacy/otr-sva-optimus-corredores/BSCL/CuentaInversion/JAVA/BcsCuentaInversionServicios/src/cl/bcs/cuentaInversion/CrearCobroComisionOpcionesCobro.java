package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
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
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCobroComisionOpcionesCobro implements JavaCall {
    private static final Logger logger = LogManager.getLogger(CrearCobroComisionOpcionesCobro.class);

    private final String SQL_INSERT_OPCION_COBRO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_InsertRegistro.sql");
    private final String SQL_CREAR_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_CrearTablaPaso.sql");
    private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_InsertTablaPaso.sql");
    private final String SQL_SELECT_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_SelectOpcionesCobroTablaPaso.sql");
    private final String SQL_DROP_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/OpcionesCobroComisiones_DropTablaPaso.sql");
    
    public static class EntradaCrearCobroComisionOpcionesCobro extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<OpcionesCobro> p_Opciones;
        
        public EntradaCrearCobroComisionOpcionesCobro() {
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
        public String FechaIngreso;
        public Date FechaVigencia;
        
        public OpcionesCobro() {
            this.IdOpcionCobro = 0L;
            this.OpcionCobro = 0L;
            this.PortafolioAPV = "N";
            this.Mercado = "";
            this.PorcentajeCobro = BigDecimal.ZERO;
            this.Estado = "N";
            this.FechaIngreso = "0001-01-01";
            this.FechaVigencia = Fecha.toDate("0001-01-01");
        }

    }

    public static class SalidaCrearCobroComisionOpcionesCobro extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        
        public SalidaCrearCobroComisionOpcionesCobro () {
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
    
    private void agregarLog(Connection con, List<OpcionesCobro> opciones, Long idEmpresa, Long idUsuario) throws Exception {
        logger.info("INICIO::CrearCobroComisionOpcionesCobro.agregarLog");
        long inicio = System.currentTimeMillis();
        
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
        
        AgregarLog agregarLogService = new AgregarLog();
        AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
        entradaAgregarLog.p_IdTrabajo = 1L;
        
        for (OpcionesCobro opcion : opciones) {
            AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

            log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
            log.IdCabeceraLog = opcion.OpcionCobro;
            log.IdTipoLog = "INGRESO";
            log.IdModulo = "CTA";
            log.IdUsuario = idUsuario;
            log.IdEmpresa = idEmpresa;
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = idUsuario;
            log.IdCliente = 0L;
            log.EstadoCliente = "";
            log.NombreCliente = "";
            log.IdTipoDocumentoIdentidad = "";
            log.IdDocumentoIdentidad = "";
            log.IdPortafolio = 0L;
            log.EstadoPortafolio = "";
            log.IdTipoRegistro = "INGRESO_OPCION_COBRO_COMISION";
            log.EstadoRegistro = "";
            log.IdTipoOperacion = "";
            log.Folio = 0L;
            log.Cantidad = BigDecimal.ZERO;
            log.PrecioTasa = BigDecimal.ZERO;
            log.Monto = BigDecimal.ZERO;
            log.IdMonedaOrigen = "";
            log.IdMonedaLiquidacion = "";
            log.TipoCambio = "";
            log.Nemo = "";
            log.IdProceso = 0L;
            log.EstadoProceso = "";
            log.NombreProceso = "Ingreso de opción de cobro "+ opcion.OpcionCobro +" en menú de opciones de Cobro Comision Patrimonio";                     
            entradaAgregarLog.p_Log.add(log);
        }
        
        agregarLogService.logica(con, entradaAgregarLog);
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearCobroComisionOpcionesCobro.agregarLog", fin - inicio);
    }

    public SalidaCrearCobroComisionOpcionesCobro logica(Connection conn, EntradaCrearCobroComisionOpcionesCobro in) throws Exception {
        logger.info("INICIO::CrearCobroComisionOpcionesCobro");
        long inicio = System.currentTimeMillis();

        SQL sql=new SQL();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA","PLA.FechaSistema");
        SalidaCrearCobroComisionOpcionesCobro Salida = new SalidaCrearCobroComisionOpcionesCobro();
        List<OpcionesCobro> insertOpciones = new ArrayList<OpcionesCobro>();

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
            List<OpcionesCobro> opcionesCobroExistentes = opcionesCobro.stream().filter(op -> op.IdOpcionCobro.compareTo(0L) > 0).collect(Collectors.toList());
            HashMap<String, String> paramMensaje = new HashMap<String, String>(4);
            
            for( OpcionesCobro op : opcionesCobroExistentes) {
                paramMensaje.put("Opcion", op.OpcionCobro.toString());
                paramMensaje.put("PortafolioAPV", op.PortafolioAPV);
                paramMensaje.put("Mercado", op.Mercado);
                logger.info("INFO::CrearCobroComisionOpcionesCobro :: ya existe opción con estas caracteristicas :: "+op.OpcionCobro+" "+op.PortafolioAPV+" "+op.Mercado);
                Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_OPCION_DUPLICADA",paramMensaje,conn,in.p_IdEmpresa));
                paramMensaje.clear();
            }
            
            Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
            
            if(Salida.P_OUT_ERRORESCONTADOR > 0) {
                return Salida;
            }
            List<OpcionesCobro> opcionesCobroNoExistentes = opcionesCobro.stream().filter(op -> op.IdOpcionCobro.compareTo(0L) == 0).collect(Collectors.toList());
            
            if(!opcionesCobroNoExistentes.isEmpty()) {
                logger.info("INICIO::CrearCobroComisionOpcionesCobro ingreso de nueva opcion");
                sql.batch(conn, SQL_INSERT_OPCION_COBRO, opcionesCobroNoExistentes, OpcionesCobro.class);
                logger.info("INICIO::CrearCobroComisionOpcionesCobro Opcion Ingresada Exitosamente");
                for( OpcionesCobro op : opcionesCobroNoExistentes) {
                    paramMensaje.put("Opcion", op.OpcionCobro.toString());
                    paramMensaje.put("PortafolioAPV", op.PortafolioAPV);
                    paramMensaje.put("Mercado", op.Mercado);
                    paramMensaje.put("PorcentajeCobro", op.PorcentajeCobro.toString());
                    Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPCION_COBRO_INGRESADA",paramMensaje,conn,in.p_IdEmpresa));
                    paramMensaje.clear();
                }
            }
        }catch (Error e) {
            logger.info("INFO::CrearCobroComisionOpcionesCobro :: Operacion Con errores: " + e);
        }finally {
            SQL.exec(conn, SQL_DROP_TABLA_PASO);
        }
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::CrearCobroComisionOpcionCobro.", fin - inicio);

        agregarLog(conn, insertOpciones, in.p_IdEmpresa, in.p_IdUsuario);
        return Salida;
    }
    
    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        Gson gson = getGsonBuilder();
        EntradaCrearCobroComisionOpcionesCobro jin=gson.fromJson(in.toString() , EntradaCrearCobroComisionOpcionesCobro.class);
        SalidaCrearCobroComisionOpcionesCobro out = logica(con,jin);
        JsonParser parser = new JsonParser();
        JsonObject dataout  = parser.parse(gson.toJson(out)).getAsJsonObject();
        
        return dataout;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("== TEST ==");
        try (Connection con = PlataformaUtils.getQAConnection()) {
            Gson gson = Utils.getGsonBuilder();         
            String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"N\",\"p_Opciones\":[{\"OpcionCobro\":133,\"PortafolioAPV\":\"S\",\"Mercado\":\"CI\",\"PorcentajeCobro\":0.0012,\"Estado\":\"INA\",\"FechaVigencia\":\"2024-06-03T04:00:00.000Z\"}]}";
            EntradaCrearCobroComisionOpcionesCobro Objin = gson.fromJson(json, EntradaCrearCobroComisionOpcionesCobro.class);
            CrearCobroComisionOpcionesCobro demo = new CrearCobroComisionOpcionesCobro();
            try {
                SalidaCrearCobroComisionOpcionesCobro salida = demo.logica(con, Objin);
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
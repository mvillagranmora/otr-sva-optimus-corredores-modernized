package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

public class EditarCobroComisionRegistros implements JavaCall {
    private static final Logger logger = LogManager.getLogger(EditarCobroComisionRegistros.class);
    private final String CONSULTA_PERIODO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_ConsultarPeriodos.sql");
    private final String CONSULTA_EDITADO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_ConsultarEditados.sql");
    private final String INSERT_EDITADO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_AgregarEditado.sql");
    private final String MODIFICACION_EDITADO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_ModificarEditado.sql");

    public static class EntradaCobroComisionEditarRegistro extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdPortafolio;
        public BigDecimal p_MontoComision;
        public Long p_Periodo;
        public String p_EstadoComision;
        
    }
    
    public static class periodoOUT extends BcsPojo {
        public Date FechaDesde;
        public Date FechaHasta;
    }
    
    public static class EditadoOUT extends BcsPojo {
        public Long IdRegistro; 
        public String EstadoComision;
        public Long IdPortafolio;
    }
    
    public static class AgregarEditadoComision extends BcsPojo  {
        public Long IdRegistro;
        public Long IdPortafolio;
        public Long Periodo;
        public BigDecimal MontoComision;
        public BigDecimal PorcentajeCobro;
        public String FechaModificacion;
        public String EstadoComision;
    }
    
    public static class ModificarEditadoComision extends BcsPojo {
        public Long IdRegistro;
        public BigDecimal MontoComision;
        public String FechaModificacion;
    }
    
    

    public static class SalidaCobroComisionEditarRegistro extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        
        public SalidaCobroComisionEditarRegistro () {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_ADVERTENCIASCONTADOR = 0L;
        }
    }
    

    public SalidaCobroComisionEditarRegistro logica(Connection conn, EntradaCobroComisionEditarRegistro in) throws SQLException, Exception {
        logger.info("INICIO::EditarCobroComisionRegistros");
        
        SQL sql=new SQL();
        SalidaCobroComisionEditarRegistro Salida = new SalidaCobroComisionEditarRegistro();
        
        
        
        //VALIDACION DE QUE FECHA DE EDICION SE ENCUENTRE POSTERIOR AL PERIODO DE CONSULTA
        
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA","PLA.FechaSistema");
        Date fechaSIS = Fecha.toDate(fechaSistema);
            
        HashMap<String, Object> per = new HashMap<String, Object>(1);
        per.put("IdPeriodo", in.p_Periodo );
        List<periodoOUT> Rangofechas = sql.query(conn, CONSULTA_PERIODO, per, periodoOUT.class);
        
        if (!Rangofechas.isEmpty()) {
            periodoOUT periodo = Rangofechas.get(0);
            Date fechaHasta = periodo.FechaHasta;
            
            if(!fechaSIS.after(fechaHasta)) {
                Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_FUERA_DE_PERIODO",new HashMap<String, String>(),conn,in.p_IdEmpresa));
                Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
                return Salida;
                
            }
            
        } else {
            Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_EXISTE_PERIODO",new HashMap<String, String>(),conn,in.p_IdEmpresa));
            Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
            return Salida;
        }
        
        
        /// VALIDAR QUE EL REGISTRO SE ENCUENTRE EN ESTADO VIGENTE
        
        if(!in.p_EstadoComision.equals("VIG")) {
            Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_REGISTRO_DISTINTO_VIG",new HashMap<String, String>(),conn,in.p_IdEmpresa));
            Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
            return Salida;
        }
        
        
        //// SE PROCEDE A VALIDAR SI EXISTE REGISTRO EN LA TABLA DE EDICION
        HashMap<String, Object> edit = new HashMap<String, Object>(2);
        edit.put("Periodo", in.p_Periodo );
        edit.put("IdPortafolio", in.p_IdPortafolio);
        
        List<EditadoOUT> registro = sql.query(conn, CONSULTA_EDITADO, edit, EditadoOUT.class);
        
        /// SI NO EXISTE SE CREA EL REGISTRO
        
        if(registro.isEmpty()) {
            List<Long> secuenciaEditado = Utils.getSecuencia(conn, "BSCL.CuentaInversion.BD.Secuencias::RegistrosEditadosComisiones", new Long(1));
            Long IdRegistro = secuenciaEditado.get(0);
            
            List<AgregarEditadoComision> registroEdit= new ArrayList<>();
            AgregarEditadoComision data = new AgregarEditadoComision();
            
            data.IdRegistro= IdRegistro;
            data.IdPortafolio = in.p_IdPortafolio;
            data.Periodo= in.p_Periodo;
            data.MontoComision = in.p_MontoComision;
            data.FechaModificacion = fechaSistema;
            data.EstadoComision = in.p_EstadoComision;
            registroEdit.add(data);
            
            sql.batch(conn, INSERT_EDITADO, registroEdit, AgregarEditadoComision.class);
            
            Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_REGISTRO_EDITADO_CORRECTAMENTE",new HashMap<String, String>(),conn,in.p_IdEmpresa));
            return Salida;
        }
        else {
            
            EditadoOUT registroBD = registro.get(0);
            String EstadoComisionBD = registroBD.EstadoComision;
            
            if(EstadoComisionBD.equals("PRO")) { // REGISTRO PROCESADO NO PUEDE SER EDITADO
                Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_REGISTRO_EDITADO_PROCESADO",new HashMap<String, String>(),conn,in.p_IdEmpresa));
                Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
                return Salida;
            }
            
            if(EstadoComisionBD.equals("INA")) { /// SE ELIMINO UN REGISTRO, PERO SE VOLVIO A REACTIVAR
                
                List<ModificarEditadoComision> registroEdit = new ArrayList<>();
                ModificarEditadoComision data = new ModificarEditadoComision();
                data.IdRegistro = registroBD.IdRegistro;
                data.FechaModificacion = fechaSistema;
                data.MontoComision = in.p_MontoComision;
                registroEdit.add(data);
                sql.batch(conn, MODIFICACION_EDITADO, registroEdit, ModificarEditadoComision.class);
                
                Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_REGISTRO_EDITADO_CORRECTAMENTE",new HashMap<String, String>(),conn,in.p_IdEmpresa));
                
                return Salida;
            }
            
            List<ModificarEditadoComision> registroEdit = new ArrayList<>();
            ModificarEditadoComision data = new ModificarEditadoComision();
            data.IdRegistro = registroBD.IdRegistro;
            data.FechaModificacion = fechaSistema;
            data.MontoComision = in.p_MontoComision;
            registroEdit.add(data);
            sql.batch(conn, MODIFICACION_EDITADO, registroEdit, ModificarEditadoComision.class);
            
            Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_REGISTRO_EDITADO_CORRECTAMENTE",new HashMap<String, String>(),conn,in.p_IdEmpresa));
            
            
            
            return Salida;
        }
        
    }
    
    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        Gson gson = getGsonBuilder();
        EntradaCobroComisionEditarRegistro jin=gson.fromJson(in.toString() , EntradaCobroComisionEditarRegistro.class);
        SalidaCobroComisionEditarRegistro out = logica(con,jin);
        JsonParser parser = new JsonParser();
        JsonObject dataout  = parser.parse(gson.toJson(out)).getAsJsonObject();
        
        return dataout;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("== TEST ==");
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")) {
            Gson gson = Utils.getGsonBuilder();         
            String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPortafolio\":85937,\"p_MontoComision\":57,\"p_Periodo\":1,\"p_EstadoComision\":\"VIG\"}"; 
            String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPortafolio\":482406,\"p_MontoComision\":145,\"p_Periodo\":1,\"p_EstadoComision\":\"VIG\"}";
            EntradaCobroComisionEditarRegistro Objin = gson.fromJson(json2, EntradaCobroComisionEditarRegistro.class);
            EditarCobroComisionRegistros demo = new EditarCobroComisionRegistros();
            try {
                SalidaCobroComisionEditarRegistro salida = demo.logica(con, Objin);
                System.out.println(salida.toString());
                con.commit();
//                con.rollback();
            }
            catch(Exception er) {
                er.printStackTrace();   
                con.rollback();
            }
        }
        System.out.println("== END ==");
    }

}
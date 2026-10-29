package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.GenerarArchivoClientesPorPagarYCobrar: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.GenerarArchivoClientesPorPagarYCobrar.lanza"), may run scripted SQL on the job connection ("colaboradores.GenerarArchivoClientesPorPagarYCobrar.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.GenerarArchivoClientesPorPagarYCobrar.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class GenerarArchivoClientesPorPagarYCobrar implements JavaCall {

    public static class EntradaGenerarArchivoClientesPorPagarYCobrar extends BcsPojo {
    		public Long p_IdEmpresa;// OBLIGATORIO
    		public Long p_IdUsuario;// OBLIGATORIO
    		public String p_IdModulo;// OBLIGATORIO
    		public String FechaSistema;
    		public String EsBusqueda;
    		public String IdMoneda;
    		public String TipoSaldo;// POS;NEG;TOD 
    	}

    public static class SalidaGenerarArchivoClientePorPagarYCobrar extends BcsPojo {
    
    	public List<MensajeSalida> P_OUT_MENSAJES;
    	public Long P_OUT_ERRORESCONTADOR;
    	public String P_OUT_RUTAARCHIVO;
    
    	public List<ClientesPagarCobrar> P_OUT_RESULTADO;
    
    	public SalidaGenerarArchivoClientePorPagarYCobrar() {
    	    this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    	    this.P_OUT_ERRORESCONTADOR = 0L;
    	    this.P_OUT_RUTAARCHIVO = "";
    	}
        }

    public static class ClientesPagarCobrar extends BcsPojo {
    		public Long IdCuentaInversion;
    		public Long IdPersona;
    		public Long IdCliente;
    		public String DocumentoIdentidad;
    		public String NombreCliente;
    		public Long IdPortafolio;
    		public String CodigoProposito;
    		public String Relacion;
    		public Date FechaIngresoRegistro;
    		public Date FechaCertificacion;
    		public Date FechaEfectivaMovimiento;
    		public String IdMoneda;
    		public BigDecimal Saldo;
    		public String NombreAgenteCliente;
    		public String DocumentoIdentidadAgente;
    		public Long IdEjecutivo;
    		public String NombreEjecutivoMovimiento;
    		public String DocumentoIdentidadEjecutivo;
    		public BigDecimal SaldoRetenido;
    		public BigDecimal MoneyMarket;
    		public BigDecimal Garantias;
    		public BigDecimal Simultanea;
    		public BigDecimal Pacto;
    
    	public ClientesPagarCobrar() {
    	    this.IdCuentaInversion = 0L;
    	    this.IdPersona = 0L;
    	    this.IdCliente = 0L;
    	    this.DocumentoIdentidad = "";
    	    this.IdPortafolio = 0L;
    	    this.CodigoProposito = "";
    	    this.Relacion = "";
    	    this.FechaIngresoRegistro = Fecha.toDateTime("0001-01-01 01:01:01");
    	    this.IdMoneda = "";
    	    this.NombreAgenteCliente = "";
    	    this.DocumentoIdentidadAgente = "";
    	    this.IdEjecutivo = 0L;
    	    this.NombreEjecutivoMovimiento = "";
    	    this.DocumentoIdentidadEjecutivo = "";
    	}
        }

    public SalidaGenerarArchivoClientePorPagarYCobrar logica(Connection con, EntradaGenerarArchivoClientesPorPagarYCobrar entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("GenerarArchivoClientesPorPagarYCobrar", entrada, SalidaGenerarArchivoClientePorPagarYCobrar.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}

package cl.bcs.fondomutuo.cierre.contrato;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;

/**
 * JSON contract of Custodia {@code ConsultarFlujoCustodia} (port H {@link CustodiaPort}) for {@code CalcularComisiones} (step 3A): the legacy request
 * classes in full (null keys written, defaults of the legacy constructors) and the part of each {@code FlujoCustodiaDetallado} row that the commission
 * calculation reads. Dates of the answer are the first ten characters ({@code yyyy-MM-dd}) of the legacy wire format ({@code yyyy-MM-dd[ HH:mm:ss[.SSS]]},
 * the legacy {@code GsonDateDeserializer} formats): a date is read as a {@link LocalDate}, so no JVM time zone takes part.
 */
public final class FlujoCustodiaContrato {

    private FlujoCustodiaContrato() {
    }

    public static class FlujoCustodiaEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaSaldo;
        public Long p_IdPais;
        public Integer p_DiasProyeccion;
        public String p_DiaHabil = "S";
        public String p_ValorizarFlujo;
        public Object p_BarraProgreso;
        public List<FlujoUso> p_Uso;
        public List<FlujoFiltrosBusqueda> p_FiltrosBusqueda;
        public List<FlujoEsAgrupadoPor> p_EsAgrupadoPor;

        public FlujoCustodiaEntrada() {
            this.p_Uso = new ArrayList<>();
            this.p_FiltrosBusqueda = new ArrayList<>();
            this.p_EsAgrupadoPor = new ArrayList<>();
        }
    }

    public static class FlujoUso {
        public String UsoTipoOperacion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String BusquedaEspecifica;
        public String IdTipoInstrumentoEspecifico;
    }

    public static class FlujoFiltrosBusqueda {
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String IdEmisor;
        public String FechaVencimiento;
        public String IdTipoReajuste;
        public String EsFungible;
        public String IdBoveda;
        public String IdRegimenFondoMutuo;
    }

    public static class FlujoEsAgrupadoPor {
        public String ClasificacionTipoInstrumento;
        public String TipoInstrumento;
        public String Instrumento;
        public String Emisor;
        public String Nemotecnico;
        public String NemoSVS;
        public String FechaEmisionInstrumento;
        public String FechaVencimientoInstrumento;
        public String FechaLiquidacionInstrumento;
        public String TipoReajuste;
        public String EsFungible;
        public String CodigoIFRS;
        public String Boveda;
        public String Bolsa;
        public String TipoOperacion;
        public String Mesa;
        public String FolioRegistro;
        public String ItemOrigen;
        public String IdRegimenFondoMutuo;
        public String GrupoRegimen;
        public String MonedaInstrumento;
    }

    public static class FlujoCustodiaDetallado {
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdInstrumento;
        public BigDecimal Cantidad;
        public BigDecimal PrecioCompra;
        public String IdConcepto;
        public LocalDate FechaOrigenCompra;
        public LocalDate FechaOperacion;
    }

    public static class FlujoCustodiaSalida {
        public Long P_OUT_ERRORESCONTADOR;
        public List<FlujoCustodiaDetallado> p_out_FlujoDetallado;

        public FlujoCustodiaSalida() {
            this.p_out_FlujoDetallado = new ArrayList<FlujoCustodiaDetallado>();
        }
    }

    private static final class FechaDeserializer implements JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonElement elemento, Type tipo, JsonDeserializationContext contexto) throws JsonParseException {
            String texto = elemento.getAsString();
            return LocalDate.parse(texto.substring(0, 10));
        }
    }

    private static final Gson LECTURA = new GsonBuilder().registerTypeAdapter(LocalDate.class, new FechaDeserializer()).create();

    /** One call of the port: the legacy {@code flujoCustodia.logica(con, entrada)}. */
    public static FlujoCustodiaSalida consultar(CustodiaPort puerto, FlujoCustodiaEntrada entrada) {
        JsonObject solicitud = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return LECTURA.fromJson(puerto.consultarFlujo(solicitud), FlujoCustodiaSalida.class);
    }
}

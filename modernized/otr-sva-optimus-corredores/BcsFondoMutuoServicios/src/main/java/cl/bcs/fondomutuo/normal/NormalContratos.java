package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.BatchPort;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.NotificacionOwpPort;

/**
 * JSON contracts (legacy class and field names, null keys written as the legacy did: the oracle's doubles record exactly this) of the collaborators of
 * Plataforma that Normal calls over the router (port H): {@code SaldoFlujoCaja}, {@code InscribirProcesoBatch}, {@code NotificarEstadoOrdenesOWP}.
 */
final class NormalContratos {

    private NormalContratos() {
    }

    static class FlujoCajaEntrada {
        public Long P_IDEMPRESA;
        public String P_FECHADESDE;
        public String P_FECHAHASTA;
        public String P_USO;
        public String P_DESPLEGARENUI = "N";
        public String P_AGRUPARPORORIGEN = "N";
        public Object p_BarraProgreso;
        public List<PortafolioAConsultar> P_PORTAFOLIOACONSULTAR = new ArrayList<>();
    }

    static class PortafolioAConsultar {
        public Long IdPortafolio;
        public String IdMoneda;
    }

    static class FlujoCajaSalida {
        public List<FlujoCaja> P_OUT_FLUJOCAJA = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    static class FlujoCaja {
        public String NombreUso;
        public String FechaSaldo;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Saldo = BigDecimal.ZERO;
    }

    static class BatchEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<TipoPrograma> p_TipoPrograma;
        public List<Parametro> p_Parametro;
    }

    static class TipoPrograma {
        public Long IdInterno;
        public String IdTipoPrograma;
        public Long SegundosDesfase;
        public String Comentario = "";
    }

    static class Parametro {
        public Long IdInterno;
        public String NombreParametro;
        public String Valor;
        public String ValorBlob;

        Parametro(Long idInterno, String nombre, String valor, String blob) {
            this.IdInterno = idInterno;
            this.NombreParametro = nombre;
            this.Valor = valor;
            this.ValorBlob = blob;
        }
    }

    static class NotificarOrden {
        public Long IdEmpresa;
        public Long IdUsuario;
        public Long Solsec;
        public String Fecha;
        public String Estado;
        public String Mercado;
        public BigDecimal NTitulos;
        public BigDecimal Precio;
        public BigDecimal Comision;
        public BigDecimal Gastos;
        public String Divisa;
        public BigDecimal TipoCambio;
        public String SolsecAsoc;
        public String Nemo;
        public String IdClasificacionTipoInstrumento;
    }

    static FlujoCajaSalida saldo(FlujoCajaPort puerto, FlujoCajaEntrada entrada) {
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.saldo(json), FlujoCajaSalida.class);
    }

    static void inscribir(BatchPort puerto, BatchEntrada entrada) {
        puerto.inscribir(JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject());
    }

    /** Legacy {@code NotificarEstadoOrdenesOWP.go(con, List)}: it is not a JavaCall entry; the wire shape is {@code {"p_Solicitudes": [...]}} (ASSUMPTION, Q-08). */
    static void notificar(NotificacionOwpPort puerto, List<NotificarOrden> ordenes) {
        JsonObject json = new JsonObject();
        JsonArray arreglo = JsonLegacy.conNulos().toJsonTree(ordenes).getAsJsonArray();
        json.add("p_Solicitudes", arreglo);
        puerto.notificar(json);
    }
}

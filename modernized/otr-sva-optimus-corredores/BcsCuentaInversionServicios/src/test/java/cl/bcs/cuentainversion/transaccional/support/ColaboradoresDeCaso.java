package cl.bcs.cuentainversion.transaccional.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.cuentainversion.port.out.Colaboradores;

/**
 * Fake of the legacy collaborators with the answers scripted in {@code case.json "colaboradores"}, the same ones the oracle
 * fakes ({@code legacy-oracle/shims}) returned. It records every call as {@code {servicio, entrada}} (class simple name; a scalar
 * entrada travels as {@code {"valor": x}} and is recorded unwrapped) in {@link #traza()}, to be compared with the golden
 * "colaboradores" by {@link Verifica#colaboradores}.
 */
public final class ColaboradoresDeCaso implements Colaboradores {

    private final JsonObject guion;
    private final List<JsonObject> traza = new ArrayList<>();

    public ColaboradoresDeCaso(Caso caso) {
        this.guion = caso.colaboradores();
    }

    public List<JsonObject> traza() {
        return traza;
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        String nombre = servicio.substring(servicio.lastIndexOf('.') + 1);
        registrar(nombre, entrada);
        return switch (nombre) {
            case "RevisarRequisitosBasicos" -> requisitosBasicos();
            case "SaldoFlujoCaja" -> saldoFlujoCaja();
            case "AgregarLog" -> contador("P_OUT_ERRORESCONTADOR", 0);
            case "AgregarEvento" -> agregarEvento(entrada);
            case "ConsultarEmpresa" -> consultarEmpresa(entrada);
            case "ConsultarImagen" -> consultarImagen();
            case "ObtenerPortafoliosEjecutivo" -> portafoliosEjecutivo(entrada);
            case "CalcularParidad" -> calcularParidad(entrada);
            case "EntregarPreciosTasas" -> entregarPreciosTasas();
            case "CrearEventoAutorizacionMasivo" -> crearEventoAutorizacion(entrada);
            case "AgregarPrograma" -> contador("P_OUT_ERRORESCONTADOR", numero("agregarProgramaErrores", 0));
            default -> throw new IllegalStateException("collaborator without script: " + servicio);
        };
    }

    private void registrar(String nombre, JsonObject entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", nombre);
        o.add("entrada", entrada != null && entrada.size() == 1 && entrada.has("valor") ? entrada.get("valor") : entrada);
        traza.add(o);
    }

    private JsonArray lista(String clave) {
        JsonElement e = guion.get(clave);
        return e == null || e.isJsonNull() ? new JsonArray() : e.getAsJsonArray();
    }

    private long numero(String clave, long defecto) {
        JsonElement e = guion.get(clave);
        return e == null || e.isJsonNull() ? defecto : e.getAsLong();
    }

    private static JsonObject contador(String campo, long valor) {
        JsonObject o = new JsonObject();
        o.addProperty(campo, valor);
        return o;
    }

    private JsonObject requisitosBasicos() {
        JsonObject o = new JsonObject();
        o.add("p_out_Mensajes", lista("requisitosBasicos").deepCopy());
        return o;
    }

    private JsonObject saldoFlujoCaja() {
        JsonArray filas = new JsonArray();
        for (JsonElement e : lista("saldoFlujoCaja")) {
            JsonObject f = e.getAsJsonObject();
            JsonObject r = new JsonObject();
            for (String k : List.of("IdPortafolio", "IdMoneda", "FechaSaldo", "Saldo")) {
                r.add(k, f.get(k));
            }
            filas.add(r);
        }
        JsonObject out = new JsonObject();
        out.add("P_OUT_FLUJOCAJA", filas);
        out.addProperty("P_OUT_ERRORESCONTADOR", numero("saldoFlujoCajaErrores", 0));
        return out;
    }

    private JsonObject agregarEvento(JsonObject entrada) {
        boolean sinId = guion.has("agregarEventoSinId") && guion.get("agregarEventoSinId").getAsBoolean();
        JsonArray ids = new JsonArray();
        long n = 9000;
        JsonElement eventos = entrada.get("P_EVENTOS");
        if (eventos != null && eventos.isJsonArray()) {
            for (JsonElement e : eventos.getAsJsonArray()) {
                JsonObject id = new JsonObject();
                id.add("IdSecuencia", e.getAsJsonObject().get("IdSecuencia"));
                id.add("IdEvento", sinId ? JsonNull.INSTANCE : new JsonPrimitive(n++));
                ids.add(id);
            }
        }
        JsonObject out = new JsonObject();
        out.add("P_OUT_IDEVENTO", ids);
        return out;
    }

    private JsonObject consultarEmpresa(JsonObject entrada) {
        JsonObject out = new JsonObject();
        out.add("IdEmpresa", entrada.get("valor"));
        out.addProperty("IdPais", numero("idPaisEmpresa", 44));
        return out;
    }

    private JsonObject consultarImagen() {
        JsonObject out = new JsonObject();
        JsonArray imagenes = new JsonArray();
        if (guion.has("logoRutaPublica") && !guion.get("logoRutaPublica").isJsonNull()) {
            JsonObject i = new JsonObject();
            i.add("RutaPublica", guion.get("logoRutaPublica"));
            imagenes.add(i);
        }
        out.add("OUT_IMAGENES", imagenes);
        return out;
    }

    private JsonObject portafoliosEjecutivo(JsonObject entrada) {
        JsonArray salida = new JsonArray();
        JsonElement filtrar = entrada.get("p_filtrarListaPortafolios");
        if (filtrar != null && !filtrar.isJsonNull() && "S".equals(filtrar.getAsString())) {
            boolean hayFiltro = guion.has("portafoliosPermitidos");
            for (JsonElement p : entrada.getAsJsonArray("p_ListaIdPortafolios")) {
                boolean ok = !hayFiltro;
                for (JsonElement permitido : lista("portafoliosPermitidos")) {
                    if (permitido.getAsLong() == p.getAsLong()) {
                        ok = true;
                    }
                }
                if (ok) {
                    salida.add(p);
                }
            }
        } else {
            lista("portafoliosEjecutivo").forEach(salida::add);
        }
        JsonObject out = new JsonObject();
        out.add("valor", salida);
        return out;
    }

    private JsonObject calcularParidad(JsonObject entrada) {
        BigDecimal f = guion.has("factorParidad") ? guion.get("factorParidad").getAsBigDecimal() : BigDecimal.ONE;
        JsonArray calculos = new JsonArray();
        for (JsonElement e : entrada.getAsJsonArray("p_CalculosEntrada")) {
            JsonObject c = e.getAsJsonObject();
            JsonObject s = new JsonObject();
            s.add("IdInterno", c.get("IdInterno"));
            s.addProperty("MontoDestino", c.get("MontoOrigen").getAsBigDecimal().multiply(f));
            calculos.add(s);
        }
        JsonObject out = new JsonObject();
        out.add("p_out_CalculosSalida", calculos);
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }

    private JsonObject entregarPreciosTasas() {
        JsonArray precios = new JsonArray();
        for (JsonElement e : lista("preciosTasas")) {
            JsonObject o = e.getAsJsonObject();
            JsonObject p = new JsonObject();
            p.add("Nemotecnico", o.get("Nemotecnico"));
            p.add("Precio", o.get("Precio"));
            if (o.has("FechaFuente") && !o.get("FechaFuente").isJsonNull()) {
                p.add("FechaFuente", o.get("FechaFuente"));
            }
            precios.add(p);
        }
        JsonObject out = new JsonObject();
        out.add("P_OUT_PRECIOSTASAS", precios);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }

    private JsonObject crearEventoAutorizacion(JsonObject entrada) {
        JsonArray ids = new JsonArray();
        long n = 7000;
        for (JsonElement e : entrada.getAsJsonArray("P_TABLAEVENTOS")) {
            JsonObject id = new JsonObject();
            id.addProperty("IdEvento", n++);
            id.add("NumeroOperacionConcepto", e.getAsJsonObject().get("NumeroOperacionConcepto"));
            ids.add(id);
        }
        JsonObject out = new JsonObject();
        out.add("P_OUT_IDEVENTO", ids);
        out.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
        out.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.add("P_OUT_ERRORESDETALLE", new JsonArray());
        return out;
    }
}

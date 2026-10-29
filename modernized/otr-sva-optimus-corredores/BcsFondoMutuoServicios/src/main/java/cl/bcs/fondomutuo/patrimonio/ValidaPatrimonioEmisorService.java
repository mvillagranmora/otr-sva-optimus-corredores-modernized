package cl.bcs.fondomutuo.patrimonio;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort.Custodia;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorPort.SerieFondo;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.Hashes;

/**
 * Legacy {@code cl.bcs.fondomutuo.ValidaPatrimonioEmisor} (233 LOC), rule FM-REQ-23. The custody of the client with the same issuer is added up
 * (same currency: the amount; other currency: converted by {@code CalcularParidad} through the H port, ONE call per request even when there is nothing
 * to convert) and compared with the minimum equity of the series' issuer. Kept as legacy: a series that does not exist is an error with no
 * parameters; the counter is the NUMBER OF MESSAGES; {@code FMM.ValorCuota} has no date filter (D-REQ-7, in the SQL); the step table is shared per
 * session (see {@link PatrimonioEmisorPort}).
 */
public class ValidaPatrimonioEmisorService {

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_Fecha;
        public List<Solicitud> p_Solicitudes = new ArrayList<>();
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Solicitud {
        public Long IdInterno;
        public Long IdCliente;
        public String IdSerieFondo;
        public String IdEmisor;
        public BigDecimal MontoPatrimonio = BigDecimal.ZERO;
        public BigDecimal ValorCuota;
    }

    /** JSON contract of the collaborator {@code CalcularParidad} (legacy field names). */
    public static class CalcularParidadEntrada {
        public Long p_IdEmpresa;
        public Long p_IdTrabajo;
        public List<CalculoEntrada> p_CalculosEntrada = new ArrayList<>();
    }

    public static class CalculoEntrada {
        public Long IdTrabajo;
        public Long IdInterno;
        public String Fecha;
        public String IdMonedaOrigen;
        public String IdMonedaDestino;
        public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
        public BigDecimal MontoOrigen;
        public String IdUso = "";
    }

    public static class CalcularParidadSalida {
        public List<CalculoSalida> p_out_CalculosSalida = new ArrayList<>();
    }

    public static class CalculoSalida {
        public Long IdInterno;
        public BigDecimal MontoDestino;
    }

    private final PatrimonioEmisorPort datos;
    private final ParidadPort paridad;
    private final DiccionarioMensajesPort diccionario;

    public ValidaPatrimonioEmisorService(PatrimonioEmisorPort datos, ParidadPort paridad, DiccionarioMensajesPort diccionario) {
        this.datos = datos;
        this.paridad = paridad;
        this.diccionario = diccionario;
    }

    public Salida logica(Entrada entrada) throws Exception {
        Salida out = new Salida();

        HashMap<String, Boolean> seriesVistas = new HashMap<>();
        HashMap<Long, Boolean> clientesVistos = new HashMap<>();
        List<String> idsSerie = new ArrayList<>();
        List<Long> idsClienteTablaPaso = new ArrayList<>();
        for (Solicitud sol : entrada.p_Solicitudes) {
            if (seriesVistas.get(sol.IdSerieFondo) == null) {
                idsSerie.add(sol.IdSerieFondo);
                seriesVistas.put(sol.IdSerieFondo, true);
            }
            if (clientesVistos.get(sol.IdCliente) == null) {
                idsClienteTablaPaso.add(sol.IdCliente);
                clientesVistos.put(sol.IdCliente, true);
            }
        }

        List<SerieFondo> series = datos.seriesFondo(idsSerie);
        HashMap<String, SerieFondo> hashSeries = Hashes.aHash(series, s -> s.IdSerieFondo);
        List<Custodia> custodias = datos.custodia(idsClienteTablaPaso, entrada.p_Fecha);
        Map<String, List<Custodia>> custodiaClienteEmisor = custodias.stream().collect(Collectors.groupingBy(c -> c.IdClienteEmisor));

        HashMap<Long, BigDecimal> patrimonioPorSolicitud = new HashMap<>(entrada.p_Solicitudes.size());
        Long i = 1L;
        HashMap<Long, Long> idsCalculo = new HashMap<>();
        CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
        paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
        paridadEntrada.p_IdTrabajo = 1L;

        for (Solicitud sol : entrada.p_Solicitudes) {
            SerieFondo serie = hashSeries.get(sol.IdSerieFondo);
            if (serie != null) {
                List<Custodia> delEmisor = custodiaClienteEmisor.get(sol.IdCliente + "_" + serie.IdEmisor);
                if (delEmisor != null) {
                    for (Custodia custodia : delEmisor) {
                        BigDecimal montoCustodia = patrimonioPorSolicitud.get(sol.IdInterno) == null ? BigDecimal.ZERO
                                : patrimonioPorSolicitud.get(sol.IdInterno);
                        if (custodia.IdMonedaInstrumento.equals(serie.IdMoneda)) {
                            montoCustodia = montoCustodia.add(custodia.Monto, MathContext.DECIMAL128);
                            patrimonioPorSolicitud.put(sol.IdInterno, montoCustodia);
                            continue;
                        }
                        CalculoEntrada calculo = new CalculoEntrada();
                        calculo.IdInterno = i;
                        calculo.IdTrabajo = 1L;
                        calculo.Fecha = entrada.p_Fecha;
                        calculo.IdMonedaOrigen = custodia.IdMonedaInstrumento;
                        calculo.IdMonedaDestino = serie.IdMoneda;
                        calculo.ValorCambioLocalModificado = BigDecimal.ZERO;
                        calculo.MontoOrigen = custodia.Monto;
                        calculo.IdUso = "";
                        paridadEntrada.p_CalculosEntrada.add(calculo);
                        idsCalculo.put(i, sol.IdInterno);
                        i++;
                    }
                }
            }
        }

        JsonObject respuesta = paridad.calcular(JsonLegacy.conNulos().toJsonTree(paridadEntrada).getAsJsonObject());
        CalcularParidadSalida paridadSalida = JsonLegacy.plano().fromJson(respuesta, CalcularParidadSalida.class);

        for (CalculoSalida calculo : paridadSalida.p_out_CalculosSalida) {
            Long idInternoSolicitud = idsCalculo.get(calculo.IdInterno);
            BigDecimal montoCustodia = patrimonioPorSolicitud.get(idInternoSolicitud) == null ? BigDecimal.ZERO
                    : patrimonioPorSolicitud.get(idInternoSolicitud);
            montoCustodia = montoCustodia.add(calculo.MontoDestino, MathContext.DECIMAL128);
            patrimonioPorSolicitud.put(idInternoSolicitud, montoCustodia);
        }

        for (Solicitud sol : entrada.p_Solicitudes) {
            SerieFondo serie = hashSeries.get(sol.IdSerieFondo);
            if (serie != null) {
                BigDecimal patrimonio = patrimonioPorSolicitud.get(sol.IdInterno) == null ? BigDecimal.ZERO : patrimonioPorSolicitud.get(sol.IdInterno);
                BigDecimal total = patrimonio.add(sol.MontoPatrimonio, MathContext.DECIMAL128);
                if (total.compareTo(serie.MontoPatrimonioEmisor) == -1) {
                    HashMap<String, String> parametros = new HashMap<>(1);
                    parametros.put("IdInstrumento", sol.IdSerieFondo);
                    out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(sol.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE",
                            parametros, diccionario, entrada.p_IdEmpresa));
                }
            } else {
                out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(sol.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE",
                        new HashMap<>(), diccionario, entrada.p_IdEmpresa));
            }
        }

        out.P_OUT_ERRORESCONTADOR = Long.valueOf(out.P_OUT_MENSAJES.size());
        return out;
    }
}

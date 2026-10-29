package cl.bcs.normativa.svc.service.interfaces;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InterfacesService {

    private static final Gson GSON = new Gson();

    private final GenerarInterfazC08CarteraNormalService c08CarteraNormal;
    private final GenerarInterfazC08CarteraIntermediadaService c08CarteraIntermediada;
    private final GenerarInterfazC08CarteraPacteadaService c08CarteraPacteada;
    private final GenerarInterfazC08InformacionContableService c08InformacionContable;
    private final GenerarInterfazC40CarteraNormalService c40CarteraNormal;
    private final GenerarInterfazC40CarteraIntermediadaService c40CarteraIntermediada;
    private final GenerarInterfazC40InformacionContableService c40InformacionContable;
    private final GenerarInterfazP40FilialService p40Filial;
    private final GenerarInterfazP40AseguradoraService p40Aseguradora;
    private final GenerarInterfazICUService icu;
    private final GenerarInterfazClienteService cliente;
    private final GenerarInterfazOrdenesService ordenes;
    private final GenerarInterfazSIGService sig;
    private final GenerarInterfazLavadodeDineroService lavadoDinero;
    private final GenerarInterfazInstrumentosFinancierosService instrumentosFinancieros;
    private final GenerarInterfazArchivoCMService archivoCM;
    private final GenerarInterfazAsignacionesService asignaciones;
    private final GenerarInterfazPolizasService polizas;
    private final GenerarInterfazMontosIntermediadosService montosIntermediados;

    public InterfacesService(
            GenerarInterfazC08CarteraNormalService c08CarteraNormal,
            GenerarInterfazC08CarteraIntermediadaService c08CarteraIntermediada,
            GenerarInterfazC08CarteraPacteadaService c08CarteraPacteada,
            GenerarInterfazC08InformacionContableService c08InformacionContable,
            GenerarInterfazC40CarteraNormalService c40CarteraNormal,
            GenerarInterfazC40CarteraIntermediadaService c40CarteraIntermediada,
            GenerarInterfazC40InformacionContableService c40InformacionContable,
            GenerarInterfazP40FilialService p40Filial,
            GenerarInterfazP40AseguradoraService p40Aseguradora,
            GenerarInterfazICUService icu,
            GenerarInterfazClienteService cliente,
            GenerarInterfazOrdenesService ordenes,
            GenerarInterfazSIGService sig,
            GenerarInterfazLavadodeDineroService lavadoDinero,
            GenerarInterfazInstrumentosFinancierosService instrumentosFinancieros,
            GenerarInterfazArchivoCMService archivoCM,
            GenerarInterfazAsignacionesService asignaciones,
            GenerarInterfazPolizasService polizas,
            GenerarInterfazMontosIntermediadosService montosIntermediados) {
        this.c08CarteraNormal         = c08CarteraNormal;
        this.c08CarteraIntermediada   = c08CarteraIntermediada;
        this.c08CarteraPacteada       = c08CarteraPacteada;
        this.c08InformacionContable   = c08InformacionContable;
        this.c40CarteraNormal         = c40CarteraNormal;
        this.c40CarteraIntermediada   = c40CarteraIntermediada;
        this.c40InformacionContable   = c40InformacionContable;
        this.p40Filial                = p40Filial;
        this.p40Aseguradora           = p40Aseguradora;
        this.icu                      = icu;
        this.cliente                  = cliente;
        this.ordenes                  = ordenes;
        this.sig                      = sig;
        this.lavadoDinero             = lavadoDinero;
        this.instrumentosFinancieros  = instrumentosFinancieros;
        this.archivoCM                = archivoCM;
        this.asignaciones             = asignaciones;
        this.polizas                  = polizas;
        this.montosIntermediados      = montosIntermediados;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> delegate(Object svc, Map<String, Object> p) {
        JsonObject params = GSON.toJsonTree(p).getAsJsonObject();
        JsonObject result = switch (svc) {
            case GenerarInterfazC08CarteraNormalService s       -> s.execute(params);
            case GenerarInterfazC08CarteraIntermediadaService s -> s.execute(params);
            case GenerarInterfazC08CarteraPacteadaService s     -> s.execute(params);
            case GenerarInterfazC08InformacionContableService s -> s.execute(params);
            case GenerarInterfazC40CarteraNormalService s       -> s.execute(params);
            case GenerarInterfazC40CarteraIntermediadaService s -> s.execute(params);
            case GenerarInterfazC40InformacionContableService s -> s.execute(params);
            case GenerarInterfazP40FilialService s              -> s.execute(params);
            case GenerarInterfazP40AseguradoraService s         -> s.execute(params);
            case GenerarInterfazICUService s                    -> s.execute(params);
            case GenerarInterfazClienteService s                -> s.execute(params);
            case GenerarInterfazOrdenesService s                -> s.execute(params);
            case GenerarInterfazSIGService s                    -> s.execute(params);
            case GenerarInterfazLavadodeDineroService s         -> s.execute(params);
            case GenerarInterfazInstrumentosFinancierosService s -> s.execute(params);
            case GenerarInterfazArchivoCMService s              -> s.execute(params);
            case GenerarInterfazAsignacionesService s           -> s.execute(params);
            case GenerarInterfazPolizasService s                -> s.execute(params);
            case GenerarInterfazMontosIntermediadosService s    -> s.execute(params);
            default -> throw new IllegalArgumentException("Unknown service: " + svc.getClass().getSimpleName());
        };
        return GSON.fromJson(result, Map.class);
    }

    public Map<String, Object> generarInterfazC08CarteraNormal(Map<String, Object> p)         { return delegate(c08CarteraNormal, p); }
    public Map<String, Object> generarInterfazC08CarteraIntermediada(Map<String, Object> p)   { return delegate(c08CarteraIntermediada, p); }
    public Map<String, Object> generarInterfazC08CarteraPacteada(Map<String, Object> p)       { return delegate(c08CarteraPacteada, p); }
    public Map<String, Object> generarInterfazC08InformacionContable(Map<String, Object> p)   { return delegate(c08InformacionContable, p); }
    public Map<String, Object> generarInterfazC40CarteraNormal(Map<String, Object> p)         { return delegate(c40CarteraNormal, p); }
    public Map<String, Object> generarInterfazC40CarteraIntermediada(Map<String, Object> p)   { return delegate(c40CarteraIntermediada, p); }
    public Map<String, Object> generarInterfazC40InformacionContable(Map<String, Object> p)   { return delegate(c40InformacionContable, p); }
    public Map<String, Object> generarInterfazP40(Map<String, Object> p)                      { return delegate(p40Filial, p); }
    public Map<String, Object> generarInterfazP40SBIF(Map<String, Object> p)                  { return delegate(p40Aseguradora, p); }
    public Map<String, Object> generarInterfazICU(Map<String, Object> p)                      { return delegate(icu, p); }
    public Map<String, Object> generarInterfazCliente(Map<String, Object> p)                  { return delegate(cliente, p); }
    public Map<String, Object> generarInterfazOrdenes(Map<String, Object> p)                  { return delegate(ordenes, p); }
    public Map<String, Object> generarInterfazSIG(Map<String, Object> p)                      { return delegate(sig, p); }
    public Map<String, Object> generarInterfazLavadoDinero(Map<String, Object> p)             { return delegate(lavadoDinero, p); }
    public Map<String, Object> generarInterfazInstrumentosFinancieros(Map<String, Object> p)  { return delegate(instrumentosFinancieros, p); }
    public Map<String, Object> generarInterfazArchivoCM(Map<String, Object> p)                { return delegate(archivoCM, p); }
    public Map<String, Object> generarInterfazAsignaciones(Map<String, Object> p)             { return delegate(asignaciones, p); }
    public Map<String, Object> generarInterfazPolizas(Map<String, Object> p)                  { return delegate(polizas, p); }
    public Map<String, Object> generarInterfazMontosIntermediados(Map<String, Object> p)      { return delegate(montosIntermediados, p); }
}

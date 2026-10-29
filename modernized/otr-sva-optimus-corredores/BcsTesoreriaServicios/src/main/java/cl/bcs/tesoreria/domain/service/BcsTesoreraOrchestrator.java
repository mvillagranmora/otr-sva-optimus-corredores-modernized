package cl.bcs.tesoreria.domain.service;

import cl.bcs.tesoreria.domain.model.*;
import cl.bcs.tesoreria.domain.port.out.PlataformaPort;
import cl.bcs.tesoreria.domain.port.out.TesoreriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BcsTesoreraOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(BcsTesoreraOrchestrator.class);

    private final PlataformaPort plataforma;
    private final TesoreriaRepository repository;

    public BcsTesoreraOrchestrator(PlataformaPort plataforma, TesoreriaRepository repository) {
        this.plataforma = plataforma;
        this.repository = repository;
    }

    public SalidaOrquestadorTesoreria ejecutar(EntradaOrquestadorTesoreria entrada) {
        List<String> mensajes = new ArrayList<>();
        List<ResultadoServicioTesoreria> resultados = new ArrayList<>();
        long erroresContador = 0;

        // H6: obtenerFechaSistema is a platform call — scope it inside simulation guard
        if (!"S".equals(entrada.simulaOperacion())) {
            plataforma.obtenerFechaSistema(entrada.idEmpresa());
        }

        List<ConfiguracionServicioTesoreria> configs =
                repository.consultarConfiguracionServicios(entrada.nombresServicios());

        ContextoServicioTesoreria ctx = new ContextoServicioTesoreria(
                entrada.idEmpresa(), entrada.idUsuario(), entrada.idModulo(), entrada.simulaOperacion());

        for (ConfiguracionServicioTesoreria config : configs) {
            try {
                List<ResultadoServicioTesoreria> res =
                        repository.ejecutarServicioTesoreria(config.beanName(), ctx);
                resultados.addAll(res);
            } catch (Exception e) {
                // H6: log the failure so on-call has signal; counter is additive (B-03)
                log.error("Service {} failed during orchestration: {}", config.beanName(), e.getMessage(), e);
                erroresContador++;
            }
        }

        // H6: compute result first, then persist independently — persist failure must not
        // prevent returning the accumulated result to the caller
        SalidaOrquestadorTesoreria salida =
                new SalidaOrquestadorTesoreria(erroresContador, mensajes, resultados);

        if (!"S".equals(entrada.simulaOperacion())) {
            repository.persistirResultado(salida);
        }

        return salida;
    }
}

package cl.bcs.contabilidad.domain.model;

import java.util.List;

/**
 * Input to the CentralizacionOrchestrator (multi-selection entry point).
 *
 * Derived from legacy CentralizaMultiSeleccion.EntradaCentralizaMultiSeleccion.
 * p_SimulaOperacion defaults to "N" in the legacy no-arg constructor.
 */
public record EntradaMultiSeleccion(
        Long         idEmpresa,
        Long         idUsuario,
        String       idModulo,
        String       fechaContable,
        String       fechaConsulta,
        Long         ejercicio,
        /** "S" = simulate (skip DB writes), "N" = real run. Legacy default: "N". */
        String       simulaOperacion,
        List<Long>   idsTipoCentralizacion
) {}

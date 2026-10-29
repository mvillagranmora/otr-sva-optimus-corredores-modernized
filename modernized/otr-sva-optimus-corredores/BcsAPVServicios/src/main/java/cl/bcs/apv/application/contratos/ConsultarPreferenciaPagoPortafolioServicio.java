package cl.bcs.apv.application.contratos;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.apv.api.casouso.ConsultarPreferenciaPagoPortafolio;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.consultarpreferenciapagoportafolio.ConsultarPreferenciaPagoPortafolioEntrada;
import cl.bcs.apv.api.dto.consultarpreferenciapagoportafolio.ConsultarPreferenciaPagoPortafolioResultado;
import cl.bcs.apv.api.dto.consultarpreferenciapagoportafolio.PreferenciaPagoPortafolio;
import cl.bcs.apv.application.comun.Mensajes;
import cl.bcs.apv.domain.port.out.contratos.PreferenciaPagoRepositorio;
import cl.bcs.apv.domain.port.out.contratos.PreferenciaPagoRepositorio.PreferenciaFila;

/** Use case {@code ConsultarPreferenciaPagoPortafolio} (legacy {@code cl.bcs.APV.ConsultarPreferenciaPagoPortafolio}); read only. */
public class ConsultarPreferenciaPagoPortafolioServicio implements ConsultarPreferenciaPagoPortafolio {

    private final PreferenciaPagoRepositorio repositorio;

    public ConsultarPreferenciaPagoPortafolioServicio(PreferenciaPagoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public ConsultarPreferenciaPagoPortafolioResultado ejecutar(ConsultarPreferenciaPagoPortafolioEntrada in) {
        List<Mensaje> mensajes = new ArrayList<>();
        List<PreferenciaPagoPortafolio> preferencias = new ArrayList<>();
        try {
            List<PreferenciaFila> filas = repositorio.preferenciasDelCliente(in.idCliente());
            for (PreferenciaFila f : filas) {
                preferencias.add(new PreferenciaPagoPortafolio(f.IdPreferenciaPagoPortafolio(), f.IdPortafolio()));
            }
            mensajes.add(Mensajes.de(1L, "APV", "OK", "Proceso Completado."));
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README APV-A, ConsultarPreferenciaPagoPortafolio): any failure is swallowed and answered as an "APV" message with the exception text;
            // its tipo is not ERROR, so the error counter stays 0
            mensajes.add(Mensajes.de(1L, "APV", "ERROR", e.getMessage()));
        }
        // legacy call(): the counter is set after logica
        return new ConsultarPreferenciaPagoPortafolioResultado(mensajes, Mensajes.numeroErrores(mensajes), preferencias);
    }
}

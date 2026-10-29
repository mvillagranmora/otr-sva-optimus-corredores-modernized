package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.sap.BusinessPartner;
import cl.bcs.contabilidad.domain.model.sap.JournalVoucher;
import cl.bcs.contabilidad.domain.model.sap.SapRespuesta;
import cl.bcs.contabilidad.domain.model.sap.SapSesion;

import java.util.List;

/**
 * SAP Business One Service Layer. The session operations are legacy {@code jobs/CallerWSJournalVoucher}
 * (CONT-B-60..65): a status outside the accepted set of an endpoint is retried; once the attempts are exhausted a
 * {@link cl.bcs.contabilidad.domain.model.sap.SapException} carries the last status and a fixed text.
 *
 * <p>URL, company, user and password come from configuration only; implementations never log or return the password.
 */
public interface SapGateway {

    /**
     * Posts centralization entries directly as a journal voucher. Not part of any migrated legacy flow (legacy posts
     * through the staging tables and {@link #enviarAsiento}); implementations may refuse it.
     */
    String postJournalEntries(List<ResultadoCentralizacion> entries);

    /** {@code POST /b1s/v1/Login}; accepted: 200. */
    SapSesion login();

    /** {@code GET /b1s/v1/BusinessPartners('cardCode')}; accepted: 200, 400, 403, 404. */
    SapRespuesta consultarSocio(SapSesion sesion, String cardCode);

    /** {@code POST /b1s/v1/BusinessPartners}; accepted: 200, 201, 400, 403. */
    SapRespuesta crearSocio(SapSesion sesion, BusinessPartner socio);

    /** {@code POST /b1s/v1/JournalVouchersService_Add}; accepted: 200, 204, 400. */
    SapRespuesta enviarAsiento(SapSesion sesion, JournalVoucher voucher);
}

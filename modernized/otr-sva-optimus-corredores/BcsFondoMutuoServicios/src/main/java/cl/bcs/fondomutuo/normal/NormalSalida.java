package cl.bcs.fondomutuo.normal;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.Pojos;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;

/** The output element of one request (legacy :1049-1074): the row, plus names of the fund and of the client, dates as text. */
final class NormalSalida {

    private NormalSalida() {
    }

    static ResultadoSolicitud armar(SolicitudAporteRescate sol, SerieFondoSolicitudes serie, ClientePortafolioSolicitudes cliente) {
        ResultadoSolicitud salida = new ResultadoSolicitud();
        salida = Pojos.copiar(sol, salida);
        if (serie != null) {
            salida.IdFondo = serie.IdFondo;
            salida.Nemotecnico = serie.Nemotecnico;
            salida.NombreFondo = serie.NombreFondo;
            salida.NombreCorto = serie.NombreCortoTipoFondo;
            salida.NombreSerieFondo = serie.NombreSerieFondo;
            salida.CodigoSerie = serie.CodigoSerie;
            salida.IdEmisorOrigen = serie.IdEmisor;
        }
        if (cliente != null) {
            salida.DocumentoIdentidad = cliente.DocumentoIdentidad;
            salida.CodigoPortafolio = cliente.CodigoPortafolio;
            salida.NombreCliente = cliente.NombreCompleto;
            salida.IdPortafolio = cliente.IdPortafolio;
        }
        salida.MontoTransferencia = sol.Monto;
        salida.NumeroCuotaTransferencia = sol.NumeroCuotas;
        salida.FechaAceptacion = FechaLegacy.texto(sol.FechaAceptacion);
        salida.FechaOriginalInversion = FechaLegacy.texto(sol.FechaOriginalInversion);
        salida.FechaIngresoRegistro = sol.FechaIngresoRegistro.toLocalDateTime().format(FechaLegacy.TS);
        salida.FechaLiquidacion = FechaLegacy.texto(sol.FechaLiquidacion);
        salida.FechaCertificacion = FechaLegacy.texto(sol.FechaCertificacion);
        salida.FechaValorCuota = FechaLegacy.texto(sol.FechaValorCuota);
        return salida;
    }
}

package cl.bcs.fondomutuo.normal;

import cl.bcs.fondomutuo.comisionnn.Pojos;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato;
import cl.bcs.fondomutuo.requisitos.contrato.FechaContrato;

/**
 * Step 4 binding of the TYPED ports of 3B ({@link FechaContrato.Puerto}, {@link CuotaContrato.Puerto}) to the 3A services: the request is copied field by
 * field (same names and types), the answer too; the only conversion is the date, {@code LocalDate} (3A, DEV-3A-1) to the {@code java.sql.Date} of the 3B contract.
 */
public final class Puertos3A {

    private Puertos3A() {
    }

    public static FechaContrato.Puerto fechas(CalcularFechaSolicitudAporteRescate servicio) {
        return entrada -> {
            CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate in = new CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate();
            in.p_IdEmpresa = entrada.p_IdEmpresa;
            in.p_IdPais = entrada.p_IdPais;
            for (FechaContrato.InFechas f : entrada.p_SolicitudAporteRescate) {
                in.p_SolicitudAporteRescate.add(Pojos.copiar(f, new CalcularFechaSolicitudAporteRescate.InFechas()));
            }
            FechaContrato.Salida salida = new FechaContrato.Salida();
            for (CalcularFechaSolicitudAporteRescate.ResultadoFecha r : servicio.logica(in).P_FECHASAPORTERESCATE) {
                FechaContrato.ResultadoFecha d = new FechaContrato.ResultadoFecha();
                d.IdInterno = r.IdInterno;
                d.IdSerieFondo = r.IdSerieFondo;
                d.FechaEmision = NormalCalculos.sql(r.FechaEmision);
                d.FechaDigitacion = NormalCalculos.sql(r.FechaDigitacion);
                d.FechaAceptacion = NormalCalculos.sql(r.FechaAceptacion);
                d.FechaValorCuota = NormalCalculos.sql(r.FechaValorCuota);
                d.FechaCertificacion = NormalCalculos.sql(r.FechaCertificacion);
                d.FechaLiquidacion = NormalCalculos.sql(r.FechaLiquidacion);
                salida.P_FECHASAPORTERESCATE.add(d);
            }
            return salida;
        };
    }

    public static CuotaContrato.Puerto cuotas(CalcularMontoNumeroCuota servicio) {
        return entrada -> {
            CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota in = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
            in.p_IdEmpresa = entrada.p_IdEmpresa;
            for (CuotaContrato.Serie s : entrada.p_Series) {
                in.p_Series.add(Pojos.copiar(s, new CalcularMontoNumeroCuota.Serie()));
            }
            CuotaContrato.Salida salida = new CuotaContrato.Salida();
            for (CalcularMontoNumeroCuota.Resultado r : servicio.logica(in).P_OUT_RESULTADO) {
                salida.P_OUT_RESULTADO.add(Pojos.copiar(r, new CuotaContrato.Resultado()));
            }
            return salida;
        };
    }
}

package cl.bcs.contabilidad.informe;

import cl.bcs.contabilidad.domain.model.basilea.CuentaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.DatoBusquedaBasilea;
import cl.bcs.contabilidad.domain.model.clienterelacionado.Comprobante;
import cl.bcs.contabilidad.domain.model.clienterelacionado.CondicionConcepto;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ConceptoNormativoConfig;
import cl.bcs.contabilidad.domain.model.clienterelacionado.FuentesOperaciones;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionDolar;
import cl.bcs.contabilidad.domain.model.clienterelacionado.PactoTermino;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ValorCondicion;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Test data builders for WP-D (informe cliente relacionado + interfaz Basilea).
 *
 * <p>Every RUT, name and portfolio code here is FAKE (same shape as the real ones:
 * 8-digit body + DV, upper-case person/company names, short portfolio code). No value was
 * copied from the legacy sources or from any database.
 */
final class ClienteRelacionadoFixtures {

    private ClienteRelacionadoFixtures() {}

    // Fake identities (shape of a Chilean RUT, not real persons).
    static final String RUT_PERSONA = "11111111-1";
    static final String NOMBRE_PERSONA = "JUAN PRUEBA FICTICIO";
    static final String RUT_EMPRESA = "22222222-2";
    static final String NOMBRE_EMPRESA = "INVERSIONES FICTICIA SPA";
    static final String PORTAFOLIO = "PF-0001";
    /** Fake RUT for the broker's own account (cuenta propia). */
    static final String RUT_CORREDORA = "76123456-K";

    static BigDecimal bd(String v) {
        return v == null ? null : new BigDecimal(v);
    }

    // ------------------------------------------------------------------ Comprobante

    static CompB comprobante(long folio) {
        return new CompB().folio(folio);
    }

    static final class CompB {
        Long folio; String fecha = "2025-03-05"; String nombre = NOMBRE_PERSONA; String portafolio = PORTAFOLIO;
        String doc = RUT_PERSONA; String tipoInstr = "ACN"; String tipoDoc = "FAC"; String cv = "C";
        String tipoOp = "COMACC"; String clasif = "RV"; String moneda = "CLP";
        BigDecimal operado = bd("0"), total = bd("0"), cobro = bd("0"), comision = bd("0"), gasto = bd("0"),
            impto = bd("0"), retencion = bd("0"), derecho = bd("0");

        CompB folio(Long v) { folio = v; return this; }
        CompB fecha(String v) { fecha = v; return this; }
        CompB nombre(String v) { nombre = v; return this; }
        CompB doc(String v) { doc = v; return this; }
        CompB tipoInstrumento(String v) { tipoInstr = v; return this; }
        CompB tipoDocumento(String v) { tipoDoc = v; return this; }
        CompB compraVenta(String v) { cv = v; return this; }
        CompB tipoOperacion(String v) { tipoOp = v; return this; }
        CompB clasificacion(String v) { clasif = v; return this; }
        CompB moneda(String v) { moneda = v; return this; }
        CompB operado(String v) { operado = bd(v); return this; }
        CompB total(String v) { total = bd(v); return this; }
        CompB cobro(String v) { cobro = bd(v); return this; }
        CompB comision(String v) { comision = bd(v); return this; }
        CompB gasto(String v) { gasto = bd(v); return this; }
        CompB impto(String v) { impto = bd(v); return this; }
        CompB retencion(String v) { retencion = bd(v); return this; }
        CompB derecho(String v) { derecho = bd(v); return this; }

        Comprobante build() {
            return new Comprobante(folio, fecha, nombre, portafolio, doc,
                tipoInstr, tipoDoc, cv, tipoOp, clasif,
                moneda, operado, total, cobro, comision, gasto, impto, retencion, derecho);
        }
    }

    // ------------------------------------------------------------------ OperacionDolar

    static DolarB dolar(long folio) {
        return new DolarB().folio(folio);
    }

    static final class DolarB {
        Long folio; String fecha = "2025-03-06"; String nombre = NOMBRE_EMPRESA; String portafolio = PORTAFOLIO;
        String doc = RUT_EMPRESA; String tipoInstr = "SPT"; String tipoDoc = "FAC"; String cv = "C";
        String tipoOp = "COMSPT"; String clasif = null;
        BigDecimal compra = bd("0"), venta = bd("0");

        DolarB folio(Long v) { folio = v; return this; }
        DolarB tipoInstrumento(String v) { tipoInstr = v; return this; }
        DolarB tipoDocumento(String v) { tipoDoc = v; return this; }
        DolarB compraVenta(String v) { cv = v; return this; }
        DolarB tipoOperacion(String v) { tipoOp = v; return this; }
        DolarB clasificacion(String v) { clasif = v; return this; }
        DolarB compra(String v) { compra = bd(v); return this; }
        DolarB venta(String v) { venta = bd(v); return this; }

        OperacionDolar build() {
            return new OperacionDolar(folio, fecha, nombre, portafolio, doc, tipoInstr, tipoDoc, cv, tipoOp, clasif,
                compra, venta);
        }
    }

    // ------------------------------------------------------------------ PactoTermino

    static PactoB pacto(long folio) {
        return new PactoB().folio(folio);
    }

    static final class PactoB {
        Long folio; String fecha = "2025-03-03"; String nombre = NOMBRE_PERSONA; String portafolio = PORTAFOLIO;
        String doc = RUT_PERSONA; String tipoInstr = "PDBC"; String tipoDoc = null; String cv = "V";
        String tipoOp = "VENPAC"; String clasif = null;
        String fechaInicio = "2025-03-03", fechaTermino = "2025-03-10";
        BigDecimal montoFinal = bd("0"), montoInicial = bd("0"), perdida = bd("0"), ganancia = bd("0");

        PactoB folio(Long v) { folio = v; return this; }
        PactoB tipoInstrumento(String v) { tipoInstr = v; return this; }
        PactoB tipoDocumento(String v) { tipoDoc = v; return this; }
        PactoB compraVenta(String v) { cv = v; return this; }
        PactoB tipoOperacion(String v) { tipoOp = v; return this; }
        PactoB fechaInicio(String v) { fechaInicio = v; return this; }
        PactoB fechaTermino(String v) { fechaTermino = v; return this; }
        PactoB montoInicial(String v) { montoInicial = bd(v); return this; }
        PactoB montoFinal(String v) { montoFinal = bd(v); return this; }
        PactoB perdida(String v) { perdida = bd(v); return this; }
        PactoB ganancia(String v) { ganancia = bd(v); return this; }

        PactoTermino build() {
            return new PactoTermino(folio, fecha, nombre, portafolio, doc, tipoInstr, tipoDoc, cv, tipoOp, clasif,
                fechaInicio, fechaTermino, montoFinal, montoInicial, perdida, ganancia);
        }
    }

    // ------------------------------------------------------------------ Conceptos normativos

    /** One condition group: IdTipoDatoEvento + values (ValorVarchar, OperadorFuncion "+"). */
    static CondicionConcepto cond(String idTipoDatoEvento, String... valores) {
        List<ValorCondicion> vs = new ArrayList<>();
        for (String v : valores) vs.add(new ValorCondicion(v, "+"));
        return new CondicionConcepto(idTipoDatoEvento, vs);
    }

    static CondicionConcepto condConOperador(String idTipoDatoEvento, String valor, String operador) {
        return new CondicionConcepto(idTipoDatoEvento, List.of(new ValorCondicion(valor, operador)));
    }

    static ConceptoNormativoConfig concepto(long id, String nombre, CondicionConcepto... condiciones) {
        return new ConceptoNormativoConfig(id, nombre, Arrays.asList(condiciones));
    }

    // ------------------------------------------------------------------ Fuentes

    static FuentesB fuentes() {
        return new FuentesB();
    }

    static final class FuentesB {
        List<Comprobante> comprobantes = new ArrayList<>();
        List<Comprobante> prestamos = new ArrayList<>();
        List<PactoTermino> pactos = new ArrayList<>();
        List<PactoTermino> terminos = new ArrayList<>();
        List<OperacionDolar> spots = new ArrayList<>();
        List<PactoTermino> terminosCartera = new ArrayList<>();
        List<OperacionDolar> forwards = new ArrayList<>();

        FuentesB comprobantes(Comprobante... v) { comprobantes.addAll(List.of(v)); return this; }
        FuentesB prestamos(Comprobante... v) { prestamos.addAll(List.of(v)); return this; }
        FuentesB pactos(PactoTermino... v) { pactos.addAll(List.of(v)); return this; }
        FuentesB terminos(PactoTermino... v) { terminos.addAll(List.of(v)); return this; }
        FuentesB spots(OperacionDolar... v) { spots.addAll(List.of(v)); return this; }
        FuentesB terminosCartera(PactoTermino... v) { terminosCartera.addAll(List.of(v)); return this; }
        FuentesB forwards(OperacionDolar... v) { forwards.addAll(List.of(v)); return this; }

        FuentesOperaciones build() {
            return new FuentesOperaciones(comprobantes, prestamos, pactos, terminos, spots, terminosCartera, forwards);
        }
    }

    // ------------------------------------------------------------------ Basilea

    /**
     * Builder for DatoBusquedaBasilea. Defaults mirror the LIBRE RF-IF row of
     * ProcesarInterfazBasilea_SelectRfIf.sql: empty strings for the columns the SQL selects as ''.
     */
    static DatoB dato() {
        return new DatoB();
    }

    static final class DatoB {
        String idBolsa = "", esVentaCorta = "", tipoCarteraCorredor = "CP", idClasificacionTipoInstrumento = "RF",
            idTipoInstrumento = "BONO", idEmisor = "", emisor = "EMISORX", nemotecnico = "", grupo = "LIBRE",
            producto = "RF-IF", idMonedaInstrumento = "CLP";
        BigDecimal monto = bd("1000000");
        String documentoIdentidadEmisor = "33333333-3", idTipoReajuste = "NR", fechaVencimiento = "2030-06-30",
            fechaVencimientoOperacion = "2025-10-01", fechaOperacion = "2024-01-15", fechaLiquidacion = "2024-01-16",
            plazo = "1750", folio = "123456";
        BigDecimal tasa = bd("3.25");
        String relacionadoCorredora = "", idTipoOperacion = "", idMonedaPrincipal = "USD", idMonedaSecundaria = "CLP",
            nemoBasilea = null;

        DatoB idBolsa(String v) { idBolsa = v; return this; }
        DatoB esVentaCorta(String v) { esVentaCorta = v; return this; }
        DatoB clasificacion(String v) { idClasificacionTipoInstrumento = v; return this; }
        DatoB tipoInstrumento(String v) { idTipoInstrumento = v; return this; }
        DatoB idEmisor(String v) { idEmisor = v; return this; }
        DatoB emisor(String v) { emisor = v; return this; }
        DatoB nemotecnico(String v) { nemotecnico = v; return this; }
        DatoB grupo(String v) { grupo = v; return this; }
        DatoB producto(String v) { producto = v; return this; }
        DatoB monedaInstrumento(String v) { idMonedaInstrumento = v; return this; }
        DatoB monto(String v) { monto = bd(v); return this; }
        DatoB tipoReajuste(String v) { idTipoReajuste = v; return this; }
        DatoB fechaVencimiento(String v) { fechaVencimiento = v; return this; }
        DatoB fechaVencimientoOperacion(String v) { fechaVencimientoOperacion = v; return this; }
        DatoB fechaOperacion(String v) { fechaOperacion = v; return this; }
        DatoB fechaLiquidacion(String v) { fechaLiquidacion = v; return this; }
        DatoB plazo(String v) { plazo = v; return this; }
        DatoB folio(String v) { folio = v; return this; }
        DatoB tasa(String v) { tasa = bd(v); return this; }
        DatoB relacionado(String v) { relacionadoCorredora = v; return this; }
        DatoB tipoOperacion(String v) { idTipoOperacion = v; return this; }
        DatoB monedaPrincipal(String v) { idMonedaPrincipal = v; return this; }
        DatoB monedaSecundaria(String v) { idMonedaSecundaria = v; return this; }
        DatoB nemoBasilea(String v) { nemoBasilea = v; return this; }

        DatoBusquedaBasilea build() {
            return new DatoBusquedaBasilea(idBolsa, esVentaCorta, tipoCarteraCorredor, idClasificacionTipoInstrumento,
                idTipoInstrumento, idEmisor, emisor, nemotecnico, grupo, producto, idMonedaInstrumento, monto,
                documentoIdentidadEmisor, idTipoReajuste, fechaVencimiento, fechaVencimientoOperacion, fechaOperacion,
                fechaLiquidacion, plazo, folio, tasa, relacionadoCorredora, idTipoOperacion, idMonedaPrincipal,
                idMonedaSecundaria, nemoBasilea);
        }
    }

    /** Builder for CuentaBasilea; defaults match the default DatoB (LIBRE / RF-IF / RF / BONO, all filters ''). */
    static CuentaB cuenta(String cuentaContable) {
        return new CuentaB().cuentaContable(cuentaContable);
    }

    static final class CuentaB {
        String concepto = "CAPITAL VALOR A MERCADO", tipoCuenta = "TITULOS (ACTIVO)", cuentaContable,
            nombreCuenta = "CUENTA FICTICIA", idBolsa = "", esVentaCorta = "", relacionado = "",
            clasif = "RF", tipoInstr = "BONO", idEmisor = "", nemotecnico = "", grupo = "LIBRE", producto = "RF-IF",
            idEstado = "ACT", tipoOp = "";

        CuentaB cuentaContable(String v) { cuentaContable = v; return this; }
        CuentaB idBolsa(String v) { idBolsa = v; return this; }
        CuentaB esVentaCorta(String v) { esVentaCorta = v; return this; }
        CuentaB relacionado(String v) { relacionado = v; return this; }
        CuentaB clasificacion(String v) { clasif = v; return this; }
        CuentaB tipoInstrumento(String v) { tipoInstr = v; return this; }
        CuentaB idEmisor(String v) { idEmisor = v; return this; }
        CuentaB nemotecnico(String v) { nemotecnico = v; return this; }
        CuentaB grupo(String v) { grupo = v; return this; }
        CuentaB producto(String v) { producto = v; return this; }
        CuentaB tipoOperacion(String v) { tipoOp = v; return this; }

        CuentaBasilea build() {
            return new CuentaBasilea(concepto, tipoCuenta, cuentaContable, nombreCuenta, idBolsa, esVentaCorta,
                relacionado, clasif, tipoInstr, idEmisor, nemotecnico, grupo, producto, idEstado, tipoOp);
        }
    }

    // ------------------------------------------------------------------ misc

    /** A real (decodable) 1x1 PNG, generated in memory: POI's Picture.resize() needs to read its size. */
    static byte[] pngUnPixel() {
        try {
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

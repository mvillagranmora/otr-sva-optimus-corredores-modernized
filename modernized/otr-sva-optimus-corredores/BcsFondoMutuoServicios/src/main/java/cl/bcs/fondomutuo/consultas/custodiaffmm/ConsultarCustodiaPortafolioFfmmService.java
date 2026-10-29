package cl.bcs.fondomutuo.consultas.custodiaffmm;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Legacy {@code ConsultarCustodiaPortafolioFFMM.logica} (FM-CPFF-01..22, read only), bug for bug: the company is read FIRST and always, then {@code PLA.FechaSistema}, the dates, the
 * guarantee and custody queries, the related persons (always) and the grouping into {@code HashMap}s of the DEFAULT capacity (custody, guarantee, group) filled in the order of the rows;
 * the output order is the iteration order of the group map (D-F3C-13). The source is the current one when the date is the default text or NOT BEFORE the system date (D-F3C-04). With a
 * guarantee the price amount is reset to zero (D-F3C-01: {@code MontoCompraCarteraOrigen} is never copied to the grouped guarantee). {@code p_IdBoveda}, {@code p_IdUsuario} and
 * {@code p_IdModulo} are never read (D-F3C-03). No requirements check, no calendar, no prices, no state; the unit of work is the ambient read-only transaction.
 */
public class ConsultarCustodiaPortafolioFfmmService implements ConsultarCustodiaPortafolioFfmmPort {

    private static final String FECHA_DEFECTO = "0001-01-01";
    private static final String SI = "S";

    private final ConfiguracionSistemaPort configuracion;
    private final EmpresaPort empresas;
    private final ConsultarCustodiaPortafolioFfmmDatos datos;

    public ConsultarCustodiaPortafolioFfmmService(ConfiguracionSistemaPort configuracion, EmpresaPort empresas, ConsultarCustodiaPortafolioFfmmDatos datos) {
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.datos = datos;
    }

    @Override
    public ConsultarCustodiaPortafolioFfmmContrato.Salida logica(ConsultarCustodiaPortafolioFfmmContrato.Entrada entrada) {
        ConsultarCustodiaPortafolioFfmmContrato.Salida out = new ConsultarCustodiaPortafolioFfmmContrato.Salida();

        InformacionEmpresa empresa = empresas.informacion(entrada.p_IdEmpresa);

        String fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

        LocalDate dFechaEntrada = FechaLegacy.aFecha(entrada.p_FechaConsulta);
        LocalDate dFechaSistema = FechaLegacy.aFecha(fechaSistema);

        ConsultarCustodiaPortafolioFfmmDatos.Parametros params = new ConsultarCustodiaPortafolioFfmmDatos.Parametros();
        params.IdEmpresa = entrada.p_IdEmpresa;
        params.IdCliente = entrada.p_IdCliente;
        params.IdPortafolio = entrada.p_IdPortafolio;
        params.IdInstrumento = entrada.p_IdInstrumento;
        params.FechaConsulta = entrada.p_FechaConsulta;
        params.IdProductoAgrupador = entrada.p_IdProductoAgrupador;
        params.IdEmisor = entrada.p_IdEmisor;
        params.IdFamiliaFondo = entrada.p_IdFamiliaFondo;

        // legacy java.sql.Date.compareTo(..) != -1: "not before" (NOT LocalDate.compareTo, which returns day / month / year differences)
        boolean vigente = FECHA_DEFECTO.equals(entrada.p_FechaConsulta) || !dFechaEntrada.isBefore(dFechaSistema);

        List<ConsultarCustodiaPortafolioFfmmContrato.Garantia> listGarantias = vigente ? datos.garantiaVigente(params) : datos.garantiaHistorico(params);

        params.TipoFechaConsulta = entrada.p_TipoFechaConsulta;
        List<ConsultarCustodiaPortafolioFfmmContrato.Custodia> listCustodias = vigente ? datos.custodiaVigente(params) : datos.custodiaHistorico(params);

        List<ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino> listPersona = datos.personasDestino(empresa.IdPersona);
        HashMap<Long, ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino> hPersonaDestino = personasPorId(listPersona);

        HashMap<String, ConsultarCustodiaPortafolioFfmmContrato.Custodia> hCustodia = new HashMap<String, ConsultarCustodiaPortafolioFfmmContrato.Custodia>();
        HashMap<String, ConsultarCustodiaPortafolioFfmmContrato.Garantia> hGarantia = new HashMap<String, ConsultarCustodiaPortafolioFfmmContrato.Garantia>();
        HashMap<String, ConsultarCustodiaPortafolioFfmmContrato.Instrumento> hInstrumento = new HashMap<String, ConsultarCustodiaPortafolioFfmmContrato.Instrumento>();

        for (ConsultarCustodiaPortafolioFfmmContrato.Custodia cus : listCustodias) {
            String key = cus.IdInstrumento + "_" + cus.Nemotecnico + "_" + cus.IdEmisor + "_" + cus.IdCliente + "_" + cus.IdPortafolio + "_" + cus.IdRegimenFondoMutuo;
            String relacion = "";
            String boveda = "";
            if (SI.equals(entrada.p_AgrupaPorRelacion)) {
                if (cus.IdCliente.equals(empresa.IdCliente)) relacion = "CPRO";
                else if (hPersonaDestino.get(cus.IdPersona) != null) relacion = "REL";
                else relacion = "TER";
                key += "_" + relacion;
            }
            if (SI.equals(entrada.p_AgrupaPorBoveda)) {
                boveda = cus.IdBoveda;
                key += "_" + boveda;
            }
            ConsultarCustodiaPortafolioFfmmContrato.Custodia custodia = hCustodia.get(key);
            if (custodia == null) {
                custodia = new ConsultarCustodiaPortafolioFfmmContrato.Custodia();
                custodia.IdClasificacionTipoInstrumento = cus.IdClasificacionTipoInstrumento;
                custodia.NombreClasificacionTipoInstrumento = cus.NombreClasificacionTipoInstrumento;
                custodia.IdTipoInstrumento = cus.IdTipoInstrumento;
                custodia.NombreTipoInstrumento = cus.NombreTipoInstrumento;
                custodia.IdInstrumento = cus.IdInstrumento;
                custodia.Nemotecnico = cus.Nemotecnico;
                custodia.IdEmisor = cus.IdEmisor;
                custodia.IdCliente = cus.IdCliente;
                custodia.IdPortafolio = cus.IdPortafolio;
                custodia.Relacion = relacion;
                custodia.IdBoveda = boveda;
                custodia.IdRegimenFondoMutuo = cus.IdRegimenFondoMutuo;
                custodia.RegimenTributario = cus.RegimenTributario;
                custodia.IdSocioDeNegocio = cus.IdSocioDeNegocio;
                custodia.DocumentoIdentidad = cus.DocumentoIdentidad;
                custodia.RazonSocial = cus.RazonSocial;
                custodia.CodigoPortafolio = cus.CodigoPortafolio;
                custodia.Producto = cus.Producto;
                custodia.EsADC = cus.EsADC;
                custodia.IdMatrizAgrupador = cus.IdMatrizAgrupador;

                if (hInstrumento.get(key) == null) {
                    ConsultarCustodiaPortafolioFfmmContrato.Instrumento instrumento = new ConsultarCustodiaPortafolioFfmmContrato.Instrumento();
                    copiarDescriptivos(custodia, instrumento);
                    instrumento.Id = key;
                    hInstrumento.put(key, instrumento);
                }
            }
            custodia.Cantidad = custodia.Cantidad.add(cus.Cantidad, MathContext.DECIMAL128);
            custodia.ValorCompra = custodia.ValorCompra.add(cus.ValorCompra, MathContext.DECIMAL128);
            custodia.PrecioCompra = cus.PrecioCompra;
            custodia.MontoCompra = custodia.MontoCompra.add(cus.MontoCompra, MathContext.DECIMAL128);
            custodia.MontoPrecioPromedio = custodia.MontoPrecioPromedio.add(cus.Cantidad.multiply(cus.PrecioCompra, MathContext.DECIMAL128), MathContext.DECIMAL128);

            hCustodia.put(key, custodia);
        }

        for (ConsultarCustodiaPortafolioFfmmContrato.Garantia gar : listGarantias) {
            String key = gar.IdInstrumento + "_" + gar.Nemotecnico + "_" + gar.IdEmisor + "_" + gar.IdCliente + "_" + gar.IdPortafolio + "_" + gar.IdRegimenFondoMutuo;
            String relacion = "";
            String boveda = "";
            if (SI.equals(entrada.p_AgrupaPorRelacion)) {
                if (gar.IdCliente.equals(empresa.IdCliente)) relacion = "CPRO";
                else if (hPersonaDestino.get(gar.IdPersona) != null) relacion = "REL";
                else relacion = "TER";
                key += "_" + relacion;
            }
            if (SI.equals(entrada.p_AgrupaPorBoveda)) {
                boveda = gar.IdBoveda;
                key += "_" + boveda;
            }
            ConsultarCustodiaPortafolioFfmmContrato.Garantia garantia = hGarantia.get(key);
            if (garantia == null) {
                garantia = new ConsultarCustodiaPortafolioFfmmContrato.Garantia();
                garantia.IdClasificacionTipoInstrumento = gar.IdClasificacionTipoInstrumento;
                garantia.NombreClasificacionTipoInstrumento = gar.NombreClasificacionTipoInstrumento;
                garantia.IdTipoInstrumento = gar.IdTipoInstrumento;
                garantia.NombreTipoInstrumento = gar.NombreTipoInstrumento;
                garantia.IdInstrumento = gar.IdInstrumento;
                garantia.Nemotecnico = gar.Nemotecnico;
                garantia.IdEmisor = gar.IdEmisor;
                garantia.IdCliente = gar.IdCliente;
                garantia.IdPortafolio = gar.IdPortafolio;
                garantia.Relacion = relacion;
                garantia.IdBoveda = boveda;
                garantia.IdRegimenFondoMutuo = gar.IdRegimenFondoMutuo;
                garantia.IdSocioDeNegocio = gar.IdSocioDeNegocio;
                garantia.DocumentoIdentidad = gar.DocumentoIdentidad;
                garantia.RazonSocial = gar.RazonSocial;
                garantia.CodigoPortafolio = gar.CodigoPortafolio;
                garantia.Producto = gar.Producto;
                garantia.EsADC = gar.EsADC;
                garantia.IdMatrizAgrupador = gar.IdMatrizAgrupador;

                if (hInstrumento.get(key) == null) {
                    ConsultarCustodiaPortafolioFfmmContrato.Instrumento instrumento = new ConsultarCustodiaPortafolioFfmmContrato.Instrumento();
                    copiarDescriptivos(garantia, instrumento);
                    instrumento.Id = key;
                    hInstrumento.put(key, instrumento);
                }
            }
            garantia.Cantidad = garantia.Cantidad.add(gar.Cantidad, MathContext.DECIMAL128);
            hGarantia.put(key, garantia);
        }

        for (Map.Entry<String, ConsultarCustodiaPortafolioFfmmContrato.Instrumento> obj : hInstrumento.entrySet()) {
            ConsultarCustodiaPortafolioFfmmContrato.Instrumento ins = obj.getValue();
            ConsultarCustodiaPortafolioFfmmContrato.SalidaCustodia salida = new ConsultarCustodiaPortafolioFfmmContrato.SalidaCustodia();
            salida.IdClasificacionTipoInstrumento = ins.IdClasificacionTipoInstrumento;
            salida.NombreClasificacionTipoInstrumento = ins.NombreClasificacionTipoInstrumento;
            salida.IdTipoInstrumento = ins.IdTipoInstrumento;
            salida.NombreTipoInstrumento = ins.NombreTipoInstrumento;
            salida.IdInstrumento = ins.IdInstrumento;
            salida.Nemotecnico = ins.Nemotecnico;
            salida.IdEmisor = ins.IdEmisor;
            salida.IdSocioDeNegocio = ins.IdSocioDeNegocio;
            salida.IdCliente = ins.IdCliente;
            salida.IdPortafolio = ins.IdPortafolio;
            salida.DocumentoIdentidad = ins.DocumentoIdentidad;
            salida.RazonSocial = ins.RazonSocial;
            salida.CodigoPortafolio = ins.CodigoPortafolio;
            salida.IdBoveda = ins.IdBoveda;
            salida.Relacion = ins.Relacion;
            salida.IdRegimenFondoMutuo = ins.IdRegimenFondoMutuo;
            salida.RegimenTributario = ins.RegimenTributario;
            salida.Producto = ins.Producto;
            salida.EsADC = ins.EsADC;
            salida.IdMatrizAgrupador = ins.IdMatrizAgrupador;
            salida.CantidadPrecioPromedio = BigDecimal.ZERO;
            salida.MontoPrecioPromedio = BigDecimal.ZERO;
            ConsultarCustodiaPortafolioFfmmContrato.Custodia custodia = hCustodia.get(ins.Id);
            if (custodia == null) {
                salida.CantidadCustodia = BigDecimal.ZERO;
                salida.INGPrecioPromedioCompra = BigDecimal.ZERO;
                salida.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
                salida.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
            } else {
                salida.CantidadCustodia = custodia.Cantidad;
                salida.CantidadPrecioPromedio = custodia.Cantidad;

                if (custodia.Cantidad.compareTo(BigDecimal.ZERO) == 0) {
                    salida.INGPrecioPromedioCompra = BigDecimal.ZERO;
                    salida.INGMontoPrecioPromedioMonedaInstrumento = BigDecimal.ZERO;
                    salida.INGMontoPrecioPromedioCompra = BigDecimal.ZERO;
                } else {
                    salida.INGPrecioPromedioCompra = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128);
                    salida.INGMontoPrecioPromedioMonedaInstrumento = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128).multiply(custodia.Cantidad, MathContext.DECIMAL128);
                    salida.INGMontoPrecioPromedioCompra = custodia.MontoCompra.divide(custodia.Cantidad, MathContext.DECIMAL128).multiply(custodia.Cantidad, MathContext.DECIMAL128);
                }
                salida.MontoPrecioPromedio = salida.MontoPrecioPromedio.add(custodia.MontoPrecioPromedio);
            }

            ConsultarCustodiaPortafolioFfmmContrato.Garantia garantia = hGarantia.get(ins.Id);
            if (garantia == null) {
                salida.CantidadGarantia = BigDecimal.ZERO;
            } else {
                salida.CantidadGarantia = garantia.Cantidad;
                salida.CantidadPrecioPromedio = salida.CantidadPrecioPromedio.add(garantia.Cantidad);
                if (garantia.MontoCompraCarteraOrigen != null) {
                    salida.MontoPrecioPromedio = salida.MontoPrecioPromedio.add(garantia.MontoCompraCarteraOrigen, MathContext.DECIMAL128);
                } else {
                    salida.MontoPrecioPromedio = BigDecimal.ZERO;
                }
            }
            salida.TotalPosicion = salida.CantidadCustodia.add(salida.CantidadGarantia, MathContext.DECIMAL128);

            if (salida.CantidadPrecioPromedio.compareTo(BigDecimal.ZERO) != 0) {
                salida.PPPCustodia = salida.MontoPrecioPromedio.divide(salida.CantidadPrecioPromedio, MathContext.DECIMAL128);
            }

            out.p_out_tbl.add(salida);
        }
        return out;
    }

    /** Legacy {@code Utils.List2Hash(list, "IdPersonaDestino")}: a repeated key is {@code Exception("key field is not unique")}, a null key is accepted (D-F3C-18). */
    private static HashMap<Long, ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino> personasPorId(List<ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino> lista) {
        try {
            return ListaHash.aHash(lista, p -> p.IdPersonaDestino);
        } catch (Exception e) {
            throw ConsultarCustodiaPortafolioFfmmService.<RuntimeException>lanzar(e);
        }
    }

    /** The legacy {@code throws Exception} of {@code logica} is unchecked here: the same exception object goes up. */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> E lanzar(Throwable e) throws E {
        throw (E) e;
    }

    /** Legacy {@code Utils.copyPojo(origen, destino)}: the fields of the group holder by name (the quantities are not part of it; a guarantee has no {@code RegimenTributario}). */
    private static void copiarDescriptivos(ConsultarCustodiaPortafolioFfmmContrato.Custodia o, ConsultarCustodiaPortafolioFfmmContrato.Instrumento d) {
        d.IdClasificacionTipoInstrumento = o.IdClasificacionTipoInstrumento;
        d.NombreClasificacionTipoInstrumento = o.NombreClasificacionTipoInstrumento;
        d.IdTipoInstrumento = o.IdTipoInstrumento;
        d.NombreTipoInstrumento = o.NombreTipoInstrumento;
        d.IdInstrumento = o.IdInstrumento;
        d.Nemotecnico = o.Nemotecnico;
        d.IdEmisor = o.IdEmisor;
        d.IdCliente = o.IdCliente;
        d.IdPortafolio = o.IdPortafolio;
        d.IdSocioDeNegocio = o.IdSocioDeNegocio;
        d.DocumentoIdentidad = o.DocumentoIdentidad;
        d.RazonSocial = o.RazonSocial;
        d.CodigoPortafolio = o.CodigoPortafolio;
        d.IdBoveda = o.IdBoveda;
        d.Relacion = o.Relacion;
        d.IdRegimenFondoMutuo = o.IdRegimenFondoMutuo;
        d.Producto = o.Producto;
        d.EsADC = o.EsADC;
        d.IdMatrizAgrupador = o.IdMatrizAgrupador;
        d.RegimenTributario = o.RegimenTributario;
    }

    private static void copiarDescriptivos(ConsultarCustodiaPortafolioFfmmContrato.Garantia o, ConsultarCustodiaPortafolioFfmmContrato.Instrumento d) {
        d.IdClasificacionTipoInstrumento = o.IdClasificacionTipoInstrumento;
        d.NombreClasificacionTipoInstrumento = o.NombreClasificacionTipoInstrumento;
        d.IdTipoInstrumento = o.IdTipoInstrumento;
        d.NombreTipoInstrumento = o.NombreTipoInstrumento;
        d.IdInstrumento = o.IdInstrumento;
        d.Nemotecnico = o.Nemotecnico;
        d.IdEmisor = o.IdEmisor;
        d.IdCliente = o.IdCliente;
        d.IdPortafolio = o.IdPortafolio;
        d.IdSocioDeNegocio = o.IdSocioDeNegocio;
        d.DocumentoIdentidad = o.DocumentoIdentidad;
        d.RazonSocial = o.RazonSocial;
        d.CodigoPortafolio = o.CodigoPortafolio;
        d.IdBoveda = o.IdBoveda;
        d.Relacion = o.Relacion;
        d.IdRegimenFondoMutuo = o.IdRegimenFondoMutuo;
        d.Producto = o.Producto;
        d.EsADC = o.EsADC;
        d.IdMatrizAgrupador = o.IdMatrizAgrupador;
    }
}

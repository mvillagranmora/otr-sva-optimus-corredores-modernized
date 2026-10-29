SELECT
 CSFM."IdCliente"
, CSFM."TipoSolicitud"
, CSFM."FechaConsulta"
, CSFM."Origen"
, CSFM."NumSolicitud"
, CSFM."DetSolicitud"
, CSFM."EstadoDetalle"
, CSFM."Rellamada"
, CSFM."OrigenSolicitud"
, CSFM."FechaSolicitud"
, CSFM."HoraSolicitud"
, CSFM."FechaCierre"
, CSFM."TipoTraspaso"
, CSFM."MotivoRechazo"
, CSFM."Folio"
, CSFM."FolioRelacionado"
, CSFM."RutCliente"
, CSFM."CodFondoOrigen"
, CSFM."NombreFondoOrigen"
, CSFM."SerieFondoOrigen"
, CSFM."RegimenFondoOrigen"
, CSFM."MontoPesosOrigen"
, CSFM."MontoCuotasOrigen"
, CSFM."ValorCuotaOrigen"
, CSFM."CodFondoDestino"
, CSFM."NombreFondoDestino"
, CSFM."SerieFondoDestino"
, CSFM."RegimenFondoDestino"
, CSFM."MontoPesosDestino"
, CSFM."MontoCuotasDestino"
, CSFM."ValorCuotaDestino"
, CSFM."MontoCuotasComision"
, CSFM."PorcentajeOrigen"
, CSFM."PorcentajeDestino"
, CSFM."NombreCliente"
, CSFM."ClasificacionMLT"
, CSFM."DescripcionTipoAhorro"
, CSFM."DescripcionTipoCuenta"
, CSFM."FechaValorCuota"
, CSFM."MontoCuotas"
, CSFM."MontoPesos"
, CSFM."NombreAdministradora"
, CSFM."OrigenValorCuota"
, CSFM."RegimenTributario"
, CSFM."SerieFondo"
, CSFM."TipoAhorro"
, CSFM."TipoCuenta"
, CSFM."TipoOperacion"
, CSFM."ValorCuota"
, CSFM."TipoMovimiento"
, CSFM."IdMatrizAgrupador"
, CSFM."EsADC"
, CSFM."FechaCertificacion"
, CSFM."FechaLiquidacion"
, CSFM."FechaCurse"
, CSFM."FechaValorCuotaOrigen"
, CSFM."FechaValorCuotaDestino"
, CSFM."IdSerieFondo" as "IdSerieFondoOrigen"
, CSFM."IdSerieFondoDestino"
, CSFM."IngresoAutomatico"
, CSFM."FolioContrato"
, CSFM."Canal"
, CSFM."DescripcionCanal"
, CSFM."IdMedioDePago"
, CSFM."DescripcionMedioPago"
, CSFM."IdContrato"
, CSFM."FechaFinalReinversion"
, COALESCE(CD."IdComprobante", 0) AS "IdComprobante"
, CSFM."FechaIngresoLocal"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesFFMM" CSFM
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" INEG ON CSFM."IdIngresoEgreso" = INEG."IdIngresoEgreso" AND CSFM."IdIngresoEgreso" > 0 AND INEG."IdTipoOperacion" IN ('RESCFM', 'APORFM')
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" CDO ON INEG."IdIngresoEgreso" = CDO."IdIngresoEgreso" AND CDO."IdComprobanteDetalle" NOT IN (SELECT "IdComprobanteDetalle" FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" WHERE "IdEstado" = 'ANU')
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD  ON CDO."IdComprobanteDetalle" = CD."IdComprobanteDetalle" AND CD."IdEstado" NOT IN ('ANU')
WHERE (:FiltraCliente = 'N' OR CSFM."IdCliente" = :IdCliente)
AND (:FiltraTipoSolicitud = 'N' OR CSFM."TipoSolicitud" = :TipoSolicitud)
AND (:FiltraTipoMovimiento = 'N' OR CSFM."TipoMovimiento" IN(SELECT "IdTipoMovimientoFondoMutuo" FROM "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" WHERE "IdTipoMovimientoFondoMutuo" != 'INGGTA'))
AND (:FiltraOrigen = 'N' OR CSFM."Origen" = :Origen)
AND (:FiltraFolio = 'N' OR CSFM."Folio" = :Folio)
AND (:FiltraTipoAhorro = 'N' OR CSFM."TipoAhorro" = :TipoAhorro)
AND (:FiltraTipoCuenta = 'N' OR CSFM."TipoCuenta" = :TipoCuenta)
AND (:FiltraIdSerieFondo = 'N' OR CSFM."IdSerieFondo" = :IdSerieFondo)
AND (:FiltraFechaDesde = 'N' OR CSFM."FechaSolicitud" >= :FechaDesde)
AND (:FiltraFechaHasta = 'N' OR CSFM."FechaSolicitud" <= :FechaHasta)
AND (:FiltraADC = 'N' OR CSFM."EsADC" = :EsADC)
AND (:FiltraIngresoAutomatico = 'N' OR CSFM."IngresoAutomatico" = :IngresoAutomatico)

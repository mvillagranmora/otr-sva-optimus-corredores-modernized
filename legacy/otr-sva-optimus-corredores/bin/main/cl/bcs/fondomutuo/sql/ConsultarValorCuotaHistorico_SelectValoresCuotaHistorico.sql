SELECT
"IdValorCuotaHistorico"
,"IdSerieFondo"
,"FechaVigencia"
,"ValorCuota" AS "ValorCuotaMonedaOrigen"
,"IdMoneda"
,"Observacion"
,"FechaCalculo"
,"FechaIngresoRegistro"
,"IdEmpresa"
,"DecimalesValorCuota"
,"CodigoSerie"
,"IdFamiliaFondo"
,"IdInstrumento"
,"NombreSerieFondo"
,"Nemotecnico"
,"IdEmisor"
,"RazonSocialFondo"
,"NombreFamilia"
,"NombreEmisor"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico"
WHERE 
(:FiltraPorNemotecnico = 0 OR "Nemotecnico" = :Nemotecnico)
AND (:FiltraPorIdSerieFondo = 0 OR "IdSerieFondo" = :IdSerieFondo)
AND (:FiltraPorIdFamiliaFondo = 0 OR "IdFamiliaFondo" = :IdFamiliaFondo)
AND (:FiltraPorIdInstrumento = 0 OR "IdInstrumento" = :IdInstrumento)
AND (:FiltraPorIdEmisor = 0 OR "IdEmisor" = :IdEmisor)
AND (:FiltraPorFechaVigenciaDesde = 0 OR "FechaVigencia" >= :FechaVigenciaDesde)
AND (:FiltraPorFechaVigenciaHasta = 0 OR "FechaVigencia" <= :FechaVigenciaHasta);
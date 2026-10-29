SELECT
SerieFondo."IdSerieFondo"
,Fondo."EstadoAporteComercial"
,Fondo."EstadoRescateComercial"
,Fondo."EstadoAporteBackoffice"
,Fondo."EstadoRescateBackoffice"
,Fondo."EstadoLiquidezComercial"
,Fondo."EstadoLiquidezBackoffice"
,SerieFondo."DiasRetencionesAporte"
,SerieFondo."DiasRetencionesRescate"
,SerieFondo."DiasValutaValorCuotaAporte"
,SerieFondo."DiasValutaValorCuotaRescate"
,SerieFondo."DiasValutaCertificacionAporte" 
,SerieFondo."DiasValutaCertificacionRescate"
,SerieFondo."DiasValutaCurseAporte"
,SerieFondo."DiasValutaCurseRescate"
,Fondo."IdEmisor"
,Fondo."FondoExtranjero"
,Fondo."IdPaisOrigen" AS "IdPaisFondo"
,Fondo."IdPaisEmisor"
,Moneda."IdPais" AS "IdPaisMoneda"
,Fondo."IdMercadoFondoMutuo"
,CASE WHEN Fondo."IdPaisOrigen" = ConfPaisSistema."Valor" AND Fondo."IdMoneda" = ConfMoneda."Valor" THEN 'S' ELSE 'N' END AS "FondoNacionalDolar"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFondo
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (SerieFondo."IdFondo" = Fondo."IdFondo" AND SerieFondo."IdEmpresa" = Fondo."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda ON (Moneda."IdMoneda" = Fondo."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS ConfMoneda ON (ConfMoneda."IdEmpresa" = SerieFondo."IdEmpresa" AND ConfMoneda."CodigoParametro" = 'PLA.MonedaAdicional')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS ConfPaisSistema ON (ConfPaisSistema."IdEmpresa" = SerieFondo."IdEmpresa" AND ConfPaisSistema."CodigoParametro" = 'PLA.PaisSistema')
WHERE SerieFondo."IdSerieFondo" IN (:IdSerieFondo) AND SerieFondo."IdEmpresa" = :IdEmpresa;
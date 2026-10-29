SELECT 
	GAR."EGRIdMovimientoCarteraEnTransito" AS "IdMovimientoCarteraEnTransito"
	,GAR."IdGarantia"
	,GARVIG."IdGarantia" AS "IdGarantiaVigente"
	,GAR."IdContraparteGarantia"
	,MOV."IdBolsa"
	,GAR."IdAgente"
	,CASE WHEN GARVIG."IdEstado" = 'LIQ' THEN 0.0 WHEN GARVIG."IdEstado" = 'VIG' THEN GARVIG."INGCantidad" END AS "CantidadVigente"
	,CASE WHEN GARVIG."IdEstado" = 'LIQ' THEN 0.0 WHEN GARVIG."IdEstado" = 'VIG' THEN GARVIG."INGMontoATasaGarantiaActual" END AS "INGMontoATasaGarantiaActualVigente"
	,CASE WHEN GARVIG."IdEstado" = 'LIQ' THEN 0.0 WHEN GARVIG."IdEstado" = 'VIG' THEN GARVIG."INGMontoGarantiaActualMonedaInstrumento" END AS "INGMontoGarantiaActualMonedaInstrumentoVigente"
FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON MOV."IdMovimientoCarteraEnTransito" = GAR."IdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" GARVIG ON GARVIG."FolioGarantia" = GAR."FolioGarantia" AND GARVIG."IdEstado" IN ('VIG','LIQ')
WHERE GAR."EGRIdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito)AND (
    GARVIG."IdEstado" = 'VIG'
    OR (GARVIG."IdEstado" = 'LIQ' AND NOT EXISTS (
        SELECT 1 FROM "BSCL.Custodia.BD::GTA.Garantia" AS subGAR
        WHERE subGAR."FolioGarantia" = GAR."FolioGarantia" AND subGAR."IdEstado" = 'VIG'
    ))
);
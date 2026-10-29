SELECT DISTINCT
	MOV."IdMovimientoCarteraEnTransito"
	,GARVIG."IdGarantia" 
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON MOV."IdMovimientoCarteraEnTransito" = GAR."IdMovimientoCarteraEnTransito"
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GARVIG ON GARVIG."FolioGarantia" = GAR."FolioGarantia" 
WHERE MOV."IdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito) AND GARVIG."IdEstado" = 'VIG';
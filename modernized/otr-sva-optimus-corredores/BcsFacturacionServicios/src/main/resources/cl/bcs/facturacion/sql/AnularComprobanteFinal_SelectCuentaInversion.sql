SELECT	ConsCarAbo."IdComprobante", 
		ConsCarAbo."IdTipoComprobante", 
		ConsCarAbo."IdCargoAbonoMovimiento",
		ConsCarAbo."NombreTipoMovimiento",
		ConsCarAbo."IdEstado"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" ConsCarAbo
WHERE 
ConsCarAbo."IdComprobante" IN (:idsComprobante) 
AND ConsCarAbo."IdEstado" <> 'ANU' 
AND ConsCarAbo."IdModulo" = 'FAC'

UNION ALL

SELECT	ConsCarAbo."IdComprobante", 
		ConsCarAbo."IdTipoComprobante", 
		ConsCarAbo."IdCargoAbonoMovimiento",
		ConsCarAbo."NombreTipoMovimiento",
		ConsCarAbo."IdEstado"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" ConsCarAbo
INNER JOIN "BSCL.Custodia.BD.Vistas::SIM.ConsultarAnticipos" Anticipo
	ON ConsCarAbo."IdOperacion" = "FolioCartera"
	AND "EGRFolioComprobante" IN (:idsComprobante) 
AND ConsCarAbo."IdEstado" <> 'ANU' 
AND ConsCarAbo."IdModulo" = 'CUS';
SELECT
	MOV."IdMovimientoCarteraEnTransito",
	MOV."IdTipoOperacion",
	MOV."IdEstado",
	MOV."IdOrigenGarantia",
	MOV."IdPortafolio",
	MOV."IdEstado",
	MOV."ExcluyeMovimientoEnCartola",
	GAR."FolioGarantia"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOV
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" GAR ON MOV."IdCustodiaOrigen" = GAR."IdGarantia"
WHERE MOV."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraTransito);
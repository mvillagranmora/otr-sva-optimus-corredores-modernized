SELECT 
	GTA."IdMovimientoCarteraEnTransito",
	GTA."IdGarantia",
	VIG."IdGarantia" AS "IdGarantiaVigente",
	GTA."MontoGarantia",
	GTA."INGCantidad",
	GTA."ValorRescate",
	GTA."IdMonedaInstrumento",
	GTA."IdEstado",
	GTA."IdOrigenGarantia",
	GTA."IdPortafolio",
	GTA."FolioGarantia",
	GTA."FechaOperacion",
	COALESCE(CAM."IdCargoAbonoMovimiento",0) AS "IdCargoAbonoMovimiento",
	GTA."FolioCarteraOrigen",
	GTA."IdConcepto",
	GTA."IdCliente"
FROM (
    SELECT 
    	GAR."IdMovimientoCarteraEnTransito",
		GAR."IdGarantia",
		GAR."MontoGarantia",
		GAR."INGCantidad",
		GAR."ValorRescate",
		GAR."IdMonedaInstrumento",
		GAR."IdEstado",
		GAR."IdOrigenGarantia",
		GAR."IdPortafolio",
		GAR."FolioGarantia",
		TO_NVARCHAR(GAR."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
		GAR."FolioCarteraOrigen",
		GAR."IdConcepto",
		ROW_NUMBER() OVER (PARTITION BY "FolioGarantia" ORDER BY "IdGarantia" DESC) AS ROW_NUM,
		GAR."IdCliente"
    FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
    WHERE GAR."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraTransito)
) AS GTA
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM ON CAM."IdComprobante" = GTA."IdGarantia" AND GTA."IdOrigenGarantia" = 'CTA' AND CAM."IdTipoOperacion" = 'INGGTA' AND CAM."IdEstado" = 'CRT'
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS VIG ON VIG."FolioGarantia" = GTA."FolioGarantia" AND VIG."IdEstado" = 'VIG'
WHERE GTA.ROW_NUM = 1;
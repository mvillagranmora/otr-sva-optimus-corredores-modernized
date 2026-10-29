
UPDATE Gta
SET "IdEstado" = 'ANU'
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovCom
INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" Gta
	ON Gta."FolioComprobante" = "IdComprobanteReferencia"
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" Car
	ON Gta."IdCarteraOrigen" = Car."IdCartera"
WHERE "IdGarantia" = (:IdGarantia)
	AND Gta."IdEstado" IN ('ANT','VIG')
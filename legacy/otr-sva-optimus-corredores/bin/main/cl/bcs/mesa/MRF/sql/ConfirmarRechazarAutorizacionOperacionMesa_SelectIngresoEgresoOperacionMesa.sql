SELECT "IdOperacion" AS "NumeroReferencia", "IdIngresoEgreso" AS "IdIngresoEgreso", '' AS  "TipoComprobante"  
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso"
WHERE "IdOperacion" = :IdOperacion 
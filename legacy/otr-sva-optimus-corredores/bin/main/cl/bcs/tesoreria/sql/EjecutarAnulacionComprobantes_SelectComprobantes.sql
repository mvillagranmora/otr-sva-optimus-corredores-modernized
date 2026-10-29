SELECT "IdComprobante"
FROM "BSCL.Tesoreria.BD::TES.Comprobante"
WHERE "IdEmpresa" = :p_IdEmpresa
AND "FechaLiquidacion" = :p_Fecha
AND "IdEstado" = 'ING';
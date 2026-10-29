SELECT "IdCartera", "FechaVencimientoInstrumento" FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "IdCartera" = :IdCartera
AND "FolioCartera" =:FolioCartera
AND "IdEstado" = 'VIG';

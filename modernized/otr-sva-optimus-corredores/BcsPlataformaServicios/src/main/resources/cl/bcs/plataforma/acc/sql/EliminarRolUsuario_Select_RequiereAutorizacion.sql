SELECT "RequiereAutorizacion" 
FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
WHERE "IdConcepto" = 'DROLUS' 
  AND "IdEmpresa" = :IdEmpresa

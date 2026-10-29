SELECT "RequiereAutorizacion"
FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
WHERE "IdConcepto" = 'AROLUS' 
AND "IdEmpresa" = :IdEmpresa

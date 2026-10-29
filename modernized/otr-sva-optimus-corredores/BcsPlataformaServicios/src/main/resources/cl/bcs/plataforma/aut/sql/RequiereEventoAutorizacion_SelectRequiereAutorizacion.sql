SELECT COALESCE("RequiereAutorizacion",'N') AS "RequiereAutorizacion"
FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion"
WHERE "IdConcepto" = :IdConcepto  and "IdEmpresa" = :IdEmpresa; 
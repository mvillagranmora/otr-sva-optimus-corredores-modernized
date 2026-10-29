SELECT "Codigo" AS "RegimenTributario", "PorcentajeRetencion"
FROM "BSCL.APV.BD::TRA.ConfiguracionRetencionTraspaso"  CRT
INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" RT ON (CRT."IdRegimenTributario" = RT."IdRegimenTributario") 
WHERE CRT."IdEstado" = 'VIG' 
AND RT."Codigo" IN (:RegimenTributario) 
AND CRT."CodigoRetiro" = 'RES'; 
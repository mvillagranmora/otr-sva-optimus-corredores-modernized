SELECT 
"IdGlosa" || '_' || "IdTipoInstrumento" AS "IdUsoGlosaIdTipoInstrumento",
"IdTipoInstrumento",
"IdGlosa",
"Prioridad" 
FROM "BSCL.Plataforma.BD::PLA.UsoGlosaTipoInstrumento"
WHERE "IdTipoInstrumento" IN (:IdTipoInstrumento)
ORDER BY "IdTipoInstrumento","Prioridad" ASC;
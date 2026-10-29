SELECT 
	"IdExclusionConcepto",
	"IdConcepto",
	"FechaVigenciaDesde"
FROM "BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto"
WHERE "IdExclusionConcepto" IN (:idsExclusionConcepto);
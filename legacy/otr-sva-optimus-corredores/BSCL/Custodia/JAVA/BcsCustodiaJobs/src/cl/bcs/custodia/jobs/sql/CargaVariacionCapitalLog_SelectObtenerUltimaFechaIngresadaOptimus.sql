WITH "VCL" AS (
	SELECT
		RANK() OVER (ORDER BY "HoraAuditoria" DESC) AS "Prioridad",
		"HoraAuditoria" AS "UltimaHoraAuditoriaCargadaOpt"
	FROM "BSCL.Custodia.BD::CUS.VariacionCapitalLog"
) 
SELECT 
	"UltimaHoraAuditoriaCargadaOpt"
FROM VCL
WHERE VCL."Prioridad" = 1
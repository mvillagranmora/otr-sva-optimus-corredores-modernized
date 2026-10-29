SELECT 
	  TP."IdConcepto"
	, CASE WHEN CCI."IdConcepto" IS NOT NULL THEN 'S' ELSE 'N' END AS "Existe"
	, CASE WHEN CCEC."IdConcepto" IS NOT NULL THEN 'S' ELSE 'N' END AS "Excluido"
FROM #ExclusionConceptoCobroPatrimonio_TablaPaso AS TP
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" AS CCI ON TP."IdConcepto" = CCI."IdConcepto"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto" AS CCEC ON TP."IdConcepto" = CCEC."IdConcepto";
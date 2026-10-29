SELECT 
	CCM."IdConcepto"
	, COALESCE(CCM."ConfiguradoEnModuloTESTra", 'NO') AS "ConfiguradoParaTES"
	, COALESCE(CCM."ConfiguradoEnModuloCTATra", 'NO') AS "ConfiguradoParaCTA"
	, COALESCE(CCM."UsoTES", '') AS "UsoTES"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosManuales" CCM
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarConceptosTesoreria" AS CCT ON CCT."IdConcepto" = CCM."IdConcepto" AND (CCT."TipoConceptoTesoreria" = 'IN' OR CCT."TipoConceptoTesoreria" = '' OR CCT."TipoConceptoTesoreria" = 'AM') 
AND CCT."NombreEntidad" = 'TES.IngresoEgreso' AND CCT."IdEstado" = 'VIG' AND CCT."DesplegarEnComboBox" = 'S' AND CCT."DisponibleIngresoManual" = 'S' 
AND CCT."VisibleOptimus" = 'S' AND (CCT."VisiblePorEjecutivo" = 'S' OR CCT."VisiblePorEjecutivo" = 'N')
WHERE CCM."IdEstado" = 'VIG' AND CCM."IdConcepto" IN (:IdsConcepto);
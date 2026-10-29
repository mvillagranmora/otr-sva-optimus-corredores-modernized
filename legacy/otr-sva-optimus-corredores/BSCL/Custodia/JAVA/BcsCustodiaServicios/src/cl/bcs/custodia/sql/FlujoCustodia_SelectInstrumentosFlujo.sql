/********************************************************** 
Proposito	: Recuperar Instrumentos asociados al flujo
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

SELECT
	ISU."IdTipoInstrumento",
	ISU."IdTipoInstrumentoSubyacente",
	ISU."IdInstrumento"
FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" AS TIS
	INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoSubyacente" AS ISU
		ON ISU."IdTipoInstrumento" = TIS."IdTipoInstrumento"
			AND ISU."IdBolsa" = TIS."IdBolsa"
			AND ISU."IdTipoInstrumentoSubyacente" = TIS."IdTipoInstrumentoSubyacente"
WHERE 
--	ISU."IdTipoInstrumento" = DFC."IdTipoInstrumentoEspecifico"
--	AND ISU."IdTipoInstrumentoSubyacente" = DFC."IdTipoInstrumento"
--	AND ISU."IdInstrumento" = DFC."IdInstrumento"
	
	ISU."IdTipoInstrumento" || '#' ||
	ISU."IdTipoInstrumentoSubyacente" || '#' ||
	ISU."IdInstrumento" IN (:Instrumentos)
	;

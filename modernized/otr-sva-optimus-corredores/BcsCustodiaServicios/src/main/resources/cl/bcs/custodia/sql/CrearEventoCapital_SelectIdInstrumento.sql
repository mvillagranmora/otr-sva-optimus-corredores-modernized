SELECT INS."Nemotecnico",
	   INS."IdInstrumento",
	   TINS."IdClasificacionTipoInstrumento",
	   INS."IdTipoInstrumento",
	   INS."IdEmisor",
	   INS."Nemotecnico" ||'_' || INS."IdTipoInstrumento" AS "LlaveNemotecnico"
FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS 
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento" 
WHERE INS."Nemotecnico" IN (:p_Nemo)
	  AND (
	  		:FiltroIdClasificacionTipoInstrumento = 0
	  		OR TINS."IdClasificacionTipoInstrumento" IN (:p_IdClasificacionTipoInstrumento)
       ); 
SELECT 'CLASIFICACION' AS "TipoAtributoInstrumento" 
	   ,"IdClasificacionTipoInstrumento" AS "IdAtributoInstrumento"
FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"

UNION ALL 

SELECT 'TIPO' AS "TipoAtributoInstrumento"
	   ,"IdTipoInstrumento" AS "IdAtributoInstrumento" 
FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" 
ORDER BY "TipoAtributoInstrumento", "IdAtributoInstrumento";
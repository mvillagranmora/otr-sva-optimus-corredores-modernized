SELECT 
OPERACION."IdCuadraturaDiariaOperaciones"
,TO_VARCHAR(OPERACION."FechaEjecucion") AS "FechaEjecucion"
,COALESCE(DICCIONARIO_OPERACION."Descripcion", OPERACION."OperacionCuadraturaDiariaOperaciones") AS "OperacionCuadraturaDiariaOperaciones"
,COALESCE(DICCIONARIO_RESULTADO."Descripcion", OPERACION."Resultado") AS "Resultado"
,OPERACION."OperacionesConProblemas"
,OPERACION."DetalleCuadraturaJSON"
FROM "BSCL.Mesa.BD::MES.CuadraturaDiariaOperacionesFWD" AS OPERACION
LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" AS DICCIONARIO_OPERACION ON (DICCIONARIO_OPERACION."CodDiccionario" = OPERACION."OperacionCuadraturaDiariaOperaciones" AND DICCIONARIO_OPERACION."CodLenguaje" = :Localizacion)
LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" AS DICCIONARIO_RESULTADO ON (DICCIONARIO_RESULTADO."CodDiccionario" = OPERACION."Resultado" AND DICCIONARIO_RESULTADO."CodLenguaje" = :Localizacion)
WHERE OPERACION."IdEmpresa" = :IdEmpresa;
DELETE tablaVariable
FROM :p_Tabla tablaVariable
WHERE CAST(:p_Filtro AS DATE) BETWEEN :p_Desde AND :p_Hasta;
SELECT * FROM :tabla
WHERE CAST (:filtro AS DATE) 
BETWEEN :p_Hasta AND :p_Desde


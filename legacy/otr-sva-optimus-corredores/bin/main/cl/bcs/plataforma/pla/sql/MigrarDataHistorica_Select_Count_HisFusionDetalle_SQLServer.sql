SELECT COUNT(1)
FROM :p_Tabla detalle
JOIN "BSCL.Custodia.BD::HIS.FusionCartera" fuscar ON detalle."IdFusion" = fuscar."IdFusion"
WHERE CAST(:p_Filtro AS DATE) BETWEEN :p_Desde AND :p_Hasta;
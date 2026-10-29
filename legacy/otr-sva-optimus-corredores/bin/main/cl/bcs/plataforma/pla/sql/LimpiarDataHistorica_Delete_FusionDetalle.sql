DELETE FROM :p_Tabla detalle
WHERE detalle."IdFusion" IN (SELECT "IdFusion" FROM "BSCL.Custodia.BD::HIS.FusionCartera" WHERE CAST( :p_Filtro AS DATE) BETWEEN :p_Desde AND :p_Hasta);

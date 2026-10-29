SELECT INDPOR."IdIndicador",INDPOR."IdTipoIndicador", INDPOR."IdPortafolio"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS INDPOR
WHERE INDPOR."CodigoTipoIndicador" IN ('COBROCUST')
AND INDPOR."IdPortafolio" IN (:IdPortafolio);
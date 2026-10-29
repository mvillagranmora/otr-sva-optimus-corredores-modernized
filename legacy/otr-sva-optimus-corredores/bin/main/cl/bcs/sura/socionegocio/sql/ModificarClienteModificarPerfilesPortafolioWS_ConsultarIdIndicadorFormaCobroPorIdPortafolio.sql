SELECT INDPOR."IdIndicador"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS INDPOR
WHERE INDPOR."CodigoTipoIndicador" IN ('COBROCUST')
AND INDPOR."IdPortafolio" = :IdPortafolio;
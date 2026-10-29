SELECT
COALESCE("ValorIndicador", 'N') AS "AfectaCustodia"
,"IdPortafolio"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
WHERE "CodigoTipoIndicador" = 'MES_CUSTRF' AND "IdPortafolio" IN (:IdPortafolio);
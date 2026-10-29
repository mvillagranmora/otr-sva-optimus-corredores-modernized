SELECT IND."IdPortafolio" AS "IdRegistro", IND."CodigoTipoIndicador" AS "Codigo", IND."ValorIndicador" AS "Valor"
FROM "BSCL.Custodia.BD::GBL.ValidaCantidadRetiroCustodia_RegistrosAValidar" REG
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND
ON (IND."CodigoTipoIndicador" IN ('GEN_OCCINV', 'GEN_SOBCTA') 
AND IND."IdPortafolio" = REG."IdPortafolio"
)
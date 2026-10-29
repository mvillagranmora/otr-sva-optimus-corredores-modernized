SELECT 
     EO."IdPortafolio"
    ,EO."IdCliente"
    ,COALESCE(IND."ValorIndicador", '1') AS "ValorIndicador"
    ,TO_VARCHAR(TO_DATE(COALESCE(EO."FechaReferencia", EO."FechaEjecucion")), 'YYYY-MM-DD') AS "FechaReferencia"
    ,TO_VARCHAR(MAX(EO."FechaEjecucion"), 'YYYY-MM-DD') AS "FechaEjecucion"
FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" EO
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI
    ON TI."CodigoTipoIndicador" = 'CIRCFREC1985'
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
    ON IND."IdEntidad" = EO."IdPortafolio"
    AND IND."IdTipoIndicador" = TI."IdTipoIndicador"
LEFT JOIN "BSCL.Normativa.BD::GBL.GenerarCartasCIR1985_IN_Cliente" PASO
    ON PASO."IdCliente" = EO."IdCliente"
    AND PASO."IdPrograma" = :p_IdPrograma
WHERE TO_DATE(COALESCE(EO."FechaReferencia", EO."FechaEjecucion")) BETWEEN TO_DATE(:p_FechaDesde) AND TO_DATE(:p_FechaHasta)
  AND (:p_TodosClientes = 1 OR PASO."IdCliente" IS NOT NULL)
GROUP BY 
     EO."IdPortafolio"
    ,EO."IdCliente"
    ,COALESCE(IND."ValorIndicador", '1')
    ,TO_DATE(COALESCE(EO."FechaReferencia", EO."FechaEjecucion"))
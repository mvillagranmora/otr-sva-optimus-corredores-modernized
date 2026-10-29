SELECT 
     "Fecha"AS"Fecha","RutCliente", "Portafolio", "FactorComision",SUM("MontoComision") AS"MontoComision","CantidadCustodia","CantidadGarantia","PrecioMercado", "MontoValorizado", "Nemo", "Mercado","FactorNemo"
FROM(
    SELECT 
        PTRCUS."FechaPatrimonio"  AS "Fecha"
        ,CLI."DocumentoIdentidad" AS "RutCliente"
        ,'(' || POR."CodigoPortafolio" || ') ' || POR."Proposito" AS "Portafolio"
        ,COALESCE(PTRCUS."PorcentajeCobroComision",0) AS "FactorComision"
        ,sum(PTRCUS."MontoPatrimonio") AS "MontoValorizado"
        ,sum(PTRCUS."CantidadCustodia") AS "CantidadCustodia"
        ,sum(PTRCUS."CantidadGarantia") AS "CantidadGarantia"
        ,sum(PTRCUS."PrecioMercadoCustodia") AS "PrecioMercado"
        ,COALESCE (EDIT."MontoComision" ,SUM(PTRCUS."MontoComision")) AS "MontoComision"   
        ,PTRCUS."Nemotecnico" AS "Nemo" 
        ,OPC_COB."IdClasificacionTipoInstrumento" AS "Mercado"
        ,CASE WHEN "CobroComisionesCustodia"='S' THEN '1' ELSE '0' END  AS "FactorNemo"      
    FROM "BSCL.Custodia.BD::CUS.PatrimonioCustodia" PTRCUS
    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = PTRCUS."IdPortafolio"
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = PTRCUS."IdCliente"
    INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = PTRCUS."IdInstrumento"
    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle" DET ON DET."IdPortafolio" = POR."IdPortafolio"
    LEFT JOIN "BSCL.APV.BD.Vistas::ConsultarOtroProductoAgrupador" PROAGR ON PROAGR."IdProductoAgrupador" = DET."IdProductoAgrupador"
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" AS CIP ON CIP."IdPortafolio" = POR."IdPortafolio" AND CIP."CodigoTipoIndicador" = 'COBROCUST'
    INNER JOIN "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" OPC_COB
        ON OPC_COB."PortafolioAPV" = CASE WHEN POR."IdMatrizAgrupadorAPV" = 0 THEN 'N' ELSE 'S' END
        AND OPC_COB."IdClasificacionTipoInstrumento" = INS."IdClasificacionTipoInstrumento"
        AND TO_NVARCHAR(OPC_COB."OpcionCobro") = CIP."ValorIndicador"
    LEFT JOIN "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones" AS EDIT ON POR."IdPortafolio" = EDIT."IdPortafolio" AND EDIT."Periodo" = :p_Periodo AND EDIT."EstadoComision" <> 'INA'
    INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON PTRCUS."IdEstadoComision" = EST."IdEstado"
    WHERE PTRCUS."FechaPatrimonio" BETWEEN :p_FechaDesde AND :p_FechaHasta
        AND (:p_Cliente IS NULL OR :p_Cliente = 0 OR CLI."IdCliente" = :p_Cliente)
        AND (:p_Portafolio IS NULL OR :p_Portafolio = 0 OR POR."IdPortafolio" = :p_Portafolio)
        AND (( COALESCE(:p_EstadoComision, '') = '' AND PTRCUS."IdEstadoComision" <> 'INA') OR PTRCUS."IdEstadoComision" = :p_EstadoComision) 
        AND PTRCUS."MontoComision" <> 0
        AND ( COALESCE(:p_ProductoAgrupador,0) = 0 OR PROAGR."IdProductoAgrupador" = :p_ProductoAgrupador OR (:p_ProductoAgrupador = 9999 AND POR."IdMatrizAgrupadorAPV" = 0))
    GROUP BY
        CLI."DocumentoIdentidad"
        ,POR."IdPortafolio"
        ,POR."CodigoPortafolio"
        ,POR."Proposito"
        ,PTRCUS."Nemotecnico"
        ,PTRCUS."PorcentajeCobroComision"
        ,PTRCUS."MontoPatrimonio"
        ,OPC_COB."IdClasificacionTipoInstrumento"
        ,INS."CobroComisionesCustodia"
        ,EDIT."MontoComision"
        ,PTRCUS."FechaPatrimonio"
    UNION ALL
    SELECT
        PTRCAJ."FechaPatrimonio" AS "Fecha"
        ,CLI."DocumentoIdentidad" AS "RutCliente"
        ,'(' || POR."CodigoPortafolio" || ') ' || POR."Proposito" AS "Portafolio"
        ,COALESCE(PTRCAJ."PorcentajeCobroComision",0) AS "FactorComision"
        ,sum(PTRCAJ."MontoPatrimonioAfectoComision") AS "MontoValorizado"
        ,0 AS "CantidadCustodia"
        ,0 AS "CantidadGarantia"
        ,0 AS "PrecioMercado"
        ,COALESCE(EDIT."MontoComision" ,SUM(PTRCAJ."MontoComision")) AS "MontoComision"
        ,'N/A' AS "Nemo"
        ,OPC_COB."IdClasificacionTipoInstrumento" AS "Mercado"
        ,'N/A'  AS "FactorNemo"
    FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" PTRCAJ
    INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = PTRCAJ."IdPortafolio"
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = PTRCAJ."IdCliente"
    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle" DET ON DET."IdPortafolio" = POR."IdPortafolio"
    LEFT JOIN "BSCL.APV.BD.Vistas::ConsultarOtroProductoAgrupador" PROAGR ON PROAGR."IdProductoAgrupador" = DET."IdProductoAgrupador"
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" AS CIP ON CIP."IdPortafolio" = POR."IdPortafolio" AND CIP."CodigoTipoIndicador" = 'COBROCUST'
    INNER JOIN "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" OPC_COB
        ON OPC_COB."PortafolioAPV" = CASE WHEN POR."IdMatrizAgrupadorAPV" = 0 THEN 'N' ELSE 'S' END
        AND OPC_COB."IdClasificacionTipoInstrumento" = 'CI'
        AND TO_NVARCHAR(OPC_COB."OpcionCobro") = CIP."ValorIndicador"
    LEFT JOIN "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones" AS EDIT ON POR."IdPortafolio" = EDIT."IdPortafolio" AND EDIT."Periodo" = :p_Periodo AND EDIT."EstadoComision" <> 'INA'
    INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON PTRCAJ."IdEstadoComision" = EST."IdEstado"
    WHERE PTRCAJ."FechaPatrimonio" BETWEEN :p_FechaDesde AND :p_FechaHasta
        AND (:p_Cliente IS NULL OR :p_Cliente = 0 OR CLI."IdCliente" = :p_Cliente)
        AND (:p_Portafolio IS NULL OR :p_Portafolio = 0 OR POR."IdPortafolio" = :p_Portafolio)
        AND (( COALESCE(:p_EstadoComision, '') = '' AND PTRCAJ."IdEstadoComision" <> 'INA') OR PTRCAJ."IdEstadoComision" = :p_EstadoComision) 
        AND PTRCAJ."MontoComision" <> 0
        AND ( COALESCE(:p_ProductoAgrupador,0) = 0 OR PROAGR."IdProductoAgrupador" = :p_ProductoAgrupador OR (:p_ProductoAgrupador = 9999 AND POR."IdMatrizAgrupadorAPV" = 0))
    GROUP BY
        CLI."DocumentoIdentidad"
        ,POR."IdPortafolio"
        ,POR."CodigoPortafolio"
        ,POR."Proposito"
        ,PTRCAJ."PorcentajeCobroComision"
        ,PTRCAJ."MontoPatrimonioAfectoComision"
        ,OPC_COB."IdClasificacionTipoInstrumento" 
        ,EDIT."MontoComision"
        ,PTRCAJ."FechaPatrimonio" 
) AS REGISTROS_INFORME_PATRIMONIO_DIARIO
GROUP BY "RutCliente", "Portafolio", "FactorComision", "Nemo", "Mercado","FactorNemo","Fecha","MontoValorizado" ,"CantidadCustodia","CantidadGarantia","PrecioMercado"
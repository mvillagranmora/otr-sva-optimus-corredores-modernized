WITH PortafolioFiltrado AS (
    SELECT
         POR."IdPortafolio"
        ,POR."IdCliente"
		,CLI."DocumentoIdentidad"
        ,CLI."NombreCompleto" AS "NombreCliente"
        ,CASE WHEN REGTRIB."Codigo" = 'BF' THEN 'APV' ELSE REGTRIB."CodigoRegimenTipoAhorro" END AS "CodigoRegimenTipoAhorro"
        ,REGTRIB."IdRegimenTributario"
        ,REGTRIB."Codigo" AS "CodigoRegimenTributario"
        ,PLAN."IdPlanTipoInversion"
        ,PLAN."NombrePlanTipoInversion"
    FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente" AND CLI."IdEmpresa" = :IdEmpresa
    INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
    INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" MADOR ON MADOR."IdProductoAgrupador" = MAT."IdProductoAgrupador" AND MADOR."IdEstado" = 'VIG'
    INNER JOIN "BSCL.APV.BD::SCS.PlanTipoInversion" PLAN ON PLAN."IdPlanTipoInversion" = MADOR."IdPlanTipoInversion"
    INNER JOIN "BSCL.APV.BD::REC.RegimenTributario" REGTRIB ON REGTRIB."IdRegimenTributario" = MAT."IdRegimenTributario"
    WHERE POR."Borrado" = 'N' 
    AND POR."Proposito" = 'PROPOSITOAPV'
	AND POR."IdMatrizAgrupadorAPV" <> 0
),
ReservaAgrupada AS (
    SELECT
         RES."FolioCarteraOrigen"
        ,SUM(RES."IngCantidad") AS "IngCantidad"
        ,SUM(RES."IngValorRescate") AS "IngValorRescate"
        ,SUM(RES."MontoValorizado") AS "IngMontoValorizado"
    FROM "BSCL.Custodia.BD::CUS.Reserva" RES
    WHERE
		RES."IdEstado" = 'VIG'
        AND RES."EgrCantidad" = 0
    GROUP BY RES."FolioCarteraOrigen"
),
CustodiaBase AS (
    SELECT
         CAR."IdCliente"
        ,POR."IdPortafolio"
        ,POR."DocumentoIdentidad"
        ,POR."NombreCliente"
        ,POR."CodigoRegimenTipoAhorro"
        ,POR."IdRegimenTributario"
        ,POR."CodigoRegimenTributario"
        ,POR."IdPlanTipoInversion"
        ,POR."NombrePlanTipoInversion"
        ,CAR."IdInstrumento"
        ,CAR."IdClasificacionTipoInstrumento"
        ,CAR."IdMonedaInstrumento" AS "IdMonedaInstrReajuste"
        ,COALESCE(CAR."INGMontoMercadoActualMonedaInstrumento",0) AS "MontoFinal"
        ,CASE WHEN CAR."IdMonedaEmision" IS NOT NULL AND CAR."IdMonedaEmision" <> '' THEN CAR."IdMonedaEmision"
              WHEN CAR."IdTipoReajuste" IS NOT NULL AND CAR."IdTipoReajuste" <> '' THEN CAR."IdTipoReajuste"
         ELSE INS."IdMoneda" END AS "IdMonedaEmision"
        ,CAR."IdTipoInstrumento"
        ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END AS "Nemotecnico"
        ,CAST(
        	COALESCE(
        		CASE
                    WHEN CAR."IdClasificacionTipoInstrumento" = 'IF'
                        THEN CAR."INGValorRescate" - COALESCE(RES."IngValorRescate",0)
                    ELSE CAR."INGCantidad" - COALESCE(RES."IngCantidad",0)
                END,
            0)
         AS DECIMAL(26,8)) AS "Cantidad"
    FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
    INNER JOIN PortafolioFiltrado POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
    INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento"
	LEFT JOIN ReservaAgrupada RES ON RES."FolioCarteraOrigen" = CAR."FolioCartera"
    WHERE
		CAR."INGFechaOperacion" <= :FechaCierre
        AND CAR."IdEstado" = 'VIG'
        AND CAR."IdEmpresa" = :IdEmpresa
),
GarantiaBase AS (
    SELECT
         GAR."IdCliente"
        ,GAR."IdPortafolio"
        ,POR."DocumentoIdentidad"
        ,POR."NombreCliente"
        ,POR."CodigoRegimenTipoAhorro"
        ,POR."IdRegimenTributario"
        ,POR."CodigoRegimenTributario"
        ,POR."IdPlanTipoInversion"
        ,POR."NombrePlanTipoInversion"
        ,GAR."IdInstrumento"
        ,GAR."IdClasificacionTipoInstrumento"
        ,CASE 
	        WHEN GAR."IdTipoReajuste" IS NOT NULL AND GAR."IdTipoReajuste" <> '' THEN GAR."IdTipoReajuste"
            WHEN INS."IdTipoReajuste" IS NOT NULL AND INS."IdTipoReajuste" <> '' THEN INS."IdTipoReajuste"
            WHEN GAR."IdMonedaInstrumento" IS NOT NULL AND GAR."IdMonedaInstrumento" <> '' THEN GAR."IdMonedaInstrumento" 
         ELSE INS."IdMoneda" END AS "IdMonedaInstrReajuste"
        ,COALESCE(GAR."INGMontoMercadoActualMonedaInstrumento",0) AS "MontoFinal"
        ,CASE 
	        WHEN GAR."IdMonedaEmision" IS NOT NULL AND GAR."IdMonedaEmision" <> '' THEN GAR."IdMonedaEmision"
			WHEN GAR."IdTipoReajuste" IS NOT NULL AND GAR."IdTipoReajuste" <> '' THEN GAR."IdTipoReajuste"
            WHEN GAR."IdMonedaInstrumento" IS NOT NULL AND GAR."IdMonedaInstrumento" <> '' THEN GAR."IdMonedaInstrumento"
            WHEN INS."IdTipoReajuste" IS NOT NULL AND INS."IdTipoReajuste" <> '' THEN INS."IdTipoReajuste"
         ELSE INS."IdMoneda" END AS "IdMonedaEmision"
        ,GAR."IdTipoInstrumento"
        ,CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' THEN GAR."NemoSVS" ELSE GAR."Nemotecnico" END AS "Nemotecnico"
        ,CAST(
        	COALESCE(
        		CASE 
        			WHEN GAR."IdClasificacionTipoInstrumento" = 'IF' 
        				THEN GAR."INGValorRescate" 
        			ELSE GAR."INGCantidad" 
        		END, 
        	0) AS DECIMAL(26,8)) AS "Cantidad"
    FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
    INNER JOIN PortafolioFiltrado POR ON POR."IdPortafolio" = GAR."IdPortafolio" AND POR."IdCliente" = GAR."IdCliente"
    INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
    INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = GAR."IdInstrumento"
    WHERE
		GAR."INGFechaOperacion" <= :FechaCierre
        AND GAR."IdEmpresa" = :IdEmpresa
        AND GAR."IdEstado" = 'VIG'
),
CustodiaFinal AS (
    SELECT * FROM CustodiaBase
    UNION ALL
    SELECT * FROM GarantiaBase
)
SELECT
     CUS."IdCliente"
    ,CUS."IdPortafolio"
    ,CUS."IdInstrumento"
    ,CUS."IdClasificacionTipoInstrumento"
    ,CUS."IdTipoInstrumento"
    ,CUS."IdMonedaEmision"
    ,CUS."Nemotecnico"
    ,CUS."DocumentoIdentidad"
    ,CUS."NombreCliente"
    ,CUS."CodigoRegimenTipoAhorro" AS "GrupoRegimenTributario"
    ,CUS."IdRegimenTributario"
    ,CUS."CodigoRegimenTributario"
    ,CUS."IdPlanTipoInversion"
    ,CUS."NombrePlanTipoInversion"
    ,CUS."IdMonedaInstrReajuste"
    ,SUM(CUS."MontoFinal") AS "MontoMercado"
    ,SUM(CUS."Cantidad") AS "CantidadCustodia"
FROM CustodiaFinal CUS
GROUP BY
     CUS."IdCliente"
    ,CUS."IdPortafolio"
    ,CUS."IdInstrumento"
    ,CUS."IdClasificacionTipoInstrumento"
    ,CUS."IdTipoInstrumento"
    ,CUS."IdMonedaEmision"
    ,CUS."Nemotecnico"
    ,CUS."DocumentoIdentidad"
    ,CUS."NombreCliente"
    ,CUS."CodigoRegimenTipoAhorro"
    ,CUS."IdRegimenTributario"
    ,CUS."CodigoRegimenTributario"
    ,CUS."IdPlanTipoInversion"
    ,CUS."NombrePlanTipoInversion"
    ,CUS."IdMonedaInstrReajuste";
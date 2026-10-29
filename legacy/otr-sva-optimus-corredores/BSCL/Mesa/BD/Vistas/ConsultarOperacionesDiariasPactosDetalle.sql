SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactosDetalle";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactosDetalle" 

AS

    SELECT DISTINCT
	    CASE 
            WHEN Sub."FechaVencimientoInstrumento" = OPE."FechaVencimiento" THEN 2 
            ELSE 0 
        END + 
        CASE 
            WHEN Sub."FechaVencimientoInstrumento" = OPE."FechaVencimiento" AND IND."ValorIndicador" = 'N' THEN 3 
            ELSE 0 
        END AS "IndicadorCobertura",
        OPE."FolioOperacion",
        TO_NVARCHAR(OPE."FechaOperacion", 'DD-MM-YYYY') AS "FechaOperacionCadena",
        TO_DATE(TO_NVARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') , 'YYYY-MM-DD') AS "FechaOperacion",
		RES."NemotecnicoDetalle" AS "Nemotecnico",
        RES."IdEmisorDetalle" AS "IdEmisor",
        RES."IdClasificacionTipoInstrumentoDetalle" AS "Mercado",
        RES."IdTipoReajuste",
        RES."TasaCompraDetalle" AS "Tasa",
        RES."FechaVencimientoInstrumentoDetalle" AS "FechaVencimientoInstrumento",
        TO_NVARCHAR(RES."FechaVencimientoInstrumentoDetalle", 'YYYY-MM-DD') AS "FechaVencimientoInstrumentoCadena",
        TO_DATE(TO_NVARCHAR(OPE."FechaLiquidacion", 'YYYY-MM-DD') , 'YYYY-MM-DD') AS "FechaLiquidacion",
		CASE WHEN RES."IdClasificacionTipoInstrumentoDetalle" = 'IF' THEN RES."ValorRescateDetalle" ELSE RES."CantidadDetalle" END AS "Cantidad",	
        RES."MontoLiquidacionOperacionDetalle" AS "MontoSubyacente",
        RES."INGMontoAPlazoDetalle" AS "MontoCompromisoDetalle",
        OPE."IdTipoOperacion",
        OPE."PlazoEnDias",	
        TO_DATE(TO_NVARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') , 'YYYY-MM-DD') AS "FechaVencimiento",
        COALESCE(IND."ValorIndicador", 'N') AS "RelacionCorredora",
        CASE
			WHEN RES."FolioCarteraDetalle" = 0 THEN res."FolioCarteraCustodiaDetalle"
			ELSE RES."FolioCarteraDetalle"
	    END AS "FolioCartera"
        ,COALESCE(FLAG."FlagDeterioro",'N') AS "FlagDeterioro" --cambio
        ,COALESCE(FLAG."PorcentajeDeterioro",0 ) AS "PorcentajeDeterioro"
        ,CASE FLAG."FlagDeterioro" WHEN 'S' THEN 
                CASE WHEN Sub."IdClasificacionTipoInstrumento" = 'IF' THEN CAST((COALESCE(FLAG."PorcentajeDeterioro" ,0)/100) * COALESCE(Sub."ValorRescate",0) AS DECIMAL)
                    ELSE CAST((COALESCE(FLAG."PorcentajeDeterioro" ,0)/100) * COALESCE(Sub."Cantidad",0) AS DECIMAL)
                    END
                ELSE 0 
        END AS "Deterioro"
        ,COALESCE(FLAG."Fase",0) AS "Fase"
	    ,CASE WHEN Sub."IdMonedaInstrumento" <> '' THEN Sub."IdMonedaInstrumento" ELSE CAR."IdMonedaSistema" END AS "IdMonedaInstrumento" --cambio
    FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" RES
	LEFT JOIN  "BSCL.Custodia.BD::CUS.Cartera" CAR ON (CAR."IdOperacion" = RES."IdOperacion")
	LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON (CAR."FolioComprobante" = OPE."IdOperacion" OR CAR."IdOperacion" = OPE."IdOperacion")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OPE."IdCliente" = CLI."IdCliente"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IND ON (IND."IdCliente" = CLI."IdCliente" AND "IdTipoIndicador" = 56 AND "GrupoIndicador" = 'NORMATIVOS')
	LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" Sub ON Sub."FolioCarteraRelacionada" = CAR."FolioCartera" AND CAR."IdTipoInstrumento" = 'PAC'
	LEFT JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS FLAG ON FLAG."Emisor" = Sub."IdEmisor" AND FLAG."IdEstado" = 'ACT' --cambio
    WHERE OPE."IdTipoOperacion" IN ('COMPAC', 'VENPAC' )

WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarOperacionesDiariasPactosDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION;


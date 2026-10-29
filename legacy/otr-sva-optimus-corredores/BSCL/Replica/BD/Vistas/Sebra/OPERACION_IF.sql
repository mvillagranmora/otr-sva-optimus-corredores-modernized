SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_IF";
--SELECT SOL_SEC,COUNT(1) FROM "BSCL.Replica.BD.Vistas::Sebra.OPERACION_IF" GROUP BY SOL_SEC HAVING COUNT(1) > 1

--select * from "BSCL.Replica.BD.Vistas::Sebra.OPERACION_IF" where ORD_SEC = 135580

--SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::Sebra.OPERACION_IF" 
CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_IF" AS

Select 
            ORD."IdOrden",
            ORD."IdOrden" AS "SOL_SEC", -- NUMERIC
            CASE ORD."IdTipoReajuste" WHEN 'CLP' THEN 'NR' WHEN 'USD' THEN 'US' WHEN 'UF' THEN 'UF' ELSE 'NR' END AS "TIPO_REAJ", -- CHAR(2)
            ORD."IdOrden"  AS "ORD_SEC", -- NUMERIC
            LEFT(COALESCE (ORD."Nemotecnico",''   ),16)  AS "OIF_NEMO", -- CHAR(16)
            COALESCE (CAST(ORD."IdEmisor" AS VARCHAR(16)),'')  AS "OIF_EMISOR", -- CHAR(16)
            CASE ORD."FechaEmision" WHEN '0001-01-01' THEN '1900-01-01' ELSE ORD."FechaEmision" END  AS "OIF_FEC_EMISION", -- DATETIME
            COALESCE (ORD."FechaVencimiento" , '1900-01-01' )  AS "OIF_FEC_VCTO", -- DATETIME
            COALESCE (ORD."TasaRFIF" , 0 )  AS "OIF_TASA", -- NUMERIC
            COALESCE (ORD."ValorRescate" , 0 )  AS "OIF_VAL_RESCATE", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "OIF_EFECTIVO_DISPONIBLE", -- NUMERIC
            COALESCE ( ORD."Cantidad" , 0 )  AS "OIF_CANTIDAD", -- NUMERIC
            COALESCE (ORD."MontoOrden" , 0 )  AS "OIF_MTO_TOTAL", -- NUMERIC
            COALESCE (COB_OR."MontoComision" , 0 )  AS "OIF_MTO_COMISION", -- NUMERIC
            COALESCE (COB_OR."MontoDerecho" , 0 )  AS "OIF_MTO_DERECHOS", -- NUMERIC
            COALESCE (COB_OR."MontoGasto" , 0 )  AS "OIF_MTO_GASTOS", -- NUMERIC
            COALESCE (ORD."ValorRescate" , 0 )  AS "OIF_VAL_RESCATE_ASIG", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "OIF_CANT_DISPONIBLE", -- NUMERIC
            COALESCE (ORD."FechaOperacion" , '1900-01-01' )  AS "OIF_FEC_ORDEN", -- DATETIME
            COALESCE (CAST(ORD."OrdenVigente" AS VARCHAR(1)),'')   AS "OIF_IND_VIGENTE", -- CHAR(1) 
            'CC' AS "OIF_CONCEPTO", -- CHAR(2)
            'TRA' AS "OIF_TRA_BOVEDA", -- CHAR(3)
            COALESCE ( 0 , 0 )  AS "OIF_TRA_FILIAL", -- INT
            COALESCE ( 0 , 0 )  AS "OIF_TRA_PERSONA", -- INT
            COALESCE ( 0 , 0 )  AS "OIF_TRA_CUENTA", -- INT
            COALESCE (CAST(ORD."IdBoveda" AS VARCHAR(3)),'')  AS "OIF_BOVEDA", -- CHAR(3)
            TO_VARCHAR (CASE WHEN ORD."FechaLiquidacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaLiquidacion" END ,'YYYY-MM-DD')  AS "OIF_FEC_LIQUIDACION", -- DATETIME
            COALESCE ( 0 , 0 )  AS "OIF_OP_SWAP", -- NUMERIC
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "OIF_TIPO_ORDEN", -- CHAR(2)
            COALESCE (CAST('P' AS VARCHAR(1)),'')  AS "OIF_TIP_MERCADO", -- CHAR(1)
            COALESCE ( 0 , 0 )  AS "OIF_PLAZO", -- SMALLINT
            COALESCE ( 0 , 0 )  AS "OIF_MTO_GARANTIA", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "OIF_VAL_IV", -- NUMERIC
            COALESCE (ORD."MontoLocalPlazo" , 0 )  AS "OIF_MTO_FINAL", -- NUMERIC
            COALESCE ( ORD."TasaRFIF" , 0 )  AS "TASA_EMISION", -- DECIMAL -- 
            COALESCE ( 0 , 0 )  AS "SPREAD_TASA_EMISION", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "MODALIDAD_PAGO", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "PERIODICIDAD_PAGO", -- CHAR(1)
            '0001-01-01'  AS "FECHA_EMISION", -- CHAR(10)
            COALESCE (CAST('1900-01-01' AS VARCHAR(15)),'')  AS "FECHA_VCTO_PAPEL", -- VARCHAR(15)
            COALESCE (ORD."PrecioOrden" , 0 )  AS "OIF_PRECIO", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "PARIDAD_MORIGEN", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "CAPTACION_MORIGEN", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(3)),'')  AS "MON_MORIGEN" -- CHAR(3)

        FROM "BSCL.Orden.BD::ORD.Orden" ORD
        LEFT JOIN (
			SELECT  CO_OR."IdOrden", 
					SUM( CASE WHEN CO_CLA."CodigoCobroClasificacion" = 'COMISION' THEN CO_OR."MontoCobroCalculadoLocal" ELSE 0 END ) AS  "MontoComision",
					SUM( CASE WHEN CO_CLA."CodigoCobroClasificacion" = 'GASTO'    THEN CO_OR."MontoCobroCalculadoLocal" ELSE 0 END ) AS  "MontoGasto",
					SUM( CASE WHEN CO_CLA."CodigoCobroClasificacion" = 'DERECHO'  THEN CO_OR."MontoCobroCalculadoLocal" ELSE 0 END ) AS  "MontoDerecho",
   					SUM( CASE WHEN CO_CLA."CodigoCobroClasificacion" = 'IMPUESTO' THEN CO_OR."MontoCobroCalculadoLocal" ELSE 0 END ) AS  "MontoIva"
			FROM "BSCL.Orden.BD::ORD.CobroOrden"  as CO_OR
			JOIN "BSCL.Plataforma.BD::COM.Cobro" AS CO ON (CO_OR."IdCobro" = CO."IdCobro")
			JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CO_CLA ON (CO."IdCobroClasificacion" = CO_CLA."IdCobroClasificacion")
			GROUP BY CO_OR."IdOrden"
		) AS COB_OR ON (ORD."IdOrden" = COB_OR."IdOrden")
		
        WHERE ORD."IdClasificacionTipoInstrumento" = 'IF'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.OPERACION_IF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

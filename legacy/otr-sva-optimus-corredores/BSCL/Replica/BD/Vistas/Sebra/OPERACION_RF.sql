SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_RF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_RF" AS

Select 
            ORD."IdOrden",
            COALESCE (ORD."IdOrden", 0 ) AS "SOL_SEC", -- NUMERIC
            REPLACE(CAST(ORD."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR') AS "TIPO_REAJ", -- CHAR(2)
            LEFT(COALESCE (ORD."Nemotecnico",''   ),16)  AS "ORF_NEMO", -- CHAR(16)
            COALESCE ( 0 , 0 )  AS "ORF_CANT_DISPONIBLE", -- NUMERIC
            COALESCE (ORD."Cantidad" , 0 )  AS "ORF_CANT_TOTAL", -- NUMERIC
            COALESCE (ORD."IdTipoPrecio",'')  AS "ORF_TIPO_PRECIO", -- CHAR(1)
            COALESCE (ORD."TasaRFIF" , 0 )  AS "ORF_TIR", -- NUMERIC
            COALESCE (ORD."PrecioOrden",0 )  AS "ORF_PRECIO_LIMITE", -- NUMERIC
            COALESCE (ORD."MontoLocal" , 0 )  AS "ORF_MTO_TOTAL", -- NUMERIC
            COALESCE (COB_OR."MontoComision" , 0 )  AS "ORF_MTO_COMISION", -- NUMERIC
            COALESCE (COB_OR."MontoDerecho" , 0 )  AS "ORF_MTO_DERECHOS", -- NUMERIC
            COALESCE (COB_OR."MontoGasto" , 0 )  AS "ORF_MTO_GASTOS", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "ORF_CANT_ASIGNAC_INI", -- NUMERIC
            COALESCE (ASIG."CantidadAsignada" , 0 )  AS "ORF_CANT_ASIGNAC_ACUM", -- NUMERIC
            COALESCE (CAST(ORD."OrdenVigente" AS VARCHAR(1)),'')  AS "ORF_IND_VIGENTE", -- CHAR(1)
            COALESCE (ORD."FechaOperacion" , '1900-01-01' )  AS "ORF_FEC_ORDEN", -- DATETIME
            COALESCE (CAST('CC' AS VARCHAR(2)),'')  AS "ORF_CONCEPTO", -- CHAR(2)
            COALESCE ( ORD."IdBoveda" ,'DCV')  AS "ORF_BOVEDA", -- CHAR(3)
            'COM'  AS "ORF_TRA_BOVEDA", -- CHAR(3)
            COALESCE ( 0 , 0 )  AS "ORF_TRA_FILIAL", -- INT
            COALESCE ( 0 , 0 )  AS "ORF_TRA_PERSONA", -- INT
            COALESCE ( 0 , 0 )  AS "ORF_TRA_CUENTA", -- INT
            TO_VARCHAR (CASE WHEN ORD."FechaLiquidacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaLiquidacion" END ,'YYYY-MM-DD') AS "ORF_FEC_LIQUIDACION", -- DATETIME
            COALESCE ( 0 , 0 )  AS "ORF_OP_SWAP", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "ORF_TIPO_ORDEN", -- CHAR(2)
            COALESCE (CAST('P' AS VARCHAR(1)),'')  AS "ORF_TIP_MERCADO", -- CHAR(1)
            COALESCE ( 0 , 0 )  AS "ORF_PLAZO", -- SMALLINT
            COALESCE ( 0 , 0 )  AS "ORF_MTO_GARANTIA", -- DECIMAL
            COALESCE ( 0.19 , 0 )  AS "ORF_VAL_IVA", -- DECIMAL
            COALESCE (ORD."MontoLocalPlazo" , 0 )  AS "ORF_MTO_FINAL", -- NUMERIC
            COALESCE (ORD."TasaRFIF" , 0 )  AS "ORF_TASA", -- NUMERIC
            TO_VARCHAR (CASE WHEN ORD."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaVencimiento" end ,'YYYY-MM-DD') AS "ORF_FEC_VCTO", -- DATETIME
            COALESCE ( 0 , 0 )  AS "PARIDAD_MORIGEN", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "CAPTACION_MORIGEN", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(3)),'')  AS "MON_MORIGEN" -- CHAR(3)

        FROM "BSCL.Orden.BD::ORD.Orden" AS ORD
        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON TOPE."IdTipoOperacion" = ORD."IdTipoOperacion"
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
		LEFT JOIN (
		 	SELECT  ASI."IdOrden", 
		 			SUM( ASI."CantidadAsignada") AS  "CantidadAsignada"
		  	FROM "BSCL.Orden.BD::ORD.Asignacion" as ASI
		  	GROUP BY ASI."IdOrden"
		) AS ASIG ON (ORD."IdOrden" = ASIG."IdOrden")
		
        WHERE ORD."IdClasificacionTipoInstrumento" = 'RF'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.OPERACION_RF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

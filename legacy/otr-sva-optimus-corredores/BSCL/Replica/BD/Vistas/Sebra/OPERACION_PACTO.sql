SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_PACTO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_PACTO" AS

Select 
            COALESCE (OPE."IdOperacion" , 0 )  AS "SOL_SEC", -- NUMERIC
            REPLACE(CAST(OPE."IdTipoReajuste" AS VARCHAR(2)), 'CL' , 'NR')  AS "TIPO_REAJ", -- CHAR(2)
            COALESCE (CAST(OPE."IdEmisor" AS VARCHAR(16)),'')  AS "OPC_EMISOR", -- CHAR(16)
            COALESCE (CAST(OPE."IdMonedaPrincipal" AS VARCHAR(3)),'')  AS "OPC_MONEDA", -- VARCHAR(3)
            COALESCE (OPE."Tasa" , 0 )  AS "OPC_TASA", -- NUMERIC
            COALESCE (OPE."TasaAgente" , 0 )  AS "OPC_TASA_AGENTE", -- NUMERIC
            COALESCE ( 0 , 0 )  AS "OPC_TASA_REFERENCIA", -- NUMERIC
            COALESCE (OPE."PlazoEnDias" , 0 )  AS "OPC_DIAS_PLAZO", -- SMALLINT
            COALESCE(TO_VARCHAR(CASE WHEN OPE."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE OPE."FechaVencimiento" END  , 'YYYY-MM-DD'),'1900-01-01')  AS "OPC_FEC_VCTO", -- DATETIME
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "OPC_FAMILIA_INSTRUMENTO", -- CHAR(2)
            COALESCE ( 0 , 0 )  AS "OPC_VAL_RESCATE", -- NUMERIC
            COALESCE ( '1900-01-01' , '1900-01-01' )  AS "OPC_FEC_ANTICIPO", -- DATETIME
            COALESCE (OPE."MontoOperacionAnticipo" , 0 )  AS "OPC_MTO_ANTICIPO", -- NUMERIC
            COALESCE (OPE."MontoOperacion" , 0 )  AS "OPC_CANT_CAPITAL", -- NUMERIC
            COALESCE (OPE."MontoOperacionFinal" , 0 )  AS "OPC_CANT_COMPROM", -- NUMERIC
            COALESCE (OPE."MontoOperacion" , 0 )  AS "OPC_MTO_CAPITAL", -- NUMERIC
            COALESCE (OPE."MontoOperacionFinal" , 0 )  AS "OPC_MTO_COMPROM" -- NUMERIC

        FROM "BSCL.Mesa.BD::MES.Operacion" OPE
        WHERE OPE."IdTipoOperacion" IN ('COMPAC','VENPAC')
		and OPE."IdEstado" = 'VIG'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.OPERACION_PACTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

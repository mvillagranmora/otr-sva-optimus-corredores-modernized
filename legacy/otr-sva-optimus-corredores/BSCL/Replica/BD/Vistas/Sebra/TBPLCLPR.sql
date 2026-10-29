SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLCLPR"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLCLPR" AS

	Select 
             COALESCE (CAST('' AS VARCHAR(1)),'')  AS "TIPO_REG", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(11)),'')  AS "RUT_CLI", -- CHAR(11)
            COALESCE (CAST('' AS VARCHAR(100)),'')  AS "SEC_RUT_CLI", -- VARCHAR(100)
            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "PRODUCTO", -- CHAR(4)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_DEST_VC_CUP", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_01", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_02", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_03", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_04", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_05", -- CHAR(1)
            COALESCE ( 0 , 0 )  AS "MONTO_GASTO", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "TIPO_COMIS", -- CHAR(2)
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "TIPO_MON_GASTO", -- CHAR(2)
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "TIPO_PER_GASTO", -- CHAR(2)
            COALESCE ( 0 , 0 )  AS "VAL_COMIS", -- DECIMAL
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_06", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_07", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_08", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_09", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "IND_LIBRE_10", -- CHAR(1)
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "FORMA_PAGO", -- CHAR(2)
            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "FORMA_RECIBO", -- CHAR(2)
            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FEC_VCTO", -- CHAR(10)
            COALESCE ( 0 , 0 )  AS "PLAZO", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "PORC_DERE", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "COMIS_MINIM", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "MONTO_TOPE", -- DECIMAL
            COALESCE ( 0 , 0 )  AS "LEVERAGE" -- DECIMAL

        from dummy


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLCLPR" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

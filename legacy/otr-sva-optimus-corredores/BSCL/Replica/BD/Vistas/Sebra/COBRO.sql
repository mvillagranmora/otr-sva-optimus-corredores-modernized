SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.COBRO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.COBRO" AS

Select 
            COALESCE (CAST(RPAD(COB."IdCobro",4) AS VARCHAR(4)),'')  AS "COB_ID", -- CHAR(4)
            COALESCE (CAST(
				CASE WHEN CLA."CodigoCobroClasificacion" = 'COMISION' THEN 'TCOBRCOMIS'
					 WHEN CLA."CodigoCobroClasificacion" = 'DERECHO' THEN 'TCOBRDEREC'
					 WHEN CLA."CodigoCobroClasificacion" = 'GASTO' THEN 'TCOBRGASTO'
					 WHEN CLA."CodigoCobroClasificacion" = 'IMPUESTO' THEN 'TCOBRIMPUE'
					 WHEN CLA."CodigoCobroClasificacion" = 'RETENCION' THEN 'TCOBRRETEN'
					 ELSE '' END AS VARCHAR(10)),'')  AS "PAR_TCO_ID", -- VARCHAR(10)
            COALESCE (CAST(COB."NombreCobro" AS VARCHAR(50)),'')  AS "COB_NOMBRE", -- VARCHAR(50)
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "COB_DIF_CANAL", -- CHAR(1)
            COALESCE (CAST(
				CASE WHEN COB."FormaCobro" = 'F' THEN 'TPCARFIJO'
					 WHEN COB."FormaCobro" = 'V' THEN 'TPCARVARIA'
					 WHEN COB."FormaCobro" = 'E' THEN 'TPCAREXENT'
					 ELSE '' END AS VARCHAR(10)),'')  AS "PAR_TIP_CARGO", -- VARCHAR(10)
            COALESCE (CAST('DESCONOCID' AS VARCHAR(10)),'')  AS "PAR_TIP_EVENTO", -- VARCHAR(10)
            COALESCE (CAST('N/A' AS VARCHAR(3)),'')  AS "COB_PERIODO", -- CHAR(3)
            COALESCE (CAST('DESCONOCID' AS VARCHAR(10)),'')  AS "PAR_BAS_CARGO", -- VARCHAR(10)
            COALESCE (CAST('TPBENNINGU' AS VARCHAR(10)),'')  AS "PAR_BEN_TIPO", -- VARCHAR(10)
            0.0  AS "COB_BEN_META", -- NUMERIC
            COALESCE (CAST('N/A' AS VARCHAR(3)),'')  AS "COB_BEN_UNIDAD", -- VARCHAR(3)
            COALESCE (CAST('N/A' AS VARCHAR(3)),'')  AS "COB_BEN_MONEDA", -- VARCHAR(3)
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "COB_INC_IVA", -- CHAR(1)
            COALESCE (CAST('N' AS VARCHAR(1)),'')  AS "COB_COM_ESP" -- CHAR(1)

        FROM "BSCL.Plataforma.BD::COM.Cobro" COB
        LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" CLA ON COB."IdCobroClasificacion" = Cla."IdCobroClasificacion"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.COBRO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

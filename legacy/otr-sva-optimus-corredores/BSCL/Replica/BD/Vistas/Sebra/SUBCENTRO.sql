SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.SUBCENTRO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.SUBCENTRO" AS

	SELECT 
            COALESCE (CAST(CCO."ValorIndicador" AS VARCHAR(4)),'')  AS "CEN_ID", -- CHAR(4)
            COALESCE (CAST(CCO."CodigoTipoIndicador" AS VARCHAR(4)),'')  AS "SUB_ID", -- CHAR(4)
            COALESCE (CAST(TIP."NombreTipoIndicador" AS VARCHAR(50)),'')  AS "SUB_NOMBRE", -- VARCHAR(50) 
            COALESCE (CAST(CCO."Habilitado" AS VARCHAR(1)),'')  AS "SUB_HABILITADO" -- CHAR(1)

        FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIP
        INNER JOIN (
        	SELECT DISTINCT IND."ValorIndicador", TIP."CodigoTipoIndicador", IND."IdTipoIndicador", 'S' AS "Habilitado"
                           FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIP
                           INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON TIP."IdTipoIndicador" = IND."IdTipoIndicador"
                           WHERE TIP."CodigoTipoIndicador" = 'CENCOSTO'
        ) AS CCO ON CCO."IdTipoIndicador" = TIP."IdTipoIndicador"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.SUBCENTRO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

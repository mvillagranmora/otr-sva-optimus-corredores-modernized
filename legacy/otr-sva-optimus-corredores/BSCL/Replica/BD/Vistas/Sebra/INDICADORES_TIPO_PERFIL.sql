SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_TIPO_PERFIL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_TIPO_PERFIL" AS

        SELECT		
			--COALESCE (CAST(PCO."IdPerfilComercialBase" AS VARCHAR(40)),'')	AS "TPE_ID", -- CHAR(2)
			TPE.TPE_ID AS "TPE_ID", -- CHAR(2),
			IND2."IdTipoIndicador" 											AS "IND_ID", -- INT
			COALESCE (CAST(IND2."ValorIndicadorDefecto" AS VARCHAR(1)),'')	AS "DIN_ID", -- CHAR(1)			
			--CAST(COALESCE(PCO."FechaCreacion", '1900-01-01')	AS DATETIME)					AS "ITP_FEC_INICIO" -- DATETIME
			TO_NVARCHAR(CASE WHEN LENGTH(PCO."FechaCreacion") = 0 THEN '1900-01-01 00:00:00' ELSE PCO."FechaCreacion" END,'YYYY-MM-DD HH24:MI:SS') AS "ITP_FEC_INICIO" -- DATETIME
		FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" AS PCO
		CROSS JOIN  "BSCL.Plataforma.BD::IND.TipoIndicador" AS IND2
		LEFT JOIN "BSCL.Replica.BD.Vistas::Sebra.TIPO_PERFIL_COMERCIAL" TPE ON PCO."IdPerfilComercialBase" = TPE."IdPerfilComercialBase"
		WHERE COALESCE (CAST(IND2."ValorIndicadorDefecto" AS VARCHAR(1)),'') != ''
            
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.INDICADORES_TIPO_PERFIL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    

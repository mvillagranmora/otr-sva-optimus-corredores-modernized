SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TIPO_PERFIL_COMERCIAL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TIPO_PERFIL_COMERCIAL" AS

		SELECT 
		    --COALESCE (PCO."IdPerfilComercialBase" ,'')  		AS "TPE_ID", -- CHAR(2)
			RIGHT(CONCAT('00',ROW_NUMBER() OVER (PARTITION BY PCO."IdEmpresa" ORDER BY PCO."FechaCreacion" ASC)),2) AS "TPE_ID", -- CHAR(2)
		    COALESCE (CAST(PCO."DescripcionPerfilComercialBase" AS VARCHAR(30)),'')	AS "TPE_NOMBRE", -- CHAR(30)
		    COALESCE (CAST(PCO."IdPerfilProductos" AS VARCHAR(10)),'')  		AS "PAR_TPP_ID", -- VARCHAR(10)
		    COALESCE (CAST(PCO."IdPerfilCobros" AS VARCHAR(10)),'')  		AS "PAR_TPC_ID", -- VARCHAR(10)
		    COALESCE (CAST(PCO."IdPerfilLimites" AS VARCHAR(10)),'')  		AS "PAR_LINEA_ID", -- VARCHAR(10)
		    COALESCE (CAST(PCO."IdModeloFacturacion" AS VARCHAR(10)),'')  		AS "PAR_MFA_ID", -- VARCHAR(10)
		    PCO."IdPerfilComercialBase"
		FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" PCO


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TIPO_PERFIL_COMERCIAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.USO_TELEFONOS"; 

--SELECT * FROM bscl."BSCL.Replica.BD.Vistas::Sebra.USO_TELEFONOS" where dir_id is null

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.USO_TELEFONOS" AS

		
		Select 
			CAST(PER."IdPersona" AS varchar) AS "IdPersona",
            COALESCE (PER."IdTelefonoPrincipal", 0 )  AS "TEL_ID", -- INT
            COALESCE (PER."IdDireccionFisicaPrincipal" , 0 )  AS "DIR_ID", -- INT
            COALESCE (PER."FechaCreacion" , '1900-01-01' )  AS "UTE_FEC_INICIO" -- DATETIME
        FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
        WHERE "IdDireccionFisicaPrincipal" IS NOT NULL AND "IdTelefonoPrincipal" IS NOT NULL	

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.USO_TELEFONOS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

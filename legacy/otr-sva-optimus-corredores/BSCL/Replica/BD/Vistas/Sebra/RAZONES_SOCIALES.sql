SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.RAZONES_SOCIALES"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.RAZONES_SOCIALES" AS

        SELECT	COALESCE(NF."IdNombreFantasia", NULLIF(PER."IdNombreFantasiaPrincipal", 0), PER."IdPersona" * -1) AS "RSO_ID",
       			CAST(
					CASE 
						WHEN instr(DOC."DocumentoIdentidad" , '-') = 0 THEN -1 
						ELSE trim(LEFT(DOC."DocumentoIdentidad"  , instr(DOC."DocumentoIdentidad" , '-')-1)) END AS INT) AS "PER_ID",--int] NOT NULL,***	
	            COALESCE ( 0 , 0 )  AS "GIR_ID", -- INT
	             COALESCE (CAST(CASE WHEN PER."Tipo" = 'J' THEN PER."RazonSocial" ELSE PER."ApellidoPaterno" END AS VARCHAR(60)),'')  AS "RSO_PATERNO", -- VARCHAR(60)
	            COALESCE (CAST(CASE WHEN PER."Tipo" = 'J' THEN PER."RazonSocial" ELSE PER."ApellidoMaterno" END AS VARCHAR(50)),'')  AS "RSO_MATERNO", -- VARCHAR(50)
	            COALESCE (CAST(CASE WHEN PER."Tipo" = 'J' THEN '' ELSE PER."Nombres" END AS VARCHAR(30)),'')  AS "RSO_NOMBRES", -- VARCHAR(30)
	            CASE WHEN LENGTH(COALESCE(NF."DescripcionNombreFantasia",PER."RazonSocial",'')) > 0 THEN  CAST(COALESCE(NF."DescripcionNombreFantasia",PER."RazonSocial",'') AS VARCHAR(80))
	            ELSE PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" || ' ' || PER."Nombres"   END           
	            AS "RSO_RAZ_SOCIAL" -- VARCHAR(80)
        FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
        JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NF ON PER."IdPersona" = NF."IdPersona"
		WHERE DOC."DocumentoIdentidad" not like '%.%'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.RAZONES_SOCIALES" TO  BCS_DESARROLLADOR WITH GRANT OPTION

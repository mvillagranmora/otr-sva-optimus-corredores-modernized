SET SCHEMA "BSCL"; 
 GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda"; 
 GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda" AS
	SELECT 	
		BOV."IdBoveda", 
		BOV."Codigo", 
		BOV."Nombre", 
		BOV."IdEstado", 
		COALESCE(ESTBOV."Nombre",'') as "NombreEstado",
		BOV."IdPersona",
		COALESCE(PER."NombreCompleto",'') as "NombreCompletoPersona",
		COALESCE(PER."TipoDocumentoIdentidad",'') AS "TipoDocumentoIdentidad", 
		COALESCE(PER."DocumentoIdentidad",'') as "DocumentoIdentidad",
		COALESCE(SOC."IdSocioDeNegocio",0) as "IdSocioDeNegocio"
		
	FROM "BSCL.Plataforma.BD::PLA.Boveda" AS BOV
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
			ON PER."IdPersona" = BOV."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SOC
			ON SOC."IdPersona" = BOV."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTBOV
			ON ESTBOV."IdEstado" = BOV."IdEstado";
GO			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoveda" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
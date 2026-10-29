SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor";
GO


CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"  
AS 
SELECT 
	EMI."IdEmisor",
	EMI."IdEmisor" AS "NombreCorto",
	EMI."CodigoEmisor",
	EMI."CodigoRegulador",
	EMI."Regulador",
	CASE WHEN IFNULL(EMI."EmiteIIF",'') = '' THEN 'N' ELSE EMI."EmiteIIF" END AS "EmiteIIF",
	CASE WHEN IFNULL(EMI."EmiteIRF",'') = '' THEN 'N' ELSE EMI."EmiteIRF" END AS "EmiteIRF",
	CASE WHEN IFNULL(EMI."EmiteFungible",'') = '' THEN 'N' ELSE EMI."EmiteFungible" END AS "EmiteFungible",
	EMI."ValorClasificacionRiesgoIIF",
	EMI."IdEntidadClasificadoraRiesgo",
	CASE WHEN IFNULL(EMI."IndicadorAGF",'') = '' THEN 'N' ELSE EMI."IndicadorAGF" END AS "IndicadorAGF",
	EMI."IdPersona",
	EMI."IdEstado",
	EMI."FechaActualizacion",
	DOC."DocumentoIdentidad",
	DOC."IdTipoDocumentoIdentidad",
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno" 
		ELSE PER."RazonSocial"
	END AS "NombreCompleto",
	EMI."TipoEntidad"
	
FROM "BSCL.Plataforma.BD::MER.Emisor" EMI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER 
	ON PER."IdPersona" = EMI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC
	ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO


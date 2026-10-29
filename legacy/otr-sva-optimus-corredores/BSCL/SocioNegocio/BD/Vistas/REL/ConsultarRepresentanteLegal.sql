SET SCHEMA "BSCL";
 GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegal";
 GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegal"
AS
SELECT  Relacion."IdPersonaDestino" AS "IdPersonaRepresentante"
       ,Relacion."DocumentoIdentidadDestino" AS "DocumentoIdentidadRepresentante"
       ,Relacion."IdTipoDocumentoIdentidadDestino" AS "IdTipoDocumentoIdentidadRepresentante"
       ,Relacion."NombreTipoDocumentoIdentidadDestino" AS "NombreTipoDocumentoIdentidadRepresentante"
       ,Relacion."NombreCompletoDestino" AS "NombreCompletoRepresentante"
	   ,Relacion."IdPersonaOrigen" AS "IdPersonaRepresentado"
	   ,Relacion."IdEstado" AS "IdEstadoRelacion"
	   ,Cliente."IdCliente" AS "IdClienteRepresentado"
	   ,Cliente."DocumentoIdentidad" AS "DocumentoIdentidadRepresentado"
	   ,Cliente."CodigoPortafolio"
	   ,Cliente."IdPortafolio"
	   ,Cliente."IdEstado" AS "IdEstadoPortafolio"
	   ,Relacion."RazonSocialOrigen" AS "NombreCompletoRepresentado"
	   ,Cliente."IdEmpresa"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" Relacion 
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" Cliente ON (Cliente."IdPersona" = Relacion."IdPersonaOrigen")
WHERE "IdTipoRelacion" IN ('REPAD','REP');

GO	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRepresentanteLegal" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

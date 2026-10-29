SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona"
AS
	SELECT   DI."IdDocumentoIdentidad"
			,DI."IdPersona"
			,DI."IdTipoDocumentoIdentidad"
			,DI."DocumentoIdentidad" 
			,DI."NumeroSerie" 
			,DI."FechaEmision" 
			,DI."FechaVencimiento" 
			,DI."IdEstado" 
			,DI."FechaIngreso"
			,DI."FechaCreacion"
			,DI."FechaModificacion"
			,DI."IdCarga"
			,DI."IdEstadoAutorizacion"
			,DI."IdOrigen"
			,DI."Borrado"
			,DI."IdUsuarioCreador"
			,DI."Predeterminado"
			,DI."IdTipoDocumentoIdentidadAsociado"
			,DI."TipoPersona"
			,DI."DocumentoIdentidadAsociado"
	FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PE ON DI."IdPersona" = PE."IdPersona";

GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

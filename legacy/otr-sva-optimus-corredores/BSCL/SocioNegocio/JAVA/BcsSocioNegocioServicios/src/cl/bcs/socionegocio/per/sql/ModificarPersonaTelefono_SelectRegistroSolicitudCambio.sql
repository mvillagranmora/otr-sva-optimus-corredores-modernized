-- Obtiene datos de los registros a modificar

WITH RegistroCambio AS
(
	SELECT 	COL."DATA_TYPE_NAME",
			COL."COLUMN_NAME",
			RCAM."CampoModificado",
			RCAM."ValorNuevo"
	FROM SYS.TABLE_COLUMNS AS COL
	INNER JOIN "BSCL.SocioNegocio.BD::GBL.ModificarPersona_RegistroCambio" AS RCAM ON (COL."COLUMN_NAME"= RCAM."CampoModificado")
	WHERE 	COL."TABLE_NAME"='BSCL.SocioNegocio.BD::PER.Telefono' 
	AND 	COL.SCHEMA_NAME='BSCL'
)

SELECT					
		CAST(:IdSolicitudCambio AS BIGINT)	AS "IdSolicitudCambio",
		:IdTipoEntidad						AS "IdTipoEntidad",
		CAST(:IdTelefono AS NVARCHAR(40))  	AS "IdEntidadOrigen",
		RCAM."CampoModificado" 				AS "CampoModificado",
		RCAM."DATA_TYPE_NAME" 				AS "TipoDato",		
		(CASE RCAM."COLUMN_NAME" WHEN 'IdPais'				THEN IFNULL(CAST (DOC."IdPais" 			AS NVARCHAR(255)),'')
								WHEN 'IdTipoTelefono'		THEN IFNULL(CAST (DOC."IdTipoTelefono" 	AS NVARCHAR(255)),'')
								WHEN 'Numero'				THEN IFNULL(CAST (DOC."Numero" 			AS NVARCHAR(255)),'')
								WHEN 'Anexo'				THEN IFNULL(CAST (DOC."Anexo" 			AS NVARCHAR(255)),'')
								WHEN 'CodigoProposito'		THEN IFNULL(CAST (DOC."CodigoProposito" AS NVARCHAR(255)),'')
								WHEN 'Comentario'			THEN IFNULL(CAST (DOC."Comentario" 		AS NVARCHAR(255)),'')
								WHEN 'IdEstado'				THEN IFNULL(CAST (DOC."IdEstado" 		AS NVARCHAR(255)),'')
		END) AS "ValorAntiguo",		
		RCAM."ValorNuevo" AS "ValorNuevo",	
		:IdEstadoAutorizacion AS  "IdEstadoAutorizacion"

FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
INNER JOIN RegistroCambio AS RCAM ON (1=1)
WHERE  DOC."IdTelefono" = :IdTelefono;
			
	

-- Obtiene datos de los registros a modificar

WITH RegistroCambio AS
(
	SELECT 	COL."DATA_TYPE_NAME",
			COL."COLUMN_NAME",
			RCAM."CampoModificado",
			RCAM."ValorNuevo"
	FROM SYS.TABLE_COLUMNS AS COL
	INNER JOIN "BSCL.SocioNegocio.BD::GBL.ModificarPersona_RegistroCambio" AS RCAM ON (COL."COLUMN_NAME"= RCAM."CampoModificado")
	WHERE 	COL."TABLE_NAME"='BSCL.SocioNegocio.BD::PER.Persona' 
	AND 	COL.SCHEMA_NAME='BSCL'
)


	
SELECT					
		CAST(:IdSolicitudCambio AS BIGINT)	AS "IdSolicitudCambio",
		:IdTipoEntidad						AS "IdTipoEntidad",
		CAST(:IdCuentaBancaria AS NVARCHAR(40))  AS "IdEntidadOrigen",
		RCAM."CampoModificado" 			AS "CampoModificado",
		RCAM."DATA_TYPE_NAME" 			AS "TipoDato",
		(CASE RCAM."COLUMN_NAME" 	WHEN 'IdEstado'						THEN IFNULL(CAST (DOC."IdEstado" AS NVARCHAR(255)),'')
									WHEN 'Tipo'							THEN IFNULL(CAST (DOC."Tipo" AS NVARCHAR(255)),'')
									WHEN 'Nombres'						THEN IFNULL(CAST (DOC."Nombres" AS NVARCHAR(255)),'')
									WHEN 'ApellidoPaterno'				THEN IFNULL(CAST (DOC."ApellidoPaterno" AS NVARCHAR(255)),'')											
									WHEN 'ApellidoMaterno'				THEN IFNULL(CAST (DOC."ApellidoMaterno" AS NVARCHAR(255)),'')
									WHEN 'RazonSocial'					THEN IFNULL(CAST (DOC."RazonSocial" AS NVARCHAR(255)),'')
									WHEN 'IdGiroPrincipal'				THEN IFNULL(CAST (DOC."IdGiroPrincipal" AS NVARCHAR(255)),'')
									WHEN 'IdPaisNacimiento'		  		THEN IFNULL(CAST (DOC."IdPaisNacimiento" AS NVARCHAR(255)),'')
									WHEN 'IdPaisResidencia'		  		THEN IFNULL(CAST (DOC."IdPaisResidencia" AS NVARCHAR(255)),'')
									WHEN 'FechaNacimiento'  			THEN IFNULL(CAST (DOC."FechaNacimiento" AS NVARCHAR(255)),'')
									WHEN 'FechaDefuncion'	 			THEN IFNULL(CAST (DOC."FechaDefuncion" AS NVARCHAR(255)),'')
									WHEN 'Sexo'							THEN IFNULL(CAST (DOC."Sexo" AS NVARCHAR(255)),'')
									WHEN 'EsExtranjero'	 			  	THEN IFNULL(CAST (DOC."CodigoIban" AS NVARCHAR(255)),'')
									WHEN 'EstadoCivil'					THEN IFNULL(CAST (DOC."EstadoCivil" AS NVARCHAR(255)),'')
									WHEN 'CodigoTipoCargoEmpleado'		THEN IFNULL(CAST (DOC."CodigoTipoCargoEmpleado" AS NVARCHAR(255)),'')
									WHEN 'Empleador'					THEN IFNULL(CAST (DOC."Empleador" AS NVARCHAR(255)),'')
									WHEN 'IdProfesion'					THEN IFNULL(CAST (DOC."IdProfesion" AS NVARCHAR(255)),'')
									WHEN 'IdUsuarioCreador'				THEN IFNULL(CAST (DOC."IdUsuarioCreador" AS NVARCHAR(255)),'')
									WHEN 'IdPersonaConyuge'				THEN IFNULL(CAST (DOC."IdPersonaConyuge" AS NVARCHAR(255)),'')
									WHEN 'IdPersonaRepresentante'		THEN IFNULL(CAST (DOC."IdPersonaRepresentante" AS NVARCHAR(255)),'')
									WHEN 'IdPaisNacionalidadPrincipal'	THEN IFNULL(CAST (DOC."IdPaisNacionalidadPrincipal" AS NVARCHAR(255)),'')
									WHEN 'GlobalLegalEntity'			THEN IFNULL(CAST (DOC."GlobalLegalEntity" AS NVARCHAR(255)),'')
									WHEN 'IdPersonaEmpleador'			THEN IFNULL(CAST (DOC."IdPersonaEmpleador" AS NVARCHAR(255)),'')
									WHEN 'IdRangoPatrimonio'			THEN IFNULL(CAST (DOC."IdRangoPatrimonio" AS NVARCHAR(255)),'')
									WHEN 'IdGiroCIIU'					THEN IFNULL(CAST (DOC."IdGiroCIIU" AS NVARCHAR(255)),'')
		END) AS "ValorAntiguo",
		RCAM."ValorNuevo" AS "ValorNuevo",	
		:IdEstadoAutorizacion AS  "IdEstadoAutorizacion"
		
FROM "BSCL.SocioNegocio.BD::PER.Persona" DOC
INNER JOIN RegistroCambio AS RCAM ON (1=1)
WHERE  DOC."IdPersona" = :IdPersona;
			
	
			
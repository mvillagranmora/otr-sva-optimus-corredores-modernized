
-- Obtiene datos de los registros a modificar

WITH RegistroCambio AS
(
	SELECT 	COL."DATA_TYPE_NAME",
			COL."COLUMN_NAME",
			RCAM."CampoModificado",
			RCAM."ValorNuevo"
	FROM SYS.TABLE_COLUMNS AS COL
	INNER JOIN "BSCL.SocioNegocio.BD::GBL.ModificarPersona_RegistroCambio" AS RCAM ON (COL."COLUMN_NAME"= RCAM."CampoModificado")
	WHERE 	COL."TABLE_NAME"='BSCL.SocioNegocio.BD::PER.CuentaBancaria' 
	AND 	COL.SCHEMA_NAME='BSCL'
)


	
SELECT					
		CAST(:IdSolicitudCambio AS BIGINT)	AS "IdSolicitudCambio",
		:IdTipoEntidad						AS "IdTipoEntidad",
		CAST(:IdCuentaBancaria AS NVARCHAR(40))  AS "IdEntidadOrigen",
		RCAM."CampoModificado" 			AS "CampoModificado",
		RCAM."DATA_TYPE_NAME" 			AS "TipoDato",
		(CASE RCAM."COLUMN_NAME" WHEN 'IdBanco'					  THEN IFNULL(CAST (DOC."IdBanco" AS NVARCHAR(255)),'')
								WHEN 'IdTipoCuentaBancaria'		  THEN IFNULL(CAST (DOC."IdTipoCuentaBancaria" AS NVARCHAR(255)),'')
								WHEN 'IdMoneda'					  THEN IFNULL(CAST (DOC."IdMoneda" AS NVARCHAR(255)),'')
								WHEN 'NumeroCuentaBancaria'		  THEN IFNULL(CAST (DOC."NumeroCuentaBancaria" AS NVARCHAR(255)),'')											
								WHEN 'IdEstado'					  THEN IFNULL(CAST (DOC."IdEstado" AS NVARCHAR(255)),'')
								WHEN 'IdPlaza'					  THEN IFNULL(CAST (DOC."IdPlaza" AS NVARCHAR(255)),'')
								WHEN 'IdTipoIdentificadorCuenta'  THEN IFNULL(CAST (DOC."IdTipoIdentificadorCuenta" AS NVARCHAR(255)),'')
								WHEN 'IdentificadorCuenta'		  THEN IFNULL(CAST (DOC."IdentificadorCuenta" AS NVARCHAR(255)),'')
								WHEN 'IdPersonaRelacionada'		  THEN IFNULL(CAST (DOC."IdPersonaRelacionada" AS NVARCHAR(255)),'')
								WHEN 'CodigoCuentaInterbancario'  THEN IFNULL(CAST (DOC."CodigoCuentaInterbancario" AS NVARCHAR(40)),'')
								WHEN 'CodigoAba'	 			  THEN IFNULL(CAST (DOC."CodigoAba" AS NVARCHAR(50)),'')
								WHEN 'OtroCodigoBancoExtranjero'  THEN IFNULL(CAST (DOC."OtroCodigoBancoExtranjero" AS NVARCHAR(50)),'')
								WHEN 'CodigoIban'	 			  THEN IFNULL(CAST (DOC."CodigoIban" AS NVARCHAR(50)),'')
								WHEN 'OtroCodigoCuentaExtranjero' THEN IFNULL(CAST (DOC."OtroCodigoCuentaExtranjero" AS NVARCHAR(50)),'')
								WHEN 'EsPac'                      THEN IFNULL(CAST (DOC."EsPac" AS NVARCHAR(50)),'')
		END) AS "ValorAntiguo",
		RCAM."ValorNuevo" AS "ValorNuevo",	
		:IdEstadoAutorizacion AS  "IdEstadoAutorizacion"
		
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DOC
INNER JOIN RegistroCambio AS RCAM ON (1=1)
WHERE  DOC."IdCuentaBancaria" = :IdCuentaBancaria;
			
	
			
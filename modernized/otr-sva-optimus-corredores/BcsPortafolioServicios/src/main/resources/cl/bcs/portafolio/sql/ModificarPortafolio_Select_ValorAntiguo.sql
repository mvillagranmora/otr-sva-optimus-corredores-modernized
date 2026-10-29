SELECT
	COL."DATA_TYPE_NAME" AS "TipoDato",
	COL."COLUMN_NAME" AS "NombreColumna",
	:CampoModificado AS "CampoModificado",
	:ValorNuevo AS "ValorNuevo",
	(CASE COL."COLUMN_NAME"
		WHEN 'CodigoPortafolio' THEN IFNULL(CAST(DOC."CodigoPortafolio" AS NVARCHAR(255)), '')
		WHEN 'DescripcionPortafolio' THEN IFNULL(CAST(DOC."DescripcionPortafolio" AS NVARCHAR(255)), '')
		WHEN 'Proposito' THEN IFNULL(CAST(DOC."Proposito" AS NVARCHAR(255)), '')
		WHEN 'Alias' THEN IFNULL(CAST(DOC."Alias" AS NVARCHAR(255)), '')
		WHEN 'IdPersonaBrokerExtranjero' THEN IFNULL(CAST(DOC."IdPersonaBrokerExtranjero" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilComercialBaseOrigen' THEN IFNULL(CAST(DOC."IdPerfilComercialBaseOrigen" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilProductos' THEN IFNULL(CAST(DOC."IdPerfilProductos" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilProductosExtra' THEN IFNULL(CAST(DOC."IdPerfilProductosExtra" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilCobros' THEN IFNULL(CAST(DOC."IdPerfilCobros" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilCobrosExtra' THEN IFNULL(CAST(DOC."IdPerfilCobrosExtra" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilCobrosPeru' THEN IFNULL(CAST(DOC."IdPerfilCobrosPeru" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilCobrosPeruExtra' THEN IFNULL(CAST(DOC."IdPerfilCobrosPeruExtra" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilLimites' THEN IFNULL(CAST(DOC."IdPerfilLimites" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilLimitesExtra' THEN IFNULL(CAST(DOC."IdPerfilLimitesExtra" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilCondicionesOperativas' THEN IFNULL(CAST(DOC."IdPerfilCondicionesOperativas" AS NVARCHAR(255)), '')
		WHEN 'IdPerfilCondicionesOperativasExtra' THEN IFNULL(CAST(DOC."IdPerfilCondicionesOperativasExtra" AS NVARCHAR(255)), '')
		WHEN 'IdDireccionEnvioCorrespondencia' THEN IFNULL(CAST(DOC."IdDireccionEnvioCorrespondencia" AS NVARCHAR(255)), '')
		WHEN 'IdTelefonoContacto' THEN IFNULL(CAST(DOC."IdTelefonoContacto" AS NVARCHAR(255)), '')
		WHEN 'IdEmpleadoEjecutivo' THEN IFNULL(CAST(DOC."IdEmpleadoEjecutivo" AS NVARCHAR(255)), '')
		WHEN 'NumeroFicha' THEN IFNULL(CAST(DOC."NumeroFicha" AS NVARCHAR(255)), '')
		WHEN 'IdNombreFantasia' THEN IFNULL(CAST(DOC."IdNombreFantasia" AS NVARCHAR(255)), '')
		WHEN 'AreaCliente' THEN IFNULL(CAST(DOC."AreaCliente" AS NVARCHAR(255)), '')
		WHEN 'IdEmpleadoEjecutivoComercial' THEN IFNULL(CAST(DOC."IdEmpleadoEjecutivoComercial" AS NVARCHAR(255)), '')
	END) AS "ValorAntiguo"
FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC,
	SYS.TABLE_COLUMNS COL
WHERE COL."TABLE_NAME" = 'BSCL.Portafolio.BD::POR.Portafolio'
AND COL.SCHEMA_NAME = (SELECT CURRENT_SCHEMA FROM DUMMY)
AND COL."COLUMN_NAME" = :CampoModificado
AND DOC."IdPortafolio" = :IdPortafolio

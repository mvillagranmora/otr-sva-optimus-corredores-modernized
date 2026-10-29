SELECT
	'D' AS "TipoRegistro" --Constante
	,TO_CHAR(ASIGNACION."FechaOperacionOrden",'DDMMYYYY') AS "FechaOrden"
	,TO_BIGINT(CASE WHEN COALESCE(ASIGNACION."FolioOrden",'') = '' THEN 0 ELSE ASIGNACION."FolioOrden" END) AS "FolioOrden"
	,'1' AS "TipoCodigoCliente"
	,COALESCE(DOCUMENTO_RUT."DocumentoIdentidad",'') AS "CodigoCliente"
	,TO_DECIMAL(ASIGNACION."CantidadAsignada",26,8) AS "Cantidad"
	,TO_CHAR(ASIGNACION."FechaAsignacion",'DDMMYYYY') AS "FechaOperacion"
	,TO_BIGINT(ASIGNACION."FolioTransaccion") AS "FolioOperacion"
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacionUI" ASIGNACION
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE ON (CLIENTE."IdCliente" = ASIGNACION."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_RUT ON (CLIENTE."IdPersona" = DOCUMENTO_RUT."IdPersona" AND DOCUMENTO_RUT."IdTipoDocumentoIdentidad" = 'RUT')
WHERE 
(TO_CHAR(ASIGNACION."FechaAsignacion",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD'))
AND (:EsTodosCliente = 'S' OR ASIGNACION."IdCliente" = :IdCliente)
AND "NombreEstadoFacturacion" = 'FACTURADO'
--FALTA ESO DE LOS CLIENTES FFMM
AND ASIGNACION."IdEmpresa" = :IdEmpresa;
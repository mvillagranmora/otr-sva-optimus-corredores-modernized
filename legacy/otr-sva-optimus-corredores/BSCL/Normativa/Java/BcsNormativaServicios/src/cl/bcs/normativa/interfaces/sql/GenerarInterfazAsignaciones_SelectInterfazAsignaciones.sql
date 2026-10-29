SELECT
	'D' AS "TipoRegistro" --Constante
	,'S80080' AS "CodigoAI" --Constante
	,TO_CHAR(ASIGNACION."FechaOperacionOrden",'DDMMYYYY') AS "FechaOrden"
	,TO_BIGINT(CASE WHEN COALESCE(ASIGNACION."FolioOrden",'') = '' THEN 0 ELSE ASIGNACION."FolioOrden" END) AS "FolioOrden"
	,TO_CHAR(ASIGNACION."FechaAsignacion",'DDMMYYYY') AS "FechaOperacion"
	,TO_CHAR(ASIGNACION."FechaAsignacion",'HHmmSS') AS "HoraMinutoSegundoOperacion"
	,TO_BIGINT(ASIGNACION."FolioTransaccion") AS "FolioOperacion"
	,TO_DECIMAL("PrecioAsignacion",26,8) AS "Precio"
	,TO_DECIMAL(ASIGNACION."CantidadAsignada",26,8) AS "CantidadAsignada"
	,COALESCE(HOMOLOGACION_MONEDA."ValorHomologacion",'') AS "MonedaOperacion" --Pendiente Mapeo
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacionUI" ASIGNACION
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_MONEDA ON (HOMOLOGACION_MONEDA."IdCampo" = 'Moneda' AND HOMOLOGACION_MONEDA."Valor" = ASIGNACION."IdMonedaOperacion")
WHERE (TO_CHAR(ASIGNACION."FechaAsignacion",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD'))
AND (:EsTodosCliente = 'S' OR ASIGNACION."IdCliente" = :IdCliente)
AND (:EsTodosInstrumentos = 'S' OR ASIGNACION."IdInstrumento" = :IdInstrumento)
AND "NombreEstadoFacturacion" = 'FACTURADO'
AND ASIGNACION."IdEmpresa" = :IdEmpresa;
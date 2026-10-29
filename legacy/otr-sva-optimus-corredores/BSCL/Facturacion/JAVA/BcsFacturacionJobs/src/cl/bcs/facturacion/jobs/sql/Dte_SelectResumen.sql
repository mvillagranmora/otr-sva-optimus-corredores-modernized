

WITH ValidacionTareaDTE AS 
(
	-- Valida si se dan condiciones para mostrar cuadro resumen
	SELECT TAR."IdTareaDTE",TAR."IdComprobante", COUNT(*) AS "Validacion"
	FROM "BSCL.Facturacion.BD::FAC.TareaDTE" AS TAR
	INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COM ON (TAR."IdComprobante" = COM."IdComprobante")
	INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS DET ON DET."IdComprobante" = COM."IdComprobante"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON COM."IdPortafolio" = POR."IdPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IND ON IND."IdEntidad" = POR."IdPortafolio"	
	WHERE TAR."IdTareaDTEGrupo" = :IdTareaDTEGrupo	
	AND ((TAR."IdEstadoDTE" IN ('ENV', 'REC') AND TAR."TipoTareaDTE" = 'DTEACT') OR (TAR."IdEstadoTareaDTE" = 'PEN' AND TAR."TipoTareaDTE" <> 'DTEACT'))	
	AND DET."IdClasificacionTipoInstrumento" = 'RV'
	AND IND."IdTipoIndicador" = 19
	AND IND."ValorIndicador" = 'S'
	GROUP BY TAR."IdTareaDTE", TAR."IdComprobante"
)
SELECT  
	TAR."IdTareaDTE",
	nemo."NemotecnicoInstrumento" AS "NombreInstrumento",
	ROUND (cant."CantidadTotal",4) AS "CantidadTotal",
	ROUND (ppp."PPPTotal",4) AS "ValorPrecioPromedio"

FROM ValidacionTareaDTE AS TAR
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS Com ON (TAR."IdComprobante"=Com."IdComprobante")

LEFT JOIN (
	SELECT "IdComprobante","NemotecnicoInstrumento" 
	FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
	GROUP BY "IdComprobante","NemotecnicoInstrumento") AS nemo ON nemo."IdComprobante" = Com."IdComprobante"

LEFT JOIN (
	SELECT "IdComprobante", SUM("Cantidad") AS "CantidadTotal" 
	FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"
	GROUP BY "IdComprobante") AS cant ON cant."IdComprobante" = Com."IdComprobante"

LEFT JOIN (
	SELECT "IdComprobante",SUM("PPP") AS "PPPTotal" 
	FROM (
		SELECT ComDet."IdComprobante",ComDet."Precio" * Pon."Ponderacion"  AS "PPP"
		FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS ComDet
		LEFT JOIN (
			SELECT det."IdComprobante",det."IdComprobanteDetalle",det."Cantidad" / pon."CantidadTotal" AS "Ponderacion" 
			FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS det
			LEFT JOIN (
				SELECT "IdComprobante",SUM("Cantidad") AS "CantidadTotal" 
				FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" 
				GROUP BY "IdComprobante") AS pon ON det."IdComprobante" = pon."IdComprobante")  AS Pon ON Pon."IdComprobante" = ComDet."IdComprobante" AND Pon."IdComprobanteDetalle" = ComDet."IdComprobanteDetalle")
GROUP BY "IdComprobante") AS ppp ON ppp."IdComprobante" = Com."IdComprobante";

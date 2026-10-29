SELECT CMB."IdComprobanteFormaDePago" ,'Monto' AS "Campo", CAST ( CMB."Monto" -  ( (CMB."Monto" * CAL."PorcentajeReducido") /100)AS NVARCHAR) AS "Valor"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CMB
INNER JOIN 
(
	SELECT DET."IdComprobante",CO."PorcentajeReducido"
	FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" DET
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" REL
	ON (REL."IdComprobanteDetalle"=DET."IdComprobanteDetalle")
	INNER JOIN 
	( 
		SELECT EGR."IdIngresoEgreso", (COM."MontoRetencionGananciaCap" * 100 / EGR."Monto") AS "PorcentajeReducido"
		FROM
		 "BSCL.Facturacion.BD::FAC.Comprobante" COM 
		 INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" EGR ON (EGR."IdComprobanteOrigen"=COM."IdComprobante" AND EGR."IdModulo"='FAC' AND EGR."IdTipoMovimiento"='MOVEGR')
		 WHERE COM."IdComprobante" IN (:IdComprobante) AND COM."CargoGananciaCap"='C' AND COM."MontoRetencionGananciaCap">0
	) AS CO  ON  ( CO."IdIngresoEgreso"=REL."IdIngresoEgreso" )
	GROUP BY  DET."IdComprobante",CO."PorcentajeReducido"
 ) CAL ON ( CAL."IdComprobante"= CMB."IdComprobante");
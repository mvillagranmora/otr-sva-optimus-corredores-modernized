SELECT DET."IdComprobanteDetalle" ,'Monto' AS "Campo", CAST ( DET."Monto" -  ( (DET."Monto" * CO."PorcentajeReducido") /100)AS NVARCHAR) AS "Valor"
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
 
 

 
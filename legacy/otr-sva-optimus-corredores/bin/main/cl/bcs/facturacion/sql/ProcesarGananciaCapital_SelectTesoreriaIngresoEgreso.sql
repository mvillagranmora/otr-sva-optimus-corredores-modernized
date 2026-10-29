SELECT EGR."IdIngresoEgreso",'Monto' AS "Campo", CAST ( (EGR."Monto" - COM."MontoRetencionGananciaCap")AS NVARCHAR) AS "Valor"
FROM
 "BSCL.Facturacion.BD::FAC.Comprobante" COM 
 INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" EGR ON (EGR."IdComprobanteOrigen"=COM."IdComprobante" AND EGR."IdModulo"='FAC' AND EGR."IdTipoMovimiento"='MOVEGR')
 WHERE COM."IdComprobante" IN (:IdComprobante) AND "CargoGananciaCap"='C' AND "MontoRetencionGananciaCap">0;

 
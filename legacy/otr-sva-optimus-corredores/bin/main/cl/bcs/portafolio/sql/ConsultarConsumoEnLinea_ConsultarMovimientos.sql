(
SELECT 
 CASE WHEN ASG."IdEstadoAsignacion" <> '' AND FAC."IdEstadoFacturacion" <> 'FAC' THEN ASG."IdEstadoAsignacion"
	  WHEN FAC."IdEstadoFacturacion" = 'FAC' THEN 'FAC'
	 ELSE ORD."IdEstadoOrden" 
END AS  "IdEstado",
ORD."IdClasificacionTipoInstrumento" AS "Mercado",
CASE WHEN ASG."IdEstadoAsignacion" <> '' AND FAC."IdEstadoFacturacion" <> 'FAC' THEN ASG."MontoAsignado"
	  WHEN FAC."IdEstadoFacturacion" = 'FAC' THEN FAC."Monto"
	 ELSE ORD."MontoOrden" 
END AS "Monto",
ORD."IdMonedaOrden" AS "IdMoneda",
'' AS "Tipo"
FROM "BSCL.Orden.BD::ORD.Orden" ORD
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ASG ON ORD."IdOrden" = ASG."IdOrden"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC ON ORD."IdOrden" = FAC."IdOrden" AND ASG."IdAsignacion" = FAC."IdAsignacion" 
WHERE ORD."IdEstadoOrden" IN ('ING','OFE','EJE','EJP','ENR')
AND ORD."IdTipoInstrumento" != 'SIM'
AND ORD."OrdenVigente" = 'S'
AND ORD."EsVentaCorta" = 'N'
AND ORD."IdEmpresa" = :IdEmpresa
AND ORD."IdPortafolioCliente" = :IdPortafolio
AND ORD."FechaOperacion" <= :FechaOperacion
)

UNION ALL 

--FFMM
(SELECT 
"Estado" AS "IdEstado",
'FM' AS "Mercado",
"Monto",
"IdMoneda",
"TipoSolicitud" AS "Tipo"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarAporteReinversionAgrupado"
WHERE "TipoSolicitud" IN ('APO') 
AND "Estado" NOT IN ('ANU')
AND "IdEmpresa" = :IdEmpresa
AND "IdPortafolio" = :IdPortafolio
AND "FechaCertificacion" <= :Fecha
)

UNION ALL 

--Cuenta Inversion
(SELECT 
"IdEstado",
'' AS "Mercado",
"Monto",
"IdMoneda",
"TipoMovimiento" AS "Tipo"
FROM  "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos"
WHERE  "IdEstado" NOT IN ('ANU')
AND "IdEmpresa" = :IdEmpresa
AND "IdPortafolio" = :IdPortafolio
AND "FechaEfectivaMovimiento" <= :Fecha
)

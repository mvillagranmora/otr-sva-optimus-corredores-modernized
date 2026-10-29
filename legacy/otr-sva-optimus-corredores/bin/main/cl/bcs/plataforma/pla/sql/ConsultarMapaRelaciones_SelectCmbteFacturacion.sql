SELECT CMBTE."IdComprobante",CMBTE."IdTipoComprobante",CMBTE."FolioComprobante",CMBTE."IdEstadoComprobante",CMBTE."MontoLiquidacionFinal",CMBTE."IdMonedaLiquidacion",
TC."Nombre" AS "NombreTipoComprobante",EST."Nombre" AS "NombreEstadoComprobante",CMBTE."DocumentoIdentidad"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE
INNER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" TC ON CMBTE."IdTipoComprobante" = TC."IdTipoComprobante"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EST."IdEstado" = CMBTE."IdEstadoComprobante"
WHERE CMBTE."IdComprobante" IN (:ids)
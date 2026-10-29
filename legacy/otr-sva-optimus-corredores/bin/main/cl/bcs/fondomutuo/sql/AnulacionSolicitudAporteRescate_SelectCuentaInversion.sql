SELECT CARGO_ABONO."IdCargoAbonoMovimiento", SOLICITUD."IdSolicitudAporteRescate" 
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS CARGO_ABONO 
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SOLICITUD ON (SOLICITUD."IdSolicitudAporteRescate" = CARGO_ABONO."IdComprobante")
WHERE (SOLICITUD."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) OR SOLICITUD."IdSolicitudAporteRescateRelacionado" IN (:IdSolicitudAporteRescate)) 
AND CARGO_ABONO."IdTipoProducto" = 'INFM' AND CARGO_ABONO."IdTipoComprobante" = 'FFM' 
AND CARGO_ABONO."IdEstado" = 'CRT';
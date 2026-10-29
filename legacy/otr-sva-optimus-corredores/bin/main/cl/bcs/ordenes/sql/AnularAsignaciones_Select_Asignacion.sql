SELECT "IdAsignacion", "IdEstadoAsignacion","FolioTransaccion", "IdOrden","NombreCompleto","FechaAsignacion","IdTipoCanalDistribucion","IdInstrumentoUI"
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion"
WHERE "IdAsignacion" IN (:IdAsignacion);
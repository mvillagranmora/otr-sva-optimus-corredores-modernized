SELECT "IdAsignacion", "MontoAsignado", "ValorRescateAsignado", "MontoAsignadoLocal", "NombreCompleto" ,"DocumentoIdentidad"  
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion"
WHERE "IdTransaccion" = :IdTransaccion AND "IdEstadoAsignacion" = 'EAF'
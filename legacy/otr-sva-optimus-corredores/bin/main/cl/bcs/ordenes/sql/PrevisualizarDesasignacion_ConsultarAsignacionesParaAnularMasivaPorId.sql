SELECT "IdAsignacion","IdTransaccion","IdOrden","Instrumento" AS "Nemotecnico","NombreLibroOrdenes","IdBolsa",
       "IdInstrumento","IdClasificacionTipoInstrumento","IdLibroOrdenes","IdTipoInstrumento","AccionOrden","FechaAsignacion",
       "IdTipoCanalIngreso","NombreTipoCanalIngreso","IdAsignacionMasiva","IdCliente", "NombreCompleto", "DocumentoIdentidad", 
       "IdPortafolio", "IdEstadoAsignacion", "IdTipoOperacion", "FolioTransaccion", "CantidadAsignada", "PrecioAsignacion" AS "Precio", "MontoAsignado",
       "IdMonedaOperacion", "IdMonedaLiquidacionOrden", "ValorTipoCambio",
       "IdCondicionDeLiquidacion", "MarcaAsignacion"
FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion"
WHERE "IdAsignacionMasiva" = :IdAsignacionMasiva
AND "IdEstadoAsignacion" = 'NEF' AND "IdTipoCanalDistribucion" = 'MAN';
SELECT "IdOrden","IdInstrumento","Nemotecnico","IdEstadoOrden","IdEstadoConfirmacion","IdCliente","IdPortafolioCliente","FechaOperacion","FechaConfirmacion","FechaConfirmacionPlazo"  
FROM "BSCL.Orden.BD::ORD.Orden" 
WHERE "IdEmpresa" = :IdEmpresa AND "IdOrden" IN (:IdOrden);

SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionBolsa"; 
GO

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTransaccionBolsa" AS 
SELECT TransBol."IdTransaccionBolsa", 
       TransBol."Folio" AS "FolioTransaccion", 
       TransBol."FechaSistemaCreacion", 
       TransBol."HoraSistemaCreacion", 
       TransBol."FechaOperacion" AS "FechaOperacionTransaccion", 
       TransBol."FechaModificacionOperacion", 
       TransBol."FechaLiquidacion", 
       TransBol."FechaEmision", 
       TransBol."FechaVencimiento", 
       TransBol."FechaIngresoOferta", 
       TransBol."FechaIngresoRegistro",
       ConsIns."IdInstrumento",
       TransBol."Nemotecnico" AS "InstrumentoUI", 
       ConsIns."IdClasificacionTipoInstrumento",
       ConsIns."CodigoClasificacionTipoInstrumento",
       ConsIns."NombreClasificacionTipoInstrumento",
       ConsIns."IdMoneda" AS "IdMonedaInstrumento",
       TransBol."Cantidad" AS "CantidadTransaccion", 
       TransBol."Monto" AS "MontoTransaccion", 
       TransBol."Precio" AS "PrecioTransaccion",
       EstOrden."IdEstado",
       Estado."Nombre" AS "EstadoTransaccion"
       
FROM "BSCL.Orden.BD::ORD.TransaccionBolsa" AS TransBol
 INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
    ON ConsIns."Nemotecnico" = TransBol."Nemotecnico"
 INNER JOIN "BSCL.Orden.BD::ORD.EstadoOrden" AS EstOrden
    ON EstOrden."IdEstado" = TransBol."IdEstado"
   AND EstOrden."NombreEntidad"= Upper('TRANSACCIONBOLSA') 
 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado
    ON Estado."IdEstado" = EstOrden."IdEstado";
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTransaccionBolsa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEventoBitacoraUI";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEventoBitacoraUI"
AS SELECT 
 evt."IdEventoBitacora",
 evt."NombreEntidad",
 evt."IdEntidad",
 evt."IdOrden",
 evt."IdAsignacion",
 evt."IdTransaccion",
 evt."FechaEventoBitacora",
 evt."MensajeEventoBitacora",
 evt."FechaIngresoRegistro",
 evt."IdTipoEventoBitacora",
 tip."DescripcionTipoEventoBitacora"
FROM "BSCL.Orden.BD::ORD.EventoBitacora" evt
INNER JOIN "BSCL.Orden.BD::ORD.TipoEventoBitacora" tip on evt."IdTipoEventoBitacora" = tip."IdTipoEventoBitacora" ;


GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEventoBitacoraUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

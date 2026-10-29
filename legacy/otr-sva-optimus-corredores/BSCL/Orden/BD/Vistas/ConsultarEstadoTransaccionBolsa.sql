SET SCHEMA "BSCL";
GO
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoTransaccionBolsa";
GO
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarEstadoTransaccionBolsa" AS 
SELECT 
    EstOrd."IdEstado",
	Estado."Nombre" AS "NombreEstado",
	EstOrd."NombreEntidad"

FROM "BSCL.Orden.BD::ORD.EstadoOrden" AS EstOrd
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" As Estado 
    ON Estado."IdEstado" = EstOrd."IdEstado"
WHERE EstOrd."NombreEntidad" = UPPER('TransaccionBolsa');
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarEstadoTransaccionBolsa" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignadoDiaOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignadoDiaOrden"
AS SELECT 
asig."IdOrden",COALESCE(SUM(asig."CantidadAsignada"),0) as "AsignadoDia"
FROM "BSCL.Orden.BD::ORD.Asignacion" asig
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" fecha on "CodigoParametro" = 'PLA.FechaSistema' and asig."FechaAsignacion" > TO_DATE(fecha."Valor") AND "FechaAsignacion" <= ADD_DAYS (TO_DATE (fecha."Valor"), 1)   
GROUP BY "IdOrden";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarAsignadoDiaOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
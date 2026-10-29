SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoAsignacion"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarTipoAsignacion"
AS  
SELECT  asig."IdTipoAsignacion",
        asig."Nombre",
        asig."IdEstado",
        asig."SoportaAsignacionAutomatica",
        bla."EsContraOrden",
        bla."IdClasificacionTipoInstrumento"
FROM "BSCL.Orden.BD::ORD.TipoAsignacion" asig
LEFT JOIN "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" bla on asig."IdTipoAsignacion" = bla."IdTipoAsignacion"
WHERE "IdEstado" = 'ACT'
;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarTipoAsignacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

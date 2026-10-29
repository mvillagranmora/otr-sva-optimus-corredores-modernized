SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenValidacion";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenValidacion"
AS 

SELECT
     "ID",
    "IdDocumento",
    "IdProceso",
    "IdUsuario",
    "CodigoValidacion",
    "DescripcionValidacion",
    "CantidadCasos",
    "FechaValidacion"
FROM "BSCL.Normativa.BD::TRI.ResumenValidacion";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenValidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

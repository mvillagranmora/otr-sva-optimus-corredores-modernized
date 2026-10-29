SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCarga";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCarga"
AS 

SELECT
    "ID",
    "IdProceso",
    "IdDocumento",
    "IdUsuario",
    "Fecha",
    "Glosa",
    "NombreArchivo",
    "TotalRegistrosLeidos",
    "TotalRegistrosCargados",
    "TotalRegistrosErroneos"
FROM "BSCL.Normativa.BD::TRI.ResumenCarga";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCarga" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

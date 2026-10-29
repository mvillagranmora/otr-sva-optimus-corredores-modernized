SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCuadratura";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCuadratura"
AS 

SELECT
    "ID",
    "IdProceso",
    "IdDocumento",
    "IdUsuario",
    "FechaCuadratura",
    "AnoMes",
    "TipoCuadratura",
    "TipoClasificador",
    "TipoDiferencia",
    "Glosa",
    "CantidadCasos"

FROM "BSCL.Normativa.BD::TRI.ResumenCuadratura";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenCuadratura" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

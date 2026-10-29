SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarParametros";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarParametros" AS
SELECT 
    "Esquema",
    "Version",
    "Codigo",
    "Descripcion",
    "Valor",
    "EsModificableUsuario",
    "Uso",
    "ValorDefecto",
    "ValoresPosibles",
    "ModificablePorUsuario",
    "ModificablePostProduccion",
    "CreadorParametro",
    "FechaCreacion",
    "ModificadorParametro",
    "FechaModificacion",
    "Observaciones",
    "IdEmpresa"
FROM "BSCL.Normativa.BD::DOC.Parametro";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarParametros" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
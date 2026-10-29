SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenGeneracion";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenGeneracion"
AS 

SELECT
    ResumenGeneracion."ID",
    ResumenGeneracion."IdProceso",
    ResumenGeneracion."IdDocumento",
    ResumenGeneracion."IdUsuario",
    ADD_SECONDS(ResumenGeneracion."Fecha",Configuracion."Valor") AS "Fecha",
    ResumenGeneracion."Glosa",
    ResumenGeneracion."TotalRegistrosGenerados",
    ResumenGeneracion."NombreArchivo",
    Usuario."CodigoUsuario" AS "Usuario"
FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion" AS ResumenGeneracion
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario ON Usuario."IdUsuario" = ResumenGeneracion."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" Configuracion ON "CodigoParametro" = 'PLA.SegundosZonaHoraria' and Usuario."IdEmpresa" = Configuracion."IdEmpresa";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarResumenGeneracion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
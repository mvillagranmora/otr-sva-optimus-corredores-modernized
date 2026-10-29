SET SCHEMA "BSCL";

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioConfiguracionDefecto";

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioConfiguracionDefecto" AS
SELECT
    "IdPortafolioConfiguracionDefecto",
    "CodigoPortafolio",
    "Proposito"
FROM "BSCL.Portafolio.BD::POR.PortafolioConfiguracionDefecto";

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioConfiguracionDefecto" TO BCS_DESARROLLADOR WITH GRANT OPTION;
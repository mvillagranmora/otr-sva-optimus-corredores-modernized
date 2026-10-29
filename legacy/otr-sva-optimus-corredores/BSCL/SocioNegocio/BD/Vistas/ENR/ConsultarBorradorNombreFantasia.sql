SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarBorradorNombreFantasia";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarBorradorNombreFantasia" AS

SELECT
"IdNombreFantasia"
, "DescripcionNombreFantasia" AS "NombreFantasia"
, "IdEnrolamiento"
FROM "BSCL.SocioNegocio.BD::ENR.NombreFantasia";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarBorradorNombreFantasia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
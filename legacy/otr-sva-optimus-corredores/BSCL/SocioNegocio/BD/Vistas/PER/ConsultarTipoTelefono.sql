SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoTelefono";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoTelefono" AS
SELECT
	TTEL."IdTipoTelefono",
	TTEL."Nombre" 
FROM "BSCL.SocioNegocio.BD::PER.TipoTelefono" as TTEL;
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoTelefono" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
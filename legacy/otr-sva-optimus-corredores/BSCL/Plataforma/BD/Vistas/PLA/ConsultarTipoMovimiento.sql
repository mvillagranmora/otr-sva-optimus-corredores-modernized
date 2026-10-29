SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" AS
SELECT
	 "IdTipoMovimiento"
	,"IdModulo"
	,"Tipo"
	,"Nombre"
	,"IdEstado"
 FROM "BSCL.Plataforma.BD::PLA.TipoMovimiento"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoMovimiento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

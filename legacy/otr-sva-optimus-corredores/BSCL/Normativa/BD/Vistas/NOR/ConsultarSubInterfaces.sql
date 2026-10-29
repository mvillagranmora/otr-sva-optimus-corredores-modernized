SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarSubInterfaces";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarSubInterfaces" AS
SELECT "IdInterfaz", "Codigo", "Nombre", "Descripcion", "Padre", "TipoInterfaz", "Estado"
FROM "BSCL.Normativa.BD::Interfaces"
WHERE"Padre"!='0';

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarSubInterfaces" TO BCS_DESARROLLADOR WITH GRANT OPTION;

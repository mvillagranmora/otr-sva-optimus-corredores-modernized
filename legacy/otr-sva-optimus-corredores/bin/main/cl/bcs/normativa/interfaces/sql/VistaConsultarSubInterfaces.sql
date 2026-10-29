SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::NOR.ConsultarSubInterfaces";
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::NOR.ConsultarSubInterfaces" AS
SELECT *
FROM "BSCL.Normativa.BD::Interfaces"
WHERE"Padre"=!0
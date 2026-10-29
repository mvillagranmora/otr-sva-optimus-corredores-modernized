SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::NOR.ConsultarInterfaces";
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::NOR.ConsultarInterfaces" AS
SELECT *
FROM "BSCL.Normativa.BD::Interfaces"
WHERE"Padre"=0
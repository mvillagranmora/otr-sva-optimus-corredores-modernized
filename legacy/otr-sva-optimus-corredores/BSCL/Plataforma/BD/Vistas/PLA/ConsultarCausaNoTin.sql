SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCausaNoTin"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCausaNoTin"
AS
	SELECT
		  "IdCausaNoTin",
		  "Descripcion"
	FROM "BSCL.Plataforma.BD::PLA.CausaNoTin";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCausaNoTin" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
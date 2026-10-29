SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaBanco";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaBanco" as
SELECT
	B."IdBanco" AS ban_id,
	B."NombreBanco" AS ban_nombre
FROM "BSCL.Plataforma.BD::BAN.Banco" B
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaBanco" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GOahi conec
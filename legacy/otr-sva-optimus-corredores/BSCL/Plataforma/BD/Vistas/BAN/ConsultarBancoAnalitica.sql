SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBancoAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBancoAnalitica" AS
SELECT 
	"NombreBanco",
	"CodigoBanco",
	"IdBanco",
	"IdPaisOperacion",
	"IdPersona",
	"IdEstado",
	"IdEmpresa",
	"IdTipoBanco"
FROM "BSCL.Plataforma.BD::BAN.Banco";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBancoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

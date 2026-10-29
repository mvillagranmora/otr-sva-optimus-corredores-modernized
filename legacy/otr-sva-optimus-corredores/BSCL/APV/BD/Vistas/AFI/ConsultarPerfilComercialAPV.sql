SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarPerfilComercialAPV";

CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarPerfilComercialAPV" AS 

SELECT
	"IdPerfilComercialAPV",
	"IdEmpresa",
	"IdCategoriaInversionista",
	"IdPerfilComercialAbierto",
	"IdPerfilComercialActivo",
	"IdEstado"
FROM "BSCL.APV.BD::APV.PerfilComercialAPV" AS Contrato;

GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarPerfilComercialAPV" TO BCS_DESARROLLADOR WITH GRANT OPTION;

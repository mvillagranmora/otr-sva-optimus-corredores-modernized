SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::ConsultarPerfilComercialBaseAPV";

CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarPerfilComercialBaseAPV" AS

SELECT
	PCB."IdPerfilComercialBase",
	PCB."DescripcionPerfilComercialBase",
	PCB."IdCategoriaInversionista",
	PCB."IdPerfilProductos",
	PCB."IdPerfilCobros",
	PCB."IdPerfilCobrosPeru",
	PCB."IdPerfilLimites",
	PCB."IdPerfilCondicionesOperativas",
	PCB."IdModeloFacturacion",
	PCB."FechaCreacion",
	PCB."FechaModificacion",
	PCB."IdEstado",
	PCB."PorDefecto",
	PCB."IdEmpresa",
	PCA."IdPlan"
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase" PCB
INNER JOIN (
	SELECT
		 PLACAT."IdPlan"
		,CATINV."IdCategoriaInversionista"
		,CATINV."IdEmpresa"
		,CASE WHEN PLACAT."IdPlan" = 1 THEN PRFAPV."IdPerfilComercialAbierto"
			  WHEN PLACAT."IdPlan" = 2 THEN PRFAPV."IdPerfilComercialActivo" END AS "IdPerfilComercial"
	FROM "BSCL.APV.BD::APV.PerfilComercialAPV" PRFAPV
	INNER JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" CATINV
		ON (PRFAPV."IdCategoriaInversionista" = CATINV."IdCategoriaInversionista" AND
			PRFAPV."IdEmpresa" = CATINV."IdEmpresa" AND
			PRFAPV."IdEstado" = 'VIG')
	INNER JOIN "BSCL.APV.BD::APV.PlanCategoria" PLACAT
		ON (CATINV."IdCategoriaInversionista" = PLACAT."IdCategoria")
	) PCA
	ON (PCB."IdPerfilComercialBase" = PCA."IdPerfilComercial" AND
		PCB."IdCategoriaInversionista" = PCA."IdCategoriaInversionista" AND
		PCB."IdEmpresa" = PCA."IdEmpresa")
;

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarPerfilComercialBaseAPV" TO BCS_DESARROLLADOR WITH GRANT OPTION;

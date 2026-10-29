SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialAdicional";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialAdicional" AS
SELECT
	PCA."IdPerfilComercialAdicional",
	PCA."IdCategoriaInversionista",
	PCA."IdFormaCobro",
	PCA."IdPerfilComercialBase",
	PCA."IdPerfilCobros",
	PCA."IdPerfilProductos",
	PCA."IdEstado",
	PCA."PorDefecto",
	PCA."IdEmpresa",
	PCA."CodigoPortafolio"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PCA
WHERE PCA."IdEstado" = 'ACT'
ORDER BY PCA."IdPerfilComercialBase", PCA."IdFormaCobro";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialAdicional" TO BCS_DESARROLLADOR WITH GRANT OPTION;

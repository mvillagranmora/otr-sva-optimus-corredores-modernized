SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaPerfilInversion";
GO
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaPerfilInversion" AS
SELECT
	PerfilComercial."IdPerfilComercialBase"
   ,PerfilComercial."DescripcionPerfilComercialBase"
   ,PerfilComercial."IdEstado"
   ,CategoriaInversionista."IdCategoriaInversionista"
   ,CategoriaInversionista."NombreCategoriaInversionista"
   ,CategoriaInversionista."IdEmpresa"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" AS PerfilComercial
LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" AS CategoriaInversionista ON CategoriaInversionista."IdCategoriaInversionista" = PerfilComercial."IdCategoriaInversionista" AND CategoriaInversionista."IdEmpresa" = PerfilComercial."IdEmpresa"
ORDER BY
	PerfilComercial."IdPerfilComercialBase";
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaPerfilInversion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO
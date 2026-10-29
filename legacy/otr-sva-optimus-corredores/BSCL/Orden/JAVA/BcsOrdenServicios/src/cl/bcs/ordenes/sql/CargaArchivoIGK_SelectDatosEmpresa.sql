SELECT
CEMP."RazonSocial" AS "NombreEmpresa"
,CEMP."DireccionFisicaPrincipalCalle" AS "DireccionEmpresa"
,CEMP."DireccionFisicaPrincipalNombreCiudad" AS "CiudadEmpresa"
,CEMP."DireccionFisicaPrincipalNombreDivisionPais" AS "DistritoEmpresa"
,CEMP."DireccionFisicaPrincipalNombrePais" AS "PaisEmpresa"
 FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CEMP WHERE CEMP."IdEmpresa" = :IdEmpresa
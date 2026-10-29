SELECT 
	PCA."IdCategoriaInversionista",
	PCA."IdFormaCobro",
	PCA."IdPerfilComercialBase",
	PCA."IdPerfilCobros",
	PCA."IdPerfilProductos",
	PCA."IdEstado",
	PCA."PorDefecto",
	PCA."IdEmpresa",
	PCA."CodigoPortafolio"
FROM "BSCL.Plataforma.BD::GBL.ConfiguracionPerfilComercialAdicional_Entrada" AS GBL
INNER JOIN  "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON TI."CodigoTipoIndicador" = 'COBROCUST' AND TI."IdTipoEntidad" = 'PORTAFOLIO'
INNER JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DET ON UPPER(DET."DetalleIndicador") = UPPER(GBL."IdFormaCobro") AND TI."IdTipoIndicador" = DET."IdTipoIndicador"
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PCA 
	ON PCA."IdCategoriaInversionista" = GBL."IdCategoriaInversionista" 
	AND UPPER(PCA."IdFormaCobro") = UPPER(DET."DetalleIndicador") 
	AND PCA."CodigoPortafolio" = GBL."CodigoPortafolio"
	AND PCA."IdEmpresa" = GBL."IdEmpresa"
WHERE GBL."IdTrabajo" = :IdTrabajo;
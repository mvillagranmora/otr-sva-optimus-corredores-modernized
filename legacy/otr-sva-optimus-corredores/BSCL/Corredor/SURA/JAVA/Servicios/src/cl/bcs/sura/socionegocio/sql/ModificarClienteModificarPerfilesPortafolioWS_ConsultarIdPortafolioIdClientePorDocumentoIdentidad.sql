SELECT DISTINCT 
	  POR."IdPortafolio"
	, POR."IdCliente"
	, DOCI."DocumentoIdentidad"
	, COALESCE(IND."IdIndicador", 0) AS "IdIndicador"
	, COALESCE(PERF."IdFormaCobro", '') AS "IdFormaCobro"
	, CLI."IdCategoriaInversionista"
	, COALESCE(PERF."IdPerfilComercialBaseOrigen" , '') AS "IdPerfilComercial"
	, COALESCE(PERF."IdPerfilCobros", '') AS "IdPerfilCobros"
	, COALESCE(PERF."IdPerfilProductos", '') AS "IdPerfilProductos"
FROM "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" AS DOCI ON DOCI."IdPersona" = POR."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = POR."IdPortafolio" AND IND."CodigoTipoIndicador" = 'COBROCUST'
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PERF ON PERF."IdFormaCobro" = IND."ValorIndicador" AND PERF."IdCategoriaInversionista" = CLI."IdCategoriaInversionista"
WHERE DOCI."DocumentoIdentidad" IN (:DocumentoIdentidad) AND DOCI."Predeterminado" = 'S';
SELECT 
	  POR."IdPortafolio"
	, POR."IdCliente"
	, DOCI."DocumentoIdentidad"
	, COALESCE(IND."IdIndicador", 0) AS "IdIndicador"
	, COALESCE(PERF."IdFormaCobro", '') AS "IdFormaCobro"
	, CLI."IdCategoriaInversionista"
	, COALESCE(PERF."IdPerfilComercialBase", '') AS "IdPerfilComercial"
	, COALESCE(PERF."IdPerfilCobros", '') AS "IdPerfilCobros"
	, COALESCE(PERF."IdPerfilProductos", '') AS "IdPerfilProductos"
FROM "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" AS DOCI ON DOCI."IdPersona" = POR."IdPersona"
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = POR."IdPortafolio" AND IND."CodigoTipoIndicador" = 'COBROCUST'
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PERF ON UPPER(PERF."IdFormaCobro") = UPPER(IND."NombreValorIndicador") AND PERF."IdCategoriaInversionista" = CLI."IdCategoriaInversionista" AND PERF."CodigoPortafolio" = ( CASE WHEN POR."Proposito" = 'PROPOSITOAPV' THEN 'APV' ELSE POR."CodigoPortafolio" END )
WHERE DOCI."DocumentoIdentidad" IN (:DocumentoIdentidad);
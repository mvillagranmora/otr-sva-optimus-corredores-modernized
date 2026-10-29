SELECT 
	  POR."IdPortafolio"
	, POR."IdCliente"
	, DOCI."DocumentoIdentidad"
	, COALESCE(PERF."IdFormaCobro", '') AS "IdFormaCobro"
	, CLI."IdCategoriaInversionista"
	, COALESCE(PERF."IdPerfilComercialBase", '') AS "IdPerfilComercial"
	, COALESCE(PERF."IdPerfilCobros", '') AS "IdPerfilCobros"
	, COALESCE(PERF."IdPerfilProductos", '') AS "IdPerfilProductos"
	, POR."CodigoPortafolio"
	, POR."Proposito"
FROM "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" AS DOCI ON DOCI."IdPersona" = POR."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = POR."IdPortafolio" AND IND."CodigoTipoIndicador" = 'COBROCUST'
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PERF ON UPPER(PERF."IdFormaCobro") = UPPER(IND."NombreValorIndicador") AND PERF."IdCategoriaInversionista" = CLI."IdCategoriaInversionista" AND PERF."CodigoPortafolio" = CASE WHEN POR."Proposito"='PROPOSITOAPV' THEN 'APV' ELSE POR."CodigoPortafolio" END
WHERE DOCI."DocumentoIdentidad" = :DocumentoIdentidad
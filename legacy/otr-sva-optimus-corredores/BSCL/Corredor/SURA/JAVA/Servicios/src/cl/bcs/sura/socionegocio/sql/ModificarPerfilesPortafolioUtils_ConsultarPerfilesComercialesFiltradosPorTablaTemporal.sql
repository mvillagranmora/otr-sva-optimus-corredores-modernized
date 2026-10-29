SELECT
	  POR."IdPortafolio"
	, POR."IdCliente"
	, DOCI."DocumentoIdentidad"
	, COALESCE(IND."IdIndicador", 0) AS "IdIndicador"
	, COALESCE(PERF."IdFormaCobro", TEMP."IdFormaCobro", '') AS "IdFormaCobro"
	, CLI."IdCategoriaInversionista"
	, COALESCE(PERF."IdPerfilComercialBase", '') AS "IdPerfilComercial"
	, COALESCE(PERF."IdPerfilCobros", '') AS "IdPerfilCobros"
	, COALESCE(PERF."IdPerfilProductos", '') AS "IdPerfilProductos"
	, POR."Proposito"
FROM "ModificarPerfilesPortafolioUtils_ClientesFormasCobro" AS TEMP
INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" AS POR ON POR."IdPortafolio" = TEMP."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdCliente" = TEMP."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" AS DOCI ON DOCI."IdPersona" = POR."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND ON IND."IdPortafolio" = POR."IdPortafolio" AND IND."CodigoTipoIndicador" = 'COBROCUST'
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" AS PERF ON PERF."IdCategoriaInversionista" = CLI."IdCategoriaInversionista" AND UPPER(PERF."IdFormaCobro") = UPPER(TEMP."IdFormaCobro") AND PERF."CodigoPortafolio" = ( CASE WHEN POR."Proposito" = 'PROPOSITOAPV' THEN 'APV' ELSE POR."CodigoPortafolio" END );
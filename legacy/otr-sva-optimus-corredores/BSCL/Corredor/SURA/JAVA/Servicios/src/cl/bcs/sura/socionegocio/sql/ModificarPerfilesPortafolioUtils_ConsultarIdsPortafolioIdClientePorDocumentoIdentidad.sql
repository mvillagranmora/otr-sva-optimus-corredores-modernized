
SELECT DISTINCT 
	POR."IdPortafolio"
	, POR."IdCliente"
	, DOCI."DocumentoIdentidad"
	, INDPORT."IdIndicador"
	, POR."Proposito"
FROM "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" AS DOCI ON DOCI."IdPersona" = POR."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON TI."CodigoTipoIndicador" = 'COBROCUST'
INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS INDPORT ON INDPORT."IdPortafolio" = POR."IdPortafolio" AND INDPORT."IdTipoIndicador" = TI."IdTipoIndicador"
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilComercialAdicional" PERF ON PERF."CodigoPortafolio" = ( CASE WHEN POR."Proposito" = 'PROPOSITOAPV' THEN 'APV' ELSE POR."CodigoPortafolio" END )
WHERE DOCI."DocumentoIdentidad" IN (:DocumentoIdentidad);
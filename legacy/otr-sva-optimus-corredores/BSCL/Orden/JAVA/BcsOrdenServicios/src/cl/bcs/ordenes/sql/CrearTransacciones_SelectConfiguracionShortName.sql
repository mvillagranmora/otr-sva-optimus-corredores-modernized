WITH LibroOrdenesCliente AS (
SELECT CIP."IdPortafolio", LIB."IdLibroOrdenes"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS CIP
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CIP."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LIB ON CIP."ValorIndicador" = lib."IdLibroOrdenes"
WHERE CIP."CodigoTipoIndicador" = 'ORDLIBRO' AND POR."Borrado" = 'N' AND CLI."IdEstado" = 'ACT' AND CLI."Borrado" = 'N' AND PER."Borrado" = 'N' AND IDENT."Borrado" = 'N'
AND (CLI."EsEnrolado" <> 'P' AND CLI."EsEnrolado" <> 'N')
)

SELECT 
    CSN."ShortName", 
    CSN."AsignacionAuto",
    CSN."MovimientoFacturarAuto",
    CSN."ComprobanteAuto",
	CSN."IdTipoInstrumento",
	LOC."IdLibroOrdenes"
FROM "BSCL.Orden.BD::ORD.ConfiguracionShortName" AS CSN
LEFT JOIN "BSCL.Orden.BD::ORD.ShortNames" AS SN ON CSN."ShortName" = SN."ShortName"
LEFT JOIN LibroOrdenesCliente LOC ON SN."IdPortafolio" = LOC."IdPortafolio"
WHERE SN."Habilitado" = 'S';
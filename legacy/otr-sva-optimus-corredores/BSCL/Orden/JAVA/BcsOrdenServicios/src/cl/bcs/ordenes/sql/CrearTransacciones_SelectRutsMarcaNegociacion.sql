WITH LibroOrdenesCliente AS (
SELECT CIP."IdPortafolio", LIB."IdLibroOrdenes"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS CIP
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CIP."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LIB ON CIP."ValorIndicador" = LIB."IdLibroOrdenes"
WHERE CIP."CodigoTipoIndicador" = 'ORDLIBRO' AND POR."Borrado" = 'N' AND CLI."IdEstado" = 'ACT' AND CLI."Borrado" = 'N' AND PER."Borrado" = 'N' AND IDENT."Borrado" = 'N'
AND (CLI."EsEnrolado" <> 'P' AND CLI."EsEnrolado" <> 'N')
),
ClientePortafolio AS (
SELECT IDENT."DocumentoIdentidad", POR."CodigoPortafolio", POR."IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR 
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE POR."Borrado" = 'N' AND CLI."Borrado" = 'N' AND PER."Borrado" = 'N' AND IDENT."Borrado" = 'N'
),
Cliente AS (
SELECT IDENT."DocumentoIdentidad",
	CASE CLI."IdEstado" WHEN 'ACT' THEN 
		CASE CLI."EsEnrolado" WHEN 'P' THEN 'PEN'
			WHEN 'N' THEN 'PEN'
			ELSE 'ACT'
		END
	ELSE 'BLO'
END AS "IdEstado"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE CLI."Borrado" = 'N' AND PER."Borrado" = 'N' AND IDENT."Borrado" = 'N'
)

SELECT 
LOC."IdLibroOrdenes",
CCP."DocumentoIdentidad",
CCP."CodigoPortafolio",
NULL AS "NombreEstado"
FROM ClientePortafolio CCP
INNER JOIN LibroOrdenesCliente LOC ON LOC."IdPortafolio" = CCP."IdPortafolio"
WHERE CCP."DocumentoIdentidad" IN (:Rut)

UNION ALL

SELECT 
NULL AS "IdLibroOrdenes",
CLI."DocumentoIdentidad",
NULL AS "CodigoPortafolio",
EST."Nombre" AS "NombreEstado"
FROM Cliente CLI
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON CLI."IdEstado" = EST."IdEstado"
WHERE NOT EXISTS (
    SELECT 1
    FROM LibroOrdenesCliente LOC
    INNER JOIN ClientePortafolio CCP ON LOC."IdPortafolio" = CCP."IdPortafolio"
    WHERE CCP."DocumentoIdentidad" IN (:Rut)
)
AND CLI."DocumentoIdentidad" IN (:Rut);
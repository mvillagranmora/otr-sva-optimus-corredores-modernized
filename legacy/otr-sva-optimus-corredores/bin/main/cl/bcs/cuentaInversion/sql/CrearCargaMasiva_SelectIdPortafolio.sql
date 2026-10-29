SELECT
DOC."DocumentoIdentidad",
POR."IdPortafolio",
POR."CodigoPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON POR."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = CLI."IdPersona" AND DOC."Predeterminado" ='S'
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupador" AS CPA ON POR."IdPortafolio" = CPA."IdPortafolio"
WHERE POR."CodigoPortafolio" IN (:p_CodigoPortafolio)
AND DOC."DocumentoIdentidad" IN (:p_DocumentoIdentidad);
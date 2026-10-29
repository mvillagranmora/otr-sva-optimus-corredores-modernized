SELECT 
"IdPersona" AS "IdPersonaCtaPropia",
"IdCliente" AS "IdClienteCtaPropia",
"DocumentoIdentidad" AS "DocumentoIdentidadCtaPropia",
"IdPortafolio" AS "IdPortafolioCtaPropia",
"CodigoPortafolio" AS "CodigoPortafolioCtaPropia"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
WHERE "IdEmpresa" = :p_IdEmpresa
AND "IdPortafolio" = :p_IdPortafolio
AND "CodigoPortafolio" = :p_CodigoPortafolioDefault
GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad", "IdPortafolio", "CodigoPortafolio";

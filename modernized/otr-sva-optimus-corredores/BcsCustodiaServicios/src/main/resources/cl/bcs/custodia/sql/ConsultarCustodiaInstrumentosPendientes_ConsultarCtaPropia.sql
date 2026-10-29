SELECT "IdPersona" AS "IdPersonaCtaPropia", 
"IdCliente" AS "IdClienteCtaPropia", 
"DocumentoIdentidad"  AS "DocumentoIdentidadCtaPropia"
,"IdPortafolio" AS "IdPortafolioCtaPropia"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
WHERE "IdEmpresa" = :IdEmpresa
GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad","IdPortafolio";
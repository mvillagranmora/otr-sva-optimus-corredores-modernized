SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarFirmaFWD";

CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.ConsultarFirmaFWD"
AS

SELECT
ANE."IdEstado" AS "FirmaEstado",
DOC."IdCliente" AS "IdCliente",
TPO."IdEmpresa" AS "IdEmpresa",
TPO."IdTipoDocumento" AS "IdTipoDocumento"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX ON ANX."IdDocumento" = DOC."IdDocumento" AND ANX."IdAnexo" = DOC."IdUltimoAnexo"
LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" ANE ON  (ANX."IdAnexo" = ANE."IdAnexo")
WHERE IFNULL(ANX."IdEstado",'PEN') = 'ACT';

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" TO BCS_DESARROLLADOR WITH GRANT OPTION;
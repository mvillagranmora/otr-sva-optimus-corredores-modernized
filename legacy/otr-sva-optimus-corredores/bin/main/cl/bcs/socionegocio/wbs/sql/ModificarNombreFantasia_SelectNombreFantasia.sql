SELECT NOMBREFANTASIA."IdPersona"
,NOMBREFANTASIA."IdNombreFantasia"
,NOMBREFANTASIA."DescripcionNombreFantasia" AS "NombreFantasia"
,NOMBREFANTASIA."IdEstado"
,PER."Tipo" AS "TipoPersona"
,CLI."TipoEnrolamiento"
,CLI."IdCliente"
FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NOMBREFANTASIA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = NOMBREFANTASIA."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE NOMBREFANTASIA."IdNombreFantasia" IN (:IdNombreFantasia);
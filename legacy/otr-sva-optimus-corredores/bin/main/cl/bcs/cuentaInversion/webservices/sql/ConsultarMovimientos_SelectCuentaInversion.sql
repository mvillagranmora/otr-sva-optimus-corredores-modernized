SELECT 
CTA."IdCuentaInversion"
,CTA."IdCliente"
,PORT."IdPortafolio"
,IDENT."DocumentoIdentidad"
,PORT."CodigoPortafolio"
,PORT."IdEmpleadoEjecutivo"
,CTA."IdMoneda" 
FROM  "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON  CLI."IdCliente" = CTA."IdCliente"
INNER JOIN  "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT" ON CLI."IdCliente" = "PORT"."IdCliente" AND CTA."IdPortafolio" = PORT."IdPortafolio"
INNER JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS "PER" ON ( "PER"."IdPersona" = CLI."IdPersona" )
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON "PER"."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND "PER"."Borrado"='N' AND IDENT."Borrado"='N' 
WHERE CTA."IdPortafolio" IN (:IdPortafolio) AND (CTA."IdMoneda" = :IdMoneda OR :IdMoneda IS NULL)
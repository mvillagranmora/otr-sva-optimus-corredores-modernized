SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionClientePAUBloqueoClientes"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionClientePAUBloqueoClientes" AS
SELECT 
CLIENTE."IdCliente"
,MAX(LOG."FechaLog") AS "FechaUltimaModificacion"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
LEFT JOIN "BSCL.Plataforma.BD::PLA.Log" AS LOG ON LOG."IdCliente" = CLIENTE."IdCliente" OR LOG."IdPersona" = CLIENTE."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" AS DETALLE_LOG ON DETALLE_LOG."IdLog" = LOG."IdLog"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.CampoLog" AS CAMPO_LOG ON CAMPO_LOG."CodigoCampo" = DETALLE_LOG."CampoRegistro"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA ON PERSONA."Tipo" = CAMPO_LOG."Tipo"
WHERE CAMPO_LOG."AfectaFechaModificacion" = 'S'
OR ((LOG."IdTipoLog" = 'INGRESO' OR LOG."IdTipoLog" = 'MODIFICACION')
AND (LOG."IdModulo" IN ('ORD', 'FAC', 'TES', 'CTA'))
OR (LOG."IdTipoRegistro" IN ('TELEFONO', 'DIRECCIONELECTRONICA')))
GROUP BY CLIENTE."IdCliente";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarFechaUltimaModificacionClientePAUBloqueoClientes" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
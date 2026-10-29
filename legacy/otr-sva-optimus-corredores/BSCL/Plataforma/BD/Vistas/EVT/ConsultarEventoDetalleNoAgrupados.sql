SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalleNoAgrupados"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalleNoAgrupados"  
AS
SELECT "IdEventoDetalle", "IdEvento", "Llave", "Valor", "IdConfiguracionEvento", "CodigoTipoEvento", "Agrupa", "IdNotificacion","IdPersonaDestinatario", "UsuarioRecibe", "UsuarioEnvia" from (
select
"IdEventoDetalle",
evtdet."IdEvento",
"Llave",
"Valor",
cevtdet."IdConfiguracionEvento",
cevtdet."CodigoTipoEvento",
ce."Agrupa",
ce."IdEstado",
notdet."IdNotificacion",
"IdPersonaDestinatario",
USUREC."IdUsuario" AS "UsuarioRecibe",
usuenv."IdUsuario" as "UsuarioEnvia"
 from "BSCL.Plataforma.BD::EVT.EventoDetalle" evtdet
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento"  cevt
ON evtdet."IdEvento" = cevt."IdEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" cevtdet
ON cevtdet."CodigoTipoEvento" = cevt."CodigoTipoEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ce
ON cevtdet."IdConfiguracionEvento" = ce."IdConfiguracionEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" notdet
ON notdet."IdEvento" = cevt."IdEvento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" personaenvia
ON personaenvia."IdPersona" = "IdPersonaRemitente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" personarecibe
ON personarecibe."IdPersona" = "IdPersonaDestinatario"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" CSN
ON  CSN."IdPersona" = "IdPersonaRemitente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" csnrev
ON  csnrev."IdPersona" = "IdPersonaDestinatario"
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" usuenv
ON (CSN."IdEmpleado" = usuenv."IdEntidad" and usuenv."IdUsuario" =cevt."IdUsuario" and usuenv."TipoEntidad" = 'EMP')
    --OR (CSN."IdCliente" = usuenv."IdEntidad" and usuenv."TipoEntidad" = 'CLI') 
    --OR (CSN."IdProveedor" = usuenv."IdEntidad" and usuenv."TipoEntidad" = 'PRO')
    --OR (CSN."IdContraparte" = usuenv."IdEntidad" and usuenv."TipoEntidad" = 'CNT') 
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" USUREC
ON (csnrev."IdEmpleado" = USUREC."IdEntidad" and USUREC."TipoEntidad" = 'EMP')
    --OR (csnrev."IdCliente" = USUREC."IdEntidad" and USUREC."TipoEntidad" = 'CLI')
    --OR (csnrev."IdProveedor" = USUREC."IdEntidad" and USUREC."TipoEntidad" = 'PRO')
    --OR(csnrev."IdContraparte" = USUREC."IdEntidad" and USUREC."TipoEntidad" = 'CNT') 
WHERE cevtdet."IdEstado" = 'ACT' and ce."IdEstado" = 'ACT'
UNION ALL
SELECT 
"IdTagGenerales" as "IdEvento",
"IdTagGenerales" as "IdEventoDetalle",
"Llave",
"ValorDefault" AS "Valor",
0,
'TagGenerales',
'S',
'ACT',
0,
0,
0,
0
FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsGenerales") 
WHERE "Agrupa" = 'N' ORDER BY "IdConfiguracionEvento" ASC;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalleNoAgrupados" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
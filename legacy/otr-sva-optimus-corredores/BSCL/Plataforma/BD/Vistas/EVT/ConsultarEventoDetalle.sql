SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalle"  
AS
SELECT "IdEventoDetalle", "IdEvento", "Llave", "Valor", "IdConfiguracionEvento", "CodigoTipoEvento", "Agrupa", "IdNotificacion","IdPersonaDestinatario", "IdUsuario" from (
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
cmpr."IdUsuario"
 from "BSCL.Plataforma.BD::EVT.EventoDetalle" evtdet
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento"  cevt
ON evtdet."IdEvento" = cevt."IdEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" cevtdet
ON cevtdet."CodigoTipoEvento" = cevt."CodigoTipoEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" ce
ON cevtdet."IdConfiguracionEvento" = ce."IdConfiguracionEvento"
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" notdet
ON notdet."IdEvento" = cevt."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMailPorRol" cmpr
ON  cmpr."IdPersona" = cevt."IdPersonaDestinatario"
WHERE cevtdet."IdEstado" = 'ACT' and ce."IdEstado" = 'ACT'
UNION ALL
SELECT 
"IdTagGenerales" as "IdEvento",
"IdTagGenerales" as "IdEventoDetalle",
"Llave",
"ValorDefault" AS "Valor",
0,
'TagGenerales',
'N',
'ACT',
0,
0,
0
FROM "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsGenerales") 
 ORDER BY "IdConfiguracionEvento" ASC;
 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

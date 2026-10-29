--DESTINATARIOS PRINCIPALES
select DISTINCT 
	CASE WHEN CE."Agrupa" = 'N' THEN ND."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio",
	ED."TipoDestinatario" as "TipoDestinatario", 
	CPDE."Direccion" as "Destinatario"
FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ND."IdEvento" = ED."IdEvento"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  ED."IdDireccionElectronica" 
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
WHERE ((N."FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP and (N."Enviado" = 'N' and N."TiempoDespacho" != 'M' ) ) or N."Enviar" = 'S' ) AND (N."FormaDespacho" != 'U') AND N."IdNotificacion" IN (:Notificaciones)-- AND N."ExcluyeDestinatario" = 'N'
UNION ALL
--DESTINATARIOS REENVIO
SELECT DISTINCT 
    CASE WHEN CE."Agrupa" = 'N' THEN ND."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio",
    'TO' AS "TipoDestinatario", 
    CASE WHEN COALESCE(N."DireccionAlternativa",'')='' THEN CPDE."Direccion" ELSE N."DireccionAlternativa" END AS "Destinatario"
FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" ED ON ND."IdEvento" = ED."IdEvento"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CPDE."IdDireccionElectronica" =  ED."IdDireccionElectronica" 
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento"
WHERE N."ReEnviar" = 'S' AND N."ReEnviado" = 'N' AND N."IdNotificacion" IN (:Notificaciones)
UNION ALL
--DESTINATARIOS COPIA CONFIGURADA NOTIFICACION
SELECT DISTINCT 
	CASE WHEN CE."Agrupa" = 'N' THEN ND."IdEvento" ELSE N."IdNotificacion" END AS "IdEnvio", 
	'CC' as "TipoDestinatario", 
	CC."Correo" as "Destinatario"
 FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
 INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion" 
 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" cc ON cc."IdConfiguracionEvento" = N."IdConfiguracionEvento" 
 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" CE ON CE."IdConfiguracionEvento" = N."IdConfiguracionEvento" 
 WHERE ((N."FechaDespachoOrginal" < CURRENT_UTCTIMESTAMP and (N."Enviado" = 'N' and N."TiempoDespacho" != 'M' ) ) or N."Enviar" = 'S' ) AND (N."FormaDespacho" != 'U') and (CC."Correo" IS NOT NULL AND CE."CopiasAdicionales" = 'S') AND N."IdNotificacion" IN (:Notificaciones)
 
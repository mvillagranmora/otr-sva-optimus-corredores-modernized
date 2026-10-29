UPDATE  N
SET 
	N."Enviado" = 'S',
	N."FechaEnviado" = CURRENT_UTCTIMESTAMP,
	N."IdUsuarioEnviado" = 0, 
	N."Enviar" = 'N',
	N."ReEnviar" = CASE WHEN N."ReEnviar" = 'S' THEN 'N' ELSE N."ReEnviar" END,
	N."ReEnviado" = CASE WHEN N."ReEnviar" = 'S' THEN 'S' ELSE N."ReEnviado" END
FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
WHERE N."IdNotificacion" = :IdEnvio;
UPDATE "BSCL.Plataforma.BD::EVT.Notificacion" 
SET "Enviar" = :Enviar, "Enviado" = CASE WHEN :Enviar ='S' THEN 'N' ELSE 'S' END
WHERE "IdNotificacion" = :IdNotificacion;
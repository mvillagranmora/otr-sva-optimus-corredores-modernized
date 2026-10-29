SELECT TOP 100 m."IdEmail", m."FechaCreacion", m."Remitente", m."Asunto", m."MensajeCuerpo", m."EstadoEnviado", r."Remitente" AS "RemitenteEvento",
COALESCE("Host",'') AS "Host",COALESCE("Usuario",'') AS "Usuario",COALESCE("Password",'') AS "Password",COALESCE("Puerto",'') AS "Puerto"
FROM "BSCL.Plataforma.BD::EVT.Mail" m 
LEFT JOIN "BSCL.Plataforma.BD::EVT.Evento" e on e."IdEvento" = m."IdEnvio" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.Remitente" r ON r."CodigoTipoEvento" = e."CodigoTipoEvento" 
WHERE "EstadoEnviado" = 'N';
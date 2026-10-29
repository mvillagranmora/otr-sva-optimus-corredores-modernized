-- INSERTAR SERVICIOS PARA NOTIFICACIONES
SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4300000, 'BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarNotificacionesPorCliente', 0, 'Q', '', '', '', 'S' 
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 4300000);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4300001, 'BSCL.SocioNegocio.BD.Servicios::CLI.ActualizarEstadoNotificacionCliente', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 4300001);
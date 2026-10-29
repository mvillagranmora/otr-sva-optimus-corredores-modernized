SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizacion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizacion" AS
SELECT 
Evento."IdEvento",
EtapaEvento."IdEtapaEvento",
AutoEtapaEvento."IdAutorizadoEtapaEvento",
Evento."Fecha",
TO_NVARCHAR(Evento."FechaHoraRegistro", 'HH24:MI') as "HoraRegistro",
TO_NVARCHAR(Evento."FechaHoraVigencia", 'HH24:MI') as "HoraVigencia",
Evento."IdEstado",
TipoEvento."Nombre" as "Autorizacion",
'Nombre' as "Nombre", -- Preguntar donde obtener Nombre
Evento."Nemo",
Evento."Cantidad",
Evento."Precio",
Evento."Monto",
Evento."IdMoneda",
'Tipo' as "Tipo", --Preguntar donde obtener Tipo
AutoEtapaEvento."IdUsuario",
Evento."Glosa",
Evento."IdEmpresa"
FROM "BSCL.Plataforma.BD::AUT.Evento" AS Evento
LEFT JOIN "BSCL.Plataforma.BD::AUT.EtapaEvento" AS EtapaEvento ON Evento."IdEvento" = EtapaEvento."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" AS AutoEtapaEvento ON AutoEtapaEvento."IdEtapaEvento" = EtapaEvento."IdEtapaEvento"
LEFT JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" AS TipoEvento ON Evento."IdTipoEvento" = TipoEvento."IdTipoEvento";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
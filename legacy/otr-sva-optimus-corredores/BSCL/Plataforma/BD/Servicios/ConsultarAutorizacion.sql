SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Servicios::ConsultarAutorizacion";

CREATE VIEW "BSCL.Plataforma.BD.Servicios::ConsultarAutorizacion" (

"IdEvento",
"IdEtapaEvento",
"IdAutorizadoEtapaEvento",
"Fecha",
"HoraRegistro",
"HoraVigencia",
"IdEstado",
"Autorizacion",
"Nombre", -- Preguntar donde obtener Nombre
"Nemo",
"Cantidad",
"Precio",
"Monto",
"IdMoneda",
"Tipo", --Preguntar donde obtener Tipo
"IdUsuario",
"Glosa"


) AS SELECT 

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
Evento."Glosa"

FROM "BSCL.Plataforma.BD::AUT.Evento" AS Evento
LEFT JOIN "BSCL.Plataforma.BD::AUT.EtapaEvento" AS EtapaEvento ON Evento."IdEvento" = EtapaEvento."IdEvento"
LEFT JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" AS AutoEtapaEvento ON AutoEtapaEvento."IdEtapaEvento" = EtapaEvento."IdEtapaEvento"
LEFT JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" AS TipoEvento ON Evento."IdTipoEvento" = TipoEvento."IdTipoEvento" AND TipoEvento."IdEmpresa" = Evento."IdEmpresa";

GRANT SELECT ON "BSCL.Plataforma.BD.Servicios::ConsultarAutorizacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;



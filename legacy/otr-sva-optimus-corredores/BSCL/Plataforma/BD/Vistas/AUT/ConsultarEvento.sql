SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento"  AS

SELECT
Evento."IdEmpresa", 
Evento."IdEvento",
Evento."Fecha",
Evento."FechaHoraRegistro" AS "HoraRegistro",
Evento."FechaHoraVigencia" AS "HoraVigencia",
Evento."IdEstado",
Estado."Nombre" AS "NombreEstado",
TipoEvento."IdTipoEvento" AS "IdTipoEvento",
TipoEvento."Nombre" AS "Autorizacion", --Nombre autorizacion
CASE 
	WHEN TEmpleado."IdEmpleado" IS NOT NULL THEN NomEmpleado."NombreCompleto" 
	ELSE NomCliente."NombreCompleto" 
END AS "Cliente",
Evento."Nemo",
Evento."Cantidad",
Evento."Precio",
Evento."Monto",
Evento."IdMoneda",
'Tipo' AS "Tipo", --Preguntar donde obtener Tipo
Evento."IdUsuario",
Usuario."CodigoUsuario" AS "Usuario",
Evento."IdUsuarioSuperAutorizador",
UsuarioSuperAutorizador."CodigoUsuario" AS "UsuarioSuperAutorizador",
Evento."IdRolSuperAutorizador",
RolSuperAutorizador."Nombre" AS  "RolSuperAutorizador",
Evento."IdUsuarioCambioEstado",
UsuarioCambioEstado."CodigoUsuario" AS "CodigoUsuarioCambioEstado", 
Evento."Glosa",
Evento."Comentario",
Evento."IdGrupo",
EtapaEvento."IdEtapaEvento",
EtapaEvento."Nombre" AS "NombreEtapa",
EtapaEvento."Orden" AS "OrdenEtapa",
EtapaEvento."IdEstado" AS "IdEstadoEtapa",
EstadoEtapa."Nombre" AS "NombreEstadoEtapa",
CASE WHEN EtapaEvento."TipoAutorizacion" = 'T' THEN 'TODOS' 
	ELSE CAST(EtapaEvento."MinimoAutorizan" AS NVARCHAR) 
END AS "MinimoAutorizan",
Evento."IdCliente",
Evento."NumeroOperacionConcepto",
MovOps."IdOperacionUI" AS "NumeroOperacionConceptoUI"
FROM "BSCL.Plataforma.BD::AUT.Evento" AS Evento
INNER JOIN "BSCL.Plataforma.BD::AUT.TipoEvento" AS TipoEvento ON Evento."IdTipoEvento" = TipoEvento."IdTipoEvento"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Estado ON Evento."IdEstado" = Estado."IdEstado"
INNER JOIN "BSCL.Plataforma.BD::AUT.EtapaEvento" AS EtapaEvento ON Evento."IdEvento" = EtapaEvento."IdEvento"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EstadoEtapa ON EtapaEvento."IdEstado" = EstadoEtapa."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" AS AutoEtapaEvento ON AutoEtapaEvento."IdEtapaEvento" = EtapaEvento."IdEtapaEvento" AND AutoEtapaEvento."IdEstado" = 'AUT'
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario ON Evento."IdUsuario" = Usuario."IdUsuario" 
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS UsuarioSuperAutorizador ON Evento."IdUsuarioSuperAutorizador" = UsuarioSuperAutorizador."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS UsuarioCambioEstado ON Evento."IdUsuarioCambioEstado" = UsuarioCambioEstado."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Rol" AS RolSuperAutorizador ON Evento."IdRolSuperAutorizador" = RolSuperAutorizador."IdRol"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS TCliente ON Evento."IdCliente" = TCliente."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS NomCliente ON TCliente."IdPersona" = NomCliente."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS TEmpleado ON Evento."IdFuncionario" = TEmpleado."IdEmpleado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS NomEmpleado ON TEmpleado."IdPersona" = NomEmpleado."IdPersona" 
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMovimientosOperaciones" AS MovOps ON CAST (MovOps."IdOperacion" AS varchar(15)) = Evento."NumeroOperacionConcepto"
GROUP BY Evento."IdEmpresa",TEmpleado."IdEmpleado",NomEmpleado."NombreCompleto",Evento."IdEvento", Evento."Fecha",Evento."FechaHoraRegistro",Evento."FechaHoraVigencia",Evento."IdEstado",Estado."Nombre" ,
TipoEvento."IdTipoEvento", TipoEvento."Nombre", Evento."Nemo",Evento."Cantidad",Evento."Precio",Evento."Monto",Evento."IdMoneda", Usuario."CodigoUsuario", UsuarioSuperAutorizador."CodigoUsuario",
Evento."IdUsuario",Evento."IdUsuarioSuperAutorizador", Evento."IdRolSuperAutorizador", RolSuperAutorizador."Nombre",Evento."Glosa",Evento."Comentario", Evento."IdGrupo",EtapaEvento."IdEtapaEvento", 
EtapaEvento."Nombre", EtapaEvento."Orden", EtapaEvento."IdEstado", EstadoEtapa."Nombre", Evento."IdCliente", Evento."NumeroOperacionConcepto", NomCliente."NombreCompleto", MovOps."IdOperacionUI", 
EtapaEvento."TipoAutorizacion", EtapaEvento."MinimoAutorizan", Evento."IdUsuarioCambioEstado", UsuarioCambioEstado."CodigoUsuario"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
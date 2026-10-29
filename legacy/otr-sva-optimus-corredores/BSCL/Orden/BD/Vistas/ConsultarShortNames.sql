SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas.ConsultarShortNames"; 

CREATE VIEW "BSCL.Orden.BD.Vistas.ConsultarShortNames" AS
SELECT
    SN."ShortName",
    SN."Habilitado",
    SN."IdSocioDeNegocio",
    SN."DocumentoIdentidad",
    SN."FechaHoraCreacion",
    USC."CodigoUsuario" as "UsuarioCreador",
    USM."CodigoUsuario" as  "UsuarioModificador",
    SN."FechaHoraModificacion",
    SN."IdPortafolio",
    CLI."NombreCompleto" as "NombreCliente",
    POR."CodigoPortafolio"
FROM "BSCL.Orden.BD::ORD.ShortNames" SN
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."DocumentoIdentidad" = SN."DocumentoIdentidad"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON POR."CodigoPortafolio" = SN."CodigoPortafolio" AND  CLI."IdCliente" = POR."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USM ON  USM."IdUsuario" = TO_BIGINT(SN."UsuarioModificador")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" USC ON USC."IdUsuario" = TO_BIGINT(SN."UsuarioCreador") ;

GRANT SELECT ON "BSCL.Orden.BD.Vistas.ConsultarShortNames" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
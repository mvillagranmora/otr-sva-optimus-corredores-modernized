SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa"
AS SELECT
	 AutorizadoEtapa."IdAutorizadoEtapa",
	 AutorizadoEtapa."IdEtapa",
	 Usuario."CodigoUsuario",
	 Usuario."IdUsuario",
	 AutorizadoEtapa."IdRol",
	 Rol."Nombre" as "NombreRol",
	 AutorizadoEtapa."IdUsuarioReemplaza",
	 UsuarioReemplaza."CodigoUsuario" as "CodigoUsuarioReemplaza",
	 AutorizadoEtapa."ReemplazaDesde",
	 AutorizadoEtapa."ReemplazaHasta",
	 AutorizadoEtapa."FechaIngreso",
	 AutorizadoEtapa."IdEstado",
	 EstadoAutorizacion."Descripcion" AS "Estado",
	 AutorizadoEtapa."EsAutorizacionPorRol"
FROM "BSCL.Plataforma.BD::AUT.AutorizadoEtapa" AS AutorizadoEtapa
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS Usuario 
	ON AutorizadoEtapa."IdUsuario" = Usuario."IdUsuario"
LEFT JOIN  "BSCL.Plataforma.BD::ACC.Usuario" AS UsuarioReemplaza 
	ON AutorizadoEtapa."IdUsuarioReemplaza" = UsuarioReemplaza."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRol" Rol 
	ON Rol."IdRol" = AutorizadoEtapa."IdRol"
LEFT JOIN "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" EstadoAutorizacion 
	ON AutorizadoEtapa."IdEstado" = EstadoAutorizacion."IdEstado" 
	AND EstadoAutorizacion."NombreEntidad" = 'AUT.AutorizadoEtapa'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizadorEtapa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
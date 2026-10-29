SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombresFantasia";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombresFantasia" 
AS 


select 
NF."IdNombreFantasia",
NF."DescripcionNombreFantasia",
NF."IdPersona",
NF."FechaCreacion",
NF."FechaModificacion",
NF."FechaIngreso",
NF."IdEstado",
NF."IdEstadoAutorizacion",
NF."Borrado",
NF."IdUsuarioCreador",
"EST"."Nombre" as "Estado"
from "BSCL.SocioNegocio.BD::PER.NombreFantasia" NF
LEFT JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" as "ESTSOC" on "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('NOMBREFANTASIA',NF."IdEstado",NF."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "ESTSOC"."NombreEntidad" = 'NOMBREFANTASIA'
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as "EST" on "ESTSOC"."IdEstado" = "EST"."IdEstado";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombresFantasia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
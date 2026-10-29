SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaDetalle";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaDetalle" AS 



select 
G."IdGrupoDireccionDetalle",
G."IdDireccionElectronica",
D."Direccion",
G."IdTipoDestinatario",
T."Nombre" as "NombreTipoDestinatario",
G."IdGrupoDireccion",
G."FechaCreacion",
G."FechaModificacion",
G."FechaIngreso",
G."IdEstado",
G."IdEstadoAutorizacion",
G."Borrado",
G."IdUsuarioCreador"
 from "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" G
 JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" D ON D."IdDireccionElectronica" = G."IdDireccionElectronica"
 JOIN "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" T ON T."IdTipoDestinatario" = G."IdTipoDestinatario"
 WHERE G."Borrado"='N';
 
 
 GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 

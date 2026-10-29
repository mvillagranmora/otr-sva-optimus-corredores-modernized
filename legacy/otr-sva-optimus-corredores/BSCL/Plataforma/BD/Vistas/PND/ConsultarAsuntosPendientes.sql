SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntosPendientes"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntosPendientes"  
AS
Select 
AP."Codigo",
AP."IdEmpresa",
AP."Asunto",
AP."CodigoNotificacion",
AP."CumplimientoAutomatico",
AP."IdEstado",
DAP."IdDetalleAsuntoPendiente",
DAP."FechaCreacion",
DAP."FechaVisibleDesde",
DAP."FechaVisibleHasta",
DAP."IdEmpleado",
DAP."Observaciones",
DAP."TipoEntidad",
CTSN."NombreTipoSocioDeNegocio",
DAP."IdEntidad",
CASE WHEN DAP."TipoEntidad" = 'CLI' THEN CC."DocumentoIdentidad" ELSE CASE WHEN DAP."TipoEntidad" = 'EMP' THEN CE."DocumentoIdentidad" END END AS "DocumentoIdentidad",
CASE WHEN DAP."TipoEntidad" = 'CLI' THEN CC."NombreCompleto" ELSE CASE WHEN DAP."TipoEntidad" = 'EMP' THEN CE."NombreCompleto" END END AS "NombreCompleto",
DAP."TipoOperacion",
DAP."IdOperacion",
DAP."Glosa",
DAP."Monto",
DAP."CompletadoAutomatico",
DAP."IdModulo",
DAP."IdEstado" AS "IdEstadoDetalle",
CEAP."Descripcion" AS "Estado"
FROM "BSCL.Plataforma.BD::PND.AsuntoPendiente" AS AP
INNER JOIN "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente" DAP on DAP."Codigo" = AP."Codigo"
INNER JOIN "BSCL.Plataforma.BD.Vistas::PND.ConsultarEstadosAsuntosPendientes" CEAP on DAP."IdEstado" = CEAP."IdEstado"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarTipoSocioNegocio" CTSN ON CTSN."IdTipoSocioDeNegocio" = DAP."TipoEntidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = DAP."IdEntidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = DAP."IdEntidad";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntosPendientes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

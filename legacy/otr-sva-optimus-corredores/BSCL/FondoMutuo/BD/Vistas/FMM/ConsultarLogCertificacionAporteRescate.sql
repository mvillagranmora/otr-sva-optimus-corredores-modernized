SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogCertificacionAporteRescate";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogCertificacionAporteRescate"
AS
SELECT 
 "LogCertificacionAporteRescate"."IdLogCertificacionAporteRescate"
, "LogCertificacionAporteRescate"."IdSolicitudAporteRescate"
, "LogCertificacionAporteRescate"."Codigo"
, "LogCertificacionAporteRescate"."IdEstado"
, "Estado"."Nombre" AS "NombreEstado"
, "LogCertificacionAporteRescate"."TipoSolicitud"
, "LogCertificacionAporteRescate"."IdTipoMovimientoFondoMutuo"
, "LogCertificacionAporteRescate"."IdUsuario"
, "LogCertificacionAporteRescate"."IdEmpresa"
, "LogCertificacionAporteRescate"."FechaIngresoRegistro"
, "LogCertificacionAporteRescate"."FechaSistema"
, "LogCertificacionAporteRescate"."IdSerieFondo"
, "LogCertificacionAporteRescate"."Monto"
, "LogCertificacionAporteRescate"."IdMoneda"
, "LogCertificacionAporteRescate"."IdCliente"
, "LogCertificacionAporteRescate"."IdPortafolio"
, "LogCertificacionAporteRescate"."Simulacion"
, CASE WHEN "Simulacion" = 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "EsSimulacionUI"
, "ConsultarCliente"."RazonSocial"
, "ConsultarCliente"."NombreCompleto"
, "ConsultarCliente"."DocumentoIdentidad"
, "ConsultarPortafolio"."CodigoPortafolio"
, "ConsultarPortafolio"."DescripcionPortafolio"
, "TipoMovimientoFondoMutuo"."Descripcion" AS "TipoMovimiento"
, "ConsultarUsuario"."CodigoUsuario"
, "LogCertificacionAporteRescate"."MontoOriginal"
, "LogCertificacionAporteRescate"."TipoCambio"
, "LogCertificacionAporteRescate"."IdMonedaOriginal"
, "LogCertificacionAporteRescate"."ValorCuota"
, "LogCertificacionAporteRescate"."NumeroCuotas"
, "LogCertificacionAporteRescate"."IdEmisor"
, "ConsultarEmisor"."NombreCompleto" AS "RazonSocialEmisor"
, "SerieFondo"."DecimalesValorCuota"
, "SerieFondo"."DecimalesCuota"
FROM "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate" AS "LogCertificacionAporteRescate"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("Estado"."IdEstado" = "LogCertificacionAporteRescate"."IdEstado")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON ("LogCertificacionAporteRescate"."IdCliente" = "ConsultarCliente"."IdCliente")
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON ("LogCertificacionAporteRescate"."IdPortafolio" = "ConsultarPortafolio"."IdPortafolio")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS "TipoMovimientoFondoMutuo" ON ("TipoMovimientoFondoMutuo"."IdTipoMovimientoFondoMutuo" = "LogCertificacionAporteRescate"."IdTipoMovimientoFondoMutuo")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" AS "ConsultarUsuario" ON ("ConsultarUsuario"."IdUsuario" = "LogCertificacionAporteRescate"."IdUsuario")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS "ConsultarEmisor" ON  ( "ConsultarEmisor"."IdEmisor" = "LogCertificacionAporteRescate"."IdEmisor" )
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("LogCertificacionAporteRescate"."IdSerieFondo" = "SerieFondo"."IdSerieFondo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogCertificacionAporteRescate" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

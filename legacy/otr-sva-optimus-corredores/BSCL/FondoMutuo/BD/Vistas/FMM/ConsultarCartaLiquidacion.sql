SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCartaLiquidacion";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCartaLiquidacion"
AS
SELECT 
CASE WHEN "TipoSolicitud" = 'APO' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END AS "MontoAporte"
, CASE WHEN "TipoSolicitud" = 'RES' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END AS "MontoRescate"
, "SolicitudAporteRescate"."IdSerieFondo"
, "Fondo"."IdMoneda" AS "IdMonedaFondo"
, "SolicitudAporteRescate"."FolioCartaLiquidacion"
, "Cliente"."DocumentoIdentidad"
, "Portafolio"."CodigoPortafolio"
, "ClienteSolicitud"."DocumentoIdentidad" AS "DocumentoIdentidadClienteSolicitud"
, "Fondo"."IdEmisor"
, "SolicitudAporteRescate"."FechaLiquidacion"
, "SolicitudAporteRescate"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SolicitudAporteRescate"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "SolicitudAporteRescate"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "SerieFondo"."IdEmpresa" = "Fondo"."IdEmpresa")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."IdCliente" = "SerieFondo"."IdCliente")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "Portafolio" ON ("Portafolio"."IdPortafolio" = "SerieFondo"."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ClienteSolicitud" ON ("ClienteSolicitud"."IdCliente" = "SolicitudAporteRescate"."IdCliente")
WHERE "EstadoLiquidacion" = 'LIQ' AND "SerieFondo"."Omnibus" = 'N'
UNION ALL
SELECT 
SUM(CASE WHEN "TipoSolicitud" = 'APO' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END) AS "MontoAporte"
,SUM(CASE WHEN "TipoSolicitud" = 'RES' THEN "SolicitudAporteRescate"."Monto" ELSE 0 END) AS "MontoRescate"
, "SolicitudAporteRescate"."IdSerieFondo"
, "Fondo"."IdMoneda" AS "IdMonedaFondo"
, "SolicitudAporteRescate"."FolioCartaLiquidacion"
, "Cliente"."DocumentoIdentidad"
, "Portafolio"."CodigoPortafolio"
, '' AS "DocumentoIdentidadClienteSolicitud"
, "Fondo"."IdEmisor"
, "SolicitudAporteRescate"."FechaLiquidacion"
, "SolicitudAporteRescate"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo" ON ("SolicitudAporteRescate"."IdSerieFondo" = "SerieFondo"."IdSerieFondo" AND "SerieFondo"."IdEmpresa" = "SolicitudAporteRescate"."IdEmpresa")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo" AND "SerieFondo"."IdEmpresa" = "Fondo"."IdEmpresa")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "Cliente" ON ("Cliente"."IdCliente" = "SerieFondo"."IdCliente")
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "Portafolio" ON ("Portafolio"."IdPortafolio" = "SerieFondo"."IdPortafolio")
WHERE "SolicitudAporteRescate"."EstadoLiquidacion" = 'LIQ' AND "SerieFondo"."Omnibus" = 'S'
GROUP BY "SolicitudAporteRescate"."IdSerieFondo","Fondo"."IdMoneda","SolicitudAporteRescate"."FolioCartaLiquidacion","Cliente"."DocumentoIdentidad","Portafolio"."CodigoPortafolio","Fondo"."IdEmisor","SolicitudAporteRescate"."FechaLiquidacion","SolicitudAporteRescate"."IdEmpresa"
ORDER BY "FolioCartaLiquidacion";
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCartaLiquidacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

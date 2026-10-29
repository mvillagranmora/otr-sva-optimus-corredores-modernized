SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario"
AS
SELECT
"SolicitudAporteRescate"."IdSolicitudAporteRescate"
,"SolicitudAporteRescate"."IdTipoMovimientoFondoMutuo"
,"SolicitudAporteRescate"."IdEmpresa"
,"SolicitudAporteRescate"."FechaCertificacion"
,"SolicitudAporteRescate"."CostoUF"
,"SolicitudAporteRescate"."ValorUFFechaInversion"
,"SolicitudAporteRescate"."NumeroCuotas"
,"SolicitudAporteRescate"."Monto"
,"SolicitudAporteRescate"."IdMoneda"
,"SolicitudAporteRescate"."ValorCuota" 
,"SolicitudAporteRescate"."ParidadFechaInversion"
,"SolicitudAporteRescate"."TipoSolicitud"
,"SolicitudAporteRescate"."TipoCarteraNormativa"
,"SolicitudAporteRescate"."IdSerieFondo"
,"ConsultarCliente"."DocumentoIdentidad"
,"ConsultarCliente"."NombreCompleto"
,"ConsultarCliente"."TipoDocumentoIdentidad"
,"ConsultarCliente"."IdPaisResidencia"
,"Fondo"."CodigoSVS"
,"Fondo"."IdFondo"
,"Fondo"."IdEmisor"
,"Fondo"."IdMoneda" AS "IdMonedaFondo"
,"Fondo"."IdTipoInstrumento"
,"SolicitudAporteRescate"."IdCliente"
,"SolicitudAporteRescate"."IdPortafolio"
,"SolicitudAporteRescate"."IdEmisorDestino"
,"SolicitudAporteRescate"."Spread"
,"SolicitudAporteRescate"."FechaAceptacion"
,"SolicitudAporteRescate"."Estado" AS "EstadoSolicitud"
--,"Emisor"."RutEmisor"
,"Persona"."DocumentoIdentidad" AS "RutEmisor"
,"SolicitudAporteRescate"."IdSerieFondoDestino"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "SolicitudAporteRescate"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "SolicitudAporteRescate"."IdSerieFondo" )
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo" )
LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio" ON  ( "ConsultarPortafolio"."IdPortafolio" = "SolicitudAporteRescate"."IdPortafolio" )
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.Emisor" AS "Emisor" ON ("Emisor"."IdEmisor" = "Fondo"."IdEmisor")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "Persona" ON ("Persona"."IdPersona" = "Emisor"."IdPersona");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateTributario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

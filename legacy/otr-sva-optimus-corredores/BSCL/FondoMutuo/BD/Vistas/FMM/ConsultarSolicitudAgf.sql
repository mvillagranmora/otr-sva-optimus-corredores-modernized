SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAgf";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAgf"
AS
SELECT 
 "InterfazAgf"."IdInterfazAgf"
 ,"InterfazAgf"."DocumentoIdentidad"
 ,"InterfazAgf"."CodigoPortafolio"
 ,"InterfazAgf"."TipoRegistro"
 ,"InterfazAgf"."NombreCompleto"
 ,"InterfazAgf"."FechaLiquidacion"
 ,"InterfazAgf"."FechaAceptacion"
 ,"InterfazAgf"."FechaIngreso"
 ,"InterfazAgf"."FechaIngresoRegistro"
 ,"InterfazAgf"."TipoSolicitud"
 ,"InterfazAgf"."IdEstadoSolicitud"
 ,"InterfazAgf"."IdSerieFondo"
 ,"InterfazAgf"."IdUsuario"
 ,"InterfazAgf"."NumeroCuotas"
 ,"InterfazAgf"."ValorCuota"
 ,"InterfazAgf"."Monto"
 ,"InterfazAgf"."CalculoPorMontoCuota" 
 ,"InterfazAgf"."IdMoneda"
 ,"InterfazAgf"."PropositoSerie"
 ,"InterfazAgf"."IdSerieFondoDestino"
 ,"InterfazAgf"."IdEstadoSerieFondo"
 ,"InterfazAgf"."IndicadorMLT"
 ,"InterfazAgf"."IdEstado"
 ,"Estado"."Nombre" AS "NombreEstado"
 ,"InterfazAgf"."IdSolicitudAporteRescate"
 ,"InterfazAgf"."IdSolicitudAporteRescateRelacionado"
 ,"InterfazAgf"."Observacion"
 ,"InterfazAgf"."IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.InterfazAgf" AS "InterfazAgf"
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("InterfazAgf"."IdEstado" = "Estado"."IdEstado");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAgf" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
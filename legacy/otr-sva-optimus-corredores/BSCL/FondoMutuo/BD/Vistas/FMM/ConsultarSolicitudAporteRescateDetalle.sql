SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateDetalle";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateDetalle"
AS
SELECT  
	"Rescate"."IdSolicitudAporteRescate" as "IdSolicitudRescate",
	"Aporte"."IdSolicitudAporteRescate" as "IdSolicitudAporte",
	"SerieDeFondo"."IdSerieFondo",
	"SerieDeFondo"."NombreSerieFondo",
	"Reinversion"."Porcentaje",
	"Rescate"."IdEmpresa",
	"Rescate"."Monto" as "MontoRescate",
	"Aporte"."Monto" as "MontoAporte",
	"Aporte"."MontoMonedaSistema" as "MontoMonedaSistemaAporte",
	"Aporte"."NumeroCuotas" AS "NumeroCuotasAporte",
	"Aporte"."FechaCertificacion" AS "FechaCertificacionAporte",
	"Aporte"."FechaLiquidacion" AS "FechaLiquidacionAporte",
	"Aporte"."FechaValorCuota" AS "FechaValorCuotaAporte",
	"Aporte"."Estado" AS "EstadoAporte",
	"Aporte"."ValorCuota" AS "ValorCuotaAporte",
	"Reinversion"."IdRegimenTributario" AS "IdRegimenFondoMutuo",
	"Reinversion"."Switch",
	CASE "Reinversion"."Switch" WHEN 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "SwitchUI",
	"Fondo"."IdEmisor"
FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS "Reinversion"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" ON ("SerieDeFondo"."IdSerieFondo" = "Reinversion"."IdSerieFondoDestino")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Rescate"
	ON ("Reinversion"."IdSolicitudAporteRescate" = "Rescate"."IdSolicitudAporteRescate")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS "Aporte"
	ON ("Reinversion"."IdSolicitudAporteRescate" = "Aporte"."IdSolicitudAporteRescateRelacionado" and
	"Aporte"."IdSerieFondo" = "Reinversion"."IdSerieFondoDestino")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo" ON ("Fondo"."IdFondo" = "SerieDeFondo"."IdFondo");
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
SET SCHEMA "BSCL";

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionSerieFondoDestinoAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionSerieFondoDestinoAnalitica" AS
SELECT  
                    Rescate."IdSolicitudAporteRescate" as "IdSolicitudRescate",
                    Aporte."IdSolicitudAporteRescate" as "IdSolicitudAporte",
                    Rescate."IdTipoMovimientoFondoMutuo" AS "IdTipoMovimientoRescate",
                    Aporte."IdTipoMovimientoFondoMutuo" AS "IdTipoMovimientoAporte",
                    Reinversion."IdSerieFondoDestino",
                    SerieDeFondo."NombreSerieFondo",
                    Reinversion."Porcentaje",
                    Rescate."Monto" as "MontoRescate",
                    Aporte."Monto" as "MontoAporte",
                    Reinversion."IdRegimenTributario",
                    Reinversion."IdReinversionSerieFondo"
                FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS Reinversion
                LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON (SerieDeFondo."IdSerieFondo" = Reinversion."IdSerieFondoDestino")
                LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS Rescate
                    ON (Reinversion."IdSolicitudAporteRescate" = Rescate."IdSolicitudAporteRescate")
                LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS Aporte
                    ON (Reinversion."IdSolicitudAporteRescate" = Aporte."IdSolicitudAporteRescateRelacionado" and
                    Aporte."IdSerieFondo" = Reinversion."IdSerieFondoDestino")
                   where Rescate."IdSolicitudAporteRescate" is not null;

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionSerieFondoDestinoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
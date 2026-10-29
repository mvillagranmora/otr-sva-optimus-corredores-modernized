SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondoAnalitica";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondoAnalitica" AS
SELECT 
		"SerieDeFondo"."IdSerieFondo"
		, "SerieDeFondo"."IdInstrumento"
		, "SerieDeFondo"."IdFondo"
		, "SerieDeFondo"."NombreSerieFondo"
		, "SerieDeFondo"."CodigoSerie"
		, "SerieDeFondo"."Alias"
		, "SerieDeFondo"."DecimalesValorCuota"
		, "SerieDeFondo"."DecimalesCuota"
		, "SerieDeFondo"."MontoMinimo"
		, "SerieDeFondo"."MontoMaximo"
		, "SerieDeFondo"."DiasRetencionesRescate"
		, "SerieDeFondo"."ExcluyeGarantia"
		, "SerieDeFondo"."CobraComision"
		, "SerieDeFondo"."Diferido"
		, "SerieDeFondo"."InstrumentoDCV"
		, "SerieDeFondo"."Omnibus"
		, "SerieDeFondo"."PatrimonioMinimo"
		, "SerieDeFondo"."IdSerieFondoOrigen"
		, COALESCE("Estado"."Nombre",'') AS "Estado"
		, "SerieDeFondo"."IdEmpresa"
		, "SerieDeFondo"."IdCliente"
		, "SerieDeFondo"."IdPortafolio"
		, "SerieDeFondo"."DiasValutaCertificacionAporte"
		, "SerieDeFondo"."DiasValutaCertificacionRescate"
		, "SerieDeFondo"."DiasValutaCurseAporte"
		, "SerieDeFondo"."DiasValutaCurseRescate"
		, "SerieDeFondo"."DiasRetencionesAporte"
		, "SerieDeFondo"."DiasValutaValorCuotaAporte"
		, "SerieDeFondo"."DiasValutaValorCuotaRescate"
		, "SerieDeFondo"."CambioFondo"
		, "SerieDeFondo"."ISIN"
		, COALESCE(TO_NVARCHAR("ValorCuota"."FechaCierre",'YYYY-MM-DD'),'0001-01-01') AS "FechaValorCuota"
		, CAST(COALESCE("ValorCuota"."ValorCuota",0) AS DECIMAL(26,8)) AS "ValorCuota"
		, COALESCE("FamiliaFondo"."NombreFamilia",'') AS "NombreFamilia"
		, COALESCE("MedioPagoAbono"."Nombre",'') AS "MedioPagoAbono"
		, COALESCE("MedioPagoCargo"."Nombre",'') AS "MedioPagoCargo"
		FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "ValorCuota" ON ("SerieDeFondo"."IdSerieFondo" = "ValorCuota"."IdSerieFondo" AND "SerieDeFondo"."IdEmpresa" = "ValorCuota"."IdEmpresa")
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.FamiliaFondo" AS "FamiliaFondo" ON ("FamiliaFondo"."IdFamiliaFondo" = "SerieDeFondo"."IdFamiliaFondo")
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" AS "Estado" ON ("Estado"."IdEstado" = "SerieDeFondo"."IdEstado")
		LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioPagoAbono" ON ("MedioPagoAbono"."IdMedioDePago" = "SerieDeFondo"."IdMedioDePagoAbono")
		LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS "MedioPagoCargo" ON ("MedioPagoCargo"."IdMedioDePago" = "SerieDeFondo"."IdMedioDePagoCargo");

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSerieDeFondoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

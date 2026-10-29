SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico" AS
SELECT
	"ValorCuotaHistorico"."IdValorCuotaHistorico"
	,"ValorCuotaHistorico"."IdSerieFondo"
	,"ValorCuotaHistorico"."FechaVigencia"
	,"ValorCuotaHistorico"."ValorCuota"
	,"ValorCuotaHistorico"."IdMoneda"
	,"ValorCuotaHistorico"."Observacion"
	,"ValorCuotaHistorico"."FechaCalculo"
	,"ValorCuotaHistorico"."FechaIngresoRegistro"
	,"ValorCuotaHistorico"."IdEmpresa"
	,"SerieFondo"."DecimalesValorCuota"
	,"SerieFondo"."CodigoSerie"
	,"SerieFondo"."IdFamiliaFondo"
	,"SerieFondo"."IdInstrumento"
	,"SerieFondo"."NombreSerieFondo"
	,"Fondo"."Nemotecnico"
	,"Fondo"."IdEmisor"
	,"Fondo"."NombreFondo" AS "RazonSocialFondo"
	,"FamiliaFondo"."NombreFamilia"
	,"Persona"."RazonSocial" AS "NombreEmisor"
	FROM (SELECT
		row_number() over (partition by ValorCuota."IdSerieFondo",ValorCuota."FechaCierre" order by ValorCuota."IdSerieFondo", ValorCuota."FechaCierre" desc,ValorCuota."IdValorCuotaHistorico" desc) AS "RowValorCuota"
		,ValorCuota."IdValorCuotaHistorico"
		,ValorCuota."IdSerieFondo"
		,ValorCuota."FechaCierre" as "FechaVigencia"
		,ValorCuota."ValorCuota"
		,ValorCuota."IdMoneda"
		,ValorCuota."Observacion"
		,ValorCuota."FechaCarga" as "FechaCalculo"
		,ValorCuota."FechaIngresoRegistro"
		,ValorCuota."IdEmpresa"
		FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" AS ValorCuota
	) AS "ValorCuotaHistorico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo"
	ON ("SerieFondo"."IdSerieFondo" = "ValorCuotaHistorico"."IdSerieFondo")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
	ON ("Fondo"."IdFondo" = "SerieFondo"."IdFondo")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.FamiliaFondo" AS "FamiliaFondo"
	ON ("FamiliaFondo"."IdFamiliaFondo" = "SerieFondo"."IdFamiliaFondo")
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS "Emisor"
	ON ("Emisor"."IdEmisor" = "Fondo"."IdEmisor")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "Persona"
	ON ("Persona"."IdPersona" = "Emisor"."IdPersona")
WHERE "SerieFondo"."IdEmpresa" = "ValorCuotaHistorico"."IdEmpresa"
	AND "ValorCuotaHistorico"."RowValorCuota" = 1
ORDER BY "ValorCuotaHistorico"."IdSerieFondo", "ValorCuotaHistorico"."FechaVigencia" DESC;


GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

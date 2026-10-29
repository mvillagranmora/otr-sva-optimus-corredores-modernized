SELECT
	"SerieFondo"."IdSerieFondo",
	COALESCE("ValorTipoCambio"."ValorDolarAporte",0) AS "ValorDolarAporte",
	COALESCE("ValorTipoCambio"."ValorDolarRescate",0) AS "ValorDolarRescate"
FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" AS "ValorTipoCambio" ON ("ValorTipoCambio"."IdSerieFondo" = "SerieFondo"."IdSerieFondo"
                                                                             AND "ValorTipoCambio"."IdEmpresa" = "SerieFondo"."IdEmpresa")
WHERE "SerieFondo"."IdEmpresa" = :IdEmpresa AND "SerieFondo"."IdSerieFondo" IN (:IdSerieFondo);
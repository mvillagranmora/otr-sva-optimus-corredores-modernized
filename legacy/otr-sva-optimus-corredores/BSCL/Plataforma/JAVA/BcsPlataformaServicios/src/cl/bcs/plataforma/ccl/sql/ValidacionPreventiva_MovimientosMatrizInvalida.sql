SELECT count(1) as "Valor5" FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateAnalitica"
WHERE "IdMatrizAgrupador" NOT IN
(
	SELECT "IdMatrizAgrupadorAPV" FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"
	UNION
	SELECT "IdMatrizAgrupadorNOAPV" FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV"
)
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarCalculoPolizaAPV"; 
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarCalculoPolizaAPV" AS 
	SELECT CirAPVA."Periodo" AS "PeriodoAPVA",
			CirAPVB."Periodo" AS "PeriodoAPVB",
			CirDCR."Periodo" AS "PeriodoDCR",
			AVG(CAST(CirAPVA."Monto" AS DECIMAL)) AS "MontoPromedioAPVA",
			AVG(CAST(CirAPVB."Monto" AS DECIMAL)) AS "MontoPromedioAPVB",
			AVG(CAST(CirDCR."Monto" AS DECIMAL)) AS "MontoPromedioDCR",
			CAST(CONFPorcentaje."Valor" AS DECIMAL)*100||'%' AS "PorcentajePromedio",
			CAST(CONFFactor."Valor" AS DECIMAL)*100||'%' AS "Factor",
			((AVG(CAST(CirAPVA."Monto" AS DECIMAL)) * CAST(CONFPorcentaje."Valor" AS DECIMAL)) * CAST(CONFFactor."Valor" AS DECIMAL)) AS "CalculoPolizaAPVA",
			((AVG(CAST(CirAPVB."Monto" AS DECIMAL)) * CAST(CONFPorcentaje."Valor" AS DECIMAL)) * CAST(CONFFactor."Valor" AS DECIMAL)) AS "CalculoPolizaAPVB",
			((AVG(CAST(CirDCR."Monto" AS DECIMAL)) * CAST(CONFPorcentaje."Valor" AS DECIMAL)) * CAST(CONFFactor."Valor" AS DECIMAL)) AS "CalculoPolizaDCR"
	   FROM "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981" CirAPVA
				LEFT JOIN "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981" CirAPVB
						ON CirAPVA."Periodo" = CirAPVB."Periodo"
				LEFT JOIN "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981" CirDCR
						ON CirAPVA."Periodo" = CirDCR."Periodo"
	 			LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" CONFPorcentaje
	 					ON CONFPorcentaje."IdEmpresa" = 1 AND CONFPorcentaje."CodigoParametro" = 'PolizaAPVPorcentajePromedio'
	 			LEFT JOIN "BSCL.Normativa.BD::NOR.Configuracion" CONFFactor
	 					ON CONFFactor."IdEmpresa" = 1 AND CONFFactor."CodigoParametro" = 'PolizaAPVFactor'
	 			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio" = CirAPVA."IdPortafolio"
	  WHERE CirAPVA."Codigo" = 'APV-A'
		AND CirAPVB."Codigo" = 'APV-B'
		AND CirDCR."Codigo" = 'DC-R'
		AND por."IdEstado" <> 'INA'
	GROUP BY CirAPVA."Periodo", CirAPVB."Periodo", CirDCR."Periodo", CONFPorcentaje."Valor", CONFFactor."Valor";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarCalculoPolizaAPV" TO BCS_DESARROLLADOR WITH GRANT OPTION;

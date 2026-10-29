((
SELECT
	CASE
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Reserva"."Nemotecnico" = 'PAGARE R'
			AND "Reserva"."IdEmisor" = 'CENTRAL' THEN 'PRBC'
			WHEN "Reserva"."Nemotecnico" = 'PAGARE R'
			AND "Reserva"."IdEmisor" != 'CENTRAL' THEN 'DP REAJUSTABLE'
			WHEN "Reserva"."Nemotecnico" = 'PAGARE NR' THEN 'DP NOMINAL'
			ELSE "Reserva"."Nemotecnico"
		END
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Reserva"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Reserva"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Reserva"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Reserva"."Nemotecnico"
		END
	END AS "InstrumentoEmisor",
	CAST ((
	CASE
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Reserva"."FechaVencimiento" THEN
			(DAYS_BETWEEN (:FechaInforme, "Reserva"."FechaVencimiento"))
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'RF' AND "Reserva"."FechaCreacion" < "Reserva"."FechaVencimiento" THEN
			(YEARS_BETWEEN ("Reserva"."FechaCreacion", "Reserva"."FechaVencimiento"))
		ELSE
			0
	END)  AS decimal (10, 2)) AS "Rango",
	0.0 AS "Volatilidad",
	CASE
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'RF' THEN MAX(COALESCE("Cartera"."INGDurationMercadoActual",0))
		ELSE 0.0
	END AS "Duracion",
	CASE
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'IF' THEN ROUND(SUM("Reserva"."IngValorRescate" / :ValorUF), 0)
		ELSE ROUND(SUM("Reserva"."IngMontoValorizado" / :ValorUF), 0)
	END AS "ValorPresente",
	0.0 AS "Var"
FROM
	"BSCL.Custodia.BD::CUS.Reserva" AS "Reserva"
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS "Operacion" ON
	"Operacion"."IdOperacion" = "Reserva"."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS "Cartera" ON
	"Cartera"."IdCartera" = "Reserva"."IdCarteraOrigen"
WHERE
	"Reserva"."IdEstado" IN ('VIG','LIQ')
	AND "Reserva"."IdClasificacionTipoInstrumento" IN ('IF', 'RF')
	AND "Operacion"."IdEstado" IN ('VIG','PEN')
	AND "Operacion"."IdTipoOperacion" IN ('VENTER','VENPAC')
	AND "Operacion"."IdCliente" = :IdCliente
GROUP BY
	"Reserva"."IdEmisor",
	"Reserva"."IdTipoInstrumento",
	"Reserva"."Nemotecnico",
	"Reserva"."IdClasificacionTipoInstrumento",
	"Reserva"."FechaCreacion",
	"Reserva"."FechaVencimiento",
	"Operacion"."PlazoEnDias"
ORDER BY
	"Rango")
UNION (
SELECT
	CASE
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Cartera"."Nemotecnico" = 'PAGARE R'
			AND "Cartera"."IdEmisor" = 'CENTRAL' THEN 'PRBC'
			WHEN "Cartera"."Nemotecnico" = 'PAGARE R'
			AND "Cartera"."IdEmisor" != 'CENTRAL' THEN 'DP REAJUSTABLE'
			WHEN "Cartera"."Nemotecnico" = 'PAGARE NR' THEN 'DP NOMINAL'
			ELSE "Cartera"."Nemotecnico"
		END
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Cartera"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Cartera"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Cartera"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Cartera"."Nemotecnico"
		END
	END AS "InstrumentoEmisor",
	CAST ((
	CASE
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Cartera"."FechaVencimientoInstrumento" THEN
			(DAYS_BETWEEN (:FechaInforme, "Cartera"."FechaVencimientoInstrumento"))
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'RF' AND "Cartera"."FechaOperacion" < "Cartera"."FechaVencimientoInstrumento" THEN
			(YEARS_BETWEEN ("Cartera"."FechaOperacion", "Cartera"."FechaVencimientoInstrumento"))
		ELSE
			0
	END)  AS decimal (10, 2)) AS "Rango",
	0.0 AS "Volatilidad",
	CASE
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'RF' THEN MAX("Cartera"."INGDurationMercadoActual")
		ELSE 0.0
	END AS "Duracion",
	ROUND(SUM("Cartera"."INGMontoATasaMercadoActual" / :ValorUF), 0) AS "ValorPresente",
	0.0 AS "Var"
FROM
	"BSCL.Custodia.BD::CUS.Cartera" AS "Cartera"

WHERE
	"Cartera"."IdEstado" = 'VIG'
	AND "Cartera"."IdClasificacionTipoInstrumento" IN ('IF', 'RF')
	AND "Cartera"."IdTipoInstrumento" NOT IN ('PAC')
	AND "Cartera"."IdCliente" = :IdCliente
GROUP BY
	"Cartera"."IdEmisor",
	"Cartera"."IdTipoInstrumento",
	"Cartera"."Nemotecnico",
	"Cartera"."IdClasificacionTipoInstrumento",
	"Cartera"."FechaEmisionInstrumento",
	"Cartera"."FechaOperacion",
	"Cartera"."FechaVencimientoInstrumento",
	"Cartera"."PlazoEnDiasOperacion"
ORDER BY
	"Rango")
UNION (
SELECT
	CASE
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Garantia"."Nemotecnico" = 'PAGARE R'
			AND "Garantia"."IdEmisor" = 'CENTRAL' THEN 'PRBC'
			WHEN "Garantia"."Nemotecnico" = 'PAGARE R'
			AND "Garantia"."IdEmisor" != 'CENTRAL' THEN 'DP REAJUSTABLE'
			WHEN "Garantia"."Nemotecnico" = 'PAGARE NR' THEN 'DP NOMINAL'
			ELSE "Garantia"."Nemotecnico"
		END
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Garantia"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Garantia"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Garantia"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Garantia"."Nemotecnico"
		END
	END AS "InstrumentoEmisor",
	CAST ((
	CASE
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Garantia"."FechaVencimientoInstrumento" THEN
			(DAYS_BETWEEN (:FechaInforme, "Garantia"."FechaVencimientoInstrumento"))
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'RF' AND "Garantia"."FechaOperacion" < "Garantia"."FechaVencimientoInstrumento" THEN
			(YEARS_BETWEEN ("Garantia"."FechaOperacion", "Garantia"."FechaVencimientoInstrumento"))
		ELSE
			0
	END)  AS decimal (10, 2)) AS "Rango",
	0.0 AS "Volatilidad",
	CASE
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'RF' THEN MAX("Garantia"."INGDurationMercadoActual")
		ELSE 0.0
	END AS "Duracion",
	ROUND(SUM("Garantia"."INGMontoATasaMercadoActual" / :ValorUF), 0) AS "ValorPresente",
	0.0 AS "Var"
FROM
	"BSCL.Custodia.BD::GTA.Garantia" AS "Garantia"

WHERE
	"Garantia"."IdEstado" = 'VIG'
	AND "Garantia"."IdClasificacionTipoInstrumento" IN ('IF', 'RF')
	AND "Garantia"."IdCliente" = :IdCliente
GROUP BY
	"Garantia"."IdEmisor",
	"Garantia"."IdTipoInstrumento",
	"Garantia"."Nemotecnico",
	"Garantia"."IdClasificacionTipoInstrumento",
	"Garantia"."FechaOperacion",
	"Garantia"."FechaVencimientoOperacion",
	"Garantia"."FechaVencimientoInstrumento",
	"Garantia"."PlazoEnDiasOperacion"
ORDER BY
	"Rango")
UNION
(
SELECT
	CASE
		WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Asignacion"."Instrumento" = 'PAGARE R'
			AND "Asignacion"."IdEmisor" = 'CENTRAL' THEN 'PRBC'
			WHEN "Asignacion"."Instrumento" = 'PAGARE R'
			AND "Asignacion"."IdEmisor" != 'CENTRAL' THEN 'DP REAJUSTABLE'
			WHEN "Asignacion"."Instrumento" = 'PAGARE NR' THEN 'DP NOMINAL'
			ELSE "Asignacion"."Instrumento"
		END
		WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Asignacion"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Asignacion"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Asignacion"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Asignacion"."Instrumento"
		END
	END AS "InstrumentoEmisor",
	CAST ((
	CASE
		WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Asignacion"."FechaVencimiento" THEN
			(DAYS_BETWEEN (:FechaInforme, "Asignacion"."FechaVencimiento"))
		WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'RF' AND "Asignacion"."FechaAsignacion" < "Asignacion"."FechaVencimiento" THEN
			(YEARS_BETWEEN ("Asignacion"."FechaAsignacion", "Asignacion"."FechaVencimiento"))
		ELSE
			0
	END)  AS decimal (10, 2)) AS "Rango",
	0.0 AS "Volatilidad",
	0.0 AS "Duracion",
	ROUND(SUM("Asignacion"."MontoAsignadoLocal" / :ValorUF), 0) AS "ValorPresente",
	0.0 AS "Var"
FROM
	"BSCL.Orden.BD.Vistas::ConsultarAsignacion" AS "Asignacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" ON
	"Instrumento"."IdInstrumento" = "Asignacion"."IdInstrumento"
WHERE
	"Asignacion"."IdEstadoAsignacion" = 'NEF'
	AND "Asignacion"."IdClasificacionTipoInstrumento" IN ('IF', 'RF')
	AND "Asignacion"."IdTipoInstrumento" NOT IN ('PAC')
	AND "Asignacion"."AccionTipoOperacion" = 'COMPRA'
	AND "Asignacion"."IdCliente" = :IdCliente
GROUP BY
	"Asignacion"."IdEmisor",
	"Asignacion"."IdTipoInstrumento",
	"Asignacion"."Instrumento",
	"Asignacion"."IdClasificacionTipoInstrumento",
	"Asignacion"."FechaAsignacion",
	"Asignacion"."FechaVencimiento",
	"Asignacion"."Plazo"
ORDER BY "Rango"))
ORDER BY "Rango"
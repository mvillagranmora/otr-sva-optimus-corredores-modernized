
(
SELECT
	CASE WHEN "Operacion"."IdTipoOperacion" = 'VENPAC' THEN 'PA'--pactos 
	ELSE 'DI'--termino
	END AS "Estado"
	,"Reserva"."IdEmisor" AS "Emisor"
	,CASE
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Reserva"."IdEmisor" = 'CENTRAL' THEN 'CE'
			ELSE 'DP'
		END
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Reserva"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Reserva"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Reserva"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Reserva"."Nemotecnico"
		END
	END AS "Familia"
	,"Reserva"."Nemotecnico" AS "Nemo"
	,"Reserva"."IngCantidad" AS "Cantidad"
	,CASE
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'IF' THEN 
			"Reserva"."IngValorRescate" 
		ELSE 
			"Reserva"."IngMontoValorizado"
	 END AS "Nominal"
	 ,"Reserva"."Tasa" AS "TirTasa"
	 ,CASE
	 	WHEN "Reserva"."IdTipoReajuste" = 'CLP' THEN 
	 		'NR'
	 	ELSE
	 		"Reserva"."IdTipoReajuste"
	 END AS "TipoReajuste"
	 ,TO_VARCHAR("Reserva"."FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimiento"
	 --,"Reserva"."IngMontoValorizado" AS "ValorPresenteCLP"
	 ,ROUND("Cartera"."INGMontoCompraActualMonedaInstrumento" , 0) AS "ValorPresente"
	 ,CAST ((
		CASE
			WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Reserva"."FechaVencimiento" THEN
				(DAYS_BETWEEN (:FechaInforme, "Reserva"."FechaVencimiento"))
			WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'RF' AND "Reserva"."FechaCreacion" < "Reserva"."FechaVencimiento" THEN
				(YEARS_BETWEEN ("Reserva"."FechaCreacion", "Reserva"."FechaVencimiento"))
			ELSE
				0
		END)  AS decimal (10, 2)) AS "Plazo"
	,0.0 AS "Volatilidad"
	,CASE
		WHEN "Reserva"."IdClasificacionTipoInstrumento" = 'RF' THEN COALESCE("Cartera"."INGDurationMercadoActual",0)
		ELSE 0.0
	 END AS "Duracion"
	,0.0 AS "Var"
	,"Reserva"."FolioCarteraOrigen" AS "SecCar"
 
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
	AND "Operacion"."IdClienteCorredor" = :IdCliente
	AND TO_DATE("Reserva"."FechaVencimiento") >= TO_DATE(:FechaInforme)
)
UNION (SELECT
	'DI' AS "Estado"
	,"Cartera"."IdEmisor" AS "Emisor"
	--,"Cartera"."IdTipoInstrumento" 
	,CASE
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Cartera"."IdEmisor" = 'CENTRAL' THEN 'CE'
			ELSE 'DP'
		END
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Cartera"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Cartera"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Cartera"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Cartera"."Nemotecnico"
		END
	END AS "Familia"
	,"Cartera"."Nemotecnico" AS "Nemo"
	,"Cartera"."INGCantidad" AS "Cantidad"
	,CASE
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'IF' THEN 
			"Cartera"."INGValorRescate" 
		ELSE 
			"Cartera"."INGMontoATasaCompraActual"
	 END AS "Nominal"
	 ,"Cartera"."INGTasaCompra" AS "TirTasa"
	 ,CASE
	 	WHEN "Cartera"."IdTipoReajuste" = 'CLP' THEN 
	 		'NR'
	 	ELSE
	 		"Cartera"."IdTipoReajuste"
	 END AS "TipoReajuste"
	 ,TO_VARCHAR("Cartera"."FechaVencimientoInstrumento",'DD-MM-YYYY') AS "FechaVencimiento"
	 --,"Cartera"."INGMontoATasaMercadoActual" AS "ValorPresente CLP"
	 ,ROUND("Cartera"."INGMontoCompraActualMonedaInstrumento", 0) AS "ValorPresente"
	 ,CAST ((
		CASE
			WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Cartera"."FechaVencimientoInstrumento" THEN
				(DAYS_BETWEEN (:FechaInforme, "Cartera"."FechaVencimientoInstrumento"))
			WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'RF' AND "Cartera"."FechaOperacion" < "Cartera"."FechaVencimientoInstrumento" THEN
				(YEARS_BETWEEN ("Cartera"."FechaOperacion", "Cartera"."FechaVencimientoInstrumento"))
			ELSE
				0
		END)  AS decimal (10, 2)) AS "Plazo"
	,0.0 AS "Volatilidad"
	,CASE
		WHEN "Cartera"."IdClasificacionTipoInstrumento" = 'RF' THEN "Cartera"."INGDurationMercadoActual"
		ELSE 0.0
	 END AS "Duracion"
	,0.0 AS "Var"
	,"Cartera"."FolioCartera" AS "SecCar"
	--,*
FROM
	"BSCL.Custodia.BD::CUS.Cartera" AS "Cartera"

WHERE
	"Cartera"."IdEstado" = 'VIG'
	AND "Cartera"."IdClasificacionTipoInstrumento" IN ('IF', 'RF')
	AND "Cartera"."IdTipoInstrumento" NOT IN ('PAC')
	AND "Cartera"."IdCliente" = :IdCliente
	AND TO_DATE("Cartera"."FechaVencimientoInstrumento") >= TO_DATE(:FechaInforme)
)
UNION(
SELECT
	'DI' AS "Estado"
	,"Garantia"."IdEmisor" AS "Emisor"
	--,"Garantia"."IdTipoInstrumento" 
	,CASE
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Garantia"."IdEmisor" = 'CENTRAL' THEN 'CE'
			ELSE 'DP'
		END
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Garantia"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Garantia"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Garantia"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Garantia"."Nemotecnico"
		END
	END AS "Familia"
	,"Garantia"."Nemotecnico" AS "Nemo"
	,"Garantia"."INGCantidad" AS "Cantidad"
	,CASE
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'IF' THEN 
			"Garantia"."INGValorRescate" 
		ELSE 
			"Garantia"."INGMontoATasaCompraActual"
	 END AS "Nominal"
	 ,"Garantia"."TasaGarantia" AS "TirTasa"
	 ,CASE
	 	WHEN "Garantia"."IdTipoReajuste" = 'CLP' THEN 
	 		'NR'
	 	ELSE
	 		"Garantia"."IdTipoReajuste"
	 END AS "TipoReajuste"
	 ,TO_VARCHAR("Garantia"."FechaVencimientoInstrumento",'DD-MM-YYYY') AS "FechaVencimiento"
	 --,"Garantia"."INGMontoATasaMercadoActual" AS "ValorPresenteCLP"
	 ,ROUND("Garantia"."INGMontoGarantiaActualMonedaInstrumento", 0) AS "ValorPresente"
	 ,CAST ((
		CASE
			WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Garantia"."FechaVencimientoInstrumento" THEN
				(DAYS_BETWEEN (:FechaInforme, "Garantia"."FechaVencimientoInstrumento"))
			WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'RF' AND "Garantia"."FechaOperacion" < "Garantia"."FechaVencimientoInstrumento" THEN
				(YEARS_BETWEEN ("Garantia"."FechaOperacion", "Garantia"."FechaVencimientoInstrumento"))
			ELSE
				0
		END)  AS decimal (10, 2)) AS "Plazo"
	,0.0 AS "Volatilidad"
	,CASE
		WHEN "Garantia"."IdClasificacionTipoInstrumento" = 'RF' THEN "Garantia"."INGDurationMercadoActual"
		ELSE 0.0
	 END AS "Duracion"
	,0.0 AS "Var"
	,"Garantia"."FolioCarteraOrigen" AS "SecCar"
	 
FROM
	"BSCL.Custodia.BD::GTA.Garantia" AS "Garantia"

WHERE
	"Garantia"."IdEstado" = 'VIG'
	AND "Garantia"."IdClasificacionTipoInstrumento" IN ('IF', 'RF')
	AND "Garantia"."IdCliente" = :IdCliente
	AND TO_DATE("Garantia"."FechaVencimientoInstrumento") >= TO_DATE(:FechaInforme)
)

UNION (
SELECT
	'DI' AS "Estado"
	,"Asignacion"."IdEmisor" AS "Emisor"
	--,"Asignacion"."IdTipoInstrumento"
	,CASE
		WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'IF' THEN
		CASE
			WHEN "Asignacion"."IdEmisor" = 'CENTRAL' THEN 'CE'
			ELSE 'DP'
		END
		WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'RF' THEN
		CASE
			WHEN "Asignacion"."IdTipoInstrumento" = 'LTH' THEN 'LH'
			WHEN "Asignacion"."IdTipoInstrumento" IN ('BBC', 'BEM') THEN 'BE'
			WHEN "Asignacion"."IdEmisor" IN ('BCP', 'BCU', 'BTP', 'CERO', 'BTU', 'BR') THEN 'CE'
			ELSE "Asignacion"."Instrumento"
		END
	END AS "Familia"
	,"Asignacion"."Instrumento" AS "Nemo"
	,"Asignacion"."CantidadAsignada" AS "Cantidad"
	,CASE
		WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'IF' THEN 
			"Asignacion"."ValorRescateAsignado" 
		ELSE 
			"Asignacion"."MontoAsignadoLocal"
	 END AS "Nominal"
	 ,"Asignacion"."Tasa" AS "TirTasa"
	 ,CASE
	 	WHEN "Asignacion"."IdTipoReajuste" = 'CLP' THEN 
	 		'NR'
	 	ELSE
	 		"Asignacion"."IdTipoReajuste"
	 END AS "TipoReajuste"
	 ,TO_VARCHAR("Asignacion"."FechaVencimiento",'DD-MM-YYYY') AS "FechaVencimiento"
	 --,"Asignacion"."MontoAsignadoLocal" AS "ValorPresenteCLP"
	 ,ROUND("Asignacion"."MontoAsignadoLocal" / :ValorUF, 0) AS "ValorPresente"
	 ,CAST ((
		CASE
			WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'IF' AND :FechaInforme < "Asignacion"."FechaVencimiento" THEN
				(DAYS_BETWEEN (:FechaInforme, "Asignacion"."FechaVencimiento"))
			WHEN "Asignacion"."IdClasificacionTipoInstrumento" = 'RF' AND "Asignacion"."FechaAsignacion" < "Asignacion"."FechaVencimiento" THEN
				(YEARS_BETWEEN ("Asignacion"."FechaAsignacion", "Asignacion"."FechaVencimiento"))
			ELSE
				0
		END)  AS decimal (10, 2)) AS "Plazo"
	,0.0 AS "Volatilidad"
	,0.0 AS "Duracion"
	,0.0 AS "Var"
	,"Asignacion"."FolioCartera" AS "SecCar"
 
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
	AND TO_DATE("Asignacion"."FechaVencimiento") >= TO_DATE(:FechaInforme)
)



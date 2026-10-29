/******************************************************************************
-- SQL para el Reporte de Gestion de Cliente - Flujo Mensual Aportes y Rescates
-- Invocado desde ReporteGestionClienteFlujoMensualAportesRescates.java
-- Servicio No. 700.021
*******************************************************************************/

WITH v_ene AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_01"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 1
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),
	 v_feb AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_02"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 2
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),	
	 v_mar AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_03"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 3
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),
	 v_abr AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_04"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 4
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente" 
			),	
	 v_may AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_05"
				FROM 	(

						
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 5
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
							
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),
	 v_jun AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_06"
				FROM 	(
				
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 6
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),	
	 v_jul AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_07"
				FROM 	(

				
						SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
						FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
						WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
						AND		MONTH(c."Fecha") = 7
						AND 	YEAR(c."Fecha") = :Agno
						GROUP BY c."IdCliente"
						
						)r

					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),
	 v_ago AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_08"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 8
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),	
	 v_sep AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_09"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 9
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente" 
			),
	 v_oct AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_10"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 10
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),
	 v_nov AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_11"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 11
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
					
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente"
			),	
	 v_dic AS (
				SELECT 	t."IdCartolaHistorica"
						,t."Fecha"
						,t."IdCliente"
						,t."AbonoIngresoMTD" - t."CargoRetiroMTD" AS "Monto_12"
				FROM 	(
							SELECT 	c."IdCliente", MAX(c."Fecha") AS "MaxDate"
							FROM 	"BSCL.Normativa.BD::CAR.CartolaHistorica" AS c
							WHERE 	(c."IdAgente" = :IdAgente OR :IdAgente = 0)
							AND		MONTH(c."Fecha") = 12
							AND 	YEAR(c."Fecha") = :Agno
							GROUP BY c."IdCliente"
						
						)r
					INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistorica" t ON t."Fecha" = r."MaxDate" AND t."IdCliente" = r."IdCliente" 
			)				
SELECT  
		 CART."NombreAgente"
		,CART."NombreCliente"		
		,MAX(COALESCE(ene."Monto_01", 0)) AS "Monto_01"
		,MAX(COALESCE(feb."Monto_02", 0)) AS "Monto_02"
		,MAX(COALESCE(mar."Monto_03", 0)) AS "Monto_03"
		,MAX(COALESCE(abr."Monto_04", 0)) AS "Monto_04"
		,MAX(COALESCE(may."Monto_05", 0)) AS "Monto_05"
		,MAX(COALESCE(jun."Monto_06", 0)) AS "Monto_06"
		,MAX(COALESCE(jul."Monto_07", 0)) AS "Monto_07"
		,MAX(COALESCE(ago."Monto_08", 0)) AS "Monto_08"
		,MAX(COALESCE(sep."Monto_09", 0)) AS "Monto_09"
		,MAX(COALESCE(oct."Monto_10", 0)) AS "Monto_10"
		,MAX(COALESCE(nov."Monto_11", 0)) AS "Monto_11"
		,MAX(COALESCE(dic."Monto_12", 0)) AS "Monto_12"	
		
		,(  MAX(COALESCE(ene."Monto_01", 0)) +
			MAX(COALESCE(feb."Monto_02", 0)) +
			MAX(COALESCE(mar."Monto_03", 0)) +
			MAX(COALESCE(abr."Monto_04", 0)) +
			MAX(COALESCE(may."Monto_05", 0)) +
			MAX(COALESCE(jun."Monto_06", 0)) +
			MAX(COALESCE(jul."Monto_07", 0)) +
			MAX(COALESCE(ago."Monto_08", 0)) +
			MAX(COALESCE(sep."Monto_09", 0)) +
			MAX(COALESCE(oct."Monto_10", 0)) +
			MAX(COALESCE(nov."Monto_11", 0)) +
			MAX(COALESCE(dic."Monto_12", 0)) 
		) AS "TotalPeriodo"

FROM "BSCL.Normativa.BD::CAR.CartolaHistorica" AS CART
LEFT JOIN v_ene AS ene ON ene."Fecha" =  CART."Fecha" AND ene."IdCliente" = CART."IdCliente"
LEFT JOIN v_feb AS feb ON feb."Fecha" =  CART."Fecha" AND feb."IdCliente" = CART."IdCliente"
LEFT JOIN v_mar AS mar ON mar."Fecha" =  CART."Fecha" AND mar."IdCliente" = CART."IdCliente"
LEFT JOIN v_abr AS abr ON abr."Fecha" =  CART."Fecha" AND abr."IdCliente" = CART."IdCliente"
LEFT JOIN v_may AS may ON may."Fecha" =  CART."Fecha" AND may."IdCliente" = CART."IdCliente"
LEFT JOIN v_jun AS jun ON jun."Fecha" =  CART."Fecha" AND jun."IdCliente" = CART."IdCliente"
LEFT JOIN v_jul AS jul ON jul."Fecha" =  CART."Fecha" AND jul."IdCliente" = CART."IdCliente"
LEFT JOIN v_ago AS ago ON ago."Fecha" =  CART."Fecha" AND ago."IdCliente" = CART."IdCliente"
LEFT JOIN v_sep AS sep ON sep."Fecha" =  CART."Fecha" AND sep."IdCliente" = CART."IdCliente"
LEFT JOIN v_oct AS oct ON oct."Fecha" =  CART."Fecha" AND oct."IdCliente" = CART."IdCliente"
LEFT JOIN v_nov AS nov ON nov."Fecha" =  CART."Fecha" AND nov."IdCliente" = CART."IdCliente"
LEFT JOIN v_dic AS dic ON dic."Fecha" =  CART."Fecha" AND dic."IdCliente" = CART."IdCliente"
WHERE (CART."IdAgente" = :IdAgente OR :IdAgente = 0)
AND YEAR(CART."Fecha") = :Agno  
GROUP BY CART."NombreAgente" 	,CART."NombreCliente"


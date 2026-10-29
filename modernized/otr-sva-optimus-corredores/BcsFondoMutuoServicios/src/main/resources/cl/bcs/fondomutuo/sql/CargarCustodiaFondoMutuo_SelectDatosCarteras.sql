SELECT "Nemotecnico" AS "IdSerieFondo",SUM(COALESCE("Cantidad",0)) AS "TotalCuotas","IdRegimen" ,"FondoExtranjero","ISIN"
FROM (
	select  CAR."Nemotecnico", 
			CAR."INGCantidad" AS "Cantidad",
			CASE WHEN COALESCE(REG."IdRegimenFondoMutuo",'')  IN ('18Q','18T') AND F."FondoExtranjero" != 'S'  THEN 'No APV'
	 			WHEN COALESCE(REG."IdRegimenFondoMutuo",'') IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND F."FondoExtranjero" != 'S' THEN 'APV'
				WHEN COALESCE(REG."IdRegimenFondoMutuo",'') = '57B' AND F."FondoExtranjero" != 'S' THEN '57B' 
				WHEN F."FondoExtranjero" = 'S' THEN ''
				ELSE '57BT' END AS	"IdRegimen",
			F."FondoExtranjero",
			CASE F."FondoExtranjero" WHEN 'S' THEN
				SF."ISIN" 
			ELSE '1'
			END AS "ISIN"
	from "BSCL.Custodia.BD::CUS.Cartera" CAR
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" REG ON (REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo")
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SF ON (CAR."Nemotecnico" = SF."IdSerieFondo")
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS F ON (SF."IdFondo" = F."IdFondo")
	where CAR."IdEmpresa" = :IdEmpresa AND
		  CAR."IdTipoInstrumento" = 'FFM' AND 
		  :FechaCuadratura BETWEEN CAR."INGFechaOperacion" and case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end and
		  :FechaCuadratura < case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end and
		  CAR."IdEstado" = 'VIG'
		  
UNION ALL

	select  CAR."Nemotecnico", 
		    CAR."INGCantidad" AS "Cantidad",
			CASE WHEN COALESCE(REG."IdRegimenFondoMutuo",'')  IN ('18Q','18T') AND F."FondoExtranjero" != 'S'  THEN 'No APV'
	 			WHEN COALESCE(REG."IdRegimenFondoMutuo",'') IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND F."FondoExtranjero" != 'S' THEN 'APV'
				WHEN COALESCE(REG."IdRegimenFondoMutuo",'') = '57B' AND F."FondoExtranjero" != 'S' THEN '57B' 
				WHEN F."FondoExtranjero" = 'S' THEN ''
				ELSE '57BT' END AS	"IdRegimen",
			F."FondoExtranjero",
			CASE F."FondoExtranjero" WHEN 'S' THEN
				SF."ISIN" 
			ELSE '1'
			END AS "ISIN"
	FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" CAR
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" REG ON (REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo")
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SF ON (CAR."Nemotecnico" = SF."IdSerieFondo")
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" F ON (SF."IdFondo" = F."IdFondo")
	where CAR."IdEmpresa" = :IdEmpresa AND
		  CAR."IdTipoInstrumento" = 'FFM' AND 
		  CAR."IdEstado" = 'VIG' AND
		  :FechaCuadratura BETWEEN CAR."INGFechaOperacion" and case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end and
		  :FechaCuadratura < case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end 
) T
group by T."Nemotecnico",T."IdRegimen",T."FondoExtranjero",T."ISIN";
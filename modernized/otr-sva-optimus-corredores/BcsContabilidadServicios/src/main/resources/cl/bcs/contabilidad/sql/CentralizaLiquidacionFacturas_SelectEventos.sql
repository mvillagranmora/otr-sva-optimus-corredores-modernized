SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_COBRO' AS "IdEvento", 'MONTO COBRO' AS "Nombre", IFNULL("MontoCobro",0) AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
			FROM 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC', 'BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_FINAL', 'MONTO FINAL',ABS(IFNULL("MontoComprobanteFinal",0)),"IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_NETO', 'MONTO NETO',IFNULL("MontoComprobante",0),"IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_COBRO_COMPROBANTE', 'MONTO COBRO COMPROBANTE',IFNULL("MontoComprobanteCobro",0),"IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION 
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_COBRO_LIQUIDACION', 'MONTO COBRO LIQUIDACION',IFNULL("MontoLiquidacionCobro",0), "IdMonedaLiquidacion"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'PERDIDA', 'PERDIDA',CASE WHEN "EGRSpread" < 0 THEN "EGRSpread" ELSE 0 END, "IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM
			INNER JOIN  "BSCL.Custodia.BD::CUS.Cartera" CAR ON  COM."IdComprobante" =  "EGRFolioComprobante"
			WHERE COM."FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND COM."IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
			AND "AccionTipoOperacion" = 'VENTA' AND COM."IdClasificacionTipoInstrumento" IN ('RF','IF')
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'UTILIDAD', 'UTILIDAD',CASE WHEN "EGRSpread" > 0 THEN "EGRSpread" ELSE 0 END, "IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM
			INNER JOIN  "BSCL.Custodia.BD::CUS.Cartera" CAR ON  COM."IdComprobante" =  "EGRFolioComprobante"
			WHERE COM."FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND COM."IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
			AND "AccionTipoOperacion" = 'VENTA' AND COM."IdClasificacionTipoInstrumento" IN ('RF','IF')
		UNION 
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'TOTAL_IMPUESTOS', 'IMPUESTO',IFNULL("TotalImpto",0), "IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION 
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'TOTAL_COMISIONES', 'COMISION',IFNULL("TotalComis",0), "IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION 
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'TOTAL_DERECHOS', 'DERECHO',IFNULL("TotalDere",0), "IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION 
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'TOTAL_GASTOS', 'GASTO',IFNULL("TotalGasto",0), "IdMonedaComprobante"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'CL'
		UNION
--- Peru PE		
--	SELECT "IdComprobante" AS "IdMovimiento",'TOTAL_POLIZA' AS "IdEvento", 'TOTAL_POLIZA' AS "Nombre", IFNULL("MontoLiquidacionFinal",0) AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'TOTAL_POLIZA', 'TOTAL_POLIZA',IFNULL(ABS("MontoLiquidacionFinal"),0), "IdMonedaLiquidacion"
			FROM 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_BRUTO', 'MONTO_BRUTO',IFNULL("MontoLiquidacion",0), "IdMonedaLiquidacion"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
			WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" = 'N' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'
		UNION
--
		
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_GENERICA', 'COMISION_GENERICA',IFNULL(COB."MontoLiquidacionCobro",0),"CodigoMonedaLiquidacionCobro"
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"			AS COM
		LEFT JOIN 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"	AS COB
		ON (COM."IdComprobante" = COB."IdComprobante")
		INNER JOIN 		"BSCL.Plataforma.BD::COM_2.DefinicionCobro"				AS DEF
		ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")						
		INNER JOIN 	"BSCL.Plataforma.BD::COM_2.ClaseCobro"						AS CLA
		ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
		WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_GENERICA'  AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'
			
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_CAVALI', 'COMISION_CAVALI',IFNULL(COB."MontoLiquidacionCobro",0),"CodigoMonedaLiquidacionCobro"
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"			AS COM
		LEFT JOIN 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"	AS COB
		ON (COM."IdComprobante" = COB."IdComprobante")
		INNER JOIN 		"BSCL.Plataforma.BD::COM_2.DefinicionCobro"				AS DEF
		ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")						
		INNER JOIN 	"BSCL.Plataforma.BD::COM_2.ClaseCobro"						AS CLA
		ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
		WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_CAVALI'  AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'
			
		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_BVL', 'COMISION_BVL',IFNULL(COB."MontoLiquidacionCobro",0),"CodigoMonedaLiquidacionCobro"
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"			AS COM
		LEFT JOIN 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"	AS COB
		ON (COM."IdComprobante" = COB."IdComprobante")
		INNER JOIN 		"BSCL.Plataforma.BD::COM_2.DefinicionCobro"				AS DEF
		ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")						
		INNER JOIN 	"BSCL.Plataforma.BD::COM_2.ClaseCobro"						AS CLA
		ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
		WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_BVL'  AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'	

		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'FONDO_GARANTIA', 'FONDO_GARANTIA',IFNULL(COB."MontoLiquidacionCobro",0),"CodigoMonedaLiquidacionCobro" 
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"			AS COM
		LEFT JOIN 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"	AS COB
		ON (COM."IdComprobante" = COB."IdComprobante")
		INNER JOIN 		"BSCL.Plataforma.BD::COM_2.DefinicionCobro"				AS DEF
		ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")						
		INNER JOIN 	"BSCL.Plataforma.BD::COM_2.ClaseCobro"						AS CLA
		ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
		WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND DEF."IdClaseCobro" = 'FONDO_GARANTIA'  AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'	

		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_SMV', 'COMISION_SMV',IFNULL(COB."MontoLiquidacionCobro",0),"CodigoMonedaLiquidacionCobro" 
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"			AS COM
		LEFT JOIN 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"	AS COB
		ON (COM."IdComprobante" = COB."IdComprobante")
		INNER JOIN 		"BSCL.Plataforma.BD::COM_2.DefinicionCobro"				AS DEF
		ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")						
		INNER JOIN 	"BSCL.Plataforma.BD::COM_2.ClaseCobro"						AS CLA
		ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
		WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_SMV'  AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'	

		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'IMPUESTO_IGV', 'IMPUESTO_IGV',IFNULL(COB."MontoLiquidacionCobro",0),"CodigoMonedaLiquidacionCobro" 
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"			AS COM
		LEFT JOIN 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"	AS COB
		ON (COM."IdComprobante" = COB."IdComprobante")
		INNER JOIN 		"BSCL.Plataforma.BD::COM_2.DefinicionCobro"				AS DEF
		ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")						
		INNER JOIN 	"BSCL.Plataforma.BD::COM_2.ClaseCobro"						AS CLA
		ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
		WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND DEF."IdClaseCobro" = 'IMPUESTO_IGV'  AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'	

		UNION
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_SAB', 'COMISION_SAB',IFNULL(COB."MontoLiquidacionCobro",0),"CodigoMonedaLiquidacionCobro" 
		FROM 		"BSCL.Facturacion.BD.Vistas::ConsultarComprobante"			AS COM
		LEFT JOIN 	"BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"	AS COB
		ON (COM."IdComprobante" = COB."IdComprobante")
		INNER JOIN 		"BSCL.Plataforma.BD::COM_2.DefinicionCobro"				AS DEF
		ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")						
		INNER JOIN 	"BSCL.Plataforma.BD::COM_2.ClaseCobro"						AS CLA
		ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
		WHERE 	COM."FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_SAB'  AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'
		
		UNION
		
		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_INVERSION', 'MONTO_INVERSION',IFNULL("MontoLiquidacion",0),"IdMonedaLiquidacion" 		
		FROM  "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
		WHERE "FechaLiquidacion" = :FechaContable AND  "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND :Sistema = 'PE'

		UNION

		SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", ComprobanteCobro."IdComprobante",Cobro."IdCobro", Cobro."NombreCobro", ABS(IFNULL("MontoLocalCobro",0)), "IdMonedaLocalCobro"
			FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" ComprobanteCobro
			JOIN "BSCL.Plataforma.BD::COM.Cobro" Cobro ON
			Cobro."IdCobro" = ComprobanteCobro."IdCobro"
			WHERE "IdComprobante" IN (SELECT "IdComprobante" FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
									   WHERE 	"FechaLiquidacion" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "IdTipoComprobante" NOT IN ('FOC','BOE', 'FOE' )	AND "EsVentaCorta" = 'N' );--AND "FolioComprobante" IN (730106, 99071));
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'TOTAL_POLIZA' AS "IdEvento", 'TOTAL_POLIZA' AS "Nombre",IFNULL(ABS("MontoLiquidacionFinal"),0) AS "MontoEvento", "IdMonedaLiquidacion" AS "CodigoMoneda"
	FROM    "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
	WHERE   "FechaSistema" = :FechaContable AND "DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_BRUTO', 'MONTO_BRUTO',IFNULL("MontoLiquidacion",0), "IdMonedaLiquidacion"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
	WHERE "FechaSistema" = :FechaContable  AND "DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_INTERES', 'MONTO_INTERES',IFNULL("Intereses",0),"IdMonedaLiquidacion"         
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
	WHERE "FechaSistema" = :FechaContable AND "DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_REAJUSTE_VAC', 'MONTO_REAJUSTE_VAC',IFNULL("ReajustesVAC",0),"IdMonedaLiquidacion"        
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
	WHERE "FechaSistema" = :FechaContable AND "DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_GENERICA', 'COMISION_GENERICA',SUM(IFNULL(COB."MontoLiquidacionCobro",0)) ,COB."CodigoMonedaLiquidacionCobro"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_GENERICA' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."MontoLiquidacionCobro",COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_CAVALI', 'COMISION_CAVALI',SUM(IFNULL(COB."MontoLiquidacionCobro",0)),COB."CodigoMonedaLiquidacionCobro"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE   COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_CAVALI' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."MontoLiquidacionCobro",COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_BVL', 'COMISION_BVL',SUM(IFNULL(COB."MontoLiquidacionCobro",0)),COB."CodigoMonedaLiquidacionCobro"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE   COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_BVL' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'FONDO_GARANTIA', 'FONDO_GARANTIA',SUM(IFNULL(COB."MontoLiquidacionCobro",0)),COB."CodigoMonedaLiquidacionCobro" 
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE   COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'FONDO_GARANTIA' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."MontoLiquidacionCobro",COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_SMV', 'COMISION_SMV',SUM(IFNULL(COB."MontoLiquidacionCobro",0)),COB."CodigoMonedaLiquidacionCobro" 
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE   COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_SMV' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'IMPUESTO_IGV', 'IMPUESTO_IGV',SUM(IFNULL(COB."MontoLiquidacionCobro",0)),COB."CodigoMonedaLiquidacionCobro" 
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'IMPUESTO_IGV' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'COMISION_SAB', 'COMISION_SAB',SUM(IFNULL(COB."MontoLiquidacionCobro",0)),COB."CodigoMonedaLiquidacionCobro" 
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE   COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_SAB' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", COB."IdComprobante",'OTRAS_COMISIONES', 'OTRAS_COMISIONES',SUM(IFNULL(COB."MontoLiquidacionCobro",0)),COB."CodigoMonedaLiquidacionCobro" 
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros"       COB
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro"         DEF
	ON (DEF."CodigoDefinicionCobro" = COB."IdCobro")                        
	INNER JOIN "BSCL.Plataforma.BD::COM_2.ClaseCobro"              CLA
	ON CLA."IdClaseCobro" = DEF."IdClaseCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = COB."IdComprobante")
	WHERE   COB."FechaSistema" = :FechaContable AND COB."IdEstado" <> 'ANU' AND DEF."IdClaseCobro" = 'COMISION_GENERICA' AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
	GROUP BY  TO_BIGINT(:IdTipoCentralizacion), COB."IdComprobante", COB."CodigoMonedaLiquidacionCobro"
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'MONTO_INVERSION', 'MONTO_INVERSION',IFNULL("MontoLiquidacion",0),"IdMonedaLiquidacion"      
	FROM  "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
	WHERE "FechaSistema" = :FechaContable AND "DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", ComprobanteCobro."IdComprobante",Cobro."IdCobro", Cobro."NombreCobro", ABS(IFNULL(ComprobanteCobro."MontoLocalCobro",0)), ComprobanteCobro."IdMonedaLocalCobro"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" ComprobanteCobro
	JOIN "BSCL.Plataforma.BD::COM.Cobro" Cobro ON
	Cobro."IdCobro" = ComprobanteCobro."IdCobro"
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM ON (COM."IdComprobante" = ComprobanteCobro."IdComprobante")
	WHERE ComprobanteCobro."IdComprobante" IN (SELECT "IdComprobante" FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
	                           WHERE    "FechaSistema" = :FechaContable) AND COM."DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'
        
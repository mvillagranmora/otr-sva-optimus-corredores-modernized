-- Obtiene detalle de movimientos para generar comprobante (movimiento unico por comprobante y agrupados por comprobante)
INSERT INTO "BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante"
(	
	"Grupo","IdComprobante","NroLinea","IdIngresoEgreso","IdTipoMovimiento","IdTipoMovimientoDetalle","IdConcepto",
	"IdCaja","IdMoneda","Monto","FechaLiquidacion",	"FechaIngresoRegistro","FechaMovimiento","IdTipoPersona",
	"IdPersona","NombrePersona","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdTipoPersonaTitularPago",	
	"IdPersonaTitularPago","NombrePersonaTitularPago","IdPortafolioTitularPago","SecuenciaAgrupacion","SecuenciaRelacionada",
	"MueveCuentaInversion","IdEmpleadoDigitador","IdSucursalEmpleadoDigitador","EsBilateral","TitularPago",	"IdTipoComprobanteOrigen",
	"IdComprobanteOrigen","FechaComprobanteOrigen","FechaLiquidacionComprobanteOrigen","IdTipoProducto","IdTipoOperacion",
	"IdBolsa","IdMonedaOperacion","MontoOperacion",	"FechaParidad","IdTipoReajuste","ValorParidad","FolioCartera",
	"IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","ComprobanteAgrupado",	"ComprobanteNeteado",
	"NumeroReferencia", "TipoComprobante","IdCondicionDeLiquidacion", "Glosa"
)

	-- *******************************************************************************
	-- MOVIMIENTOS EN GRUPO COMPROBANTE (v_MovimientoEnGruposComprobantes)
	-- *******************************************************************************	

	SELECT  IEA."Grupo",IEA."IdComprobante",
			ROW_NUMBER() over (Partition by
									IE."NumeroReferencia",											
			 						IE."IdPersonaTitularPago",
			 						IE."IdPortafolioTitularPago",
									IE."IdMoneda",
					                IE."FechaLiquidacion",
					                IE."SecuenciaAgrupacion",
					                IE."MueveCuentaInversion",
					                IEA."TipoComprobante",
					                IEA."IdCaja",
					                IEA."NeteaComprobante",
					                IEA."IdTipoMovimiento",
					                IEA."AgrupaComprobante",	
			                		IEA."IdInternoTes",					                	
					                IEA."IdConcepto",
					                IEA."IdEmpleadoDigitador",
					                IEA."IdSucursalEmpleadoDigitador",
					                IEA."IdTipoComprobanteOrigen",
					                IEA."IdTipoProducto",
					                IEA."IdTipoOperacion",
					                IEA."IdBolsa",
					                IEA."IdCondicionDeLiquidacion"
					    		) as "NroLinea",
			IE."IdIngresoEgreso",CASE WHEN IEA."NeteaComprobante" = 'S' THEN '' ELSE IE."IdTipoMovimiento" END AS "IdTipoMovimiento",
			IE."IdTipoMovimiento" AS "IdTipoMovimientoDetalle", IE."IdConcepto",
			CASE WHEN :IdCajaComprobante = 0 THEN (CASE WHEN COALESCE(IE."IdCaja",0) = 0 THEN CMU."IdCaja" ELSE IE."IdCaja" END) ELSE :IdCajaComprobante END as "IdCaja",
			IE."IdMoneda",CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN CAST((-1*IE."Monto") AS DECIMAL(26,8)) ELSE IE."Monto" END AS "Monto",IE."FechaLiquidacion",
			IE."FechaIngresoRegistro",IE."FechaMovimiento",
	
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN '' 							 WHEN IE."TipoComprobante" = 'PAREGR' THEN 'CNT' ELSE IE."IdTipoPersona" END AS "IdTipoPersona",
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."IdPersona" 		 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."IdPersona" ELSE IE."IdPersona" END AS "IdPersona",					
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."NombreCompleto" 	 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."NombreCompleto" ELSE IE."NombrePersona" END AS "NombrePersona",
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."Tipo" 				 WHEN IE."TipoComprobante" = 'PAREGR' THEN '' ELSE IE."IdTipoDocumentoIdentidad" END AS "IdTipoDocumentoIdentidad",
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."DocumentoIdentidad" WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."DocumentoIdentidadDefecto" ELSE IE."DocumentoIdentidad" END AS "DocumentoIdentidad",
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN '' 							 WHEN IE."TipoComprobante" = 'PAREGR' THEN 'CNT' ELSE IE."IdTipoPersonaTitularPago" END AS "IdTipoPersonaTitularPago",
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."IdPersona" 		 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."IdPersona" ELSE IE."IdPersonaTitularPago" END AS "IdPersonaTitularPago",
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN InfoCorr."NombreCompleto" 	 WHEN IE."TipoComprobante" = 'PAREGR' THEN InfoBanco."NombreCompleto" ELSE IE."NombrePersonaTitularPago" END AS "NombrePersonaTitularPago",
			CASE WHEN IE."TipoComprobante" = 'PARING' THEN 0 							 WHEN IE."TipoComprobante" = 'PAREGR' THEN 0 ELSE IE."IdPortafolioTitularPago" END AS "IdPortafolioTitularPago",
			
			IE."SecuenciaAgrupacion",IE."SecuenciaRelacionada",IE."MueveCuentaInversion",IE."IdEmpleadoDigitador",IE."IdSucursalEmpleadoDigitador",IE."EsBilateral",					
			CASE WHEN IE."TipoComprobante" = 'PAREGR' THEN 'S' ELSE IE."TitularPago" END AS "TitularPago",
			IE."IdTipoComprobanteOrigen",IE."IdComprobanteOrigen",IE."FechaComprobanteOrigen",IE."FechaLiquidacionComprobanteOrigen",
			IE."IdTipoProducto",IE."IdTipoOperacion",IE."IdBolsa",IE."IdMonedaOperacion",IE."MontoOperacion",IE."FechaParidad",IE."IdTipoReajuste",IE."ValorParidad",
			IE."FolioCartera",IE."IdInstrumento",IE."IdTipoInstrumento",IE."IdClasificacionTipoInstrumento",IE."AgrupaComprobante" AS "ComprobanteAgrupado",
			IE."NeteaComprobante" AS "ComprobanteNeteado",IE."NumeroReferencia",IE."TipoComprobante",
			IE."IdCondicionDeLiquidacion",
			COALESCE(IE."Glosa", '') AS "Glosa"
	  FROM "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso" AS IE
	  -- Movimientos Agrupables (v_MovimientosAgrupables)
	 INNER JOIN (SELECT * FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" WHERE "IdInternoTes" IS NULL) AS IEA 
	 											ON (   IE."NumeroReferencia"		= IEA."NumeroReferencia"
												   AND IE."IdMoneda" 				= IEA."IdMoneda"
												   AND IE."FechaLiquidacion"		= IEA."FechaLiquidacion"
												   AND IE."MueveCuentaInversion"	= IEA."MueveCuentaInversion"
												   AND IE."SecuenciaAgrupacion" 	= IEA."SecuenciaAgrupacion")
	  LEFT JOIN ( SELECT "IdCaja","IdMoneda","IdTipoMovimiento"
				  FROM ( 
						SELECT MAX("IdCaja") over (partition by "IdMoneda") as "MaxIdCaja","IdCaja","IdMoneda","IdTipoMovimiento"
					      FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario"
					     WHERE "IdEmpresa" = :IdEmpresa 		       
					       AND "IdUsuario" = :IdUsuario
					       AND "IdEstadoCaja" = 'ABT'
					       AND "IdEstadoCajaMoneda" = 'ACT'
					       AND "IdEstadoCajaUsuario" = 'ACT'
					       AND "FechaInicio"  <= :FechaMovimiento
						   AND "FechaTermino" >= :FechaMovimiento ) AS T
				 WHERE "MaxIdCaja" = "IdCaja") AS CMU ON (CMU."IdMoneda" = IE."IdMoneda" 
					  									   AND (CMU."IdTipoMovimiento" = IE."IdTipoMovimiento" OR  CMU."IdTipoMovimiento" = 'TODOS'))
					  									   
	  LEFT JOIN (SELECT DISTINCT FP."NumeroReferencia", IB.* -- Solo para partidas de egreso
	  			 FROM "BSCL.Tesoreria.BD::GBL.FormaDePagoGenerarComprobante_Proceso"AS FP 
	  			 -- Informacion Banco Corredora (v_InformacionBancoCorredora)
	  			 LEFT JOIN "BSCL.Tesoreria.BD::GBL.CuentaBancariaCorredoraGenerarComprobante" AS IB ON IB."IdCuentaBanco" = FP."IdCuentaBancariaCorredora"
	  			) AS InfoBanco
	  		 ON  (InfoBanco."NumeroReferencia" = IE."NumeroReferencia" AND IE."TipoComprobante" = 'PAREGR')
	  		 
	 -- Informacion Corredora (v_InformacionCorredora)	 
	 INNER JOIN (
		SELECT CSN."IdPersona",CC."NombreCompleto",CC."Tipo",CC."DocumentoIdentidad" --,CP."IdPortafolio"
		  FROM "BSCL.Plataforma.BD::PLA.Empresa" AS E
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = E."IdSocioDeNegocio"
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = CSN."IdCliente"
	 	 WHERE E."IdEmpresa" = :IdEmpresa) AS InfoCorr ON 1=1
	 
	 
	 --WHERE IE."IdIngresoEgreso" NOT IN (select "IdIngresoEgreso" FROM :v_MovimientosNoAgrupables)
WHERE IE."IdIngresoEgreso" NOT IN (SELECT "IdInternoTes" FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" WHERE COALESCE("IdInternoTes",0) > 0)
	   AND IE."IdPersonaTitularPago" 			= COALESCE(IEA."IdPersonaTitularPago",IE."IdPersonaTitularPago")
	   AND IE."IdPortafolioTitularPago" 		= COALESCE(IEA."IdPortafolioTitularPago",IE."IdPortafolioTitularPago")
	   AND IE."IdCaja" 							= COALESCE(IEA."IdCaja",IE."IdCaja")
	   AND IE."NeteaComprobante"				= COALESCE(IEA."NeteaComprobante",IE."NeteaComprobante")
	   AND IE."IdConcepto" 						= COALESCE(IEA."IdConcepto",IE."IdConcepto")
	   AND IFNULL(IE."IdEmpleadoDigitador" , -1) = COALESCE(IEA."IdEmpleadoDigitador", IE."IdEmpleadoDigitador", -1)
	   AND IE."IdSucursalEmpleadoDigitador" 	= COALESCE(IEA."IdSucursalEmpleadoDigitador",IE."IdSucursalEmpleadoDigitador")
	   AND IE."IdTipoComprobanteOrigen" 		= COALESCE(IEA."IdTipoComprobanteOrigen",IE."IdTipoComprobanteOrigen")
	   AND IE."IdTipoProducto" 					= COALESCE(IEA."IdTipoProducto",IE."IdTipoProducto")
	   AND IE."IdTipoOperacion" 				= COALESCE(IEA."IdTipoOperacion",IE."IdTipoOperacion")
	   AND IE."IdBolsa" 						= COALESCE(IEA."IdBolsa",IE."IdBolsa")
	   AND IE."IdTipoMovimiento" 				= COALESCE(IEA."IdTipoMovimiento",IE."IdTipoMovimiento")
	   AND IE."IdCondicionDeLiquidacion"		= COALESCE(NULLIF(IEA."IdCondicionDeLiquidacion", ''),IE."IdCondicionDeLiquidacion")
			   
UNION ALL

	-- ************************************************************************
	-- MOVIMIENTOS UNICO POR COMPROBANTE (v_MovimientosUnicoPorComprobante)
	-- ************************************************************************
			
	SELECT  IEA."Grupo",IEA."IdComprobante",1 as "NroLinea",
			IE."IdIngresoEgreso",IE."IdTipoMovimiento",IE."IdTipoMovimiento" AS "IdTipoMovimientoDetalle",IE."IdConcepto",
			CASE WHEN :IdCajaComprobante = 0 THEN (CASE WHEN COALESCE(IE."IdCaja",0) = 0 THEN CMU."IdCaja" ELSE IE."IdCaja" END) ELSE :IdCajaComprobante END as "IdCaja",
			IE."IdMoneda",CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN CAST((-1*IE."Monto" ) AS DECIMAL(26,8)) ELSE IE."Monto" END AS "Monto",
			IE."FechaLiquidacion",IE."FechaIngresoRegistro",IE."FechaMovimiento",IE."IdTipoPersona",IE."IdPersona",IE."NombrePersona",
			IE."IdTipoDocumentoIdentidad",IE."DocumentoIdentidad",IE."IdTipoPersonaTitularPago",IE."IdPersonaTitularPago",IE."NombrePersonaTitularPago",
			IE."IdPortafolioTitularPago",IE."SecuenciaAgrupacion",IE."SecuenciaRelacionada",IE."MueveCuentaInversion",IE."IdEmpleadoDigitador",
			IE."IdSucursalEmpleadoDigitador",IE."EsBilateral",IE."TitularPago",IE."IdTipoComprobanteOrigen",IE."IdComprobanteOrigen",IE."FechaComprobanteOrigen",
			IE."FechaLiquidacionComprobanteOrigen",IE."IdTipoProducto",IE."IdTipoOperacion",IE."IdBolsa",IE."IdMonedaOperacion",IE."MontoOperacion",
			IE."FechaParidad",IE."IdTipoReajuste",IE."ValorParidad",IE."FolioCartera",IE."IdInstrumento",IE."IdTipoInstrumento",IE."IdClasificacionTipoInstrumento",
			'N' AS "ComprobanteAgrupado",'N' AS "ComprobanteNeteado",IE."NumeroReferencia",IE."TipoComprobante",IE."IdCondicionDeLiquidacion", COALESCE(IE."Glosa", '') AS "Glosa"
	  FROM "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso" AS IE
	  
	  -- Movimientos No Agrupables (v_MovimientosNoAgrupables)
	  INNER JOIN (	SELECT "IdInternoTes","Grupo","IdComprobante" 
	  				FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" 
	  				WHERE COALESCE("IdInternoTes",0) > 0) AS IEA ON (IE."IdIngresoEgreso" = IEA."IdInternoTes")
	  
	  -- Caja Moneda Usuario (v_CajaMonedaUsuario)
	  LEFT JOIN ( SELECT "IdCaja","IdMoneda","IdTipoMovimiento"
				  FROM (
						SELECT MAX("IdCaja") over (partition by "IdMoneda") as "MaxIdCaja","IdCaja","IdMoneda","IdTipoMovimiento"
					      FROM "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario"
					     WHERE "IdEmpresa" = :IdEmpresa 		       
					       AND "IdUsuario" = :IdUsuario
					       AND "IdEstadoCaja" = 'ABT'
					       AND "IdEstadoCajaMoneda" = 'ACT'
					       AND "IdEstadoCajaUsuario" = 'ACT'
					       AND "FechaInicio"  <= :FechaMovimiento
						   AND "FechaTermino" >= :FechaMovimiento ) AS T
				  WHERE "MaxIdCaja" = "IdCaja") AS CMU ON (CMU."IdMoneda" = IE."IdMoneda" AND (CMU."IdTipoMovimiento" = IE."IdTipoMovimiento" OR  CMU."IdTipoMovimiento" = 'TODOS'));

		      
WITH ultimoRespaldo AS (
	SELECT 
		MAX(Cartera."FechaRespaldo") AS "FechaUltimoRespaldo"
	FROM 
		"BSCL.Custodia.BD::HIS.Cartera" AS Cartera
	WHERE 
		 Cartera."FechaRespaldo" BETWEEN ADD_DAYS(:FechaCorteCuponDesde, -7) AND ADD_DAYS(:FechaCorteCuponDesde, -1)
), CuponesCarteraHistorica AS (
	SELECT 
		CAR."FolioCartera", 
		CAR."IdEstado", 
		COALESCE(TO_VARCHAR(CAR."FechaRespaldo", 'YYYY-MM-DD'), '0001-01-01') AS "FechaUltimoRespaldo",
		CAR."Nemotecnico"
	FROM 
		"BSCL.Custodia.BD::HIS.Cartera" AS CAR
	INNER JOIN ultimoRespaldo UR ON  CAR."FechaRespaldo" = UR."FechaUltimoRespaldo"  AND CAR."IdEstado" = 'VIG'
	WHERE 
		CAR."IdClasificacionTipoInstrumento"   = 'RF'
		AND   CAR."INGFechaPagoCorteCupon"			BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
		AND   CAR."FechaLiquidacion" 				<= :FechaCorteCuponDesde
		AND   CAR."IdEstado" 						= 'VIG'
		AND	  CAR."INGFechaCorteCupon"				<= CAR."INGFechaPagoCorteCupon"
		AND   CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		AND   CAR."FechaRespaldo"					 < CAR."INGFechaCorteCupon"
		AND (CASE
				WHEN CAR."IdTipoInstrumento" = 'LTH' AND LOCATE(CAR."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
				WHEN CAR."IdTipoInstrumento" = 'LTH' AND LOCATE(CAR."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
				ELSE 'CORTE_CUPON'
			END) = 'CORTE_CUPON' 
		AND   CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
)
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,CA."INGNumeroCupon"											AS "Secuencia"	
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		 ELSE IFNULL(SOC."IdSocioDeNegocio",0)
		 END															AS "IdSocioNegocio"
		,CASE 
			WHEN SOC."IdCliente" > 0 		THEN 'CLI'
			WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
			WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
			WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
		END																AS "TipoSocioNegocio"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(CA."IdPortafolioRelacionado")
		 ELSE CA."IdPortafolio"
		 END 															AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(DOCIDEREL."DocumentoIdentidad")
		 ELSE CL."DocumentoIdentidad"
		 END 															AS "DocumentoIdentidad"
		,CASE
           WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN CASE
                                                            WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
                                                          ELSE MAX(PERREL."RazonSocial")
                                                          END 		                                                                                              
		 ELSE IFNULL(CL."NombreCompleto",'')
		 END 															AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,CA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,CASE WHEN CA."IdEstado" = 'VIG' 
				THEN SUM("Saldo") 
				ELSE SUM("Cantidad")									END AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		--AMORTIZACION
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGMontoCapitalCorteCupon"/"INGValorFactorMonedaInstrumentoActual"))
					ELSE SUM("INGMontoCapitalCorteCuponMonedaInstrumento") 
				 END
		ELSE 
				CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
					THEN SUM(CA."INGMontoCapitalCorteCupon") 
					ELSE SUM(CA."INGMontoCapitalCorteCuponMonedaInstrumento") 
				END
		END	AS "MontoCapital" --AMORTIZACION
		--INTERES
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGInteresesPagoCorteCupon"/"INGValorFactorMonedaInstrumentoActual"))
				ELSE SUM("INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		ELSE 
			CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(CA."INGInteresesPagoCorteCupon") 
				ELSE SUM(CA."INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		END	AS "MontoInteres" --INTERES
		--TOTAL
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGMontoCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) 
				ELSE SUM("INGMontoCorteCuponMonedaInstrumento") 
			END
		 ELSE
		 	CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN SUM(CA."INGMontoCorteCupon") 
		 		ELSE SUM(CA."INGMontoCorteCuponMonedaInstrumento") 
		 	END
		END	AS "Total"
		,CA."INGIdMonedaPagoCupon"										AS "IdMoneda"
		,CA."INGNumeroCupon"											AS "SecuenciaCupon"			
		,COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				AS "IdEmpleadoEjecutivoPortafolio"
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."NombreCompletoEjecutivo",'')						AS "NombreAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."INGMontoReajusteCorteCupon")							AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,CA."INGMontoInteresCorteCupon"									AS "INGMontoInteresCorteCupon"	
		,COALESCE(CA."IdTipoOperacion",'') AS "IdTipoOperacion"
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,CA."INGValorFactorMonedaInstrumentoProximo"
		,CA."INGFechaCorteCupon"
		,CA."INGFechaFactorMonedaAdicionalProximo"
		,CA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon" THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,CA."INGIdTipoOperacion"
		,CA."IdCartera"
		,'CARTERA' as "QueryOrigen"
FROM (
		SELECT 
		  		 CAR."IdEmpresa"
		  		,CAR."IdCartera"
				,CAR."INGNumeroCupon"	
				,CAR."IdClienteRelacionado"
				,CAR."IdTipoOperacion"
				,CAR."IdPortafolioRelacionado"
		  		,CAR."IdPortafolio"
		  		,CAR."IdCliente"
		  		,CAR."IdInstrumento"
		  		,CAR."IdTipoInstrumento"
		  		,CAR."INGFechaPagoCorteCupon"
		  		,CAR."IdClasificacionTipoInstrumento"
		  		,CAR."Nemotecnico"
		  		,CAR."IdEstado"
		  		,CASE WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG') THEN 0
		  			ELSE (CAR."INGCantidad" - CAR."EGRCantidad") END as "Saldo"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT') THEN CARVCD."INGCantidad"
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG') THEN 0 
			  		ELSE CAR."INGCantidad" END AS "Cantidad"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT' AND CAR."INGCantidad" > 0)
			  			THEN 
			  			CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP'
			  				THEN ROUND((CAR."INGMontoCapitalCorteCupon" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  				ELSE ROUND((CAR."INGMontoCapitalCorteCuponMonedaInstrumento" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  			END 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG') 
			  			THEN 0
				  		ELSE CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' THEN CAR."INGMontoCapitalCorteCupon" ELSE CAR."INGMontoCapitalCorteCuponMonedaInstrumento" END 
				 END AS "INGMontoCapitalCorteCupon"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT' AND CAR."INGCantidad" > 0)
			  			THEN 
			  			CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP'
			  				THEN ROUND((CAR."INGInteresesPagoCorteCupon" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  				ELSE ROUND((CAR."INGMontoInteresCorteCuponMonedaInstrumento" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  			END 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG')
			  			THEN 0
			  			ELSE CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' THEN CAR."INGInteresesPagoCorteCupon" ELSE CAR."INGMontoInteresCorteCuponMonedaInstrumento" END 
			  	 END AS "INGInteresesPagoCorteCupon"
		  		,CASE 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CARVCD."EGRIdTipoOperacion" = 'VCDTIT' AND CAR."INGCantidad" > 0)
			  			THEN 
			  			CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
			  				THEN ROUND((CAR."INGMontoCorteCupon" / CAR."INGCantidad") * CARVCD."INGCantidad")
			  				ELSE ROUND((CAR."INGMontoCorteCuponMonedaInstrumento" / CAR."INGCantidad") * CARVCD."INGCantidad") 
			  			END 
			  		WHEN (CAR."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CAR."IdEstado" <> 'VIG')
			  			THEN 0
			  			ELSE CASE WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' THEN CAR."INGMontoCorteCupon" ELSE CAR."INGMontoCorteCuponMonedaInstrumento" END 
			  	 END AS "INGMontoCorteCupon"
		  		,CAR."INGIdMonedaPagoCupon"
		  		,CAR."IdBoveda"
		  		,CAR."IdBolsa"
		  		,CAR."INGMontoReajusteCorteCupon"
		  		,CAR."INGMontoInteresCorteCupon"
		  		,CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
		  		,CAR."INGMontoCorteCuponMonedaInstrumento"
		  		,CAR."IdTipoReajuste"
		  		,CAR."FechaLiquidacion"
				,CAR."INGFechaCorteCupon"
				,CAR."INGValorFactorMonedaInstrumentoProximo"
				,CAR."INGValorFactorMonedaInstrumentoActual"
				,CAR."INGFechaFactorMonedaAdicionalProximo"
				,CAR."INGIdTipoOperacion"
				,CAR."INGMontoInteresCorteCuponMonedaInstrumento"
		  		
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = CAR."EGRFolioComprobante" AND CMBTE."IdCliente" = CAR."IdCliente" AND CAR."EGRFolioComprobante"!= 0
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARVCD ON (
			CAR."FechaLiquidacion" = CARVCD."FechaLiquidacion"
			AND CAR."IdInstrumento" = CARVCD."IdInstrumento"
			AND CAR."IdPortafolioRelacionado" = CARVCD."IdPortafolio" 
			AND CAR."IdEstado" IN ('VIG','VCD') AND CAR."IdTipoOperacion" IN ('COMPAC','VENPAC') AND CARVCD."INGIdTipoOperacion" = 'VCDPAC'
		)
		WHERE CAR."IdClasificacionTipoInstrumento" = 'RF'
		AND (CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta)
		AND CAR."FechaComprobante" < CAR."INGFechaCorteCupon" 
		AND CAR."INGFechaOperacion" < CAR."INGFechaPagoCorteCupon" 
		AND CAR."IdEstado" IN ('VCD','VIG')
		AND CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)

		UNION ALL 

		SELECT --ventas
		  		 CAR."IdEmpresa"
		  		,CAR."IdCartera"
				,CAR."INGNumeroCupon"	
				,CAR."IdClienteRelacionado"
				,CAR."IdTipoOperacion"
				,CAR."IdPortafolioRelacionado"
		  		,CAR."IdPortafolio"
		  		,CAR."IdCliente"
		  		,CAR."IdInstrumento"
		  		,CAR."IdTipoInstrumento"
		  		,CAR."INGFechaPagoCorteCupon"
		  		,CAR."IdClasificacionTipoInstrumento"
		  		,CAR."Nemotecnico"
		  		,'VIG' AS "IdEstado"
		  		,CAR."EGRCantidad" as "Saldo"
		  		,CAR."EGRCantidad" AS "Cantidad"
		  		-- Monto capital
				,CASE 
				    WHEN "IdEstado" = 'RPA' THEN 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN (CAR."INGMontoCapitalCorteCupon" / CAR."INGCantidad") * "EGRCantidad"
				                ELSE (CAR."INGMontoCapitalCorteCuponMonedaInstrumento" / CAR."INGCantidad") * "EGRCantidad"
				        END 
				    ELSE 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAR."INGMontoCapitalCorteCupon"
				                ELSE CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
				        END 
				END AS "INGMontoCapitalCorteCupon",
				
				-- Intereses
				CASE 
				    WHEN "IdEstado" = 'RPA' THEN 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN (CAR."INGInteresesPagoCorteCupon" / CAR."INGCantidad") * "EGRCantidad"
				                ELSE (CAR."INGMontoInteresCorteCuponMonedaInstrumento" / CAR."INGCantidad") * "EGRCantidad"
				        END 
				    ELSE 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAR."INGInteresesPagoCorteCupon"
				                ELSE CAR."INGMontoInteresCorteCuponMonedaInstrumento"
				        END 
				END AS "INGInteresesPagoCorteCupon",
				
				-- Monto total corte
				CASE 
				    WHEN "IdEstado" = 'RPA' THEN 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN (CAR."INGMontoCorteCupon" / CAR."INGCantidad") * "EGRCantidad"
				                ELSE (CAR."INGMontoCorteCuponMonedaInstrumento" / CAR."INGCantidad") * "EGRCantidad"
				        END 
				    ELSE 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAR."INGMontoCorteCupon"
				                ELSE CAR."INGMontoCorteCuponMonedaInstrumento"
				        END 
				END AS "INGMontoCorteCupon"
		  		,CAR."INGIdMonedaPagoCupon"
		  		,CAR."IdBoveda"
		  		,CAR."IdBolsa"
		  		,CAR."INGMontoReajusteCorteCupon"
		  		,CAR."INGMontoInteresCorteCupon"
		  		,CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
		  		,CAR."INGMontoCorteCuponMonedaInstrumento"
		  		,CAR."IdTipoReajuste"
		  		,CAR."FechaLiquidacion"
				,CAR."INGFechaCorteCupon"
				,CAR."INGValorFactorMonedaInstrumentoProximo"
				,CAR."INGValorFactorMonedaInstrumentoActual"
				,CAR."INGFechaFactorMonedaAdicionalProximo"
				,CAR."INGIdTipoOperacion"
				,CAR."INGMontoInteresCorteCuponMonedaInstrumento"
		  		
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE ON CMBTE."IdComprobante" = CAR."EGRFolioComprobante" AND CMBTE."IdCliente" = CAR."IdCliente" AND CAR."EGRFolioComprobante"!= 0 AND CMBTE."IdEstadoComprobante" != 'ANU'
		WHERE CAR."IdClasificacionTipoInstrumento"   = 'RF'
		AND (CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta)
		AND (CAR."EGRFechaLiquidacion" >= :FechaCorteCuponDesde AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N')  
		AND CAR."INGFechaCorteCupon" < CAR."EGRFechaLiquidacion"
		AND CAR."IdTipoInstrumento" != 'SIM'
		AND CAR."IdClasificacionTipoInstrumento" != 'DV'
		AND CAR."IdEstado" != 'ANU'
		AND CAR."EGRIdTipoOperacion" = 'VENRF'
		AND CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		
		UNION ALL
		
		SELECT --Devuelve cantidades retiradas cuando la fecha de liquidación del retiro es superior a la fecha de corte de cupón
		  		CAR."IdEmpresa"
		  		,CAR."IdCartera"
				,CAR."INGNumeroCupon"	
				,CAR."IdClienteRelacionado"
				,CAR."IdTipoOperacion"
				,CAR."IdPortafolioRelacionado"
		  		,CAR."IdPortafolio"
		  		,CAR."IdCliente"
		  		,CAR."IdInstrumento"
		  		,CAR."IdTipoInstrumento"
		  		,CAR."INGFechaPagoCorteCupon"
		  		,CAR."IdClasificacionTipoInstrumento"
		  		,CAR."Nemotecnico"
		  		,'VIG' AS "IdEstado"
		  		,CAR."EGRCantidad" as "Saldo"
		  		,CAR."EGRCantidad" AS "Cantidad"
		  				-- Monto capital
				,CASE 
				    WHEN "IdEstado" = 'RPA' THEN 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAST((("INGMontoCapitalCorteCupon" / CAR."INGCantidad") * "EGRCantidad") AS INT)
				                ELSE (CAR."INGMontoCapitalCorteCuponMonedaInstrumento" / CAR."INGCantidad") * "EGRCantidad"
				        END 
				    ELSE 
				        CASE 
				            WHEN CAR."INGIdMonedaPagoCupon" = 'CLP' 
				                THEN CAR."INGMontoCapitalCorteCupon"
				                ELSE CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
				        END 
				END AS "INGMontoCapitalCorteCupon"

				,CASE WHEN "IdEstado" = 'RPA' THEN 
						CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP' 
							THEN CAST((("INGMontoInteresCorteCupon" / CAR."INGCantidad") * "EGRCantidad") AS INT)
							ELSE ("INGMontoInteresCorteCuponMonedaInstrumento"/CAR."INGCantidad")*"EGRCantidad"
						END 
				 ELSE CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
				 			THEN "INGMontoInteresCorteCupon" 
				 			ELSE "INGMontoInteresCorteCuponMonedaInstrumento"
				 	  END 
				 END  AS "INGInteresesPagoCorteCupon"	 
				,CASE WHEN "IdEstado" = 'RPA' THEN
						CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
							THEN CAST((("INGMontoCorteCupon"/CAR."INGCantidad")*"EGRCantidad") AS INT)
							ELSE ("INGMontoCorteCuponMonedaInstrumento"/CAR."INGCantidad")*"EGRCantidad"
						END 
				 ELSE CASE WHEN  CAR."INGIdMonedaPagoCupon" = 'CLP'
				 			THEN "INGMontoCorteCupon" 
				 			ELSE "INGMontoCorteCuponMonedaInstrumento" 
				 		END 
			     END AS "INGMontoCorteCupon"
		  		,CAR."INGIdMonedaPagoCupon"
		  		,CAR."IdBoveda"
		  		,CAR."IdBolsa"
		  		,CAR."INGMontoReajusteCorteCupon"
		  		,CAR."INGMontoInteresCorteCupon"
		  		,CAR."INGMontoCapitalCorteCuponMonedaInstrumento"
		  		,CAR."INGMontoCorteCuponMonedaInstrumento"
		  		,CAR."IdTipoReajuste"
		  		,CAR."FechaLiquidacion"
				,CAR."INGFechaCorteCupon"
				,CAR."INGValorFactorMonedaInstrumentoProximo"
				,CAR."INGValorFactorMonedaInstrumentoActual"
				,CAR."INGFechaFactorMonedaAdicionalProximo"
				,CAR."INGIdTipoOperacion"
		  		,CAR."INGMontoInteresCorteCuponMonedaInstrumento"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		WHERE CAR."IdClasificacionTipoInstrumento"   = 'RF'
		AND (CAR."INGFechaPagoCorteCupon" BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta)
		AND (CAR."EGRFechaLiquidacion" >= :FechaCorteCuponDesde AND CAR."EGRCantidad" <> 0 AND CAR."EGRRetiroPorFusion" = 'N')  
		AND CAR."INGFechaCorteCupon" < CAR."EGRFechaLiquidacion"
		AND CAR."IdTipoInstrumento" != 'SIM'
		AND CAR."IdClasificacionTipoInstrumento" != 'DV'
		AND CAR."IdEstado" != 'ANU'
		AND CAR."EGRIdTipoOperacion" = 'RETTIT'
		AND CAR."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CAR."IdInstrumento" ELSE :IdInstrumento END)
		)  AS CA
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC  ON (CA."IdCliente" = SOC."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"        AS SOCREL ON (CA."IdClienteRelacionado" = SOCREL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL   ON (CA."IdCliente" = CL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"                               AS CLREL ON CLREL."IdCliente" = CA."IdClienteRelacionado"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"                               AS PERREL ON PERREL."IdPersona" = CLREL."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                    AS DOCIDEREL ON DOCIDEREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE  ON ((CA."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CA."IdPortafolioRelacionado" = EJE."IdPortafolio") OR (CA."IdTipoOperacion" NOT IN ('VENPAC','COMPAC') AND CA."IdPortafolio" = EJE."IdPortafolio"))
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" 								AS EMP  ON EMP."IdEmpresa" = CA."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 						AS SOCCLIREL ON SOCCLIREL."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
ON 	(   CA."IdInstrumento" 		  = EC."IdInstrumento" 			AND 
		CA."IdTipoInstrumento" 	  = EC."IdTipoInstrumento"		AND
		CA."INGFechaPagoCorteCupon" = EC."FechaPago"    	    AND
		EC."IdTipoEventoCapital"  = 'CC'                        AND   
		EC."IdEstado"            <> 'ANU'
	)

WHERE (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
		WHEN CA."IdTipoInstrumento" = 'LTH' AND LOCATE(CA."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
		WHEN CA."IdTipoInstrumento" = 'LTH' AND LOCATE(CA."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
		ELSE 'CORTE_CUPON'
	END) = 'CORTE_CUPON' 	
AND EC."IdTipoInstrumento" 				IS NULL
 
GROUP BY 
	CA."IdCartera",
	EC."IdEventoCapital",
	IFNULL(SOC."IdSocioDeNegocio",0),
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END,
	CA."IdPortafolio",				
	CL."DocumentoIdentidad",
	IFNULL(CL."NombreCompleto",''),				
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	CA."Nemotecnico",
	CA."INGFechaPagoCorteCupon",
	CA."INGFechaCorteCupon",
	CA."INGIdMonedaPagoCupon",						
	CA."INGNumeroCupon",
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
	COALESCE(EJE."CodigoEmpleado",''),
	COALESCE(EJE."NombreCompletoEjecutivo",''),
	COALESCE(EJE."CodigoSucursal",''),			
	CA."IdBoveda",
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."INGMontoInteresCorteCupon",
	CA."IdTipoOperacion",
	CA."IdEstado",	
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCapitalCorteCuponMonedaInstrumento"
	END,
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCorteCuponMonedaInstrumento"
	END,
	CA."FechaLiquidacion",
	CA."INGValorFactorMonedaInstrumentoProximo",
	CA."INGFechaCorteCupon",
	CA."INGFechaFactorMonedaAdicionalProximo",
	CA."INGValorFactorMonedaInstrumentoActual",
	CA."INGIdTipoOperacion"
	
	UNION ALL
	--Cartera Historica
	SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'N'															AS "EsCarteraNegativa"
		,CA."INGNumeroCupon"											AS "Secuencia"	
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		 ELSE IFNULL(SOC."IdSocioDeNegocio",0)
		 END															AS "IdSocioNegocio"
		,CASE 
			WHEN SOC."IdCliente" > 0 		THEN 'CLI'
			WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
			WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
			WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
		END																AS "TipoSocioNegocio"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(CA."IdPortafolioRelacionado")
		 ELSE CA."IdPortafolio"
		 END 															AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN MAX(DOCIDEREL."DocumentoIdentidad")
		 ELSE CL."DocumentoIdentidad"
		 END 															AS "DocumentoIdentidad"
		,CASE
           WHEN MAX(CA."IdTipoOperacion") IN ('COMPAC','VENPAC') THEN CASE
                                                            WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
                                                          ELSE MAX(PERREL."RazonSocial")
                                                          END 		                                                                                              
		 ELSE IFNULL(CL."NombreCompleto",'')
		 END 															AS "NombreCliente"
		,CA."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,CA."IdTipoInstrumento"											AS "IdTipoInstrumento"
		,CA."IdInstrumento"												AS "IdInstrumento"
		,CA."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."INGFechaPagoCorteCupon"									AS "FechaCierre"
		,CA."INGFechaPagoCorteCupon"									AS "FechaPago"
		,CASE WHEN CA."IdEstado" = 'VIG' 
				THEN SUM(CA."INGCantidad" - CA."EGRCantidad") 
				ELSE SUM("Cantidad")									END AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
					THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGMontoCapitalCorteCupon"/"INGValorFactorMonedaInstrumentoActual"))
					ELSE SUM("INGMontoCapitalCorteCuponMonedaInstrumento") 
				 END
		ELSE 
				CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
					THEN SUM(CA."INGMontoCapitalCorteCupon") 
					ELSE SUM(CA."INGMontoCapitalCorteCuponMonedaInstrumento") 
				END
		END	AS "MontoCapital" --AMORTIZACION
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGInteresesPagoCorteCupon"/"INGValorFactorMonedaInstrumentoActual"))
				ELSE SUM("INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		ELSE 
			CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP' 
				THEN SUM(CA."INGInteresesPagoCorteCupon") 
				ELSE SUM(CA."INGMontoInteresCorteCuponMonedaInstrumento") 
			END
		END	AS "MontoInteres" --INTERES
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND "INGFechaPagoCorteCupon" != "INGFechaCorteCupon"
			THEN CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
				THEN SUM("INGValorFactorMonedaInstrumentoProximo" * ("INGMontoCorteCupon"/"INGValorFactorMonedaInstrumentoActual")) 
				ELSE SUM("INGMontoCorteCuponMonedaInstrumento") 
			END
		 ELSE
		 	CASE WHEN CA."INGIdMonedaPagoCupon" = 'CLP'
		 		THEN SUM(CA."INGMontoCorteCupon") 
		 		ELSE SUM(CA."INGMontoCorteCuponMonedaInstrumento") 
		 	END
		END	AS "Total"
		,CA."INGIdMonedaPagoCupon"										AS "IdMoneda"
		,CA."INGNumeroCupon"											AS "SecuenciaCupon"			
		,COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				AS "IdEmpleadoEjecutivoPortafolio"
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."NombreCompletoEjecutivo",'')						AS "NombreAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."INGMontoReajusteCorteCupon")							AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,CA."INGMontoInteresCorteCupon"									AS "INGMontoInteresCorteCupon"	
		,COALESCE(CA."IdTipoOperacion",'') AS "IdTipoOperacion"
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,CA."INGValorFactorMonedaInstrumentoProximo"
		,CA."INGFechaCorteCupon"
		,'0001-01-01' AS "INGFechaFactorMonedaAdicionalProximo"
		,CA."INGValorFactorMonedaInstrumentoActual"
		,CASE WHEN "IdTipoReajuste" != 'CLP'  AND ("INGFechaPagoCorteCupon" != "INGFechaCorteCupon" OR "FechaRespaldo" < "INGFechaCorteCupon") THEN 'S' 
			ELSE 'N' END AS "UsaProximoFactorValorMoneda"
		,CA."IdTipoOperacion" AS "INGIdTipoOperacion"
		,CA."IdCartera"
		,'CARTERA_HISTORICA' as "QueryOrigen"
FROM CuponesCarteraHistorica AS CarteraHistorica
INNER JOIN "BSCL.Custodia.BD::HIS.Cartera"									AS CA ON CA."FolioCartera" = CarteraHistorica."FolioCartera" AND CA."FechaRespaldo" = CarteraHistorica."FechaUltimoRespaldo" AND CA."IdEstado" = CarteraHistorica."IdEstado"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC  ON (CA."IdCliente" = SOC."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"        AS SOCREL ON (CA."IdClienteRelacionado" = SOCREL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL   ON (CA."IdCliente" = CL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"                               AS CLREL ON CLREL."IdCliente" = CA."IdClienteRelacionado"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"                               AS PERREL ON PERREL."IdPersona" = CLREL."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                    AS DOCIDEREL ON DOCIDEREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE  ON ((CA."IdTipoOperacion" IN ('VENPAC','COMPAC') AND CA."IdPortafolioRelacionado" = EJE."IdPortafolio") OR (CA."IdTipoOperacion" NOT IN ('VENPAC','COMPAC') AND CA."IdPortafolio" = EJE."IdPortafolio"))
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" 								AS EMP  ON EMP."IdEmpresa" = CA."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 						AS SOCCLIREL ON SOCCLIREL."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
ON 	(   CA."IdInstrumento" 		  = EC."IdInstrumento" 			AND 
		CA."IdTipoInstrumento" 	  = EC."IdTipoInstrumento"		AND
		CA."INGFechaPagoCorteCupon" = EC."FechaPago"          	AND
		EC."IdTipoEventoCapital"  = 'CC'                        AND   
		EC."IdEstado"            <> 'ANU'
	)
WHERE EC."IdTipoInstrumento" 				IS NULL
GROUP BY 
	CA."IdCartera",
	EC."IdEventoCapital",
	IFNULL(SOC."IdSocioDeNegocio",0),
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END,
	CA."IdPortafolio",				
	CL."DocumentoIdentidad",
	IFNULL(CL."NombreCompleto",''),				
	CA."IdClasificacionTipoInstrumento",	
	CA."IdTipoInstrumento",		
	CA."IdInstrumento",
	CA."Nemotecnico",
	CA."INGFechaPagoCorteCupon",
	CA."INGFechaCorteCupon",
	CA."INGIdMonedaPagoCupon",						
	CA."INGNumeroCupon",
	CA."IdTipoReajuste",
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
	COALESCE(EJE."CodigoEmpleado",''),
	COALESCE(EJE."NombreCompletoEjecutivo",''),
	COALESCE(EJE."CodigoSucursal",''),			
	CA."IdBoveda",
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."INGMontoInteresCorteCupon",
	CA."IdTipoOperacion",
	CA."IdEstado",
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCapitalCorteCuponMonedaInstrumento"
	END,
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."INGMontoCorteCuponMonedaInstrumento"
	END,
	CA."FechaLiquidacion",
	CA."INGValorFactorMonedaInstrumentoProximo",
	CA."INGFechaCorteCupon",
	CA."INGValorFactorMonedaInstrumentoActual",
	CA."FechaRespaldo"
	
	UNION ALL 
	--Cartera Negativa
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'S'															AS "EsCarteraNegativa"
		,CA."NumeroCupon"												AS "Secuencia"	
		,CASE 
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		   WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		 ELSE IFNULL(SOC."IdSocioDeNegocio",0)
		 END															AS "IdSocioNegocio"
		,CASE 
			WHEN SOC."IdCliente" > 0 		THEN 'CLI'
			WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
			WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
			WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
		END																AS "TipoSocioNegocio"
		,CASE 
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN MAX(CA."IdPortafolio")
		   WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN MAX(CA."IdPortafolio")
		 ELSE CA."IdPortafolio"
		 END 															AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN MAX(DOCIDEREL."DocumentoIdentidad")
		   WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN MAX(DOCIDEREL."DocumentoIdentidad")
		 ELSE DOCIDEREL ."DocumentoIdentidad"
		 END 															AS "DocumentoIdentidad"
		,CASE
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN CASE
		                                                                                             WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
		                                                                                           ELSE MAX(PERREL."RazonSocial")
		                                                                                           END
           WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN CASE
                                                            WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
                                                          ELSE MAX(PERREL."RazonSocial")
                                                          END 		                                                                                              
		 ELSE IFNULL(CL."NombreCompleto",'')
		 END 															AS "NombreCliente"
		,TIP."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,TIP."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,INS."IdInstrumento"											AS "IdInstrumento"
		,INS."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."FechaPagoCorteCupon"										AS "FechaCierre"
		,CA."FechaPagoCorteCupon"										AS "FechaPago"
		,SUM("Cantidad")*-1									 			AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,SUM(CA."MontoCapitalCorteCupon") * -1							AS "MontoCapital"
		,SUM(CA."InteresesPagoCorteCupon") * -1							AS "MontoInteres"
		,SUM(CA."MontoCorteCupon") * -1									AS "Total"
		,CA."IdMonedaPagoCupon"											AS "IdMoneda"
		,CA."NumeroCupon"												AS "SecuenciaCupon"			
		,COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				AS "IdEmpleadoEjecutivoPortafolio"
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."NombreCompletoEjecutivo",'')						AS "NombreAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."MontoReajusteCorteCupon")								AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,CA."MontoInteresCorteCupon"									AS "INGMontoInteresCorteCupon"	
		,COALESCE(CA."IdTipoOperacion",'') AS "IdTipoOperacion"
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,0 AS "INGValorFactorMonedaInstrumentoProximo"
		,'0001-01-01' AS "INGFechaCorteCupon"
		,'0001-01-01' AS "INGFechaFactorMonedaAdicionalProximo"
		,0 AS "INGValorFactorMonedaInstrumentoActual"
		,'N' AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCarteraNegativa"
		,'CARTERA_NEGATIVA' as "QueryOrigen"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" 							AS CA
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CA."IdInstrumento" = INS."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIP ON INS."IdTipoInstrumento"  = TIP."IdTipoInstrumento" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC  ON (CA."IdCliente" = SOC."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"        AS SOCREL ON (CA."IdCliente" = SOCREL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL   ON (CA."IdCliente" = CL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"                               AS CLREL ON CLREL."IdCliente" = CA."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"                               AS PERREL ON PERREL."IdPersona" = CLREL."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                    AS DOCIDEREL ON DOCIDEREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE  ON (CA."IdPortafolio" = EJE."IdPortafolio")
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" 								AS EMP  ON EMP."IdEmpresa" = CA."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 						AS SOCCLIREL ON SOCCLIREL."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
ON 	(   CA."IdInstrumento" 		  = EC."IdInstrumento" 			AND 
		TIP."IdTipoInstrumento" 	  = EC."IdTipoInstrumento"		AND
		CA."FechaPagoCorteCupon" = EC."FechaPago"          	AND
		EC."IdTipoEventoCapital"  = 'CC'                        AND   
		EC."IdEstado"            <> 'ANU'
	)

WHERE TIP."IdClasificacionTipoInstrumento"   = 'RF' 
AND   CA."FechaPagoCorteCupon"					BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND   CA."FechaLiquidacion" 				<= :FechaCorteCuponDesde
AND   CA."IdEstado" 						IN ('VIG','VCD')
AND   EC."IdTipoInstrumento" 				IS NULL
AND   CA."FechaCorteCupon"					= CA."FechaPagoCorteCupon"

AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)
AND (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
		ELSE 'CORTE_CUPON'
	END) = 'CORTE_CUPON' 	

GROUP BY 
	CA."IdCarteraNegativa",
	EC."IdEventoCapital",
	IFNULL(SOC."IdSocioDeNegocio",0),
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END,
	CA."IdPortafolio",				
	DOCIDEREL."DocumentoIdentidad",
	IFNULL(CL."NombreCompleto",''),				
	TIP."IdClasificacionTipoInstrumento",	
	TIP."IdTipoInstrumento",		
	INS."IdInstrumento",
	INS."Nemotecnico",
	CA."FechaPagoCorteCupon",
	CA."IdMonedaPagoCupon",						
	CA."NumeroCupon",
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
	COALESCE(EJE."CodigoEmpleado",''),
	COALESCE(EJE."NombreCompletoEjecutivo",''),
	COALESCE(EJE."CodigoSucursal",''),			
	CA."IdBoveda",
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."MontoInteresCorteCupon",
	CA."IdTipoOperacion",
	CA."IdEstado",
	
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."MontoCapitalCorteCuponMonedaInstrumento"
	END,
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."MontoCorteCuponMonedaInstrumento"
	END,
	CA."FechaLiquidacion"
UNION ALL
	--Cartera Negativa Historica
SELECT 
		'CC'															AS "IdTipoEventoCapital"
		,'S'															AS "EsCarteraNegativa"
		,CA."NumeroCupon"												AS "Secuencia"	
		,CASE 
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		   WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN IFNULL(MAX(SOCREL."IdSocioDeNegocio"), 0)
		 ELSE IFNULL(SOC."IdSocioDeNegocio",0)
		 END															AS "IdSocioNegocio"
		,CASE 
			WHEN SOC."IdCliente" > 0 		THEN 'CLI'
			WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
			WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
			WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
		END																AS "TipoSocioNegocio"
		,CASE 
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN MAX(CA."IdPortafolio")
		   WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN MAX(CA."IdPortafolio")
		 ELSE CA."IdPortafolio"
		 END 															AS "IdPortafolio"
		,'R'															AS "IdTipoDocumentoIdentidad"
		,CASE 
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN MAX(DOCIDEREL."DocumentoIdentidad")
		   WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN MAX(DOCIDEREL."DocumentoIdentidad")
		 ELSE DOCIDEREL ."DocumentoIdentidad"
		 END 															AS "DocumentoIdentidad"
		,CASE
		   WHEN MAX(CA."IdCliente") = MAX(SOCCLIREL."IdCliente") AND max(CA."IdTipoOperacion") = 'VENPAC' THEN CASE
		                                                                                             WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
		                                                                                           ELSE MAX(PERREL."RazonSocial")
		                                                                                           END
           WHEN MAX(CA."IdTipoOperacion") = 'COMPAC' THEN CASE
                                                            WHEN MAX(PERREL."Tipo") = 'N' THEN MAX(PERREL."Nombres") ||' '|| MAX(PERREL."ApellidoPaterno") || ' ' || MAX(PERREL."ApellidoMaterno")
                                                          ELSE MAX(PERREL."RazonSocial")
                                                          END 		                                                                                              
		 ELSE IFNULL(CL."NombreCompleto",'')
		 END 															AS "NombreCliente"
		,TIP."IdClasificacionTipoInstrumento"							AS "IdClasificacionTipoInstrumento"
		,TIP."IdTipoInstrumento"										AS "IdTipoInstrumento"
		,INS."IdInstrumento"											AS "IdInstrumento"
		,INS."Nemotecnico"												AS "Nemo"
		,TO_BIGINT(0)													AS "IdInstrumentoNuevo"
		,''																AS "NemoNuevo"
		,CA."FechaPagoCorteCupon"										AS "FechaCierre"
		,CA."FechaPagoCorteCupon"										AS "FechaPago"
		,SUM("Cantidad")*-1									 			AS "CantidadDerecho"
		,'CUS'															AS "TipoCartera"
		,TO_DECIMAL(0,26,8)												AS "CantidadNueva"
		,SUM(CA."MontoCapitalCorteCupon") * -1							AS "MontoCapital"
		,SUM(CA."InteresesPagoCorteCupon") * -1							AS "MontoInteres"
		,SUM(CA."MontoCorteCupon") * -1									AS "Total"
		,CA."IdMonedaPagoCupon"											AS "IdMoneda"
		,CA."NumeroCupon"												AS "SecuenciaCupon"			
		,COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)				AS "IdEmpleadoEjecutivoPortafolio"
		,COALESCE(EJE."CodigoEmpleado",'')								AS "CodigoUsuarioAgente"
		,COALESCE(EJE."NombreCompletoEjecutivo",'')						AS "NombreAgente"
		,COALESCE(EJE."CodigoSucursal",'')								AS "CodigoSucursal"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraIngresoRegistro"
		,CURRENT_UTCTIMESTAMP											AS "FechaHoraUltimaActualizacion"		
		,'VIG'															AS "IdEstado"
		,CA."IdBoveda"													AS "IdBoveda"
		,CA."IdBolsa"													AS "IdBolsa"
		,''																AS "IdConceptoGarantia"
		,''																AS "IdTipoOperacionGarantia"
		,SUM(CA."MontoReajusteCorteCupon")								AS "MontoReajuste"
		,'SIS'															AS "Origen"
		,CA."IdTipoReajuste"											AS "IdTipoReajuste"
		,CA."MontoInteresCorteCupon"									AS "INGMontoInteresCorteCupon"	
		,COALESCE(CA."IdTipoOperacion",'') AS "IdTipoOperacion"
		,''	AS "TipoOperacion"
		,CA."FechaLiquidacion"
		,0 AS "INGValorFactorMonedaInstrumentoProximo"
		,'0001-01-01' AS "INGFechaCorteCupon"
		,'0001-01-01' AS "INGFechaFactorMonedaAdicionalProximo"
		,0 AS "INGValorFactorMonedaInstrumentoActual"
		,'N' AS "UsaProximoFactorValorMoneda"
		,'' AS "INGIdTipoOperacion"
		,CA."IdCarteraNegativa"
		,'CARTERA_NEGATIVA_HISTORICA' as "QueryOrigen"
FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" 							AS CA
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CA."IdInstrumento" = INS."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIP ON INS."IdTipoInstrumento"  = TIP."IdTipoInstrumento" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC  ON (CA."IdCliente" = SOC."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"        AS SOCREL ON (CA."IdCliente" = SOCREL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL   ON (CA."IdCliente" = CL."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"                               AS CLREL ON CLREL."IdCliente" = CA."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"                               AS PERREL ON PERREL."IdPersona" = CLREL."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"                    AS DOCIDEREL ON DOCIDEREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE  ON (CA."IdPortafolio" = EJE."IdPortafolio")
INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" 								AS EMP  ON EMP."IdEmpresa" = CA."IdEmpresa"
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 						AS SOCCLIREL ON SOCCLIREL."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" 			EC
ON 	(   CA."IdInstrumento" 		  = EC."IdInstrumento" 			AND 
		TIP."IdTipoInstrumento" 	  = EC."IdTipoInstrumento"		AND
		CA."FechaPagoCorteCupon" = EC."FechaPago"          	AND
		EC."IdTipoEventoCapital"  = 'CC'                        AND   
		EC."IdEstado"            <> 'ANU'
	)

WHERE TIP."IdClasificacionTipoInstrumento"   = 'RF' 
AND   CA."FechaPagoCorteCupon"					BETWEEN :FechaCorteCuponDesde AND :FechaCorteCuponHasta	
AND   CA."FechaLiquidacion" 				< :FechaCorteCuponDesde
AND   CA."IdEstado" 						= 'VIG'
AND   EC."IdTipoInstrumento" 				IS NULL
AND	  CA."FechaCorteCupon"					<> CA."FechaPagoCorteCupon"
AND   CA."FechaRespaldo"					 = CA."FechaCorteCupon" 

AND     CA."IdInstrumento" IN (CASE WHEN (COALESCE(:IdInstrumento,'') = '') THEN CA."IdInstrumento" ELSE :IdInstrumento END)
AND (CASE -- Letras hipotecarias cuyo nemo tiene "*" o "&" no generan pago cupon sino cambio de nemo
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '*') > 0 THEN 'CAMBIO_NEMO' 
		WHEN TIP."IdTipoInstrumento" = 'LTH' AND LOCATE(INS."Nemotecnico", '&') > 0 THEN 'CAMBIO_NEMO'
		ELSE 'CORTE_CUPON'
	END) = 'CORTE_CUPON' 	

GROUP BY 
	CA."IdCarteraNegativa",
	EC."IdEventoCapital",
	IFNULL(SOC."IdSocioDeNegocio",0),
	CASE 
		WHEN SOC."IdCliente" > 0 		THEN 'CLI'
		WHEN SOC."IdProveedor" > 0 		THEN 'PRO'
		WHEN SOC."IdEmpleado" > 0 		THEN 'EMP'
		WHEN SOC."IdContraparte" > 0 	THEN 'CNT'
	END,
	CA."IdPortafolio",				
	DOCIDEREL."DocumentoIdentidad",
	IFNULL(CL."NombreCompleto",''),				
	TIP."IdClasificacionTipoInstrumento",	
	TIP."IdTipoInstrumento",		
	INS."IdInstrumento",
	INS."Nemotecnico",
	CA."FechaPagoCorteCupon",
	CA."IdMonedaPagoCupon",						
	CA."NumeroCupon",
	COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
	COALESCE(EJE."CodigoEmpleado",''),
	COALESCE(EJE."NombreCompletoEjecutivo",''),
	COALESCE(EJE."CodigoSucursal",''),			
	CA."IdBoveda",
	CA."IdBolsa",
	CA."IdTipoReajuste",
	CA."MontoInteresCorteCupon",
	CA."IdTipoOperacion",
	CA."IdEstado",
	
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."MontoCapitalCorteCuponMonedaInstrumento"
	END,
	CASE 
	  WHEN :AgruparEventoCapitalCliente = 'N' THEN CA."MontoCorteCuponMonedaInstrumento"
	END,
	CA."FechaLiquidacion";

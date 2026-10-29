SET SCHEMA "BSCL";   
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI";   

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI" AS
WITH
	
	v_cartera_ingreso as (
	
		SELECT 
				cus."FolioCartera" AS "FolioCustodia",
				
				CUS."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'ICU' AS "Origen",
				CASE
					WHEN TIPINS."IdClasificacionTipoInstrumento" = 'FM'
        				THEN SOLFM."SecuenciaMovimiento"
					WHEN (TRANSITO."IdTipoOperacion"  IN ('INGTIT','RETTIT') and TRANSITO."PermiteCertificacionPorUsuario" = 'S' and TRANSITO."IdConcepto" NOT IN('VENPAC','VCDTIT','VCTSIC','VCTSIV') and TRANSITO."EsNominal" = 'N')
						THEN TRANSITO."SecuenciaMovimiento"
					WHEN TRANSITO."IdTipoOperacion"  IN ('COMTER','COMPAC','VENPAC','VENTER')
						THEN CASE SIDE."SIDE"
				            WHEN 'CLI' THEN RES."SecMovCliIngreso"
				            ELSE RES."SecMovCorIngreso"
				        END
					ELSE MOVTOFAC."SecuenciaMovimiento" 
				END AS "Folio",
				CUS."FechaIngresoRegistro",CUS."FechaOperacion",
				CASE 
					WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN CUS."FechaVencimientoInstrumento" <= '1900-01-01' THEN CUS."FechaVencimientoOperacion" ELSE CUS."FechaVencimientoInstrumento" END
					ELSE CUS."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				CUS."FechaLiquidacion" AS "FechaLiquidacion",CUS."PlazoEnDiasOperacion" as "Plazo",
				CUS."IdCondicionLiquidacion",CUS."IdEstado" AS "IdEstado",
				CASE
				    WHEN RES."IdReserva" IS NULL THEN CUS."IdCliente"
				    WHEN SIDE."SIDE" = 'CLI' THEN OPE."IdCliente"
				    ELSE PORCORR."IdCliente"
				END AS "IdCliente",
				CASE
				    WHEN RES."IdReserva" IS NULL THEN CUS."IdPortafolio"
				    WHEN SIDE."SIDE" = 'CLI' THEN OPE."IdPortafolio"
				    ELSE PORCORR."IdPortafolio"
				END AS "IdPortafolio",
				0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CASE
				    WHEN RES."IdReserva" IS NULL THEN CUS."INGIdTipoOperacion"
				    WHEN SIDE."SIDE" = 'CLI' AND OPE."IdTipoOperacion" = 'COMPAC' THEN 'VENPAC'
				    WHEN SIDE."SIDE" = 'CLI' AND OPE."IdTipoOperacion" = 'VENPAC' THEN 'COMPAC'
				    WHEN SIDE."SIDE" = 'CLI' AND OPE."IdTipoOperacion" = 'VENTER' THEN 'COMTER'
				    WHEN SIDE."SIDE" = 'CLI' AND OPE."IdTipoOperacion" = 'COMTER' THEN 'VENTER'
				    ELSE OPE."IdTipoOperacion"
				END AS "IdTipoOperacion",
				CUS."IdTipoProducto" AS "IdTipoProducto",CUS."IdConcepto",CUS."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(CUS."FolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."FolioComprobante" END AS "IdComprobante",
				CASE COALESCE(CUS."FolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."FolioComprobante" END as "FolioComprobante",CUS."IdMesa" AS "IdMesa",
				CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",CUS."IdMonedaEmision",CUS."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CASE 	
					WHEN CUS."Nemotecnico" IN ('SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA') AND TRA."NemotecnicoSubyacente" IS NOT NULL THEN TRA."NemotecnicoSubyacente"
					WHEN CUS."IdTipoOperacion" IN ('PRESTA') THEN SUB."Nemotecnico"
					ELSE CUS."Nemotecnico" END AS "Nemotecnico",
				CUS."IdBolsa" AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	CUS."Cantidad",CUS."ValorRescate",CUS."IdTipoReajuste",
				CASE WHEN CUS."Nemotecnico" IN ('SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA') THEN CUS."PrecioAPlazo" ELSE CUS."PrecioCompra" END AS "PrecioCompra",CUS."TasaCompra" AS "TasaCompra",CUS."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
			   	CASE WHEN CUS."Nemotecnico" IN ('SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA') THEN CUS."MontoAPlazo" ELSE CUS."MontoCompra" END AS "MontoCompra",
			   	
			   	CUS."INGPrecioMercadoActual" AS "PrecioMercado",CUS."CodigoIFRS" AS "CodigoIFRS",
			   		CASE CUS."IdTipoComprobante" WHEN 'FAE' THEN CUS."FolioExternoReferencia" ELSE '' END AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE
				    WHEN RES."IdReserva" IS NULL THEN ''
				    WHEN SIDE."SIDE" = 'CLI' AND LEFT(OPE."IdTipoOperacion", 3) = 'COM' THEN 'E'
				    WHEN SIDE."SIDE" = 'CLI' AND LEFT(OPE."IdTipoOperacion", 3) = 'VEN' THEN 'I'
				    WHEN SIDE."SIDE" = 'COR' AND LEFT(OPE."IdTipoOperacion", 3) = 'COM' THEN 'I'
				    WHEN SIDE."SIDE" = 'COR' AND LEFT(OPE."IdTipoOperacion", 3) = 'VEN' THEN 'E'
				    ELSE 'X'
				END AS "IngresoEgreso",
			   	'N' AS "Nominal", CASE WHEN CUS."IdConcepto" IN ('VNSIMU','CMSIMU') THEN ORDEN."IdOrden" ELSE 0 END AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte"

		FROM "BSCL.Custodia.BD::CUS.Cartera" CUS
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO ON TRANSITO."IdMovimientoCarteraEnTransito" = CUS."IdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON TRANSITO."IdOperacion" = RES."IdReserva" AND TRANSITO."IdTipoOperacion" IN ('COMTER','COMPAC','VENPAC','VENTER')
		LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = RES."IdOperacion"
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdMesa" = OPE."IdMesaOrigen"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORCORR ON PORCORR."IdPortafolio" = MES."IdPortafolio"
		INNER JOIN (
		    SELECT 'CLI' AS "SIDE" FROM DUMMY
		    UNION ALL
		    SELECT 'COR' AS "SIDE" FROM DUMMY
		) SIDE ON SIDE."SIDE" = 'CLI' OR RES."IdReserva" IS NOT NULL
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CASE WHEN RES."IdReserva" IS NULL THEN CUS."IdPortafolio" ELSE OPE."IdPortafolio" END
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = CUS."FolioExternoReferencia" AND TRA."FechaSistemaCreacion" = CUS."FechaOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOLFM ON SOLFM."IdSolicitudAporteRescate" = TRANSITO."IdOperacion" AND TIPINS."IdClasificacionTipoInstrumento" = 'FM'
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = 	CASE
																		            WHEN RES."IdReserva" IS NULL THEN CUS."IdCliente"
																		            WHEN SIDE."SIDE" = 'CLI' THEN OPE."IdCliente"
																		            ELSE PORCORR."IdCliente"
																		        END
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN (
			SELECT rank() OVER (PARTITION BY SUB."FolioSubyacente" ORDER BY SUB."IdSubyacente" asc) as ranking,"FolioCarteraRelacionada","Nemotecnico" 
			FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB ) 
				AS SUB ON SUB."FolioCarteraRelacionada" = CUS."FolioCartera" AND SUB.RANKING = 1

		WHERE CUS."IdCarga" = 0 AND CUS."EsPrimeraCartera" = 'S' AND TIPINS."IdTipoInstrumento" != 'FWD'
		AND CUS."Nemotecnico" NOT IN ('PACTO VENTA','PACTO COMPRA') and CUS."IdTipoOperacion" NOT IN ('EMPRES','VENEMP')
		and CUS."ValorRescate" <= 999999999999999
		AND (RES."IdReserva" IS NULL OR (
			RES."IdEstado" <> 'ANU'
	        AND OPE."IdOperacion" IS NOT NULL
	        AND OPE."IdTipoOperacion" IN ('COMTER','COMPAC','VENPAC','VENTER')
        	)
        )
	),
	v_cartera_retiro as (
	
		SELECT CUS."FolioCartera" AS "FolioCustodia",CUS."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'ECU' AS "Origen", 
				CASE
					WHEN (TRANSITO."IdTipoOperacion"  IN ('INGTIT','RETTIT') and TRANSITO."PermiteCertificacionPorUsuario" = 'S' and TRANSITO."IdConcepto" NOT IN('VENPAC','VCDTIT','VCTSIC','VCTSIV') and TRANSITO."EsNominal" = 'N')
						THEN TRANSITO."SecuenciaMovimiento"
					WHEN TRANSITO."IdTipoOperacion"  IN ('COMTER','COMPAC','VENPAC','VENTER')
						THEN CASE SIDE."SIDE"
				            WHEN 'CLI' THEN RES."SecMovCliLiquidacion"
				            ELSE RES."SecMovCorLiquidacion"
				        END
					ELSE MOVTOFAC."SecuenciaMovimiento" 
				END AS "Folio",
				CUS."FechaIngresoRegistro",CUS."EGRFechaOperacion" AS "FechaOperacion",
				--CUS."EGRFechaLiquidacion" AS "FechaVencimientoOperacion",
				CASE 
					WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN CUS."FechaVencimientoInstrumento" <= '1900-01-01' THEN CUS."FechaVencimientoOperacion" ELSE CUS."FechaVencimientoInstrumento" END
					ELSE CUS."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				CUS."EGRFechaLiquidacion" AS "FechaLiquidacion",CUS."PlazoEnDiasOperacion" as "Plazo",
				CUS."EGRIdCondicionLiquidacion" AS "IdCondicionLiquidacion",CUS."IdEstado" AS "IdEstado",
				CASE
				    WHEN RES."IdReserva" IS NULL THEN CUS."IdCliente"
				    WHEN SIDE."SIDE" = 'CLI' THEN OPE."IdCliente"
				    ELSE PORCORR."IdCliente"
				END AS "IdCliente",
				
				CASE
				    WHEN RES."IdReserva" IS NULL THEN CUS."IdPortafolio"
				    WHEN SIDE."SIDE" = 'CLI' THEN OPE."IdPortafolio"
				    ELSE PORCORR."IdPortafolio"
				END AS "IdPortafolio",
				0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CASE
				    WHEN RES."IdReserva" IS NULL THEN CUS."EGRIdTipoOperacion"			
				    WHEN SIDE."SIDE" = 'CLI' AND OPE."IdTipoOperacion" = 'COMPAC' THEN 'VENPAC'
				    WHEN SIDE."SIDE" = 'CLI' AND OPE."IdTipoOperacion" = 'VENPAC' THEN 'COMPAC'
				    ELSE OPE."IdTipoOperacion"
				END AS "IdTipoOperacion",
				CUS."IdTipoProducto" AS "IdTipoProducto",CUS."EGRIdConcepto" as "IdConcepto",CUS."EGRIdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(CUS."EGRFolioComprobante",0) WHEN 0 THEN 0 ELSE "EGRFolioComprobante" END AS "IdComprobante",
				CASE COALESCE(CUS."EGRFolioComprobante",0) WHEN 0 THEN 0 ELSE "EGRFolioComprobante" END as "FolioComprobante",CUS."IdMesa" AS "IdMesa",
				CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",CUS."IdMonedaEmision",CUS."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CUS."Nemotecnico",CUS."IdBolsa" AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	CUS."EGRCantidad" as "Cantidad",CUS."EGRValorRescate" as "ValorRescate",CUS."IdTipoReajuste",
			   	CUS."EGRPrecio" AS "PrecioCompra",CUS."TasaCompra" AS "TasaCompra",CUS."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
			   	CUS."EGRMontoMonedaInstrumento" AS "MontoCompra",CUS."INGPrecioMercadoActual" AS "PrecioMercado",CUS."CodigoIFRS" AS "CodigoIFRS",CUS."EGRFolioExternoReferencia" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CASE WHEN CUS."EGRIdConcepto" IN ('VNSIMU') THEN ORDEN."IdOrden" ELSE 0 END AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte"
				
		FROM "BSCL.Custodia.BD::CUS.Cartera" CUS
		INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO ON TRANSITO."IdMovimientoCarteraEnTransito" = CUS."EGRIdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON TRANSITO."IdOperacion" = RES."IdReserva" AND TRANSITO."IdTipoOperacion" IN ('COMTER','COMPAC','VENPAC','VENTER')
		LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = RES."IdOperacion"
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdMesa" = OPE."IdMesaOrigen"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORCORR ON PORCORR."IdPortafolio" = MES."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLICORR ON CLICORR."IdCliente" = PORCORR."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = CUS."EGRFolioExternoReferencia" AND TRA."FechaSistemaCreacion" = CUS."EGRFechaOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CUS."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		INNER JOIN (
		    SELECT 'CLI' AS "SIDE" FROM DUMMY
		    UNION ALL
		    SELECT 'COR' AS "SIDE" FROM DUMMY
		) SIDE ON SIDE."SIDE" = 'CLI' OR RES."IdReserva" IS NOT NULL
		WHERE TIPINS."IdClasificacionTipoInstrumento" != 'FM' and TIPINS."IdTipoInstrumento" != 'FWD'
		AND CUS."Nemotecnico" NOT IN ('PACTO VENTA','PACTO COMPRA','SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA')
		and CUS."EGRCantidad" > 0 and CUS."IdConcepto" NOT IN('CMSIMU')
		and CUS."EGRValorRescate" <= 999999999999999
		AND (RES."IdReserva" IS NULL OR (OPE."IdOperacion" IS NOT NULL AND OPE."IdEstado" IN ('LIQ','ANT')))
	),
	v_Facturacion as (
		SELECT 	"IdEmpresa",'' AS "EsIngresoManual",'FAC' AS"IdModulo",'MFAC' AS "Origen",COM."IdComprobante" AS "Folio",COM."FechaIngresoRegistro",'0001-01-01' AS "FechaOperacion",
				'0001-01-01' AS "FechaVencimientoOperacion",COM."FechaLiquidacion",COM."DiasPlazo" AS "Plazo",
				COM."IdCondicionLiquidacion",COM."IdEstadoComprobante" AS "IdEstado",COM."IdCliente",COM."IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				COM."IdTipoOperacion",COM."IdTipoProducto",COM."IdConcepto" AS "IdConcepto",COM."IdTipoComprobante",COM."IdComprobante",CAST(COM."FolioComprobante" AS DECIMAL) AS "FolioComprobante",
				0 AS "IdMesa",COM."IdClasificacionTipoInstrumento",COM."IdTipoInstrumento",0 AS "IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",'' AS "IdMonedaOperacion",
				'' AS "Nemotecnico",COM."IdBolsa",COM."IdBoveda",'' AS "EsFungible",COM."IdEmpleadoEjecutivoPortafolio" AS "CodigoAgente",'' AS "CodigoSucursal",
			   	COM."Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	"Precio" AS "PrecioCompra","Tasa" AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	COM."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",'' AS "EmitidoA", CBS."Comision", CBS."Gastos",
			   	''  AS "IngresoEgreso"
		FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
		LEFT JOIN (
			SELECT "IdComprobante", cast(SUM("COFI"+"COSI"+"COVA"+"COAI"+"COIF"+"CCOR") as decimal(26,8)) AS "Comision",SUM("GFOD"+"GFFA"+"GAFI") AS "Gastos"
			FROM (
				SELECT  "IdComprobante",
					--COMISIONES
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COFI' THEN "MontoCobro" END),0) AS "COFI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COSI' THEN "MontoCobro" END),0) AS "COSI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COVA' THEN "MontoCobro" END),0) AS "COVA",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COAI' THEN "MontoCobro" END),0) AS "COAI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COIF' THEN "MontoCobro" END),0) AS "COIF",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'CCOR' THEN "MontoCobro" END),0) AS "CCOR",
					--GASTOS
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GFOD' THEN "MontoCobro" END),0) AS "GFOD",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GFFA' THEN "MontoCobro" END),0) AS "GFFA",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GAFI' THEN "MontoCobro" END),0) AS "GAFI"
				FROM (
						SELECT "IdComprobante", "IdCobro", "MontoCobro"
						FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
				     )
				GROUP BY "IdComprobante"
				)
			GROUP BY "IdComprobante"
		) AS CBS ON CBS."IdComprobante" = COM."IdComprobante"	
	)
	
	
	
        SELECT
            "Folio"  AS "SEC_MOVTO", -- DECIMAL
            "FolioCustodia" AS "SEC_CAR_INVER", -- DECIMAL
             TO_DECIMAL(SINFO."Cantidad",17,6)   AS "CANTIDAD", -- DECIMAL
             
            TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD')   AS "FEC_INVER", -- CHAR(10)
            
            "Plazo"  AS "PLAZO", -- DECIMAL
            
             "PrecioCompra"  AS "PRECIO_PROM_COMP", -- DECIMAL
             "TasaCompra"  AS "TASA_PROM_COMP", -- DECIMAL
              "MontoCompra"  AS "VALOR_INVER", -- DECIMAL
              TO_VARCHAR(SINFO."FechaVencimientoOperacion", 'YYYY-MM-DD') 	  AS "FEC_VCTO_TP", -- CHAR(10)
              
               CASE
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'IC'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'AD'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'AV'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'EC'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CD'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CV'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'IC'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'AD'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'AV'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'EC'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CD'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CV'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'FAE' THEN 'FD'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'FEE' THEN 'FD'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'MFW' THEN 'FW'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'FFM' THEN 'FM'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'FOE' THEN 'FT'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'FOX' THEN 'FX'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'NCE' THEN 'ND'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'NEE' THEN 'ND'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'ANSIMV' THEN 'ND'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'ANSIMC' THEN 'ND'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'ANSIMC' THEN 'ND'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'MES' THEN 'MA'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' THEN 'MA'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCTSIC' THEN 'FD'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCTSIV' THEN 'FD'
            	WHEN SINFO."Origen" = 'IGT' THEN 'IT'
            	WHEN SINFO."Origen" = 'RGT' THEN 'ET'
            	WHEN SINFO."Origen" = 'FMM' THEN 'FM'
            	
              	WHEN SINFO."Origen" = 'TRA' AND SINFO."IdTipoComprobante" = 'FAE' THEN 'FD'
            	           	
            	WHEN SINFO."Origen" = 'ICU' AND SINFO."IdTipoComprobante" = 'FAE' THEN 'FD'
            	WHEN SINFO."Origen" = 'ICU' AND SINFO."IdTipoOperacion" IN ('INGTIT','SUSOSA') THEN 'IT'
            	WHEN SINFO."Origen" = 'ICU' AND SINFO."IdTipoOperacion" IN ('COMTER','VENTER','COMPAC','VENPAC') THEN 'MA'
            	
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoComprobante" = 'FAE' THEN 'FD'
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoComprobante" = 'NCE' THEN 'ND'
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('RETTIT') THEN 'ET'
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('COMTER','VENTER','COMPAC','VENPAC','VCDPAC') THEN 'MA'
            	
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA','COMRF','COMIIF') AND SINFO."IdEstado" = 'ANU' THEN 'FD'
	           	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA','VENRF','VENIIF') AND SINFO."IdEstado" = 'ANU' THEN 'FD'
            	
            	ELSE ''
            	END   AS "TIPO_COMP_ADJ", -- CHAR(2)
            	
	            COALESCE(CASE
	            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE','FOX') THEN SINFO."FolioComprobanteDTE"
	            	WHEN SINFO."Origen" = 'ICU' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE') THEN SINFO."FolioComprobanteDTE"
	            	WHEN SINFO."Origen" = 'TRA' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE') THEN SINFO."FolioComprobanteDTE"
	            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE') THEN SINFO."FolioComprobanteDTE"
	            	WHEN SINFO."Origen" = 'TRA' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE') THEN SINFO."FolioComprobanteDTE"
	
	            	WHEN SINFO."Origen" = 'CTA' and SINFO."FolioComprobante" != 0 THEN SINFO."FolioComprobante"
		            WHEN SINFO."Origen" = 'CTA' and (SINFO."FolioComprobante" = 0 OR SINFO."FolioComprobante" IS NULL) THEN SINFO."Folio"
		            
		            WHEN SINFO."FolioComprobante" != '0' THEN SINFO."FolioComprobante"
		            ELSE 0
	            
	            END,0)   AS "FOLIO_COMP_ADJ", -- DECIMAL
            
              0  AS "LIN_COMP_ADJ", -- DECIMAL
              
              COALESCE("IdOrden",0)  AS "SEC_ORDEN", -- DECIMAL
              0  AS "LIN_ORDEN", -- DECIMAL
              ''  AS "VTO_COMP_ADJ", -- CHAR(10)
              0  AS "PRECIO_PROM_CALC", -- DECIMAL
              0  AS "PARIDAD_MORIGEN", -- DECIMAL
              TO_DECIMAL(SINFO."MontoCompra",17, 2) AS "CAPTACION_MORIGEN", -- DECIMAL
              
              'CLP'  AS "MON_MORIGEN", -- CHAR(3)
              0  AS "COSTO_UF", -- DECIMAL
              'N'  AS "IND_FEC_APORTE", -- CHAR(1)
              0  AS "COSTO_UF_ANT", -- DECIMAL
              0  AS "CLAS_TRIB", -- INT
              ''  AS "IND_TRIB" -- CHAR(4)

     FROM (
	
		SELECT "FolioCustodia", cing."IdEmpresa",cing."EsIngresoManual",cing."IdModulo",cing."Origen",cing."Folio",cing."FechaIngresoRegistro",cing."FechaOperacion",
				cing."FechaVencimientoOperacion",cing."FechaLiquidacion",cing."Plazo",
				cing."IdCondicionLiquidacion",cing."IdEstado",cing."IdCliente",cing."IdPortafolio",cing."IdClienteCorredora",cing."IdPortafolioCorredora",
				cing."IdTipoOperacion",cing."IdTipoProducto",cing."IdConcepto",cing."IdTipoComprobante",cing."FolioComprobante",cing."IdMesa",
				cing."IdClasificacionTipoInstrumento",cing."IdTipoInstrumento",cing."IdInstrumento",cing."FechaEmisionInstrumento",cing."IdMonedaEmision",
				cing."IdMonedaOperacion",cing."IdEmisor",cing."Nemotecnico",cing."EsFungible",cing."IdBolsa",cing."IdBoveda",cing."CodigoAgente",cing."CodigoSucursal",
				cing."IdTipoReajuste",cing."Cantidad",cing."ValorRescate",
				cing."PrecioCompra",cing."TasaCompra",cing."TasaAgente",cing."TasaEmision",cing."MontoCompra",cing."PrecioMercado",cing."CodigoIFRS",
				cing."FolioTransaccion",cing."IdMedioDePago",cing."EmitidoA",
				CASE WHEN cing."IdComprobante" IS NOT NULL THEN ROUND(FAC."Comision" * cing."Cantidad" / FAC."Cantidad",4 ) ELSE cing."Comision" END AS "Comision",
				CASE WHEN cing."IdComprobante" IS NOT NULL THEN ROUND(FAC."Gastos" * cing."Cantidad" / FAC."Cantidad",4 ) ELSE cing."Gastos" END AS "Gastos",
				cing."IngresoEgreso",cing."IdComprobante",cing."Nominal",cing."IdOrden",cing."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE"
		FROM v_cartera_ingreso cing
		LEFT JOIN v_Facturacion FAC ON cing."IdComprobante" = FAC."IdComprobante" AND cing."IdTipoComprobante"  IN ('FAE','NCE','FEE')
		UNION
		SELECT "FolioCustodia", ret."IdEmpresa",ret."EsIngresoManual",ret."IdModulo",ret."Origen",ret."Folio",ret."FechaIngresoRegistro",ret."FechaOperacion",
				ret."FechaVencimientoOperacion",ret."FechaLiquidacion",ret."Plazo",
				ret."IdCondicionLiquidacion",ret."IdEstado",ret."IdCliente",ret."IdPortafolio",ret."IdClienteCorredora",ret."IdPortafolioCorredora",
				ret."IdTipoOperacion",ret."IdTipoProducto",ret."IdConcepto",ret."IdTipoComprobante",ret."FolioComprobante",ret."IdMesa",
				ret."IdClasificacionTipoInstrumento",ret."IdTipoInstrumento",ret."IdInstrumento",ret."FechaEmisionInstrumento",ret."IdMonedaEmision",
				ret."IdMonedaOperacion",ret."IdEmisor",ret."Nemotecnico",ret."EsFungible",ret."IdBolsa",ret."IdBoveda",ret."CodigoAgente",ret."CodigoSucursal",
				ret."IdTipoReajuste",ret."Cantidad",ret."ValorRescate",
				ret."PrecioCompra",ret."TasaCompra",ret."TasaAgente",ret."TasaEmision",ret."MontoCompra",ret."PrecioMercado",ret."CodigoIFRS",
				ret."FolioTransaccion",ret."IdMedioDePago",ret."EmitidoA",
				CASE WHEN ret."IdComprobante" IS NOT NULL THEN ROUND(FAC."Comision" * ret."Cantidad" / FAC."Cantidad",4 ) ELSE ret."Comision" END AS "Comision",
				CASE WHEN ret."IdComprobante" IS NOT NULL THEN ROUND(FAC."Gastos" * ret."Cantidad" / FAC."Cantidad",4 ) ELSE ret."Gastos" END AS "Gastos",
				ret."IngresoEgreso",ret."IdComprobante",ret."Nominal",ret."IdOrden",ret."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE"
		FROM v_cartera_retiro ret
		LEFT JOIN v_Facturacion FAC ON ret."IdComprobante" = FAC."IdComprobante" AND ret."IdTipoComprobante" IN ('FAE','NCE','FEE')
	) AS SINFO
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = SINFO."IdPortafolio"
	LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdPortafolio" = POR."IdPortafolio" AND MES."IdTipoProducto" = 'MERF' AND MES."PorDefecto" = 'S'
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SINFO."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PER ON PER."IdPersona" = CLI."IdPersona"
	
	LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr 
						ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                               AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                               AND hpr."ValorDestino" = SINFO."IdTipoInstrumento"   
                               AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET'))
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMPRESA ON SINFO."IdEmpresa" = EMPRESA."IdEmpresa"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EMPRESA."IdSocioDeNegocio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CORR ON CORR."IdPersona" = SOC."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PCORR ON PCORR."IdPersona" = CORR."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CODLIQ ON  CODLIQ."IdCondicionDeLiquidacion" = SINFO."IdCondicionLiquidacion"
	WHERE SINFO."Folio" IS NOT NULL

WITH READ ONLY;


GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
     
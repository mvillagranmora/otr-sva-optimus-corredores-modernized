SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" AS

	WITH
	v_ffmm as (
		SELECT FM."IdEmpresa",'N' AS "EsIngresoManual",'FMM' AS "IdModulo",'FMM' AS "Origen",FM."IdSolicitudAporteRescate" AS "Folio",FM."FechaEmision" AS "FechaIngresoRegistro",FM."FechaEmision" AS "FechaOperacion",
				FM."FechaLiquidacion" AS "FechaVencimientoOperacion",FM."FechaLiquidacion" AS "FechaLiquidacion",0 as "Plazo",
				'' AS "IdCondicionLiquidacion",FM."Estado" AS "IdEstado",FM."IdCliente",FM."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				FM."TipoSolicitud" AS "IdTipoOperacion",'INFM' AS "IdTipoProducto",FM."IdTipoMovimientoFondoMutuo" as "IdConcepto",'FMM' AS "IdTipoComprobante",FM."IdSolicitudAporteRescate" AS "IdComprobante",
				FM."IdSolicitudAporteRescate" AS "FolioComprobante",0 AS "IdMesa",
				'FM' AS "IdClasificacionTipoInstrumento",'FFM' AS "IdTipoInstrumento",SER."IdInstrumento" AS "IdInstrumento",FONDO."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",FM."IdMoneda" AS "IdMonedaEmision",FM."IdMoneda" AS "IdMonedaOperacion",
				FM."IdSerieFondo" AS "Nemotecnico",'' AS "IdBolsa",'COR' AS "IdBoveda",'' AS "EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",SUC."CodigoSucursal",
			   	"NumeroCuotas" AS "Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	FM."ValorCuota" AS "PrecioCompra",0 AS"TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	FM."Monto" AS "MontoCompra",FM."ValorCuota" AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	FM."IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN  FM."TipoSolicitud"= 'APO' THEN 'E' ELSE 'I' END   AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona"
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" FM
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = FM."IdSerieFondo"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON FONDO."IdFondo" = SER."IdFondo" 
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON FM."IdPortafolio" = POR."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = FM."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		
		
	),
	v_nominales as (
		SELECT  TRANSITO."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'TRA' AS "Origen",-10*TRANSITO."IdMovimientoCarteraEnTransito" AS "Folio",TRANSITO."FechaIngresoRegistro",
				TRANSITO."FechaOperacion" AS "FechaOperacion",
				CASE 
					WHEN TRANSITO."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN TRANSITO."FechaVencimientoInstrumento" <= '1900-01-01' THEN TRANSITO."FechaVencimientoOperacion" ELSE TRANSITO."FechaVencimientoInstrumento" END
					ELSE TRANSITO."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				TRANSITO."FechaLiquidacion" AS "FechaLiquidacion",TRANSITO."PlazoEnDiasOperacion" as "Plazo",
				TRANSITO."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",TRANSITO."IdEstado" AS "IdEstado",TRANSITO."IdCliente",TRANSITO."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				TRANSITO."IdTipoOperacion" AS "IdTipoOperacion",TRANSITO."IdTipoProducto" AS "IdTipoProducto",TRANSITO."IdConcepto" as "IdConcepto",
				TRANSITO."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(TRANSITO."FolioComprobante",0) WHEN 0 THEN 0 ELSE TRANSITO."FolioComprobante" END AS "IdComprobante",
				CASE COALESCE(TRANSITO."FolioComprobante",0) WHEN 0 THEN 0 ELSE TRANSITO."FolioComprobante" END as "FolioComprobante",TRANSITO."IdMesa" AS "IdMesa",
				TRANSITO."IdClasificacionTipoInstrumento",TRANSITO."IdTipoInstrumento",TRANSITO."IdInstrumento",TRANSITO."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",TRANSITO."IdMonedaEmision",TRANSITO."IdMonedaInstrumento" AS "IdMonedaOperacion",
				TRANSITO."Nemotecnico",TRANSITO."IdBolsa" AS "IdBolsa",TRANSITO."IdBoveda",TRANSITO."EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",'' as "CodigoSucursal",
			   	TRANSITO."Cantidad" as "Cantidad",TRANSITO."ValorRescate" as "ValorRescate",TRANSITO."IdTipoReajuste",
			   	TRANSITO."PrecioCompra" AS "PrecioCompra",TRANSITO."TasaCompra" AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	TRANSITO."MontoCompra" AS "MontoCompra",0 AS "PrecioMercado",TRANSITO."CodigoIFRS" AS "CodigoIFRS",TRANSITO."FolioExternoReferencia" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN LEFT(TRANSITO."IdTipoOperacion",3) = 'COM' THEN 'I' ELSE 'E' END AS "IngresoEgreso",TRANSITO."EsNominal" AS "Nominal",ORDEN."IdOrden" AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	CASE WHEN TRANSITO."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN TRANSITO."MontoCompra" ELSE 0 END AS "MontoPlazo"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = TRANSITO."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdOperacion"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = TRANSITO."FolioExternoReferencia" AND TRA."IdTipoOperacion" = TRANSITO."IdTipoOperacion" AND (TRA."FechaSistemaCreacion" = TRANSITO."FechaOperacion") --OR (TRA."Nemotecnico" = TRANSITO."Nemotecnico" AND TRA."Precio" = TRANSITO."PrecioCompra" AND TRA."Tasa" = TRANSITO."TasaCompra" AND TRA."IdTipoOperacion" = TRANSITO."IdTipoOperacion"))
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on TRANSITO."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = TRANSITO."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE (TRANSITO."IdTipoOperacion" IN ('COMACN','COMCFI','COMCFX','COMETN','COMETX','VENACN','VENCFI','VENETN','VENETX','VENCFX','COMRF','COMIIF','VENRF','VENIIF','RETTIT','INGTIT','COMTER','VENTER')
		and (TRANSITO."EsNominal" = 'S') OR (TRANSITO."EsNominal" = 'N' and TRANSITO."IdEstado" = 'PEN'))
	),
	v_cartera_ingreso as (
		SELECT
				CUS."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'ICU' AS "Origen",CUS."IdCartera" AS "Folio",CUS."FechaIngresoRegistro",CUS."INGFechaOperacion" AS "FechaOperacion",
				CASE 
					WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN CUS."FechaVencimientoInstrumento" <= '1900-01-01' THEN CUS."FechaVencimientoOperacion" ELSE CUS."FechaVencimientoInstrumento" END
					ELSE CUS."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				CUS."FechaLiquidacion" AS "FechaLiquidacion",CUS."PlazoEnDiasOperacion" as "Plazo",
				CUS."IdCondicionLiquidacion",
				CASE WHEN MOVTOFAC."IdEstadoFacturacion" = 'ANU' THEN 'ANU' ELSE CUS."IdEstado" END AS "IdEstado",
				CUS."IdCliente",CUS."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CUS."INGIdTipoOperacion" AS "IdTipoOperacion",CUS."IdTipoProducto" AS "IdTipoProducto",
				CUS."IdConcepto" AS "IdConcepto",
				CUS."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(CUS."FolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."FolioComprobante" END AS "IdComprobante",
				CASE COALESCE(CUS."FolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."FolioComprobante" END as "FolioComprobante",
				CUS."IdMesa" AS "IdMesa",
				CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",CUS."IdMonedaEmision",CUS."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CASE 	
					WHEN CUS."IdTipoOperacion" IN ('PRESTA') THEN SUB."Nemotecnico"
					--VITP
					WHEN CUS."IdTipoOperacion" IN ('VENSIM') THEN SUB."Nemotecnico"
					--CITP
					WHEN CUS."IdTipoOperacion" IN ('COMSIM') and CUS."Nemotecnico" = 'SIMULTÁNEA COMPRA' THEN SUB."Nemotecnico"
					ELSE CUS."Nemotecnico" END AS "Nemotecnico",
				CUS."IdBolsa" AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",
				COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	CUS."INGCantidad" as "Cantidad",CUS."ValorRescate", CASE CUS."IdClasificacionTipoInstrumento" WHEN 'RV' THEN INS."IdTipoReajuste" ELSE CUS."IdTipoReajuste" END AS "IdTipoReajuste",
				CASE WHEN CUS."IdTipoOperacion" IN ('VENSIM') THEN CUS."INGPrecioAPlazo"
					 WHEN CUS."IdTipoOperacion" IN ('COMSIM') and CUS."Nemotecnico" = 'SIMULTÁNEA COMPRA' THEN CUS."INGPrecioAPlazo"
					ELSE CUS."INGPrecioCompra" END AS "PrecioCompra",
				CUS."TasaCompra" AS "TasaCompra",CUS."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
				CASE WHEN CUS."IdTipoOperacion" IN ('VENSIM') THEN CUS."INGMontoAPlazo"
					 WHEN CUS."IdTipoOperacion" IN ('COMSIM') and CUS."Nemotecnico" = 'SIMULTÁNEA COMPRA' THEN CUS."INGMontoAPlazo"
					ELSE CUS."MontoCompra" END AS "MontoCompra",
			   	CUS."INGPrecioMercadoActual" AS "PrecioMercado",CUS."CodigoIFRS" AS "CodigoIFRS",
			   		CASE CUS."IdTipoComprobante" WHEN 'FAE' THEN CUS."FolioExternoReferencia" ELSE '' END AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal", CASE WHEN CUS."IdConcepto" IN ('VNSIMU','CMSIMU', 'CMACEX', 'CMACMI', 'CMACNA', 'CMCFEX', 'CMCFIN', 'CMCFMN', 'CMDVFU', 'CMETFN', 'CMETFX', 'CMINFI', 'CMINRF', 'CMMONE', 'CMOSAC', 'COMIFI', 'COMIRF', 'COMIRV') THEN ORDEN."IdOrden" ELSE 0 END AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	CASE WHEN CUS."IdTipoOperacion" IN ('VENSIM') THEN CUS."INGMontoAPlazo"
					 WHEN CUS."IdTipoOperacion" IN ('COMSIM') and CUS."Nemotecnico" = 'SIMULTÁNEA COMPRA' THEN CUS."INGMontoAPlazo"
					 WHEN CUS."IdTipoOperacion" IN ('INGTIT') THEN 0
					ELSE CUS."MontoCompra" END AS "MontoPlazo"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CUS
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO ON TRANSITO."IdMovimientoCarteraEnTransito" = CUS."IdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = CUS."FolioExternoReferencia" AND TRA."FechaSistemaCreacion" = CUS."FechaOperacion"
			AND TRA."IdTipoOperacion" = CUS."IdTipoOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CUS."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CUS."FolioCartera" AND SUB."EsPrimeraCartera" = 'S'
		WHERE CUS."IdCarga" = 0 and (CUS."EsPrimeraCartera" = 'S')
		 AND CUS."IdClasificacionTipoInstrumento" != 'FM' and CUS."IdTipoInstrumento" != 'FWD'
		AND CUS."Nemotecnico" NOT IN ('PACTO VENTA','PACTO COMPRA') and CUS."IdTipoOperacion" NOT IN ('EMPRES','VENEMP')
		and CUS."ValorRescate" <= 999999999999999
		and not (CUS."INGIdConcepto" = 'VNSIMU' and CUS."Nemotecnico" != 'SIMULTÁNEA VENTA') --para quitar vencimientos de compra a plazo de ICU
		and not (CUS."INGIdConcepto" = 'ANTSIV' and CUS."Nemotecnico" != 'SIMULTÁNEA VENTA') --para quitar anticipos de compra a plazo de ICU
	),
	v_cartera_retiro as (
	
		SELECT CUS."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'ECU' AS "Origen",COALESCE(conf."Valor",-1)*CUS."IdCartera" AS "Folio",CUS."FechaIngresoRegistro",CUS."EGRFechaOperacion" AS "FechaOperacion",
				--CUS."EGRFechaLiquidacion" AS "FechaVencimientoOperacion",
				CASE 
					WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN CUS."FechaVencimientoInstrumento" <= '1900-01-01' THEN CUS."FechaVencimientoOperacion" ELSE CUS."FechaVencimientoInstrumento" END
					ELSE CUS."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				CUS."EGRFechaLiquidacion" AS "FechaLiquidacion",CUS."PlazoEnDiasOperacion" as "Plazo",
				CUS."EGRIdCondicionLiquidacion" AS "IdCondicionLiquidacion",
				CASE WHEN MOVTOFAC."IdEstadoFacturacion" = 'ANU' THEN 'ANU'
				ELSE CUS."IdEstado" END AS "IdEstado",
				CUS."IdCliente",CUS."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CUS."EGRIdTipoOperacion" AS "IdTipoOperacion",CUS."IdTipoProducto" AS "IdTipoProducto",
				CASE 
				 WHEN CUS."EGRIdConcepto" = 'VNSIMU' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN 'VCTSIC'
				 WHEN CUS."EGRIdConcepto" = 'ANTSIV' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN 'ANTSIV'
				 ELSE CUS."EGRIdConcepto" END AS "IdConcepto",
				CUS."EGRIdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(CUS."EGRFolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."EGRFolioComprobante" END AS "IdComprobante",
				CASE COALESCE(CUS."EGRFolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."EGRFolioComprobante" END as "FolioComprobante",CUS."IdMesa" AS "IdMesa",
				CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",CUS."IdMonedaEmision",CUS."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CASE 	
					WHEN CUS."EGRIdConcepto" = 'VNSIMU' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN SUB."Nemotecnico"
					WHEN CUS."EGRIdConcepto" = 'ANTSIV' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN SUB."Nemotecnico"
					WHEN CUS."EGRIdConcepto" IN ('VCTSIV') THEN SUB."Nemotecnico"
					WHEN CUS."EGRIdConcepto" IN ('ANTSIC') THEN SUB."Nemotecnico"
					ELSE CUS."Nemotecnico" END AS "Nemotecnico"
					,CUS."IdBolsa" AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	CUS."EGRCantidad" as "Cantidad",CUS."EGRValorRescate" as "ValorRescate", CASE CUS."IdClasificacionTipoInstrumento" WHEN 'RV' THEN INS."IdTipoReajuste" ELSE CUS."IdTipoReajuste" END AS "IdTipoReajuste",
			   	CASE 
			   	 WHEN CUS."EGRIdConcepto" IN ('ANTSIC') THEN CUS."PrecioAPlazo" 
			   	 WHEN CUS."EGRIdConcepto" = 'VNSIMU' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN CUS."PrecioAPlazo" 
			   	 WHEN CUS."EGRIdConcepto" = 'ANTSIV' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN CUS."PrecioAPlazo" 
			   	 ELSE CUS."EGRPrecio" END AS "PrecioCompra",
			   	CUS."TasaCompra" AS "TasaCompra",CUS."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
			   	CASE 
			   	  WHEN CUS."EGRIdConcepto" IN ('ANTSIC') THEN CUS."EGRMontoARecibirAnticipo" 
			   	  WHEN CUS."EGRIdConcepto" = 'VNSIMU' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN ROUND(CUS."PrecioAPlazo"*CUS."EGRCantidad",0,ROUND_HALF_UP)
			   	  WHEN CUS."EGRIdConcepto" = 'ANTSIV' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN CUS."EGRMontoARecibirAnticipo"
			   	  WHEN CUS."EGRIdConcepto" IN ('VCTSIV') THEN ROUND(CUS."PrecioAPlazo"*CUS."EGRCantidad",0,ROUND_HALF_UP)
			   	  ELSE CUS."EGRMontoMonedaInstrumento" END AS "MontoCompra",
			   	CUS."INGPrecioMercadoActual" AS "PrecioMercado",CUS."CodigoIFRS" AS "CodigoIFRS",CUS."EGRFolioExternoReferencia" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",
			   	CASE WHEN CUS."EGRIdConcepto" IN ('VENIFI', 'VENIRF', 'VENIRV', 'VNACEX', 'VNACMI', 'VNACNA', 'VNCFEX', 'VNCFIN', 'VNCFMN', 'VNDVFU', 'VNETFN', 'VNETFX', 'VNINFI', 'VNINRF', 'VNMONE', 'VNOSAC', 'VNSIMU')  THEN ORDEN."IdOrden" ELSE 0 END AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	CASE
			   	 WHEN CUS."EGRIdConcepto" IN ('ANTSIC') THEN ROUND(CUS."PrecioAPlazo"*CUS."EGRCantidad",0,ROUND_HALF_UP)
			   	 WHEN CUS."EGRIdConcepto" = 'VNSIMU' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN ROUND(CUS."PrecioAPlazo"*CUS."EGRCantidad",0,ROUND_HALF_UP)
			   	 WHEN CUS."EGRIdConcepto" = 'ANTSIV' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' THEN ROUND(CUS."PrecioAPlazo"*CUS."EGRCantidad",0,ROUND_HALF_UP)
			   	 WHEN CUS."EGRIdConcepto" IN ('VCTSIV') THEN ROUND(CUS."PrecioAPlazo"*CUS."EGRCantidad",0,ROUND_HALF_UP)
			   	 ELSE 0 END AS "MontoPlazo"
			   	 --TCTP VENC COMPRA A PLAZO : CUS."EGRIdConcepto" = 'VNSIMU' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' => VCTSIC
				 --NCTP ANT COMPRA A PLAZO : CUS."EGRIdConcepto" = 'ANTSIV' AND CUS."Nemotecnico" = 'SIMULTÁNEA VENTA' => ANTSIV
		FROM "BSCL.Custodia.BD::CUS.Cartera" CUS
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO ON TRANSITO."IdMovimientoCarteraEnTransito" = CUS."EGRIdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = CUS."EGRFolioExternoReferencia" AND TRA."FechaSistemaCreacion" = CUS."EGRFechaOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CUS."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CUS."FolioCartera" AND SUB."EsPrimeraCartera" = 'S'
		LEFT JOIN "BSCL.Replica.BD::ConfiguracionReplica" conf on conf. "CodigoParametro" = 'TBCTCMVTO_OffsetCarteraRetiro'
		WHERE CUS."IdClasificacionTipoInstrumento" != 'FM' and CUS."IdTipoInstrumento" != 'FWD'
		AND CUS."Nemotecnico" NOT IN ('PACTO VENTA','PACTO COMPRA')
		and CUS."EGRCantidad" > 0 
		--ESTO ES PARA FILTRAR LOS INGRESOS AUTOMATICOS A GARANTIA DE COMSIM
		AND NOT (CUS."Nemotecnico" != 'SIMULTÁNEA COMPRA' AND CUS."EGRIdConcepto" = 'CMSIMU')
		AND NOT (CUS."Nemotecnico" != 'SIMULTÁNEA VENTA' AND CUS."EGRIdConcepto" = 'ANTSIV')
		AND NOT (CUS."EGRIdTipoOperacion" IN ('INGGTA','RETGTA'))
		and CUS."EGRValorRescate" <= 999999999999999
	),
	v_garantia_ingreso as (
	
		SELECT * FROM (SELECT rank() OVER (PARTITION BY "FolioGarantia" ORDER BY "IdGarantia" asc) AS RANKING,				
				GTA."IdEmpresa",'N' AS "EsIngresoManual",'GTA' AS "IdModulo",'IGT' AS "Origen",GTA."IdGarantia" AS "Folio",GTA."FechaIngresoRegistro",GTA."FechaOperacion",
				--GTA."FechaLiquidacion" AS "FechaVencimientoOperacion",
				CASE 
					WHEN GTA."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN GTA."FechaVencimientoInstrumento" <= '1900-01-01' THEN GTA."FechaVencimientoOperacion" ELSE GTA."FechaVencimientoInstrumento" END
					ELSE 
						CASE WHEN GTA."FechaVencimientoOperacion" <= '1900-01-01' THEN GTA."FechaLiquidacion" ELSE GTA."FechaVencimientoOperacion" END
				END	AS "FechaVencimientoOperacion",
				GTA."FechaLiquidacion" AS "FechaLiquidacion",0 as "Plazo",
				GTA."IdCondicionLiquidacion",GTA."IdEstado" AS "IdEstado",GTA."IdCliente",GTA."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				'INGGTA' AS "IdTipoOperacion",'' AS "IdTipoProducto",GTA."IdConcepto",'CUS' AS "IdTipoComprobante",GTA."FolioComprobante" AS "IdComprobante",
				GTA."FolioComprobante" as "FolioComprobante",0 AS "IdMesa",
				GTA."IdClasificacionTipoInstrumento",GTA."IdTipoInstrumento",GTA."IdInstrumento",GTA."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",GTA."IdMonedaEmision",GTA."IdMonedaInstrumento" AS "IdMonedaOperacion",
				GTA."Nemotecnico",'' AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	GTA."Cantidad",GTA."ValorRescate",CUS."IdTipoReajuste",
			   	GTA."PrecioGarantia" AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	GTA."MontoGarantia" AS "MontoCompra",CUS."INGPrecioMercadoActual" AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GTA
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CUS on CUS."IdCartera" = GTA."IdCarteraOrigen"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on GTA."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = GTA."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = GTA."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = por."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE GTA."IdCarga" = 0 and GTA."IdOrigenGarantia" ='CUS'
		) WHERE RANKING = 1
		
	),
	v_garantia_retiro as (
	
		SELECT	GTA."IdEmpresa",'N' AS "EsIngresoManual",'GTA' AS "IdModulo",'RGT' AS "Origen", -1*GTA."IdGarantia" AS "Folio",GTA."FechaIngresoRegistro",GTA."EGRFechaOperacion" AS "FechaOperacion",
				--GTA."EGRFechaLiquidacion" AS "FechaVencimientoOperacion",
				CASE 
					WHEN GTA."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN GTA."FechaVencimientoInstrumento" <= '1900-01-01' THEN GTA."FechaVencimientoOperacion" ELSE GTA."FechaVencimientoInstrumento" END
					ELSE 
						CASE WHEN GTA."FechaVencimientoOperacion" <= '1900-01-01' THEN GTA."EGRFechaLiquidacion" ELSE GTA."FechaVencimientoOperacion" END
				END	AS "FechaVencimientoOperacion",
				GTA."EGRFechaLiquidacion" AS "FechaLiquidacion",0 as "Plazo",
				GTA."EGRIdCondicionLiquidacion" AS "IdCondicionLiquidacion",GTA."IdEstado" AS "IdEstado",GTA."IdCliente",GTA."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				'RETGTA' AS "IdTipoOperacion",'' AS "IdTipoProducto",GTA."EGRIdConcepto" AS "IdConcepto",'CUS' AS "IdTipoComprobante",GTA."EGRFolioComprobante" AS "IdComprobante",
				GTA."EGRFolioComprobante" as "FolioComprobante",0 AS "IdMesa",
				GTA."IdClasificacionTipoInstrumento",GTA."IdTipoInstrumento",GTA."IdInstrumento",GTA."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",GTA."IdMonedaEmision",GTA."IdMonedaInstrumento" AS "IdMonedaOperacion",
				GTA."Nemotecnico",'' AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	GTA."EGRCantidad" AS "Cantidad",GTA."EGRValorRescate" as "ValorRescate",CUS."IdTipoReajuste",
			   	GTA."EGRPrecio" AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	GTA."EGRMontoMonedaInstrumento" AS "MontoCompra",CUS."INGPrecioMercadoActual" AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona"
		FROM "BSCL.Custodia.BD::GTA.Garantia" GTA
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CUS on CUS."IdCartera" = GTA."IdCarteraOrigen"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on GTA."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = GTA."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = GTA."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = por."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE GTA."IdCarga" = 0 AND GTA."EGRCantidad" > 0  and GTA."IdOrigenGarantia" ='CUS'
		
	),
	v_CuentaInversion as (
		SELECT 	MOV."IdEmpresa","EsIngresoManual",MOV."IdModulo",'CTA' AS "Origen",CASE WHEN MOV."IdModulo" = 'TES' THEN MOV."IdComprobante" ELSE MOV."IdCargoAbonoMovimiento" END AS "Folio",MOV."FechaIngresoRegistro",MOV."FechaSistema" AS "FechaOperacion",
				CASE WHEN FAC."IdComprobante" IS NOT NULL AND FAC."IdClasificacionTipoInstrumento" = 'IF' THEN FAC."FechaVencimiento" ELSE MOV."FechaEfectivaMovimiento" END AS "FechaVencimientoOperacion",MOV."FechaEfectivaMovimiento" AS "FechaLiquidacion",0 AS "Plazo",
				MOV."IdCondicionDeLiquidacion" AS "IdCondicionLiquidacion",MOV."IdEstado",CI."IdCliente" AS "IdCliente",CI."IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				MOV."IdTipoOperacion",MOV."IdTipoProducto",MOV."IdConcepto",MOV."IdTipoComprobante",MOV."IdComprobante",
				FAC."FolioComprobante" AS "FolioComprobante",0 AS "IdMesa",
				TIPINS."IdClasificacionTipoInstrumento",TIPINS."IdTipoInstrumento",MOV."IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",MOV."IdMonedaOperacion" AS "IdMonedaEmision",
				--EN SEBRA TODO VA A PESO para SPOT
				--CASE WHEN CI."IdMoneda" = 'USD' AND MOV."Nemo" = 'DISTRI' AND MOV."IdTipoComprobante" = 'FEE' AND MOV."IdModulo" = 'FAC' THEN 'CLP' ELSE CI."IdMoneda" END AS "IdMonedaOperacion",
				CI."IdMoneda" AS "IdMonedaOperacion",
				CASE 
				 WHEN MOV."IdConcepto" IN ('VCTSIC','VCTSIV','ANTSIV','ANTSIC') THEN SUB."Nemotecnico"
				 WHEN CI."IdMoneda" = 'EUR' AND MOV."Nemo" = 'DISTRI' THEN ''
				 WHEN CI."IdMoneda" = 'USD' AND MOV."Nemo" IN ('DISTRI','INTERB') THEN 'DOLAR'
				 WHEN CI."IdMoneda" = 'CLP' AND MOV."Nemo" IN ('DISTRI','INTERB') THEN 'DOLAR'
				 WHEN CI."IdMoneda" = 'GBP' AND MOV."Nemo" IN ('DISTRI','INTERB') THEN 'LIBRA'
				 WHEN CI."IdMoneda" = 'JPY' AND MOV."Nemo" IN ('DISTRI','INTERB') THEN 'YEN'
				 WHEN CI."IdMoneda" = 'CHF' AND MOV."Nemo" IN ('DISTRI','INTERB') THEN 'FRANCOS'
				 WHEN CI."IdMoneda" = 'NOK' AND MOV."Nemo" IN ('DISTRI','INTERB') THEN 'CORONA'
 				 WHEN CI."IdMoneda" = 'CAD' AND MOV."Nemo" IN ('DISTRI','INTERB','ARBDIS') THEN 'CAD'
 				 WHEN CI."IdMoneda" = 'AUD' AND MOV."Nemo" IN ('DISTRI','INTERB','ARBDIS') THEN 'AUD'
				 
				 ELSE INS."Nemotecnico" END AS "Nemotecnico",
				FAC."IdBolsa",'' AS "IdBoveda",'' AS "EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",SUC."CodigoSucursal",
			   	CASE 
			   	 WHEN CI."IdMoneda" = 'EUR' AND MOV."Nemo" = 'DISTRI' THEN 0
			   	 WHEN FAC."IdComprobante" is not null THEN FAC."Cantidad" 
			   	 WHEN SOL."IdSolicitudAporteRescate" is not null THEN SOL."NumeroCuotas" 
 			   	 WHEN EVC."IdEventoCapitalCliente" is not null THEN EVC."CantidadDerecho" 
 			   	 WHEN CARVENC."IdCartera" is not null and CARVENC."Nemotecnico" = 'PDBC' THEN CARVENC."EGRValorRescate"/5000000	   	 
  			   	 WHEN CARVENC."IdCartera" is not null THEN CARVENC."Cantidad"
 	 			 ELSE MOV."Cantidad" 
				END AS "Cantidad",
			   	0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	CASE 
				  WHEN CI."IdMoneda" = 'EUR' AND MOV."Nemo" = 'DISTRI' THEN 0
				  WHEN CARVENC."IdCartera" is not null AND MOV."IdConcepto" IN ('VCTSIC','VCTSIV') THEN CARVENC."PrecioAPlazo"
				  WHEN SOL."IdSolicitudAporteRescate" is not null THEN SOL."ValorCuotaInversion" 
				  WHEN FAC."IdComprobante" IS NOT NULL THEN FAC."Precio"
				  ELSE 0 END AS "PrecioCompra",
			   	0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	--CASE WHEN CI."IdMoneda" = 'USD' AND MOV."Nemo" = 'DISTRI' THEN MOV."MontoOperacion" ELSE MOV."Monto" END AS "MontoCompra",
				MOV."Monto" AS "MontoCompra",
			   	0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",MOV."NombreCliente" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN  MOV."IdTipoMovimiento"= 'MOVABN' THEN 'I' ELSE 'E' END   AS "IngresoEgreso",'N' AS "Nominal",
			   	COALESCE(EVC."Secuencia",0) AS "SecuenciaEECC",CLI."IdPersona",
			   	CASE WHEN MOV."IdConcepto" IN ('VCDPAC') THEN MOV."Monto"
			   	     WHEN FAC."IdComprobante" IS NOT NULL AND FAC."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN MOV."Monto"
			   	     WHEN MOV."IdConcepto" IN ('VCTSIC','VCTSIV') THEN MOV."Monto"
			   	     WHEN MOV."IdConcepto" IN ('ANTSIC') THEN MOV."Monto"
			   	     WHEN MOV."IdConcepto" IN ('ANTSIV') THEN MOV."Monto"
			   	ELSE 0 END AS "MontoPlazo"
		FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS MOV
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FAC on MOV."IdComprobante" = FAC."IdComprobante" AND MOV."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE','FOX')
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL on SOL."IdSolicitudAporteRescate" = MOV."IdComprobante" AND MOV."IdTipoComprobante" IN ('FFM')
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVC ON EVC."IdEventoCapitalCliente" = MOV."IdOperacion" AND MOV."IdConcepto" IN ('CIECDV','REPDIN')
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARVENC ON MOV."IdComprobante" = CARVENC."IdCartera" AND MOV."IdConcepto" IN ('VCTSIC','VCTSIV','VCDTIT')
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on MOV."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = COALESCE(INS."IdTipoInstrumento",MOV."IdTipoInstrumento")
		LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON CI."IdCuentaInversion" = MOV."IdCuentaInversion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = CI."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = MOV."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON CAST(SUB."FolioCarteraRelacionada" AS NVARCHAR) = MOV."FolioComprobante" 
			AND MOV."IdConcepto" IN ('VCTSIC','VCTSIV','ANTSIV','ANTSIC') AND SUB."IdEstado" IN ('ANT','VCD')
			--FILTROS MOVIMIENTOS DE SPOT DESDE TESORERIA YA QUE EN SEBRA ESTOS NO EXISTEN
		--WHERE NOT (MOV."IdModulo" = 'TES' AND MOV."IdConcepto" IN ('CMMESP','VNMESP'))
		--WHERE NOT (MOV."IdModulo" = 'FAC' AND MOV."IdConcepto" IN ('CMMESP','VNMESP') AND CI."IdMoneda" = 'USD')
	),
	v_CmbtesTes as (
		SELECT 	CMBTE."IdEmpresa",'N' AS "EsIngresoManual",'TES' AS "IdModulo",'TES' AS "Origen","IdComprobante" AS "Folio",CMBTE."FechaIngresoRegistro",CMBTE."FechaMovimiento" AS "FechaOperacion",
				CMBTE."FechaLiquidacion" AS "FechaVencimientoOperacion",CMBTE."FechaLiquidacion" AS "FechaLiquidacion",0 AS "Plazo",
				'T0' AS "IdCondicionLiquidacion",CMBTE."IdEstado",0 AS "IdCliente",0 AS "IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CMBTE."IdTipoMovimiento" AS "IdTipoOperacion",'' AS "IdTipoProducto",CMBTE."IdConcepto",'' AS "IdTipoComprobante",CMBTE."IdComprobante",
				"IdComprobante" AS "FolioComprobante",0 AS "IdMesa",
				'' AS "IdClasificacionTipoInstrumento",'' AS "IdTipoInstrumento", 0 AS "IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",CMBTE."IdMoneda" AS "IdMonedaOperacion",
				'' AS "Nemotecnico",'' AS "IdBolsa",'' AS "IdBoveda",'' AS "EsFungible",'' AS "CodigoAgente",'' AS "CodigoSucursal",
			   	0 AS "Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	0 AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	CMBTE."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",CMBTE."NombrePersona" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN  CMBTE."IdTipoMovimiento"= 'MOVING' THEN 'I' ELSE 'E' END   AS "IngresoEgreso",'N' AS "Nominal",0 AS "SecuenciaEECC","IdPersona"
		FROM  "BSCL.Tesoreria.BD::TES.Comprobante" AS CMBTE
		WHERE CMBTE."IdTipoPersona" IN ('CNT','PER','PRO')
		
	),
	v_comisiones as (
		select COMP."IdComprobante","IdBolsa","Precio","FolioComprobante","Cantidad",
		COALESCE(MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN cob."MontoCobro" END),0) as "Comision",
		COALESCE(MAX(CASE WHEN "CodigoCobroClasificacion" IN ('IMPUESTO','GASTO') THEN cob."MontoCobro" END),0) as "Gastos",
		comp."MontoComprobante" AS "Monto"
		from "BSCL.Facturacion.BD::FAC.Comprobante" comp
		left join (
			SELECT "IdComprobante", CLASIF."CodigoCobroClasificacion" , SUM("MontoComprobanteCobro") AS "MontoCobro"
			FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" COMCOBRO
			LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" COBRO ON COBRO."IdCobro" = COMCOBRO."IdCobro" 
			LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" CLASIF ON COBRO."IdCobroClasificacion" = CLASIF."IdCobroClasificacion" 
			group by "IdComprobante", CLASIF."CodigoCobroClasificacion"
		) as cob on cob."IdComprobante" = comp."IdComprobante"
		group by comp."IdComprobante","IdBolsa","Precio","FolioComprobante","Cantidad","MontoComprobante"
	)
	
        SELECT
	        SINFO."Origen",
        	SINFO."IdTipoOperacion",
        	sinfo."IdConcepto",
        	CORR."IdCliente" as cuentapropia,
			SINFO."IdComprobante",
			SINFO."Folio",
       		SINFO."IdCliente",
        	SINFO."IngresoEgreso",
        	SINFO."IdModulo",
        	SINFO."IdTipoComprobante",
        	SINFO."IdClasificacionTipoInstrumento",
        	SINFO."EsIngresoManual",
        	SINFO."Nominal",
        	SINFO."IdMonedaOperacion",
        	SINFO."Nemotecnico",
        	
            CASE 
            	WHEN SINFO."Origen" = 'CTA' THEN ' ' 
            	WHEN SINFO."Origen" = 'TES' THEN ' ' 
            	WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" = 'VENSIM' THEN 'E'
            	WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" = 'COMSIM' AND SINFO."IdTipoInstrumento" = 'SIM' THEN 'S'            	
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdConcepto" = 'VCTSIC' THEN 'S'
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdConcepto" = 'ANTSIV' THEN 'S'
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdConcepto" IN ('VCTSIV','ANTSIC') THEN 'E'
            	ELSE '' END AS    "IND_CUST_APLAZO", -- CHAR(1)
			CASE 
				WHEN SINFO."Origen" in ('IGT','ICU','ECU') AND SINFO."IdEstado" != 'ANU' THEN 'I'
				WHEN SINFO."Origen" in ('FMM') AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdEstado" ='CRT' THEN 'I'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."IdEstado" ='ANU' THEN 'A'
				ELSE 'I'
			END AS "IND_ANULADO", -- CHAR(1)
			  
            CASE 
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdEstado" ='CRT' THEN 'T'
				WHEN SINFO."Origen" IN ('IGT','RGT','ICU','ECU') AND SINFO."IdEstado" != 'ANU' THEN 'T'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" ='LIQ' THEN 'T'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" NOT IN ('LIQ','ING') THEN 'I'
				WHEN SINFO."Origen" = 'TRA' AND SINFO."IdEstado" ='PEN' THEN 'I'
				WHEN SINFO."Origen" = 'TRA' AND SINFO."IdEstado" ='CRT' THEN 'T'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdEstado" ='CRT' THEN 'T'
				WHEN SINFO."IdEstado" ='ANU' THEN 'A'
				ELSE 'I'
			 END AS "EST_MOVTO", -- CHAR(1) 
            
            TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD')  AS "FEC_MVTO", -- CHAR(10)
            
            "Folio"  AS "SEC_MOVTO", -- DECIMAL
            CASE 
            	WHEN SINFO."Origen" = 'CTA' THEN 'S' 
            	WHEN SINFO."Origen" = 'TES' THEN 'S' 
				ELSE 'N'
			END AS "IND_SALDO_CTDO", -- CHAR(1)
            
           CASE 
				WHEN SINFO."Origen" in ('CTA') AND SINFO."IdTipoComprobante" = 'MES' AND "IdTipoOperacion" IN ('COMPAC','VENPAC') THEN  'P'
				WHEN SINFO."Origen" in ('CTA') AND SINFO."IdConcepto" IN ('VCTSIV','VCTSIC') THEN  'T'
				ELSE 'N'
			END AS "IND_SALDO_APLAZO", -- CHAR(1) indicador de operacione a plazo N contado / P Plazo / Contado pa facturas
            
           CASE 
           		WHEN SINFO."Origen" = 'CTA' THEN ' '  
           		WHEN SINFO."Origen" = 'TES' THEN ' '  
           		WHEN SINFO."Origen" IN ('IGT','RGT') THEN 'N'
           		WHEN SINFO."Origen" IN ('ICU') THEN 'E'
           		WHEN SINFO."Origen" = 'ECU' AND SINFO."IdConcepto" = 'ANTSIC' THEN 'S'
           		WHEN SINFO."Origen" = 'ECU' AND SINFO."IdConcepto" = 'VCTSIV' THEN 'S'
           		WHEN SINFO."Origen" = 'ECU' THEN 'E'
           		WHEN "IngresoEgreso" = 'E' THEN 'E' 
           		WHEN "IngresoEgreso" = 'I' THEN 'S' 
           		ELSE '' END   AS "IND_CUST_CTDO", -- CHAR(1)
            
            "BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PER."DocumentoIdentidad")  AS "RUT_CLI", -- CHAR(11)
            
            COALESCE (POR."CodigoPortafolio" ,COALESCE(CONF."Valor", '-1'))  AS "SEC_RUT_CLI", -- DECIMAL(2,0)
            
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
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'FEE' THEN 'FX'
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
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoComprobante" = 'NCR' THEN 'ND'
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('RETTIT') THEN 'ET'
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('COMTER','VENTER','COMPAC','VENPAC','VCDPAC') THEN 'MA'
            	
            	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA','COMRF','COMIIF') AND SINFO."IdEstado" = 'ANU' THEN 'FD'
	           	WHEN SINFO."Origen" = 'ECU' AND SINFO."IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA','VENRF','VENIIF') AND SINFO."IdEstado" = 'ANU' THEN 'FD'
            	
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'IC'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'AD'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'AV'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'EC'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CD'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CV'
            	
            	ELSE ''
            END AS "TIPO_COMP_ADJ", -- CHAR(2) 
            
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
            
            END,0) AS "FOLIO_COMP_ADJ", -- DECIMAL
            
            CASE WHEN  SINFO."Origen" = 'TRANSITO'  then "Folio"  ELSE  0  END  AS "SEC_TRASPASO", -- DECIMAL
            
           CASE WHEN SINFO."Origen" IN ('FMM','IGT','ICU','ECU','TRA') THEN 'CU'
				  WHEN SINFO."Origen" IN ('TES', 'CTA') THEN 'TE'
			      ELSE 'CU'
			  END   AS "UNIDAD_DES", -- CHAR(2)
            
            CASE
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'DIPE'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'DIUS'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'PEDI'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'PEUS'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'REPDIN' AND SINFO."IngresoEgreso" = 'I' THEN 'REPA'
            	
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'FDMOOT' THEN 'MOOT'
            	
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CRCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CRCD'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ARVD'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ARVR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDTIT' AND SINFO."IngresoEgreso" = 'I' AND (SINFO."Nemotecnico" IN('PAGARE R','PAGARE NR','PDBC')) THEN 'VCIF'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'ANPCME' AND SINFO."IngresoEgreso" = 'I' THEN 'CRCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'ANPCME' AND SINFO."IngresoEgreso" = 'E' THEN 'ARVR'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'ABEU'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CAEU'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'ABEU'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CAEU'
            	
            	--FACTURAS RV
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA') THEN 'FCVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA') THEN 'FCRV'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA') THEN 'FVVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA') THEN 'FVRV'
				--NOTAS DE CREDITO RV
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA') THEN 'NVVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA') THEN 'NRVV'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA') THEN 'NCVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA') THEN 'NRVC'
				
				--FACTURAS RF
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('COMRF') AND "IdMonedaOperacion" = 'USD' THEN 'FCRU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('COMRF') THEN 'FCRF'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('VENRF') AND "IdMonedaOperacion" = 'USD' THEN 'FVRU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('VENRF') THEN 'FVRF'
				--NOTAS DE CREDITO RF
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANCRF') AND "IdMonedaOperacion" = 'USD' THEN 'NCRU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANCRF') THEN 'NRFC'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANVRF') AND "IdMonedaOperacion" = 'USD' THEN 'NVRU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANVRF') THEN 'NRFV'
				
				--FACTURAS IF
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('COMIIF') AND "IdMonedaOperacion" = 'USD' THEN 'FCIU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('COMIIF') THEN 'FCIF'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('VENIIF') AND "IdMonedaOperacion" = 'USD' THEN 'FVIU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('VENIIF') THEN 'FVIF'
				--NOTAS DE CREDITO IF
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANCIIF') AND "IdMonedaOperacion" = 'USD' THEN 'NCIU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANCIIF') THEN 'NIFC'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANVIIF') AND "IdMonedaOperacion" = 'USD' THEN 'NVIU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANVIIF') THEN 'NIFV'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND "IdTipoOperacion" IN ('SUSOSA') THEN 'EMPA'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" IN ('FAE','FEE') AND "IdTipoOperacion" IN ('COMSPT','VENSPT') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" IN ('FAE','FEE') AND "IdTipoOperacion" IN ('COMSPT','VENSPT') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" IN ('FAE','FEE') AND "IdTipoOperacion" IN ('COMSPT','VENSPT') AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'ABEU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" IN ('FAE','FEE') AND "IdTipoOperacion" IN ('COMSPT','VENSPT') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" IN ('FAE','FEE') AND "IdTipoOperacion" IN ('COMSPT','VENSPT') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" IN ('FAE','FEE') AND "IdTipoOperacion" IN ('COMSPT','VENSPT') AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CAEU'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" IN ('NCE','NEE') AND "IdTipoOperacion" IN ('ANCSPT','ANVSPT') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" IN ('NCE','NEE') AND "IdTipoOperacion" IN ('ANCSPT','ANVSPT') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" IN ('NCE','NEE') AND "IdTipoOperacion" IN ('ANCSPT','ANVSPT') AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'ABEU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" IN ('NCE','NEE') AND "IdTipoOperacion" IN ('ANCSPT','ANVSPT') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" IN ('NCE','NEE') AND "IdTipoOperacion" IN ('ANCSPT','ANVSPT') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" IN ('NCE','NEE') AND "IdTipoOperacion" IN ('ANCSPT','ANVSPT') AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CAEU'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MFW' AND SINFO."IdConcepto" IN ('VCDFWD','ANTFWD') AND SINFO."IngresoEgreso" = 'E' THEN 'CAFW'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MFW' AND SINFO."IdConcepto" IN ('VCDFWD','ANTFWD') AND SINFO."IngresoEgreso" = 'I' THEN 'ABFW'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FMM' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'SOAP'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FMM' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'SOAD'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FMM' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'SOAX'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FMM' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'SORE'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FMM' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'SORD'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FMM' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'SORX'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FAC' AND SINFO."IdTipoOperacion" = 'COMOTC' AND SINFO."IdTipoComprobante" IN ('FOE') THEN 'FCOT'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FAC' AND SINFO."IdTipoOperacion" = 'VENOTC' AND SINFO."IdTipoComprobante" IN ('FOE') THEN 'FVOT'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FAC' AND SINFO."IdTipoOperacion" = 'COMOTC' AND SINFO."IdTipoComprobante" IN ('FOX','FEE') THEN 'FCEX'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'FAC' AND SINFO."IdTipoOperacion" = 'VENOTC' AND SINFO."IdTipoComprobante" IN ('FOX','FEE') THEN 'FVEX'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECCC' THEN 'PACU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdTipoOperacion" = 'ANSIMV' THEN 'NTPC'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdTipoOperacion" = 'ANSIMC' THEN 'NTPV'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCTSIC' THEN 'CCTP' --VENCIMIENTO COMPRA A PLAZO
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCTSIV' THEN 'CVTP' --VENCIMIENTO VENTA A PLAZO
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'I' THEN 'ADCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'I' THEN 'AVCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'E' THEN 'CCCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'E' THEN 'CDCR'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'I' THEN 'ADCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'I' THEN 'AVCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'E' THEN 'CCCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'E' THEN 'CDCR'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMTER' AND SINFO."IngresoEgreso" = 'E' THEN 'AVFR' --PATA CORREDORA
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMTER' AND SINFO."IngresoEgreso" = 'I' THEN 'AVFR' --PATA CLIENTE
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENTER' AND SINFO."IngresoEgreso" = 'E' THEN 'CCFR' --PATA CLIENTE
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENTER' AND SINFO."IngresoEgreso" = 'I' THEN 'VFRC' --PATA CORREDORA
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'INGGTA' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'IGMN' 
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'INGGTA' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'IGUS'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'INGGTA' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'IGEU' 

				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'RETGTA' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'RGMN' 
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'RETGTA' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'RGUS'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoOperacion" = 'RETGTA' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'RGEU' 

				--DEFAULTS PARA CTA INVERSION
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'ABEU'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CAEU'
            	
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'GBP' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'ABMN' --ARBITRAJES EN CLP
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'GBP' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'CAMN' --ARBITRAJES EN CLP
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'GBP' AND SINFO."IdMonedaEmision" = 'USD' THEN 'ABDO' --ARBITRAJES EN USD
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'GBP' AND SINFO."IdMonedaEmision" = 'USD' THEN 'CADO' --ARBITRAJES EN USD
            	
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'NOK' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'ABMN' --ARBITRAJES EN CLP
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'NOK' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'CAMN' --ARBITRAJES EN CLP
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'NOK' AND SINFO."IdMonedaEmision" = 'USD' THEN 'ABDO' --ARBITRAJES EN USD
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'NOK' AND SINFO."IdMonedaEmision" = 'USD' THEN 'CADO' --ARBITRAJES EN USD

				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'AUD' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'ABMN' --ARBITRAJES EN CLP
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'AUD' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'CAMN' --ARBITRAJES EN CLP
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'AUD' AND SINFO."IdMonedaEmision" = 'USD' THEN 'ABDO' --ARBITRAJES EN USD
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'AUD' AND SINFO."IdMonedaEmision" = 'USD' THEN 'CADO' --ARBITRAJES EN USD

            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CAD' AND SINFO."IdMonedaEmision" = 'CAD' THEN 'ABMN' --ARBITRAJES EN CLP
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CAD' AND SINFO."IdMonedaEmision" = 'CAD' THEN 'CAMN' --ARBITRAJES EN CLP
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CAD' AND SINFO."IdMonedaEmision" = 'USD' THEN 'ABDO' --ARBITRAJES EN USD
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CAD' AND SINFO."IdMonedaEmision" = 'USD' THEN 'CADO' --ARBITRAJES EN USD

            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CHF' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'ABMN' --ARBITRAJES EN CLP
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CHF' AND SINFO."IdMonedaEmision" = 'CLP' THEN 'CAMN' --ARBITRAJES EN CLP
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CHF' AND SINFO."IdMonedaEmision" = 'USD' THEN 'ABDO' --ARBITRAJES EN USD
 	            WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CHF' AND SINFO."IdMonedaEmision" = 'USD' THEN 'CADO' --ARBITRAJES EN USD

				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdModulo" = 'TES' AND SINFO."IdMonedaOperacion" NOT IN ('CLP','USD','EUR') THEN 'ABMN' --PARA EL CASO DE ARBITRAJES EN OTRAS MONEDS, QUE SE PAGAN POR TESORERIA
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdModulo" = 'TES' AND SINFO."IdMonedaOperacion" NOT IN ('CLP','USD','EUR') THEN 'CAMN' --LOS DEJAMOS COMO SI FUERA CLP, NO TENEMOS MAS INFO
            	
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdConcepto" = 'INGTIT' THEN 'ITFM'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdConcepto" = 'RETTIT' THEN 'RTFM'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdTipoOperacion" = 'APO' THEN 'CIFM'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdTipoOperacion" = 'RES' THEN 'VIFM'

				WHEN SINFO."Origen" = 'IGT' and SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'IGRV'
				WHEN SINFO."Origen" = 'IGT' and SINFO."IdClasificacionTipoInstrumento" = 'FM' THEN 'IGFM'
				WHEN SINFO."Origen" = 'IGT' and SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'IGRF'
				WHEN SINFO."Origen" = 'IGT' and SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'IGIF'
				WHEN SINFO."Origen" = 'RGT' and SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RGRV'
				WHEN SINFO."Origen" = 'RGT' and SINFO."IdClasificacionTipoInstrumento" = 'FM' THEN 'RGFM'
				WHEN SINFO."Origen" = 'RGT' and SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RGRF'
				WHEN SINFO."Origen" = 'RGT' and SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RGIF'

				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA') THEN 'CIRV'
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('COMRF') THEN 'CIRF'
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('COMIIF') THEN 'CIIF'
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA') THEN 'VIRV'
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('VENRF') THEN 'VIRF'
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('VENIIF') THEN 'VIIF'

				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND "Nominal" = 'S' THEN 'VFIN'  --LA MESA ESTA AL REVES PORQUE ESTAN DESDE
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND "Nominal" = 'N' THEN 'VFIC'  --EL PUNTO DE VISTA DE LA CORREDORA
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND "Nominal" = 'S' THEN 'VFRN' 
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND "Nominal" = 'N' THEN 'VFRC' 

				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND "Nominal" = 'S' THEN 'CFIN'	--LA MESA ESTA AL REVES PORQUE ESTAN DESDE
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND "Nominal" = 'N' THEN 'CFIC' --EL PUNTO DE VISTA DE LA CORREDORA
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND "Nominal" = 'S' THEN 'CFRN' 
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND "Nominal" = 'N' THEN 'CFRC' 
				
				
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('INGTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'IIRV' 
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('INGTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'IIRF' 
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('INGTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'IIIF' 
				
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('RETTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RIRV' 
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('RETTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RIRF' 
				WHEN SINFO."Origen" = 'TRA' and SINFO."IdTipoOperacion" IN ('RETTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RIIF' 

				WHEN SINFO."Origen" = 'ICU' and SINFO."IdConcepto" = 'VCTSIC' THEN 'XXXX'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdConcepto" = 'ANTSIV' THEN 'XXXX'
				
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" = 'COMSIM' AND SINFO."IdTipoInstrumento" != 'SIM' THEN 'CIRV'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" = 'COMSIM' AND SINFO."IdTipoInstrumento" = 'SIM' THEN 'VITP'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" = 'VENSIM' THEN 'CITP'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA') THEN 'CIRV'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('COMRF') THEN 'CIRF'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('COMIIF') THEN 'CIIF'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('INGOSA') THEN 'EOSA'
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('DIVACC','DIVACG') THEN 'DIAC'

				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'CFIC' --CORREDORA COMPRA A TERMINO (ASUME CUSTODIA)
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'CFRC' --CORREDORA COMPRA A TERMINO (ASUME CUSTODIA)
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VFIC' --CLIENTE COMPRA A TERMINO (ASUME CUSTODIA)
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VFRC' --CLIENTE COMPRA A TERMINO (ASUME CUSTODIA)

				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('INGTIT','SUSOSA') AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'IIRV' 
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('INGTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'IIRF' 
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('INGTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'IIIF'

				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('TRATIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'ITRV' 
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('TRATIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'ITRF' 
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('TRATIT') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'ITIF' 

				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('PRESTA') THEN 'PTIP' 
				
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('COMPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'CRRC'   --COMPRA CON PACTO (ASUME CUSTODIA)
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('COMPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'CRIC'   --COMPRA CON PACTO (ASUME CUSTODIA)
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VRIC'  --CLIENTE VENDE CON PACTO
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" != SINFO."IdCliente" THEN 'CRRC' --CORREDORA COMPRA CON PACTO
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VRRC'  --CLIENTE VENDE CON PACTO
				WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" != SINFO."IdCliente" THEN 'CRIC' --CORREDORA COMPRA CON PACTO
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdConcepto" IN ('VCTSIV') THEN 'TVTP'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdConcepto" IN ('VCTSIC') THEN 'TCTP'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdConcepto" IN ('ANTSIC') THEN 'NVTP'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdConcepto" IN ('ANTSIV') THEN 'NCTP'
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA') THEN 'VIRV'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENRF') THEN 'VIRF'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENIIF') THEN 'VIIF'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('RETOSA') THEN 'VOSA' --VENCIMIENTO OSA
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('SUSOSA') THEN 'RIRV' --SUSCRIPCION OSA
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA') THEN 'NVRV'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('ANVRF') THEN 'NVRF'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('ANVIIF') THEN 'NVIF'
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA') THEN 'NCRV'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('ANCRF') THEN 'NCRF'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('ANCIIF') THEN 'NCIF'

				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND MES."IdMesa" IS NOT NULL THEN 'VFRC' --CORREDORA VENDE
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND MES."IdMesa" IS NOT NULL THEN 'VFIC' --CORREDORA VENDE
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND MES."IdMesa" IS NULL THEN 'CFRC'     --CLIENTE COMPRA
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND MES."IdMesa" IS NULL THEN 'CFIC'     --CLIENTE COMPRA
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VFIC' --CORREDORA COMPRA CLIENTE VENDE
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('COMTER') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VFRC' --CORREDORA COMPRA CLIENTE VENDE

				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('RETTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RIRV' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('RETTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RIRF' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('RETTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RIIF'

				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('TRATIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RTRV' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('TRATIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RTRF' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('TRATIT') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RTIF' 
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('COMPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VRIC' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('COMPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VRRC' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VRRC'  --CUENTA PROPIA
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" != SINFO."IdCliente" THEN 'CRRC' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VRIC' 
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VENPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" != SINFO."IdCliente" THEN 'CRIC' 
				
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA') THEN 'CIRV'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('COMRF') THEN 'CIRF'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('COMIIF') THEN 'CIIF'

				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VCDTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VTIF'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VCDTIT') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VTRF'
				
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VCDPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RIRF'
				WHEN SINFO."Origen" = 'ECU' and SINFO."IdTipoOperacion" IN ('VCDPAC') AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RIIF'
				
				
				WHEN SINFO."Origen" = 'ECU' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RIRF'
				WHEN SINFO."Origen" = 'ECU' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RIRV'
				WHEN SINFO."Origen" = 'ECU' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RIIF'
				
				WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'ABEU'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CAEU'
            	
            	ELSE '' END AS "COD_MOVTO", -- CHAR(4) --

            
            CASE SINFO."Nemotecnico" 
            	WHEN 'CFMTOEEQU A' THEN 'CFMTOEEQUA'
	    		WHEN 'CFMCGACH D' THEN 'CFMCGACHD'
				WHEN 'CFMCGACH D' THEN 'CFMCGACHD'
				WHEN 'CFMCGDCH B' THEN 'CFMCGDCHB'
             	ELSE COALESCE(LEFT(SINFO."Nemotecnico",16),'')	END	  AS "NEMO", -- CHAR(16)
            'B' 									AS "CALC_MTO_APLAZO", -- CHAR(1)
            
            'L'  									AS "CALC_MTO_CTDO", -- CHAR(1)
            CASE 
            	WHEN SINFO."Origen" in ('ECU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'VCDTIT' THEN CAST(SINFO."ValorRescate"/5000000 AS DECIMAL(17,6))
            	WHEN SINFO."Origen" in ('ICU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'VENPAC' THEN CAST(SINFO."Cantidad" AS DECIMAL(17,6))
	            WHEN SINFO."Origen" in ('TRA','ICU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'VENIIF' THEN 1 
    	        WHEN SINFO."Origen" in ('TRA','ICU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'COMIIF' THEN CAST(SINFO."Cantidad" AS DECIMAL(17,6))
            ELSE CAST(SINFO."Cantidad" AS DECIMAL(17,6)) END AS "CANTIDAD", -- DECIMAL

            COALESCE(LEFT(SINFO."CodigoAgente",4),'    ') AS "COD_AGENTE", -- CHAR(4)
            
            '    ' AS "COD_BCO_PAGO", -- CHAR(4) --- JK valdidar de donde obtener
            
            COALESCE(SINFO."IdBoveda",'')		  AS "COD_BOVEDA", -- CHAR(3)
            "CodigoCorredorContraparte"			  AS "COD_CORR_CONTRA", -- DECIMAL -- sacar info del palo
            ''						  				AS "COD_MESA", -- CHAR(4)
            '0' 								  AS "COD_OPE", -- CHAR(4)
            
            CASE SINFO."IdMonedaOperacion" WHEN 'USD' THEN 'VD' ELSE '' END AS "COD_RUBRO", -- CHAR(4) -- analizar este campo
            COALESCE(LEFT(SINFO."CodigoSucursal",4),'    ')					  AS "COD_SUC", -- CHAR(4)
            
            COALESCE(SINFO."Comision",0.0) AS "COMISION", -- DECIMAL
            
            
			CASE 
				WHEN SINFO."IngresoEgreso" = 'I' AND CONCEPTO."ConceptoAbonoReplica" <> '' AND CONCEPTO."ConceptoAbonoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoAbonoReplica",2)
				WHEN SINFO."IngresoEgreso" = 'E' AND CONCEPTO."ConceptoCargoReplica" <> '' AND CONCEPTO."ConceptoCargoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoCargoReplica",2)
				WHEN SINFO."IngresoEgreso" = '' AND SINFO."IdTipoOperacion" = 'INGTIT' AND CONCEPTO."ConceptoAbonoReplica" <> '' AND CONCEPTO."ConceptoAbonoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoAbonoReplica",2)
				WHEN SINFO."IngresoEgreso" = '' AND SINFO."IdTipoOperacion" = 'RETTIT' AND CONCEPTO."ConceptoCargoReplica" <> '' AND CONCEPTO."ConceptoCargoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoCargoReplica",2)
				ELSE 	
					CASE 
						WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'FAE' THEN '  '
						WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" IN ('FOX','FOE') THEN RIGHT(SINFO."IdConcepto",2)
						WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' THEN RIGHT(SINFO."IdConcepto",2)
						WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDFWD' then 'VF'
						WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' THEN 'cp'
						WHEN "IdTipoOperacion" IN ('VENSPT') THEN 'AV'
						WHEN "IdTipoOperacion" IN ('COMSPT') THEN 'CD'
						WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'S' then RIGHT(SINFO."IdConcepto",2)
				
						WHEN SINFO."Origen" = 'FMM' AND SINFO."IdTipoOperacion" = 'APO' AND CONCEPTO."ConceptoAbonoReplica" <> '' AND CONCEPTO."ConceptoAbonoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoAbonoReplica",2)
						WHEN SINFO."Origen" = 'FMM' AND SINFO."IdTipoOperacion" = 'RES' AND CONCEPTO."ConceptoCargoReplica" <> '' AND CONCEPTO."ConceptoCargoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoCargoReplica",2)
						
						WHEN SINFO."Origen" = 'IGT' AND CONCEPTO."ConceptoAbonoReplica" <> '' AND CONCEPTO."ConceptoAbonoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoAbonoReplica", 2)
						WHEN SINFO."Origen" = 'RGT' AND CONCEPTO."ConceptoCargoReplica" <> '' AND CONCEPTO."ConceptoCargoReplica" IS NOT NULL THEN RIGHT(CONCEPTO."ConceptoCargoReplica", 2)
						WHEN SINFO."Origen" IN ('ICU','ECU','TRA') and "IdTipoOperacion" IN ('INGTIT','RETTIT','TRATTIT') THEN RIGHT(SINFO."IdConcepto",2)
						ELSE '  ' 
					END 
			END AS "CONCEPTO_MVTO", -- CHAR(2)
            
            cast(0 as decimal(14, 4))					  AS "DERECHOS", -- DECIMAL   -- JK validar como obtenerlos y para que movimientos 
            
            COALESCE(left(SINFO."IdEmisor",10),'')		  AS "EMISOR", -- CHAR(10)
            LEFT(SINFO."EmitidoA",39)  		  AS "EMITIDO_A", -- CHAR(40)
            
            0  AS "FAC_AJUSTE", -- DECIMAL -- jk ver si se utiliza
            TO_VARCHAR(SINFO."FechaEmisionInstrumento", 'YYYY-MM-DD')  AS "FEC_EMI", -- CHAR(10)
           
            TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD')  	  AS "FEC_ING_MVTO", -- CHAR(10)
            CASE 
            WHEN SINFO."Origen" = 'ICU' AND SINFO."IdConcepto" IN ('VNSIMU','CMSIMU') AND SINFO."IdTipoInstrumento" = 'SIM' THEN
				TO_VARCHAR(SINFO."FechaVencimientoOperacion", 'YYYY-MM-DD')
            ELSE TO_VARCHAR(SINFO."FechaLiquidacion"	, 'YYYY-MM-DD')	
            END	AS "FEC_LIQUID", -- CHAR(10)

            TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD')  AS "FEC_TRANS", -- CHAR(10) -- traer info del palo
            
            TO_VARCHAR(SINFO."FechaVencimientoOperacion", 'YYYY-MM-DD') 		 AS "FEC_VCTO", -- CHAR(10)
            0  AS "FOLIO_COMP_CONT", -- DECIMAL -- folio factura relacionada
            
            LPAD("FolioTransaccion" ,8,' ') AS "FOLIO_TRANS", -- CHAR(8) -- traer del palo ahora taa to blanco
            
            CASE WHEN SINFO."IdMedioDePago" = 'EBA' THEN 'ET'
				  WHEN SINFO."IdMedioDePago" = 'CHE' THEN 'CH'
				  WHEN SINFO."IdMedioDePago" = 'BDE' THEN 'BD'
				  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'EF'
				  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'EU'
				  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'US'
				  WHEN SINFO."IdMedioDePago" = 'CIN' THEN ''	--CUENTA INVERSION
				  WHEN SINFO."IdMedioDePago" = 'VVI' THEN 'VV'
				  WHEN SINFO."IdMedioDePago" = 'VVE' THEN 'VE'
				  WHEN SINFO."IdMedioDePago" = 'TLR' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'TSW' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'TDP' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'TRA' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'AJU' THEN ''	--AJUSTE CUENTA CORRIENTE
				  ELSE ''
			  END   AS "FORMA_PAGO", -- CHAR(2)
			  
            CASE WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" = 'VENSIM' THEN 0 
            WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" = 'COMSIM' AND SINFO."IdTipoInstrumento" = 'SIM' THEN 0
            ELSE COALESCE(SINFO."Gastos",0.0) END AS "GASTOS", -- DECIMAL
            
            ''  AS "IDENTIFIC_DOCTO", -- CHAR(20) -- instruccion de pago DCTO texto libre
            
            COALESCE(SINFO."IdBolsa",'')						  AS "IND_BOLSA", -- CHAR(3)
            
            CASE 
            	WHEN SINFO."Origen" = 'PLZ' AND SINFO."IngresoEgreso" = 'I' THEN 'A'
				WHEN SINFO."Origen" = 'PLZ' AND SINFO."IngresoEgreso" = 'E' THEN 'C'
				ELSE ' '
				END   AS "IND_CCTE_APLAZO", -- CHAR(1)
				  
				  
           CASE 
           		WHEN SINFO."Origen" IN ('IGT','RGT','TRA') THEN ' '
           		WHEN SINFO."IngresoEgreso" = 'I' THEN 'A'
				WHEN SINFO."IngresoEgreso" = 'E' THEN 'C'
				ELSE ' '
			END  AS "IND_CCTE_CTDO", -- CHAR(1) -- 
				  
            CASE 
            	  WHEN SINFO."IdMedioDePago" = 'CHE' THEN 'O'
				  ELSE 'N'
			  END 	 AS "IND_CHEQUE", -- CHAR(1)  ver si aun aplica  A /N / O
			  
            'N' 									 AS "IND_COBRO_SEP", -- CHAR(1)
            'N' 									 AS "IND_CORREC", -- CHAR(1)
            
            'N'										 AS "IND_EXENCION", -- CHAR(1)
            'M'										 AS "IND_FUS_CAR_DIA", -- CHAR(1)
            'S' 									 AS "IND_FUSION_TRAS", -- CHAR(1)
            'S'										 AS "IND_GASTO_FAC", -- CHAR(1)
            
            'S'  AS "IND_HAB_CARTERA", -- CHAR(1)
            'N'  AS "IND_INC_GAS_CAR", -- CHAR(1)
            
            CASE
            	WHEN SINFO."Origen" = 'ECU' and SINFO."IdConcepto" IN ('VCTSIV','ANTSIV','ANTSIC','VCTSIC') THEN 'TP' 
            	WHEN COALESCE(CODLIQ."Codigo",'') = 'CN' 
            	THEN  '' 
            	ELSE  COALESCE(CODLIQ."Codigo",'') END  AS "IND_LIQUID", -- CHAR(2) 
            
           CASE "IngresoEgreso" WHEN 'I' THEN 'V' ELSE 'C' END AS IND_PAGO_COBRO,
          
            'F'  AS "IND_REBAJ_CAR", -- CHAR(1)
            ' '  AS "IND_RECAM_CAR", -- CHAR(1)
            'N'  AS "IND_REPAR_CAR", -- CHAR(1)
            'N'  AS "IND_RETENIDO", -- CHAR(1)
            'C'   AS "IND_RIESGO_CLI", -- CHAR(1)
           
        	CASE WHEN SINFO."Origen" = 'PLZ' AND SINFO."IdTipoProducto" in ('MERF','MEFW') THEN 'F'
					  ELSE ' '
				  END 	  AS "IND_RUEDA", -- CHAR(1)
				  
			CASE WHEN SINFO."Origen" IN ('CTA' , 'TES') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'C'
		    	  WHEN SINFO."Origen" IN ('CTA' , 'TES') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'U'
		    	  WHEN SINFO."Origen" IN ('CTA' , 'TES') AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'V'
		    	  WHEN SINFO."Origen" IN ('FMM','IGT','RGT','ICU','ECU','TRA') THEN 'T'
		    	  ELSE ''
		      END AS IND_TIT_CCTE 	,
		      
		      --char](1) NOT NULL
			CASE WHEN SINFO."Origen" IN ( 'CUS' , 'TRANSITO','FMM','IGT','RGT','ICU','ECU') THEN 'C'
				  WHEN SINFO."Origen" = 'TRA' THEN 'N'
				  ELSE ' '
			  END AS IND_TIT_CUS ,											--char](1) NOT NULL
            
            'N'  AS "IND_TRANSF", -- CHAR(1)
            'N'  AS "IND_TRASPASO", -- CHAR(1)   N   / S si es traspaso ver de donde sacar
            
            0  AS "LIN_COMP_ADJ", -- DECIMAL
            0  AS "LIN_ORDEN", -- DECIMAL
          
            COALESCE(LEFT(CASE WHEN SINFO."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN "IdClasificacionTipoInstrumento"
            			WHEN SINFO."IdTipoInstrumento" IN ('OSA','SIM') THEN 'AC'
            			WHEN SINFO."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
            			WHEN SINFO."IdTipoInstrumento" = 'FWD' THEN 'FU'
            			WHEN SINFO."IdTipoInstrumento" = 'CFI' THEN 'CF'
            			ELSE COALESCE( hpr."ValorOrigen", SINFO."IdTipoInstrumento")  END ,2),'')	 AS "MERCADO", -- CHAR(2) -- homologar
           --ROUND(x,0,ROUND_HALF_UP)
           CAST(
	           CASE
	            WHEN SINFO."Origen" in ('ECU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'VCDTIT' THEN SINFO."ValorRescate"
	            WHEN SINFO."Origen" in ('ECU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'RETTIT' THEN ROUND((SINFO."MontoCompra"/SINFO."Cantidad"))
				WHEN SINFO."Origen" in ('TRA','ICU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'VENIIF' THEN ROUND(SINFO."MontoCompra",0, ROUND_HALF_UP) 
	            WHEN SINFO."Origen" in ('TRA','ICU') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'COMIIF' THEN ROUND((SINFO."MontoCompra"/SINFO."Cantidad") ,0, ROUND_HALF_UP) 
	           ELSE SINFO."MontoCompra" END
	           	AS DECIMAL(17,2)) AS "MONTO", -- DECIMAL
           cast(	
           	CASE --WHEN SINFO."Origen" IN ('ECU','ICU','TRA') AND "IdTipoOperacion" IN ('COMSIM','VENSIM') THEN SINFO."MontoCompra"
                 WHEN SINFO."Origen" IN ('ECU','ICU','TRA') THEN SINFO."MontoPlazo"
          		 WHEN SINFO."Origen" IN ('CTA') THEN SINFO."MontoPlazo"
				  ELSE 0 
			  END   as decimal(17, 2)) as "MONTO_APLAZO", -- DECIMAL
			  
            0  AS "MONTO_CAP_COMP", -- DECIMAL
            0 AS "MONTO_INTERES", -- DECIMAL
            
            COALESCE(TO_DECIMAL(SINFO."PrecioCompra" ,17, 6),0) AS "PRECIO", -- DECIMAL
            
            CASE
				WHEN SINFO."IdTipoComprobante" = 'MES' THEN 'CPRO' 
				WHEN SINFO."IdTipoProducto" = 'MEME' THEN 'MDOL' 
				WHEN SINFO."IdTipoProducto" = 'MEFW' THEN '    ' 
				WHEN SINFO."IdTipoProducto" = 'INCI' THEN 'INCF'
				WHEN SINFO."IdTipoProducto" = 'INSI' THEN 'INTS'
				WHEN SINFO."IdTipoProducto" = 'CUST' AND SINFO."IdTipoInstrumento" = 'ACN' THEN 'INAC' 
				WHEN SINFO."IdTipoProducto" = 'CUST' AND SINFO."IdTipoInstrumento" = 'CFI' THEN 'INCF' 
				WHEN SINFO."IdTipoProducto" = 'EVCA' THEN '    ' 
				WHEN SINFO."IdTipoOperacion" = 'ANSIMV' THEN 'INTS'
				WHEN SINFO."IdTipoOperacion" = 'ANSIMC' THEN 'INTS'
				ELSE COALESCE(SINFO."IdTipoProducto",'')
			END AS "PRODUCTO", -- CHAR(4)
            
            COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PCORR."DocumentoIdentidad"),'0-0') AS "RUT_CORR", -- CHAR(11)
            
            0  AS "SEC_CAR_COMPROM", -- DECIMAL --jk validar si vale la pena incorporar
            SINFO."SecuenciaEECC"  AS "SEC_EVE", -- DECIMAL
            
            0  AS "SEC_MOVTO_ASOC", -- DECIMAL
            
            COALESCE("IdOrden",0) AS "SEC_ORDEN", -- DECIMAL
            
            0	 AS "SEC_RUT_CORR", -- DECIMAL
            0  AS "SPREAD", -- DECIMAL
            CAST(CASE WHEN SINFO."TasaCompra" > 999 THEN 999 ELSE SINFO."TasaCompra" END AS DECIMAL(7,4))	  AS "TASA", -- DECIMAL
            tO_decimal(SINFO."TasaAgente",7, 4)	  AS "TASA_AGE", -- DECIMAL
            0  AS "TASA_COMPROM", -- DECIMAL --jk tasa pacto
            
            ''  AS "TIPO_FAM", -- CHAR(2)  tipo instrumento
            
  		    CASE
  		    	WHEN SINFO."Origen" = 'IGT' THEN 'MN' 
  		    	WHEN SINFO."IdClasificacionTipoInstrumento" = 'RV' AND left(SINFO."IdTipoReajuste",2) = 'CL' THEN 'CL'
		    	WHEN left(SINFO."IdTipoReajuste",2) = 'CL' THEN 'NR'
  		    	ELSE COALESCE(left(SINFO."IdTipoReajuste",2),'') END  AS TIPO_REAJ, -- CHAR 2
            
            CASE WHEN SINFO."Origen" IN ('TRA','IGT') THEN 'CU'
				  WHEN SINFO."Origen" IN ('TES', 'CTA') THEN 'TE'
			      ELSE 'CU'
			  END   AS "UNIDAD_SOL", -- CHAR(2)
            
            0  AS "USUARIO_CER", -- DECIMAL
            0  AS "USUARIO_SOL", -- DECIMAL
            
            CASE WHEN SINFO."Nemotecnico" = 'PDBC' THEN 5000000 ELSE to_decimal(SINFO."ValorRescate",15, 4) end  AS "VAL_RESC", -- DECIMAL
            0  AS "VALOR_UNIT", -- DECIMAL -- info de evento capital
            
            CASE
            	WHEN SINFO."Origen" = 'ICU' AND SINFO."IdConcepto" IN ('VNSIMU','CMSIMU') THEN
				TO_VARCHAR(SINFO."FechaVencimientoOperacion", 'YYYY-MM-DD')
            ELSE TO_VARCHAR(SINFO."FechaLiquidacion"	, 'YYYY-MM-DD')	
            END	AS "VTO_COMP_ADJ", -- CHAR(10) -- fecha de pago

            '  '  AS "TIPO_CLI", -- CHAR(2) -- homologar
            
            ''  AS "CLAVE_DCV_CLI", -- CHAR(15)
            ''  AS "ENV_DCV_CLI", -- CHAR(1)
            ''  AS "CLAVE_DCV_COR", -- CHAR(15)
            ''  AS "ENV_DCV_COR", -- CHAR(1)
            ''  AS "PORTAFOLIO", -- CHAR(10)
            0  AS "RETENCION_FUENTE", -- DECIMAL
            ''  AS "CODIGO_SER", -- CHAR(3)
            0  AS "FOLIO_COMPROBANTE", -- DECIMAL
            

            CASE WHEN SINFO."IdMedioDePago" = 'EBA' THEN 'ET'
				  WHEN SINFO."IdMedioDePago" = 'CHE' THEN 'CH'
				  WHEN SINFO."IdMedioDePago" = 'BDE' THEN 'BD'
				  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'EF'
				  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'EU'
				  WHEN SINFO."IdMedioDePago" = 'EFE' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'US'
				  WHEN SINFO."IdMedioDePago" = 'CIN' THEN ''	--CUENTA INVERSION
				  WHEN SINFO."IdMedioDePago" = 'VVI' THEN 'VV'
				  WHEN SINFO."IdMedioDePago" = 'VVE' THEN 'VE'
				  WHEN SINFO."IdMedioDePago" = 'TLR' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'TSW' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'TDP' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'TRA' THEN 'TH'
				  WHEN SINFO."IdMedioDePago" = 'AJU' THEN ''	--AJUSTE CUENTA CORRIENTE
				  ELSE ''
			  END AS "FPA_ID", -- CHAR(2) -- ver de donde sacar
            COALESCE(SINFO."EsFungible" ,' ') AS "IND_FUNGIBILIDAD", -- CHAR(1)
            cast(SINFO."TasaEmision" as decimal(10, 4))  AS "TASA_EMISION", -- DECIMAL
            0					 AS "SPREAD_TASA_EMISION", -- DECIMAL
            ''					 AS "MODALIDAD_PAGO", -- CHAR(1)
            ''					 AS "PERIODICIDAD_PAGO", -- CHAR(1)
            CASE WHEN SINFO."IdTipoOperacion" IN ('VENSIM','COMSIM') THEN 'TS'
				  ELSE ''
			  END  AS "IND_SIMUL", -- CHAR(2)
            ''  AS "ORIGEN_RECURSOS", -- CHAR(1)
            ''  AS "CLASIF_CARTERA", -- CHAR(1)
            CASE 
            	WHEN coalesce(to_decimal(SINFO."PrecioMercado" ,17, 6),0) > 999 THEN 999 
	            ELSE coalesce(to_decimal(SINFO."PrecioMercado" ,17, 6),0) END
           	 AS "PRECIO_MED_CIE", -- DECIMAL (9,6)
            0						  AS "PRECIO_PROM_COMP", -- DECIMAL
            ''  AS "COD_FUNGIBLE", -- CHAR(20)
            ''  AS "COD_EMISION", -- CHAR(20)
            ''  AS "COD_TITULO", -- CHAR(10)
            ''  AS "GEN_MOVTO", -- CHAR(1)
            'N'  AS "IND_EXIMIDO", -- CHAR(1)
            0  AS "CODCATIFRSID", -- TINYINT
			'' as USR_LIB_SLD
     FROM (
		SELECT  CTA."IdEmpresa",CTA."EsIngresoManual",CTA."IdModulo",CTA."Origen",CTA."Folio",CTA."FechaIngresoRegistro",CTA."FechaOperacion",CTA."FechaVencimientoOperacion",CTA."FechaLiquidacion",CTA."Plazo",
				CTA."IdCondicionLiquidacion",CTA."IdEstado",CTA."IdCliente",CTA."IdPortafolio",CTA."IdClienteCorredora",CTA."IdPortafolioCorredora",
				CTA."IdTipoOperacion",CTA."IdTipoProducto",CTA."IdConcepto",CTA."IdTipoComprobante",CTA."FolioComprobante",CTA."IdMesa",
				CTA."IdClasificacionTipoInstrumento",CTA."IdTipoInstrumento",CTA."IdInstrumento",CTA."FechaEmisionInstrumento",CTA."IdMonedaEmision",
				CTA."IdMonedaOperacion",CTA."IdEmisor",CTA."Nemotecnico",CTA."EsFungible",CTA."IdBolsa",CTA."IdBoveda",CTA."CodigoAgente",CTA."CodigoSucursal",
				CTA."IdTipoReajuste",CTA."Cantidad",CTA."ValorRescate",
				CTA."PrecioCompra",CTA."TasaCompra",CTA."TasaAgente",CTA."TasaEmision",CTA."MontoCompra",CTA."PrecioMercado",CTA."CodigoIFRS",
				CTA."FolioTransaccion",CTA."IdMedioDePago",CTA."EmitidoA",com."Comision",com."Gastos",CTA."IngresoEgreso",CTA."IdComprobante",CTA."Nominal",0 as "IdOrden",0 AS "CodigoCorredorContraparte",
				CTA."SecuenciaEECC",CTA."FolioComprobante" AS "FolioComprobanteDTE",CTA."IdPersona",CTA."MontoPlazo"
		FROM v_CuentaInversion CTA
		LEFT JOIN v_comisiones com ON CTA."IdComprobante" = com."IdComprobante" AND CTA."IdTipoComprobante"  IN ('FAE','FEE','NCE','NEE','FOX')
		UNION
		SELECT  FMM."IdEmpresa",FMM."EsIngresoManual",FMM."IdModulo",FMM."Origen",FMM."Folio",FMM."FechaIngresoRegistro",FMM."FechaOperacion",FMM."FechaVencimientoOperacion",FMM."FechaLiquidacion",FMM."Plazo",
				FMM."IdCondicionLiquidacion",FMM."IdEstado",FMM."IdCliente",FMM."IdPortafolio",FMM."IdClienteCorredora",FMM."IdPortafolioCorredora",
				FMM."IdTipoOperacion",FMM."IdTipoProducto",FMM."IdConcepto",FMM."IdTipoComprobante",FMM."FolioComprobante",FMM."IdMesa",
				FMM."IdClasificacionTipoInstrumento",FMM."IdTipoInstrumento",FMM."IdInstrumento",FMM."FechaEmisionInstrumento",FMM."IdMonedaEmision",
				FMM."IdMonedaOperacion",FMM."IdEmisor",FMM."Nemotecnico",FMM."EsFungible",FMM."IdBolsa",FMM."IdBoveda",FMM."CodigoAgente",FMM."CodigoSucursal",
				FMM."IdTipoReajuste",FMM."Cantidad",FMM."ValorRescate",
				FMM."PrecioCompra",FMM."TasaCompra",FMM."TasaAgente",FMM."TasaEmision",FMM."MontoCompra",FMM."PrecioMercado",FMM."CodigoIFRS",
				FMM."FolioTransaccion",FMM."IdMedioDePago",FMM."EmitidoA",FMM."Comision",FMM."Gastos",FMM."IngresoEgreso",FMM."IdComprobante",FMM."Nominal",0 as "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",FMM."IdPersona",0 AS "MontoPlazo"
		FROM v_ffmm FMM
		UNION
		SELECT  GTAING."IdEmpresa",GTAING."EsIngresoManual",GTAING."IdModulo",GTAING."Origen",GTAING."Folio",GTAING."FechaIngresoRegistro",GTAING."FechaOperacion",
				GTAING."FechaVencimientoOperacion",GTAING."FechaLiquidacion",GTAING."Plazo",
				GTAING."IdCondicionLiquidacion",GTAING."IdEstado",GTAING."IdCliente",GTAING."IdPortafolio",GTAING."IdClienteCorredora",GTAING."IdPortafolioCorredora",
				GTAING."IdTipoOperacion",GTAING."IdTipoProducto",GTAING."IdConcepto",GTAING."IdTipoComprobante",GTAING."FolioComprobante",GTAING."IdMesa",
				GTAING."IdClasificacionTipoInstrumento",GTAING."IdTipoInstrumento",GTAING."IdInstrumento",GTAING."FechaEmisionInstrumento",GTAING."IdMonedaEmision",
				GTAING."IdMonedaOperacion",GTAING."IdEmisor",GTAING."Nemotecnico",GTAING."EsFungible",GTAING."IdBolsa",GTAING."IdBoveda",GTAING."CodigoAgente",GTAING."CodigoSucursal",
				GTAING."IdTipoReajuste",GTAING."Cantidad",GTAING."ValorRescate",
				GTAING."PrecioCompra",GTAING."TasaCompra",GTAING."TasaAgente",GTAING."TasaEmision",GTAING."MontoCompra",GTAING."PrecioMercado","CodigoIFRS",
				GTAING."FolioTransaccion",GTAING."IdMedioDePago",GTAING."EmitidoA",GTAING."Comision",GTAING."Gastos",GTAING."IngresoEgreso",GTAING."IdComprobante",GTAING."Nominal",0 as "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",GTAING."IdPersona",0 AS "MontoPlazo"
		FROM v_garantia_ingreso GTAING
		UNION
		SELECT  GTARET."IdEmpresa",GTARET."EsIngresoManual",GTARET."IdModulo",GTARET."Origen",GTARET."Folio",GTARET."FechaIngresoRegistro",GTARET."FechaOperacion",
				GTARET."FechaVencimientoOperacion",GTARET."FechaLiquidacion",GTARET."Plazo",
				GTARET."IdCondicionLiquidacion",GTARET."IdEstado",GTARET."IdCliente",GTARET."IdPortafolio",GTARET."IdClienteCorredora",GTARET."IdPortafolioCorredora",
				GTARET."IdTipoOperacion",GTARET."IdTipoProducto",GTARET."IdConcepto",GTARET."IdTipoComprobante",GTARET."FolioComprobante",GTARET."IdMesa",
				GTARET."IdClasificacionTipoInstrumento",GTARET."IdTipoInstrumento",GTARET."IdInstrumento",GTARET."FechaEmisionInstrumento",GTARET."IdMonedaEmision",
				GTARET."IdMonedaOperacion",GTARET."IdEmisor",GTARET."Nemotecnico",GTARET."EsFungible",GTARET."IdBolsa",GTARET."IdBoveda",GTARET."CodigoAgente",GTARET."CodigoSucursal",
				GTARET."IdTipoReajuste",GTARET."Cantidad",GTARET."ValorRescate",
				GTARET."PrecioCompra",GTARET."TasaCompra",GTARET."TasaAgente",GTARET."TasaEmision",GTARET."MontoCompra",GTARET."PrecioMercado","CodigoIFRS",
				GTARET."FolioTransaccion",GTARET."IdMedioDePago",GTARET."EmitidoA",GTARET."Comision",GTARET."Gastos",GTARET."IngresoEgreso",GTARET."IdComprobante",GTARET."Nominal",0 AS "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",GTARET."IdPersona",0 AS "MontoPlazo"
		FROM v_garantia_retiro GTARET
		UNION
		SELECT  nom."IdEmpresa",nom."EsIngresoManual",nom."IdModulo",nom."Origen",nom."Folio",nom."FechaIngresoRegistro",nom."FechaOperacion",
				nom."FechaVencimientoOperacion",nom."FechaLiquidacion",nom."Plazo",
				nom."IdCondicionLiquidacion",nom."IdEstado",nom."IdCliente",nom."IdPortafolio",nom."IdClienteCorredora",nom."IdPortafolioCorredora",
				nom."IdTipoOperacion",nom."IdTipoProducto",nom."IdConcepto",nom."IdTipoComprobante",nom."FolioComprobante",nom."IdMesa",
				nom."IdClasificacionTipoInstrumento",nom."IdTipoInstrumento",nom."IdInstrumento",nom."FechaEmisionInstrumento",nom."IdMonedaEmision",
				nom."IdMonedaOperacion",nom."IdEmisor",nom."Nemotecnico",nom."EsFungible",nom."IdBolsa",nom."IdBoveda",nom."CodigoAgente",nom."CodigoSucursal",
				nom."IdTipoReajuste",nom."Cantidad",nom."ValorRescate",
				nom."PrecioCompra",nom."TasaCompra",nom."TasaAgente",nom."TasaEmision",nom."MontoCompra",nom."PrecioMercado",nom."CodigoIFRS",
				nom."FolioTransaccion",nom."IdMedioDePago",nom."EmitidoA",
				CASE WHEN nom."IdComprobante" IS NOT NULL AND FAC."Monto" != 0 THEN ROUND(FAC."Comision" * nom."MontoCompra" / FAC."Monto",0 ) ELSE nom."Comision" END AS "Comision",
				CASE WHEN nom."IdComprobante" IS NOT NULL AND FAC."Monto" != 0 THEN ROUND(FAC."Gastos" * nom."MontoCompra" / FAC."Monto",0 ) ELSE nom."Gastos" END AS "Gastos",
				nom."IngresoEgreso",nom."IdComprobante",nom."Nominal",nom."IdOrden",nom."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",nom."IdPersona","MontoPlazo" AS "MontoPlazo"
		FROM v_nominales nom
		LEFT JOIN v_comisiones FAC ON nom."IdComprobante" = FAC."IdComprobante" AND nom."IdTipoComprobante"  IN ('FAE','NCE','FEE')
		UNION
		SELECT  cing."IdEmpresa",cing."EsIngresoManual",cing."IdModulo",cing."Origen",cing."Folio",cing."FechaIngresoRegistro",cing."FechaOperacion",
				cing."FechaVencimientoOperacion",cing."FechaLiquidacion",cing."Plazo",
				cing."IdCondicionLiquidacion",cing."IdEstado",cing."IdCliente",cing."IdPortafolio",cing."IdClienteCorredora",cing."IdPortafolioCorredora",
				cing."IdTipoOperacion",cing."IdTipoProducto",cing."IdConcepto",cing."IdTipoComprobante",cing."FolioComprobante",cing."IdMesa",
				cing."IdClasificacionTipoInstrumento",cing."IdTipoInstrumento",cing."IdInstrumento",cing."FechaEmisionInstrumento",cing."IdMonedaEmision",
				cing."IdMonedaOperacion",cing."IdEmisor",cing."Nemotecnico",cing."EsFungible",cing."IdBolsa",cing."IdBoveda",cing."CodigoAgente",cing."CodigoSucursal",
				cing."IdTipoReajuste",cing."Cantidad",cing."ValorRescate",
				cing."PrecioCompra",cing."TasaCompra",cing."TasaAgente",cing."TasaEmision",cing."MontoCompra",cing."PrecioMercado",cing."CodigoIFRS",
				cing."FolioTransaccion",cing."IdMedioDePago",cing."EmitidoA",
				CASE WHEN cing."IdComprobante" IS NOT NULL AND FAC."Monto" != 0 THEN ROUND(FAC."Comision" * cing."MontoCompra" / FAC."Monto",0 ) ELSE cing."Comision" END AS "Comision",
				CASE WHEN cing."IdComprobante" IS NOT NULL AND FAC."Monto" != 0 THEN ROUND(FAC."Gastos" * cing."MontoCompra" / FAC."Monto",0 ) ELSE cing."Gastos" END AS "Gastos",
				cing."IngresoEgreso",cing."IdComprobante",cing."Nominal",cing."IdOrden",cing."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",cing."IdPersona","MontoPlazo" AS "MontoPlazo"
		FROM v_cartera_ingreso cing
		LEFT JOIN v_comisiones FAC ON cing."IdComprobante" = FAC."IdComprobante" AND cing."IdTipoComprobante"  IN ('FAE','NCE','FEE')
		UNION
		SELECT  ret."IdEmpresa",ret."EsIngresoManual",ret."IdModulo",ret."Origen",ret."Folio",ret."FechaIngresoRegistro",ret."FechaOperacion",
				ret."FechaVencimientoOperacion",ret."FechaLiquidacion",ret."Plazo",
				ret."IdCondicionLiquidacion",ret."IdEstado",ret."IdCliente",ret."IdPortafolio",ret."IdClienteCorredora",ret."IdPortafolioCorredora",
				ret."IdTipoOperacion",ret."IdTipoProducto",ret."IdConcepto",ret."IdTipoComprobante",ret."FolioComprobante",ret."IdMesa",
				ret."IdClasificacionTipoInstrumento",ret."IdTipoInstrumento",ret."IdInstrumento",ret."FechaEmisionInstrumento",ret."IdMonedaEmision",
				ret."IdMonedaOperacion",ret."IdEmisor",ret."Nemotecnico",ret."EsFungible",ret."IdBolsa",ret."IdBoveda",ret."CodigoAgente",ret."CodigoSucursal",
				ret."IdTipoReajuste",ret."Cantidad",ret."ValorRescate",
				ret."PrecioCompra",ret."TasaCompra",ret."TasaAgente",ret."TasaEmision",ret."MontoCompra",ret."PrecioMercado",ret."CodigoIFRS",
				ret."FolioTransaccion",ret."IdMedioDePago",ret."EmitidoA",
				CASE WHEN ret."IdComprobante" IS NOT NULL AND FAC."Monto" != 0 THEN ROUND(FAC."Comision" * ret."MontoCompra" / FAC."Monto",0 ) ELSE ret."Comision" END AS "Comision",
				CASE WHEN ret."IdComprobante" IS NOT NULL AND FAC."Monto" != 0 THEN ROUND(FAC."Gastos" * ret."MontoCompra" / FAC."Monto",0 ) ELSE ret."Gastos" END AS "Gastos",
				ret."IngresoEgreso",ret."IdComprobante",ret."Nominal",ret."IdOrden",ret."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",ret."IdPersona","MontoPlazo" AS "MontoPlazo"
		FROM v_cartera_retiro ret
		LEFT JOIN v_comisiones FAC ON ret."IdComprobante" = FAC."IdComprobante" AND ret."IdTipoComprobante" IN ('FAE','NCE','FEE')
		UNION
		SELECT  tes."IdEmpresa",tes."EsIngresoManual",tes."IdModulo",tes."Origen",tes."Folio",tes."FechaIngresoRegistro",tes."FechaOperacion",
				tes."FechaVencimientoOperacion",tes."FechaLiquidacion",tes."Plazo",
				tes."IdCondicionLiquidacion",tes."IdEstado",tes."IdCliente",tes."IdPortafolio",tes."IdClienteCorredora",tes."IdPortafolioCorredora",
				tes."IdTipoOperacion",tes."IdTipoProducto",tes."IdConcepto",tes."IdTipoComprobante",tes."FolioComprobante",tes."IdMesa",
				tes."IdClasificacionTipoInstrumento",tes."IdTipoInstrumento",tes."IdInstrumento",tes."FechaEmisionInstrumento",tes."IdMonedaEmision",
				tes."IdMonedaOperacion",tes."IdEmisor",tes."Nemotecnico",tes."EsFungible",tes."IdBolsa",tes."IdBoveda",tes."CodigoAgente",tes."CodigoSucursal",
				tes."IdTipoReajuste",tes."Cantidad",tes."ValorRescate",
				tes."PrecioCompra",tes."TasaCompra",tes."TasaAgente",tes."TasaEmision",tes."MontoCompra",tes."PrecioMercado",tes."CodigoIFRS",
				tes."FolioTransaccion",tes."IdMedioDePago",tes."EmitidoA",
				tes."Comision",tes."Gastos",
				tes."IngresoEgreso",tes."IdComprobante",tes."Nominal",0 AS "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",tes."IdPersona",0 AS "MontoPlazo"
		FROM v_CmbtesTes tes
		
	) AS SINFO
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = SINFO."IdPortafolio"
	LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdPortafolio" = POR."IdPortafolio" AND MES."IdTipoProducto" = 'MERF' AND MES."PorDefecto" = 'S'
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERS ON PERS."IdPersona" = SINFO."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PER ON PER."IdDocumentoIdentidad" = PERS."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTO ON CONCEPTO."IdConcepto" = SINFO."IdConcepto"
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
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONF ON CONF."CodigoParametro" = 'Replica.CodigoPortafolioDefecto' AND CONF."Valor" <> ''
	WHERE POR."CodigoPortafolio" IS NULL OR POR."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R')
	
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

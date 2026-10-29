SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTOv2"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTOv2" AS

	WITH
	v_ffmm as (
		SELECT FM."IdEmpresa",'N' AS "EsIngresoManual",'FMM' AS "IdModulo",'FMM' AS "Origen",FM."SecuenciaMovimiento" AS "Folio",FM."FechaEmision" AS "FechaIngresoRegistro",FM."FechaEmision" AS "FechaOperacion",
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
			   	CASE WHEN  FM."TipoSolicitud"= 'APO' THEN 'E' ELSE 'I' END   AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona",
			   	0 as FAC_AJUSTE,
				0 AS "FolioComprobanteReferencia"
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" FM
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = FM."IdSerieFondo"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON FONDO."IdFondo" = SER."IdFondo" 
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON FM."IdPortafolio" = POR."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = FM."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
	),
	v_mesa_ingreso_cliente_corredora as (
		SELECT  OPE."IdEmpresa",'N' AS "EsIngresoManual",'MRF' AS "IdModulo",'MIC' AS "Origen",CASE SIDE WHEN 'CLI' THEN RES."SecMovCliIngreso" ELSE RES."SecMovCorIngreso" END AS "Folio",
				OPE."FechaCreacion" AS "FechaIngresoRegistro",
				OPE."FechaOperacion" AS "FechaOperacion",
				OPE."FechaVencimiento" AS "FechaVencimientoOperacion",RES."FechaVencimiento" AS "FechaVencimientoInstrumento",
				CASE WHEN "IdTipoOperacion" IN ('COMPAC','VENPAC') THEN OPE."FechaLiquidacion" ELSE OPE."FechaVencimiento" END AS "FechaLiquidacion",OPE."PlazoEnDias" as "Plazo",
				OPE."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",OPE."IdEstado" AS "IdEstado",CASE WHEN SIDE = 'CLI' THEN OPE."IdCliente" ELSE PORCORR."IdCliente" END AS "IdCliente",
				CASE WHEN SIDE = 'CLI' THEN OPE."IdPortafolio" ELSE PORCORR."IdPortafolio" END AS "IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CASE 
					WHEN side = 'CLI' AND OPE."IdTipoOperacion" = 'COMPAC' THEN 'VENPAC' 
					WHEN side = 'CLI' AND OPE."IdTipoOperacion" = 'VENPAC' THEN 'COMPAC' 
					WHEN side = 'CLI' AND OPE."IdTipoOperacion" = 'VENTER' THEN 'COMTER' 
					WHEN side = 'CLI' AND OPE."IdTipoOperacion" = 'COMTER' THEN 'VENTER' 
					ELSE OPE."IdTipoOperacion" END AS "IdTipoOperacion",
				OPE."IdTipoProducto" AS "IdTipoProducto",'' as "IdConcepto",
				'' AS "IdTipoComprobante",
				CASE COALESCE(OPE."IdOperacion",0) WHEN 0 THEN 0 ELSE OPE."IdOperacion" END AS "IdComprobante",
				CASE COALESCE(OPE."IdOperacion",0) WHEN 0 THEN 0 ELSE OPE."IdOperacion" END as "FolioComprobante",OPE."IdMesaOrigen" AS "IdMesa",
				RES."IdClasificacionTipoInstrumento",RES."IdTipoInstrumento",RES."IdInstrumento",RES."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",RES."IdMonedaEmisionInstrumento" AS "IdMonedaEmision",RES."IdMonedaLiquidacionInstrumento" AS "IdMonedaOperacion",
				RES."Nemotecnico",'' AS "IdBolsa",RES."IdBoveda",RES."EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",SUC."CodigoSucursal" as "CodigoSucursal",
			   	RES."CantidadInicial" as "Cantidad",RES."ValorRescateInicial" as "ValorRescate",RES."IdTipoReajuste",
			   	RES."Precio" AS "PrecioCompra",
			   	RES."Tasa" AS "TasaCompra",0 AS "TasaAgente",OPE."Tasa" AS "TasaEmision",
			   	RES."MontoValorizado" AS "MontoCompra",0 AS "PrecioMercado",RES."CodigoIFRS" AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE 
			   		WHEN SIDE = 'CLI' AND LEFT(OPE."IdTipoOperacion",3) = 'COM' THEN 'E'
				   	WHEN SIDE = 'CLI' AND LEFT(OPE."IdTipoOperacion",3) = 'VEN' THEN 'I'
			   		WHEN SIDE = 'COR' AND LEFT(OPE."IdTipoOperacion",3) = 'COM' THEN 'I'
				   	WHEN SIDE = 'COR' AND LEFT(OPE."IdTipoOperacion",3) = 'VEN' THEN 'E'
			   	 ELSE 'X' END AS "IngresoEgreso", --AL REVES
			   	CASE OPE."AfectaCustodia" WHEN 'S' THEN 'N' ELSE 'S' END AS "Nominal",0 AS "IdOrden",
			   	0 AS "CodigoCorredorContraparte",
			   	CASE WHEN SIDE = 'CLI' THEN CLI."IdPersona" ELSE CLICORR."IdPersona" END AS "IdPersona",
			   	RES."INGMontoAPlazo" AS "MontoPlazo",0 AS "SecuenciaEECC",0 as SEC_MOVTO_ASOC,
			   	0 as FAC_AJUSTE,
				0 AS "FolioComprobanteReferencia"
		FROM "BSCL.Custodia.BD::CUS.Reserva" RES
		LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = RES."IdOperacion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = OPE."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = OPE."IdCliente"
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdMesa" = OPE."IdMesaOrigen"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORCORR ON PORCORR."IdPortafolio" = MES."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLICORR ON CLICORR."IdCliente" = PORCORR."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on RES."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		CROSS JOIN (
			SELECT 'COR' AS SIDE FROM dummy UNION 
			SELECT 'CLI' AS SIDE FROM dummy 
		)
		WHERE OPE."IdTipoOperacion" IN ('COMTER','COMPAC','VENPAC','VENTER')
	),
	--RETROCOMPRAS Y RETROVENTAS
	v_mesa_retro_cliente_corredora as (
		SELECT  OPE."IdEmpresa",'N' AS "EsIngresoManual",'MRF' AS "IdModulo",'RET' AS "Origen",CASE SIDE WHEN 'CLI' THEN RES."SecMovCliLiquidacion" ELSE RES."SecMovCorLiquidacion" END AS "Folio",
				OPE."FechaCreacion" AS "FechaIngresoRegistro",
				CASE OPE."IdEstado" WHEN 'ANT' THEN OPE."FechaCondicionLiquidacionAnticipo" ELSE OPE."FechaVencimiento" END AS "FechaOperacion",
				OPE."FechaVencimiento" AS "FechaVencimientoOperacion",RES."FechaVencimiento" AS "FechaVencimientoInstrumento",
				CASE WHEN "IdTipoOperacion" IN ('COMPAC','VENPAC') THEN OPE."FechaLiquidacion" ELSE OPE."FechaVencimiento" END AS "FechaLiquidacion",OPE."PlazoEnDias" as "Plazo",
				OPE."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",OPE."IdEstado" AS "IdEstado",CASE WHEN SIDE = 'CLI' THEN OPE."IdCliente" ELSE PORCORR."IdCliente" END AS "IdCliente",
				CASE WHEN SIDE = 'CLI' THEN OPE."IdPortafolio" ELSE PORCORR."IdPortafolio" END AS "IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CASE 
					WHEN side = 'CLI' AND OPE."IdTipoOperacion" = 'COMPAC' THEN 'VENPAC' 
					WHEN side = 'CLI' AND OPE."IdTipoOperacion" = 'VENPAC' THEN 'COMPAC'
					ELSE OPE."IdTipoOperacion" END AS "IdTipoOperacion",
				OPE."IdTipoProducto" AS "IdTipoProducto",'' as "IdConcepto",
				'' AS "IdTipoComprobante",
				CASE COALESCE(OPE."IdOperacion",0) WHEN 0 THEN 0 ELSE OPE."IdOperacion" END AS "IdComprobante",
				CASE COALESCE(OPE."IdOperacion",0) WHEN 0 THEN 0 ELSE OPE."IdOperacion" END as "FolioComprobante",OPE."IdMesaOrigen" AS "IdMesa",
				RES."IdClasificacionTipoInstrumento",RES."IdTipoInstrumento",RES."IdInstrumento",RES."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",RES."IdMonedaEmisionInstrumento" AS "IdMonedaEmision",RES."IdMonedaLiquidacionInstrumento" AS "IdMonedaOperacion",
				RES."Nemotecnico",'' AS "IdBolsa",RES."IdBoveda",RES."EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",SUC."CodigoSucursal" as "CodigoSucursal",
			   	CASE RES."CantidadInicial" WHEN 0 THEN 1 ELSE RES."CantidadInicial" END as "Cantidad",CASE WHEN RES."Nemotecnico" = 'PDBC' THEN 5000000 ELSE RES."ValorRescateInicial" END as "ValorRescate",RES."IdTipoReajuste",
			   	RES."Precio" AS "PrecioCompra",
			   	RES."Tasa" AS "TasaCompra",0 AS "TasaAgente",OPE."Tasa" AS "TasaEmision",
			   	RES."MontoValorizado" AS "MontoCompra",0 AS "PrecioMercado",RES."CodigoIFRS" AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE 
			   		WHEN SIDE = 'CLI' AND LEFT(OPE."IdTipoOperacion",3) = 'COM' THEN 'E'
				   	WHEN SIDE = 'CLI' AND LEFT(OPE."IdTipoOperacion",3) = 'VEN' THEN 'I'
			   		WHEN SIDE = 'COR' AND LEFT(OPE."IdTipoOperacion",3) = 'COM' THEN 'I'
				   	WHEN SIDE = 'COR' AND LEFT(OPE."IdTipoOperacion",3) = 'VEN' THEN 'E'
			   	 ELSE 'X' END AS "IngresoEgreso", --AL REVES
			   	CASE OPE."AfectaCustodia" WHEN 'S' THEN 'N' ELSE 'S' END AS "Nominal",0 AS "IdOrden",
			   	0 AS "CodigoCorredorContraparte",
			   	CASE WHEN SIDE = 'CLI' THEN CLI."IdPersona" ELSE CLICORR."IdPersona" END AS "IdPersona",
			   	RES."INGMontoAPlazo" AS "MontoPlazo",0 AS "SecuenciaEECC",0 as SEC_MOVTO_ASOC,
			   	0 as FAC_AJUSTE,
				0 AS "FolioComprobanteReferencia"
		FROM "BSCL.Custodia.BD::CUS.Reserva" RES
		LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OPE ON OPE."IdOperacion" = RES."IdOperacion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = OPE."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = OPE."IdCliente"
		LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdMesa" = OPE."IdMesaOrigen"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORCORR ON PORCORR."IdPortafolio" = MES."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLICORR ON CLICORR."IdCliente" = PORCORR."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on RES."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		CROSS JOIN (
			SELECT 'COR' AS SIDE FROM dummy UNION 
			SELECT 'CLI' AS SIDE FROM dummy 
		)
		WHERE OPE."IdTipoOperacion" IN ('COMPAC','VENPAC') AND OPE."IdEstado" IN ('LIQ','ANT')
	),
	--VENCIMIENTOS RF, IF, SIM
	v_vencimientos as (
		SELECT  CUS."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'VCD' AS "Origen",COALESCE(CASE WHEN CUS."EGRIdTipoOperacion" IN ('COMSIM','VENSIM') THEN TRASIM."SecuenciaMovimiento" WHEN CUS."EGRIdTipoOperacion" IN ('SORLET') THEN TRASIM."SecuenciaMovimiento"-10000 ELSE TRA."SecuenciaMovimiento" END,CUS."IdCartera") AS "Folio",
				CUS."EGRFechaOperacion" AS "FechaIngresoRegistro",
				CUS."EGRFechaOperacion" AS "FechaOperacion",
				CASE 
					WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF') THEN INS."FechaVencimiento"
					ELSE CUS."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				CUS."EGRFechaOperacion" AS "FechaLiquidacion",CUS."PlazoEnDiasOperacion" as "Plazo",
				CUS."EGRIdCondicionLiquidacion" AS "IdCondicionLiquidacion",CUS."IdEstado" AS "IdEstado",CUS."IdCliente",CUS."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CUS."EGRIdTipoOperacion" AS "IdTipoOperacion",CUS."IdTipoProducto" AS "IdTipoProducto",CUS."EGRIdConcepto" as "IdConcepto",
				'CUS' AS "IdTipoComprobante",
				CASE WHEN CUS."EGRIdTipoOperacion" IN ('COMSIM','VENSIM') THEN FAC."FolioComprobante" ELSE 0 END AS "IdComprobante",
				CASE WHEN CUS."EGRIdTipoOperacion" IN ('COMSIM','VENSIM') THEN FAC."FolioComprobante" ELSE 0 END AS "FolioComprobante",
				CUS."IdMesa" AS "IdMesa",
				CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",CUS."IdMonedaEmision",CUS."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CASE WHEN CUS."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN SUB."Nemotecnico" ELSE CUS."Nemotecnico" END AS "Nemotecnico",CUS."IdBolsa" AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",'' as "CodigoSucursal",
			   	CUS."EGRCantidad" as "Cantidad",CUS."EGRValorRescate" as "ValorRescate",CUS."IdTipoReajuste",
			   	CASE WHEN CUS."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN CUS."EGRPrecioAPlazo" ELSE CUS."PrecioCompra" END AS "PrecioCompra",
			   	CUS."TasaCompra" AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	CUS."EGRMonto" AS "MontoCompra",0 AS "PrecioMercado",CUS."CodigoIFRS" AS "CodigoIFRS", TO_NVARCHAR(CUS."IdOperacion") AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE CUS."EGRIdConcepto" WHEN 'VCDTIT' THEN 'E' WHEN 'VCTSIC' THEN 'E' WHEN 'VCTSIV' THEN 'I' ELSE 'X' END as "IngresoEgreso",
			   	'N' AS "Nominal",0 AS "IdOrden",
			   	0 AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	CASE WHEN CUS."EGRIdTipoOperacion" IN ('COMSIM','VENSIM') THEN CUS."INGMontoAPlazo" ELSE 0 END AS "MontoPlazo",
			   	0 as FAC_AJUSTE,
			   	IFNULL(COMREF."FolioComprobante",0) AS "FolioComprobanteReferencia"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CUS
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FAC ON FAC."IdComprobante" = CUS."FolioComprobante" AND CUS."IdTipoInstrumento" = 'SIM'
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRA ON TRA."IdMovimientoCarteraEnTransito" = CUS."EGRIdMovimientoCarteraEnTransito" AND CUS."IdTipoInstrumento" != 'SIM'
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRASIM ON TRASIM."IdMovimientoCarteraEnTransito" = CUS."IdMovimientoCarteraEnTransito" AND CUS."EGRIdTipoOperacion" IN ('COMSIM','VENSIM','SORLET')
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CUS."FolioCartera" AND CUS."IdTipoInstrumento" = 'SIM' AND SUB."EsPrimeraCartera" = 'S'
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN ( --Se incluye casuistica de los Anticipos de Sim dejando solo la Ultima NC asociada a la factura como FOLIO_COMP_CONT como en Sebra
			SELECT "IdComprobanteReferencia", "FolioComprobante" FROM (
				SELECT RANK() OVER (PARTITION BY "IdComprobanteReferencia" ORDER BY "IdComprobante" DESC)  as RANK,"FolioComprobanteReferencia", "IdComprobanteReferencia", "IdEstadoComprobante", "FolioComprobante"
				FROM "BSCL.Facturacion.BD::FAC.Comprobante" WHERE "IdComprobanteReferencia" <> 0 AND "IdEstadoComprobante" != 'ANU'
			) WHERE RANK = 1
		) AS COMREF ON FAC."IdComprobante" = COMREF."IdComprobanteReferencia"
		--WHERE CUS."EGRIdTipoOperacion" IN ('VENSIM','COMSIM','VENPRT','VENEMP','VCDTIT') AND CUS."IdEstado" = 'VCD' --DEJAMOS FUERA PRESTAMOS Y EMPRESTITOS POR AHORA
		WHERE (CUS."EGRIdTipoOperacion" IN ('VENSIM','COMSIM','VCDTIT') AND CUS."IdEstado" = 'VCD') OR (CUS."EGRIdTipoOperacion" = 'SORLET' AND CUS."IdEstado"= 'LIQ')
	),
	--COMPRAS CIRV,CIRF,CIIF
	--VENTAS VIRV,VIRF,VIIF
	--NC COMPRAS NCRV,NCRF,NCIF
	--NC VENTAS NVRV,NVRF,NVIF
	--ANTICIPOS NVTP, NCTP
	v_movimiento_comprobante as (
		SELECT
				FAC."IdEmpresa",'N' AS "EsIngresoManual",'FAC' AS "IdModulo",'FAC' AS "Origen",FAC."SecuenciaMovimiento" AS "Folio",FAC."FechaIngresoRegistro",FAC."FechaMovimientoComprobante" AS "FechaOperacion",
				CASE 
					WHEN FAC."IdClasificacionTipoInstrumento" IN ('RF') THEN INS."FechaVencimiento"
					ELSE FAC."FechaVencimiento"
				END	AS "FechaVencimientoOperacion",
				FAC."FechaLiquidacion" AS "FechaLiquidacion",
				FAC."DiasPlazo" as "Plazo", FAC."IdCondicionLiquidacion",
				FAC."IdEstadoFacturacion" AS "IdEstado",
				FAC."IdCliente",FAC."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CASE 
					WHEN FAC."IdTipoOperacion" IN ('COMSIM') AND FAC."EsAPlazo" = 'N' THEN 'COMACN'
					WHEN FAC."IdTipoOperacion" IN ('VENSIM') AND FAC."EsAPlazo" = 'N' THEN 'VENACN'
					ELSE FAC."IdTipoOperacion" END AS "IdTipoOperacion",FAC."IdTipoProducto" AS "IdTipoProducto",
				FAC."IdConceptoMovimiento" AS "IdConcepto",
				FAC."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(FAC."IdComprobante",0) WHEN 0 THEN 0 ELSE FAC."IdComprobante" END as "IdComprobante",
				CASE COALESCE(CMBT."FolioComprobante",0) WHEN 0 THEN 0 ELSE CMBT."FolioComprobante" END as "FolioComprobante",
				FAC."IdMesa" AS "IdMesa",
				FAC."IdClasificacionTipoInstrumento",FAC."IdTipoInstrumento",FAC."IdInstrumento",FAC."IdEmisor",
				FAC."FechaMovimientoComprobante" AS "FechaEmisionInstrumento",FAC."IdMonedaEmision",FAC."IdMoneda" AS "IdMonedaOperacion",
				CASE 	
					--VITP,CITP,NVTP,NCTP
					WHEN FAC."IdTipoOperacion" IN ('VENSIM','COMSIM','ANSIMC','ANSIMV') THEN FAC."NemotecnicoSubyacente"
					WHEN FAC."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN FAC."NemotecnicoRelacionado"
					ELSE FAC."NemotecnicoInstrumento" END AS "Nemotecnico",
				FAC."IdBolsa" AS "IdBolsa",FAC."IdBoveda",FAC."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",
				COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	CASE WHEN FAC."IdTipoOperacion" IN ('VENIIF') THEN 1 ELSE FAC."Cantidad" END as "Cantidad",
			   	CASE WHEN FAC."IdTipoOperacion" IN ('COMIIF') AND INS."Nemotecnico" = 'PDBC' THEN 5000000 ELSE fac."ValorRescate" END AS "ValorRescate", CASE fac."IdClasificacionTipoInstrumento" WHEN 'RV' THEN INS."IdTipoReajuste" ELSE FAC."IdTipoReajuste" END AS "IdTipoReajuste",
				FAC."Precio" AS "PrecioCompra",
				FAC."Tasa" AS "TasaCompra",FAC."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
				CASE WHEN FAC."IdTipoOperacion" IN ('COMSIM','VENSIM') AND FAC."EsAPlazo" = 'S' THEN 0 ELSE FAC."MontoMovimientoComprobante" END AS "MontoCompra",
			   	0 AS "PrecioMercado",
			   	FAC."CodigoIFRS" AS "CodigoIFRS",
			   		FAC."FolioTransaccion" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos", --UNIFICAR
			   	CASE WHEN FAC."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA','COMRF','COMIIF','COMMON') THEN 'I'
					WHEN FAC."IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA','VENRF','VENIIF','VENMON') THEN 'E'
					WHEN FAC."IdTipoOperacion" IN ('ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA','ANCRF','ANCIIF','ANCMON') THEN 'E'
					WHEN FAC."IdTipoOperacion" IN ('ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA','ANVRF','ANVIIF','ANVMON') THEN 'I'
					WHEN FAC."IdTipoOperacion" IN ('ANSIMC') THEN 'E'
					WHEN FAC."IdTipoOperacion" IN ('ANSIMV') THEN 'I' ELSE '' END
			   	AS "IngresoEgreso",FAC."ActualizarCustodia" AS "Nominal", 
			   	CASE WHEN FAC."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA','COMRF','COMIIF','COMMON',
										'VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA','VENRF','VENIIF','VENMON') 
			   		THEN FAC."IdOrden" ELSE 0 END AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	CASE WHEN FAC."IdTipoOperacion" IN ('COMSIM','VENSIM') AND FAC."EsAPlazo" = 'S' THEN FAC."Monto" ELSE 0 END AS "MontoPlazo","IdMovimientoComprobante" AS SEC_MOVTO_ASOC,
			   	COALESCE(CMBT."ValorTipoCambio",1) AS FAC_AJUSTE,
			   	IFNULL(COMREF."FolioComprobante",0) AS "FolioComprobanteReferencia",
			   	0 AS  "SecuenciaEECC"
		FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBT ON FAC."IdComprobante" = CMBT."IdComprobante"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on FAC."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = FAC."IdEmpleadoEjecutivoPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = FAC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON FAC."IdCorredoraContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = FAC."IdBolsa"
		LEFT JOIN ( --Se incluye casuistica de los Anticipos de Sim dejando solo la Ultima NC asociada a la factura como FOLIO_COMP_CONT como en Sebra
			SELECT "IdComprobanteReferencia", "FolioComprobante" FROM (
				SELECT RANK() OVER (PARTITION BY "IdComprobanteReferencia" ORDER BY "IdComprobante" DESC)  as RANK,"FolioComprobanteReferencia", "IdComprobanteReferencia", "IdEstadoComprobante", "FolioComprobante"
				FROM "BSCL.Facturacion.BD::FAC.Comprobante" WHERE "IdComprobanteReferencia" <> 0 AND "IdEstadoComprobante" != 'ANU'
			) WHERE RANK = 1
		) AS COMREF ON FAC."IdComprobante" = COMREF."IdComprobanteReferencia"
		WHERE FAC."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA','COMRF','COMIIF','COMMON',
										'VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA','VENRF','VENIIF','VENMON',
										'ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA','ANCRF','ANCIIF','ANCMON',
										'ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA','ANVRF','ANVIIF','ANVMON',
										'ANSIMC','ANSIMV'
		)
	),
	v_garantias as (
	
		SELECT	
				GTA."IdEmpresa",'N' AS "EsIngresoManual",'GTA' AS "IdModulo",'GTA' AS "Origen",GTA."SecuenciaMovimiento" AS "Folio",GTA."FechaIngresoRegistro",GTA."FechaOperacion",
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
				GTA."Nemotecnico",'' AS "IdBolsa",GTA."IdBoveda",GTA."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	GTA."Cantidad",GTA."ValorRescate",GTA."IdTipoReajuste",
			   	GTA."PrecioCompra" AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	GTA."MontoCompraMonedaInstrumento" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona",
			   	0 as FAC_AJUSTE,
				0 AS "FolioComprobanteReferencia"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" GTA
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on GTA."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = GTA."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = GTA."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = por."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE GTA."IdTipoOperacion" IN ('INGGTA','RETGTA')
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
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona",0 as FAC_AJUSTE,
				0 AS "FolioComprobanteReferencia"
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
		SELECT 	MOV."IdEmpresa","EsIngresoManual",MOV."IdModulo",'CTA' AS "Origen",MOV."SecuenciaMovimiento" AS "Folio",MOV."FechaIngresoRegistro",MOV."FechaSistema" AS "FechaOperacion",
				CASE WHEN FAC."IdComprobante" IS NOT NULL AND FAC."IdClasificacionTipoInstrumento" = 'IF' THEN FAC."FechaVencimiento" ELSE MOV."FechaEfectivaMovimiento" END AS "FechaVencimientoOperacion",MOV."FechaEfectivaMovimiento" AS "FechaLiquidacion",0 AS "Plazo",
				MOV."IdCondicionDeLiquidacion" AS "IdCondicionLiquidacion",MOV."IdEstado",CI."IdCliente" AS "IdCliente",CI."IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				MOV."IdTipoOperacion",MOV."IdTipoProducto",MOV."IdConcepto",MOV."IdTipoComprobante",MOV."IdComprobante",
				CASE 
					WHEN MOV."IdTipoOperacion" IN ('COMPAC','VENPAC') AND MOV."IdModulo" IN ('PLA','MRF') THEN MOV."IdOperacion" 
					WHEN MOV."IdTipoOperacion" IN ('COMPAC','VENPAC') AND MOV."IdModulo" = 'TES' THEN MOV."IdComprobante" 
				ELSE FAC."FolioComprobante" END AS "FolioComprobante",
				0 AS "IdMesa",
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
			   	ELSE 0 END AS "MontoPlazo",0 as FAC_AJUSTE,
			   	IFNULL(COMREF."FolioComprobante",0) AS "FolioComprobanteReferencia"
		FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS MOV
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FAC on MOV."IdComprobante" = FAC."IdComprobante" AND MOV."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE','FOX')
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL on SOL."IdSolicitudAporteRescate" = MOV."IdComprobante" AND MOV."IdTipoComprobante" IN ('FFM')
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVC ON EVC."IdEventoCapitalCliente" = MOV."IdOperacion" AND MOV."IdConcepto" IN ('CIECDV','REPDIN')
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARVENC ON MOV."IdComprobante" = CARVENC."IdCartera" AND MOV."IdConcepto" IN ('VCTSIC','VCTSIV','VCDTIT')
		LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" TESDET ON TESDET."IdComprobanteDetalle" = MOV."IdComprobanteDetalle" AND MOV."IdModulo" = 'TES'
		LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" INGEGR ON INGEGR."IdIngresoEgreso" = TESDET."IdIngresoEgreso"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on MOV."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = COALESCE(INS."IdTipoInstrumento",MOV."IdTipoInstrumento")
		LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON CI."IdCuentaInversion" = MOV."IdCuentaInversion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = CI."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = MOV."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON CAST(SUB."FolioCarteraRelacionada" AS NVARCHAR) = MOV."FolioComprobante" 
			AND MOV."IdConcepto" IN ('VCTSIC','VCTSIV','ANTSIV','ANTSIC') AND SUB."IdEstado" IN ('ANT','VCD')
		LEFT JOIN ( --Se incluye casuistica de los Anticipos de Sim dejando solo la Ultima NC asociada a la factura como FOLIO_COMP_CONT como en Sebra
			SELECT "IdComprobanteReferencia", "FolioComprobante" FROM (
				SELECT RANK() OVER (PARTITION BY "IdComprobanteReferencia" ORDER BY "IdComprobante" DESC)  as RANK,"FolioComprobanteReferencia", "IdComprobanteReferencia", "IdEstadoComprobante", "FolioComprobante"
				FROM "BSCL.Facturacion.BD::FAC.Comprobante" WHERE "IdComprobanteReferencia" <> 0 AND "IdEstadoComprobante" != 'ANU'
			) WHERE RANK = 1
		) AS COMREF ON FAC."IdComprobante" = COMREF."IdComprobanteReferencia"
			--FILTROS MOVIMIENTOS DE SPOT DESDE TESORERIA YA QUE EN SEBRA ESTOS NO EXISTEN
		--WHERE NOT (MOV."IdModulo" = 'TES' AND MOV."IdConcepto" IN ('CMMESP','VNMESP'))
		--WHERE NOT (MOV."IdModulo" = 'FAC' AND MOV."IdConcepto" IN ('CMMESP','VNMESP') AND CI."IdMoneda" = 'USD')
	),
	v_nominales as (
		SELECT  TRANSITO."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'NOM' AS "Origen",TRANSITO."SecuenciaMovimiento" AS "Folio",TRANSITO."FechaIngresoRegistro",
				CASE WHEN TRANSITO_REL."Nemotecnico" IN ('FINES GENERALES') AND CAR_PRT."IdEstado" IN ('VCD','ANT') THEN CAR_PRT."EGRFechaOperacion" WHEN TRANSITO."IdEstado" = 'CRT' THEN TRANSITO."FechaLiquidacion" ELSE TRANSITO."FechaOperacion" END AS "FechaOperacion",
				CASE 
					WHEN TRANSITO."IdClasificacionTipoInstrumento" IN ('RF') THEN INS."FechaVencimiento"
					ELSE TRANSITO."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				TRANSITO."FechaLiquidacion" AS "FechaLiquidacion",TRANSITO."PlazoEnDiasOperacion" as "Plazo",
				TRANSITO."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",TRANSITO."IdEstado" AS "IdEstado",TRANSITO."IdCliente",TRANSITO."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				TRANSITO."IdTipoOperacion" AS "IdTipoOperacion",TRANSITO."IdTipoProducto" AS "IdTipoProducto",
				CASE WHEN TRANSITO."IdConcepto" = 'INGPRT' AND TRANSITO_REL."Nemotecnico" = 'FINES GENERALES' AND CAR_PRT."IdEstado" = 'VCD' THEN 'VCDPFG' 
					WHEN TRANSITO."IdConcepto" = 'INGPRT' AND TRANSITO_REL."Nemotecnico" = 'FINES GENERALES' AND CAR_PRT."IdEstado" = 'ANT' THEN 'ANTPFG' 
				ELSE TRANSITO."IdConcepto" END as "IdConcepto", --VCDPFG VCDPVC VCDPAD para vencimientos de prestamo
				TRANSITO."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(TRANSITO."FolioComprobante",0) WHEN 0 THEN 0 ELSE TRANSITO."FolioComprobante" END AS "IdComprobante",
				CASE COALESCE(TRANSITO."FolioComprobante",0) WHEN 0 THEN 0 ELSE TRANSITO."FolioComprobante" END as "FolioComprobante",TRANSITO."IdMesa" AS "IdMesa",
				TRANSITO."IdClasificacionTipoInstrumento",CASE WHEN TRANSITO."Nemotecnico" = 'FINES GENERALES' THEN TRANSITO_REL2."IdTipoInstrumento" ELSE TRANSITO."IdTipoInstrumento" END AS "IdTipoInstrumento",
				TRANSITO."IdInstrumento",CASE WHEN TRANSITO."Nemotecnico" = 'FINES GENERALES' THEN TRANSITO_REL2."IdEmisor" ELSE TRANSITO."IdEmisor" END AS "IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",TRANSITO."IdMonedaEmision",TRANSITO."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CASE WHEN TRANSITO."Nemotecnico" = 'FINES GENERALES' THEN TRANSITO_REL2."Nemotecnico" ELSE TRANSITO."Nemotecnico" END AS "Nemotecnico",
				TRANSITO."IdBolsa" AS "IdBolsa",TRANSITO."IdBoveda",TRANSITO."EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",'' as "CodigoSucursal",
			   	TRANSITO."Cantidad" as "Cantidad",TRANSITO."ValorRescate" as "ValorRescate",TRANSITO."IdTipoReajuste",
			   	TRANSITO."PrecioCompra" AS "PrecioCompra",TRANSITO."TasaCompra" AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	TRANSITO."MontoCompra" AS "MontoCompra",0 AS "PrecioMercado",TRANSITO."CodigoIFRS" AS "CodigoIFRS",TRANSITO."FolioExternoReferencia" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN LEFT(TRANSITO."IdTipoOperacion",3) = 'COM' THEN 'I' WHEN TRANSITO."IdTipoOperacion" = 'ANSIMC' THEN 'E' 
			   	WHEN TRANSITO_REL."Nemotecnico" = 'FINES GENERALES' THEN 'I' 
			   	WHEN TRANSITO."IdTipoOperacion" IN ('INGTIT','DIVACC','EMILIB') THEN 'I' ELSE 'E' END AS "IngresoEgreso",
			   	CASE WHEN TRANSITO."IdConcepto" = 'INGPRT' THEN 'N' ELSE TRANSITO."EsNominal" END AS "Nominal",ORDEN."IdOrden" AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	CASE WHEN TRANSITO."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN TRANSITO."MontoCompra" ELSE 0 END AS "MontoPlazo",
				0 AS "FolioComprobanteReferencia",
				COALESCE(ECCS."Secuencia",0) AS  "SecuenciaEECC"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO_REL ON TRANSITO_REL."IdMovimientoCarteraEnTransito" = TRANSITO."IdMovimientoCarteraEnTransitoRelacionado" AND TRANSITO_REL."Nemotecnico" IN ('FINES GENERALES')
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO_REL2 ON TRANSITO_REL2."IdMovimientoCarteraEnTransitoRelacionado" = TRANSITO."IdMovimientoCarteraEnTransito" AND TRANSITO."Nemotecnico" IN ('FINES GENERALES')
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR_PRT ON CAR_PRT."IdMovimientoCarteraEnTransito" = TRANSITO_REL."IdMovimientoCarteraEnTransito" AND TRANSITO_REL."Nemotecnico" IN ('FINES GENERALES') AND CAR_PRT."EsPrimeraCartera" = 'S'
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = TRANSITO."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."IdTransaccion" = MOVTOFAC."IdTransaccion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on TRANSITO."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = TRANSITO."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECCS ON ECCS."IdComprobanteFac" = TRANSITO."FolioComprobante" AND TRANSITO."IdTipoOperacion" IN ('EMILIB','DIVACC') AND TRANSITO."IdTipoComprobante" = 'FEE' AND ECCS."IdPortafolio" = TRANSITO."IdPortafolio" AND ECCS."IdTipoEventoCapital" IN ('EL', 'DA')
		WHERE 
		--NOMINALES
		(TRANSITO."IdTipoOperacion" IN ('COMACN','COMCFI','COMCFX','COMETN','COMETX','VENACN','VENCFI','VENETN','VENETX','VENCFX','COMRF','COMIIF','VENRF','VENIIF','COMTER','VENTER','COMPAC','VENPAC','ANSIMC') and (TRANSITO."EsNominal" = 'S')) OR 
		--MANUALES
		(TRANSITO."IdTipoOperacion"  IN ('INGTIT','RETTIT') and TRANSITO."PermiteCertificacionPorUsuario" = 'S' and TRANSITO."IdConcepto" NOT IN('VENPAC','VCDTIT','VCTSIC','VCTSIV') and TRANSITO."IdTipoInstrumento" NOT IN ('OSA') and TRANSITO."EsNominal" = 'N') OR
		--PRESTAMOS, SOLO FINES GENERALES POR AHORA
		(TRANSITO."IdTipoOperacion"  IN ('PRESTA') AND TRANSITO."Nemotecnico" IN ('FINES GENERALES')) OR --INGRESOS
 		(TRANSITO_REL."Nemotecnico" IN ('FINES GENERALES') AND CAR_PRT."IdEstado" IN ('VCD','ANT')) OR --VENCIMIENTOS Y ANTICIPOS
 		(TRANSITO."IdTipoOperacion"  IN ('DIVACC','EMILIB') and TRANSITO."IdConcepto" IN('DIEVAC','EMILIB') and TRANSITO."EsNominal" = 'N')
	),
	v_CmbtesTes as (
		SELECT 	CMBTE."IdEmpresa",'N' AS "EsIngresoManual",'TES' AS "IdModulo",'TES' AS "Origen","SecuenciaMovimiento" AS "Folio",CMBTE."FechaIngresoRegistro",CMBTE."FechaMovimiento" AS "FechaOperacion",
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
			   	CASE WHEN  CMBTE."IdTipoMovimiento"= 'MOVING' THEN 'I' ELSE 'E' END   AS "IngresoEgreso",'N' AS "Nominal",0 AS "SecuenciaEECC","IdPersona",0 as FAC_AJUSTE,
				0 AS "FolioComprobanteReferencia"
		FROM  "BSCL.Tesoreria.BD::TES.Comprobante" AS CMBTE
		WHERE CMBTE."IdTipoPersona" IN ('CNT','PER','PRO')
	),
	v_comisiones as (
		select COMP."IdComprobante","IdBolsa","Precio","FolioComprobante","Cantidad",
		COALESCE(MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN cob."MontoCobro" END),0) as "Comision",
		COALESCE(MAX(CASE WHEN "CodigoCobroClasificacion" IN ('IMPUESTO','GASTO') THEN cob."MontoCobro" END),0) as "Gastos",
		comp."MontoComprobante"
		from "BSCL.Facturacion.BD::FAC.Comprobante" comp
		left join (
			SELECT "IdComprobante", CLASIF."CodigoCobroClasificacion" , SUM("MontoComprobanteCobro") AS "MontoCobro"
			FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" COMCOBRO
			LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" COBRO ON COBRO."IdCobro" = COMCOBRO."IdCobro" 
			LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" CLASIF ON COBRO."IdCobroClasificacion" = CLASIF."IdCobroClasificacion"
			WHERE COMCOBRO."IdEstado" = 'VIG' 
			group by "IdComprobante", CLASIF."CodigoCobroClasificacion"
		) as cob on cob."IdComprobante" = comp."IdComprobante"
		group by comp."IdComprobante","IdBolsa","Precio","FolioComprobante","Cantidad","MontoComprobante"
	),
	-- Ingresos de título por EVC EP
	v_mov_custodia_osas as (
		SELECT  
			CUS."IdEmpresa"
			,'N' AS "EsIngresoManual"
			,'CUS' AS "IdModulo"
			,'CUS' AS "Origen"
			,CUS."SecuenciaMovimiento"  AS "Folio"
			,CUS."FechaOperacion" AS "FechaIngresoRegistro",	
			CUS."FechaOperacion" AS "FechaOperacion",
			'0001-01-01' AS "FechaVencimientoOperacion",
			CUS."FechaLiquidacion" AS "FechaLiquidacion", 
			CUS."PlazoEnDiasOperacion" as "Plazo",
			CUS."IdCondicionLiquidacion" AS "IdCondicionLiquidacion"
			,CUS."IdEstado" AS "IdEstado"
			,CUS."IdCliente"
			,CUS."IdPortafolio"
			,0 as "IdClienteCorredora"
			,0 AS "IdPortafolioCorredora",
			CUS."IdTipoOperacion" AS "IdTipoOperacion"
			,CUS."IdTipoProducto" AS "IdTipoProducto"
			,CUS."IdConcepto" as "IdConcepto",
			'CUS' AS "IdTipoComprobante",
			CUS."FolioComprobante" AS "IdComprobante",
			CUS."FolioComprobante" AS "FolioComprobante",
			CUS."IdMesa" AS "IdMesa",
			CUS."IdClasificacionTipoInstrumento"
			,CUS."IdTipoInstrumento"
			,CUS."IdInstrumento"
			,CUS."IdEmisor",
			'0001-01-01' as "FechaEmisionInstrumento"
			,CUS."IdMonedaEmision"
			,CUS."IdMonedaInstrumento" AS "IdMonedaOperacion"
			,CUS."Nemotecnico" AS "Nemotecnico"
			, CUS."IdBolsa" AS "IdBolsa"
			,CUS."IdBoveda"
			,CUS."EsFungible"
			,EMP."CodigoEmpleado" AS "CodigoAgente"
			,'' as "CodigoSucursal",
			CUS."Cantidad" as "Cantidad"
			, CUS."ValorRescate" as "ValorRescate"
			,CUS."IdTipoReajuste",
			CUS."PrecioCompra" AS "PrecioCompra",
			CUS."TasaCompra" AS "TasaCompra"
			,0 AS "TasaAgente"
			,0 AS "TasaEmision",
			CUS."MontoCompra" AS "MontoCompra"
			,0 AS "PrecioMercado"
			,CUS."CodigoIFRS" AS "CodigoIFRS"
			,'0' AS "FolioTransaccion",
			'' AS "IdMedioDePago"
			,PER."NombreCompleto" AS "EmitidoA"
			,0 AS "Comision"
			,0 AS "Gastos"
			,'E' as "IngresoEgreso",
			'N' AS "Nominal"
			,0 AS "IdOrden",
			0 AS "CodigoCorredorContraparte",CLI."IdPersona",
			0 AS "MontoPlazo",
			0 as FAC_AJUSTE,
			0 AS "FolioComprobanteReferencia",
			COALESCE(ECCS."Secuencia",0) AS  "SecuenciaEECC"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS CUS
					LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECCS ON ECCS."IdEventoCapital" = CUS."IdOperacion" and CUS."IdTipoOperacion" = 'RETOSA'
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
					LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente"
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
					WHERE  CUS."IdTipoInstrumento" = 'OSA'  
					AND CUS."IdCarga" = 0 
					AND CUS."IdTipoOperacion" IN ('RETTIT', 'RETOSA')
					AND CUS."IdTipoComprobante" NOT IN ('FAE','FEE')
			UNION ALL 
			SELECT  
			CUS."IdEmpresa"
			,'N' AS "EsIngresoManual"
			,'CUS' AS "IdModulo"
			,'CUS' AS "Origen"
			,CUS."SecuenciaMovimiento"  AS "Folio"
			,CUS."FechaOperacion" AS "FechaIngresoRegistro",	
			CUS."FechaOperacion" AS "FechaOperacion",
			'0001-01-01' AS "FechaVencimientoOperacion",
			CUS."FechaLiquidacion" AS "FechaLiquidacion", 
			CUS."PlazoEnDiasOperacion" as "Plazo",
			CUS."IdCondicionLiquidacion" AS "IdCondicionLiquidacion"
			,CUS."IdEstado" AS "IdEstado"
			,CUS."IdCliente"
			,CUS."IdPortafolio"
			,0 as "IdClienteCorredora"
			,0 AS "IdPortafolioCorredora",
			CUS."IdTipoOperacion" AS "IdTipoOperacion"
			,CUS."IdTipoProducto" AS "IdTipoProducto"
			,CUS."IdConcepto" as "IdConcepto",
			'CUS' AS "IdTipoComprobante",
			CUS."FolioComprobante" AS "IdComprobante",
			CUS."FolioComprobante" AS "FolioComprobante",
			CUS."IdMesa" AS "IdMesa",
			CUS."IdClasificacionTipoInstrumento"
			,CUS."IdTipoInstrumento"
			,CUS."IdInstrumento"
			,CUS."IdEmisor",
			'0001-01-01' as "FechaEmisionInstrumento"
			,CUS."IdMonedaEmision"
			,CUS."IdMonedaInstrumento" AS "IdMonedaOperacion"
			,CUS."Nemotecnico" AS "Nemotecnico"
			, CUS."IdBolsa" AS "IdBolsa"
			,CUS."IdBoveda"
			,CUS."EsFungible"
			,EMP."CodigoEmpleado" AS "CodigoAgente"
			,'' as "CodigoSucursal",
			CUS."Cantidad" as "Cantidad"
			, CUS."ValorRescate" as "ValorRescate"
			,CUS."IdTipoReajuste",
			CUS."PrecioCompra" AS "PrecioCompra",
			CUS."TasaCompra" AS "TasaCompra"
			,0 AS "TasaAgente"
			,0 AS "TasaEmision",
			CUS."MontoCompra" AS "MontoCompra"
			,0 AS "PrecioMercado"
			,CUS."CodigoIFRS" AS "CodigoIFRS"
			,'0' AS "FolioTransaccion",
			'' AS "IdMedioDePago"
			,PER."NombreCompleto" AS "EmitidoA"
			,0 AS "Comision"
			,0 AS "Gastos"
			,'I' as "IngresoEgreso",
			'N' AS "Nominal"
			,0 AS "IdOrden",
			0 AS "CodigoCorredorContraparte",CLI."IdPersona",
			0 AS "MontoPlazo",
			0 as FAC_AJUSTE,
			0 AS "FolioComprobanteReferencia",
			COALESCE(ECCS."Secuencia",0) AS  "SecuenciaEECC"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS CUS
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdMovimientoCarteraEnTransito" = CUS."IdMovimientoCarteraEnTransito" AND CAR."IdTipoOperacion" = 'INGOSA' AND CAR."EsPrimeraCartera" = 'S'
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECCS ON ECCS."IdCartera" = CAR."IdCartera"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE  CUS."IdTipoInstrumento" = 'OSA'  
		AND CUS."IdCarga" = 0 
		AND CUS."IdTipoOperacion" IN ('INGOSA','INGTIT')
		AND CUS."IdTipoComprobante" NOT IN ('FAE','FEE')
		
	),
	v_mov_fac_suscripcion_osas as (
		SELECT
				FAC."IdEmpresa",'N' AS "EsIngresoManual",'FAC' AS "IdModulo",'FAC' AS "Origen",MOV."SecuenciaMovimiento" AS "Folio",FAC."FechaIngresoRegistro",FAC."FechaMovimientoComprobante" AS "FechaOperacion",
				FAC."FechaVencimiento" AS "FechaVencimientoOperacion",
				FAC."FechaLiquidacion" AS "FechaLiquidacion",
				FAC."DiasPlazo" as "Plazo", FAC."IdCondicionLiquidacion",
				FAC."IdEstadoFacturacion" AS "IdEstado",
				FAC."IdCliente",FAC."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				FAC."IdTipoOperacion" AS "IdTipoOperacion",FAC."IdTipoProducto" AS "IdTipoProducto",
				FAC."IdConceptoMovimiento" AS "IdConcepto",
				FAC."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(FAC."IdComprobante",0) WHEN 0 THEN 0 ELSE FAC."IdComprobante" END as "IdComprobante",
				CASE COALESCE(CMBT."FolioComprobante",0) WHEN 0 THEN 0 ELSE CMBT."FolioComprobante" END as "FolioComprobante",
				FAC."IdMesa" AS "IdMesa",
				FAC."IdClasificacionTipoInstrumento",FAC."IdTipoInstrumento",FAC."IdInstrumento",FAC."IdEmisor",
				FAC."FechaMovimientoComprobante" AS "FechaEmisionInstrumento",FAC."IdMonedaEmision",FAC."IdMoneda" AS "IdMonedaOperacion",
				CASE 	
					WHEN FAC."IdTipoOperacion" IN ('ANSOSA') THEN FAC."NemotecnicoRelacionado"
					ELSE FAC."NemotecnicoInstrumento" END AS "Nemotecnico",
				FAC."IdBolsa" AS "IdBolsa",FAC."IdBoveda",FAC."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",
				COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	FAC."Cantidad" AS "Cantidad",
			   	0 AS "ValorRescate", CASE fac."IdClasificacionTipoInstrumento" WHEN 'RV' THEN INS."IdTipoReajuste" ELSE FAC."IdTipoReajuste" END AS "IdTipoReajuste",
				FAC."Precio" AS "PrecioCompra",
				FAC."Tasa" AS "TasaCompra",FAC."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
				FAC."MontoMovimientoComprobante" AS "MontoCompra",
			   	0 AS "PrecioMercado",
			   	FAC."CodigoIFRS" AS "CodigoIFRS",
			   		FAC."FolioTransaccion" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos", --UNIFICAR
			   	CASE WHEN FAC."IdTipoOperacion" IN ('SUSOSA') THEN 'I'
					WHEN FAC."IdTipoOperacion" IN ('ANSOSA') THEN 'E'
					ELSE '' END
			   	AS "IngresoEgreso",'N' AS "Nominal", 
			   	0 AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	0 AS "MontoPlazo",FAC."IdMovimientoComprobante" AS SEC_MOVTO_ASOC,
			   	COALESCE(CMBT."ValorTipoCambio",1) AS FAC_AJUSTE,
			   	0 AS "FolioComprobanteReferencia",
			   	COALESCE(ECCS."Secuencia", 0) AS  "SecuenciaEECC"
		FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOV ON MOV."IdMovimientoComprobante" = FAC."IdMovimientoComprobante" 
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBT ON FAC."IdComprobante" = CMBT."IdComprobante"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on FAC."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = FAC."IdEmpleadoEjecutivoPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = FAC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON FAC."IdCorredoraContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = FAC."IdBolsa"
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECCS ON ECCS."IdMovimientoComprobanteFac" = FAC."IdMovimientoComprobante" 
		WHERE FAC."IdTipoOperacion" IN ('SUSOSA')
		UNION ALL 
		SELECT
				FAC."IdEmpresa",'N' AS "EsIngresoManual",'FAC' AS "IdModulo",'FAC' AS "Origen",FAC."SecuenciaMovimiento" AS "Folio",FAC."FechaIngresoRegistro",FAC."FechaMovimientoComprobante" AS "FechaOperacion",
				FAC."FechaVencimiento" AS "FechaVencimientoOperacion",
				FAC."FechaLiquidacion" AS "FechaLiquidacion",
				FAC."DiasPlazo" as "Plazo", FAC."IdCondicionLiquidacion",
				FAC."IdEstadoFacturacion" AS "IdEstado",
				FAC."IdCliente",FAC."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				FAC."IdTipoOperacion" AS "IdTipoOperacion",FAC."IdTipoProducto" AS "IdTipoProducto",
				FAC."IdConceptoMovimiento" AS "IdConcepto",
				FAC."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(FAC."IdComprobante",0) WHEN 0 THEN 0 ELSE FAC."IdComprobante" END as "IdComprobante",
				CASE COALESCE(CMBT."FolioComprobante",0) WHEN 0 THEN 0 ELSE CMBT."FolioComprobante" END as "FolioComprobante",
				FAC."IdMesa" AS "IdMesa",
				FAC."IdClasificacionTipoInstrumento",FAC."IdTipoInstrumentoRelacionado",FAC."IdInstrumentoRelacionado",FAC."IdEmisor",
				FAC."FechaMovimientoComprobante" AS "FechaEmisionInstrumento",FAC."IdMonedaEmision",FAC."IdMoneda" AS "IdMonedaOperacion",
				CASE 	
					WHEN FAC."IdTipoOperacion" IN ('ANSOSA') THEN FAC."NemotecnicoInstrumento"
					ELSE FAC."NemotecnicoRelacionado" END AS "Nemotecnico",
				FAC."IdBolsa" AS "IdBolsa",FAC."IdBoveda",FAC."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",
				COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	FAC."Cantidad" AS "Cantidad",
			   	0 AS "ValorRescate", CASE fac."IdClasificacionTipoInstrumento" WHEN 'RV' THEN INS."IdTipoReajuste" ELSE FAC."IdTipoReajuste" END AS "IdTipoReajuste",
				FAC."Precio" AS "PrecioCompra",
				FAC."Tasa" AS "TasaCompra",FAC."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
				FAC."MontoMovimientoComprobante" AS "MontoCompra",
			   	0 AS "PrecioMercado",
			   	FAC."CodigoIFRS" AS "CodigoIFRS",
			   		FAC."FolioTransaccion" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos", --UNIFICAR
			   	CASE WHEN FAC."IdTipoOperacion" IN ('SUSOSA') THEN 'E'
					WHEN FAC."IdTipoOperacion" IN ('ANSOSA') THEN 'I'
					ELSE '' END
			   	AS "IngresoEgreso",'N' AS "Nominal", 
			   	0 AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona",
			   	0 AS "MontoPlazo",FAC."IdMovimientoComprobante" AS SEC_MOVTO_ASOC,
			   	COALESCE(CMBT."ValorTipoCambio",1) AS FAC_AJUSTE,
			   	0 AS "FolioComprobanteReferencia",
			   	COALESCE(ECCS."Secuencia", 0) AS  "SecuenciaEECC"
		FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" FAC
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" CMBT ON FAC."IdComprobante" = CMBT."IdComprobante"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on FAC."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = FAC."IdEmpleadoEjecutivoPortafolio"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = FAC."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON FAC."IdCorredoraContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = FAC."IdBolsa"
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" ECCS ON ECCS."IdMovimientoComprobanteFac" = FAC."IdMovimientoComprobante" 
		WHERE FAC."IdTipoOperacion" IN ('SUSOSA')
		
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
            
            	WHEN SINFO."Origen" = 'MIC' AND SINFO."IdTipoOperacion" = 'COMPAC' THEN 'S'
            	WHEN SINFO."Origen" = 'MIC' AND SINFO."IdTipoOperacion" = 'VENPAC' THEN 'E'
            	WHEN SINFO."Origen" = 'MIC' THEN ''
            	WHEN SINFO."Origen" = 'RET' AND SINFO."IdTipoOperacion" = 'COMPAC' THEN 'E' --RETROVENTA
            	WHEN SINFO."Origen" = 'RET' AND SINFO."IdTipoOperacion" = 'VENPAC' THEN 'S' --RETROCOMPRA
            	WHEN SINFO."Origen" = 'VCD' AND SINFO."IdTipoOperacion" = 'COMSIM' THEN 'E'
            	WHEN SINFO."Origen" = 'VCD' AND SINFO."IdTipoOperacion" = 'VENSIM' THEN 'S'
            	WHEN SINFO."Origen" = 'VCD' THEN ' '
            	WHEN SINFO."Origen" = 'GTA' THEN ' '
            	WHEN SINFO."Origen" = 'CTA' THEN ' ' 
            	WHEN SINFO."Origen" = 'TES' THEN ' ' 
            	WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" = 'VENSIM' THEN 'E'
            	WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" = 'COMSIM' THEN 'S'  
            	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" = 'ANSIMV' THEN 'S'
            	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" = 'ANSIMC' THEN 'E'
            	ELSE '' END AS    "IND_CUST_APLAZO", -- CHAR(1)
			CASE 
				WHEN SINFO."Origen" = 'FAC' AND SINFO."IdEstado" != 'ANU' THEN 'I'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdEstado" ='CRT' THEN 'I'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."Origen" = 'MIC' AND SINFO."IdEstado" != 'ANU' THEN 'I'
				WHEN SINFO."Origen" = 'VCD' THEN 'I'
				WHEN SINFO."Origen" = 'RET' THEN 'I'
				WHEN SINFO."IdEstado" ='ANU' THEN 'A'
				ELSE 'I'
			END AS "IND_ANULADO", -- CHAR(1)
            CASE  
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdEstado" ='CRT' THEN 'T'
				WHEN SINFO."Origen" = 'FAC' AND SINFO."IdEstado" = 'FAC' THEN 'T'
				WHEN SINFO."Origen" = 'FAC' AND SINFO."IdEstado" = 'ING' THEN 'I'
				WHEN SINFO."Origen" = 'MIC' AND SINFO."IdTipoOperacion" IN ('COMTER','VENTER') AND SINFO."IdEstado" = 'VIG' THEN 'T'
				WHEN SINFO."Origen" = 'MIC' AND SINFO."IdTipoOperacion" IN ('COMPAC','VENPAC') AND SINFO."IdEstado" = 'PEN' THEN 'I'
				WHEN SINFO."Origen" = 'MIC' AND SINFO."IdTipoOperacion" IN ('COMPAC','VENPAC') AND SINFO."IdEstado" = 'VIG' THEN 'T'
				WHEN SINFO."Origen" = 'MIC' AND SINFO."IdTipoOperacion" IN ('COMPAC','VENPAC') AND SINFO."IdEstado" = 'LIQ' THEN 'T'
				WHEN SINFO."Origen" = 'RET' THEN 'T'
				WHEN SINFO."Origen" = 'VCD' THEN 'T'
				WHEN SINFO."Origen" = 'GTA' AND SINFO."IdEstado" = 'PEN' THEN 'I'
				WHEN SINFO."Origen" = 'GTA' AND SINFO."IdEstado" = 'CRT' THEN 'T'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" ='LIQ' THEN 'T'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IdEstado" NOT IN ('LIQ','ING') THEN 'I'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdEstado" ='ING' THEN 'I'
				WHEN SINFO."Origen" = 'FMM' AND SINFO."IdEstado" ='CRT' THEN 'T'
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdEstado" ='PEN' THEN 'I'
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdEstado" ='CRT' THEN 'T'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdEstado" = 'PEN' THEN 'I'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdEstado" = 'CRT' THEN 'T'
				WHEN SINFO."IdEstado" ='ANU' THEN 'A'
				ELSE 'I'
			 END AS "EST_MOVTO", -- CHAR(1) 
            TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD')  AS "FEC_MVTO", -- CHAR(10)
            "Folio"  AS "SEC_MOVTO", -- DECIMAL
            CASE 
            	WHEN SINFO."Origen" = 'RET' THEN 'N' 
            	WHEN SINFO."Origen" = 'MIC' THEN 'N' 
            	WHEN SINFO."Origen" = 'VCD' THEN 'N' 
            	WHEN SINFO."Origen" = 'GTA' THEN 'N' 
            	WHEN SINFO."Origen" = 'NOM' THEN 'N' 
            	WHEN SINFO."Origen" = 'FAC' THEN 'N' 
            	WHEN SINFO."Origen" = 'CTA' THEN 'S' 
				WHEN SINFO."Origen" = 'TES' THEN 'S' 
				ELSE 'N'
			END AS "IND_SALDO_CTDO", -- CHAR(1)
           CASE 
				WHEN SINFO."Origen" = 'RET' THEN 'N' 
				WHEN SINFO."Origen" = 'MIC' THEN 'N' 
				WHEN SINFO."Origen" = 'VCD' THEN 'N' 
				WHEN SINFO."Origen" = 'GTA' THEN 'N' 
				WHEN SINFO."Origen" = 'FAC' THEN 'N' 
				WHEN SINFO."Origen" = 'NOM' THEN 'N' 
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" = 'MES' AND "IdTipoOperacion" IN ('COMPAC','VENPAC') THEN  'P'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" IN ('VCTSIV','VCTSIC') THEN  'T'
				ELSE 'N'
			END AS "IND_SALDO_APLAZO", -- CHAR(1) indicador de operacione a plazo N contado / P Plazo / Contado pa facturas
           CASE 
           		WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" = 'SUSOSA' AND SINFO."IngresoEgreso" = 'I' THEN 'E'
           		WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" = 'ANSOSA' AND SINFO."IngresoEgreso" = 'E' THEN 'S'
           		WHEN SINFO."Origen" = 'FAC' AND SINFO."Nominal" = 'S' THEN 'N'
           		WHEN SINFO."Origen" = 'NOM' AND SINFO."Nominal" = 'S' THEN 'N'
          		WHEN SINFO."Origen" = 'MIC' AND SINFO."Nominal" = 'S' THEN 'N'
          		WHEN SINFO."Origen" = 'RET' AND SINFO."Nominal" = 'S' THEN 'N'
          		WHEN SINFO."Origen" = 'GTA' THEN 'N'
           		WHEN SINFO."Origen" = 'CTA' THEN ' '  
           		WHEN SINFO."Origen" = 'TES' THEN ' '  
           		WHEN SINFO."Origen" = 'FAC' AND "IdTipoOperacion" IN ('COMSIM','VENSIM') THEN ''
           		WHEN "IngresoEgreso" = 'E' THEN 'S' 
           		WHEN "IngresoEgreso" = 'I' THEN 'E' 
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
            	WHEN SINFO."Origen" = 'FMM' THEN 'FM'
            	WHEN SINFO."Origen" = 'MIC' THEN 'MA'
            	WHEN SINFO."Origen" = 'RET' THEN 'MA'
            	WHEN SINFO."Origen" = 'NOM' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
            	WHEN SINFO."Origen" = 'NOM' AND SINFO."IngresoEgreso" = 'I' THEN 'IT'
            	WHEN SINFO."Origen" = 'NOM' AND SINFO."IngresoEgreso" = 'E' THEN 'ET'
            	WHEN SINFO."Origen" = 'GTA' AND SINFO."IdTipoOperacion" = 'INGGTA' THEN 'IT'
            	WHEN SINFO."Origen" = 'GTA' AND SINFO."IdTipoOperacion" = 'RETGTA' THEN 'ET'
            	WHEN SINFO."Origen" = 'VCD' AND SINFO."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN 'FD'
            	WHEN SINFO."Origen" = 'VCD' AND SINFO."IdTipoOperacion" IN ('SORLET') THEN 'ET'
              	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoComprobante" = 'FAE' THEN 'FD'
            	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoComprobante" = 'NCE' THEN 'ND'
            	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoComprobante" = 'NCR' THEN 'ND'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'IC'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'AD'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'AV'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'EC'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CD'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CV'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" = 'INGTIT' THEN 'IT'
            	WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" = 'RETTIT' THEN 'ET'
            	ELSE ''
            END AS "TIPO_COMP_ADJ", -- CHAR(2) 
            COALESCE(CASE
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE','FOX') THEN SINFO."FolioComprobanteDTE"
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdTipoComprobante" IN ('MES') THEN SINFO."IdComprobante"
            	WHEN SINFO."Origen" = 'CTA' and SINFO."FolioComprobante" != 0 THEN SINFO."FolioComprobante"
	            WHEN SINFO."Origen" = 'CTA' and (SINFO."FolioComprobante" = 0 OR SINFO."FolioComprobante" IS NULL) THEN SINFO."Folio"
	            WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 0
	            WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 0
	            WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE') THEN SINFO."FolioComprobanteDTE"
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN 0
	            WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoComprobante" IN ('FAE','FEE','NCE','NEE') THEN SINFO."FolioComprobanteDTE"
            	WHEN SINFO."Origen" = 'MIC' THEN SINFO."FolioComprobante"
            	WHEN SINFO."Origen" = 'RET' THEN SINFO."FolioComprobante"
            	WHEN SINFO."FolioComprobante" != '0' THEN SINFO."FolioComprobante"
	            ELSE 0
            END,0) AS "FOLIO_COMP_ADJ", -- DECIMAL
            --CASE WHEN  SINFO."Origen" = 'TRANSITO'  then "Folio"  ELSE  0  END  AS "SEC_TRASPASO", -- DECIMAL
			0 AS "SEC_TRASPASO", -- DECIMAL
			CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				WHEN SINFO."Origen" IN ('FMM','FAC','MIC','RET','VCD','GTA') THEN 'CU'
				  WHEN SINFO."Origen" IN ('TES', 'CTA') THEN 'TE'
			      ELSE 'CU'
			  END   AS "UNIDAD_DES", -- CHAR(2)
            CASE
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECSL' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABSL'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'DIPE'
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'DIUS'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'PEDI'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'PEUS'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'TES' AND SINFO."IdConcepto" = 'CIECDV' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'REPDIN' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'REPA'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'REPDIN' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'REUS'
            	
            	WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'FDMOOT' THEN 'MOOT'
            	--VENCIMIENTO PACTOS
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'E' AND "IdModulo" = 'TES' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'E' AND "IdModulo" IN('PLA','MRF') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CRCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'E' AND "IdModulo" = 'TES' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'E' AND "IdModulo" IN('PLA','MRF') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CRCD'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'I' AND "IdModulo" = 'TES' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'I' AND "IdModulo" IN('PLA','MRF') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ARVD'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'I' AND "IdModulo" = 'TES' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDPAC' AND SINFO."IngresoEgreso" = 'I' AND "IdModulo"  IN('PLA','MRF') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ARVR'
				--VENCIMIENTO IF
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCDTIT' AND SINFO."IngresoEgreso" = 'I' AND (SINFO."Nemotecnico" IN('PAGARE R','PAGARE NR','PDBC')) THEN 'VCIF'

				--HOMOLOGAR CON VENCIMIENTOS
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'ANPCME' AND SINFO."IngresoEgreso" = 'I' AND "IdModulo"  IN('TES') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'ANPCME' AND SINFO."IngresoEgreso" = 'I' THEN 'ARVR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'ANPCME' AND SINFO."IngresoEgreso" = 'E' AND "IdModulo" IN('TES') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'ANPCME' AND SINFO."IngresoEgreso" = 'E' THEN 'CRCR'
				
				--MANUALES Y TESORERIA
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
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA','COMMON') THEN 'FCVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMETX','COMSIM','COMOSA','COMMON') THEN 'FCRV'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA','VENMON') THEN 'FVVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'FAE' AND "IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENSIM','VENOSA','VENMON') THEN 'FVRV'
				--NOTAS DE CREDITO RV
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA','ANVMON') THEN 'NVVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA','ANVMON') THEN 'NRVV'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdMonedaOperacion" = 'USD' AND "IdTipoOperacion" IN ('ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA','ANCMON') THEN 'NCVU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."EsIngresoManual" = 'N' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdTipoComprobante" = 'NCE' AND "IdTipoOperacion" IN ('ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA','ANCMON') THEN 'NRVC'
				
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
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdConcepto" IN ('CIECCC','CICCMI','CICCAM') THEN 'PACU'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdTipoOperacion" = 'ANSIMV' THEN 'NTPC'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'CUS' AND SINFO."IdTipoOperacion" = 'ANSIMC' THEN 'NTPV'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCTSIC' THEN 'CCTP' --VENCIMIENTO COMPRA A PLAZO
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdConcepto" = 'VCTSIV' THEN 'CVTP' --VENCIMIENTO VENTA A PLAZO
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'I' THEN 'ADCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'I' THEN 'ABMN'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'E' THEN 'CCCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'E' THEN 'CDCR'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'I' THEN 'ADCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'I' THEN 'AVCR'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'CLP' AND SINFO."IngresoEgreso" = 'E' THEN 'CCCR' --SERA CAMN?
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMPAC' AND SINFO."IdMonedaOperacion" = 'USD' AND SINFO."IngresoEgreso" = 'E' THEN 'CDCR'
				
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMTER' AND SINFO."IngresoEgreso" = 'E' THEN 'CAMN' --PATA CORREDORA
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'COMTER' AND SINFO."IngresoEgreso" = 'I' THEN 'AVFR' --PATA CLIENTE
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENTER' AND SINFO."IngresoEgreso" = 'E' THEN 'CCFR' --PATA CLIENTE
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IdModulo" = 'MRF' AND SINFO."IdTipoOperacion" = 'VENTER' AND SINFO."IngresoEgreso" = 'I' THEN 'ABMN' --PATA CORREDORA
				
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

				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'IGRV'
				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'FM' THEN 'IGFM'
				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'IGRF'
				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'IGIF'
				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RGRV'
				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'FM' THEN 'RGFM'
				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RGRF'
				WHEN SINFO."Origen" = 'GTA' and SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RGIF'

				WHEN SINFO."Origen" = 'NOM' and SINFO."IdTipoOperacion" = 'EMILIB' THEN 'EMLI' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IdTipoOperacion" = 'DIVACC' THEN 'DIAC' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IdConcepto" = 'INGPRT' THEN 'PTIP' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IdConcepto" = 'VCDPFG' THEN 'PTIL'
				WHEN SINFO."Origen" = 'NOM' and SINFO."IdConcepto" = 'ANTPFG' THEN 'PTLA'

				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'I' AND SINFO."Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RIRV' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'I' AND SINFO."Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RIRF' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'I' AND SINFO."Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RIIF' 
				
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'E' AND SINFO."Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'IIRV' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'E' AND SINFO."Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'IIRF' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'E' AND SINFO."Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'IIIF' 

				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'I' AND SINFO."Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'IIRV'
				WHEN SINFO."Origen" = 'CUS' and SINFO."IngresoEgreso" = 'I' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' AND SINFO."IdTipoOperacion" NOT IN ('INGOSA','RETOSA') THEN 'IIRV'  
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'I' AND SINFO."Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'IIRF' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'I' AND SINFO."Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'IIIF' 
				
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'E' AND SINFO."Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'RIRV'
				WHEN SINFO."Origen" = 'CUS' and SINFO."IngresoEgreso" = 'E' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' AND SINFO."IdTipoOperacion" NOT IN ('INGOSA','RETOSA') THEN 'RIRV'  
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'E' AND SINFO."Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RIRF' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IngresoEgreso" = 'E' AND SINFO."Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RIIF' 


				WHEN SINFO."Origen" = 'VCD' and SINFO."IdTipoOperacion" = 'COMSIM' THEN 'TVTP'
				WHEN SINFO."Origen" = 'VCD' and SINFO."IdTipoOperacion" = 'VENSIM' THEN 'TCTP'
				WHEN SINFO."Origen" = 'VCD' and SINFO."IdTipoOperacion" = 'SORLET' THEN 'RTSL'
				WHEN SINFO."Origen" = 'VCD' and SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VTIF'
				WHEN SINFO."Origen" = 'VCD' and SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VTRF'
				
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" = 'VENSIM' THEN 'VITP'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" = 'COMSIM' THEN 'CITP'

				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('COMACN','COMACX','COMCFI','COMCFX','COMETN','COMOSA','COMMON') THEN 'CIRV'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('COMRF') THEN 'CIRF'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('COMETX') THEN 'CIET'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('COMIIF') THEN 'CIIF'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANSIMC') THEN 'NVTP'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANSIMV') THEN 'NCTP'
				
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('VENACN','VENACX','VENCFI','VENCFX','VENETN','VENETX','VENOSA') THEN 'VIRV'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('VENRF') THEN 'VIRF'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('VENIIF') THEN 'VIIF'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('VENETX') THEN 'VIET'
				
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANVACN','ANVACX','ANVCFI','ANVCFX','ANVETN','ANVETX','ANVSIM','ANVOSA','ANVMON') THEN 'NVRV'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANVRF') THEN 'NVRF'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANVIIF') THEN 'NVIF'
				
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANCACN','ANCACX','ANCCFI','ANCCFX','ANCETN','ANCETX','ANCSIM','ANCOSA','ANCMON') THEN 'NCRV'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANCRF') THEN 'NCRF'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('ANCIIF') THEN 'NCIF'
				WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 'EOSA'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA') THEN 'EOSA'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('RETOSA') THEN 'VOSA'
				
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'CRIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'CRIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'CRIN'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'CRRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'CRRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'CRRN'

				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VRIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VRIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VRIN'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VRRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VRRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VRRN'

				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMTER') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'CFIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'CFIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'CFIN'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMTER') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'CFRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'CFRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('COMTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'CFRN'

				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENTER') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VFIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VFIC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'VFIN'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENTER') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VFRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' AND CORR."IdCliente" = SINFO."IdCliente" THEN 'VFRC'
				WHEN SINFO."Origen" = 'MIC' and SINFO."IdTipoOperacion" IN ('VENTER') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'VFRN'
				
				
				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RVIC'  --RETROVENTA
				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RVIN'
				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RVRC'
				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('COMPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RVRN'

				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RCIC'
				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'IF' THEN 'RCIN'
				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'N' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RCRC'
				WHEN SINFO."Origen" = 'RET' and SINFO."IdTipoOperacion" IN ('VENPAC') AND "Nominal" = 'S' AND SINFO."IdClasificacionTipoInstrumento" = 'RF' THEN 'RCRN'

				--WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('INGOSA') THEN 'EOSA' -> IdTipoOperacion = SUSOSA en MovimientoComprobante
				--WHEN SINFO."Origen" = 'ICU' and SINFO."IdTipoOperacion" IN ('DIVACC','DIVACG') THEN 'DIAC'  -> IdTipoOperacion = DIVACG DIVACC en MovimientoComprobante

				WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'ABMN'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'CAMN'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'ABDO'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'USD' THEN 'CADO'
				WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'I' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'ABEU'
            	WHEN SINFO."Origen" = 'TES' AND SINFO."IngresoEgreso" = 'E' AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'CAEU'
            	
            	ELSE '' END AS "COD_MOVTO", -- CHAR(4) --

            
            COALESCE(LEFT(SINFO."Nemotecnico",16),'') AS "NEMO", -- CHAR(16)
            'B' 									AS "CALC_MTO_APLAZO", -- CHAR(1)
            
            'L'  									AS "CALC_MTO_CTDO", -- CHAR(1)
            CASE 
    	        WHEN SINFO."Origen" in ('FAC','NOM') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'COMIIF' THEN CAST(SINFO."Cantidad" AS DECIMAL(17,6))
    	        WHEN SINFO."Origen" in ('MIC','RET','VCD','GTA') AND SINFO."Nemotecnico" = 'PDBC' THEN CAST(SINFO."Cantidad" AS DECIMAL(17,6))
            ELSE CAST(SINFO."Cantidad" AS DECIMAL(17,6)) END AS "CANTIDAD", -- DECIMAL

            COALESCE(LEFT(SINFO."CodigoAgente",4),'    ') AS "COD_AGENTE", -- CHAR(4)
            
            '    ' AS "COD_BCO_PAGO", -- CHAR(4) 
            
            COALESCE(SINFO."IdBoveda",'')		  AS "COD_BOVEDA", -- CHAR(3)
            "CodigoCorredorContraparte"			  AS "COD_CORR_CONTRA", -- DECIMAL 
            ''						  				AS "COD_MESA", -- CHAR(4)
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN '    ' 
				WHEN SINFO."Origen" = 'NOM' and SINFO."IdTipoOperacion" IN ('DIVACC', 'EMILIB') THEN '    ' 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN  '    ' 
				ELSE '0' END AS "COD_OPE", -- CHAR(4)
            
            CASE SINFO."IdMonedaOperacion" WHEN 'USD' THEN 'VD' ELSE '' END AS "COD_RUBRO", -- CHAR(4) -- analizar este campo
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN '    ' 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN '    '
				ELSE COALESCE(LEFT(SINFO."CodigoSucursal",4),'    ') END AS "COD_SUC", -- CHAR(4)
            
            CASE WHEN SINFO."Comision" > 9999999999 THEN 9999999999 ELSE COALESCE(SINFO."Comision",0.0) END AS "COMISION", -- DECIMAL
            
            
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
						
						WHEN SINFO."Origen" IN ('GTA') THEN RIGHT(SINFO."IdConcepto",2)
						--NOM NO POR AHORA
						ELSE '  ' 
					END 
			END AS "CONCEPTO_MVTO", -- CHAR(2)
            
            cast(0 as decimal(14, 4))					  AS "DERECHOS", -- DECIMAL  
            
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('DIVACC','EMILIB') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				ELSE COALESCE(left(SINFO."IdEmisor",10),'')	END	  AS "EMISOR", -- CHAR(10)
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('DIVACC','EMILIB') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				ELSE LEFT(SINFO."EmitidoA",39)  END		  AS "EMITIDO_A", -- CHAR(40)
            
            FAC_AJUSTE  AS "FAC_AJUSTE", -- DECIMAL
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN '0001-01-01'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN '0001-01-01'
				ELSE TO_VARCHAR(SINFO."FechaEmisionInstrumento", 'YYYY-MM-DD') END AS "FEC_EMI", -- CHAR(10)
           
            TO_VARCHAR("FechaIngresoRegistro", 'YYYY-MM-DD')  	  AS "FEC_ING_MVTO", -- CHAR(10)
            CASE 
	        	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN '0001-01-01'
            WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('VENSIM','COMSIM') AND SINFO."IdTipoInstrumento" = 'SIM' THEN
				TO_VARCHAR(SINFO."FechaVencimientoOperacion", 'YYYY-MM-DD')
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN '0001-01-01'
            ELSE TO_VARCHAR(SINFO."FechaLiquidacion"	, 'YYYY-MM-DD')	
            END	AS "FEC_LIQUID", -- CHAR(10)

            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN '0001-01-01' 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA','INGTIT','RETTIT') THEN '0001-01-01' 
			ELSE TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') END AS "FEC_TRANS", -- CHAR(10) -- traer info del palo
            
            CASE WHEN SINFO."Origen" IN ('MIC','RET','VCD') THEN TO_VARCHAR(SINFO."FechaVencimientoInstrumento", 'YYYY-MM-DD')
            ELSE TO_VARCHAR(SINFO."FechaVencimientoOperacion", 'YYYY-MM-DD') 	END	 AS "FEC_VCTO", -- CHAR(10)
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 0 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 0
			ELSE SINFO."FolioComprobanteReferencia" END  AS "FOLIO_COMP_CONT", -- DECIMAL -- folio factura relacionada
            
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
			  
            CASE WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" = 'VENSIM' THEN 0 
            WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" = 'COMSIM' AND SINFO."IdTipoInstrumento" = 'SIM' THEN 0
            WHEN COALESCE(SINFO."Gastos",0.0) > 9999999999 THEN 9999999999
            ELSE COALESCE(SINFO."Gastos",0.0) END AS "GASTOS", -- DECIMAL
            ''  AS "IDENTIFIC_DOCTO", -- CHAR(20) -- instruccion de pago DCTO texto libre
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA') THEN ''
				ELSE  COALESCE(SINFO."IdBolsa",'')	END				  AS "IND_BOLSA", -- CHAR(3)
            '' AS "IND_CCTE_APLAZO", -- CHAR(1)
		   CASE 
           		WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'I' THEN 'A'
				WHEN SINFO."Origen" = 'CTA' AND SINFO."IngresoEgreso" = 'E' THEN 'C'
				ELSE ' '
			END  AS "IND_CCTE_CTDO", -- CHAR(1) -- 
            CASE 
	            WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
            	  WHEN SINFO."IdMedioDePago" = 'CHE' THEN 'O'
				  ELSE 'N'
			  END 	 AS "IND_CHEQUE", -- CHAR(1)  ver si aun aplica  A /N / O
			  
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
				ELSE  'N' 	END							 AS "IND_COBRO_SEP", -- CHAR(1)
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN '' 
				ELSE  'N' 	END							 AS "IND_CORREC", -- CHAR(1)
            
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
			 	ELSE  'N'	END								 AS "IND_EXENCION", -- CHAR(1)
            'M'										 AS "IND_FUS_CAR_DIA", -- CHAR(1)
            'S' 									 AS "IND_FUSION_TRAS", -- CHAR(1)
            CASE WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN '' ELSE 'S' END										 AS "IND_GASTO_FAC", -- CHAR(1)
            
            'S'  AS "IND_HAB_CARTERA", -- CHAR(1)
            'N'  AS "IND_INC_GAS_CAR", -- CHAR(1)
            
            CASE
	            WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
            	WHEN SINFO."Origen" = 'FAC' and SINFO."IdTipoOperacion" IN ('VENSIM','COMSIM') THEN 'TP' 
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC')  THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
            	--faltan vencimientos de simultanea
            	WHEN COALESCE(CODLIQ."Codigo",'') = 'CN' THEN  '' 
            	ELSE  COALESCE(CODLIQ."Codigo",'') END  AS "IND_LIQUID", -- CHAR(2) 
           CASE WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN 'C' 
		   		WHEN "IngresoEgreso" = 'I' THEN 'V' 
				ELSE 'C' END AS IND_PAGO_COBRO,
            'F'  AS "IND_REBAJ_CAR", -- CHAR(1)
            ' '  AS "IND_RECAM_CAR", -- CHAR(1)
            'N'  AS "IND_REPAR_CAR", -- CHAR(1)
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN '' 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
			ELSE 'N' END  AS "IND_RETENIDO", -- CHAR(1)
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN '' 
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN '' 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN ''
				ELSE 'C' END  AS "IND_RIESGO_CLI", -- CHAR(1)
        	CASE WHEN SINFO."IdTipoProducto" in ('MERF','MEFW') THEN 'F'
			ELSE ' '
				  END 	  AS "IND_RUEDA", -- CHAR(1)
				  
			CASE WHEN SINFO."Origen" IN ('CTA' , 'TES') AND SINFO."IdMonedaOperacion" = 'CLP' THEN 'C'
		    	  WHEN SINFO."Origen" IN ('CTA' , 'TES') AND SINFO."IdMonedaOperacion" = 'USD' THEN 'U'
		    	  WHEN SINFO."Origen" IN ('CTA' , 'TES') AND SINFO."IdMonedaOperacion" = 'EUR' THEN 'V'
		    	  WHEN SINFO."Origen" IN ('FMM','FAC','MIC','RET','VCD','GTA','NOM','CUS') THEN 'T'
		    	  ELSE ''
		      END AS IND_TIT_CCTE 	,
		      
		      --char](1) NOT NULL
			CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 'C'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 'C'
				WHEN SINFO."Origen" IN ('FAC') AND "IdTipoOperacion" IN ('COMSIM','VENSIM') THEN 'N'
				WHEN SINFO."Origen" IN ( 'FMM') THEN 'C'
				WHEN SINFO."Origen" IN ('FAC','MIC','RET','VCD','GTA','NOM','CUS') AND SINFO."Nominal" = 'S' THEN 'N'
				WHEN SINFO."Origen" IN ('FAC','MIC','RET','VCD','GTA','NOM','CUS') AND SINFO."Nominal" = 'N' THEN 'C'
				  ELSE ' '
			  END AS IND_TIT_CUS ,											--char](1) NOT NULL
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 'N' 
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN 'N' 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 'N'
				ELSE 'N' END  AS "IND_TRANSF", -- CHAR(1)
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 'N' 
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN 'N'
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 'N' 
				ELSE 'N' END  AS "IND_TRASPASO", -- CHAR(1)   N   / S si es traspaso ver de donde sacar
            0  AS "LIN_COMP_ADJ", -- DECIMAL
            0  AS "LIN_ORDEN", -- DECIMAL
            COALESCE(CASE WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" = 'DIVACC' THEN 'CM' 
						WHEN SINFO."IdClasificacionTipoInstrumento" IN ('RF', 'IF', 'FM') THEN "IdClasificacionTipoInstrumento"
            			WHEN SINFO."IdTipoInstrumento" IN ('ACN','OSA','SIM') THEN 'AC'
            			WHEN SINFO."IdClasificacionTipoInstrumento" = 'SP' THEN 'MO'
            			WHEN SINFO."IdTipoInstrumento" = 'FWD' THEN 'FU'
            			WHEN SINFO."IdTipoInstrumento" = 'CFI' THEN 'CF'
            			WHEN SINFO."IdTipoInstrumento" = 'CFX' THEN 'CO'
            			WHEN SINFO."IdTipoInstrumento" = 'ETN' THEN 'EN'
            			WHEN SINFO."IdTipoInstrumento" = 'ETX' THEN 'ET'
            			WHEN SINFO."IdTipoInstrumento" = 'ACX' THEN 'AO'
            			WHEN SINFO."IdTipoInstrumento" = 'CFX' THEN 'CO'
            			WHEN SINFO."IdTipoInstrumento" = 'MIL' THEN 'ST'
	          			WHEN SINFO."IdTipoInstrumento" = 'MON' THEN 'MO'
            			END,'') AS "MERCADO",
           CAST(
	           CASE
	            WHEN SINFO."Origen" in ('FAC') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'VENIIF' THEN ROUND(SINFO."MontoCompra",0, ROUND_HALF_UP) 
	            WHEN SINFO."Origen" in ('FAC') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'COMIIF' THEN ROUND((SINFO."MontoCompra"/SINFO."Cantidad") ,0, ROUND_HALF_UP) 
	            WHEN SINFO."Origen" in ('NOM') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'VENIIF' THEN ROUND(SINFO."MontoCompra",0, ROUND_HALF_UP) 
	            WHEN SINFO."Origen" in ('NOM') AND SINFO."Nemotecnico" = 'PDBC' AND SINFO."IdTipoOperacion" = 'COMIIF' THEN ROUND((SINFO."MontoCompra"/SINFO."Cantidad") ,0, ROUND_HALF_UP) 
	            WHEN SINFO."Origen" in ('MIC') AND SINFO."Nemotecnico" = 'PDBC' THEN ROUND((SINFO."MontoCompra"/SINFO."Cantidad") ,0, ROUND_HALF_UP) 
	            WHEN SINFO."Origen" in ('RET') AND SINFO."Nemotecnico" = 'PDBC' THEN ROUND((SINFO."MontoCompra"/SINFO."Cantidad") ,0, ROUND_HALF_UP) 
   	            WHEN SINFO."Origen" in ('VCD') AND SINFO."IdTipoOperacion" IN ('COMSIM','VENSIM') THEN SINFO."MontoPlazo" 
   	            WHEN SINFO."Origen" in ('VCD') THEN SINFO."MontoCompra" 
   	            WHEN SINFO."Origen" in ('GTA') THEN SINFO."MontoCompra" 
	            WHEN SINFO."Origen" in ('RET') THEN SINFO."MontoPlazo"
	           ELSE SINFO."MontoCompra" END
	           	AS DECIMAL(17,2)) AS "MONTO", -- DECIMAL
           cast(	
           	CASE 
	             WHEN SINFO."Origen" in ('MIC') AND SINFO."IdTipoOperacion" IN ('COMTER','VENTER') THEN 0
                 WHEN SINFO."Origen" in ('MIC','RET','VCD') AND SINFO."Nemotecnico" = 'PDBC' THEN ROUND((SINFO."MontoPlazo"/SINFO."Cantidad") ,0, ROUND_HALF_UP) 
                 WHEN SINFO."Origen" in ('MIC','RET','VCD') THEN SINFO."MontoPlazo"
                 WHEN SINFO."Origen" IN ('FAC') THEN SINFO."MontoPlazo"
          		 WHEN SINFO."Origen" IN ('CTA') THEN SINFO."MontoPlazo"
				  ELSE 0 
			  END   as decimal(17, 2)) as "MONTO_APLAZO", -- DECIMAL
			  
            CASE 
            	WHEN SINFO."Origen" in ('FAC') AND SINFO."Nemotecnico" IN ('PAGARE R','PAGARE NR','PDBC') AND SINFO."IdTipoOperacion" IN ('COMIIF','VENIIF') THEN SINFO."MontoCompra" 
            	WHEN SINFO."Origen" in ('MIC','RET') AND SINFO."Nemotecnico" = 'PDBC' THEN ROUND((SINFO."MontoCompra"/SINFO."Cantidad") ,0, ROUND_HALF_UP) 
	            WHEN SINFO."Origen" in ('MIC','RET','VCD') AND SINFO."Nemotecnico" IN ('PAGARE R','PAGARE NR') THEN SINFO."MontoCompra" 
            ELSE 0 END AS "MONTO_CAP_COMP", -- DECIMAL
            0 AS "MONTO_INTERES", -- DECIMAL
            
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 0 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 0
            	ELSE COALESCE(TO_DECIMAL(SINFO."PrecioCompra" ,17, 6),0) END AS "PRECIO", -- DECIMAL
            
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
				WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGTIT','RETTIT') THEN 'INAC' 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN '' 
				ELSE COALESCE(SINFO."IdTipoProducto",'')
			END AS "PRODUCTO", -- CHAR(4)
            
            COALESCE("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(PCORR."DocumentoIdentidad"),'0-0') AS "RUT_CORR", -- CHAR(11)
            
            CASE WHEN SINFO."Origen" IN ('MIC','RET') THEN SINFO."FolioComprobante" ELSE 0 END  AS "SEC_CAR_COMPROM", -- DECIMAL
            SINFO."SecuenciaEECC"  AS "SEC_EVE", -- DECIMAL
            
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 0 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 0
            	ELSE SEC_MOVTO_ASOC END AS "SEC_MOVTO_ASOC", -- DECIMAL
            
            COALESCE("IdOrden",0) AS "SEC_ORDEN", -- DECIMAL
            
            CASE WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN 1 
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN 1 
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN 1
            	ELSE 0 END AS "SEC_RUT_CORR", -- DECIMAL
            0  AS "SPREAD", -- DECIMAL
            CAST(CASE WHEN SINFO."TasaCompra" > 999 THEN 999 ELSE SINFO."TasaCompra" END AS DECIMAL(7,4))	  AS "TASA", -- DECIMAL
            tO_decimal(SINFO."TasaAgente",7, 4)	  AS "TASA_AGE", -- DECIMAL
            CASE WHEN SINFO."Origen" IN ('MIC','RET') THEN SINFO."TasaEmision" ELSE  0 END   AS "TASA_COMPROM", -- DECIMAL 
            ''  AS "TIPO_FAM", -- CHAR(2)  tipo instrumento
  		    CASE
  		    	WHEN SINFO."Origen" = 'FAC' AND "IdTipoOperacion" IN ('COMSIM','VENSIM') THEN 'PB' 
  		    	WHEN SINFO."Origen" = 'GTA' AND SINFO."IdClasificacionTipoInstrumento" = 'RV' THEN 'MN'
  		    	WHEN SINFO."IdClasificacionTipoInstrumento" = 'RV' AND left(SINFO."IdTipoReajuste",2) = 'CL' THEN 'CL'
		    	WHEN left(SINFO."IdTipoReajuste",2) = 'CL' THEN 'NR'
  		    	ELSE COALESCE(left(SINFO."IdTipoReajuste",2),'') END  AS TIPO_REAJ, -- CHAR 2
            
            CASE WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN '' 
				 WHEN SINFO."Origen" IN ('NOM','GTA') THEN 'CU'
				  WHEN SINFO."Origen" IN ('TES', 'CTA') THEN 'TE'
				  WHEN SINFO."Origen" IN ('MIC','RET') THEN 'OP'
			      ELSE ''
			  END   AS "UNIDAD_SOL", -- CHAR(2)
            
            0  AS "USUARIO_CER", -- DECIMAL
            0  AS "USUARIO_SOL", -- DECIMAL
            
            cast(SINFO."ValorRescate" as decimal(15,4)) AS "VAL_RESC", -- DECIMAL
            0  AS "VALOR_UNIT", -- DECIMAL -- info de evento capital
            
            CASE
            	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('VENSIM','COMSIM') THEN
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
            CASE WHEN SINFO."Origen" = 'FAC' AND "IdTipoOperacion" IN ('COMSIM','VENSIM') THEN ''
            	WHEN SINFO."Origen" = 'FAC' AND SINFO."IdTipoOperacion" IN ('SUSOSA','ANSOSA') THEN ''
				WHEN SINFO."Origen" = 'NOM' AND SINFO."IdTipoOperacion" IN ('EMILIB','DIVACC') THEN ''
				WHEN SINFO."Origen" = 'CUS' AND SINFO."IdTipoOperacion" IN ('INGOSA','RETOSA') THEN '' 
            ELSE COALESCE(SINFO."EsFungible" ,' ') END AS "IND_FUNGIBILIDAD", -- CHAR(1)
            CASE SINFO."Origen" WHEN 'MIC' THEN 0 ELSE cast(SINFO."TasaEmision" as decimal(10, 4)) END AS "TASA_EMISION", -- DECIMAL
            0					 AS "SPREAD_TASA_EMISION", -- DECIMAL
            ''					 AS "MODALIDAD_PAGO", -- CHAR(1)
            ''					 AS "PERIODICIDAD_PAGO", -- CHAR(1)
            CASE WHEN SINFO."IdTipoOperacion" IN ('VENACN') AND SINFO."IdConcepto" = 'VNSIMU' THEN 'TS'
	            WHEN SINFO."IdTipoOperacion" IN ('COMACN') AND SINFO."IdConcepto" = 'CMSIMU' THEN 'TS'
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
            ''  AS "IND_EXIMIDO", -- CHAR(1)
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
				CTA."SecuenciaEECC",CTA."FolioComprobante" AS "FolioComprobanteDTE",CTA."IdPersona",CTA."MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, CTA."FolioComprobanteReferencia"
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
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",FMM."IdPersona",0 AS "MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, FMM."FolioComprobanteReferencia"
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
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",GTAING."IdPersona",0 AS "MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, GTAING."FolioComprobanteReferencia"
		FROM v_garantias GTAING
		UNION
		SELECT  ven."IdEmpresa",ven."EsIngresoManual",ven."IdModulo",ven."Origen",ven."Folio",ven."FechaIngresoRegistro",ven."FechaOperacion",
				ven."FechaVencimientoOperacion",ven."FechaLiquidacion",ven."Plazo",
				ven."IdCondicionLiquidacion",ven."IdEstado",ven."IdCliente",ven."IdPortafolio",ven."IdClienteCorredora",ven."IdPortafolioCorredora",
				ven."IdTipoOperacion",ven."IdTipoProducto",ven."IdConcepto",ven."IdTipoComprobante",ven."FolioComprobante",ven."IdMesa",
				ven."IdClasificacionTipoInstrumento",ven."IdTipoInstrumento",ven."IdInstrumento",ven."FechaEmisionInstrumento",ven."IdMonedaEmision",
				ven."IdMonedaOperacion",ven."IdEmisor",ven."Nemotecnico",ven."EsFungible",ven."IdBolsa",ven."IdBoveda",ven."CodigoAgente",ven."CodigoSucursal",
				ven."IdTipoReajuste",ven."Cantidad",ven."ValorRescate",
				ven."PrecioCompra",ven."TasaCompra",ven."TasaAgente",ven."TasaEmision",ven."MontoCompra",ven."PrecioMercado",ven."CodigoIFRS",
				ven."FolioTransaccion",ven."IdMedioDePago",ven."EmitidoA",
				ven."Comision",
				ven."Gastos",
				ven."IngresoEgreso",ven."IdComprobante",ven."Nominal",ven."IdOrden",ven."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",ven."IdPersona",ven."MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, ven."FolioComprobanteReferencia"
		FROM v_vencimientos ven
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
				CASE WHEN cing."IdComprobante" IS NOT NULL AND FAC."MontoComprobante" != 0 THEN ROUND(FAC."Comision" * cing."MontoCompra" / FAC."MontoComprobante",0 ) ELSE cing."Comision" END AS "Comision",
				CASE WHEN cing."IdComprobante" IS NOT NULL AND FAC."MontoComprobante" != 0 THEN ROUND(FAC."Gastos" * cing."MontoCompra" / FAC."MontoComprobante",0 ) ELSE cing."Gastos" END AS "Gastos",
				cing."IngresoEgreso",cing."IdComprobante",cing."Nominal",cing."IdOrden",cing."CodigoCorredorContraparte",
				cing."SecuenciaEECC" AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",cing."IdPersona","MontoPlazo" AS "MontoPlazo",SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, cing."FolioComprobanteReferencia"
		FROM v_movimiento_comprobante cing
		LEFT JOIN v_comisiones FAC ON cing."IdComprobante" = FAC."IdComprobante" AND cing."IdTipoComprobante"  IN ('FAE','NCE','FEE')
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
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",tes."IdPersona",0 AS "MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, tes."FolioComprobanteReferencia"
		FROM v_CmbtesTes tes
		UNION
		SELECT  mic."IdEmpresa",mic."EsIngresoManual",mic."IdModulo",mic."Origen",mic."Folio",mic."FechaIngresoRegistro",mic."FechaOperacion",
				mic."FechaVencimientoOperacion",mic."FechaLiquidacion",mic."Plazo",
				mic."IdCondicionLiquidacion",mic."IdEstado",mic."IdCliente",mic."IdPortafolio",mic."IdClienteCorredora",mic."IdPortafolioCorredora",
				mic."IdTipoOperacion",mic."IdTipoProducto",mic."IdConcepto",mic."IdTipoComprobante",mic."FolioComprobante",mic."IdMesa",
				mic."IdClasificacionTipoInstrumento",mic."IdTipoInstrumento",mic."IdInstrumento",mic."FechaEmisionInstrumento",mic."IdMonedaEmision",
				mic."IdMonedaOperacion",mic."IdEmisor",mic."Nemotecnico",mic."EsFungible",mic."IdBolsa",mic."IdBoveda",mic."CodigoAgente",mic."CodigoSucursal",
				mic."IdTipoReajuste",mic."Cantidad",mic."ValorRescate",
				mic."PrecioCompra",mic."TasaCompra",mic."TasaAgente",mic."TasaEmision",mic."MontoCompra",mic."PrecioMercado",mic."CodigoIFRS",
				mic."FolioTransaccion",mic."IdMedioDePago",mic."EmitidoA",
				mic."Comision",mic."Gastos",
				mic."IngresoEgreso",mic."IdComprobante",mic."Nominal",mic."IdOrden",mic."CodigoCorredorContraparte",
				mic."SecuenciaEECC",0 AS "FolioComprobanteDTE",mic."IdPersona",mic."MontoPlazo",mic.SEC_MOVTO_ASOC,mic."FechaVencimientoInstrumento",FAC_AJUSTE, mic."FolioComprobanteReferencia"
		FROM v_mesa_ingreso_cliente_corredora mic
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
				ret."Comision",ret."Gastos",
				ret."IngresoEgreso",ret."IdComprobante",ret."Nominal",ret."IdOrden",ret."CodigoCorredorContraparte",
				ret."SecuenciaEECC",0 AS "FolioComprobanteDTE",ret."IdPersona",ret."MontoPlazo",ret.SEC_MOVTO_ASOC,ret."FechaVencimientoInstrumento",FAC_AJUSTE, ret."FolioComprobanteReferencia"
		FROM v_mesa_retro_cliente_corredora ret
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
				ret."Comision",ret."Gastos",
				ret."IngresoEgreso",ret."IdComprobante",ret."Nominal",ret."IdOrden",ret."CodigoCorredorContraparte",
				ret."SecuenciaEECC" AS "SecuenciaEECC",ret."FolioComprobante" AS "FolioComprobanteDTE",ret."IdPersona",ret."MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",0 AS FAC_AJUSTE, ret."FolioComprobanteReferencia"
		FROM v_nominales ret
		LEFT JOIN v_comisiones FAC ON ret."IdComprobante" = FAC."IdComprobante" AND ret."IdTipoComprobante"  IN ('FAE','NCE','FEE')
		UNION
		SELECT  vtic."IdEmpresa",vtic."EsIngresoManual",vtic."IdModulo",vtic."Origen",vtic."Folio",vtic."FechaIngresoRegistro",vtic."FechaOperacion",
				vtic."FechaVencimientoOperacion",vtic."FechaLiquidacion",vtic."Plazo",
				vtic."IdCondicionLiquidacion",vtic."IdEstado",vtic."IdCliente",vtic."IdPortafolio",vtic."IdClienteCorredora",vtic."IdPortafolioCorredora",
				vtic."IdTipoOperacion",vtic."IdTipoProducto",vtic."IdConcepto",vtic."IdTipoComprobante",vtic."FolioComprobante",vtic."IdMesa",
				vtic."IdClasificacionTipoInstrumento",vtic."IdTipoInstrumento",vtic."IdInstrumento",vtic."FechaEmisionInstrumento",vtic."IdMonedaEmision",
				vtic."IdMonedaOperacion",vtic."IdEmisor",vtic."Nemotecnico",vtic."EsFungible",vtic."IdBolsa",vtic."IdBoveda",vtic."CodigoAgente",vtic."CodigoSucursal",
				vtic."IdTipoReajuste",vtic."Cantidad",vtic."ValorRescate",
				vtic."PrecioCompra",vtic."TasaCompra",vtic."TasaAgente",vtic."TasaEmision",vtic."MontoCompra",vtic."PrecioMercado",vtic."CodigoIFRS",
				vtic."FolioTransaccion",vtic."IdMedioDePago",vtic."EmitidoA",
				vtic."Comision",
				vtic."Gastos",
				vtic."IngresoEgreso",vtic."IdComprobante",vtic."Nominal",vtic."IdOrden",vtic."CodigoCorredorContraparte",
				vtic."SecuenciaEECC" AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",vtic."IdPersona",vtic."MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, vtic."FolioComprobanteReferencia"
		FROM v_mov_custodia_osas vtic
		UNION
		SELECT  mfso."IdEmpresa",mfso."EsIngresoManual",mfso."IdModulo",mfso."Origen",mfso."Folio",mfso."FechaIngresoRegistro",mfso."FechaOperacion",
				mfso."FechaVencimientoOperacion",mfso."FechaLiquidacion",mfso."Plazo",
				mfso."IdCondicionLiquidacion",mfso."IdEstado",mfso."IdCliente",mfso."IdPortafolio",mfso."IdClienteCorredora",mfso."IdPortafolioCorredora",
				mfso."IdTipoOperacion",mfso."IdTipoProducto",mfso."IdConcepto",mfso."IdTipoComprobante",mfso."FolioComprobante",mfso."IdMesa",
				mfso."IdClasificacionTipoInstrumento",mfso."IdTipoInstrumento",mfso."IdInstrumento",mfso."FechaEmisionInstrumento",mfso."IdMonedaEmision",
				mfso."IdMonedaOperacion",mfso."IdEmisor",mfso."Nemotecnico",mfso."EsFungible",mfso."IdBolsa",mfso."IdBoveda",mfso."CodigoAgente",mfso."CodigoSucursal",
				mfso."IdTipoReajuste",mfso."Cantidad",mfso."ValorRescate",
				mfso."PrecioCompra",mfso."TasaCompra",mfso."TasaAgente",mfso."TasaEmision",mfso."MontoCompra",mfso."PrecioMercado",mfso."CodigoIFRS",
				mfso."FolioTransaccion",mfso."IdMedioDePago",mfso."EmitidoA",
				mfso."Comision",
				mfso."Gastos",
				mfso."IngresoEgreso",mfso."IdComprobante",mfso."Nominal",mfso."IdOrden",mfso."CodigoCorredorContraparte",
				mfso."SecuenciaEECC" AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",mfso."IdPersona",mfso."MontoPlazo",0 as SEC_MOVTO_ASOC,'' AS "FechaVencimientoInstrumento",FAC_AJUSTE, mfso."FolioComprobanteReferencia"
		FROM v_mov_fac_suscripcion_osas mfso
	) AS SINFO
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = SINFO."IdPortafolio"
	LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdPortafolio" = POR."IdPortafolio" AND MES."IdTipoProducto" = 'MERF' AND MES."PorDefecto" = 'S'
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERS ON PERS."IdPersona" = SINFO."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PER ON PER."IdDocumentoIdentidad" = PERS."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTO ON CONCEPTO."IdConcepto" = SINFO."IdConcepto"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMPRESA ON SINFO."IdEmpresa" = EMPRESA."IdEmpresa"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EMPRESA."IdSocioDeNegocio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CORR ON CORR."IdPersona" = SOC."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PCORR ON PCORR."IdPersona" = CORR."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CODLIQ ON  CODLIQ."IdCondicionDeLiquidacion" = SINFO."IdCondicionLiquidacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONF ON CONF."CodigoParametro" = 'Replica.CodigoPortafolioDefecto' AND CONF."Valor" <> ''
	WHERE POR."CodigoPortafolio" IS NULL OR POR."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R')
	and "Folio" != 0
	UNION ALL
	SELECT 'HIS' AS "Origen",'' AS "IdTipoOperacion",'' AS "IdConcepto",0 AS "cuentapropia",0 AS "IdComprobante",0 AS "Folio",
	0 AS "IdCliente",'' AS "IngresoEgreso",'' AS "IdModulo",'' AS "IdTipoComprobante",
	'' AS "IdClasificacionTipoInstrumento",'' AS "EsIngresoManual",'' AS "Nominal",'' AS "IdMonedaOperacion",'' AS "Nemotecnico","IND_CUST_APLAZO","IND_ANULADO","EST_MOVTO","FEC_MVTO",
	"SEC_MOVTO","IND_SALDO_CTDO","IND_SALDO_APLAZO","IND_CUST_CTDO","RUT_CLI","SEC_RUT_CLI","TIPO_COMP_ADJ","FOLIO_COMP_ADJ","SEC_TRASPASO","UNIDAD_DES",
	"COD_MOVTO","NEMO","CALC_MTO_APLAZO","CALC_MTO_CTDO","CANTIDAD","COD_AGENTE","COD_BCO_PAGO","COD_BOVEDA","COD_CORR_CONTRA","COD_MESA","COD_OPE","COD_RUBRO",
	"COD_SUC","COMISION","CONCEPTO_MVTO","DERECHOS","EMISOR","EMITIDO_A","FAC_AJUSTE","FEC_EMI","FEC_ING_MVTO","FEC_LIQUID","FEC_TRANS","FEC_VCTO","FOLIO_COMP_CONT",
	"FOLIO_TRANS","FORMA_PAGO","GASTOS","IDENTIFIC_DOCTO","IND_BOLSA","IND_CCTE_APLAZO","IND_CCTE_CTDO","IND_CHEQUE","IND_COBRO_SEP","IND_CORREC","IND_EXENCION",
	"IND_FUS_CAR_DIA","IND_FUSION_TRAS","IND_GASTO_FAC","IND_HAB_CARTERA","IND_INC_GAS_CAR","IND_LIQUID","IND_PAGO_COBRO","IND_REBAJ_CAR","IND_RECAM_CAR",
	"IND_REPAR_CAR","IND_RETENIDO","IND_RIESGO_CLI","IND_RUEDA","IND_TIT_CCTE","IND_TIT_CUS","IND_TRANSF","IND_TRASPASO","LIN_COMP_ADJ","LIN_ORDEN","MERCADO",
	"MONTO","MONTO_APLAZO","MONTO_CAP_COMP","MONTO_INTERES","PRECIO","PRODUCTO","RUT_CORR","SEC_CAR_COMPROM","SEC_EVE","SEC_MOVTO_ASOC","SEC_ORDEN","SEC_RUT_CORR","SPREAD","TASA",
	"TASA_AGE","TASA_COMPROM","TIPO_FAM","TIPO_REAJ","UNIDAD_SOL","USUARIO_CER","USUARIO_SOL","VAL_RESC","VALOR_UNIT","VTO_COMP_ADJ","TIPO_CLI","CLAVE_DCV_CLI",
	"ENV_DCV_CLI","CLAVE_DCV_COR","ENV_DCV_COR","PORTAFOLIO","RETENCION_FUENTE","CODIGO_SER","FOLIO_COMPROBANTE","FPA_ID","IND_FUNGIBILIDAD","TASA_EMISION",
	"SPREAD_TASA_EMISION","MODALIDAD_PAGO","PERIODICIDAD_PAGO","IND_SIMUL","ORIGEN_RECURSOS","CLASIF_CARTERA","PRECIO_MED_CIE","PRECIO_PROM_COMP","COD_FUNGIBLE",
	"COD_EMISION","COD_TITULO","GEN_MOVTO","IND_EXIMIDO","CODCATIFRSID",'' AS "USR_LIB_SLD"
	FROM "BSCL.Sebra.BD::MIG.TBCTMVTO" M
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" C ON "CodigoParametro" = 'PLA.FechaGoLive'
	WHERE M.FEC_MVTO >= C."Valor"
	
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTOv2" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

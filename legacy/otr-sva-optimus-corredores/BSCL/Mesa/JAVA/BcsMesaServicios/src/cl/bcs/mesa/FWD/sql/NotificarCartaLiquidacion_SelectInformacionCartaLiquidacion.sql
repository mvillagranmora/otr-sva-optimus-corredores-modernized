SELECT
	 FWD."IdCartera"												AS "IdCartera"
	,FWD."TipoOperacion"
	,FWD."TipoVencimiento"											AS "TipoVencimiento"
	,FWD."IdMonedaPrincipal"
	,FWD."IdMonedaSecundaria"
	,FWD."MontoMonedaPrincipal"
	,CASE
		WHEN FWD."Nemotecnico" = 'ARBITR' THEN FWD."ParidadCierre" 
		ELSE FWD."TipoCambioCierre" END								AS "ValorCampoTCFWD"
	,COALESCE(PARDO."TipoCambioMercado", 0)							AS "PrecioReferenciaDO"
	,CASE
		WHEN FWD."PrecioVencimiento" <> 0 THEN FWD."PrecioVencimiento"
		WHEN FWD."ParidadVencimiento" <> 0 THEN FWD."ParidadVencimiento"
		ELSE 0 END													AS "PrecioReferencia"
	,FWD."ModalidadLiquidacion"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN
			CASE WHEN FWD."IdMonedaPrincipal" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
				 WHEN FWD."IdMonedaPrincipal" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
				 ELSE '' END
		WHEN FWD."IdMonedaPrincipal" = FWD."IdMonedaLiquidacion" OR (FWD."Nemotecnico" = 'ARBITR' AND FWD."IdMonedaSecundaria" = FWD."IdMonedaLiquidacion") THEN
			CASE WHEN FWD."IdMonedaPrincipal" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
				 WHEN FWD."IdMonedaPrincipal" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
				 ELSE '' END
		ELSE '' END													AS "NombreMedioDePagoEntregamos"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN
			CASE WHEN FWD."IdMonedaSecundaria" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
				 WHEN FWD."IdMonedaSecundaria" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
				 ELSE '' END
		WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN
			CASE WHEN FWD."IdMonedaPrincipal" = FPE."IdMoneda" THEN COALESCE(FPE."NombreFormaDePago",'')
				 WHEN FWD."IdMonedaPrincipal" = FPR."IdMoneda" THEN COALESCE(FPR."NombreFormaDePago",'')
				 ELSE '' END
		ELSE '' END													AS "NombreMedioDePagoRecibimos"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' AND FWD."MontoRecibimos" <> 0 THEN FWD."MontoRecibimos"
		WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' AND FWD."MontoPagamos"   <> 0 THEN FWD."MontoPagamos"
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoRecibimos"
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoPagamos"
		ELSE 0 END													AS "MontoSaldo1"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' AND FWD."MontoRecibimos" <> 0 THEN FWD."NombreEmpresa"
		WHEN FWD."ModalidadLiquidacionValor" = 'COMPENSADA' AND FWD."MontoPagamos"   <> 0 THEN FWD."NombreCliente"
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."NombreEmpresa"
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."NombreCliente"
		ELSE '' END													AS "AFavor1"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaPrincipal"
		ELSE FWD."IdMonedaLiquidacion" END							AS "IdMoneda1"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."MontoPagamos"
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."MontoRecibimos"
		ELSE 0 END													AS "MontoSaldo2"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'COMFWD' THEN FWD."NombreCliente"
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' AND FWD."IdTipoOperacion" = 'VENFWD' THEN FWD."NombreEmpresa"
		ELSE '' END													AS "AFavor2"
	,CASE
		WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaSecundaria"
		ELSE FWD."IdMonedaLiquidacion" END							AS "IdMoneda2"
	,CASE
		WHEN FWD."MontoRecibimos" <> 0 THEN COALESCE(TO_VARCHAR(FWD."FechaFiltroGrillaVcto",'DD-MM-YYYY'), '')
		ELSE '' END													AS "FechaRecibimos"
	,CASE
		WHEN FWD."MontoPagamos" <> 0 THEN COALESCE(TO_VARCHAR(FWD."FechaFiltroGrillaVcto",'DD-MM-YYYY'), '')
		ELSE '' END													AS "FechaEntregamos"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" FWD
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" FPE
	ON (FWD."IdCartera" = FPE."IdOperacion" AND FPE."IdTipoProducto" = 'MEFW' AND FPE."RecibePaga" = 'P' AND FPE."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" FPR
	ON (FWD."IdCartera" = FPR."IdOperacion" AND FPE."IdTipoProducto" = 'MEFW' AND FPE."RecibePaga" = 'R' AND FPE."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PARDO
	ON (PARDO."IdMoneda" = 'USD' AND PARDO."Fecha" = FWD."FechaVencimiento")
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
	ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = FWD."IdMonedaPrincipal") AND MONPRI."IdTipoOperacion" = ''
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
	ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = FWD."IdMonedaSecundaria") AND MONSEC."IdTipoOperacion" = ''
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONLIQ
	ON (MONLIQ."IdUsoMonedaFormato" = 'FWD' AND MONLIQ."IdMoneda" = FWD."IdMonedaLiquidacion") AND MONLIQ."IdTipoOperacion" = ''
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONLIQ1
	ON (MONLIQ1."IdUsoMonedaFormato" = 'FWD' AND 
		MONLIQ1."IdMoneda" = CASE WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaPrincipal"
								  ELSE FWD."IdMonedaLiquidacion" END) AND MONLIQ1."IdTipoOperacion" = ''
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONLIQ2
	ON (MONLIQ2."IdUsoMonedaFormato" = 'FWD' AND 
		MONLIQ2."IdMoneda" = CASE WHEN FWD."ModalidadLiquidacionValor" = 'ENTREGA_FISICA' THEN FWD."IdMonedaSecundaria"
								  ELSE FWD."IdMonedaLiquidacion" END) AND MONLIQ2."IdTipoOperacion" = ''
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
	ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO') AND MONDO."IdTipoOperacion" = ''
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUF
	ON (MONUF."IdUsoMonedaFormato" = 'FWD' AND MONUF."IdMoneda" = 'UF') AND MONUF."IdTipoOperacion" = ''
INNER JOIN "BSCL.Mesa.BD::GBL.NotificarCartaLiquidacion_Proceso_Paso" CL
	ON ( FWD."IdCartera" = CL."IdCartera" AND FWD."IdOperacion" = CL."IdOperacion" AND 
		 CL."IdTrabajo" = :IdTrabajo AND FWD."IdEstado" = CL."IdEstado" AND 
		 FWD."FolioOperacion" = CL."FolioOperacion" AND FWD."FechaOperacion" = CL."FechaOperacion" )
WHERE
	FWD."IdEstado" IN ('VCD','LIQ');
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico";

CREATE VIEW "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico"
AS 
WITH 
CteCalculosRenovacionesPrestamo AS (
		SELECT Cartera."FolioCarteraRelacionada" AS "FolioCarteraPrestamoOriginal"
			 , SUM(COALESCE(Cartera."PlazoEnDiasOperacion", 0) - Cartera."PlazoResidual") AS "TotalDiasRenovados"
		FROM (
			SELECT
				Renovacion."FolioCarteraRelacionada"
				, COALESCE(Renovacion."PlazoEnDiasOperacion", 0) AS "PlazoEnDiasOperacion"
				, CASE WHEN "INGCantidad" = "EGRCantidad" AND "EGRFechaOperacion" < "FechaVencimientoOperacion" 
					THEN "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo"(Renovacion."EGRFechaOperacion", Renovacion."FechaVencimientoOperacion", tp."EsDiaHabilPlazoPrestamo", 'T0', (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'PLA.PaisSistema' AND conf."IdEmpresa" = Renovacion."IdEmpresa"))
					ELSE 0 
				  END AS "PlazoResidual"
			FROM
				"BSCL.Custodia.BD::CUS.Cartera" Renovacion
			INNER JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" tp ON tp."IdTipoPrestamo" = Renovacion."IdTipoPrestamo"
			WHERE
					Renovacion."IdTipoInstrumento" IN ('PRT','PRF') 
				AND	Renovacion."IdEstado" = 'RNV'
				AND Renovacion."FolioCarteraRelacionada" > 0
			
		) Cartera
		GROUP BY
			Cartera."FolioCarteraRelacionada"
	)
SELECT
	sby."IdInstrumento" 		as "IdInstrumentoSubyacente",
	sby."IdEmisor" 				as "IdEmisorSubyacente",
	sby."Nemotecnico"			as "NemoSubyacente",
	sby."NemoSVS" 				as "NemoSVSSubyacente",
	sby."IdTipoInstrumento"		as "IdTipoInstrumentoSubyacente",
	sbyi."Nombre" 				as "NombreTipoInstrumentoSubyacente",
	sby."IdClasificacionTipoInstrumento"		as "IdClasificacionTipoInstrumentoSubyacente",
	IFNULL(asig."CantidadAsignada",0) as "CantidadAsignada",
	IFNULL(asig."CantidadPorAsignar",0) as "CantidadPorAsignar",
	sby."IdMonedaInstrumento" as "IdMonedaSubyacente",
	sby."IdMonedaEmision", 
	sby."IdTipoReajusteSubyacente", 
	prt."IdBoveda" as "IdBovedaSubyacente",

	prt."IdCartera",
	prt."IdEmpresa",
	prt."FolioCartera" as "FolioPrestamo",
	prt."FolioCarteraRelacionada",
	prt."IdEstado",
	estado."Nombre" AS "NombreEstado",
	prt."IdMonedaSistema",
	prt."IdMonedaInstrumento",
	prt."IdOperacion",
	prt."IdTipoOperacion",
	TipOpe."Nombre" as "NombreTipoOperacion",
	prt."IdBoveda",
	prt."IdTipoReajuste",
	prt."IdTipoProducto",
	prt."IdSucursal",
	prt."IdAgente",
	prt."IdBolsa",
	prt."IdConcepto",
	Concepto."Nombre" as "NombreConcepto",
	prt."IdCondicionLiquidacion",
	prt."IdModulo",
	prt."IdCliente",
	Cli."DocumentoIdentidad",
	prt."IdPortafolio",
	Port."CodigoPortafolio",
	Cli."NombreCompleto" as "RazonSocial",
	prt."Nemotecnico" as "NemotecnicoPrestamo",
	prt."NemoSVS",
	prt."IdInstrumento" as "IdInstrumentoPrestamo", 
	prt."IdClasificacionTipoInstrumento",
	prt."IdTipoInstrumento",
	prt."IdEmisor",
	prt."FechaOperacion" as "FechaPrestamo",
	prt."IdTipoComprobante",
	prt."FolioComprobante",
	prt."FechaComprobante",
	prt."Cantidad",
	prt."CorteMinimo",
	prt."PrecioCompra" as "PrecioInstrumento",
	prt."MontoCompra" as "MontoInstrumento",
	prt."TasaCompra" as "TasaPrestamo",
	prt."ValorRescate",
	prt."FechaVencimientoOperacion" as "FechaVencimientoPrestamo",
	prt."FechaEmisionInstrumento",
	prt."FechaVencimientoInstrumento",
	prt."PlazoEnDiasOperacion",
	prt."EsFungible",
	prt."CodigoIFRS",
	prt."FechaIngresoRegistro",
	prt."FechaModificacionRegistro",
	prt."TasaAgente",
	prt."FechaLiquidacion",
	prt."IdTipoPrestamo",
	Instrumento."Nemotecnico" as "NombreTipoPrestamo",
	tpres."MinimoDiasAnticipo",
	tpres."MaximoDiasPrestamo",
	tpres."MinimoDiasPrestamo",
	tpres."EsDiaHabilPlazoPrestamo",
	tpres."EsDiaHabilParaCalculoInteres",
	prt."UsaGarantiaEspecifica",
	prt."FolioExternoReferencia" as "FolioEntidad",
	prt."CobraPrimaAlInicio",
	prt."IdTipoCanalIngreso",
	prt."INGGlosa",
	prt."INGFechaOperacion",
	prt."INGCantidad",
	prt."INGValorRescate",
	prt."INGPrecioAPlazo" 					as "INGMontoPrima",
	prt."INGMontoAPlazo" 					as "INGMontoTotalPrestamo",
	prt."INGPrecioCompra"		 			as "INGPrecioInstrumento",
	prt."INGTasaCompra" 					as "INGTasaPrestamo",
	prt."INGMontoATasaCompraActual" 		as "INGMontoInstrumento",
	prt."INGFechaPrecioTasaMercadoActual",
	prt."INGPrecioMercadoActual" 			as "INGPrecioMercadoInstrumento",
	prt."INGTasaMercadoActual",
	prt."INGMontoATasaMercadoActual" 		as "INGMontoMercadoInstrumento",	 
	prt."EGRGlosa",
	prt."EGRFechaOperacion",
	prt."EGRIdTipoOperacion",
	prt."EGRIdOperacion",
	prt."EGRIdMovimientoCarteraEnTransito",
	prt."EGRIdTipoCanalIngreso",
	prt."EGRIdCondicionLiquidacion",
	prt."EGRFechaLiquidacion",
	prt."EGRIdTipoComprobante",
	prt."EGRFolioComprobante",
	prt."EGRFechaComprobante",
	prt."EGRFolioExternoReferencia",
	prt."EGRIdModulo",
	prt."EGRIdConcepto",
	prt."EGRIdUsuario",
	prt."EGRIdSucursal",
	prt."EGRIdAgente",
	prt."EGRFechaCurseRetiro",
	CASE WHEN prt."IdEstado" = 'MOD' THEN prt."INGCantidad" ELSE prt."EGRCantidad" END AS "EGRCantidad",
	prt."EGRValorRescate",
	prt."EGRPrecio",
	prt."EGRTasa",
	prt."EGRMonto",
	prt."EGRMontoMonedaInstrumento",
	prt."EGRSpread",
	prt."EGRPrecioAPlazo",
	prt."EGRMontoAPlazo",
	prt."EGRMontoAPlazoMonedaInstrumento",
	prt."EGRIdMonedaLiquidacion",
	prt."EGRMontoLiquidacion",
	prt."EGRMontoLiquidacionMonedaPrincipal",
	prt."EstadoLiquidacion",
	prt."TipoCustodia",
	tpres."EsRenovable",
	Mercado."Nombre" AS "NombreMercado",
	Port."CodigoPortafolio" || ' - ' || Port."Proposito" AS "DescripcionPortafolio",
	CASE prt."IdEstado" WHEN 'ANT' THEN prt."EGRMonto" ELSE 0 END AS "EGRMontoAnticipo",
	CASE prt."IdEstado" WHEN 'ANT' THEN prt."EGRFechaOperacion" ELSE '0001-01-01' END AS "EGRFechaAnticipo",
	CASE sby."IdClasificacionTipoInstrumento" WHEN 'IF' THEN sby."ValorRescate" ELSE sby."Cantidad" END AS "Posicion",
	CASE sby."IdClasificacionTipoInstrumento" WHEN 'IF' THEN sby."INGValorRescate" ELSE sby."INGCantidad" END AS "INGPosicion",
	CASE sby."IdClasificacionTipoInstrumento" WHEN 'IF' THEN sby."EGRValorRescate" ELSE sby."EGRCantidad" END AS "EGRPosicion",
	CASE WHEN tpres."EsRenovable" = 'S' AND (prt."IdEstado" = 'VCD' OR 
			 (prt."IdEstado" = 'ANT' AND (CASE sby."IdClasificacionTipoInstrumento" WHEN 'IF' THEN sby."INGValorRescate" ELSE sby."INGCantidad" END) = (CASE sby."IdClasificacionTipoInstrumento" WHEN 'IF' THEN sby."EGRValorRescate" ELSE sby."EGRCantidad" END)))
		THEN COALESCE((tpres."MaximoDiasPrestamo" - ((prt."PlazoEnDiasOperacion" - (CASE WHEN prt."INGCantidad" = prt."EGRCantidad" AND prt."EGRFechaOperacion" < prt."FechaVencimientoOperacion" 
																							THEN "BSCL.Custodia.BD.Utilitarios.PRT::CalcularPlazo"(prt."EGRFechaOperacion", prt."FechaVencimientoOperacion", tpres."EsDiaHabilPlazoPrestamo", 'T0', (SELECT "Valor" FROM "BSCL.Plataforma.BD::PLA.Configuracion" conf WHERE conf."CodigoParametro" = 'PLA.PaisSistema' AND conf."IdEmpresa" = prt."IdEmpresa")) 
																							ELSE 0 END)) + COALESCE(CalculosRenovacionesPrevias."TotalDiasRenovados", 0))), 0)
		ELSE 0 
	END AS "PlazoDisponibleRenovacion",
	prt."FolioSistemaExterno",
	sby."INGTasaSubyacente",
	CASE WHEN prt."PrecioSucio" = 0 THEN prt."PrecioCompra" ELSE prt."PrecioSucio" END AS "PrecioCompraInicial"
FROM "BSCL.Custodia.BD::CUS.Cartera" as prt
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (prt."FolioCartera" = ultSub."FolioCarteraRelacionada")
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = prt."IdPortafolio" 
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = prt."IdCliente" 
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
	LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarAsignacionesPrestamoHistorico" as asig on asig."FolioCarteraPrestamo" = prt."FolioCartera"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" as TipOpe ON (TipOpe."IdTipoOperacion" = prt."IdTipoOperacion" AND TipOpe."IdTipoInstrumento" = prt."IdTipoInstrumento")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON prt."IdConcepto" = Concepto."IdConcepto" 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as Instrumento ON Prt."IdInstrumento" = Instrumento."IdInstrumento"
	LEFT JOIN "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo" as tpres on tpres."IdInstrumentoPrestamo" = prt."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as sbyi ON sbyi."IdTipoInstrumento" = sby."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as estado ON prt."IdEstado" = estado."IdEstado"
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS Mercado ON Mercado."IdClasificacionTipoInstrumento" = sby."IdClasificacionTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" precios on precios."IdInstrumento" = sby."IdInstrumento" and precios."Fecha" = prt."FechaOperacion"  and precios."IdBolsa" = prt."IdBolsa"
	LEFT JOIN CteCalculosRenovacionesPrestamo CalculosRenovacionesPrevias ON (CalculosRenovacionesPrevias."FolioCarteraPrestamoOriginal" = prt."FolioCarteraRelacionada")
WHERE (prt."IdTipoInstrumento" = 'PRT' OR prt."IdTipoInstrumento" = 'PRF') AND prt."IdEstado" <> 'VIG'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico" TO BCS_DESARROLLADOR WITH GRANT OPTION;

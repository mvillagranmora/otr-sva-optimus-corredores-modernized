SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias" AS 
SELECT 
	A."IdGarantia"			     				,
	A."FolioGarantia"	             			,
	A."FolioCarteraOrigen"	     				,
	A."FolioCarteraGarantizada" 				,
	A."EsEspecifica" 							,
	A."IdInstrumentoCarteraGarantizada"	 			,
	CASE WHEN A."IdConcepto" IN ('GARDEV','GARDVA') THEN sub."Nemotecnico" ELSE Y."Nemotecnico" END as "NemoGarantizado"        ,
    	CASE WHEN A."IdConcepto" IN ('GARDEV','GARDVA') THEN sub."IdTipoInstrumento" ELSE Y."IdTipoInstrumento" END as "IdTipoInstrumentoGarantizado",
	A."IdContraparteGarantia" 					,
	COALESCE(G."RazonSocial",'') as "RazonSocialContraparte",
	A."IdOrigenGarantia"						,
	F."Nombre" as "NombreOrigenGarantia" 		,
	A."IdEspecieGarantia"						,
	IFNULL(C."Descripcion",'') as "DescripcionEspecie" 	,
	A."FechaIngresoRegistro" 					,
	A."FechaModificacionRegistro"				,
	A."IdEmpresa" 								,
	A."IdUsuario" 								,
	A."IdModulo" 								,
	A."IdCliente" 								,
	A."IdPortafolio" 							,
	cli."NombreCompleto" as "NombreCliente" 	,
	cli."DocumentoIdentidad" 					,
	por."CodigoPortafolio" 						,
	A."IdEstado" 								,
	Z."Nombre" as "NombreEstado" 				,
	car."IdTipoProducto" as "IdTipoProductoOperacionOrigen"			,
	A."IdTipoCanalIngreso"						,
	A."IdMonedaSistema"							,
	CONCAT(COALESCE(CONCAT (H."Codigo", ' - '),''), COALESCE(H."Nombre",'')) as "NombreMonedaSistema",
	A."CodigoIFRS" 								,
	A."IdConcepto" 								,
	B."Nombre" as "NombreConcepto" 				,
	CAR."IdTipoOperacion" as "IdTipoOperacionOperacionOrigen"			,	 
	A."FechaOperacion"							,
	A."FechaVencimientoOperacion"				,
	A."PlazoEnDiasOperacion"					, 
	A."FolioExternoReferencia" 					,
	car."IdMesa" as "IdMesaOperacionOrigen"		,
	car."IdBolsa" as "IdBolsaOperacionOrigen"	,
--	J."Nombre" as "NombreBolsa" 				,
	A."IdSucursal" 								,
	A."IdAgente" 								,
	A."IdCondicionLiquidacion" 					,
	A."FechaLiquidacion" 						,
	A."IdClasificacionTipoInstrumento" 			,
	i."Nombre" as "ClasificacionTipoInstrumento",
	A."IdTipoInstrumento"						,	 
	D."Nombre" as "NombreTipoInstrumento" 		,
	A."IdInstrumento" 							,
	--A."Nemotecnico"								,
	CASE 
		WHEN aval."IdClasificacionTipoInstrumento" = 'IF' AND IFNULL(A."NemoSVS",'') <> '' THEN A."NemoSVS" ELSE A."Nemotecnico" 
	END AS "Nemotecnico",
	A."IdMonedaEmision" AS "EsPagaderoEn",
	
	CASE
		when A."IdOrigenGarantia" = 'CUS' THEN COALESCE(E."Nemotecnico", '')
		when A."IdOrigenGarantia" = 'CTA' THEN 'GARANTIA EN EFECTIVO'
		when A."IdOrigenGarantia" = 'ESP' THEN 'GARANTIA EN ESPECIE'
	END AS "NombreInstrumento",
	
	A."IdEmisor" 								,
	A."NemoSVS"	 								,
	A."IdTipoReajuste" 							,
	A."FechaEmisionInstrumento"					,
	A."FechaVencimientoInstrumento"				,
	A."EsFungible" 								,
	IFNULL(car."IdBoveda",'') 	as "IdBovedaOperacionOrigen",
	A."IdMonedaInstrumento"						,
	A."MontoGarantiaMonedaInstrumento"			,
	CONCAT(COALESCE(CONCAT (H2."Codigo", ' - '),''), COALESCE(H2."Nombre",'')) as "NombreMonedaInstrumento",
	car."IdTipoComprobante" as "IdTipoComprobanteOperacionOrigen",
	car."FolioComprobante" as "FolioComprobanteOperacionOrigen",
	car."FechaComprobante" as "FechaComprobanteOperacionOrigen",
	
	A."Cantidad" 								,
	A."PrecioGarantia"							,
	A."TasaGarantia",
	A."MontoGarantia"							,
	A."CorteMinimo"								,
	A."ValorRescate"							,
	A."PrecioCompraOperacionOrigen"				,
	A."TasaCompraOperacionOrigen" 				,
	A."PrecioPromedioCompraOperacionOrigen",
	A."MontoPrecioPromedioCompraOperacionOrigen",
	A."MontoPrecioPromedioMonedaInstrumentoOperacionOrigen",
	car."ValorTipoCambioCierre", 
	A."INGFechaOperacion" 						,
	A."INGFechaLiquidacion"						,
	A."INGCantidad"								,	
	CASE
		WHEN A."IdOrigenGarantia" = 'CUS' THEN A."INGCantidad"
		WHEN A."IdOrigenGarantia" = 'CTA' THEN 1				
		WHEN A."IdOrigenGarantia" = 'ESP' THEN 1                
	END	AS "CantidadGarantia",
	
	A."INGPrecioGarantia"						,
	A."INGTasaGarantia"							,
	A."INGValorRescate" 						,
	A."INGMontoATasaMercadoActual" as "INGMonto",
	A."INGMontoATasaCompraActual" 				,
	A."INGMontoCompraActualMonedaInstrumento"	,

	CASE
		WHEN A."IdOrigenGarantia" = 'CUS' THEN A."INGMontoGarantiaActualMonedaInstrumento"   -- Monto valorizado
		WHEN A."IdOrigenGarantia" = 'CTA' THEN A."INGCantidad"				 				 -- Para garantias en dinero no se valoriza
		WHEN A."IdOrigenGarantia" = 'ESP' THEN A."MontoGarantiaMonedaInstrumento"            -- Para garantias en especies no se valoriza
	END	AS "MontoValorizadoMonedaInstrumento",
	
	CASE
		WHEN A."IdOrigenGarantia" = 'CUS' THEN A."INGMontoATasaGarantiaActual"   -- Monto valorizado
		WHEN A."IdOrigenGarantia" = 'CTA' THEN A."INGCantidad"				 -- Para garantias en dinero no se valoriza
		WHEN A."IdOrigenGarantia" = 'ESP' THEN A."MontoGarantia"               -- Para garantias en especies no se valoriza
	END	AS "MontoValorizado",
	A."INGMontoATasaGarantiaActual",
	
	A."INGMontoATasaCompraAnterior"				,
	A."INGPrecioMercadoActual"					,
	A."INGTasaMercadoActual" 					,
	A."INGMontoATasaMercadoActual" 				,
	A."INGMontoATasaMercadoAnterior"			,
	aval."IdTipoInstrumento" as "IdTipoInstrumentoAval",
	aval."NombreTipoInstrumento" as "NombreTipoInstrumentoAval",
	aval."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoAval",
	aval."NombreClasificacionTipoInstrumento" as "NombreClasificacionTipoInstrumentoAval",
	ICR."ValorClasificacionRiesgo" as "ValorClasificacionRiesgo", 
	ICR."IdEntidadClasificadoraRiesgo" as "IdEntidadClasificadoraRiesgo",
	TICR."IdTipoClasificacionRiesgo" as "IdTipoClasificacionRiesgo",
	
	
	
	A."INGDurationCompraActual",
	A."INGPrecioMercadoProximo",
	A."INGTasaMercadoProximo",
	A."INGDurationMercadoActual",
	A."INGMontoATasaCompraProximo",
	A."INGMontoATasaMercadoProximo",
	A."INGMontoInteresCompraProximo"			,
	A."INGDurationCompraProximo"				,
	A."INGMontoReajusteCompraProximo"			,
	A."INGFuenteTasaPrecioMercadoActual" AS "FuenteTasaPrecioMercado",
	A."INGFechaValorizacionGarantia" AS "FechaValorizacionGarantia",
	A."INGPrecioMercadoAnterior"				,
	A."EGRCantidad",
	A."EGRMonto",
    	A."EGRMontoMonedaInstrumento",
    	A."EGRMontoMonedaAdicional",
	A."EGRFechaLiquidacion"						,
	A."EGRFechaOperacion" ,
	car."ISIN"									,
	A."INGMontoInteresCorridoGarantiaActual" AS "MontoInteresCorrido",
	A."INGMontoReajusteCorridoGarantiaActual" AS "MontoReajusteCorrido",
	A."INGMontoInteresCorridoMercadoActual"	AS "MontoInteresCorridoMercado",
	A."INGMontoReajusteCorridoMercadoActual" AS "MontoReajusteCorridoMercado",
	A."INGFechaValorizacionAnterior"			,
	A."INGFechaValorizacionActual"				,
	(car."CorteMinimo"*A."INGCantidad") AS "ValorNominal",
	A."EGRIdConcepto"							,
	car."IdCuentaCustodioPortafolio"			,
	GE."IdEntidadDestinoGarantia" AS "IdEntidadDestinoGarantiaEmpresa"
	--,ultCar."IdOperacionOrigen" AS "IdOperacionOrigenGarantia"
	,A."FechaRespaldo"
	,A."INGMontoMercadoAnteriorMonedaInstrumento"
	,A."INGMontoMercadoActualMonedaInstrumento"
	,A."INGMontoCompraAnteriorMonedaInstrumento"
	,A."FolioComprobante"								AS "FolioComprobanteGarantia"
FROM "BSCL.Custodia.BD::HIS.Garantia" A
LEFT JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoCartera" as ultCar on ultCar."IdEmpresa" = A."IdEmpresa" and ultCar."FolioCartera" = A."FolioCarteraOrigen" AND ultCar."FechaRespaldo" = A."FechaRespaldo"
LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" as car on car."IdCartera" = ultCar."IdCartera" AND ultCar."FechaRespaldo" = car."FechaRespaldo"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" B ON B."IdConcepto" = A."IdConcepto"
LEFT JOIN "BSCL.Custodia.BD::GTA.EspecieEnGarantia" C ON C."IdEspecieGarantia" = A."IdEspecieGarantia"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" i ON i."IdClasificacionTipoInstrumento" = A."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" E ON E."IdInstrumento" = A."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" D ON D."IdTipoInstrumento" = A."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" AS TICR ON TICR."IdTipoInstrumento" = E."IdTipoInstrumento" AND TICR."EntidadClasificadoraRiesgoPrincipal" = 'S'
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = E."IdInstrumento" AND ICR."IdEntidadClasificadoraRiesgo" = TICR."IdEntidadClasificadoraRiesgo"	
LEFT JOIN "BSCL.Custodia.BD::GTA.OrigenGarantia" F ON F."IdOrigenGarantia" = A."IdOrigenGarantia"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON A."IdEmpresa" = EMP."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" G ON G."IdContraparte" = A."IdContraparteGarantia"
LEFT JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" GE ON GE."IdPersona" = EMP."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H on H."IdMoneda" = A."IdMonedaSistema"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H2 on H2."IdMoneda" = A."IdMonedaInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Z ON Z."IdEstado" = A."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Y ON Y."IdInstrumento" = A."IdInstrumentoCarteraGarantizada"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS aval ON aval."IdInstrumento" = A."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdCliente" = A."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por on por."IdPortafolio" = A."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" sub ON (sub."IdTipoInstrumento" = 'DER' )
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
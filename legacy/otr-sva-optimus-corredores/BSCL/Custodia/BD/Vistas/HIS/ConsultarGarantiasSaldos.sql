SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantiasSaldos";
GO

 
CREATE VIEW "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantiasSaldos" AS 
SELECT
	A."FechaRespaldo" 							,
	A."IdGarantia"			     				,
	A."FolioGarantia"	             			,
	A."FolioCarteraOrigen"	     				,
	A."FolioCarteraGarantizada" 				,
	A."EsEspecifica" 							,
	A."IdInstrumentoCarteraGarantizada"	 			,
	Y."Nemotecnico" as "NemoGarantizado" 		,
	Y."IdTipoInstrumento" as "IdTipoInstrumentoGarantizado",
	A."IdContraparteGarantia" 					,
	COALESCE(G."RazonSocial",'') as "RazonSocialContraparte",
	A."IdOrigenGarantia"						,
	F."Nombre" as "NombreOrigenGarantia" 		,
	A."IdEspecieGarantia"						,
	C."Descripcion" as "DescripcionEspecie" 	,
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
	por."Proposito" as "PropositoPortafolio" 	,
	A."IdEstado" 								,
	Z."Nombre" as "NombreEstado" 				,
	car."IdTipoProducto" as "IdTipoProductoOperacionOrigen"			,
	A."IdTipoCanalIngreso"						,
	A."IdMonedaSistema"							,
	CONCAT(COALESCE(CONCAT (H."Codigo", ' - '),''), COALESCE(H."Nombre",'')) as "NombreMonedaSistema",
	A."CodigoIFRS" 								,
	A."IdConcepto" 								,
	B."Nombre" as "NombreConcepto" 				,
	car."IdTipoOperacion" as "IdTipoOperacionOperacionOrigen"			,	 
	A."FechaOperacion"							,
	A."FechaVencimientoOperacion"				,
	A."PlazoEnDiasOperacion"					, 
	A."FolioExternoReferencia" 					,
	car."IdMesa" as "IdMesaOperacionOrigen"					,
	car."IdBolsa" as "IdBolsaOperacionOrigen"					,
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
	A."Nemotecnico"								,
	case 
--		when A."IdOrigenGarantia" = 'CUS' THEN CONCAT(COALESCE(CONCAT (E."Nemotecnico", ' - '),''), COALESCE(E."RazonSocial",'')) 
		when A."IdOrigenGarantia" = 'CUS' THEN COALESCE(E."Nemotecnico", '')
		when A."IdOrigenGarantia" = 'CTA' THEN 'GARANTIA EN EFECTIVO'
		when A."IdOrigenGarantia" = 'ESP' THEN 'GARANTIA EN ESPECIE'
		end 
	as "NombreInstrumento",
	A."IdMonedaEmision" AS "EsPagaderoEn",
	A."IdMonedaEmision",
	A."IdEmisor" 								,
	A."NemoSVS"	 								,
	A."IdTipoReajuste" 							,
	A."FechaEmisionInstrumento"					,
	A."FechaVencimientoInstrumento"				,
	A."EsFungible" 								,
	car."IdBoveda" as "IdBovedaOperacionOrigen"					,
	A."IdMonedaInstrumento"						,
	CONCAT(COALESCE(CONCAT (H2."Codigo", ' - '),''), COALESCE(H2."Nombre",'')) as "NombreMonedaInstrumento",
--	E."IdClasificacionRiesgo" AS "IdClasificacionRiesgoInstrumento",
--	R."ClasificacionRiesgo",
	
	car."IdTipoComprobante" as "IdTipoComprobanteOperacionOrigen",
	car."FolioComprobante" as "FolioComprobanteOperacionOrigen",
	car."FechaComprobante" as "FechaComprobanteOperacionOrigen",

	A."Cantidad" 								,
	A."PrecioGarantia"							,
	A."MontoGarantia"							,
	A."CorteMinimo"								,
	A."ValorRescate"							,
	A."PrecioCompraOperacionOrigen"				,
	A."TasaCompraOperacionOrigen" 				,
	A."PrecioPromedioCompraOperacionOrigen",
	A."MontoPrecioPromedioCompraOperacionOrigen",
	A."MontoPrecioPromedioMonedaInstrumentoOperacionOrigen",
	A."INGFechaOperacion" 						,
	A."INGCantidad"								,
	A."INGValorRescate" 						,
	A."INGMontoATasaMercadoActual" as "INGMonto",
	A."INGMontoATasaCompraActual" 				,
	A."INGPrecioMercadoActual"					,
	A."INGTasaMercadoActual" 					,
	A."INGMontoATasaMercadoActual" 				,
	aval."IdTipoInstrumento" as "IdTipoInstrumentoAval",
	aval."NombreTipoInstrumento" as "NombreTipoInstrumentoAval",
	aval."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoAval",
	aval."NombreClasificacionTipoInstrumento" as "NombreClasificacionTipoInstrumentoAval",
--	E."ValorClasificacionRiesgo" as "ValorClasificacionRiesgo", 
--	E."IdEntidadClasificadoraRiesgo" as "IdEntidadClasificadoraRiesgo",
--	TCRiesgo."IdTipoClasificacionRiesgo" as "IdTipoClasificacionRiesgo",
	
	A."INGMontoInteresCompraProximo"			,
	A."INGDurationCompraProximo"				,
	A."INGMontoReajusteCompraProximo"			
	
	
	
	
	
FROM "BSCL.Custodia.BD::HIS.Garantia" A
INNER JOIN (SELECT
	 gar."IdEmpresa",
	 gar."FolioGarantia",
	 gar."FechaRespaldo",
	 Max(gar."IdGarantia") as "IdGarantia" 
	from "BSCL.Custodia.BD::HIS.Garantia" gar
	WHERE gar."EGRCantidad" = 0 and gar."IdEstado" = 'VIG' -- tomar solo los saldos
	GROUP BY gar."IdEmpresa", gar."FolioGarantia", gar."FechaRespaldo" ) as Saldos ON A."IdGarantia" = Saldos."IdGarantia" AND A."FechaRespaldo" = Saldos."FechaRespaldo"	
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" B ON B."IdConcepto" = A."IdConcepto"
LEFT JOIN "BSCL.Custodia.BD::GTA.EspecieEnGarantia" C ON C."IdEspecieGarantia" = A."IdEspecieGarantia"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" i ON i."IdClasificacionTipoInstrumento" = A."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" D ON D."IdTipoInstrumento" = A."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" E ON E."IdInstrumento" = A."IdInstrumento"
LEFT JOIN "BSCL.Custodia.BD::GTA.OrigenGarantia" F ON F."IdOrigenGarantia" = A."IdOrigenGarantia"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" G ON G."IdContraparte" = A."IdContraparteGarantia"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H on H."IdMoneda" = A."IdMonedaSistema"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H2 on H2."IdMoneda" = A."IdMonedaInstrumento"
--LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" J on J."IdBolsa" = A."IdBolsaOperacionOrigen"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Z ON Z."IdEstado" = A."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Y ON Y."IdInstrumento" = A."IdInstrumentoCarteraGarantizada"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS aval ON aval."IdInstrumento" = A."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdCliente" = A."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por on por."IdPortafolio" = A."IdPortafolio"
--LEFT JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" TCRiesgo on TCRiesgo."IdClasificacionTipoInstrumento" =  aval."IdClasificacionTipoInstrumento"
--LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" R ON R."ClasificacionRiesgo" = E."ValorClasificacionRiesgo"
--	AND R."IdEntidadClasificadoraRiesgo" = E."IdEntidadClasificadoraRiesgo"
--	AND R."IdTipoClasificacionRiesgo" = TCRiesgo."IdTipoClasificacionRiesgo"
	
	
LEFT JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoCartera" as ultCar on 
	ultCar."IdEmpresa" = A."IdEmpresa" and 
	ultCar."FolioCartera" = A."FolioCarteraOrigen" AND
	ultCar."FechaRespaldo" = A."FechaRespaldo"
LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" as car on car."IdCartera" = ultCar."IdCartera" AND car."FechaRespaldo" = ultCar."FechaRespaldo" 

WITH READ ONLY;
GO


GRANT SELECT ON "BSCL.Custodia.BD.Vistas::HIS.ConsultarGarantiasSaldos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
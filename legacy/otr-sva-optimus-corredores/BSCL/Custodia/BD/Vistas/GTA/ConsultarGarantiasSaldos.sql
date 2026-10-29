SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" AS 
SELECT 
	A."IdGarantia"			     				,
	A."FolioGarantia"	             			,
	A."FolioCarteraOrigen"	     				,
	A."IdCarteraOrigen"							,
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
	InstruClasi."ValorClasificacionRiesgo"		AS "ClasificacionRiesgo",
	
	car."IdTipoComprobante" as "IdTipoComprobanteOperacionOrigen",
	car."FolioComprobante" as "FolioComprobanteOperacionOrigen",
	car."FechaComprobante" as "FechaComprobanteOperacionOrigen",

	A."Cantidad" 								,
	A."PrecioGarantia"							,
	A."INGMontoATasaGarantiaActual" AS "MontoGarantia"							,
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
	InstruClasi."ValorClasificacionRiesgo" as "ValorClasificacionRiesgo", 
	InstruClasi."IdEntidadClasificadoraRiesgo" as "IdEntidadClasificadoraRiesgo",
	TCRiesgo."IdTipoClasificacionRiesgo" as "IdTipoClasificacionRiesgo",
	
	A."INGMontoInteresCompraProximo"			,
	A."INGDurationCompraProximo"				,
	A."INGMontoReajusteCompraProximo"           ,
	CASE WHEN A."IdOrigenGarantia" = 'CTA' AND A."EsEspecifica" = 'S' THEN A."EGRIdOperacion"
		 WHEN A."IdOrigenGarantia" = 'CTA' AND A."EsEspecifica" = 'N' THEN A."EGRIdOperacion"
		ELSE cart."EGRIdOperacion" 
	END AS "IdOperacionOrigenGarantia",
	--cart."EGRIdOperacion" AS "IdOperacionOrigenGarantia", -- Folio de la orden que originó la garantía 
	EntDest."IdEntidadDestinoGarantia"
	
FROM "BSCL.Custodia.BD::GTA.Garantia" A
INNER JOIN (SELECT gar."IdEmpresa", gar."FolioGarantia", Max(gar."IdGarantia") as "IdGarantia" 
			FROM "BSCL.Custodia.BD::GTA.Garantia" gar
			WHERE gar."EGRCantidad" = 0 and gar."IdEstado" = 'VIG' -- tomar solo los saldos
			GROUP BY gar."IdEmpresa", gar."FolioGarantia" ) as Saldos ON A."IdGarantia" = Saldos."IdGarantia"	
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as cart on cart."IdCartera" = A."IdCarteraOrigen"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" B ON B."IdConcepto" = A."IdConcepto"
LEFT JOIN "BSCL.Custodia.BD::GTA.EspecieEnGarantia" C ON C."IdEspecieGarantia" = A."IdEspecieGarantia"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" i ON i."IdClasificacionTipoInstrumento" = A."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" D ON D."IdTipoInstrumento" = A."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" E ON E."IdInstrumento" = A."IdInstrumento"
LEFT JOIN "BSCL.Custodia.BD::GTA.OrigenGarantia" F ON F."IdOrigenGarantia" = A."IdOrigenGarantia"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" G ON G."IdContraparte" = A."IdContraparteGarantia"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H on H."IdMoneda" = A."IdMonedaSistema"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" H2 on H2."IdMoneda" = A."IdMonedaInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS Z ON Z."IdEstado" = A."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" Y ON Y."IdInstrumento" = A."IdInstrumentoCarteraGarantizada"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS aval ON aval."IdInstrumento" = A."IdInstrumento"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli on cli."IdCliente" = A."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por on por."IdPortafolio" = A."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" TCRiesgo on TCRiesgo."IdClasificacionTipoInstrumento" =  aval."IdClasificacionTipoInstrumento"
LEFT JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" EntDest ON EntDest."IdContraparte" = A."IdContraparteGarantia" 
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" InstruClasi ON InstruClasi."IdInstrumento" = A."IdInstrumento" AND EntDest."IdEntidad" = InstruClasi."IdEntidadClasificadoraRiesgo"    
LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultCar on ultCar."IdEmpresa" = A."IdEmpresa" and ultCar."FolioCartera" = A."FolioCarteraOrigen"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as car on car."IdCartera" = ultCar."IdCartera"
WHERE A."IdEstado" = 'VIG'
	
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
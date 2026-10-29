SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::FWD.UIConsultarMovimientosFWDVencimiento";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::FWD.UIConsultarMovimientosFWDVencimiento" AS

SELECT
	  FWD."IdEmpresa"
	, FWD."NombreEmpresa"
	, FWD."IdCartera"
	, FWD."IdOperacion"
	, FWD."FolioOperacion"
	, FWD."FechaOperacion"
	, FWD."FechaLiquidacion"
	, FWD."FechaAnticipo"
	, FWD."FechaEntregamos"
	--, FWD."NombreMedioDePagoEntregamos"
	, FWD."FechaRecibimos"
	--, FWD."NombreMedioDePagoRecibimos"
	, FWD."HoraOperacion"
	, FWD."FechaOperacionOPE"
	, FWD."IdTipoInstrumento"
	, FWD."IdInstrumento"
	, FWD."NombreInstrumento"
	, FWD."Nemotecnico"
	, FWD."IdTipoOperacion"
	, FWD."TipoOperacion"
	, FWD."IdClasificacionTipoInstrumento"
	, FWD."IdTipoProductoTIPOPE"
	, FWD."IdCliente"
	, FWD."RutCliente"
	, FWD."NombreCliente"
	, FWD."IdMonedaLocal"
	, FWD."IdMonedaPrincipal"
	, FWD."IdMonedaSecundaria"
	, FWD."MontoMonedaPrincipalIngreso"
	, FWD."MontoMonedaPrincipal"
	, FWD."MontoMonedaSecundariaIngreso"
	, FWD."MontoMonedaSecundaria"
	, FWD."ResultadoMonedaLocalPuntas"
	, FWD."ResultadoMonedaLocalBidAsk"
	, FWD."ResultadoMonedaLocalDO"
	, FWD."ResultadoMonedaLocalTCSpot"
	, FWD."ResultadoMonedaLocalTCCierreMesa"
	, FWD."ResultadoMonedaLocalConsolidado"
	, FWD."MontoMonedaPrincipalEquivalente"
	, FWD."IdMonedaEquivalente"
	, FWD."TipoCambioCierre"
	, FWD."TipoCambioCosto"
	, FWD."ParidadCierre"
	, FWD."ParidadCosto"
	, FWD."MontoEquivalente"
	, FWD."Margen"
	, FWD."Utilidad"
	, FWD."MontoDiferido"
	, FWD."MontoCalce"
	, FWD."MontoDisponible"
	, FWD."EGRMontoLiquidacion"
	, FWD."EGRMontoARecibirAnticipo"
	, CASE 
		WHEN FWD."IdEstado" = 'LIQ' THEN FWD."MontoRecibimos" 
		WHEN FWD."IdEstado" IN ('VCD','ANT') THEN 
			CASE 
				WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN FWD."MontoRecibimos" ELSE FWD."MontoRecibimos" / COALESCE(FWD."PrecioVencimiento",1)
			END
		ELSE 0 END
	  AS "MontoRecibimos"
	, CASE 
		WHEN FWD."IdEstado" = 'LIQ' THEN FWD."MontoRecibimos" 
		WHEN FWD."IdEstado" IN ('VCD','ANT') THEN 
			CASE 
				WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN FWD."MontoRecibimos" ELSE FWD."MontoRecibimos" / COALESCE(FWD."PrecioVencimiento",1)
			END
		ELSE 0 END
	  AS "MontoRecibimosAux"
	, CASE 
		WHEN FWD."IdEstado" = 'LIQ' THEN FWD."MontoPagamos" 
		WHEN FWD."IdEstado" IN ('VCD','ANT') THEN 
			CASE 
				WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN FWD."MontoPagamos" ELSE FWD."MontoPagamos" / COALESCE(FWD."PrecioVencimiento",1)
			END
		ELSE 0 END
	  AS "MontoPagamos"
	, CASE 
		WHEN FWD."IdEstado" = 'LIQ' THEN FWD."MontoPagamos" 
		WHEN FWD."IdEstado" IN ('VCD','ANT') THEN 
			CASE 
				WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN FWD."MontoPagamos" ELSE FWD."MontoPagamos" / COALESCE(FWD."PrecioVencimiento",1)
			END
		ELSE 0 END
	  AS "MontoPagamosAux"
	, FWD."IdMonedaMontoRecibimos"
	, FWD."IdMonedaMontoPagamos"
	, FWD."IngresoEgreso"
	, FWD."EsPuntos"
	, FWD."PuntosForward"
	, FWD."TasaForward"
	, FWD."Modalidad"
	, FWD."ModalidadLiquidacion"
	, FWD."ModalidadLiquidacionValor"
	, FWD."IdFixing"
	, FWD."FechaFixingCartera"
	, FWD."FechaFixing"
	, FWD."FechaFixingGrillaVcto"
	, FWD."HoraFixing"
	, FWD."FixingNombre"
	, FWD."Fixing"
	, FWD."Plazo"
	, FWD."PlazoResidual" 
	, FWD."FechaVencimiento"
	, FWD."Precio"
	, FWD."PrecioVencimiento"
	, FWD."ParidadVencimiento"
	, FWD."TipoCambioAnticipo"
	, FWD."ParidadAnticipo"
	, FWD."IdMonedaLiquidacion" 
	, FWD."FechaEfectiva"
	, FWD."IdAgente"
	, FWD."Agente"
	, FWD."NombreAgente"
	, FWD."OperacionFicticia"
	, FWD."CodigoUsuario"
	--, FWD."IdEstado"
	, CASE WHEN FWD."IdEstado" = 'ANU' THEN 'REV'--cartera reversada
		   ELSE FWD."IdEstado" END AS "IdEstado"
	, FWD."Estado"
	, FWD."EstadoOperacion"
	, FWD."IdTipoProducto"
	, FWD."Mesa"
	, FWD."IdSocioDeNegocio"
	, FWD."IdPortafolio"
	, FWD."CodigoPropositoPortafolio"
	, FWD."IdTipoCanalIngreso"
	, FWD."MontoValorRazonableActivo"	
	, FWD."MontoValorRazonablePasivo"
	, FWD."MontoValorRazonableNeto"	
	, FWD."TasaValorRazonableActivo"
	, FWD."TasaValorRazonablePasivo"
	, FWD."INGCantidad"
	, FWD."INGMontoMonedaPrincipal"
	, FWD."INGMontoMonedaSecundaria"
	, FWD."FechaFiltroGrillaVcto"
	, FWD."IdMesa"
	, FWD."IdSpotAsociado"
	, FWD."IdClasificacionCartera"
	, FWD."ClasificacionCartera"
	, FWD."Glosa"
	, FWD."TCObservadoTABANT"
	, FWD."MontoMonedaPricipalTABANT"
	, FWD."MontoMonedaSecundariaTABANT"
	, FWD."TasaDescuentoTABANT"
	, FWD."MontoAnticipoTABANT"
	, FWD."TCAnticipoTABANT"
	, FWD."IdPlaza"
	, FWD."TasaCompra"
	, FWD."EGRIdSpotAsociado"
	, FWD."FechaPago"
	, MF."MontoTrucaRedondea"
	, MF."MontoDecimales"
	, FWD."TCMercadoMonedaPrincipal"
	, FWD."TCMercadoMonedaSecundaria"
	, FWD."Accion"
	, FWD."EGRIdTipoOperacion"
	, FWD."EsAnticipoParcial"
	, FWD."TipoVencimiento"
	, CASE WHEN "ModalidadLiquidacionValor" != 'ENTREGA_FISICA' THEN 
			CASE WHEN FWD."IdEstado" = 'LIQ' THEN 
				CASE WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN FWD."MontoRecibimos" - FWD."MontoPagamos" ELSE ROUND((FWD."MontoRecibimos" - FWD."MontoPagamos") * COALESCE(FWD."PrecioVencimiento",1)) END 
			WHEN FWD."IdEstado" IN ('VCD','ANT') THEN
				FWD."MontoRecibimos" - FWD."MontoPagamos"
			ELSE 0 END
	  ELSE 0 END AS "MontoLiquidacion"
	, FWD."IdMonedaCompensacion"
	, CASE 
		WHEN "ModalidadLiquidacionValor" != 'ENTREGA_FISICA' THEN 
		CASE 
			WHEN FWD."IdEstado" = 'LIQ' THEN FWD."MontoRecibimos" - FWD."MontoPagamos"
			WHEN FWD."IdEstado" IN ('VCD','ANT') THEN 
				CASE 
					WHEN FWD."IdMonedaLiquidacion" = 'CLP' THEN FWD."MontoRecibimos" - FWD."MontoPagamos" ELSE (FWD."MontoRecibimos" - FWD."MontoPagamos") / COALESCE(FWD."PrecioVencimiento",1)
				END
		ELSE 0 END
	 ELSE 0 END AS "ValorLiquidacion"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" AS FWD
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
	ON (MF."IdUsoMonedaFormato" = 'FWD' AND 
		MF."IdMoneda" = FWD."IdMonedaLiquidacion" AND
		MF."IdTipoOperacion" = '')
WHERE FWD."Estado" <> 'ANU'		
;
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::FWD.UIConsultarMovimientosFWDVencimiento" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

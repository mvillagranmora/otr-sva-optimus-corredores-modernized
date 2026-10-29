SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldosPrestamistas";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldosPrestamistas" AS
SELECT
	Empresa."Nombre" AS "NombreEmpresa",
	Mon1."Nombre" AS "Moneda",
	Mon2."Nombre" AS "MonedaInstrumento",
	Estado."Nombre" AS "NombreEstado",
	TipoOperacion."Nombre" AS "NombreTipoOperacion",	
	Boveda."Nombre" AS "NombreBoveda",
	TipoReajuste."Nombre" AS "NombreTipoReajuste",
	(  ( TipoReajuste."IdTipoReajuste" || ' - ' )  || TipoReajuste."Nombre" )  AS "NombreLargoTipoReajuste",
	Bolsa."Nombre" AS "NombreBolsa",
	Concepto."Nombre" AS "NombreConcepto",
	CondicionLiquidacion."Nombre" AS "NombreCondicionLiquidacion",
--	Modulo."Nombre" AS "NombreModulo",
	cli."DocumentoIdentidad",
	Port."CodigoPortafolio",
	Port."Proposito" as "PropositoPortafolio",
	cli."NombreCompleto" AS "RazonSocial",
	case 
		when car."IdTipoInstrumento" IN ('SIM','PRT','EMP','RRV','RRF') then
			--(select distinct "Nemotecnico" from "BSCL.Custodia.BD::CUS.Subyacente" as Suby where (car."IdEmpresa" = Suby."IdEmpresa" and car."FolioCartera" = Suby."FolioCarteraRelacionada"))
			(select "Nemotecnico" 
			from (
					select "Nemotecnico", row_number() over(order by (SELECT 1 from dummy) ) as "NroFila" 
					from "BSCL.Custodia.BD::CUS.Subyacente" as miSub
					where car."FolioCartera" = miSub."FolioCarteraRelacionada"
				 ) as Suby
			where Suby."NroFila" = 1
			)

		else car."Nemotecnico"
	end as "Nemotecnico",
--	Riesgo."ClasificacionRiesgo",
		CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' THEN Emi."ValorClasificacionRiesgoIIF" ELSE ICR."ValorClasificacionRiesgo" END AS "ValorClasificacionRiesgo",
	Ins."Reajustable",
	ClasInst."Nombre" as "ClasificacionTipoInstrumento",
	TipoInstrumento."Descripcion" AS "TipoInstrumento",
	car."IdMonedaEmision",
--	Emisor."Nombre" AS "NombreEmisor",
	(CASE
		when car."PlazoEnDiasOperacion" > 0 then car."PlazoEnDiasOperacion"
		WHEN car."FechaVencimientoOperacion" > TO_DATE('0001-01-01','YYYY-MM-DD') THEN DAYS_BETWEEN(car."FechaOperacion", car."FechaVencimientoOperacion")
		ELSE 0
	END) AS "PlazoEnDiasOperacion", 
	tpres."IdTipoPrestamo",
	tpres."MinimoDiasAnticipo",
	tpres."MaximoDiasPrestamo",
	case when IFNULL(car."IdTipoPrestamo",'') <> '' then car."Nemotecnico" else '' end as "NombreTipoPrestamo",
	tpres."EsDiaHabilPlazoPrestamo",
	tpres."EsDiaHabilParaCalculoInteres",
	Usuario."CodigoUsuario" as "NombreUsuario"
	
	
	, car."IdCartera"
	, car."FolioCartera"
	, car."FolioCarteraRelacionada"
	, car."FolioGarantiaOrigen"
	, car."FechaIngresoRegistro"
	, car."FechaModificacionRegistro"
	, car."IdEmpresa"
	, car."FechaOperacion"
	, car."FechaVencimientoOperacion"
	, car."IdCliente"
	, car."IdPortafolio"
	, car."IdClienteRelacionado"
	, car."IdPortafolioRelacionado"
	, car."IdEstado"
	, car."IdTipoProducto"
	, car."IdTipoCanalIngreso"
	, car."IdMonedaSistema"
	, car."IdMovimientoCarteraEnTransito"
	, car."IdConcepto"
	, car."IdTipoOperacion"
	, car."IdOperacion"
	, car."IdTipoComprobante"
	, car."FolioComprobante"
	, car."FechaComprobante"
	, car."FolioExternoReferencia" as "FolioEntidad"
	, car."IdMesa"
	, car."IdBolsa"
	, car."IdSucursal"
	, car."IdAgente"
	, car."IdCondicionLiquidacion"
	, car."FechaLiquidacion"
	, car."EsIntraday"
	, car."UsaGarantiaEspecifica"
	, car."CobraPrimaAlInicio"
	, car."Glosa"
	, car."IdUsuario"
	, resTot."IdUsuarioReserva"
	, car."IdModulo"
	, car."TipoCarteraNormativa"
	, car."FechaCurseInversion"
	, car."TipoFactorAjuste"
	, car."ValorFactorAjusteaFechaCurse"
	, car."CostoInversionEnFactorAjuste"
	, car."IdClasificacionTipoInstrumento"
	, car."IdTipoInstrumento"
	, car."IdInstrumento"
	, car."IdEmisor"
	, car."NemoSVS"
	, car."IdTipoReajuste"
	, car."FechaEmisionInstrumento"
	, car."FechaVencimientoInstrumento"
	, car."EsFungible"
	, car."CodigoIFRS"
	, car."IdBoveda"
	, car."CuentaBoveda"
	, car."IdMonedaInstrumento"
	, car."Tamano"
	, car."TIFAfectoBeneficio"
	, car."TIFTasa"
	, car."Cantidad"
	, car."CorteMinimo"
	, car."ValorRescate"
	, car."PrecioCompra"
	, car."TasaCompra"

	--, car."MontoCompra"
	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
			    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN	
      	        	 	  ( ( (CAR."INGValorRescate")-resTot."IngValorRescate")  * CAR."MontoCompra" ) 
      	        	 	 		/ (CAR."INGValorRescate")
      	        	 ELSE CAR."MontoCompra" 
      	        END
	  	   --* RF's
	  	   ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
	 	  	   			 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."MontoCompra" ) 
	 	 	        	 / CAR."INGCantidad" 
	  	   			 ELSE CAR."MontoCompra" 
	  	   END 
	  END AS "MontoCompra"	
	
	, car."MontoCompraMonedaInstrumento"
	, car."TasaAgente"
	, car."PrecioAPlazo"
	, car."MontoAPlazo"
	, car."MontoAPlazoMonedaInstrumento"
	, car."FechaEfectiva"
	, car."IdPlaza"
	, car."IdFixing"
	, car."FechaFixing"
	, car."HoraFixing"
	, car."IdModalidadCumplimiento"
	, car."IdReferenciaMercado"
	, car."IdMonedaPrincipal"
	, car."IdMonedaSecundaria"
	, car."ValorTipoCambioCierre"
	, car."ValorParidadCierre"
	, car."ValorTipoCambioCosto"
	, car."ValorParidadCosto"
	, car."MontoMonedaPrincipal"
	, car."MontoMonedaSecundaria"
	, car."IdCarga"
	, car."IdSecuenciaCarteraMigracion"
	, car."CodigoPeriodo"
	, car."INGGlosa"
	, car."INGTasaAgente"
	, car."INGFechaOperacion"
	, car."INGIdCondicionLiquidacion"
	, car."INGFechaLiquidacion"
	
--	, (car."INGCantidad" - COALESCE(resTot."IngCantidad",0)) AS "INGCantidad"	
	, CASE WHEN COALESCE(resTot."IngCantidad",0) > 0 
	  THEN CASE WHEN CAR."EsFungible" = 'S' AND CAR."IdClasificacionTipoInstrumento" = 'IF' 
	  	   THEN CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" 
	  	        THEN 1
	  	        ELSE 0 END
	  	   ELSE car."INGCantidad" - resTot."IngCantidad" END	
	  ELSE car."INGCantidad" 
	  END AS "INGCantidad"	
	
--	, (car."INGValorRescate" - COALESCE(resTot."IngValorRescate",0)) AS "INGValorRescate"
	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
			    ( CAR."INGValorRescate" - COALESCE(resTot."IngValorRescate",0) ) 
	  	   --* RF's
	  	   ELSE ( CAR."INGValorRescate" - COALESCE(resTot."IngValorRescate",0) )
	  END AS "INGValorRescate"	
	
	
	, car."INGPrecioPromedioCompra"
	, car."INGMontoPrecioPromedioCompra"
	, car."INGMontoPrecioPromedioMonedaInstrumento"
	, car."INGPrecioAPlazo"
	, car."INGMontoAPlazo"
	, car."INGMontoAPlazoMonedaInstrumento"
	, car."INGEsPuntos"
	, car."INGValorTipoCambioSpot"
	, car."INGValorTipoCambioCotizador"
	, car."INGTasaPuntosMonedaPrincipal"
	, car."INGTasaPuntosMonedaSecundaria"
	, car."INGFechaValorizacionCartera"
	, car."INGFechaValorizacionAnterior"
	, car."INGFechaValorizacionActual"
	, car."INGFechaValorizacionProximo"
	, case when ifnull(car."INGPrecioCompra",0) = 0 then car."PrecioCompra" else ifnull(car."INGPrecioCompra",0) end as "INGPrecioCompra"
	, car."INGTasaCompra"
	, car."INGMontoATasaCompraAnterior"
	, car."INGMontoInteresCompraAnterior"
	, car."INGMontoReajusteCompraAnterior"
	, car."INGMontoCompraAnteriorMonedaInstrumento"
	, car."INGMontoInteresCompraAnteriorMonedaInstrumento"
	, car."INGMontoReajusteCompraAnteriorMonedaInstrumento"
	, car."INGDurationCompraAnterior"
	, car."INGConvexidadCompraAnterior"
	, car."INGMontoCompraAnteriorPibMas"
	, car."INGMontoCompraAnteriorPibMasMonedaInstrumento"
	, car."INGMontoCompraAnteriorPibMenos"
	, car."INGMontoCompraAnteriorPibMenosMonedaInstrumento"

	--, (car."INGMontoATasaCompraActual" - COALESCE(resTot."IngMontoValorizado",0)) AS "INGMontoATasaCompraActual"
	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
			    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN	
      	        	 	  ( ( ( CAR."INGValorRescate")-resTot."IngValorRescate")  * CAR."INGMontoATasaCompraActual" ) 
      	        	 	 		/ ( CAR."INGValorRescate")
      	        	 ELSE CAR."INGMontoATasaCompraActual" 
      	        END
	  --* RF's
	  ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
	  	   			 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."INGMontoATasaCompraActual" ) 
	  	        	 / CAR."INGCantidad" 
	  	   		ELSE CAR."INGMontoATasaCompraActual" 
	  	   END 
	  END AS "INGMontoATasaCompraActual"	
	
	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
			    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN	
      	        	 	  ( ( ( CAR."INGValorRescate")-resTot."IngValorRescate")  * CAR."INGMontoCompraActualMonedaInstrumento" ) 
      	        	 	 		/ ( CAR."INGValorRescate")
      	        	 ELSE CAR."INGMontoCompraActualMonedaInstrumento" 
      	        END
	  --* RF's
	  ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
	  	   			 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."INGMontoCompraActualMonedaInstrumento" ) 
	  	        	 / CAR."INGCantidad" 
	  	   		ELSE CAR."INGMontoCompraActualMonedaInstrumento" 
	  	   END 
	  END AS "INGMontoCompraActualMonedaInstrumento" --car."INGMontoCompraActualMonedaInstrumento"
	, car."INGDurationCompraActual"
	, car."INGConvexidadCompraActual"
	, car."INGMontoCompraActualPibMas"
	, car."INGMontoCompraActualPibMasMonedaInstrumento"
	, car."INGMontoCompraActualPibMenos"
	, car."INGMontoCompraActualPibMenosMonedaInstrumento"
	, car."INGMontoATasaCompraProximo"
	, car."INGMontoInteresCompraProximo"
	, car."INGMontoInteresCorridoCompraActual"
	, car."INGMontoReajusteCompraProximo"
	, car."INGMontoCompraProximoMonedaInstrumento"
	, car."INGMontoInteresCompraProximoMonedaInstrumento"
	, car."INGMontoReajusteCompraProximoMonedaInstrumento"
	, car."INGDurationCompraProximo"
	, car."INGConvexidadCompraProximo"
	, car."INGFechaPrecioTasaMercadoAnterior"
	, car."INGFuenteTasaPrecioMercadoAnterior"
	, car."INGPrecioMercadoAnterior"
	, car."INGTasaMercadoAnterior"
	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN
			    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN	
      	        	 	  ( ( ( CAR."INGValorRescate")-resTot."IngValorRescate")  * CAR."INGMontoATasaMercadoAnterior" ) 
      	        	 	 		/ ( CAR."INGValorRescate")
      	        	 ELSE CAR."INGMontoATasaMercadoAnterior" 
      	        END
	  --* RF's
	  ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
	  	   			 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."INGMontoATasaMercadoAnterior" ) 
	  	        	 / CAR."INGCantidad" 
	  	   		ELSE CAR."INGMontoATasaMercadoAnterior" 
	  	   END 
	  END AS "INGMontoATasaMercadoAnterior" -- car."INGMontoATasaMercadoAnterior"
	, car."INGMontoInteresMercadoAnterior"
	, car."INGMontoReajusteMercadoAnterior"
	, car."INGMontoMercadoAnteriorMonedaInstrumento"
	, car."INGMontoInteresMercadoAnteriorMonedaInstrumento"
	, car."INGMontoReajusteMercadoAnteriorMonedaInstrumento"
	, car."INGDurationMercadoAnterior"
	, car."INGConvexidadMercadoAnterior"
	, car."INGMontoMercadoAnteriorPibMas"
	, car."INGMontoMercadoAnteriorPibMasMonedaInstrumento"
	, car."INGMontoMercadoAnteriorPibMenos"
	, car."INGMontoMercadoAnteriorPibMenosMonedaInstrumento"
	, car."INGFechaPrecioTasaMercadoActual"
	, car."INGFuenteTasaPrecioMercadoActual"
	, car."INGPrecioMercadoActual"
	, car."INGTasaMercadoActual"
	
	--, car."INGMontoATasaMercadoActual"
	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' THEN
      	   	    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN
	  				 --* Tiene tomado solo una parte del papel ?
      	        	 ( ( CAR."INGValorRescate"-resTot."IngValorRescate")  * CAR."INGMontoATasaMercadoActual" ) 
      	        	 / CAR."INGValorRescate"
      	        	 ELSE CAR."INGMontoATasaMercadoActual" 
      	        END
	  	   --* RF's
	  	   ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
	  	   	 		 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."INGMontoATasaMercadoActual" ) 
	  	        	 / CAR."INGCantidad" 
	  	   			 ELSE CAR."INGMontoATasaMercadoActual" 
	  	   		END
	  END AS "INGMontoATasaMercadoActual"	
	  
--	, car."INGMontoMercadoActualMonedaInstrumento"
	,  CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' THEN
      	   	    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN
	  				 --* Tiene tomado solo una parte del papel ?
      	        	 ( ( CAR."INGValorRescate"-resTot."IngValorRescate")  * CAR."INGMontoMercadoActualMonedaInstrumento" ) 
      	        	 / CAR."INGValorRescate"
      	        	 ELSE CAR."INGMontoMercadoActualMonedaInstrumento" 
      	        END
	  	   --* RF's
	  	   ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
	  	   	 		 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."INGMontoMercadoActualMonedaInstrumento" ) 
	  	        	 / CAR."INGCantidad" 
	  	   			 ELSE CAR."INGMontoMercadoActualMonedaInstrumento" 
	  	   		END
	  END AS "INGMontoMercadoActualMonedaInstrumento"	
	, car."INGDurationMercadoActual"
	, car."INGConvexidadMercadoActual"
	, car."INGMontoMercadoActualPibMas"
	, car."INGMontoMercadoActualPibMasMonedaInstrumento"
	, car."INGMontoMercadoActualPibMenos"
	, car."INGMontoMercadoActualPibMenosMonedaInstrumento"
	, car."INGFechaPrecioTasaMercadoProximo"
	, car."INGFuenteTasaPrecioMercadoProximo"
	, car."INGPrecioMercadoProximo"
	, car."INGTasaMercadoProximo"
	, CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."EsFungible" = 'S' THEN
      	   	    CASE WHEN CAR."INGValorRescate" > resTot."IngValorRescate" THEN
	  				 --* Tiene tomado solo una parte del papel ?
      	        	 ( ( CAR."INGValorRescate"-resTot."IngValorRescate")  * CAR."INGMontoATasaMercadoProximo" ) 
      	        	 / CAR."INGValorRescate"
      	        	 ELSE CAR."INGMontoATasaMercadoProximo" 
      	        END
	  	   --* RF's
	  	   ELSE CASE WHEN resTot."IngCantidad" > 0 AND CAR."INGCantidad" > 0 THEN
	  	   	 		 ( ( CAR."INGCantidad"-resTot."IngCantidad")  * CAR."INGMontoATasaMercadoProximo" ) 
	  	        	 / CAR."INGCantidad" 
	  	   			 ELSE CAR."INGMontoATasaMercadoProximo" 
	  	   		END
	  END AS "INGMontoATasaMercadoProximo"	-- car."INGMontoATasaMercadoProximo"
	, car."INGMontoInteresMercadoProximo"
	, car."INGMontoReajusteMercadoProximo"
	, car."INGMontoMercadoProximoMonedaInstrumento"
	, car."INGMontoInteresMercadoProximoMonedaInstrumento"
	, car."INGMontoReajusteMercadoProximoMonedaInstrumento"
	, car."INGDurationMercadoProximo"
	, car."INGConvexidadMercadoProximo"
	, car."INGTasaCorredora"
	, car."INGFechaTasaCorredora"
	, car."INGMontoATasaCorredora"
	, car."INGMontoATasaCorredoraMonedaInstrumento"
	, car."INGFechaCorteCupon"
	, car."INGNumeroCupon"
	, car."INGIdMonedaPagoCupon"
	, car."INGMontoCorteCupon"
	, car."INGFechaPagoCorteCupon"
	, car."INGMontoCapitalCorteCupon"
	, car."INGMontoInteresCorteCupon"
	, car."INGMontoReajusteCorteCupon"
	, car."INGMontoCorteCuponMonedaInstrumento"
	, car."INGMontoCapitalCorteCuponMonedaInstrumento"
	, car."INGMontoInteresCorteCuponMonedaInstrumento"
	, car."INGMontoReajusteCorteCuponMonedaInstrumento"
	, car."INGMontoPresenteTIF"
	, car."INGMontoInteresTIF"
	, car."INGMontoReajusteTIF"
	, car."INGMontoInteresAcumTIF"
	, car."INGMontoReajusteAcumTIF"
	, car."INGMontoPresenteTIFMonedaInstrumento"
	, car."INGMontoInteresTIFMonedaInstrumento"
	, car."INGMontoReajusteTIFMonedaInstrumento"
	, car."INGMontoInteresAcumTIFMonedaInstrumento"
	, car."INGMontoReajusteAcumTIFMonedaInstrumento"
	, car."INGMontoMonedaPrincipal"
	, car."INGMontoMonedaSecundaria"
	, car."INGMontoDiferido"
	, car."INGMontoUtilidadDiferida"
	, car."INGMontoPerdidaDiferida"
	, car."INGMontoMargen"
	, car."INGMontoValorRazonableActivo"
	, car."INGMontoValorRazonablePasivo"
	, car."INGMontoValorRazonableNeto"
	, car."INGTasaValorRazonableActivo"
	, car."INGTasaValorRazonablePasivo"
	, car."INGMontoDevengoTipoCambio"
	, car."INGMontoDevengoTipoCambioAnterior"
	, car."INGMontoDiferidoDiario"
	, car."INGMontoDiferidoAcumulado"
	, car."INGMontoMatriz"
	, car."TipoCarteraCorredor"
	, car."TipoValorizacion"
	, Ins."EsPutEsCall" as "TipoOpcion"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS car
LEFT JOIN (
    SELECT res."FolioCarteraOrigen"
        ,res."IngIdUsuario" as "IdUsuarioReserva"
        ,SUM("IngCantidad") AS "IngCantidad"
        ,SUM("IngValorRescate") AS "IngValorRescate"
        ,SUM("MontoValorizado") AS "IngMontoValorizado"
    FROM "BSCL.Custodia.BD::CUS.Reserva" AS res 
    WHERE res."EgrCantidad" = 0
    GROUP BY res."FolioCarteraOrigen", res."IngIdUsuario") AS resTot ON resTot."FolioCarteraOrigen" = car."FolioCartera"  
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON (Port."IdPortafolio" = car."IdPortafolio")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON (Cli."IdEmpresa" = car."IdEmpresa" and Cli."IdCliente" = Port."IdCliente")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON (car."IdTipoOperacion" = TipoOperacion."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" as Boveda ON (car."IdBoveda" = Boveda."IdBoveda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoReajuste" as TipoReajuste ON (car."IdTipoReajuste" = TipoReajuste."IdTipoReajuste")
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa ON (car."IdBolsa" = Bolsa."IdBolsa")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Ins ON (car."IdInstrumento" = Ins."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Ins."IdInstrumento"

LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON (car."IdConcepto" = Concepto."IdConcepto")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as ConceptoEGR ON (car."EGRIdConcepto" = ConceptoEGR."IdConcepto")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" as CondicionLiquidacion ON (car."IdCondicionLiquidacion" = CondicionLiquidacion."IdCondicionDeLiquidacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" as Empresa ON (car."IdEmpresa" = Empresa."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" as Usuario ON (car."IdUsuario" = Usuario."IdUsuario")

LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as Estado ON (car."IdEstado" = Estado."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON (TipoInstrumento."IdTipoInstrumento" = Ins."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON (ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento")

LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon1 ON (car."IdMonedaSistema" = Mon1."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon2 ON (car."IdMonedaInstrumento" = Mon2."IdMoneda")
LEFT JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" as tpres ON (car."IdInstrumento" = tpres."IdInstrumentoPrestamo")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldosPrestamistas"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
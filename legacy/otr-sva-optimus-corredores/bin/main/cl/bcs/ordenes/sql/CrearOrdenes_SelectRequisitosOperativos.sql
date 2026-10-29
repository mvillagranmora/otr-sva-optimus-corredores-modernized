SELECT	"IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTEMPRESTITOS' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoEmprestitos",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTSIM' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoSimultanea",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCUSTODIA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoCustodia",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PORTBLOQ' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PortafolioBloqueado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCONDGENFUT' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoFuturos",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTVENCORRV' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoVentaCortaRV",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTVENCORRF' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoVentaCortaRF",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTMERCANTIL' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoMercantil",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CNOCRV'  AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_CompraNominalRV",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CNOCRF' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_CompraNominalRFIF",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'VNOCRV' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_VentaNominalRV",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'VNOCRF' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_VentaNominalRFIF",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONORDVERESC' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ConfirmacionOrdenVerbalEscrita",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCONDGRALES' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoCondicionesGenerales",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTDECCRSFATCA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoDeclaracionCRSFatca",
		
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CLIENTEBLOQUEADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ClienteBloqueado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PERSONABLOQUEADA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PersonaBloqueada",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'OPERACIONSINFLUJOCUSTODIA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OperacionSinFlujoCustodia",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'EJECUTIVOSINPRODUCTO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_EjecutivoSinProductoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'EJECUTIVOSINRUTEO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_EjecutivoSinRuteoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'EJECUTIVOPORTAFOLIOBLOQUEADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_EjecutivoPortafolioBloqueado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MONTOOPERACIONSUPERAPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MontoOperacionSuperaPermitidoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COMISIONSUPERAMAXIMOPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ComisionSuperaMaximoPermitido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COMISIONBAJOMINIMOPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ComisionBajoMinimoPermitido",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MONTOSUPERAMAXIMOCANALDISTRIBUCION' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MontoSuperaMaximoPermitidoCanalDistribucion",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'ORDENREQUIEREAUTORIZACION' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OrdenRequiereAutorizacion",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'DESCUENTOCOMISIONSUPERAPERMITIDO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_DescuentoComisionSuperaPermitidoPlantilla",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'FOTOCOPIACARNET' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_FotocopiaCarnet",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'ANTECEDENTESLEGALES' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_AntecedentesLegales",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'ORDENPERMANENTEPRECIOMERCADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OrdenPermanentePrecioMercado",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MECANISMOCAPTACIONHABILITADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MecanismoCaptacion",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'DIFERENCIAPRECIOORDEN' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_DiferenciaPrecioOrden",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MONEDAINSTRUMENTODIFMONEDALIQUIDACION' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MonedaInstrumentoDiferenteMonedaLiquidacion",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'ORDENMISMAOPERACIONINSTRUMENTO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OrdenMismaOperacionInstrumento",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'RUTEOPRECIOMERCADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_RuteoPrecioMercado",		
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'NOUSAPREFERENCIANEGOCIOOPERACTAINV' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_NoUsaPreferenciaNegocioOperaCuentaInversion",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COVEREXGICOR' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ExigibilidadCorredora",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COVEREXGIENT' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ExigibilidadEntidad",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'OPERACIONSINFLUJOCAJA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OperacionSinFlujoCaja",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PORCASMAYOIGUDEF' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PorcentajeCastigo",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'OPERACIONSINFLUJOVENTADESCUBIERTA' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_OperacionSinFlujoVentaDescubierta",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" =	'FLUJOCUSTODIAINSUFICIENTE' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_FlujoCantidadCustodiaInsuficiente",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" =	'MONTOMINIMOORDEN' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MontoMinimoCrearOrden",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" =	'MONTOMINIMOORDENAUTORIZAR' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MontoMinimoCrearOrdenAutorizar",
		COALESCE(MAX(CASE WHEN "IdCondicionOperativa" =	'INSTRUMENTOCALIFICADO' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_InstrumentoCalificado",
		
		--OPCIONES
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCONDGENOPC' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoOpciones",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'CONTCANINTERNET' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_ContratoCanalInternet",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'COBROMAXPER' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_CobroMaxPermitido",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PORCENTAJEMAXORDEN' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PorcentajeMaxOrden",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'PORCENTAJEVISIBLIDAD' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_PorcentajeVisibilidad",
	    COALESCE(MAX(CASE WHEN "IdCondicionOperativa" = 'MONTOORDENVENTAMENORCOSTOS' AND "IdEstado" = 'ACT' THEN "IdTipoComportamiento" END),null) AS "CO_MontoOrdenVentaMenorCostos"
	FROM (
	
		SELECT "IdCondicionOperativa","IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso","IdTipoComportamiento","IdEstado"
		FROM "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo"
		WHERE "IdTipoEtapa" = 'INGRESOORDEN' and "IdEmpresa" = :IdEmpresa
	) 
	GROUP BY "IdTipoEtapa","IdEmpresa","IdTipoCanalIngreso";


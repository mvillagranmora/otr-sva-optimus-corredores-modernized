SELECT 
	"IdInterno"
	,"IdMovimiento"
	,"FechaSimulacion"
	,salsim."FechaIngresoRegistro"
	,salsim."IdTransaccion"
	,"FolioTransaccion"
	,"FechaTransaccion"
	,salsim."FolioCartera"
	,"IdPortafolio"
	,"CodigoPortafolio"
	,"IdCliente"
	,salsim."IdLibroOrdenes"
	,salsim."IdInstrumento"
	,salsim."Instrumento"
	,salsim."IdTipoInstrumento"
	,"IdMonedaInstrumento"
	,"IdOrden"
	,"NombreCompleto"
	,salsim."DocumentoIdentidad"
	,"CantidadAsignacion"
	,"ValorRescateAsignacion"
	,CASE
		WHEN salsim."IdClasificacionTipoInstrumento" != 'IF'
			THEN
				salsim."CantidadTransaccion"
			ELSE
				tra."ValorRescate"
	END 																AS "ValorRescateMontoTransaccion"
	,"PorcentajeAsignacion"
	,"PrecioSimulacion"
	,salsim."IdTipoProducto"
	,salsim."IdTipoOperacion"
	,salsim."IdTipoCanalIngreso"
	,salsim."IdBolsa"
	,salsim."IdMonedaLiquidacionOrden"
	,salsim."IdClasificacionTipoInstrumento"
	,salsim."EsFungible"
	,salsim."CantidadTransaccion"
	,salsim."MontoTransaccion"
	,salsim."MontoAsignacion"
	,bol."EsExtranjera"
	,CASE
		WHEN salsim."IdClasificacionTipoInstrumento" != 'IF'
			THEN 
				"CantidadAsignacion"
			ELSE 
				"ValorRescateAsignacion"
	 END 																AS "CantidadAsignadaSimulacion"
	,CASE
		WHEN salsim."IdClasificacionTipoInstrumento" != 'IF'
			THEN 
				"CantidadTransaccion"
			ELSE
				"ValorRescateAsignacion"
	 END 																AS "CantidadTransaccionSimulacion"
	

	--OPCIONES CL
	,inst."PrecioStrike"
	,CASE 
		WHEN inst."EsDivisible" = 'P' 
			THEN 'FISICA' 
		WHEN inst."EsDivisible" = 'C' 
			THEN 'FINANCIERA' 
			ELSE '' 
	END																	AS "ModalidadLiquidacion"
	,CASE 
		WHEN inst."LargoPlazo" = 'A' 
			THEN 'AUTOMATICA' 
		WHEN inst."LargoPlazo" = 'M' 
			THEN 'MANUAL' 
			ELSE '' 
	END 																AS "ModalidadEjercicio"
	,CASE 
		WHEN inst."ISIN" = 'C' 
			THEN 'CALL' 
		WHEN inst."ISIN" = 'P' 
			THEN 'PUT' 
			ELSE '' 
	END 																AS "TipoOpcion"
	,CASE 
		WHEN inst."EstaInscrito" = 'A' 
			THEN 'AMERICANA' 
		WHEN inst."EstaInscrito" = 'E' 
			THEN 'EUROPEA' 
			ELSE '' 
	END 																AS "EstiloOpcion"
	,inst."Tamano"
	,inst."CorteMinimo"
	,inst1."Nemotecnico" AS "InstrumentoNegociacionTran"	
FROM "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Simulacion" salsim
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" AS 		tra 	ON tra."IdTransaccion" 		= salsim."IdTransaccion"
LEFT JOIN  "BSCL.Plataforma.BD::INS.Instrumento" 	inst 	ON salsim."IdInstrumento" 	= inst."IdInstrumento"
LEFT JOIN  "BSCL.Plataforma.BD::INS.Instrumento" 	inst1 	ON tra."IdInstrumentoNegociacion" 	= inst1."IdInstrumento"
LEFT JOIN  "BSCL.Plataforma.BD::INS.Bolsa" 			bol 	ON salsim."IdBolsa" 		= bol."IdBolsa";


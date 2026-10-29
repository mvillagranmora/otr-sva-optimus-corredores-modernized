WITH 
	Operaciones AS (
		SELECT 	 CAR."IdEmpresa"
				,CAR."IdCliente" 
				,DI."DocumentoIdentidad" 
				,POR."IdPortafolio"
				,POR."CodigoPortafolio"
				,CAR."INGFechaOperacion" 																					AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" 																					AS "FechaLiquidacion"
				,CAR."FechaVencimientoOperacion" 																			AS "FechaVencimientoOperacion"
				,CAR."IdCartera"																							AS "IdOperacion"
				,CAR."FolioCartera"																							AS "FolioOperacion"
				,COALESCE(SUB."IdClasificacionTipoInstrumento", CAR."IdClasificacionTipoInstrumento")						AS "IdClasificacionTipoInstrumento"
				,COALESCE(SUB."IdTipoInstrumento", CAR."IdTipoInstrumento") 												AS "IdTipoInstrumento"
				,COALESCE(SUB."IdInstrumento",CAR."IdInstrumento")															AS "IdInstrumento"
				,CASE WHEN COALESCE(SUB."IdClasificacionTipoInstrumento", CAR."IdClasificacionTipoInstrumento") = 'IF' 
					  THEN COALESCE(SUB."NemoSVS", CAR."NemoSVS") ELSE COALESCE(SUB."NemoSVS", CAR."NemoSVS")  END 			AS "Nemotecnico"
				,COALESCE(SUB."IdEmisor", CAR."IdEmisor") 																	AS "IdEmisor"
				,COALESCE(SUB."IdTipoReajuste", CAR."IdTipoReajuste")														AS "IdTipoReajuste"
				,COALESCE(SUB."FechaVencimientoInstrumento",CAR."FechaVencimientoInstrumento")								AS "FechaVencimientoInstrumento"
				,CAR."IdBoveda"
				,CAR."CodigoIFRS"
				,CAR."IdMonedaSistema"
				,COALESCE(SUB."IdMonedaInstrumento", CAR."IdMonedaInstrumento")												AS "IdMonedaInstrumento"
				,CASE WHEN COALESCE(SUB."IdClasificacionTipoInstrumento", CAR."IdClasificacionTipoInstrumento") = 'IF' 
					  THEN COALESCE(SUB."INGValorRescate", CAR."INGValorRescate")
					  ELSE COALESCE(SUB."INGCantidad",CAR."INGCantidad")
				 END																										AS "Cantidad"
				,COALESCE(SUB."INGTasaSubyacente",0)																		AS "TasaTir"
				,CAR."INGTasaCompra"																						AS "TasaCompra"
				,COALESCE(CAR."INGPrecioCompra",0) 																			AS "PrecioCompra"
				,CAR."MontoAPlazo"																							AS "MontoAPlazoAlInicio" 
				,CAR."MontoAPlazoMonedaInstrumento"																			AS "ValorAPlazoAlInicio"
				
				,COALESCE(CAR."INGMontoMercadoAnteriorMonedaInstrumento",SUB."INGMontoMercadoAnteriorMonedaInstrumento")	AS "ValorMercadoAnterior"
				,COALESCE(CAR."INGTasaMercadoAnterior",SUB."INGTasaMercadoAnterior")										AS "MontoMercadoAnterior"
				,COALESCE(CAR."INGMontoMercadoActualMonedaInstrumento",SUB."INGMontoMercadoActualMonedaInstrumento")		AS "ValorMercadoActual"
				,COALESCE(CAR."INGTasaMercadoActual",SUB."INGTasaMercadoActual")											AS "MontoMercadoActual"
				,COALESCE(CAR."INGMontoMercadoProximoMonedaInstrumento",SUB."INGMontoMercadoProximoMonedaInstrumento")		AS "ValorMercadoProximo"
				,COALESCE(CAR."INGMontoATasaMercadoProximo",SUB."INGMontoATasaMercadoProximo")								AS "MontoMercadoProximo"
				
				,COALESCE(CAR."INGMontoATasaCompraAnterior",SUB."INGMontoATasaSubyacenteAnterior")							AS "MontoCompraAnterior"
				,COALESCE(CAR."INGMontoATasaCompraActual",SUB."INGMontoATasaSubyacenteActual")								AS "MontoCompraActual"
				,COALESCE(CAR."INGMontoATasaCompraProximo",SUB."INGMontoATasaSubyacenteProximo")							AS "MontoCompraProximo"
				,COALESCE(CAR."INGMontoCompraAnteriorMonedaInstrumento",SUB."INGMontoSubyacenteAnteriorMonedaInstrumento")	AS "ValorCompraAnterior"
				,COALESCE(CAR."INGMontoCompraActualMonedaInstrumento",SUB."INGMontoSubyacenteActualMonedaInstrumento")		AS "ValorCompraActual"
				,COALESCE(CAR."INGMontoCompraProximoMonedaInstrumento",SUB."INGMontoSubyacenteProximoMonedaInstrumento")	AS "ValorCompraProximo"
				,CAR."IdEstado"
		FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" 
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = CAR."IdCliente" 
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON DI."IdPersona" = CLI."IdPersona" 
		LEFT JOIN "BSCL.Custodia.BD::HIS.Subyacente" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."IdEstado" = 'VIG'
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONF ON CONF."CodigoParametro" = 'CUS.UsaFechaLiquidacionEnVencimiento'
		WHERE CAR."IdEstado" = 'VIG'
		  AND CAR."IdTipoInstrumento" IN ('PRF', 'PRT')
		  AND CAR."IdTipoInstrumento" = :IdTipoInstrumento
		  AND CAR."FechaRespaldo" = :FechaContable 
		  AND ( (COALESCE(CONF."Valor",'S') = 'S' AND CAR."INGFechaLiquidacion" <= :FechaContable)  OR  (COALESCE(CONF."Valor",'S') = 'N' AND CAR."INGFechaOperacion" <= :FechaContable) )
		  AND CAR."IdEmpresa" = :IdEmpresa
 		)
 SELECT	 OPE."DocumentoIdentidad" 
 		,"IdPortafolio"
		,"CodigoPortafolio"
		,CASE EMP."DocumentoIdentidad" WHEN OPE."DocumentoIdentidad" THEN 'SI' ELSE 'NO' END AS "EsCuentaPropia"
		,CASE WHEN COALESCE(CIC."ValorIndicador",'N') ='S' THEN 'SI' ELSE 'NO' END AS "EsRelacionado"
		,"FechaOperacion"
		,"FechaVencimientoOperacion"
		,"FechaLiquidacion"
		,"IdOperacion"
		,"FolioOperacion"
		,"IdClasificacionTipoInstrumento"
		,"IdTipoInstrumento"
		,"IdInstrumento"
		,"Nemotecnico"
		,OPE."IdEmisor"
		,"IdTipoReajuste"
		,"FechaVencimientoInstrumento"
		,"IdBoveda"
		,"CodigoIFRS"
		,"IdMonedaSistema"
		,"IdMonedaInstrumento"
		,"Cantidad"
		,"TasaTir"
		,"TasaCompra"
		,"PrecioCompra"
		,"MontoAPlazoAlInicio"
		,"ValorAPlazoAlInicio"
		,"ValorMercadoAnterior"
		,"MontoMercadoAnterior"
		,"ValorMercadoActual"
		,"MontoMercadoActual"
		,"ValorMercadoProximo"
		,"MontoMercadoProximo"
		,"MontoCompraAnterior"
		,"MontoCompraActual"
		,"MontoCompraProximo"
		,"ValorCompraAnterior"
		,"ValorCompraActual"
		,"ValorCompraProximo"
		,COALESCE( ABS("MontoCompraActual" - "MontoCompraAnterior") ,0) AS "InteresMontoCompraAnterior"
		,COALESCE( ABS("MontoCompraProximo" - "MontoCompraActual") ,0) AS "InteresMontoCompraProximo"
		,OPE."IdEmpresa"
		,OPE."IdEstado"
 FROM Operaciones AS OPE
 INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS EMP ON EMP."IdEmpresa" = OPE."IdEmpresa"
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC ON CIC."IdCliente" = OPE."IdCliente" AND CIC."CodigoTipoIndicador" = 'RELCORR'
  LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = OPE."IdEmisor"
 ORDER BY "FolioOperacion" DESC
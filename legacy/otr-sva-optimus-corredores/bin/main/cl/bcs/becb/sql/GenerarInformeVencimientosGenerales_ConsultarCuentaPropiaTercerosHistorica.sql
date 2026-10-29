SELECT 
	   CLI."NombreCompleto"                          												AS "NombreCliente" 
      ,CLI."DocumentoIdentidad"                      												AS "RutCliente"
      ,CLI."CodigoPortafolio"                        												AS "CodigoPortafolio"
	  ,CAR."NemoSVS"																				AS "Nemotecnico"
	  ,TO_NVARCHAR(CAR."FechaOperacion",'DD-MM-YYYY')												AS "FechaCompra"
	  ,CASE 
	  	WHEN CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND INS."NumeroCupones" = 0 
	  		THEN TO_NVARCHAR(CAR."FechaVencimientoInstrumento",'DD-MM-YYYY') 
	  		ELSE TO_NVARCHAR(CAR."INGFechaPagoCorteCupon",'DD-MM-YYYY') 
	  	END 																						AS "FechaVencimiento"
	  ,CAR."IdEmisor"																				AS "Emisor"
	  ,CAR."IdTipoReajuste"																			AS "Reajustabilidad"
	  ,CAR."PrecioCompra"																			AS "Precio"
	  ,CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 
				THEN CAR."INGMontoCorteCuponMonedaInstrumento"
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."IdTipoReajuste" = 'UF'
				THEN COALESCE(CAR."INGValorRescate" * PF."Valor", CAR."INGValorRescate")
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
				THEN CAR."INGValorRescate"
			ELSE CAR."INGMontoCompraActualMonedaInstrumento"						
		END																							AS "ValorRescate"
	  ,CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 
				THEN CAR."INGMontoCorteCupon"
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' AND CAR."IdTipoReajuste" = CAR."IdMonedaInstrumento"
				THEN CAR."INGValorRescate"
			ELSE CAR."INGMontoATasaCompraActual"
		END 																						AS "MontoPagar"
	  ,CAR."IdBoveda"																				AS "TipoCustodia"
	  ,CAR."IdMonedaInstrumento"																	AS "IdMonedaPago"
	  ,'S'																							AS "EsCuentaPropia"
	  ,'N'																							AS "EsAdministradorCartera"
      ,CONCAT('S_N', CONCAT('_',CAR."IdTipoReajuste"))												AS "AgruparSeccion"	-- Campos para agrupar por secion (EsCuentaPropia + EsAdministradorCartera + Reajuste)
	FROM 	   "BSCL.Plataforma.BD::PLA.Empresa" 							AS EMP
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 					AS SOC 
			ON  SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
	INNER JOIN 	BSCL."BSCL.Custodia.BD::HIS.Cartera" 						AS CAR
			ON  EMP."IdEmpresa" = CAR."IdEmpresa"	AND
				SOC."IdCliente" = CAR."IdCliente"
 	LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" 	AS CLI 
 			ON CLI."IdCliente"    = CAR."IdCliente" 	AND 
 			   CLI."IdPortafolio" = CAR."IdPortafolio" 			
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" 						AS INS
			ON  INS."IdInstrumento" = CAR."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero"  				AS PF 
			ON  PF."IdParametroFinanciero" = 'UF' AND PF."Fecha" = :p_FechaVencimiento
	WHERE		
		CAR."IdEstado" IN ('VIG','VCD')					AND 
		CAR."FechaRespaldo" = :p_FechaVencimiento		AND 
		EMP."IdEmpresa" = :p_IdEmpresa
	AND CAR."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	AND CAR."IdTipoInstrumento" NOT IN ('SIM', 'PRT', 'EMP', 'PAC','RRV','RRF','OPC')
	AND (	CAR."IdClasificacionTipoInstrumento" IN ('RF','IF') AND INS."NumeroCupones" = 0 AND CAR."FechaVencimientoInstrumento" = :p_FechaVencimiento	   
	   		OR
	   		CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 AND CAR."INGFechaPagoCorteCupon" = :p_FechaVencimiento
		)

	ORDER BY "AgruparSeccion";
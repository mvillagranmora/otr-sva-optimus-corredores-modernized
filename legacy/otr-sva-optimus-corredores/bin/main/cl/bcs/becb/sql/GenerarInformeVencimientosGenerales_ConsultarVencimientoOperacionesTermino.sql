SELECT    TO_NVARCHAR(CAR."FechaOperacion" ,'DD-MM-YYYY') AS "FechaCompra", 
	      TO_NVARCHAR(CAR."FechaVencimientoOperacion" ,'DD-MM-YYYY') AS "FechaVencimiento",
	      INS."Nemotecnico" AS "Nemotecnico",
	      CAR."IdEmisor" AS "IdEmisor",
	      CAR."IdTipoReajuste" AS "IdReajuste",
	      CAR."INGTasaCompra" AS "Precio",
	      CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 
				THEN CAR."INGMontoCorteCupon"
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
				THEN CAR."INGValorRescate"
			ELSE CAR."INGMontoATasaCompraActual"
		  END  AS "Monto",
	      CAR."IdMonedaEmision" AS "IdMonedaPago",
	      CAR."IdBoveda" AS "IdBoveda",
	      COALESCE(CIndicadores."ValorIndicador",'N') AS "EsAdministracionCartera",
	      COALESCE(CIndicadores."ValorIndicador",'N') AS "AgruparSeccion",
	      CLI."DocumentoIdentidad" AS "DocumentoIdentidad",
	      CLI."NombreCompleto" AS "NombreCompleto",
	      CLI."CodigoPortafolio" AS "CodigoPortafolio",
	      CASE
			WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND INS."NumeroCupones" > 0 
				THEN CAR."INGMontoCorteCuponMonedaInstrumento"
			WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
				THEN CAR."INGValorRescate"
			ELSE CAR."INGMontoCompraActualMonedaInstrumento"						
		  END AS "ValorRescate"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON (CAR."IdInstrumento" = INS."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS CIndicadores ON (CAR."IdCliente" = CIndicadores."IdCliente" AND CAR."IdPortafolio" = CIndicadores."IdPortafolio" AND CIndicadores."CodigoTipoIndicador" = 'ADMCART')
LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS CLI ON CLI."IdCliente" = CAR."IdCliente" AND CLI."IdPortafolio" = CAR."IdPortafolio" 	
LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON EMP."IdSocioDeNegocio" = CLI."IdSocioDeNegocio" AND EMP."IdEmpresa" = CAR."IdEmpresa"
WHERE CAR."IdTipoOperacion" IN ('COMTER')
AND CAR."FechaVencimientoOperacion" = :p_FechaVencimiento
AND EMP."IdSocioDeNegocio" IS NULL
AND CAR."IdEstado" IN ('VIG', 'VCD')
AND CAR."IdEmpresa" = :p_IdEmpresa;
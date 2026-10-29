	SELECT
	   CAR."IdClasificacionTipoInstrumento"
	   ,CAR."IdTipoInstrumento"
	   ,CAR."IdInstrumento"
	   ,CAR."Nemotecnico" as "Nemotecnico"
	   ,CAR."IdEmisor"
	   ,CAR."IdCliente"
	   ,CAR."IdPortafolio"
	   ,CAR."IdBoveda" 
	   ,CAST(IFNULL(CAR."INGCantidad",0) as DECIMAL(26,8)) as "Cantidad"
	   ,CAR."INGMontoCompraActualMonedaInstrumento" as "ValorCompra"
	   ,CAR."INGMontoATasaCompraActual" as "MontoCompra"					
	   ,CAR."IdRegimenFondoMutuo"
	   ,CLI."IdPersona"
	   ,CTINS."Nombre" AS "NombreClasificacionTipoInstrumento"
	   ,TINS."Nombre" AS "NombreTipoInstrumento"
	   ,"IDENT"."DocumentoIdentidad"
	   ,PER."RazonSocial"
	   ,Portafolio."CodigoPortafolio"
	   ,SOC."IdSocioDeNegocio"
	   ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"
	   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
	   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
	   ,COALESCE (REG."Descripcion",'') 								AS "RegimenTributario"
		FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INS on CAR."IdInstrumento" = INS."IdInstrumento"
		
		LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CTINS ON CTINS."IdClasificacionTipoInstrumento" = TINS."IdClasificacionTipoInstrumento"
				
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")--desarmar
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS "PER" ON "PER"."IdPersona" = CLI."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC ON CLI."IdPersona" = SOC."IdPersona"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = CAR."IdInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND Portafolio."IdMatrizAgrupadorAPV" = 0
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" 	AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
		WHERE 
			CAR."IdEmpresa" = :IdEmpresa
		AND Serie."IdEmpresa" = :IdEmpresa
		AND	CAR."IdCliente" = :IdCliente
		AND CAR."IdClasificacionTipoInstrumento" = 'FM'
		AND
		(
			((
				:FechaConsulta BETWEEN CAR."INGFechaLiquidacion" and CASE WHEN IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') END
				AND
				:FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end	   
			))
			
		)
		AND	CAR."IdEstado" = 'VIG'

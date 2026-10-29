UPDATE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" AS CMF
SET 
    "Nemotecnico" = 
        CASE
            WHEN CMF."IdClasificacionTipoInstrumento" = 'FM' THEN COALESCE((SELECT TOP 1 "NemoCMF" FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER WHERE SER."IdSerieFondo" = :Nemotecnico), CMF."Nemotecnico")
            ELSE CMF."Nemotecnico"
        END
    ,
   "TasaMercadoCustodia" =
    CASE
        WHEN (SELECT "Valor" FROM "BSCL.Normativa.BD::NOR.Configuracion" WHERE "CodigoParametro"='NCG499_TIR_PRECIOTASA' ) = 'S' THEN
            COALESCE(
                (   SELECT MAX(TAS."Tasa")
                    FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
                    WHERE TAS."IdTipoTasa" = 'TASA'
                    AND TAS."IdEstado" = 'PUB'
                    AND CMF."IdClasificacionTipoInstrumento" = 'RF'
                    AND CMF."TasaMercadoCustodia" = 0
                    AND TAS."Nemotecnico" = CMF."Nemotecnico"
                    AND TAS."IdTipoInstrumento" = CMF."IdTipoInstrumento"
                    AND TAS."FechaIngreso" = (
                            SELECT MAX(TAS2."FechaIngreso")
                            FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS2
                            WHERE TAS2."IdTipoTasa" = 'TASA'
                            AND TAS2."IdEstado" = 'PUB'
                            AND TAS2."Nemotecnico" = CMF."Nemotecnico"
                            AND TAS2."IdTipoInstrumento" = CMF."IdTipoInstrumento"
                        )
                ),CMF."TasaMercadoCustodia"
            )

        ELSE
            COALESCE(
                (
                    SELECT INS."Tasa"
                    FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
                    WHERE INS."Nemotecnico" = CMF."Nemotecnico"
                      AND INS."IdTipoInstrumento" = CMF."IdTipoInstrumento"
                      AND CMF."IdClasificacionTipoInstrumento" = 'RF'
                      AND CMF."TasaMercadoCustodia" = 0
                ),CMF."TasaMercadoCustodia",0
            )
    END,
    "IdMonedaPrincipal"= COALESCE((SELECT INS."IdMoneda" FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
        WHERE  INS."IdInstrumento" = CMF."IdInstrumento" AND CMF."IdClasificacionTipoInstrumento" = 'RF'),CMF."IdMonedaInstrReajuste"),
    "FuentePrecios" = (
        SELECT IFNULL(MAX("INGFuenteTasaPrecioMercadoActual"),'')
        FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
        WHERE CAR."IdCliente" = :IdCliente
        AND CAR."IdPortafolio" = :IdPortafolio
        AND CAR."IdTipoInstrumento" = :IdTipoInstrumento
        AND CAR."NemoSVS" = :Nemotecnico
        AND CAR."IdEstado"='VIG'
        --AND CAR."IdMonedaEmision" = :IdMonedaPrincipal
        --AND CAR."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
        --ORDER BY CAR."IdCartera" DESC
    ),
    "Duration" = (
	    SELECT MAX("Duration")
		FROM (
		    SELECT IFNULL(MIN(GAR."INGDurationMercadoActual"),0) AS "Duration"
		    FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
		    WHERE GAR."IdCliente" = :IdCliente
		      AND GAR."IdPortafolio" = :IdPortafolio
		      AND GAR."IdTipoInstrumento" = :IdTipoInstrumento
		      AND GAR."NemoSVS" = :Nemotecnico
		      AND GAR."IdEstado"='VIG'
		
		    UNION ALL
		
		    SELECT IFNULL(MIN(CAR."INGDurationMercadoActual"),0) AS "Duration"
		    FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		    WHERE CAR."IdCliente" = :IdCliente
		      AND CAR."IdPortafolio" = :IdPortafolio
		      AND CAR."IdTipoInstrumento" = :IdTipoInstrumento
		      AND CAR."NemoSVS" = :Nemotecnico
		      AND CAR."IdEstado"='VIG'
		
		    UNION ALL
		
		    SELECT IFNULL(MIN(SUB."INGDurationMercadoActual"),0) AS "Duration"
		    FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		    INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" SUB 
		        ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
		    WHERE CAR."IdEstado" = 'VIG'
		      AND CAR."IdClienteRelacionado" = :IdCliente
		      AND CAR."IdPortafolioRelacionado" = :IdPortafolio
		      AND CAR."IdTipoInstrumento" = 'PAC'
		      AND SUB."IdTipoInstrumento" = :IdTipoInstrumento
		      AND SUB."NemoSVS" = :Nemotecnico
		      AND SUB."IdEstado"='VIG'
			)
    ),
    "FechaVencimiento" =  COALESCE(COALESCE(
	    (SELECT MAX(CAR."FechaVencimientoInstrumento") FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		WHERE CAR."NemoSVS" = :Nemotecnico AND CAR."IdTipoInstrumento" = :IdTipoInstrumento
		AND CAR."IdPortafolio" = :IdPortafolio AND CAR."IdCliente" = :IdCliente AND "IdEstado" NOT IN ('VCD','ANU'))
	    ,(SELECT INS."FechaVencimiento" FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		WHERE INS."Nemotecnico"= :Nemotecnico AND INS."IdTipoInstrumento" = :IdTipoInstrumento)), '0001-01-01'),
    "ClasificacionRiesgoInstrumento" =
        CASE
            WHEN CMF."IdClasificacionTipoInstrumento" IN ('IF', 'RF') THEN COALESCE((SELECT TOP 1 SUBSTRING(TRIM("ValorClasificacionRiesgoIIF"), 0, 2) 
                                                                        FROM "BSCL.Plataforma.BD::MER.Emisor" AS EMI WHERE EMI."IdEmisor" = :IdEmisor), '')
            ELSE ''
        END 
    ,
    "FuentePreciosGarantia" = (COALESCE((
        SELECT IFNULL(MAX("INGFuenteTasaPrecioMercadoActual"),'')
        FROM "BSCL.Custodia.BD::GTA.Garantia"
		WHERE "IdCliente" = :IdCliente
        AND "IdPortafolio" = :IdPortafolio
        AND "IdTipoInstrumento" = :IdTipoInstrumento
        AND "NemoSVS" = :Nemotecnico
        AND "IdEstado"='VIG'
        --ORDER BY "IdCarteraOrigen" DESC  
    ),''))
WHERE CMF."IdCliente" = :IdCliente
AND CMF."IdPortafolio" = :IdPortafolio
AND CMF."IdTipoInstrumento" = :IdTipoInstrumento
AND CMF."Nemotecnico" = :Nemotecnico
AND CMF."IdMonedaPrincipal" = :IdMonedaPrincipal
AND CMF."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
AND CMF."Fecha" = :FechaCuadratura;
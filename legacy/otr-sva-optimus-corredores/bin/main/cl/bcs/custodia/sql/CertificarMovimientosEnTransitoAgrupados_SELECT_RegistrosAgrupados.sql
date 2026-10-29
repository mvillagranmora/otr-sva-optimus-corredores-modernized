SELECT 
MOVD."IdMovimientoCarteraEnTransito" 
,MOVD."IdTipoOperacion"
,MOVD."IdConcepto" 
,MOVD."IdClasificacionTipoInstrumento" 
,MOVD."Glosa"
FROM #MOVENTRANSITOAGR MOVAGR
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" MOVD ON (
MOVAGR."IdCliente" = MOVD."IdCliente"
AND MOVAGR."DocumentoIdentidad" = MOVD."DocumentoIdentidad"
AND MOVAGR."IdTipoOperacion" = MOVD."IdTipoOperacion"
AND MOVAGR."IdClasificacionTipoInstrumento" = MOVD."IdClasificacionTipoInstrumento"
AND MOVAGR."FechaOperacion" = MOVD."FechaOperacion"
AND MOVAGR."FechaLiquidacion" = MOVD."FechaLiquidacion"
AND MOVAGR."IdCondicionLiquidacion" = MOVD."IdCondicionLiquidacion"
AND MOVAGR."IdConcepto" = MOVD."IdConcepto"
AND MOVAGR."IdInstrumento" = MOVD."IdInstrumento"
AND MOVAGR."Nemotecnico" = CASE 
            					WHEN MOVD."IdClasificacionTipoInstrumento" = 'IF' 
	            				THEN MOVD."NemoSVS" 
	            				ELSE MOVD."Nemotecnico" 
        					END
AND MOVAGR."IdMonedaInstrumento" = MOVD."IdMonedaInstrumento"
AND MOVAGR."IdBoveda" = MOVD."IdBoveda"
AND MOVAGR."EsNominal" = MOVD."EsNominal"
AND MOVD."IdEstado" IN ('PDA', 'PEN', 'MAU'))
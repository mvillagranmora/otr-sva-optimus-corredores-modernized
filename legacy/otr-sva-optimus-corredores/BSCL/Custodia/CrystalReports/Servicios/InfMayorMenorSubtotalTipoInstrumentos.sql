SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::InfMayorMenorSubtotalTipoInstrumentos";
GO
CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::InfMayorMenorSubtotalTipoInstrumentos" ( 
    --VARIABLES OBLIGATORIAS
   	IN p_IdInforme              			NVARCHAR(80)
   	,IN p_IdEmpresa							BIGINT	
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
    DECLARE v_count           INT;
    BEGIN
    
	v_TipoInstrumento = SELECT "IdTipoInstrumento"
					  FROM "BSCL.Plataforma.BD::INS.TipoInstrumento"
					  WHERE "IdClasificacionTipoInstrumento" IN ('IF', 'RF', 'RV');
	
	v_DiferenciadorInforme = SELECT DISTINCT "Diferenciador"
						  FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF"
						  WHERE "IdInforme" = :p_IdInforme;
				
	v_TipoInstrumentoDiferenciador = SELECT 
									  TI."IdTipoInstrumento" AS "IdTipoInstrumento",
									  CAR."Diferenciador" AS "Diferenciador"
									FROM :v_TipoInstrumento AS TI
									CROSS JOIN :v_DiferenciadorInforme AS CAR
									ORDER BY TI."IdTipoInstrumento", CAR."Diferenciador";
										
                   
    v_InformeValorizado = SELECT                                  
                            CAR."IdTipoInstrumento",
                            CAR."IdClasificacionTipoInstrumento",
                            COALESCE(TT."CodigoTipoEmisor",'-') AS "CodigoTipoEmisor",
                            CAR."Cantidad",
                            CAR."TasaCompra",
                            CAR."TasaActual",
                            CAR."PrecioCompra",
                            CAR."PrecioActual",
                            CAR."MontoCompra",
                            CAR."MontoCompraMonedaInstrumento",
                            CAR."MontoActual",
                            CAR."MontoActualMonedaInstrumento",
                            CAR."MontoProximo",
                            CAR."MontoProximoMonedaInstrumento",
							CASE
					        	WHEN COALESCE(CAR."MontoProximo",0) - COALESCE(CAR."MontoActual",0) < 0 
					        		THEN ABS(COALESCE("MontoProximo",0) - COALESCE("MontoActual",0))
					        	ELSE 0
					        END AS "MenorValorMonedaSistema",
					        CASE
					          WHEN COALESCE(CAR."MontoProximo",0) - COALESCE(CAR."MontoActual",0) > 0 
					        		THEN ABS(COALESCE("MontoProximo",0) - COALESCE("MontoActual",0))
					        	ELSE 0
					        END AS "MayorValorMonedaSistema",
                            CAR."Diferenciador"
                        FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" AS CAR
						LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" AS TT 
							ON CAR."IdClasificacionTipoInstrumento" = TT."IdClasificacionTipoInstrumento"
							AND CAR."IdTipoInstrumento" = TT."IdTipoInstrumento"
							AND ( 
							  TT."Nemotecnico" != '' AND (TRIM(TT."Nemotecnico") = CAR."Nemotecnico")
													 AND ((TT."IdEmisor" != '' AND TT."IdEmisor" = CAR."Emisor") OR (TT."IdEmisor" = ''))					
							  OR
							  TT."Nemotecnico" = '' AND ((TT."IdEmisor" = CAR."Emisor") OR (TT."IdEmisor" = ''))
							)
                        WHERE "IdInforme" = :p_IdInforme;
    
    SELECT 
		TID."IdTipoInstrumento",  
		TID."Diferenciador",
		SUM (COALESCE(IV."Cantidad", 0)) AS "TotalCantidad",
		SUM (COALESCE(IV."MontoCompra", 0)) AS "TotalMontoCompraMonedaSistema",
		SUM (COALESCE(IV."MontoActual", 0)) AS "TotalMontoActualMonedaSistema",
		SUM (COALESCE(IV."MontoProximo", 0)) AS "TotalMontoProximoMonedaSistema",
		SUM (COALESCE(IV."MenorValorMonedaSistema", 0)) AS "TotalMenorValorMonedaSistema",
		SUM (COALESCE(IV."MayorValorMonedaSistema", 0)) AS "TotalMayorValorMonedaSistema"
   	FROM :v_TipoInstrumentoDiferenciador AS TID
    LEFT JOIN :v_InformeValorizado IV ON IV."IdTipoInstrumento" = TID."IdTipoInstrumento"
    AND IV."Diferenciador" = TID."Diferenciador"
    GROUP BY TID."IdTipoInstrumento", TID."Diferenciador"
	HAVING SUM(COALESCE(IV."Cantidad", 0)) > 0;
   
	END;
END
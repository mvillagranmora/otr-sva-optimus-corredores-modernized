-- ***************************************************************************************************************
-- Carteras Libre RF - Interfaz GenerarInterfazC40INTDET
-- ***************************************************************************************************************

SELECT 
	OPER."IdTipoOperacion", 
	DET."Nemotecnico",
	CAR."FolioCartera",
	CASE WHEN DET."IdTipoReajuste" = 'USD' THEN 'US'
		 WHEN DET."IdTipoReajuste" = 'CLP' THEN 'CL'
		 ELSE DET."IdTipoReajuste"
	END AS "IdTipoReajuste",
	COALESCE(CAR."INGCantidad",0) AS "CantidadNominal",
	0 AS "CantidadTramiteCom",
	COALESCE (TO_VARCHAR(OPER."FechaVencimiento", 'YYYY-MM-DD'),'' )  AS "FechaVencimiento",
	COALESCE (TO_VARCHAR(OPER."FechaOperacion", 'YYYY-MM-DD'),'' )  AS "FechaOperacion",
	OPER."PlazoEnDias",
    '' AS "Banda",
    OPER."MontoOperacionInicial" as "Capital",
    CAR."INGMontoMonedaPrincipal", -- Validar si corresponde a valor presente (este valor se usa para calcular Interes Deve)
    DET."IdClasificacionTipoInstrumento",
   	COALESCE(CAR."INGMontoInteresMercadoProximo"+ CAR."INGMontoReajusteMercadoProximo",0) as "ReajusteDev",
    COALESCE(CAR."INGMontoReajusteCorridoCompraActual",0) as "ReajusteProy",
	COALESCE(CAR."INGMontoInteresMercadoProximo",0) AS "InteresProy",
	COALESCE(CAR."INGMontoInteresCorridoMercadoActual",0) AS "InteresDeve"
FROM    "BSCL.Mesa.BD::MES.Operacion" AS OPER
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS DET ON DET."IdOperacion" = OPER."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = DET."IdOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON DET."IdInstrumento" = INS."IdInstrumento"
WHERE OPER."IdEmpresa"						 = :IdEmpresa
  AND OPER."IdEstado" 						 = 'VIG'
  AND DET."IdClasificacionTipoInstrumento" IN ('RF','IF') 
  AND OPER."FechaOperacion" <= :Fecha
ORDER BY DET."IdClasificacionTipoInstrumento" DESC;
			
					
					
					

SELECT 	
	TOPER."Nombre" AS "IdTipoOperacion", 
	CASE WHEN DET."IdTipoReajuste" = 'CLP' THEN 'PAGARE NR'
		 ELSE 'PAGARE R'
	END AS "Nemotecnico",
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
    CAR."INGMontoMercadoActualMonedaInstrumento", -- Validar si corresponde a valor presente (este valor se usa para calcular Interes Deve)
    DET."IdClasificacionTipoInstrumento",
    TO_DECIMAL(CAR."IdOperacion",26,0) AS "IdOperacion",
    CAR."TasaCompra",
    CAR."INGMontoMercadoActualMonedaInstrumento" AS "ValorPresente",
   	COALESCE(CAR."INGMontoInteresMercadoProximo"+ CAR."INGMontoReajusteMercadoProximo",0) as "ReajusteDev",
    COALESCE(CAR."INGMontoReajusteCorridoCompraActual",0) as "ReajusteProy",
	COALESCE(CAR."INGMontoInteresMercadoProximo",0) AS "InteresProy",
	COALESCE(CAR."INGMontoInteresCorridoMercadoActual",0) AS "InteresDeve"
	FROM    "BSCL.Mesa.BD::MES.Operacion" AS OPER
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS DET ON DET."IdOperacion" = OPER."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."IdOperacion" = DET."IdOperacion"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS  ON INS."IdTipoInstrumento" = CAR."IdTipoInstrumento" 
																	  AND CAR."IdTipoInstrumento" = 'PAC'
																	  AND INS."Nemotecnico" = CAR."Nemotecnico"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPER ON TOPER."IdTipoOperacion" = CAR."IdTipoOperacion"
WHERE OPER."IdEmpresa"= :IdEmpresa
  	  AND CAR."IdEstado" = 'VIG'
  	  AND OPER."FechaOperacion" <= :Fecha   ;

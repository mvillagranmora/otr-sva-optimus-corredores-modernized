SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarFormatos";

CREATE PROCEDURE
-- 
--  Proposito: Busca formatos para instrumentos y montos, cantidades, etc.
--  Autor:  - BCS
--  Fecha Creacion: 04/11/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarFormatos" (
		OUT p_out_MonedaFormato TABLE(
			 "IdUsoMonedaFormato" 			NVARCHAR(3),
			 "IdMoneda" 					NVARCHAR(3),
			 "IdTipoInstrumento" 			NVARCHAR(3),
			 "IdTipoOperacion" 				NVARCHAR(6),
			 "PrecioDecimales" 				INTEGER,
			 "PrecioTrucaRedondea" 			NVARCHAR(1),
			 "TasaDecimales" 				INTEGER,
			 "TasaTrucaRedondea" 			NVARCHAR(1),
			 "CantidadDecimales" 			INTEGER,
			 "CantidadTrucaRedondea" 		NVARCHAR(1),
			 "MontoDecimales" 				INTEGER,
			 "MontoTrucaRedondea" 			NVARCHAR(1),	 
			 "TipoCambioDecimales" 			INTEGER,
			 "TipoCambioTrucaRedondea" 		NVARCHAR(1),
			 "ParidadDecimales" 			INTEGER,
			 "ParidadTrucaRedondea" 		NVARCHAR(1),
			 "PuntosDecimales" 				INTEGER,
			 "PuntosTrucaRedondea" 			NVARCHAR(1)
		)		
	) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA AS

BEGIN

	-- Consulta formatos para cantidades, montos, precios, etc.
	p_out_MonedaFormato = SELECT
							 "MonedaFormato"."IdUsoMonedaFormato",	 
							 "MonedaFormato"."IdMoneda",
							 "MonedaFormato"."IdTipoInstrumento",
							 "MonedaFormato"."IdTipoOperacion",
							 "MonedaFormato"."PrecioDecimales", "MonedaFormato"."PrecioTrucaRedondea",
							 "MonedaFormato"."TasaDecimales", "MonedaFormato"."TasaTrucaRedondea",
							 "MonedaFormato"."CantidadDecimales", "MonedaFormato"."CantidadTrucaRedondea",	 
							 "MonedaFormato"."MontoDecimales", "MonedaFormato"."MontoTrucaRedondea",
							 "MonedaFormato"."TipoCambioDecimales", "MonedaFormato"."TipoCambioTrucaRedondea",
							 "MonedaFormato"."ParidadDecimales", "MonedaFormato"."ParidadTrucaRedondea",
							 "MonedaFormato"."PuntosDecimales", "MonedaFormato"."PuntosTrucaRedondea"
							 
						  FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" AS "MonedaFormato"
						  INNER JOIN "BSCL.Plataforma.BD::PLA.UsoMonedaFormato" AS "UsoMonedaFormato" ON ("MonedaFormato"."IdUsoMonedaFormato" = "UsoMonedaFormato"."IdUsoMonedaFormato")
						  WHERE "MonedaFormato"."IdEstado" = 'ACT';

  	
END;

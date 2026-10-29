SET SCHEMA "BSCL";
DROP VIEW   "BSCL.Plataforma.BD.Vistas::VAL.ConsultarTipoInstrumentosValorizadosRF";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarTipoInstrumentosValorizadosRF" AS 
SELECT 
 	 CAST(0 AS BIGINT) 				AS "IdValorizacion"					
	,CAST('' AS NVARCHAR(100)) 		AS "CodigoMensaje"						
	,CAST('' AS NVARCHAR(1)) 		AS "ValorizaACompra"					
	,CAST('' AS NVARCHAR(1)) 		AS "ValorizaAMercado"					
	,CAST('0001-01-01' AS DATE) 	AS "FechaValorizacion"					
	,CAST('' AS NVARCHAR(3)) 		AS "IdClasificacionTipoInstrumento"	
	,CAST('' AS NVARCHAR(3)) 		AS "IdTipoInstrumento"					
	,CAST(0 AS BIGINT) 				AS "IdInstrumento"						
	,CAST('' AS NVARCHAR(40)) 		AS "Nemotecnico"						
	,CAST('' AS NVARCHAR(3)) 		AS "IdTipoReajuste"					
	,CAST(0 AS DECIMAL(26,8)) 		AS "Cantidad"							
	,CAST(0 AS DECIMAL(26,8)) 		AS "Precio"													
--PARA OBTENCION DE PRECIOS O TASAS
	,CAST('' AS NVARCHAR(40)) 		AS "UsoPrecio"							
	,CAST('' AS NVARCHAR(40)) 		AS "TipoPrecio"						
	,CAST('' AS NVARCHAR(3)) 		AS "Fuente"							
	,CAST('' AS NVARCHAR(1)) 		AS "EsCuentaPropia"					
--PARA OBTENCION DE INTERESES
	,CAST(0 AS DECIMAL(26,8)) 		AS "Tasa"								
	,CAST(0 AS BIGINT) 				AS "Plazo"								
	,CAST('0001-01-01' AS DATE) 	AS "FechaVencimientoOperacion"			
	,CAST(0 AS BIGINT) 				AS "Base"								
	,CAST(0 AS BIGINT) 				AS "PlazoResidual"						
--MONTOS VALORIZADOS
	,CAST(0 AS DECIMAL(26,8)) 		AS "Monto"								
	,CAST(0 AS DECIMAL(26,8)) 		AS "MontoMercado"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "MontoMonedaLocal"					
	,CAST(0 AS DECIMAL(26,8)) 		AS "MontoMonedaLocalMercado"			
--DE CALCULO DE INTERESES
	,CAST(0 AS BIGINT) 				AS "DiasTranscurridos"					
	,CAST(0 AS DECIMAL(26,8)) 		AS "TasaPorPlazo"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "TasaPorDiasTranscurridos"			
	,CAST(0 AS DECIMAL(26,8)) 		AS "InteresDiario"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "InteresAcumulado"					
	,CAST(0 AS DECIMAL(26,8)) 		AS "ValorPresente"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "MontoAPlazo"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "MontoAPlazoMonedaLocal" 			
--DE OBTENCION DE PRECIOS O TASAS
	,CAST('0001-01-01' AS DATE) 	AS "FechaPrecio"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "PrecioMercado"						
	,CAST('' AS NVARCHAR(3)) 		AS "IdMonedaMercado"					
	,CAST('' AS NVARCHAR(60)) 		AS "TablaFuente"						
	,CAST(0 AS BIGINT) 				AS "TamanoMercado"						
--DEL VALORIZADOR
	,CAST(0 AS DECIMAL(26,8)) 		AS "Duration"							
	,CAST(0 AS DECIMAL(26,8)) 		AS "Convexidad"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "PlazoValorizador"					
--CORTE DE CUPON
	,CAST(0 AS BIGINT) 				AS "NumeroCupon"						
	,CAST(0 AS DECIMAL(26,8)) 		AS "MontoCupon"						
--ERRORES O ADVERTENCIAS
	,CAST('' AS NVARCHAR(5000))		AS "DescripcionMensaje"				
	,CAST('' AS NVARCHAR(100)) 		AS "CodigoDato1"						
	,CAST('' AS NVARCHAR(520)) 		AS "ValorDato1"						
	,CAST('' AS NVARCHAR(100)) 		AS "CodigoDato2"						
	,CAST('' AS NVARCHAR(520)) 		AS "ValorDato2"															
FROM dummy;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::VAL.ConsultarTipoInstrumentosValorizadosRF"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;

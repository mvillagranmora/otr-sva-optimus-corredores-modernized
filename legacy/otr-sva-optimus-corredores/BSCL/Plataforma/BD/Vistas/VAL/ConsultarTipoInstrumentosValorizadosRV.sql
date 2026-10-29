SET SCHEMA "BSCL";
DROP VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarTipoInstrumentosValorizadosRV";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarTipoInstrumentosValorizadosRV" AS 
SELECT 
	 CAST (0 AS BIGINT) 			AS "IdValorizacion"				
	,CAST ('' AS NVARCHAR(100)) 	AS "CodigoMensaje"					
	,CAST ('' AS NVARCHAR(1)) 		AS "ValorizaACompra"				
	,CAST ('' AS NVARCHAR(1)) 		AS "ValorizaAMercado"		
	,CAST ('0001-01-01' AS DATE) 	AS "FechaValorizacion"
	,CAST ('' AS NVARCHAR(3)) 		AS "IdClasificacionTipoInstrumento"
	,CAST ('' AS NVARCHAR(3)) 		AS "IdTipoInstrumento"			
	,CAST (0 AS BIGINT) 			AS "IdInstrumento"			
	,CAST ('' AS NVARCHAR(40)) 	AS "Nemotecnico"					
	,CAST ('' AS NVARCHAR(3)) 		AS "IdMoneda"						
	,CAST (0 AS DECIMAL(26,8)) 		AS "Cantidad"							
	,CAST (0 AS DECIMAL(26,8)) 		AS "Precio"							
	,CAST (0 AS BIGINT) 			AS "Tamano"							
--PARA OBTENCION DE PRECIOS O TASAS
	,CAST ('' AS NVARCHAR(40)) 	AS "UsoPrecio"							
	,CAST ('' AS NVARCHAR(40)) 	AS "TipoPrecio"						
	,CAST ('' AS NVARCHAR(40)) 	AS "Fuente"							
	,CAST ('' AS NVARCHAR(1)) 		AS "EsCuentaPropia"					
--MONTOS VALORIZADOS
	,CAST (0 AS DECIMAL(26,8)) 		AS "Monto"								
	,CAST (0 AS DECIMAL(26,8)) 		AS "MontoMercado"						
	,CAST (0 AS DECIMAL(26,8)) 		AS "MontoMonedaLocal"					
	,CAST (0 AS DECIMAL(26,8)) 		AS "MontoMonedaLocalMercado"			
--DE OBTENCION DE PRECIOS O TASAS
	,CAST ('0001-01-01' AS DATE) 	AS "FechaPrecio"						
	,CAST (0 AS DECIMAL(26,8)) 		AS "PrecioMercado"						
	,CAST ('' AS NVARCHAR(3)) 		AS "IdMonedaMercado"					
	,CAST ('' AS NVARCHAR(60)) 	AS "TablaFuente"						
	,CAST (0 AS BIGINT) 			AS "TamanoMercado"						
--ERRORES O ADVERTENCIAS
	,CAST ('' AS NVARCHAR(5000)) 	AS "DescripcionMensaje"				
	,CAST ('' AS NVARCHAR(100)) 	AS "CodigoDato1"						
	,CAST ('' AS NVARCHAR(520)) 	AS "ValorDato1"						
	,CAST ('' AS NVARCHAR(100)) 	AS "CodigoDato2"						
	,CAST ('' AS NVARCHAR(520)) 	AS "ValorDato2"					
FROM dummy;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::VAL.ConsultarTipoInstrumentosValorizadosRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

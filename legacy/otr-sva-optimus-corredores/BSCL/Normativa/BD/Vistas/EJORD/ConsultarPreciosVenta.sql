SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta" AS 
SELECT * FROM  (  
 ---PRECIOS MAXIMOS
SELECT DISTINCT * FROM (
			SELECT RANK() over (partition by   TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD'),MKD."instrument"  order by COALESCE (MKD."priceSellingPrice",0) DESC,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') DESC) as RANK
					,TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD') 				AS "Fecha"
					,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') AS "FechaHora"
					, MKD."instrument" 													AS "Nemo"
					, COALESCE (MKD."priceSellingPrice",0) 								AS "Precio"
					, 'MERCADO' 														AS "Ambito"
					, 'MAXIMO' 															AS "Tipo"
					
			FROM  "BSCL.Orden.BD::MRV.HIS_PALOS_RV" MKD
			WHERE MKD."priceExchange"='XSGO'AND MKD."priceSellingPrice" != 0
)WHERE RANK=1
UNION ALL
SELECT DISTINCT * FROM (
			SELECT RANK() over (partition by   TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD'),MKD."instrument"  order by  COALESCE (MKD."priceSellingPrice",0) DESC,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') DESC) as RANK
					,TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD') 				AS "Fecha"
					,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') AS "FechaHora"
					, MKD."instrument" 													AS "Nemo"
					, COALESCE (MKD."priceSellingPrice",0) 								AS "Precio"
					, 'OTRASBOLSAS' 													AS "Ambito"
					, 'MAXIMO' 															AS "Tipo"
					
			FROM  "BSCL.Orden.BD::MRV.HIS_PALOS_RV" MKD
			WHERE MKD."priceExchange"<>'XSGO' AND MKD."priceSellingPrice" != 0
)WHERE RANK=1
UNION ALL
SELECT DISTINCT * FROM (
			SELECT RANK() over (partition by   TO_DATE(TO_VARCHAR(ORD."FechaEjecucion", 'YYYYMMDD')),ORD."Instrumento"  order by COALESCE (ORD."PrecioEjecucion",0) DESC ,TO_TIMESTAMP(ORD."FechaEjecucion", 'YYYY-MM-DD HH24:MI:SS') DESC) as RANK
					,TO_DATE(TO_VARCHAR(ORD."FechaEjecucion", 'YYYYMMDD')) 				AS "Fecha"
					,TO_TIMESTAMP(ORD."FechaEjecucion", 'YYYY-MM-DD HH24:MI:SS') 		AS "FechaHora"
					, ORD."Instrumento" 												AS "Nemo"
					, COALESCE (ORD."PrecioEjecucion",0) 								AS "Precio"
					, 'CORREDOR' 														AS "Ambito"
					, 'MAXIMO' 															AS "Tipo"
					
			FROM  "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenes" ORD --WHERE ORD."Instrumento"='FALABELLA'
			WHERE ORD."PrecioEjecucion" != 0
)WHERE RANK=1

---PRECIOS MINIMOS
UNION ALL
SELECT DISTINCT * FROM (
			SELECT RANK() over (partition by   TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD'),MKD."instrument"  order by COALESCE (MKD."priceSellingPrice",0) ASC,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') ASC) as RANK
					,TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD') 				AS "Fecha"
					,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') AS "FechaHora"
					, MKD."instrument" 													AS "Nemo"
					, COALESCE (MKD."priceSellingPrice",0) 								AS "Precio"
					, 'MERCADO' 														AS "Ambito"
					, 'MINIMO' 															AS "Tipo"
					
			FROM  "BSCL.Orden.BD::MRV.HIS_PALOS_RV" MKD
			WHERE MKD."priceExchange"='XSGO' AND MKD."priceSellingPrice" != 0
)WHERE RANK=1
UNION ALL
SELECT DISTINCT * FROM (
			SELECT RANK() over (partition by   TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD'),MKD."instrument"  order by  COALESCE (MKD."priceSellingPrice",0) ASC,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') ASC) as RANK
					,TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD') 				AS "Fecha"
					,TO_TIMESTAMP(SUBSTRING (MKD."tradeTime",1,14), 'YYYYMMDDHH24MISS') AS "FechaHora"
					, MKD."instrument" 													AS "Nemo"
					, COALESCE (MKD."priceSellingPrice",0) 								AS "Precio"
					, 'OTRASBOLSAS' 													AS "Ambito"
					, 'MINIMO' 															AS "Tipo"
					
			FROM  "BSCL.Orden.BD::MRV.HIS_PALOS_RV" MKD
			WHERE MKD."priceExchange"<>'XSGO' AND MKD."priceSellingPrice" != 0
)WHERE RANK=1
UNION ALL
SELECT DISTINCT * FROM (
			SELECT RANK() over (partition by   TO_DATE(TO_VARCHAR(ORD."FechaEjecucion", 'YYYYMMDD')),ORD."Instrumento"  order by COALESCE (ORD."PrecioEjecucion",0) ASC ,TO_TIMESTAMP(ORD."FechaEjecucion", 'YYYY-MM-DD HH24:MI:SS') ASC) as RANK
					,TO_DATE(TO_VARCHAR(ORD."FechaEjecucion", 'YYYYMMDD')) 				AS "Fecha"
					,TO_TIMESTAMP(ORD."FechaEjecucion", 'YYYY-MM-DD HH24:MI:SS') 		AS "FechaHora"
					, ORD."Instrumento" 												AS "Nemo"
					, COALESCE (ORD."PrecioEjecucion",0) 								AS "Precio"
					, 'CORREDOR' 														AS "Ambito"
					, 'MINIMO' 															AS "Tipo"
					
			FROM  "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenes" ORD --WHERE ORD."Instrumento"='FALABELLA'
			WHERE ORD."PrecioEjecucion" != 0
)WHERE RANK=1
ORDER BY "Nemo","Fecha","Ambito" 


); --WHERE "Nemo"='COPEC'

GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
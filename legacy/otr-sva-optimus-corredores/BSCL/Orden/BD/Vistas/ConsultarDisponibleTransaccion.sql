SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion"  
AS SELECT 
"TRAN"."IdTransaccion"  ,
"TRAN"."Cantidad"  ,

CASE
	WHEN "TRAN"."IdClasificacionTipoInstrumento" = 'IF' and "TRAN"."EsFungible" = 'S' AND SUM (COALESCE("ASIG"."MontoAsignado",0)) = "TRAN"."Monto" THEN 0
	WHEN "TRAN"."IdClasificacionTipoInstrumento" = 'IF' and "TRAN"."EsFungible" = 'S' AND SUM (COALESCE("ASIG"."MontoAsignado",0)) < "TRAN"."Monto" THEN 1
	ELSE "TRAN"."Cantidad" -  SUM  ( COALESCE ( "ASIG"."CantidadAsignada" , 0 )  ) END AS "CantidadDisponible" ,
	
CASE
	WHEN "TRAN"."IdClasificacionTipoInstrumento" = 'IF' and "TRAN"."EsFungible" = 'S' AND SUM (COALESCE("ASIG"."MontoAsignado",0)) > 0 THEN 1
	WHEN "TRAN"."IdClasificacionTipoInstrumento" = 'IF' and "TRAN"."EsFungible" = 'S' AND SUM (COALESCE("ASIG"."MontoAsignado",0)) = 0 THEN 0
	ELSE SUM(COALESCE("ASIG"."CantidadAsignada",0)) END AS "CantidadAsignada" ,
	
"TRAN"."Monto" -  SUM  ( COALESCE ( "ASIG"."MontoAsignado" , 0 )  ) AS "MontoDisponible" ,  
SUM  ( COALESCE ( "ASIG"."MontoAsignado" , 0 )  )  AS "MontoAsignado" ,

CASE WHEN "TRAN"."IdClasificacionTipoInstrumento" = 'IF' THEN "TRAN"."ValorRescate" -  SUM  ( COALESCE ( "ASIG"."ValorRescateAsignado" , 0 )  ) ELSE 0 END AS "ValorRescateDisponible" ,  
SUM  ( COALESCE ( "ASIG"."ValorRescateAsignado" , 0 )  )  AS "ValorRescateAsignado"

FROM "BSCL.Orden.BD::ORD.Transaccion" AS "TRAN" 
LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Asignacion" AS "ASIG" ON "ASIG"."IdTransaccion" = "TRAN"."IdTransaccion" AND "ASIG"."IdEstadoAsignacion" != 'ANU' AND "ASIG"."IdEstadoAsignacion" != 'REF'
GROUP BY "TRAN"."IdClasificacionTipoInstrumento","TRAN"."EsFungible","TRAN"."IdTransaccion" , "TRAN"."Cantidad","TRAN"."Monto","TRAN"."ValorRescate";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarDisponibleTransaccion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

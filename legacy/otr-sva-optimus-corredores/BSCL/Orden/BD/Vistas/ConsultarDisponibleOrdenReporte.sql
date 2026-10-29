SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrdenReporte"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrdenReporte"
AS SELECT 
ord."IdOrden" ,
ord."MontoOrden",
ord."Cantidad",
( ord."MontoOrden" -  SUM  ( IFNULL ( asoc."MontoAsociado" , 0 )  )  )  AS "MontoDisponible"  ,
( ord."Cantidad" - SUM (IFNULL ( asoc."CantidadAsociada" , 0 )) ) AS "CantidadDisponible" ,
SUM  ( IFNULL ( asoc."MontoAsociado" , 0 )  )  AS "MontoAsociado",
SUM  ( IFNULL ( asoc."CantidadAsociada" , 0 )  )  AS "CantidadAsociada"
FROM "BSCL.Orden.BD::ORD.Orden" AS ord 
LEFT OUTER JOIN "BSCL.Orden.BD.Vistas::ConsultarOrdenAsociada" AS asoc ON asoc."IdOrdenReportado" = ord."IdOrden"
WHERE ord."IdTipoOperacion" IN ('VENRRV','VENRRF')
GROUP BY ord."IdOrden", ord."MontoOrden", ord."Cantidad"
UNION ALL
SELECT 
ord."IdOrden" ,
ord."MontoOrden",
ord."Cantidad",
( ord."MontoOrden" -  SUM  ( IFNULL ( asoc."MontoAsociado" , 0 )  )  )  AS "MontoDisponible"  ,
1 AS "CantidadDisponible",
SUM  ( IFNULL ( asoc."MontoAsociado" , 0 )  )  AS "MontoAsociado",
SUM  ( IFNULL ( asoc."CantidadAsociada" , 0 )  )  AS "CantidadAsociada"
FROM "BSCL.Orden.BD::ORD.Orden" AS ord 
LEFT OUTER JOIN "BSCL.Orden.BD.Vistas::ConsultarOrdenAsociada" AS asoc ON asoc."IdOrdenReportante" = ord."IdOrden"
WHERE ord."IdTipoOperacion" IN ('COMRRV','COMRRF')
GROUP BY ord."IdOrden", ord."MontoOrden", ord."Cantidad";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrdenReporte" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

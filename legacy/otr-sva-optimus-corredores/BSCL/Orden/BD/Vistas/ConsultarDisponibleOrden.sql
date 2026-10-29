SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden" AS
SELECT
	ord."IdOrden" ,
	(ord."Cantidad" - IFNULL(ordRef."Cantidad", 0)) AS "Cantidad",
	CASE
		WHEN ord."IdOrdenReferencia" IS NOT NULL THEN 0
		ELSE (ord."Cantidad" - SUM(IFNULL(asig."CantidadAsignada", 0)) - IFNULL(ordRef."Cantidad", 0))
	END AS "CantidadDisponible" ,
	SUM(IFNULL(asig."CantidadAsignada", 0)) AS "CantidadAsignada",
	(ord."MontoOrden" - SUM(IFNULL(asig."MontoAsignado", 0)) - IFNULL(ordRef."MontoOrden", 0)) AS "MontoDisponible",
	SUM(IFNULL(asig."MontoAsignado", 0)) AS "MontoAsignado",
	CASE WHEN SUM(IFNULL(asig."CantidadAsignada", 0)) = 0 THEN 0
	     ELSE (SUM(IFNULL(asig."MontoAsignado", 0)) / SUM(IFNULL(asig."CantidadAsignada", 0))) END AS "PrecioPromedioAsignacion",
	(ord."ValorRescate" - SUM(IFNULL(asig."ValorRescateAsignado", 0)) - IFNULL(ordRef."ValorRescate", 0)) AS "ValorRescateDisponible",
	SUM(IFNULL(asig."ValorRescateAsignado", 0)) AS "ValorRescateAsignado"
FROM
	"BSCL.Orden.BD::ORD.Orden" AS ord
LEFT OUTER JOIN "BSCL.Orden.BD::ORD.Asignacion" AS asig ON
	asig."IdOrden" = ord."IdOrden"
	AND asig."EsAPlazo" = 'N'
	AND asig."IdEstadoAsignacion" != 'ANU'
	AND asig."IdEstadoAsignacion" != 'REF'
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ordRef ON
	ord."IdOrden" = ordRef."IdOrdenReferencia"
	AND ordRef."IdEstadoOrden" = 'ANU'
GROUP BY
	ord."IdOrden",
	ordRef."IdOrden",
	ord."IdOrdenReferencia",
	ord."Cantidad",
	ordRef."Cantidad",
	ord."MontoOrden",
	ordRef."MontoOrden",
	ord."ValorRescate",
	ordRef."ValorRescate";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarDisponibleOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDeIngresoEgreso";
GO
CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDeIngresoEgreso" AS
SELECT 
IE."IdIngresoEgreso","IdComprobante"
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" IE
  LEFT OUTER JOIN (SELECT RANK () OVER (PARTITION BY  CDO."IdIngresoEgreso" ORDER BY CD."IdComprobante" DESC) AS RANK, CDO."IdIngresoEgreso", CD."IdComprobante",CD."IdEstado"
  					FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD
  					LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle")
  					WHERE (COALESCE(CD."IdEstado",'') NOT IN ('MOD','ANU') OR COALESCE(CD."IdEstado",'') = '') -- No considerar cuando comprobante fue agrupado
  					GROUP BY CDO."IdIngresoEgreso", CD."IdComprobante",CD."IdEstado"
  				  ) as CD 
  			   ON (CD."IdIngresoEgreso" = IE."IdIngresoEgreso" AND RANK = 1)
WITH READ ONLY;
GO 
GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDeIngresoEgreso" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
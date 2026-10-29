SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCompraVenta";
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCompraVenta"
AS 
SELECT 'COMPRA' AS "AccionOrden",'COMPRA' AS "NombreAccionOrden" FROM DUMMY
UNION 
SELECT 'VENTA' AS "AccionOrden",'VENTA' AS "NombreAccionOrden" FROM DUMMY;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCompraVenta" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.APV.BD.Vistas::ConsultarOtroProductoAgrupador";
GO

CREATE VIEW "BSCL.APV.BD.Vistas::ConsultarOtroProductoAgrupador" AS
SELECT "IdProductoAgrupador" AS "Orden", "IdProductoAgrupador", "NombreProductoAgrupador", "IdEstado"
FROM "BSCL.APV.BD::APV.ProductoAgrupador"
WHERE "IdEstado" = 'VIG'
UNION ALL
SELECT 0, 9999, 'Otros (No APV)', 'VIG' 
FROM DUMMY;
GO

GRANT SELECT ON "BSCL.APV.BD.Vistas::ConsultarOtroProductoAgrupador" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
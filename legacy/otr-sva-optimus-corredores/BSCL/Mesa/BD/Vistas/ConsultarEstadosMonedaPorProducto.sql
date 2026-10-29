SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosMonedaPorProducto";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadosMonedaPorProducto"
AS 
SELECT "EstadoMesa"."IdEstadoMesa"
     , "EstadoMesa"."IdTipoProducto"
     , "EstadoMesa"."IdEstado"
     , COALESCE("Estado"."Nombre", "EstadoMesa"."IdEstado") AS "NombreEstado"
     , "EstadoMesa"."NombreEntidad"
  FROM "BSCL.Mesa.BD::MES.EstadoMesa" AS "EstadoMesa"
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "Estado" ON "Estado"."IdEstado" = "EstadoMesa"."IdEstado";
  
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarEstadosMonedaPorProducto" TO BCS_DESARROLLADOR WITH GRANT OPTION;

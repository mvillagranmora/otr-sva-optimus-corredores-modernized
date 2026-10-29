SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas" AS
SELECT
	EMES."IdEstadoMesa",
	EMES."IdTipoProducto",
	EMES."IdEstado",
	EMES."NombreEntidad",
	EST."Nombre"
FROM "BSCL.Mesa.BD::MES.EstadoMesa" EMES
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" EST ON EMES."IdEstado" = EST."IdEstado";
  
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarEstadoMesas" TO BCS_DESARROLLADOR WITH GRANT OPTION;

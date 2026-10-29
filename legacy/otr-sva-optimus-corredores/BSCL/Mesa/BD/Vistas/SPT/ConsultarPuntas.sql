SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntas";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntas"
AS 
SELECT
  "IdPuntas"
, "IdEmpresa"
, "Fecha"
, "IdUsuario"
, "AperturaPrecio"
, "PuntaCompra"
, "PuntaVenta"
, "IdMonedaPunta"
, "Estado"
FROM "BSCL.Mesa.BD::SPT.Puntas";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntas" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

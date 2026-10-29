SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial" AS

SELECT *
FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa";
  
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial" TO BCS_DESARROLLADOR WITH GRANT OPTION;
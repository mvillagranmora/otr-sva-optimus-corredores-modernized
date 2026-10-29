SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarEstadoMesa"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarEstadoMesa" 
AS

SELECT x."Codigo", x."IdEstadoMesa" FROM "BSCL.Mesa.BD::MES.Mesa" x
WHERE "Codigo" IN ('MESA_SPOT');

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAU.ConsultarEstadoMesa" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
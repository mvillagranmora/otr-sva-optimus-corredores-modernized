
SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoInstrumento";

CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarTipoInstrumento"
AS 
SELECT DISTINCT
	   InstrumentoMoneda."IdTipoProducto"
     , InstrumentoMoneda."IdTipoInstrumento"
     , TipoOperacion."Nombre" as "NombreOperacion"
  FROM "BSCL.Mesa.BD::MES.InstrumentoMoneda" as InstrumentoMoneda 
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion
	ON TipoOperacion."IdTipoOperacion" = InstrumentoMoneda."IdTipoInstrumento"
   AND TipoOperacion."IdTipoProducto" = InstrumentoMoneda."IdTipoProducto";
   
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarTipoInstrumento" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

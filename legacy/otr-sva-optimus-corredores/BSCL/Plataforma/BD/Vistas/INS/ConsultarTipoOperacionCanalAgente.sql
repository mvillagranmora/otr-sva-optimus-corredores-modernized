SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacionCanalAgente"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacionCanalAgente"
AS

SELECT 
TI."IdTipoInstrumento",
TTO."IdTipoOperacion",
TTO."AccionOrden",
TTO."IdTipoProducto",
TTO."EsVentaCorta"
FROM  "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" TI 
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TTO 
ON TI."IdTipoOperacion" = TTO."IdTipoOperacion" 
WHERE TTO."AccionOrden" IS NOT NULL AND TTO."EsOrden" = 'S'

GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacionCanalAgente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
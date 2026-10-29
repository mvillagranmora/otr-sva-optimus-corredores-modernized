SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarPreciosSistema"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarPreciosSistema"  
AS
SELECT
	VP."IdInstrumento",
	CI."Nemotecnico",
	VP."FechaVector",
	CI."IdMoneda",
	VP."ValorPrecioBase",
	VP."ValorPrecioAlgorithmics100",
	VP."ValorPrecioAlgorithmics98",
	VP."ValorPrecioAlgorithmics95",
	VP."ValorPrecioOperacionesPlazo01",
	VP."ValorPrecioVentaCorta02",
	VP."ValorOtrosPrecios03",
	VP."ValorOtrosPrecios04",
	VP."ValorOtrosPrecios05",
	CI."IdEstado"
FROM "BSCL.Plataforma.BD::INS.VectorPrecios" AS  VP 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"  AS CI ON CI."IdInstrumento"=VP."IdInstrumento" 
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarPreciosSistema"   TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO

SET SCHEMA "BSCL";
GO 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado";
GO 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado" AS
SELECT
	 FER."IdFeriado"
	,FER."Tipo"
	,COALESCE(TFE."Nombre",' -') AS "TipoDiaFeriado"
	,FER."Fecha"
	,TO_NVARCHAR(FER."Fecha",'DD-MM-YYYY') AS "FechaUI"
	,FER."Dia"
	,COALESCE(PAR."NombreParametro",' -') AS "DiaUI"
	,FER."DescripcionDia"
	,FER."AgnoCarga"
	,FER."IdPais"
	,PAI."NombrePais"
	,PAI."CodigoPais"
	,FER."IdEstado"
FROM "BSCL.Plataforma.BD::REG.DiaFeriado" FER
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDiaFeriado" TFE
	ON (FER."Tipo" = TFE."IdTipoDiaFeriado" AND TFE."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" PAR
	ON (FER."Dia" = PAR."ValorParametro" AND PAR."NombreTipoParametro" = 'DIASEMANAESPANOL')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAI
	ON (FER."IdPais" = PAI."IdPais")
ORDER BY FER."AgnoCarga" ASC, FER."Fecha" ASC, FER."Dia" ASC;
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO 
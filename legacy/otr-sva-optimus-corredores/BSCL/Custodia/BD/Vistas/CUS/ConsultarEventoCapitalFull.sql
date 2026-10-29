SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalFull";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalFull"
AS 

SELECT
	EvCptal."NumeroDividendo"					AS "NumeroDividendo",
	EvCptal."Secuencia"							AS "Secuencia",
	EvCptal."IdTipoEventoCapital"				AS "IdTipoEventoCapital",
	TipEvCap."Nombre" 							AS "TipoEvento",
	EvCptal."IdTipoInstrumento"					AS "IdTipoInstrumento",
	EvCptal."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento",
	TipInstru."Descripcion" 					AS "TipoInstrumento",
	EvCptal."IdInstrumento"						AS "IdInstrumento",
	EvCptal."Nemo"								AS "Nemo",
	EvCptal."FechaPago"							AS "FechaPago",
	EvCptal."Valor"								AS "Valor",
	EvCptal."Factor"							AS "Factor",
	EvCptal."Descripcion" 						AS "DescripcionEvento",
	EvCptal."ISFUT"								AS "ISFUT",
	ins."AfectoArt104" 							AS "RETART104",
	EvCptal."TasaEfectiva"						AS "TasaEfectiva"
FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS EvCptal
	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoEventoCapital" AS TipEvCap
		ON EvCptal."IdTipoEventoCapital" = TipEvCap."IdTipoEventoCapital"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento" AS TipInstru
		ON EvCptal."IdTipoInstrumento" = TipInstru."IdTipoInstrumento"
	   AND EvCptal."IdClasificacionTipoInstrumento" = TipInstru."IdClasificacionTipoInstrumento"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ins
		ON EvCptal."IdInstrumento" = ins."IdInstrumento";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalFull" TO BCS_DESARROLLADOR WITH GRANT OPTION; 

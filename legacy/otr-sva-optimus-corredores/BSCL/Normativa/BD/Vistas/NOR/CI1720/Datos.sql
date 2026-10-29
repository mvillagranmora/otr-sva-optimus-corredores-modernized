SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.CI1720.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.CI1720.Datos"(
	"ID",
	"IdDocumento",
	"VboUsuario",
	"Observaciones",
	"FuenteDatos",
	--"IdPortafolio",
	--"RutCliente",
	--"TipoRegistro",
	"Nemotecnico",
	"NombreEmisor",
	"TipoInstrumento",
	"CantidadUnidadesNominal",
	"TipoUnidades",
	"Valorizacion",
	"CarteraPropia",
	"CustodiaTerceros",
	"CustodiaPropiaDCV",
	"CustodiaTercerosDCV",
	"CustodiaSociedadEmisora",
	"CustodiaIntermediario",
	"CustodiaBolsas",
	"CustodiaOtros",
	"FechaRegistro",
	--"IdCliente"
	"Usuario"
) AS 
SELECT
	A."ID",
	A."IdDocumento",
	A."VboUsuario",
	A."Observaciones" ,
	A."FuenteDatos",
	A."Nemotecnico",
	A."NombreEmisor",
	A."TipoInstrumento",
	A."CantidadUnidadesNominal",
	A."TipoUnidades",
	A."Valorizacion",
	A."CarteraPropia",
	A."CustodiaTerceros",
	A."CustodiaPropiaDCV",
	A."CustodiaTercerosDCV",
	A."CustodiaSociedadEmisora",
	A."CustodiaIntermediario",
	A."CustodiaBolsas",
	A."CustodiaOtros",
	A."FechaRegistro",
	A."Usuario"
FROM "BSCL.Normativa.BD::NOR.DatosCI1720" A
INNER JOIN 
	(
		select
		"IdDocumento",
		MAX("FechaRegistro") AS "FechaRegistro"
		from  "BSCL.Normativa.BD::NOR.DatosCI1720"
		GROUP BY  "IdDocumento"
	) F --Solo muestra la fecha mayor del periodo
	ON (A."IdDocumento"=F."IdDocumento" AND	A."FechaRegistro"=F."FechaRegistro"	)
	
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.CI1720.Datos" TO BCS_DESARROLLADOR WITH GRANT OPTION;


SET SCHEMA BSCL;

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarPrecioInstrumentoAnalitica";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarPrecioInstrumentoAnalitica" 
AS SELECT
	P."IdInstrumento" ,
	INS."Nemotecnico" AS "Nemotecnico" ,
	CAST(P."Precio" AS DECIMAL(26,8)) AS "Precio",
	P."FechaSistema" as "Fecha" ,
	INS."RazonSocial" as "RazonSocial" ,
	CASE WHEN INS."IdMoneda" = '' THEN 'CLP' ELSE INS."IdMoneda" END AS "Moneda" ,
	P."CodigoFuente" as "Codigo_Fuente"
	FROM "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento" P 
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = P."IdInstrumento";
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarPrecioInstrumentoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

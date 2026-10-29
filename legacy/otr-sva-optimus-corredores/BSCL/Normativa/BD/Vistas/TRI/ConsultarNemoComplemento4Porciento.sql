SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarNemoComplemento4Porciento";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarNemoComplemento4Porciento" AS
SELECT DISTINCT cons."Nemotecnico",
				datos."IdCliente" 
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" cons
JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos" AS datos ON datos."NemotecnicoSVS" = cons."Nemotecnico"
WHERE datos."ValResultado" = 'OK'
WITH read ONLY;
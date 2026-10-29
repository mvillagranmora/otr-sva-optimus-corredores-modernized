SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"
AS 
SELECT
	 "IdConcepto",
	 "Nombre",
	 "IdEstado"
FROM "BSCL.Plataforma.BD::PLA.Concepto";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

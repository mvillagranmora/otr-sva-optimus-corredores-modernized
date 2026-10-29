SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxOpcionCobertura";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxOpcionCobertura" 
AS
	SELECT 1 AS "IdOpcionCobertura"
			,'Por Carteras' AS "ValorOpcionCobertura"
	FROM DUMMY
	UNION ALL
	SELECT 2 AS "IdOpcionCobertura"
	,'Por Pactos Calzados' AS "ValorOpcionCobertura"
	FROM DUMMY
	;
GRANT SELECT 	ON 	"BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxOpcionCobertura" TO	BCS_DESARROLLADOR WITH GRANT OPTION; 

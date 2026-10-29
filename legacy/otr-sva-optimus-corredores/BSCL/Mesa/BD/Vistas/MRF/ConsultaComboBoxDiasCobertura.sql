SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxDiasCobertura";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxDiasCobertura" 
AS
	SELECT 1 AS "IdDiasCobertura"
			,'Menor a 90' AS "ValorDiasCobertura"
	FROM DUMMY
	UNION ALL
	SELECT 2 AS "IdDiasCobertura"
	,'Mayor a 90' AS "ValorDiasCobertura"
	FROM DUMMY
	;
GRANT SELECT 	ON 	"BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxDiasCobertura" TO	BCS_DESARROLLADOR WITH GRANT OPTION; 

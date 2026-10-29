SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxCoberturaPatrimonial";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxCoberturaPatrimonial" 
AS
	SELECT 1 AS "IdValorCobertura"
			,'Art. 514' AS "ValorCobertura"
	FROM DUMMY
	UNION ALL
	SELECT 2 AS "IdValorCobertura"
	,'A Mercado' AS "ValorCobertura"
	FROM DUMMY
	;
GRANT SELECT 	ON 	"BSCL.Mesa.BD.Vistas::MRF.ConsultaComboBoxCoberturaPatrimonial" TO	BCS_DESARROLLADOR WITH GRANT OPTION; 

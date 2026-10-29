SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor";

CREATE PROCEDURE "BSCL.Plataforma.BD.Utilitarios::SplitLlaveValor"(
-- 
--  Proposito: Devolver la linea de texto separada por (comas,punto y coma, etc),
--			   en n registros llave valor.
--			   ejemplo entrada: 1,'hola,como,estas'
--					   salida:	1,hola
--								1,como
--								1,estas
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 08/08/2016

	IN p_Texto TABLE ("IdInterno" BIGINT,"Texto" NVARCHAR(1000)),
	IN p_Split NVARCHAR(1) default ',',
	OUT p_out_Items TABLE ("IdInterno" BIGINT,"Orden" INTEGER,"Item" NVARCHAR(1000))
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE v_items		NVARCHAR(1000) ARRAY;
	DECLARE v_IdInterno	BIGINT ARRAY;
	DECLARE v_text		NVARCHAR(1000);
	DECLARE v_index		INTEGER := 1;
	
	DECLARE v_Id		BIGINT ARRAY;
	DECLARE v_Texto		NVARCHAR(1000) ARRAY;
	DECLARE v_j			BIGINT := 0;
									   
	v_Id := ARRAY_AGG(:p_Texto."IdInterno" ORDER BY "IdInterno" ASC);
	v_Texto := ARRAY_AGG(:p_Texto."Texto" ORDER BY "IdInterno" ASC);	
	
	FOR v_j IN 1 .. CARDINALITY(:v_Id) DO
		v_text := :v_Texto[v_j];
		
		WHILE LOCATE(:v_text,:p_Split) > 0 DO
			v_items[:v_index] := SUBSTR_BEFORE(:v_text,:p_Split); -- Subcadena antes de la primera aparición del delimitador  	  
			v_IdInterno[:v_index] := :v_Id[v_j];
			v_text := SUBSTR_AFTER(:v_text,:p_Split);   -- Subcadena después de la primera aparición del delimitador	  
			v_index := :v_index + 1;
		END WHILE;
		
		v_items[:v_index] := :v_text;
		v_IdInterno[:v_index] := :v_Id[v_j];
		v_index := :v_index + 1;
	END FOR;
	
	v_out_Items = UNNEST(:v_IdInterno,:v_items) AS ("IdInterno","Item");  -- Convierte un arreglo en tabla

	p_out_Items =
		SELECT
			"IdInterno",
			ROW_NUMBER() over (PARTITION BY "IdInterno") AS "Orden",
			"Item"
		FROM :v_out_Items;	
END; 

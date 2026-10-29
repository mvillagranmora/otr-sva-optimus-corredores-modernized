SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::SplitString";

CREATE PROCEDURE 
-- 
--  Proposito: Transformar un string(delimitado por un caracter ,;-) en una tabla. 
--             Ejemplo  Entrada: primero,segundo
--                      Salida : [primero]
--                               [segundo]
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 30/08/2016
--   
"BSCL.Plataforma.BD.Utilitarios::SplitString" (

	IN p_String 		   	NVARCHAR(1000),
	IN p_Delimitador	  	NVARCHAR(1)     DEFAULT ',',
	OUT p_out_Elementos   	TABLE ("Item" NVARCHAR(1000))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN

  DECLARE elementos 	   NVARCHAR(1000) ARRAY;
  DECLARE item 			   NVARCHAR(1000);
  DECLARE indice 		   INTEGER;
  DECLARE v_LargoString    INTEGER;
  
  SELECT LENGTH(p_String)
  INTO v_LargoString
  FROM DUMMY;
  
  IF (v_LargoString> 0) THEN
	  item    := :p_String;
	  indice  := 1;
	 
	  WHILE LOCATE(:item,:p_Delimitador) > 0 DO  
		  elementos[:indice] := SUBSTR_BEFORE(:item, :p_Delimitador);	-- Subcadena antes de la primera aparición del delimitador  	  
		  item     := SUBSTR_AFTER(:item, :p_Delimitador);              -- Subcadena después de la primera aparición del delimitador	  
		  indice   := :indice + 1;
	  END WHILE;
	  
	  elementos[:indice] := :item;
	 
	  v_Elementos = UNNEST(:elementos) AS ("Item"); -- Convierte un arreglo en tabla
	      
	  p_out_Elementos =  SELECT "Item" FROM :v_Elementos;  
  END IF;
  
  
END;

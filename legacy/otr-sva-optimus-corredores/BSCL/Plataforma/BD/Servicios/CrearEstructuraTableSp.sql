SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CrearEstructuraTableSp";
CREATE PROCEDURE 
-- 
--  Proposito: Crea dinamicamente un Insert para usar en SPS
--  Autor: chernandez
--  Fecha Creacion: 27/10/2015
--  call "BSCL.Plataforma.BD.Servicios::CrearEstructuraTableSp"('BSCL.SocioNegocio.BD::PER.Persona',?)
"BSCL.Plataforma.BD.Servicios::CrearEstructuraTableSp"(IN p_schema NVARCHAR(20), IN p_table_name NVARCHAR(255), OUT p_out_sql NVARCHAR(5000)) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN 
	declare v_sql nvarchar(5000);
	declare v_type nvarchar(100);
	
	v_ref_cols = select COLUMN_NAME,DATA_TYPE_NAME,LENGTH,SCALE from TABLE_COLUMNS where SCHEMA_NAME = p_schema AND TABLE_NAME = :p_table_name order by POSITION ASC;
	v_sql := 'DECLARE v_table TABLE('||char(13)||char(10);
	BEGIN
	 DECLARE CURSOR cur_col FOR
	 SELECT COLUMN_NAME,DATA_TYPE_NAME,LENGTH,SCALE FROM :v_ref_cols;
	 FOR v_row as cur_col DO
	 		v_type := CASE 
	 					WHEN v_row.DATA_TYPE_NAME='NVARCHAR' OR v_row.DATA_TYPE_NAME='CHAR' THEN '(' || TO_VARCHAR(v_row.LENGTH) ||')' 
	 					WHEN v_row.DATA_TYPE_NAME='DECIMAL' THEN '(' || TO_VARCHAR(v_row.LENGTH) ||')' 
	 					ELSE '' END;
	 					
		 	v_sql :=   v_sql ||'        "'|| v_row.COLUMN_NAME || '" ' || v_row.DATA_TYPE_NAME || v_type ||','||char(13)||char(10);
	  END FOR;
	END;  
	v_sql :=  v_sql || ' );--' || 'eliminar la comilla del ultimo campo';
	p_out_sql:=v_sql;
END;

SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::GeneraAlterTable";

CREATE PROCEDURE
-- 
--  Proposito: Genera Sintaxis para realizar Alter Table
--  Autor: JGarces
--  Fecha Creacion: 01/03/2017
--   
"BSCL.Plataforma.BD.Utilitarios::GeneraAlterTable" ( 	
    IN  p_SchemaName	NVARCHAR(40) default 'BSCL',
    IN  p_Action		NVARCHAR(40) default 'ALTER', -- ALTER, ADD, DROP
	IN  p_TableName		NVARCHAR(500) default null, -- TODO: si viene solo aplicar a esa tabla, sino a todas las que encuentre con ese campo
    IN  p_ColumnName	NVARCHAR(100),
    IN	p_DataTypeName	NVARCHAR(40) default '',-- NVARCHAR, BIGINT, INT, DATE, DECIMAL, etc
    IN  p_LengthNew		INT default 0,
	OUT p_out_Salida	NVARCHAR(100)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
--READS SQL DATA 
AS BEGIN
	-- variables
	--manejo errores
	BEGIN -- procesamiento
		p_out_Salida = 'ok';
		IF(p_Action='ALTER') THEN
			--v_res = select 1 from dummy;
			v_res = select 'alter table "' || tc.table_name || '" alter( "'|| :p_ColumnName ||'" '||p_DataTypeName||'('|| cast(:p_LengthNew as nvarchar) ||')' ||
			case tc.is_nullable when 'FALSE' then 'NOT NULL DEFAULT ''''' else 'NULL' end || ');' as "--LE_ALTER" 
			from table_columns tc join sys.tables t on t.table_name = tc.table_name and t.schema_name = tc.schema_name
			where tc.schema_name = :p_SchemaName and tc.column_name = :p_ColumnName
			and tc.data_type_name = :p_DataTypeName and tc.length < :p_LengthNew and t.is_user_defined_type = 'FALSE';
			select * from :v_res;
		ELSE
			p_out_Salida = 'p_Action no codificado';
		END IF;
	END;
END;

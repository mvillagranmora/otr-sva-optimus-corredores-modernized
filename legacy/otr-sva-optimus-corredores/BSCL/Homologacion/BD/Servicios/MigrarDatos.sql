---call "BSCL"."BSCL.Homologacion.BD.Servicios::MigrarDatos"('BSCL.Mercado.BD::MER.Emisor',?)        
DROP PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::MigrarDatos";

CREATE PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::MigrarDatos" (
	IN  p_Tabla    	 NVARCHAR(100),
    OUT p_out_Script NVARCHAR(520)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE LOGICA
	--------------------
 	DECLARE v_Pos       BIGINT;
 	DECLARE v_Column	NVARCHAR(256);
 	DECLARE v_Type		NVARCHAR(100);	
    DECLARE Arr_COLUMN_NAME         NVARCHAR(100) ARRAY;
	DECLARE Arr_DATA_TYPE_NAME      NVARCHAR(100) ARRAY;
	DECLARE v_Configuracion table ("DATA_TYPE_NAME" NVARCHAR(100), "COLUMN_NAME" NVARCHAR(256));
	
	--------------------
	--DECLARE SALIDA
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN	
		
	END;	
	
	--------------------
	--LOGICA 
	--------------------	   
	v_Configuracion =  SELECT DATA_TYPE_NAME, COLUMN_NAME FROM TABLE_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND  TABLE_NAME =  p_Tabla;
	
    Arr_COLUMN_NAME     := ARRAY_AGG(:v_Configuracion."COLUMN_NAME");          
    Arr_DATA_TYPE_NAME  := ARRAY_AGG(:v_Configuracion."DATA_TYPE_NAME");
 
 	p_out_Script := ' INSERT INTO "#TABLA_SCRIPT"  Select ' || '''' || ' insert into '|| p_Tabla || ' select ' || '''' || '||';

 	FOR v_Pos IN 1 .. CARDINALITY(:Arr_COLUMN_NAME) DO 
 	
         v_Column := :Arr_COLUMN_NAME[:v_Pos];
         v_Type   := :Arr_DATA_TYPE_NAME[:v_Pos];
         if v_Pos = 1 then
         	if v_Type = 'NVARCHAR' then
		         p_out_Script := p_out_Script ||'''''''''||ifnull('||'"' || TRIM(v_Column) || '"'||','''')||''''''''';
		    else 
			    p_out_Script := p_out_Script ||'"' || TRIM(v_Column) || '"';
			end if;
	     else
		     if v_Type = 'NVARCHAR' then
		         p_out_Script := p_out_Script ||'||'''||','||'''||'||'''''''''||ifnull('||'"' || TRIM(v_Column) || '"'||','''')||''''''''';
		    else 
			    p_out_Script := p_out_Script ||'||'''|| ','||'''||'|| '"' || TRIM(v_Column) || '"';
			end if;
			
	     end if;
         
	END FOR; 
		
	p_out_Script :=  p_out_Script || ' as salida from ' || '"BSCL".'||'"'||p_Tabla||'"';
	
	CREATE LOCAL TEMPORARY TABLE "#TABLA_SCRIPT" (Grande nvarchar(1000));
--	SELECT  count(*) FROM "#TABLA_SCRIPT";
	
	exec p_out_Script;
	
	SELECT  * FROM "#TABLA_SCRIPT";
	
	DROP TABLE "#TABLA_SCRIPT";
	--------------------
	--TERMINO OK 
	--------------------	 	
--    SIGNAL SALIDA_CONTROLADA; 	
END;




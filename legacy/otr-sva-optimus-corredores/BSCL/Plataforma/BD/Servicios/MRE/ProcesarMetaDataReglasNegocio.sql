SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MRE.ProcesarMetaDataReglasNegocio";

CREATE PROCEDURE
-- 
--  Proposito:  Procesa la meta data de las reglas actualizando los parametros de llamadas
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::MRE.ProcesarMetaDataReglasNegocio" () 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN --SEQUENTIAL EXECUTION
	DECLARE ERROR_PARAMETRO_SINDEFINIR CONDITION FOR SQL_ERROR_CODE 10002;
	DECLARE ERROR_EXCEDE_REGLAS CONDITION FOR SQL_ERROR_CODE 10004;
	declare v_schema		  nvarchar(40);
	declare v_param			  nvarchar(520);
		
	CREATE LOCAL TEMPORARY TABLE "#TEST_TABLE" (SP VARCHAR(520),PARAM VARCHAR(520));

	v_schema := 'BSCL'; --TODO: OBTENER DESDE UNA CONFIGURACION DE SISTEMA

	v_datosSPT1  = SELECT PARAMETER_NAME as "PARAM",DATA_TYPE_NAME as "TYPE",PROCEDURE_NAME as "SP",POSITION as "POS"   
			  	    FROM "SYS"."PROCEDURE_PARAMETERS" 
				  WHERE SCHEMA_NAME = :v_schema 
				      and PROCEDURE_NAME IN (SELECT distinct "FuncionSP"  FROM "BSCL.Plataforma.BD::MRE.Regla" )
					  and PARAMETER_NAME not in ('P_IDEJECUCION','P_OUT_RESULTADOREGLA','P_OUT_MENSAJEREGLA')
				  ORDER BY  "SP","POS";
		
    begin
    	declare CURSOR c_c1 FOR SELECT "FuncionSP" FROM "BSCL.Plataforma.BD::MRE.Regla";
    	FOR reg AS c_c1 DO
    		declare sp nvarchar(520) := reg."FuncionSP";
    		declare CURSOR c_c2 FOR SELECT "PARAM","TYPE" FROM :v_datosSPT1 WHERE "SP" = :sp;    		    		
    		v_param:=' ';
    		FOR regSP AS c_c2 DO
					if regSP."TYPE" = 'NVARCHAR' then
						v_param:=v_param||''''||LCASE(regSP."PARAM")||''',';
					elseif regSP."TYPE" = 'VARCHAR' then
						v_param:=v_param||''''||LCASE(regSP."PARAM")||''',';
					else 
						v_param:=v_param||LCASE(regSP."PARAM")||',';					
					end if;	    		    			
    		END FOR;      		
    		--quita la ',' final
    		if right(v_param,1) = ',' then
    			v_param := substring(v_param,1,length(v_param)-1);
    		end if;
    		insert into  "#TEST_TABLE"	values (reg."FuncionSP",v_param);	
    	END FOR;        	
    end;	
	
	update   t1
	 set "Parametros" = t2."PARAM"
	from "BSCL.Plataforma.BD::MRE.Regla" as t1
	left join "#TEST_TABLE" as t2 on t1."FuncionSP" = t2."SP"; 
		
	drop table "#TEST_TABLE";
		
END;
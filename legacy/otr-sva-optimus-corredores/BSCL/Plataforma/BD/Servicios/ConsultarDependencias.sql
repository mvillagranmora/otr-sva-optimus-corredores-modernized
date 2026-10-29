SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarDependencias";
CREATE PROCEDURE 
-- 
--  Proposito: Consulta las dependencias de un SP
--  Autor: CST
--  Fecha Creacion: 27/10/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarDependencias"(IN p_sp NVARCHAR(255)) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN 

declare dep nvarchar(255);
declare pos int default 0; 

create local temporary table #RESCALL("POS" int,"YO_LLAMO" nvarchar(200), "OBJECTO_TIPO" nvarchar(80) ,"NOMBRE" nvarchar(200),"NIVEL" int);

--quien me usa
select dependent_object_name as "ME_LLAMAN",dependent_object_type as "OBJECTO_TIPO" 
from "SYS"."OBJECT_DEPENDENCIES"
 where base_schema_name = 'BSCL' 
 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') 
  AND DEPENDENCY_TYPE = 1
 AND base_object_name = :p_sp
 ORDER BY dependent_object_name;

--quien estoy usando
--nivel 1------ inicio
dep = :p_sp;
BEGIN
	DECLARE CURSOR c_cursor1 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
	FOR r1 AS c_cursor1 DO	
		pos = pos + 1;
		insert into #RESCALL select pos,r1.base_object_name,r1.base_object_type,r1.base_object_name,1 from dummy;
		--nivel 2------ inicio
		dep = r1.base_object_name;
		BEGIN
			DECLARE CURSOR c_cursor2 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
			FOR r2 AS c_cursor2 DO	
				pos = pos + 1;				
				insert into #RESCALL select pos,'----> '||r2.base_object_name,r2.base_object_type,r2.base_object_name,2  from dummy;
				--nivel 3------ inicio
				dep = r2.base_object_name;
				BEGIN
					DECLARE CURSOR c_cursor3 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
					FOR r3 AS c_cursor3 DO	
						pos = pos + 1;				
						insert into #RESCALL select pos,'--------> '||r3.base_object_name,r3.base_object_type,r3.base_object_name,3  from dummy;
						--nivel 4------ inicio
						dep = r3.base_object_name;
						BEGIN
							DECLARE CURSOR c_cursor4 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
							FOR r4 AS c_cursor4 DO	
								pos = pos + 1;				
								insert into #RESCALL select pos,'------------> '||r4.base_object_name,r4.base_object_type,r4.base_object_name,4  from dummy;
								--nivel 5------ inicio
								dep = r4.base_object_name;
								BEGIN
									DECLARE CURSOR c_cursor5 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
									FOR r5 AS c_cursor5 DO	
										pos = pos + 1;				
										insert into #RESCALL select pos,'--------------> '||r5.base_object_name,r5.base_object_type,r5.base_object_name,5  from dummy;
										--nivel 6------ inicio
										dep = r5.base_object_name;
										BEGIN
											DECLARE CURSOR c_cursor6 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
											FOR r6 AS c_cursor6 DO	
												pos = pos + 1;				
												insert into #RESCALL select pos,'----------------> '||r6.base_object_name,r6.base_object_type,r6.base_object_name,6  from dummy;
												--nivel 7------ inicio
												dep = r6.base_object_name;
												BEGIN
													DECLARE CURSOR c_cursor7 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
													FOR r7 AS c_cursor7 DO	
														pos = pos + 1;				
														insert into #RESCALL select pos,'------------------> '||r7.base_object_name,r7.base_object_type,r7.base_object_name,7  from dummy;
														--nivel 8------ inicio
														dep = r7.base_object_name;
														BEGIN
															DECLARE CURSOR c_cursor8 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
															FOR r8 AS c_cursor8 DO	
																pos = pos + 1;				
																insert into #RESCALL select pos,'--------------------> '||r8.base_object_name,r8.base_object_type,r8.base_object_name,8  from dummy;
																--nivel 9------ inicio
																dep = r8.base_object_name;
																BEGIN
																	DECLARE CURSOR c_cursor9 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
																	FOR r9 AS c_cursor9 DO	
																		pos = pos + 1;				
																		insert into #RESCALL select pos,'----------------------> '||r9.base_object_name,r9.base_object_type,r9.base_object_name,9  from dummy;
																		--nivel 10------ inicio
																		dep = r9.base_object_name;
																		BEGIN
																			DECLARE CURSOR c_cursor10 FOR SELECT base_object_name,base_object_type FROM "SYS"."OBJECT_DEPENDENCIES"  WHERE base_schema_name = 'BSCL'  AND DEPENDENCY_TYPE = 1 AND base_object_type not in ('TABLE','SEQUENCE') and dependent_object_type not in ('SYNONYM') AND dependent_object_name = :dep;
																			FOR r10 AS c_cursor10 DO	
																				pos = pos + 1;				
																				insert into #RESCALL select pos,'------------------------> '||r10.base_object_name,r10.base_object_type,r10.base_object_name,10  from dummy;
																			END FOR;
																		END;		
																		--nivel 10------ fin		
																	END FOR;
																END;		
																--nivel 9------ fin		
															END FOR;
														END;		
														--nivel 8------ fin		
													END FOR;
												END;		
												--nivel 7------ fin		
											END FOR;
										END;		
										--nivel 6------ fin		
									END FOR;
								END;		
								--nivel 5------ fin		
							END FOR;
						END;		
						--nivel 4------ fin		
					END FOR;
				END;		
				--nivel 3------ fin		
			END FOR;
		END;		
		--nivel 2------ fin		
	END FOR;
END;
--nivel 1------ fin
select * from #RESCALL order by POS;  

 
drop table #RESCALL;


END;

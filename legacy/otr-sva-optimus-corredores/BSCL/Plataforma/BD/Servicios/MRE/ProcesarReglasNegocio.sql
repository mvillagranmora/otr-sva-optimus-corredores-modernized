SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MRE.ProcesarReglasNegocio";

CREATE PROCEDURE
-- 
--  Proposito: Ejecuta dinamicamente las funciones de validacion y de reglas para las operaciones
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::MRE.ProcesarReglasNegocio" (
			 IN  p_IdEmpresa			bigint,
			 IN  p_IdGrupoRegla			bigint,
			 IN  p_SoloValidaciones		nvarchar(1), 	--S/N Procesa solo validaciones
			 IN  p_MatrizEvaluar		table ("Codigo" nvarchar(40),"Valor" nvarchar(520)),
			 OUT p_out_ResultadoReglas	nvarchar(40), 	--OK=Cumple todos los requisitos,
			 								  		 	--ADVE=Alguna de las reglas tiene solamente adevertencias
			 								  		 	--RAUT=Alguna de las reglas requiere autorizacion
			 								   			--RECH=Alguna de las reglas detienen la operacion
			 OUT p_out_ResultadoMatriz	Table("idRegla"         bigint,         --valor de configuracion de la regla para el producto
			 							  "Habilitado"          nvarchar(1),    --valor de configuracion de la regla para el producto
			 							  "CodComportamiento"   nvarchar(40),   --valor de configuracion de la regla para el producto
			 							  "ResultadoEvaluacion" integer,        --Valor que retorna la funcion de regla
			 							  "ReglaActivada" 	 	integer,        --En base al comportamiento indica si se activa
			 							  "MensajeRegla"        nvarchar(520)   --Mensaje de la regla
			 							  )			  			  
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
MAX_REGLAS integer := 1000;
BEGIN --SEQUENTIAL EXECUTION
	DECLARE ERROR_PARAMETRO_SINDEFINIR CONDITION FOR SQL_ERROR_CODE 10002;
	DECLARE ERROR_EXCEDE_REGLAS CONDITION FOR SQL_ERROR_CODE 10004;
	declare v_schema		  nvarchar(40);
	declare v_existe		  integer;
	declare v_secregla 		  bigint := 0;
	declare v_secsec 		  bigint := 0;
	declare v_ReglaEvaluacion integer;
	declare v_ReglaResultado  nvarchar(1) := '-';
	declare v_Ambito 		  nvarchar(1) := '-';
	declare v_ReglaActivada   integer := -1;
	declare v_ReglaMensaje    nvarchar(520) := ' ';
	declare v_Exec   		  nvarchar(520) := ' ';
	declare v_Valor   		  nvarchar(520) := ' ';
	DECLARE v_SPparam	      nvarchar(520) ARRAY;
	DECLARE v_HCod		      nvarchar(520) ARRAY;	
	DECLARE v_HVal		      nvarchar(520) ARRAY;	
		
	p_out_ResultadoReglas := 'OK';
	p_out_ResultadoMatriz = select "idRegla","Habilitado","CodComportamiento","ResultadoEvaluacion","ReglaActivada","MensajeRegla"
	                        from "BSCL.Plataforma.BD::MRE.ReglaSalida" 
	                        where "IdEjecucion" = 0;		

	v_schema := 'BSCL'; --TODO: OBTENER DESDE UNA CONFIGURACION DE SISTEMA
	
	v_MatrizEvaluarMinus = select LCASE("Codigo") as "Codigo","Valor" from :p_MatrizEvaluar;
	v_HCod := ARRAY_AGG(:v_MatrizEvaluarMinus."Codigo" ORDER BY "Codigo" ASC);
	v_HVal := ARRAY_AGG(:v_MatrizEvaluarMinus."Valor"  ORDER BY "Codigo" ASC);
		
	SELECT "BSCL.Plataforma.BD.Secuencias::ReglaEjecucion".NEXTVAL INTO v_secregla FROM DUMMY;
	v_secsec:=v_secregla;
																               				   
	if p_SoloValidaciones = 'S' then
		v_Ambito := 'V';
	else
		v_Ambito := 'R';
	end if;
	
	v_RProducto= SELECT  ROW_NUMBER() OVER(ORDER BY rp."IdReglaOperacion" ASC) as "RNUM",
						rp.*,
						rr."EsquemaDB"  as "EsquemaDB",
						rr."FuncionSP"  as "FuncionSP", 
						rr."Parametros" as "SPPARAM"
                   FROM "BSCL.Plataforma.BD::MRE.ReglaOperacion" rp 
                   left join  "BSCL.Plataforma.BD::MRE.Regla" rr on rp."CodigoRegla" =  rr."CodigoRegla"                                                       
                  where  rp."IdEmpresa"=p_IdEmpresa and rp."IdGrupoRegla" = :p_IdGrupoRegla and
                         rp."Ambito"=v_Ambito
               order by  rp."Prioridad" ASC;

	--Deben existir reglas asociadas, si no se activa la regla base :)
 	select count(1) into v_existe from :v_RProducto;
 	if :v_existe = 0 then
 		p_out_ResultadoReglas := 'RECH';
 		v_secsec := v_secsec + 1;
		insert into  "BSCL.Plataforma.BD::MRE.ReglaSalida"  (
					"IdEjecucion",
					"IdSecuencia",
					"idRegla",
					"Habilitado",
					"CodComportamiento",
					"ResultadoEvaluacion",
					"ReglaActivada",
					 "MensajeRegla"
		)  values (
					v_secregla,
					v_secsec,
					0,
					'S',
					'DETO',
					1,
					1,
					'No existen reglas asociadas. Debe exister a lo menos una para [empresa, grupo, ambito].'						
		);	 		 	
   else            
	    --Arma parametros de las llamadas, en un futuro si Hana tien hash mejoraria el rendimiento
	    begin    	
	    	declare CURSOR c_c1 FOR SELECT "RNUM","SPPARAM" FROM :v_RProducto;
	    	FOR reg AS c_c1 DO
				declare v_pos integer := CARDINALITY(:v_HCod);
	    		v_SPparam[reg."RNUM"]:=LCASE(reg."SPPARAM");
	    		while v_pos > 0 do
	    			v_SPparam[reg."RNUM"]:=replace(:v_SPparam[reg."RNUM"],:v_HCod[:v_pos],:v_HVal[:v_pos]);
	    			v_pos := v_pos - 1;
	    		end while;									    	
	    	END FOR;        	
	    end;
	    --itera sobre el numero de reglas                  		               	
		begin				
			declare CURSOR c_c1 FOR SELECT * FROM :v_RProducto;
			FOR reg AS c_c1 DO
				begin
					--Arma LLamada exe										
					v_secsec := v_secsec + 1;
					if (v_secsec>(v_secregla+MAX_REGLAS)) then
						SIGNAL ERROR_EXCEDE_REGLAS SET MESSAGE_TEXT = 'Exede el # de reglas. Revisar Constante en Store Procedure.';					 
					end if;																						 										 					
					v_Exec:='CALL "'||v_schema||'"."'||reg."FuncionSP"||'" (';
					v_Exec:=v_Exec||:v_SPparam[reg."RNUM"];									
					v_Exec:=v_Exec||','||v_secsec||',?,?)';											
					--Ejecuta
					if reg."Habilitado" = 'S' then	
						exec v_Exec;	--no recomendado, solo hace lecturas a reglas						 							 		
						select "Resultado","Mensaje" into v_ReglaResultado, v_ReglaMensaje  
						 from "BSCL.Plataforma.BD::MRE.ReglaEjecucion" 
						where  "Id" = v_secsec;										  					
						if v_ReglaResultado<>'S' then
							v_ReglaActivada:=1;					
							if reg."CodigoComportamiento" = 'SDET' then
								p_out_ResultadoReglas := 'RECH';
							elseif reg."CodigoComportamiento" = 'DETO' then
								p_out_ResultadoReglas := 'RECH';
							elseif 	reg."CodigoComportamiento" = 'SAUT' and  
									p_out_ResultadoReglas <> 'RECH' then
								p_out_ResultadoReglas := 'RAUT';
							elseif 	reg."CodigoComportamiento" = 'AUTH' and  
									p_out_ResultadoReglas <> 'RECH' then
								p_out_ResultadoReglas := 'RAUT';
							elseif 	reg."CodigoComportamiento" = 'ADVE' and  
									p_out_ResultadoReglas <> 'RAUT' and
									p_out_ResultadoReglas <> 'RECH' then
								p_out_ResultadoReglas := 'ADV';
							end if;  						
						else
							v_ReglaActivada:=0;
						end if; 													
					else
						v_ReglaResultado:='-';
						v_ReglaActivada:=-1;
						v_ReglaMensaje:='Regla Deshabilitada';
					end if;				
					--valida resultado final
					if v_ReglaResultado='S' then 
						v_ReglaEvaluacion := 1;
					else
						v_ReglaEvaluacion := 0;
					end if;													
					insert into  "BSCL.Plataforma.BD::MRE.ReglaSalida"  (
								"IdEjecucion",
								"IdSecuencia",
								"idRegla",
								"Habilitado",
								"CodComportamiento",
								"ResultadoEvaluacion",
								"ReglaActivada",
								 "MensajeRegla"
					)  values (
								v_secregla,
								v_secsec,
								reg."IdReglaOperacion",
								reg."Habilitado",
								reg."CodigoComportamiento",
								v_ReglaEvaluacion,
								v_ReglaActivada,
								v_ReglaMensaje							
					);				
					--no sigue evaluando
					if p_SoloValidaciones = 'S' then
						if v_ReglaActivada=1 and reg."DetieneValidacion"='S' then
							break;
						end if;
					end if;						
				end; 
			END FOR;--FOR reg AS c_c1 DO		
		end; 	
	end if; --validacion de numero reglas
	p_out_ResultadoMatriz = select "idRegla","Habilitado","CodComportamiento","ResultadoEvaluacion","ReglaActivada","MensajeRegla"
	                        from "BSCL.Plataforma.BD::MRE.ReglaSalida" 
	                        where "IdEjecucion" = v_secregla;	
	delete 	 from "BSCL.Plataforma.BD::MRE.ReglaSalida" where "IdEjecucion" = v_secregla;	
END;

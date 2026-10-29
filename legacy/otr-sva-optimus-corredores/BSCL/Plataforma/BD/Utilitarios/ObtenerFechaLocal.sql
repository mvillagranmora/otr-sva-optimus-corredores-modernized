SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLocal";

CREATE FUNCTION 
-- 
--  Proposito: Obtiene la fecha LOCAL desde una fecha UTC
--  Autor: CST - BCS
--  Fecha Creacion: 01/08/2015
--  
"BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLocal" ( 
	in  p_FechaHoraUTC  		timestamp,
	in  p_Zona					NVARCHAR(40) default 'CLT'	
) 
	RETURNS resultado timestamp
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	declare ERROR_FECHA_LOCAL_SIN_DATOS CONDITION FOR SQL_ERROR_CODE 10006;
	declare v_error int := 0;
	declare v_segundos int;
	
	declare v_verano  int;
	declare v_ano_verano_inicio int;	
	declare v_mes_verano_inicio int;
	declare v_dia_verano_inicio int;
	declare v_seg_verano_inicio int;
	
	declare v_invierno int;
	declare v_ano_invierno_inicio int;	
	declare v_mes_invierno_inicio int;
	declare v_dia_invierno_inicio int;
	declare v_seg_invierno_inicio int;
		
	-------------------------------------------
	--CHILE
	-------------------------------------------	
	if (p_Zona = 'CL' or p_Zona = 'CLT') then
		--Estandar
		--El horario de verano (-3h) se iniciaba el segundo domingo de octubre de cada año, cuando los relojes se adelantaban una hora. El cambio se realizaba a las 00:00.
		--El horario de invierno (-4h estándar) se iniciaba el segundo domingo de marzo de cada año, cuando los relojes se retrasaban una hora. El cambio se realizaba a las 00:00.		
		v_invierno				:= -4*3600;
		v_ano_invierno_inicio   := year(:p_FechaHoraUTC);
		v_mes_invierno_inicio   := 3;
		v_dia_invierno_inicio   := 14 - WEEKDAY (TO_DATE (v_ano_invierno_inicio||'-03-01', 'YYYY-MM-DD')); 		
		v_verano				:= -3*3600;		
		v_ano_verano_inicio  	:= year(:p_FechaHoraUTC);
		v_mes_verano_inicio  	:= 10;
		v_dia_verano_inicio  	:= 14 - WEEKDAY (TO_DATE (v_ano_verano_inicio||'-10-01', 'YYYY-MM-DD')); 	
		--excepciones
		--En 1987 el cambio al horario de verano se postergó hasta el sábado 11 de abril debido a la visita del papa Juan Pablo II.
		--En 1988 el horario de verano empezó una semana antes con el fin de tener más luz para el plebiscito nacional del 5 de octubre.
		--En 1990 el horario de invierno fue postergado hasta el 17 de marzo, puesto que Patricio Aylwin inició su mandato presidencial el 11 de marzo.
		--Ese mismo año, el inicio del horario de verano se llevó a cabo el 15 de septiembre, con el propósito de ahorrar energía debido a las desfavorables condiciones hidrológicas.
		--En 1997 el inicio de la hora oficial fue el 29 de marzo para hacer frente, como en 1990, a las adversas condiciones hidrológicas.
		--En 2008 el retorno a la hora oficial se retrasó tres semanas, debido a una gran sequía que afectó al país.
		--En 2010, debido al terremoto que sacudió al país, el horario de invierno empezó a las 00:00 del 4 de abril, y en Isla de Pascua a las 20:00 del día anterior.
		--En 2011 el gobierno pospuso el cambio de hora ante la expectativa de un año seco. Tras una primera extensión del horario de verano durante todo el mes de marzo, se decretó el cambio de hora para la primera semana de mayo. De esta forma, el cambio de hora oficial quedó programado para el 7 de mayo a las 24:00, y en Isla de Pascua a las 22:00 del mismo día. La fecha de término fue el 8 de octubre, sin embargo, fue adelantada para el 19 de agosto.14
		--En 2012 el gobierno decidió mantener el plan implementado en 2011 de reducir el horario de invierno y extender el de verano, para así generar un ahorro energético y proteger la seguridad ciudadana. De esta forma, el cambio de hora oficial quedó programado para el día 28 de abril a las 24:00, y en Isla de Pascua a las 22:00 del mismo día. La fecha de término fue el 1 de septiembre.15		
		if year(:p_FechaHoraUTC) = 1987 then
			v_mes_invierno_inicio := 4;
			v_dia_invierno_inicio := 11;			
		elseif year(:p_FechaHoraUTC) = 1988 then
			v_dia_verano_inicio := v_dia_verano_inicio-7;						
		elseif year(:p_FechaHoraUTC) = 1990 then
			v_dia_invierno_inicio := 17;	
			v_dia_verano_inicio := 15;						
		elseif year(:p_FechaHoraUTC) = 1997 then
			v_dia_invierno_inicio := 29;		
		elseif year(:p_FechaHoraUTC) = 2008 then
			v_dia_invierno_inicio := 30;					
		elseif year(:p_FechaHoraUTC) = 2010 then
			v_mes_invierno_inicio := 4;		
			v_dia_invierno_inicio := 4;			
		elseif year(:p_FechaHoraUTC) = 2011 then
			v_mes_invierno_inicio := 5;		
			v_dia_invierno_inicio := 8;	
			v_mes_verano_inicio := 8;
			v_dia_verano_inicio := 19;
		elseif year(:p_FechaHoraUTC) = 2012 then
			v_mes_invierno_inicio := 4;		
			v_dia_invierno_inicio := 29;	
			v_mes_verano_inicio := 9;
			v_dia_verano_inicio := 1;					
		elseif year(:p_FechaHoraUTC) = 2015 then
			v_mes_invierno_inicio := 3;		
			v_dia_invierno_inicio := 15;		
			--todo el año invierno/vernano a partir de 3/15			
			v_invierno			  := -3*3600;
			v_verano			  := -3*3600;	
		elseif year(:p_FechaHoraUTC) = 2016 then
			v_mes_invierno_inicio := 1; 	
			v_dia_invierno_inicio := 1;
			v_mes_verano_inicio := 5;
			v_dia_verano_inicio := 15;						
			v_invierno			  := -3*3600;
			v_verano			  := -4*3600;	
		elseif year(:p_FechaHoraUTC) >= 2017 then
			v_mes_invierno_inicio := 1; 	
			v_dia_invierno_inicio := 1;
			v_mes_verano_inicio := 5;
			v_dia_verano_inicio := 15;						
			v_invierno			  := -4*3600;
			v_verano			  := -3*3600;				
		end if;		
		
		v_segundos 				:= (DAYOFYEAR(:p_FechaHoraUTC)-1)*24*3600 + hour(:p_FechaHoraUTC)*3600 + minute(:p_FechaHoraUTC)*60 + second(:p_FechaHoraUTC);		
		v_seg_invierno_inicio   := (DAYOFYEAR(TO_DATE(v_ano_invierno_inicio||'-'||v_mes_invierno_inicio||'-'||v_dia_invierno_inicio, 'YYYY-MM-DD'))-1)*24*3600 - v_verano;								
		v_seg_verano_inicio  	:= (DAYOFYEAR(TO_DATE(v_ano_verano_inicio||'-'||v_mes_verano_inicio||'-'||v_dia_verano_inicio, 'YYYY-MM-DD'))-1)*24*3600 -v_invierno;										
		
		if v_segundos < v_seg_invierno_inicio then
			resultado := ADD_SECONDS (:p_FechaHoraUTC, v_verano);
		elseif v_segundos < v_seg_verano_inicio then
			resultado := ADD_SECONDS (:p_FechaHoraUTC, v_invierno);
		else
			resultado := ADD_SECONDS (:p_FechaHoraUTC, v_verano);		
		end if;		
	-------------------------------------------
	--EROR PAIS NO DEFINDO
	-------------------------------------------
	else
		v_error:=1;
	end if;	
 	if v_error = 1 then
 		SIGNAL ERROR_FECHA_LOCAL_SIN_DATOS 
 		  SET MESSAGE_TEXT = 'No se han encontado valores en la funcion ObtenerFechaLocal'; 		
 	end if;	
END;

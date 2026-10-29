SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio que convierte un monto numerico a monto escrito
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 04/05/2016
--  
"BSCL.Plataforma.BD.Servicios::ConvertirMontoEscrito" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_Monto			DECIMAL(26,8),
	IN p_Pais			NVARCHAR(10),
	IN p_TipoMoneda		NVARCHAR(3),
	
	  	
	-- Parametros salida del servicio publico
	OUT p_MontoEscrito	nvarchar(2000)	

) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
	--DECLARE VARIABLES
	DECLARE i INT;
	DECLARE j INT;
	DECLARE v_monto_char NVARCHAR(1000);
	DECLARE v_max integer;
	DECLARE v_cantidad_digitos INT;
	DECLARE v_text NVARCHAR(1000);
	DECLARE v_result NVARCHAR(1000);
	DECLARE v_nombreMoneda NVARCHAR(100) DEFAULT '';
	
	DECLARE v_moneda_millones INT DEFAULT 3;
	DECLARE v_get_millon INT DEFAULT 2;
	
	DECLARE v_tresNum NVARCHAR(3);
	DECLARE v_centena NVARCHAR(1);
	DECLARE v_decena NVARCHAR(1);
	DECLARE v_unidad NVARCHAR(1);
	DECLARE v_monto_millones NVARCHAR(3) DEFAULT '';
	DECLARE v_centena_millones NVARCHAR(3);
	DECLARE v_decena_millones NVARCHAR(3);
	DECLARE v_unidad_millones NVARCHAR(3);
    
	DECLARE v_array_unidades NVARCHAR(20) ARRAY;
	DECLARE v_array_decenas NVARCHAR(20) ARRAY;
	DECLARE v_array_centenas NVARCHAR(20) ARRAY;
	DECLARE v_array_millones NVARCHAR(20) ARRAY;
	
	DECLARE v_decimalesText   NVARCHAR(20);
	DECLARE v_DecimalesMoneda BIGINT;
	DECLARE v_divisor         NVARCHAR(20) DEFAULT '1' ;
	DECLARE v_decimal_result  NVARCHAR(20) ;
	DECLARE v_decimalesNum    BIGINT;
	
	--INIT VARIABLES
    v_array_unidades := ARRAY('UN', 'DOS', 'TRES', 'CUATRO', 'CINCO', 'SEIS', 'SIETE', 'OCHO', 'NUEVE', 'DIECI', 'ONCE', 'DOCE', 'TRECE', 'CATORCE', 'QUINCE');
    v_array_decenas := ARRAY('DIEZ', 'VEIN', 'TREINTA', 'CUARENTA', 'CINCUENTA', 'SESENTA', 'SETENTA', 'OCHENTA', 'NOVENTA');
    v_array_centenas:= ARRAY('CIEN','','','','QUINIENTOS','','SETECIENTOS','','NOVECIENTOS');
    v_array_millones:= ARRAY('M', 'B', 'TR','QUA','QUIN','SEC');
    
    
	v_monto_char := TO_VARCHAR(p_Monto);
	v_max:=1;
	v_cantidad_digitos:=0;
	v_text:='';
	v_result:='';
	
	/*
	IF p_TipoMoneda='CLP' THEN
		v_moneda_millones:=3;
	ELSE
		v_moneda_millones:=2;
	END IF;
	*/
	
	-- Cantidad de decimales
	SELECT 
	"PrecioDecimales" INTO v_DecimalesMoneda
	FROM "BSCL.Plataforma.BD::PLA.MonedaFormato"
	WHERE "IdUsoMonedaFormato"= 'GEN'
	  AND "IdMoneda"=:p_TipoMoneda
	LIMIT 1;

	SELECT COUNT("SeparadorMillones")INTO v_get_millon FROM "BSCL.Plataforma.BD::PLA.LocalizacionMoneda" 
	WHERE "Pais" = p_Pais AND "IdMoneda" = p_TipoMoneda;
	
	IF v_get_millon >0 THEN
		SELECT "SeparadorMillones" INTO v_moneda_millones FROM "BSCL.Plataforma.BD::PLA.LocalizacionMoneda" 
		WHERE "Pais" = p_Pais AND "IdMoneda" = p_TipoMoneda;
	END IF;
	
	CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,3,'ConvertirMontoEscrito','MONEDAMILLONES', v_moneda_millones);

	--LOGIC
	IF p_Monto = 0.0 THEN
		p_MontoEscrito := 'CERO';
	ELSE
		v_monto_char := TO_VARCHAR( CAST(p_Monto AS BIGINT) );
		v_cantidad_digitos := LENGTH(v_monto_char);
		v_decimalesText:= SUBSTRING (p_Monto,v_cantidad_digitos+2,v_DecimalesMoneda )  ;
		IF v_decimalesText = '' THEN
			v_decimalesText := '0';
		END IF;
		v_decimalesNum:= CAST(v_decimalesText AS BIGINT);
	
		
		WHILE MOD(:v_cantidad_digitos, 3) > 0
		DO
			v_monto_char:= CONCAT('0', v_monto_char); 
			v_cantidad_digitos := LENGTH(v_monto_char);
		END WHILE;
		v_max := CEIL(v_cantidad_digitos/3);
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP ,3,'ConvertirMontoEscrito','MAX', v_max);
		
		FOR i IN 1.. v_max DO
			v_text:='';
			
			if i = 1 THEN
				v_tresNum := SUBSTRING(v_monto_char,1,3);
			ELSE
				v_tresNum := SUBSTRING(v_monto_char,((i-1)*3)+1,3);
			END IF;
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP ,3,'ConvertirMontoEscrito','TRESNUM', v_tresNum);
			
			v_centena := SUBSTRING(v_tresNum,1,1);
			v_decena := SUBSTRING(v_tresNum,2,1);
			v_unidad := SUBSTRING(v_tresNum,3,1);
			
			

			IF v_centena !='0' THEN
				IF v_centena = '1' THEN
					IF v_decena = '0' and v_unidad = 0 THEN
						v_text := 'CIEN ';
					ELSE
						v_text := 'CIENTO ';
					END IF;
				ELSEIF v_centena ='5' OR v_centena ='7' OR v_centena ='9' THEN					
					v_text := :v_array_centenas[TO_INT(v_centena)] || ' ';
				ELSE
					v_text := :v_array_unidades[TO_INT(v_centena)] || 'CIENTOS ';-- corregir cuando la decena no es 0					
				END IF;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP ,3,'ConvertirMontoEscrito','CENTENA', v_text);
			
			IF v_decena = '0' THEN
				IF v_unidad != '0' THEN
					IF v_centena='0' AND v_decena='0' AND v_unidad='1' AND v_max - :i != 0  THEN --ADJUNTAR UNIDADES
						v_text := :v_text || TO_VARCHAR(:v_array_unidades[TO_INT(v_unidad)]);
					ELSEIF v_unidad='1' AND SUBSTRING(v_monto_char,(:i)*3,3) <= 1 THEN
						v_text := :v_text || TO_VARCHAR(:v_array_unidades[TO_INT(v_unidad)]) || 'O';
					ELSE
						v_text := :v_text || TO_VARCHAR(:v_array_unidades[TO_INT(v_unidad)]);
					END IF;
					CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP ,3,'ConvertirMontoEscrito','UNIDAD', TO_VARCHAR(:v_array_unidades[TO_INT(v_unidad)]));
				END IF;
			ELSE 
				IF v_unidad = '0' THEN
					IF v_decena = '2' THEN
						v_text := :v_text || TO_VARCHAR(:v_array_decenas[TO_INT(v_decena)]) || 'TE';
					ELSE
						v_text := :v_text || TO_VARCHAR(:v_array_decenas[TO_INT(v_decena)]);
					END IF;
				ELSE
					IF v_decena = '1' THEN
						IF v_unidad='1' OR v_unidad='2' OR v_unidad='3' OR v_unidad='4' OR v_unidad='5' THEN
							v_text := :v_text || TO_VARCHAR(:v_array_unidades[TO_INT(v_decena||v_unidad)]);
						ELSE --unidades 6,7,8,9
							v_text := :v_text || 'DIECI' || TO_VARCHAR(:v_array_unidades[TO_INT(v_unidad)]);	
						END IF;
					ELSEIF v_unidad!='0' THEN
						IF v_decena !='2' THEN--veintiXXX
							v_text := :v_text || TO_VARCHAR(:v_array_decenas[TO_INT(v_decena)]) || ' Y ';
						ELSE
							v_text := :v_text || TO_VARCHAR(:v_array_decenas[TO_INT(v_decena)]) || 'TI';
						END IF;
						
						IF v_unidad= '1' AND SUBSTRING(v_monto_char,(:i)*3,3) = 1 THEN--ADJUNTAR UNIDADES CON FIX PARA EL UN UNO
							v_text := :v_text || TO_VARCHAR(:v_array_unidades[TO_INT(v_unidad)]) || 'O';
						ELSE
							v_text := :v_text || TO_VARCHAR(:v_array_unidades[TO_INT(v_unidad)]);
						END IF;
					ELSE
						v_text := :v_text || TO_VARCHAR(:v_array_decenas[TO_INT(v_decena)]);
					END IF;
					
				END IF;
			END IF;
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP ,3,'ConvertirMontoEscrito','TOTAL', v_text);
			
			--Agregar si es millon, mil lo que sea.
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP ,3,'ConvertirMontoEscrito','INDEX MILLON', (v_max - :i));
			IF :v_text!='' THEN
				IF v_max - :i = 0 AND v_result!='' AND SUBSTRING(v_monto_char,(:i-1)*3,3) < 10 THEN
					v_result:= v_result || ' Y ';
				ELSEIF v_result !='' THEN 
					v_result:= :v_result || ' ';
				END IF;
				IF v_max - :i = 1 THEN --miles
					IF v_centena='0' AND v_decena='0' AND v_unidad='1' THEN
						v_text:= 'MIL';
					ELSE
						v_text:= :v_text || ' MIL';
					END IF;
				END IF;
				
				IF v_max - :i = 2 THEN --millones						
					IF v_centena='0' AND v_decena='0' AND v_unidad='1' AND :v_monto_millones=''THEN
						v_text:= :v_text || ' MILLON';
					ELSE
						v_text:= :v_text || ' MILLONES';
					END IF;
				END IF;
				
				IF v_max - :i = :v_moneda_millones THEN --mil millones
					v_monto_millones := SUBSTRING(:v_monto_char,:i*3+1,3);
					v_centena_millones := SUBSTRING(:v_monto_millones,1,1);
					v_decena_millones := SUBSTRING(:v_monto_millones,2,1);
					v_unidad_millones := SUBSTRING(:v_monto_millones,3,1);
					
					IF v_centena_millones!='0' OR v_decena_millones!='0' OR v_unidad_millones!='0' THEN
						IF v_centena='0' AND v_decena='0' AND v_unidad='1' THEN
							v_text:= 'MIL';
						ELSE
							v_text:= :v_text || ' MIL';
						END IF;
					ELSEIF v_centena='0' AND v_decena='0' AND v_unidad='1' THEN
						v_text:= ' MIL MILLONES';
					ELSE
						v_text:= :v_text || ' MIL MILLONES';
					END IF;
				END IF;
				
				IF v_max - :i > :v_moneda_millones THEN --bi|tri/llones						
					IF v_centena='0' AND v_decena='0' AND v_unidad='1' THEN
						v_text:= :v_text || ' ' || TO_VARCHAR(:v_array_millones[(v_max - :i - v_moneda_millones+1)]) || 'ILLON';
					ELSE
						v_text:= :v_text || ' ' || TO_VARCHAR(:v_array_millones[(v_max - :i - v_moneda_millones+1)]) || 'ILLONES';
					END IF;
				END IF;
				
			END IF;
			
			v_result := :v_result || :v_text;
 		END FOR;
 		
 		
 		IF  LENGTH (v_decimalesText) >0 AND p_TipoMoneda != 'CLP' THEN 
 			v_divisor ='1';
 			FOR j IN 1.. LENGTH (v_decimalesText) DO
 				v_divisor:= :v_divisor||'0'; 			
	 		END FOR;
	 		
	 		v_decimal_result = :v_decimalesText ||'/'||:v_divisor;
	 		
	 		v_result= :v_result ||' '||:v_decimal_result;	
 			
 		END IF;
 		
 		
		p_MontoEscrito := :v_result;	
		
 	END IF;
 	
END;
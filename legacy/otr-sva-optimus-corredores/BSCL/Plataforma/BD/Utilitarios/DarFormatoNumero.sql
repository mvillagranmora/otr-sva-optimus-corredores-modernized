SET SCHEMA "BSCL";
GO
DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero";
GO
CREATE FUNCTION 
-- 
--  Proposito: Devuelve el formato de un numero #.###.###,###
--  Autor: CST - BCS
--  Fecha Creacion: 01/08/2015
--  Tipos de numeros:
--     i=entero
--     d=decimal estandar 
--     dx=decimal con p_decimales
--     t=tasa
--     tx=tasa  con p_decimales
--     p=porcentaje
--     px=porcentaje  con p_decimales
"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero" ( 
	in  p_numero				double,
	in  p_tipo					nvarchar(3) default 'd',
	in  p_decimales				int			default 0,
	IN p_codigoPais            nvarchar(3) DEFAULT 'CL'
) 
	RETURNS resultado nvarchar(45)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	--
	declare v_separador_mil 		nvarchar(1);
	declare v_separador_dec			nvarchar(1);
	declare v_dec_p					int;
	declare v_dec_d					int;
	declare v_dec_t					int;	
	--
	declare v_decimal	 			nvarchar(100);
	declare v_resultado 			nvarchar(100);
	declare v_tnum					nvarchar(100);
	declare v_tdec					nvarchar(100);		
	declare v_numero_dec			int;
	declare v_pos_dec				int;
	declare v_len					int;
	declare v_grupo					int;	
	declare v_rest					int;	
	declare v_signo					int;	
	declare v_index					int;
	-------------------------------------------
	--VALIDA PARAMETROS
	-------------------------------------------	

	-------------------------------------------
	--CHILE
	-------------------------------------------	
	if (p_codigoPais = 'CL') then
		 v_separador_mil 	:= '.';
		 v_separador_dec 	:= ',';		
		 v_dec_p			:= 2;
		 v_dec_t			:= 2;
		 v_dec_d			:= 3;		 
	end if;
	-------------------------------------------
    --PERU
    ------------------------------------------- 
	if (p_codigoPais = 'PE') then
         v_separador_mil    := ',';
         v_separador_dec    := '.';     
         v_dec_p            := 3;
         v_dec_t            := 3;
         v_dec_d            := 3;        
    end if; 
	-------------------------------------------
	--Configura numero
	-------------------------------------------
	if v_decimal < 0 then
		v_signo		:= 1;
		v_decimal 	:= abs(v_decimal);
	else
		v_signo		:= 0;
		v_decimal 	:= abs(v_decimal);
	end if;			
	if p_tipo = 'p' then
		v_decimal  := cast(p_numero*100 as nvarchar(100));	
		v_numero_dec := v_dec_p;		
	elseif p_tipo = 'px' then
		v_decimal  := cast(p_numero*100 as nvarchar(100));	
		v_numero_dec := p_decimales;				
	else 
		v_decimal := cast(p_numero as nvarchar(100));		
		if p_tipo = 'i' then
			v_numero_dec := 0;			
		elseif p_tipo = 'd' then
			v_numero_dec := v_dec_d;
		elseif p_tipo = 'dx' then
			v_numero_dec := p_decimales;			
		elseif p_tipo = 't' then
			v_numero_dec := v_dec_t;			
		elseif p_tipo = 'tx' then
			v_numero_dec := p_decimales;
		else
			v_numero_dec := p_decimales;									
		end if;		
	end if;
	-------------------------------------------
	--Agrega separador de mil
	-------------------------------------------
	v_pos_dec := locate(v_decimal,'.');
	if v_pos_dec > 0 then
		v_tnum := substr(v_decimal,1,v_pos_dec-1);
		v_tdec := substr(v_decimal,v_pos_dec+1);
		v_len  := length(v_tnum);
	else
		v_tnum := v_decimal;
		v_tdec := '0';
		v_len  := length(v_tnum);	 				
	end if;	
	v_grupo	  := to_int(v_len/3);
	v_rest	  := v_len - v_grupo*3;	
	if v_rest > 0 then
		v_resultado	:= substr(v_tnum,1,v_rest);
	else 
		v_resultado := '';
	end if;		
	for v_index IN 1 .. v_grupo do 
		if length(v_resultado) > 0 then
			v_resultado := v_resultado || v_separador_mil || substr(v_tnum,v_rest+1+(v_index-1)*3,3);
		else
			v_resultado := substr(v_tnum,v_rest+1+(v_index-1)*3,3);			
		end if; 
	end for; 		
	if v_signo = 1 then
		v_resultado := '-'||v_resultado;
	end if;
	-------------------------------------------
	--Limita los decimales
	-------------------------------------------		
	if v_numero_dec > 0 then
		v_resultado := v_resultado || v_separador_dec ||rpad(substr(v_tdec,1,v_numero_dec),v_numero_dec,'0');
	end if;		
	-- FIN --
	resultado := v_resultado;	
END;
/*
SELECT "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(25.32000000,'i',3, 'PE') AS "PrecioOrden"
FROM DUMMY;
*/
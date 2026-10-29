SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.ValidaHashServicioUsuario";

CREATE PROCEDURE
-- 
--  Proposito: Valida una sesion de usuario 
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::ACC.ValidaHashServicioUsuario" (
	IN p_IdEmpresa					BIGINT,	
	IN p_IdUsuario					BIGINT,	
	IN p_Hash						NVARCHAR(520),		
	-- Salida Correcta
	OUT p_out_PermiteUso			NVARCHAR(5),
	OUT p_out_Servicio				NVARCHAR(520),
	OUT p_out_Tipo  				NVARCHAR(1),
	OUT p_FiltroServicio table ( spConsulta NVARCHAR(1040),valor NVARCHAR(1040), operador NVARCHAR(40))
	
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 	
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------	
		declare v_existe 						int;
			
		--default 
		p_out_PermiteUso := 'N';
		p_out_Servicio   := '';
																
		--Valida que el servicio exista
		select count(1) into v_existe   
		 from "BSCL.Plataforma.BD::PLA.Servicio"
		where "IdServicio" = :p_Hash;		
		if v_existe > 0 then
			select "RutaServicio",'S', "TipoServicio" into p_out_Servicio,p_out_PermiteUso,p_out_Tipo  
			 from "BSCL.Plataforma.BD::PLA.Servicio"
			where "IdServicio" = :p_Hash;	

            --obtiene filtros para la consulta
            if p_out_Tipo ='O' then 
                p_FiltroServicio =
    	        select T1."spConsulta" as spConsulta , T1."valor" as valor, T1."operador" as operador
    	        from "BSCL.Plataforma.BD::PLA.Filtro" T1
    	        INNER Join "BSCL.Plataforma.BD::PLA.ParametroFiltro" T2 on T1."IdFiltro" = T2."IdFiltro"
    	        where T2."IdServicio" = p_Hash 
    	            and T1."estado" = 'S';
    	           -- and T1."IdRol" =  p_IdRol; se comenta el rol mientras no exista definicion para los usuarios. debe ser parametro de entrada
            end if;	
			--Estadistica de uso
			BEGIN AUTONOMOUS TRANSACTION	
				update "BSCL.Plataforma.BD::PLA.Servicio"
				   set "UsoServicio" =  "UsoServicio" +  1
				where "IdServicio" = :p_Hash;	
			END;						
		end if;		
		
		--Otras futuras validaciones
						
		-----------------------------------------------------
		--FIN SP 
		-----------------------------------------------------
	END;	
END;

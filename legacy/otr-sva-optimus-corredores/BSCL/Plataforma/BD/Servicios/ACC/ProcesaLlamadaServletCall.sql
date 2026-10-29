SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.ProcesaLlamadaServletCall";
CREATE PROCEDURE
-- 
--  Proposito: Procesa la llamada que hace el servlet call.  Devolviendo informacion necesaria para procesar por el call
--  Autor: CSTANGE - BCS
--  Fecha Creacion: 13/12/2016
--  Modificaciones: 
--  22-02-2017 Se agrega informacion estadistica mensual
--
"BSCL.Plataforma.BD.Servicios::ACC.ProcesaLlamadaServletCall" (
	p_Token					NVARCHAR(520),
	p_IdServicio			INT,
	p_IdTransaccion			NVARCHAR(40),
	p_IdCanal				NVARCHAR(10) DEFAULT 'COR',
	-- Salida Info sesion
	OUT p_out_IdEmpresa				BIGINT,	
	OUT p_out_IdUsuario				BIGINT,	 
	OUT p_out_NombreUsuario			NVARCHAR(80), 
	OUT p_out_TipoUsuario			NVARCHAR(80), 
	-- Salida Info Servicio
	OUT p_out_NombreServicio		NVARCHAR(100),	
	OUT p_out_TipoServicio			NVARCHAR(1),
	OUT p_out_ServicioTempIn		NVARCHAR(5000),
	OUT p_out_ServicioTempOut		NVARCHAR(5000),
	-- Retorno
	OUT p_out_PermiteUso			NVARCHAR(1), 
	OUT p_out_Mensaje				NVARCHAR(80) ,
	OUT p_out_Extras				NVARCHAR(5000) 
	
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	--------------------
	--Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		p_out_PermiteUso := 'N';
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_PermiteUso := 'N';
		IF ::SQL_ERROR_CODE > 10000 THEN
			p_out_Mensaje := 'Error de aplicativo '||::SQL_ERROR_CODE;												
		ELSE
			p_out_Mensaje := 'Error interno '||::SQL_ERROR_CODE;			
			RESIGNAL; 
		END IF;		
	END;	
	
	
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------	
		declare v_existe 						int;
		declare v_IdEstado						nvarchar(3);		
		declare v_FechaVigencia 				date;
		declare v_registraUso					NVARCHAR(1);
		DECLARE p_sysDate NVARCHAR(10);
		
		--Valida que el idservicio exista y sea valido
		select count(1) into v_existe 
		 from "BSCL.Plataforma.BD::PLA.Servicio"
		where "IdServicio" = :p_IdServicio;
		if v_existe = 0 then
			p_out_Mensaje := 'SERVICIO INVALIDO';			
			SIGNAL SALIDA_CONTROLADA; 		  			
		end if;		
				
		--informacion del servicio
		select srv."RutaServicio",srv."TipoServicio",srv."TablasTempIN",srv."TablasTempOUT",srv."RegistraUso",coalesce(string_agg(srvextdet."RutaServicio",','),'') as "Extras"
			into p_out_NombreServicio,p_out_TipoServicio,p_out_ServicioTempIn,p_out_ServicioTempOut,v_registraUso,p_out_Extras
		from "BSCL.Plataforma.BD::PLA.Servicio" srv
		left join "BSCL.Plataforma.BD::PLA.ServicioExtra" srvext on srv."IdServicio" = srvext."IdServicio" and srvext."IdCanal" = :p_IdCanal
		left join "BSCL.Plataforma.BD::PLA.Servicio" srvextdet on srvext."IdServicioExtra" = srvextdet."IdServicio"
		where srv."IdServicio" = :p_IdServicio
		group by srv."RutaServicio",srv."TipoServicio",srv."TablasTempIN",srv."TablasTempOUT",srv."RegistraUso";
		--select "RutaServicio","TipoServicio","TablasTempIN","TablasTempOUT","RegistraUso" 
		--	into p_out_NombreServicio,p_out_TipoServicio,p_out_ServicioTempIn,p_out_ServicioTempOut,v_registraUso
		-- from "BSCL.Plataforma.BD::PLA.Servicio"
		--where "IdServicio" = :p_IdServicio;		
						
		--Valida que el token exista y sea valido
		select count(1) into v_existe 
		 from "BSCL.Plataforma.BD::ACC.Sesion"
		where "IdTokenSesion" = :p_Token and "TokenValido" = 'S';
		if v_existe = 0 then
			p_out_Mensaje := 'TOKEN INVALIDO';			
			SIGNAL SALIDA_CONTROLADA; 		  			
		end if;		
		
		--obtiene id usuario
		select "IdUsuario"  into p_out_IdUsuario
		 from "BSCL.Plataforma.BD::ACC.Sesion"
		where "IdTokenSesion" = :p_Token and "TokenValido" = 'S';	
		
		--obtiene datos usuarios
		select "IdEmpresa","FechaVigencia","IdEstado","CodigoUsuario", "TipoEntidad" into p_out_IdEmpresa,v_FechaVigencia, v_IdEstado,p_out_NombreUsuario, p_out_TipoUsuario
		  from "BSCL.Plataforma.BD::ACC.Usuario" 
		where "IdUsuario" = :p_out_IdUsuario;	
	
	
		CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_out_IdEmpresa, p_sysDate);
		--valida que Usuario no este bloqueado
		IF :v_FechaVigencia <> '0001-01-01' THEN
			IF :v_FechaVigencia < :p_sysDate  THEN
			p_out_Mensaje := 'CUENTA BLOQUEADA O INACTIVA';	
				SIGNAL SALIDA_CONTROLADA; 		  			
			END IF;
		END IF;
		IF :v_IdEstado = 'BLO' THEN
			p_out_Mensaje := 'CUENTA BLOQUEADA O INACTIVA';	
			SIGNAL SALIDA_CONTROLADA; 		  			
		END IF;			
					
						
		--Transaccion				
		BEGIN AUTONOMOUS TRANSACTION			
			
		    INSERT INTO "BSCL.Plataforma.BD::PLA.AuditoriaServicios" 
		    ("FechaRegistro","IdServicio","IdUsuario","RutaServicio","CodigoEvento","Adicional1","Adicional2","Adicional3","Adicional4","IdTransaccion")
			SELECT CURRENT_TIMESTAMP,:p_IdServicio,:p_out_IdUsuario,:p_out_NombreServicio,'INICIO',
			NULL,NULL,NULL,NULL,:p_IdTransaccion FROM DUMMY WHERE :p_IdServicio NOT IN (600017,14,1381);
		    
		END;
		--Salida	
	    p_out_PermiteUso := 'S';
	    p_out_Mensaje 	 := 'OK++';
		--------------------
		--FIN SP 
		--------------------
	END;	
END;


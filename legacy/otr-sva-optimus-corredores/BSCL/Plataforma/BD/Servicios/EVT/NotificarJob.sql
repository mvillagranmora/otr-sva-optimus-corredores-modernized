SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.NotificarJob";
 
CREATE PROCEDURE
-- 
--  Proposito: Actualiza una plantilla para que sea usada en alguna notificación
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 04/03/2016
--   
"BSCL.Plataforma.BD.Servicios::EVT.NotificarJob"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
	                                IN p_IdJob 				BIGINT,
	                             
 									 
 									OUT p_Salida 			NVARCHAR(520)
    	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 

 
 		DECLARE countrow BIGINT;	
		DECLARE fechaSistema SECONDDATE;	
 	 
		 
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,fechaSistema);
		
		p_Eventos = 
			SELECT 'NOTJOB' AS "CodigoTipoEvento" , p_IdJob AS "IdSecuencia", fechaSistema AS "FechaCreacionSistema" ,1 as "IdPersonaRemitente" , 0 "IdPersonaDestinatario" from "BSCL.Plataforma.BD::BAT.Programa" WHERE "IdPrograma" = p_IdJob and "IdTipoPrograma" != 'NOTIFY';
		p_Valores =
			SELECT p_IdJob AS "IdSecuencia" ,"IdTipoPrograma" as "Llave" , 'Programa' as "Valor" 
		 		FROM "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = p_IdJob and "IdTipoPrograma" != 'NOTIFY'
		 UNION ALL
		  SELECT p_IdJob AS "IdSecuencia" ,"Resultado" as "Llave", 'Resultado' as "Valor" 
		 	from "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = p_IdJob and "IdTipoPrograma" != 'NOTIFY';
		
		p_Archivos = SELECT 0 as "IdSecuencia",'a' as "ArchivoAdjunto",'b' as "Nombre",'c' as "Contrasenia" FROM DUMMY;
		
		p_Direcciones = SELECT 0 as "IdSecuencia" ,'c' as "IdDireccion", 'x' as "TipoDestinatario" FROM DUMMY;
		
		
		SELECT 
			COUNT(1) INTO countrow FROM :p_Eventos;
			
		IF :countrow > 0 THEN
			
			
			CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(p_IdEmpresa,p_IdUsuario,p_IdModulo,:p_Eventos,:p_Valores,:p_Archivos,:p_Direcciones,:p_IdEventos);
			
			p_DetalleLog =
	 			SELECT '' as "SubRegistro", 'Notificar job' as "CampoRegistro", '' as "ValorAntiguo",  ''  as "ValorNuevo"			
	    		FROM dummy;
	
		    CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'INFORMATIVO' 
			,p_IdModulo => 'EVT' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			,p_FechaSistemaLog => CURRENT_DATE
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'NOTIFICAR TAREA ::: '
			,p_IdRegistro => p_IdJob
			,p_DetalleLog => :p_DetalleLog
			);
			
			p_Salida := 'NOTIFICAR TAREA :::'||p_IdJob;
	
		ELSE
			p_Salida := 'SIN TAREA A NOTIFICAR ';
		
		END IF;
			
		
 	 
		
	 
 	 	
	 	

	   
		  
		  
 
END;

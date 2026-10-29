SET SCHEMA BSCL;
GO 
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma";
GO
CREATE PROCEDURE
-- 
--  Proposito: Agrega Programa a tabla para la ejecucion batch
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
 (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	IN	p_IdAgenda	 		BIGINT, 
	IN	p_IdTipoTarea		NVARCHAR(40),   
	IN	p_Segundos			BIGINT,
	IN  p_TablaParametros table("NombreParametro" nvarchar(100) , "Valor" nvarchar(5000)) DEFAULT EMPTY,
	IN  p_CreaParam			NVARCHAR(1),
	OUT p_out_IdPrograma		table("IdPrograma" BIGINT),
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
		declare fechaSistemaUTC NVARCHAR(40);
		declare fechaSistemaLocal NVARCHAR(40);
		declare fechaHorasistemaUTC NVARCHAR(40); 
		declare fechaHorasistemaLOCAL NVARCHAR(40);
		declare fechaHoraUTC NVARCHAR(40);
		declare fechaHoraLocal NVARCHAR(40);
		declare v_Nodo NVARCHAR(1);
		declare v_ExisteAgenda INT;
		declare v_ExisteTipoTarea INT;
		declare v_Validacion INT;
		declare v_IdPrograma BIGINT;
		declare v_IdSecuencia BIGINT;
		declare v_fechaSistema DATE;
		DECLARE v_grupo_param BIGINT;
		declare v_fechaAProgramar seconddate;
		
		v_ExisteAgenda := -1;
		v_ExisteTipoTarea := -1;
		
		SELECT "Nodo" INTO v_Nodo from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = p_IdTipoTarea;
		SELECT "BSCL.Plataforma.BD.Secuencias::Programa".NEXTVAL into v_IdPrograma from dummy;
 		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(p_IdEmpresa,fechaSistemaUTC,fechaSistemaLocal,fechaHorasistemaUTC,fechaHorasistemaLOCAL,fechaHoraUTC,fechaHoraLocal);
		SELECT TO_DATE(to_char(SUBSTRING(fechaHorasistemaUTC,1,10),'YYYY-MM-DD'),'YYYY-MM-DD') into v_fechaSistema  FROM DUMMY;
		select count(1) into v_ExisteAgenda from "BSCL.Plataforma.BD::BAT.Agenda" where "IdAgenda" = p_IdAgenda;
		select count(1) into v_ExisteTipoTarea from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = p_IdTipoTarea;
	   
 	    IF :p_Segundos = 0 THEN
 	    	select CURRENT_UTCTIMESTAMP into v_fechaAProgramar from dummy;
 	    ELSE
 	    	select add_seconds(CURRENT_UTCTIMESTAMP,p_Segundos) into v_fechaAProgramar from dummy;
		END IF;
	
		IF :p_CreaParam = 'S' THEN
			IF :v_ExisteTipoTarea = 1 and :v_ExisteAgenda = 0 THEN
				INSERT INTO "BSCL.Plataforma.BD::BAT.Programa"("IdPrograma","IdAgenda","IdTipoPrograma","Intento","IdUsuario","idEstado","Resultado","fechaCreacion","fechaEjecucion","fechaSistema","Nodo")
				VALUES(v_IdPrograma,0,p_IdTipoTarea,0,p_IdUsuario,'PEN','Esperando ejecución',CURRENT_UTCTIMESTAMP,v_fechaAProgramar, v_fechaSistema,v_Nodo);
	   
				select "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma".nextval into v_grupo_param from dummy;
				
				insert into "BSCL.Plataforma.BD::BAT.ParametrosPrograma"("IdSecuencia","IdGrupoParametro","IdPrograma","Orden","NombreParametro","Valor")
				select "BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia".nextval,v_grupo_param,v_IdPrograma,1,"NombreParametro","Valor"
				from :p_TablaParametros;
				
				update "BSCL.Plataforma.BD::BAT.Programa" set "IdGrupoParametro" = v_grupo_param where "IdPrograma" = v_IdPrograma;
		
				p_out_IdPrograma = select v_IdPrograma as "IdPrograma" from dummy;
				--p_out_ErroresDetalle = select 'OK' as "Codigo",'OK' as "Descripcion" from dummy;
		
			END IF;
		
			IF :v_ExisteTipoTarea = 1 and :v_ExisteAgenda = 1 THEN
				INSERT INTO "BSCL.Plataforma.BD::BAT.Programa"("IdPrograma","IdAgenda","IdTipoPrograma","Intento","IdUsuario","idEstado","Resultado","fechaCreacion","fechaEjecucion","fechaSistema","Nodo")
				VALUES(v_IdPrograma,p_IdAgenda,p_IdTipoTarea,0,p_IdUsuario,'PEN','Esperando ejecución',fechaHoraUTC,v_fechaAProgramar,v_fechaSistema,v_Nodo);
       
        		select "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma".nextval into v_grupo_param from dummy;
        		select "BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia".nextval into v_IdSecuencia from dummy;
        
				insert into "BSCL.Plataforma.BD::BAT.ParametrosPrograma" ("IdSecuencia","IdGrupoParametro","IdPrograma","Orden","NombreParametro","Valor")
				select "BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia".nextval,v_grupo_param,v_IdPrograma,1,"NombreParametro","Valor"
				from :p_TablaParametros;
				
				update "BSCL.Plataforma.BD::BAT.Programa" set "IdGrupoParametro" = v_grupo_param where "IdPrograma" = v_IdPrograma;
		        update "BSCL.Plataforma.BD::BAT.Agenda" set "habilitado" = 'S' where "IdAgenda" = p_IdAgenda;
        
 				p_out_IdPrograma = select v_IdPrograma as "IdPrograma" from dummy;
			END IF;
		ELSE
			 INSERT INTO "BSCL.Plataforma.BD::BAT.Programa"("IdPrograma","IdAgenda","IdTipoPrograma","Intento","IdUsuario","idEstado","Resultado","fechaCreacion","fechaEjecucion","fechaSistema","Nodo")
			 VALUES(v_IdPrograma,0,p_IdTipoTarea,0,p_IdUsuario,'PEN','Esperando ejecución',fechaHoraUTC,v_fechaAProgramar,v_fechaSistema,v_Nodo);
			 p_out_IdPrograma = select v_IdPrograma as "IdPrograma" from dummy;
		END IF;
	
		IF :v_ExisteTipoTarea = 0 THEN
			p_out_ErroresContador := 1;
			p_out_ErroresDetalle = SELECT '001NETP' as "Codigo", 'No existe tipo tarea relacionada' as"Descripcion" from dummy;
			p_out_IdPrograma = select -1 as "IdPrograma" from dummy;
		END IF;
		
	END;
	 

END;

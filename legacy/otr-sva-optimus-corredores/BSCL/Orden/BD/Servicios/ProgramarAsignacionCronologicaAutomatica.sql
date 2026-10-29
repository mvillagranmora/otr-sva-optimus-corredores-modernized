SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ProgramarAsignacionCronologicaAutomatica";
				

CREATE PROCEDURE
-- 
--  Proposito: Programa Asignación Cronológica OnDemand
--  Autor: Exequiel Espinoza  
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::ProgramarAsignacionCronologicaAutomatica" (
	-- Parametros entrada del servicio publico
	 
									IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
									IN p_IdModulo           NVARCHAR(3),
									
									IN 	p_IdLibroOrdenes		BIGINT,
									
									OUT p_out_salida		NVARCHAR(80),
									OUT p_out_IdPrograma	NVARCHAR(80),
									OUT p_out_AdvertenciasContador int,
									OUT p_out_AdvertenciasDetalle table ( "Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
									OUT p_out_ErroresContador int,
									OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	 
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
  		declare v_out_FechaSistemaUTC   	NVARCHAR(40);
		declare v_out_FechaSistemaLOCAL   	NVARCHAR(40); 
		declare v_out_FechaHoraSistemaUTC   NVARCHAR(40); 
		declare v_out_FechaHoraSistemaLOCAL NVARCHAR(40); 
		declare v_out_FechaHoraUTC   		NVARCHAR(40); 
		declare v_out_FechaHoraLOCAL   		NVARCHAR(40);
		declare v_IdEmpresa  BIGINT;
		declare v_IdUsuario  BIGINT;
		declare v_IdModulo   NVARCHAR(3);
		declare v_ejecucion 	TABLE(contador BIGINT);
		declare v_ejec 		BIGINT;
		declare v_existe 	 BIGINT;
		declare v_llamar	 BIGINT;
		declare v_contador BIGINT;
		declare v_IdAgenda BIGINT;
		declare v_grupo_param_agenda BIGINT;
 		declare v_NombreLibro NVARCHAR(80);
		declare v_grupo_param BIGINT;
		declare v_TablaParametros table("NombreParametro" NVARCHAR(100), "Valor" NVARCHAR(100));
		declare v_AsignacionActiva BIGINT;
		declare out_no_ocupable table("IdPrograma" BIGINT);
		declare v_IdPrograma BIGINT;
	
		 v_AsignacionActiva := 0;
	
		v_existe := -1; 
		
	 	v_grupo_param_agenda :=0;
	 	
	 	
	 	select count(1) into v_AsignacionActiva from "BSCL.Orden.BD::ORD.LibroOrdenes" where "IdLibroOrdenes" = p_IdLibroOrdenes and "IdEstado" = 'ACT' ;
	 	select "NombreLibroOrdenes" INTO v_NombreLibro from "BSCL.Orden.BD::ORD.LibroOrdenes" where "IdLibroOrdenes" = p_IdLibroOrdenes;
	 	select "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma".nextval into v_grupo_param_agenda from dummy;
 		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(p_IdEmpresa,v_out_FechaSistemaUTC,v_out_FechaSistemaLOCAL,v_out_FechaHoraSistemaUTC,v_out_FechaHoraSistemaLOCAL,v_out_FechaHoraUTC,v_out_FechaHoraLOCAL);
 		
		
		select count(1) into v_contador from "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas" 
		where  "Valor" = p_IdLibroOrdenes and 
		"IdTipoPrograma" = 'ASIG_CRONO' and 
		"NombreParametro" = 'IdLibroOrdenes' and ("idEstado" = 'PEN' or "idEstado" = 'ASI');
		 
		 
		IF :v_AsignacionActiva = 1 THEN
		
			
	
		     select count(1) into v_AsignacionActiva from "BSCL.Orden.BD::ORD.LibroOrdenes" where "IdTipoAsignacion" = 'ASIGNACION_CRONOLOGICA' and "IdLibroOrdenes" = p_IdLibroOrdenes and "AsignacionAutomatica" = 'S'   and "IdEstado" = 'ACT';
		   IF :v_AsignacionActiva = 1 THEN
		   	 p_out_ErroresContador		:= 1;
		     p_out_salida:= 'Existe asignación cronológica automatica activa';
		   ELSE
			IF :v_contador = 0 THEN
			v_TablaParametros = SELECT 'IdLibroOrdenes' as "NombreParametro", p_IdLibroOrdenes as "Valor" from dummy; 
	 		call "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(
				p_IdEmpresa   
				,p_IdUsuario
				,p_IdModulo 
				,0
				,'ASIG_CRONO'
				,0
				, :v_TablaParametros
				,'S'
				,out_no_ocupable
				,p_out_AdvertenciasContador
				,p_out_AdvertenciasDetalle
				,p_out_ErroresContador
				,p_out_ErroresDetalle
				);
			
			
			 select "IdPrograma" into v_IdPrograma from :out_no_ocupable;
 			 p_out_IdPrograma := v_IdPrograma;
		     p_out_salida:=  'Se programó Asignación Aut        omática : ' || p_out_IdPrograma  || '  '  ;
		 ELSE
			 p_out_ErroresContador		:= 1;
		     p_out_salida:= 'Existe ejecución activa para la asignación crónologica';
		 END IF;
		 END IF;

		ELSE 
			 p_out_ErroresContador		:= 1;
		     p_out_salida:= 'Libro desactivado';
		END IF;
		 
		 
END; 

END;
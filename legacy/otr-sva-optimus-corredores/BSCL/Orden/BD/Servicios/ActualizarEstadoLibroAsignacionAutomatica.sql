SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ActualizarEstadoLibroAsignacionAutomatica";

CREATE PROCEDURE
-- 
--  Proposito: Actualiza estado asignacion automatica 
--  Autor: Exequiel Espinoza  
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::ActualizarEstadoLibroAsignacionAutomatica" (
	-- Parametros entrada del servicio publico
	 
									IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
									IN p_IdModulo           NVARCHAR(3),
									
									IN 	p_IdLibroOrdenes		BIGINT,
									
									OUT p_out_salida		NVARCHAR(200),
									OUT p_out_AdvertenciasContador int,
									OUT p_out_AdvertenciasDetalle table (  "Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
									OUT p_out_ErroresContador int,
									OUT p_out_ErroresDetalle table ( "Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	 
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
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;


 
  BEGIN
 
		declare v_existe 	 BIGINT;
		declare v_llamar	 BIGINT;
		declare v_IdAgenda BIGINT;
		declare v_grupo_param_agenda BIGINT;
		declare v_IdPrograma BIGINT;
		declare v_grupo_param BIGINT;
		declare v_IdProgramaTable table( "IdPrograma" BIGINT);
		declare v_TablaParametrosPrograma table( "NombreParametro" nvarchar(100), "Valor" nvarchar(100));
		declare v_TablaParametrosAgenda table( "NombreParametro" nvarchar(100), "Valor" nvarchar(100));
		declare v_contadorAgenda BIGINT;
		declare v_AgendaId BIGINT;
 
		v_existe := -1;
		
	 	select count(1) into v_contadorAgenda from "BSCL.Plataforma.BD::BAT.Agenda" ag 
		LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosAgenda" pag
		ON ag."IdAgenda" = pag."IdAgenda" 
		where "NombreParametro" = 'IdLibroOrdenes' and "Valor" = p_IdLibroOrdenes;
		
		select "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma".nextval into v_grupo_param from dummy;
 		select count(1) into v_existe from "BSCL.Orden.BD::ORD.LibroOrdenes" where "IdLibroOrdenes" = p_IdLibroOrdenes and "IdEstado" = 'ACT';
	 
		
		
		 
		IF :v_existe = 1 THEN 
						select count(1) into v_llamar from "BSCL.Plataforma.BD::BAT.Agenda" "agenda"
							LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosAgenda" "param"
							ON "param"."IdAgenda" = "agenda"."IdAgenda"
							LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" "conf"
							ON p_IdLibroOrdenes = "conf"."IdLibroOrdenes"
							where "param"."NombreParametro" = 'IdLibroOrdenes'
							and "param"."Valor" = "conf"."IdLibroOrdenes"  ;
		     
		   
		     
		     
		   	 
		END IF;
		if :v_llamar = 0 THEN
		
		v_TablaParametrosAgenda = select 'IdLibroOrdenes' as "NombreParametro", p_IdLibroOrdenes as "Valor" from dummy;
		
		call "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
		p_IdEmpresa
		,p_IdUsuario
		,p_IdModulo 
		,'ASIG_CRONO_AUTOMATIC'
		,'N'
		,'**'
		,'*'
		,'**'
		,'**'
		,'15'
		,'N'
		,'N'
		,'N'
		,'S'
		,:v_TablaParametrosAgenda
 		,v_IdAgenda
		,v_IdPrograma
		,p_out_AdvertenciasContador
		,p_out_AdvertenciasDetalle
		,p_out_ErroresContador
		,p_out_ErroresDetalle);
		
		
				
		
		update "BSCL.Orden.BD::ORD.LibroOrdenes" set "IdEstado" = 'ACT' where "IdLibroOrdenes" = p_IdLibroOrdenes;
		p_out_salida := 'Se activo libro para asignación cronologica ';
		END IF;
		
		IF :v_llamar = 1 THEN
		
		select  distinct "agenda"."IdAgenda" into v_IdAgenda from "BSCL.Plataforma.BD::BAT.Agenda" "agenda"
							LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosAgenda" "param"
							ON "param"."IdAgenda" = "agenda"."IdAgenda"
							LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" "conf"
							ON p_IdLibroOrdenes = "conf"."IdLibroOrdenes"
							LEFT JOIN "BSCL.Plataforma.BD::BAT.Programa" "programa"
							ON "programa"."IdAgenda" = "agenda"."IdAgenda"
							where "param"."NombreParametro" = 'IdLibroOrdenes' 
							and p_IdLibroOrdenes = "param"."Valor"  ;
		 
		select  count("agenda"."IdAgenda") into v_llamar from "BSCL.Plataforma.BD::BAT.Agenda" "agenda"
							LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosAgenda" "param"
							ON "param"."IdAgenda" = "agenda"."IdAgenda"
							LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" "conf"
							ON p_IdLibroOrdenes = "conf"."IdLibroOrdenes"
							LEFT JOIN "BSCL.Plataforma.BD::BAT.Programa" "programa"
							ON "programa"."IdAgenda" = "agenda"."IdAgenda"
							where "param"."NombreParametro" = 'IdLibroOrdenes' and "programa"."idEstado" =  'PEN'
							and p_IdLibroOrdenes = "param"."Valor"  ;
						
		if :v_IdAgenda > 0 and v_llamar = 0 THEN 
				v_TablaParametrosPrograma = select 'IdLibroOrdenes' as "NombreParametro", p_IdLibroOrdenes as "Valor" from dummy;
		
		call "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(
			p_IdEmpresa
			,p_IdUsuario
			,p_IdModulo 
			,v_IdAgenda
			,'ASIG_CRONO_AUTOMATIC'
			,0
			,:v_TablaParametrosPrograma
 			,'S'
 			,v_IdProgramaTable
			,p_out_AdvertenciasContador
			,p_out_AdvertenciasDetalle
			,p_out_ErroresContador
			,p_out_ErroresDetalle

			);
 
		END IF;

	

		update "BSCL.Orden.BD::ORD.LibroOrdenes" set "IdEstado" = 'ACT' where "IdLibroOrdenes" = p_IdLibroOrdenes;

		p_out_salida := 'Se activo libro para asignación cronologica ' || p_IdLibroOrdenes;
	

 		END if;
		
		IF :v_contadorAgenda = 1 THEN
			select ag."IdAgenda" into v_AgendaId from "BSCL.Plataforma.BD::BAT.Agenda" ag 
			LEFT JOIN "BSCL.Plataforma.BD::BAT.ParametrosAgenda" pag
			ON ag."IdAgenda" = pag."IdAgenda" 
			where "NombreParametro" = 'IdLibroOrdenes' and "Valor" = p_IdLibroOrdenes;
			
			update "BSCL.Orden.BD::ORD.LibroOrdenes" set "IdEstado" = 'ACT' where "IdLibroOrdenes" = p_IdLibroOrdenes;
			call "BSCL.Plataforma.BD.Servicios::BAT.ActualizarAgenda"(p_IdEmpresa,p_IdUsuario,p_IdModulo, v_AgendaId, 'H', p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);	
			p_out_salida := 'Se activo libro para asignación cronologica ' || p_IdLibroOrdenes;
			
		END IF;
		
 		select count(1) into v_existe from "BSCL.Orden.BD::ORD.LibroOrdenes" where "IdLibroOrdenes" = p_IdLibroOrdenes and "IdEstado" = 'INA';

 		IF :v_existe = 1 THEN

 		  p_out_ErroresContador:= -1;

 		  p_out_salida := 'No puede activar asignación automatica, el libro está inactivo';

 		END IF;

 	

 	

 		

  	

 	

END;


END
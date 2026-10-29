SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Orden.BD.Servicios::DesactivarAsignacionAutomaticaLibro";

CREATE PROCEDURE
-- 
--  Proposito: Desactiva Asignacion Automatica por libro 
--  Autor: Exequiel Espinoza  
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Orden.BD.Servicios::DesactivarAsignacionAutomaticaLibro" (
	-- Parametros entrada del servicio publico
	IN p_IdEmpresa                      BIGINT,
	IN p_IdUsuario                      BIGINT,
	IN p_IdModulo                       NVARCHAR(3),
	
	IN p_IdLibroOrdenes					BIGINT,
	
	OUT p_mensaje_salida 				NVARCHAR(200),
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
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

		declare v_existe BIGINT;
		declare v_IdAgenda BIGINT;
		declare v_IdGrupoParametro BIGINT;
		DECLARE v_estaDesactivado BIGINT;
		DECLARE v_Ahora seconddate;
		DECLARE v_IdPrograma BIGINT;
		DECLARE v_existeAgenda BIGINT;
		declare v_IdEmpresa  BIGINT;
		declare v_IdUsuario  NVARCHAR(40);
		declare v_IdModulo   NVARCHAR(3);
		
 
		
		select COUNT("IdAgenda") into  v_existeAgenda from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" where "NombreParametro" = 'IdLibroOrdenes' and "Valor" = p_IdLibroOrdenes; 
		
	 
		
		select CURRENT_UTCTIMESTAMP into v_Ahora from dummy;
		IF :v_existeAgenda > 0 THEN
		
		  select "IdAgenda" into  v_IdAgenda from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" where "NombreParametro" = 'IdLibroOrdenes' and "Valor" = p_IdLibroOrdenes; 
		
		END IF;
		
		 
		select count(1) into v_existe from "BSCL.Orden.BD::ORD.LibroOrdenes" where "IdLibroOrdenes" = p_IdLibroOrdenes AND "IdEstado" = 'ACT';
		
		IF :v_existe = 1 THEN 
		  IF :v_IdAgenda > 0  THEN
		  
		    update "BSCL.Orden.BD::ORD.LibroOrdenes" set "IdEstado" = 'INA' where "IdLibroOrdenes" = p_IdLibroOrdenes;
		    call "BSCL.Plataforma.BD.Servicios::BAT.ActualizarAgenda"(p_IdEmpresa,p_IdUsuario,p_IdModulo,v_IdAgenda,'D',p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);
		    		    select count("IdPrograma") into v_IdPrograma from "BSCL.Plataforma.BD::BAT.Programa" where "IdAgenda" = v_IdAgenda and "idEstado" = 'PEN'  and "fechaEjecucion" < v_Ahora ;
		    IF :v_IdPrograma > 0 THEN
		    select "IdPrograma" into v_IdPrograma from "BSCL.Plataforma.BD::BAT.Programa" where "IdAgenda" = v_IdAgenda and "idEstado" = 'PEN'  and "fechaEjecucion" < v_Ahora ;
		   
		   call "BSCL.Plataforma.BD.Servicios::BAT.CancelarPrograma"(p_IdEmpresa,p_IdUsuario,p_IdModulo,v_IdAgenda,p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);
		    END IF;
    		 p_mensaje_salida := 'Libro desactivado ' || p_IdLibroOrdenes;
    		 
		  END IF;
		  
    	ELSE
    	  p_mensaje_salida := 'Libro se encuentra desactivado  ' ||  p_IdLibroOrdenes;
		END IF;
		select count(1) into v_existe from "BSCL.Orden.BD::ORD.LibroOrdenes" where "IdLibroOrdenes" = p_IdLibroOrdenes and "IdEstado" = 'INA';
 		IF :v_existe = 1 THEN
 		  p_mensaje_salida := 'No puede desactivar asignación automatica, el libro está inactivo';
		  p_out_ErroresContador:= -1;
 		END IF;
		
		
 END;
END;

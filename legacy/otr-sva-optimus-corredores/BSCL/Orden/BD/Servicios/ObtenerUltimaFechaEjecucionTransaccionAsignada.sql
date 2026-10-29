SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ObtenerUltimaFechaEjecucionTransaccionAsignada";
CREATE PROCEDURE
-- 
--  Proposito: Obtiene la ultima fecha de asignación de la transaccion y asignacion 
--   y evalua si debe ejecutar la asignación cronológica
--  Autor:    
--  Fecha Creacion: 20/07/2015 
--  
"BSCL.Orden.BD.Servicios::ObtenerUltimaFechaEjecucionTransaccionAsignada" ( 
    --VARIABLES DE ENTRADA OBLIGATORIAS 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),
	
	IN p_IdLibroOrdenes 					 BIGINT,
 
	OUT p_fechaModifcacion			TABLE ("FechaModificacionTransaccion" NVARCHAR(19),"contador" BIGINT ),
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
    
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
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
	p_out_asignaciones 			= SELECT 0 as "IdPrograma", 0 as "IdAsignacion" from dummy where 1=2; 
	
	BEGIN
	
		declare fechaActual NVARCHAR(40);
		declare fechaInic NVARCHAR(40);
		declare fechaSistemaUTC NVARCHAR(40); 
		declare fechaSistemaLocal NVARCHAR(40);
		declare fechaHoraUTC NVARCHAR(40);
		declare fechaHoraLocal NVARCHAR(40);
		DECLARE v_FechaUltimaFechaDeTransaccionAsignada  NVARCHAR(40);
		DECLARE v_FechaTransaccion NVARCHAR(19);
		DECLARE v_FechaModificacionTransaccion NVARCHAR(19);
		DECLARE v_fechaUltimaEjecucionLibro NVARCHAR(19);
		DECLARE v_fechaMaxima NVARCHAR(19);
		DECLARE contador bigint;
		DECLARE contadorAsig bigint;
		declare fechaDate NVARCHAR(10);
		declare fechaTime NVARCHAR(10);
		
		contador := -1;
		
	 
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(1,fechaInic,fechaActual,fechaSistemaUTC,fechaSistemaLocal,fechaHoraUTC,fechaHoraLocal);
		--Última fecha de transacción modificada
		SELECT COUNT("FechaOperacion") into contador from "BSCL.Orden.BD::ORD.Transaccion" where "IdLibroOrdenes" = p_IdLibroOrdenes
		and "FechaModificacionOperacion" between (to_seconddate(TO_DATE(fechaSistemaUTC) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
		and (to_seconddate(fechaSistemaUTC, 'YYYY-MM-DD HH24:MI:SS')) and "IdEstado" != 'ANU' and "Bloqueado" = 'N' and "Reservado" ='N';
 		 
		 
		IF :contador > 0  THEN
		SELECT MAX("FechaModificacionOperacion") into v_fechaMaxima from "BSCL.Orden.BD::ORD.Transaccion" where "IdLibroOrdenes" = p_IdLibroOrdenes and "IdEstado" != 'ANU' and "Bloqueado" = 'N' and "Reservado" ='N' ;
		
		SELECT "FechaOperacion", "FechaModificacionOperacion" into v_FechaTransaccion,v_FechaModificacionTransaccion from "BSCL.Orden.BD::ORD.Transaccion" where "IdLibroOrdenes" = p_IdLibroOrdenes
		and "FechaModificacionOperacion" between (to_seconddate(TO_DATE(fechaSistemaLocal) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
		and (to_seconddate(v_fechaMaxima, 'YYYY-MM-DD HH24:MI:SS'))
		 ORDER BY "FechaModificacionOperacion" DESC LIMIT 1;
		END IF;
		 
		
		select count(1) INTO contador from "BSCL.Orden.BD::ORD.Asignacion" 
				where "FechaIngresoRegistro" between 
				(to_seconddate(TO_DATE(fechaHoraUTC) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
				and (to_seconddate(TO_DATE(fechaHoraUTC) || ' ' || '23:59:59', 'YYYY-MM-DD HH24:MI:SS'))
				and "IdLibroOrdenes" = p_IdLibroOrdenes;
		
		IF :contador = 0 THEN 
			--Primera ejecución del día
			p_fechaModifcacion = select fechaSistemaLocal as "FechaModificacionTransaccion", 0 as "contador" from dummy;
		ELSE 
		Select "UltimaFechaDeEjecucion" into v_fechaUltimaEjecucionLibro from "BSCL.Orden.BD::ORD.LibroOrdenes"
				where "IdLibroOrdenes" = p_IdLibroOrdenes;
								
			IF  to_seconddate(:v_FechaModificacionTransaccion , 'YYYY-MM-DD HH:MI:SS') > to_seconddate(:v_fechaUltimaEjecucionLibro , 'YYYY-MM-DD HH:MI:SS') THEN
			--Preparar datos con fecha
			p_fechaModifcacion = select v_FechaTransaccion  as "FechaModificacionTransaccion", 1 as "contador" from dummy;
		
			ELSE
			p_fechaModifcacion = select v_FechaModificacionTransaccion || '  ' || v_fechaUltimaEjecucionLibro   as "FechaModificacionTransaccion", -1 as "contador" from dummy;
			END IF;
		
		
		END IF;
		
	
	 
		
		
		/**
		
		IF :contador > 0 THEN
		SELECT transaccion."FechaOperacion" into v_FechaUltimaFechaDeTransaccionAsignada from "BSCL.Orden.BD::ORD.Transaccion" transaccion
		LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" asignacion
		ON transaccion."IdTransaccion" = asignacion."IdTransaccion"
		where transaccion."IdLibroOrdenes" = asignacion."IdLibroOrdenes" and
		asignacion."IdLibroOrdenes" = p_IdLibroOrdenes;
			 SELECT "FechaOperacion" into v_FechaTransaccion from "BSCL.Orden.BD::ORD.Transaccion" where "IdLibroOrdenes" = p_IdLibroOrdenes ORDER BY "FechaModificacionOperacion" DESC LIMIT 1;
		
				IF to_seconddate(:v_FechaTransaccion , 'YYYY-MM-DD HH:MI:SS') < to_seconddate(:v_FechaUltimaFechaDeEjecucion , 'YYYY-MM-DD HH:MI:SS') THEN
				
				p_fechaModifcacion = select v_FechaTransaccion as "FechaModificacionTransaccion", 1 as "contador" from dummy;
				ELSE
			 		p_fechaModifcacion = select v_FechaTransaccion as "FechaModificacionTransaccion", -1 as "contador" from dummy;
			    END IF;
		ELSE
			
				 p_fechaModifcacion = select '' as "FechaModificacionTransaccion", 0 as "contador" from dummy;  --Primera Ejecución
				
				
		END IF;
		*/
	   
		
 
		
	END;
END;
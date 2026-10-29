SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Orden.BD.Servicios::PrepararAsignacionCronologica";

CREATE PROCEDURE
-- 
--  Proposito: Prepara datos para la asignación cronológica
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Orden.BD.Servicios::PrepararAsignacionCronologica" (
IN p_IdEmpresa                        BIGINT,
IN p_IdUsuario                        BIGINT,
IN p_IdModulo                         NVARCHAR(3),
in p_IdPrograma BIGINT,
in p_IdLibroOrdenes BIGINT,

	-- Parametros entrada del servicio publico
 	
	
OUT p_out_cantidadResultantes	TABLE (
		"CantidadOrdenes"					BIGINT
		,"TransaccionOrdenes"				BIGINT
		,"Asignaciones"						BIGINT
		,"IdPrograma"						BIGINT
	 	,"Fecha"							NVARCHAR(20)
		), 
		
 
		
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdPrograma" BIGINT, "Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdPrograma" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520))
    
	-- Salidas para mensajes de advertencias y errores
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
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
	p_out_AdvertenciasDetalle	= select 0 as "IdPrograma", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 as "IdPrograma",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_asignaciones 			= SELECT 0 as "IdPrograma", 0 as "IdAsignacion" from dummy where 1=2; 
	
BEGIN 

		declare fechaActual NVARCHAR(40);
		declare fechaInic NVARCHAR(40);
		declare fechaSistemaUTC NVARCHAR(40); 
		declare fechaSistemaLocal NVARCHAR(40); 
		declare fechaHoraUTC NVARCHAR(40);
		declare fechaHoraLocal NVARCHAR(40);
		declare v_IdBolsa NVARCHAR(3);
		declare v_Canal NVARCHAR(3);
		declare v_contadorSalidaOrdenes BIGINT;
		declare v_contadorSalidaTransacciones BIGINT;
		declare v_contadorSalidaAsignaciones BIGINT;
		
		

		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(1,fechaInic,fechaActual,fechaSistemaUTC,fechaSistemaLocal,fechaHoraUTC,fechaHoraLocal);

	
 		UPDATE "BSCL.Orden.BD::ORD.LibroOrdenes" SET "UltimaFechaDeEjecucion" =  fechaHoraUTC where "IdLibroOrdenes" = p_IdLibroOrdenes;
		
		INSERT INTO "BSCL.Orden.BD::ORD.OrdenPaso"
		SELECT  
		p_IdPrograma,
		"O"."IdOrden",
		"O"."IdClasificacionTipoInstrumento",
		"O"."IdTipoInstrumento",
		"O"."IdBolsa",
		"O"."IdTipoOperacion",
		"O"."IdCondicionDeLiquidacion",
		"O"."PrecioOrden",
		"O"."CantidadDisponibleOrden",
		"O"."CantidadAsignadaOrden",
		"O"."MontoDisponibleOrden" as "Monto",
		"O"."IdTipoCanalDistribucion",
		"O"."FechaIngresoRegistro",
		"O"."FechaOperacionOrden",
		"O"."IdLibroOrdenes",
		"O"."IdTipoPrecio",
		"O"."IdTipoCanalIngreso",
		"O"."EspecificaBolsa",
		"O"."IdInstrumento"
		from "BSCL.Orden.BD.Vistas::ConsultarOrden" "O" 
		where "O"."IdTipoCanalDistribucion" != 'RUA' and "OrdenVigente" = 'S' and "Reservado" = 'N'
		 and "Bloqueado" = 'N' and "IdEstadoOrden" = 'OFE' AND "IdTipoInstrumento" != 'SIM' AND "IdClasificacionTipoInstrumento" = 'RV' and "EsVentaCorta" = 'N' AND 
		  "O"."IdLibroOrdenes" = p_IdLibroOrdenes ;
		--and	"O"."FechaOperacion" between 
		-- No es por FechaOperacion, si no por Ordenes Vigentes.
		--(to_seconddate(TO_DATE('2015-09-22') || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
		--and (to_seconddate(TO_DATE('2015-09-22') || ' ' || '23:59:59', 'YYYY-MM-DD HH24:MI:SS'));
		--Agregar ordenes Vigentes
		--(to_seconddate(TO_DATE(fechaSistemaLocal) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
		--and (to_seconddate(fechaSistemaLocal, 'YYYY-MM-DD HH24:MI:SS'));
	 

	

		
 	INSERT INTO "BSCL.Orden.BD::ORD.TransaccionPaso"
	SELECT DISTINCT
		p_IdPrograma,
		"T"."IdTransaccion",
		"T"."IdClasificacionTipoInstrumento",
		"T"."IdTipoInstrumento",
		"T"."IdBolsa",
		"T"."IdTipoOperacion",
		"T"."IdCondicionDeLiquidacion",
		"T"."PrecioTransaccion",
		"T"."CantidadTransaccion",
		"T"."CantidadDisponibleTransaccion",
		"T"."CantidadAsignadaTransaccion",
		"T"."MontoDisponibleTransaccion",
 		"T"."FechaSistemaCreacion",
		"T"."FechaOperacionTransaccion",
		"T"."IdLibroOrdenes",
		"T"."IdInstrumento"
	 from "BSCL.Orden.BD.Vistas::ConsultarTransaccion" "T"
	where "T"."IdLibroOrdenes" = p_IdLibroOrdenes and "IdEstadoTransaccion" != 'ANU' and "Bloqueado" = 'N' and "Reservado" ='N'  AND "IdTipoInstrumento" != 'SIM' 
	AND "IdClasificacionTipoInstrumento" = 'RV' and "EsVentaCorta" = 'N'
		and	"T"."FechaOperacionTransaccion" between 
	--	(to_seconddate(TO_DATE('2015-09-22') || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
	--	and (to_seconddate(TO_DATE('2015-09-22') || ' ' || '23:59:59', 'YYYY-MM-DD HH24:MI:SS'));		
		(to_seconddate(TO_DATE(fechaSistemaUTC) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
		and (to_seconddate(fechaSistemaUTC, 'YYYY-MM-DD HH24:MI:SS'));

		/*
		CREATE LOCAL TEMPORARY TABLE #transaccion_in ("IdTransaccion" BIGINT NOT NULL);
		insert into #transaccion_in("IdTransaccion") SELECT
		"IdTransaccionPaso"
		from "BSCL.Orden.BD::ORD.TransaccionPaso"; */
 		INSERT INTO "BSCL.Orden.BD::ORD.AsignacionPaso"
		(			 
					"IdAsignacionPaso" 	 ,
					 "IdEstadoFacturacion" 		 ,
					 "CantidadAsignada" 		 ,
					 "FechaAsignacion" 			 ,
					 "IdTransaccionPaso" 		 , 
					 "IdOrdenPaso" 				 ,
					 "IdPrograma"
					  				
		)
		SELECT
		
 			"A"."IdAsignacion",
			"A"."IdEstadoAsignacion",
			"A"."CantidadAsignada",
			"A"."FechaAsignacion",
			"A"."IdTransaccion",
			"A"."IdOrden",
			p_IdPrograma
			
			from "BSCL.Orden.BD.Vistas::ConsultarAsignacion" "A"
			LEFT JOIN "BSCL.Orden.BD::ORD.OrdenPaso" op
			ON "IdOrdenPaso" = "IdOrden" 
			LEFT JOIN "BSCL.Orden.BD::ORD.TransaccionPaso" tp
			ON "IdTransaccionPaso" = "IdTransaccion"
			where 		
			 "A"."IdEstadoAsignacion" = 'NEF' 
			and "A"."FechaAsignacion" 
			between 
			--Asignaciones del día
			(to_seconddate(TO_DATE(fechaSistemaLocal) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
			and (to_seconddate(fechaSistemaLocal, 'YYYY-MM-DD HH24:MI:SS'))
			and "A"."IdLibroOrdenes" = p_IdLibroOrdenes
			and op."IdPrograma" = p_IdPrograma 
			and tp."IdPrograma" = p_IdPrograma; 
			
			--(to_seconddate(TO_DATE(fechaSistemaLocal) || ' ' || '00:00:00', 'YYYY-MM-DD HH24:MI:SS'))
			--and (to_seconddate(fechaSistemaLocal, 'YYYY-MM-DD HH24:MI:SS'));
			
	
	/*CREATE LOCAL TEMPORARY TABLE #asignacion_in ("IdAsignacion" BIGINT NOT NULL);
		insert into #asignacion_in("IdAsignacion") SELECT
		"IdAsignacionPaso"
		from "BSCL.Orden.BD::ORD.AsignacionPaso"; */
	/*
	update "BSCL.Orden.BD::ORD.TransaccionPaso" 
    set "CantidadDisponible" = ("BSCL.Orden.BD::ORD.TransaccionPaso"."CantidadDisponible" - "A"."CantidadAsignada"),
    "CantidadAsignada" = ("BSCL.Orden.BD::ORD.TransaccionPaso"."CantidadAsignada"+"A"."CantidadAsignada")
    from "BSCL.Orden.BD::ORD.TransaccionPaso"
    inner join "BSCL.Orden.BD::ORD.AsignacionPaso" "A"
    on "A"."IdTransaccionPaso" = "BSCL.Orden.BD::ORD.TransaccionPaso"."IdTransaccionPaso"
    where "A"."IdEstadoFacturacion" = 'AEF';
    
    update"BSCL.Orden.BD::ORD.OrdenPaso" 
    set "CantidadDisponible" = ("BSCL.Orden.BD::ORD.OrdenPaso"."CantidadDisponible" - "A"."CantidadAsignada"),
    "CantidadAsignada" = ( "BSCL.Orden.BD::ORD.OrdenPaso"."CantidadAsignada"+"A"."CantidadAsignada")
    from "BSCL.Orden.BD::ORD.OrdenPaso"
    inner join "BSCL.Orden.BD::ORD.AsignacionPaso" "A"
    on "A"."IdOrdenPaso" = "BSCL.Orden.BD::ORD.OrdenPaso"."IdOrdenPaso"
    where "A"."IdEstadoFacturacion" = 'AEF'; */


	--delete from "BSCL.Orden.BD::ORD.AsignacionPaso";
	 	 
		select count(1) into v_contadorSalidaOrdenes  from "BSCL.Orden.BD::ORD.OrdenPaso" where "IdPrograma" = p_IdPrograma;
	 	select count(1) into v_contadorSalidaTransacciones  from "BSCL.Orden.BD::ORD.TransaccionPaso" where "IdPrograma" = p_IdPrograma;
	 	select count(1) into v_contadorSalidaAsignaciones  from "BSCL.Orden.BD::ORD.AsignacionPaso" where "IdPrograma" = p_IdPrograma;
	  
	 	p_out_cantidadResultantes = 
	 	select 
	 	 	v_contadorSalidaOrdenes as "CantidadOrdenes"
	 	 	, v_contadorSalidaTransacciones as "TransaccionOrdenes"
	 	 	, p_IdLibroOrdenes as "Asignaciones"
	 	 	, p_IdPrograma as "IdPrograma" 
	 	 	, fechaSistemaUTC as "Fecha" from dummy;


END;
END;

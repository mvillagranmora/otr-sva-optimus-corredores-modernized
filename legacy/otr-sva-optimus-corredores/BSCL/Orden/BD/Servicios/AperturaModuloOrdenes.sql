SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::AperturaModuloOrdenes";

CREATE PROCEDURE
-- 
--  Proposito: Validar que los requisitos de apertura se encuentren OK.
--  Autor: CHERNANDEZ
--  Fecha Creacion: 13/10/2016
--  
"BSCL.Orden.BD.Servicios::AperturaModuloOrdenes" ( 
    IN p_IdEmpresa                        BIGINT,
    IN p_IdUsuario                        BIGINT,
    IN p_IdModulo                         NVARCHAR(3),
    
    OUT p_out_CARGATRANSACCIONES		NVARCHAR(30),
    OUT p_out_CAMBIOFECHA				NVARCHAR(30),    
    OUT p_out_RUTEOAUTOMATICO			NVARCHAR(30),    
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador int,
	OUT p_out_InformacionDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	)
	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE v_mensajes "BSCL.Orden.BD.Tipos::SalidaDetalle";

	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
	
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_mensajes,
	
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select 0 AS "IdInterno" ,0 AS "IdOperacion", v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_InformacionContador	:= 0;
	p_out_InformacionDetalle	= select 0 AS "IdInterno" ,0 AS "IdOperacion", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_AdvertenciasDetalle	= select 0 AS "IdInterno" ,0 AS "IdOperacion", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 AS "IdInterno" ,0 AS "IdOperacion", '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	v_mensajes = SELECT 0 as "IdInterno",0 as "IdOperacion",'' AS "Resultado",'' AS "Codigo",'' as "CodigoDato1", '' as "ValorDato1" ,'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY WHERE 1=2;
	
	BEGIN
		--------------------
		--Validaciones
		--------------------
		
		
	END;
	
	BEGIN
		--------------------
		--Variables
		--------------------
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		
		DECLARE v_fechaSistema NVARCHAR(10);
		DECLARE v_CARGATRANSACCIONES BIGINT DEFAULT 0;
		DECLARE v_CAMBIOFECHA BIGINT DEFAULT 0;
		DECLARE v_RUTEOAUTOMATICO BIGINT DEFAULT 0;	
		DECLARE v_contadorOperacionesOK INT DEFAULT 0;	
		--------------------
		--Logica de Negocio
		--------------------					
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(
			p_IdEmpresa => p_IdEmpresa, 
			p_out_FechaSistema => v_fechaSistema
		);
		--Averiguar si la carga de palos se esta ejecutando
		select COUNT("IdTipoPrograma") INTO v_CARGATRANSACCIONES from "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma" 
		WHERE "IdTipoPrograma" = 'CARGATRANSACCIONES' AND "IdEstado" ='ASI' AND "fechaEjecucion" BETWEEN ("fechaSistema" || ' 00:00:00') AND ("fechaSistema" ||' 23:59:59');
		
		IF v_CARGATRANSACCIONES > 0 THEN
			p_out_CARGATRANSACCIONES := 'UI_EJECUTADO';
			v_contadorOperacionesOK = v_contadorOperacionesOK+1;
		ELSE
			p_out_CARGATRANSACCIONES := 'UI_NO_EJECUTADO';
		END IF;
		
		--Averiguar si el cambio de fecha fue ejecutado
		select COUNT("IdTipoPrograma") INTO v_CAMBIOFECHA from "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma" 
		WHERE "IdTipoPrograma" = 'CAMBIO_FECHA_AUTOMATICO' AND "IdEstado" ='TER' AND "fechaEjecucion" BETWEEN TO_NVARCHAR(CURRENT_DATE) || ' 00:00:00' AND TO_NVARCHAR(CURRENT_DATE) ||' 23:59:59';
		
		IF v_CAMBIOFECHA > 0 THEN
			p_out_CAMBIOFECHA := 'UI_EJECUTADO';
			v_contadorOperacionesOK = v_contadorOperacionesOK+1;
		ELSE
			p_out_CAMBIOFECHA := 'UI_NO_EJECUTADO';
		END IF;	
		
		--Averiguar si el ruteo automatico funciona
		/*
		select *from "BSCL.Plataforma.BD.Vistas::BAT.ConsultarPrograma"
		WHERE "IdTipoPrograma" = 'ProcesamientoMensajesFix' AND "IdEstado" ='ASI' AND "fechaEjecucion" BETWEEN ("fechaSistema" || ' 00:00:00') AND ("fechaSistema" ||' 23:59:59');
		
		IF v_RUTEOAUTOMATICO > 0 THEN
			p_out_RUTEOAUTOMATICO := 'UI_EJECUTADO';
			v_contadorOperacionesOK = v_contadorOperacionesOK+1;
		ELSE
			p_out_RUTEOAUTOMATICO := 'UI_NO_EJECUTADO';
		END IF;
		*/
		
		IF v_contadorOperacionesOK = 1 THEN
			p_DetalleLog =  select 'APERTURA' as "SubRegistro", 'EVENTO' as "CampoRegistro", '' as "ValorAntiguo", 'APEMOD' as "ValorNuevo" FROM dummy;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'INFORMATIVO' 
				,p_IdModulo => 'ORD' 
				,p_IdUsuario => p_IdUsuario 
				,p_IdEmpresa => p_IdEmpresa 
				,p_FechaSistemaLog => v_fechaSistema
				,p_IdUsuarioAtendido => p_IdUsuario
				,p_IdTipoRegistro => 'ORDEN'
				,p_IdProceso => 5
				,p_NombreProceso => 'APERTURA_ORDEN'
				,p_DetalleLog => :p_DetalleLog
			);
			
		END IF;

		SIGNAL SALIDA_CONTROLADA;
	END;
END;



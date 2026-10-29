SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Orden.BD.Servicios::ModificarTransaccion";
GO
CREATE PROCEDURE
-- 
--  Proposito: Modificar transaccion
--  Autor: Renzo Ulloa 
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Orden.BD.Servicios::ModificarTransaccion"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                        	BIGINT,
    IN p_IdUsuario                        	BIGINT,
    IN p_IdModulo                         	NVARCHAR(3),
    IN p_UsaReglasNegocio				  	NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
  	
  	IN p_IdTransaccion                    	BIGINT,
    IN p_Monto                            	DECIMAL(26,8)	DEFAULT NULL,
    IN p_Precio                           	DECIMAL(26,8)	DEFAULT NULL,
    IN p_Tasa                             	DECIMAL(26,8)	DEFAULT NULL,
    IN p_IdLibroOrdenes                   	BIGINT			DEFAULT NULL,
    IN p_ValorRescate                    	DECIMAL(26,8)	DEFAULT NULL,
    IN p_Cantidad                        	DECIMAL(26,8)	DEFAULT NULL,
    IN p_FechaEmision						NVARCHAR(10)	DEFAULT NULL,
	
    
    IN p_EsBilateral		 				NVARCHAR(1)		DEFAULT NULL,
    IN p_BloqueoLiquidacion 				NVARCHAR(1)		DEFAULT NULL,
    IN p_FechaLiquidacion 	 				NVARCHAR(10)	DEFAULT NULL,
    IN p_MontoRetencion 	 				DECIMAL(26,8)	DEFAULT NULL, 


    IN p_AccionTipoOperacion				NVARCHAR(40)	DEFAULT NULL,
	IN p_TipoCambioCorresponsal				DECIMAL(26,8)	DEFAULT NULL,
	IN p_IdCorredorCorresponsal				BIGINT			DEFAULT NULL,
	IN p_MontoLiquidacionCorresponsal		DECIMAL(26,8)	DEFAULT NULL,
	IN p_GastosCorresponsal					DECIMAL(26,8)	DEFAULT NULL,
	IN p_IdMonedaLiquidacionCorresponsal	NVARCHAR(3)		DEFAULT NULL,
    IN p_IdCamara					        NVARCHAR(20)	DEFAULT NULL,
	
	IN p_Motivo								NVARCHAR(5000)	DEFAULT '',
	IN p_Observaciones						NVARCHAR(5000)	DEFAULT '',
	IN p_FechaModificacion					NVARCHAR(19)	DEFAULT '',
	IN p_MarcaNegociacion					NVARCHAR(40)	DEFAULT '',
    
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador int,
	OUT p_out_InformacionDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdInterno" BIGINT,"IdOperacion" BIGINT,"Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
    )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN

	--MANEJO DE ERRORES
	DECLARE v_Errores "BSCL.Orden.BD.Tipos::SalidaDetalle" DEFAULT SELECT 0 as "IdInterno",0 as "IdOperacion",'' as "Resultado",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2", '' as "ValorDato2" FROM DUMMY WHERE 1=2;
	DECLARE v_out_InformacionContador	 INT;
	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		
		select * from :v_Errores;
		CALL "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
			p_SalidaDetalle			=> :v_Errores,
			
			p_out_InformacionContador		=> p_out_InformacionContador,
			p_out_InformacionDetalle		=> p_out_InformacionDetalle,
			p_out_AdvertenciasContador		=> p_out_AdvertenciasContador,
			p_out_AdvertenciasDetalle		=> p_out_AdvertenciasDetalle,
			p_out_ErroresContador			=> p_out_ErroresContador,
			p_out_ErroresDetalle			=> p_out_ErroresDetalle);
				 					 	
	END;
	
	BEGIN
		DECLARE v_resultadoRegla				NVARCHAR(40);
		DECLARE v_RECHAZO						BIGINT;
		DECLARE v_IdEstado						NVARCHAR(3);
		DECLARE v_IdTransaccionSim				BIGINT;
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		if :v_resultadoRegla = 'RECH' then
			v_Errores = SELECT 0 as "IdInterno",0 as "IdOperacion", 'RECHAZO' AS "Resultado","Codigo", '' as "CodigoDato1",'' as "ValorDato1", '' as "CodigoDato2",'' as "ValorDato2" 
						FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		end if;
		
		IF (:p_FechaModificacion = '') THEN
			SELECT TO_SECONDDATE(CURRENT_TIMESTAMP) INTO p_FechaModificacion FROM DUMMY;
		END IF;
		
		
		SELECT "IdEstado" INTO v_IdEstado FROM "BSCL.Orden.BD::ORD.Transaccion" WHERE "IdTransaccion" = :p_IdTransaccion;
		SELECT "IdTransaccionPlazo" INTO v_IdTransaccionSim FROM "BSCL.Orden.BD::ORD.Transaccion" WHERE "IdTransaccion" = :p_IdTransaccion;
		
		CALL "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosModificarTransaccion"(
			p_IdTransaccion						=> :p_IdTransaccion,
			p_IdEstado							=> :v_IdEstado,
			p_Cantidad							=> :p_Cantidad,
			p_Monto								=> :p_Monto,
			p_Precio							=> :p_Precio,
			p_FechaEmision						=> :p_FechaEmision,
			p_Tasa								=> :p_Tasa,
			p_ValorRescate						=> :p_ValorRescate,
			p_AccionTipoOperacion				=> :p_AccionTipoOperacion,
			p_TipoCambioCorresponsal			=> :p_TipoCambioCorresponsal,
			p_IdCorredorCorresponsal			=> :p_IdCorredorCorresponsal,
			p_MontoLiquidacionCorresponsal		=> :p_MontoLiquidacionCorresponsal,
			p_GastosCorresponsal				=> :p_GastosCorresponsal,
			p_IdMonedaLiquidacionCorresponsal	=> :p_IdMonedaLiquidacionCorresponsal,
			p_out_SalidaDetalle					=> :v_out_SalidaDetalle,
			p_out_Salida        				=> :v_out_Salida
		);
		
	
		v_Errores = select * from :v_out_SalidaDetalle union all select * from :v_Errores;
		
		SELECT "RECHAZO" INTO v_RECHAZO FROM :v_out_Salida;
		
		v_transacciones = SELECT p_IdTransaccion as "IdTransaccion" FROM DUMMY;
							--UNION ALL
							--SELECT "IdTransaccionPlazo" as "IdTransaccion" from "BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" = :p_IdTransaccion
		
		UPDATE TRAN
		SET
			"Monto" 							= CASE WHEN "IdClasificacionTipoInstrumento" = 'RV' THEN COALESCE(p_Precio,"Precio") * COALESCE(p_Cantidad,"Cantidad") ELSE COALESCE(p_Monto,"Monto") END,
			"Precio" 							= COALESCE(p_Precio,"Precio"),
			"FechaEmision" 						= COALESCE(p_FechaEmision,"FechaEmision"),
			"Cantidad"				 			= COALESCE(p_Cantidad,"Cantidad"),
			"IdLibroOrdenes" 					= COALESCE(p_IdLibroOrdenes,"IdLibroOrdenes"),
			
			"TipoCambioCorresponsal"			= COALESCE(p_TipoCambioCorresponsal,"TipoCambioCorresponsal"),
			"IdCorredorCorresponsal"			= COALESCE(p_IdCorredorCorresponsal,"IdCorredorCorresponsal"),
			"MontoLiquidacionCorresponsal"		= COALESCE(p_MontoLiquidacionCorresponsal,"MontoLiquidacionCorresponsal"),
			"GastosCorresponsal"				= COALESCE(p_GastosCorresponsal,"GastosCorresponsal"),
			"IdMonedaLiquidacionCorresponsal"	= COALESCE(p_IdMonedaLiquidacionCorresponsal,"IdMonedaLiquidacionCorresponsal"),
			"MarcaNegociacion"					= COALESCE(p_MarcaNegociacion,"MarcaNegociacion"),
			"Tasa"								= COALESCE(p_Tasa,"Tasa"),
			"EsBilateral"						= COALESCE(p_EsBilateral,"EsBilateral"),
			"BloqueoLiquidacion"				= COALESCE(p_BloqueoLiquidacion,"BloqueoLiquidacion"),			
			"FechaLiquidacion"					= COALESCE(p_FechaLiquidacion,"FechaLiquidacion"),
			"MontoRetencion"					= COALESCE(p_MontoRetencion,"MontoRetencion"),
			"IdCamara"							= COALESCE(p_IdCamara,"IdCamara")
			
		FROM "BSCL.Orden.BD::ORD.Transaccion" TRAN 
		INNER JOIN :v_transacciones TT on TT."IdTransaccion" = TRAN."IdTransaccion"
		WHERE :v_RECHAZO = 0;
		
		IF (v_IdTransaccionSim > 0) THEN
			
			UPDATE TRAN
			SET
				"FechaEmision" 						= COALESCE(p_FechaEmision,"FechaEmision"),
				"Cantidad"				 			= COALESCE(p_Cantidad,"Cantidad"),
				"IdLibroOrdenes" 					= COALESCE(p_IdLibroOrdenes,"IdLibroOrdenes"),

				"EsBilateral"						= COALESCE(p_EsBilateral,"EsBilateral"),
				"BloqueoLiquidacion"				= COALESCE(p_BloqueoLiquidacion,"BloqueoLiquidacion"),			
				"FechaLiquidacion"					= COALESCE(p_FechaLiquidacion,"FechaLiquidacion"),				
				"MontoRetencion"					= COALESCE(p_MontoRetencion,"MontoRetencion"),
				"Tasa"								= COALESCE(p_Tasa,"Tasa"),
				"TipoCambioCorresponsal"			= COALESCE(p_TipoCambioCorresponsal,"TipoCambioCorresponsal"),
				"IdCorredorCorresponsal"			= COALESCE(p_IdCorredorCorresponsal,"IdCorredorCorresponsal"),
				"MontoLiquidacionCorresponsal"		= COALESCE(p_MontoLiquidacionCorresponsal,"MontoLiquidacionCorresponsal"),
				"GastosCorresponsal"				= COALESCE(p_GastosCorresponsal,"GastosCorresponsal"),
				"IdMonedaLiquidacionCorresponsal"	= COALESCE(p_IdMonedaLiquidacionCorresponsal,"IdMonedaLiquidacionCorresponsal")
				
			FROM "BSCL.Orden.BD::ORD.Transaccion" TRAN 
			WHERE :v_RECHAZO = 0 AND TRAN."IdTransaccion" = :v_IdTransaccionSim;
		
		END IF;
		
		
		
		
		
		v_Errores = select :p_IdTransaccion as "IdInterno",:p_IdTransaccion as "IdOperacion",'INFORMACION' as "Resultado",'TRANSACCION_MODIFICADA' AS "Codigo",
					'IdTransaccion' as "CodigoDato1",CAST(:p_IdTransaccion AS NVARCHAR(502)) as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" 
					from dummy where :v_RECHAZO = 0 
					union all select * from :v_Errores;
		
		SIGNAL SALIDA_CONTROLADA;
		
	END;--2	
END;--1


SET SCHEMA "BSCL";  
go
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CuadraturaVencimientoOrdenes";
go
-- 
--  Proposito: Servicio para el vencimiento diario de ordenes.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 01/04/2016
--  
-- CORRECCION 20170828 - CHERNANDEZ - Vencer ordenes con fecha vigencia menor o igual a la fecha de vencimiento.
--
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::CuadraturaVencimientoOrdenes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la creacion del usuario
	----IN  p_FechaVencimiento			timestamp,  		
	IN  p_FechaVencimiento			NVARCHAR(10),

	-- Parametros salida del servicio publico
	OUT p_cantidad_ordenes_vencidas bigint,
	OUT p_resultado_cierre_diario nvarchar(3),
	
	-- Salidas para mensajes de advertencias y errores
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


	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;



	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE v_fechaSistema NVARCHAR(10);
		DECLARE v_CodigoUsuario NVARCHAR(40);
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						  
			
		END;
		-----------------------------------------------------
		--Actualizar la fecha de vigencia de todas las ordenes vigentes, que se encuentren en el rango de fecha sistema recibido.
		-----------------------------------------------------
		p_resultado_cierre_diario := 'NOK';
		
		BEGIN
			
			
			p_Ordenes = SELECT "IdOrden", "Cantidad", "NombreCompleto"
            FROM "BSCL.Orden.BD::ORD.Orden" 
            ----WHERE "FechaVigencia" <= TO_DATE(p_FechaVencimiento)
            WHERE "FechaVigencia" <= p_FechaVencimiento            
            AND "OrdenVigente" = 'S'
            AND "IdEstadoOrden" <> 'RZD' AND "IdEstadoOrden" <> 'ANU';
            
            --SELECT 'p_Ordenes', * FROM :p_Ordenes; -- revisar 
            
            p_Asignacion_Ordenes = SELECT CA."IdOrden", SUM("CantidadAsignada") AS "TotalAsignacion" 
            FROM "BSCL.Orden.BD.Vistas::ConsultarAsignacion" CA
            INNER JOIN :p_Ordenes O ON CA."IdOrden" = O."IdOrden"
            GROUP BY CA."IdOrden";
            
            --SELECT 'p_Asignacion_Ordenes', * FROM :p_Asignacion_Ordenes; -- revisar
            
            /*  VCD: SIN ASIGNAR
                EJP: EJECUTADA PARCIALMENTE
                EJE: EJECUTADA */
            p_Ordenes_Vencer =
            SELECT O."IdOrden", 
            CASE 
                WHEN O."Cantidad" = AO."TotalAsignacion" THEN 'EJE'
                WHEN O."Cantidad" < AO."TotalAsignacion" THEN 'EJP'
                ELSE 'VCD'
            END AS "IdEstado"
            FROM :p_Ordenes O
            INNER JOIN :p_Asignacion_Ordenes AO ON O."IdOrden" = AO."IdOrden"
            UNION All
            SELECT O."IdOrden", 
                   'VCD' AS "IdEstado"
            FROM :p_Ordenes O
            WHERE O."IdOrden" NOT IN (SELECT AO."IdOrden" 
                                      FROM :p_Asignacion_Ordenes AO);
            
            --SELECT 'p_Ordenes_Vencer', * FROM :p_Ordenes_Vencer; -- revisar
            
			
			--Actualizar los datos del perfil
			UPDATE O
			SET O."OrdenVigente" = 'N', O."IdEstadoOrden" = ov."IdEstado"
			FROM "BSCL.Orden.BD::ORD.Orden" O, :p_Ordenes_Vencer ov
			WHERE O."IdOrden" = ov."IdOrden";
			
			--Actualizar ultima fecha de cierre en la configucarion
			SELECT COUNT(*) INTO p_cantidad_ordenes_vencidas FROM :p_Ordenes_Vencer AS O WHERE O."IdEstado" = 'VCD' ;
            p_resultado_cierre_diario := 'OK+';
            
            --SELECT 'p_cantidad_ordenes_vencidas', :p_cantidad_ordenes_vencidas FROM DUMMY; -- revisar
            
            
			---------------------------------------------------     
            --NOTIFICAR
            ---------------------------------------------------                        
            -- SE COMENTA PARA PRUEBA DEL VENCIMIENTO DE ORDENES EN PRESURA
            /*IF p_cantidad_ordenes_vencidas > 0 THEN
                p_Notificar = SELECT "IdOrden" FROM :p_Ordenes_Vencer;
                --SELECT 'p_Notificar', :p_Notificar FROM DUMMY; -- revisar
                CALL "BSCL.Orden.BD.Servicios::NotificarEstadoOrden"(:p_IdEmpresa, :p_IdUsuario, :p_IdModulo, :p_Notificar);
            END IF;*/
            
			
			
		END;
		---------------------------------------------------		
		--Ejecutar el programa de logeo.
		---------------------------------------------------
		p_DetalleLog =  select 'UI_ORDENES_VENCIDAS' as "SubRegistro", TO_VARCHAR(p_cantidad_ordenes_vencidas) as "CampoRegistro", '' as "ValorAntiguo", 'CIEMOD' as "ValorNuevo"
		FROM dummy;
		
		--Ejecutar el programa de logeo.
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			p_IdTipoLog => 'INFORMATIVO' 
			,p_IdModulo => 'ORD' 
			,p_IdUsuario => p_IdUsuario 
			,p_IdEmpresa => p_IdEmpresa 
			----,p_FechaSistemaLog => TO_VARCHAR (p_FechaVencimiento, 'YYYY-MM-DD')
			,p_FechaSistemaLog => p_FechaVencimiento
			,p_IdUsuarioAtendido => p_IdUsuario
			,p_IdTipoRegistro => 'ORDEN'
			,p_IdProceso => 5
			,p_NombreProceso => 'VENCIMIENTO_ORDEN'
			,p_DetalleLog => :p_DetalleLog
		);
		
		--LOG PARA EL CIERRE DE MODULO
		SELECT "CodigoUsuario" INTO v_CodigoUsuario FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" 
		WHERE "IdUsuario" = p_IdUsuario AND "IdEmpresa" = p_IdEmpresa;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(
			p_IdEmpresa => p_IdEmpresa,
			p_out_FechaSistema => v_fechaSistema
		);
		
		INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso"
		("IdEmpresa", "IdControlProceso", "CodigoModulo", "CodigoProceso", "FechaSistema", "FechaRegistro", "CodigoUsuario")
		VALUES(p_IdEmpresa, "BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL, 'ORD', 'VENCIMIENTO_ORDEN', v_fechaSistema, CURRENT_UTCTIMESTAMP, v_CodigoUsuario);

		
	END;
END;

SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::CRE.EntregaSistema";

CREATE PROCEDURE 
-- 
--  Proposito: Servicio que solicita Apertura de Modulo, Si el estado de solicitud es Aprobado se Abre el Modulo. 
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 15-03-2016
--  
"BSCL.Plataforma.BD.Servicios::CRE.EntregaSistema"(
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_simularOperacion       					NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos) 
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	-- Parametros salida del servicio publico
	OUT p_out_Validaciones 		table ("TipoRequisito" nvarchar(100), "Error" nvarchar(500)),		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA

	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdCREModuloEmpresa 		BIGINT := 100; --SISTEMA es 100
		DECLARE v_FechaHoraRegistroVarchar 	NVARCHAR(40);
		DECLARE v_HoraSistema				TIME;
		DECLARE v_countCREModuloEmpresa		BIGINT;
		DECLARE v_permisoEntregaConError 	NVARCHAR(3);
		DECLARE v_cntErroresParaEntrega 	INT;
		DECLARE v_FechaSistemaT 	NVARCHAR(40);
			--------------------
		--Validaciones
		--------------------

		--valida si tiene permitido entregar con errores
		begin

			SELECT "Valor" 
			  INTO v_permisoEntregaConError
			  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		     WHERE "CodigoParametro" = 'PAU.PermiteEntregaSistemaConError'
			   AND "IdEmpresa" = p_IdEmpresa;
			
			SELECT "Valor" INTO v_FechaSistemaT
			  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		     WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';
		 
			SELECT count(1)
			  INTO v_cntErroresParaEntrega
              FROM "BSCL.Plataforma.BD::PAU.ControlProceso"  
             WHERE "FechaSistema" = :v_FechaSistemaT
               AND COALESCE("ResultadoProceso",'OK') != 'OK';
		
			if v_permisoEntregaConError = 'N' and v_cntErroresParaEntrega > 0 then
				p_out_ErroresDetalle =  select 'NO_PUEDE_ENTREGAR_CON_ERRORES' as "Codigo", 'No puede entregar Sistema si hay Errores.' as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;
		
		end;

		-- Existencia 		
		BEGIN
			
			SELECT COUNT("IdCREModuloEmpresa") INTO v_countCREModuloEmpresa FROM "BSCL.Plataforma.BD::CRE.ModuloEmpresa" WHERE "IdCREModuloEmpresa"=v_IdCREModuloEmpresa;
			IF(v_countCREModuloEmpresa=0) THEN
				p_out_ErroresDetalle =  select 'MODULOEMPRESA_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
		END;		
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			v_Requisitos = SELECT * 
						FROM "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre" 
						WHERE "IdCREModuloEmpresa" = v_IdCREModuloEmpresa
						AND "Tipo" = 'C';
		
		--validar que el modulo esté abierto...
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa, :v_FechaHoraRegistroVarchar);
		v_HoraSistema:= TO_TIME(SUBSTRING (v_FechaHoraRegistroVarchar,12,8), 'HH24:MI:SS');	
		
		v_Perfiles = SELECT "IdUsuario", "IdPerfil"
		 FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"
		 WHERE "IdUsuario" = p_IdUsuario;		
		
		
		-- Requisito de hora
		 p_out_Validaciones = SELECT 'Hora Mínima' as "TipoRequisito",
		 CASE WHEN TO_TIME("HoraMinCierre",'HH24:MI') < v_HoraSistema 
		 	THEN 'OK' ELSE 'ERROR_REQUISITO_HORA_MINIMA_CIERRE' END as "Error"
		 FROM :v_Requisitos
		 WHERE "IdTipoRequisito" = 'HOR'
		 UNION
		-- Requisito de Usuario
		 SELECT 'Usuario' as "TipoRequisito",
		 CASE WHEN "IdUsuario" = p_IdUsuario 
		 	THEN 'OK' ELSE 'ERROR_REQUISITO_USUARIO_CIERRE' END as "Error"
		 FROM :v_Requisitos
		 WHERE "IdTipoRequisito" = 'USU'
		 UNION
		-- Requisito de Perfil
		 SELECT 'Perfil' as "TipoRequisito",CASE WHEN R."IdPerfil" = P."IdPerfil"  
		 	THEN 'OK' ELSE 'ERROR_REQUISITO_PERFIL_CIERRE' END as "Error"
		 FROM :v_Requisitos R
		 LEFT JOIN :v_Perfiles P ON R."IdPerfil" = P."IdPerfil"
		 WHERE "IdTipoRequisito" = 'PRF'
		 UNION
		 -- Solo se puede entregar si el modulo esta abierto
		 SELECT 'Estado Módulo' as "TipoRequisito",
		 CASE WHEN "TipoEstado" = 'A' THEN 'OK' ELSE 'ERROR_REQUISITO_SISTEMA_CERRADO' END as "Error"
		 FROM "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
		 WHERE "IdCREModuloEmpresa" = v_IdCREModuloEmpresa;
		 
		-- Requisito de Modulo ???

		-- Añadir Requisitos adicionales....
		
		
			--Grabar en log
			
					
		END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

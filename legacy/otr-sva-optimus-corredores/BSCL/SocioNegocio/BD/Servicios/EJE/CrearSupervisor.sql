SET SCHEMA "BSCL";

DROP PROCEDURE   "BSCL.SocioNegocio.BD.Servicios::EJE.CrearSupervisor";
  
CREATE PROCEDURE
-- 
--  Proposito: Servicio para agregar un supervisor de ejecutivos
--  Autor: - CAPONTE
--  Fecha Creacion: 03/08/2016 
--  
"BSCL.SocioNegocio.BD.Servicios::EJE.CrearSupervisor" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	
	-- Parametros entrada del servicio publico
	IN	p_IdEmpleadoSupervisor 			BIGINT,
	IN	p_IdEstado 				NVARCHAR(3),
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
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        DECLARE v_countSupervisor BIGINT := 0;
        DECLARE v_Fecha NVARCHAR(40);
		--------------------
		--Validaciones
		--------------------
	BEGIN
			
			---Verifica si empleado ya existe como supervisor
			SELECT COUNT(1) INTO v_countSupervisor
			FROM "BSCL.SocioNegocio.BD::EJE.Supervisor"
			WHERE "IdEmpleadoSupervisor" = :p_IdEmpleadoSupervisor; 
		
			IF(v_countSupervisor>0) THEN
			    v_hash= select 'SUPERVISOR_EXISTE' as "Codigo", '' as "Valor" FROM DUMMY;
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('SUPERVISOR_EXISTE',:v_hash,:v_erroroutmsg);
                p_out_ErroresDetalle =  select 'SUPERVISOR_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
                                        select * from :p_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;	
			
	END;		

		
		
				
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
		
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa, :v_Fecha);
			
		 	------------------------------------------
			--Crear 
			------------------------------------------
			INSERT INTO "BSCL.SocioNegocio.BD::EJE.Supervisor"
			(
			 	"IdEmpleadoSupervisor",
				"FechaCreacion",
				"IdUsuarioCreacion",
				"IdEstado"				
  			)VALUES 
  			(
				p_IdEmpleadoSupervisor,
				TO_SECONDDATE (:v_Fecha, 'YYYY-MM-DD HH24:MI:SS'),
				p_IdUsuario,
				p_IdEstado
			);
					
					
		---Insertar en log
			BEGIN 
				DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE v_NombreProceso	NVARCHAR(100):='Crear Supervisor';
				
				
				--DATOS DETALLE LOG
				p_DetalleLog = 
				(Select ''		 as "SubRegistro",
						'IdEmpleadoSupervisor'	 as "CampoRegistro",
						'' 	 as "ValorAntiguo",
						p_IdEmpleadoSupervisor 		 as "ValorNuevo" 
				FROM dummy);
				


				--GRABAR LOG

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'INGRESO' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_Fecha, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => 'SUPERVISOR'--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :p_IdEmpleadoSupervisor			
					);
	
			END;
			--LOG DE AUDITORIA (Fin)			
			

	
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
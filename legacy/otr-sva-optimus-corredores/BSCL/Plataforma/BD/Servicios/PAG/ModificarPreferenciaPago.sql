SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::PAG.ModificarPreferenciaPago" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la modificación de preferencia de pago
--  Autor: JGarces - BCS
--  Fecha Creacion: 18/04/2016
-- 

"BSCL.Plataforma.BD.Servicios::PAG.ModificarPreferenciaPago" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	IN  p_IdPreferenciaPago			NVARCHAR(40), 
	IN  p_IdTipoOperacion 			NVARCHAR(6) ,
	IN  p_IdMedioDePago				NVARCHAR(3)	,
	IN  p_IdMoneda 					NVARCHAR(3)	,
	IN  p_UsaPersonaRelacionada		NVARCHAR(1)	default 'N',
	IN  p_IdEstado					NVARCHAR(3) , -- 'ACT', 'INA'
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
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
		--DECLARE v_IdPerfilCobros			 	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    --SELECT "BSCL.SocioNegocio.BD.Secuencias::Telefono".NEXTVAL INTO v_IdPerfilCobros FROM DUMMY;
			--v_IdPerfilCobros:=p_IdPerfilProductos;
	
			
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------

	
			UPDATE 
				"BSCL.Plataforma.BD::PAG.PreferenciaPago" 
			SET
				"IdTipoOperacion" 		= :p_IdTipoOperacion,
				"IdMedioDePago" 		= :p_IdMedioDePago,
				"IdMoneda" 				= :p_IdMoneda,
				"UsaPersonaRelacionada" = :p_UsaPersonaRelacionada,
				"FechaModificacion" 	= CURRENT_UTCTIMESTAMP,
				"IdEstado" 				= :p_IdEstado,
				"IdEmpresa" 			= :p_IdEmpresa 
			WHERE
				"IdPreferenciaPago" = :p_IdPreferenciaPago;

			p_out_RefId:=1;
			
			
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
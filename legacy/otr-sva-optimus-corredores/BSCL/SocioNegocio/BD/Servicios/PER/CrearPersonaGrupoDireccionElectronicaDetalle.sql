SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaGrupoDireccionElectronicaDetalle";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para agregar una direccion electronica al grupo.
--  Autor: - CFLORES
--  Fecha Creacion: 17/03/2016 
--  
"BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaGrupoDireccionElectronicaDetalle" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	
		  
	-- Parametros entrada del servicio publico
	IN	p_IdDireccionElectronica 	BIGINT,
	IN	p_IdTipoDestinatario 		NVARCHAR(6),
	IN	p_IdGrupoDireccion 		 	BIGINT,
	IN	p_IdEstado 	 		 	 	NVARCHAR(3),
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
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
		DECLARE v_IdGrupoDireccionDetalle		 		bigint:=0;
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
		DECLARE v_countAtributo	 		   bigint:=0;
		DECLARE v_FechaIngreso			   SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdEstadoAutorizacion	   NVARCHAR(3);

		--------------------
		--Validaciones
		--------------------
		
		BEGIN

			---Verifica que la direccion electronica no exista en el grupo
			SELECT COUNT(1) INTO v_countAtributo
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaDetalle"
			WHERE "IdGrupoDireccion" = :p_IdGrupoDireccion AND "IdDireccionElectronica" = :p_IdDireccionElectronica AND "IdEstado" = 'ACT' AND "Borrado" = 'N';
			IF(v_countAtributo>0) THEN
    			v_hash= select 'SOCIONEGOCIO' as "Codigo", '' as "Valor" FROM DUMMY;
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_DIRECCION_ELECTRONICA_EXISTE',:v_hash,:v_erroroutmsg);
                p_out_ErroresDetalle =  select 'UI_DIRECCION_ELECTRONICA_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;							
		END;		
		
		
		
			
		--------------------
		--Reglas Negocio 
		--------------------		
		
		--------------------
		--Lógica de Negocio 
		--------------------
		v_IdEstadoAutorizacion 			:= 'PEN';
				
		BEGIN
		
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			    -- recuperamos numero de secuencia que usaremos para el id del documento
		    SELECT "BSCL.SocioNegocio.BD.Secuencias::GrupoDireccionDetalle".NEXTVAL INTO v_IdGrupoDireccionDetalle FROM DUMMY;
	
			p_out_RefId:=v_IdGrupoDireccionDetalle;
			
		 	------------------------------------------
			--Crear 
			------------------------------------------

	IF (v_IdEstadoAutorizacion = 'PEN') then
	
		v_IdEstadoAutorizacion = 'ACT';

	end if; 
					insert into "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle"
					(
						 "IdGrupoDireccionDetalle",
						 "IdDireccionElectronica",
						 "IdTipoDestinatario",
					     "IdGrupoDireccion",
						 "FechaCreacion",
						 "FechaModificacion",
						 "FechaIngreso",	 
						 "IdEstado",
						 "IdEstadoAutorizacion",
					     "IdUsuarioCreador"
						) VALUES (
						v_IdGrupoDireccionDetalle,
						p_IdDireccionElectronica,
						p_IdTipoDestinatario,
						p_IdGrupoDireccion,
						v_FechaIngreso,--TO_TIMESTAMP(p_FechaIngreso,'YYYY-MM-DD HH24:MI:SS.FF7') 					
						CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
						CURRENT_UTCTIMESTAMP,	
						p_IdEstado,
						v_IdEstadoAutorizacion,
						p_IdUsuario
						);
				

				END;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
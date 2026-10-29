SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::IND.ModificarTipoIndicador";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de los tipos de indicadores modificar, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07-04-2016
--  
"BSCL.Plataforma.BD.Servicios::IND.ModificarTipoIndicador" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdTipoIndicador				    		BIGINT,	
	IN  p_RequiereAutorizacion						NVARCHAR(1),
	IN  p_NombreTipoIndicador						NVARCHAR(255),
	IN  p_GrupoIndicador 							NVARCHAR(40),
	IN  p_IdEstado		 							NVARCHAR(3),
	IN  p_IdTipoPersona								NVARCHAR(1),
	IN  p_RequiereFechaVencimiento					NVARCHAR(1),
	IN 	p_ValorIndicadorDefecto 					NVARCHAR(255),
	IN  p_CantidadDecimales 						BIGINT,

	-- Parametros salida del servicio publico
	OUT p_out_RefId 					BIGINT,	
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
       
					
		DECLARE v_countAtributo	 		   bigint:=0;
		--------------------
		--Validaciones
		--------------------
		

		BEGIN
	
				---Verifica si el Atributo no este registrado
				SELECT COUNT(1) INTO v_countAtributo
				FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador"
				WHERE "IdTipoIndicador" = :p_IdTipoIndicador; 
				
				IF(v_countAtributo=0) THEN
					p_out_ErroresDetalle =  select 'INDICADOR_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
		

			UPDATE  ATR
			SET
				ATR."RequiereAutorizacion" 	= p_RequiereAutorizacion,
				ATR."NombreTipoIndicador" 	= p_NombreTipoIndicador,
				ATR."GrupoIndicador" 		= p_GrupoIndicador,
				ATR."IdEstado" 				= p_IdEstado,
				ATR."IdTipoPersona"			= p_IdTipoPersona,
				ATR."RequiereFechaVencimiento" = p_RequiereFechaVencimiento,
				ATR."ValorIndicadorDefecto" = p_ValorIndicadorDefecto,
				ATR."CantidadDecimales" = p_CantidadDecimales
			FROM "BSCL.Plataforma.BD::IND.TipoIndicador" ATR
			WHERE 
				ATR."IdTipoIndicador" 		= p_IdTipoIndicador;
	
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

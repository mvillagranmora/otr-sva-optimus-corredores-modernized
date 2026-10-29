SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CalcularMontoPorTipoInstrumento";

CREATE PROCEDURE
-- 
--  Proposito: Calcular monto de la operacion por tipo de instrumento
--  Autor: Oscar Carreno Tolmo :: OCT
--  Fecha Creacion: 19/08/2015
--  
"BSCL.Plataforma.BD.Servicios::CalcularMontoPorTipoInstrumento"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa           BIGINT,
    IN p_IdUsuario           BIGINT,
    IN p_IdModulo            NVARCHAR(3),
	IN p_UsaReglasNegocio	 nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	 
    --ENUMERAR VARIABLES DE ENTRADA
    IN p_Cantidad            DECIMAL(26,8),
    IN p_Precio              DECIMAL(26,8), 
    IN p_Tamano              BIGINT default 1,
    IN p_IdTipoInstrumento   NVARCHAR(3),
    IN p_AplicaValidaciones	 nvarchar(1),
    
    --VARIABLES DE SALIDA OBLIGATORIAS
    OUT p_out_Monto          DECIMAL(26,8),
    
	-- Salidas para mensajes de advertencias y errores
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
			--Error interno no manejado  (El log ser� captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	
	BEGIN
	-----------------------------------------------------
	--Declaraciones Variables
	-----------------------------------------------------
	DECLARE v_Monto DECIMAL(26,8)  :=0;
	DECLARE v_Contador SMALLINT := 0;
		
		-----------------------------------------------------
		--Validaciones b�sicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
		    
			declare v_resultadoRegla nvarchar(40);
			
			IF (:p_AplicaValidaciones = 'S') THEN

				--valida que el tipo instrumento exista

/*
				CALL "BSCL.Plataforma.BD.Reglas::TipoInstrumentoExiste"(:p_IdTipoInstrumento,0,:v_resultadoRegla,:v_erroroutmsg);
				if :v_resultadoRegla = 'N' then
					p_out_ErroresDetalle =  select 'TIPO_INSTRUMENTO_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
					SIGNAL SALIDA_CONTROLADA;
				end if;
*/
				v_Contador := 0;
				SELECT COUNT("IdTipoInstrumento")
				INTO v_Contador
			    FROM "BSCL.Plataforma.BD::INS.TipoInstrumento"
			    WHERE "IdTipoInstrumento" = p_IdTipoInstrumento;
			    
			    if v_Contador = 0 then
					p_out_ErroresDetalle =  select 'TIPO_INSTRUMENTO_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
					SIGNAL SALIDA_CONTROLADA;
			    end if; 
			    


							
				--validacion sin signal
				if (:p_Cantidad <= 0) then
					v_hash = select 'dummy' as "Codigo",'dummy' as "Valor" from dummy;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CANTIDAD_MENOR_IGUAL_A_CERO',:v_hash,:v_erroroutmsg);
					p_out_ErroresDetalle =  select 'CANTIDAD_MENOR_IGUAL_A_CERO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle;
				end if;
		
				if (:p_Precio <= 0) then
					v_hash = select 'dummy' as "Codigo",'dummy' as "Valor" from dummy;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('PRECIO_MENOR_IGUAL_A_CERO',:v_hash,:v_erroroutmsg);
					p_out_ErroresDetalle =  select 'PRECIO_MENOR_IGUAL_A_CERO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle;
				end if;
				if (:p_Tamano <= 0) then
					v_hash = select 'dummy' as "Codigo",'dummy' as "Valor" from dummy;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('TAMANO_MENOR_IGUAL_A_CERO',:v_hash,:v_erroroutmsg);
					p_out_ErroresDetalle =  select 'TAMANO_MENOR_IGUAL_A_CERO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle;
				end if;
			END IF; --(:p_AplicaValidaciones = 'S')
	
			If (:p_IdTipoInstrumento = 'ACN'
			or :p_IdTipoInstrumento = 'ACX'
			or :p_IdTipoInstrumento = 'CFI'
			or :p_IdTipoInstrumento = 'CFM'
			or :p_IdTipoInstrumento = 'ETN'
			or :p_IdTipoInstrumento = 'ETX'
			or :p_IdTipoInstrumento = 'MON'
			or :p_IdTipoInstrumento = 'CFX'
			or :p_IdTipoInstrumento = 'FDO'
			or :p_IdTipoInstrumento = 'FIP'
			or :p_IdTipoInstrumento = 'FIC'
			or :p_IdTipoInstrumento = 'FUF'
			or :p_IdTipoInstrumento = 'SIM'
			or :p_IdTipoInstrumento = 'MIL'
			
			) then
				v_Monto := :p_Cantidad * :p_Precio * :p_Tamano;
			else
					v_hash = select 'dummy' as "Codigo",'dummy' as "Valor" from dummy;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CLASIFICACION_NO_SOPORTADA',:v_hash,:v_erroroutmsg);
					p_out_ErroresDetalle =  select 'CLASIFICACION_NO_SOPORTADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle;
			end if ;
			
			IF (:p_AplicaValidaciones = 'S') THEN	
				if (:v_Monto <= 0) then
					v_hash = select 'dummy' as "Codigo",'dummy' as "Valor" from dummy;
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('MONTO_MENOR_IGUAL_A_CERO',:v_hash,:v_erroroutmsg);
					p_out_ErroresDetalle =  select 'MONTO_MENOR_IGUAL_A_CERO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
											select * from :p_out_ErroresDetalle;
				end if;
			END IF;--(:p_AplicaValidaciones = 'S')
			
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;	
			
			
			p_out_Monto := v_Monto;
		END;
	END;
END;

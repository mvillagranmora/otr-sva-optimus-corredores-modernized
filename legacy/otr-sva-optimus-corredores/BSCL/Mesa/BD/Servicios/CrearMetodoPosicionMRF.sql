SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::CrearMetodoPosicionMRF";

-- 
--  Proposito: Crear Metodo Posicion MRF
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 29/12/2016
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::CrearMetodoPosicionMRF" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_NombreMetodo		NVARCHAR(150),
	IN p_Glosa				NVARCHAR(200)	DEFAULT '',
	IN p_IdTipoReajuste		NVARCHAR(3),
	IN p_IdMesa				BIGINT,
	IN p_IdEstado			NVARCHAR(3)		DEFAULT 'VIG',
	IN p_IdTipoPosicion		NVARCHAR(20)	DEFAULT 'DIARIA',
	
	-- Parametros salida del servicio
    OUT p_out_RefId			BIGINT,
    
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
	
	DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
	
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle
								   UNION ALL
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
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
	    
	    DECLARE v_Count		BIGINT:=0;
		DECLARE v_IdMetodo	BIGINT:=0;
		
		--------------------
		--Validaciones
		--------------------
		
		-- Validacion existe Metodo Posicion con mismo NombreMetodo, IdTipoReajuste y IdMesa
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Mesa.BD::MES.MetodoPosicion"
		WHERE "NombreMetodo"	= p_NombreMetodo
		  AND "IdTipoReajuste"	= p_IdTipoReajuste
		  AND "IdMesa"			= p_IdMesa
		  AND "IdEstado"		= 'VIG';
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'METODO POSICION YA EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
				
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			p_out_RefId:=1;
			
		 	------------------------------------------
			--Crear 
			------------------------------------------
			
			SELECT "BSCL.Mesa.BD.Secuencias::MRF.IdMetodoPosicion".NEXTVAL INTO v_IdMetodo FROM DUMMY;
			p_out_RefId := v_IdMetodo;
			
			INSERT INTO "BSCL.Mesa.BD::MES.MetodoPosicion"(
				"IdMetodo",
				"NombreMetodo",
				"Glosa",
				"IdTipoReajuste",
				"IdMesa",
				"IdEstado",
				"IdTipoPosicion"
			)
			VALUES(
				v_IdMetodo,
				p_NombreMetodo,
				p_Glosa,
				p_IdTipoReajuste,
				p_IdMesa,
				p_IdEstado,
				p_IdTipoPosicion
			);
	
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

SET SCHEMA "BSCL";
GO 

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PLA.CrearDiaFeriado";
GO
-- 
--  Proposito: Crear Dia Feriado
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 03/01/2017
-- 
  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::PLA.CrearDiaFeriado" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_Tipo				NVARCHAR(40),
	IN p_Fecha				NVARCHAR(10),
	IN p_Dia				INTEGER,
	IN p_DescripcionDia		NVARCHAR(100) DEFAULT '',
	IN p_AgnoCarga			INTEGER,
	IN p_IdPais				BIGINT,
	
	-- Parametros salida del servicio
    OUT p_out_RefId			BIGINT,
    
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
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
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
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
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        
		DECLARE v_Count		BIGINT:=0;
		DECLARE v_IdFeriado	BIGINT:=0;
		
		
		--------------------
		--Validaciones
		--------------------
		
		-- Verifica si Dia Feriado existe
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado"
		WHERE "Tipo"		= p_Tipo
		  AND "Fecha"		= p_Fecha
		  AND "Dia"			= p_Dia
		  --AND "DescripcionDia" = p_DescripcionDia
		  AND "AgnoCarga"	= p_AgnoCarga
		  AND "IdPais"		= p_IdPais
		  AND "IdEstado"    = 'ACT';
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle =  select 'DIA FERIADO EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- Verifica si existe Dia Feriado para misma Fecha, AgnoCarga y IdPais
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado"
		WHERE "Fecha"		= p_Fecha
		  AND "Dia"			= p_Dia
		  AND "AgnoCarga"	= p_AgnoCarga
		  AND "IdPais"		= p_IdPais
		  AND "IdEstado"    = 'ACT';
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle =  select 'DIA FERIADO EXISTE PARA MISMA FECHA Y PAIS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
	
		-- Verifica si existe Dia Feriado para misma Fecha y IdPais 
		SELECT COUNT(1) INTO v_Count 
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado" 
		WHERE "Fecha"		= p_Fecha 
		  AND "Dia"			= p_Dia 
		  AND "IdPais"		= p_IdPais 
		  AND "IdEstado"    = 'ACT'; 
		 
		IF(v_Count > 0 AND p_Fecha <> '0001-01-01') THEN 
			-- Se inactiva día feriado anterior 
			UPDATE "BSCL.Plataforma.BD::REG.DiaFeriado" 
			SET "IdEstado" = 'INA' 
			WHERE "Fecha"		= p_Fecha 
			  AND "Dia"			= p_Dia 
			  AND "IdPais"		= p_IdPais 
			  AND "IdEstado"    = 'ACT'; 
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
			
			SELECT "BSCL.Plataforma.BD.Secuencias::REG.IdFeriado".NEXTVAL INTO v_IdFeriado FROM DUMMY;
			p_out_RefId := v_IdFeriado;
			
			INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado"(
				 "IdFeriado"
				,"Tipo"
				,"Fecha"
				,"Dia"
				,"DescripcionDia"
				,"AgnoCarga"
				,"IdPais"
				,"IdEstado"
			)
			VALUES(
				 v_IdFeriado
				,p_Tipo
				,p_Fecha
				,p_Dia
				,p_DescripcionDia
				,p_AgnoCarga
				,p_IdPais
				,'ACT'
			);
	
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
GO 
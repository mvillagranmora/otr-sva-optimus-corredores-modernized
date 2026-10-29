SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CambiarEstadoLibroOrdenesDetalle";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para modificar el estado de algún Elemento de la Sub-Grilla de Libro de Ordenes (Operadores, Segmento o Marca Neg.)
--  Autor: - MMACHMAR
--  Fecha Creacion: 07/03/2016 
--  
"BSCL.Orden.BD.Servicios::CambiarEstadoLibroOrdenesDetalle" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN p_IdLibroOrdenes			BIGINT,
	IN p_IdLibroOrdenesOperador	BIGINT,
	IN p_IdLibroOrdenesSegmento	BIGINT,
	IN p_IdMarcaNegociacion		BIGINT,
	IN p_IdEstado				NVARCHAR(3),
	IN p_FechaModificacion		NVARCHAR(27),
	IN p_Elemento				NVARCHAR(3),
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY
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
	
	BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_countElementoSubGrilla BIGINT := 0;
		DECLARE v_msjElementoSubGrilla NVARCHAR(40);
		
		--------------------
		--Validaciones
		--------------------
		-- Verifica que exista Libro de Ordenes
		BEGIN
			IF p_Elemento = 'OPE' THEN
				v_msjElementoSubGrilla := 'OPERADOR_NO_EXISTE';
				
				SELECT COUNT(1) INTO v_countElementoSubGrilla
				  FROM "BSCL.Orden.BD::ORD.LibroOrdenesOperador"
				 WHERE "IdLibroOrdenesOperador"	= :p_IdLibroOrdenesOperador
				   AND "IdLibroOrdenes"			= :p_IdLibroOrdenes
				   AND "IdEstado"				= 'ACT';
			ELSEIF p_Elemento = 'SEG' THEN
				v_msjElementoSubGrilla := 'SEGMENTO_NO_EXISTE';
				
				SELECT COUNT(1) INTO v_countElementoSubGrilla
				  FROM "BSCL.Orden.BD::ORD.LibroOrdenesSegmento"
				 WHERE "IdLibroOrdenesSegmento"	= :p_IdLibroOrdenesSegmento
				   AND "IdLibroOrdenes"			= :p_IdLibroOrdenes
				   AND "IdEstado"				= 'ACT';
			ELSEIF p_Elemento = 'MAR' THEN
				v_msjElementoSubGrilla := 'MARCA_NEGOCIACION_NO_EXISTE';
				
				SELECT COUNT(1) INTO v_countElementoSubGrilla
				  FROM "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"
				 WHERE "IdMarcaNegociacion"	= :p_IdMarcaNegociacion
				   AND "IdLibroOrdenes"		= :p_IdLibroOrdenes
				   AND "IdEstado"			= 'ACT';
			END IF;
		
			IF(v_countElementoSubGrilla = 0) THEN
				p_out_ErroresDetalle = select v_msjElementoSubGrilla as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			IF p_Elemento = 'OPE' THEN
				UPDATE LOOPE
				   SET LOOPE."IdEstado"					= p_IdEstado,
				       LOOPE."FechaModificacion"		= CURRENT_UTCTIMESTAMP
				  FROM "BSCL.Orden.BD::ORD.LibroOrdenesOperador" AS LOOPE
				 WHERE LOOPE."IdLibroOrdenesOperador"	= p_IdLibroOrdenesOperador
				   AND LOOPE."IdLibroOrdenes"			= p_IdLibroOrdenes
				   AND LOOPE."IdEmpresa"				= p_IdEmpresa;
			ELSEIF p_Elemento = 'SEG' THEN
				UPDATE LOSEG
				   SET LOSEG."IdEstado"					= p_IdEstado,
				       LOSEG."FechaModificacion"		= CURRENT_UTCTIMESTAMP
				  FROM "BSCL.Orden.BD::ORD.LibroOrdenesSegmento" AS LOSEG
				 WHERE LOSEG."IdLibroOrdenesSegmento"	= p_IdLibroOrdenesSegmento
				   AND LOSEG."IdLibroOrdenes"			= p_IdLibroOrdenes
				   AND LOSEG."IdEmpresa"				= p_IdEmpresa;
			ELSEIF p_Elemento = 'MAR' THEN
				UPDATE LOMNEG
				   SET LOMNEG."IdEstado"			= p_IdEstado,
				       LOMNEG."FechaModificacion"	= CURRENT_UTCTIMESTAMP
				  FROM "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion" AS LOMNEG
				 WHERE LOMNEG."IdMarcaNegociacion"	= p_IdMarcaNegociacion
				   AND LOMNEG."IdLibroOrdenes"		= p_IdLibroOrdenes
				   AND LOMNEG."IdEmpresa"			= p_IdEmpresa;
			END IF;
			
			UPDATE LIB
			   SET LIB."FechaModificacion" = CURRENT_UTCTIMESTAMP
			  FROM "BSCL.Orden.BD::ORD.LibroOrdenes" AS LIB
			 WHERE LIB."IdLibroOrdenes" = p_IdLibroOrdenes
			   AND LIB."IdEmpresa"		= p_IdEmpresa;
		END;
		--------------------
		--FIN SP
		--------------------
	END;
END;

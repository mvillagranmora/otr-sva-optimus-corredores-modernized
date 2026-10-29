SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CrearLibroOrdenesMarcasNegociacion";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para crear Marcas de Negociación a un Libro de Ordenes.
--  Autor: - MMACHMAR
--  Fecha Creacion: 07/03/2016 
--  
"BSCL.Orden.BD.Servicios::CrearLibroOrdenesMarcasNegociacion" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN p_IdLibroOrdenes		BIGINT,
	IN p_MarcaNegociacion	NVARCHAR(520),
	
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		DECLARE v_countLibroOrdenesMarcasNegociacion	BIGINT := 0;
		
		--------------------
		--Validaciones
		--------------------

		-- Verifica que Marca Negociacion NO este asociada a Libro de Ordenes
		BEGIN
			SELECT COUNT(1) INTO v_countLibroOrdenesMarcasNegociacion
			  FROM "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion" AS "MN"
			  LEFT JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" AS "LIB" ON "MN"."IdLibroOrdenes" = "LIB"."IdLibroOrdenes"
			 WHERE "MN"."MarcaNegociacion"	= :p_MarcaNegociacion
			   AND "MN"."IdEmpresa"			= :p_IdEmpresa
			   AND "MN"."IdEstado"			= 'ACT'
			   AND "LIB"."Borrado"			= 'N';
		
			IF(v_countLibroOrdenesMarcasNegociacion > 0) THEN
				p_out_ErroresDetalle = select 'MARCA_NEGOCIACION_YA_ASOCIADA_A_LIBRO_ORDENES' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			SELECT "BSCL.Orden.BD.Secuencias::LibroOrdenesMarcaNegociacion".NEXTVAL INTO p_out_RefId FROM DUMMY;
			
		 	------------------------------------------
			--Crear 
			------------------------------------------
			INSERT INTO "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion"(
				"IdMarcaNegociacion",
				"IdEmpresa",
				"IdLibroOrdenes",
				"MarcaNegociacion",
				"IdEstado",
				"FechaCreacion",
				"FechaModificacion")
			VALUES(
				p_out_RefId,
				p_IdEmpresa,
				p_IdLibroOrdenes,
				p_MarcaNegociacion,
				'ACT',
				CURRENT_UTCTIMESTAMP,
				CURRENT_UTCTIMESTAMP);
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
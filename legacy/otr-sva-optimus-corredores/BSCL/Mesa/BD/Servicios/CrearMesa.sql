SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::CrearMesa";
GO
-- 
--  Proposito: Crear Mesa RF
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 12/09/2016  
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::CrearMesa" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_Nombre				NVARCHAR(30) 	DEFAULT '',
	IN p_Codigo				NVARCHAR(40)	DEFAULT '',
	IN p_IdCliente			BIGINT			DEFAULT 0,
	IN p_IdPortafolio		BIGINT			DEFAULT 0,
	IN p_IdTipoProducto		NVARCHAR(4) 	DEFAULT '',
	IN p_ValorRazonable		NVARCHAR(255) 	DEFAULT '',
	IN p_IdEstadoMesa		NVARCHAR(3) 	DEFAULT 'A',
	IN p_IdEstadoRegistro	NVARCHAR(3) 	DEFAULT 'ACT',
	IN p_CodigoIFRS			NVARCHAR(3)     DEFAULT '',
	IN p_PorDefecto			NVARCHAR(1) 	DEFAULT 'N',
	IN p_AplicaFusion		NVARCHAR(1) 	DEFAULT 'N',

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
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        
		DECLARE v_Count			BIGINT:=0;
		DECLARE v_FechaIngreso	SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdMesa		BIGINT:=0;
		
		--------------------
		--Validaciones
		--------------------
		
		---Verifica que Codigo de Mesa no esté ingresado
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Mesa.BD.Vistas::ConsultarMesas"
		WHERE "Codigo" = :p_Codigo;
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle =  select 'CODIGO MESA EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		-- validacion para largo de campo Codigo y campo Nombre
		IF((LENGTH(p_Codigo) > 40) OR (LENGTH(p_Nombre) > 30)) THEN
			p_out_ErroresDetalle =  select 'ERROR LARGO CAMPO CODIGO NOMBRE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--Verifica no exista Mesa marcada Por Defecto para el mismo TipoProducto
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Mesa.BD::MES.Mesa" MES
		WHERE MES."IdTipoProducto" = p_IdTipoProducto
		  AND p_PorDefecto		= 'S'
		  AND MES."PorDefecto"	= 'S'
		  AND MES."IdEmpresa"	= p_IdEmpresa;
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'TIPO PRODUCTO YA TIENE MESA POR DEFECTO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		--Verifica que no haya mesa ma de 2 mesas con el mismo portafolio
		SELECT count(1) INTO v_Count
		FROM "BSCL.Mesa.BD::MES.Mesa" MES
        WHERE MES."IdTipoProducto" = p_IdTipoProducto
          AND MES."IdCliente" = p_IdCliente
          AND MES."IdPortafolio" = p_IdPortafolio
          AND MES."IdEstadoMesa" = 'A'
          AND MES."IdEmpresa"   = p_IdEmpresa;
          
        IF(v_Count > 0) THEN
            p_out_ErroresDetalle = select 'PORTAFOLIO ASIGNADO A OTRA MESA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			SELECT "BSCL.Mesa.BD.Secuencias::IdMesa".NEXTVAL INTO v_IdMesa FROM DUMMY;
			p_out_RefId := v_IdMesa;
			
			insert into "BSCL.Mesa.BD::MES.Mesa"(
				"IdMesa",
			    "IdEmpresa",
				"IdCliente",
				"IdPortafolio",
				"Codigo",
				"Nombre",
				"IdTipoProducto",
				"IdEstadoMesa",
				"IdEstadoRegistro",
				"IdUsuarioModificacion",
				"FechaModificacion",
				"PorDefecto",
				"ValorRazonable",
				"CodigoIFRS",
				"AplicaFusion"
			)
			VALUES(
				v_IdMesa,
				p_IdEmpresa,
				p_IdCliente,
				p_IdPortafolio,
				p_Codigo,
				p_Nombre,
				p_IdTipoProducto,
				p_IdEstadoMesa,
				p_IdEstadoRegistro,
				p_IdUsuario,
				v_FechaIngreso,
				p_PorDefecto,
				p_ValorRazonable,
				p_CodigoIFRS,
				p_AplicaFusion);
	
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

SET SCHEMA "BSCL";

DROP PROCEDURE   "BSCL.Plataforma.BD.Servicios::IND.AgregarDetalleIndicador";

CREATE PROCEDURE 
-- 
--  Proposito: Servicio para agregar el detalle de un Tipo de Indicador (Valores del tipo combobox) .
--  Autor: - CFLORES
--  Fecha Creacion: 07/04/2016 
--  
"BSCL.Plataforma.BD.Servicios::IND.AgregarDetalleIndicador" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
	
		  
	-- Parametros entrada del servicio publico
	
	IN	p_IdTipoIndicador      		bigint,
	IN  p_IdDetalleIndicador		bigint default 0,
	IN	p_DetalleIndicador         	NVARCHAR(255),
	IN	p_Codigo         			NVARCHAR(40),
	
	--IN	p_IdEstado								NVARCHAR(3),
	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
	
	,out p_out_depuracion nvarchar(1000)
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
		DECLARE v_IdDetalleIndicador		 		bigint:=0;
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
			
		DECLARE v_countDetalleIndicador	 	   bigint:=0;
		DECLARE v_countCodigoIndicador	 	   bigint:=0;
		DECLARE v_CodigoIndicadorOriginal	   NVARCHAR(40):='';
		--------------------
		--Validaciones
		--------------------
		BEGIN

			SELECT COUNT(1) INTO v_countCodigoIndicador
			FROM "BSCL.Plataforma.BD::IND.DetalleIndicador"
			WHERE "IdTipoIndicador" = :p_IdTipoIndicador 
			AND "Codigo" = :p_Codigo;
			
			IF(p_IdDetalleIndicador>0) THEN
				SELECT COUNT(1) INTO v_countDetalleIndicador
				FROM "BSCL.Plataforma.BD::IND.DetalleIndicador"
				WHERE "IdDetalleIndicador" = p_IdDetalleIndicador;
			END IF;
			
			IF (v_countDetalleIndicador>0) THEN
				SELECT "Codigo" INTO v_CodigoIndicadorOriginal
				FROM "BSCL.Plataforma.BD::IND.DetalleIndicador"
				WHERE "IdDetalleIndicador" = p_IdDetalleIndicador;
			END IF;
				
			IF(v_countCodigoIndicador>0 AND p_Codigo != v_CodigoIndicadorOriginal)THEN
				p_out_ErroresDetalle = select 'DETALLE_EXISTE' as "Codigo",'UI_DETALLE_EXISTE' as "Descripcion" from dummy;
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
			
			IF (p_IdDetalleIndicador > 0) THEN
				-- MODIFICACION
				 UPDATE "BSCL.Plataforma.BD::IND.DetalleIndicador" 
				 SET "DetalleIndicador" = p_DetalleIndicador,
				 	"Codigo" = p_Codigo
				 WHERE "IdDetalleIndicador" = p_IdDetalleIndicador;
			 
			ELSE 
				-- CREACION
				SELECT "BSCL.Plataforma.BD.Secuencias::DetalleIndicador".NEXTVAL 
				 INTO v_IdDetalleIndicador 
				 FROM DUMMY;
				 
				 p_out_RefId:=v_IdDetalleIndicador;
				 
				 insert into "BSCL.Plataforma.BD::IND.DetalleIndicador" 
				 	( "IdDetalleIndicador",
					"IdTipoIndicador",
					"DetalleIndicador",
					"Codigo" )
				 VALUES
				 ( v_IdDetalleIndicador,
					p_IdTipoIndicador,
					p_DetalleIndicador,
					p_Codigo);
			END IF;
	
		END;
		
		
		
		p_out_RefId := 1;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

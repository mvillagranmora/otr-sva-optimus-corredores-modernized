SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::CrearComprobanteFormaDePago";
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::CrearComprobanteFormaDePago" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_tablaFormaPago		TABLE(
								 "IdOperacion"					BIGINT
								,"IdTipoProducto"				NVARCHAR(4)
								,"IdTipoMovimiento"				NVARCHAR(6)
								,"IdMedioDePago"				NVARCHAR(3)
								,"NombreMedioDePago"			NVARCHAR(100)
								,"IdModalidadPago"				NVARCHAR(6)
								,"NombreModalidadDePago"		NVARCHAR(100)
								,"IdMoneda"						NVARCHAR(3)
								,"Monto" 						DECIMAL(26,8)
								,"FechaIngresoRegistro"		 	NVARCHAR(10)
								,"FechaCondicionLiquidacion"	NVARCHAR(10)
								,"IdCartera"					BIGINT
							),
	OUT p_out_FormaPagoIngresados TABLE(
							  		"IdOperacion"					BIGINT,
							  		"IdComprobanteFormaDePago" 		BIGINT
							  ) DEFAULT EMPTY,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("IdOperacion" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
								   select 0,v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
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
	p_out_ErroresDetalle		= select 0 as "IdOperacion",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
		--------------------
		--Declare Variables
		--------------------		
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
		INSERT INTO "BSCL.Mesa.BD::ComprobanteFormaDePago"
		(
			  "IdComprobanteFormaDePago" 	
			, "IdEmpresa"					
			, "FolioOperacion"				
			, "IdTipoProducto" 				
			, "IdTipoMovimiento" 			
			, "IdMedioDePago" 				
			, "NombreMedioDePago" 			
			, "IdModalidadPago" 			
			, "NombreModalidadDePago" 		
			, "IdMoneda" 					
			, "Monto" 						
			, "FechaIngresoRegistro" 		
			, "FechaCondicionLiquidacion"	
			, "IdEstado"
			, "IdCartera"
		)
		SELECT "BSCL.Mesa.BD.Secuencias::IdComprobanteFormaDePago".NEXTVAL AS "IdComprobanteFormaDePago" 
			 , :p_IdEmpresa    			AS "IdEmpresa"
			 , FMP."IdOperacion" 		AS "FolioOperacion"				
			 , FMP."IdTipoProducto" 				
			 , FMP."IdTipoMovimiento" 			
			 , FMP."IdMedioDePago" 				
			 , FMP."NombreMedioDePago" 			
			 , FMP."IdModalidadPago" 			
			 , FMP."NombreModalidadDePago" 		
			 , FMP."IdMoneda" 					
			 , FMP."Monto" 						
			 , FMP."FechaIngresoRegistro" 		
			 , FMP."FechaCondicionLiquidacion"	
			 , 'VIG'
			 , FMP."IdCartera"
		FROM :p_tablaFormaPago AS FMP;
		
		
		p_out_FormaPagoIngresados = SELECT COMP."FolioOperacion" AS "IdOperacion"
										 , COMP."IdComprobanteFormaDePago"
									FROM "BSCL.Mesa.BD::ComprobanteFormaDePago" AS COMP
									INNER JOIN :p_tablaFormaPago AS FMP
											ON FMP."IdOperacion" = COMP."FolioOperacion"
										   AND FMP."IdTipoProducto" = COMP."IdTipoProducto";
		
		SELECT COALESCE(COUNT(1),0) AS "Count"
		INTO p_out_ErroresContador
		FROM :p_out_FormaPagoIngresados;
		
		IF(:p_out_ErroresContador = 0)THEN
			p_out_ErroresDetalle = SELECT "IdOperacion"
										, '1' as "Codigo"
			                            , 'Problema al Ingreso Formas de Pago.' as "Descripcion" 
			                       FROM :p_tablaFormaPago
			                       GROUP BY "IdOperacion";
		END IF;
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

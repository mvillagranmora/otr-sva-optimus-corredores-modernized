SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobrosSinAut";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que modifica un perfil de cobros, sin seguir el modelo de autorizaciones.
-- 	Debe ser reemplazado una vez en las autorizaciones se implemente identificador de entidades a alfanumericas.
--  Autor: JGARCES
--  Fecha Creacion: 19/01/2016
--  
"BSCL.Plataforma.BD.Servicios::COM.ModificarPerfilCobrosSinAut" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	IN  p_DescripcionPerfilCobros					NVARCHAR(100),
	IN  p_EsPerfilPersonalizado						NVARCHAR(1),
	IN  p_IdCategoriaInversionista					NVARCHAR(40),
	IN  p_IdEstado									NVARCHAR(3),
	IN  p_IdPerfilCobros							NVARCHAR(40),
	IN  p_PorDefecto								NVARCHAR(1),
	
	IN p_DetallePerfil TABLE (
		p_IdDetallePerfilCobros		BIGINT,
		p_DiasPlazo					INT,
		p_IdBolsa					NVARCHAR(40),
		p_IdCanalIngreso			NVARCHAR(40),
		p_IdEstado					NVARCHAR(3),
		p_IdTipoOperacion			BIGINT,
		p_IdTipoProducto			NVARCHAR(40),
		p_IdValorCobro				NVARCHAR(40),
		p_Prioridad 				BIGINT	
	),

	
	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
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
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
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
			
			
		--------------------
		--Validaciones
		--------------------
		

		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			
			


		p_out_RefId:= ::ROWCOUNT;

	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarFormaDePagoEventoCapitalCliente";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarFormaDePagoEventoCapitalCliente" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
	IN p_IdEmpresa 						BIGINT 			default 0,
	IN p_IdUsuario 						BIGINT 			default 0,
	IN p_IdModulo 						NVARCHAR(3) 	default '',  
	
	IN p_UsaReglasNegocio 				NVARCHAR(1) 	default 'N',
	
	IN p_IdEventoCapitalCliente			BIGINT 			default 0,
	IN p_IdPreferenciaPagoPortafolio	BIGINT 			default 0,

	-- PARAMETROS DE SALIDA
	OUT p_out_AdvertenciasContador	Int,
	OUT p_out_AdvertenciasDetalle 	Table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		Int,   
	OUT p_out_ErroresDetalle 		Table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)))   
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;	
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
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
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
		DECLARE v_CuentaRegistro		BIGINT;
		DECLARE v_IdModalidadDePago		NVARCHAR(40);
		DECLARE v_Monto					DECIMAL(26,8);

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
	 	v_CuentaRegistro	:= 0;
		v_IdModalidadDePago	:= '';
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
			--OBTENGO MONTO DE LA OPERACION
		SELECT "Total" INTO v_Monto
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
		WHERE "IdEventoCapitalCliente" = :p_IdEventoCapitalCliente;

		SELECT Count(1) INTO v_CuentaRegistro
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"
		WHERE "IdEventoCapitalCliente" = p_IdEventoCapitalCliente;
		
		IF :v_CuentaRegistro = 0 THEN
			-- no encuentra forma de pago, se debe insertar

			INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"
			(	"IdEventoCapitalClienteFormaDePago", 
				"IdEventoCapitalCliente", 
				"IdTipoMovimiento", 
				"IdModalidadPago", 
				"IdMedioDePago", 
				"NombreMedioDePago", 
				"IdBanco", 
				"NombreBanco", 
				"IdTipoCuentaBancaria", 
				"NombreTipoCuentaBancaria", 
				"IdCuentaBancaria", 
				"NombreCuentaBancaria", 
				"NumeroCuentaBancaria", 
				"IdTipoTarjetaBancaria", 
				"NumeroTarjetaBancaria", 
--					"NumeroMedioDePago", 
--					"Sucursal", 
--					"FechaVctoDocumento", 
				"Monto", 
				"FechaHoraIngresoRegistro", 
				"IdUsuarioIngreso", 
				"IdEstado", 
				"IdPreferenciaPagoPortafolio")

			SELECT 
				"BSCL.Custodia.BD.Secuencias::EventoCapitalClienteFormaDePago".NEXTVAL,
				p_IdEventoCapitalCliente, 
				'I', 
				IFNULL(MdP."IdModalidadDePago",'MODCAJ'),
				IFNULL(CPPEC."IdMedioDePago",''),
				IFNULL(CPPEC."MedioDePago",''),
				IFNULL(CPPEC."IdBancoCuentaBancaria",0),
				IFNULL(CPPEC."NombreBancoCuentaBancaria",''),
				IFNULL(CPPEC."IdTipoCuentaBancaria",''),
				IFNULL(CPPEC."NombreTipoCuentaBancaria",''),
				IFNULL(CPPEC."IdCuentaBancaria",0),
				'' 							AS "NombreCuentaBancaria",
				IFNULL(CPPEC."NumeroCuentaBancaria",''),
				IFNULL(CPPEC."IdTipoTarjetaBancaria",''),
				IFNULL(CPPEC."NumeroTarjetaBancaria",''),
				:v_Monto,
				CURRENT_UTCTIMESTAMP,
				p_IdUsuario,
				'VIG' AS "IdEstado", 
				p_IdPreferenciaPagoPortafolio
			FROM "BSCL.Custodia.BD.Vistas::EVC.ConsultarPreferenciaPagoEventoCapital"	CPPEC
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago"		MdP
					ON CPPEC."IdMedioDePago" 		= MdP."IdMedioDePago"
			WHERE CPPEC."IdPreferenciaPagoPortafolio" = p_IdPreferenciaPagoPortafolio;
			
		ELSE
   
			UPDATE ECCFP SET
				"IdPreferenciaPagoPortafolio"	= CPPEC."IdPreferenciaPagoPortafolio",
				"IdModalidadPago"				= IFNULL(MdP."IdModalidadDePago",'MODCAJ'),
				"IdMedioDePago"					= IFNULL(CPPEC."IdMedioDePago",''),
				"NombreMedioDePago"				= IFNULL(CPPEC."MedioDePago",''),
				"IdBanco"						= IFNULL(CPPEC."IdBancoCuentaBancaria",0),
				"NombreBanco"					= IFNULL(CPPEC."NombreBancoCuentaBancaria",''),
				"IdTipoCuentaBancaria"			= IFNULL(CPPEC."IdTipoCuentaBancaria",''),
				"NombreTipoCuentaBancaria"		= IFNULL(CPPEC."NombreTipoCuentaBancaria",''),
				"IdCuentaBancaria"				= IFNULL(CPPEC."IdCuentaBancaria",0),
--				"NombreCuentaBancaria"
				"NumeroCuentaBancaria"			= IFNULL(CPPEC."NumeroCuentaBancaria",''),
				"IdTipoTarjetaBancaria"			= IFNULL(CPPEC."IdTipoTarjetaBancaria",''),
				"NumeroTarjetaBancaria"			= IFNULL(CPPEC."NumeroTarjetaBancaria",''),
--				"NumeroMedioDePago"
				"FechaHoraIngresoRegistro"		= CURRENT_UTCTIMESTAMP,
				"IdUsuarioIngreso"				= p_IdUsuario
			FROM "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago" 						ECCFP
				INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 						ECC
					ON ECCFP."IdEventoCapitalCliente" = ECC."IdEventoCapitalCliente"
				LEFT JOIN "BSCL.Custodia.BD.Vistas::EVC.ConsultarPreferenciaPagoEventoCapital"	CPPEC
					ON ECC."IdPortafolio" 		= CPPEC."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago"				MdP
					ON CPPEC."IdMedioDePago" 		= MdP."IdMedioDePago"
			WHERE  ECCFP."IdEventoCapitalCliente" = p_IdEventoCapitalCliente
			   AND CPPEC."IdPreferenciaPagoPortafolio" = p_IdPreferenciaPagoPortafolio;

		END IF;
		-----------------------------------------------------
		--FIN PROCESO DE NEGOCIO
		---------------------------------------------------
	END;
END; 

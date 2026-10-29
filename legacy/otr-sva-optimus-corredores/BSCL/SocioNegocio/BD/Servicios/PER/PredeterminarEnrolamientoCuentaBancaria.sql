SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.PredeterminarEnrolamientoCuentaBancaria";

CREATE PROCEDURE
--  Creado a partir de PredeterminarPersonaDireccionElectronica.sql
--  Proposito: Servicio que predetermina la Cuenta Bancaria de una persona
--  para ser creado como una solicitud de Cambio.
--  Autor: HBravo_ext - 
--  Fecha Creacion: 04-07-2022
--  
"BSCL.SocioNegocio.BD.Servicios::PER.PredeterminarEnrolamientoCuentaBancaria" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdCuentaBancaria						   	BIGINT,	
	IN  p_CrearPreferenciaPago						NVARCHAR(1) DEFAULT 'N',
	-- Parametros salida del servicio publico
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
			DECLARE v_tipoEntidad									NVARCHAR(40);
			DECLARE v_IdConcepto	       							NVARCHAR(6);
			DECLARE v_Evento      									BIGINT:=1;
			DECLARE v_TolalEventos						      		BIGINT:=0;	
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_IdEntidadBase									BIGINT:=0;
			DECLARE v_countEntidad			   					    BIGINT;
			DECLARE v_estadoAutorizacion							NVARCHAR(3);
			DECLARE v_RequiereAutorizacion							NVARCHAR(1);
			DECLARE v_secuencia_IdSolicitudCambio					BIGINT;
			DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
			DECLARE p_IdPersonaRelacionada							BIGINT;
			
		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			DECLARE v_fechaHoraSistema NVARCHAR(40);
			DECLARE v_id_moneda		   NVARCHAR(40)='';
			
			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdCuentaBancaria AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countEntidad FROM "BSCL.SocioNegocio.BD::ENR.CuentaBancaria" WHERE "IdCuentaBancaria"=p_IdCuentaBancaria;
			
			IF(v_countEntidad=0) THEN
				p_out_ErroresDetalle =  select 'ENTIDAD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
				ELSE
					SELECT "IdEnrolamiento" INTO v_IdEntidadBase FROM "BSCL.SocioNegocio.BD::ENR.CuentaBancaria" WHERE "IdCuentaBancaria"=p_IdCuentaBancaria;
			END IF;
										
		END;		
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			--Inicializacion de variables:
			v_secuencia_IdSolicitudCambio 	:= 	-1;
			
			v_estadoAutorizacion 			:= 'PEN';
			v_tipoEntidad					:= 'CUENTABANCARIA-PREDETERMINAR';
			v_IdConcepto					:= 'PRDDEA';
			p_out_RefId						:=	0;
			p_IdPersonaRelacionada			:= 	0;
			
			
			-- 1) OBTENCION DE VALORES SEGUN CAMPOS ENVIADOS DESDE UI
			-- (Se obtiene: Tipo de Dato y Valor Anterior del campo modificado
			
			IF p_CrearPreferenciaPago = 'S' THEN

				DECLARE v_IdMoneda NVARCHAR(3);
				--DECLARE v_IdCliente BIGINT;

				SELECT CB."IdMoneda"--, PER."IdEnrolamiento"
					INTO v_IdMoneda--, v_IdCliente
					FROM "BSCL.SocioNegocio.BD::ENR.CuentaBancaria" CB
					-- INNER JOIN  "BSCL.SocioNegocio.BD::ENR.Enrolamiento" PER ON PER."IdEnrolamiento" = CB."IdEnrolamiento"
					WHERE CB."IdCuentaBancaria" = v_IdEntidadOrigen;

				UPDATE	DOC
					SET	DOC."PorDefecto" = 'N'
					FROM "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio" DOC
					INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE ON PRE."IdPreferenciaPago" = DOC."IdPreferenciaPago"
					WHERE
						DOC."IdEnrolamiento" = v_IdEntidadBase --v_IdCliente
						AND PRE."IdMoneda" = v_IdMoneda
						AND PRE."RecibePagaPortafolio" = 'R'
						AND PRE."IdTipoProducto" NOT IN ('MEME', 'MERF', 'MEFW');

				DELETE FROM "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio" 
					WHERE "IdCuentaBancaria" = p_IdCuentaBancaria;

				INSERT INTO
					"BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio" (
						"IdPreferenciaPagoPortafolio",
						"IdEnrolamiento",
						"IdPreferenciaPago",
						"IdPersonaRelacionada",
						"IdCuentaBancaria",
						"IdEmpresa",
						"IdTarjetaBancaria",
						"PorDefecto"
					)
					WITH Enrolamiento AS (
						SELECT "IdEnrolamiento"
						FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento"
						WHERE "IdEnrolamiento" = v_IdEntidadBase
					)
					SELECT
						"BSCL.SocioNegocio.BD.Secuencias::BorradorPreferenciaPagoPortafolio".NEXTVAL,
						ENR."IdEnrolamiento",
						PRE."IdPreferenciaPago",
						p_IdPersonaRelacionada,
						p_IdCuentaBancaria,
						p_IdEmpresa,
						0,
						'S'
					FROM
						Enrolamiento AS ENR
					    CROSS JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" AS  PRE
					WHERE
						PRE."IdMedioDePago" = 'TRA'
						AND PRE."IdMoneda" = v_IdMoneda
						AND PRE."RecibePagaPortafolio" = 'R'
						AND PRE."IdTipoProducto" NOT IN ('MEME', 'MERF', 'MEFW')
						AND PRE."IdEmpresa" = p_IdEmpresa;

			END IF;
			-- ........................................................................

				v_persona_cuentabancaria = SELECT "IdEnrolamiento", "IdCuentaBancaria"
						FROM "BSCL.SocioNegocio.BD::ENR.CuentaBancaria" CTB
						WHERE 
							 CTB."IdCuentaBancaria"	= v_IdEntidadOrigen;
						
						--quitar como predeterminado la Cuenta Bancaria anterior de esa moneda
						SELECT "IdEnrolamiento", "IdMoneda" INTO v_IdEntidadBase, v_id_moneda 
						FROM "BSCL.SocioNegocio.BD::ENR.CuentaBancaria" WHERE "IdCuentaBancaria" = :p_IdCuentaBancaria;
						
						UPDATE "BSCL.SocioNegocio.BD::ENR.CuentaBancaria"
						SET "Predeterminado" = 'N'
						WHERE "IdEnrolamiento" = v_IdEntidadBase AND "IdMoneda" = v_id_moneda;

				UPDATE
					CTB
				SET
					CTB."Predeterminado" = 'S'
				FROM
					"BSCL.SocioNegocio.BD::ENR.CuentaBancaria" CTB , :v_persona_cuentabancaria PDE
					WHERE
					CTB."IdEnrolamiento" = PDE."IdEnrolamiento"
					AND CTB."IdCuentaBancaria" = v_IdEntidadOrigen;

				UPDATE P
					SET P."IdCuentaBancariaPrincipal" = p_IdCuentaBancaria
					FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" P, :v_persona_cuentabancaria PDE
					WHERE P."IdEnrolamiento" = PDE."IdEnrolamiento";
		END;				
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
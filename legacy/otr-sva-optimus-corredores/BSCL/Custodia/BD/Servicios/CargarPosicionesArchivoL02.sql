SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::CargarPosicionesArchivoL02";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::CargarPosicionesArchivoL02" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca
--	IN p_Fecha					NVARCHAR(10) default '0001-01-01',
	IN p_Fecha					DATE,

	-- Parametros entrada del servicio privado
	IN p_DataArchivo			TABLE (	"CUENTA"						NVARCHAR(8),
										"NEMO"							NVARCHAR(12),
										"MADUREZ"						DECIMAL(26,8),
										"POSICIONDISPONIBLE"			DECIMAL(26,8),
										"POSICIONBLOQUEADA"				DECIMAL(26,8),
										"POSICIONREVENTA"				DECIMAL(26,8),
										"POSICIONRECOMPRA"				DECIMAL(26,8),
--										"POSICIONTOTALVALORIZADAUF"		DECIMAL(26,8),
										"MENSAJEVALIDACION" 			NVARCHAR(200)),
	
	-- Parametros entrada del servicio privado
	IN p_DataArchivoEmisores	TABLE (	"Boveda"				NVARCHAR(3),
										"Nemotecnico"			NVARCHAR(13),
										"PosicionBoveda"		DECIMAL(26,0),
										"MENSAJEVALIDACION" 	NVARCHAR(200)),

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(41),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(41),"Descripcion" nvarchar(5000))
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
		DECLARE v_ExistenRegistros INT := 0;
		---------------------------------------------------------------
		-- Archivo Posiciones L02
		---------------------------------------------------------------
		SELECT COUNT(*) INTO v_ExistenRegistros FROM :p_DataArchivo;
		IF(v_ExistenRegistros > 0) THEN
			--------------------
			--Variables
			--------------------
			DECLARE	v_FechaSistemaCreacion		NVARCHAR(20);
			DECLARE	v_countTransacciones		BIGINT;
			DECLARE	v_countOrdenes				BIGINT;
			DECLARE	v_countClientes				BIGINT;
			DECLARE	v_countAsignacion			BIGINT;
			DECLARE v_ErroresContadorArchivo	BIGINT;
			DECLARE v_ErroresContadorCustodia	BIGINT;
			DECLARE v_IdPersonaCPro				BIGINT;
			DECLARE v_DCVCuentaPropia			NVARCHAR(40):='';
			DECLARE v_DCVCuentaTerceros			NVARCHAR(40):='';
			DECLARE v_DCVCuentaRelacionado		NVARCHAR(40):='';
			DECLARE v_DCVCuentaRetencion		NVARCHAR(40):='';
			--------------------
			-- Validaciones
			-- Si se encuentra algún registro, no se puede cargar el archivo.
			--------------------
	
			BEGIN -- VALIDA ARCHIVO
	
				SELECT COUNT(1) INTO v_ErroresContadorArchivo
				FROM :p_DataArchivo			AS Archivo
				WHERE
				   TRIM(Archivo."NEMO")					= 	''		-- NEMO NO PUEDE VENIR EN BLANCO
				OR TRIM(Archivo."CUENTA")				=	''		-- CUENTA NO PUEDE VENIR EN BLANCO
				OR (Archivo."POSICIONDISPONIBLE"		<=	0	
				AND Archivo."POSICIONBLOQUEADA" 		<=	0
				AND Archivo."POSICIONREVENTA"			<=	0
				AND Archivo."POSICIONRECOMPRA"			<=	0) -- NO PUEDE VENIR SIN POSICIÓN
				OR TRIM(Archivo."MENSAJEVALIDACION")	!=	'OK';	-- VALIDACIÓN CON ERROR
											
				IF(v_ErroresContadorArchivo > 0) THEN
					p_out_ErroresContador = 10;
					p_out_ErroresDetalle = select 'ARCHIVO_L02_CON_ERRORES_REVISAR' as "Codigo",'ARCHIVO_L02_CON_ERRORES_REVISAR' as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
	
				SELECT COUNT(1) INTO v_ErroresContadorCustodia
				FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"	AS Custodia
				WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
				  AND Custodia."IdEstado"					= 'VIG';
	
	
				IF(v_ErroresContadorCustodia < 1) THEN
					p_out_ErroresContador = 10;
					p_out_ErroresDetalle = select 'CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA' as "Codigo",'CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA' as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
	
			END;
	
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
			
			--- OBTENEMOS ID PERSONA DE LA CORREDORA
			SELECT CC."IdPersona" into v_IdPersonaCPro
			FROM "BSCL.Plataforma.BD::PLA.Empresa" AS E
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS CSN ON CSN."IdSocioDeNegocio" = E."IdSocioDeNegocio"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CC ON CC."IdCliente" = CSN."IdCliente"
			WHERE E."IdEmpresa" = p_IdEmpresa;
	
			--- OBTENEMOS CUENTAS CONFIGURADAS EN EL SISTEMA
			SELECT "Valor" INTO v_DCVCuentaPropia
			FROM "BSCL.Custodia.BD::CUS.Configuracion"
			WHERE "CodigoParametro" = 'CuentaDCVCuentaPropia'
			  AND "IdEmpresa" = p_IdEmpresa;
								
			SELECT "Valor" INTO v_DCVCuentaTerceros
			FROM "BSCL.Custodia.BD::CUS.Configuracion"
			WHERE "CodigoParametro" = 'CuentaDCVCuentaTerceros'
			  AND "IdEmpresa" = p_IdEmpresa;
								
			SELECT "Valor" INTO v_DCVCuentaRelacionado
			FROM "BSCL.Custodia.BD::CUS.Configuracion"
			WHERE "CodigoParametro" = 'CuentaDCVCuentaRelacionado'
			  AND "IdEmpresa" = p_IdEmpresa;
			  
			 
			SELECT "Valor" INTO v_DCVCuentaRetencion
			FROM "BSCL.Custodia.BD::CUS.Configuracion"
			WHERE "CodigoParametro" = 'CuentaDCVRetencion'
			  AND "IdEmpresa" = p_IdEmpresa;
			  
			 
			 
		    -----------------------------------------------------------------
			-- SE BUSCAN LAS CUENTAS MANDANTES DE LOS CLIENTES Y SI SON O NO RELACIONADOS 
			-- y SE ACTUALIZA LA CUENTA A LA CORRESPONDIENTE.
			-----------------------------------------------------------------
			
			v_tblRelacionados = SELECT distinct PER."IdPersonaDestino"
								FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" PER
								WHERE PER."IdPersonaOrigen" = v_IdPersonaCPro --Corredor
								AND PER."IdEstado"='ACT';
			
			v_CuentasMandantesCliente = SELECT 	CUENTAS."Codigo"
												,PORTAFOLIO."IdCliente"
												,CASE WHEN ifnull(REL."IdPersonaDestino",0) = 0 THEN 'N' ELSE 'S' END AS "EsRelacionado"
										FROM "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CUENTAS 
										INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PORTAFOLIO ON (CUENTAS."IdPortafolio" = PORTAFOLIO."IdPortafolio")
										INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE ON (CLIENTE."IdCliente" = PORTAFOLIO."IdCliente")
										INNER JOIN :p_DataArchivo AS ARCHIVO ON (RTRIM(ARCHIVO."CUENTA") = RTRIM(CUENTAS."Codigo"))
										LEFT JOIN  :v_tblRelacionados AS REL ON (REL."IdPersonaDestino" = CLIENTE."IdPersona")
										WHERE 
												CUENTAS."IdBoveda" = 'DCV' 
												AND CUENTAS."IdEstado" = 'ACT'
												AND ARCHIVO."CUENTA" NOT IN (v_DCVCuentaPropia,v_DCVCuentaTerceros,v_DCVCuentaRelacionado,v_DCVCuentaRetencion)
										GROUP BY CUENTAS."Codigo",PORTAFOLIO."IdCliente",REL."IdPersonaDestino";
										
			
			v_DatosCuentaMandantes = SELECT CASE CLIENTE."EsRelacionado" WHEN 'S' THEN v_DCVCuentaRelacionado ELSE v_DCVCuentaTerceros END AS "CUENTA"					
									,ARCHIVO."NEMO"
									,ARCHIVO."MADUREZ"
									,ARCHIVO."POSICIONDISPONIBLE"
									,ARCHIVO."POSICIONBLOQUEADA"
									,ARCHIVO."POSICIONREVENTA"
									,ARCHIVO."POSICIONRECOMPRA"
									FROM :p_DataArchivo ARCHIVO
									INNER JOIN :v_CuentasMandantesCliente CLIENTE ON (RTRIM(ARCHIVO."CUENTA") = RTRIM(CLIENTE."Codigo"));
	
			-----------------------------------------------------------------
			-- PREPARAMOS INFORMACIÓN PARA POSTERIORMENTE PODER ACTUALIZAR.
			-----------------------------------------------------------------
			v_CargaDePosiciones = 
				SELECT 	Custodia."Nemotecnico" 				as Nemotecnico,
						Custodia."CuentaBoveda" 			as CuentaBoveda,
						Custodia."FechaCuadraturaCustodia" 	as FechaCuadraturaCustodia,
						CASE
		   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRetencion AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					ELSE
		   						0
	   					END AS PosicionBoveda,
						CASE
							WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRetencion AND Archivo."POSICIONBLOQUEADA" > 0) 
		   						THEN Archivo."POSICIONBLOQUEADA"
	   						ELSE
	   							0
	   					END AS PosicionBloqueada	,
						CASE
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRetencion AND Archivo."POSICIONREVENTA" > 0) 
		   						THEN Archivo."POSICIONREVENTA"
	   						ELSE
	   							0
	   					END AS PosicionReventa,
						CASE
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaPropia AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRetencion AND Archivo."POSICIONRECOMPRA" > 0) 
		   						THEN Archivo."POSICIONRECOMPRA"
	   						ELSE
	   							0
	   					END AS PosicionRecompra
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
				INNER JOIN :p_DataArchivo 					AS Archivo
					ON RTRIM(Archivo."CUENTA")	= RTRIM(Custodia."CuentaBoveda")
				   AND Rtrim(Archivo."NEMO")	= Custodia."Nemotecnico"
			WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
			  AND Custodia."IdBoveda"					= 'DCV'
			  AND Custodia."IdEstado"					= 'VIG';
			
			  
			-----------------------------------------------------------------
			-- PREPARAMOS INFORMACIÓN PARA POSTERIORMENTE PODER INSERTAR.
			-----------------------------------------------------------------
			v_CargaDePosicionesFaltantes = SELECT 	
						Archivo."NEMO" 				as Nemotecnico,
						Archivo."CUENTA" 			as CuentaBoveda,
						CASE
		   					WHEN (Archivo."CUENTA" = v_DCVCuentaPropia AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Archivo."CUENTA" = v_DCVCuentaRetencion AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					ELSE
		   						0
	   					END AS PosicionBoveda,
						CASE
							WHEN (Archivo."CUENTA" = v_DCVCuentaPropia AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Archivo."CUENTA" = v_DCVCuentaRetencion AND Archivo."POSICIONBLOQUEADA" > 0) 
		   						THEN Archivo."POSICIONBLOQUEADA"
	   						ELSE
	   							0
	   					END AS PosicionBloqueada	,
						CASE
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaPropia AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaRetencion AND Archivo."POSICIONREVENTA" > 0) 
		   						THEN Archivo."POSICIONREVENTA"
	   						ELSE
	   							0
	   					END AS PosicionReventa,
						CASE
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaPropia AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaRetencion AND Archivo."POSICIONRECOMPRA" > 0) 
		   						THEN Archivo."POSICIONRECOMPRA"
	   						ELSE
	   							0
	   					END AS PosicionRecompra
			FROM :p_DataArchivo AS Archivo
			LEFT JOIN "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
					ON RTRIM(Archivo."CUENTA") = RTRIM(Custodia."CuentaBoveda")
				   AND Rtrim(Archivo."NEMO") = Custodia."Nemotecnico"
				   AND Custodia."FechaCuadraturaCustodia" = p_Fecha
				   AND Custodia."IdBoveda" = 'DCV'
				    AND Custodia."IdEstado"	= 'VIG'
			WHERE Custodia."IdCuadraturaCustodia" IS NULL;  
			
			INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCustodia"(
				 "IdCuadraturaCustodia"
				,"IdBoveda"
			    ,"IdInstrumento"
				,"IdTipoInstrumento"
				,"IdClasificacionTipoInstrumento"
				,"Nemotecnico"
				,"PosicionBoveda"
				,"PosicionBloqueada"
				,"PosicionReventa"
				,"PosicionRecompra"
				,"PosicionTotal"
				,"Diferencia"
				,"FechaCuadraturaCustodia"
				,"TipoCuenta"
				,"IdEstado"
				,"IdUsuario"
				,"CuentaBoveda"
				,"FechaModificacion"
				,"NombreBeneficiario"
				,"RutBeneficiario"
			)
			SELECT "BSCL.Custodia.BD.Secuencias::CuadraturaCustodia".NEXTVAL AS "IdCuadraturaCustodia"
			     , 'DCV'
				 , COALESCE(Instrumento."IdInstrumento",0)
				 , COALESCE(Instrumento."IdTipoInstrumento",'')
				 , COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'')
			     , Carga.Nemotecnico
			     , PosicionBoveda
			     , PosicionBloqueada
			     , PosicionReventa
			     , PosicionRecompra
			     , PosicionBoveda + PosicionBloqueada + PosicionReventa + PosicionRecompra
			     , 0 - (PosicionBoveda + PosicionBloqueada + PosicionReventa + PosicionRecompra)
			     , :p_Fecha
			     , CASE 
					WHEN Carga.CuentaBoveda = v_DCVCuentaPropia then 'CPRO' 
					WHEN Carga.CuentaBoveda = v_DCVCuentaRelacionado then 'REL'
					ELSE 'TER' 
				 END
			     , 'VIG'
			     , :p_IdUsuario
			     , CASE 
					WHEN Carga.CuentaBoveda = v_DCVCuentaPropia then v_DCVCuentaPropia
					WHEN Carga.CuentaBoveda = v_DCVCuentaRelacionado then v_DCVCuentaRelacionado
					ELSE v_DCVCuentaTerceros
				 END
			     , :v_FechaSistemaCreacion
			     ,''
			     ,''
			FROM :v_CargaDePosicionesFaltantes AS Carga
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
					ON Instrumento."Nemotecnico" = Carga.Nemotecnico
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
					ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento";
			
			
			  
		    --------------------------------------------------------------------------------------
			-- PREPARAMOS INFORMACIÓN DE CUENTAS MANDATORIAS PARA POSTERIORMENTE PODER ACTUALIZAR.
			--------------------------------------------------------------------------------------
			v_CargaDePosicionesMandatorias = 
				SELECT 	Custodia."Nemotecnico" 				as Nemotecnico,
						Custodia."CuentaBoveda" 			as CuentaBoveda,
						Custodia."FechaCuadraturaCustodia" 	as FechaCuadraturaCustodia,
						CASE
		   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					ELSE
		   						0
	   					END AS PosicionBoveda,
						CASE
							WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
	   						ELSE
	   							0
	   					END AS PosicionBloqueada	,
						CASE
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						ELSE
	   							0
	   					END AS PosicionReventa,
						CASE
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaTerceros AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Custodia."CuentaBoveda" = v_DCVCuentaRelacionado AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						ELSE
	   							0
	   					END AS PosicionRecompra
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
				INNER JOIN :v_DatosCuentaMandantes 			AS Archivo
					ON RTRIM(Archivo."CUENTA")	= RTRIM(Custodia."CuentaBoveda")
				   AND Rtrim(Archivo."NEMO")	= Custodia."Nemotecnico"
			WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
			  AND Custodia."IdBoveda"					= 'DCV'
			  AND Custodia."IdEstado"					= 'VIG';
			  
			--------------------------------------------------------------------------------------
			-- PREPARAMOS INFORMACIÓN DE CUENTAS MANDATORIAS PARA POSTERIORMENTE PODER INSERTAR.
			--------------------------------------------------------------------------------------
			  
			v_CargaDePosicionesMandatoriasFaltantes = 
				SELECT 	Archivo."NEMO" 				as Nemotecnico,
						Archivo."CUENTA" 			as CuentaBoveda,
						CASE
		   					WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONDISPONIBLE" > 0) 
		   						THEN Archivo."POSICIONDISPONIBLE"
		   					ELSE
		   						0
	   					END AS PosicionBoveda,
						CASE
							WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
							WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONBLOQUEADA" > 0) 
								THEN Archivo."POSICIONBLOQUEADA"
	   						ELSE
	   							0
	   					END AS PosicionBloqueada	,
						CASE
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONREVENTA" > 0) 
	   							THEN Archivo."POSICIONREVENTA"
	   						ELSE
	   							0
	   					END AS PosicionReventa,
						CASE
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaTerceros AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						WHEN (Archivo."CUENTA" = v_DCVCuentaRelacionado AND Archivo."POSICIONRECOMPRA" > 0) 
	   							THEN Archivo."POSICIONRECOMPRA"
	   						ELSE
	   							0
	   					END AS PosicionRecompra
			FROM :v_DatosCuentaMandantes AS Archivo
			LEFT JOIN "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
					ON RTRIM(Archivo."CUENTA") = RTRIM(Custodia."CuentaBoveda")
				   AND Rtrim(Archivo."NEMO") = Custodia."Nemotecnico"
				   AND Custodia."FechaCuadraturaCustodia" = p_Fecha
				   AND Custodia."IdBoveda" = 'DCV'
				   AND Custodia."IdEstado" = 'VIG'
			WHERE Custodia."IdCuadraturaCustodia" IS NULL;
			
			INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCustodia"(
				 "IdCuadraturaCustodia"
				,"IdBoveda"
			    ,"IdInstrumento"
				,"IdTipoInstrumento"
				,"IdClasificacionTipoInstrumento"
				,"Nemotecnico"
				,"PosicionBoveda"
				,"PosicionBloqueada"
				,"PosicionReventa"
				,"PosicionRecompra"
				,"PosicionTotal"
				,"Diferencia"
				,"FechaCuadraturaCustodia"
				,"TipoCuenta"
				,"IdEstado"
				,"IdUsuario"
				,"CuentaBoveda"
				,"FechaModificacion"
				,"NombreBeneficiario"
				,"RutBeneficiario"
			)
			SELECT
			 "BSCL.Custodia.BD.Secuencias::CuadraturaCustodia".NEXTVAL AS "IdCuadraturaCustodia"
		     , 'DCV'
			 , COALESCE(Instrumento."IdInstrumento",0)
			 , COALESCE(Instrumento."IdTipoInstrumento",'')
			 , COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'')
		     , Carga.Nemotecnico
		     , PosicionBoveda
		     , PosicionBloqueada
		     , PosicionReventa
		     , PosicionRecompra
		     , PosicionBoveda + PosicionBloqueada + PosicionReventa + PosicionRecompra
		     , 0 - (PosicionBoveda + PosicionBloqueada + PosicionReventa + PosicionRecompra)
		     , :p_Fecha
		     , CASE 
				WHEN Carga.CuentaBoveda = v_DCVCuentaPropia then 'CPRO' 
				WHEN Carga.CuentaBoveda = v_DCVCuentaRelacionado then 'REL'
				ELSE 'TER' 
			 END
		     , 'VIG'
		     , :p_IdUsuario
		     , CASE 
				WHEN Carga.CuentaBoveda = v_DCVCuentaPropia then v_DCVCuentaPropia
				WHEN Carga.CuentaBoveda = v_DCVCuentaRelacionado then v_DCVCuentaRelacionado
				ELSE v_DCVCuentaTerceros
			 END
		     , :v_FechaSistemaCreacion
		     ,''
		     ,''
			FROM :v_CargaDePosicionesMandatoriasFaltantes AS Carga
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
					ON Instrumento."Nemotecnico" = Carga.Nemotecnico
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
					ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento";
			  
			-----------------------------------------------------------------
			-- ACTUALIZACIÓN DE POSICIONES EN TABLA DE CUADRATURA DE CUSTODIA.
			-----------------------------------------------------------------
			  
			UPDATE Custodia SET
				Custodia."PosicionBoveda"		= Posiciones.PosicionBoveda,
				Custodia."PosicionBloqueada"	= Posiciones.PosicionBloqueada,
				Custodia."PosicionReventa"		= Posiciones.PosicionReventa,
				Custodia."PosicionRecompra"		= Posiciones.PosicionRecompra,
				Custodia."PosicionTotal"		= Posiciones.PosicionBoveda + Posiciones.PosicionBloqueada + Posiciones.PosicionReventa + Posiciones.PosicionRecompra,
				Custodia."Diferencia"			= Custodia."CantidadTotal" - (Posiciones.PosicionBoveda + Posiciones.PosicionBloqueada + Posiciones.PosicionReventa + Posiciones.PosicionRecompra)
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
				INNER JOIN :v_CargaDePosiciones				AS Posiciones
					ON RTRIM(Posiciones.CuentaBoveda)	= RTRIM(Custodia."CuentaBoveda")
				   AND Rtrim(Posiciones.Nemotecnico)	= Custodia."Nemotecnico"
			WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
			  AND Custodia."IdBoveda"					= 'DCV'
			  AND Custodia."IdEstado"					= 'VIG';
	
			------------------------------------------------------------------------------------------
			-- ACTUALIZACIÓN DE POSICIONES EN TABLA DE CUADRATURA DE CUSTODIA CON CUENTAS MANDATORIAS.
			------------------------------------------------------------------------------------------
			  
			UPDATE Custodia SET
				Custodia."PosicionBoveda"		= Posiciones.PosicionBoveda,
				Custodia."PosicionBloqueada"	= Posiciones.PosicionBloqueada,
				Custodia."PosicionReventa"		= Posiciones.PosicionReventa,
				Custodia."PosicionRecompra"		= Posiciones.PosicionRecompra,
				Custodia."PosicionTotal"		= (Posiciones.PosicionBoveda + Posiciones.PosicionBloqueada + Posiciones.PosicionReventa + Posiciones.PosicionRecompra),
				Custodia."Diferencia"			= Custodia."CantidadTotal" - (Posiciones.PosicionBoveda + Posiciones.PosicionBloqueada + Posiciones.PosicionReventa + Posiciones.PosicionRecompra)
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
				INNER JOIN :v_CargaDePosicionesMandatorias	AS Posiciones
					ON RTRIM(Posiciones.CuentaBoveda)	= RTRIM(Custodia."CuentaBoveda")
				   AND Rtrim(Posiciones.Nemotecnico)	= Custodia."Nemotecnico"
			WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
			  AND Custodia."IdBoveda"					= 'DCV'
			  AND Custodia."IdEstado"					= 'VIG';
			

		END IF;
		
		
		---------------------------------------------------------------
		-- Archivo Otros Emisores
		---------------------------------------------------------------
		SELECT COUNT(*) INTO v_ExistenRegistros FROM :p_DataArchivoEmisores;
		IF(v_ExistenRegistros > 0) THEN
			--------------------
			--Variables
			--------------------
			DECLARE v_ErroresContadorArchivo	BIGINT;
			DECLARE v_ErroresContadorCustodia	BIGINT;
			DECLARE v_DCVCuentaTerceros			NVARCHAR(40):='';
			DECLARE v_FechaSistema				NVARCHAR(20);
			--------------------
			-- Validaciones
			-- Si se encuentra algun registro, no se puede cargar el archivo.
			--------------------
	
			BEGIN -- VALIDA ARCHIVO
	
				SELECT COUNT(1) INTO v_ErroresContadorArchivo
				FROM :p_DataArchivoEmisores		AS Archivo
				WHERE
				   TRIM(Archivo."Nemotecnico")			= 	''		-- NEMO NO PUEDE VENIR EN BLANCO
				OR TRIM(Archivo."Boveda")				=	''		-- BOVEDA NO PUEDE VENIR EN BLANCO
				OR Archivo."PosicionBoveda"			<=	0		-- NO PUEDE VENIR SIN POSICION
				OR TRIM(Archivo."MENSAJEVALIDACION")	!=	'OK';	-- VALIDACION CON ERROR
	
	
				IF(v_ErroresContadorArchivo > 0) THEN
					p_out_ErroresContador = 10;
					p_out_ErroresDetalle = select 'ARCHIVO_BOVEDAS_CON_ERRORES_REVISAR' as "Codigo",'ARCHIVO_BOVEDAS_CON_ERRORES_REVISAR' as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
	
				SELECT COUNT(1) INTO v_ErroresContadorCustodia
				FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"	AS Custodia
				WHERE Custodia."FechaCuadraturaCustodia"	= p_Fecha
				  AND Custodia."IdEstado"					= 'VIG';
				
				SELECT "Valor" INTO v_DCVCuentaTerceros
				FROM "BSCL.Custodia.BD::CUS.Configuracion"
				WHERE "CodigoParametro" = 'CuentaDCVCuentaTerceros'
			  	AND "IdEmpresa" = p_IdEmpresa; 
			  
			  	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistema);				
				
				IF(v_ErroresContadorCustodia < 1) THEN
					p_out_ErroresContador = 10;
					p_out_ErroresDetalle = select 'CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA' as "Codigo",'CARGA_DE_CUADRATURA_CUSTODIA_NO_REALIZADA' as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
	
			END;
	
			-----------------------------------------------------------------
			-- PREPARAMOS INFORMACION PARA POSTERIORMENTE PODER ACTUALIZAR.
			-----------------------------------------------------------------
			v_CargaActualizada = SELECT CASE "Boveda"
										  	WHEN 'DRE' THEN 'CIA'
										  	WHEN 'SCR' THEN 'CIA'
										  	WHEN 'PAS' THEN 'CIA'
										  	WHEN 'DCV' THEN 'CIA'
										  	WHEN 'MILA' THEN 'CIA'
										ELSE "Boveda"
										END	AS "IdBoveda"
									  , "Nemotecnico"
									  , SUM(COALESCE("PosicionBoveda",0)) AS "PosicionBoveda"
								 FROM :p_DataArchivoEmisores
								 GROUP BY "Boveda","Nemotecnico"; 
			
			v_CargaParaActualizar = SELECT "IdBoveda"
									   , "Nemotecnico"
									   , "PosicionBoveda"
								    FROM :v_CargaActualizada AS Carga
								    WHERE (Carga."IdBoveda",Carga."Nemotecnico")IN (	SELECT "IdBoveda","Nemotecnico"
												   										FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"
												   										WHERE "FechaCuadraturaCustodia" = p_Fecha
												   										AND "IdEstado"		= 'VIG'
			  																			AND "TipoCuenta" 	= 'TER'
												   									);								 
								 
								 
			v_CargaParaIngresar = SELECT "IdBoveda"
									   , "Nemotecnico"
									   , "PosicionBoveda"
								  FROM :v_CargaActualizada AS Carga
								  WHERE (Carga."IdBoveda",Carga."Nemotecnico") NOT IN (	SELECT "IdBoveda","Nemotecnico"
												   										FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia"
												   										WHERE "FechaCuadraturaCustodia" = p_Fecha
												   										AND "IdEstado"		= 'VIG'
			  																			AND "TipoCuenta" 	= 'TER'
												   									  );  		 
								 
			-----------------------------------------------------------------
			-- ACTUALIZACION DE POSICION EN TABLA DE CUADRATURA DE CUSTODIA.
			-----------------------------------------------------------------
			UPDATE Custodia SET
				Custodia."PosicionBoveda"		= Archivo."PosicionBoveda",
				Custodia."PosicionTotal"		= (Archivo."PosicionBoveda" + Custodia."PosicionBloqueada" + Custodia."PosicionReventa" + Custodia."PosicionRecompra"),
				Custodia."Diferencia"			= Custodia."CantidadTotal" - (Archivo."PosicionBoveda" + Custodia."PosicionBloqueada" + Custodia."PosicionReventa" + Custodia."PosicionRecompra")			  
			FROM "BSCL.Custodia.BD::CUS.CuadraturaCustodia" AS Custodia
			INNER JOIN :v_CargaParaActualizar 					AS Archivo 
					ON Archivo."IdBoveda" 			= Custodia."IdBoveda" 
				   AND Archivo."Nemotecnico" 		= Custodia."Nemotecnico"
			WHERE Custodia."IdEstado"					= 'VIG'
			  AND Custodia."TipoCuenta" 				= 'TER'
			  AND Custodia."FechaCuadraturaCustodia"	= p_Fecha;
			  
			------------------------------------------------------------------
			-- INGRESO LOS REGISTROS QUE NO EXISTEN EN LA CUADRATURA
			------------------------------------------------------------------
			INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaCustodia"(
				 "IdCuadraturaCustodia"
				,"IdBoveda"
			    ,"IdInstrumento"
				,"IdTipoInstrumento"
				,"IdClasificacionTipoInstrumento"
				,"Nemotecnico"
				,"PosicionBoveda"
				,"FechaCuadraturaCustodia"
				,"TipoCuenta"
				,"IdEstado"
				,"IdUsuario"
				,"CuentaBoveda"
				,"FechaModificacion"
				,"NombreBeneficiario"
				,"RutBeneficiario"
			)
			SELECT "BSCL.Custodia.BD.Secuencias::CuadraturaCustodia".NEXTVAL AS "IdCuadraturaCustodia"
			     , Carga."IdBoveda"
				 , COALESCE(Instrumento."IdInstrumento",0)
				 , COALESCE(Instrumento."IdTipoInstrumento",'')
				 , COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'')
			     , Carga."Nemotecnico"
			     , CASE WHEN Carga."IdBoveda" IN ('CIA','BCS','BOV','BEC') THEN
   				   		Carga."PosicionBoveda"
   				   ELSE 0
   				   END
			     , :p_Fecha
			     , 'TER'
			     , 'VIG'
			     , :p_IdUsuario
			     , v_DCVCuentaTerceros
			     , v_FechaSistema
			     ,''
			     ,''
			FROM :v_CargaParaIngresar AS Carga
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento
					ON Instrumento."Nemotecnico" = Carga."Nemotecnico"
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
					ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento";  

		END IF;
		--------------------
		--FIN SP 
		--------------------
	END;
END;
GO
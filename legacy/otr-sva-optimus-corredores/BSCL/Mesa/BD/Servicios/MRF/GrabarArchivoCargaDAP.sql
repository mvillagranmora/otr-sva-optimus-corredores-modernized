SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::MRF.GrabarArchivoCargaDAP";

--
--  Proposito: Servicio para grabar Depositos que fueron cargados desde archivo.
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 21/07/2016
--

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.GrabarArchivoCargaDAP" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo					NVARCHAR(3),	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	 		NVARCHAR(1)	,	-- S/N se usa en caso de servicios anidados.
	
	-- Parametros entrada del servicio privado
	IN p_RegistrosGrabar			TABLE (	"FechaProceso"			NVARCHAR(10),
											"IdSecuencia"			BIGINT,
											"RutClienteComprador"	NVARCHAR(11),
											"SecuenciaRutComprador"	DECIMAL(2,0),
											"CustodiaComprador"		NVARCHAR(1),
											"RutClienteVendedor"	NVARCHAR(11),
											"SecuenciaRutVendedor"	DECIMAL(2,0),
											"CustodiaVendedor"		NVARCHAR(1),
											"Emisor"				NVARCHAR(10),
											"FechaInicioOperacion"	NVARCHAR(10),
											"FechaTerminoPapel"		NVARCHAR(10),
											"TipoReajuste"			NVARCHAR(2),
											"MontoFinal"			DECIMAL(17,2),
											"MontoInicial"			DECIMAL(17,2),
											"NumeroCompra"			BIGINT,
											"NumeroVenta"			BIGINT,
											"IdEstado"				NVARCHAR(3),
											"Glosa"					NVARCHAR(250)),
	OUT p_out_CantidadRegistros		BIGINT,
	OUT p_out_RegistrosCargadosOK	BIGINT,
	OUT p_out_RegistrosCargadosNOK	BIGINT,
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	int,
	OUT p_out_AdvertenciasDetalle	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador		int,
	OUT p_out_ErroresDetalle		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
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
		--Variables
		--------------------
		DECLARE	v_FechaSistemaCreacion		TIMESTAMP;
		DECLARE v_Count			 			BIGINT:=0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
		
		p_out_CantidadRegistros		:=0;
		p_out_RegistrosCargadosOK	:=0;
		p_out_RegistrosCargadosNOK	:=0;
		
		--------------------
		--Validaciones
		--------------------
		
		-- validacion IDSECUENCIA de archivo ya existe (mismo id y fechaproceso)
		SELECT COUNT(1) into v_Count
	    FROM :p_RegistrosGrabar RG
	   	INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR ON (RG."IdSecuencia"=CAR."IdSecuencia" AND RG."FechaProceso"=CAR."FechaProceso" AND CAR."IdEstado"='GRA')
		WHERE CAR."IdEmpresa" = p_IdEmpresa;
		
		IF v_Count > 0 THEN
			p_out_ErroresDetalle = SELECT 'UI_REGISTRO_YA_EXISTE' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- validacion IDSECUENCIA de archivo ya existe (mismo id y estado GRA)
		SELECT COUNT(1) into v_Count
	    FROM :p_RegistrosGrabar RG
	   	INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR ON (RG."IdSecuencia"=CAR."IdSecuencia" AND CAR."IdEstado"='GRA')
		WHERE CAR."IdEmpresa" = p_IdEmpresa;
		
		IF v_Count > 0 THEN
			p_out_ErroresDetalle = SELECT 'UI_REGISTRO_YA_EXISTE' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		--------------------
		--Reglas Negocio 
		--------------------		
		
		
		--------------------
		--Lógica de Negocio 
		--------------------
		
		BEGIN
			v_ValidacionComprador =
				SELECT
					"IdSecuencia",
					CASE WHEN CLI."DocumentoIdentidad" IS NULL THEN 'ERR' ELSE RG."IdEstado" END AS "IdEstado",
					CASE WHEN CLI."DocumentoIdentidad" IS NULL THEN 'UI_COMPRADOR_NO_REGISTRADO' ELSE RG."Glosa" END AS "Glosa",
					CLI."DocumentoIdentidad"
				FROM :p_RegistrosGrabar RG
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI 
					ON (RG."RutClienteComprador"=CLI."DocumentoIdentidad" AND CLI."IdEmpresa"=p_IdEmpresa AND CLI."IdEstado"='ACT')
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
					ON (POR."IdCliente" = CLI."IdCliente" AND POR."CodigoPortafolio" = CAST(RG."SecuenciaRutComprador" AS NVARCHAR(100)));
			
			v_ValidacionVendedor =
				SELECT
					"IdSecuencia",
					CASE WHEN CLI."DocumentoIdentidad" IS NULL THEN 'ERR' ELSE RG."IdEstado" END AS "IdEstado",
					CASE WHEN CLI."DocumentoIdentidad" IS NULL THEN 'UI_VENDEDOR_NO_REGISTRADO' ELSE RG."Glosa" END AS "Glosa",
					CLI."DocumentoIdentidad"
				FROM :p_RegistrosGrabar RG
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI 
					ON (RG."RutClienteVendedor"=CLI."DocumentoIdentidad" AND CLI."IdEmpresa"=p_IdEmpresa AND CLI."IdEstado"='ACT')
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
					ON (POR."IdCliente" = CLI."IdCliente" AND POR."CodigoPortafolio" = CAST(RG."SecuenciaRutVendedor" AS NVARCHAR(100)));
				
			v_RegistrosGrabar =
				SELECT
					RG."FechaProceso",
					RG."IdSecuencia",
					RG."RutClienteComprador",
					RG."SecuenciaRutComprador",
					RG."CustodiaComprador",
					RG."RutClienteVendedor",
					RG."SecuenciaRutVendedor",
					RG."CustodiaVendedor",
					RG."Emisor",
					RG."FechaInicioOperacion",
					RG."FechaTerminoPapel",
					RG."TipoReajuste",
					RG."MontoFinal",
					RG."MontoInicial",
					CASE WHEN VC."IdEstado"='ERR' OR VV."IdEstado"='ERR' THEN 'ERR' ELSE 'GRA' END AS "IdEstado",
					CASE WHEN VC."IdEstado"='ERR' AND VV."IdEstado"='ERR' THEN VC."Glosa"||' / '||VV."Glosa"
						 WHEN VC."IdEstado"='ERR' THEN VC."Glosa"
						 WHEN VV."IdEstado"='ERR' THEN VV."Glosa"
						 ELSE 'UI_REGISTRO_GRABADO_EN_SISTEMA'
					END AS "Glosa"
				FROM :p_RegistrosGrabar RG
				INNER JOIN :v_ValidacionComprador VC ON (RG."IdSecuencia" = VC."IdSecuencia")
				INNER JOIN :v_ValidacionVendedor  VV ON (RG."IdSecuencia" = VV."IdSecuencia");
			
			INSERT INTO "BSCL.Mesa.BD::MES.CargaMasivaDepositos"(
				"IdCargaMasivaDepositos",
				"FechaProceso",
				"IdSecuencia",
				"RutClienteComprador",
				"SecuenciaRutComprador",
				"CustodiaComprador",
				"RutClienteVendedor",
				"SecuenciaRutVendedor",
				"CustodiaVendedor",
				"Emisor",
				"FechaInicioOperacion",
				"FechaTerminoPapel",
				"TipoReajuste",
				"MontoFinal",
				"MontoInicial",
				--"CodigoAgente", Sacar del Cliente el Codigo Agente
				"IdEstado",
				"Glosa",
				"IdEmpresa"
			)
			SELECT
				"BSCL.Mesa.BD.Secuencias::MRF.IdCargaMasivaDepositos".NEXTVAL,
				RG."FechaProceso",
				RG."IdSecuencia",
				RG."RutClienteComprador",
				RG."SecuenciaRutComprador",
				RG."CustodiaComprador",
				RG."RutClienteVendedor",
				RG."SecuenciaRutVendedor",
				RG."CustodiaVendedor",
				RG."Emisor",
				RG."FechaInicioOperacion",
				RG."FechaTerminoPapel",
				RG."TipoReajuste",
				RG."MontoFinal",
				RG."MontoInicial",
				RG."IdEstado",
				RG."Glosa",
				p_IdEmpresa
			FROM :v_RegistrosGrabar RG;
			
			SELECT COUNT(1) INTO p_out_CantidadRegistros FROM :v_RegistrosGrabar RG;
			SELECT COUNT(1) INTO p_out_RegistrosCargadosOK FROM :v_RegistrosGrabar RG WHERE "IdEstado"='GRA';
			SELECT COUNT(1) INTO p_out_RegistrosCargadosNOK FROM :v_RegistrosGrabar RG WHERE "IdEstado"='ERR';
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;
END;
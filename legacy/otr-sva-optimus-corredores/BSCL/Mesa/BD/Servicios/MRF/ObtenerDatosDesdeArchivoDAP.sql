SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::MRF.ObtenerDatosDesdeArchivoDAP";

--
--  Proposito: Servicio para obtener desde archivo los datos de Depositos.
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 19/07/2016
--

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.ObtenerDatosDesdeArchivoDAP" (
	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo					NVARCHAR(3),	--ID del Modulo que lo invoca
	
	-- Parametros entrada del servicio privado
	IN p_NombreArchivo				NVARCHAR(100),
	IN p_DataArchivo				TABLE (	"FECHAPROCESO"			NVARCHAR(10),
											"IDSECUENCIA"			BIGINT,--DECIMAL(8,0)
											"RUTCOMPRADOR"			NVARCHAR(11),
											"SECUENCIARUTCOMPRADOR"	DECIMAL(2,0),
											"CUSTODIACOMPRADOR"		NVARCHAR(1),
											--"NUMEROCOMPRA"			BIGINT,
											"RUTVENDEDOR"			NVARCHAR(11),
											"SECUENCIARUTVENDEDOR"	DECIMAL(2,0),
											"CUSTODIAVENDEDOR"		NVARCHAR(1),
											--"NUMEROVENTA"			BIGINT,
											"EMISOR"				NVARCHAR(10),
											"FECHAINICIOOPERACION"	NVARCHAR(10),
											"FECHATERMINOPAPEL"		NVARCHAR(10),
											"TIPOREAJUSTE"			NVARCHAR(2),
											"MONTOFINAL"			DECIMAL(17,2),
											"MONTOINICIAL"			DECIMAL(17,2)),
	
	-- Parametros salida del servicio privado
	OUT p_out_Depositos				TABLE (	"FechaProceso"			NVARCHAR(10),
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
											"EstadoDeposito"		NVARCHAR(520),
											"TipoDocumentoIdentidad" NVARCHAR(1),
											"IdMoneda"				NVARCHAR(3),
											"Glosa"					NVARCHAR(250)),
	
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
		select count(1) into p_out_AdvertenciasContador	from :p_out_AdvertenciasDetalle;
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
		DECLARE	v_countDepositos			BIGINT;
		DECLARE v_FechaProcesoArchivo		NVARCHAR(1);
		DECLARE v_TipoDocumentoIdentidad	NVARCHAR(1);
		DECLARE v_Count			 			BIGINT:=0;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);
		
		
		--------------------
		--Validaciones
		--------------------
		
		-- validacion IDSECUENCIA de archivo ya existe (mismo id y fechaproceso)
		SELECT COUNT(1) into v_Count
	    FROM :p_DataArchivo DA
	   	INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR ON (DA."IDSECUENCIA"=CAR."IdSecuencia" AND DA."FECHAPROCESO"=CAR."FechaProceso" AND CAR."IdEstado"='GRA')
		WHERE CAR."IdEmpresa" = p_IdEmpresa;
		
		IF v_Count > 0 THEN
			p_out_ErroresDetalle = SELECT 'UI_REGISTRO_YA_EXISTE' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- validacion IDSECUENCIA de archivo ya existe (mismo id y estado GRA)
		SELECT COUNT(1) into v_Count
	    FROM :p_DataArchivo DA
	   	INNER JOIN "BSCL.Mesa.BD::MES.CargaMasivaDepositos" CAR ON (DA."IDSECUENCIA"=CAR."IdSecuencia" AND CAR."IdEstado"='GRA')
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
		SELECT "Valor" INTO v_TipoDocumentoIdentidad FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa" WHERE "CodigoParametro" = 'TIPODOCTOIDENTCARGAARCHIVO';
		
		BEGIN
			---------- Consulta Depositos de Archivo ----------
			v_Depositos = SELECT
							DA."FECHAPROCESO"			AS "FechaProceso",
							DA."IDSECUENCIA"			AS "IdSecuencia",
							DA."RUTCOMPRADOR"			AS "RutClienteComprador",
							DA."SECUENCIARUTCOMPRADOR"	AS "SecuenciaRutComprador",
							DA."CUSTODIACOMPRADOR"		AS "CustodiaComprador",
							DA."RUTVENDEDOR"			AS "RutClienteVendedor",
							DA."SECUENCIARUTVENDEDOR"	AS "SecuenciaRutVendedor",
							DA."CUSTODIAVENDEDOR"		AS "CustodiaVendedor",
							DA."EMISOR"					AS "Emisor",
							DA."FECHAINICIOOPERACION"	AS "FechaInicioOperacion",
							DA."FECHATERMINOPAPEL"		AS "FechaTerminoPapel",
							DA."TIPOREAJUSTE"			AS "TipoReajuste",
							DA."MONTOFINAL"				AS "MontoFinal",
							DA."MONTOINICIAL"			AS "MontoInicial",
							0							AS "NumeroCompra",
							0							AS "NumeroVenta",
							'ACP'						AS "IdEstado",
							'ACEPTADO'					AS "EstadoDeposito",
							v_TipoDocumentoIdentidad	AS "TipoDocumentoIdentidad",
							CASE WHEN DA."TIPOREAJUSTE"='NR' THEN 'CLP' ELSE 'USD' END AS "IdMoneda",
							'UI_REGISTRO_CARGADO_DE_ARCHIVO' AS "Glosa"
						FROM :p_DataArchivo DA;
			
			p_out_Depositos = SELECT * FROM :v_Depositos;
		END;
		
		--------------------
		--FIN SP 
		--------------------
	END;
END;
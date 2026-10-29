SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.AgregarLogAplicacionDeClavesPorUsuario";

CREATE PROCEDURE

	--  Proposito: Insertar un registro en el log para cada usuario bloqueado por la aplicación de gestión de claves 
	--  Autor: BCS
	--  Fecha Creacion: 15/10/2025
	--  Fecha Modificacion: 
	
	"BSCL.Plataforma.BD.Servicios::ACC.AgregarLogAplicacionDeClavesPorUsuario" (
		IN p_CodigoUsuario NVARCHAR(100)
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL"
	AS
	BEGIN
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
	RESIGNAL;
	END;

	BEGIN
	DECLARE v_IdLog BIGINT;
	DECLARE v_IdUsuario BIGINT;
	DECLARE v_contDetalle BIGINT;
	DECLARE v_CodigoAutLog NVARCHAR(64);
	DECLARE v_FechaRegistro SECONDDATE;    
	DECLARE v_IdTipoLog NVARCHAR(40) := 'ACTUALIZACION';
	DECLARE v_IdModulo NVARCHAR(40) := 'PLA';
	DECLARE v_IdEmpresa BIGINT := 1;
	DECLARE v_FechaSistemaLog NVARCHAR(10);
	DECLARE v_IdUsuarioAtendido BIGINT;
	DECLARE v_IdPersona BIGINT  DEFAULT 0;
	DECLARE v_IdCliente BIGINT DEFAULT 0; 
	DECLARE v_EstadoCliente NVARCHAR(3) DEFAULT '';
	DECLARE v_NombreCliente NVARCHAR(100) DEFAULT '';
	DECLARE v_IdTipoDocumentoIdentidad NVARCHAR(40) DEFAULT '';
	DECLARE v_IdDocumentoIdentidad NVARCHAR(40) DEFAULT '';
	DECLARE v_IdPortafolio BIGINT DEFAULT 0;
	DECLARE v_EstadoPortafolio NVARCHAR(3) DEFAULT '';
	DECLARE v_IdTipoRegistro NVARCHAR(40) := 'BLOQUEO_USUARIO_POR_APLICACION_CLAVE';
	DECLARE v_IdRegistro NVARCHAR(40) DEFAULT '';
	DECLARE v_EstadoRegistro NVARCHAR(3) DEFAULT '';
	DECLARE v_IdTipoOperacion NVARCHAR(40) DEFAULT '';
	DECLARE v_Folio BIGINT DEFAULT 0;
	DECLARE v_Cantidad DECIMAL(26,8) DEFAULT 0;
	DECLARE v_PrecioTasa DECIMAL(26,8) DEFAULT 0;
	DECLARE v_Monto DECIMAL(26,8) DEFAULT 0;
	DECLARE v_IdMonedaOrigen NVARCHAR(3) DEFAULT '';
	DECLARE v_IdMonedaLiquidacion NVARCHAR(3) DEFAULT '';
	DECLARE v_TipoCambio NVARCHAR(40) DEFAULT '';
	DECLARE v_Nemo NVARCHAR(5000) DEFAULT '';
	DECLARE v_IdProceso BIGINT DEFAULT 0;
	DECLARE v_EstadoProceso NVARCHAR(3) DEFAULT '';
	DECLARE v_NombreProceso NVARCHAR(100) DEFAULT 'Bloqueo de usuarios activos por la aplicación de gestión de claves, habilitados por Soporte Nivel 2';
	DECLARE v_IdTipoPrograma NVARCHAR(40)  DEFAULT '';

	DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(100), "CampoRegistro" nvarchar(500), "ValorAntiguo" nvarchar(5000), "ValorNuevo" nvarchar(5000));
	p_DetalleLog = SELECT 
        "CodigoUsuario" AS "SubRegistro",
        'CambioEstado' AS "CampoRegistro",
        "IdEstado" AS "ValorAntiguo",
        'BLO' AS "ValorNuevo"
    FROM "BSCL.Plataforma.BD::ACC.Usuario"
    WHERE UPPER("CodigoUsuario") = UPPER(:p_CodigoUsuario);


	SELECT "Valor" INTO v_FechaSistemaLog FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema';
	SELECT "Valor" INTO v_IdUsuario FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.IdUsuarioBolsa';
    SELECT "IdUsuario" INTO v_IdUsuarioAtendido FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE UPPER("CodigoUsuario") = UPPER(:p_CodigoUsuario);
	SELECT "BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL into v_IdLog from dummy;
	SELECT (CAST(CURRENT_UTCTIMESTAMP AS SECONDDATE)) into v_FechaRegistro from dummy;
	
	v_tablaTempDetLog = SELECT "BSCL.Plataforma.BD.Secuencias::DetalleLog".NEXTVAL as "IdDetLog", "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" FROM :p_DetalleLog;
	SELECT COUNT(*) INTO v_contDetalle from :p_DetalleLog;

	SELECT cast(HASH_SHA256(TO_BINARY(
		CONCAT(:v_IdLog
		,CONCAT(:v_IdTipoLog
		,CONCAT(:v_IdModulo
		,CONCAT(:v_IdUsuario
		,CONCAT(:v_IdEmpresa
		,CONCAT(:v_FechaSistemaLog
		,CONCAT(SUBSTRING(:v_FechaSistemaLog , 0, 7)
		,CONCAT(COALESCE(:v_IdUsuarioAtendido,0)
		,CONCAT(COALESCE(:v_IdCliente,0)
		,CONCAT(COALESCE(:v_EstadoCliente,'')
		,CONCAT(COALESCE(:v_NombreCliente,'')
		,CONCAT(COALESCE(:v_IdTipoDocumentoIdentidad,'')
		,CONCAT(COALESCE(:v_IdDocumentoIdentidad,'')
		,CONCAT(COALESCE(:v_IdPortafolio,0)
		,CONCAT(COALESCE(:v_EstadoPortafolio,'')
		,CONCAT(COALESCE(:v_IdRegistro,'')
		,CONCAT(COALESCE(:v_EstadoRegistro,'')
		,CONCAT(COALESCE(:v_IdTipoRegistro,'')
		,CONCAT(COALESCE(:v_IdTipoOperacion,'')
		,CONCAT(COALESCE(:v_Folio,0)
		,CONCAT(COALESCE(:v_Cantidad,0)
		,CONCAT(COALESCE(:v_PrecioTasa,0)
		,CONCAT(COALESCE(:v_Monto,0)
		,CONCAT(COALESCE(:v_IdProceso,0)
		,CONCAT(COALESCE(:v_EstadoProceso,'')
		,CONCAT(COALESCE(:v_NombreProceso,'')
		,CONCAT(COALESCE(:v_IdMonedaOrigen,'')
		,CONCAT(COALESCE(:v_IdMonedaLiquidacion,'')
		,CONCAT(COALESCE(:v_TipoCambio,'')
		,CONCAT(:v_FechaRegistro
		,CONCAT(COALESCE(:v_Nemo,'')
		,COALESCE(:v_IdPersona,0)
	))))))))))))))))))))))))))))))))) AS VARCHAR) into v_CodigoAutLog FROM DUMMY;
	IF v_contDetalle > 0 THEN
		v_tablaCodLogDetalle = SELECT "IdDetLog",cast(HASH_SHA256(TO_BINARY(
			CONCAT("IdDetLog"
			,CONCAT(:v_IdLog
			,CONCAT(COALESCE("SubRegistro",'')
			,CONCAT(COALESCE("ValorAntiguo",'')
			,CONCAT(COALESCE("ValorNuevo",''),COALESCE("CampoRegistro",''
		)))))))) AS VARCHAR) AS "CodigoDetalle" FROM :v_tablaTempDetLog;
	END IF;

	INSERT INTO "BSCL.Plataforma.BD::PLA.Log" (
		"IdLog"
		,"IdTipoLog"
		,"IdModulo"
		,"IdUsuario"
		,"IdEmpresa"
		,"FechaLog"
		,"PeriodoLog"
		,"FechaRegistro"
		,"IdUsuarioAtendido"
		,"IdPersona"
		,"IdCliente"
		,"EstadoCliente"
		,"NombreCliente"
		,"IdTipoDocumentoIdentidad"
		,"IdDocumentoIdentidad"
		,"IdPortafolio"
		,"EstadoPortafolio"
		,"IdRegistro"
		,"EstadoRegistro"
		,"IdTipoRegistro"
		,"IdTipoOperacion"
		,"Folio"
		,"Cantidad"
		,"PrecioTasa"
		,"Monto"
		,"IdMonedaOrigen"
		,"IdMonedaLiquidacion"
		,"TipoCambio"
		,"Nemo"
		,"IdProceso"
		,"EstadoProceso"
		,"NombreProceso"
		,"IdTipoPrograma"
		,"CodigoAutenticacion"
	)
	VALUES (
		:v_IdLog
		,:v_IdTipoLog
		,:v_IdModulo
		,:v_IdUsuario
		,:v_IdEmpresa
		,:v_FechaSistemaLog
		,SUBSTRING(:v_FechaSistemaLog , 0, 7)
		,:v_FechaRegistro
		,:v_IdUsuarioAtendido
		,:v_IdPersona
		,:v_IdCliente
		,:v_EstadoCliente
		,:v_NombreCliente
		,:v_IdTipoDocumentoIdentidad
		,:v_IdDocumentoIdentidad
		,:v_IdPortafolio
		,:v_EstadoPortafolio
		,:v_IdRegistro
		,:v_EstadoRegistro
		,:v_IdTipoRegistro
		,:v_IdTipoOperacion
		,:v_Folio
		,:v_Cantidad
		,:v_PrecioTasa
		,:v_Monto
		,:v_IdMonedaOrigen
		,:v_IdMonedaLiquidacion
		,:v_TipoCambio
		,:v_Nemo
		,:v_IdProceso
		,:v_EstadoProceso
		,:v_NombreProceso
		,:v_IdTipoPrograma
		,:v_CodigoAutLog
	);
	INSERT INTO "BSCL.Plataforma.BD::PLA.DetalleLog"(
		"IdDetalleLog"
		,"IdLog"
		,"SubRegistro"
		,"CampoRegistro"
		,"ValorAntiguo"
		,"ValorNuevo"
		,"CodigoAutenticacion"
	)
	SELECT
		TEMP."IdDetLog"
		,:v_IdLog
		,"SubRegistro"
		,"CampoRegistro"
		,"ValorAntiguo"
		,"ValorNuevo"
		,COD."CodigoDetalle"
	FROM :v_tablaTempDetLog AS TEMP
	INNER JOIN :v_tablaCodLogDetalle AS COD ON COD."IdDetLog" = TEMP."IdDetLog";
	END;
END;

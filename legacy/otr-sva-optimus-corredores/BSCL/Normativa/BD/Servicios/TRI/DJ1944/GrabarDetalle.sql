 set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1944.GrabarDetalle";

 CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1944.GrabarDetalle"
 (
 ------------ Parametros de Entrada Comunes ----------
 -- ID empresa del login.
 IN p_IdEmpresa         BIGINT   ,
 -- ID usuario del login.
 IN p_IdUsuario         BIGINT   ,
 -- ID modulo invocador.
 IN p_IdModulo         NVARCHAR(3)  ,
 ------------ Parametros de Entrada Propios ----------
 -- ID del detalle.
 -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
 -- En caso contrario, se intenta crear un nuevo registro.
 IN p_ID          BIGINT   ,
 -- ID del documento.
 IN p_IdDocumento        BIGINT   ,
 -- VoBo del usuario sobre el registro (S/N).
 IN p_VboUsuario        CHAR(1)   ,
 -- Observacion ingresada por usuario que edito el registro.
 IN p_Observaciones        NVARCHAR(250),
 -- Fuente de los datos.
 IN p_FuenteDatos        NVARCHAR(40) ,
 -- IN p_IdComprobante                          BIGINT       ,
 IN p_EjecutadoComplementoIPC     NVARCHAR(1),
 IN p_Numero         BIGINT,
 IN p_TipoCartera        NVARCHAR(10),
 IN p_RutCliente        NVARCHAR(40),
 --IN p_IdCliente         BIGINT,
 IN p_FechaOperacion       DATE,
 IN p_RutInstitucion       NVARCHAR(40),
 IN p_SalAhorroNetoEjerNEGT1     DECIMAL(26,8),
 IN p_SalArrastreEjerSIGT1      DECIMAL(26,8),
 IN p_SalAhorroNetoEjerT2      DECIMAL(26,8),
 IN p_SalArrastreEjerSigNegT2     DECIMAL(26,8),
 IN p_MtoRetiro57BisT2       DECIMAL(26,8),
 IN p_SalAhorroNetoEjeNegT3      DECIMAL(26,8),
 IN p_SalArrastreEjeSigNegT3     DECIMAL(26,8),
 IN p_MtoRetiroAnoT3       DECIMAL(26,8),
 IN p_MtoRenRetFFMMPOS       DECIMAL(26,8),
 IN p_MtoRenRetFFMMNEG       DECIMAL(26,8),
 IN p_MtoRenRetOTROSPOS       DECIMAL(26,8),
 IN p_MtoRenRetOTROSNEG       DECIMAL(26,8),
 IN p_NumeroCertificado       BIGINT,
 ------------ Parametros de Salida Propios ----------
 -- ID del registro.
 OUT p_out_id         BIGINT   ,
 ------------ Salidas Advertencias ----------
 -- Contador de advertencias.
 OUT p_out_AdvertenciasContador     INT    ,
 -- Tabla de advertencias.
 OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),
 ------------ Salidas Errores ----------
 -- Contador de errores
 -- Importante: Debe ser cero para que se haga commit.
 OUT p_out_ErroresContador INT,
 -- Tabla de errores.
 OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
 )
 LANGUAGE sqlscript
 SQL security invoker
 DEFAULT SCHEMA "BSCL"
 AS
 BEGIN
 ------------ Variables Propias ----------
 ------------ Bloque Comun de Inicializacion ----------
 DECLARE salida_controlada condition;
 DECLARE exit handler FOR salida_controlada
 BEGIN
 SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
 SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
 END;
 DECLARE exit handler FOR sqlexception
 BEGIN
 DECLARE v_errorExt NVARCHAR(40);
 IF ::sql_error_code > 10000 THEN
 -- Error pseudo-controlado pero desde un sub-sp.
 CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
 p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
 SELECT v_errorExt, ::sql_error_message FROM dummy;
 SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
 ELSE
 -- Error interno no manejado (el log sera captado por la capa de XS control).
 RESIGNAL;
 END IF;
 END;
 p_out_AdvertenciasContador := 0;
 p_out_ErroresContador := 0;
 p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
 p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
 ------------ Logica de Actualizacion ----------
 IF p_ID > 0 THEN
 p_out_id := p_ID;
 UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1944"
 SET
 "IdDocumento"     = p_IdDocumento
 ,"VboUsuario"     = p_VboUsuario
 ,"Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
 ,"FuenteDatos"     = p_FuenteDatos
 ,"EjecutadoComplementoIPC" = p_EjecutadoComplementoIPC
 ,"Numero"     = p_Numero
 ,"TipoCartera"              = p_TipoCartera
 ,"RutCliente"               = p_RutCliente
 --,"IdCliente"                = p_IdCliente
 ,"FechaOperacion"           = p_FechaOperacion
 ,"RutInstitucion"           = p_RutInstitucion
 ,"SalAhorroNetoEjerNEGT1"   = p_SalAhorroNetoEjerNEGT1
 ,"SalArrastreEjerSIGT1"     = p_SalArrastreEjerSIGT1
 ,"SalAhorroNetoEjerT2"      = p_SalAhorroNetoEjerT2
 ,"SalArrastreEjerSigNegT2"  = p_SalArrastreEjerSigNegT2
 ,"MtoRetiro57BisT2"         = p_MtoRetiro57BisT2
 ,"SalAhorroNetoEjeNegT3"    = p_SalAhorroNetoEjeNegT3
 ,"SalArrastreEjeSigNegT3"   = p_SalArrastreEjeSigNegT3
 ,"MtoRetiroAnoT3"           = p_MtoRetiroAnoT3
 ,"MtoRenRetFFMMPOS"         = p_MtoRenRetFFMMPOS
 ,"MtoRenRetFFMMNEG"         = p_MtoRenRetFFMMNEG
 ,"MtoRenRetOTROSPOS"        = p_MtoRenRetOTROSPOS
 ,"MtoRenRetOTROSNEG"        = p_MtoRenRetOTROSNEG
 ,"NumeroCertificado"        = p_NumeroCertificado
 WHERE "ID" = p_ID;
 ELSE
 SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1944";
 insert into "BSCL.Normativa.BD::TRI.DatosDJ1944"(
 "ID"
 ,"IdDocumento"
 ,"VboUsuario"
 ,"Observaciones"
 ,"FuenteDatos"
 ,"EjecutadoComplementoIPC"
 ,"Numero"
 ,"TipoCartera"
 ,"RutCliente"
 ,"IdCliente"
 ,"FechaOperacion"
 ,"RutInstitucion"
 ,"SalAhorroNetoEjerNEGT1"
 ,"SalArrastreEjerSIGT1"
 ,"SalAhorroNetoEjerT2"
 ,"SalArrastreEjerSigNegT2"
 ,"MtoRetiro57BisT2"
 ,"SalAhorroNetoEjeNegT3"
 ,"SalArrastreEjeSigNegT3"
 ,"MtoRetiroAnoT3"
 ,"MtoRenRetFFMMPOS"
 ,"MtoRenRetFFMMNEG"
 ,"MtoRenRetOTROSPOS"
 ,"MtoRenRetOTROSNEG"
 ,"NumeroCertificado"
 ) values (
 p_out_id
 ,p_IdDocumento
 ,'N'
 ,''
 ,'MANTENEDOR'
 --,p_IdComprobante
 ,p_EjecutadoComplementoIPC
 ,p_Numero
 ,p_TipoCartera
 ,p_RutCliente
 ,0--,p_IdCliente
 ,p_FechaOperacion
 ,p_RutInstitucion
 ,p_SalAhorroNetoEjerNEGT1
 ,p_SalArrastreEjerSIGT1
 ,p_SalAhorroNetoEjerT2
 ,p_SalArrastreEjerSigNegT2
 ,p_MtoRetiro57BisT2
 ,p_SalAhorroNetoEjeNegT3
 ,p_SalArrastreEjeSigNegT3
 ,p_MtoRetiroAnoT3
 ,p_MtoRenRetFFMMPOS
 ,p_MtoRenRetFFMMNEG
 ,p_MtoRenRetOTROSPOS
 ,p_MtoRenRetOTROSNEG
 ,p_NumeroCertificado
 );
 END IF;
 END;
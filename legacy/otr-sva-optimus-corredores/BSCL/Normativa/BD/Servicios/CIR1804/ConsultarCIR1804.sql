SET SCHEMA "BSCL";
 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CIR1804::ConsultarCIR1804";

CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar información Circular 1804.
--  Autor: 
--  Fecha Creacion: 09/10/2018

"BSCL.Normativa.BD.Servicios.CIR1804::ConsultarCIR1804" ( 

-- Parametros entrada comunes en todos los servicios privados
    IN p_IdEmpresa          BIGINT,
    IN p_IdUsuario          BIGINT,
    IN p_IdModulo           NVARCHAR(3),
 
    --VARIABLES DEL PROCESO
    IN  p_Periodo                   NVARCHAR(10)    DEFAULT '0001-01-01',
    IN  p_IdPersonaAGF              BIGINT          DEFAULT '0',
    IN  p_IdPersonaFondo            BIGINT          DEFAULT '0',
    IN  p_IdSerieFondo              NVARCHAR(40)    DEFAULT '',
    
    --VARIABLES DE SALIDA OBLIGATORIAS

    OUT p_out_TablaCir1804   "BSCL.Normativa.BD::NOR.SALIDA_CIR1804"

)
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL"
    AS
BEGIN
        --------------------
        --Declare Variables
        --------------------
        DECLARE v_IdInstrumento         BIGINT:=0;
        DECLARE v_SaldoInicial          DECIMAL(26,8);
        DECLARE v_ultimoDiaMesAnterior  NVARCHAR(10)    DEFAULT '0001-01-01';
        DECLARE v_PeriodoAnterior       NVARCHAR(7)     DEFAULT '0001-01';
        DECLARE v_SaldoActual           DECIMAL(26,8);
        DECLARE v_ultimoDiaMesActual    NVARCHAR(10)    DEFAULT '0001-01-01';
        DECLARE v_PeriodoActual         NVARCHAR(7)     DEFAULT '0001-01';
        
        DECLARE v_out_error                      NVARCHAR(500);
        DECLARE CONTINUE HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
		      v_IdInstrumento = NULL;
		END;
  

--CONSULTA SALDO INICIAL


--CON IDSERIEFONDO OBTENER IDINSTRUMENTO.

SELECT "IdInstrumento" INTO v_IdInstrumento FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
WHERE "IdSerieFondo" = :p_IdSerieFondo;



SELECT LAST_DAY (ADD_MONTHS (TO_DATE (:p_Periodo, 'YYYY-MM-DD'), -1)) INTO v_ultimoDiaMesAnterior FROM DUMMY;

SELECT SUBSTRING(:v_ultimoDiaMesAnterior, 0, 7) INTO v_PeriodoAnterior FROM DUMMY;


--SALDO INICIAL

CALL "BSCL.FondoMutuo.BD.Servicios::ConsultarCustodiaPortafolioFondoMutuo"(
:p_IdEmpresa,
:p_IdUsuario,
:p_IdModulo,
:p_IdPersonaAGF,
0,
0
,''
,'N'
,
:v_ultimoDiaMesAnterior,
NULL,
NULL,
'',
'',
v_CustodiaFFMMInicial);

--CALCULAR MONTO TOTAL SALDO INICIAL

SELECT SUM("Monto") INTO v_SaldoInicial FROM :v_CustodiaFFMMInicial;




SELECT LAST_DAY (TO_DATE (:p_Periodo, 'YYYY-MM-DD')) INTO v_ultimoDiaMesActual FROM DUMMY;

SELECT SUBSTRING(:p_Periodo, 0, 7) INTO v_PeriodoActual FROM DUMMY;

--SALDO ACTUAL

CALL "BSCL.FondoMutuo.BD.Servicios::ConsultarCustodiaPortafolioFondoMutuo"(
:p_IdEmpresa,
:p_IdUsuario,
:p_IdModulo,
:p_IdPersonaAGF,
0,
0
,''
,'N'
,
:p_Periodo,
NULL,
NULL,
'',
'',
v_CustodiaFFMMActual);

--CALCULAR MONTO TOTAL SALDO ACTUAL

SELECT SUM("Monto") INTO v_SaldoActual FROM :v_CustodiaFFMMActual;

--SALIDA
p_out_TablaCir1804 =
    SELECT
        SAR."IdPersonaAGF", SAR."IdSolicitudAporteRescate",
         :v_PeriodoActual AS "PeriodoInformado"
        ,SAR."RazonSocial" AS "RazonSocialAGF"
        ,SAR."DocumentoIdentidad" AS "DocumentoIdentidadAGF" --ConsultarCliente.
        ,SAR."NombreFondo" AS "NombreFondo"
        ,'1-9' AS "DocumentoIdentidadFondo"--   ,SAR."DocumentoIdentidadFondo" --Fondo.   Se a�ade cuando se cambie la vista ConsultarSolicitudAporteRescate y se a�ada la relacion con Fondo.
        ,/*SAR."IdMoneda"*/'CLP' AS "IdMoneda" -- declaro todo como CLP por la conversion de moneda en los campos "Monto"
        ,SAR."FechaCertificacion" AS "FechaTransaccion"
        ,SAR."DocumentoIdentidadDestino" AS "DocumentoIdentidadDelParticipe"
        ,'PR' AS "CodigoDelParticipe"
        ,:v_SaldoInicial    AS "PatrimonioNetoInicial"
        ,100 AS "PorcentajeSaldoInicial"
        ,:v_SaldoActual AS "PatrimonioNeto"
        ,CASE WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion" WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='USD' THEN SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD') ELSE 0 END AS "MontoTransadoEntrada"
        ,CASE WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion" WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='USD' THEN SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD') ELSE 0 END AS "MontoTransadoSalida"
        ,:v_SaldoInicial+sum(CASE WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion" WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='USD' THEN SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD') ELSE CASE WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion"*-1 WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='USD' THEN (SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD'))*-1 ELSE 0 END END) OVER (PARTITION BY SAR."IdPersonaAGF" ORDER BY SAR."IdSolicitudAporteRescate")AS "SaldoIndividual"
        ,CASE WHEN :v_SaldoInicial=0 THEN 0 ELSE  (:v_SaldoInicial+sum(CASE WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion" WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='USD' THEN SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD') ELSE CASE WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion"*-1 WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='USD' THEN (SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD'))*-1 ELSE 0 END END) OVER (PARTITION BY SAR."IdPersonaAGF" ORDER BY SAR."IdSolicitudAporteRescate"))/(:v_SaldoInicial) END  AS "SaldoIndividual_P"
        ,:v_SaldoActual+sum(CASE WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion" WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='USD' THEN SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD') ELSE CASE WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion"*-1 WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='USD' THEN (SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD'))*-1 ELSE 0 END END) OVER (PARTITION BY SAR."IdPersonaAGF" ORDER BY SAR."IdSolicitudAporteRescate")AS "SaldoTotal"
        ,CASE WHEN :v_SaldoActual=0 THEN 0 ELSE  (:v_SaldoActual+sum(CASE WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion" WHEN "TipoSolicitud" = 'APO' AND SAR."IdMoneda"='USD' THEN SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD') ELSE CASE WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='CLP' THEN SAR."MontoInversion"*-1 WHEN "TipoSolicitud" = 'RES' AND SAR."IdMoneda"='USD' THEN (SAR."MontoInversion"*(SELECT max("TipoCambioMercado") FROM "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" WHERE "IdMoneda"='USD'))*-1 ELSE 0 END END) OVER (PARTITION BY SAR."IdPersonaAGF" ORDER BY SAR."IdSolicitudAporteRescate"))/(:v_SaldoActual) END AS "SaldoTotal_P"
        FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" SAR 
        LEFT JOIN (SELECT distinct("IdPersonaDestino") FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" WHERE "IdEstado" = 'ACT') AS B ON SAR."IdPersonaAGF"=B."IdPersonaDestino"
        WHERE  SAR."IdPersonaAGF" = :p_IdPersonaAGF AND
        ( SAR."FechaCertificacion" BETWEEN ADD_MONTHS(NEXT_DAY(LAST_DAY(:p_Periodo)),-1) and LAST_DAY(:p_Periodo))
       -- ORDER BY SAR."IdPersonaAGF", SAR."IdSolicitudAporteRescate"
       ; 

END; 
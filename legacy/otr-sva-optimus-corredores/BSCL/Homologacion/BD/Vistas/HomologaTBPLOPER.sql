SET SCHEMA "BSCL"; 
--GO 
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaTBPLOPER";
--GO
create view "BSCL.Homologacion.BD.Vistas::HomologaTBPLOPER" as
SELECT
'OP' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
 OP."CodigoOperador" AS COD_OPERARIO,-- char(4) NOT NULL, ********************
'' AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
'' AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
'' AS CARGO,-- char(20) NOT NULL,
'' AS CASILLA,-- char(15) NOT NULL,
'' AS CENTRO_OPER,-- char(4) NOT NULL,
'' AS CIUDAD,-- char(4) NOT NULL,
'' AS COD_MESA_MON,-- char(4) NOT NULL,
'' AS COD_MESA_PAC,-- char(4) NOT NULL,
'' AS COMUNA,-- char(4) NOT NULL,
'' AS DIRECCION,-- char(40) NOT NULL,
'' AS FAX,-- char(20) NOT NULL,
'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
'' AS IND_HAB_OPER,-- char(1) NOT NULL,
'' AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
'' AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
'' AS PAIS,-- char(4) NOT NULL,
'' AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
'' AS TELEFONO,-- char(20) NOT NULL,
'' AS TIPO_COMIS,-- char(2) NOT NULL,
'' AS TIPO_FUNC,-- char(2) NOT NULL,
'' AS TIPO_INCEN,-- char(2) NOT NULL,
'' AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
'' AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
'' AS VER_AGE_01,-- char(4) NOT NULL,
'' AS VER_AGE_02,-- char(4) NOT NULL,
'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
'' AS PASSWORD,-- char(8) NOT NULL,
'' AS IND_EXP_PASS,-- char(1) NOT NULL,
'' AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
'' AS AGENTE_BCI,-- char(10) NOT NULL,
'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
'' AS DIREC_INTERNET,-- char(100) NOT NULL,
'' AS IND_VEN_UTIL-- char(1) NOT NULL,
FROM "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" OP
LEFT JOIN "BSCL.Plataforma.BD::TRA.HomologacionOperador" HO on HO."CodigoOperador" = OP."CodigoOperador"
UNION 
SELECT
'PA' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
OP."CodigoUsuario" AS COD_OPERARIO,-- char(4) NOT NULL, ********************
'' AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
OP."DocumentoIdentidad" AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
'' AS CARGO,-- char(20) NOT NULL,
'' AS CASILLA,-- char(15) NOT NULL,
'' AS CENTRO_OPER,-- char(4) NOT NULL,
'' AS CIUDAD,-- char(4) NOT NULL,
'' AS COD_MESA_MON,-- char(4) NOT NULL,
'' AS COD_MESA_PAC,-- char(4) NOT NULL,
'' AS COMUNA,-- char(4) NOT NULL,
'' AS DIRECCION,-- char(40) NOT NULL,
'' AS FAX,-- char(20) NOT NULL,
'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
'' AS IND_HAB_OPER,-- char(1) NOT NULL,
'' AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
OP."CodigoNombreAgOp" AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
'' AS PAIS,-- char(4) NOT NULL,
'' AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
'' AS TELEFONO,-- char(20) NOT NULL,
'' AS TIPO_COMIS,-- char(2) NOT NULL,
'' AS TIPO_FUNC,-- char(2) NOT NULL,
'' AS TIPO_INCEN,-- char(2) NOT NULL,
'' AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
'' AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
'' AS VER_AGE_01,-- char(4) NOT NULL,
'' AS VER_AGE_02,-- char(4) NOT NULL,
'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
'' AS PASSWORD,-- char(8) NOT NULL,
'' AS IND_EXP_PASS,-- char(1) NOT NULL,
'' AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
'' AS AGENTE_BCI,-- char(10) NOT NULL,
'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
'' AS DIREC_INTERNET,-- char(100) NOT NULL,
'' AS IND_VEN_UTIL-- char(1) NOT NULL,
FROM "BSCL.Mesa.BD.Vistas::ConsultarAgenteOperadorMesa" OP
UNION 
SELECT
'AG' AS TIPO_OPERARIO,-- char(2) NOT NULL, ******************** 
'' AS LUGAR_OPERAC,-- char(4) NOT NULL,
EJE."CodigoEmpleado" AS COD_OPERARIO,-- char(4) NOT NULL, ********************
'' AS CLAVE_LOGIN,-- decimal(3, 0) NOT NULL,
EJE."DocumentoIdentidad" AS RUT_OPERARIO,-- char(11) NOT NULL,  ********************
'' AS COD_GASTO_OPER,-- char(4) NOT NULL,
'' AS CARGO,-- char(20) NOT NULL,
'' AS CASILLA,-- char(15) NOT NULL,
'' AS CENTRO_OPER,-- char(4) NOT NULL,
'' AS CIUDAD,-- char(4) NOT NULL,
'' AS COD_MESA_MON,-- char(4) NOT NULL,
'' AS COD_MESA_PAC,-- char(4) NOT NULL,
'' AS COMUNA,-- char(4) NOT NULL,
'' AS DIRECCION,-- char(40) NOT NULL,
'' AS FAX,-- char(20) NOT NULL,
'' AS IND_CAJA_CHICA,-- char(1) NOT NULL,
'' AS IND_HAB_OPER,-- char(1) NOT NULL,
'' AS MONTO_MAX_MESA,-- decimal(6, 0) NOT NULL,
EJE."NombreCompleto" AS NOMBRE_OPERARIO,-- char(50) NOT NULL,  ********************
'' AS PAIS,-- char(4) NOT NULL,
'' AS SEC_RUT_OPER,-- decimal(2, 0) NOT NULL,
'' AS SUBCENTRO_OPER,-- char(4) NOT NULL,
'' AS TELEFONO,-- char(20) NOT NULL,
'' AS TIPO_COMIS,-- char(2) NOT NULL,
'' AS TIPO_FUNC,-- char(2) NOT NULL,
'' AS TIPO_INCEN,-- char(2) NOT NULL,
'' AS VAL_COMIS,-- decimal(15, 4) NOT NULL,
'' AS VAL_INCEN,-- decimal(15, 4) NOT NULL,
'' AS VER_AGE_01,-- char(4) NOT NULL,
'' AS VER_AGE_02,-- char(4) NOT NULL,
'' AS VER_CLI_TODOS,-- char(1) NOT NULL,
'' AS VER_ORD_TODAS,-- char(1) NOT NULL,
'' AS IND_SOBREGIRO,-- char(1) NOT NULL,
'' AS PASSWORD,-- char(8) NOT NULL,
'' AS IND_EXP_PASS,-- char(1) NOT NULL,
'' AS DIAS_EXP_PASS,-- decimal(3, 0) NOT NULL,
'' AS FEC_EXP_PASS,-- char(10) NOT NULL,
'' AS AGENTE_BCI,-- char(10) NOT NULL,
'' AS IND_SIMUL_EAI,-- char(1) NOT NULL,
'' AS DIREC_INTERNET,-- char(100) NOT NULL,
'' AS IND_VEN_UTIL-- char(1) NOT NULL,
FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJE
;
--GO
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaTBPLOPER" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
--GO
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','ASIGNACION_IF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:ASIGNACION_IF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_IF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','ASIGNACION_RF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:ASIGNACION_RF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','ASIGNACION_RV', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:ASIGNACION_RV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RV"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','BANCO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:BANCO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaBANCO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaBANCO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CIRCULARIZACION', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CIRCULARIZACION;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCIRCULARIZACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CLIENTE', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CLIENTE;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCLIENTE";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','COBRO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:COBRO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','COBRO_CANAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:COBRO_CANAL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','COMUNA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:COMUNA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCOMUNA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUENTA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CUENTA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUENTA_BANCARIA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CUENTA_BANCARIA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','DETALLE_ANOTACION', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:DETALLE_ANOTACION;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','DETALLE_INDICADOR', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:DETALLE_INDICADOR;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','DIRECCION', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:DIRECCION;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaDIRECCION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','EMPLEADO_CORREDORA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:EMPLEADO_CORREDORA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_CORREDORA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','EMPLEADO_FILIAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:EMPLEADO_FILIAL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_FILIAL";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','ENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:ENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','FILIAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:FILIAL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFILIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','FMCarteraForward', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:FMCarteraForward;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFMCarteraForward";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','FMMOVTOSFORWARD', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:FMMOVTOSFORWARD;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','FMMovtosSpot', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:FMMovtosSpot;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','INDICADOR', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:INDICADOR;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADOR";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','INDICADORES_CLIENTE', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:INDICADORES_CLIENTE;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','INDICADORES_CUENTA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:INDICADORES_CUENTA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','INDICADORES_TIPO_PERFIL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:INDICADORES_TIPO_PERFIL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_TIPO_PERFIL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','MERCADO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:MERCADO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaMERCADO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','MODELO_FACTURACION', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:MODELO_FACTURACION;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','MONEDA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:MONEDA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaMONEDA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_CARTERA_IF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_CARTERA_IF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_IF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_CARTERA_RF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_CARTERA_RF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_RF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_IF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_IF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_IF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_PACTO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_PACTO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_PACTO"


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_RF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_RF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_RF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_RV', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_RV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_RV"


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','ORDEN', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:ORDEN;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaORDEN";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PARAMETRO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:PARAMETRO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PERFIL_ACCESO_CUENTA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:PERFIL_ACCESO_CUENTA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ACCESO_CUENTA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PERSONA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:PERSONA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPERSONA";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPERSONA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PLANTILLA_COBRO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:PLANTILLA_COBRO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_COBRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PLANTILLA_PRODUCTO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:PLANTILLA_PRODUCTO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO";



INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PRODUCTO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:PRODUCTO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','RAZONES_SOCIALES', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:RAZONES_SOCIALES;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','REGION', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:REGION;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaREGION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','RELACIONES', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:RELACIONES;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','RELACIONES_CUENTA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:RELACIONES_CUENTA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','RELACIONES_FUNCIONARIOS', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:RELACIONES_FUNCIONARIOS;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','SOLICITUD', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:SOLICITUD;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaSOLICITUD";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','SUBCENTRO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:SUBCENTRO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','SUCURSAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:SUCURSAL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCODTSD', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCODTSD;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCODTSD";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCOMSSD', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCOMSSD;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCOMSSD";



INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCPDTPC', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCPDTPC;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCPMSPC', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCPMSPC;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCIIF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCIIF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCIFM', 'I', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCIRF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIFM";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCIRF', 'I', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCIRF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRF";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCIRV', 'I', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCIRV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCTCL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCTCL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCUFM', 'I', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCUFM;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUFM";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUFM"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCURF', 'I', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCURF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF";

delete from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" 

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCURV', 'I', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCURV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURV";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCUST', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCUST;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTDTCF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTDTCF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTECCL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTECCL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTECCL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTGTIA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTGTIA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTMSCF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTMSCF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTMVTO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTMVTO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO";
SELECT fec_mvto,sec_movto,unidad_sol,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" GROUP BY fec_mvto,sec_movto,unidad_sol
HAVING count(1) > 1
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO"
WHERE fec_mvto = '2021-03-15' AND sec_movto = 105983


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTTRAS', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTTRAS;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRAS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTTRCO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTTRCO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRCO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTTRTP', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTTRTP;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRTP";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRTP";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBMOMVTO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBMOMVTO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLFICL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLFICL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLGLOS', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLGLOS;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLMAIN', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLMAIN"

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLMAIN";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLOPER', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLOPER;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLOPER";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLPASI', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLPASI;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPASI";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLPERS', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLPERS;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLRECL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLRECL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLTBIF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLTBIF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBIF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLTBRF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLTBRF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRF";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLTBRV', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLTBRV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV";
SELECT fecha,folio_trans,tipo_operac,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV" GROUP BY folio_trans,tipo_operac,fecha HAVING count(1) > 1

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBPLTRANPARAM', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBPLTRANPARAM;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBSILEVC', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBSILEVC;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBSILEVC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBSITRAD', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBSITRAD;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBTEBACC', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBTEBACC;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEBACC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBTECMBT', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBTECMBT";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBTECMBT" brbvrt 

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBTEDCTO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBTEDCTO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEDCTO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TELEFONO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TELEFONO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTELEFONO";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TIPO_OPERACION', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TIPO_OPERACION;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TIPO_PERFIL_COMERCIAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TIPO_PERFIL_COMERCIAL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','USO_DIRECCIONES', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:USO_DIRECCIONES;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','USO_TELEFONOS', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:USO_TELEFONOS;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS";







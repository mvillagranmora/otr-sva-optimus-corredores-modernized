INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'BANCO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaBANCO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'CLIENTE', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCLIENTE";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'COBRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'COBRO_CANAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','COMUNA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:COMUNA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaCOMUNA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'CONF_NOTIFICACIONES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCONF_NOTIFICACIONES";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'CUENTA_BANCARIA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'DETALLE_ANOTACION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'DETALLE_INDICADOR', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'DIRECCION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaDIRECCION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'EMPLEADO_CORREDORA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_CORREDORA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'EMPLEADO_FILIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_FILIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'ENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'ESTADO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaESTADO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'FILIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaFILIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'GIRO_COMERCIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaGIRO_COMERCIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'INDICADOR', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADOR";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'INDICADORES_CLIENTE', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'INDICADORES_CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'INDICADORES_TIPO_PERFIL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_TIPO_PERFIL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'MERCADO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaMERCADO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'MODELO_FACTURACION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'MONEDA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaMONEDA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'OPCIONES_ENTREGA_MAILING', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaOPCIONES_ENTREGA_MAILING";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'PAIS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPAIS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PARAMETRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PERFIL_ACCESO_CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ACCESO_CUENTA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PERSONA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPERSONA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PLANTILLA_COBRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_COBRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PLANTILLA_PRODUCTO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PRODUCTO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'RAZONES_SOCIALES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'REGION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaREGION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'RELACIONES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'RELACIONES_CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA" c;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'RELACIONES_FUNCIONARIOS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'SUBCENTRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'SUCURSAL', 'I', 0,  CURRENT_TIMESTAMP , ''
FROM "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCOPLAN', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCOPLAN";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLFICL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLGLOS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLMAIN', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLMAIN";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLOPER', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLOPER";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLPASI', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPASI";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLPERS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLRECL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLTMOV', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLTRANPARAM', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBSITRAD', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBTEBACC', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEBACC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TELEFONO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTELEFONO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TIPO_OPERACION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TIPO_PERFIL_COMERCIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'USO_DIRECCIONES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'USO_TELEFONOS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS";

--DELETE FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"
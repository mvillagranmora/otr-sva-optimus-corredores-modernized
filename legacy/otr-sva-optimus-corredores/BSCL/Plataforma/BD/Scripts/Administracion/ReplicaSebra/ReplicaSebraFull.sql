INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'ASIGNACION_IF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_IF";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_IF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'ASIGNACION_RF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RF";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'ASIGNACION_RV', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RV";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RV"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'BANCO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaBANCO";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaBANCO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaBANCO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CIRCULARIZACION', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CIRCULARIZACION;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCIRCULARIZACION";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaCIRCULARIZACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'CLIENTE', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCLIENTE";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCLIENTE";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'COBRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'COBRO_CANAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaCOBRO_CANAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','COMUNA', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:COMUNA;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaCOMUNA";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCOMUNA";
SELECT com_id,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCOMUNA" GROUP BY com_id HAVING count(1) > 1

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaCOMUNA"


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'CONF_NOTIFICACIONES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCONF_NOTIFICACIONES";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCONF_NOTIFICACIONES";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'CUENTA_BANCARIA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA";

select CBA_ID ,count(1)
FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA" group by CBA_ID 
having count(1) > 1

select *
FROM "BSCL.Replica.BD.Vistas::ReplicaCUENTA_BANCARIA" where CBA_ID  = 13276

select * from "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM where (HOM."TipoValor" = 'TipoCuenta' AND HOM."Entidad" = 'CUENTACORRIENTE' )


SELECT hom.*
FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTA ON PER."IdPersona" = CTA."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON (ban."IdBanco"  = CTA."IdBanco" )
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON BAN."IdPaisOperacion" = PAIS."IdPais" 
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM ON (HOM."TipoValor" = 'TipoCuenta' AND HOM."Entidad" = 'CUENTACORRIENTE' AND HOM."ValorDestino" = CTA."IdTipoCuentaBancaria")
WHERE CTA."Borrado" = 'N' AND CTA."IdCuentaBancaria"  = 13276



INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'DETALLE_ANOTACION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_ANOTACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'DETALLE_INDICADOR', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'DIRECCION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaDIRECCION";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaDIRECCION";
SELECT dir_id,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaDIRECCION" GROUP BY dir_id HAVING count(1) > 1
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaDIRECCION" WHERE dir_id = 67

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'EMPLEADO_CORREDORA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_CORREDORA";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_CORREDORA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'EMPLEADO_FILIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_FILIAL";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaEMPLEADO_FILIAL";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'ENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaENCUESTA_INVERSIONISTA_CALIFICADOS_HIST_CLIENTE";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'ESTADO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaESTADO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaESTADO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaESTADO";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'FILIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaFILIAL";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFILIAL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaFILIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','FMCarteraForward', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:FMCarteraForward;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFMCarteraForward";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'FMMOVTOSFORWARD', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD" WHERE FECHACREACION >= '2021-04-01';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD" WHERE FECHACREACION >= '2021-04-01';


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'FMMovtosSpot', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot" WHERE FECHACREACION = '2022-01-17' ;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaFMMovtosSpot";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")

SELECT table_key,'GIRO_COMERCIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaGIRO_COMERCIAL";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaGIRO_COMERCIAL";

SELECT GIR_ID ,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaGIRO_COMERCIAL" GROUP BY GIR_ID HAVING count(1)>1;

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaGIRO_COMERCIAL";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'INDICADOR', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADOR";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADOR";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADOR";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'INDICADORES_CLIENTE', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" where DIN_ID is null

select * from "BSCL.Replica.BD.Vistas::ReplicaINDICADOR" brbvri where IND_ID  = 9000


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'INDICADORES_CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'INDICADORES_TIPO_PERFIL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_TIPO_PERFIL";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_TIPO_PERFIL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_TIPO_PERFIL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'MERCADO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaMERCADO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaMERCADO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaMERCADO";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaMERCADO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'MODELO_FACTURACION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaMODELO_FACTURACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'MONEDA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaMONEDA";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaMONEDA";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaMONEDA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'OPCIONES_ENTREGA_MAILING', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaOPCIONES_ENTREGA_MAILING";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPCIONES_ENTREGA_MAILING"

SELECT  "BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".IDSTACK,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".ACTION,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".TABLE_KEY,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".PERID,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".CUENTA,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".NOTIFICACION_DIARIA,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".DIR_ID_PRIMARIO,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".DIR_ID_SECUNDARIO,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".SUSPENCION_INICIO,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".SUSPENCION_FIN,"BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING".SUSPENCION FROM BSCL."BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING" "BSCL.Replica.BD.Vistas::Sebra.OPCIONES_ENTREGA_MAILING"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'PAIS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPAIS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PARAMETRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PERFIL_ACCESO_CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ACCESO_CUENTA";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ACCESO_CUENTA";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPERFIL_ACCESO_CUENTA";



INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PERSONA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPERSONA";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPERSONA";

SELECT PER_ID ,COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPERSONA" GROUP BY PER_ID HAVING COUNT(1) > 1 

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPERSONA" WHERE PER_SEXO is null;


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PLANTILLA_COBRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_COBRO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_COBRO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_COBRO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PLANTILLA_PRODUCTO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPLANTILLA_PRODUCTO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'PRODUCTO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaPRODUCTO";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_CARTERA_IF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_CARTERA_IF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_IF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_CARTERA_RF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_CARTERA_RF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_RF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_IF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_IF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_IF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_PACTO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_PACTO;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_PACTO"


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_RF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_RF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_RF"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','OPERACION_RV', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:OPERACION_RV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_RV"


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','ORDEN', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:ORDEN;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaORDEN";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaORDEN" where OPE_ID  is null

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'RAZONES_SOCIALES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaRAZONES_SOCIALES";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'REGION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaREGION";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaREGION";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaREGION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'RELACIONES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'RELACIONES_CUENTA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA" c;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA" where PER_ID  = 16573731;

SELECT PER_ID ,cuenta,FIL_ID ,PER_ID_REL ,PAR_TRE_ID ,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_CUENTA"
group by PER_ID ,cuenta,FIL_ID ,PER_ID_REL ,PAR_TRE_ID 
having count(1) > 1

16573731	0	0	16210208	TRELACONYU	2
3839508	0	0	10969461	TRELALEGAL	2
77611702	0	0	8597933	TRELAAPODE	2

select * from "BSCL.Homologacion.BD::HOM.Homologacion" where "TipoValor"  = 'TipoRelacion'

SELECT *
					FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona" REL
		INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio" POR ON POR."IdPersona" = REL."IdPersonaOrigen"
		and por."DocumentoIdentidad" != '15.829.477-K'
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PERREL ON REL."IdPersonaDestino" = PERREL."IdPersona"      
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCREL ON  (DOCREL."IdDocumentoIdentidad" = PERREL."IdDocumentoIdentidad")
		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hr 
						ON (hr."TipoValor" = 'TipoRelacion' and hr."ValorDestino" = REL."IdTipoRelacion"  )
		WHERE DOCREL."DocumentoIdentidad" not like '%.%' and POR."DocumentoIdentidad" not like '%.%'
		and POR."DocumentoIdentidad"  = '77611702-1'


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'RELACIONES_FUNCIONARIOS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaRELACIONES_FUNCIONARIOS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','SOLICITUD', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:SOLICITUD;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
--quizas filtrar lo que se envia por fecha, dado que las ordenes se cargan de forma historica
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaSOLICITUD";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaSOLICITUD" where OPE_ID is null;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'SUBCENTRO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'SUCURSAL', 'I', 0,  CURRENT_TIMESTAMP , ''
FROM "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaSUCURSAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCODTSD', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCODTSD" WHERE fec_cmbte >= '2021-04-01';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCODTSD";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCODTSD";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCOMSSD', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCOMSSD;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCOMSSD";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCOPLAN', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCOPLAN";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCOPLAN";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCOPLAN";



INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCPDTPC', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCPDTPC;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT ("IdReserva"),'TBCPDTPC', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC"

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPDTPC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCPMSPC', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCPMSPC;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCPMSPC";


SELECT * FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCIFM"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCIIF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCIIF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIF";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIIF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCIRF', 'I', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCIRF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRF" WHERE cod_per >= '2021-04';

SELECT table_key,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRF" GROUP BY table_key HAVING count(1) > 1


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTCIRV', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCIRV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCTCL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCTCL;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCTCL"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCUFM', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCUFM;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT ("FechaCustodia" || ':' || "IdPortafolio" || ':' || "NEMO"),'TBCTCUFM', 'I', 0,  CURRENT_TIMESTAMP 
FROM "BSCL.Replica.BD.Vistas::Sebra.TBCTCUFM"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCURF', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCURF;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','TBCTCURV', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:TBCTCURV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCURV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTCUST', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTCUST";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTDTCF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF" WHERE folio_comp_fac IN (SELECT FOLIO_COMP_FAC FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF" WHERE  fec_ing_comp >= '2021-04-01') AND folio_comp_fac > 11202;


SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF" WHERE table_key = 'FD:178912:1:2003-09-02'
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF" WHERE folio_comp_fac = 178912
SELECT table_key,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF" GROUP BY table_key HAVING count(1) > 1

SELECT * FROM "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" ORDER BY "IdComprobante" desc
SELECT "FolioComprobante",count(1) FROM "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante" GROUP BY "FolioComprobante" HAVING count(1) > 1

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF" WHERE folio_comp_fac = 179014


SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTDTCF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTECCL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTECCL" WHERE fec_mvto >= '2021-04-01';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTECCL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTECCL" ;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTGTIA', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA" WHERE cod_per >= '2021-04';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTGTIA";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBCTMSCF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM  "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF" WHERE fec_ing_comp >= '2021-04-01';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBCTMVTO', 'I', 0,  CURRENT_TIMESTAMP , ''
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2022-01-18' AND fec_mvto <= '2022-01-31'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2022-02-01' AND fec_mvto <= '2022-02-28'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2022-03-01' AND fec_mvto <= '2022-03-31'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2022-04-01' AND fec_mvto <= '2022-04-30'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-01-01' AND fec_mvto <= '2023-01-31'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-02-01' AND fec_mvto <= '2023-02-28'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-03-01' AND fec_mvto <= '2023-03-31'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-04-01' AND fec_mvto <= '2023-04-30'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-05-01' AND fec_mvto <= '2023-05-31'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-06-01' AND fec_mvto <= '2023-06-30'
--FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-07-01' AND fec_mvto <= '2023-07-31'
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" WHERE fec_mvto >= '2023-08-01' AND fec_mvto <= '2023-08-31'

delete from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" where "Table"  = 'TBCTMVTO'

select FEC_MVTO ,count(1) 
FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO"
where FEC_MVTO >= '2023-01-01'
group by FEC_MVTO order by FEC_MVTO desc

select * from "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" brbvrt where IDSTACK is not null

SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws  WHERE "Status"  = '0' AND "Table"  = 'TBCTMVTO'

SELECT "Table" ,count(1) FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 
WHERE "Status"  = '0'
and "Stamp" >= '2023-08-20 00:00:00'
group by "Table" 

select count(1) from "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" t
where FEC_MVTO >= '2023-01-01'  


SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO";
SELECT fec_mvto,sec_movto,unidad_sol,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" GROUP BY fec_mvto,sec_movto,unidad_sol
HAVING count(1) > 1

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO"
WHERE fec_mvto = '2021-03-15' AND sec_movto = 105983

1348802022-02-09


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
    'tables:TBCTTRTP;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;
SELECT  COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRTP";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBCTTRTP";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBMOMVTO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO" WHERE fec_mvto >= '2021-04-01';
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLFICL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLFICL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLGLOS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLGLOS";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLMAIN', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLMAIN";

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLMAIN";

SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLMAIN";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLOPER', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLOPER";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLOPER";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLOPER";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLPASI', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPASI";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPASI";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPASI";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLPERS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS";
SELECT table_key,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLPERS" GROUP BY table_key HAVING count(1) > 1;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLRECL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLRECL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLTBIF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBIF" WHERE fecha >= '2021-04-01';;

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBIF";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLTBRF', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRF" WHERE fecha >= '2021-04-01';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRF";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBPLTBRV', 'I', 0,  CURRENT_TIMESTAMP , ''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV" WHERE fecha >= '2021-04-01';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV" WHERE fecha >= '2021-04-01';
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV";
SELECT fecha,folio_trans,tipo_operac,count(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTBRV" GROUP BY folio_trans,tipo_operac,fecha HAVING count(1) > 1

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLTMOV', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBPLTRANPARAM', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBSILEVC', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBSILEVC" WHERE fec_pago >= '2021-04-01';

SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBSILEVC";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBSILEVC" WHERE fec_pago >= '2021-04-01';

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TBSITRAD', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBTEBACC', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEBACC";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEBACC";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEBACC";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TBTEDCTO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEDCTO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEDCTO";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBTEDCTO";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TELEFONO', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTELEFONO";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTELEFONO";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT TABLE_KEY,'TIPO_OPERACION', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'TIPO_PERFIL_COMERCIAL', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'USO_DIRECCIONES', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_DIRECCIONES";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT table_key,'USO_TELEFONOS', 'I', 0,  CURRENT_TIMESTAMP ,''
FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS";
SELECT COUNT(1) FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS";
SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS";







delete FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"

SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack"


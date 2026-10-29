SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoInstrumento"; 

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ACN','AC_NAC','ACCIONES NACIONALES','ACCIONES NACIONALES','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ACX','AC_EXT','ACCIONES EXTRANJERAS','ACCIONES EXTRANJERAS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('MIL','AC_MIL','ACCIONES MILA','ACCIONES MILA','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CFI','CFI','CUOTAS DE FONDOS DE INVERSION','CUOTAS DE FONDOS DE INVERSION','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CFM','CFM','CUOTAS DE FONDOS MUTUOS','CUOTAS DE FONDOS MUTUOS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CFX','CFX','CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('DIV','DIVISA','DIVISAS','DIVISAS','VIG','MO');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('EMP','EMPRESTITO','EMPRESTITO','EMPRESTITO','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ETN','ETF_NAC','ETF NACIONALES','ETF NACIONALES','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ETX','ETF_EXT','ETF EXTRANJEROS','ETF EXTRANJEROS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FFM','FFMM','FONDOS MUTUOS','FONDOS MUTUOS','VIG','FM');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('MON','MON','MONETARIOS','MONETARIOS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PAG','PAGARE','PAGARE','PAGARE','VIG','IF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PRT','PRESTAMO','PRESTAMOS','PRESTAMOS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('SIM','SIM','SIMULTANEA','SIMULTANEA','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FDO','FUT_DOLAR','FUTUROS DOLAR','FUTUROS DOLAR','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FIP','FUT_IPSA','FUTUROS IPSA','FUTUROS IPSA','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FBC','FUT_BCU','FUTUROS BCU','FUTUROS BCU','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FIC','FUT_ICP','FUTUROS ICP','FUTUROS ICP','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FUF','FUT_UF','FUTUROS UF','FUTUROS UF','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BBC','BB','BONOS BANCARIOS','BONOS BANCARIOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCD','BCD','BANCO CENTRAL BONOS BULLET EN DOLARES','BANCO CENTRAL BONOS BULLET EN DOLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCP','BCP','BANCO CENTRAL BONOS BULLET EN PESOS','BANCO CENTRAL BONOS BULLET EN PESOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCU','BCU','BANCO CENTRAL BONOS BULLET EN UF','BANCO CENTRAL BONOS BULLET EN UF','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BEM','BE','BONOS EMPRESAS','BONOS EMPRESAS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CER','CERO','CUPONES DE EMISION REAJUSTABLES EN UF','CUPONES DE EMISION REAJUSTABLES EN UF','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FRW','FORW','FORWARDS','FORWARDS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('LTH','LH','LETRAS HIPOTECARIAS','LETRAS HIPOTECARIAS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PRC','PRC','PAGARE REAJUSTABLE DEL BANCO CENTRAL','PAGARE REAJUSTABLE DEL BANCO CENTRAL','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ZER','ZERO','CUPONES DE EMISION REAJUSTABLES EN DOLARES','CUPONES DE EMISION REAJUSTABLES EN DOLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PAC','PAC','PACTOS','PACTOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCA','BC','BONO CACSA','BONO CACSA','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCX','BCX','BONOS BANCO CENTRAL EN DÓLARES','BONOS BANCO CENTRAL EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BTP','BTP','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN PESOS','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN PESOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BTU','BTU','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN UF','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN UF','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BUS','BU','BONO SUBORDINADO','BONO SUBORDINADO','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BVL','BVL','MINISTERIO DE VIVIENDO Y URBANISMO LEASING','MINISTERIO DE VIVIENDO Y URBANISMO LEASING','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('COR','CORA','BONO REFORMA AGRARIA','BONO REFORMA AGRARIA','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PCX','PCX','PAGARÉS BANCO CENTRAL EN DÓLARES','PAGARÉS BANCO CENTRAL EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PRD','PRD','PAGARÉS REAJUSTABLES DEL BANCO CENTRAL DE CHILE EN DÓLARES','PAGARÉS REAJUSTABLES DEL BANCO CENTRAL DE CHILE EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('XER','XERO','CUPONES DE EMISIÓN DEL BANCO CENTRAL DE CHILE EN DÓLARES','CUPONES DE EMISIÓN DEL BANCO CENTRAL DE CHILE EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('OSA','OSA','OPCIONES DE SUSCRIPCION DE ACCIONES','OPCIONES DE SUSCRIPCION DE ACCIONES','VIG','RV');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BR','BR','BONO DE RECONOCIMIENTO','BONO DE RECONOCIMIENTO','VIG','Rf');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BH','BH','BONO HIPOTECARIO','BONO HIPOTECARIO','VIG','RF');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('TPR','TPR','PAGARE REAJUSTABLE DE LA TESORERIA','PAGARE REAJUSTABLE DE LA TESORERIA','VIG','IF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PDB','PDB','PAGARE DESCONTABLE DEL BANCO CENTRAL','PAGARE DESCONTABLE DEL BANCO CENTRAL','VIG','IF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PDT','PDT','PAGARE DESCONTABLE TESORERIA','PAGARE DESCONTABLE DE LA TESORERIA','VIG','IF');

/*
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'NR',5000000 
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE NR' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'US',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'EU',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'UF',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'NR',5000000 
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PDBC' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'UF',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PRBC' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'NR',5000000 
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PDT' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'UF',5000
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PRT' and "IdTipoInstrumento" = 'PAG';

*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;

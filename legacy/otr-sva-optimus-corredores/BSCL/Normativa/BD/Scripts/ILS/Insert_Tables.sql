SET SCHEMA "BSCL";

DELETE FROM "BSCL.Normativa.BD::ILS.InformeTipo"
DELETE FROM "BSCL.Normativa.BD::ILS.ItemTipo"
DELETE FROM "BSCL.Normativa.BD::ILS.ProcesoFuncion";

/*==================================================================*/
/*            "BSCL.Normativa.BD::ILS.InformeTipo"              */
/*==================================================================*/
INSERT INTO "BSCL.Normativa.BD::ILS.InformeTipo"
("IdTipoInforme"
,"IdEmpresa"
,"Nombre"
,"Estado")
VALUES(1,1,'INFORME TEST','VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.InformeTipo"
("IdTipoInforme"
,"IdEmpresa"
,"Nombre"
,"Estado")
VALUES(2,1,'TEST INFORME 1','VIG'); 

/*==================================================================*/
/*            "BSCL.Normativa.BD::ILS.ItemTipo"              */
/*==================================================================*/
INSERT INTO "BSCL.Normativa.BD::ILS.ItemTipo"
("IdTipoItem"
,"Nombre"
,"Orden"
,"Estado")
VALUES('CTB','CONTABLE',1,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ItemTipo"
("IdTipoItem"
,"Nombre"
,"Orden"
,"Estado")
VALUES('DIG','DIGITADA',1,'VIG'); 

INSERT INTO "BSCL.Normativa.BD::ILS.ItemTipo"
("IdTipoItem"
,"Nombre"
,"Orden"
,"Estado")
VALUES('PRG','PROCEDIMIENTO DE SISTEMA',1,'VIG'); 


/*==================================================================*/
/*            "BSCL.Normativa.BD::ILS.ProcesoFuncion"              */
/*==================================================================*/
INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(1,'NOR_IlsSP_QA_1','NEMO',1,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(2,'NOR_IlsSP_QA_2','NEMO',2,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(3,'NOR_IlsSP_QA_3','NEMO',3,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(4,'NOR_IlsSP_QA_4','NEMO',4,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(5,'NOR_IlsSP_QA_5','NEMO',5,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(6,'NOR_IlsSP_QA_6','NEMO',6,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(7,'Nor_IlsSp_CarteraObligaciones','NEMO',1,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(8,'Nor_IlsSp_SimultaneasGarantias','NEMO',2,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(9,'Nor_IlsSp_SimultaneasDerechos','NEMO',3,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(10,'Nor_IlsSp_SimultaneasObligaciones','NEMO',4,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(11,'Nor_IlsSp_DerechosMonedaExtranjera','NEMO',4,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(12,'Nor_IlsSp_ObligacionMenosDerechoMonExt','NEMO',4,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(13,'Nor_IlsSp_ObligacionRetroVenta','NEMO',4,'VIG');

INSERT INTO "BSCL.Normativa.BD::ILS.ProcesoFuncion"
("IdProcesoFuncion"
,"Nombre"
,"Nemo"
,"Orden"
,"Estado")
VALUES(14,'Nor_IlsSp_DerechoRetroCompra','NEMO',4,'VIG');

/*===================================*/
/*            SERVICIOS              */
/*===================================*/

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
VALUES(671,'BSCL.Normativa.BD.Servicios::ILS.IndiceBuscar',0,'P','Buscador Indices por Indices');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
VALUES
(672,'BSCL.Normativa.BD.Servicios::ILS.IndiceInformeBuscar',0,'P','Buscador Indices por Informes');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
VALUES 
(13041,'BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeResultadoBuscar',0,'Q','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
VALUES
(13042,'BSCL.Normativa.BD.Servicios::ILS.MantenedorInformeConfiguracion',0,'P','Insert y Delete Indices');


insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
values 
(13044,'BSCL.Normativa.BD.Vistas::ILS.ConsultaIndiceInforme',0,'Q','');

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
values 
(13045,'BSCL.Normativa.BD.Servicios::ILS.GeneraReporteILS',0,'P','Genera Reportes ILS');

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
values 
(13046,'BSCL.Normativa.BD.Servicios::ILS.Consultap_SaldosCuentasContables',0,'P','Saldos Cuentas Contables');

/*===================================*/
/*            FORMATOS INDICES       */
/*===================================*/

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato"
(	"IdMonedaFormato",
	"IdMoneda",
	"IdUsoMonedaFormato",
	"IdTipoInstrumento",
	"IdTipoOperacion",
	"PrecioDecimales",
	"PrecioTrucaRedondea",
	"TasaDecimales",
	"TasaTrucaRedondea",
	"CantidadDecimales",
	"CantidadTrucaRedondea",
	"MontoDecimales",
	"MontoTrucaRedondea",
	"TipoCambioDecimales",
	"TipoCambioTrucaRedondea",
	"ParidadDecimales",
	"ParidadTrucaRedondea",
	"PuntosDecimales",
	"PuntosTrucaRedondea",
	"IdEstado"
	)
	VALUES(344,'USD','ILS','','INDICE','4','T','4','T','4','T','4','T','4','T','4','T','4','T','ACT');


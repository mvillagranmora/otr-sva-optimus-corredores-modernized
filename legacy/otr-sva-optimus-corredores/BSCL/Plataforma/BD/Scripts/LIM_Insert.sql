SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM "BSCL.Plataforma.BD::LIM.GrupoLimite";
DELETE FROM "BSCL.Plataforma.BD::LIM.TipoLimite";
DELETE FROM "BSCL.Plataforma.BD::LIM.Limite";
DELETE FROM "BSCL.Plataforma.BD::LIM.EstadoLim";
DELETE FROM "BSCL.Plataforma.BD::LIM.LimiteEmisor";
DELETE FROM "BSCL.Plataforma.BD::LIM.RangosMontoPlazo";
--DELETE FROM "BSCL.Plataforma.BD::LIM.LimiteClasificacionRiesgo";


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

--INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
--VALUES('RV','LÍMITES DE RENTA VARIABLE');
--INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
--VALUES('RF','LÍMITES DE RENTA FIJA');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('PORTAFOLIO','PORTAFOLIO');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('CLASIFICACIONRIESGO','LÍMITES CLASIFICACIÓN RIESGO');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('MARGENTASAS','LÍMITES MARGEN TASAS');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('PLAZO','LÍMITES PLAZO');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('SPOT','LÍMITES DE MESA SPOT');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('FORWARD','LÍMITES FORWARD');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('CARTERA','LÍMITES CARTERA');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('CLIENTE','LÍMITES CLIENTE');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('AGENTE','LÍMITES AGENTE');
INSERT INTO "BSCL.Plataforma.BD::LIM.GrupoLimite"("IdGrupoLimite","Descripcion")
VALUES('EXCLUIREMISOR','LÍMITES EXCLUIR EMISOR');


/*
insert into "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado")

SELECT 'LIMCPCARTERA',1,'Limite de Cartera para Cuenta Propia','S','RAU','CARTERA','POSICION','ACT'  FROM DUMMY
UNION ALL
SELECT 'LIMRVCPPLAZO',1,'Limite en Cuenta Propia para Operaciones a Plazo en Renta Variable','S','RZD','RV','OPERACION','ACT'  FROM DUMMY
UNION ALL
SELECT 'LIMRVCPOPERACION',1,'Limite en Cuenta Propia para Ingreso de Operaciones por Canal','S','RZD','RV','OPERACION','ACT'  FROM DUMMY
UNION ALL
SELECT 'LIMRVCPEXCLUYEEMISOR',1,'Exclusión de Emisores en Ingreso de Operaciones RV en Cuenta Propia','S','RZD','RV','OPERACION','ACT'  FROM DUMMY
UNION ALL
SELECT 'LIMRVCPEXCLUYECLASIFICACION',1,'Exclusión de Instrumentos RV segun Lista a la cual pertenecen','S','RZD','RV','OPERACION','ACT'  FROM DUMMY
UNION ALL
SELECT 'LIMRVCTPLAZO',1,'Limite en Cuenta Tercero para Operaciones a Plazo en Renta Variable','N','RZD','RV','OPERACION','ACT'  FROM DUMMY
UNION ALL
SELECT 'LIMRVCTOPERACION',1,'Limite en Cuenta Tercero para Ingreso de Operaciones por Canal','N','RZD','RV','OPERACION','ACT' FROM DUMMY
UNION ALL
SELECT 'LIMRVCPMARGEN',1,'Limite en Cuenta Propia para márgenes de Operaciones a plazo','S','RZD','RV','OPERACION','ACT' FROM DUMMY
UNION ALL
SELECT 'LIMCLLINEARIESGO',1,'Limite de Linea de Riesgo','N','RZD','CLIENTE','OPERACION','ACT' FROM DUMMY
UNION ALL
SELECT 'LIMAGLINEARIESGO',1,'Limite de Linea de Riesgo','N','RZD','AGENTE','OPERACION','ACT' FROM DUMMY
;
*/

---CLIENTE/PORTAFOLIO
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMPORTPRODUCTO',1,'LÍMITE PORTAFOLIO PRODUCTO','N','AUT','PORTAFOLIO','OPERACION','ACT','LIPPRO',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMPORTSOBREGIRO',1,'LÍMITE PORTAFOLIO SOBREGIRO','N','AUT','PORTAFOLIO','OPERACION','ACT','LIPSGR',1);
---CUENTA PROPIA
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPPLAZO',1,'LÍMITE CUENTA PROPIA PLAZO','S','AUT','PLAZO','OPERACION','ACT','LIPLCP',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPEXCLUIREMISOR',1,'LÍMITE CUENTA PROPIA EXCLUIR EMISOR','S','AUT','EXCLUIREMISOR','OPERACION','ACT','LIEECP',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPCLASIFRIESGO',1,'LÍMITE CUENTA PROPIA CLAS. RIESGO','S','AUT','CLASIFICACIONRIESGO','OPERACION','ACT','LICRCP',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPMARGENTASAS',1,'LÍMITE CUENTA PROPIA MARGEN TASAS','S','AUT','MARGENTASAS','OPERACION','ACT','LIMTCP',1);
-- Tipo Limite SPOT
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPPOSICIONINTERBANCARIO',1,'LÍMITE CUENTA PROPIA POSICIÓN INTERBANCARIO','S','AUT','SPOT','OPERACION','ACT','LISICP',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPMESADISTRIBUCION',1,'LÍMITE CUENTA PROPIA MESA DISTRIBUCIÓN','S','AUT','SPOT','OPERACION','ACT','LISDCP',1);
-- Tipo Limite FORWARD
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPFORWARD',1,'LÍMITE CUENTA PROPIA FORWARD','S','AUT','FORWARD','OPERACION','ACT','LIFDCP',1);
-- Tipo Limite CARTERA
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPCARTERA',1,'LÍMITE CUENTA PROPIA CARTERA','S','AUT','CARTERA','OPERACION','ACT','LICACP',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPGRUPOEMISOR',1,'LÍMITE CUENTA PROPIA GRUPO EMISOR','S','AUT','CARTERA','OPERACION','ACT','LICGCP',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPEMISOR',1,'LÍMITE CUENTA PROPIA EMISOR','S','AUT','CARTERA','OPERACION','ACT','LIEMCP',1);
-- Tipo Limite AGENTE
INSERT INTO "BSCL.Plataforma.BD::LIM.TipoLimite"("IdTipoLimite","IdEmpresa","Nombre","EsCuentaPropia","IdComportamiento","IdGrupoLimite","Categoria","IdEstado","IdTipoEvento","Prioridad")
VALUES('LIMCTPAGENTE',1,'LÍMITE CUENTA PROPIA AGENTE','S','AUT','AGENTE','OPERACION','ACT','LIAGCP',1);



/*
insert into "BSCL.Plataforma.BD::LIM.Limite"
("IdLimite","IdTipoLimite","Nombre","IdMesa","IdCanal","IdCategoriaInversionista","IdUsuario","IdCliente","IdPortafolio","IdTipoOperacion","IdTipoInstrumento","IdInstrumento", "UnidadPlazo","Plazo","IdTipoReajuste","CodigoOperador","MontoTotal",
"MontoIntraday","MontoOvernight","IdMoneda","IdMonedaCalculo","ClasificacionRiesgoMinima","IdEstado","EsPersonalizada")

SELECT 1,'LIMCPCARTERA'        ,'Limite Compra de Simultanea en Cuenta Propia'                                    , 0,''   ,'',0,0,0,''      ,'SIM',2248,'' ,0  ,'', 0,300000000,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 2,'LIMRVCPPLAZO'        ,'Limite de operaciones a plazo para Compra de Simultanea en Cuenta Propia'        , 0,''   ,'',0,0,0,'COMSIM',''   ,0,'D',120,'', 0,500000000,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 3,'LIMRVCPPLAZO'        ,'Limite de operaciones a plazo para Venta de Simultanea en Cuenta Propia'         , 0,''   ,'',0,0,0,'VENSIM',''   ,0,'D',90 ,'', 0,4000000  ,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 4,'LIMRVCTOPERACION'    ,'Limite por Operacion Canal ICliente para Compras Simultaneas'                    , 0,'INT','',0,0,0,'COMSIM',''   ,0,'' ,0  ,'', 0,10000000 ,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 5,'LIMRVCTOPERACION'    ,'Limite por Operacion Canal ICliente para Compras Simultaneas Institucionales'    , 0,'INT','',0,0,0,'COMSIM',''   ,0,'' ,0  ,'', 0,50000000 ,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 6,'LIMRVCPEXCLUYEEMISOR','Limite de exclusión de emisores para Compra de Simultanea en Cuenta Propia'      , 0,''   ,'',0,0,0,'COMSIM',''   ,0,'' ,0  ,'', 0,0        ,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 7,'LIMRVCPMARGEN'       ,'Limite en Cuenta Propia para márgenes de Simultaneas para Compra'                , 0,'INT','',0,0,0,'COMSIM',''   ,0,'' ,0  ,'', 0,0        ,0,0,'CLP','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 8,'LIMRVCPMARGEN'       ,'Limite en Cuenta Propia para márgenes de Simultaneas para Venta'                 , 0,'INT','',0,0,0,'VENSIM',''   ,0   ,'' ,0  ,'', 0,0        ,0,0,'CLP','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 9,'LIMCLLINEARIESGO'    ,'Limite Linea de Riesgo BASE para Cliente Venta Simultanea'              	      , 0,''   ,'ARRIESGADO',0,0,0,'VENSIM','SIM',2249,'' ,0  ,'',0,0        ,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 10,'LIMAGLINEARIESGO'   ,'Limite Linea de Riesgo BASE para Agente VENTA Simultanea'                        , 0,'AGE','',0,0,0,'VENSIM','SIM',2249,'' ,0  ,'',0,0        ,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 11,'LIMCPCARTERA'      ,'Limite de Acciones Nacionales en Cuenta Propia'                                   , 0,''   ,'',0,0,0,''      ,'ACN',0   ,'' ,0  ,'', 0,400000000,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 12,'LIMCPCARTERA'      ,'Limite de Letras Hipotecarias en Cuenta Propia'                                   , 0,''   ,'',0,0,0,''      ,'LTH',0   ,'' ,0  ,'', 0,200000000,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 13,'LIMCPCARTERA'      ,'Limite de Futuros IPSA en Cuenta Propia'                                          , 0,''   ,'',0,0,0,''      ,'FIP',0   ,'' ,0  ,'', 0,100000000,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 14,'LIMCPCARTERA'      ,'Limite Ventas de Simultanea en Cuenta Propia'                                     , 0,''   ,'',0,0,0,''      ,'SIM',2249,'' ,0  ,'', 0,200000000,0,0,'','','','ACT','N' FROM DUMMY
UNION ALL
SELECT 15,'LIMRVCPEXCLUYECLASIFICACION','Limite de exclusión de listas para Compra de Simultanea en Cuenta Propia', 0,''   ,'',0,0,0,'COMSIM',''   ,0,'' ,0  ,'', 0,0        ,0,0,'','','','ACT','N' FROM DUMMY
;
*/

/*
insert into "BSCL.Plataforma.BD::LIM.RangosMontoPlazo"
("IdRangosMontoPlazo","IdLimite","MontoDesde","MontoHasta",
 "PlazoDesde","PlazoHasta","Tasa","PuntosTasaMenos","PuntosTasaMas",
"PuntosTasaMenosAgente","PuntosTasaMasAgente")

SELECT 1,8,0,5000000, 1, 2,0.035,0,0,0,0 FROM DUMMY UNION all
SELECT 2,8,0,5000000, 3, 7,0.030,0,0,0,0 FROM DUMMY UNION all
SELECT 3,8,0,5000000, 8,10,0.055,0,0,0,0 FROM DUMMY UNION all
SELECT 4,8,0,5000000,11,30,0.065,0,0,0,0 FROM DUMMY UNION all
SELECT 5,8,0,5000000,31,60,0.075,0,0,0,0 FROM DUMMY UNION all

SELECT 6,8,5000001,10000000, 1, 2,0.040,0,0,0,0 FROM DUMMY UNION all
SELECT 7,8,5000001,10000000, 3, 7,0.050,0,0,0,0 FROM DUMMY UNION all
SELECT 8,8,5000001,10000000, 8,10,0.060,0,0,0,0 FROM DUMMY UNION all
SELECT 9,8,5000001,10000000,11,30,0.070,0,0,0,0 FROM DUMMY UNION all
SELECT 10,8,5000001,10000000,31,60,0.080,0,0,0,0 FROM DUMMY UNION all

SELECT 11,8,10000001,25000000, 1, 2,0.045,0,0,0,0 FROM DUMMY UNION all
SELECT 12,8,10000001,25000000, 3, 7,0.055,0,0,0,0 FROM DUMMY UNION all
SELECT 13,8,10000001,25000000, 8,10,0.065,0,0,0,0 FROM DUMMY UNION all
SELECT 14,8,10000001,25000000,11,30,0.075,0,0,0,0 FROM DUMMY UNION all
SELECT 15,8,10000001,25000000,31,60,0.085,0,0,0,0 FROM DUMMY UNION all

SELECT 16,8,25000001,100000000, 1, 2,0.050,0,0,0,0 FROM DUMMY UNION all
SELECT 17,8,25000001,100000000, 3, 7,0.060,0,0,0,0 FROM DUMMY UNION all
SELECT 18,8,25000001,100000000, 8,10,0.070,0,0,0,0 FROM DUMMY UNION all
SELECT 19,8,25000001,100000000,11,30,0.080,0,0,0,0 FROM DUMMY UNION all
SELECT 20,8,25000001,100000000,31,60,0.090,0,0,0,0 FROM DUMMY UNION all

SELECT 21,8,100000001,500000000, 1, 2,0.055,0,0,0,0 FROM DUMMY UNION all
SELECT 22,8,100000001,500000000, 3, 7,0.065,0,0,0,0 FROM DUMMY UNION all
SELECT 23,8,100000001,500000000, 8,10,0.075,0,0,0,0 FROM DUMMY UNION all
SELECT 24,8,100000001,500000000,11,30,0.085,0,0,0,0 FROM DUMMY UNION all
SELECT 25,8,100000001,500000000,31,60,0.095,0,0,0,0 FROM DUMMY UNION all

SELECT 26,8,500000001,1000000000, 1, 2,0.060,0,0,0,0 FROM DUMMY UNION all
SELECT 27,8,500000001,1000000000, 3, 7,0.070,0,0,0,0 FROM DUMMY UNION all
SELECT 28,8,500000001,1000000000, 8,10,0.080,0,0,0,0 FROM DUMMY UNION all
SELECT 29,8,500000001,1000000000,11,30,0.090,0,0,0,0 FROM DUMMY UNION all
SELECT 30,8,500000001,1000000000,31,60,0.100,0,0,0,0 FROM DUMMY UNION all
SELECT 31,7,0,5000000, 1, 2,0.035,0,0,0,0 FROM DUMMY UNION all
SELECT 32,7,0,5000000, 3, 7,0.030,0,0,0,0 FROM DUMMY UNION all
SELECT 33,7,0,5000000, 8,10,0.055,0,0,0,0 FROM DUMMY UNION all
SELECT 34,7,0,5000000,11,30,0.065,0,0,0,0 FROM DUMMY UNION all
SELECT 35,7,0,5000000,31,60,0.075,0,0,0,0 FROM DUMMY UNION all

SELECT 36,7,5000001,10000000, 1, 2,0.040,0,0,0,0 FROM DUMMY UNION all
SELECT 37,7,5000001,10000000, 3, 7,0.050,0,0,0,0 FROM DUMMY UNION all
SELECT 38,7,5000001,10000000, 8,10,0.060,0,0,0,0 FROM DUMMY UNION all
SELECT 39,7,5000001,10000000,11,30,0.070,0,0,0,0 FROM DUMMY UNION all
SELECT 40,7,5000001,10000000,31,60,0.080,0,0,0,0 FROM DUMMY UNION all

SELECT 41,7,10000001,25000000, 1, 2,0.045,0,0,0,0 FROM DUMMY UNION all
SELECT 42,7,10000001,25000000, 3, 7,0.055,0,0,0,0 FROM DUMMY UNION all
SELECT 43,7,10000001,25000000, 8,10,0.065,0,0,0,0 FROM DUMMY UNION all
SELECT 44,7,10000001,25000000,11,30,0.075,0,0,0,0 FROM DUMMY UNION all
SELECT 45,7,10000001,25000000,31,60,0.085,0,0,0,0 FROM DUMMY UNION all

SELECT 46,7,25000001,100000000, 1, 2,0.050,0,0,0,0 FROM DUMMY UNION all
SELECT 47,7,25000001,100000000, 3, 7,0.060,0,0,0,0 FROM DUMMY UNION all
SELECT 48,7,25000001,100000000, 8,10,0.070,0,0,0,0 FROM DUMMY UNION all
SELECT 49,7,25000001,100000000,11,30,0.080,0,0,0,0 FROM DUMMY UNION all
SELECT 50,7,25000001,100000000,31,60,0.090,0,0,0,0 FROM DUMMY UNION all

SELECT 51,7,100000001,500000000, 1, 2,0.055,0,0,0,0 FROM DUMMY UNION all
SELECT 52,7,100000001,500000000, 3, 7,0.065,0,0,0,0 FROM DUMMY UNION all
SELECT 53,7,100000001,500000000, 8,10,0.075,0,0,0,0 FROM DUMMY UNION all
SELECT 54,7,100000001,500000000,11,30,0.085,0,0,0,0 FROM DUMMY UNION all
SELECT 55,7,100000001,500000000,31,60,0.095,0,0,0,0 FROM DUMMY UNION all

SELECT 56,7,500000001,1000000000, 1, 2,0.060,0,0,0,0 FROM DUMMY UNION all
SELECT 57,7,500000001,1000000000, 3, 7,0.070,0,0,0,0 FROM DUMMY UNION all
SELECT 58,7,500000001,1000000000, 8,10,0.080,0,0,0,0 FROM DUMMY UNION all
SELECT 59,7,500000001,1000000000,11,30,0.090,0,0,0,0 FROM DUMMY UNION all
SELECT 60,7,500000001,1000000000,31,60,0.100,0,0,0,0 FROM DUMMY ;
*/

INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion")
VALUES(1,'ACT','LIMITES','ACTIVO');
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion")
VALUES(2,'INA','LIMITES','INACTIVO');
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion")
VALUES(3,'RZD','LIMITES.COMPORTAMIENTO','RECHAZADO');
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion")
VALUES(4,'RAU','LIMITES.COMPORTAMIENTO','REQUIERE AUTORIZACIÓN');
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion")
VALUES(5,'WAR','LIMITES.COMPORTAMIENTO','ADVERTENCIA');
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion", "Prioridad")
VALUES(6,'ACP','EVALUACION','ACEPTADA',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion", "Prioridad")
VALUES(7,'ADV','EVALUACION','ADVERTENCIA',2);
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion", "Prioridad")
VALUES(8,'PEN','EVALUACION','PENDIENTE AUTORIZACION',3);
INSERT INTO "BSCL.Plataforma.BD::LIM.EstadoLim"("IdEstadoLim","IdEstado","NombreEntidad", "Descripcion", "Prioridad")
VALUES(9,'RZD','EVALUACION','RECHAZADA',4);

/*
insert into "BSCL.Plataforma.BD::LIM.LimiteEmisor"("IdLimiteEmisor","IdEmisor","IdLimite", "IdEstado") 
SELECT 1,'AGUAS',6,'ACT' FROM DUMMY
UNION ALL
SELECT 2,'MADECO',6,'ACT' FROM DUMMY
;
*/

/*
insert into "BSCL.Plataforma.BD::LIM.LimiteClasificacionRiesgo"("IdLimiteClasificacionRiesgo","IdClasificacionRiesgo","IdLimite", "IdEstado") 
SELECT 1,2,15,'ACT' FROM DUMMY
UNION ALL
SELECT 2,3,15,'ACT' FROM DUMMY
*/


--Excluir Limite
INSERT INTO "BSCL.Plataforma.BD::LIM.ExcluirLimite" ("IdModulo","IdTipoInstrumento","IdEstado","TipoExclusion","IdEmpresa")
VALUES('ORD','','INA','MODULO',1);
INSERT INTO "BSCL.Plataforma.BD::LIM.ExcluirLimite" ("IdModulo","IdTipoInstrumento","IdEstado","TipoExclusion","IdEmpresa")
VALUES('ORD','ACN','INA','MODULOTIPOINSTRUMENTO',1);


--Configuracion Limites
INSERT INTO "BSCL"."BSCL.Plataforma.BD::LIM.Configuracion" ("IdEmpresa", "Codigo", "Descripcion", "Valor")
VALUES(1,'FILTROPLANTILLA','Filtro Plantilla para ClasifTipoInstr y TipoInstr','IdClasificacionTipoInstrumento');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::LIM.Configuracion" ("IdEmpresa", "Codigo", "Descripcion", "Valor")
VALUES(1,'ENTIDADCLASIFICADORARIESGO','ENTIDAD CLASIFICADORA RIESGO','STDANDPOOR');


--GrupoEmisor por Defecto
INSERT INTO "BSCL"."BSCL.Plataforma.BD::LIM.GrupoEmisor" ("IdGrupoEmisor","IdEmpresa","Descripcion","IdEstado","FechaIngreso",
"FechaCreacion","FechaModificacion","IdEstadoAutorizacion","Borrado","IdUsuarioCreador")
VALUES ('DEFAULT',1,'DEFAULT','ACT',CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,'AUT','N',1);


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;



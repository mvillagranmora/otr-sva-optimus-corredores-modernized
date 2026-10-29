--- EJECUTAR EN SQL PARA CARGAR DESDE SEBRA
select 'INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" )' + 
' Select '
+''''+LTRIM(RTRIM(NemoMoneda)) +''''+','+
+''''+LTRIM(RTRIM(NemoMoneda)) +''''+','+
+convert(varchar,CodMoneda)+','+
+''''+LTRIM(RTRIM(NomMoneda)) +''''+','+
+''''+LTRIM(RTRIM(NemoMoneda)) +''''+','+
+convert(varchar,Decimales)+','+
+''''+LTRIM(RTRIM(MultDivide)) +''''+','
+convert(varchar,RangoSupParidad)+','+
+convert(varchar,RangoInfParidad)+','+
+convert(varchar,PorcentSupParidad)+','+
+convert(varchar,PorcentInfParidad)+','+
+convert(varchar,RangoSupTC)+','+
+convert(varchar,RangoInfTC)+','+
+convert(varchar,PorcentSupTC)+','+
+convert(varchar,PorcentInfTC)+','+
+convert(varchar,OrdenMon)+','+
+'(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = '+''''+pa.NemoPais+''''+'),'
+''''+ 'ACT' +'''' + 'from dummy ;'
from plmonedas mon, PlPais pa 
where --mon.NemoMoneda not in('USD','JPY','CLP','EUR','UF') and 
mon.CodPais = pa.CodPais

SET SCHEMA "BSCL";
-- SALIDA ANTERIOR EJECUTAR EN HANA

delete from "BSCL.Plataforma.BD::PLA.Moneda"; -- where "IdMoneda" not in('CLP','USD','EUR','PEN')

--- Tomado de IMTRUST PCG
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'N/A','N/A',0,'<MONEDA NO EXISTE>','N/A',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'UDB','UDB',2,'MONEDA PRUEBA','UDB',1,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'BRL','BRL',5,'REAL BRASILERO','BRL',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'BR'),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'CAD','CAD',6,'DOLAR CANADIENSE','CAD',4,'M',0.1605000000,0.1605000000,15.0000000000,15.0000000000,49.0950000000,49.0950000000,10.0000000000,10.0000000000,6,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'AT        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'USD','USD',13,'Dólar Control de Precio','USD',2,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,7.1800000000,7.1800000000,1.0000000000,1.0000000000,1,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'PR        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'PEN','PEN',24,'Nuevo Sol Peru','PEN',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'PE'),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'AUD','AUD',36,'DOLAR AUSTRALIA','AUD',2,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,8,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'AU        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'RMB','RMB',48,'RENMINBI CHINA','RMB',4,'M',0.1855000000,0.1800000000,0.0000000000,0.0000000000,20.0000000000,15.0000000000,0.0000000000,0.0000000000,11,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'DKK','DKK',51,'CORONA DANESA','DKK',4,'M',0.8309000000,0.8309000000,15.0000000000,15.0000000000,9.4830000000,9.4830000000,10.0000000000,10.0000000000,12,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'MDP','MDP',68,'MONEDA DE PRUEBA','MDP',0,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'AF'),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'JPY','JPY',72,'YEN JAPONES','JPY',4,'M',18.2681000000,18.2681000000,15.0000000000,15.0000000000,0.0000000000,0.0000000000,10.0000000000,10.0000000000,5,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'CHF','CHF',82,'FRANCO SUIZO','CHF',4,'M',0.1839000000,0.1839000000,15.0000000000,15.0000000000,42.8520000000,42.8520000000,10.0000000000,10.0000000000,18,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CH        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'NOK','NOK',96,'CORONA NORUEGA','NOK',4,'M',0.9065000000,0.9065000000,15.0000000000,15.0000000000,8.6920000000,8.6920000000,10.0000000000,10.0000000000,19,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'NZD','NZD',97,'Dólar Nueva Zelanda','NZD',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,20,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'GBP','GBP',102,'LIBRA ESTERLINA','GBP',4,'D',0.0165000000,0.0165000000,1.0000000000,1.0000000000,76.8852000000,76.8852000000,10.0000000000,10.0000000000,4,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'SEK','SEK',113,'CORONA SUECA','SEK',4,'M',1.0382000000,1.0382000000,15.0000000000,15.0000000000,7.5900000000,7.5900000000,10.0000000000,10.0000000000,21,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'HKD','HKD',127,'DOLAR HONG KONG','HKD',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,22,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'HK'),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'MXN','MXN',132,'PESO MEXICANO','MXN',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'IC','IC',133,'Indice Promedio Cámara','IC',2,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CL        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'EUR','EUR',142,'EURO','EUR',4,'D',0.0000000000,0.0000000000,0.0000000000,0.0000000000,7.8512000000,7.8512000000,1.0000000000,1.0000000000,2,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'AF'),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'COP','COP',212,'Peso Colombiano','COP',1,'M',0.0000000000,0.0000000000,2.0000000000,3.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'AF'),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'UTM','UTM',503,'UNIDAD TRIBUTARIA MENSUAL','UTM',0,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,24,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select '$','$',951,'Pesos','$',0,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'DI','DI',988,'DOLAR INTERBANCARIO','DI',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,27,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'DO','DO',994,'Dólar Observado','DO',2,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,28,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CL        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'DA','DA',995,'DOLAR ACUERDO','DA',2,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,29,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = ''),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'IVP','IVP',997,'INDICE VALOR PROMEDIO','IVP',2,'M',1.0000000000,1.0000000000,0.0000000000,0.0000000000,10.0000000000,9.0000000000,0.0000000000,0.0000000000,30,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CL        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'UF','UF',998,'UNIDAD DE FOMENTO','UF',4,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,3,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CL        '),'ACT'from dummy ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" ) Select 'CLP','CLP',999,'PESOS','CLP',0,'M',0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,0.0000000000,31,(select "IdPais" from "BSCL.Plataforma.BD::REG.Pais" where "CodigoPais" = 'CL        '),'ACT'from dummy ;

delete FROM  "BSCL.Plataforma.BD::PLA.Moneda" where "IdMoneda" = 'N/A';
delete FROM  "BSCL.Plataforma.BD::PLA.Moneda" where "IdMoneda" = 'UDB';

/* CORRECCION POR ERRORES EN PRIMERA CARGA
update "BSCL.Plataforma.BD::PLA.Moneda" set "Codigo"= "IdMoneda" where "IdMoneda" in('CLP','USD','EUR','PEN')
update "BSCL.Plataforma.BD::PLA.Moneda" set "CodigoNumerico"= 999 where "IdMoneda" = 'CLP';
update "BSCL.Plataforma.BD::PLA.Moneda" set "CodigoNumerico"= 13 where "IdMoneda" = 'USD';
update "BSCL.Plataforma.BD::PLA.Moneda" set "CodigoNumerico"= 142 where "IdMoneda" = 'EUR';
update "BSCL.Plataforma.BD::PLA.Moneda" set "CodigoNumerico"= 24 where "IdMoneda" = 'PEN';
*/

select * FROM  "BSCL.Plataforma.BD::PLA.Moneda";


-- COMPARAR CON CODIGOS DE MONEDA ISO 4217
/*
SET SCHEMA "BSCL";
--- Recibo tabla
DROP TABLE #MonedasISO4217;

CREATE LOCAL TEMPORARY TABLE #MonedasISO4217
						( "Codigo"          NVARCHAR(10)
						,"CodigoNumerico"	BIGINT
						,"Decimales"		BIGINT
						,"Nombre"			NVARCHAR(100)
						);
															
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'AED',784,2,'Dírham de los Emiratos Árabes Unidos');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'AFN',971,2,'Afgani');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ALL',8,2,'Lek');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'AMD',51,2,'Dram armenio');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ANG',532,2,'Florín antillano neerlandés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'AOA',973,2,'Kwanza');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ARS',32,2,'Peso argentino');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'AUD',36,2,'Dólar australiano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'AWG',533,2,'Florín arubeño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'AZN',944,2,'Manat azerbaiyano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BAM',977,2,'Marco convertible');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BBD',52,2,'Dólar de Barbados');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BDT',50,2,'Taka');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BGN',975,2,'Lev búlgaro');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BHD',48,3,'Dinar bareiní');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BIF',108,0,'Franco de Burundi');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BMD',60,2,'Dólar bermudeño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BND',96,2,'Dólar de Brunéi');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BOB',68,2,'Boliviano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BOV',984,2,'MVDOL');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BRL',986,2,'Real brasileño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BSD',44,2,'Dólar bahameño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BTN',64,2,'Ngultrum');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BWP',72,2,'Pula');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BYR',974,0,'Rublo bielorruso');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'BZD',84,2,'Dólar beliceño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CAD',124,2,'Dólar canadiense');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CDF',976,2,'Franco congoleño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CHE',947,2,'Euro WIR');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CHF',756,2,'Franco suizo');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CHW',948,2,'Franco WIR');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CLF',990,4,'Unidad de fomento');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CLP',152,0,'Peso chileno');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CNY',156,2,'Yuan chino');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'COP',170,2,'Peso colombiano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'COU',970,2,'Unidad de valor real');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CRC',188,2,'Colón costarricense');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CUC',931,2,'Peso convertible');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CUP',192,2,'Peso cubano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CVE',132,2,'Escudo caboverdiano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'CZK',203,2,'Corona checa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'DJF',262,0,'Franco yibutiano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'DKK',208,2,'Corona danesa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'DOP',214,2,'Peso dominicano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'DZD',12,2,'Dinar argelino');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'EGP',818,2,'Libra egipcia');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ERN',232,2,'Nakfa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ETB',230,2,'Birr etíope');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'EUR',978,2,'Euro');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'FJD',242,2,'Dólar fiyiano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'FKP',238,2,'Libra malvinense');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GBP',826,2,'Libra esterlina');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GEL',981,2,'Lari');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GHS',936,2,'Cedi ghanés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GIP',292,2,'Libra de Gibraltar');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GMD',270,2,'Dalasi');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GNF',324,0,'Franco guineano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GTQ',320,2,'Quetzal');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'GYD',328,2,'Dólar guyanés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'HKD',344,2,'Dólar de Hong Kong');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'HNL',340,2,'Lempira');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'HRK',191,2,'Kuna');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'HTG',332,2,'Gourde');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'HUF',348,2,'Forinto');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'IDR',360,2,'Rupia indonesia');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ILS',376,2,'Nuevo shéquel israelí');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'INR',356,2,'Rupia india');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'IQD',368,3,'Dinar iraquí');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'IRR',364,2,'Rial iraní');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ISK',352,0,'Corona islandesa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'JMD',388,2,'Dólar jamaiquino');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'JOD',400,3,'Dinar jordano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'JPY',392,0,'Yen');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KES',404,2,'Chelín keniano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KGS',417,2,'Som');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KHR',116,2,'Riel');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KMF',174,0,'Franco comorense');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KPW',408,2,'Won norcoreano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KRW',410,0,'Won');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KWD',414,3,'Dinar kuwaití');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KYD',136,2,'Dólar de las Islas Caimán');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'KZT',398,2,'Tenge');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'LAK',418,2,'Kip');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'LBP',422,2,'Libra libanesa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'LKR',144,2,'Rupia de Sri Lanka');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'LRD',430,2,'Dólar liberiano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'LSL',426,2,'Loti');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'LYD',434,3,'Dinar libio');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MAD',504,2,'Dírham marroquí');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MDL',498,2,'Leu moldavo');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MGA',969,2,'Ariary malgache');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MKD',807,2,'Denar');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MMK',104,2,'Kyat');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MNT',496,2,'Tugrik');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MOP',446,2,'Pataca');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MRO',478,2,'Uquiya');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MUR',480,2,'Rupia de Mauricio');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MVR',462,2,'Rufiyaa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MWK',454,2,'Kwacha');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MXN',484,2,'Peso mexicano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MXV',979,2,'Unidad de Inversión (UDI) mexicana');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MYR',458,2,'Ringgit malayo');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'MZN',943,2,'Metical mozambiqueño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'NAD',516,2,'Dólar namibio');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'NGN',566,2,'Naira');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'NIO',558,2,'Córdoba');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'NOK',578,2,'Corona noruega');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'NPR',524,2,'Rupia nepalí');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'NZD',554,2,'Dólar neozelandés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'OMR',512,3,'Rial omaní');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'PAB',590,2,'Balboa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'PEN',604,2,'Sol');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'PGK',598,2,'Kina');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'PHP',608,2,'Peso filipino');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'PKR',586,2,'Rupia pakistaní');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'PLN',985,2,'Złoty');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'PYG',600,0,'Guaraní');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'QAR',634,2,'Riyal qatarí');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'RON',946,2,'Leu rumano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'RSD',941,2,'Dinar serbio');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'RUB',643,2,'Rublo ruso');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'RWF',646,0,'Franco ruandés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SAR',682,2,'Riyal saudí');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SBD',90,2,'Dólar de las Islas Salomón');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SCR',690,2,'Rupia seychelense');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SDG',938,2,'Dinar sudanés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SEK',752,2,'Corona sueca');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SGD',702,2,'Dólar de Singapur');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SHP',654,2,'Libra de Santa Elena');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SLL',694,2,'Leone');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SOS',706,2,'Chelín somalí');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SRD',968,2,'Dólar surinamés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SSP',728,2,'Libra sursudanesa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'STD',678,2,'Dobra');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SVC',222,2,'Colon Salvadoreño');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SYP',760,2,'Libra siria');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'SZL',748,2,'Lilangeni');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'THB',764,2,'Baht');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TJS',972,2,'Somoni tayiko');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TMT',934,2,'Manat turcomano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TND',788,3,'Dinar tunecino');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TOP',776,2,'Paʻanga');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TRY',949,2,'Lira turca');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TTD',780,2,'Dólar de Trinidad y Tobago');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TWD',901,2,'Nuevo dólar taiwanés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'TZS',834,2,'Chelín tanzano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'UAH',980,2,'Grivna');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'UGX',800,0,'Chelín ugandés');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'USD',840,2,'Dólar estadounidense');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'USN',997,2,'Dólar estadounidense (Siguiente día)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'UYI',940,0,'Peso en Unidades Indexadas (Uruguay)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'UYU',858,2,'Peso uruguayo');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'UZS',860,2,'Som uzbeko');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'VEF',937,2,'Bolívar');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'VND',704,0,'Dong vietnamita');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'VUV',548,0,'Vatu');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'WST',882,2,'Tala');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XAF',950,0,'Franco CFA de África Central');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XAG',961,0,'Plata (una onza troy)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XAU',959,0,'Oro (una onza troy)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XBA',955,0,'Unidad compuesta europea (EURCO) (Unidad del mercados de bonos)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XBB',956,0,'Unidad Monetaria europea (E.M.U.-6) (Unidad del mercado de bonos)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XBC',957,0,'Unidad europea de cuenta 9 (E.U.A.-9) (Unidad del mercado de bonos)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XBD',958,0,'Unidad europea de cuenta 17 (E.U.A.-17) (Unidad del mercado de bonos)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XCD',951,2,'Dólar del Caribe Oriental');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XDR',960,0,'Derechos especiales de giro');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XOF',952,0,'Franco CFA de África Occidental');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XPD',964,0,'Paladio (una onza troy)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XPF',953,0,'Franco CFP');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XPT',962,0,'Platino (una onza troy)');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XSU',994,0,'SUCRE');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XTS',963,0,'Reservado para pruebas');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XUA',965,0,'Unidad de cuenta BAD');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'XXX',999,0,'Sin divisa');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'YER',886,2,'Rial yemení');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ZAR',710,2,'Rand');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ZMW',967,2,'Kwacha zambiano');
insert into #MonedasISO4217("Codigo","CodigoNumerico","Decimales","Nombre") values( 'ZWL',932,2,'Dólar zimbabuense');


---- Monedas no encontradas en la norma
SELECT MON."Codigo", ISO."Codigo", * FROM "BSCL.Plataforma.BD::PLA.Moneda" MON 
	LEFT JOIN #MonedasISO4217 ISO ON ( LTRIM(RTRIM(MON."Codigo")) = LTRIM(RTRIM(ISO."Codigo"))	                                       )
WHERE IFNULL(ISO."Codigo",'') = ''

SELECT * FROM #MonedasISO4217 WHERE "Nombre" like '%lar%';
SELECT * FROM #MonedasISO4217 WHERE "Codigo"='USD'
*/


/* Paises
-- Feriados Nacionales
select * from ruebas..TBUTFECH where tipo_fec = 'F'
-- Feriados InterNacionales
select * from RUE_TBBEFECHAS

*/

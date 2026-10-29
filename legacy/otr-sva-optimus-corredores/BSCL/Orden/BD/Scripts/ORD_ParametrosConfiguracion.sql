set schema "BSCL";

select * from "BSCL"."BSCL.Plataforma.BD::PLA.Empresa"

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'PorcentajeDivergenciaPrecio','Porcentaje que puede diverger el precio de la ordden','20' from dummy;


insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'BovedaSimultanea','Boveda de instrumentos simultaneables','DCV' from dummy;

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'ListasAyBSimultanea','Permite ingresar ordenes de simultaneas con instrumentos de lista A y B solamente','S' from dummy;

INSERT INTO "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes" 
VALUES(1,'TemplateOrden','Id del template utilizado para generar los comprobantes de ordenes', '57E1211B8A820F76E10000007F000002');

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'UsoFlujoCajaIngresoOrdenes','Uso configurado de flujo de caja para ingreso de ordenes','ORDENES_DE_COMPRA' from dummy;

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'IdEmpresaClientesFix','Empresa que utilizaran los mensajes que lleguen por dma y market data',1 from dummy;

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'MontoMaximoOrden','Monto maximo de orden permitido por la corredora en moneda local',0 from dummy;

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'MontoMinimoOrden','Monto minimo de orden permitido por la corredora en moneda local',0 from dummy;

insert into "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
SELECT 1,'IdUsuarioAsignacionOperadorDirecto','IdUsuario digitador de las ordenes de operador directo',4 from dummy;
commit

--SELECT "Valor" FROM "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes" where "CodigoParametro" = 'IdEmpresaClientesFix'
COMMIT

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.ConfiguracionOrdenes"

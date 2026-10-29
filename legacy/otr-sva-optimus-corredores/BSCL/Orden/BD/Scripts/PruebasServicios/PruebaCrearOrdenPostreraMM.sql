SET SCHEMA "BSCL";
drop TABLE #TT;
 
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdInterno"    BIGINT,
 "FechaRecepcion"   NVARCHAR(19),
 "Glosa"     NVARCHAR(520),
 "IdBoveda"   NVARCHAR(3),
 "IdFormaCustodia"   NVARCHAR(3),
 "EsFacturaInmediata"  NVARCHAR(1),
 "IdTransaccion"   BIGINT,
 "FolioTransaccion"   NVARCHAR(40),
 "IdPortafolio"    BIGINT,
 "Cantidad"     DECIMAL(26,8),
 "ValorRescate"    DECIMAL(26,8),
 "FolioCartera"    BIGINT,
 "TasaVentaCorta"  DECIMAL(26,8),
 "PlazoVentaCorta"  BIGINT
);

drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2		(
										"IdInterno"				BIGINT,
										"IdCobro"				NVARCHAR(6),
										"ValorCobroAplicado"	DECIMAL(26,8)
										);
										
										
INSERT INTO #TT ("IdInterno","FechaRecepcion","Glosa","IdBoveda","IdFormaCustodia","EsFacturaInmediata","IdTransaccion","FolioTransaccion","IdPortafolio","Cantidad","ValorRescate","FolioCartera","TasaVentaCorta","PlazoVentaCorta")
SELECT 1240 as "IdInterno",'2017-02-06 19:27:21' as "FechaRecepcion",'' as "Glosa",'0' as "IdBoveda",'CUS' as "IdFormaCustodia",'N' as "EsFacturaInmediata",246 as "IdTransaccion",'20003' as "FolioTransaccion",100039 as "IdPortafolio",550 as "Cantidad",0 as "ValorRescate",0 as "FolioCartera",0 as "TasaVentaCorta",1 as "PlazoVentaCorta" from dummy;




--SET 'DEPURACION' = 'S'; 
CALL "BSCL"."BSCL.Orden.BD.Servicios::CrearOrdenPostreraDummy"(1,41,'ORD','',#tt,#tt2);

rollbacK

























SELECT * FROM "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes"
CALL "BSCL"."BSCL.Orden.BD.Servicios::CrearOrdenes"(1,41,'ORD','N','N',?,?,?);
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Resultado";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Cobros";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Garantias";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_InformacionDetalle";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_AdvertenciasDetalle";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_ErroresDetalle";


delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Garantias";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_CobrosModificados";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Ordenes";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Cobros";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Resultado";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Cobros";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Garantias";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_InformacionDetalle";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_AdvertenciasDetalle";
delete from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_ErroresDetalle";



asdfasdfasd
SET SCHEMA "BSCL";

--INSERT INTO "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ("IdInterno","FechaRecepcion","FechaVigencia","FechaEmision","FechaOperacion","IdTipoVigencia","Glosa","Cantidad","MontoNeto","ValorRescate","IdTipoCustodia","IdFormaCustodia","IdPorcentajeVisibilidad","PrecioLimite","PrecioMercado","EsFacturaInmediata","IdTipoPrecio","IdPortafolio","IdTipoCanalIngreso","IdTipoReajuste","IdEmisor","IdBolsa","EspecificaBolsa","IdMonedaLiquidacionOrden","IdMonedaLiquidacionCobros","TipoCambio","IdInstrumento","IdInstrumentoSubyacente","IdTipoOperacion","IdCondicionDeLiquidacionContado","IdCondicionDeLiquidacionPlazo","TasaVentaCorta","PlazoVentaCorta","EsVentaCorta","TasaSimultanea","PlazoSimultanea","PlazoIF","TasaRFIF","EsOD","EsFungible","MontoPlazo","IdTipoLibroOrden","IdCorredorCorresponsal","IdPersonaAutorizadoDarOrdenes","IdTipoCanalDistribucion")
--SELECT 0 as "IdInterno",'0001-01-01 00:00:00' as "FechaRecepcion",'2016-12-05' as "FechaVigencia",'0001-01-01' as "FechaEmision",'0001-01-01' as "FechaOperacion",'SH' as "IdTipoVigencia",'' as "Glosa",1000 as "Cantidad",0 as "MontoNeto",0 as "ValorRescate",'DCV' as "IdTipoCustodia",'CUS' as "IdFormaCustodia",1 as "IdPorcentajeVisibilidad",6281.2 as "PrecioLimite",0 as "PrecioMercado",'N' as "EsFacturaInmediata",'M' as "IdTipoPrecio",343710 as "IdPortafolio",'AGE' as "IdTipoCanalIngreso",'CLP' as "IdTipoReajuste",'COPEC' as "IdEmisor",'BCS' as "IdBolsa",'S' as "EspecificaBolsa",'CLP' as "IdMonedaLiquidacionOrden",'CLP' as "IdMonedaLiquidacionCobros",null as "TipoCambio",502 as "IdInstrumento",0 as "IdInstrumentoSubyacente",'VENACN' as "IdTipoOperacion",'T2' as "IdCondicionDeLiquidacionContado",'' as "IdCondicionDeLiquidacionPlazo",0 as "TasaVentaCorta",0 as "PlazoVentaCorta",'N' as "EsVentaCorta",0 as "TasaSimultanea",0 as "PlazoSimultanea",0 as "PlazoIF",0 as "TasaRFIF",'N' as "EsOD",'N' as "EsFungible",0 as "MontoPlazo",'NORMAL' as "IdTipoLibroOrden",null as "IdCorredorCorresponsal",null as "IdPersonaAutorizadoDarOrdenes",'MAN' as "IdTipoCanalDistribucion" from dummy;

INSERT INTO "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ("IdInterno","FechaRecepcion","FechaVigencia","FechaEmision","FechaOperacion","IdTipoVigencia","Glosa","Cantidad","MontoNeto","ValorRescate","IdBoveda","IdFormaCustodia","IdPorcentajeVisibilidad","PrecioLimite","PrecioMercado","EsFacturaInmediata","IdTipoPrecio","IdPortafolio","IdTipoLibroOrden","IdTipoCanalIngreso","IdTipoReajuste","IdEmisor","IdBolsa","EspecificaBolsa","IdMonedaLiquidacionOrden","IdMonedaLiquidacionCobros","TipoCambio","IdInstrumento","IdInstrumentoSubyacente","IdCondicionDeLiquidacionPlazo","TasaVentaCorta","TasaSimultanea","TasaRFIF","PlazoIF","PlazoSimultanea","PlazoVentaCorta","EsVentaCorta","EsOD","EsFungible","IdCorredorCorresponsal","MontoPlazo","IdPersonaAutorizadoDarOrdenes","IdTipoCanalDistribucion")
SELECT 
0 AS "IdInterno",'0001-01-01 00:00:00' AS "FechaRecepcion",'2017-04-05' AS "FechaVigencia",
'0001-01-01' AS "FechaEmision",'0001-01-01' AS "FechaOperacion",
'SH' AS "IdTipoVigencia",'' AS "Glosa",
null AS "Cantidad",10000 AS "MontoNeto",0 AS "ValorRescate",null AS "IdBoveda",
'CUS' AS "IdFormaCustodia",null AS "IdPorcentajeVisibilidad",
null AS "PrecioLimite",null AS "PrecioMercado",null AS "EsFacturaInmediata",
null AS "IdTipoPrecio",343710 aS "IdPortafolio",
null AS "IdTipoLibroOrden",
null AS "IdTipoCanalIngreso",
null AS "IdTipoReajuste",null AS "IdEmisor",'BCS' AS "IdBolsa",'S' AS "EspecificaBolsa",
'USD' AS "IdMonedaLiquidacionOrden",
'USD' AS "IdMonedaLiquidacionCobros",
null AS "TipoCambio",2250 AS "IdInstrumento",null AS "IdInstrumentoSubyacente",null AS "IdCondicionDeLiquidacionPlazo",null AS "TasaVentaCorta",
null AS "TasaSimultanea",null AS "TasaRFIF",
null AS "PlazoIF",null AS "PlazoSimultanea",null AS "PlazoVentaCorta",null AS "EsVentaCorta",null AS "EsOD",null AS "EsFungible",
null AS "IdCorredorCorresponsal",null AS "MontoPlazo",null AS "IdPersonaAutorizadoDarOrdenes",null AS "IdTipoCanalDistribucion"
from dummy;


SELECT * FROM "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes"

CALL "BSCL"."BSCL.Orden.BD.Servicios::CrearOrdenes"(1,4,'ORD','S','S','COMRRV','T0',?,?,?);

rollback
		SELECT 'BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada',* FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada";

select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Resultado";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Cobros";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Garantias";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_InformacionDetalle";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_AdvertenciasDetalle";
select * from "BSCL.Orden.BD::ORD.CrearOrdenes_Salida_ErroresDetalle";
select * from  "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso_Errores"
rollback;
commit;
select * fr




SELECT * FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"



SELECT *
			FROM "BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes" ord
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst on ord."IdInstrumento" = inst."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipoinst on tipoinst."IdTipoInstrumento" = inst."IdTipoInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" cla on cla."IdClasificacionTipoInstrumento" = tipoinst."IdClasificacionTipoInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" tipope on tipope."IdTipoOperacion" = 'COMRRV' and tipope."IdTipoInstrumento" = inst."IdTipoInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsa" bol on bol."IdBolsa" = ord."IdBolsa" and bol."IdTipoInstrumento" = inst."IdTipoInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" condliqcon on condliqcon."IdCondicionDeLiquidacion" = 'T0' and condliqcon."IdTipoOperacion" = 'COMRRV'
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCondicionDeLiquidacion" condliqpla on condliqpla."IdCondicionDeLiquidacion" = ord."IdCondicionDeLiquidacionPlazo" and condliqpla."IdTipoOperacion" = 'COMRRV'
															
			LEFT JOIN "BSCL.Orden.BD::ORD.TipoVigenciaOrden" ordvig on ordvig."IdTipoVigenciaOrden" = ord."IdTipoVigencia"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Boveda" boveda on boveda."IdBoveda" = ord."IdBoveda" AND boveda."IdEstado" = 'VIG'
			LEFT JOIN "BSCL.Orden.BD::ORD.TipoFormaCustodiaOrden" ordformcus on ordformcus."IdTipoFormaCustodiaOrden" = ord."IdFormaCustodia"
			LEFT JOIN "BSCL.Orden.BD::ORD.PorcentajeVisibilidad" ordvis on ordvis."IdPorcentajeVisibilidad" = ord."IdPorcentajeVisibilidad"
			LEFT JOIN "BSCL.Orden.BD::ORD.TipoPrecioOrden" ordtipopre on ordtipopre."IdTipoPrecioOrden" = ord."IdTipoPrecio"
			LEFT JOIN "BSCL.Orden.BD::ORD.ConfiguracionHorarioDistribucion" ordhorario on 
				ordhorario."IdTipoCanalDistribucion" = ord."IdTipoCanalDistribucion" and
				ordhorario."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso" and
				ordhorario."IdTipoInstrumento" = inst."IdTipoInstrumento" and
				ordhorario."IdBolsa" = ord."IdBolsa" and
				ordhorario."IdCondicionDeLiquidacion" = 'T0'
			
			
			LEFT JOIN "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion" ordconfcanaldist on 
				1 = ordconfcanaldist."IdEmpresa" and 
				ordconfcanaldist."IdBolsa" = ord."IdBolsa" and 
				ordconfcanaldist."IdTipoInstrumento" = inst."IdTipoInstrumento" and 
				ordconfcanaldist."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso" and 
				ordconfcanaldist."IdTipoCanalDistribucion" = ord."IdTipoCanalDistribucion" 
							
			LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ordcanal on ordcanal."IdTipoCanalIngreso" = ord."IdTipoCanalIngreso"
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" as itr ON inst."IdInstrumento"=itr."IdInstrumento" and inst."IdTipoInstrumento"=itr."IdTipoInstrumento" and itr."IdTipoReajuste" = ord."IdTipoReajuste" and itr."IdEstado" = 'ACT'  
			
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" subprt on ord."IdInstrumento" = subprt."IdInstrumento" and subprt."IdTipoInstrumento" = 'PRT'
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" subsim on ord."IdInstrumentoSubyacente" = subsim."IdInstrumento" and subsim."IdTipoInstrumento" = 'SIM'
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" instsim on ord."IdInstrumentoSubyacente" = instsim."IdInstrumento"
			LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuario on usuario."IdUsuario" = 4 AND "TipoEntidad" = 'EMP'
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" CRE ON CRE."IdModulo" = 'ORD' AND CRE."IdEmpresa" = 1;

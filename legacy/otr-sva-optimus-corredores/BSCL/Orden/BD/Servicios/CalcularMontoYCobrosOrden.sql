SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Orden.BD.Servicios::CalcularMontoYCobrosOrden";
GO

CREATE PROCEDURE
--  Proposito: Calcular monto, cantidad, cobros, de orden
--  Autor: - TEAM ORDENES - AWW YEAH
--  Fecha Creacion: 

 "BSCL.Orden.BD.Servicios::CalcularMontoYCobrosOrden" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa				BIGINT,			--ID Empresa destino
	IN  p_IdUsuario				BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo				NVARCHAR(3),  	--ID del Modulo que lo invoca		
	
    -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_ErroresContador 		int
 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN
	
	DECLARE	v_FechaSistema				NVARCHAR(10);
	DECLARE v_IdTrabajoParidad			BIGINT;
	DECLARE	v_FechaSistemaDate			DATE;
	DECLARE v_IdMonedaSistema			NVARCHAR(3);
	DECLARE v_IdMonedaSimultanea		NVARCHAR(3);
	DECLARE v_out_ErroresContador_matriz		INT;
	DECLARE v_out_AdvertenciasContador_matriz	INT;
	DECLARE v_out_error					NVARCHAR(500);
	DECLARE v_count_paridades			INT;
	DECLARE v_out_error1				NVARCHAR(500);
	DECLARE v_out_error2				NVARCHAR(500);
	DECLARE v_out_AdvertenciasDetalle  	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(40),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40)) DEFAULT
										SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(40),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40)) DEFAULT
										SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
	DECLARE	v_in_CalcularParidadYTipoCambio	"BSCL.Plataforma.BD.Tipos::MER:EntradaCalcularParidadYTipoCambio";
	DECLARE v_out_CalcularParidadYTipoCambio "BSCL.Plataforma.BD.Tipos::MER:SalidaCalcularParidadYTipoCambio";
	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		INSERT INTO "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_ErroresDetalle"
		("IdInterno","Codigo","Descripcion")
		SELECT errores."IdInterno",errores."Codigo",msj."Descripcion"
		FROM :v_out_ErroresDetalle errores
		INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
		
		INSERT INTO "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_AdvertenciasDetalle"
		("IdInterno","RequiereAutorizacion","Codigo","Descripcion")					
		SELECT adv."IdInterno",adv."Codigo",adv."RequiereAutorizacion",msj."Descripcion"
		FROM :v_out_AdvertenciasDetalle adv
		INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";

		select count(1) into p_out_ErroresContador 
		from "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_ErroresDetalle";
		select count(1) into p_out_AdvertenciasContador 
		from "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_AdvertenciasDetalle";
				 					 	
	END;
	
	--call "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CalcularMontoYCobrosOperacion','INICIO','');
		
	p_out_AdvertenciasContador 	:= 0;
	p_out_ErroresContador 		:= 0;
	
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	v_FechaSistemaDate = TO_DATE(:v_FechaSistema);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:p_IdEmpresa, 'PLA.MonedaSistema',v_IdMonedaSistema);
	
	SELECT "Valor" INTO v_IdMonedaSimultanea
	FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
	WHERE "IdEmpresa" = :p_IdEmpresa
	AND "CodigoParametro" = 'MonedaSimultanea';
	
	
	--call "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CalcularMontoYCobrosOperacion','PASO 1','TERMINO CONFIGURACION');
	
	INSERT INTO "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes"("IdInterno","IdPortafolio","IdTipoProducto","IdTipoOperacion","IdTipoCanalIngreso","IdBolsa","IdCorredorCorresponsal","IdInstrumento","DiasPlazo","CantidadOrden","MontoOrden","AccionOrden",
	"ValorRescateOrden","EsFungible","TasaSimultanea","TasaRFIF","TasaVentaCorta","IdMonedaLiquidacionOrden","PrecioOrden","IdMonedaInstrumento","TipoCambio","MontoTotal","EsPorMonto")
	SELECT "IdInterno","IdPortafolio",ord."IdTipoProducto",ord."IdTipoOperacion","IdTipoCanalIngreso","IdBolsa","IdCorredorCorresponsal",ord."IdInstrumento","DiasPlazo","CantidadOrden","MontoOrden",tipoope."Accion" as "AccionOrden",
	"ValorRescateOrden","EsFungible","TasaSimultanea","TasaRFIF","TasaVentaCorta","IdMonedaLiquidacionOrden","PrecioOrden",ins."IdMoneda" as "IdMonedaInstrumento",COALESCE("TipoCambio",0) AS "TipoCambio","MontoTotal","EsPorMonto"
	FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes" ord
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON ins."IdInstrumento" = ord."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope on ord."IdTipoOperacion" = tipoope."IdTipoOperacion"
	where ord."EsPorMonto" = 'S';
	
	--PASAMOS LOS PRECIOS A LA MONEDA DE LIQUIDACION
	SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
	
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
	SELECT :v_IdTrabajoParidad,ord."IdInterno",:v_FechaSistemaDate as "Fecha", ord."IdMonedaInstrumento" as "IdMonedaOrigen", ord."IdMonedaLiquidacionOrden" as "IdMonedaDestino",
	COALESCE(ord."TipoCambio",0) as "ValorCambioLocalModificado",ord."PrecioOrden" as "MontoOrigen",'ORDENES' as "IdUso"
	FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes" ord;
	
	call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => :v_IdTrabajoParidad
	);

	
	UPDATE ORD
	SET "PrecioOrdenMonedaLiquidacion" = outt."MontoDestino"
	FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes" ORD
	LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt on ORD."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
	
	v_Portafolios = select ord."IdInterno",ord."IdPortafolio", ord."IdTipoProducto", ord."IdTipoOperacion", ord."IdTipoCanalIngreso" as "IdCanalIngreso",ord."IdBolsa",ord."IdCorredorCorresponsal"
					FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes" ord;
	
	--SELECT 'v_Portafolios',* FROM :v_Portafolios;
	
	CALL "BSCL.Orden.BD.Servicios::ConsultarCobrosPortafolios"(
  		p_IdEmpresa 			=> :p_IdEmpresa,
  		p_IdUsuario 			=> :p_IdUsuario,
 		p_IdModulo 				=> :p_IdModulo,
	  	p_Portafolios 			=> :v_Portafolios,
		p_out_MatrizOperacional => v_MatrizOperacionalTemp,
		
		p_out_AdvertenciasContador 	=> v_out_AdvertenciasContador_matriz,
	    p_out_AdvertenciasDetalle 	=> :v_out_AdvertenciasDetalle_matriz,
	    p_out_ErroresContador 		=> v_out_ErroresContador_matriz,
	    p_out_ErroresDetalle 		=> :v_out_ErroresDetalle_matriz
	);
	
	--call "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CalcularMontoYCobrosOperacion','PASO 2','TERMINO OBTENCION COBROS');
	v_MatrizOperacional = SELECT row_number() over (order by "IdInterno","IdCobro") as "IdInternoCobro",* FROM :v_MatrizOperacionalTemp;
	
	--SELECT 'v_MatrizOperacional',* FROM :v_MatrizOperacional;
	
	-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	---ORDEN POR MONTO------------------------------------------------------------------------------------------------------------------------------------------------------
	-------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	INSERT INTO "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos"("IdInterno","IdInternoCobro","IdCobro","IdMonedaCobro","IdMonedaLiquidacion","ValorCobro","FormaCobro","TipoCambio","ValorTopeMinimoOrden")
	SELECT ord."IdInterno",row_number() over (order by ord."IdInterno",mat."IdCobro") as "IdInternoCobro",mat."IdCobro",mat."IdMoneda" as "IdMonedaCobro",ord."IdMonedaLiquidacionOrden" as "IdMonedaLiquidacion",mat."ValorCobro",mat."FormaCobro",COALESCE(ord."TipoCambio",0) AS "TipoCambio","ValorTopeMinimoOrden"
	FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes" ord 
	inner join :v_MatrizOperacional mat on ord."IdInterno" = mat."IdInterno" and ord."DiasPlazo" = mat."DiasPlazo"
	where ord."EsPorMonto" = 'S';
	
	SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
	
	--SELECT '"BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"',* FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada";
	
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
	SELECT :v_IdTrabajoParidad,cob."IdInternoCobro" as "IdInterno",:v_FechaSistemaDate as "Fecha", cob."IdMonedaCobro" as "IdMonedaOrigen", cob."IdMonedaLiquidacion" as "IdMonedaDestino",
	COALESCE(cob."TipoCambio",0) as "ValorCambioLocalModificado",
	CASE cob."FormaCobro" WHEN 'F' THEN cob."ValorCobro" WHEN 'V' THEN cob."ValorTopeMinimoOrden" END as "MontoOrigen",
	'ORDENES' as "IdUso"
	FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos" cob;
	
	call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => :v_IdTrabajoParidad
	);
	
	--SELECT '0"BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos"',* FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos";
	--select '"BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida"',* from "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida";
		
	UPDATE COB
	SET 
		"ValorCobroMonedaLiquidacion" = CASE "FormaCobro" WHEN 'F' THEN outt."MontoDestino" ELSE "ValorCobro" END,
		"ValorTopeMinimoOrdenMonedaLiquidacion" = CASE "FormaCobro" WHEN 'V' THEN outt."MontoDestino" ELSE "ValorTopeMinimoOrdenMonedaLiquidacion" END
	FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos" COB
	LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt on COB."IdInternoCobro" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
	
	--SELECT '1"BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos"',* FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos";
	
	UPDATE COB
	SET 
		"FormaCobro" = CASE WHEN "FormaCobro" = 'V' and ORD."MontoTotal" * (COB."ValorCobro" /100) < COB."ValorTopeMinimoOrdenMonedaLiquidacion" THEN 'F' ELSE "FormaCobro" END,
		"ValorCobroMonedaLiquidacion" = CASE 
											WHEN "FormaCobro" = 'V' and ORD."MontoTotal" * (COB."ValorCobro" /100) < COB."ValorTopeMinimoOrdenMonedaLiquidacion" THEN COB."ValorTopeMinimoOrdenMonedaLiquidacion" 
											WHEN "FormaCobro" = 'V' and ORD."MontoTotal" * (COB."ValorCobro" /100) > COB."ValorTopeMinimoOrdenMonedaLiquidacion" THEN ORD."MontoTotal" * (COB."ValorCobro" /100) 
										ELSE COB."ValorCobroMonedaLiquidacion" END
	FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos" COB
	INNER JOIN "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes" ORD on COB."IdInterno" = ORD."IdInterno";

	--select '"BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes"',* from "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes";
	--select '"BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos"',* from "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos";
	
  	v_cantidadesCOMPRA = 
  	select ord."IdInterno",ord."EsPorMonto",ord."MontoTotal",
  	
  	
  					( SUM(CASE cob."FormaCobro" WHEN 'F' THEN cob."ValorCobroMonedaLiquidacion" ELSE 0 END)) as "Factor1",( 1 + COALESCE(mat."ValorCobro",0)/100.0) as "Factor2",
  					SUM(CASE cob."FormaCobro" WHEN 'V' THEN cob."ValorCobro" /100.0 ELSE 0 END) as "Factor3",( 1 + COALESCE(mat."ValorCobro",0)/100.0) as "Factor4",
  					
  					
  					(
  					   (ord."MontoTotal" - ( SUM(CASE cob."FormaCobro" WHEN 'F' THEN cob."ValorCobroMonedaLiquidacion" ELSE 0 END)) * ( 1 + COALESCE(mat."ValorCobro",0)/100.0)
  					)
  					/
  					(ord."PrecioOrdenMonedaLiquidacion" * ( 1 + SUM(CASE cob."FormaCobro" WHEN 'V' THEN cob."ValorCobro" /100.0 ELSE 0 END)  *  ( 1 + COALESCE(mat."ValorCobro",0)/100.0)     ) ))
  					 AS "CantidadSinRedondear",
  					 
  					 
  					 
  					 
  					 
  					FLOOR(
  					   (ord."MontoTotal" - ( SUM(CASE cob."FormaCobro" WHEN 'F' THEN cob."ValorCobroMonedaLiquidacion" ELSE 0 END)) * ( 1 + COALESCE(mat."ValorCobro",0)/100.0)
  					)
  					/
  					(ord."PrecioOrdenMonedaLiquidacion" * ( 1 + SUM(CASE cob."FormaCobro" WHEN 'V' THEN cob."ValorCobro" /100.0 ELSE 0 END)  *  ( 1 + COALESCE(mat."ValorCobro",0)/100.0)     ) ))
  					 as "CantidadOrden"
  					
  				from "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes" ord
  				INNER JOIN "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos" cob on ord."IdInterno" = cob."IdInterno"
 				LEFT JOIN :v_MatrizOperacional mat on
 					mat."IdCobro" = 'IVA' and
			   		mat."IdInterno" = ord."IdInterno"
			   	where ord."EsPorMonto" = 'S' and ord."AccionOrden" = 'COMPRA'
  				group by ord."IdInterno",ord."MontoTotal",ord."PrecioOrdenMonedaLiquidacion",mat."ValorCobro",ord."EsPorMonto";
  	--SELECT 'v_cantidadesCOMPRA',* FROM :v_cantidadesCOMPRA;
  			
  	v_cantidadesVENTA = 
  	select ord."IdInterno",ord."EsPorMonto",ord."MontoTotal",
  					FLOOR(
  					   (ord."MontoTotal" + ( SUM(CASE cob."FormaCobro" WHEN 'F' THEN cob."ValorCobroMonedaLiquidacion" ELSE 0 END)) * ( 1 + COALESCE(mat."ValorCobro",0)/100)
  					)/
  					(ord."PrecioOrdenMonedaLiquidacion" * ( 1 - SUM(CASE cob."FormaCobro" WHEN 'V' THEN cob."ValorCobro" /100 ELSE 0 END)  *  ( 1 + COALESCE(mat."ValorCobro",0)/100)     ) ))
  					 as "CantidadOrden"
  					
  				from "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes" ord
  				INNER JOIN "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos" cob on ord."IdInterno" = cob."IdInterno"
 				LEFT JOIN :v_MatrizOperacional mat on
 					mat."IdCobro" = 'IVA' and
			   		mat."IdInterno" = ord."IdInterno"
			   	where ord."EsPorMonto" = 'S' and ord."AccionOrden" = 'VENTA'
  				group by ord."IdInterno",ord."MontoTotal",ord."PrecioOrdenMonedaLiquidacion",mat."ValorCobro",ord."EsPorMonto";
  	
  --SELECT 'v_cantidadesVENTA',* FROM :v_cantidadesCOMPRA;
  			
  	--CANTIDADES LISTAS
	v_ordenes = SELECT ord."IdInterno","IdPortafolio","IdTipoProducto","IdTipoOperacion","IdTipoCanalIngreso","IdBolsa","IdInstrumento","DiasPlazo",
						CASE ord."EsPorMonto" 
							WHEN 'N' THEN ord."CantidadOrden" 
							ELSE COALESCE(cantC."CantidadOrden",cantV."CantidadOrden",0)
							END as "CantidadOrden" ,
						"MontoOrden","ValorRescateOrden","EsFungible","TasaSimultanea","TasaRFIF",
						"TasaVentaCorta","IdMonedaLiquidacionOrden","PrecioOrden",COALESCE("TipoCambio",0) as "TipoCambio",ord."MontoTotal",ord."EsPorMonto"
				FROM "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes" ord
				LEFT JOIN :v_cantidadesCOMPRA cantC on ord."IdInterno" = cantC."IdInterno"
				LEFT JOIN :v_cantidadesVENTA cantV on ord."IdInterno" = cantV."IdInterno";
	
	-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	---CALCULO PXQ----------------------------------------------------------------------------------------------------------------------------------------------------------
	-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	
	v_Ordenes_Calculos = SELECT ord."IdInterno",
							CASE --cantidad
								WHEN ins."IdClasificacionTipoInstrumento" = 'RV' and ord."IdTipoOperacion"  = 'COMSIM' THEN	1
								WHEN ins."IdClasificacionTipoInstrumento" = 'IF' and ord."EsFungible"  = 'S' THEN	1
								ELSE ord."CantidadOrden"
							END as "CantidadOrden",
							CASE 
								WHEN ins."IdClasificacionTipoInstrumento" = 'RV' AND ord."IdTipoOperacion" != 'COMSIM' THEN
									ord."PrecioOrden" * ord."CantidadOrden"
								WHEN ins."IdClasificacionTipoInstrumento" = 'DV' THEN
									ord."PrecioOrden" * ord."CantidadOrden" * ins."Tamano"
								ELSE
									ord."MontoOrden" --RF, IF, COMSIM
							END as "MontoOrden",
							CASE 
								WHEN ins."IdClasificacionTipoInstrumento" = 'IF' THEN
									ord."ValorRescateOrden"
								ELSE
									0
							END as "ValorRescateOrden",
							ord."EsFungible",ord."TasaSimultanea",ord."TasaRFIF",ord."TasaVentaCorta",ord."IdMonedaLiquidacionOrden",
							ord."PrecioOrden",
							ord."TipoCambio",ord."MontoTotal",ord."EsPorMonto",							
							CASE 
								WHEN ord."IdTipoOperacion" in ('COMSIM','VENSIM') THEN :v_IdMonedaSimultanea
								ELSE ins."IdMoneda" 
							END as "IdMonedaOrden",
							ope."AccionOrden" as "AccionOrden"
						FROM :v_ordenes ord
						INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON ins."IdInstrumento" = ord."IdInstrumento"
						INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" ope on ord."IdTipoOperacion" = ope."IdTipoOperacion" ;
	
	--SELECT 'v_ordenes',* FROM :v_ordenes;
	--SELECT 'v_Ordenes_Calculos',* FROM :v_Ordenes_Calculos;
	--SELECT 'v_MatrizOperacional',* FROM :v_MatrizOperacional;
	SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
	--PASAMOS MONTOS DE OPERACION A MONEDA DEL COBRO
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
	SELECT :v_IdTrabajoParidad,mat."IdInternoCobro" AS "IdInterno",:v_FechaSistemaDate as "Fecha", ord_calc."IdMonedaOrden" as "IdMonedaOrigen", mat."IdMoneda" As "IdMonedaDestino",
									ord."TipoCambio" as "ValorCambioLocalModificado",
									ord_calc."MontoOrden" as "MontoOrigen",
									'ORDENES' as "IdUso"
									FROM :v_ordenes ord
									inner join :v_Ordenes_Calculos ord_calc on ord."IdInterno" = ord_calc."IdInterno"
									inner join :v_MatrizOperacional mat on mat."IdInterno" = ord."IdInterno";
	
	
	--select 'v_in_CalcularParidadYTipoCambio1',* from :v_in_CalcularParidadYTipoCambio;
	
	call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => :v_IdTrabajoParidad
	);
	
	--SELECT 'v_out_CalcularParidadYTipoCambio1',* FROM :v_out_CalcularParidadYTipoCambio;
	
	v_CalcularCobros_Cobros = select ord."IdInterno" as "IdOperacion",mat."IdInternoCobro", mat."IdCobro",mat."ValorCobro" as "ValorCobroPerfil",mat."ValorTopeMinimoOrden" as "MontoMinimo", 
					 				mat."IdMoneda", mat."FormaCobro",mat."CodigoCobroClasificacion",ord_calc."CantidadOrden" as "Cantidad",COALESCE(ind."MontoDestino",0) as "MontoOperacion",ord_calc."ValorRescateOrden" as "ValorRescate",ord."DiasPlazo",mat."PermiteCambiarValor"
									from :v_ordenes ord
									inner join :v_Ordenes_Calculos ord_calc on ord."IdInterno" = ord_calc."IdInterno"
									inner join :v_MatrizOperacional mat on mat."IdInterno" = ord."IdInterno"
									LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" ind on mat."IdInternoCobro" = ind."IdInterno" AND ind."IdTrabajo" = :v_IdTrabajoParidad;
									
				
	--SELECT 'v_CalcularCobros_Cobros',* FROM :v_CalcularCobros_Cobros;
	v_CalcularCobros_CobrosModificados = select ord."IdInterno" as "IdOperacion",cob."IdCobro",cob."ValorCobroAplicado"
										FROM :v_ordenes ord
										INNER JOIN "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_CobrosModificados" cob on ord."IdInterno" = cob."IdInterno";
	
	CALL "BSCL.Plataforma.BD.Servicios::COM.CalcularCobros" (
		p_IdEmpresa 			=> :p_IdEmpresa,
		p_Cobros				=> :v_CalcularCobros_Cobros,
		p_CobrosModificados		=> :v_CalcularCobros_CobrosModificados,
		p_out_Cobros			=> :v_out_cobros
	);
	
	--SELECT 'v_out_cobros',* FROM :v_out_cobros;
	SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
	--PASAMOS MONTOS DE COBROS A MONEDA LIQUIDACION Y MONTO ORDEN A MONEDA LIQUIDACION
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
	SELECT :v_IdTrabajoParidad,mat."IdInternoCobro" AS "IdInterno",:v_FechaSistemaDate as "Fecha", cob."IdMoneda" as "IdMonedaOrigen", ord."IdMonedaLiquidacionOrden" As "IdMonedaDestino",
			ord."TipoCambio" as "ValorCambioLocalModificado",
			cob."MontoCobroCalculado" as "MontoOrigen",
			'ORDENES' as "IdUso"
			FROM :v_ordenes ord
			INNER join :v_MatrizOperacional mat on mat."IdInterno" = ord."IdInterno"
			INNER JOIN :v_out_cobros cob on mat."IdInterno" = cob."IdOperacion" AND mat."IdCobro" = cob."IdCobro";
	
	call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => :v_IdTrabajoParidad
	);
	
	--SELECT '1',:v_IdTrabajoParidad,* FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida";
	INSERT INTO "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Cobros"
				("IdInterno","IdCobro","CodigoCobroClasificacion","IdMonedaCobro","ValorCobroPerfil","PermiteCambiarValor","FormaCobro",
				"MontoMinimo","ValorCobroAplicado","MontoCobroCalculado","MontoCobroCalculadoLocal","MontoCobroCalculadoLiquidacion")
	SELECT		cob."IdOperacion" as "IdInterno",cob."IdCobro",cob."CodigoCobroClasificacion",cob."IdMoneda" as "IdMonedaCobro",cob."ValorCobroPerfil",
				cob."PermiteCambiarValor",cob."FormaCobro",cob."MontoMinimo",cob."ValorCobroAplicado",cob."MontoCobroCalculado",
				COALESCE(ind."MontoLocal",cob."MontoCobroCalculado") as "MontoCobroCalculadoLocal",
				COALESCE(ind."MontoDestino",cob."MontoCobroCalculado") as "MontoCobroCalculadoLiquidacion"
	FROM :v_ordenes ord 
	INNER join :v_MatrizOperacional mat on mat."IdInterno" = ord."IdInterno"
	INNER JOIN :v_out_cobros cob on mat."IdInterno" = cob."IdOperacion" AND mat."IdCobro" = cob."IdCobro"
	LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" ind on ind."IdInterno" = mat."IdInternoCobro" AND ind."IdTrabajo" = :v_IdTrabajoParidad
	ORDER BY cob."IdOperacion",cob."IdCobro";
	
	SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
	
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
	SELECT :v_IdTrabajoParidad,ord."IdInterno",:v_FechaSistemaDate as "Fecha", ord_calc."IdMonedaOrden" as "IdMonedaOrigen", ord."IdMonedaLiquidacionOrden" As "IdMonedaDestino",
									ord."TipoCambio" as "ValorCambioLocalModificado",
									ord_calc."MontoOrden" as "MontoOrigen",
									'ORDENES' as "IdUso"
									FROM :v_ordenes ord
									INNER JOIN :v_Ordenes_Calculos ord_calc on ord."IdInterno" = ord_calc."IdInterno";
	
	call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => :v_IdTrabajoParidad
	);
	
	
	v_ordenes_CalculoFinal = 	select ord."IdInterno", 
								COALESCE(ind."MontoLocal",ord_calc."MontoOrden") as "MontoOperacionLocal", 
								COALESCE(ind."MontoDestino",ord_calc."MontoOrden") as "MontoOperacionLiquidacion", 
								sum(cob."MontoCobroCalculadoLocal") as "TotalCobrosLocal", sum(cob."MontoCobroCalculadoLiquidacion") as "TotalCobrosLiquidacion",
								CASE ord_calc."AccionOrden" 
									WHEN 'COMPRA' THEN COALESCE(ind."MontoDestino",ord_calc."MontoOrden") + sum(cob."MontoCobroCalculadoLiquidacion")
									WHEN 'VENTA' THEN  COALESCE(ind."MontoDestino",ord_calc."MontoOrden") - sum(cob."MontoCobroCalculadoLiquidacion")
									ELSE 0 
								END AS "MontoTotalLiquidacion"
							 	from :v_ordenes ord 
								inner join :v_Ordenes_Calculos ord_calc on ord."IdInterno" = ord_calc."IdInterno"
								LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" ind on ind."IdInterno" = ord_calc."IdInterno" AND ind."IdTrabajo" = :v_IdTrabajoParidad
								INNER JOIN "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Cobros" cob on ord."IdInterno" = cob."IdInterno"
								GROUP BY ord."IdInterno", ind."MontoLocal", ind."MontoDestino",ord_calc."AccionOrden",ord_calc."MontoOrden" ;
		
	
	--SELECT 'v_ordenes_CalculoFinal',* FROM :v_ordenes_CalculoFinal;
	
	INSERT INTO "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Ordenes"
			("IdInterno","IdPortafolio","IdTipoProducto","IdTipoOperacion","IdTipoCanalIngreso","IdBolsa","IdInstrumento","DiasPlazo","CantidadOrden","MontoOrden",
			"ValorRescateOrden","EsFungible","TasaSimultanea","TasaRFIF","TasaVentaCorta","IdMonedaLiquidacionOrden","PrecioOrden","TipoCambio","MontoTotal","EsPorMonto",
			"IdMonedaOrden", "MontoTotalLiquidacion","MontoOperacion","CantidadOperacion","ValorRescateOperacion","MontoOperacionLocal","MontoOperacionLiquidacion", 
			"TotalCobrosLocal","TotalCobrosLiquidacion")
		
	SELECT	ord."IdInterno",ord."IdPortafolio",ord."IdTipoProducto",ord."IdTipoOperacion",ord."IdTipoCanalIngreso",ord."IdBolsa",ord."IdInstrumento",ord."DiasPlazo",ord."CantidadOrden",ord."MontoOrden",
			ord."ValorRescateOrden",ord."EsFungible",ord."TasaSimultanea",ord."TasaRFIF",ord."TasaVentaCorta",ord."IdMonedaLiquidacionOrden",ord."PrecioOrden",ord."TipoCambio",ord."MontoTotal",ord."EsPorMonto",
			ord_calc."IdMonedaOrden", ord_calc_final."MontoTotalLiquidacion",ord_calc."MontoOrden" as "MontoOperacion",ord_calc."CantidadOrden" as "CantidadOperacion",ord_calc."ValorRescateOrden" as "ValorRescateOperacion", 
			ord_calc_final."MontoOperacionLocal",ord_calc_final."MontoOperacionLiquidacion",ord_calc_final."TotalCobrosLocal",ord_calc_final."TotalCobrosLiquidacion"
 	FROM :v_ordenes ord
 	INNER JOIN :v_Ordenes_Calculos ord_calc on ord."IdInterno" = ord_calc."IdInterno"
	INNER JOIN :v_ordenes_CalculoFinal ord_calc_final on ord."IdInterno" = ord_calc_final."IdInterno";
	
										
	--call "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'CalcularMontoYCobrosOperacion','PASO 3','TERMINO CALCULO PXQ');
	SIGNAL SALIDA_CONTROLADA;
	
END;
GO


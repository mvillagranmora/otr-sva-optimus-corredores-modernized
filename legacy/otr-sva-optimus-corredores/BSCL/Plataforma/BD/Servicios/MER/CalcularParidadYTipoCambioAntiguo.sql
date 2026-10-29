SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambioAntiguo";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambioAntiguo"(
	
	IN	p_IdEmpresa	BIGINT,
	IN	p_Entrada "BSCL.Plataforma.BD.Tipos::MER:EntradaCalcularParidadYTipoCambio",
	OUT	p_Salida "BSCL.Plataforma.BD.Tipos::MER:SalidaCalcularParidadYTipoCambio" DEFAULT EMPTY					
										
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
  BEGIN	
  	
  	DECLARE v_IdTipoRegistroDefault NVARCHAR(40) = 'OBSERVADO';
  	DECLARE v_IdMonedaCambio 		NVARCHAR(3) = 'USD';
  	DECLARE v_IdMonedaLocal 		NVARCHAR(3) = 'CLP';
  	DECLARE v_TipoBusquedaDefault 	NVARCHAR(1) = 'D';
  	
  	-- MONEDA ORIGEN --(FACTOR ORIGEN CAMBIO)--> MONEDA CAMBIO --(TIPO DE CAMBIO NEGOCIADO O FACTOR CAMBIO LOCAL)--> MONEDA LOCAL --(FACTOR LOCAL CAMBIO)--> MONEDA CAMBIO --(FACTOR CAMBIO DESTINO)--> MONEDA DESTINO
	
	v_Indicadores = WITH ASDF AS (
						SELECT ent."IdInterno",
							par."IdParametroFinanciero",CASE defpar."Periodo" WHEN 'DI' THEN par."Fecha" WHEN 'ME' THEN par."AgnoMes" || '-01' WHEN 'AN' THEN par."Agno" || '-01-01' END as "FechaParametroFinanciero",defpar."Nombre",par."Valor",defpar."IdMonedaValor" as "IdMoneda",
						ROW_NUMBER() OVER (partition by "IdInterno",par."IdParametroFinanciero" order by par."IdParametroFinanciero",par."Fecha" DESC) AS "Prioridad"
						FROM :p_Entrada ent
						INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" par ON par."IdParametroFinanciero" in (ent."IdMonedaOrigen",ent."IdMonedaDestino") AND par."Fecha" <= ent."Fecha"
						INNER JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" defpar ON defpar."IdParametroFinanciero" = par."IdParametroFinanciero"
					)
					SELECT "IdInterno","IdParametroFinanciero","FechaParametroFinanciero","Nombre","Valor","IdMoneda"
					FROM ASDF WHERE "Prioridad" = 1;
	
	
					
	v_EntradaCorregida = 	SELECT ent."IdInterno",ent."Fecha","IdMonedaOrigen" AS "IdMonedaOrigenOriginal","IdMonedaDestino" AS "IdMonedaDestinoOriginal",
							COALESCE(ind1."IdMoneda","IdMonedaOrigen") AS "IdMonedaOrigen",COALESCE(ind2."IdMoneda","IdMonedaDestino") AS "IdMonedaDestino",
							"ValorCambioLocalModificado",COALESCE("MontoOrigen"*ind1."Valor","MontoOrigen") AS "MontoOrigen","IdUso"
							FROM :p_Entrada ent
							LEFT JOIN :v_Indicadores ind1 on ent."IdInterno" = ind1."IdInterno" AND ent."IdMonedaOrigen" = ind1."IdParametroFinanciero"
							LEFT JOIN :v_Indicadores ind2 on ent."IdInterno" = ind2."IdInterno" AND ent."IdMonedaDestino" = ind2."IdParametroFinanciero";
	
	--SELECT 'v_EntradaCorregida',* FROM :v_EntradaCorregida;
	
	v_Combinaciones =	WITH ASDF AS (
							SELECT "IdInterno","IdUso","IdMonedaOrigen" AS "M1",:v_IdMonedaCambio AS "M2",'OrigenCambio' as "Paso",1 as "OrdenPaso"
							FROM :v_EntradaCorregida
							UNION
							SELECT "IdInterno","IdUso",:v_IdMonedaCambio AS "M1",:v_IdMonedaLocal AS "M2",'CambioLocal' as "Paso",2 as "OrdenPaso"
							FROM :v_EntradaCorregida
							UNION
							SELECT "IdInterno","IdUso",:v_IdMonedaLocal AS "M1",:v_IdMonedaCambio AS "M2",'LocalCambio' as "Paso",3 as "OrdenPaso"
							FROM :v_EntradaCorregida
							UNION
							SELECT "IdInterno","IdUso",:v_IdMonedaCambio AS "M1","IdMonedaDestino" AS "M2",'CambioDestino' as "Paso",4 as "OrdenPaso"
							FROM :v_EntradaCorregida
							order by "IdInterno"
						) SELECT ASDF."IdInterno",ASDF."IdUso",COALESCE(uso."TipoBusqueda",:v_TipoBusquedaDefault) as "TipoBusqueda",ASDF."M1",ASDF."M2",ASDF."Paso",ASDF."OrdenPaso"
						FROM ASDF 
						LEFT JOIN "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio" uso on ASDF."IdUso" = uso."IdUso";
	
	
	v_DatosParidadYTipoCambio =	
						
						SELECT par."Fecha",par."IdMoneda" AS "IdMonedaOrigen",:v_IdMonedaLocal as "IdMonedaDestino",par."IdTipoRegistro",par."ParidadMonedaCambio" as "Factor"
						FROM :v_EntradaCorregida ent
						LEFT JOIN "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" par on (par."IdMoneda" IN (ent."IdMonedaOrigen",ent."IdMonedaDestino",:v_IdMonedaLocal,:v_IdMonedaCambio)) AND par."Fecha" <= ent."Fecha" AND par."IdEmpresa" = :p_IdEmpresa AND par."IdEstado" = 'ACT'
							
						UNION
						SELECT par."Fecha",par."IdMoneda" AS "IdMonedaOrigen",:v_IdMonedaCambio as "IdMonedaDestino",par."IdTipoRegistro",par."ParidadMonedaCambio" as "Factor"
						FROM :v_EntradaCorregida ent
						LEFT JOIN "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" par on (par."IdMoneda" IN (ent."IdMonedaOrigen",ent."IdMonedaDestino",:v_IdMonedaLocal,:v_IdMonedaCambio)) AND par."Fecha" <= ent."Fecha" AND par."IdEmpresa" = :p_IdEmpresa AND par."IdEstado" = 'ACT';
							
	

	
	--SELECT :v_maxFecha FROM DUMMY;
	--SELECT 'blabla',par.*
	--FROM "BSCL.Plataforma.BD::MER.ParidadYTipoCambio" par
	--INNER JOIN :v_MonedasUtilizadas mon on par."IdMoneda" = mon."IdMoneda" AND par."IdEmpresa" = :p_IdEmpresa AND par."IdEstado" = 'ACT' AND par."Fecha" <= :v_maxFecha;
	
	--select 'v_DatosParidadYTipoCambio',* from :v_DatosParidadYTipoCambio;
					
	v_DatosFinales =	SELECT * FROM :v_DatosParidadYTipoCambio
						UNION 
						SELECT "Fecha","IdMonedaDestino" AS "IdMonedaOrigen","IdMonedaOrigen" AS "IdMonedaDestino","IdTipoRegistro",CAST(1/"Factor" AS DECIMAL(26,8)) as "Factor"
						FROM :v_DatosParidadYTipoCambio
						WHERE "Factor" > 0;
	
	--SELECT 'v_DatosFinales',* FROM :v_DatosFinales;
	
	--busqueda de uso, proridad y tipo de registro que aplica
	v_Busqueda = SELECT ent."IdInterno",ent."M1",ent."M2",ent."Paso",ent."OrdenPaso",ent."IdUso",ent."TipoBusqueda",usotipo."Prioridad" AS "Prioridad",usotipo."IdTipoRegistro" as "IdTipoRegistro"
				FROM :v_Combinaciones ent
				INNER JOIN "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" usotipo on ent."IdUso" = usotipo."IdUso" 
				and 'S' = CASE "Paso"
									WHEN 'OrigenCambio' THEN usotipo."AplicaOrigenCambio"
									WHEN 'CambioLocal' THEN usotipo."AplicaCambioLocal"
									WHEN 'LocalCambio' THEN usotipo."AplicaLocalCambio"
									WHEN 'CambioDestino' THEN usotipo."AplicaCambioDestino" END
				UNION
				--BUSQUEDA DEFAULT
				SELECT "IdInterno","M1","M2","Paso","OrdenPaso","IdUso","TipoBusqueda",99 as "Prioridad",:v_IdTipoRegistroDefault AS "IdTipoRegistro"
				FROM :v_Combinaciones;
	
	--SELECT 'v_Busqueda',* FROM :v_Busqueda;
	
	
	v_DatosEncontrados =	WITH SSS AS (
								SELECT	"IdInterno","M1","M2","Paso","TipoBusqueda","IdUso","Prioridad",bus."IdTipoRegistro","Fecha","Factor",
										row_number() over (partition by "IdInterno","Paso" order by "Prioridad","Fecha" desc) as "OrdinalDia",
										row_number() over (partition by "IdInterno","Paso" order by "Fecha" desc,"Prioridad") as "OrdinalUso"
								FROM :v_Busqueda bus
								INNER JOIN :v_DatosFinales par on  bus."IdTipoRegistro" = par."IdTipoRegistro" and bus."M1" = par."IdMonedaOrigen" AND bus."M2" = par."IdMonedaDestino"
								order by "IdInterno"
							)
							SELECT "IdInterno","M1","M2","Paso","TipoBusqueda","IdUso","Prioridad","IdTipoRegistro","Fecha","Factor" FROM SSS
							WHERE CASE "TipoBusqueda" WHEN 'D' THEN "OrdinalDia" WHEN 'U' THEN "OrdinalUso" END = 1;
	
	--SELECT 'v_DatosEncontrados',* FROM :v_DatosEncontrados;
							
	v_CasiListo = 	SELECT ent."IdInterno","IdMonedaOrigen",:v_IdMonedaCambio AS "IdMonedaCambio",:v_IdMonedaLocal AS "IdMonedaLocal","IdMonedaDestino",data1."Factor" AS "FactorOrigenCambioOriginal",
							1/data1."Factor" AS "FactorOrigenCambio",data2."Factor" AS "FactorCambioLocal",data3."Factor" AS "FactorLocalCambio",data4."Factor" AS "FactorCambioDestino",
							data1."Fecha" AS "FechaOrigenCambio",data2."Fecha" AS "FechaCambioLocal",data3."Fecha" AS "FechaLocalCambio",data4."Fecha" AS "FechaCambioDestino",
							data1."IdTipoRegistro" AS "IdTipoRegistroOrigenCambio",data2."IdTipoRegistro" AS "IdTipoRegistroCambioLocal",data3."IdTipoRegistro" AS "IdTipoRegistroLocalCambio",data4."IdTipoRegistro" AS "IdTipoRegistroCambioDestino",
							ent."MontoOrigen",ent."IdUso",ent."ValorCambioLocalModificado"
					FROM :v_EntradaCorregida ent
					LEFT JOIN :v_DatosEncontrados data1 on ent."IdInterno" = data1."IdInterno" AND data1."Paso" = 'OrigenCambio' AND ent."IdMonedaOrigen" = data1."M1" AND :v_IdMonedaCambio = data1."M2"
					LEFT JOIN :v_DatosEncontrados data2 on ent."IdInterno" = data2."IdInterno" AND data2."Paso" = 'CambioLocal' AND :v_IdMonedaCambio = data2."M1" AND :v_IdMonedaLocal = data2."M2"
					LEFT JOIN :v_DatosEncontrados data3 on ent."IdInterno" = data3."IdInterno" AND data3."Paso" = 'LocalCambio' AND :v_IdMonedaLocal = data3."M1" AND :v_IdMonedaCambio = data3."M2"
					LEFT JOIN :v_DatosEncontrados data4 on ent."IdInterno" = data4."IdInterno" AND data4."Paso" = 'CambioDestino' AND :v_IdMonedaCambio = data4."M1" AND ent."IdMonedaDestino" = data4."M2";
	
	--SELECT 'v_CasiListo',* FROM :v_CasiListo;
	
	v_Salida = 
		SELECT ccc."IdInterno",ccc."IdMonedaOrigen",ccc."IdMonedaDestino",ccc."ValorCambioLocalModificado",ccc."MontoOrigen",ccc."IdUso",
				
				CASE 
					WHEN ccc."IdMonedaDestino" = ccc."IdMonedaOrigen" AND ccc."ValorCambioLocalModificado" = 0 THEN 1						--no hago nada
					WHEN ccc."IdMonedaDestino" = :v_IdMonedaCambio AND ccc."ValorCambioLocalModificado" = 0 THEN "FactorOrigenCambio"		--hago ORIGEN -> CAMBIO 				
					WHEN  ccc."IdMonedaDestino" = :v_IdMonedaLocal THEN 																	--hago ORIGEN -> CAMBIO -> LOCAL
						"FactorOrigenCambio" * 
						CASE ccc."ValorCambioLocalModificado" WHEN 0 THEN "FactorCambioLocal" ELSE ccc."ValorCambioLocalModificado" END		
					ELSE																													--tengo que hacer todo el camino																								
						"FactorOrigenCambio" * 
						CASE ccc."ValorCambioLocalModificado" WHEN 0 THEN "FactorCambioLocal" ELSE  ccc."ValorCambioLocalModificado" END * 
						"FactorLocalCambio" * 
						"FactorCambioDestino" 
					END
				AS "FactorOrigenDestino",
				
				CASE 
					WHEN :v_IdMonedaLocal = ccc."IdMonedaOrigen" AND ccc."ValorCambioLocalModificado" = 0 THEN 1							--no hago nada
					WHEN :v_IdMonedaLocal = :v_IdMonedaCambio AND ccc."ValorCambioLocalModificado" = 0 THEN	"FactorOrigenCambio"			--hago ORIGEN -> CAMBIO 									
					ELSE
						"FactorOrigenCambio" * 
						CASE ccc."ValorCambioLocalModificado" WHEN 0 THEN "FactorCambioLocal" ELSE ccc."ValorCambioLocalModificado" 
					END
				END AS "FactorOrigenLocal",
				
				ccc."FactorOrigenCambio",ccc."FactorOrigenCambioOriginal",ccc."FechaOrigenCambio",ccc."IdTipoRegistroOrigenCambio",
				ccc."FactorCambioLocal",ccc."FechaCambioLocal",ccc."IdTipoRegistroCambioLocal",
				ccc."FactorLocalCambio",ccc."FechaLocalCambio",ccc."IdTipoRegistroLocalCambio",
				ccc."FactorCambioDestino",ccc."FechaCambioDestino",ccc."IdTipoRegistroCambioDestino"
		FROM :v_Casilisto ccc;
	
	--select 'v_Salida',* from :v_Salida;
	
	p_Salida = 
		SELECT sal."IdInterno",ent."Fecha",ent."IdMonedaOrigenOriginal" AS "IdMonedaOrigen",ent."IdMonedaDestinoOriginal" AS "IdMonedaDestino",sal."ValorCambioLocalModificado",
				CASE WHEN ent."IdMonedaOrigenOriginal" = ent."IdMonedaOrigen" THEN sal."MontoOrigen" ELSE sal."MontoOrigen"/ind1."Valor" END AS "MontoOrigen",
				sal."IdUso",
				
				CASE 
					WHEN ent."IdMonedaOrigenOriginal" != ent."IdMonedaOrigen"	THEN "FactorOrigenDestino"*ind1."Valor"
					WHEN ent."IdMonedaDestinoOriginal" != ent."IdMonedaDestino" THEN "FactorOrigenDestino"/ind2."Valor" 
					ELSE "FactorOrigenDestino" END AS "FactorOrigenDestino",
				CASE 
					WHEN ent."IdMonedaOrigenOriginal" != ent."IdMonedaOrigen"	THEN sal."MontoOrigen" * "FactorOrigenDestino"
					WHEN ent."IdMonedaDestinoOriginal" != ent."IdMonedaDestino" THEN sal."MontoOrigen" * "FactorOrigenDestino"/ind2."Valor" 
					ELSE sal."MontoOrigen" * "FactorOrigenDestino" END AS "MontoDestino",
				CASE 
					WHEN ent."IdMonedaOrigenOriginal" != ent."IdMonedaOrigen" THEN "FactorOrigenLocal"/ind1."Valor"				--SI CORREGIMOS LA MONEDA AL PRINCIPIO, AQUI LA DEVOLVEMOS
					ELSE "FactorOrigenLocal"  END AS "FactorOrigenLocal",					
				sal."MontoOrigen" * "FactorOrigenLocal" AS "MontoLocal",
				
				CASE 
					WHEN ent."IdMonedaOrigenOriginal" != ent."IdMonedaOrigen"	THEN "FactorOrigenCambio"*ind1."Valor"
					ELSE "FactorOrigenCambio" END AS "FactorOrigenCambio",
				sal."FactorOrigenCambioOriginal",sal."FechaOrigenCambio",sal."IdTipoRegistroOrigenCambio",
				sal."FactorCambioLocal",sal."FechaCambioLocal",sal."IdTipoRegistroCambioLocal",
				sal."FactorLocalCambio",sal."FechaLocalCambio",sal."IdTipoRegistroLocalCambio",
				CASE 
					WHEN ent."IdMonedaDestinoOriginal" != ent."IdMonedaDestino"	THEN "FactorCambioDestino"/ind2."Valor"
					ELSE "FactorCambioDestino" END AS "FactorCambioDestino",
				sal."FechaCambioDestino",sal."IdTipoRegistroCambioDestino",
	
				CASE 	WHEN sal."FactorOrigenDestino" IS NULL THEN 'RECHAZO'
						WHEN sal."FactorOrigenLocal" IS NULL THEN 'RECHAZO' 
						ELSE 'OK' END AS "Resultado",
				CASE	WHEN sal."FactorOrigenDestino" IS NULL THEN 'ERROR_CALCULAR_PARIDAD_MONTO_DESTINO'
						WHEN sal."FactorOrigenLocal" IS NULL THEN 'ERROR_CALCULAR_PARIDAD_MONTO_LOCAL'
						ELSE 'OK' END AS "CodigoMensaje"
		FROM :v_Salida sal
		INNER JOIN :v_EntradaCorregida ent on sal."IdInterno" = ent."IdInterno"
		LEFT JOIN :v_Indicadores ind1 on ind1."IdParametroFinanciero" = ent."IdMonedaOrigenOriginal"
		LEFT JOIN :v_Indicadores ind2 on ind2."IdParametroFinanciero" = ent."IdMonedaDestinoOriginal";
	
 END;

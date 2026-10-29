SELECT NOR_ilsItem."IdItem",NOR_ilsItem."IdTipoItem",NOR_ilsItem."Nombre",NOR_ilsItem."NombreCorto"
	,COALESCE(t1."CuentaContable", '') as "CuentaContable"
	,t1."CuentaOptimus",COALESCE(t1."SaldoDebeHaber", '') as "SaldoDebeHaber",COALESCE(t1."Condicion", '') as "Condicion",COALESCE(t1."Monto", 0) as "Monto",COALESCE(t1."Operacion", '') as "Operacion"
	,COALESCE(t1."Factor", 0) as "Factor",COALESCE(t3."MayorACero", 'N') as "MayorACero",COALESCE(t3."MenorACero", 'N') as "MenorACero",COALESCE(t3."PermiteCero", 'N') as "PermiteCero"
	,COALESCE(t2."IdProcesoFuncion", 0) as "IdProcesoFuncion"
	,IFNULL(	LTRIM(RTRIM(NOR_ilsProcesoFuncion."IdProcesoFuncion")) || ' - ' || LTRIM(RTRIM(NOR_ilsProcesoFuncion."Nombre")), '') 	AS "ProcesoFuncion"
	,COALESCE(LTRIM(RTRIM(NOR_ilsProcesoFuncion."Nombre")), '')	AS "NombreFuncion"
	,TO_DECIMAL(COALESCE(NOR_ilsItemDig."Monto",0.0),26,8) AS "MontoCalculado"
	,COALESCE(NOR_ilsConceptoConfiguracion."IdConceptoGrupo",NOR_ilsItemDig."IdConceptoGrupo") AS "IdConceptoGrupo"
	,COALESCE(NOR_ilsConceptoConfiguracion."IdConcepto",NOR_ilsItemDig."IdConcepto") AS "IdConcepto"
	,COALESCE(NOR_ilsIndiceConfiguracion."IdIndice", NOR_ilsItemDig."IdIndice") AS "IdIndice"
	,NOR_ilsConceptoConfiguracion."Operacion" AS "OperacionGC"
FROM "BSCL.Normativa.BD::ILS.Item" AS NOR_ilsItem		
LEFT JOIN
(SELECT DISTINCT "IdItem", "CuentaContable", "CuentaOptimus", "SaldoDebeHaber", "Condicion", "Monto", "Operacion", "Factor", "Estado" FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" WHERE "TipoItem" = 'C') t1
ON NOR_ilsItem."IdItem"=t1."IdItem"
LEFT JOIN
(SELECT distinct "IdItem", "IdProcesoFuncion", "Estado" FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" WHERE "TipoItem" = 'P') t2
ON NOR_ilsItem."IdItem"=t2."IdItem"
LEFT JOIN
(SELECT distinct "IdItem", "MayorACero", "MenorACero", "PermiteCero", "Estado" FROM "BSCL.Normativa.BD::ILS.ItemConfiguracion" WHERE "TipoItem" = 'D') t3
ON NOR_ilsItem."IdItem" = t3."IdItem"
	LEFT JOIN "BSCL.Normativa.BD::ILS.ProcesoFuncion" AS NOR_ilsProcesoFuncion ON 
		NOR_ilsProcesoFuncion."IdProcesoFuncion" 	= t2."IdProcesoFuncion"
	LEFT JOIN "BSCL.Normativa.BD::ILS.ConceptoConfiguracion" AS NOR_ilsConceptoConfiguracion ON 
		NOR_ilsConceptoConfiguracion."IdItem" 	= NOR_ilsItem."IdItem" 
	LEFT JOIN "BSCL.Normativa.BD::ILS.IndiceConfiguracion" AS NOR_ilsIndiceConfiguracion ON 
		NOR_ilsIndiceConfiguracion."IdConcepto" = NOR_ilsConceptoConfiguracion."IdConcepto"
	LEFT JOIN "BSCL.Normativa.BD::ILS.InformeResultadoItemDig" AS NOR_ilsItemDig ON 
		NOR_ilsItem."IdItem" = 	NOR_ilsItemDig."IdItem"
		AND NOR_ilsConceptoConfiguracion."IdConcepto" = NOR_ilsItemDig."IdConcepto"
		AND NOR_ilsIndiceConfiguracion."IdIndice" = NOR_ilsItemDig."IdIndice"
		AND NOR_ilsConceptoConfiguracion."IdConceptoGrupo" = NOR_ilsItemDig."IdConceptoGrupo"
		AND NOR_ilsItemDig."FechaContable" = :p_FechaContable
WHERE NOR_ilsItem."IdItem"	IN (:p_IdItem) AND NOR_ilsItem."Estado" ='VIG' AND (t1."Estado" ='VIG' OR t2."Estado" ='VIG' OR t3."Estado" ='VIG')
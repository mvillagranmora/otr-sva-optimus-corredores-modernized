SELECT	Met."IdMetodo"
		,Met."NombreMetodo"
		,Met."Glosa"  		
		,Met."IdTipoReajuste"
		,Met."IdMesa"
		,Mesa."Nombre"
		,Mesa."IdPortafolio"
		,Eve."IdEvento"
		,Ope."IdTipoProducto"  
		,CASE 
			WHEN Ope."IdTipoOperacion" = 'VESIMV' THEN 'VENSIM'
			WHEN Ope."IdTipoOperacion" = 'VESIMC' THEN 'COMSIM' 
			WHEN Ope."IdTipoProducto" = 'CUST' AND  Ope."IdTipoOperacion" = 'RETTIT' THEN 'VCDTIT'
			ELSE Ope."IdTipoOperacion"
		END AS "IdTipoOperacion"
		,Eve."IdTipoEvento" 	  
		,Ope."NombreEvento"	  
		,Eve."IndAccion"		  
		,MetRelEve."IdAgrupacion" 
		,Prm."NombreParametro" AS "NombreAgrupacion"
		,Ope."IdTipoComprobante"
		,Ope."IdTipoEstado"
		,CAST(0 AS DECIMAL(26,8)) AS "MtoEvento"
		,CAST(0 AS DECIMAL(26,8)) AS "MtoSuma"
		,CAST(0 AS DECIMAL(26,8)) AS "MtoResta"	
		,MetRelEve."OrdenItem" AS "OrdenItem"
		,CAST(Ord."NombreParametro" AS BIGINT) AS "OrdenGrupo"
		,Met."IdTipoPosicion"
		,MetRelEve."IdMatriz"
FROM	"BSCL.Mesa.BD::MES.MetodoPosicion" Met
		INNER JOIN "BSCL.Mesa.BD::MES.Mesa" Mesa ON Met."IdMesa" = Mesa."IdMesa"
		INNER JOIN "BSCL.Mesa.BD::MES.MetodoEventoPosicion" MetRelEve ON Met."IdMetodo" = MetRelEve."IdMetodo"
		INNER JOIN "BSCL.Mesa.BD::MES.EventoPosicion" Eve ON MetRelEve."IdEvento" = Eve."IdEvento" And Eve."IdEstado" = 'VIG'
		INNER JOIN "BSCL.Mesa.BD::MES.TipoOperacionPosicion" Ope ON Eve."IdOperacion" = Ope."IdOperacion"
		LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" Prm	ON Prm."NombreTipoParametro" = 'Grupo_PosMRF' AND Prm."ValorParametro" = MetRelEve."IdAgrupacion" 
		LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" Ord	ON Ord."NombreTipoParametro" = 'Ord_Grupo_PosMRF' AND Ord."ValorParametro" = MetRelEve."IdAgrupacion" 
WHERE Met."IdEstado" = 'VIG' AND Eve."IdTipoEvento" = 'A' AND Met."IdMetodo" = :p_IdMetodo
ORDER BY Ord."NombreParametro", MetRelEve."OrdenItem"	
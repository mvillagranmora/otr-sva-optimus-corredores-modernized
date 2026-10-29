SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.PosicionMRF";

-- 
--  Proposito : Obtener Posicion RF 
--  Autor ....: MSANDOVAL - BCS
--  Fecha ....: 01-12-2016
-- 

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::MRF.PosicionMRF" (
    --* VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa	BIGINT,
    IN p_IdUsuario	BIGINT,
    IN p_IdModulo	NVARCHAR(3),
    IN p_IdPais		BIGINT,

	--* Variables del SP
	IN p_FechaPosicion	DATE,
	IN p_IdMetodo BIGINT,
	--* Metodo a utilzar :
	--* En este parametro esta implicita la Mesa + el Tipo de Reajuste.
	IN p_TipoPosicion NVARCHAR(15), 
	--* Que grupo de posicion retornar :
	--* 	DET : Detalle de los items
	--* 	RSM : Resumen

    --* Salida SP
	OUT p_out_PosicionFinal DECIMAL(26,8),
	OUT p_out_NombreMesa VARCHAR(60),
	OUT p_out_IdMesa			BIGINT,
    OUT p_out_Posicion TABLE (	
			"IdMetodo"		 	BIGINT
			,"NombreMetodo"   	NVARCHAR(150) 
			,"IdTipoReajuste" 	NVARCHAR(3)
			,"IdMesa"    	 	BIGINT
			,"NomMesa" 			NVARCHAR(60)
			,"IdEvento"		  	BIGINT
			,"IdTipoProducto"  	NVARCHAR(4)
			,"IdTipoOperacion" 	NVARCHAR(6)
			,"IdTipoEvento"		NVARCHAR(3)
			,"NombreAgrupacion" NVARCHAR(100)
			,"NombreEvento"		NVARCHAR(100)
			,"IndAccion"		NVARCHAR(1)
			,"Debe" 			DECIMAL(28,8)
			,"Haber" 			DECIMAL(28,8)
	)
	

) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA BSCL
AS
BEGIN

	DECLARE v_HayEvento BigInt;
	CREATE LOCAL TEMPORARY TABLE #TmpPosicion (
		"IdMetodo"		  BIGINT,
		"NombreMetodo"    NVARCHAR(150),
		"Glosa"  		  NVARCHAR(200),
		"IdTipoReajuste"  NVARCHAR(3),
		"IdMesa"    	  BIGINT,
		"IdPortafolio"	  BIGINT,
		"IdEvento"		  BIGINT,
		"IdTipoProducto"  NVARCHAR(4),
		"IdTipoOperacion" NVARCHAR(6),
		"IdTipoEvento" 	  NVARCHAR(3),	-- I: Inicial, M: Manual, A: Automatica
		"NombreEvento"	  NVARCHAR(60),
		"IndAccion"		  NVARCHAR(1),	-- S: Suma a la Pos., R: Resta a la Pos.
		"IdAgrupacion" 	  NVARCHAR(10),
		"NombreAgrupacion" NVARCHAR(100),
		"IdTipoComprobante" NVARCHAR(6),
		"IdTipoEstado"	   NVARCHAR(3),
		"MtoEvento"			DECIMAL(26,8),
		"MtoSuma"		  DECIMAL(26,8),
		"MtoResta"		  DECIMAL(26,8),
		"OrdenItem"		  BIGINT,
		"OrdenGrupo"		  BIGINT
	);


	INSERT	INTO #TmpPosicion
	SELECT	Met."IdMetodo"
			,Met."NombreMetodo"
			,Met."Glosa"  		
			,Met."IdTipoReajuste"
			,Met."IdMesa"
			,Mesa."IdPortafolio"
			,Eve."IdEvento"
			,Ope."IdTipoProducto"  
			,Ope."IdTipoOperacion" 
			,Eve."IdTipoEvento" 	  
			,Ope."NombreEvento"	  
			,Eve."IndAccion"		  
			,MetRelEve."IdAgrupacion" 
			,Prm."NombreParametro" AS "NombreAgrupacion"
			,Ope."IdTipoComprobante"
			,Ope."IdTipoEstado"
			,0 AS "MtoEvento"
			,0 AS "MtoSuma"
			,0 AS "MtoResta"	
			,MetRelEve."OrdenItem" AS "OrdenItem"
			,Ord."NombreParametro" AS "OrdenGrupo"
	FROM	"BSCL.Mesa.BD::MES.MetodoPosicion" Met
			INNER JOIN "BSCL.Mesa.BD::MES.Mesa" Mesa ON Met."IdMesa" = Mesa."IdMesa"
			INNER JOIN "BSCL.Mesa.BD::MES.MetodoEventoPosicion" MetRelEve ON Met."IdMetodo" = MetRelEve."IdMetodo"
			INNER JOIN "BSCL.Mesa.BD::MES.EventoPosicion" Eve ON MetRelEve."IdEvento" = Eve."IdEvento" And Eve."IdEstado" = 'VIG'
			INNER JOIN "BSCL.Mesa.BD::MES.TipoOperacionPosicion" Ope ON Eve."IdOperacion" = Ope."IdOperacion"
			LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" Prm	ON Prm."NombreTipoParametro" = 'Grupo_PosMRF' AND Prm."ValorParametro" = MetRelEve."IdAgrupacion" 
			LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" Ord	ON Ord."NombreTipoParametro" = 'Ord_Grupo_PosMRF' AND Ord."ValorParametro" = MetRelEve."IdAgrupacion" 
	WHERE	Met."IdMetodo" = p_IdMetodo
			AND Met."IdEstado" = 'VIG';


	--* Nombre de la Mesa
	--* ----------------------------------------------------------------
	SELECT	Mes."Nombre",Mes."IdMesa"
	INTO	p_out_NombreMesa,p_out_IdMesa
	FROM	"BSCL.Mesa.BD::MES.Mesa" Mes, 
			"BSCL.Mesa.BD::MES.MetodoPosicion" Met
	WHERE	Met."IdMetodo" = p_IdMetodo
			AND Met."IdMesa" = Mes."IdMesa";

	
	--* Renta Fija (Desde la Mesa)
	--* ----------------------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_HayEvento
	FROM	#TmpPosicion
	WHERE	"IdTipoProducto" = 'MERF';
	
	IF v_HayEvento > 0 THEN
		--* OPERACIONES DEL DIA...
		UPDATE	POS
		SET		POS."MtoEvento" = TMP."MtoEvento"
				,"MtoSuma" = CASE WHEN POS."IndAccion" = 'S' THEN TMP."MtoEvento" ELSE 0 END
				,"MtoResta" = CASE WHEN POS."IndAccion" = 'R' THEN TMP."MtoEvento" ELSE 0 END
		FROM	#TmpPosicion POS, 
				( 	SELECT	OPE."IdTipoProducto"
							,OPE."IdTipoOperacion"
							,TMP."IdAgrupacion"
							,TMP."IdTipoEstado"
							,SUM("MontoOperacionInicial") AS "MtoEvento"
					FROM	#TmpPosicion TMP,
							"BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
					WHERE	TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') = TO_VARCHAR(p_FechaPosicion, 'YYYY-MM-DD')
							AND OPE."IdTipoProducto" = TMP."IdTipoProducto"
							AND OPE."IdTipoOperacion" = TMP."IdTipoOperacion"
							AND OPE."IdTipoReajuste" = TMP."IdTipoReajuste"							
							AND OPE."IdMesaOrigen" = TMP."IdMesa" 
							AND ( OPE."IdEstado" = 'VIG' OR OPE."IdEstado" = 'PEN' )
							AND TMP."IdTipoEstado" = 'OPE'	


					GROUP BY TMP."IdAgrupacion", OPE."IdTipoProducto", OPE."IdTipoOperacion", TMP."IdTipoEstado" 
				) AS TMP
		WHERE	POS."IdTipoProducto" = TMP."IdTipoProducto"
				AND POS."IdTipoOperacion" = TMP."IdTipoOperacion"
				AND POS."IdTipoEstado" = TMP."IdTipoEstado";
				

		--* ANTICIPOS...
		UPDATE	POS
		SET		POS."MtoEvento" = TMP."MtoEvento"
				,"MtoSuma" = CASE WHEN POS."IndAccion" = 'S' THEN TMP."MtoEvento" ELSE 0 END
				,"MtoResta" = CASE WHEN POS."IndAccion" = 'R' THEN TMP."MtoEvento" ELSE 0 END
		FROM	#TmpPosicion POS, 
				( 	SELECT	OPE."IdTipoProducto"
							,OPE."IdTipoOperacion"
							,TMP."IdTipoEstado" 
							,SUM("MontoOperacionFinal") AS "MtoEvento"
					FROM	#TmpPosicion TMP,
							"BSCL.Mesa.BD.Vistas::ConsultarOperaciones" OPE
					WHERE	TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_FechaPosicion, 'YYYY-MM-DD')
							AND OPE."IdTipoProducto" = TMP."IdTipoProducto"
							AND OPE."IdTipoOperacion" = TMP."IdTipoOperacion"
							AND OPE."IdTipoReajuste" = TMP."IdTipoReajuste"									
							AND OPE."IdMesaOrigen" = TMP."IdMesa" 							
							AND OPE."IdEstado" = 'ANT'
					GROUP BY TMP."IdAgrupacion", OPE."IdTipoProducto", OPE."IdTipoOperacion", TMP."IdTipoEstado" 
				) AS TMP
		WHERE	POS."IdTipoProducto" = TMP."IdTipoProducto"
				AND POS."IdTipoOperacion" = TMP."IdTipoOperacion"			
				AND POS."IdTipoComprobante" = 'OPE'
				AND POS."IdTipoEstado" = 'ANT';		

	END IF;


		
	--* Facturas
	--* ----------------------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_HayEvento
	FROM	#TmpPosicion
	WHERE	"IdTipoProducto" IN ('INSI', 'INIF', 'INRF');		

	IF v_HayEvento > 0 THEN

		--* Facturas Simultaneas CONTADO 
		UPDATE	POS
		SET		POS."MtoEvento" = TMP."MtoEvento"
				,"MtoSuma" = CASE WHEN POS."IndAccion" = 'S' THEN TMP."MtoEvento" ELSE 0 END
				,"MtoResta" = CASE WHEN POS."IndAccion" = 'R' THEN TMP."MtoEvento" ELSE 0 END
		FROM	#TmpPosicion POS, 
				( 		
					SELECT	"IdTipoProducto"
							,"IdTipoOperacion"
							,"IdEstadoComprobante"
							,SUM("Monto") AS "MtoEvento"
					FROM (							
						SELECT 	TMP."IdAgrupacion", OPE."IdTipoProducto" ,OPE."IdTipoOperacion",  
								CASE WHEN OPE."IdEstadoComprobante" IN ('ENV') THEN 'FAC' ELSE OPE."IdEstadoComprobante" END AS "IdEstadoComprobante"
								,"Monto"
						FROM	#TmpPosicion TMP,
								"BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante" OPE
						WHERE	TO_VARCHAR("FechaMovimientoComprobante", 'YYYY-MM-DD') = TO_VARCHAR(:p_FechaPosicion, 'YYYY-MM-DD')
								AND OPE."IdTipoProducto" = TMP."IdTipoProducto"
								AND OPE."IdTipoOperacion" = TMP."IdTipoOperacion"
								AND OPE."IdEstadoComprobante" NOT IN ('ANU')
								AND OPE."IdCondicionLiquidacion" = 'T0' AND "EsAPlazo" = 'N'
								AND OPE."IdPortafolio" = TMP."IdPortafolio"
						)
					GROUP BY "IdAgrupacion", "IdTipoProducto", "IdTipoOperacion", "IdEstadoComprobante"	
				) AS TMP
		WHERE	POS."IdTipoProducto" = TMP."IdTipoProducto"
				AND POS."IdTipoOperacion" = TMP."IdTipoOperacion"
				AND POS."IdTipoComprobante" = TMP."IdEstadoComprobante"
				AND ( POS."IdTipoEstado" = 'OPE' OR POS."IdTipoEstado" = 'ANT');		
				

		--* Simultaneas ANTICIPOS + Anticipos + Facturas IF/RF.
		UPDATE	POS
		SET		POS."MtoEvento" = TMP."MtoEvento"
				,"MtoSuma" = CASE WHEN POS."IndAccion" = 'S' THEN TMP."MtoEvento" ELSE 0 END
				,"MtoResta" = CASE WHEN POS."IndAccion" = 'R' THEN TMP."MtoEvento" ELSE 0 END
		FROM	#TmpPosicion POS, 
				( 	SELECT	OPE."IdTipoProducto"
							,OPE."IdTipoOperacion"
							,OPE."IdEstadoComprobante"
							,SUM("Monto") AS "MtoEvento"
					FROM	#TmpPosicion TMP,
							"BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante" OPE
					WHERE	TO_VARCHAR("FechaMovimientoComprobante", 'YYYY-MM-DD') = TO_VARCHAR(p_FechaPosicion, 'YYYY-MM-DD')
							AND OPE."IdTipoProducto" = TMP."IdTipoProducto"
							AND OPE."IdTipoOperacion" = TMP."IdTipoOperacion"
							AND OPE."IdEstadoComprobante" = TMP."IdTipoComprobante"
							AND OPE."IdCondicionLiquidacion" = 'T0' AND "EsAPlazo" = 'N'
					GROUP BY TMP."IdAgrupacion", OPE."IdTipoProducto", OPE."IdTipoOperacion", OPE."IdEstadoComprobante"
				) AS TMP
		WHERE	POS."IdTipoProducto" = TMP."IdTipoProducto"
				AND POS."IdTipoOperacion" = TMP."IdTipoOperacion"
				AND POS."IdTipoComprobante" = TMP."IdEstadoComprobante"
				AND ( POS."IdTipoEstado" = 'OPE' OR POS."IdTipoEstado" = 'ANT');		


		--* Simultaneas VENCIMIENTOS.
		--* ESTO NO SE PUDO PROBAR (CUSTODIA TODAVÃ�A NO IMPLEMENTA EL PRODUCTO)
		UPDATE	POS
		SET		POS."MtoEvento" = TMP."MtoEvento"
				,"MtoSuma" = CASE WHEN POS."IndAccion" = 'S' THEN TMP."MtoEvento" ELSE 0 END
				,"MtoResta" = CASE WHEN POS."IndAccion" = 'R' THEN TMP."MtoEvento" ELSE 0 END
		FROM	#TmpPosicion POS, 
				( 	SELECT	OPE."IdTipoProducto"
							,OPE."IdTipoOperacion"
							,OPE."IdEstado"
							,SUM("MontoAPlazo") AS "MtoEvento"
					FROM	#TmpPosicion TMP,
							"BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" OPE
					WHERE	TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_FechaPosicion, 'YYYY-MM-DD')
							AND OPE."IdTipoProducto" = TMP."IdTipoProducto"
							AND OPE."IdTipoOperacion" = TMP."IdTipoOperacion"
							AND OPE."IdEstado" = 'VCD'
							---AND OPE."EsAPlazo" = 'N'
					GROUP BY TMP."IdAgrupacion", OPE."IdTipoProducto", OPE."IdTipoOperacion", OPE."IdEstado"
				) AS TMP
		WHERE	POS."IdTipoProducto" = TMP."IdTipoProducto"
				AND POS."IdTipoOperacion" = TMP."IdTipoOperacion"
				---AND POS."IdTipoComprobante" = TMP."IdEstadoComprobante"
				AND ( POS."IdTipoEstado" = 'OPE' OR POS."IdTipoEstado" = 'LIQ');		

	END IF;


	--* Eventos Manuales (Ingresados por Usuario)
	--* ----------------------------------------------------------------
	SELECT	COUNT(1)
	INTO	v_HayEvento
	FROM	#TmpPosicion
	WHERE	"IdTipoComprobante" = 'MAN';		

	IF v_HayEvento > 0 THEN
		UPDATE	TMP
		SET		TMP."MtoEvento" = POS."MontoEvento"
				,"MtoSuma" = CASE WHEN TMP."IndAccion" = 'S' THEN POS."MontoEvento" ELSE 0 END
				,"MtoResta" = CASE WHEN TMP."IndAccion" = 'R' THEN POS."MontoEvento" ELSE 0 END
		FROM	#TmpPosicion TMP,
				( SELECT * FROM "BSCL.Mesa.BD::MES.PosicionMRF" WHERE TO_VARCHAR("Fecha", 'YYYY-MM-DD') = TO_VARCHAR(p_FechaPosicion) ) POS
		WHERE	"IdTipoComprobante" = 'MAN' AND POS."IdMatriz" = TMP."IdMetodo";
	END IF;


	SELECT	SUM("MtoSuma") - SUM("MtoResta")
	INTO	p_out_PosicionFinal
	FROM	#TmpPosicion;
		
	--* DEBE MANTENER ESTOS INSERT PARA ASEGURAR QUE POS. INICIAL APARESCA PRIMERA..
	v_Detalle =
		SELECT 	TMP."IdMetodo"
				,TMP."NombreMetodo"
				,TMP."IdTipoReajuste"
				,TMP."IdMesa" 
				,'FALTA' AS "NomMesa"
				,TMP."IdEvento"
				,TMP."IdTipoProducto"
				,TMP."IdTipoOperacion"
				,TMP."IdTipoEvento"	
				,TMP."NombreAgrupacion"				
				,TMP."NombreEvento"
				,TMP."IndAccion"
				,CASE WHEN TMP."IdTipoOperacion" IN ('COMTER','COMPAC','COMSIM','ANSIMC','VESIMC','COMIIF','COMRF') THEN TMP."MtoEvento"
					  WHEN TMP."IndAccion" = 'R' THEN TMP."MtoEvento"
					  ELSE 0 END AS "Debe"
				,CASE WHEN TMP."IdTipoOperacion" IN ('VENTER','VENPAC','VENSIM','ANSIMV','VESIMV','VENIIF','VENRF') THEN TMP."MtoEvento"
					  WHEN TMP."IndAccion" = 'S' THEN TMP."MtoEvento"
					  ELSE 0 END AS "Haber"
		FROM	#TmpPosicion TMP
		ORDER BY TMP."OrdenGrupo", TMP."OrdenItem";

	v_Resumen =
		SELECT 	-1 				AS "IdMetodo"
				,'RSM' 			AS "NombreMetodo"
				,'RSM'			AS "IdTipoReajuste"
				,0				AS "IdMesa"
				,'RSM'			AS "NomMesa"
				,0				AS "IdEvento"
				,'RSM'			AS "IdTipoProducto"
				,'RSM'			AS "IdTipoOperacion"
				,''				AS "IdTipoEvento"	
				,''				AS "NombreAgrupacion"
				,"NombreAgrupacion" AS "NombreEvento"
				,''				AS "IndAccion"
				,SUM("Debe") 	AS "Debe"
				,SUM("Haber") 	AS "Haber"
		FROM	:v_Detalle
		GROUP BY "NombreAgrupacion";

	--* AHORA SE DEJA SOLO EL QUE SE ESTA PIDIENDO...
	IF p_TipoPosicion = 'RSM' THEN
		p_out_Posicion =
			SELECT * FROM :v_Resumen;
	ELSE
		p_out_Posicion =
			SELECT * FROM :v_Detalle;
	END IF;


	DROP TABLE #TmpPosicion;

END;
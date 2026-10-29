
SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::MRF.ConsultarCarteraConsolidada";


-- 
-- Proposito .....: Crear datos para la vista de Cartera Consolidada
-- Autor .........: MSH
-- Fecha Creacion : OCtubre 2016
--
GO    
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::MRF.ConsultarCarteraConsolidada" (
    --VARIABLES DE ENTRADA OBLIGATORIAS
	IN p_Fecha		DATE,
    IN p_IdEmpresa	BIGINT,
    IN p_IdUsuario	BIGINT,
    IN p_IdModulo	NVARCHAR(3),
    IN p_IdPais		BIGINT,
 
	--* Variables del SP
	IN p_TipoCartera	VarChar(4),
	IN p_IdCliente		BigInt,
	IN p_IdPortafolio	BigInt,
	
    --* Salida SP
    OUT p_out_CConsolidada TABLE (	
			"FolioOperacion" BigInt
			,"TipoCartera" NVarChar(6)
			,"FechaOperacion" Date
			,"IdCliente" BigInt
			,"RUTCliente" NVarChar(11)
			,"RazonSocial" NVarChar(60)
			,"RelacionCorredora" NVarChar(40)
			,"FamiliaInstrumento" NVarChar(10)
			,"Nemotecnico" NVarChar(40)
			,"IdEmisor" NVarChar(40)
			,"RutEmisor" NVarChar(11)
			,"FechaVencimiento" Date 
			,"FechaVencimientoPacto" Date
			,"DiasAlVencimiento" BigInt
			,"TasaPacto" Decimal(28,6)
			,"MonedaPacto" NVarChar(03)
			,"TipoTasa" NVarChar(03)
			,"Nominal" Decimal(28,6)
			,"MonedaPapel" NVarChar(03)
			,"TasaCompraOriginal" Decimal(28,6)
			,"ValorCompraEnPesos" Decimal(28,6)
			,"ValorCompraEnMoneda" Decimal(28,6)
			,"InteresAcumulado" Decimal(28,6)
			,"ReajusteAcumulado" Decimal(28,6)
			,"ValorPresente" Decimal(28,6)
			,"Interes" Decimal(28,6)
			,"Reajuste" Decimal(28,6)
			,"ValorPresente_t1" Decimal(28,6)
			,"ValorAlVencimientoEnPesos" Decimal(28,6)
			,"ValorAlVencimientoEnMoneda" Decimal(28,6)
			,"ClasificacionRiesgo" NVarChar(10)
			,"TasaMercado" Decimal(28,6)
			,"ValorMercado" Decimal(28,6)
			,"MenorValorAlVencimiento" Decimal(28,6)
			,"MayorValorAlVencimiento" Decimal(28,6)	
			,"FechaTasaBCS" Date
			,"TasaMercadoBCS" Decimal(28,6)
			,"ValorMercadoBCS" Decimal(28,6)
			,"MenorValor" Decimal(28,6)
			,"MayorValor" Decimal(28,6)
			,"TipoPacto" NVarChar(10)
			,"FechaProximoCupon" Date 
			,"ProximoCuponNominal" Decimal(28,6)
			,"AmortizacionCupon" Decimal(28,6)
			,"InteresCupon" Decimal(28,6)
			,"NumeroCupon" BigInt
			,"FrecuenciaCupon" BigInt
			,"TieneCustodia" NVarChar(10)
			,"TipoBoveda" NVarChar(10)
			,"Fungibilidad" NVarChar(05)
			,"ClasificacionTablaIndice" NVarChar(15)
			,"TieneReserva" NVarChar(15)
			,"TipoGarantia" NVarChar(15)
			,"NemoSVS" NVarChar(40)
			,"TasaTera" Decimal(28,6)
			,"InteresDevengado" Decimal(28,6)
			,"FolioCartera" BigInt	
			,"SecCliente" Nvarchar(60)
			,"TipoOperacion" NVarChar(10)
			,"ValorLibro" Decimal(28,6)
			,"CodigoIFRS" Nvarchar(20)
			,"NombreMesa" Nvarchar(40)			
	)

) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA BSCL

READS SQL DATA WITH RESULT VIEW "BSCL.Custodia.BD.Vistas::MRF.ConsultarCarteraConsolidada" AS
BEGIN
	/* Se crea tabla dado que son más de 1 sola mesa*/
	DECLARE v_CuentaPropia  	TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT)
								DEFAULT SELECT  0  AS "IdCliente" ,0 AS "IdPortafolio"
								FROM DUMMY
								WHERE 1=2;
	DECLARE v_ContadorCuentaPropia BigInt;
	DECLARE v_CuentaPropiaTerceros BIGINT;
	DECLARE v_IdCorredora BigInt;
	DECLARE v_IdCuenta BigInt;
	DECLARE v_FechaSistema	NVARCHAR(10);
	
	
	DECLARE v_TblCartera TABLE
	(	
		"Tipo" NVarChar(10)
		,"IdTipoOperacion" NVarChar(10)
		,"FolioOperacion" BigInt
		,"TipoCartera" NVarChar(6)
		,"FechaOperacion" Date
		,"IdCliente" BigInt
		,"RUTCliente" NVarChar(11)
		,"RazonSocial" NVarChar(60)
		,"RelacionCorredora" NVarChar(40)
		,"FamiliaInstrumento" NVarChar(10)
		,"Nemotecnico" NVarChar(40)
		,"IdEmisor" NVarChar(40)
		,"RutEmisor" NVarChar(11)
		,"FechaVencimiento" Date 
		,"FechaVencimientoPacto" Date
		,"DiasAlVencimiento" BigInt
		,"TasaPacto" Decimal(28,6)
		,"MonedaPacto" NVarChar(03)
		,"TipoTasa" NVarChar(03)
		,"Nominal" Decimal(28,6)
		,"MonedaPapel" NVarChar(03)
		,"TasaCompraOriginal" Decimal(28,6)
		,"ValorCompraEnPesos" Decimal(28,6)
		,"ValorCompraEnMoneda" Decimal(28,6)
		,"InteresAcumulado" Decimal(28,6)
		,"ReajusteAcumulado" Decimal(28,6)
		,"ValorPresente" Decimal(28,6)
		,"Interes" Decimal(28,6)
		,"Reajuste" Decimal(28,6)
		,"ValorPresente_t1" Decimal(28,6)
		,"ValorAlVencimientoEnPesos" Decimal(28,6)
		,"ValorAlVencimientoEnMoneda" Decimal(28,6)
		,"ClasificacionRiesgo" NVarChar(10)
		,"TasaMercado" Decimal(28,6)
		,"ValorMercado" Decimal(28,6)
		,"MenorValorAlVencimiento" Decimal(28,6)
		,"MayorValorAlVencimiento" Decimal(28,6)	
		,"FechaTasaBCS" Date
		,"TasaMercadoBCS" Decimal(28,6)
		,"ValorMercadoBCS" Decimal(28,6)
		,"MenorValor" Decimal(28,6)
		,"MayorValor" Decimal(28,6)
		,"TipoPacto" NVarChar(10)
		,"FechaProximoCupon" Date 
		,"ProximoCuponNominal" Decimal(28,6)
		,"AmortizacionCupon" Decimal(28,6)
		,"InteresCupon" Decimal(28,6)
		,"NumeroCupon" BigInt
		,"FrecuenciaCupon" BigInt
		,"TieneCustodia" NVarChar(10)
		,"TipoBoveda" NVarChar(10)
		,"Fungibilidad" NVarChar(05)
		,"ClasificacionTablaIndice" NVarChar(15)
		,"TieneReserva" NVarChar(15)
		,"TipoGarantia" NVarChar(15)
		,"NemoSVS" NVarChar(40)
		,"TasaTera" Decimal(28,6)
		,"InteresDevengado" Decimal(28,6)
		,"FolioCartera" BigInt
		,"SecCliente" Nvarchar(60)
		,"TipoOperacion" NVarChar(10)
		,"ValorLibro" Decimal(28,6)
		,"CodigoIFRS" Nvarchar(20)
		,"NombreMesa" Nvarchar(40)		
	);
	
	
	v_TblCartera =
	SELECT	
		'' AS "Tipo"
		,'' AS "IdTipoOperacion"
		,0 AS "FolioOperacion"
		,'' AS "TipoCartera"
		,'0001-01-01' AS "FechaOperacion" --Date
		,0 AS "IdCliente"
		,'' AS "RUTCliente"
		,'' AS "RazonSocial"
		,'' AS "RelacionCorredora"
		,'' AS "FamiliaInstrumento"
		,'' AS "Nemotecnico"
		,'' AS "IdEmisor"
		,'' AS "RutEmisor"
		,'0001-01-01' AS "FechaVencimiento" 
		,'0001-01-01' AS "FechaVencimientoPacto"
		,0 AS "DiasAlVencimiento"
		,0 AS "TasaPacto"
		,'' AS "MonedaPacto"
		,'' AS "TipoTasa"
		,0 AS "Nominal"
		,'' AS "MonedaPapel"		
		,0 AS "TasaCompraOriginal"
		,0 AS "ValorCompraEnPesos"
		,0 AS "ValorCompraEnMoneda"
		,0 AS "InteresAcumulado"
		,0 AS "ReajusteAcumulado"
		,0 AS "ValorPresente"
		,0 AS "Interes"
		,0 AS "Reajuste"
		,0 AS "ValorPresente_t1"
		,0 AS "ValorAlVencimientoEnPesos"
		,0 AS "ValorAlVencimientoEnMoneda"
		,'' AS "ClasificacionRiesgo"
		,0 AS "TasaMercado"
		,0 AS "ValorMercado"
		,0 AS "MenorValorAlVencimiento"
		,0 AS "MayorValorAlVencimiento"
		,'0001-01-01' AS "FechaTasaBCS"
		,0 AS "TasaMercadoBCS"
		,0 AS "ValorMercadoBCS"
		,0 AS "MenorValor"
		,0 AS "MayorValor"
		,'' AS "TipoPacto"
		,'0001-01-01' AS "FechaProximoCupon"
		,0 AS "ProximoCuponNominal"
		,0 AS "AmortizacionCupon"
		,0 AS "InteresCupon"
		,0 AS "NumeroCupon"
		,0 AS "FrecuenciaCupon"
		,'' AS "TieneCustodia"
		,'' AS "TipoBoveda"
		,'' AS "Fungibilidad"
		,'' AS "ClasificacionTablaIndice"
		,'' AS "TieneReserva"
		,'' AS "TipoGarantia"
		,'' AS "NemoSVS"
		,0 AS "TasaTera"
		,0 AS "InteresDevengado"
		,0 AS "FolioCartera"		
		,'' AS "SecCliente"
		,'' AS "TipoOperacion"
		,0  AS "ValorLibro"
		,'' AS "CodigoIFRS"
		,'' AS "NombreMesa"
	FROM DUMMY WHERE 1=2;
	
	
	-- Obtiene Fecha de sistema	 
 	SELECT "Valor" 
 	INTO v_FechaSistema
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" = :p_IdEmpresa and "CodigoParametro" = 'PLA.FechaSistema';
	
	--* Solo cartera propia
	IF  p_IdCliente	= -1 AND p_IdPortafolio = -1 THEN
	
		v_CuentaPropia = SELECT	"IdCliente", "IdPortafolio"
		FROM 	"BSCL.Mesa.BD::MES.Mesa" 
		WHERE 	"IdTipoProducto" = 'MERF' 
				AND "IdEstadoRegistro" = 'ACT'
				Group By "IdCliente","IdPortafolio";

				
		SELECT COUNT(1) INTO v_ContadorCuentaPropia  FROM :v_CuentaPropia;
	ELSE
		Select COUNT(1) INTO v_CuentaPropiaTerceros FROM "BSCL.Mesa.BD::MES.Mesa"  Where "IdCliente" =:p_IdCliente;
		v_ContadorCuentaPropia = 0;			
	END IF;



	IF (:p_Fecha < TO_DATE(:v_FechaSistema,'YYYY-MM-DD')) THEN
	--* --------------------------------------------------------------------------------------
	--* CARTERA DISPONIBLE (TODAS Ó SOLO CORREDOR Ó 1 CLIENTE)
	--* --------------------------------------------------------------------------------------
		IF (TRIM(p_TipoCartera) = '' OR TRIM(p_TipoCartera) = 'DISP' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia = 0 THEN 
			--Otros CLientes
			v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL
			SELECT 'TERCEROS' AS "Tipo"
				,Car."IdTipoOperacion" AS "IdTipoOperacion"
				,OP."FolioOperacion" AS "FolioOperacion"
				,'DISP' AS "TipoCartera"
				,Car."FechaOperacion"
				,Car."IdCliente"
				,IDENT."DocumentoIdentidad"        AS "RUTCliente"
				,CASE PER."Tipo"
					WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
					ELSE PER."RazonSocial"
				END  AS "RazonSocial"
				,'' AS "RelacionCorredora"
				,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
				,Car."Nemotecnico"
				,Car."IdEmisor" --Modificado
				,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
					ELSE '' END AS "RutEmisor" --Modificado 
				,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
				,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
				,0 AS "DiasAlVencimiento"
				,COALESCE(OP."Tasa",0) AS "TasaPacto"
				,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
				,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
				,Car."IdTipoReajuste" AS "MonedaPapel"
				,Car."TasaCompra" AS "TasaCompraOriginal"
				,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
				,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
				,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
				,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
				,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
				,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
				,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
				,0 AS "ValorAlVencimientoEnPesos" 
				,0 AS "ValorAlVencimientoEnMoneda" 
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
				THEN Emi."ValorClasificacionRiesgoIIF" 
				ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
				,Car."INGTasaMercadoActual" AS "TasaMercado"
				,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
				,0 AS "MenorValorAlVencimiento"
				,0 AS "MayorValorAlVencimiento"	
				,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"		--* FALTAN
				,99.99 AS "TasaMercadoBCS"			--* FALTAN
				,15000000 AS "ValorMercadoBCS"		--* FALTAN
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
				,'' AS "TipoPacto"
				,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
				,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
				,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
				,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
				,Car."INGNumeroCupon" AS "NumeroCupon"
				,CI."FrecuenciaCupon"
				,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
				,Car."IdBoveda" AS "TipoBoveda"
				,Car."EsFungible" AS "Fungibilidad"
				,'' AS "ClasificacionTablaIndice" 				--* UPDATE
				,'' AS "TieneReserva"							--* UPDATE
				,'Disponible' AS "TipoGarantia"					--* UPDATE
				,Car."NemoSVS"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"		
				,Car."FolioCartera" AS "FolioCartera"
				,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'CP' ELSE 
					 CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'CP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'VP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'COMTER' AND :v_CuentaPropiaTerceros > 0 THEN Car."IdTipoOperacion" ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENTER' AND :v_CuentaPropiaTerceros > 0 THEN Car."IdTipoOperacion" ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'CP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'COMTER' AND :v_CuentaPropiaTerceros = 0 THEN 'COMTER' ELSE  
					 CASE WHEN Car."IdTipoOperacion" = 'VENTER' AND :v_CuentaPropiaTerceros = 0 THEN 'COMTER' ELSE 
					 Car."IdTipoOperacion" END END END END END END END END  AS "TipoOperacion"							--* FALTAN
				,444441 AS "ValorLibro"
				,Car."CodigoIFRS" AS "CodigoIFRS"
				,MESE."Nombre" AS "NombreMesa"
		FROM 	"BSCL.Custodia.BD::HIS.Cartera"    Car
		
				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = Car."IdPortafolio")
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESE ON (MESE."IdMesa" = Car."IdMesa")
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
				WHERE Car."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')
				AND TO_DATE(COALESCE(car."FechaRespaldo", TIMESTAMP'0001-01-01 00:00:00')) =p_Fecha AND ( ( Car."IdCliente"  = p_IdCliente AND Car."IdPortafolio" = p_IdPortafolio ) OR ( p_IdCliente = 0 AND p_IdPortafolio = 0 ))
				ORDER BY "FolioCartera"	DESC;
				 
				-- Garantias
		v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
		SELECT	'PROPIA' AS "Tipo"
				,Car."IdTipoOperacion" AS "IdTipoOperacion"
				,OP."FolioOperacion" AS "FolioOperacion"
				,'GTA' AS "TipoCartera"
				,Car."FechaOperacion"
				,Car."IdCliente"
				,IDENT."DocumentoIdentidad"        AS "RUTCliente"
				,CASE PER."Tipo" 
					WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
					ELSE PER."RazonSocial"
				END  AS "RazonSocial"
				,'' AS "RelacionCorredora"
				,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
				,Car."Nemotecnico"
				,Car."IdEmisor" --Modificado
				,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
					ELSE '' END AS "RutEmisor" --Modificado 
				,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
				,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
				,0 AS "DiasAlVencimiento"
				,COALESCE(OP."Tasa",0) AS "TasaPacto"
				,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
				,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
				,CASE WHEN Gar."IdClasificacionTipoInstrumento" = 'IF' THEN Gar."INGValorRescate" ELSE Gar."INGCantidad" END AS "Nominal"
				,Car."IdTipoReajuste" AS "MonedaPapel"
				,Car."TasaCompra" AS "TasaCompraOriginal"
				,Gar."INGMontoATasaGarantiaActual" AS "ValorCompraEnPesos"
				,Gar."INGMontoGarantiaActualMonedaInstrumento" AS "ValorCompraEnMoneda"
				,Gar."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
				,Gar."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
				,Gar."INGMontoATasaMercadoActual" AS "ValorPresente"
				,Gar."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
				,Gar."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
				,Gar."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
				,0 AS "ValorAlVencimientoEnPesos" 
				,0 AS "ValorAlVencimientoEnMoneda" 
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
				THEN Emi."ValorClasificacionRiesgoIIF" 
				ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
				,Gar."INGTasaMercadoActual" AS "TasaMercado"
				,Gar."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
				,0 AS "MenorValorAlVencimiento"
				,0 AS "MayorValorAlVencimiento"	
				,Gar."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
				,99.99 AS "TasaMercadoBCS"			--* FALTAN
				,15000000 AS "ValorMercadoBCS"		--* FALTAN
				,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" < 0 THEN ABS (Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
				,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" > 0 THEN Gar."INGMontoATasaMercadoActual" -Gar."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
				,'' AS "TipoPacto"
				,Gar."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
				,Gar."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
				,Gar."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
				,Gar."INGMontoInteresCorteCupon" AS "InteresCupon"
				,Gar."INGNumeroCupon" AS "NumeroCupon"
				,CI."FrecuenciaCupon"
				,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
				,Car."IdBoveda" AS "TipoBoveda"
				,Gar."EsFungible" AS "Fungibilidad"
				,'' AS "ClasificacionTablaIndice" 				--* UPDATE
				,'' AS "TieneReserva"							--* UPDATE
				,Gar."IdConcepto" AS "TipoGarantia"					--* UPDATE
				,Gar."NemoSVS"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
				,Gar."INGMontoInteresCompraProximoMonedaInstrumento" + Gar."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
				,Car."FolioCartera" AS "FolioCartera"
				,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
				,'GTA' AS "TipoOperacion"						--* FALTAN
				,444441 AS "ValorLibro"
				,Car."CodigoIFRS" AS "CodigoIFRS"
				,MESE."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Custodia.BD::HIS.Garantia"  Gar
				LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS  Car ON (Gar."FolioCarteraOrigen" = Car."FolioCartera" )
				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = Car."IdPortafolio")
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESE ON (MESE."IdMesa" = Car."IdMesa")
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
				WHERE /*Car."IdTipoOperacion" IN ('COMTER','COMIIF','COMRF','INGTIT') AND*/   Gar."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF') AND Gar."FechaRespaldo" = p_Fecha AND Car."FechaRespaldo" = p_Fecha
				AND ( ( Car."IdCliente"  = p_IdCliente AND Car."IdPortafolio" = p_IdPortafolio ) OR ( p_IdCliente = 0 AND p_IdPortafolio = 0 )) AND CAR."IdTipoReajuste" <>''
				
				GROUP BY Car."IdTipoOperacion",OP."FolioOperacion",Car."FechaOperacion",Car."IdCliente",Car."IdTipoInstrumento" ,Car."Nemotecnico",Car."IdEmisor",Emi."DocumentoIdentidad",Emi."DocumentoIdentidad"
				,Car."FechaVencimientoInstrumento" ,Car."FechaVencimientoOperacion" ,Car."IdTipoReajuste",Car."IdClasificacionTipoInstrumento",Gar."INGValorRescate",Gar."INGCantidad",Car."IdTipoReajuste",Car."TasaCompra",Gar."INGMontoATasaCompraActual"
				,Gar."INGMontoCompraActualMonedaInstrumento",Gar."INGMontoInteresCorridoCompraActual",Gar."INGMontoReajusteCorridoCompraActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoInteresCompraProximoMonedaInstrumento",Gar."INGMontoReajusteCompraProximoMonedaInstrumento"
				,Gar."INGMontoATasaMercadoProximo",Gar."IdClasificacionTipoInstrumento",Emi."ValorClasificacionRiesgoIIF" ,ICR."ValorClasificacionRiesgo",Gar."INGTasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo"
				,Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoProximo",Gar."INGFechaPagoCorteCupon"							
				,Gar."INGMontoCapitalCorteCupon",Gar."INGMontoCapitalCorteCupon",Gar."INGMontoInteresCorteCupon",Gar."INGNumeroCupon",CI."FrecuenciaCupon",Car."IdBoveda",Gar."EsFungible",Gar."NemoSVS",Car."FolioCartera" ,PORT."CodigoPortafolio", PORT."Proposito", Car."CodigoIFRS"
				,MESE."Nombre",CI."Tasa",Gar."INGFechaPrecioTasaMercadoActual",Gar."INGMontoATasaGarantiaActual",Gar."INGMontoGarantiaActualMonedaInstrumento",Gar."IdConcepto",Gar."INGMontoMercadoActualMonedaInstrumento" 
				,IDENT."DocumentoIdentidad", PER."Tipo", PER."Nombres", PER."ApellidoPaterno", PER."ApellidoMaterno", PER."RazonSocial"
				,OP."Tasa",OP."IdTipoReajuste"	
				
				
			ORDER BY "FolioCartera";
			
		END IF; --> IF TRIM(p_TipoCartera) = 'DISP' OR TRIM(p_TipoCartera) = 'TODAS' THEN 
	            
	
		/* Cuando Vamos por las mesas Cuentas Propias */
		IF (TRIM(p_TipoCartera) = '' OR TRIM(p_TipoCartera) = 'DISP' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia > 0 THEN
		--INSERT	INTO #TmpCartera
		v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL
			SELECT 'PROPIA' AS "Tipo"
				,Car."IdTipoOperacion" AS "IdTipoOperacion"
				,OP."FolioOperacion" AS "FolioOperacion"
				,'DISP' AS "TipoCartera"
				,Car."FechaOperacion"
				,Car."IdCliente"
				,IDENT."DocumentoIdentidad"        AS "RUTCliente"
				,CASE PER."Tipo" 
					WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
					ELSE PER."RazonSocial"
				END  AS "RazonSocial"
				,'' AS "RelacionCorredora"
				,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
				,Car."Nemotecnico"
				,Car."IdEmisor" --Modificado
				,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
					ELSE '' END AS "RutEmisor" --Modificado 
				,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
				,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
				,0 AS "DiasAlVencimiento"
				,COALESCE(OP."Tasa",0) AS "TasaPacto"
				,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
				,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
				,Car."IdTipoReajuste" AS "MonedaPapel"
				,Car."TasaCompra" AS "TasaCompraOriginal"
				,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
				,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
				,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
				,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
				,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
				,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
				,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
				,0 AS "ValorAlVencimientoEnPesos" 
				,0 AS "ValorAlVencimientoEnMoneda" 
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
				THEN Emi."ValorClasificacionRiesgoIIF" 
				ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
				,Car."INGTasaMercadoActual" AS "TasaMercado"
				,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
				,0 AS "MenorValorAlVencimiento"
				,0 AS "MayorValorAlVencimiento"	
				,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
				,99.99 AS "TasaMercadoBCS"			--* FALTAN
				,15000000 AS "ValorMercadoBCS"		--* FALTAN
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
				,'' AS "TipoPacto"
				,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
				,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
				,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
				,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
				,Car."INGNumeroCupon" AS "NumeroCupon"
				,CI."FrecuenciaCupon"
				,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
				,Car."IdBoveda" AS "TipoBoveda"
				,Car."EsFungible" AS "Fungibilidad"
				,'' AS "ClasificacionTablaIndice" 				--* UPDATE
				,'' AS "TieneReserva"							--* UPDATE
				,'Disponible' AS "TipoGarantia"					--* UPDATE
				,Car."NemoSVS"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"		
				,Car."FolioCartera" AS "FolioCartera"
				,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE
				 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' THEN 'VP' ELSE Car."IdTipoOperacion" END END AS "TipoOperacion"						--* FALTAN
				,444441 AS "ValorLibro"
				,Car."CodigoIFRS" AS "CodigoIFRS"
				,MESE."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Custodia.BD::HIS.Cartera"    Car
				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
				INNER JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON Car."IdCliente" = MES."IdCliente" AND Car."IdPortafolio" = MES."IdPortafolio" AND MES."IdTipoProducto" = 'MERF'
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = MES."IdPortafolio")
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESE ON (MESE."IdMesa" = Car."IdMesa")
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
				WHERE /*Car."IdTipoOperacion" IN ('COMTER','COMIIF','COMRF','INGTIT') AND */  Car."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')
				AND Car."FechaRespaldo" =p_Fecha AND CAR."IdTipoReajuste" <>''
				GROUP BY Car."IdTipoOperacion",OP."FolioOperacion", Car."FechaOperacion",Car."IdCliente",Car."IdTipoInstrumento" ,Car."Nemotecnico",Car."IdEmisor",Emi."DocumentoIdentidad",Emi."DocumentoIdentidad"
				,Car."FechaVencimientoInstrumento" ,Car."FechaVencimientoOperacion" ,Car."IdTipoReajuste",Car."IdClasificacionTipoInstrumento",Car."INGValorRescate",Car."INGCantidad",Car."IdTipoReajuste",Car."TasaCompra",Car."INGMontoATasaCompraActual"
				,Car."INGMontoCompraActualMonedaInstrumento",Car."INGMontoInteresCorridoCompraActual",Car."INGMontoReajusteCorridoCompraActual",Car."INGMontoATasaMercadoActual",Car."INGMontoInteresCompraProximoMonedaInstrumento",Car."INGMontoReajusteCompraProximoMonedaInstrumento"
				,Car."INGMontoATasaMercadoProximo",Car."IdClasificacionTipoInstrumento",Emi."ValorClasificacionRiesgoIIF" ,ICR."ValorClasificacionRiesgo",Car."INGTasaMercadoActual",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo"
				,Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGFechaPagoCorteCupon"			
				,Car."INGMontoCapitalCorteCupon",Car."INGMontoCapitalCorteCupon",Car."INGMontoInteresCorteCupon",Car."INGNumeroCupon",CI."FrecuenciaCupon",Car."IdBoveda",Car."EsFungible",Car."NemoSVS",Car."INGMontoDevengoTipoCambio",Car."FolioCartera" ,PORT."CodigoPortafolio", PORT."Proposito",Car."CodigoIFRS"				
				,MESE."Nombre",CI."Tasa",CAR."INGFechaPrecioTasaMercadoActual",Car."INGMontoMercadoActualMonedaInstrumento",OP."Tasa",OP."IdTipoReajuste"				
				,IDENT."DocumentoIdentidad", PER."Tipo", PER."Nombres", PER."ApellidoPaterno", PER."ApellidoMaterno", PER."RazonSocial"
				ORDER BY "FolioCartera" DESC;
				
				-- Garantias
		----INSERT	INTO #TmpCartera
		v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL
		SELECT	'PROPIA' AS "Tipo"
				,Car."IdTipoOperacion" AS "IdTipoOperacion"
				,OP."FolioOperacion" AS "FolioOperacion"
				,'GTA' AS "TipoCartera"
				,Car."FechaOperacion"
				,Car."IdCliente"
				,IDENT."DocumentoIdentidad"        AS "RUTCliente"
				,CASE PER."Tipo" 
					WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
					ELSE PER."RazonSocial"
				END  AS "RazonSocial"
				,'' AS "RelacionCorredora"
				,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
				,Car."Nemotecnico"
				,Car."IdEmisor" --Modificado
				,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
					ELSE '' END AS "RutEmisor" --Modificado 
				,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
				,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
				,0 AS "DiasAlVencimiento"
				,COALESCE(OP."Tasa",0) AS "TasaPacto"
				,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
				,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
				,CASE WHEN Gar."IdClasificacionTipoInstrumento" = 'IF' THEN Gar."INGValorRescate" ELSE Gar."INGCantidad" END AS "Nominal"
				,Car."IdTipoReajuste" AS "MonedaPapel"
				,Car."TasaCompra" AS "TasaCompraOriginal"
				,Gar."INGMontoATasaGarantiaActual" AS "ValorCompraEnPesos"
				,Gar."INGMontoGarantiaActualMonedaInstrumento" AS "ValorCompraEnMoneda"
				,Gar."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
				,Gar."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
				,Gar."INGMontoATasaMercadoActual" AS "ValorPresente"
				,Gar."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
				,Gar."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
				,Gar."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
				,0 AS "ValorAlVencimientoEnPesos" 
				,0 AS "ValorAlVencimientoEnMoneda" 
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
				THEN Emi."ValorClasificacionRiesgoIIF" 
				ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
				,Gar."INGTasaMercadoActual" AS "TasaMercado"
				,Gar."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
				,0 AS "MenorValorAlVencimiento"
				,0 AS "MayorValorAlVencimiento"	
				,Gar."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
				,99.99 AS "TasaMercadoBCS"			--* FALTAN
				,15000000 AS "ValorMercadoBCS"		--* FALTAN
				,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" < 0 THEN ABS (Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
				,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" > 0 THEN Gar."INGMontoATasaMercadoActual" -Gar."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
				,'' AS "TipoPacto"
				,Gar."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
				,Gar."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
				,Gar."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
				,Gar."INGMontoInteresCorteCupon" AS "InteresCupon"
				,Gar."INGNumeroCupon" AS "NumeroCupon"
				,CI."FrecuenciaCupon"
				,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
				,Car."IdBoveda" AS "TipoBoveda"
				,Gar."EsFungible" AS "Fungibilidad"
				,'' AS "ClasificacionTablaIndice" 				--* UPDATE
				,'' AS "TieneReserva"							--* UPDATE
				,Gar."IdConcepto" AS "TipoGarantia"					--* UPDATE
				,Gar."NemoSVS"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
				,Gar."INGMontoInteresCompraProximoMonedaInstrumento" + Gar."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
				,Car."FolioCartera" AS "FolioCartera"
				,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
				,'GTA' AS "TipoOperacion"						--* FALTAN
				,444441 AS "ValorLibro"
				,Car."CodigoIFRS" AS "CodigoIFRS"
				,MESE."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Custodia.BD::HIS.Garantia"  Gar
				LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS  Car ON (Gar."FolioCarteraOrigen" = Car."FolioCartera" )
				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
				INNER JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON Car."IdCliente" = MES."IdCliente" AND Car."IdPortafolio" = MES."IdPortafolio" AND MES."IdTipoProducto" = 'MERF'
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = Car."IdPortafolio")
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESE ON (MESE."IdMesa" = Car."IdMesa")
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
				WHERE /*Car."IdTipoOperacion" IN ('COMTER','COMIIF','COMRF','INGTIT') AND*/   Gar."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF') AND Gar."FechaRespaldo" = p_Fecha AND Car."FechaRespaldo" = p_Fecha
				AND Car."IdTipoReajuste" <> ''
				GROUP BY Car."IdTipoOperacion",OP."FolioOperacion",Car."FechaOperacion",Car."IdCliente",Car."IdTipoInstrumento" ,Car."Nemotecnico",Car."IdEmisor",Emi."DocumentoIdentidad",Emi."DocumentoIdentidad"
				,Car."FechaVencimientoInstrumento" ,Car."FechaVencimientoOperacion" ,Car."IdTipoReajuste",Car."IdClasificacionTipoInstrumento",Gar."INGValorRescate",Gar."INGCantidad",Car."IdTipoReajuste",Car."TasaCompra",Gar."INGMontoATasaCompraActual"
				,Gar."INGMontoCompraActualMonedaInstrumento",Gar."INGMontoInteresCorridoCompraActual",Gar."INGMontoReajusteCorridoCompraActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoInteresCompraProximoMonedaInstrumento",Gar."INGMontoReajusteCompraProximoMonedaInstrumento"
				,Gar."INGMontoATasaMercadoProximo",Gar."IdClasificacionTipoInstrumento",Emi."ValorClasificacionRiesgoIIF" ,ICR."ValorClasificacionRiesgo",Gar."INGTasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo"
				,Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoProximo",Gar."INGFechaPagoCorteCupon"							
				,Gar."INGMontoCapitalCorteCupon",Gar."INGMontoCapitalCorteCupon",Gar."INGMontoInteresCorteCupon",Gar."INGNumeroCupon",CI."FrecuenciaCupon",Car."IdBoveda",Gar."EsFungible",Gar."NemoSVS",Car."FolioCartera" ,PORT."CodigoPortafolio", PORT."Proposito",Car."CodigoIFRS"
				,MESE."Nombre",CI."Tasa",Gar."INGFechaPrecioTasaMercadoActual",Gar."INGMontoATasaGarantiaActual",Gar."INGMontoGarantiaActualMonedaInstrumento",Gar."IdConcepto",Gar."INGMontoMercadoActualMonedaInstrumento",OP."Tasa",OP."IdTipoReajuste"				
				,IDENT."DocumentoIdentidad", PER."Tipo", PER."Nombres", PER."ApellidoPaterno", PER."ApellidoMaterno", PER."RazonSocial"
				
				 
			ORDER BY "FolioCartera";
					
			
		END IF; 
	ELSE
		IF (TRIM(p_TipoCartera) = '' OR TRIM(p_TipoCartera) = 'DISP' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia = 0 THEN
			IF :p_IdCLiente = 0 THEN
			/* 	INSERT	INTO #TmpCartera
			SELECT	'PROPIA' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,'DISP' AS "TipoCartera"
					,Car."FechaOperacion"
					,Car."IdCliente"
					,Cli."DocumentoIdentidad" AS "RUTCliente"
					,CASE WHEN Cli."RazonSocial" = '' THEN Cli."NombreCompleto" ELSE Cli."RazonSocial" END AS "RazonSocial"
					,'' AS "RelacionCorredora"
					,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN CCS."INGValorRescate" ELSE CCS."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					--,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					,CCS."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					--,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,CCS."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,CCS."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,CCS."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,0 AS "ValorAlVencimientoEnPesos" 
					,0 AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Car."INGTasaMercadoActual" AS "TasaMercado"
					--,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,CCS."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN CCS."INGMontoATasaMercadoActual" - CCS."INGMontoATasaMercadoProximo" < 0 THEN ABS (CCS."INGMontoATasaMercadoActual" - CCS."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN CCS."INGMontoATasaMercadoActual" - CCS."INGMontoATasaMercadoProximo" > 0 THEN CCS."INGMontoATasaMercadoActual" -CCS."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,'' AS "TipoPacto"
					,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Car."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Car."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,'Disponible' AS "TipoGarantia"					--* UPDATE
					,Car."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"		
					,Car."FolioCartera" AS "FolioCartera"
					,PORT."CodigoPropositoPortafolio" AS "SecCliente"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE
				 	 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' THEN 'VP' ELSE Car."IdTipoOperacion" END END AS "TipoOperacion"							--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MES."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Custodia.BD::CUS.Cartera"   Car
					LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CCS ON (Car."FolioCartera" = CCS."FolioCartera")
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
					INNER JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON MES."IdMesa" = Car."IdMesa" AND  Car."IdCliente" = MES."IdCliente" AND Car."IdPortafolio" = MES."IdPortafolio" AND MES."IdTipoProducto" = 'MERF'
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS PORT ON PORT."IdPortafolio" = MES."IdPortafolio"
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON CLI."IdCliente" = PORT."IdCliente"
					LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarClasificacionCartera" AS CatIFRS ON (Car."CodigoIFRS" = CatIFRS."CodigoCarteraIFRS")
					--LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa"  )
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
					WHERE Car."IdTipoOperacion" NOT IN ('VENPAC','VENTER') AND  Car."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')
					AND CAR."IdTipoReajuste" <> ''
				GROUP BY Car."IdTipoOperacion",OP."FolioOperacion" ,Car."FechaOperacion",Car."IdCliente",Cli."DocumentoIdentidad",Cli."RazonSocial",Car."IdTipoInstrumento" ,Car."Nemotecnico",Car."IdEmisor",Emi."DocumentoIdentidad",Emi."DocumentoIdentidad"
				,Car."FechaVencimientoInstrumento" ,Car."FechaVencimientoOperacion" ,Car."IdTipoReajuste",Car."IdClasificacionTipoInstrumento",CCS."INGValorRescate",CCS."INGCantidad",Car."IdTipoReajuste",Car."TasaCompra",CCS."INGMontoATasaCompraActual"
				,CCS."INGMontoCompraActualMonedaInstrumento",Car."INGMontoInteresCorridoCompraActual",Car."INGMontoReajusteCorridoCompraActual",CCS."INGMontoATasaMercadoActual",Car."INGMontoInteresCompraProximoMonedaInstrumento",Car."INGMontoReajusteCompraProximoMonedaInstrumento"
				,CCS."INGMontoATasaMercadoProximo",Car."IdClasificacionTipoInstrumento",Emi."ValorClasificacionRiesgoIIF" ,ICR."ValorClasificacionRiesgo",Car."INGTasaMercadoActual",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo"
				,Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGFechaPagoCorteCupon"
				,Car."INGMontoCapitalCorteCupon",Car."INGMontoCapitalCorteCupon",Car."INGMontoInteresCorteCupon",Car."INGNumeroCupon",CI."FrecuenciaCupon",Car."IdBoveda",Car."EsFungible",Car."NemoSVS",Car."INGMontoDevengoTipoCambio",Car."FolioCartera" ,PORT."CodigoPropositoPortafolio",Car."CodigoIFRS"			
				,Cli."NombreCompleto",MES."Nombre",CI."Tasa",CAR."INGFechaPrecioTasaMercadoActual",CCS."INGMontoMercadoActualMonedaInstrumento",OP."Tasa",OP."IdTipoReajuste"	
			ORDER BY "FolioCartera";
			 
			 */
			END IF;
			--Otros Clientes
			v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL
			SELECT	'TERCEROS' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,'DISP' AS "TipoCartera"
					,Car."FechaOperacion"
					,Car."IdCliente"
					,IDENT."DocumentoIdentidad"        AS "RUTCliente"
					,CASE PER."Tipo" 
						WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
						ELSE PER."RazonSocial"
					END  AS "RazonSocial"
					,'' AS "RelacionCorredora"
					,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN CCS."INGValorRescate" ELSE CCS."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					--,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					,CCS."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					--,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,CCS."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,CCS."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,CCS."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,0 AS "ValorAlVencimientoEnPesos" 
					,0 AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Car."INGTasaMercadoActual" AS "TasaMercado"
					--,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,CCS."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN CCS."INGMontoATasaMercadoActual" - CCS."INGMontoATasaMercadoProximo" < 0 THEN ABS (CCS."INGMontoATasaMercadoActual" - CCS."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN CCS."INGMontoATasaMercadoActual" - CCS."INGMontoATasaMercadoProximo" > 0 THEN CCS."INGMontoATasaMercadoActual" -CCS."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,'' AS "TipoPacto"
					,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Car."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Car."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,'Disponible' AS "TipoGarantia"					--* UPDATE
					,Car."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"		
					,Car."FolioCartera" AS "FolioCartera"
					,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
					
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'CP' ELSE 
					 CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'CP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'VP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'COMTER' AND :v_CuentaPropiaTerceros > 0 THEN Car."IdTipoOperacion" ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENTER' AND :v_CuentaPropiaTerceros > 0 THEN Car."IdTipoOperacion" ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'CP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'COMTER' AND :v_CuentaPropiaTerceros = 0 THEN 'COMTER' ELSE  
					 CASE WHEN Car."IdTipoOperacion" = 'VENTER' AND :v_CuentaPropiaTerceros = 0 THEN 'COMTER' ELSE 
					 Car."IdTipoOperacion" END END END END END END END END  AS "TipoOperacion"							--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MES."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Custodia.BD::CUS.Cartera"   Car
					LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CCS ON (Car."FolioCartera" = CCS."FolioCartera")
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento" 
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = Car."IdPortafolio")
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa")
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
					WHERE /*Car."IdTipoOperacion" NOT IN ('COMTER','COMPAC') AND MES."IdCliente" <> Cli."IdCliente" AND*/  Car."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')
					AND ( 	(  Car."IdCliente" = p_IdCliente AND 
					Car."IdPortafolio" = p_IdPortafolio ) 
					OR ( p_IdCliente = 0 AND p_IdPortafolio = 0 ))
			ORDER BY "FolioCartera";
			
			
			--Garantias
			v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL
			SELECT	'TERCEROS' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,'GTA' AS "TipoCartera"
					,Car."FechaOperacion"
					,Car."IdCliente"
					,IDENT."DocumentoIdentidad"        AS "RUTCliente"
					,CASE PER."Tipo" 
						WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
						ELSE PER."RazonSocial"
					END  AS "RazonSocial"
					,'' AS "RelacionCorredora"
					,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"	
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Gar."IdClasificacionTipoInstrumento" = 'IF' THEN Gar."INGValorRescate" ELSE Gar."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					,Gar."INGMontoATasaGarantiaActual" AS "ValorCompraEnPesos"
					,Gar."INGMontoGarantiaActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Gar."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Gar."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,Gar."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Gar."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Gar."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,Gar."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,0 AS "ValorAlVencimientoEnPesos" 
					,0 AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Gar."INGTasaMercadoActual" AS "TasaMercado"
					,Gar."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Gar."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" < 0 THEN ABS (Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" > 0 THEN Gar."INGMontoATasaMercadoActual" -Gar."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,'' AS "TipoPacto"
					,Gar."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Gar."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Gar."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Gar."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Gar."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Gar."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,Gar."IdConcepto" AS "TipoGarantia"					--* UPDATE
					,Gar."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
					,Gar."INGMontoInteresCompraProximoMonedaInstrumento" + Gar."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
					,Car."FolioCartera" AS "FolioCartera"
					,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
					
					,'GTA' AS "TipoOperacion"						--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MESE."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Custodia.BD::GTA.Garantia"  Gar
					LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS  Car ON (Gar."FolioCarteraOrigen" = Car."FolioCartera" )
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
					INNER JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON Car."IdCliente" = MES."IdCliente" AND Car."IdPortafolio" = MES."IdPortafolio" AND MES."IdTipoProducto" = 'MERF' 
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = Car."IdPortafolio")
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESE ON (MESE."IdMesa" = Car."IdMesa")
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
					WHERE /*Car."IdTipoOperacion" IN ('COMTER','COMIIF','COMRF','INGTIT') AND  */ Gar."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')
					AND ( 	( Car."IdCliente"  = p_IdCliente AND Car."IdPortafolio" = p_IdPortafolio ) OR ( p_IdCliente = 0 AND p_IdPortafolio = 0 )) AND Car."IdTipoReajuste" <> '' AND Gar."IdTipoReajuste" <> ''
					GROUP BY Car."IdTipoOperacion",OP."FolioOperacion" ,Car."FechaOperacion",Car."IdCliente",Car."IdTipoInstrumento" ,Car."Nemotecnico",Car."IdEmisor",Emi."DocumentoIdentidad",Emi."DocumentoIdentidad"
					,Car."FechaVencimientoInstrumento" ,Car."FechaVencimientoOperacion" ,Car."IdTipoReajuste",Car."IdClasificacionTipoInstrumento",Gar."INGValorRescate",Gar."INGCantidad",Car."IdTipoReajuste",Car."TasaCompra",Gar."INGMontoATasaCompraActual"
					,Gar."INGMontoCompraActualMonedaInstrumento",Gar."INGMontoInteresCorridoCompraActual",Gar."INGMontoReajusteCorridoCompraActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoInteresCompraProximoMonedaInstrumento",Gar."INGMontoReajusteCompraProximoMonedaInstrumento"
					,Gar."INGMontoATasaMercadoProximo",Gar."IdClasificacionTipoInstrumento",Emi."ValorClasificacionRiesgoIIF" ,ICR."ValorClasificacionRiesgo",Gar."INGTasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo"
					,Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoProximo",Gar."INGFechaPagoCorteCupon"
					,Gar."INGMontoCapitalCorteCupon",Gar."INGMontoCapitalCorteCupon",Gar."INGMontoInteresCorteCupon",Gar."INGNumeroCupon",CI."FrecuenciaCupon",Car."IdBoveda",Gar."EsFungible",Gar."NemoSVS",Car."FolioCartera" , PORT."CodigoPortafolio", PORT."Proposito", Car."CodigoIFRS"
					,MESE."Nombre",CI."Tasa",Gar."INGFechaPrecioTasaMercadoActual",Gar."INGMontoATasaGarantiaActual",Gar."INGMontoGarantiaActualMonedaInstrumento",Gar."IdConcepto",Gar."INGMontoMercadoActualMonedaInstrumento",OP."Tasa",OP."IdTipoReajuste"
					,IDENT."DocumentoIdentidad", PER."Tipo", PER."Nombres", PER."ApellidoPaterno", PER."ApellidoMaterno", PER."RazonSocial"
					
				ORDER BY "FolioCartera";	
			
		END IF; --> IF TRIM(p_TipoCartera) = 'DISP' OR TRIM(p_TipoCartera) = 'TODAS' THEN 
	
		/* Cuando Vamos por las mesas Cuentas Propias */
		IF (TRIM(p_TipoCartera) = '' OR TRIM(p_TipoCartera) = 'DISP' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia > 0 THEN 
		--Select * FROM :v_CuentaPropia; -- Verificamos contenido
		
		v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL
			SELECT	'PROPIA' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
				,OP."FolioOperacion" AS "FolioOperacion"
				,'DISP' AS "TipoCartera"
				,Car."FechaOperacion"
				,Car."IdCliente"
				,IDENT."DocumentoIdentidad"        AS "RUTCliente"
				,CASE PER."Tipo" 
					WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
					ELSE PER."RazonSocial"
				END  AS "RazonSocial"
				,'' AS "RelacionCorredora"
				,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
				,Car."Nemotecnico"
				,Car."IdEmisor" --Modificado
				,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
					ELSE '' END AS "RutEmisor" --Modificado 
				,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
				,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
				,0 AS "DiasAlVencimiento"
				,COALESCE(OP."Tasa",0) AS "TasaPacto"
				,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
				,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN CCS."INGValorRescate" ELSE CCS."INGCantidad" END AS "Nominal"
				,Car."IdTipoReajuste" AS "MonedaPapel"
				,Car."TasaCompra" AS "TasaCompraOriginal"
				,CCS."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
				,CCS."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
				,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
				,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
				,CCS."INGMontoATasaMercadoActual" AS "ValorPresente"
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
				,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
				,CCS."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
				,0 AS "ValorAlVencimientoEnPesos" 
				,0 AS "ValorAlVencimientoEnMoneda" 
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
				THEN Emi."ValorClasificacionRiesgoIIF" 
				ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
				,Car."INGTasaMercadoActual" AS "TasaMercado"
				,CCS."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
				,0 AS "MenorValorAlVencimiento"
				,0 AS "MayorValorAlVencimiento"	
				,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
				,99.99 AS "TasaMercadoBCS"			--* FALTAN
				,15000000 AS "ValorMercadoBCS"		--* FALTAN
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
				,'' AS "TipoPacto"
				,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
				,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
				,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
				,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
				,Car."INGNumeroCupon" AS "NumeroCupon"
				,CI."FrecuenciaCupon"
				,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
				,Car."IdBoveda" AS "TipoBoveda"
				,Car."EsFungible" AS "Fungibilidad"
				,'' AS "ClasificacionTablaIndice" 				--* UPDATE
				,'' AS "TieneReserva"							--* UPDATE
				,'Disponible' AS "TipoGarantia"					--* UPDATE
				,Car."NemoSVS"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
				,Car."FolioCartera" AS "FolioCartera"
				,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE
				 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' THEN 'VP' ELSE Car."IdTipoOperacion" END END AS "TipoOperacion"						--* FALTAN
				,444441 AS "ValorLibro"
				,Car."CodigoIFRS" AS "CodigoIFRS"
				,MESE."Nombre" AS "NombreMesa"
		FROM 	"BSCL.Custodia.BD::CUS.Cartera"   Car
				LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CCS ON (Car."FolioCartera" = CCS."FolioCartera")
				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
				INNER JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON Car."IdCliente" = MES."IdCliente" AND Car."IdPortafolio" = MES."IdPortafolio" AND MES."IdTipoProducto" = 'MERF'				
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = Car."IdPortafolio")
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESE ON (MESE."IdMesa" = Car."IdMesa")
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
				WHERE /*Car."IdTipoOperacion" IN ('COMTER','COMIIF','COMRF','INGTIT') AND*/   Car."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF') AND CAR."IdTipoReajuste" <> ''
				GROUP BY Car."IdTipoOperacion",OP."FolioOperacion" ,Car."FechaOperacion",Car."IdCliente",Car."IdTipoInstrumento" ,Car."Nemotecnico",Car."IdEmisor",Emi."DocumentoIdentidad",Emi."DocumentoIdentidad"
				,Car."FechaVencimientoInstrumento" ,Car."FechaVencimientoOperacion" ,Car."IdTipoReajuste",Car."IdClasificacionTipoInstrumento",CCS."INGValorRescate",CCS."INGCantidad",Car."IdTipoReajuste",Car."TasaCompra",CCS."INGMontoATasaCompraActual"
				,CCS."INGMontoCompraActualMonedaInstrumento",Car."INGMontoInteresCorridoCompraActual",Car."INGMontoReajusteCorridoCompraActual",CCS."INGMontoATasaMercadoActual",Car."INGMontoInteresCompraProximoMonedaInstrumento",Car."INGMontoReajusteCompraProximoMonedaInstrumento"
				,CCS."INGMontoATasaMercadoProximo",Car."IdClasificacionTipoInstrumento",Emi."ValorClasificacionRiesgoIIF" ,ICR."ValorClasificacionRiesgo",Car."INGTasaMercadoActual",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo"
				,Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGMontoATasaMercadoActual",Car."INGMontoATasaMercadoProximo",Car."INGFechaPagoCorteCupon"				
				,Car."INGMontoCapitalCorteCupon",Car."INGMontoCapitalCorteCupon",Car."INGMontoInteresCorteCupon",Car."INGNumeroCupon",CI."FrecuenciaCupon",Car."IdBoveda",Car."EsFungible",Car."NemoSVS",Car."INGMontoDevengoTipoCambio",Car."FolioCartera" , PORT."CodigoPortafolio", PORT."Proposito",Car."CodigoIFRS"				
				,MESE."Nombre",CI."Tasa",CAR."INGFechaPrecioTasaMercadoActual",CCS."INGMontoMercadoActualMonedaInstrumento",OP."Tasa",OP."IdTipoReajuste"
				,IDENT."DocumentoIdentidad", PER."Tipo", PER."Nombres", PER."ApellidoPaterno", PER."ApellidoMaterno", PER."RazonSocial"
				
			ORDER BY "FolioCartera";
			
			--Garantias
			v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
			SELECT	'PROPIA' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,'GTA' AS "TipoCartera"
					,Car."FechaOperacion"
					,Car."IdCliente"
					,IDENT."DocumentoIdentidad"        AS "RUTCliente"
					,CASE PER."Tipo" 
						WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
						ELSE PER."RazonSocial"
					END  AS "RazonSocial"
					,'' AS "RelacionCorredora"
					,Car."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Gar."IdClasificacionTipoInstrumento" = 'IF' THEN Gar."INGValorRescate" ELSE Gar."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					,Gar."INGMontoATasaGarantiaActual" AS "ValorCompraEnPesos"
					,Gar."INGMontoGarantiaActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Gar."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Gar."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,Gar."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Gar."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Gar."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,Gar."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,0 AS "ValorAlVencimientoEnPesos" 
					,0 AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Gar."INGTasaMercadoActual" AS "TasaMercado"
					,Gar."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Gar."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" < 0 THEN ABS (Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN Gar."INGMontoATasaMercadoActual" - Gar."INGMontoATasaMercadoProximo" > 0 THEN Gar."INGMontoATasaMercadoActual" -Gar."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,'' AS "TipoPacto"
					,Gar."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Gar."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Gar."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Gar."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Gar."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Gar."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,Gar."IdConcepto" AS "TipoGarantia"					--* UPDATE
					,Gar."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
					,Gar."INGMontoInteresCompraProximoMonedaInstrumento" + Gar."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
					,Car."FolioCartera" AS "FolioCartera"
					,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
					,'GTA' AS "TipoOperacion"						--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MESE."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Custodia.BD::GTA.Garantia"  Gar
					LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS  Car ON (Gar."FolioCarteraOrigen" = Car."FolioCartera" )
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON Car."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = Car."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = Car."IdInstrumento"
					INNER JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON Car."IdCliente" = MES."IdCliente" AND Car."IdPortafolio" = MES."IdPortafolio" AND MES."IdTipoProducto" = 'MERF'					
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = Car."IdPortafolio")					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MESE ON (MESE."IdMesa" = Car."IdMesa")
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON (OP."IdOperacion" = CAR."FolioComprobante")
					WHERE/* Car."IdTipoOperacion" IN ('COMTER','COMIIF','COMRF','INGTIT')AND*/    Gar."IdEstado" = 'VIG' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')
					AND Car."IdTipoReajuste" <> '' AND Gar."IdTipoReajuste" <> ''
					GROUP BY Car."IdTipoOperacion",OP."FolioOperacion" ,Car."FechaOperacion",Car."IdCliente",Car."IdTipoInstrumento" ,Car."Nemotecnico",Car."IdEmisor",Emi."DocumentoIdentidad",Emi."DocumentoIdentidad"
					,Car."FechaVencimientoInstrumento" ,Car."FechaVencimientoOperacion" ,Car."IdTipoReajuste",Car."IdClasificacionTipoInstrumento",Gar."INGValorRescate",Gar."INGCantidad",Car."IdTipoReajuste",Car."TasaCompra",Gar."INGMontoATasaCompraActual"
					,Gar."INGMontoCompraActualMonedaInstrumento",Gar."INGMontoInteresCorridoCompraActual",Gar."INGMontoReajusteCorridoCompraActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoInteresCompraProximoMonedaInstrumento",Gar."INGMontoReajusteCompraProximoMonedaInstrumento"
					,Gar."INGMontoATasaMercadoProximo",Gar."IdClasificacionTipoInstrumento",Emi."ValorClasificacionRiesgoIIF" ,ICR."ValorClasificacionRiesgo",Gar."INGTasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo"
					,Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoActual",Gar."INGMontoATasaMercadoProximo",Gar."INGMontoATasaMercadoProximo",Gar."INGFechaPagoCorteCupon"					
					,Gar."INGMontoCapitalCorteCupon",Gar."INGMontoCapitalCorteCupon",Gar."INGMontoInteresCorteCupon",Gar."INGNumeroCupon",CI."FrecuenciaCupon",Car."IdBoveda",Gar."EsFungible",Gar."NemoSVS",Car."FolioCartera" , PORT."CodigoPortafolio", PORT."Proposito",Car."CodigoIFRS"					
					,MESE."Nombre",CI."Tasa",Gar."INGFechaPrecioTasaMercadoActual",Gar."INGMontoATasaGarantiaActual",Gar."INGMontoGarantiaActualMonedaInstrumento",Gar."IdConcepto",Gar."INGMontoMercadoActualMonedaInstrumento",OP."Tasa",OP."IdTipoReajuste"					
					,IDENT."DocumentoIdentidad", PER."Tipo", PER."Nombres", PER."ApellidoPaterno", PER."ApellidoMaterno", PER."RazonSocial"
 					
					
				ORDER BY "FolioCartera";		
			
		END IF; 
	END IF;
	--*
	--* ------------------------------------------------------------------------------------------------
	--* ------------------------------------------------------------------------------------------------
	--*
		IF (TRIM(p_TipoCartera) = 'DRC' OR TRIM(p_TipoCartera) = 'DRV' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia = 0 THEN 
		
		--Cuenta Propia
		IF :p_IdCliente =0 THEN
				v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
				SELECT	'PROPIA' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'DRV' ELSE 'DRC' END AS "TipoCartera"
					,Car."FechaOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" ELSE Car."IdClienteRelacionado" END AS "IdCliente"--Car."IdCliente"
					,IDENT."DocumentoIdentidad"        AS "RUTCliente"
					,CASE PER."Tipo" 
						WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
						ELSE PER."RazonSocial"
					END  AS "RazonSocial"

					,'' AS "RelacionCorredora"
					,RE."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,OP."MontoOperacionFinal" AS "ValorAlVencimientoEnPesos"--(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoATasaCompraActual" AS "ValorAlVencimientoEnPesos" 
					,OP."MontoOperacionFinalMoneda" AS "ValorAlVencimientoEnMoneda" --(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoCompraActualMonedaInstrumento" AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Car."INGTasaMercadoActual" AS "TasaMercado"
					,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END   AS "TipoPacto"
					,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Car."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Car."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,'Disponible' AS "TipoGarantia"					--* UPDATE
					,Car."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
					,RE."FolioCarteraOrigen" AS "FolioCartera"
					,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END  AS "TipoOperacion"						--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MES."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
					--INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  Car ON (OP."IdOperacion" = Car."FolioComprobante" AND RE."IdCarteraCustodia" = Car."IdCartera" ) --AND  Car."EGRIdConcepto" = '' /*AND RE."IdCarteraCustodia" = Car."IdCartera"*/)
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  CAR ON ( RE."FolioCarteraCustodia"  = CAR."FolioCartera")
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio" ELSE Car."IdPortafolioRelacionado" END)
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa")
					WHERE  (OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND Car."IdTipoOperacion" IN ('COMPAC','VENPAC')) AND   OP."IdEstado" <> 'PEN' AND OP."IdEstado" <> 'ANU' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')  AND 
						((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
						(TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') < TO_VARCHAR(OP."FechaLiquidacion",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
						TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD') 
			ORDER BY "FolioCartera" DESC;	
		END IF;
		--Otros Clientes
		v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
			SELECT	'TERCEROS' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'DRV' ELSE 'DRC' END AS "TipoCartera"--El cliente Se voltea EJ: Mesa Compra Pacto(ORC) -> Cliente Venta Pacto (ORV)
					,Car."FechaOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" ELSE Car."IdClienteRelacionado" END AS "IdCliente"
					,IDENT."DocumentoIdentidad"        AS "RUTCliente"
					,CASE PER."Tipo" 
						WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
						ELSE PER."RazonSocial"
					END  AS "RazonSocial"
					,'' AS "RelacionCorredora"
					,RE."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,OP."MontoOperacionFinal" AS "ValorAlVencimientoEnPesos"--(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoATasaCompraActual" AS "ValorAlVencimientoEnPesos" 
					,OP."MontoOperacionFinalMoneda" AS "ValorAlVencimientoEnMoneda" --(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoCompraActualMonedaInstrumento" AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Car."INGTasaMercadoActual" AS "TasaMercado"
					,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'CP' ELSE 
					 CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'VP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'VP' ELSE 'CP' END END END    AS "TipoPacto"
					,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Car."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Car."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,'Disponible' AS "TipoGarantia"					--* UPDATE
					,Car."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"										--* FALTAN
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
					,Car."FolioCartera" AS "FolioCartera"
					,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'CP' ELSE 
					 CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'VP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'VP' ELSE 'CP' END END END  AS "TipoOperacion"							--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MES."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  Car ON (OP."IdOperacion" = Car."FolioComprobante" AND RE."IdCarteraCustodia" = Car."IdCartera" ) --AND  Car."EGRIdConcepto" = '' /*AND RE."IdCarteraCustodia" = Car."IdCartera")*/
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" =  CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio"
					ELSE CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'VENPAC' THEN Car."IdPortafolioRelacionado" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros = 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolioRelacionado"
					ELSE Car."IdPortafolio" END END END )
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa")
					WHERE (OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND Car."IdTipoOperacion" IN ('COMPAC','VENPAC')) AND  OP."IdEstado" <> 'PEN' AND OP."IdEstado" <> 'ANU' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')  AND 
					((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
					(TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') < TO_VARCHAR(OP."FechaLiquidacion",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
					TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD')
					AND ( 	( (CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'VENPAC' THEN Car."IdClienteRelacionado" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros = 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdClienteRelacionado"
					ELSE Car."IdCliente" END END END )  = p_IdCliente AND 
					(CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'VENPAC' THEN Car."IdPortafolioRelacionado" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros = 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolioRelacionado"
					ELSE Car."IdPortafolio" END END END ) = p_IdPortafolio ) 
					OR ( p_IdCliente = 0 AND p_IdPortafolio = 0 ))
			ORDER BY "FolioCartera" DESC;
		END IF;	

	/* Para todos los portafolios cuenta propia*/
		IF (TRIM(p_TipoCartera) = 'DRC' OR TRIM(p_TipoCartera) = 'DRV' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia > 0  THEN 
		--Select * FROM :v_CuentaPropia; -- Verificamos contenido
		v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
		SELECT	'PROPIA' AS "Tipo"
				,Car."IdTipoOperacion" AS "IdTipoOperacion"
				,OP."FolioOperacion" AS "FolioOperacion"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'DRV' ELSE 'DRC' END AS "TipoCartera"
				,Car."FechaOperacion"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" ELSE Car."IdClienteRelacionado" END AS "IdCliente"--Car."IdCliente"
				,IDENT."DocumentoIdentidad"        AS "RUTCliente"
				,CASE PER."Tipo"
					WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
					ELSE PER."RazonSocial"
				END  AS "RazonSocial"
				,'' AS "RelacionCorredora"
				,RE."IdTipoInstrumento" AS "FamiliaInstrumento"
				,Car."Nemotecnico"
				,Car."IdEmisor" --Modificado
				,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
					ELSE '' END AS "RutEmisor" --Modificado 
				,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
				,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
				,0 AS "DiasAlVencimiento"
				,COALESCE(OP."Tasa",0) AS "TasaPacto"
				,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
				,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
				,Car."IdTipoReajuste" AS "MonedaPapel"
				,Car."TasaCompra" AS "TasaCompraOriginal"
				,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
				,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
				,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
				,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
				,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
				,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
				,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
				,OP."MontoOperacionFinal" AS "ValorAlVencimientoEnPesos"--(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoATasaCompraActual" AS "ValorAlVencimientoEnPesos" 
				,OP."MontoOperacionFinalMoneda" AS "ValorAlVencimientoEnMoneda" --(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoCompraActualMonedaInstrumento" AS "ValorAlVencimientoEnMoneda" 
				,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
				THEN Emi."ValorClasificacionRiesgoIIF" 
				ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
				,Car."INGTasaMercadoActual" AS "TasaMercado"
				,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
				,0 AS "MenorValorAlVencimiento"
				,0 AS "MayorValorAlVencimiento"	
				,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
				,99.99 AS "TasaMercadoBCS"			--* FALTAN
				,15000000 AS "ValorMercadoBCS"		--* FALTAN
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END   AS "TipoPacto"
				,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
				,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
				,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
				,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
				,Car."INGNumeroCupon" AS "NumeroCupon"
				,CI."FrecuenciaCupon"
				,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
				,Car."IdBoveda" AS "TipoBoveda"
				,Car."EsFungible" AS "Fungibilidad"
				,'' AS "ClasificacionTablaIndice" 				--* UPDATE
				,'' AS "TieneReserva"							--* UPDATE
				,'Disponible' AS "TipoGarantia"					--* UPDATE
				,Car."NemoSVS"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"											--* FALTAN
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
				,RE."FolioCarteraOrigen" AS "FolioCartera"
				,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END  AS "TipoOperacion"						--* FALTAN
				,444441 AS "ValorLibro"
				,Car."CodigoIFRS" AS "CodigoIFRS"
				,MES."Nombre" AS "NombreMesa"
		FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
				INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
				--INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  Car ON (OP."IdOperacion" = Car."FolioComprobante" AND RE."IdCarteraCustodia" = Car."IdCartera" ) --AND  Car."EGRIdConcepto" = '' /*AND RE."IdCarteraCustodia" = Car."IdCartera"*/)
				INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  CAR ON ( RE."FolioCarteraCustodia"  = CAR."FolioCartera")
				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio" ELSE Car."IdPortafolioRelacionado" END)
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa")
				WHERE  (OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND Car."IdTipoOperacion" IN ('COMPAC','VENPAC')) AND   OP."IdEstado" <> 'PEN' AND OP."IdEstado" <> 'ANU' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')  AND 
					((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
					(TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') < TO_VARCHAR(OP."FechaLiquidacion",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
					TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD') 
		ORDER BY "FolioCartera" DESC;	

	
	END IF;

	--*
	--* ------------------------------------------------------------------------------------------------
	--* SE DEBE HACER LO MISMO QUE ARRIBA, PERO CON LAS CARTERAS QUE VIENEN DE COMPRAS CON PACTO
	--* A LA FECHA DE ESTE DESARROLLO, NO SE TIENE CLARIDAD COMO VAN A QUEDAR GRABADAS EN 
	--* CUSTODIA (CARTERA + SUBYACENTE)
	--* ------------------------------------------------------------------------------------------------
	--*
	IF (TRIM(p_TipoCartera) = 'ORC' OR TRIM(p_TipoCartera) = 'ORV' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia = 0 THEN 
	--Cuenta Propia
		IF :p_IdCliente = 0 THEN
				v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
				SELECT	'PROPIA' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'ORV' ELSE 'ORC' END  AS "TipoCartera"
					,Car."FechaOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" ELSE Car."IdClienteRelacionado" END AS "IdCliente"--Car."IdCliente"
					,IDENT."DocumentoIdentidad"        AS "RUTCliente"
					,CASE PER."Tipo" 
						WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
						ELSE PER."RazonSocial"
					END  AS "RazonSocial"					
					,'' AS "RelacionCorredora"
					,RE."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,OP."MontoOperacionFinal" AS "ValorAlVencimientoEnPesos"--(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoATasaCompraActual" AS "ValorAlVencimientoEnPesos" 
					,OP."MontoOperacionFinalMoneda" AS "ValorAlVencimientoEnMoneda" --(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoCompraActualMonedaInstrumento" AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Car."INGTasaMercadoActual" AS "TasaMercado"
					,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END  AS "TipoPacto"
					,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Car."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Car."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,'Disponible' AS "TipoGarantia"					--* UPDATE
					,Car."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"										--* FALTAN
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
					,RE."FolioCarteraOrigen" AS "FolioCartera"
					,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END  AS "TipoOperacion"						--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MES."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
					--INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  Car ON (OP."IdOperacion" = Car."FolioComprobante" AND RE."IdCarteraCustodia" = Car."IdCartera" ) --AND  Car."EGRIdConcepto" = '' /*AND RE."IdCarteraCustodia" = Car."IdCartera"*/)
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  CAR ON ( RE."FolioCarteraCustodia"  = CAR."FolioCartera")
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio" ELSE Car."IdPortafolioRelacionado" END)
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa")
					WHERE  (OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND Car."IdTipoOperacion" IN ('COMPAC','VENPAC')) AND   OP."IdEstado" <> 'PEN' AND OP."IdEstado" <> 'ANU' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')  AND 
						((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
						(TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') < TO_VARCHAR(OP."FechaLiquidacion",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
						TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD') 
			ORDER BY "FolioCartera" DESC;	
		END IF;
	--Otros Clientes
		
		    v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
			SELECT	'TERCEROS' AS "Tipo"
					,Car."IdTipoOperacion" AS "IdTipoOperacion"
					,OP."FolioOperacion" AS "FolioOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'ORV' ELSE 'ORC' END AS "TipoCartera"--El cliente Se voltea EJ: Mesa Compra Pacto(ORC) -> Cliente Venta Pacto (ORV)
					,Car."FechaOperacion"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" ELSE Car."IdClienteRelacionado" END AS "IdCliente"--Car."IdCliente"
					,IDENT."DocumentoIdentidad"        AS "RUTCliente"
					,CASE PER."Tipo" 
						WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
						ELSE PER."RazonSocial"
					END  AS "RazonSocial"
					,'' AS "RelacionCorredora"
					,RE."IdTipoInstrumento" AS "FamiliaInstrumento"
					,Car."Nemotecnico"
					,Car."IdEmisor" --Modificado
					,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
						ELSE '' END AS "RutEmisor" --Modificado 
					,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
					,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
					,0 AS "DiasAlVencimiento"
					,COALESCE(OP."Tasa",0) AS "TasaPacto"
					,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
					,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
					,Car."IdTipoReajuste" AS "MonedaPapel"
					,Car."TasaCompra" AS "TasaCompraOriginal"
					,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
					,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
					,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
					,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
					,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
					,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
					,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
					,OP."MontoOperacionFinal" AS "ValorAlVencimientoEnPesos"--(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoATasaCompraActual" AS "ValorAlVencimientoEnPesos" 
					,OP."MontoOperacionFinalMoneda" AS "ValorAlVencimientoEnMoneda" --(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoCompraActualMonedaInstrumento" AS "ValorAlVencimientoEnMoneda" 
					,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
					THEN Emi."ValorClasificacionRiesgoIIF" 
					ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
					,Car."INGTasaMercadoActual" AS "TasaMercado"
					,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
					,0 AS "MenorValorAlVencimiento"
					,0 AS "MayorValorAlVencimiento"	
					,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
					,99.99 AS "TasaMercadoBCS"			--* FALTAN
					,15000000 AS "ValorMercadoBCS"		--* FALTAN
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
					,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'CP' ELSE 
					 CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'VP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'VP' ELSE 'CP' END END END  AS "TipoPacto"
					,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
					,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
					,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
					,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
					,Car."INGNumeroCupon" AS "NumeroCupon"
					,CI."FrecuenciaCupon"
					,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
					,Car."IdBoveda" AS "TipoBoveda"
					,Car."EsFungible" AS "Fungibilidad"
					,'' AS "ClasificacionTablaIndice" 				--* UPDATE
					,'' AS "TieneReserva"							--* UPDATE
					,'Disponible' AS "TipoGarantia"					--* UPDATE
					,Car."NemoSVS"
					,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"										--* FALTAN
					,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
					,Car."FolioCartera" AS "FolioCartera"
					,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
					,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'CP' ELSE 
					 CASE WHEN Car."IdTipoOperacion" = 'COMPAC' AND :v_CuentaPropiaTerceros = 0 THEN 'VP' ELSE
					 CASE WHEN Car."IdTipoOperacion" = 'VENPAC' AND :v_CuentaPropiaTerceros > 0 THEN 'VP' ELSE 'CP' END END END  AS "TipoOperacion"						--* FALTAN
					,444441 AS "ValorLibro"
					,Car."CodigoIFRS" AS "CodigoIFRS"
					,MES."Nombre" AS "NombreMesa"
			FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
					INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  Car ON (OP."IdOperacion" = Car."FolioComprobante" AND RE."IdCarteraCustodia" = Car."IdCartera" ) 
					LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio"
					ELSE CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'VENPAC' THEN Car."IdPortafolioRelacionado" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros = 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolioRelacionado"
					ELSE Car."IdPortafolio" END END END)
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa")
					WHERE (OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND Car."IdTipoOperacion" IN ('COMPAC','VENPAC')) AND   OP."IdEstado" <> 'PEN' AND OP."IdEstado" <> 'ANU' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')  AND 
					((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
					(TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') < TO_VARCHAR(OP."FechaLiquidacion",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
					TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD')
					AND ( 	( (CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'VENPAC' THEN Car."IdClienteRelacionado" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros = 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdClienteRelacionado"
					ELSE Car."IdCliente" END END END )  = p_IdCliente AND 
					(CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros > 0 AND Car."IdTipoOperacion" = 'VENPAC' THEN Car."IdPortafolioRelacionado" 
					ELSE CASE WHEN :v_CuentaPropiaTerceros = 0 AND Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolioRelacionado"
					ELSE Car."IdPortafolio" END END END ) = p_IdPortafolio ) 
					OR ( p_IdCliente = 0 AND p_IdPortafolio = 0 ))
			ORDER BY "FolioCartera" DESC;
	
	END IF;
	IF (TRIM(p_TipoCartera) = 'ORC' OR TRIM(p_TipoCartera) = 'ORV' OR TRIM(p_TipoCartera) = 'TOD') AND :v_ContadorCuentaPropia > 0 THEN
		v_TblCartera = SELECT * FROM :v_TblCartera UNION ALL 
		SELECT	'PROPIA' AS "Tipo"
				,Car."IdTipoOperacion" AS "IdTipoOperacion"
				,OP."FolioOperacion" AS "FolioOperacion"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'ORV' ELSE 'ORC' END  AS "TipoCartera"
				,Car."FechaOperacion"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdCliente" ELSE Car."IdClienteRelacionado" END AS "IdCliente"--Car."IdCliente"
				,IDENT."DocumentoIdentidad"        AS "RUTCliente"
				,CASE PER."Tipo" 
					WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
					ELSE PER."RazonSocial"
				END  AS "RazonSocial"
				,'' AS "RelacionCorredora"
				,RE."IdTipoInstrumento" AS "FamiliaInstrumento"
				,Car."Nemotecnico"
				,Car."IdEmisor" --Modificado
				,CASE WHEN Emi."DocumentoIdentidad" IS NOT NULL THEN Emi."DocumentoIdentidad"
					ELSE '' END AS "RutEmisor" --Modificado 
				,Car."FechaVencimientoInstrumento" AS "FechaVencimiento"--Inst."FechaVencimiento"
				,Car."FechaVencimientoOperacion" AS "FechaVencimientoPacto" --Modificado
				,0 AS "DiasAlVencimiento"
				,COALESCE(OP."Tasa",0) AS "TasaPacto"
				,COALESCE(OP."IdTipoReajuste",'') AS "MonedaPacto"
				,CASE WHEN Car."IdTipoReajuste" IN ('CLP', 'NR') THEN 'M' ELSE 'A' END AS "TipoTasa"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."INGValorRescate" ELSE Car."INGCantidad" END AS "Nominal"
				,Car."IdTipoReajuste" AS "MonedaPapel"
				,Car."TasaCompra" AS "TasaCompraOriginal"
				,Car."INGMontoATasaCompraActual" AS "ValorCompraEnPesos"
				,Car."INGMontoCompraActualMonedaInstrumento" AS "ValorCompraEnMoneda"
				,Car."INGMontoInteresCorridoCompraActual" AS "InteresAcumulado"--Car."INGMontoATasaMercadoActual" AS "InteresAcumulado"
				,Car."INGMontoReajusteCorridoCompraActual" AS "ReajusteAcumulado"--Car."INGMontoReajusteMercadoAnterior" AS "ReajusteAcumulado"
				,Car."INGMontoATasaMercadoActual" AS "ValorPresente"
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" AS "Interes"
				,Car."INGMontoReajusteCompraProximoMonedaInstrumento" AS "Reajuste"
				,Car."INGMontoATasaMercadoProximo" AS "ValorPresente_t1"
				,OP."MontoOperacionFinal" AS "ValorAlVencimientoEnPesos"--(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoATasaCompraActual" AS "ValorAlVencimientoEnPesos" 
				,OP."MontoOperacionFinalMoneda" AS "ValorAlVencimientoEnMoneda" --(((OP."Tasa"/3000)*Car."PlazoEnDiasOperacion")+1)*Car."INGMontoCompraActualMonedaInstrumento" AS "ValorAlVencimientoEnMoneda" 
				,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
				THEN Emi."ValorClasificacionRiesgoIIF" 
				ELSE ICR."ValorClasificacionRiesgo" END  AS "ClasificacionRiesgo"
				,Car."INGTasaMercadoActual" AS "TasaMercado"
				,Car."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado"
				,0 AS "MenorValorAlVencimiento"
				,0 AS "MayorValorAlVencimiento"	
				,Car."INGFechaPrecioTasaMercadoActual" AS "FechaTasaBCS"
				,99.99 AS "TasaMercadoBCS"			--* FALTAN
				,15000000 AS "ValorMercadoBCS"		--* FALTAN
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" < 0 THEN ABS (Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo") ELSE 0 END	AS "MenorValor"
				,CASE WHEN Car."INGMontoATasaMercadoActual" - Car."INGMontoATasaMercadoProximo" > 0 THEN Car."INGMontoATasaMercadoActual" -Car."INGMontoATasaMercadoProximo" ELSE 0 END AS "MayorValor"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END  AS "TipoPacto"
				,Car."INGFechaPagoCorteCupon" AS "FechaProximoCupon"
				,Car."INGMontoCapitalCorteCupon" AS "ProximoCuponNominal" 
				,Car."INGMontoCapitalCorteCupon" AS "AmortizacionCupon"
				,Car."INGMontoInteresCorteCupon" AS "InteresCupon"
				,Car."INGNumeroCupon" AS "NumeroCupon"
				,CI."FrecuenciaCupon"
				,'SI' AS "TieneCustodia"	--* CODIGO SEBRA ES CONSTANTE.
				,Car."IdBoveda" AS "TipoBoveda"
				,Car."EsFungible" AS "Fungibilidad"
				,'' AS "ClasificacionTablaIndice" 				--* UPDATE
				,'' AS "TieneReserva"							--* UPDATE
				,'Disponible' AS "TipoGarantia"					--* UPDATE
				,Car."NemoSVS"
				,CASE WHEN Car."IdClasificacionTipoInstrumento" = 'IF' THEN Car."TasaCompra" ELSE CI."Tasa" END AS "TasaTera"										--* FALTAN
				,Car."INGMontoInteresCompraProximoMonedaInstrumento" + Car."INGMontoReajusteCompraProximoMonedaInstrumento"  AS "InteresDevengado"	
				,RE."FolioCarteraOrigen" AS "FolioCartera"
				,PORT."CodigoPortafolio" ||' '|| PORT."Proposito" as "SecCliente"
				,CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN 'CP' ELSE 'VP' END  AS "TipoOperacion"						--* FALTAN
				,444441 AS "ValorLibro"
				,Car."CodigoIFRS" AS "CodigoIFRS"
				,MES."Nombre" AS "NombreMesa"
		FROM 	"BSCL.Mesa.BD::MES.Operacion"  OP
				INNER JOIN 	"BSCL.Custodia.BD::CUS.Reserva" RE ON (OP."IdOperacion" = RE."IdOperacion")
				--INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  Car ON (OP."IdOperacion" = Car."FolioComprobante" AND RE."IdCarteraCustodia" = Car."IdCartera" ) --AND  Car."EGRIdConcepto" = '' /*AND RE."IdCarteraCustodia" = Car."IdCartera"*/)
				INNER JOIN 	"BSCL.Custodia.BD::CUS.Cartera"  CAR ON ( RE."FolioCarteraCustodia"  = CAR."FolioCartera")
				LEFT JOIN   "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS Emi  ON RE."IdEmisor" = Emi."IdEmisor" --Traer Clasificacion Riesgo IIF
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" CI ON CI."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS ICR ON ICR."IdInstrumento" = RE."IdInstrumento"
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT ON (PORT."IdPortafolio" = CASE WHEN Car."IdTipoOperacion" = 'COMPAC' THEN Car."IdPortafolio" ELSE Car."IdPortafolioRelacionado" END)
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON (CLI."IdCliente" = PORT."IdCliente" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER  ON (PER."IdPersona" = CLI."IdPersona" )
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
				LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" AS MES ON (MES."IdMesa" = Car."IdMesa")
				WHERE (OP."IdTipoOperacion" IN ('COMPAC','VENPAC') AND Car."IdTipoOperacion" IN ('COMPAC','VENPAC')) AND   OP."IdEstado" <> 'PEN' AND OP."IdEstado" <> 'ANU' AND Car."IdTipoInstrumento" <> 'PAC' AND Car."IdClasificacionTipoInstrumento" IN ('RF','IF')  AND 
					((TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') <= TO_VARCHAR(OP."FechaVencimiento",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001') OR 
					(TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') < TO_VARCHAR(OP."FechaLiquidacion",'YYYY-MM-DD') AND TO_VARCHAR(OP."FechaLiquidacion", 'DD-MM-YYYY') <> '01-01-0001')) AND 
					TO_VARCHAR(:p_Fecha,'YYYY-MM-DD') >= TO_VARCHAR(OP."FechaOperacion",'YYYY-MM-DD') 
		ORDER BY "FolioCartera" DESC;		
				
	END IF; --> IF TRIM(p_TipoCartera) = 'DRC' OR TRIM(p_TipoCartera) = 'DRV' OR TRIM(p_TipoCartera) = 'TODAS' THEN 






	--* ---------------------------------------------------------------------------------
	--* UPDATES VARIOS
	--* ---------------------------------------------------------------------------------
	
	
	IF (:p_Fecha < TO_DATE(:v_FechaSistema,'YYYY-MM-DD')) THEN
		v_TieneReserva =
		SELECT TMP."FolioOperacion", TMP."IdCliente", TMP."FolioCartera", 'S' AS "TieneReserva"
		FROM 	:v_TblCartera AS TMP
		INNER JOIN "BSCL.Custodia.BD::HIS.Reserva" RES ON 
		(	
			  	TMP."FolioCartera" = (CASE WHEN RES."FolioCarteraOrigen" = 0 THEN RES."FolioCarteraCustodia" ELSE RES."FolioCarteraOrigen" END)
			AND RES."IdEstado" = 'VIG' 
			AND RES."FechaRespaldo" = :p_Fecha
		)
		AND TMP."TipoCartera" = 'DISP'
		GROUP BY TMP."FolioOperacion", TMP."IdCliente", TMP."FolioCartera";
	ELSE
	    v_TieneReserva = 
		SELECT TMP."FolioOperacion", TMP."IdCliente", TMP."FolioCartera", 'S' AS "TieneReserva"
		FROM 	:v_TblCartera AS TMP
		INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON (TMP."FolioCartera" = (CASE WHEN RES."FolioCarteraOrigen" = 0 THEN RES."FolioCarteraCustodia" ELSE RES."FolioCarteraOrigen" END) AND RES."IdEstado" = 'VIG')
		AND TMP."TipoCartera" = 'DISP'
		GROUP BY TMP."FolioOperacion", TMP."IdCliente", TMP."FolioCartera";
	END IF;
		
	
	
	v_RelacionCorredora = 
	SELECT TMP."IdCliente", TMP."RUTCliente", 'Relacionado' AS "RelacionCorredora"
	FROM 	:v_TblCartera AS TMP 
    LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" REL ON (TMP."RUTCliente" = REL."DocumentoIdentidad" AND REL."NombreEstado" ='ACTIVO')
    GROUP BY TMP."IdCliente", TMP."RUTCliente";
	    

	    
	    
	p_out_CConsolidada = 
		Select	
				 TMP."FolioOperacion" 
				,"TipoCartera" 
				,"FechaOperacion"
				,TMP."IdCliente" 
				,TMP."RUTCliente" 
				,"RazonSocial"
				,COALESCE(REL."RelacionCorredora",'NO') AS "RelacionCorredora"
				,"FamiliaInstrumento" 
				,"Nemotecnico"
				,"IdEmisor" 
				,"RutEmisor" 
				,"FechaVencimiento" 
				,"FechaVencimientoPacto" 
				,"DiasAlVencimiento" 
				,"TasaPacto" 
				,"MonedaPacto"
				,"TipoTasa" 
				,"Nominal" 
				,"MonedaPapel" 
				,"TasaCompraOriginal"
				,"ValorCompraEnPesos"
				,"ValorCompraEnMoneda"
				,"InteresAcumulado" 
				,"ReajusteAcumulado" 
				,"ValorPresente" 
				,"Interes" 
				,"Reajuste" 
				,"ValorPresente_t1" 
				,"ValorAlVencimientoEnPesos" 
				,"ValorAlVencimientoEnMoneda" 
				,"ClasificacionRiesgo"
				,"TasaMercado" 
				,"ValorMercado"
				,"MenorValorAlVencimiento" 
				,"MayorValorAlVencimiento" 
				,"FechaTasaBCS" 
				,"TasaMercadoBCS" 
				,"ValorMercadoBCS" 
				,"MenorValor" 
				,"MayorValor" 
				,"TipoPacto" 
				,"FechaProximoCupon" 
				,"ProximoCuponNominal"
				,"AmortizacionCupon"
				,"InteresCupon" 
				,"NumeroCupon" 
				,"FrecuenciaCupon" 
				,"TieneCustodia" 
				,"TipoBoveda" 
				,"Fungibilidad"

				,(CASE
					WHEN "TipoCartera" = 'DISP' THEN
						CASE WHEN TMP."IdCliente" != v_IdCorredora THEN 'No Aplica' 
							 WHEN "DiasAlVencimiento" <= 90 THEN 'Categoria A'
							 WHEN "DiasAlVencimiento" > 0090 AND "DiasAlVencimiento" <= 360 THEN 'Categoria B'
							 WHEN "DiasAlVencimiento" > 0360 AND "DiasAlVencimiento" <= 1800 THEN 'Categoria C'
							 WHEN "DiasAlVencimiento" > 1800 AND "DiasAlVencimiento" <= 4320 THEN 'Categoria D'						
							 ELSE 'Categoria E' 
						END				
					ELSE -- "TipoCartera" != 'DISP'
						CASE WHEN TMP."IdCliente" != v_IdCorredora THEN 'No Aplica' 
							 WHEN "IdTipoOperacion" = 'COMPAC' THEN ''
							 WHEN "DiasAlVencimiento" <= 90 THEN 'Categoria A'
							 WHEN "DiasAlVencimiento" > 0090 AND "DiasAlVencimiento" <= 360 THEN 'Categoria B'
							 WHEN "DiasAlVencimiento" > 0360 AND "DiasAlVencimiento" <= 1800 THEN 'Categoria C'
							 WHEN "DiasAlVencimiento" > 1800 AND "DiasAlVencimiento" <= 4320 THEN 'Categoria D'						
							 ELSE 'Categoria E' 
						END				
				END) AS "ClasificacionTablaIndice"

				,COALESCE(TMP."TieneReserva",'N') AS "TieneReserva"
				,"TipoGarantia" 
				,"NemoSVS" 
				,"TasaTera" 
				,"InteresDevengado" 
				,TMP."FolioCartera" 
				,"SecCliente" 
				,"TipoOperacion" 
				,"ValorLibro"
				,"CodigoIFRS"
				,"NombreMesa"
		
		FROM 	:v_TblCartera AS TMP
	    LEFT JOIN :v_TieneReserva AS RES ON (TMP."FolioOperacion"=RES."FolioOperacion" AND TMP."IdCliente"=RES."IdCliente" AND TMP."FolioCartera" = RES."FolioCartera")
	    LEFT JOIN :v_RelacionCorredora AS REL ON (TMP."IdCliente"=REL."IdCliente" AND TMP."RUTCliente"=REL."RUTCliente");
	

END;

GO

SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCertificadoCustodia";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCertificadoCustodia" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                		BIGINT,
   	IN p_FechaConsulta 					NVARCHAR(10),-- default '0001-01-01',
   	IN p_TipoFechaConsulta				NVARCHAR(3) DEFAULT 'LIQ', --PARA MANTENER EL COMPORTAMIENTO ORIGINAL							
   -- Parametros de Salida
   	OUT p_out_tbl 						TABLE (
   											"TipoCartera"							NVARCHAR(40)
									   		,"IdClasificacionTipoInstrumento" 		NVARCHAR(3)
									   		,"NombreClasificacionTipoInstrumento" 	NVARCHAR(100)
									   		,"IdTipoInstrumento" 					NVARCHAR(3)
									   		,"NombreTipoInstrumento" 				NVARCHAR(100)
									   		,"IdInstrumento" 						BIGINT
									  		,"Nemotecnico" 							NVARCHAR(40)
									  		,"IdEmisor" 							NVARCHAR(40)
									  		,"EsFungible" 							NVARCHAR(1)
									  		,"IdCliente" 							BIGINT
									  		,"IdPortafolio" 						BIGINT
									  		,"DocumentoIdentidad" 					NVARCHAR(40)
									  		,"NombreCliente" 						NVARCHAR(302)
									  		,"CodigoPortafolio" 					NVARCHAR(100)
									  		,"IdBoveda" 							NVARCHAR(3)
									  		,"Relacion" 							NVARCHAR(20)
									  		,"TasaMercadoActual"		 			DECIMAL(26,8)
									  		,"IdMoneda"								NVARCHAR(3)
									  		,"FechaOperacion"						NVARCHAR(10)
									  		,"FechaVencimiento"						NVARCHAR(10)
											,"CantidadCustodia" 					DECIMAL(26,8)
											,"CantidadGarantia" 					DECIMAL(26,8)
											,"PrecioMercadoCustodia"				DECIMAL(26,8)
											,"ValorMercadoCustodia"					DECIMAL(26,8)
											,"MontoMercadoCustodia"					DECIMAL(26,8)
											,"IdTipoOperacion"                      NVARCHAR(40)
									   	  ) default empty
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	DECLARE v_DocumentoIdentidadCuentaPropia    NVARCHAR(40);
	DECLARE v_IdPersonaCuentaPropia             BIGINT;
	DECLARE v_IdClienteCuentaPropia             BIGINT;				
	DECLARE v_FechaConsulta 					NVARCHAR(10) := '2300-12-31';
	DECLARE v_count	BIGINT:=0;
	
	DECLARE v_AgrupaPorBoveda 					NVARCHAR(1) := 'S';
	DECLARE v_AgrupaPorRelacion 				NVARCHAR(1) := 'S';
	
	DECLARE v_tblInst TABLE (
		"TipoCartera"						NVARCHAR(40)
   		,"IdClasificacionTipoInstrumento" 	NVARCHAR(3)
   		,"IdTipoInstrumento" 				NVARCHAR(3)
   		,"IdInstrumento" 					BIGINT
   		,"Nemotecnico" 						NVARCHAR(40)
   		,"IdEmisor" 						NVARCHAR(40)
   		,"EsFungible" 						NVARCHAR(1)
   		,"IdCliente" 						BIGINT
   		,"IdPortafolio" 					BIGINT
   		,"NombreCliente" 					NVARCHAR(302)
   		,"DocumentoIdentidad"				NVARCHAR(40)
   		,"IdBoveda" 						NVARCHAR(3)
   		,"Relacion" 						NVARCHAR(20)
		,"IdMoneda"							NVARCHAR(3)
		,"FechaOperacion"					NVARCHAR(10)
		,"FechaVencimiento"					NVARCHAR(10)
		,"INGPrecioMercadoActual"	   		DECIMAL(26,8)
		,"PrecioMercadoCustodia"			DECIMAL(26,8)
		,"IdTipoOperacion"                  NVARCHAR(40)
	 );
	 
	 DECLARE v_tblCustodia TABLE (
	  	"TipoCartera"						NVARCHAR(40)
   		, "IdClasificacionTipoInstrumento" 	NVARCHAR(3)
   		,"IdTipoInstrumento" 				NVARCHAR(3)
   		,"IdInstrumento" 					BIGINT
   		,"Nemotecnico" 						NVARCHAR(40)
   		,"IdEmisor" 						NVARCHAR(40)
   		,"EsFungible" 						NVARCHAR(1)
   		,"IdCliente" 						BIGINT
   		,"IdPortafolio" 					BIGINT
   		,"IdBoveda" 						NVARCHAR(3)
   		,"Relacion" 						NVARCHAR(20)
		,"IdMoneda"							NVARCHAR(3)
		,"FechaOperacion"					NVARCHAR(10)
		,"FechaVencimiento"					NVARCHAR(10)	
   		,"Cantidad" 						DECIMAL(26,8)
   		,"INGTasaMercadoActual"				DECIMAL(26,8) --RF,IF
   		,"INGPrecioMercadoActual"			DECIMAL(26,8) --RV.
     	,"ValorMercado"						DECIMAL(26,8) --suma INGMontoMercadoActualMonedaInstrumento
     	,"MontoMercado"						DECIMAL(26,8) --suma INGMontoATasaMercadoActual
     	,"PrecioMercadoCustodia"			DECIMAL(26,8)
     	,"IdTipoOperacion"                  NVARCHAR(40)
	 );
	
	DECLARE v_fechaHoraSistema 				NVARCHAR(40):='';
	DECLARE v_IdEmpresa  					BIGINT:=0;
	DECLARE v_DO 							DECIMAL = 0;
	
	DECLARE v_AdvertenciasContador	INT;
	DECLARE v_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000));	
	DECLARE v_ErroresContador		INT;
	DECLARE v_ErroresDetalle		TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000));
	
	DECLARE v_tblInstrumentos TABLE(
		    "IdEmpresa" 				BIGINT,
		    "IdInstrumento" 			BIGINT,
		    "IdBolsa" 					NVARCHAR(20),
		    "Fecha" 					NVARCHAR(10),
		    "IdCondicionDeLiquidacion" 	NVARCHAR(20)
	);
	
	----------------------------------------------------------
	---- inicializa las tablas de paso

	v_tblCustodia = select  '' AS "TipoCartera"
					,'' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "IdEmisor", '' as "EsFungible", 0 as "IdCliente", 0 as "IdPortafolio", '' as "IdBoveda",'' as "Relacion",'' AS "IdMoneda",'' AS "FechaOperacion",'' AS "FechaVencimiento", 0 as "Cantidad"
	,0 AS "INGTasaMercadoActual" , 0 AS "INGPrecioMercadoActual", 0 AS "ValorMercado",0 AS  "MontoMercado", 0 AS "PrecioMercadoCustodia", '' AS "IdTipoOperacion"
					from DUMMY where 1=2;
				
				
		v_tblSimulC				= SELECT * FROM :v_tblCustodia WHERE 1=2;
		v_tblSimulV				= SELECT * FROM :v_tblCustodia WHERE 1=2;
		v_tblPrestamos			= SELECT * FROM :v_tblCustodia WHERE 1=2;
		v_tblEmprestitos		= SELECT * FROM :v_tblCustodia WHERE 1=2;
		v_tblGarantias			= SELECT * FROM :v_tblCustodia WHERE 1=2;
		v_tblTrasnIng			= SELECT * FROM :v_tblCustodia WHERE 1=2;
		v_tblTrasnEgr			= SELECT * FROM :v_tblCustodia WHERE 1=2;
		v_tblCustodiaNegativa	= SELECT * FROM :v_tblCustodia WHERE 1=2;
		
	begin 
	v_TablaCarteras    		  =		SELECT * FROM "BSCL.Custodia.BD::GBL.ConsultarCertificadoCustodia_Entrada_tblCartera";	  
	v_TablaInstrumentos 	  =		SELECT * FROM "BSCL.Custodia.BD::GBL.ConsultarCertificadoCustodia_Entrada_tblInstrumento";
 	v_FechaConsulta=p_FechaConsulta;
	v_IdEmpresa = p_IdEmpresa;
	-- v_TablaCarteras= SELECT 116323 AS "IdCliente", 20021 AS "IdPortafolio" FROM DUMMY;
 
	-- Obtiene los datos de la corredora(para cuenta propia)	
	SELECT "IdPersona", "IdCliente", "DocumentoIdentidad" 
	INTO v_IdPersonaCuentaPropia, v_IdClienteCuentaPropia, v_DocumentoIdentidadCuentaPropia 
	FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
	WHERE "IdEmpresa" = :p_IdEmpresa
	GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad";


	-- Obtiene los relacionados a la corredora
	v_tblRelacionados = 
	SELECT distinct PER."IdPersonaDestino"
	FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" AS PER
	WHERE PER."IdPersonaOrigen" = :v_IdPersonaCuentaPropia AND PER."IdEmpresa" = p_IdEmpresa 
	AND PER."IdEstado" = 'ACT';	
	
	
  
	SELECT COUNT(1) INTO v_count FROM :v_TablaInstrumentos;
	IF v_count=0 THEN
		v_TablaInstrumentos = SELECT NULL AS "IdClasificacionTipoInstrumento" FROM DUMMY;
	END IF;


	SELECT COUNT(1) INTO v_count FROM :v_TablaCarteras;
	IF v_count=0 THEN
		v_TablaCarteras = SELECT NULL AS "IdCliente",NULL AS "IdPortafolio" FROM DUMMY;
	END IF;
    end;

	
	-------------------------- 
	-- custodia
	-------------------------- 
	 begin 
		-- Detalle
	     call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => v_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);

		IF  TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') < TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN
			v_tblInstrumentos = 
				SELECT
				:p_IdEmpresa		AS "IdEmpresa",			
				"IdInstrumento"		AS "IdInstrumento",
				'' 					AS "IdBolsa",
				:p_FechaConsulta 	AS "Fecha",
				'' 					AS "IdCondicionDeLiquidacion"
				FROM (
			         SELECT "IdInstrumento" FROM "BSCL.Custodia.BD::HIS.Cartera"
			         WHERE "IdPortafolio" IN (SELECT "IdPortafolio" FROM :v_TablaCarteras) AND "IdEstado" = 'VIG' AND TO_DATE("FechaRespaldo") = TO_DATE(v_FechaConsulta)
			         UNION
			         SELECT "IdInstrumento" FROM "BSCL.Custodia.BD::HIS.CarteraNegativa"
			         WHERE "IdPortafolio" IN (SELECT "IdPortafolio" FROM :v_TablaCarteras) AND "IdEstado" = 'VIG' AND TO_DATE("FechaRespaldo") = TO_DATE(v_FechaConsulta)
					 UNION
			         SELECT "IdInstrumento" FROM "BSCL.Custodia.BD::HIS.Garantia"
			         WHERE "IdPortafolio" IN (SELECT "IdPortafolio" FROM :v_TablaCarteras) AND "IdEstado" = 'VIG' AND TO_DATE("FechaRespaldo") = TO_DATE(v_FechaConsulta)
				);
		ELSE
			v_tblInstrumentos = 
				SELECT
				:p_IdEmpresa		AS "IdEmpresa",			
				"IdInstrumento"		AS "IdInstrumento",
				'' 					AS "IdBolsa",
				:p_FechaConsulta 	AS "Fecha",
				'' 					AS "IdCondicionDeLiquidacion"
				FROM (
					SELECT "IdInstrumento" FROM "BSCL.Custodia.BD::CUS.Cartera"
			         WHERE "IdPortafolio" IN (SELECT "IdPortafolio" FROM :v_TablaCarteras) AND "IdEstado" = 'VIG'
			         UNION
			         SELECT "IdInstrumento" FROM "BSCL.Custodia.BD::CUS.CarteraNegativa"
			         WHERE "IdPortafolio" IN (SELECT "IdPortafolio" FROM :v_TablaCarteras) AND "IdEstado" = 'VIG'
					 UNION
			         SELECT "IdInstrumento" FROM "BSCL.Custodia.BD::GTA.Garantia"
			         WHERE "IdPortafolio" IN (SELECT "IdPortafolio" FROM :v_TablaCarteras) AND "IdEstado" = 'VIG'
				);
		END IF;
		
		CALL "BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa" ( 
							p_Instrumentos => :v_tblInstrumentos,
							p_out_Instrumento => v_tblPrecios,
							p_out_AdvertenciasContador => v_AdvertenciasContador,
							p_out_AdvertenciasDetalle => v_AdvertenciasDetalle,
							p_out_ErroresContador => v_ErroresContador,
							p_out_ErroresDetalle => v_ErroresDetalle
						);
				
		v_tblPrecioCFI = WITH precio AS (
								SELECT 
									ROW_NUMBER() OVER (PARTITION BY P."IdInstrumento" order by P."IdInstrumento" desc) AS row_num,
									P."FechaConsulta" AS "Fecha", I."Nemotecnico", '' AS "Entidad", P."PrecioMercado" AS "ValorRegistro"
								FROM :v_tblPrecios AS P
								LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS I ON I."IdInstrumento" = P."IdInstrumento"
						)
						SELECT "Fecha", "Nemotecnico", "Entidad", "ValorRegistro"
						FROM precio
						WHERE row_num = 1;
					
		v_tblPrecioACXETX =
						WITH precio AS (
								SELECT 
									ROW_NUMBER() OVER (PARTITION BY P."IdInstrumento" order by P."IdInstrumento" desc) AS row_num,
									P."FechaConsulta" AS "Fecha", I."Nemotecnico", '' AS "Entidad", P."PrecioMercado" AS "ValorRegistro"
								FROM :v_tblPrecios AS P
								LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS I ON I."IdInstrumento" = P."IdInstrumento"
						)
						SELECT "Fecha", "Nemotecnico", "Entidad", "ValorRegistro"
						FROM precio
						WHERE row_num = 1;

		WITH dolarObservado AS (
		    SELECT ROW_NUMBER() OVER (PARTITION BY PF."IdParametroFinanciero" ORDER BY PF."Fecha" DESC) AS row_num,
		        PF."Valor"
		    FROM
		        "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
		    WHERE
		        PF."Fecha" <= :v_FechaConsulta
		        AND PF."IdParametroFinanciero" = 'DO')
					SELECT
					    COALESCE("Valor", 0)
					INTO v_DO
					FROM
					    dolarObservado
					WHERE
					    row_num = 1;

		v_tblParametrosFinancieros = WITH dolarObservado AS (
										    SELECT
										        ROW_NUMBER() OVER (
										            PARTITION BY PF."IdParametroFinanciero"
										            ORDER BY
										                PF."Fecha" DESC
										        ) AS row_num,
										        PF."IdParametroFinanciero",
										        PF."Fecha",
										        PF."Valor"
										    FROM
										        "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
										    WHERE
										        PF."Fecha" <= :v_FechaConsulta
										)
										SELECT
											"IdParametroFinanciero",
											"Fecha",
										    CASE WHEN "IdParametroFinanciero" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP') THEN 
										    	"Valor" * :v_DO
										    ELSE
										    	"Valor"
										    END AS "Valor"
										FROM
										    dolarObservado
										WHERE
										    row_num = 1 AND "IdParametroFinanciero" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO');

      			IF  TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') < TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN      
					v_tblCustodia = SELECT  'CUSTODIA' AS "TipoCartera",CAR."IdClasificacionTipoInstrumento", CAR."IdTipoInstrumento", CAR."IdInstrumento", 
					case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."NemoSVS" else CAR."Nemotecnico" end as "Nemotecnico", 
					CAR."IdEmisor",
					case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."EsFungible" else 'N' end as "EsFungible", 
					CAR."IdCliente", CAR."IdPortafolio", CAR."IdBoveda", 
					CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion",
					CASE when CAR."IdClasificacionTipoInstrumento" in ('DV') THEN CAR."IdMonedaPrincipal" when CAR."IdClasificacionTipoInstrumento" in ('RF','IF') THEN CAR."IdTipoReajuste" ELSE (case when ifnull(CAR."IdMonedaInstrumento",'') <> '' then CAR."IdMonedaInstrumento" else ifnull(CAR."IdTipoReajuste",'') end ) END AS "IdMoneda",
					CAR."INGFechaOperacion"  AS "FechaOperacion",
					CASE  when CAR."IdClasificacionTipoInstrumento" in ('DV') THEN CAR."FechaVencimientoOperacion" ELSE CAR."FechaVencimientoInstrumento" END AS "FechaVencimiento",
					
					cast(IFNULL(case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."INGValorRescate" ELSE CAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad",
					case when CAR."IdClasificacionTipoInstrumento" in ('IF','RF') then CAR."INGTasaMercadoActual"
						when CAR."IdClasificacionTipoInstrumento" in ('DV') then CAR."ValorParidadCierre"
						else 0 end as "INGTasaMercadoActual",
					case when CAR."IdClasificacionTipoInstrumento" in ('RV','RF') then CAR."INGPrecioMercadoActual"
						when CAR."IdClasificacionTipoInstrumento" in ('FM') 	  THEN CASE WHEN ifnull(VCUOTA."ValorCuota",0)<>0 THEN VCUOTA."ValorCuota" ELSE CAR."INGPrecioMercadoActual" end
						when CAR."IdClasificacionTipoInstrumento" in ('DV') 	  then CAR."ValorTipoCambioCierre"
						else 0 end as "INGPrecioMercadoActual",
					CAR."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado",
					CAR."INGMontoATasaMercadoActual" AS "MontoMercado",
					COALESCE(NULLIF(PREMER."PrecioCierre", 0), CAR."INGPrecioMercadoActual") AS "PrecioMercadoCustodia",
					CAR."IdTipoOperacion"
				FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR	
				INNER JOIN :v_TablaCarteras PC 		ON (PC."IdCliente"=CAR."IdCliente" AND PC."IdPortafolio"=CAR."IdPortafolio" OR (PC."IdCliente" IS NULL AND PC."IdPortafolio" IS NULL))	
				INNER JOIN :v_TablaInstrumentos PI 	ON (PI."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento" OR PI."IdClasificacionTipoInstrumento" IS NULL  )	
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN (
				SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
					FROM
					( 	
									
						SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
						,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
						INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
						ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
						WHERE prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
				) PRE	
				WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
				
			) AS  PREMER
				ON (CAR."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa )
				LEFT JOIN 	"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente" AS VCUOTA ON CAR."IdInstrumento" = VCUOTA."IdInstrumento" AND VCUOTA."FechaVigencia" = v_FechaConsulta
				WHERE CAR."IdEmpresa" = p_IdEmpresa 
				AND	  CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF','PAC','FWD') 
				AND (p_TipoFechaConsulta = 'LIQ' OR p_TipoFechaConsulta = 'ADQ'	)
					AND  CAR."FechaRespaldo" = v_FechaConsulta
					AND	   CAR."IdEstado" ='VIG';
			ELSE
				v_tblCustodia =
					SELECT  'CUSTODIA' AS "TipoCartera",CAR."IdClasificacionTipoInstrumento", CAR."IdTipoInstrumento", CAR."IdInstrumento", 
						case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."NemoSVS" else CAR."Nemotecnico" end as "Nemotecnico", 
						CAR."IdEmisor",
						case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."EsFungible" else 'N' end as "EsFungible", 
						CAR."IdCliente", CAR."IdPortafolio", CAR."IdBoveda", 
						CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion",
						CASE when CAR."IdClasificacionTipoInstrumento" in ('DV') THEN CAR."IdMonedaPrincipal" when CAR."IdClasificacionTipoInstrumento" in ('RF','IF') THEN CAR."IdTipoReajuste" ELSE (case when ifnull(CAR."IdMonedaInstrumento",'') <> '' then CAR."IdMonedaInstrumento" else ifnull(CAR."IdTipoReajuste",'') end ) END AS "IdMoneda",
						CAR."INGFechaOperacion"  AS "FechaOperacion",
						CASE  when CAR."IdClasificacionTipoInstrumento" in ('DV') THEN CAR."FechaVencimientoOperacion" ELSE CAR."FechaVencimientoInstrumento" END AS "FechaVencimiento",
						
						cast(IFNULL(case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."INGValorRescate" ELSE CAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad",
						case when CAR."IdClasificacionTipoInstrumento" in ('IF','RF') then CAR."INGTasaMercadoActual"
							when CAR."IdClasificacionTipoInstrumento" in ('DV') then CAR."ValorParidadCierre"
							else 0 end as "INGTasaMercadoActual",
						case when CAR."IdClasificacionTipoInstrumento" in ('RV','RF') then CAR."INGPrecioMercadoActual"
							when CAR."IdClasificacionTipoInstrumento" in ('FM') 	  THEN CASE WHEN ifnull(VCUOTA."ValorCuota",0)<>0 THEN VCUOTA."ValorCuota" ELSE CAR."INGPrecioMercadoActual" end
							when CAR."IdClasificacionTipoInstrumento" in ('DV') 	  then CAR."ValorTipoCambioCierre"
							else 0 end as "INGPrecioMercadoActual",
						CAR."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado",
						CAR."INGMontoATasaMercadoActual" AS "MontoMercado",
						COALESCE(NULLIF(PREMER."PrecioCierre", 0), CAR."INGPrecioMercadoActual") AS "PrecioMercadoCustodia",
						CAR."IdTipoOperacion"
					FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR	
					INNER JOIN :v_TablaCarteras PC 		ON (PC."IdCliente"=CAR."IdCliente" AND PC."IdPortafolio"=CAR."IdPortafolio" OR (PC."IdCliente" IS NULL AND PC."IdPortafolio" IS NULL))	
					INNER JOIN :v_TablaInstrumentos PI 	ON (PI."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento" OR PI."IdClasificacionTipoInstrumento" IS NULL  )	
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
					LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN (
				SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
					FROM
					( 	
									
						SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
						,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
						INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
						ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
						WHERE prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
				) PRE	
				WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
				
			) AS  PREMER
			ON (CAR."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa)			
			LEFT JOIN 	"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente" AS VCUOTA ON CAR."IdInstrumento" = VCUOTA."IdInstrumento" AND VCUOTA."FechaVigencia" = v_FechaConsulta		
					
					WHERE CAR."IdEmpresa" = p_IdEmpresa 
					AND	  CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF','PAC','FWD') 
					AND (p_TipoFechaConsulta = 'LIQ' OR p_TipoFechaConsulta = 'ADQ' )				  
					AND	   CAR."IdEstado" ='VIG';
				 END IF;


		v_tblCustodia =
			SELECT  'CUSTODIA' AS "TipoCartera","IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
			    "Nemotecnico", "IdEmisor", "EsFungible", "IdCliente", "IdPortafolio", 
			    case when v_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda", 
			    case when v_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion","IdMoneda","FechaOperacion","FechaVencimiento",
			    cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
			    ,coalesce(sum("INGTasaMercadoActual"),0)/count(1) as "INGTasaMercadoActual"
			    ,coalesce(sum("INGPrecioMercadoActual"),0)/count(1) as "INGPrecioMercadoActual"
			    ,coalesce(sum("ValorMercado"),0) as "ValorMercado"
				,coalesce(sum("MontoMercado"),0) as "MontoMercado",
				"PrecioMercadoCustodia",
			    "IdTipoOperacion"
			FROM :v_tblCustodia
			group by "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
					"Nemotecnico", "IdEmisor","EsFungible", "IdCliente", "IdPortafolio","IdMoneda", "FechaVencimiento","FechaOperacion",
					"PrecioMercadoCustodia","IdTipoOperacion",
					case when v_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end,
					case when v_AgrupaPorRelacion = 'S' then "Relacion" else '' end;					 
	 end; --custodia

	v_tblCustodia = SELECT 
						CUS."TipoCartera",
						CUS."IdClasificacionTipoInstrumento",
						CUS."IdTipoInstrumento",
						CUS."IdInstrumento",
						CUS."Nemotecnico",
						CUS."IdEmisor",
						CUS."EsFungible",
						CUS."IdCliente",
						CUS."IdPortafolio",
						CUS."IdBoveda",
						CUS."Relacion",
						CUS."IdMoneda",
						CUS."FechaOperacion",
						CUS."FechaVencimiento",
						CUS."Cantidad",
						CUS."INGTasaMercadoActual",
						CASE
							WHEN CUS."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO') THEN 
								CUS."INGPrecioMercadoActual" * COALESCE(PF."Valor", 1)
							WHEN CUS."IdMoneda" = 'USD' THEN 
								CUS."INGPrecioMercadoActual" * v_DO
							ELSE
								CUS."INGPrecioMercadoActual"
						END AS "INGPrecioMercadoActual",
						CUS."ValorMercado",
						CUS."MontoMercado",
						CASE
							WHEN CUS."IdTipoInstrumento" = 'CFI' THEN 
								COALESCE(CFI."ValorRegistro", CUS."PrecioMercadoCustodia")
							WHEN CUS."IdTipoInstrumento" IN ('ACX', 'ETX') THEN 
								COALESCE(ACX_ETX."ValorRegistro", CUS."PrecioMercadoCustodia")
							ELSE
								CUS."PrecioMercadoCustodia"
						END * 
						CASE 
							WHEN CUS."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO') THEN COALESCE(PF."Valor", 1)
							WHEN CUS."IdMoneda" = 'USD' THEN COALESCE(v_DO, 1)
							ELSE 1
						END
						AS "PrecioMercadoCustodia",
						"IdTipoOperacion"
					FROM  :v_tblCustodia CUS LEFT JOIN :v_tblPrecioCFI CFI ON CUS."Nemotecnico" = CFI."Nemotecnico"
				 	LEFT JOIN :v_tblPrecioACXETX ACX_ETX ON ACX_ETX."Nemotecnico" = CUS."Nemotecnico"
					LEFT JOIN :v_tblParametrosFinancieros PF ON PF."IdParametroFinanciero" = CUS."IdMoneda";


	----------------------------------------------------------
	-- Inicio Carteras Negativas (Por ahora solo mercado RF)
	----------------------------------------------------------
	BEGIN
		IF  TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') < TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN      
			v_tblCustodiaNegativa = 
				SELECT  
					'CUSTODIA_NEGATIVA' AS "TipoCartera",
					ClasInst."IdClasificacionTipoInstrumento", 
					TipoInstrumento."IdTipoInstrumento", 
					CAR."IdInstrumento", 
					CASE WHEN ClasInst."IdClasificacionTipoInstrumento" = 'IF' THEN  CAR."NemoSVS" ELSE CAR."Nemotecnico"  END AS "Nemotecnico", 
					CAR."IdEmisor",
					CASE WHEN ClasInst."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."EsFungible"  ELSE 'N' END AS "EsFungible", 
					CAR."IdCliente", 
					CAR."IdPortafolio", 
					CAR."IdBoveda", 
					CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 THEN 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion",
					CASE WHEN IFNULL(CAR."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento" ELSE IFNULL(CAR."IdTipoReajuste",'') END AS "IdMoneda",
					CAR."FechaOperacion"  AS "FechaOperacion",
					INS."FechaVencimiento" AS "FechaVencimiento",
					CASE WHEN ClasInst."IdClasificacionTipoInstrumento" = 'IF' THEN CAST(IFNULL(CAR."ValorRescate",0) AS DECIMAL(26,8)) ELSE CAST(IFNULL(CAR."Cantidad",0) AS DECIMAL(26,8)) END AS "Cantidad",
					CAR."TasaMercado" AS "INGTasaMercadoActual",
					CAR."PrecioMercado" AS "INGPrecioMercadoActual",
					CAR."MontoMercadoMonedaInstrumento" AS "ValorMercado",
					CAR."MontoMercadoMonedaInstrumento" AS "MontoMercado",
					PREMER."PrecioCierre" AS "PrecioMercadoCustodia",
					CAR."IdTipoOperacion"
				FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" AS CAR
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS	ON INS."IdInstrumento" = CAR."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INS."IdTipoInstrumento" 
				LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento"
				INNER JOIN :v_TablaCarteras PC 		ON (PC."IdCliente"=CAR."IdCliente" AND PC."IdPortafolio"=CAR."IdPortafolio" OR (PC."IdCliente" IS NULL AND PC."IdPortafolio" IS NULL))	
				INNER JOIN :v_TablaInstrumentos PI 	ON (PI."IdClasificacionTipoInstrumento"=ClasInst."IdClasificacionTipoInstrumento" OR PI."IdClasificacionTipoInstrumento" IS NULL  )	
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN (
					SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
						FROM
						( 	
										
							SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
							,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
							FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
							INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
							ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
							WHERE prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
						) PRE	
					WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
				) AS  PREMER ON (CAR."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa )
				LEFT JOIN 	"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente" AS VCUOTA ON CAR."IdInstrumento" = VCUOTA."IdInstrumento" AND VCUOTA."FechaVigencia" = v_FechaConsulta
				WHERE 
					CAR."IdEmpresa" = p_IdEmpresa 
					AND TipoInstrumento."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF','PAC','FWD') 
					AND (p_TipoFechaConsulta = 'LIQ' OR p_TipoFechaConsulta = 'ADQ'	)
					AND CAR."FechaRespaldo" = v_FechaConsulta
					AND	CAR."IdEstado" ='VIG';
		ELSE
			v_tblCustodiaNegativa =
				SELECT  
					'CUSTODIA_NEGATIVA' AS "TipoCartera",
					ClasInst."IdClasificacionTipoInstrumento", 
					TipoInstrumento."IdTipoInstrumento", 
					CAR."IdInstrumento", 
					CASE WHEN ClasInst."IdClasificacionTipoInstrumento" = 'IF' THEN  CAR."NemoSVS" ELSE CAR."Nemotecnico"  END AS "Nemotecnico", 
					CAR."IdEmisor",
					CASE WHEN ClasInst."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."EsFungible"  ELSE 'N' END AS "EsFungible", 
					CAR."IdCliente", 
					CAR."IdPortafolio", 
					CAR."IdBoveda", 
					CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 THEN 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion",
					CASE WHEN IFNULL(CAR."IdMonedaInstrumento",'') <> '' THEN CAR."IdMonedaInstrumento" ELSE IFNULL(CAR."IdTipoReajuste",'') END AS "IdMoneda",
					CAR."FechaOperacion"  AS "FechaOperacion",
					INS."FechaVencimiento" AS "FechaVencimiento",
					CASE WHEN ClasInst."IdClasificacionTipoInstrumento" = 'IF' THEN CAST(IFNULL(CAR."ValorRescate",0) AS DECIMAL(26,8)) ELSE CAST(IFNULL(CAR."Cantidad",0) AS DECIMAL(26,8)) END AS "Cantidad",
					CAR."TasaMercado" AS "INGTasaMercadoActual",
					CAR."PrecioMercado" AS "INGPrecioMercadoActual",
					CAR."MontoMercadoMonedaInstrumento" AS "ValorMercado",
					CAR."MontoMercadoMonedaInstrumento" AS "MontoMercado",
					PREMER."PrecioCierre" AS "PrecioMercadoCustodia",
					CAR."IdTipoOperacion"
				FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" AS CAR
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS	ON INS."IdInstrumento" = CAR."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INS."IdTipoInstrumento" 
				LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento"
				INNER JOIN :v_TablaCarteras PC 		ON (PC."IdCliente"=CAR."IdCliente" AND PC."IdPortafolio"=CAR."IdPortafolio" OR (PC."IdCliente" IS NULL AND PC."IdPortafolio" IS NULL))	
				INNER JOIN :v_TablaInstrumentos PI 	ON (PI."IdClasificacionTipoInstrumento"=ClasInst."IdClasificacionTipoInstrumento" OR PI."IdClasificacionTipoInstrumento" IS NULL  )	
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN (
					SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
						FROM
						( 				
							SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
							,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
							FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
							INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
							ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
							WHERE prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
						) PRE	
					WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
				) AS  PREMER ON (CAR."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa)			
				LEFT JOIN 	"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente" AS VCUOTA ON CAR."IdInstrumento" = VCUOTA."IdInstrumento" AND VCUOTA."FechaVigencia" = v_FechaConsulta			
				WHERE 
					CAR."IdEmpresa" = p_IdEmpresa 
					AND	TipoInstrumento."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF','PAC','FWD') 
					AND (p_TipoFechaConsulta = 'LIQ' OR p_TipoFechaConsulta = 'ADQ' )				  
					AND CAR."IdEstado" ='VIG';
		END IF;

	v_tblCustodiaNegativa =
		SELECT  'CUSTODIA_NEGATIVA' AS "TipoCartera","IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
			"Nemotecnico", "IdEmisor", "EsFungible", "IdCliente", "IdPortafolio", 
			case when v_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda", 
			case when v_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion","IdMoneda","FechaOperacion","FechaVencimiento",
			cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
			,coalesce(sum("INGTasaMercadoActual"),0)/count(1) as "INGTasaMercadoActual"
			,coalesce(sum("INGPrecioMercadoActual"),0)/count(1) as "INGPrecioMercadoActual"
			,coalesce(sum("ValorMercado"),0) as "ValorMercado"
			,coalesce(sum("MontoMercado"),0) as "MontoMercado",
			"PrecioMercadoCustodia",
			"IdTipoOperacion"
		FROM :v_tblCustodiaNegativa
		group by "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", 
				"Nemotecnico", "IdEmisor","EsFungible", "IdCliente", "IdPortafolio","IdMoneda", "FechaVencimiento","FechaOperacion",
				"PrecioMercadoCustodia","IdTipoOperacion",
				case when v_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end,
				case when v_AgrupaPorRelacion = 'S' then "Relacion" else '' end;					 
	END;

	v_tblCustodiaNegativa = 
		SELECT 
			CUS."TipoCartera",
			CUS."IdClasificacionTipoInstrumento",
			CUS."IdTipoInstrumento",
			CUS."IdInstrumento",
			CUS."Nemotecnico",
			CUS."IdEmisor",
			CUS."EsFungible",
			CUS."IdCliente",
			CUS."IdPortafolio",
			CUS."IdBoveda",
			CUS."Relacion",
			CUS."IdMoneda",
			CUS."FechaOperacion",
			CUS."FechaVencimiento",
			CUS."Cantidad",
			CUS."INGTasaMercadoActual",
			CASE
				WHEN CUS."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO') THEN 
					CUS."INGPrecioMercadoActual" * COALESCE(PF."Valor", 1)
				WHEN CUS."IdMoneda" = 'USD' THEN 
					CUS."INGPrecioMercadoActual" * v_DO
				ELSE
					CUS."INGPrecioMercadoActual"
			END AS "INGPrecioMercadoActual",
			CUS."ValorMercado",
			CUS."MontoMercado",
			CASE
				WHEN CUS."IdTipoInstrumento" = 'CFI' THEN 
					COALESCE(CFI."ValorRegistro", CUS."PrecioMercadoCustodia")
				WHEN CUS."IdTipoInstrumento" IN ('ACX', 'ETX') THEN 
					COALESCE(ACX_ETX."ValorRegistro", CUS."PrecioMercadoCustodia")
				ELSE
					CUS."PrecioMercadoCustodia"
			END * 
			CASE 
				WHEN CUS."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO') THEN COALESCE(PF."Valor", 1)
				WHEN CUS."IdMoneda" = 'USD' THEN COALESCE(v_DO, 1)
				ELSE 1
			END
			AS "PrecioMercadoCustodia",
			"IdTipoOperacion"
		FROM  :v_tblCustodiaNegativa CUS LEFT JOIN :v_tblPrecioCFI CFI ON CUS."Nemotecnico" = CFI."Nemotecnico"
		LEFT JOIN :v_tblPrecioACXETX ACX_ETX ON ACX_ETX."Nemotecnico" = CUS."Nemotecnico"
		LEFT JOIN :v_tblParametrosFinancieros PF ON PF."IdParametroFinanciero" = CUS."IdMoneda";


	--------------------------------------
	-- Fin Carteras Negativas
	--------------------------------------


	--------------------------------------
	-- Garantias
	--------------------------------------
	 begin 
	 	-- Detalle
		
		 IF TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') < TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN
		 v_tblGarantias =
			SELECT  'GARANTIAS' AS "TipoCartera",
				GAR."IdClasificacionTipoInstrumento", 
				GAR."IdTipoInstrumento", 
				GAR."IdInstrumento", 
				case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico", 
				GAR."IdEmisor",
				case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."EsFungible" else 'N' end as "EsFungible",
				GAR."IdCliente", GAR."IdPortafolio", 
				IFNULL(car."IdBoveda",'') as "IdBoveda",
				CASE
					WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"	
				,GAR."IdMonedaInstrumento"AS "IdMoneda"		
				,GAR."INGFechaOperacion"  AS "FechaOperacion"																												
				,GAR."FechaVencimientoInstrumento" AS "FechaVencimiento"											
				,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			    ,case when GAR."IdClasificacionTipoInstrumento" in ('IF','RF') then GAR."INGTasaMercadoActual"
					else 0 end as "INGTasaMercadoActual"				
				,case when GAR."IdClasificacionTipoInstrumento" in ('RV','FM') then GAR."INGPrecioMercadoActual"
					else 0 end as "INGPrecioMercadoActual"
				,GAR."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado",
				GAR."INGMontoATasaMercadoActual" AS "MontoMercado",
				COALESCE(NULLIF(PREMER."PrecioCierre", 0), GAR."INGPrecioMercadoActual") AS "PrecioMercadoCustodia",
				CAR."IdTipoOperacion"
			FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
			INNER JOIN :v_TablaCarteras PC 		ON (PC."IdCliente"=GAR."IdCliente" AND PC."IdPortafolio"=GAR."IdPortafolio" OR (PC."IdCliente" IS NULL AND PC."IdPortafolio" IS NULL))	
			INNER JOIN :v_TablaInstrumentos PI 	ON (PI."IdClasificacionTipoInstrumento"=GAR."IdClasificacionTipoInstrumento" OR PI."IdClasificacionTipoInstrumento" IS NULL )	
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = GAR."IdCliente")
			LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as car on car."IdCartera" = GAR."IdCarteraOrigen"
			LEFT JOIN (SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
					FROM
					( 	
									
						SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
						,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
						INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
						ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
						WHERE prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
				) PRE	
				WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
				
			) AS  PREMER
			ON (GAR."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa)			
					
			WHERE GAR."IdEmpresa" = p_IdEmpresa AND GAR."IdEstado" = 'VIG' 
			AND (p_TipoFechaConsulta = 'LIQ' OR p_TipoFechaConsulta = 'ADQ')
		    AND  GAR."FechaRespaldo" = v_FechaConsulta;
		 ELSE
		v_tblGarantias =
			SELECT  'GARANTIAS' AS "TipoCartera",
				GAR."IdClasificacionTipoInstrumento", 
				GAR."IdTipoInstrumento", 
				GAR."IdInstrumento", 
				case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico", 
				GAR."IdEmisor",
				case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."EsFungible" else 'N' end as "EsFungible",
				GAR."IdCliente", GAR."IdPortafolio", 
				IFNULL(car."IdBoveda",'') as "IdBoveda",
				CASE
					WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"	
				,GAR."IdMonedaInstrumento"AS "IdMoneda"		
				,GAR."INGFechaOperacion"  AS "FechaOperacion"																												
				,GAR."FechaVencimientoInstrumento" AS "FechaVencimiento"											
				,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			    ,case when GAR."IdClasificacionTipoInstrumento" in ('IF','RF') then GAR."INGTasaMercadoActual"
					else 0 end as "INGTasaMercadoActual"				
				,case when GAR."IdClasificacionTipoInstrumento" in ('RV','FM') then GAR."INGPrecioMercadoActual"
					else 0 end as "INGPrecioMercadoActual"
				,GAR."INGMontoMercadoActualMonedaInstrumento" AS "ValorMercado",
				GAR."INGMontoATasaMercadoActual" AS "MontoMercado",
				COALESCE(NULLIF(PREMER."PrecioCierre", 0), GAR."INGPrecioMercadoActual") AS "PrecioMercadoCustodia",
				CAR."IdTipoOperacion"
			FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
			INNER JOIN :v_TablaCarteras PC 		ON (PC."IdCliente"=GAR."IdCliente" AND PC."IdPortafolio"=GAR."IdPortafolio" OR (PC."IdCliente" IS NULL AND PC."IdPortafolio" IS NULL))	
			INNER JOIN :v_TablaInstrumentos PI 	ON (PI."IdClasificacionTipoInstrumento"=GAR."IdClasificacionTipoInstrumento" OR PI."IdClasificacionTipoInstrumento" IS NULL )	
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = GAR."IdCliente")
			LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as car on car."IdCartera" = GAR."IdCarteraOrigen"
			LEFT JOIN  (SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
					FROM
					( 	
									
						SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
						,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
						FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
						INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
						ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
						WHERE prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
				) PRE	
				WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
				
			) AS  PREMER
			ON (GAR."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa)			
					
			WHERE GAR."IdEmpresa" = p_IdEmpresa AND
				  GAR."IdEstado" = 'VIG' AND (p_TipoFechaConsulta = 'LIQ' OR p_TipoFechaConsulta = 'ADQ');
		  END IF;	
	 end; --garantias

	v_tblGarantias = SELECT 
						GAR."TipoCartera",
						GAR."IdClasificacionTipoInstrumento",
						GAR."IdTipoInstrumento",
						GAR."IdInstrumento",
						GAR."Nemotecnico",
						GAR."IdEmisor",
						GAR."EsFungible",
						GAR."IdCliente",
						GAR."IdPortafolio",
						GAR."IdBoveda",
						GAR."Relacion",
						GAR."IdMoneda",
						GAR."FechaOperacion",
						GAR."FechaVencimiento",
						GAR."Cantidad",
						GAR."INGTasaMercadoActual",
						CASE
							WHEN GAR."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO') THEN 
								GAR."INGPrecioMercadoActual" * PF."Valor"
							WHEN GAR."IdMoneda" = 'USD' THEN 
								GAR."INGPrecioMercadoActual" * v_DO
							ELSE
								GAR."INGPrecioMercadoActual"
						END AS "INGPrecioMercadoActual",
						CASE
							WHEN GAR."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO') THEN 
								GAR."INGPrecioMercadoActual" * PF."Valor" * GAR."Cantidad"
							WHEN GAR."IdMoneda" = 'USD' THEN 
								GAR."INGPrecioMercadoActual" * v_DO * GAR."Cantidad"
							ELSE
								GAR."ValorMercado"
						END AS "ValorMercado",
						GAR."MontoMercado",
						CASE
							WHEN GAR."IdTipoInstrumento" = 'CFI' THEN 
								COALESCE(CFI."ValorRegistro", GAR."PrecioMercadoCustodia")
							WHEN GAR."IdTipoInstrumento" IN ('ACX', 'ETX') THEN 
								COALESCE(ACX_ETX."ValorRegistro", GAR."PrecioMercadoCustodia")
							ELSE
								GAR."PrecioMercadoCustodia"
						END * 
						CASE 
							WHEN GAR."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO') THEN PF."Valor"
							WHEN GAR."IdMoneda" = 'USD' THEN v_DO
							ELSE 1
						END
						AS "PrecioMercadoCustodia",
						"IdTipoOperacion"
					FROM  :v_tblGarantias GAR LEFT JOIN :v_tblPrecioCFI CFI ON GAR."Nemotecnico" = CFI."Nemotecnico"
					LEFT JOIN :v_tblPrecioACXETX ACX_ETX ON ACX_ETX."Nemotecnico" = GAR."Nemotecnico"
					LEFT JOIN :v_tblParametrosFinancieros PF ON PF."IdParametroFinanciero" = GAR."IdMoneda";

	-- Instrumentos de los cuales el portafolio tiene custodia
	v_tblInst =	 
	SELECT Distinct a."TipoCartera",a."IdClasificacionTipoInstrumento", a."IdTipoInstrumento", a."IdInstrumento", a."Nemotecnico",a."IdEmisor",a."EsFungible",
		a."IdCliente",a."IdPortafolio", CLI."NombreCompleto" as "NombreCliente", CLI."DocumentoIdentidad", a."IdBoveda", a."Relacion",a."IdMoneda",a."FechaOperacion",  a."FechaVencimiento",a."INGPrecioMercadoActual", a."PrecioMercadoCustodia", a."IdTipoOperacion"
	FROM
	(
		SELECT distinct "TipoCartera","IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "Relacion","IdMoneda", "FechaOperacion","FechaVencimiento","INGPrecioMercadoActual", "PrecioMercadoCustodia", "IdTipoOperacion" FROM :v_tblCustodia UNION ALL
		SELECT distinct "TipoCartera","IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "Relacion","IdMoneda", "FechaOperacion","FechaVencimiento","INGPrecioMercadoActual", "PrecioMercadoCustodia", "IdTipoOperacion" FROM :v_tblCustodiaNegativa UNION ALL
		SELECT distinct "TipoCartera","IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdBoveda", "Relacion","IdMoneda", "FechaOperacion","FechaVencimiento","INGPrecioMercadoActual", "PrecioMercadoCustodia", "IdTipoOperacion" FROM :v_tblGarantias
	) as a
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = a."IdCliente");





	 
	------------------------------------------
	-- SALIDA
	------------------------------------------

	p_out_tbl = 
	SELECT * FROM (
		SELECT cus."TipoCartera" AS "TipoCartera", 
		ins."IdClasificacionTipoInstrumento"
				, inst2."NombreClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento"
				,inst2."NombreTipoInstrumento"
				,ins."IdInstrumento"
				,ins."Nemotecnico"
				,ins."IdEmisor"
				,ins."EsFungible"
				,ins."IdCliente"
				,ins."IdPortafolio"
				,ins."DocumentoIdentidad"
				,ins."NombreCliente"
				,Port."CodigoPortafolio"
				,ins."IdBoveda"
 				,ins."Relacion"
 				,cast((sum(cus."INGTasaMercadoActual")/count(1))  as DECIMAL(26,8) ) AS "TasaMercadoActual"
 				,ins."IdMoneda"
 				,ins."FechaOperacion"
 				,ins."FechaVencimiento"	
				, cast(sum(ifnull( cus."Cantidad",0)) as DECIMAL(26,8)) as "CantidadCustodia"
				, 0 as "CantidadGarantia"
				,(CASE ins."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(cast((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1))  as DECIMAL(26,8) ),cast((sum(cus."INGPrecioMercadoActual")/count(1))  as DECIMAL(26,8) ) )  ELSE cast((sum(ifnull(cus."INGPrecioMercadoActual",0))/count(1))  as DECIMAL(26,8) ) END ) AS "PrecioMercadoCustodia"
				,(CASE ins."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1)),0 )=0 THEN cast(sum(cus."ValorMercado") as DECIMAL(26,8) ) ELSE cast( COALESCE((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1)),0 ) * sum(ifnull( cus."Cantidad",0)) as DECIMAL(26,8)) END)  
				 ELSE cast(sum(cus."ValorMercado") as DECIMAL(26,8) ) END) AS "ValorMercadoCustodia"
			    ,cast(sum(cus."MontoMercado") as DECIMAL(26,8) ) 			 as "MontoMercadoCustodia",
				ins."IdTipoOperacion"
		FROM :v_tblInst as ins
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as inst2 on ins."IdInstrumento" = inst2."IdInstrumento"
		left join :v_tblCustodia AS cus    on cus."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and cus."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and cus."IdInstrumento" = ins."IdInstrumento"
		                                  and cus."Nemotecnico" = ins."Nemotecnico"
		                                  and cus."EsFungible" = ins."EsFungible"
		                                  and cus."IdCliente" = ins."IdCliente"
		                                  and cus."IdPortafolio" = ins."IdPortafolio"
		                                  and cus."IdBoveda" = ins."IdBoveda"
		                                  and cus."Relacion" = ins."Relacion"
		                                  and cus."IdMoneda"=ins."IdMoneda"
		                                  and cus."FechaOperacion"=ins."FechaOperacion"
		                                  and cus."FechaVencimiento"=ins."FechaVencimiento"
		                                  and cus."INGPrecioMercadoActual"=ins."INGPrecioMercadoActual"
		                                  and cus."TipoCartera"=ins."TipoCartera"
		                                  and cus."IdTipoOperacion"=ins."IdTipoOperacion" 
	
		inner JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = ins."IdPortafolio" 			
	    group by ins."IdClasificacionTipoInstrumento", inst2."NombreClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento", inst2."NombreTipoInstrumento"
				,ins."IdInstrumento", ins."Nemotecnico", ins."IdEmisor", ins."EsFungible", ins."IdCliente", ins."IdPortafolio",ins."DocumentoIdentidad", ins."NombreCliente", Port."CodigoPortafolio"
				,ins."IdBoveda", ins."Relacion", ins."IdMoneda",ins."FechaOperacion"	, ins."FechaVencimiento",ins."INGPrecioMercadoActual", cus."TipoCartera", ins."IdTipoOperacion"	
	    order by ins."IdClasificacionTipoInstrumento", ins."IdTipoInstrumento", ins."Nemotecnico", ins."IdEmisor", ins."EsFungible", ins."IdCliente", ins."IdPortafolio" ,ins."DocumentoIdentidad", ins."NombreCliente",Port."CodigoPortafolio", ins."IdBoveda", ins."Relacion"
	    )
	UNION ALL
	SELECT * FROM (
		SELECT cus."TipoCartera" AS "TipoCartera", 
		ins."IdClasificacionTipoInstrumento"
				, inst2."NombreClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento"
				,inst2."NombreTipoInstrumento"
				,ins."IdInstrumento"
				,ins."Nemotecnico"
				,ins."IdEmisor"
				,ins."EsFungible"
				,ins."IdCliente"
				,ins."IdPortafolio"
				,ins."DocumentoIdentidad"
				,ins."NombreCliente"
				,Port."CodigoPortafolio"
				,ins."IdBoveda"
 				,ins."Relacion"
 				,cast((sum(cus."INGTasaMercadoActual")/count(1))  as DECIMAL(26,8) ) AS "TasaMercadoActual"
 				,ins."IdMoneda"
 				,ins."FechaOperacion"
 				,ins."FechaVencimiento"	
				, cast(sum(ifnull( cus."Cantidad",0)) as DECIMAL(26,8)) as "CantidadCustodia"
				, 0 as "CantidadGarantia"
				,(CASE ins."IdClasificacionTipoInstrumento" WHEN 'RV' THEN 
					COALESCE(cast((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1))  as DECIMAL(26,8) ),cast((sum(cus."INGPrecioMercadoActual")/count(1))  as DECIMAL(26,8) ) )  
					ELSE cast((sum(ifnull(cus."INGPrecioMercadoActual",0))/count(1))  as DECIMAL(26,8) )
					 END ) AS "PrecioMercadoCustodia"
				,(CASE ins."IdClasificacionTipoInstrumento" WHEN 'RV' THEN 
					(CASE WHEN COALESCE((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1)),0 )=0 
						THEN cast(sum(cus."ValorMercado") as DECIMAL(26,8) ) 
						ELSE cast( COALESCE((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1)),0 ) * sum(ifnull( cus."Cantidad",0))  as DECIMAL(26,8)) 
					END)  
				 ELSE cast(sum(cus."ValorMercado") as DECIMAL(26,8) ) 
				 END) AS "ValorMercadoCustodia"
			    ,cast(sum(cus."MontoMercado") as DECIMAL(26,8) ) 			 as "MontoMercadoCustodia"
				,ins."IdTipoOperacion"
		FROM :v_tblInst as ins
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as inst2 on ins."IdInstrumento" = inst2."IdInstrumento"
		left join :v_tblCustodiaNegativa AS cus      on cus."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
													and cus."IdTipoInstrumento" = ins."IdTipoInstrumento"
													and cus."IdInstrumento" = ins."IdInstrumento"
													and cus."Nemotecnico" = ins."Nemotecnico"
													and cus."EsFungible" = ins."EsFungible"
													and cus."IdCliente" = ins."IdCliente"
													and cus."IdPortafolio" = ins."IdPortafolio"
													and cus."IdBoveda" = ins."IdBoveda"
													and cus."Relacion" = ins."Relacion"
													and cus."IdMoneda"=ins."IdMoneda"
													and cus."FechaOperacion"=ins."FechaOperacion"
													and cus."FechaVencimiento"=ins."FechaVencimiento"
													and cus."INGPrecioMercadoActual"=ins."INGPrecioMercadoActual"
													and cus."TipoCartera"=ins."TipoCartera"
													and cus."IdTipoOperacion"=ins."IdTipoOperacion"
		inner JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = ins."IdPortafolio" 			
	    group by ins."IdClasificacionTipoInstrumento", inst2."NombreClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento", inst2."NombreTipoInstrumento"
				,ins."IdInstrumento", ins."Nemotecnico", ins."IdEmisor", ins."EsFungible", ins."IdCliente", ins."IdPortafolio",ins."DocumentoIdentidad", ins."NombreCliente", Port."CodigoPortafolio"
				,ins."IdBoveda", ins."Relacion", ins."IdMoneda",ins."FechaOperacion"	, ins."FechaVencimiento",ins."INGPrecioMercadoActual", cus."TipoCartera", ins."IdTipoOperacion"	
	    order by ins."IdClasificacionTipoInstrumento", ins."IdTipoInstrumento", ins."Nemotecnico", ins."IdEmisor", ins."EsFungible", ins."IdCliente", ins."IdPortafolio" ,ins."DocumentoIdentidad", ins."NombreCliente",Port."CodigoPortafolio", ins."IdBoveda", ins."Relacion"
	)
	UNION ALL
	SELECT * FROM (
			SELECT gar."TipoCartera" AS "TipoCartera", 
				ins."IdClasificacionTipoInstrumento"
				,inst2."NombreClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento"
				,inst2."NombreTipoInstrumento"
				,ins."IdInstrumento"
				,ins."Nemotecnico"
				,ins."IdEmisor"
				,ins."EsFungible"
				,ins."IdCliente"
				,ins."IdPortafolio"
				,ins."DocumentoIdentidad"
				,ins."NombreCliente"
				,Port."CodigoPortafolio"
				,ins."IdBoveda"
 				,ins."Relacion"
 				,cast((sum(gar."INGTasaMercadoActual")/count(1))  as DECIMAL(26,8) ) AS "TasaMercadoActual"
 				,ins."IdMoneda"
 				,ins."FechaOperacion"
 				,ins."FechaVencimiento"	
				,0 as "CantidadCustodia"
				,cast(sum(ifnull( gar."Cantidad",0)) as DECIMAL(26,8)) as "CantidadGarantia"
				,(CASE ins."IdClasificacionTipoInstrumento" WHEN 'RV' THEN COALESCE(cast((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1))  as DECIMAL(26,8) ),cast((sum(ifnull(gar."INGPrecioMercadoActual",0))/count(1))  as DECIMAL(26,8) ) )  ELSE cast((sum(ifnull(gar."INGPrecioMercadoActual",0))/count(1))  as DECIMAL(26,8) ) END ) AS "PrecioMercadoCustodia"
				,(CASE ins."IdClasificacionTipoInstrumento" WHEN 'RV' THEN (CASE WHEN COALESCE((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1)),0 )=0 THEN cast(sum(gar."ValorMercado") as DECIMAL(26,8) ) ELSE cast( COALESCE((sum(ifnull(ins."PrecioMercadoCustodia",0))/count(1)),0 ) * sum(ifnull( gar."Cantidad",0)) as DECIMAL(26,8)) END)  
				 ELSE cast(sum(gar."ValorMercado") as DECIMAL(26,8) ) END) AS "ValorMercadoCustodia"
			    ,cast(sum(gar."MontoMercado") as DECIMAL(26,8) ) 			 as "MontoMercadoCustodia"
			    ,ins."IdTipoOperacion"
		FROM :v_tblInst as ins
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as inst2 on ins."IdInstrumento" = inst2."IdInstrumento"
		left join :v_tblGarantias as gar   on gar."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and gar."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and gar."IdInstrumento" = ins."IdInstrumento"
		                                  and gar."IdCliente" = ins."IdCliente"
		                                  and gar."Nemotecnico" = ins."Nemotecnico"
		                                  and gar."EsFungible" = ins."EsFungible"
		                                  and gar."IdPortafolio" = ins."IdPortafolio"
		                                  and gar."IdBoveda" = ins."IdBoveda"
		                                  and gar."Relacion" = ins."Relacion"
		                                  and gar."IdMoneda"=ins."IdMoneda"	
		                                  and gar."FechaOperacion"=ins."FechaOperacion"		                                  	                                  
		                                  and gar."FechaVencimiento"=ins."FechaVencimiento"		                                  
		                                  and gar."INGPrecioMercadoActual"=ins."INGPrecioMercadoActual"
		                                  and gar."TipoCartera"=ins."TipoCartera"
										  and gar."IdTipoOperacion"=ins."IdTipoOperacion"
	
		inner JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = ins."IdPortafolio" 			
	    group by ins."IdClasificacionTipoInstrumento", inst2."NombreClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento", inst2."NombreTipoInstrumento"
				,ins."IdInstrumento", ins."Nemotecnico", ins."IdEmisor", ins."EsFungible", ins."IdCliente", ins."IdPortafolio",ins."DocumentoIdentidad", ins."NombreCliente", Port."CodigoPortafolio"
				,ins."IdBoveda", ins."Relacion", ins."IdMoneda",ins."FechaOperacion"	, ins."FechaVencimiento",ins."INGPrecioMercadoActual", gar."TipoCartera",ins."IdTipoOperacion"	
	    order by ins."IdClasificacionTipoInstrumento", ins."IdTipoInstrumento", ins."Nemotecnico", ins."IdEmisor", ins."EsFungible", ins."IdCliente", ins."IdPortafolio" ,ins."DocumentoIdentidad", ins."NombreCliente",Port."CodigoPortafolio", ins."IdBoveda", ins."Relacion"
	    );

	    SELECT * FROM :v_tblTrasnEgr;

END;
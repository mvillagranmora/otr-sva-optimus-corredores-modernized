SET SCHEMA "BSCL";
go
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCustodiaGeneral";
go
CREATE PROCEDURE
-- 
--  Proposito: Consulta custodia general por instrumento(tambien llamado custodia oficina)
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 30/11/2016  
-- 
"BSCL.Custodia.BD.Servicios::ConsultarCustodiaGeneral" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                		BIGINT,
    IN p_IdUsuario                		BIGINT,
    IN p_IdModulo                 		NVARCHAR(3), 
	IN p_FechaConsulta 					NVARCHAR(10) DEFAULT '0001-01-01',	
--	IN p_TipoFechaConsulta				NVARCHAR(3)  DEFAULT 'LIQ',   -- LIQ: Filtrar por Fecha Liquidación   ADQ: Filtrar por Fecha Adquisición
	IN p_TipoFechaConsulta				NVARCHAR(3),   -- LIQ: Filtrar por Fecha Liquidación   ADQ: Filtrar por Fecha Adquisición

	IN p_IdClasificacionTipoInstrumento NVARCHAR(3),
	IN p_IdTipoInstrumento 				NVARCHAR(3),
	IN p_IdInstrumento 					BIGINT 		DEFAULT 0,
	IN p_Nemotecnico					NVARCHAR(40) DEFAULT '',
	IN p_AgrupaPorBoveda 				NVARCHAR(1) DEFAULT 'N', -- S: usa boveda como un criterio más de agrupacion y la muestra   N: no considera bóveda en la agrupacion
	IN p_IdBoveda 						NVARCHAR(3) DEFAULT '',	 -- solo debe venir si p_AgrupaPorBoveda = N
	IN p_AgrupaPorRelacion 				NVARCHAR(1) DEFAULT 'N'  -- S: usa relacion como un criterio más de agrupacion y la muestra   N: no considera relacion en la agrupacion

   -- Parametros de Salida
   	,OUT p_out_tbl 						TABLE (
									   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
									   		,"NombreClasificacionTipoInstrumento" 	NVARCHAR(100)
									   		,"IdTipoInstrumento" 					NVARCHAR(3)
									   		,"NombreTipoInstrumento" 				NVARCHAR(100)
									   		,"IdInstrumento" 						BIGINT
									  		,"Nemotecnico" 							NVARCHAR(40)
									  		,"ISIN"									NVARCHAR(40)
									  		,"IdEmisor" 							NVARCHAR(40)
									  		,"IdBoveda" 							NVARCHAR(3)
									  		,"NumeroCuentaBoveda"					NVARCHAR(40)
									  		,"Relacion" 							NVARCHAR(40) -- REL / TER / CPRO
											,"CantidadCustodia" 					DECIMAL(26,8)
											,"CantidadSimulCom" 					DECIMAL(26,8)
											,"CantidadSimulVen" 					DECIMAL(26,8)
											,"CantidadPrestamo" 					DECIMAL(26,8)
											,"CantidadEmpresti" 					DECIMAL(26,8)
											,"CantidadGarantia" 					DECIMAL(26,8)
											,"CantidadTransIng" 					DECIMAL(26,8)
											,"CantidadTransEgr" 					DECIMAL(26,8)
											,"TotalPosicion"    					DECIMAL(26,8)
											,"MontoValorizado"    					DECIMAL(26,8)
											,"CantidadPactoVenta"    					DECIMAL(26,8)
											,"CantidadPactoCompra"    					DECIMAL(26,8)
									   	  ) default empty
    
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	DECLARE v_DocumentoIdentidadCuentaPropia   NVARCHAR(40);
	DECLARE v_IdPersonaCuentaPropia            BIGINT;
	DECLARE v_IdClienteCuentaPropia            BIGINT;			
	DECLARE v_FechaConsulta 			       NVARCHAR(10) := '2300-12-31';
	DECLARE v_FechaSistema  			       NVARCHAR(10);
	DECLARE v_AdvertenciasContador  		   INTEGER;
	DECLARE v_ErroresContador  				   INTEGER;

	DECLARE v_tblInst TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
		,"Relacion"                         	NVARCHAR(20)
	 );


	DECLARE v_tblCustodia TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)
		,"Relacion"                         	NVARCHAR(20)
	 );
	 
	DECLARE v_tblSimulC TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		
		,"Relacion"                         	NVARCHAR(20)
	 );
	 
	DECLARE v_tblSimulV TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		
		,"Relacion"                         	NVARCHAR(20)
	 );

	DECLARE v_tblPrestamos TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		   		
		,"Relacion"                         	NVARCHAR(20)
	 );
	 
	DECLARE v_tblEmprestitos TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)
		,"Relacion"                         	NVARCHAR(20)
	 );
	 
	DECLARE v_tblGarantias TABLE (
   		 "IdClasificacionTipoInstrumento"		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)
		,"Relacion"                         	NVARCHAR(20)
	 );
	 
	DECLARE v_tblTrasnIng TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8) 
		,"Relacion"                         	NVARCHAR(20)
	 );
	 
	DECLARE v_tblTrasnEgr TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		   		
		,"Relacion"                         	NVARCHAR(20)
	 );

	 DECLARE v_tblPactoVen TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		   		
		,"Relacion"                         	NVARCHAR(20)
		,"ClienteOpera"                         NVARCHAR(50)
	 );

	 DECLARE v_tblPactoCom TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		   		
		,"Relacion"                         	NVARCHAR(20)
		,"ClienteOpera"                         NVARCHAR(50)
	 );

	 DECLARE v_tblEmpInt TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		   		
		,"Relacion"                         	NVARCHAR(20)
	 );

	 DECLARE v_tblReporVen TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		   		
		,"Relacion"                         	NVARCHAR(20)
	 );
	
	DECLARE v_tblReporCom TABLE (
   		 "IdClasificacionTipoInstrumento" 		NVARCHAR(3)
   		,"IdTipoInstrumento" 					NVARCHAR(3)
   		,"IdInstrumento" 						BIGINT
   		,"Nemotecnico" 							NVARCHAR(40)
   		,"ISIN"									NVARCHAR(40)
   		,"IdBoveda" 							NVARCHAR(3)
   		,"Cantidad" 							DECIMAL(26,8)
   		,"MontoValorizado"						DECIMAL(26,8)   		   		
		,"Relacion"                         	NVARCHAR(20)
	 );

	IF p_FechaConsulta = '0001-01-01' THEN
		Select top 1 "Valor" INTO v_FechaConsulta FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = :p_IdEmpresa;
	ELSE
		v_FechaConsulta := p_FechaConsulta;
	END IF;

	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(
		p_IdEmpresa
		, v_FechaSistema
	);


	-- Obtiene los datos de la corredora(para cuenta propia)	
	SELECT "IdPersona", "IdCliente", "DocumentoIdentidad" 
	INTO v_IdPersonaCuentaPropia, v_IdClienteCuentaPropia, v_DocumentoIdentidadCuentaPropia 
	FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
	WHERE "IdEmpresa" = :p_IdEmpresa
	GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad";

	
	-- Obtiene los relacionados a la corredora
	v_tblRelacionados = 
	SELECT CLI."IdPersona" AS "IdPersonaDestino" 
	FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON IND."IdEntidad" = CLI."IdCliente"
	LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO ON IND."IdTipoIndicador" = TPO."IdTipoIndicador"
	WHERE TPO."IdTipoEntidad" = 'CLIENTE' AND TPO."CodigoTipoIndicador"  = 'RELCORR' AND IND."ValorIndicador" = 'S';


	----------------------------------------------------------
	---- inicializa las tablas de paso
	v_tblCustodia    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblSimulC      = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblSimulV      = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblPrestamos   = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblEmprestitos = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblGarantias   = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblTrasnIng    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblTrasnEgr    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblPactoVen    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
						, '' AS "ClienteOpera"
					   from DUMMY where 1=2;
	v_tblPactoCom    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
						, '' AS "ClienteOpera"
					   from DUMMY where 1=2;
	v_tblEmpInt    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblReporVen    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
	v_tblReporCom    = select  '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico", '' as "ISIN", '' as "IdBoveda", 0 as "Cantidad", 0 as "MontoValorizado", '' as "Relacion"
					   from DUMMY where 1=2;
			   



	------------------------------------------
	-- CUSTODIA
	------------------------------------------
	BEGIN

	IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN
			--CUSTODIA
			v_tblCustodia =
			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion" 
			FROM(
				--CUSTODIA NEGATIVA
				WITH custodia AS (
					SELECT
					   CLASINST."IdClasificacionTipoInstrumento"
					   ,inst3."IdTipoInstrumento" 
					   ,CAR."IdInstrumento"
					   ,CASE 
	       					WHEN CLASINST."IdClasificacionTipoInstrumento" = 'IF' 
	       						THEN CAR."NemoSVS" 
	       						ELSE CAR."Nemotecnico" 
	       					END	 														AS "Nemotecnico"
					   ,inst3."ISIN"	
					   ,CASE 
	       					WHEN p_AgrupaPorBoveda = 'N' 
	       						THEN 'DCV' 
	       						ELSE COALESCE(CAR."IdBoveda",'DCV') 
	       					END 														AS "IdBoveda" 
	       				,CAST(IFNULL(
	       					CASE WHEN CLASINST."IdClasificacionTipoInstrumento" = 'IF' 
	       						THEN  CAR."ValorRescate" 
	       						ELSE  CAR."Cantidad"
	       				END,0) AS DECIMAL(26,8)) 										AS "Cantidad"
	       				,0 as "MontoValorizado"	
	       				,CASE WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
										WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
										ELSE 'TER'                                                  -- Tercero
										END AS "Relacion"
						, CAR."FechaOperacion" as "INGFechaOperacion"
						, CAR."FechaLiquidacion" AS "INGFechaLiquidacion"
						, CAR."IdEmpresa"
					FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" 					AS CAR      
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 			AS CLI 				ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")  
					LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"			AS Portafolio	 	ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 	AS MatrizAgrupador 	ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI 		ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 		AS inst3 			ON inst3."IdInstrumento" = CAR."IdInstrumento"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV"AS MAT_NOAPV 		ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" 			AS MADOR			ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INST3."IdTipoInstrumento" 
					LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento" 
					WHERE CAR."IdEmpresa" = p_IdEmpresa 
					AND CAR."IdEstado" = 'VIG' 
					AND CLASINST."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE CLASINST."IdClasificacionTipoInstrumento" END 
					AND TipoInstrumento."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE TipoInstrumento."IdTipoInstrumento" END 
					AND CAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE CAR."IdInstrumento" END 
					AND CAR."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE CAR."Nemotecnico" END 
					AND CAR."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE CAR."IdBoveda" END	
				)
				SELECT  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", 				
							case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda", 							
							(SUM("Cantidad")*-1) as "Cantidad",
							SUM("MontoValorizado") as "MontoValorizado",
							case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion" 
				FROM custodia
				where custodia."IdEmpresa" = p_IdEmpresa 
				AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND custodia."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND custodia."INGFechaOperacion" <= v_FechaConsulta)
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN",
				case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,
				case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			
				UNION ALL
				
				--CUSTODIA
				SELECT  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", 				
							case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda", 				
							SUM("Cantidad") as "Cantidad",
							SUM("MontoValorizado") as "MontoValorizado",
							case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion" 
				FROM(
					SELECT
						CAR."IdClasificacionTipoInstrumento"
					   ,CAR."IdTipoInstrumento"
					   ,CAR."IdInstrumento"
					   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END	AS "Nemotecnico"
					   ,inst3."ISIN"
					   ,CASE WHEN CAR."IdBoveda" = '' THEN 'DCV' ELSE CAR."IdBoveda" END AS "IdBoveda"            
					   ,CAST(IFNULL(CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN  CAR."INGValorRescate" - COALESCE (resTot."IngValorRescate",0)ELSE  CAR."INGCantidad"- COALESCE (resTot."IngCantidad",0) END,0) AS DECIMAL(26,8)) AS "Cantidad"       
					   ,cast(IFNULL(CAR."INGMontoATasaMercadoActual",0) as DECIMAL(26,8)) as "MontoValorizado"
					   ,CASE WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
									WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
									ELSE 'TER'                                                  -- Tercero
									END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.Cartera" 					AS CAR 	
					LEFT JOIN (
						SELECT RES."FolioCarteraOrigen"
							,SUM("IngCantidad") AS "IngCantidad"
							,SUM("IngValorRescate") AS "IngValorRescate"
							,SUM("MontoValorizado") AS "IngMontoValorizado"
						FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES 
						WHERE RES."IdEstado"='VIG' AND RES."EgrCantidad" = 0
						GROUP BY RES."FolioCarteraOrigen") 
						AS resTot ON resTot."FolioCarteraOrigen" = CAR."FolioCartera"           
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 			AS CLI 				ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")  
					LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"			AS Portafolio	 	ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 	AS MatrizAgrupador 	ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI 		ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 		AS inst3 			ON inst3."IdInstrumento" = CAR."IdInstrumento"
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" 	AS REG 				ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV"AS MAT_NOAPV 		ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" 			AS MADOR			ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
	    
					WHERE CAR."IdEmpresa" = p_IdEmpresa
					AND CAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE CAR."IdClasificacionTipoInstrumento" END 
					AND CAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE CAR."IdTipoInstrumento" END 
					AND CAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE CAR."IdInstrumento" END 
					AND CAR."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE CAR."Nemotecnico" END 
					AND CAR."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE CAR."IdBoveda" END
					AND CAR."IdEstado" = 'VIG'
					AND CAR."IdTipoInstrumento" NOT IN ('PRT', 'PRF', 'EMP', 'ERF', 
					'SIM', 'PAC', 'REP', 'RRV', 'RRF', 'FWD', 'FDO', 'FIP', 'FBC', 'FIC', 'FUF', 'IOA', 'IOE', 'IOI', 'IOD', 'IOB', 'IOC', 'IOU', 'IOM', 'IOF', 'FBO', 'DER')
					AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidación						
								v_FechaConsulta BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end 
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND v_FechaConsulta < case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end
						   ))
							OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición

								v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaOperacion",'0001-01-01') end 
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND v_FechaConsulta < case when IFNULL(CAR."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') end
						   )) 
					)
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN",
				case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,
				case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END

				UNION ALL

				--COMPRA_PACTO NOMINAL(S)
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, (SUM("Cantidad")*-1) as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
				SELECT 
					SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,SUB."IdInstrumento" as "IdInstrumento"
				   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
				   ,inst3."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"
				   ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 
   						CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
				   ELSE 0 END	AS "Cantidad" 
				   , 0 as "MontoValorizado"
				   ,CASE
					WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
				LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
				LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND POR."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
				WHERE CAR."IdEmpresa" = p_IdEmpresa
				AND CAR."IdEstado" = 'VIG'
				AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
				)
				AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
				AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
				AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
				AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
				AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
				AND CAR."IdTipoInstrumento" = 'PAC'
				AND CAR."IdTipoOperacion"   = 'VENPAC'
				AND SUB."IdEstado"='VIG'
				AND RES."FolioCarteraOrigen"=0
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,				
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END

				UNION ALL

				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, (SUM("Cantidad")*-1) as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
				SELECT 
					SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,SUB."IdInstrumento" as "IdInstrumento"
				   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
				   ,inst3."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"	
				   ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 
   						CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
				   ELSE 0 END	AS "Cantidad" 
				   , 0 as "MontoValorizado"
				   ,CASE
					WHEN CAR."IdClienteRelacionado" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
				LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
				LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND POR."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
				WHERE CAR."IdEmpresa" = p_IdEmpresa
				AND CAR."IdEstado" = 'VIG'
				AND	CAR."IdClienteRelacionado" = :v_IdClienteCuentaPropia
				AND	CAR."IdPortafolioRelacionado" = CAR."IdPortafolioRelacionado"
				AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
				)
				AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
				AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
				AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
				AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
				AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
				AND CAR."IdTipoInstrumento" = 'PAC'
				AND CAR."IdTipoOperacion"   = 'COMPAC'
				AND SUB."IdEstado"='VIG'
				AND RES."FolioCarteraOrigen"=0
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,				
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END

				UNION ALL

				--INGRESOS TRANSITO
				SELECT  "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN" 				
						, "IdBoveda"
						, SUM("Cantidad") as "Cantidad"
						, SUM("MontoValorizado") as "MontoValorizado"
						, "Relacion"
				FROM(
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento"
						,MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"  
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND MOV."EsVentaDescubierta" = 'N'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'INGRESO' AND  MOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
						AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				
				--EGRESOS TRANSITO
				UNION ALL
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						MOV."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
					   ,MOV."IdTipoInstrumento" AS "IdTipoInstrumento" 
					   ,MOV."IdInstrumento" AS "IdInstrumento"
					   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND MOV."EsVentaDescubierta" = 'N'
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'RETIRO' AND  MOV."EsNominal" = 'N'
							)
						AND MOV."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE MOV."IdClasificacionTipoInstrumento" END 
						AND MOV."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE MOV."IdTipoInstrumento" END 
						AND MOV."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE MOV."IdInstrumento" END 
						AND MOV."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE MOV."Nemotecnico" END 
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
		
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion"
				
				UNION ALL

				--VENTA_CORTA_CON_CUSTODIA
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, (SUM("Cantidad")*-1) as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
				WITH 
					CarteraPrestamo AS (
						SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS "ultimaCartera",
								"IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio",
								"IdTipoInstrumento", "IdTipoPrestamo", "IdEstado", "IdEmpresa", "Cantidad", "INGCantidad",
								"INGFechaLiquidacion", "INGFechaOperacion", "IdBoveda", "IdMonedaSistema", "IdMonedaInstrumento",
								"EGRFechaOperacion"
						FROM "BSCL.Custodia.BD::CUS.Cartera"
						WHERE "IdTipoInstrumento" IN ('PRT','PRF')
						  AND "IdTipoPrestamo" IN ('PRTVTC','PRVCRF')		
						  AND "IdEstado"        	= 'VIG'
						  AND "IdEmpresa" 			=p_IdEmpresa
					),
					SubyacentePrestamo AS (
						SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdSubyacente" DESC) AS "ultimoSubyacente",
								sb.*
						FROM "BSCL.Custodia.BD::CUS.Subyacente" AS sb
						INNER JOIN CarteraPrestamo as car on car."FolioCartera" = sb."FolioCarteraRelacionada"
						WHERE car."ultimaCartera" = 1
					),
					IndicadorContratoCustodia AS (
						SELECT  ATR_CC."IdEntidad" AS "IdCliente",
								ATR_CC."ValorIndicador" AS "ContratoCustodia"
						FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
						INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
								ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
							   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
						WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
					),
					consultarAsignacionesPrestamo AS (
						SELECT 
							 prt."FolioCartera" as "FolioCarteraPrestamo"
							,AVG(prt."Cantidad") as "CantidadPrestamo"
							,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
							,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
						FROM CarteraPrestamo as prt
						LEFT JOIN  "BSCL.Custodia.BD::CUS.Cartera" as emp on 
							prt."IdEmpresa" = emp."IdEmpresa" and
							prt."FolioCartera" = emp."FolioCarteraRelacionada" and
							emp."IdTipoInstrumento" IN ('EMP','ERF') and
							emp."IdEstado" = 'VIG' and
							emp."FolioCarteraRelacionada" > 0
						WHERE prt."ultimaCartera" = 1
						GROUP BY  prt."IdEmpresa", prt."FolioCartera"
					)	
				SELECT 
						sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
					   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
					   ,sby."IdInstrumento" as "IdInstrumento"
					   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
					   ,INS."ISIN" 
					   ,CASE 
	   						WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						WHEN p_AgrupaPorBoveda='S' AND IFNULL(PRE."IdBoveda",'')<>'' THEN PRE."IdBoveda" 
	   						ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CASE
	   						WHEN PRE."INGCantidad" = ASIG."CantidadAsignada" THEN 0
	   						ELSE ASIG."CantidadPorAsignar"
					   END AS "Cantidad"
					   , 0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
				FROM CarteraPrestamo AS PRE
				INNER JOIN SubyacentePrestamo as sby on (PRE."FolioCartera" = sby."FolioCarteraRelacionada") AND "ultimoSubyacente" = 1
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = sby."IdInstrumento"
				INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
				INNER JOIN consultarAsignacionesPrestamo ASIG ON ASIG."FolioCarteraPrestamo" = PRE."FolioCartera"
				LEFT JOIN IndicadorContratoCustodia IND ON IND."IdCliente" = PRE."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = PRE."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
				WHERE PRE."ultimaCartera" = 1
				AND COALESCE(IND."ContratoCustodia",'N') = 'S'
				AND
				(									
					   (p_TipoFechaConsulta = 'LIQ' AND PRE."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND PRE."INGFechaOperacion" <= v_FechaConsulta)
				)
				AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
				AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
				AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
				AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
				AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end

				UNION ALL

				--ING_TRANSITO_NOMINAL
				SELECT 
					"IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"	   
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"	   
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   , 0 as "MontoValorizado"
					   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")		
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''						
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
						AND	(p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
							v_FechaConsulta >= MOV."FechaLiquidacion")
							OR
							(p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
							v_FechaConsulta >= MOV."FechaOperacion")
						))
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND COALESCE(MatrizAgrupador."IdProductoAgrupador", 0) = 0
						AND 
							(
							CLI."IdCliente" <> :v_IdClienteCuentaPropia AND MOV."IdTipoOperacion" IN ('VENPAC','VENTER') AND TipoOperacion."AccionCustodia" IN('INGRESO','RETIRO') 
							AND MOV."EsNominal" = 'S'			
							OR
							TipoOperacion."AccionCustodia" IN('INGRESO') AND MOV."EsNominal" = 'S' AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV')
							)					
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
										case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
										case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
				
				UNION ALL

				--RETIRO_TRANSITO_NOMINAL
				SELECT 
					"IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, (SUM("Cantidad")*-1) as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"	   
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"	   
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   , 0 as "MontoValorizado"
					   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")		
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''						
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
						AND	(p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
							v_FechaConsulta >= MOV."FechaLiquidacion")
							OR
							(p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
							v_FechaConsulta >= MOV."FechaOperacion")
						))
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND COALESCE(MatrizAgrupador."IdProductoAgrupador", 0) = 0				
						AND 
							(
							CLI."IdCliente" <> :v_IdClienteCuentaPropia AND MOV."IdTipoOperacion" IN ('COMPAC','COMTER') AND TipoOperacion."AccionCustodia" IN('INGRESO','RETIRO') 
							AND MOV."EsNominal" = 'S'			
							OR
							TipoOperacion."AccionCustodia" IN('RETIRO') AND MOV."EsNominal" = 'S' AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV')
							)
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
										case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
										case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END			

				UNION ALL

				--EGRESOS TRANSITO VENTA DESCUBIERTA
				
				SELECT  "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN" 				
						, "IdBoveda"
						, (SUM("Cantidad")*-1) as "Cantidad"
						, SUM("MontoValorizado") as "MontoValorizado"
						, "Relacion"
				FROM(
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento"
						,MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"  
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND MOV."EsVentaDescubierta" = 'S'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'INGRESO' AND  MOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
						AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				--EGRESOS TRANSITO
				UNION ALL
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						MOV."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
					   ,MOV."IdTipoInstrumento" AS "IdTipoInstrumento" 
					   ,MOV."IdInstrumento" AS "IdInstrumento"
					   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND MOV."EsVentaDescubierta" = 'S'
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'RETIRO' AND  MOV."EsNominal" = 'N'
							)
						AND MOV."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE MOV."IdClasificacionTipoInstrumento" END 
						AND MOV."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE MOV."IdTipoInstrumento" END 
						AND MOV."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE MOV."IdInstrumento" END 
						AND MOV."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE MOV."Nemotecnico" END 
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
		
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion"
				
				UNION ALL

				--ING_TRANSITO_SIMULTANEAS_NOMINAL
				SELECT 
					"IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"	   
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"	   
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   , 0 as "MontoValorizado"
					   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")		
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
						AND	(p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
							v_FechaConsulta >= MOV."FechaLiquidacion")
							OR
							(p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
							v_FechaConsulta >= MOV."FechaOperacion")
						))
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND COALESCE(MatrizAgrupador."IdProductoAgrupador", 0) = 0
						AND MOV."IdTipoOperacion" IN ('COMSIM') 
						AND TipoOperacion."AccionCustodia" IN('INGRESO') 
						AND MOV."EsNominal" = 'S'
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
										case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
										case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END						
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";

	Else

		v_tblCustodia = 
			--CUSTODIA HISTORICA
			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion" 
			FROM(
			--CUSTODIA NEGATIVA HISTORICA
			WITH custodia AS (
				SELECT
				   CLASINST."IdClasificacionTipoInstrumento"   
				   ,inst3."IdTipoInstrumento" 
				   ,CAR."IdInstrumento"
				   ,CASE 
       					WHEN CLASINST."IdClasificacionTipoInstrumento" = 'IF' 
       						THEN CAR."NemoSVS" 
       						ELSE CAR."Nemotecnico" 
       					END	 														AS "Nemotecnico"
				  ,inst3."ISIN"		
				  ,CASE 
       					WHEN p_AgrupaPorBoveda = 'N' 
       						THEN 'DCV' 
       						ELSE COALESCE(CAR."IdBoveda",'DCV') 
       					END 														AS "IdBoveda" 
				   ,CAST(IFNULL(
       					CASE WHEN CLASINST."IdClasificacionTipoInstrumento" = 'IF' 
       						THEN  CAR."ValorRescate" 
       						ELSE  CAR."Cantidad"
       				END,0) AS DECIMAL(26,8)) 										AS "Cantidad"		
       				,0 as "MontoValorizado"	
  					,CASE WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
									WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
									ELSE 'TER'                                                  -- Tercero
									END AS "Relacion"
					, CAR."FechaOperacion" as "INGFechaOperacion"
					, CAR."FechaLiquidacion" AS "INGFechaLiquidacion"
					, CAR."IdEmpresa"
				FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" 					AS CAR
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" 			AS CLI 				ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio"			AS Portafolio	 	ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" 	AS MatrizAgrupador 	ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI 		ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 		AS inst3 			ON inst3."IdInstrumento" = CAR."IdInstrumento"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV"AS MAT_NOAPV 		ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" 			AS MADOR			ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = INST3."IdTipoInstrumento" 
				LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = TipoInstrumento."IdClasificacionTipoInstrumento" 
    
				WHERE CAR."IdEmpresa" = p_IdEmpresa
				AND CAR."IdEstado" = 'VIG'
				AND CAR."FechaRespaldo" = v_FechaConsulta
				AND CLASINST."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE CLASINST."IdClasificacionTipoInstrumento" END 
				AND TipoInstrumento."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE TipoInstrumento."IdTipoInstrumento" END 
				AND CAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE CAR."IdInstrumento" END 
				AND CAR."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE CAR."Nemotecnico" END 
				AND CAR."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE CAR."IdBoveda" END

			)
			SELECT  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", 				
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda", 						
						(SUM("Cantidad")*-1) as "Cantidad",
						SUM("MontoValorizado") as "MontoValorizado",
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion" 
			FROM custodia
			where custodia."IdEmpresa" = p_IdEmpresa
			AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND custodia."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND custodia."INGFechaOperacion" <= v_FechaConsulta)
				)				
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN",
				case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END, 
				case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END

			UNION ALL

			--CUSTODIA HISTORICA
			SELECT  "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", 				
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda", 			 				
						SUM("Cantidad") as "Cantidad",
						SUM("MontoValorizado") as "MontoValorizado",
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion" 
						FROM(	
			SELECT
				   CAR."IdClasificacionTipoInstrumento"
				   ,CAR."IdTipoInstrumento"
				   ,CAR."IdInstrumento"
				   ,Case when CAR."IdClasificacionTipoInstrumento" = 'IF' then CAR."NemoSVS" else CAR."Nemotecnico" end as "Nemotecnico"
				   ,inst3."ISIN"
				   ,CASE WHEN CAR."IdBoveda" = '' THEN 'DCV' ELSE CAR."IdBoveda" END AS "IdBoveda"        
					,CAST(IFNULL(CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN  
       					CAR."INGValorRescate" - COALESCE (resTot."IngValorRescate",0)
       				ELSE 
       					CAR."INGCantidad"- COALESCE (resTot."IngCantidad",0)  END,0) AS DECIMAL(26,8)) AS "Cantidad"
				   ,CAST(IFNULL(CAR."INGMontoATasaMercadoActual",0) as DECIMAL(26,8)) as "MontoValorizado"
				   ,CASE WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
									WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
									ELSE 'TER'                                                  -- Tercero
									END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
				LEFT JOIN (
					SELECT RES."FolioCarteraOrigen"
						,SUM("IngCantidad") AS "IngCantidad"
						,SUM("IngValorRescate") AS "IngValorRescate"
						,SUM("MontoValorizado") AS "IngMontoValorizado"
					FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES 
					WHERE RES."IdEstado"='VIG' AND RES."EgrCantidad" = 0
					GROUP BY RES."FolioCarteraOrigen") 
					AS resTot ON resTot."FolioCarteraOrigen" = CAR."FolioCartera"       
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = CAR."IdInstrumento"
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE CAR."IdEmpresa" = p_IdEmpresa
				AND CAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE CAR."IdClasificacionTipoInstrumento" END 
				AND CAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE CAR."IdTipoInstrumento" END 
				AND CAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE CAR."IdInstrumento" END 
				AND CAR."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE CAR."Nemotecnico" END 
				AND CAR."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE CAR."IdBoveda" END
				AND CAR."IdEstado" = 'VIG'
				AND CAR."FechaRespaldo" = v_FechaConsulta
				AND CAR."IdTipoInstrumento" NOT IN ('PRT', 'EMP', 'SIM', 'PAC', 'REP', 'RRV', 'RRF', 'FWD', 'FDO', 'FIP', 'FBC', 'FIC', 'FUF', 'IOA', 'IOE', 'IOI', 'IOD', 'IOB', 'IOC', 'IOU', 'IOM', 'IOF', 'FBO', 'DER')
				AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidación						
								v_FechaConsulta BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end 
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND v_FechaConsulta < case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end
						   ))
							OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición

								v_FechaConsulta BETWEEN "INGFechaOperacion" and case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaOperacion",'0001-01-01') end 
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND v_FechaConsulta < case when IFNULL(CAR."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') end
						   )) 
					)
				)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN",
				case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END, 
				case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END


			UNION ALL

			--INGRESOS TRANSITO HISTORICO
			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion"
			FROM(
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT
					COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
				   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
				   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
				   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
				   ,inst4."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   					 ELSE 'DCV' 
	   				END AS "IdBoveda"
				   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   ,0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
					LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"  
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
					LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE
						HMOV."IdEmpresa" = p_IdEmpresa
					AND rel."EsVentaDescubierta" = 'N'		
					AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
					AND	HMOV."IdEstado" = 'PEN'
					AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
					AND
						(
							TipoOperacion."AccionCustodia" = 'INGRESO' AND  HMOV."EsNominal" = 'N'
						)
					AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
					AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
					AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
					AND	COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") END
					AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
					AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
					AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
						
			UNION ALL
			
			--EGRESOS TRANSITO HISTORICO
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT
					COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
				   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
				   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
				   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
				   ,inst4."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   					 ELSE 'DCV' 
	   				END AS "IdBoveda"
				   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
				   ,0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
				INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON HMOV."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito"
					LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
					LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE
						HMOV."IdEmpresa" = p_IdEmpresa
					AND rel."EsVentaDescubierta" = 'N'
					AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
					AND	HMOV."IdEstado" = 'PEN'
					AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
					AND
						(
							TipoOperacion."AccionCustodia" = 'RETIRO' AND  HMOV."EsNominal" = 'N'
						)
					AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
					AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
					AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
					AND	COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") END
					AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
					AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
					AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion"

			UNION ALL

			--VENTA_CORTA_CON_CUSTODIA_HIS
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, (SUM("Cantidad")*-1) as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			WITH 
				CarteraPrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS "ultimaCartera",
							"IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio",
							"IdTipoInstrumento", "IdEstado", "IdEmpresa", "Cantidad", "INGCantidad",
							"INGFechaLiquidacion", "INGFechaOperacion", "IdBoveda", "IdMonedaSistema", "FechaRespaldo", 
							"IdMonedaInstrumento", "EGRFechaOperacion"
					FROM "BSCL.Custodia.BD::HIS.Cartera"
					WHERE "IdTipoInstrumento" IN ('PRT','PRF')
					  AND "Nemotecnico"  		NOT IN ('ADMINISTRATIVO','FINES GENERALES')	 
					  AND "IdEstado"        	= 'VIG'
					  AND "IdEmpresa" 			=1
				),
				SubyacentePrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdSubyacente" DESC) AS "ultimoSubyacente",
							sb.*
					FROM "BSCL.Custodia.BD::HIS.Subyacente" AS sb
					INNER JOIN CarteraPrestamo as car on car."FolioCartera" = sb."FolioCarteraRelacionada"
					WHERE car."ultimaCartera" = 1
				),
				IndicadorContratoCustodia AS (
					SELECT  ATR_CC."IdEntidad" AS "IdCliente",
							ATR_CC."ValorIndicador" AS "ContratoCustodia"
					FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
					INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
							ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
						   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
					WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
				),
				consultarAsignacionesPrestamo AS (
					SELECT 
						 prt."FolioCartera" as "FolioCarteraPrestamo"
						,AVG(prt."Cantidad") as "CantidadPrestamo"
						,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
						,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
					FROM CarteraPrestamo as prt
					LEFT JOIN  "BSCL.Custodia.BD::HIS.Cartera" as emp on 
						prt."IdEmpresa" = emp."IdEmpresa" and
						prt."FolioCartera" = emp."FolioCarteraRelacionada" and
						emp."IdTipoInstrumento" IN ('EMP','ERF') and
						emp."IdEstado" = 'VIG' and
						emp."FolioCarteraRelacionada" > 0
					WHERE prt."ultimaCartera" = 1
					GROUP BY  prt."IdEmpresa", prt."FolioCartera"
				)
			SELECT 
					sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,sby."IdInstrumento" as "IdInstrumento"
				   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
				   ,INS."ISIN"	   
				   ,CASE 
	   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					WHEN p_AgrupaPorBoveda='S' AND IFNULL(PRE."IdBoveda",'')<>'' THEN PRE."IdBoveda" 
	   					ELSE 'DCV' 
	   				END AS "IdBoveda"	   
				   ,CASE
	   					WHEN PRE."INGCantidad" = ASIG."CantidadAsignada" THEN 0
	   					ELSE ASIG."CantidadPorAsignar"
				   END AS "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
			FROM CarteraPrestamo AS PRE
			INNER JOIN SubyacentePrestamo as sby on (PRE."FolioCartera" = sby."FolioCarteraRelacionada") AND "ultimoSubyacente" = 1
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = sby."IdInstrumento"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
			INNER JOIN consultarAsignacionesPrestamo ASIG ON ASIG."FolioCarteraPrestamo" = PRE."FolioCartera"
			LEFT JOIN IndicadorContratoCustodia IND ON IND."IdCliente" = PRE."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = PRE."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
			WHERE PRE."ultimaCartera" 					= 1
			AND COALESCE(IND."ContratoCustodia",'N')	= 'S'
			AND	PRE."FechaRespaldo" = v_FechaConsulta
			AND
				(									
					   (p_TipoFechaConsulta = 'LIQ' AND PRE."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND PRE."INGFechaOperacion" <= v_FechaConsulta)
				)
			AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
			
			UNION ALL

			--ING_TRANSITO_NOMINAL_HISTORICO			
			SELECT 
				"IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT
					COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
				   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
				   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
				   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
				   ,inst4."ISIN"	   
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   					 ELSE 'DCV' 
	   				END AS "IdBoveda"	   
				   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
					WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS MOV
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OM ON OM."IdOperacion"=MOV."FolioComprobante"
					LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
					LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")		
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE MOV."IdEmpresa" = p_IdEmpresa
					AND MOV."FechaRespaldo" = v_FechaConsulta
					AND	MOV."IdEstado" = 'PEN'						
					AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''					
					AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
					AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
					AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
					AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
					AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
					AND	(p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
						v_FechaConsulta >= MOV."FechaLiquidacion")
						OR
						(p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
						v_FechaConsulta >= MOV."FechaOperacion")
					))
					AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 		
					AND COALESCE(MatrizAgrupador."IdProductoAgrupador", 0) = 0
					AND 
						(
						CLI."IdCliente" <> :v_IdClienteCuentaPropia AND MOV."IdTipoOperacion" IN ('VENPAC','VENTER') AND TipoOperacion."AccionCustodia" IN('INGRESO','RETIRO') 
						AND MOV."EsNominal" = 'S'			
						OR
						TipoOperacion."AccionCustodia" IN('INGRESO') AND MOV."EsNominal" = 'S' AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV')
						)
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
									case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
									case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
						
			UNION ALL

			--RET_TRANSITO_NOMINAL_HISTORICO
			SELECT 
				"IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, (SUM("Cantidad")*-1) as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT
					COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
				   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
				   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
				   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
				   ,inst4."ISIN"	   
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   					 ELSE 'DCV' 
	   				END AS "IdBoveda"	   
				   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
					WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS MOV
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OM ON OM."IdOperacion"=MOV."FolioComprobante"
					LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
					LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")		
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE MOV."IdEmpresa" = p_IdEmpresa
					AND MOV."FechaRespaldo" = v_FechaConsulta
					AND	MOV."IdEstado" = 'PEN'						
					AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''					
					AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
					AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
					AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
					AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
					AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
					AND	(p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
						v_FechaConsulta >= MOV."FechaLiquidacion")
						OR
						(p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
						v_FechaConsulta >= MOV."FechaOperacion")
					))
					AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 		
					AND COALESCE(MatrizAgrupador."IdProductoAgrupador", 0) = 0
					AND 
						(
						CLI."IdCliente" <> :v_IdClienteCuentaPropia AND MOV."IdTipoOperacion" IN ('COMPAC','COMTER') AND TipoOperacion."AccionCustodia" IN('INGRESO','RETIRO') 
						AND MOV."EsNominal" = 'S'			
						OR
						TipoOperacion."AccionCustodia" IN('RETIRO') AND MOV."EsNominal" = 'S' AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV')
						)
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
									case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
									case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END														

			UNION ALL
			
			--EGRESOS TRANSITO VENTA DESCUBIERTA HISTORICO 
			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, (SUM("Cantidad")*-1) as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion"
			FROM(			
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT
					COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
				   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
				   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
				   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
				   ,inst4."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   					 ELSE 'DCV' 
	   				END AS "IdBoveda"
				   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   ,0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
					LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"  
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
					LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE
						HMOV."IdEmpresa" = p_IdEmpresa
					AND rel."EsVentaDescubierta" = 'S'		
					AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
					AND	HMOV."IdEstado" = 'PEN'
					AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
					AND
						(
							TipoOperacion."AccionCustodia" = 'INGRESO' AND  HMOV."EsNominal" = 'N'
						)
					AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
					AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
					AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
					AND	COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") END
					AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
					AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
					AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
			--EGRESOS TRANSITO
			UNION ALL

			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT
					COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
				   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
				   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
				   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
				   ,inst4."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   					 ELSE 'DCV' 
	   				END AS "IdBoveda"
				   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
				   ,0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
				INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON HMOV."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito"
					LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
					LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE
						HMOV."IdEmpresa" = p_IdEmpresa
					AND rel."EsVentaDescubierta" = 'S'
					AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
					AND	HMOV."IdEstado" = 'PEN'
					AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
					AND
						(
							TipoOperacion."AccionCustodia" = 'RETIRO' AND  HMOV."EsNominal" = 'N'
						)
					AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
					AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
					AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
					AND	COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") END
					AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
					AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
					AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion"

			UNION ALL

			--ING_TRANSITO_SIMULTANEA_NOMINAL_HISTORICO
			SELECT 
				"IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT
					COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
				   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
				   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
				   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
				   ,inst4."ISIN"	   
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   					 ELSE 'DCV' 
	   				END AS "IdBoveda"	   
				   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
					WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS MOV
					LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OM ON OM."IdOperacion"=MOV."FolioComprobante"
					LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
					LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")		
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE MOV."IdEmpresa" = p_IdEmpresa
					AND MOV."FechaRespaldo" = v_FechaConsulta
					AND	MOV."IdEstado" = 'PEN'						
					AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''				
					AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
					AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
					AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
					AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
					AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
					AND	(p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidacion
						v_FechaConsulta >= MOV."FechaLiquidacion")
						OR
						(p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
						v_FechaConsulta >= MOV."FechaOperacion")
					))
					AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 		
					AND COALESCE(MatrizAgrupador."IdProductoAgrupador", 0) = 0
					AND MOV."IdTipoOperacion" IN ('COMSIM') 
					AND TipoOperacion."AccionCustodia" IN('INGRESO') 
					AND MOV."EsNominal" = 'S'
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
									case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
									case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END		
									
			UNION ALL
			
			--COMPRA_PACTO NOMINAL(S) HISTORICO
			SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, (SUM("Cantidad")*-1) as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
				(
				SELECT 
					SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,SUB."IdInstrumento" as "IdInstrumento"
				   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
				   ,inst3."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"
				   ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 
   						CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
				   ELSE 0 END	AS "Cantidad" 
				   , 0 as "MontoValorizado"
				   ,CASE
					WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
				INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as SUB on (SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."FechaRespaldo" = CAR."FechaRespaldo")
				LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")			
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
				WHERE CAR."IdEmpresa" = p_IdEmpresa
				AND	CAR."FechaRespaldo" = v_FechaConsulta
				AND CAR."IdEstado" = 'VIG'
				AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
				)
				AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
				AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
				AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
				AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
				AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
				AND CAR."IdTipoInstrumento" = 'PAC'
				AND CAR."IdTipoOperacion"   = 'VENPAC'
				AND SUB."IdEstado"='VIG'
				AND RES."FolioCarteraOrigen"=0
				)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,				
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END

			UNION ALL
				
			SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, (SUM("Cantidad")*-1) as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
				SELECT 
					SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,SUB."IdInstrumento" as "IdInstrumento"
				   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
				   ,inst3."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"	
				   ,CASE WHEN RES."FolioCarteraOrigen"=0 THEN 
   						CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 
				   ELSE 0 END	AS "Cantidad" 
				   , 0 as "MontoValorizado"
				   ,CASE
					WHEN CAR."IdClienteRelacionado" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
					WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
					ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
				INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as SUB on (SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."FechaRespaldo" = CAR."FechaRespaldo")
				LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")			
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
				WHERE CAR."IdEmpresa" = p_IdEmpresa
				AND	CAR."FechaRespaldo" = v_FechaConsulta
				AND CAR."IdEstado" = 'VIG'
				AND	CAR."IdClienteRelacionado" = :v_IdClienteCuentaPropia
				AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
				)
				AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
				AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
				AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
				AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
				AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
				AND CAR."IdTipoInstrumento" = 'PAC'
				AND CAR."IdTipoOperacion"   = 'COMPAC'
				AND SUB."IdEstado"='VIG'
				AND RES."FolioCarteraOrigen"=0
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,				
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";
		END IF;
	 END;

	--------------------------
	--Simultaneas de compra
	--------------------------
	 begin 
	 IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN
	 	--SIMULTANEAS COMPRA
		v_tblSimulC =
			SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(

				SELECT
					   SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento", 
					   SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento", 
					   SIM."suby_IdInstrumento" as "IdInstrumento"
					   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
					   ,inst3."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE SIM."suby_IdBoveda" END AS "IdBoveda"
					   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"       
					FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = SIM."IdCliente")
					LEFT JOIN :v_tblRelacionados				  as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")					
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio" AND Portafolio."IdCliente" = SIM."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SIM."suby_IdInstrumento"
					WHERE SIM."IdEmpresa" =p_IdEmpresa
					AND SIM."IdEstado" = 'VIG'        
					AND SIM."suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SIM."suby_IdClasificacionTipoInstrumento" END
					AND SIM."suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
					AND SIM."suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SIM."suby_IdInstrumento" END
					AND SIM."suby_Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SIM."suby_Nemotecnico" END 
					AND SIM."suby_IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE "suby_IdBoveda" END
					AND SIM."IdTipoOperacion" = 'COMSIM'  
					AND
							(
								   (p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidación
											v_FechaConsulta BETWEEN SIM."INGFechaLiquidacion" and case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end 						
										AND v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end 
								   ))
								OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
											v_FechaConsulta BETWEEN SIM."INGFechaOperacion" and case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end 						
										AND v_FechaConsulta < case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaOperacion",'0001-01-01') end 

									)) 
							)
					)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end;

	ELSE

		v_tblSimulC =
			--SIMULTANEAS COMPRA HISTORICO
			SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(

				SELECT
					   SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
					   , SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento"
					   , SIM."suby_IdInstrumento" as "IdInstrumento"
					   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
						,inst3."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE SIM."suby_IdBoveda" END AS "IdBoveda" 
					   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"       
					FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaHis" AS SIM
					LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = SIM."IdCliente")
					LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
					LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio" AND Portafolio."IdCliente" = SIM."IdCliente"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
					LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
					LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
					LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
					LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SIM."suby_IdInstrumento"
					WHERE SIM."IdEmpresa" =p_IdEmpresa
					AND	SIM."FechaRespaldo" = v_FechaConsulta
					AND	SIM."suby_FechaRespaldo" = v_FechaConsulta
					AND SIM."IdEstado" = 'VIG'
					AND SIM."suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SIM."suby_IdClasificacionTipoInstrumento" END
					AND SIM."suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
					AND SIM."suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SIM."suby_IdInstrumento" END
					AND SIM."suby_Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SIM."suby_Nemotecnico" END 
					AND SIM."suby_IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE "suby_IdBoveda" END
					AND SIM."IdTipoOperacion" = 'COMSIM'    
					AND
							(
								   (p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidación
											v_FechaConsulta BETWEEN SIM."INGFechaLiquidacion" and case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end 						
										AND v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end 
								   ))
								OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
											v_FechaConsulta BETWEEN SIM."INGFechaOperacion" and case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end 						
										AND v_FechaConsulta < case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaOperacion",'0001-01-01') end 

									)) 
							)
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end;

		END IF;		

	 end;


	--------------------------
	--Simultaneas de venta
	--------------------------
	 begin 

		IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN
	 	
			v_tblSimulV =
				--SIMULTANEAS VENTA
				SELECT "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN"
						, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
						, SUM("Cantidad") as "Cantidad"
						, SUM("MontoValorizado") AS "MontoValorizado"
						, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
					FROM
					(

					SELECT
						   SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento", 
						   SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento", 
						   SIM."suby_IdInstrumento" as "IdInstrumento"
						   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
						   ,inst3."ISIN"
						   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE SIM."suby_IdBoveda" END AS "IdBoveda"
						   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
						   ,0 as "MontoValorizado"
						   ,CASE
								WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
								WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
								ELSE 'TER'                                                  -- Tercero
							END AS "Relacion"       
						FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = SIM."IdCliente")
						LEFT JOIN :v_tblRelacionados				  as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")					
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio" AND Portafolio."IdCliente" = SIM."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SIM."suby_IdInstrumento"
						WHERE SIM."IdEmpresa" =p_IdEmpresa
						AND SIM."IdEstado" = 'VIG'        
						AND SIM."suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SIM."suby_IdClasificacionTipoInstrumento" END
						AND SIM."suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
						AND SIM."suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SIM."suby_IdInstrumento" END
						AND SIM."suby_Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SIM."suby_Nemotecnico" END 
						AND SIM."suby_IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE "suby_IdBoveda" END						
						AND SIM."IdTipoOperacion" = 'VENSIM'    
						AND
								(
									   (p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidación
												v_FechaConsulta BETWEEN SIM."INGFechaLiquidacion" and case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end 						
											AND v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end 
									   ))
									OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
												v_FechaConsulta BETWEEN SIM."INGFechaOperacion" and case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end 						
											AND v_FechaConsulta < case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaOperacion",'0001-01-01') end 

										)) 
								)
						)
					GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
							case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
							case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end;

		ELSE

			v_tblSimulV =
				--SIMULTANEAS VENTA HISTORICO
				SELECT "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN"
						, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
						, SUM("Cantidad") as "Cantidad"
						, SUM("MontoValorizado") AS "MontoValorizado"
						, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
					FROM
					(

					SELECT
						   SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
						   , SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento"
						   , SIM."suby_IdInstrumento" as "IdInstrumento"
						   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
							,inst3."ISIN"
						   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE SIM."suby_IdBoveda" END AS "IdBoveda" 
						   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
						   ,0 as "MontoValorizado"
						   ,CASE
								WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
								WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
								ELSE 'TER'                                                  -- Tercero
							END AS "Relacion"       
						FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaHis" AS SIM
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = SIM."IdCliente")
						LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio" AND Portafolio."IdCliente" = SIM."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SIM."suby_IdInstrumento"
						WHERE SIM."IdEmpresa" =p_IdEmpresa
						AND	SIM."FechaRespaldo" = v_FechaConsulta
						AND	SIM."suby_FechaRespaldo" = v_FechaConsulta
						AND SIM."IdEstado" = 'VIG'
						AND SIM."suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SIM."suby_IdClasificacionTipoInstrumento" END
						AND SIM."suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
						AND SIM."suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SIM."suby_IdInstrumento" END
						AND SIM."suby_Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SIM."suby_Nemotecnico" END 
						AND SIM."suby_IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE "suby_IdBoveda" END						
						AND SIM."IdTipoOperacion" = 'VENSIM'    
						AND
								(
									   (p_TipoFechaConsulta = 'LIQ' AND (   -- Filtra por Fecha Liquidación
												v_FechaConsulta BETWEEN SIM."INGFechaLiquidacion" and case when IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaLiquidacion",'0001-01-01') end 						
											AND v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end 
									   ))
									OR (p_TipoFechaConsulta = 'ADQ' AND (   -- Filtra por Fecha Adquisición
												v_FechaConsulta BETWEEN SIM."INGFechaOperacion" and case when IFNULL(SIM."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(SIM."EGRFechaOperacion",'0001-01-01') end 						
											AND v_FechaConsulta < case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaOperacion",'0001-01-01') end 

										)) 
								)
					)
					GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
							case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
							case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end;

			END IF;	 				
	 				
	 end;


	--------------------------
	--PRESTAMOS
	--------------------------
	 begin 
    
	 IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN
	 	
	 	v_tblPrestamos = 
			--PRESTAMOS 
			SELECT  "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN" 				
				, "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") as "MontoValorizado"
				, "Relacion" 
			FROM(
			--VENTA_CORTA_CON_CUSTODIA
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			WITH 
				CarteraPrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS "ultimaCartera",
							"IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio",
							"IdTipoInstrumento", "IdTipoPrestamo", "IdEstado", "IdEmpresa", "Cantidad", "INGCantidad",
							"INGFechaLiquidacion", "INGFechaOperacion", "IdBoveda", "IdMonedaSistema", "IdMonedaInstrumento",
							"EGRFechaOperacion"
					FROM "BSCL.Custodia.BD::CUS.Cartera"
					WHERE "IdTipoInstrumento" IN ('PRT','PRF')
					  AND "IdTipoPrestamo" IN ('PRTVTC','PRVCRF')		
					  AND "IdEstado"        	= 'VIG'
					  AND "IdEmpresa" 			=p_IdEmpresa
				),
				SubyacentePrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdSubyacente" DESC) AS "ultimoSubyacente",
							sb.*
					FROM "BSCL.Custodia.BD::CUS.Subyacente" AS sb
					INNER JOIN CarteraPrestamo as car on car."FolioCartera" = sb."FolioCarteraRelacionada"
					WHERE car."ultimaCartera" = 1
				),
				IndicadorContratoCustodia AS (
					SELECT  ATR_CC."IdEntidad" AS "IdCliente",
							ATR_CC."ValorIndicador" AS "ContratoCustodia"
					FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
					INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
							ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
						   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
					WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
				),
				consultarAsignacionesPrestamo AS (
					SELECT 
						 prt."FolioCartera" as "FolioCarteraPrestamo"
						,AVG(prt."Cantidad") as "CantidadPrestamo"
						,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
						,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
					FROM CarteraPrestamo as prt
					LEFT JOIN  "BSCL.Custodia.BD::CUS.Cartera" as emp on 
						prt."IdEmpresa" = emp."IdEmpresa" and
						prt."FolioCartera" = emp."FolioCarteraRelacionada" and
						emp."IdTipoInstrumento" IN ('EMP','ERF') and
						emp."IdEstado" = 'VIG' and
						emp."FolioCarteraRelacionada" > 0
					WHERE prt."ultimaCartera" = 1
					GROUP BY  prt."IdEmpresa", prt."FolioCartera"
				)	
			SELECT 
					sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,sby."IdInstrumento" as "IdInstrumento"
				   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
				   ,INS."ISIN" 
				   ,CASE 
	   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					WHEN p_AgrupaPorBoveda='S' AND IFNULL(PRE."IdBoveda",'')<>'' THEN PRE."IdBoveda" 
	   					ELSE 'DCV' 
	   				END AS "IdBoveda"
				   ,PRE."INGCantidad" AS "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
			FROM CarteraPrestamo AS PRE
			INNER JOIN SubyacentePrestamo as sby on (PRE."FolioCartera" = sby."FolioCarteraRelacionada") AND "ultimoSubyacente" = 1
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = sby."IdInstrumento"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
			INNER JOIN consultarAsignacionesPrestamo ASIG ON ASIG."FolioCarteraPrestamo" = PRE."FolioCartera"
			LEFT JOIN IndicadorContratoCustodia IND ON IND."IdCliente" = PRE."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = PRE."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
			WHERE PRE."ultimaCartera" = 1
			AND COALESCE(IND."ContratoCustodia",'N') = 'S'
			AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end							   
						   ))
						OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
						)) 
					)
			AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end

			UNION ALL

			--VENTA_CORTA_SIN_CUSTODIA
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			WITH 
				CarteraPrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS "ultimaCartera",
							"IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio",
							"IdTipoInstrumento", "IdTipoPrestamo", "IdEstado", "IdEmpresa", "Cantidad", "INGCantidad",
							"INGFechaLiquidacion", "INGFechaOperacion", "IdBoveda", "IdMonedaSistema", "IdMonedaInstrumento",
							"EGRFechaOperacion"
					FROM "BSCL.Custodia.BD::CUS.Cartera"
					WHERE "IdTipoInstrumento" IN ('PRT','PRF')	 
					  AND "IdTipoPrestamo" IN ('PRTVTC','PRVCRF')		 
					  AND "IdEstado"        	= 'VIG'
					  AND "IdEmpresa" 			=p_IdEmpresa
				),
				SubyacentePrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdSubyacente" DESC) AS "ultimoSubyacente",
							sb.*
					FROM "BSCL.Custodia.BD::CUS.Subyacente" AS sb
					INNER JOIN CarteraPrestamo as car on car."FolioCartera" = sb."FolioCarteraRelacionada"
					WHERE car."ultimaCartera" = 1
				),
				IndicadorContratoCustodia AS (
					SELECT  ATR_CC."IdEntidad" AS "IdCliente",
							ATR_CC."ValorIndicador" AS "ContratoCustodia"
					FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
					INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
							ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
						   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
					WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
				),
				consultarAsignacionesPrestamo AS (
					SELECT 
						 prt."FolioCartera" as "FolioCarteraPrestamo"
						,AVG(prt."Cantidad") as "CantidadPrestamo"
						,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
						,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
					FROM CarteraPrestamo as prt
					LEFT JOIN  "BSCL.Custodia.BD::CUS.Cartera" as emp on 
						prt."IdEmpresa" = emp."IdEmpresa" and
						prt."FolioCartera" = emp."FolioCarteraRelacionada" and
						emp."IdTipoInstrumento" IN ('EMP','ERF') and
						emp."IdEstado" = 'VIG' and
						emp."FolioCarteraRelacionada" > 0
					WHERE prt."ultimaCartera" = 1
					GROUP BY  prt."IdEmpresa", prt."FolioCartera"
				)
			SELECT 
					sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,sby."IdInstrumento" as "IdInstrumento"
				   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
				   ,INS."ISIN"
				   ,CASE 
	   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					WHEN p_AgrupaPorBoveda='S' AND IFNULL(PRE."IdBoveda",'')<>'' THEN PRE."IdBoveda" 
	   					ELSE 'DCV' 
	   				END AS "IdBoveda"
	   				,PRE."INGCantidad" AS "Cantidad"
	   				, 0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
			FROM CarteraPrestamo AS PRE
			INNER JOIN SubyacentePrestamo as sby on (PRE."FolioCartera" = sby."FolioCarteraRelacionada") AND "ultimoSubyacente" = 1
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = sby."IdInstrumento"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
			INNER JOIN consultarAsignacionesPrestamo ASIG ON ASIG."FolioCarteraPrestamo" = PRE."FolioCartera"
			LEFT JOIN IndicadorContratoCustodia IND ON IND."IdCliente" = PRE."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = PRE."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
			WHERE PRE."ultimaCartera" 					= 1
			AND COALESCE(IND."ContratoCustodia",'N')	= 'N'
			AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end							   
						   ))
						OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
						)) 
					)
			AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end

			UNION ALL	
	
	
			--PRESTAMOS_ADMINISTRATIVO_FINES_GENERALES	
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			SELECT
					sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,sby."IdInstrumento" as "IdInstrumento"
				   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
				   ,inst3."ISIN"
				   ,CASE 
	   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					WHEN p_AgrupaPorBoveda='S' AND IFNULL(sby."IdBoveda",'')<>'' THEN sby."IdBoveda" 
	   					ELSE 'DCV' 
	   				END AS "IdBoveda"
				   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then PRE."INGValorRescate" else PRE."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::CUS.Cartera" AS PRE
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PRE."FolioCartera" = ultSub."FolioCarteraRelacionada")
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente")
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =:p_IdEmpresa AND CLI."IdCliente" = PRE."IdCliente")
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
				WHERE PRE."IdEmpresa" = p_IdEmpresa
				AND PRE."IdEstado" = 'VIG'
				AND PRE."IdTipoInstrumento" IN ('PRT','PRF')
				AND PRE."IdTipoPrestamo" IN ('PRTADM','PRTFIN','PRADRF','PRFGRF')
				AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end							   
						   ))
						OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
						)) 
					)
				AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
				AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
				AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
				AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
				AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";

	 ELSE

		v_tblPrestamos = 
			
			--PRESTAMOS HISTORICO
			SELECT  "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN" 				
				, "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") as "MontoValorizado"
				, "Relacion" 
			FROM(
			--VENTA_CORTA_CON_CUSTODIA_HIS
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			WITH 
				CarteraPrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS "ultimaCartera",
							"IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio",
							"IdTipoInstrumento", "IdEstado", "IdEmpresa", "Cantidad", "INGCantidad",
							"INGFechaLiquidacion", "INGFechaOperacion", "IdBoveda", "IdMonedaSistema", "FechaRespaldo", 
							"IdMonedaInstrumento", "EGRFechaOperacion"
					FROM "BSCL.Custodia.BD::HIS.Cartera"
					WHERE "IdTipoInstrumento" IN ('PRT','PRF')
					  AND "Nemotecnico"  		NOT IN ('ADMINISTRATIVO','FINES GENERALES')	 
					  AND "IdEstado"        	= 'VIG'
					  AND "IdEmpresa" 			=1
				),
				SubyacentePrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdSubyacente" DESC) AS "ultimoSubyacente",
							sb.*
					FROM "BSCL.Custodia.BD::HIS.Subyacente" AS sb
					INNER JOIN CarteraPrestamo as car on car."FolioCartera" = sb."FolioCarteraRelacionada"
					WHERE car."ultimaCartera" = 1
				),
				IndicadorContratoCustodia AS (
					SELECT  ATR_CC."IdEntidad" AS "IdCliente",
							ATR_CC."ValorIndicador" AS "ContratoCustodia"
					FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
					INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
							ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
						   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
					WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
				),
				consultarAsignacionesPrestamo AS (
					SELECT 
						 prt."FolioCartera" as "FolioCarteraPrestamo"
						,AVG(prt."Cantidad") as "CantidadPrestamo"
						,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
						,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
					FROM CarteraPrestamo as prt
					LEFT JOIN  "BSCL.Custodia.BD::HIS.Cartera" as emp on 
						prt."IdEmpresa" = emp."IdEmpresa" and
						prt."FolioCartera" = emp."FolioCarteraRelacionada" and
						emp."IdTipoInstrumento" IN ('EMP','ERF') and
						emp."IdEstado" = 'VIG' and
						emp."FolioCarteraRelacionada" > 0
					WHERE prt."ultimaCartera" = 1
					GROUP BY  prt."IdEmpresa", prt."FolioCartera"
				)
			SELECT 
					sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,sby."IdInstrumento" as "IdInstrumento"
				   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
				   ,INS."ISIN"	   
				   ,CASE 
	   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					WHEN p_AgrupaPorBoveda='S' AND IFNULL(PRE."IdBoveda",'')<>'' THEN PRE."IdBoveda" 
	   					ELSE 'DCV' 
	   				END AS "IdBoveda"	   
				   ,PRE."INGCantidad" AS "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
			FROM CarteraPrestamo AS PRE
			INNER JOIN SubyacentePrestamo as sby on (PRE."FolioCartera" = sby."FolioCarteraRelacionada") AND "ultimoSubyacente" = 1
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = sby."IdInstrumento"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
			INNER JOIN consultarAsignacionesPrestamo ASIG ON ASIG."FolioCarteraPrestamo" = PRE."FolioCartera"
			LEFT JOIN IndicadorContratoCustodia IND ON IND."IdCliente" = PRE."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = PRE."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
			WHERE PRE."ultimaCartera" 					= 1
			AND COALESCE(IND."ContratoCustodia",'N')	= 'S'
			AND	PRE."FechaRespaldo" = v_FechaConsulta
			AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end							   
						   ))
						OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
						)) 
					)
			AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end

			UNION ALL

			--VENTA_CORTA_SIN_CUSTODIA_HIS
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			WITH 
				CarteraPrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdCartera" DESC) AS "ultimaCartera",
							"IdCartera", "FolioCartera", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio",
							"IdTipoInstrumento", "IdEstado", "IdEmpresa", "Cantidad", "INGCantidad",
							"INGFechaLiquidacion", "INGFechaOperacion", "IdBoveda", "IdMonedaSistema", "FechaRespaldo", 
							"IdMonedaInstrumento", "EGRFechaOperacion"
					FROM "BSCL.Custodia.BD::HIS.Cartera"
					WHERE "IdTipoInstrumento" IN ('PRT','PRF')
					  AND "Nemotecnico"  		NOT IN ('ADMINISTRATIVO','FINES GENERALES')			 		 
					  AND "IdEstado"        	= 'VIG'
					  AND "IdEmpresa" 			=1
				),
				SubyacentePrestamo AS (
					SELECT  ROW_NUMBER() OVER(PARTITION BY "FolioCartera" ORDER BY "IdSubyacente" DESC) AS "ultimoSubyacente",
							sb.*
					FROM "BSCL.Custodia.BD::HIS.Subyacente" AS sb
					INNER JOIN CarteraPrestamo as car on car."FolioCartera" = sb."FolioCarteraRelacionada"
					WHERE car."ultimaCartera" = 1
				),
				IndicadorContratoCustodia AS (
					SELECT  ATR_CC."IdEntidad" AS "IdCliente",
							ATR_CC."ValorIndicador" AS "ContratoCustodia"
					FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC
					INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC 
							ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" 
						   AND ATR_CC."IdTipoEntidad" = 'CLIENTE'
					WHERE TPO_CC."CodigoTipoIndicador" = 'CC'
				),
				consultarAsignacionesPrestamo AS (
					SELECT 
						 prt."FolioCartera" as "FolioCarteraPrestamo"
						,AVG(prt."Cantidad") as "CantidadPrestamo"
						,COALESCE(SUM(emp."INGCantidad"),0) as "CantidadAsignada"
						,AVG(prt."INGCantidad") - COALESCE(SUM(emp."INGCantidad"),0) as "CantidadPorAsignar"
					FROM CarteraPrestamo as prt
					LEFT JOIN  "BSCL.Custodia.BD::HIS.Cartera" as emp on 
						prt."IdEmpresa" = emp."IdEmpresa" and
						prt."FolioCartera" = emp."FolioCarteraRelacionada" and
						emp."IdTipoInstrumento" IN ('EMP','ERF') and
						emp."IdEstado" = 'VIG' and
						emp."FolioCarteraRelacionada" > 0
					WHERE prt."ultimaCartera" = 1
					GROUP BY  prt."IdEmpresa", prt."FolioCartera"
				)
			SELECT 
					sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,sby."IdInstrumento" as "IdInstrumento"
				   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
				   ,INS."ISIN"	   
				   ,CASE 
	   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					WHEN p_AgrupaPorBoveda='S' AND IFNULL(PRE."IdBoveda",'')<>'' THEN PRE."IdBoveda" 
	   					ELSE 'DCV' 
	   				END AS "IdBoveda"
				   ,PRE."INGCantidad" AS "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
			FROM CarteraPrestamo AS PRE
			INNER JOIN SubyacentePrestamo as sby on (PRE."FolioCartera" = sby."FolioCarteraRelacionada") AND "ultimoSubyacente" = 1
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = sby."IdInstrumento"
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = PRE."IdCliente"
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
			INNER JOIN consultarAsignacionesPrestamo ASIG ON ASIG."FolioCarteraPrestamo" = PRE."FolioCartera"
			LEFT JOIN IndicadorContratoCustodia IND ON IND."IdCliente" = PRE."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS conf ON (conf."IdEmpresa" = PRE."IdEmpresa" AND conf."CodigoParametro" = 'PLA.MonedaSistema')
			WHERE PRE."ultimaCartera" 					= 1
			AND COALESCE(IND."ContratoCustodia",'N')	= 'N'
			AND	PRE."FechaRespaldo" = v_FechaConsulta
			AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end							   
						   ))
						OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
						)) 
					)
			AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
		
			UNION ALL


			--ADMINISTRATIVO_FINES_GENERALES_HIS
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			SELECT
					sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
				   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
				   ,sby."IdInstrumento" as "IdInstrumento"
				   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
				   ,inst3."ISIN"
				   ,CASE 
	   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   					WHEN p_AgrupaPorBoveda='S' AND IFNULL(sby."IdBoveda",'')<>'' THEN sby."IdBoveda" 
	   					ELSE 'DCV' 
	   				END AS "IdBoveda"	   	   		   
				   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then PRE."INGValorRescate" else PRE."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   , 0 as "MontoValorizado"
				   ,CASE
						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.Cartera" AS PRE
				INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" as ultSub on (PRE."FolioCartera" = ultSub."FolioCarteraRelacionada" AND PRE."FechaRespaldo" = ultSub."FechaRespaldo")
				INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on (sby."IdSubyacente" = ultSub."IdSubyacente" AND PRE."FechaRespaldo" = sby."FechaRespaldo")
				INNER JOIN "BSCL.Custodia.BD::PRT.TipoPrestamo" AS TPRE ON TPRE."IdInstrumentoPrestamo" = PRE."IdInstrumento"
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =1 AND CLI."IdCliente" = PRE."IdCliente")
				LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PRE."IdPortafolio" AND Portafolio."IdCliente" = PRE."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PRE."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
				WHERE PRE."IdEmpresa" =1
				AND	PRE."FechaRespaldo" = v_FechaConsulta
				AND PRE."IdEstado" = 'VIG'
				AND PRE."IdTipoInstrumento" IN ('PRT','PRF')
				AND TPRE."IdTipoPrestamo" IN ('PRTADM','PRTFIN','PRADRF','PRFGRF')
				AND
					(
						   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end							   
						   ))
						OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
									  v_FechaConsulta BETWEEN PRE."INGFechaOperacion" and case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
									  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								AND	  v_FechaConsulta < case when IFNULL(PRE."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(PRE."EGRFechaOperacion",'0001-01-01') end
						)) 
					)
				AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
				AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
				AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
				AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
				AND   PRE."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PRE."IdBoveda" END	
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";

	 END IF;

	 end;



	--------------------------
	-- Emprestitos
	--------------------------
	 begin 
	 	IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN

			v_tblEmprestitos =
				--PRESTAMISTA
				SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion" 
				FROM(
				--EMPRESTITOS
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
				SELECT
							sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
							,sby."IdTipoInstrumento" as "IdTipoInstrumento"
							,sby."IdInstrumento" as "IdInstrumento"
							,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
							,inst3."ISIN"			   
							,CASE
								WHEN p_AgrupaPorBoveda='N' THEN 'DCV'
			   					WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   					WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   					ELSE 'DCV'
							END AS "IdBoveda"			   			   
							,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
							, 0 as "MontoValorizado"
							,CASE
								WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
								WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
								ELSE 'TER'                                                  -- Tercero
							END AS "Relacion"
							FROM "BSCL.Custodia.BD::CUS.Cartera" AS EMP
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
							INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
							LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
							LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
							LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
							LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
							LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
							LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
							LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
							LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
							LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
							LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
							WHERE EMP."IdEmpresa" =p_IdEmpresa
							AND EMP."IdEstado" = 'VIG'
							AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
							AND IFNULL(ATR_CC."ValorIndicador",'N') = 'S'
							AND
								(
									   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end							   
									   ))
									OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
									)) 
								)
							AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
							AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
							AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
							AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
							AND   EMP."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE EMP."IdBoveda" END					
					)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
		
				UNION ALL

				--EMPRESTITOS_EXTERNOS
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
				SELECT
								sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
							   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
							   ,sby."IdInstrumento" as "IdInstrumento"
							   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
							   ,inst3."ISIN"			   
							   ,CASE
			   						WHEN p_AgrupaPorBoveda='N' THEN 'DCV'
			   						WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   						WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   						ELSE 'DCV'
			   					END AS "IdBoveda"			   			   
							   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
							   , 0 as "MontoValorizado"
							   ,CASE
			   						WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
			   						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
			   						ELSE 'TER'                                                  -- Tercero
								END AS "Relacion"			  
							FROM "BSCL.Custodia.BD::CUS.Cartera" AS EMP
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
							INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
							LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
							LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
							LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
							LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
							LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
							LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
							LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
							LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
							LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
							LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
							WHERE EMP."IdEmpresa" =p_IdEmpresa
							AND EMP."IdEstado" = 'VIG'
							AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
							AND IFNULL(ATR_CC."ValorIndicador",'N') = 'N'
							AND
								(
									   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end							   
									   ))
									OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
									)) 
								)
							AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
							AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
							AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
							AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
							AND   EMP."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE EMP."IdBoveda" END
					)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";
		
		ELSE
			
			v_tblEmprestitos =
				--PRESTAMISTA HISTORICO
				SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion" 
				FROM(
				--EMPRESTITOS HISTORICO
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
				SELECT
								sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
							   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
							   ,sby."IdInstrumento" as "IdInstrumento"
							   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
							   ,inst3."ISIN"			   
							   ,CASE
			   						WHEN p_AgrupaPorBoveda='N' THEN 'DCV'
			   						WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   						WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   						ELSE 'DCV'
			   					END AS "IdBoveda"			   			   
							   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
							   , 0 as "MontoValorizado"
							   ,CASE
									WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
									WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
									ELSE 'TER'                                                  -- Tercero
								END AS "Relacion"
							FROM "BSCL.Custodia.BD::HIS.Cartera" AS EMP
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
							INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
							LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
							LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
							LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
							LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
							LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
							LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
							LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
							LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
							LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
							LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
							WHERE EMP."IdEmpresa" =p_IdEmpresa
							AND	EMP."FechaRespaldo" = v_FechaConsulta
							AND EMP."IdEstado" = 'VIG'
							AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
							AND IFNULL(ATR_CC."ValorIndicador",'N') = 'S'
							AND
								(
									   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end							   
									   ))
									OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
									)) 
								)
							AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
							AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
							AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
							AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
							AND   EMP."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE EMP."IdBoveda" END			
					)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END

		
				UNION ALL


				--EMPRESTITOS_EXTERNOS
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
				SELECT
								sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
							   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
							   ,sby."IdInstrumento" as "IdInstrumento"
							   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
							   ,inst3."ISIN"			   
							   ,CASE
			   						WHEN p_AgrupaPorBoveda='N' THEN 'DCV'
			   						WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   						WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   						ELSE 'DCV'
			   					END AS "IdBoveda"			   			   
							   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
							   , 0 as "MontoValorizado"
							   ,CASE
									WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
									WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
									ELSE 'TER'                                                  -- Tercero
								END AS "Relacion"
							FROM "BSCL.Custodia.BD::HIS.Cartera" AS EMP
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
							INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
							INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
							LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
							LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
							LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
							LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
							LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
							LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
							LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
							LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
							LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
							LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
							LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
							WHERE EMP."IdEmpresa" =p_IdEmpresa
							AND	EMP."FechaRespaldo" = v_FechaConsulta
							AND EMP."IdEstado" = 'VIG'
							AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
							AND IFNULL(ATR_CC."ValorIndicador",'N') = 'N'
							AND
								(
									   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end							   
									   ))
									OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
												  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
												  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
											AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
									)) 
								)
							AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
							AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
							AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
							AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
							AND   EMP."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE EMP."IdBoveda" END			
					)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";

		END IF;	
	 end;



	----------------------------------
	-- Movimiento en transito, ingreso
	----------------------------------
	 begin 
	 	IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN
		
			v_tblTrasnIng =
				--INGRESOS TRANSITO
				SELECT  "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN" 				
						, "IdBoveda"
						, SUM("Cantidad") as "Cantidad"
						, SUM("MontoValorizado") as "MontoValorizado"
						, "Relacion"
				FROM(
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento"
						,MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"  
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'INGRESO' AND  MOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
						AND MOV."IdTipoOperacion" IN ('INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				--EGRESOS TRANSITO
				UNION ALL
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						MOV."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
					   ,MOV."IdTipoInstrumento" AS "IdTipoInstrumento" 
					   ,MOV."IdInstrumento" AS "IdInstrumento"
					   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'RETIRO' AND  MOV."EsNominal" = 'N'
							)
						AND MOV."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE MOV."IdClasificacionTipoInstrumento" END 
						AND MOV."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE MOV."IdTipoInstrumento" END 
						AND MOV."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE MOV."IdInstrumento" END 
						AND MOV."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE MOV."Nemotecnico" END 
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END		
		
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND MOV."IdTipoOperacion" IN ('INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";

		ELSE

			v_tblTrasnIng = 
				--INGRESOS TRANSITO HISTORICO
				SELECT  "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN" 				
						, "IdBoveda"
						, SUM("Cantidad") as "Cantidad"
						, SUM("MontoValorizado") as "MontoValorizado"
						, "Relacion"
				FROM(
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"  
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							HMOV."IdEmpresa" = p_IdEmpresa
						AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
						AND	HMOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'INGRESO' AND  HMOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") END
						AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
						AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
						AND HMOV."IdTipoOperacion" IN ('INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				--EGRESOS TRANSITO
				UNION ALL

				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
					INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON HMOV."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito"
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							HMOV."IdEmpresa" = p_IdEmpresa
						AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
						AND	HMOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'RETIRO' AND  HMOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,0) > 0 THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
						AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
						AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND HMOV."IdTipoOperacion" IN ('INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";

		END IF;
			
	 end;


	-------------------------------------
	--Movimiento en transito, egreso
	-------------------------------------

	 begin 
	 	IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN

			v_tblTrasnEgr =
				--EGRESOS TRANSITO
				SELECT  "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN" 				
						, "IdBoveda"
						, SUM("Cantidad") as "Cantidad"
						, SUM("MontoValorizado") as "MontoValorizado"
						, "Relacion"
				FROM(
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento"
						,MOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", MOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",MOV."NemoSVS") else COALESCE(rel."Nemotecnico",MOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = MOV."IdMovimientoCarteraEnTransito"  
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",MOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND	MOV."IdEstado" = 'PEN'
						AND MOV."EsVentaDescubierta" = 'N'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'INGRESO' AND  MOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",MOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", MOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",MOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",MOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",MOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",MOV."Nemotecnico") END
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
						AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				--EGRESOS TRANSITO
				UNION ALL
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						MOV."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
					   ,MOV."IdTipoInstrumento" AS "IdTipoInstrumento" 
					   ,MOV."IdInstrumento" AS "IdInstrumento"
					   ,CASE WHEN MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(MOV."IdBoveda",'')<>'' THEN IFNULL(MOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = MOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = MOV."IdInstrumento"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = MOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							MOV."IdEmpresa" = p_IdEmpresa
						AND MOV."EsVentaDescubierta" = 'N'
						AND	MOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'RETIRO' AND  MOV."EsNominal" = 'N'
							)
						AND MOV."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE MOV."IdClasificacionTipoInstrumento" END 
						AND MOV."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE MOV."IdTipoInstrumento" END 
						AND MOV."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE MOV."IdInstrumento" END 
						AND MOV."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE MOV."Nemotecnico" END 
						AND MOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE MOV."IdBoveda" END
		
						AND MOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND MOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";
		ELSE

			v_tblTrasnEgr =
				--EGRESOS TRANSITO HISTORICO
				SELECT  "IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "ISIN" 				
						, "IdBoveda"
						, SUM("Cantidad") as "Cantidad"
						, SUM("MontoValorizado") as "MontoValorizado"
						, "Relacion"
				FROM(
				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"  
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							HMOV."IdEmpresa" = p_IdEmpresa
						AND rel."EsVentaDescubierta" = 'N'		
						AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
						AND	HMOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'INGRESO' AND  HMOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") END
						AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
						AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
		
						AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				--EGRESOS TRANSITO
				UNION ALL

				SELECT "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN"
					, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") AS "MontoValorizado"
					, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				FROM
				(
					SELECT
						COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") AS "IdClasificacionTipoInstrumento"
					   ,COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") AS "IdTipoInstrumento" 
					   ,COALESCE(rel."IdInstrumento", HMOV."IdInstrumento") AS "IdInstrumento"
					   ,CASE WHEN COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = 'IF' then COALESCE(rel."NemoSVS",HMOV."NemoSVS") else COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") end as "Nemotecnico"
					   ,inst4."ISIN"
					   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' 
	   						 WHEN p_AgrupaPorBoveda='S' AND IFNULL(HMOV."IdBoveda",'')<>'' THEN IFNULL(HMOV."IdBoveda",'')
	   						 ELSE 'DCV' 
	   					END AS "IdBoveda"
					   ,CAST(COALESCE(case when HMOV."IdClasificacionTipoInstrumento" = 'IF' then HMOV."ValorRescate" else HMOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
					   ,0 as "MontoValorizado"
					   ,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(RELC."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
					FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS HMOV
					INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON HMOV."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito"
						LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS rel ON rel."IdMovimientoCarteraEnTransitoRelacionado" = HMOV."IdMovimientoCarteraEnTransito"
						LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON HMOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = HMOV."IdCliente")
						LEFT JOIN :v_tblRelacionados							as RELC				ON (RELC."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = HMOV."IdPortafolio" AND Portafolio."IdCliente" = HMOV."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = HMOV."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = COALESCE(rel."IdInstrumento",HMOV."IdInstrumento")
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = HMOV."IdRegimenFondoMutuo"
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
					WHERE
							HMOV."IdEmpresa" = p_IdEmpresa
						AND rel."EsVentaDescubierta" = 'N'
						AND TO_NVARCHAR(HMOV."FechaRespaldo",'YYYY-MM-DD') = v_FechaConsulta
						AND	HMOV."IdEstado" = 'PEN'
						AND COALESCE(TipoOperacion."AccionCustodia",'') <> ''
						AND
							(
								TipoOperacion."AccionCustodia" = 'RETIRO' AND  HMOV."EsNominal" = 'N'
							)
						AND	COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") = CASE WHEN COALESCE(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE COALESCE(rel."IdClasificacionTipoInstrumento",HMOV."IdClasificacionTipoInstrumento") END
						AND	COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") = CASE WHEN COALESCE(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE COALESCE(rel."IdTipoInstrumento", HMOV."IdTipoInstrumento") END
						AND	COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") = CASE WHEN COALESCE(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE COALESCE(rel."IdInstrumento",HMOV."IdInstrumento") END
						AND	COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") = CASE WHEN COALESCE(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE COALESCE(rel."Nemotecnico",HMOV."Nemotecnico") END
						AND HMOV."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE HMOV."IdBoveda" END
						AND HMOV."IdMovimientoCarteraEnTransitoRelacionado" = 0 
						AND HMOV."IdTipoOperacion" NOT IN ('VESIMC','VESIMV', 'PRESTA','EMPRES','INGTIT')
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion";

		END IF;

	 end;
	 
	--------------------------------
	-- Garantias
	--------------------------------
	 begin 
	 	--GARANTÍAS
		IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN
			v_tblGarantias =
			SELECT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN",
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda",						 
					SUM("Cantidad") as "Cantidad",				
					SUM("MontoValorizado") AS "MontoValorizado", 
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"		
			FROM (
			SELECT
					GAR."IdClasificacionTipoInstrumento"
				   ,GAR."IdTipoInstrumento"
				   ,GAR."IdInstrumento"
				   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico"
				   ,inst4."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda = 'N' THEN 'DCV' ELSE CAR."IdBoveda" END AS "IdBoveda"
				   ,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF'  then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   ,0 as "MontoValorizado" 
				   ,CASE WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
				INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = GAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")				
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = GAR."IdPortafolio" AND Portafolio."IdCliente" = GAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = GAR."IdInstrumento"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE GAR."IdEmpresa" = p_IdEmpresa
				AND GAR."IdEstado" = 'VIG'
				AND GAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE GAR."IdClasificacionTipoInstrumento" END 
				AND GAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE GAR."IdTipoInstrumento" END 
				AND GAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE GAR."IdInstrumento" END  
				AND GAR."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE GAR."Nemotecnico" END 
				AND CAR."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE CAR."IdBoveda" END
				AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND GAR."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND GAR."INGFechaOperacion" <= v_FechaConsulta)
				)
	
				)
				GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
						case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end,					
						case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end;

		ELSE
			
			v_tblGarantias =
			--GARANTÍAS HISTORICO
			SELECT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN",
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda",						 
					SUM("Cantidad") as "Cantidad",				
					SUM("MontoValorizado") AS "MontoValorizado", 
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"		
			FROM (
			SELECT
					GAR."IdClasificacionTipoInstrumento"
				   ,GAR."IdTipoInstrumento"
				   ,GAR."IdInstrumento"
				   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico"
				   ,inst4."ISIN"
				   ,CASE WHEN p_AgrupaPorBoveda = 'N' THEN 'DCV' ELSE CAR."IdBoveda" END AS "IdBoveda"
				   ,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF'  then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   ,0 as "MontoValorizado" 
				   ,CASE WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						ELSE 'TER'                                                  -- Tercero
					END AS "Relacion"
				FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
				INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" AND CAR."FechaRespaldo" = v_FechaConsulta
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = GAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")				
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = GAR."IdPortafolio" AND Portafolio."IdCliente" = GAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst4 ON inst4."IdInstrumento" = GAR."IdInstrumento"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
				LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
				LEFT JOIN "BSCL.APV.BD::REC.RegimenTributario" 			AS REGTRIB			ON REGTRIB."Codigo" = REG."IdRegimenFondoMutuo"
				WHERE GAR."IdEmpresa" =p_IdEmpresa
				AND GAR."FechaRespaldo" = v_FechaConsulta
				AND GAR."IdEstado" = 'VIG'	
				AND GAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE GAR."IdClasificacionTipoInstrumento" END 
				AND GAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE GAR."IdTipoInstrumento" END 
				AND GAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE GAR."IdInstrumento" END  
				AND GAR."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE GAR."Nemotecnico" END 
				AND CAR."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE CAR."IdBoveda" END
				AND
				(
					   (p_TipoFechaConsulta = 'LIQ' AND GAR."INGFechaLiquidacion" <= v_FechaConsulta)
					OR (p_TipoFechaConsulta = 'ADQ' AND GAR."INGFechaOperacion" <= v_FechaConsulta)
				)
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end;
		END IF;
	 end;


	------------------------------------------
	-- REPORTE VENTA
	------------------------------------------
	BEGIN

		v_tblReporVen =
			--REPORTE VENTA
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			SELECT
				SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento"
			   ,SIM."suby_IdInstrumento" as "IdInstrumento"
			   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE COALESCE(CCPI."IdBoveda",SIM."suby_IdBoveda",'') END AS "IdBoveda"
			   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF'  then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   , 0 as "MontoValorizado"
			   ,CASE
				WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = SIM."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio" AND Portafolio."IdCliente" = SIM."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SIM."suby_IdInstrumento"
			WHERE
				SIM."IdEmpresa" =p_IdEmpresa
			AND SIM."IdEstado" = 'VIG'
			AND	SIM."IdTipoOperacion" IN ('COMRRV','COMRRF')
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND SIM."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND SIM."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND SIM."suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SIM."suby_IdClasificacionTipoInstrumento" END
			AND SIM."suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
			AND SIM."suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SIM."suby_IdInstrumento" END
			AND SIM."suby_Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SIM."suby_Nemotecnico" END 
			AND SIM."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE SIM."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END;

	end;


	------------------------------------------
	-- REPORTE COMPRA
	------------------------------------------
	BEGIN

		v_tblReporCom =
			--REPORTE COMPRA
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			SELECT
				SIM."suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,SIM."suby_IdTipoInstrumento" as "IdTipoInstrumento"
			   ,SIM."suby_IdInstrumento" as "IdInstrumento"
			   ,case when "suby_IdClasificacionTipoInstrumento" = 'IF' then SIM."suby_NemoSVS" else SIM."suby_Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE COALESCE(CCPI."IdBoveda",SIM."suby_IdBoveda",'') END AS "IdBoveda"
			   ,cast(IFNULL(case when "suby_IdClasificacionTipoInstrumento" = 'IF'  then SIM."INGValorRescate" else SIM."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   , 0 as "MontoValorizado"
			   ,CASE
				WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = SIM."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SIM."IdPortafolio" AND Portafolio."IdCliente" = SIM."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = SIM."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SIM."suby_IdInstrumento"
			WHERE
				SIM."IdEmpresa" =p_IdEmpresa
			AND SIM."IdEstado" = 'VIG'
			AND	SIM."IdTipoOperacion" IN ('VENRRV','VENRRF')
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND SIM."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND SIM."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND SIM."suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SIM."suby_IdClasificacionTipoInstrumento" END
			AND SIM."suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SIM."suby_IdTipoInstrumento" END
			AND SIM."suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SIM."suby_IdInstrumento" END
			AND SIM."suby_Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SIM."suby_Nemotecnico" END 
			AND SIM."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE SIM."IdBoveda" END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END;

	end;

	 ------------------------------------------
	-- PACTOS VENTA
	------------------------------------------
	BEGIN

	IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN

		v_tblPactoVen =
			--VENTA_PACTO
			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion"
					, "ClienteOpera"
			FROM(
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				, "ClienteOpera"
			FROM
			(
			SELECT 
				SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,SUB."IdInstrumento" as "IdInstrumento"
			   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda" 
			   ,CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END AS "Cantidad" 
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND  POR."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
			WHERE CAR."IdEmpresa" = p_IdEmpresa
			AND CAR."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
			AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
			AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
			AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
			AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
			AND CAR."IdTipoInstrumento" = 'PAC'
			AND SUB."IdEstado"='VIG'
			AND CAR."IdTipoOperacion"   = 'COMPAC'
	
			UNION ALL

			SELECT 
				SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,SUB."IdInstrumento" as "IdInstrumento"
			   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"
			   ,CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 	AS "Cantidad"
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN CAR."IdClienteRelacionado" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND  POR."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
			WHERE CAR."IdEmpresa" = p_IdEmpresa
			AND CAR."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
			AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
			AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
			AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
			AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
			AND CAR."IdTipoInstrumento" = 'PAC'
			AND SUB."IdEstado"='VIG'
			AND CAR."IdTipoOperacion"   = 'VENPAC'
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "ClienteOpera", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion", "ClienteOpera";

	ELSE

		v_tblPactoVen = 
			--VENTA PACTO HISTORICO
			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion"
					, "ClienteOpera"
			FROM(
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				, "ClienteOpera"
			FROM
			(
			SELECT
				sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
			INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on (sby."FolioCarteraRelacionada" = PAC."FolioCartera" AND sby."FechaRespaldo" = PAC."FechaRespaldo")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= sby."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = PAC."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
			WHERE 
				PAC."IdEmpresa" = p_IdEmpresa
			AND	PAC."FechaRespaldo" = v_FechaConsulta
			AND PAC."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND PAC."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND PAC."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND PAC."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PAC."IdBoveda" END
			AND PAC."IdTipoInstrumento" = 'PAC'
			AND PAC."IdTipoOperacion" = 'COMPAC'

			UNION ALL

			SELECT
				sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"		   
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN PAC."IdClienteRelacionado" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
			INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on (sby."FolioCarteraRelacionada" = PAC."FolioCartera" AND sby."FechaRespaldo" = PAC."FechaRespaldo")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= sby."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = PAC."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
			WHERE 
				PAC."IdEmpresa" = p_IdEmpresa
			AND	PAC."FechaRespaldo" = v_FechaConsulta
			AND PAC."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND PAC."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND PAC."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND PAC."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PAC."IdBoveda" END
			AND PAC."IdTipoInstrumento" = 'PAC'
			AND PAC."IdTipoOperacion" = 'VENPAC'
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "ClienteOpera",
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion", "ClienteOpera";

	END IF;

	end;


	------------------------------------------
	-- PACTOS COMPRA
	------------------------------------------
	BEGIN

	IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN

		v_tblPactoCom =

			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion"
					, "ClienteOpera"
			FROM(
			--COMPRA PACTO
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				, "ClienteOpera"
			FROM
			(
			SELECT 
				SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,SUB."IdInstrumento" as "IdInstrumento"
			   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico" 
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"			   
			   ,CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END 	AS "Cantidad" 
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND POR."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
			WHERE CAR."IdEmpresa" = p_IdEmpresa
			AND CAR."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
			AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
			AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
			AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
			AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
			AND CAR."IdTipoInstrumento" = 'PAC'
			AND CAR."IdTipoOperacion"   = 'VENPAC'
			AND SUB."IdEstado"='VIG'

			UNION ALL

			SELECT 
				SUB."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,SUB."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,SUB."IdInstrumento" as "IdInstrumento"
			   ,case when SUB."IdClasificacionTipoInstrumento" = 'IF' then SUB."NemoSVS" else SUB."Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda"	   
			   ,CASE WHEN SUB."IdClasificacionTipoInstrumento"= 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END AS "Cantidad" 
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN CAR."IdClienteRelacionado" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR 
			LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= SUB."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = CAR."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."IdPortafolio" = CAR."IdPortafolio" AND POR."IdCliente" = CAR."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MT ON MT."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = CAR."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND POR."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MT."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = SUB."IdInstrumento"
			WHERE CAR."IdEmpresa" = p_IdEmpresa
			AND CAR."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND CAR."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND CAR."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	SUB."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE SUB."IdClasificacionTipoInstrumento" END
			AND	SUB."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE SUB."IdTipoInstrumento" END
			AND	SUB."IdInstrumento"     = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE SUB."IdInstrumento" END
			AND SUB."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE SUB."Nemotecnico" END 
			AND RES."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE RES."IdBoveda" END
			AND CAR."IdTipoInstrumento" = 'PAC'
			AND CAR."IdTipoOperacion"   = 'COMPAC'
			AND SUB."IdEstado"='VIG'
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "ClienteOpera",
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion", "ClienteOpera";

	ELSE

		v_tblPactoCom = 
			--COMPRA PACTO HISTORICO
			SELECT  "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumento"
					, "IdInstrumento"
					, "Nemotecnico"
					, "ISIN" 				
					, "IdBoveda"
					, SUM("Cantidad") as "Cantidad"
					, SUM("MontoValorizado") as "MontoValorizado"
					, "Relacion"
					, "ClienteOpera"
			FROM(
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
				, "ClienteOpera"
			FROM
			( 
			SELECT
				sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE RES."IdBoveda" END AS "IdBoveda" 
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
			INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on (sby."FolioCarteraRelacionada" = PAC."FolioCartera" AND sby."FechaRespaldo" = PAC."FechaRespaldo")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= sby."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" =  PAC."IdCliente")
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
			WHERE
				PAC."IdEmpresa" =p_IdEmpresa
			AND	PAC."FechaRespaldo" = v_FechaConsulta
			AND PAC."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND PAC."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND PAC."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND PAC."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PAC."IdBoveda" END
			AND PAC."IdTipoInstrumento" = 'PAC'
			AND PAC."IdTipoOperacion" = 'VENPAC'

			UNION ALL

			SELECT
				sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
			   ,sby."IdInstrumento" as "IdInstrumento"
			   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
			   ,inst3."ISIN"
			   ,CASE WHEN p_AgrupaPorBoveda='N' THEN 'DCV' ELSE COALESCE(RES."IdBoveda",'DCV') END AS "IdBoveda"  	
			   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."INGValorRescate" else sby."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
			   ,0 as "MontoValorizado"
			   ,CASE
				WHEN PAC."IdClienteRelacionado" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				ELSE 'TER'                                                  -- Tercero
				END AS "Relacion"
			   , CASE WHEN RES."FolioCarteraOrigen"=0 THEN 'NOMINAL'
			   		ELSE 'CUSTODIA' END AS "ClienteOpera"
			FROM "BSCL.Custodia.BD::HIS.Cartera" AS PAC
			INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarCarteraSubyacenteBasica" as sby on (sby."FolioCarteraRelacionada" = PAC."FolioCartera" AND sby."FechaRespaldo" = PAC."FechaRespaldo")
			LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."FolioSubyacente"= sby."FolioSubyacente" 
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = PAC."IdCliente")			
			LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
			LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = PAC."IdPortafolio" AND Portafolio."IdCliente" = PAC."IdCliente"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = PAC."IdCuentaCustodioPortafolio"
			LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
			LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
			WHERE
				PAC."IdEmpresa" =p_IdEmpresa
			AND	PAC."FechaRespaldo" = v_FechaConsulta
			AND PAC."IdEstado" = 'VIG'
			AND
			(
				   (p_TipoFechaConsulta = 'LIQ' AND PAC."INGFechaLiquidacion" <= v_FechaConsulta)
				OR (p_TipoFechaConsulta = 'ADQ' AND PAC."INGFechaOperacion" <= v_FechaConsulta)
			)
			AND	sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
			AND	sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
			AND	sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
			AND sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
			AND PAC."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE PAC."IdBoveda" END
			AND PAC."IdTipoInstrumento" = 'PAC'
			AND PAC."IdTipoOperacion" = 'COMPAC'
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "ClienteOpera",
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","ISIN", "IdBoveda", "Relacion", "ClienteOpera";

	END IF;

	end;


	------------------------------------------
	-- EMPRESTITO INTERNO
	------------------------------------------
	BEGIN

	IF v_FechaSistema = v_FechaConsulta OR v_FechaSistema < v_FechaConsulta THEN

		v_tblEmpInt =
			--EMPRESTITO INTERNO
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			SELECT
						sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
						,sby."IdTipoInstrumento" as "IdTipoInstrumento"
						,sby."IdInstrumento" as "IdInstrumento"
						,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
						,inst3."ISIN"			   
						,CASE
							WHEN p_AgrupaPorBoveda='N' THEN 'DCV'
			   				WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   				WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   				ELSE 'DCV'
						END AS "IdBoveda"			   			   
						,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
						, 0 as "MontoValorizado"
						,CASE
							WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
							WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
							ELSE 'TER'                                                  -- Tercero
						END AS "Relacion"
						FROM "BSCL.Custodia.BD::CUS.Cartera" AS EMP
						INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
						INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
						INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
						LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
						LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
						LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
						LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
						WHERE EMP."IdEmpresa" =p_IdEmpresa
						AND EMP."IdEstado" = 'VIG'
						AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
						AND IFNULL(ATR_CC."ValorIndicador",'N') = 'S'
						AND
							(
								   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
											  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
											  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
										AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end							   
								   ))
								OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
											  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
											  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
										AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
								)) 
							)
						AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
						AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
						AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
						AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
						AND   EMP."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE EMP."IdBoveda" END					
				)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END;

	ELSE

		v_tblEmpInt = 
			--EMPRESTITOS HISTORICO
			SELECT "IdClasificacionTipoInstrumento"
				, "IdTipoInstrumento"
				, "IdInstrumento"
				, "Nemotecnico"
				, "ISIN"
				, case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
				, SUM("Cantidad") as "Cantidad"
				, SUM("MontoValorizado") AS "MontoValorizado"
				, case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			FROM
			(
			SELECT
							sby."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
						   ,sby."IdTipoInstrumento" as "IdTipoInstrumento"
						   ,sby."IdInstrumento" as "IdInstrumento"
						   ,case when sby."IdClasificacionTipoInstrumento" = 'IF' then sby."NemoSVS" else sby."Nemotecnico" end as "Nemotecnico"
						   ,inst3."ISIN"			   
						   ,CASE
			   					WHEN p_AgrupaPorBoveda='N' THEN 'DCV'
			   					WHEN ifnull(EMP."IdBoveda",'') <> '' THEN EMP."IdBoveda"
			   					WHEN ifnull(orig."IdBoveda",'') <> '' THEN orig."IdBoveda"
			   					ELSE 'DCV'
			   				END AS "IdBoveda"			   			   
						   ,cast(IFNULL(case when sby."IdClasificacionTipoInstrumento" = 'IF'  then EMP."INGValorRescate" else EMP."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
						   , 0 as "MontoValorizado"
						   ,CASE
								WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
								WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
								ELSE 'TER'                                                  -- Tercero
							END AS "Relacion"
						FROM "BSCL.Custodia.BD::HIS.Cartera" AS EMP
						INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub on (EMP."FolioCartera" = ultSub."FolioCarteraRelacionada")
						INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoCartera" as ultPrt on (EMP."IdEmpresa" = ultPrt."IdEmpresa" and EMP."FolioCarteraRelacionada" = ultPrt."FolioCartera")
						INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as prt on (prt."IdCartera" = ultPrt."IdCartera")
						LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSubyacenteBasica" as sby on sby."IdSubyacente" = ultSub."IdSubyacente"
						LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" as orig on (orig."IdCartera" = sby."IdCarteraRetirada")
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" =p_IdEmpresa AND CLI."IdCliente" = EMP."IdCliente")
						LEFT JOIN :v_tblRelacionados							as REL				ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = EMP."IdPortafolio" AND Portafolio."IdCliente" = EMP."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = EMP."IdCuentaCustodioPortafolio"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0
						LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS MADOR ON MADOR."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
						LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst3 ON inst3."IdInstrumento" = sby."IdInstrumento"
						LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO_CC ON TPO_CC."CodigoTipoIndicador" = 'CC'
						LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR_CC ON ATR_CC."IdTipoIndicador" = TPO_CC."IdTipoIndicador" AND ATR_CC."IdTipoEntidad" = 'CLIENTE' AND ATR_CC."IdEntidad" = TO_VARCHAR(EMP."IdCliente")
						WHERE EMP."IdEmpresa" =p_IdEmpresa
						AND	EMP."FechaRespaldo" = v_FechaConsulta
						AND EMP."IdEstado" = 'VIG'
						AND EMP."IdTipoInstrumento" IN ('EMP','ERF')
						AND IFNULL(ATR_CC."ValorIndicador",'N') = 'S'
						AND
							(
								   (p_TipoFechaConsulta = 'LIQ' AND (  -- Filtra por Fecha Liquidacion				   
											  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
											  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
										AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end							   
								   ))
								OR (p_TipoFechaConsulta = 'ADQ' AND ( -- Filtra por Fecha Adquisicion
											  v_FechaConsulta BETWEEN EMP."INGFechaOperacion" and case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
											  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
										AND	  v_FechaConsulta < case when IFNULL(EMP."EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(EMP."EGRFechaOperacion",'0001-01-01') end
								)) 
							)
						AND	  sby."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE sby."IdClasificacionTipoInstrumento" END
						AND	  sby."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE sby."IdTipoInstrumento" END
						AND	  sby."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE sby."IdInstrumento" END
						AND   sby."Nemotecnico" = CASE WHEN IFNULL(p_Nemotecnico,'') <> '' THEN p_Nemotecnico ELSE sby."Nemotecnico" END 
						AND   EMP."IdBoveda" = CASE WHEN IFNULL(p_IdBoveda,'') <> '' THEN p_IdBoveda ELSE EMP."IdBoveda" END			
				)
			GROUP BY "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", 
					case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END,					
					case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END;

	END IF;

	end;

	-- Instrumentos de los cuales existe custodia
	v_tblInst =	 
	SELECT Distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion"
	FROM
	(
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblCustodia UNION ALL
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblSimulC UNION ALL
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblSimulV UNION ALL
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblPrestamos UNION ALL
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblEmprestitos UNION ALL
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblTrasnIng UNION ALL
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblTrasnEgr UNION ALL
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblGarantias UNION ALL 
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblPactoCom UNION ALL 
		SELECT distinct "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico", "ISIN", "IdBoveda", "Relacion" FROM :v_tblPactoVen
	);


	------------------------------------------
	-- SALIDA
	------------------------------------------

	p_out_tbl = 
		SELECT 	 ins."IdClasificacionTipoInstrumento" 
				,inst2."NombreClasificacionTipoInstrumento"
				,ins."IdTipoInstrumento" 
				,inst2."NombreTipoInstrumento"
				,ins."IdInstrumento" 
				,ins."Nemotecnico"
				,ins."ISIN"
				,inst2."IdEmisor"
				,ins."IdBoveda" As "IdBoveda"
				,'' AS "NumeroCuentaBoveda"
				,ins."Relacion"
				,cast(ifnull(cus."Cantidad",0) as DECIMAL(26,8)) as "CantidadCustodia"
				,cast(ifnull(simc."Cantidad",0) as DECIMAL(26,8)) as "CantidadSimulCom"
				,cast(ifnull(simv."Cantidad",0) as DECIMAL(26,8)) as "CantidadSimulVen"
				,cast(ifnull(prt."Cantidad",0) as DECIMAL(26,8)) as "CantidadPrestamo"
				,cast(ifnull(emp."Cantidad",0) as DECIMAL(26,8)) as "CantidadEmpresti"
				,cast(ifnull(gar."Cantidad",0) as DECIMAL(26,8)) as "CantidadGarantia"
				,cast(ifnull(mti."Cantidad",0) as DECIMAL(26,8)) as "CantidadTransIng"
				,cast(ifnull(mte."Cantidad",0) as DECIMAL(26,8)) as "CantidadTransEgr"			
				, cast(
					(	cast(ifnull(cus."Cantidad",0) as DECIMAL(26,8)) + cast(ifnull(gar."Cantidad",0) as DECIMAL(26,8)) +
						cast(ifnull(pve."Cantidad",0) as DECIMAL(26,8)) - cast(ifnull(pco."Cantidad",0) as DECIMAL(26,8)) +
						cast(ifnull(emi."Cantidad",0) as DECIMAL(26,8)) + (cast(ifnull(simv."Cantidad",0) as DECIMAL(26,8)) -
						cast(ifnull(simc."Cantidad",0) as DECIMAL(26,8))) + (cast(ifnull(rco."Cantidad",0) as DECIMAL(26,8)) -
						cast(ifnull(rve."Cantidad",0) as DECIMAL(26,8))) 
				  	) as DECIMAL(26,8)
				  ) as "TotalPosicion"
				, cast(
					(	cast(ifnull(cus."MontoValorizado",0) as DECIMAL(26,8)) + cast(ifnull(simc."MontoValorizado",0) as DECIMAL(26,8)) +
						cast(ifnull(simv."MontoValorizado",0) as DECIMAL(26,8)) + cast(ifnull(prt."MontoValorizado",0) as DECIMAL(26,8)) +
						cast(ifnull(emp."MontoValorizado",0) as DECIMAL(26,8)) + cast(ifnull(gar."MontoValorizado",0) as DECIMAL(26,8)) +
						cast(ifnull(mti."MontoValorizado",0) as DECIMAL(26,8)) + cast(ifnull(mte."MontoValorizado",0) as DECIMAL(26,8)) 
				  	) as DECIMAL(26,8)
				  ) as "MontoValorizado"
				,cast(ifnull(pve."Cantidad",0) + COALESCE(pven."Cantidad", 0) as DECIMAL(26,8)) as "CantidadPactoVenta"	
				,cast(ifnull(pco."Cantidad",0) + COALESCE(pcon."Cantidad", 0) as DECIMAL(26,8)) as "CantidadPactoCompra"	
		FROM :v_tblInst as ins
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as inst2 on ins."IdInstrumento" = inst2."IdInstrumento"		
		left join :v_tblCustodia AS cus    on cus."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and cus."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and cus."IdInstrumento" = ins."IdInstrumento"
		                                  and cus."IdBoveda" = ins."IdBoveda"
		                                  and cus."Relacion" = ins."Relacion"
		                                  and cus."Nemotecnico" = ins."Nemotecnico"
		                                 
		left join :v_tblSimulC as simc     on simc."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and simc."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and simc."IdInstrumento" = ins."IdInstrumento"
		                                  and simc."IdBoveda" = ins."IdBoveda"
		                                  and simc."Relacion" = ins."Relacion"
		                                  and simc."Nemotecnico" = ins."Nemotecnico"
		                              
		left join :v_tblSimulV as simv     on simv."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and simv."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and simv."IdInstrumento" = ins."IdInstrumento"
		                                  and simv."IdBoveda" = ins."IdBoveda"
		                                  and simv."Relacion" = ins."Relacion"
		                                  and simv."Nemotecnico" = ins."Nemotecnico"
		                               
		left join :v_tblPrestamos as prt   on prt."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and prt."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and prt."IdInstrumento" = ins."IdInstrumento"
		                                  and prt."IdBoveda" = ins."IdBoveda"
		                                  and prt."Relacion" = ins."Relacion"
		                                  and prt."Nemotecnico" = ins."Nemotecnico"
		                                  
		left join :v_tblEmprestitos as emp on emp."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and emp."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and emp."IdInstrumento" = ins."IdInstrumento"
		                                  and emp."IdBoveda" = ins."IdBoveda"
		                                  and emp."Relacion" = ins."Relacion"
		                                  and emp."Nemotecnico" = ins."Nemotecnico"
		                                
		left join :v_tblGarantias as gar   on gar."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and gar."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and gar."IdInstrumento" = ins."IdInstrumento"
		                                  and gar."IdBoveda" = ins."IdBoveda"
		                                  and gar."Relacion" = ins."Relacion"
		                                  and gar."Nemotecnico" = ins."Nemotecnico"
		                                  
		left join :v_tblTrasnIng as mti    on mti."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and mti."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and mti."IdInstrumento" = ins."IdInstrumento"
		                                  and mti."IdBoveda" = ins."IdBoveda"
		                                  and mti."Relacion" = ins."Relacion"
		                                  and mti."Nemotecnico" = ins."Nemotecnico"
		                                 
		left join :v_tblTrasnEgr as mte    on mte."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and mte."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and mte."IdInstrumento" = ins."IdInstrumento"
		                                  and mte."IdBoveda" = ins."IdBoveda"
		                                  and mte."Relacion" = ins."Relacion"
		                                  and mte."Nemotecnico" = ins."Nemotecnico"
		
		left join :v_tblPactoVen as pve    on pve."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and pve."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and pve."IdInstrumento" = ins."IdInstrumento"
		                                  and pve."IdBoveda" = ins."IdBoveda"
		                                  and pve."Relacion" = ins."Relacion"
		                                  and pve."Nemotecnico" = ins."Nemotecnico"
		                                  AND pve."ClienteOpera" = 'CUSTODIA'

		left join :v_tblPactoCom as pco    on pco."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and pco."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and pco."IdInstrumento" = ins."IdInstrumento"
		                                  and pco."IdBoveda" = ins."IdBoveda"
		                                  and pco."Relacion" = ins."Relacion"
		                                  and pco."Nemotecnico" = ins."Nemotecnico"
		                                  AND pco."ClienteOpera" = 'CUSTODIA'
		                                  
		left join :v_tblPactoVen as pven   on pven."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and pven."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and pven."IdInstrumento" = ins."IdInstrumento"
		                                  and pven."IdBoveda" = ins."IdBoveda"
		                                  and pven."Relacion" = ins."Relacion"
		                                  and pven."Nemotecnico" = ins."Nemotecnico"
		                                  AND pven."ClienteOpera" = 'NOMINAL'

		left join :v_tblPactoCom as pcon   on pcon."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and pcon."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and pcon."IdInstrumento" = ins."IdInstrumento"
		                                  and pcon."IdBoveda" = ins."IdBoveda"
		                                  and pcon."Relacion" = ins."Relacion"
		                                  and pcon."Nemotecnico" = ins."Nemotecnico"
		                                  AND pcon."ClienteOpera" = 'NOMINAL'
		
		left join :v_tblEmpInt as emi    on emi."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and emi."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and emi."IdInstrumento" = ins."IdInstrumento"
		                                  and emi."IdBoveda" = ins."IdBoveda"
		                                  and emi."Relacion" = ins."Relacion"
		                                  and emi."Nemotecnico" = ins."Nemotecnico"
		
		left join :v_tblReporVen as rve    on rve."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and rve."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and rve."IdInstrumento" = ins."IdInstrumento"
		                                  and rve."IdBoveda" = ins."IdBoveda"
		                                  and rve."Relacion" = ins."Relacion"
		                                  and rve."Nemotecnico" = ins."Nemotecnico"

		left join :v_tblReporCom as rco    on rco."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
		                                  and rco."IdTipoInstrumento" = ins."IdTipoInstrumento"
		                                  and rco."IdInstrumento" = ins."IdInstrumento"
		                                  and rco."IdBoveda" = ins."IdBoveda"
		                                  and rco."Relacion" = ins."Relacion"
		                                  and rco."Nemotecnico" = ins."Nemotecnico"
		                                  	   
	    order by ins."IdClasificacionTipoInstrumento", ins."IdTipoInstrumento", ins."Nemotecnico", ins."IdBoveda", ins."Relacion";
END;
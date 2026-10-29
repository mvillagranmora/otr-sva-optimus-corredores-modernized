SET SCHEMA "BSCL";

GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::ConsultarCustodiaPortafolioFFMM";

GO
CREATE PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::ConsultarCustodiaPortafolioFFMM" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa						BIGINT,
    IN p_IdUsuario						BIGINT,
    IN p_IdModulo						NVARCHAR(3), 
	
	IN p_IdCliente						BIGINT			DEFAULT 0,
	IN p_IdPortafolio					BIGINT			DEFAULT 0,
	IN p_IdProductoAgrupador			BIGINT			DEFAULT NULL,
	IN p_IdEmisor						NVARCHAR(40)	DEFAULT NULL,
	IN p_IdClasificacionTipoInstrumento	NVARCHAR(3),
	IN p_IdTipoInstrumento				NVARCHAR(3),
	IN p_IdInstrumento					BIGINT			DEFAULT 0,
	IN p_IdBoveda						NVARCHAR(3)		DEFAULT '',				-- solo actua como filtro, no agrupa x boveda
	IN p_AgrupaPorBoveda				NVARCHAR(1)		DEFAULT 'N',
	IN p_AgrupaPorRelacion				NVARCHAR(1)		DEFAULT 'N',			-- S: usa relacion como un criterio más de agrupacion y la muestra   N: no considera relacion en la agrupacion
	IN p_FechaConsulta					NVARCHAR(10)	DEFAULT '0001-01-01',
	IN p_TipoFechaConsulta				NVARCHAR(3)		DEFAULT 'LIQ',		     -- Consultar por LIQ: Fecha Liquidación  ADQ: Fecha Adquisicion
	
	IN p_IdFamiliaFondo 				NVARCHAR(40)    DEFAULT '',
	-- Parametros de Salida
   	OUT p_out_tbl						TABLE (
											"IdClasificacionTipoInstrumento"			NVARCHAR(3)
										   ,"NombreClasificacionTipoInstrumento"		NVARCHAR(100)
										   ,"IdTipoInstrumento"							NVARCHAR(3)
										   ,"NombreTipoInstrumento"						NVARCHAR(100)
										   ,"IdInstrumento"								BIGINT
										   ,"Nemotecnico"								NVARCHAR(40)
										   ,"IdEmisor"									NVARCHAR(40)
										   ,"IdSocioDeNegocio"							BIGINT
										   ,"IdCliente"									BIGINT
										   ,"IdPortafolio"								BIGINT
										   ,"DocumentoIdentidad"						NVARCHAR(40)
										   ,"RazonSocial"								NVARCHAR(100)
										   ,"CodigoPortafolio"							NVARCHAR(100)
										   ,"IdBoveda"									NVARCHAR(3)
										   ,"Relacion"									NVARCHAR(20)
										   ,"CantidadCustodia"							DECIMAL(26,8)
										   ,"CantidadGarantia"							DECIMAL(26,8)
										   ,"INGPrecioPromedioCompra"					DECIMAL(26,8)
										   ,"INGMontoPrecioPromedioCompra"				DECIMAL(26,8)
										   ,"INGMontoPrecioPromedioMonedaInstrumento"	DECIMAL(26,8)
										   ,"TotalPosicion"								DECIMAL(26,8)
										   ,"RegimenTributarioCartera"					NVARCHAR(40)
										   ,"RegimenTributarioGarantia"					NVARCHAR(40)
										) DEFAULT empty
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA

AS
BEGIN
	
	DECLARE v_DocumentoIdentidadCuentaPropia	NVARCHAR(40);
	DECLARE v_IdPersonaCuentaPropia				BIGINT;
	DECLARE v_IdClienteCuentaPropia				BIGINT;
	DECLARE v_FechaConsulta						NVARCHAR(10) := '2300-12-31';
	
	DECLARE v_tblInst TABLE (
		"IdClasificacionTipoInstrumento"	NVARCHAR(3)
	   ,"IdTipoInstrumento"					NVARCHAR(3)
	   ,"IdInstrumento"						BIGINT
	   ,"Nemotecnico"						NVARCHAR(40)
	   ,"IdEmisor"							NVARCHAR(40)
	   ,"IdCliente"							BIGINT
	   ,"IdPortafolio"						BIGINT
	   ,"IdBoveda"							NVARCHAR(3)
	   ,"Relacion"							NVARCHAR(20)   		
	);
	
	DECLARE v_tblCustodia TABLE (
		"IdClasificacionTipoInstrumento"			NVARCHAR(3)
	   ,"IdTipoInstrumento"							NVARCHAR(3)
	   ,"IdInstrumento"								BIGINT
	   ,"Nemotecnico"								NVARCHAR(40)
	   ,"IdEmisor"									NVARCHAR(40)
	   ,"IdCliente"									BIGINT
	   ,"IdPortafolio"								BIGINT
	   ,"IdBoveda"									NVARCHAR(3)
	   ,"Relacion"									NVARCHAR(20)
	   ,"Cantidad"									DECIMAL(26,8)
	   ,"INGPrecioPromedioCompra"					DECIMAL(26,8)
	   ,"INGMontoPrecioPromedioCompra"				DECIMAL(26,8)
	   ,"INGMontoPrecioPromedioMonedaInstrumento"	DECIMAL(26,8)
	   ,"PrecioCompra"							DECIMAL(26,8) --RV.
   	   ,"ValorCompra"							DECIMAL(26,8) --RV.
   	   ,"MontoCompra"							DECIMAL(26,8) --RV.  
	   ,"RegimenTributarioCartera"					NVARCHAR(40)    
	);
	
	DECLARE v_tblGarantias TABLE (
		"IdClasificacionTipoInstrumento"	NVARCHAR(3)
	   ,"IdTipoInstrumento"					NVARCHAR(3)
	   ,"IdInstrumento"						BIGINT
	   ,"Nemotecnico"						NVARCHAR(40)
	   ,"IdEmisor"							NVARCHAR(40)
	   ,"IdCliente"							BIGINT
	   ,"IdPortafolio"						BIGINT
	   ,"IdBoveda"							NVARCHAR(3)
	   ,"Relacion"							NVARCHAR(20)
	   ,"Cantidad"							DECIMAL(26,8)
	   ,"RegimenTributarioGarantia"			NVARCHAR(40) 
	);
	
	
	DECLARE v_FechaSistema NVARCHAR(10);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	
	 
	IF p_FechaConsulta = '0001-01-01' THEN
	
		SELECT top 1 "Valor" 
		INTO v_FechaConsulta
		FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE "CodigoParametro" = 'PLA.FechaSistema'
		AND "IdEmpresa" = p_IdEmpresa;
	
	ELSE
	
		v_FechaConsulta := p_FechaConsulta;
	
	END IF;
	
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
	WHERE PER."IdPersonaOrigen" = :v_IdPersonaCuentaPropia
	AND PER."IdEstado" = 'ACT';	
	
	---------------------------------------------------------
	------------- inicializa las tablas de paso -------------
	---------------------------------------------------------
	v_tblCustodia =
		SELECT
			'' as "IdClasificacionTipoInstrumento"
		   ,'' as "IdTipoInstrumento"
		   , 0 as "IdInstrumento"
		   ,'' as  "Nemotecnico"
		   ,'' as "IdEmisor"
		   , 0 as "IdCliente"
		   , 0 as "IdPortafolio"
		   ,'' as "IdBoveda"
		   ,'' as "Relacion"
		   , 0 as "Cantidad"
		   , 0 as "INGPrecioPromedioCompra"
		   , 0 as "INGMontoPrecioPromedioCompra"
		   , 0 as "INGMontoPrecioPromedioMonedaInstrumento"
		   , 0 as "PrecioCompra"
   		   , 0 as "ValorCompra"	
   		   , 0 as "MontoCompra"	      
		   , '' as "RegimenTributarioCartera"
		FROM DUMMY
		WHERE 1 = 2;
	
	v_tblGarantias =
		SELECT
			'' as "IdClasificacionTipoInstrumento"
		   ,'' as "IdTipoInstrumento"
		   , 0 as "IdInstrumento"
		   ,'' as "Nemotecnico"
		   ,'' as "IdEmisor"
		   , 0 as "IdCliente"
		   , 0 as "IdPortafolio"
		   ,'' as "IdBoveda"
		   ,'' as "Relacion"
		   , 0 as "Cantidad"
		   , '' as "RegimenTributarioGarantia"
		FROM DUMMY
		WHERE 1 = 2;
	
	
	-------------------------- 
	-- CUSTODIA
	-------------------------- 
	BEGIN 
		If To_Date(:p_FechaConsulta) >= To_Date(:v_FechaSistema) THEN 
			-- Detalle
			v_tblCustodia =
				SELECT
				"IdClasificacionTipoInstrumento"
			   ,"IdTipoInstrumento"
			   ,"IdInstrumento"
			   ,"Nemotecnico"
			   ,"IdEmisor"
			   ,"IdCliente"
			   ,"IdPortafolio"
			   ,"IdBoveda"
			   ,"Relacion"
			   ,"Cantidad"
			   , 0 as "INGPrecioPromedioCompra"
			   , 0 as "INGMontoPrecioPromedioCompra"
			   , 0 as "INGMontoPrecioPromedioMonedaInstrumento"
			   , "PrecioCompra"
	   		   , "ValorCompra"	
	   		   , "MontoCompra"	
	   		   , "RegimenTributarioCartera"      
				   FROM
					(
						SELECT
							RANK() over (partition by  CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
						   ,CAR."IdClasificacionTipoInstrumento"
						   ,CAR."IdTipoInstrumento"
						   ,CAR."IdInstrumento"
						   ,CAR."Nemotecnico" as "Nemotecnico"
						   ,CAR."IdEmisor"
						   ,CAR."IdCliente"
						   ,CAR."IdPortafolio"
						   ,CAR."IdBoveda" 
						   ,CASE
						   		WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						   		WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						   		ELSE 'TER'                                                  -- Tercero
						   	END AS "Relacion"
						   ,CAST(IFNULL(CAR."INGCantidad",0) as DECIMAL(26,8)) as "Cantidad"
						   -- Monto Valorizado
						   ,"INGPrecioPromedioCompra"
						   ,"INGMontoPrecioPromedioCompra"
						   ,"INGMontoPrecioPromedioMonedaInstrumento"
						   ,CAR."INGPrecioCompra" as "PrecioCompra"
						   ,CAR."INGMontoCompraActualMonedaInstrumento" as "ValorCompra"
						   ,CAR."INGMontoATasaCompraActual" as "MontoCompra"					
						   ,COALESCE (REG."Descripcion",'') as "RegimenTributarioCartera"
						   
						   
						FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")
						LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = CAR."IdInstrumento" AND Serie."IdEmpresa" = :p_IdEmpresa
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"	
						
						WHERE 
							CAR."IdEmpresa" = p_IdEmpresa
						AND	CAR."IdCliente" = case when p_IdCliente > 0 then p_IdCliente else CAR."IdCliente" END
						AND	CAR."IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else CAR."IdPortafolio" END
						AND	CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','PAC','REP','RRV','RRF')
						AND CAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END
						AND	CAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumento" END
						AND	CAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE CAR."IdInstrumento" END
						AND
						(
							(p_TipoFechaConsulta = 'LIQ' AND (
								-- Filtra por Fecha Liquidacion
								v_FechaConsulta BETWEEN CAR."INGFechaLiquidacion" and case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end
								AND
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end	   
							))
							OR
							(p_TipoFechaConsulta = 'ADQ' AND (
								-- Filtra por Fecha Adquisición
								v_FechaConsulta BETWEEN CAR."INGFechaOperacion" and case when IFNULL(CAR."INGFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') END
								AND
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								v_FechaConsulta < case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') end	
							))
						)
						AND	CAR."IdEstado" <> 'ANU'
						AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
						AND (:p_IdEmisor IS NULL OR CAR."IdEmisor" = :p_IdEmisor)
						AND	Serie."IdFamiliaFondo" = CASE WHEN IFNULL(:p_IdFamiliaFondo,'') <> '' THEN IFNULL(:p_IdFamiliaFondo,'') ELSE Serie."IdFamiliaFondo" END
					)
			WHERE RANK = 1;
		Else
		  -- Detalle
			v_tblCustodia =
				SELECT
				"IdClasificacionTipoInstrumento"
			   ,"IdTipoInstrumento"
			   ,"IdInstrumento"
			   ,"Nemotecnico"
			   ,"IdEmisor"
			   ,"IdCliente"
			   ,"IdPortafolio"
			   ,"IdBoveda"
			   ,"Relacion"
			   ,"Cantidad"
			   , 0 as "INGPrecioPromedioCompra"
			   , 0 as "INGMontoPrecioPromedioCompra"
			   , 0 as "INGMontoPrecioPromedioMonedaInstrumento"
			   , "PrecioCompra"
	   		   , "ValorCompra"	
	   		   , "MontoCompra"	   
	   		   , "RegimenTributarioCartera"   
				   FROM
					(
						SELECT
							RANK() over (partition by  CAR."FolioCartera" order by CAR."IdCartera" desc) as RANK
						   ,CAR."IdClasificacionTipoInstrumento"
						   ,CAR."IdTipoInstrumento"
						   ,CAR."IdInstrumento"
						   ,CAR."Nemotecnico" as "Nemotecnico"
						   ,CAR."IdEmisor"
						   ,CAR."IdCliente"
						   ,CAR."IdPortafolio"
						   ,CAR."IdBoveda" 
						   ,CASE
						   		WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
						   		WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
						   		ELSE 'TER'                                                  -- Tercero
						   	END AS "Relacion"
						   ,CAST(IFNULL(CAR."INGCantidad",0) as DECIMAL(26,8)) as "Cantidad"
						   -- Monto Valorizado
						   ,"INGPrecioPromedioCompra"
						   ,"INGMontoPrecioPromedioCompra"
						   ,"INGMontoPrecioPromedioMonedaInstrumento"
						   ,CAR."INGPrecioCompra" as "PrecioCompra"
						   ,CAR."INGMontoCompraActualMonedaInstrumento" as "ValorCompra"
						   ,CAR."INGMontoATasaCompraActual" as "MontoCompra"					
						   ,COALESCE (REG."Descripcion",'') as "RegimenTributarioCartera"
						   
						   
						FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
						LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = CAR."IdCliente")
						LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
						LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
						LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = CAR."IdInstrumento" AND Serie."IdEmpresa" = :p_IdEmpresa
						LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"	
						WHERE 
							CAR."IdEmpresa" = p_IdEmpresa
						AND	CAR."IdCliente" = case when p_IdCliente > 0 then p_IdCliente else CAR."IdCliente" END
						AND	CAR."IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else CAR."IdPortafolio" END
						AND	CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','PAC','REP','RRV','RRF')
						AND CAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END
						AND	CAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumento" END
						AND	CAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE CAR."IdInstrumento" END
						AND
						(
							(p_TipoFechaConsulta = 'LIQ' AND (
								-- Filtra por Fecha Liquidacion
								v_FechaConsulta BETWEEN CAR."INGFechaLiquidacion" and case when IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end
								AND
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								v_FechaConsulta < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaLiquidacion",'0001-01-01') end	   
							))
							OR
							(p_TipoFechaConsulta = 'ADQ' AND (
								-- Filtra por Fecha Adquisición
								v_FechaConsulta BETWEEN CAR."INGFechaOperacion" and case when IFNULL(CAR."INGFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') END
								AND
								-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
								v_FechaConsulta < case when IFNULL("EGRFechaOperacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(CAR."EGRFechaOperacion",'0001-01-01') end	
							))
						)
						AND	CAR."IdEstado" <> 'ANU'
						AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
						AND (:p_IdEmisor IS NULL OR CAR."IdEmisor" = :p_IdEmisor)
						AND	Serie."IdFamiliaFondo" = CASE WHEN IFNULL(:p_IdFamiliaFondo,'') <> '' THEN IFNULL(:p_IdFamiliaFondo,'') ELSE Serie."IdFamiliaFondo" END
					)
			WHERE RANK = 1;
		End If;
		
		
		
		-- Resumen
		v_tblCustodia =
			SELECT
				"IdClasificacionTipoInstrumento"
			   ,"IdTipoInstrumento"
			   ,"IdInstrumento"
			   ,"Nemotecnico"
			   ,"IdEmisor"
			   ,"IdCliente"
			   ,"IdPortafolio"
			   ,CASE when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
			   ,CASE when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			   ,SUM("Cantidad") as "Cantidad"
			  -- ,"INGPrecioPromedioCompra" 		as "INGPrecioPromedioCompra"
			   ,SUM("ValorCompra")/SUM("Cantidad") AS "INGPrecioPromedioCompra"
			   --,SUM("INGMontoPrecioPromedioCompra") 			as "INGMontoPrecioPromedioCompra"
			    ,(SUM("MontoCompra")/SUM("Cantidad")) * SUM("Cantidad") AS  "INGMontoPrecioPromedioCompra" --moneda de sistema
			   --,SUM("INGMontoPrecioPromedioMonedaInstrumento") 	as "INGMontoPrecioPromedioMonedaInstrumento"
			    ,(SUM("ValorCompra")/SUM("Cantidad")) * SUM("Cantidad") AS  "INGMontoPrecioPromedioMonedaInstrumento" --moneda instrumento
			   ,0 as "PrecioCompra"
		   	   ,SUM("ValorCompra") as "ValorCompra"
		   	   ,SUM("MontoCompra") as "MontoCompra"
		   	   ,"RegimenTributarioCartera"
			FROM :v_tblCustodia
			GROUP BY
				"IdClasificacionTipoInstrumento"
			   ,"IdTipoInstrumento"
			   ,"IdInstrumento"
			   ,"Nemotecnico"
			   ,"IdEmisor"
			   ,"IdCliente"
			   ,"IdPortafolio"
			   ,case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END
			   ,case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
			   ,"RegimenTributarioCartera";
			   --,"INGPrecioPromedioCompra";
		
	END; 
	-- CUSTODIA
	
	
	--------------------------------------
	-- GARANTIAS
	--------------------------------------
	BEGIN
		
		-- Detalle
        If To_Date(:p_FechaConsulta) >= To_Date(:v_FechaSistema) THEN 
			v_tblGarantias =
				SELECT
					GAR."IdClasificacionTipoInstrumento"
				   ,GAR."IdTipoInstrumento"
				   ,GAR."IdInstrumento"
				   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico"
				   ,GAR."IdEmisor"
				   ,GAR."IdCliente"
				   ,GAR."IdPortafolio"
				   ,CAR."IdBoveda" as "IdBoveda"
				   ,CASE
				   		WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				   		WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				   		ELSE 'TER'                                                  -- Tercero
				   	END AS "Relacion"
				   ,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   ,COALESCE (REG."Descripcion",'') as "RegimenTributarioGarantia"
				FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
				INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = GAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = GAR."IdInstrumento" AND Serie."IdEmpresa" = :p_IdEmpresa
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
				WHERE
					GAR."IdEmpresa" = p_IdEmpresa
				AND GAR."IdCliente" = case when p_IdCliente > 0 then p_IdCliente else GAR."IdCliente" END
				AND GAR."IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else GAR."IdPortafolio" END
				AND GAR."IdEstado" <> 'ANU'
				AND v_FechaConsulta BETWEEN GAR."INGFechaLiquidacion" and case when IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
				-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				AND v_FechaConsulta < case when IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
				AND GAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE GAR."IdClasificacionTipoInstrumento" END
				AND GAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE GAR."IdTipoInstrumento" END
				AND GAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE GAR."IdInstrumento" END
				AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
				AND (:p_IdEmisor IS NULL OR CAR."IdEmisor" = :p_IdEmisor)
				AND	Serie."IdFamiliaFondo" = CASE WHEN IFNULL(:p_IdFamiliaFondo,'') <> '' THEN IFNULL(:p_IdFamiliaFondo,'') ELSE Serie."IdFamiliaFondo" END;
		Else
		  v_tblGarantias =
				SELECT
					GAR."IdClasificacionTipoInstrumento"
				   ,GAR."IdTipoInstrumento"
				   ,GAR."IdInstrumento"
				   ,case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."NemoSVS" else GAR."Nemotecnico" end as "Nemotecnico"
				   ,GAR."IdEmisor"
				   ,GAR."IdCliente"
				   ,GAR."IdPortafolio"
				   ,CAR."IdBoveda" as "IdBoveda"
				   ,CASE
				   		WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
				   		WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
				   		ELSE 'TER'                                                  -- Tercero
				   	END AS "Relacion"
				   ,cast(IFNULL(case when GAR."IdClasificacionTipoInstrumento" = 'IF' then GAR."INGValorRescate" else GAR."INGCantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
				   ,COALESCE (REG."Descripcion",'') as "RegimenTributarioGarantia"
				FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
				INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" as CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen"
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = GAR."IdCliente")
				LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = CAR."IdPortafolio" AND Portafolio."IdCliente" = CAR."IdCliente"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS Serie ON Serie."IdInstrumento" = GAR."IdInstrumento" AND Serie."IdEmpresa" = :p_IdEmpresa
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS REG ON REG."IdRegimenFondoMutuo" = CAR."IdRegimenFondoMutuo"
				WHERE
					GAR."IdEmpresa" = p_IdEmpresa
				AND GAR."IdCliente" = case when p_IdCliente > 0 then p_IdCliente else GAR."IdCliente" END
				AND GAR."IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else GAR."IdPortafolio" END
				AND GAR."IdEstado" <> 'ANU'
				AND v_FechaConsulta BETWEEN GAR."INGFechaLiquidacion" and case when IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
				-- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
				AND v_FechaConsulta < case when IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL(GAR."EGRFechaLiquidacion",'0001-01-01') END
				AND GAR."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE GAR."IdClasificacionTipoInstrumento" END
				AND GAR."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE GAR."IdTipoInstrumento" END
				AND GAR."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE GAR."IdInstrumento" END
				AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
				AND (:p_IdEmisor IS NULL OR CAR."IdEmisor" = :p_IdEmisor)
				AND	Serie."IdFamiliaFondo" = CASE WHEN IFNULL(:p_IdFamiliaFondo,'') <> '' THEN IFNULL(:p_IdFamiliaFondo,'') ELSE Serie."IdFamiliaFondo" END;
		End If;
			
		-- Resumen
		v_tblGarantias =
			SELECT
				"IdClasificacionTipoInstrumento"
			   ,"IdTipoInstrumento"
			   ,"IdInstrumento"
			   ,"Nemotecnico"
			   ,"IdEmisor"
			   ,"IdCliente"
			   ,"IdPortafolio"
			   ,case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' end as "IdBoveda"
			   ,case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
			   ,SUM("Cantidad") as "Cantidad"
			   ,"RegimenTributarioGarantia"
			FROM :v_tblGarantias
			GROUP BY
				"IdClasificacionTipoInstrumento"
			   ,"IdTipoInstrumento"
			   ,"IdInstrumento"
			   ,"Nemotecnico"
			   ,"IdEmisor"
			   ,"IdCliente"
			   ,"IdPortafolio"
			   ,case when p_AgrupaPorBoveda = 'S' then "IdBoveda" else '' END
			   ,case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end
			   ,"RegimenTributarioGarantia";
		
	END;
	-- GARANTIAS
	
	
	----------------------------------------------------------
	-- Instrumentos de los cuales el portafolio tiene custodia
	----------------------------------------------------------
	v_tblInst =	
		SELECT DISTINCT
			"IdClasificacionTipoInstrumento"
		   ,"IdTipoInstrumento"
		   ,"IdInstrumento"
		   ,"Nemotecnico"
		   ,"IdEmisor"
		   ,"IdCliente"
		   ,"IdPortafolio"
		   ,"IdBoveda"
		   ,"Relacion"
		FROM
		(
			SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor", "IdCliente", "IdPortafolio", "IdBoveda", "Relacion" FROM :v_tblCustodia
			UNION ALL
			SELECT DISTINCT "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor", "IdCliente", "IdPortafolio", "IdBoveda", "Relacion" FROM :v_tblGarantias
		);
	
	
	------------------------------------------
	-- SALIDA
	------------------------------------------
	p_out_tbl = 
		SELECT
			ins."IdClasificacionTipoInstrumento"
		   ,inst2."NombreClasificacionTipoInstrumento"
		   ,ins."IdTipoInstrumento"
		   ,inst2."NombreTipoInstrumento"
		   ,ins."IdInstrumento"
		   ,ins."Nemotecnico"
		   ,ins."IdEmisor"
		   ,Soc."IdSocioDeNegocio"
		   ,ins."IdCliente"
		   ,ins."IdPortafolio"
		   ,cli."DocumentoIdentidad"
		   ,cli."RazonSocial"
		   ,Port."CodigoPortafolio"
		   ,ins."IdBoveda"
		   ,ins."Relacion"
		   ,cast(sum(ifnull( cus."Cantidad",0)) as DECIMAL(26,8)) as "CantidadCustodia"
		   ,cast(sum(ifnull( gar."Cantidad",0)) as DECIMAL(26,8)) as "CantidadGarantia"
		   ,cus."INGPrecioPromedioCompra"
		   ,cast(sum(ifnull( cus."INGMontoPrecioPromedioCompra",0)) as DECIMAL(26,8)) as "INGMontoPrecioPromedioCompra"
		   ,cast(sum(ifnull( cus."INGMontoPrecioPromedioMonedaInstrumento",0)) as DECIMAL(26,8)) as "INGMontoPrecioPromedioMonedaInstrumento"
		   ,cast((cast(sum(ifnull( cus."Cantidad",0)) as DECIMAL(26,8)) + cast(sum(ifnull( gar."Cantidad",0)) as DECIMAL(26,8))) as DECIMAL(26,8)) as "TotalPosicion"
		   ,cus."RegimenTributarioCartera" as "RegimenTributarioCartera"
		   ,gar."RegimenTributarioGarantia" as "RegimenTributarioGarantia"
		FROM :v_tblInst as ins
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as inst2 on ins."IdInstrumento" = inst2."IdInstrumento"
		LEFT JOIN :v_tblCustodia AS cus
			   ON cus."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
			  AND cus."IdTipoInstrumento" = ins."IdTipoInstrumento"
			  AND cus."IdInstrumento" = ins."IdInstrumento"
			  AND cus."Nemotecnico" = ins."Nemotecnico"
			  AND cus."IdCliente" = ins."IdCliente"
			  AND cus."IdPortafolio" = ins."IdPortafolio"
			  AND cus."IdBoveda" = ins."IdBoveda"
			  AND cus."Relacion" = ins."Relacion"
		LEFT JOIN :v_tblGarantias AS gar
			   ON gar."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
			  AND gar."IdTipoInstrumento" = ins."IdTipoInstrumento"
			  AND gar."IdInstrumento" = ins."IdInstrumento"
			  AND gar."IdCliente" = ins."IdCliente"
			  AND gar."Nemotecnico" = ins."Nemotecnico"
			  AND gar."IdPortafolio" = ins."IdPortafolio"
			  AND gar."IdBoveda" = ins."IdBoveda"
			  AND gar."Relacion" = ins."Relacion"		                                  		                                  		                                  
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = ins."IdPortafolio" 
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as Cli ON Cli."IdCliente" = Port."IdCliente" 
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as Soc ON Cli."IdPersona" = Soc."IdPersona"
	    GROUP BY
	    	ins."IdClasificacionTipoInstrumento"
	       ,inst2."NombreClasificacionTipoInstrumento"
	       ,ins."IdTipoInstrumento"
	       ,inst2."NombreTipoInstrumento"
	       ,ins."IdInstrumento"
	       ,ins."Nemotecnico"
	       ,ins."IdEmisor"
	       ,Soc."IdSocioDeNegocio"
	       ,ins."IdCliente"
	       ,ins."IdPortafolio"
	       ,cli."DocumentoIdentidad"
	       ,cli."RazonSocial"
	       ,Port."CodigoPortafolio"
	       ,ins."IdBoveda"
	       ,ins."Relacion"
	       ,cus."INGPrecioPromedioCompra"
	       ,cus."RegimenTributarioCartera"
	       ,gar."RegimenTributarioGarantia"
	    ORDER BY
	    	ins."IdClasificacionTipoInstrumento"
	       ,ins."IdTipoInstrumento"
	       ,ins."Nemotecnico"
	       ,ins."IdEmisor"
	       ,Soc."IdSocioDeNegocio"
	       ,ins."IdCliente"
	       ,ins."IdPortafolio"
	       ,cli."DocumentoIdentidad"
	       ,cli."RazonSocial"
	       ,Port."CodigoPortafolio"
	       ,ins."IdBoveda"
	       ,ins."Relacion";
	
END;
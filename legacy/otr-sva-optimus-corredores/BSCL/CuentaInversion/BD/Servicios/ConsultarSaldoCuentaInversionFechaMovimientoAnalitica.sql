SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionFechaMovimientoAnalitica";

CREATE PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionFechaMovimientoAnalitica" (
	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN  p_IdEmpresa                		BIGINT default 1,
	IN  p_FechaConsulta 				NVARCHAR(10) 	default '0001-01-01',
  	-- Parametros de Salida
  	OUT p_out_Resultado					TABLE("IdEjecutivo"				BIGINT,
											 "NombreEjecutivo"			NVARCHAR(300),
											 "CodigoSucursal"			NVARCHAR(40),
	 										 "NombreSucursal"			NVARCHAR(100),
											 "IdCuentaInversion"      	BIGINT, 
                                          	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                          	 "IdCliente"               	BIGINT,
                                          	 "NombreCliente"			NVARCHAR(300),
                                          	 "DocumentoIdentidad"		NVARCHAR(40),
                                          	 "IdPortafolio"            	BIGINT,
                                          	 "NombrePortafolio"			NVARCHAR(100),
                                          	 "PropositoPortafolio"		NVARCHAR(100),
                                          	 "CodigoPortafolio"			NVARCHAR(100),
                                          	 "FechaSaldo" 				NVARCHAR(10),
                                          	 "IdMoneda"                	NVARCHAR(3),
                                          	 "CodigoMoneda"				NVARCHAR(40),
                                          	 "NombreMoneda"				NVARCHAR(100),
                                          	 "SaldoDisponible"          DECIMAL(26,8),
                                          	 "SaldoEnGarantia"			DECIMAL(26,8),
                                          	 "SaldoAPlazo"				DECIMAL(26,8),
                                          	 "SaldoAPlazoPactos"		DECIMAL(26,8),
                                          	 "SaldoAPlazoPrestamos"		DECIMAL(26,8),
                                          	 "SaldoRetiro"				DECIMAL(26,8),
                                          	 "SaldoRetenido"			DECIMAL(26,8),
                                          	 "Producto"					NVARCHAR(100),
                                          	 "TipoRegistro"				NVARCHAR(3),
                                          	 "IdMatrizAgrupador"		BIGINT,
                                          	 "EsADC"					NVARCHAR(1),
											 "UltimaOperacion"			BIGINT)						
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA WITH RESULT VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoCuentaInversionFechaMovimientoAnalitica" AS 
BEGIN
  		DECLARE v_SaldoEnGarantia			TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
		DECLARE v_SaldoAPlazo				TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10),"Tipo" NVARCHAR(10));
		DECLARE v_SaldoTesoreria			TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
		DECLARE v_SaldoRetenido				TABLE ("IdCliente" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3),"Monto" DECIMAL(26,8),"FechaSaldo" NVARCHAR(10));
		DECLARE v_SaldoPorFechaMovimiento	TABLE("IdEjecutivo"				BIGINT,
											 "NombreEjecutivo"			NVARCHAR(300),
											 "CodigoSucursal"			NVARCHAR(40),
	 										 "NombreSucursal"			NVARCHAR(100),
											 "IdCuentaInversion"      	BIGINT, 
                                          	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                          	 "IdCliente"               	BIGINT,
                                          	 "NombreCliente"			NVARCHAR(300),
                                          	 "DocumentoIdentidad"		NVARCHAR(40),
                                          	 "IdPortafolio"            	BIGINT,
                                          	 "NombrePortafolio"			NVARCHAR(100),
                                          	 "PropositoPortafolio"		NVARCHAR(100),
                                          	 "CodigoPortafolio"			NVARCHAR(100),
                                          	 "FechaSaldo" 				NVARCHAR(10),
                                          	 "IdMoneda"                	NVARCHAR(3),
                                          	 "CodigoMoneda"				NVARCHAR(40),
                                          	 "NombreMoneda"				NVARCHAR(100),
                                          	 "SaldoDisponible"          DECIMAL(26,8),
                                          	 "SaldoEnGarantia"			DECIMAL(26,8),
                                          	 "SaldoAPlazo"				DECIMAL(26,8),
                                          	 "SaldoAPlazoPactos"		DECIMAL(26,8),
                                          	 "SaldoAPlazoPrestamos"		DECIMAL(26,8),
                                          	 "SaldoRetiro"				DECIMAL(26,8),
                                          	 "SaldoRetenido"			DECIMAL(26,8),
                                          	 "Producto"					NVARCHAR(100),
                                          	 "TipoRegistro"				NVARCHAR(3),
                                          	 "IdMatrizAgrupador"		BIGINT,
                                          	 "EsADC"					NVARCHAR(1),
											 "UltimaOperacion"			BIGINT);
		
		SELECT CASE p_FechaConsulta WHEN '0001-01-01' THEN "Valor" ELSE p_FechaConsulta END INTO p_FechaConsulta FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.FechaSistema' AND "IdEmpresa" = p_IdEmpresa;
		
		
		p_out_Resultado = SELECT  0 as "IdEjecutivo",'' as "NombreEjecutivo",'' as "CodigoSucursal",'' as "NombreSucursal",
								  0 as "IdCuentaInversion",'' as "CodigoCuentaInversion",0 as "IdCliente",'' as "NombreCliente",
								 '' as "DocumentoIdentidad",0 as "IdPortafolio",'' as "NombrePortafolio",'' AS "PropositoPortafolio", '' AS "CodigoPortafolio",
								 '0001-01-01' as "FechaSaldo", '' as "IdMoneda",'' as "CodigoMoneda",'' as "NombreMoneda", 0 as "SaldoDisponible", 
								  0 as "SaldoEnGarantia",0 as "SaldoAPlazo",0 as "SaldoAPlazoPactos",0 as "SaldoAPlazoPrestamos",0 as "SaldoRetiro", 
								  0 as "SaldoRetenido", '' AS "Producto", '' as "TipoRegistro", 0 AS "IdMatrizAgrupador", '' AS "EsADC",0 as "UltimaOperacion" FROM dummy WHERE 1=2;
		v_SaldoPorFechaMovimiento = SELECT  0 as "IdEjecutivo",'' as "NombreEjecutivo",'' as "CodigoSucursal",'' as "NombreSucursal",
								  0 as "IdCuentaInversion",'' as "CodigoCuentaInversion",0 as "IdCliente",'' as "NombreCliente",
								 '' as "DocumentoIdentidad",0 as "IdPortafolio",'' as "NombrePortafolio",'' AS "PropositoPortafolio", '' AS "CodigoPortafolio",
								 '0001-01-01' as "FechaSaldo", '' as "IdMoneda", '' as "CodigoMoneda", '' as "NombreMoneda", 0 as "SaldoDisponible", 
								  0 as "SaldoEnGarantia",0 as "SaldoAPlazo",0 as "SaldoAPlazoPactos",0 as "SaldoAPlazoPrestamos",0 as "SaldoRetiro", 
								  0 as "SaldoRetenido",'' AS "Producto", '' as "TipoRegistro", 0 AS "IdMatrizAgrupador", '' AS "EsADC",0 as "UltimaOperacion" FROM dummy WHERE 1=2;
		v_SaldoEnGarantia = SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
		v_SaldoAPlazo = SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo",'' as "Tipo" FROM DUMMY WHERE 1=2;
		v_SaldoTesoreria = SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
		v_SaldoRetenido = SELECT 0 as "IdCliente",0 as "IdPortafolio",'' as "IdMoneda",0 as "Monto",'0001-01-01' as "FechaSaldo" FROM DUMMY WHERE 1=2;
		
		v_InfoCuentasDeInversion =
			SELECT		 		 
				EPOR."IdEmpleadoEjecutivo" as "IdEjecutivo"
			   ,EJENOM."NombreCompleto" as "NombreEjecutivo"
			   ,SUC."CodigoSucursal"
			   ,SUC."NombreSucursal"
			   ,CLI."IdPersona"
			   ,CTAINV."IdCuentaInversion"
			   ,CTAINV."IdCliente"
			   ,IDENT."DocumentoIdentidad"
			   ,PERNOM."NombreCompleto" as "NombreCliente"
			   ,CTAINV."IdPortafolio"
			   ,POR."DescripcionPortafolio" as "NombrePortafolio"
			   ,POR."Proposito" AS "PropositoPortafolio"
			   ,POR."CodigoPortafolio"
			   ,CTAINV."IdMoneda"
			   ,Mon."Codigo" as "CodigoMoneda"
			   ,(Mon."Codigo" ||' - '|| Mon."Nombre") as "NombreMoneda"
			   ,CTAINV."CuentaInversionPrincipal"
			FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTAINV
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" as POR ON POR."IdPortafolio" = CTAINV."IdPortafolio"
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdCliente" = CTAINV."IdCliente"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND IDENT."Borrado" = 'N'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PERNOM ON PER."IdPersona" = PERNOM."IdPersona"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as Mon ON Mon."IdMoneda" = CTAINV."IdMoneda"
			LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as EPOR ON EPOR."IdEmpleadoEjecutivo"	= POR."IdEmpleadoEjecutivo"
			LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EPOR."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" EJENOM ON EMP."IdPersona" = EJENOM."IdPersona"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC ON SUC."IdSucursal" = EMP."IdSucursal";
		
		
			CALL "BSCL.CuentaInversion.BD.Servicios::SaldoCuentaInversionFechaMovimientoAnalitica" (
				 p_IdEmpresa					=> p_IdEmpresa
				,p_FechaDesde					=> p_FechaConsulta
			    ,p_FechaHasta					=> p_FechaConsulta
	 			,p_out_Saldo					=> v_out_Saldo
			);
				
			--2) Consulta operaciones a Plazo
			v_SaldoAPlazo = 
			WITH
				OperacionesAPlazo AS
				(
					SELECT
						CAR."IdCliente"
					   ,CAR."IdPortafolio"
					   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
					   ,MAP(CAR."IdTipoOperacion",'EMPRES',-1*CAR."INGMontoATasaCompraActual",CAR."INGMontoATasaCompraActual" ) AS "Monto"
					   ,:p_FechaConsulta AS "FechaSaldo"							
					FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
					INNER JOIN :v_InfoCuentasDeInversion AS CI 
							ON CI."IdCliente" 		= CAR."IdCliente"
						   AND CI."IdPortafolio" 	= CAR."IdPortafolio"
						   AND CI."IdMoneda" 		= CAR."IdMonedaInstrumento"
					WHERE
						"IdTipoOperacion" 			IN ('PRESTA','EMPRES')
					AND CAR."INGFechaLiquidacion"	>= :p_FechaConsulta
					AND CAR."IdEmpresa" 			= :p_IdEmpresa
					AND CAR."IdEstado"				= 'VIG'
				),
				OperacionesAPlazoSimultaneas AS
				(
					SELECT
						CAR."IdCliente"
					   ,CAR."IdPortafolio"
					   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
					   ,MAP(CAR."IdTipoOperacion",'VENSIM',-1*CAR."INGMontoAPlazo",CAR."INGMontoAPlazo" ) AS "Monto"
					   ,:p_FechaConsulta AS "FechaSaldo"							
					FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
					INNER JOIN :v_InfoCuentasDeInversion AS CI
							ON CI."IdCliente" 		= CAR."IdCliente"
						   AND CI."IdPortafolio" 	= CAR."IdPortafolio"
						   AND CI."IdMoneda" 		= CAR."IdMonedaInstrumento"
					WHERE
						"IdTipoOperacion" 					IN ('COMSIM','VENSIM')
					AND CAR."IdTipoInstrumento"				=  'SIM'
					AND CAR."FechaVencimientoOperacion" 	>= :p_FechaConsulta
					AND CAR."IdEmpresa" 					= :p_IdEmpresa
					AND CAR."IdEstado"						= 'VIG'
				),
				OperacionesAPlazoPactos AS
				(
					SELECT
						CAR."IdCliente"
					   ,CAR."IdPortafolio"
					   ,CASE WHEN CAR."IdClasificacionTipoInstrumento" IN ('IF','RF') THEN CAR."IdTipoReajuste" ELSE CAR."IdMonedaInstrumento" END AS "IdMoneda"
					   ,MAP(CAR."IdTipoOperacion",'COMPAC',-1*CAR."INGMontoATasaCompraActual",CAR."INGMontoATasaCompraActual" ) AS "Monto"
					   ,:p_FechaConsulta AS "FechaSaldo"							
					FROM "BSCL.Custodia.BD::CUS.Cartera" as CAR
					INNER JOIN :v_InfoCuentasDeInversion as CI
							ON CI."IdCliente" 		= CAR."IdCliente"
						   AND CI."IdPortafolio" 	= CAR."IdPortafolio"
						   AND CI."IdMoneda" 		= CAR."IdTipoReajuste"
					WHERE
						"IdTipoOperacion" 			IN ('COMPAC','VENPAC')
					AND CAR."IdTipoInstrumento" = 'PAC'
					AND CAR."INGFechaLiquidacion" 	>= :p_FechaConsulta
					AND CAR."IdEmpresa" 			= :p_IdEmpresa
					AND CAR."IdEstado"				= 'VIG'
				)
			SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo","Tipo"
			FROM
			(				  		
				SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo",'PRT' AS "Tipo"
				FROM OperacionesAPlazo
				UNION ALL
				SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo",'SIM' AS "Tipo"
				FROM OperacionesAPlazoSimultaneas
				UNION ALL
				SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo" ,'PAC' AS "Tipo"
				FROM OperacionesAPlazoPactos
			)
			GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda","Tipo";				 
			
			
			--3) Consultar Garantias
			v_SaldoEnGarantia =
			WITH
				Garantias AS
				(
					SELECT
						GAR."IdCliente"
					   ,GAR."IdPortafolio"
					   ,GAR."IdMonedaInstrumento" AS "IdMoneda"
					   ,GAR."INGCantidad" AS "Monto"
					   ,:p_FechaConsulta AS "FechaSaldo"
					FROM "BSCL.Custodia.BD::GTA.Garantia" as GAR
					INNER JOIN :v_InfoCuentasDeInversion as CI
							ON CI."IdCliente" 		= GAR."IdCliente"
						   AND CI."IdPortafolio"	= GAR."IdPortafolio"
						   AND CI."IdMoneda"		= GAR."IdMonedaInstrumento"
					WHERE
						GAR."IdEstado" 			= 'VIG'
					AND GAR."IdOrigenGarantia" 	= 'CTA'
					AND GAR."IdEmpresa" 			= :p_IdEmpresa
				)
			SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
			FROM Garantias
			GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
			
			
			--4) Ingresos y Egresos
			v_SaldoTesoreria =
			WITH
				MovimientosTesoreria AS 
				(
					SELECT
						0 AS "IdCliente"
					   ,IE."IdPortafolioTitularPago" as "IdPortafolio"
					   ,IE."IdMoneda"
					   ,CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN -1*IE."Monto" ELSE IE."Monto" END "Monto"
					   ,:p_FechaConsulta AS "FechaSaldo"
					FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
					INNER JOIN :v_InfoCuentasDeInversion as CI
							ON CI."IdPortafolio" 	= IE."IdPortafolioTitularPago"
						   AND CI."IdMoneda"		= IE."IdMoneda"
					WHERE
						IE."IdEstado" 			= 'ING'
					AND IE."FechaLiquidacion"	>= :p_FechaConsulta
					AND IE."IdEmpresa" 			= :p_IdEmpresa
				),
				ComprobanteTesoreria AS
				(
					SELECT
						0 AS "IdCliente"
					   ,COM."IdPortafolioTitularPago" as "IdPortafolio"
					   ,COM."IdMoneda"
					   ,CASE WHEN COM."IdTipoMovimiento" = 'MOVEGR' THEN -1*COM."Monto" ELSE COM."Monto" END "Monto"
					   ,:p_FechaConsulta AS "FechaSaldo"
					FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
					INNER JOIN :v_InfoCuentasDeInversion as CI
							ON CI."IdPortafolio" 	= COM."IdPortafolioTitularPago"
						   AND CI."IdMoneda"		= COM."IdMoneda"
					WHERE
						COM."IdEstado" 			NOT IN ('ANU','MOD','LIQ')
					AND COM."FechaLiquidacion"	>= :p_FechaConsulta
					AND COM."IdEmpresa" 		= :p_IdEmpresa
				)
			SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") as DECIMAL(26,8)) as "Monto","FechaSaldo"
			FROM
			(
				SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo"
				FROM MovimientosTesoreria
				UNION ALL
				SELECT "IdCliente","IdPortafolio","IdMoneda","Monto","FechaSaldo"
				FROM ComprobanteTesoreria
			)
			GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";
			
			--5) Saldo retenido
			v_SaldoRetenido = 
			WITH
			    CuentaInversion AS
			    (
			      SELECT
			      info."IdCliente"
			      ,info."IdPortafolio"
			      ,info."IdMoneda"
			      ,CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVABN' THEN cargoabonomov."Monto" ELSE -1 * cargoabonomov."Monto" END AS "Monto"
			      ,:p_FechaConsulta AS "FechaSaldo"
			      FROM :v_InfoCuentasDeInversion info
			      INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA ON CTA."IdPortafolio" = info."IdPortafolio" and CTA."IdMoneda" = info."IdMoneda"
			      INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov ON (cargoabonomov."IdCuentaInversion" = info."IdCuentaInversion")
				  WHERE cargoabonomov."IdEstado" = 'CRT' AND cargoabonomov."FechaSistema" <= :p_FechaConsulta AND cargoabonomov."FechaEfectivaMovimiento" > :p_FechaConsulta AND cargoabonomov."IdEmpresa" = :p_IdEmpresa
			    )
			SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
			FROM CuentaInversion
			GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";

			
			
			v_SaldoPorFechaMovimiento = 
			SELECT
				InfCtaInv."IdEjecutivo"
			   ,CAST(InfCtaInv."NombreEjecutivo" as NVARCHAR(300)) as "NombreEjecutivo"
			   ,InfCtaInv."CodigoSucursal"
			   ,CAST(InfCtaInv."NombreSucursal" as NVARCHAR(100)) as "NombreSucursal"
			   ,SPM."IdCuentaInversion"
			   ,SPM."CodigoCuentaInversion"
			   ,SPM."IdCliente"
			   ,CAST(InfCtaInv."NombreCliente" as NVARCHAR(300)) as "NombreCliente"
			   ,InfCtaInv."DocumentoIdentidad"
			   ,SPM."IdPortafolio"
			   ,CAST(InfCtaInv."NombrePortafolio" as NVARCHAR(100)) as "NombrePortafolio"
			   ,CAST(InfCtaInv."PropositoPortafolio" as NVARCHAR(100)) as "PropositoPortafolio"
			   ,CAST(InfCtaInv."CodigoPortafolio" as NVARCHAR(100)) as "CodigoPortafolio"
			   ,SPM."FechaSaldo"
			   ,SPM."IdMoneda"
			   ,InfCtaInv."CodigoMoneda"
			   ,CAST(InfCtaInv."NombreMoneda" as NVARCHAR(100)) as "NombreMoneda"
			   ,SPM."Saldo" as "SaldoDisponible"
			   ,CAST(COALESCE(sldGar."Monto",0) as DECIMAL(26,8)) as "SaldoEnGarantia"
			   ,CAST(COALESCE(sldPlz."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazo"
			   ,CAST(COALESCE(sldPlzPac."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoPactos"
			   ,CAST(COALESCE(sldPlzPrt."Monto",0) as DECIMAL(26,8)) as "SaldoAPlazoPrestamos"
			   ,CAST(COALESCE(SldTes."Monto",0) as DECIMAL(26,8)) as "SaldoRetiro"
			   ,CAST(COALESCE(SldRet."Monto",0) as DECIMAL(26,8)) as "SaldoRetenido"
			   ,ProductoAgrupador."NombreProductoAgrupador" as "Producto"
			   ,'MOV' AS "TipoRegistro"
			   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
			   ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC",
			   "UltimaOperacion"					   
			FROM :v_out_Saldo as SPM
			INNER JOIN :v_InfoCuentasDeInversion AS InfCtaInv ON (SPM."IdCuentaInversion" = InfCtaInv."IdCuentaInversion")
			 LEFT JOIN :v_SaldoEnGarantia AS sldGar
					ON sldGar."IdCliente" 		= SPM."IdCliente"
				   AND sldGar."IdPortafolio" 	= SPM."IdPortafolio"
				   AND sldGar."IdMoneda" 		= SPM."IdMoneda"
				   AND sldGar."FechaSaldo" 		= SPM."FechaSaldo"
			 LEFT JOIN :v_SaldoAPlazo as sldPlz
			 		ON sldPlz."IdCliente" 		= SPM."IdCliente"
			 	   AND sldPlz."IdPortafolio" 	= SPM."IdPortafolio"
			 	   AND sldPlz."IdMoneda" 		= SPM."IdMoneda"
			 	   AND sldPlz."FechaSaldo" 		= SPM."FechaSaldo"
			 	   AND sldPlz."Tipo" = 'SIM'
			LEFT JOIN :v_SaldoAPlazo as sldPlzPac
			 		ON sldPlzPac."IdCliente" 		= SPM."IdCliente"
			 	   AND sldPlzPac."IdPortafolio" 	= SPM."IdPortafolio"
			 	   AND sldPlzPac."IdMoneda" 		= SPM."IdMoneda"
			 	   AND sldPlzPac."FechaSaldo" 		= SPM."FechaSaldo"
			 	   AND sldPlzPac."Tipo" = 'PAC'
			 LEFT JOIN :v_SaldoAPlazo as sldPlzPrt
			 		ON sldPlzPrt."IdCliente" 		= SPM."IdCliente"
			 	   AND sldPlzPrt."IdPortafolio" 	= SPM."IdPortafolio"
			 	   AND sldPlzPrt."IdMoneda" 		= SPM."IdMoneda"
			 	   AND sldPlzPrt."FechaSaldo" 		= SPM."FechaSaldo"
			 	   AND sldPlzPrt."Tipo" = 'PRT'
			 LEFT JOIN :v_SaldoTesoreria as SldTes
			 		ON SldTes."IdPortafolio"		= SPM."IdPortafolio"
			 	   AND SldTes."IdMoneda"			= SPM."IdMoneda"
			 	   AND SldTes."FechaSaldo"		= SPM."FechaSaldo"
			 LEFT JOIN :v_SaldoRetenido as SldRet
			 		ON SldRet."IdPortafolio"		= SPM."IdPortafolio"
			 	   AND SldRet."IdMoneda"			= SPM."IdMoneda"
			 	   AND SldRet."FechaSaldo"		= SPM."FechaSaldo"
			 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
					ON Portafolio."IdPortafolio" = SPM."IdPortafolio" 
				   AND Portafolio."IdCliente"    = SPM."IdCliente"
			 LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador 
					ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
			 LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador 
					ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
			 LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND  Portafolio."IdMatrizAgrupadorAPV" = 0;
			
			p_out_Resultado = 
			SELECT COALESCE("IdEjecutivo",0) AS "IdEjecutivo"
				 ,COALESCE("NombreEjecutivo",'') AS "NombreEjecutivo"
				 ,COALESCE("CodigoSucursal",'') AS "CodigoSucursal"
				 ,COALESCE("NombreSucursal",'') AS "NombreSucursal"
				 ,"IdCuentaInversion" 
              	 ,"CodigoCuentaInversion" 
              	 ,"IdCliente"
              	 ,"NombreCliente"
              	 ,"DocumentoIdentidad"
              	 ,"IdPortafolio"
              	 ,"NombrePortafolio"
              	 ,"PropositoPortafolio"
              	 ,"CodigoPortafolio"
              	 ,"FechaSaldo"
              	 ,"IdMoneda"
              	 ,"CodigoMoneda"
              	 ,"NombreMoneda"
              	 ,"SaldoDisponible"
              	 ,"SaldoEnGarantia"
              	 ,"SaldoAPlazo"
              	 ,"SaldoAPlazoPactos"
              	 ,"SaldoAPlazoPrestamos"
              	 ,"SaldoRetiro"
              	 ,"SaldoRetenido"
              	 ,COALESCE("Producto",'') AS "Producto"
              	 ,"TipoRegistro"
              	 ,"IdMatrizAgrupador"
              	 ,"EsADC"
				 ,"UltimaOperacion"
            FROM :v_SaldoPorFechaMovimiento;
END;

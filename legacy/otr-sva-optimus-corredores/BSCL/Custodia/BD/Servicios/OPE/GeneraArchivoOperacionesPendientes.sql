SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.GeneraArchivoOperacionesPendientes";
GO
--* Mofidicaciones
--* Autor .........: CGO
--* Fecha Creacion : Mayo 2019
--* Jira		   : CELCONDOR-434 Permitir realizar Traspasos y Transferencia de Títulos al DCV

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.GeneraArchivoOperacionesPendientes"
(
     -- Parametros de Validacion
	IN p_IdEmpresa BIGINT,  
	IN p_IdUsuario BIGINT,   
	IN p_IdModulo  NVARCHAR(4), 
     
	IN 	p_Registros	TABLE ( "IdRegistro" BIGINT,	
							"IdOperacionPendiente" BIGINT,	
							"IdTipoOperacion" NVARCHAR(6),
							"FolioOperacion" BIGINT,
							"Clave" NVARCHAR(15) ),
	OUT p_out_Data	TABLE ( "Linea" NVARCHAR(130),
							"Estado" NVARCHAR(5) ),
    
     --Salidas para mensajes de advertencias y errores
	OUT p_out_ErroresContador 	INT,
	OUT p_out_ErroresDetalle 	TABLE (	
									"IdInterno" NVARCHAR(40), 
									"Codigo" NVARCHAR(203),
									"Descripcion" NVARCHAR(520) )
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 

AS
BEGIN
    --------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_out_ErroresDetalle  	TABLE (
										"IdInterno" NVARCHAR(40), 
										"Codigo" NVARCHAR(100),
										"ValorParametro" NVARCHAR(100),
										"NombreParametro" NVARCHAR(40) )
									DEFAULT SELECT 0  AS "IdInterno" 
												, ''  AS "Codigo"
												, ''  AS "ValorParametro" 
												, ''  AS "NombreParametro" 
											FROM dummy 
											WHERE 1=2;

	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :v_out_ErroresDetalle;

		v_TablaCodigosMensajes = 
			SELECT 	"Codigo" AS "CodigoMensaje",
					"NombreParametro" AS "CodigoDato",
					"ValorParametro" AS "ValorDato" 
			FROM 	:v_out_ErroresDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" 
			(:v_TablaCodigosMensajes, v_out_ErroresDetalleMensajes );
		
		p_out_ErroresDetalle = 	
			SELECT 	Err."IdInterno",
					Err."Codigo"||' - '||"ValorParametro" as "Codigo",
					Msj."Descripcion"
			FROM 	:v_out_ErroresDetalle Err
					INNER JOIN :v_out_ErroresDetalleMensajes Msj on Err."Codigo" = Msj."Codigo";
         
	END;
	
	p_out_ErroresContador := 0;
	p_out_ErroresDetalle = 
		SELECT * FROM :p_out_ErroresDetalle WHERE 1 = 2;
	
	
	BEGIN  --Logica

		DECLARE	v_FechaOperacionSecondate SECONDDATE;
		DECLARE v_NroRegistro BIGINT;
		-- Cuentas --------------------------------------
		DECLARE v_NroCta3ro NVARCHAR(16);
		DECLARE v_NroCtaPropia NVARCHAR(16);
		DECLARE v_NroCtaRelacionado NVARCHAR(16);
		DECLARE v_NroCtaLiquidacion NVARCHAR(16);		
		-- Corredor -------------------------------------
		DECLARE v_IdClienteCorredor BIGINT;

		CREATE LOCAL TEMPORARY TABLE #TmpCorredoras (
			"IdCorredora" BIGINT,
			"IdPersona" BIGINT,
			"IdCliente" BIGINT,
			"IdPortafolio" NVARCHAR(16)
		);
		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
			( :p_IdEmpresa, v_FechaOperacionSecondate );		

		-- Cta. DCV para Cta. Propia
		SELECT 	"Valor" INTO v_NroCtaPropia 
		FROM 	"BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia" 
		WHERE	"CodigoParametro" = 'CuentaDCVCuentaPropia' AND "IdEmpresa" = p_IdEmpresa;
			
		-- Cta. DCV para Cta. 3ro
		SELECT 	"Valor" INTO v_NroCta3ro
		FROM 	"BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia" 
		WHERE	"CodigoParametro" = 'CuentaDCVCuentaTerceros' AND "IdEmpresa" = p_IdEmpresa;

		-- Cta. DCV para Relacionados
		SELECT 	"Valor" INTO v_NroCtaRelacionado
		FROM 	"BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia" 
		WHERE	"CodigoParametro" = 'CuentaDCVCuentaRelacionado' AND "IdEmpresa" = p_IdEmpresa;

		-- Cta. DCV para Liquidacion
		SELECT 	"Valor" INTO v_NroCtaLiquidacion
		FROM 	"BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia" 
		WHERE	"CodigoParametro" = 'CuentaDCVCuentaLiquidacion' AND "IdEmpresa" = p_IdEmpresa;

		-- ID Corredor
		SELECT	CLI."IdCliente"  INTO v_IdClienteCorredor
		FROM	"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
				INNER JOIN  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON CLI."IdPersona" = EMP."IdPersona"
		WHERE	EMP."IdEmpresa" = p_IdEmpresa;

		v_NroCta3ro = COALESCE( v_NroCta3ro, '');
		v_NroCtaPropia = COALESCE( v_NroCtaPropia, '');
		v_NroCtaRelacionado = COALESCE( v_NroCtaRelacionado, '');
		v_NroCtaLiquidacion = COALESCE( v_NroCtaLiquidacion, '');
		v_IdClienteCorredor = COALESCE( v_IdClienteCorredor, 0);

		-- Para las cuentas de los corredores contrapartes
		INSERT INTO #TmpCorredoras
		SELECT 	"IdCorredora", Soc."IdPersona", Soc."IdCliente", 0
		FROM 	"BSCL.Plataforma.BD::INS.Corredora"Corr
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Soc 
				ON Corr."IdPersona" = Soc."IdPersona";

		UPDATE 	Reg
		SET 	Reg."IdPortafolio" = Por."IdPortafolio"
		FROM 	#TmpCorredoras Reg, "BSCL.Portafolio.BD::POR.Portafolio" Por 
		WHERE	Reg."IdCliente" = Por."IdCliente" AND Por."CodigoPortafolio" = '0';				

		-- Si vienen IDRegistro en blanco => debe crearlos en la tabla de Traspaso DCV
		-- Puede venir mas de un registro para la misma operacion => se deja solo 1 registro.
		tbl_PreNuevos = 					
			SELECT	DISTINCT
					"IdTipoOperacion"	AS "IdTipoOperacion"
					,"FolioOperacion"	AS "FolioOperacion"
					,''					AS "Clave"
					,'PEN'				AS "IdEstado"
					,p_IdUsuario		AS "IdUsuario"
					,v_FechaOperacionSecondate	AS "FechaEstado"
			FROM	:p_Registros
			WHERE	"IdOperacionPendiente" = 0;


		tbl_Nuevos = 					
			SELECT	0 AS "IdRegistro"
					,"BSCL.Custodia.BD.Secuencias::IdOperacionPendiente".NEXTVAL AS "IdOperacionPendiente"
					,"IdTipoOperacion"
					,"FolioOperacion"
					,"Clave"
					,"IdEstado"
					,"IdUsuario"
					,"FechaEstado"
			FROM	:tbl_PreNuevos;

		
		-- Inserta nuevos registros que este recien procesando (Garantias, Traspasos)
		INSERT	INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes"
				( "IdOperacionPendiente", "IdTipoOperacion", "FolioOperacion", "Clave", "IdEstado",
				  "IdUsuario", "FechaEstado"	)
		SELECT	"IdOperacionPendiente", "IdTipoOperacion", "FolioOperacion", "Clave", "IdEstado",
				"IdUsuario", "FechaEstado"
		FROM	:tbl_Nuevos;

		
		tbl_Registros = 
			SELECT	Prm."IdRegistro",
					Prm."IdOperacionPendiente",
					Prm."IdTipoOperacion",
					Prm."FolioOperacion",
					COALESCE(Ope."Clave", '') AS "Clave"
			FROM	:p_Registros Prm
					LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" Ope
					ON Prm."IdOperacionPendiente" = Ope."IdOperacionPendiente"
			WHERE	Prm."IdOperacionPendiente" > 0 
			-----------------------------------------
			UNION ALL
			-----------------------------------------
			SELECT	"IdRegistro",
					"IdOperacionPendiente",
					"IdTipoOperacion",
					"FolioOperacion",
					"Clave"
			FROM	:tbl_Nuevos;


		-- Preparo las cuentas de Clientes Relacionados...
		tbl_CLientes = 
			SELECT 	Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					DCOMP."IdCliente",
					DCOMP."IdPortafolio"
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante" AS DCOMP
					ON	Reg."FolioOperacion" = DCOMP."IdMovimientoComprobante" AND Reg."IdTipoOperacion" = DCOMP."IdTipoComprobante"
			-----------------------------------------
			UNION ALL
			-----------------------------------------
			SELECT 	Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					GARAN."IdCliente",
					GARAN."IdPortafolio"
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" AS GARAN				
					ON	Reg."FolioOperacion" = GARAN."FolioComprobante"--GARAN."FolioGarantia" 
						AND GARAN."IdEstado" IN ('VIG', 'LIQ')
			GROUP BY 
				Reg."IdOperacionPendiente", 
				Reg."IdTipoOperacion", 
				Reg."FolioOperacion",
				GARAN."IdCliente",
				GARAN."IdPortafolio"
						
						-- AND Reg."IdTipoOperacion" = 'INGGTA' AND GARAN."IdEstado" = 'VIG'
			-----------------------------------------
			UNION ALL
			-----------------------------------------
			SELECT 	Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					OPER."IdCliente",
					OPER."IdPortafolio"
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPER
					ON	Reg."FolioOperacion" = OPER."FolioOperacion" AND Reg."IdTipoOperacion" = OPER."IdTipoOperacion"
						AND OPER."IdEstado" = 'VIG';
/*
			-----------------------------------------
			UNION ALL
			-----------------------------------------
			SELECT 	Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					Tmp."IdCliente",
					Tmp."IdPortafolio"
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS Trx
					ON Trx."IdTransaccion" = Reg."FolioOperacion"			
					INNER JOIN #TmpCorredoras AS Tmp
					ON Trx."IdCorredorContraparte" = Tmp."IdCorredora"; 
*/

		--* Nemos IF/RF
		--* -----------------------------------------------------
		tbl_Nemos_MA = 
			SELECT 	DISTINCT 
					COALESCE(CAR."FolioCartera", 0) AS "FolioCartera",
					COALESCE(CAR."Nemotecnico", DET."Nemotecnico") AS "Nemotecnico",
					COALESCE(CAR."NemoSVS", DET."Nemotecnico") AS "NemoSVS"
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Mesa.BD::MES.Operacion" OPER
					ON	Reg."FolioOperacion" = OPER."FolioOperacion" AND Reg."IdTipoOperacion" = OPER."IdTipoOperacion"
						AND OPER."IdEstado" = 'VIG'
					LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" DET
					ON 	OPER."IdOperacion" = DET."IdOperacion"
					LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
					ON CAR."FolioCartera" = DET."FolioCarteraCustodia";


		--* Cuentas DCV, Mandante y Socio
		--* -----------------------------------------------------
		tbl_Ctas_TMP = 
 			SELECT 	CLI."IdOperacionPendiente", 
					CLI."IdTipoOperacion", 
					CLI."FolioOperacion",
					CLI."IdCliente",
					CLI."IdPortafolio",
					IND."ValorIndicador" AS "EsRelacionado", -- "N" > No relacionado => Cta 3ros.., "S" > Relacionado => Cta Relacionados
					COALESCE(CTA."Codigo", ' ') AS "Cuenta3ro",
					CASE WHEN CLI."IdCliente" = v_IdClienteCorredor THEN v_NroCtaPropia
						 WHEN COALESCE(CTA."Codigo", ' ') > '' THEN CTA."Codigo"
						 WHEN IND."ValorIndicador" = 'S' THEN v_NroCtaRelacionado
						 ELSE v_NroCta3ro
					END	AS "CuentaCli_DCV"
			FROM 	:tbl_Clientes CLI
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" CTA
					ON 	CLI."IdPortafolio" = CTA."IdPortafolio" 
						AND CTA."IdBoveda" = 'DCV' AND CTA."IdEstado" = 'ACT' AND CTA."IdEstadoAutorizacion" = 'AUT'						
					LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND					
					ON 	IND."IdUso" = 'IND' AND CLI."IdCliente" = IND."IdCliente" AND IND."GrupoIndicador" = 'NORMATIVOS' 
						AND "CodigoTipoIndicador" = 'RELCORR';

		tbl_Socio = 
 			SELECT 	ROW_NUMBER() OVER (PARTITION BY  DCV."IdPortafolio") AS "IdSocio",
 					DCV."IdPortafolio",
 					DCV."Codigo"
			FROM  	"BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" DCV
					INNER JOIN :tbl_Ctas_TMP TMP
					ON TMP."IdPortafolio" = DCV."IdPortafolio" 
			WHERE 	DCV."IdEstado" = 'ACT' AND DCV."IdEstadoAutorizacion" = 'AUT';
			

		tbl_Ctas = 
			SELECT	CTA.*,
					CASE WHEN CTA."IdCliente" = v_IdClienteCorredor THEN v_NroCtaPropia
						 ELSE COALESCE( SOC."Codigo", ' ') 
					END AS "Socio_DCV"
			FROM 	:tbl_Ctas_TMP CTA
					LEFT JOIN :tbl_Socio SOC
					ON CTA."IdPortafolio" = SOC."IdPortafolio" AND SOC."IdSocio" = 1;


		--* Los registros de Venta deben tener clave, de lo contrario no se pueden ir...
		--* ----------------------------------------------------------------------------
		tbl_Tmp	=
			SELECT	Reg."IdRegistro",
					Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					Reg."Clave",
					DCOMP."IdCliente",
					DCOMP."IdPortafolio",
					'FD' AS "TipoRegistro",
					CASE WHEN DCOMP."IdFormaCustodia" = 'CUS' 
						      AND LEFT(DCOMP."AccionTipoOperacion",2) = 'VE' THEN Ctas."CuentaCli_DCV"
 						 WHEN DCOMP."IdFormaCustodia" = 'CUS' THEN v_NroCtaLiquidacion
						 ELSE v_NroCtaLiquidacion 
					END  AS "CtaOrigen",
					CASE WHEN DCOMP."IdFormaCustodia" = 'CUS' 
						 	  AND LEFT(DCOMP."AccionTipoOperacion",2) = 'VE' THEN v_NroCtaLiquidacion
 						 WHEN DCOMP."IdFormaCustodia" = 'CUS' THEN Ctas."CuentaCli_DCV"
						 ELSE Ctas."Socio_DCV" END AS "CtaDestino",
					-------------------------------------------------------------------------------
					TO_VARCHAR(DCOMP."FechaIngresoRegistro", 'DDMMYYYY') ||		-- FECHA OPERACION
					CASE WHEN DCOMP."IdFormaCustodia" = 'CUS' 
							  AND LEFT(DCOMP."AccionTipoOperacion",2) = 'VE' THEN RPAD( Ctas."CuentaCli_DCV", 8, ' ' )
 						 WHEN DCOMP."IdFormaCustodia" = 'CUS' THEN RPAD( v_NroCtaLiquidacion, 8, ' ' ) 
						 ELSE RPAD( v_NroCtaLiquidacion, 8, ' ' ) END ||		-- CUENTA ORIGEN
					CASE WHEN DCOMP."IdFormaCustodia" = 'CUS' 
						 	  AND LEFT(DCOMP."AccionTipoOperacion",2) = 'VE' THEN RPAD( v_NroCtaLiquidacion, 8, ' ' )
 						 WHEN DCOMP."IdFormaCustodia" = 'CUS' THEN RPAD( Ctas."CuentaCli_DCV", 8, ' ' ) 						 	  
						 ELSE RPAD( Ctas."Socio_DCV", 8, ' ' ) END ||			-- CONTRAPARTE (DESTINO)
					RPAD(Reg."Clave", 15, ' ')	||								-- CLAVE
					TO_VARCHAR(DCOMP."FechaLiquidacion", 'DDMMYYYY') ||			-- FECHA LIQUIDACION
					CASE WHEN DCOMP."IdFormaCustodia" = 'CUS' THEN 'TC'
						 ELSE 'CV' END ||										-- TIPO OPERACION : COMPRA/VENTA
					CASE WHEN DCOMP."IdFormaCustodia" = 'CUS' THEN '  '
						 WHEN LEFT(DCOMP."AccionTipoOperacion",2) = 'CO' THEN 'VE'
						 WHEN DCOMP."CodigoTipoOperacion" = 'ANSIMV' THEN 'VE' 
						 	  ELSE 'CO' END  ||									-- MOVIMIENTO : CO/VE
					CASE WHEN DCOMP."IdTipoProducto" = 'INSI' 
						 THEN RPAD( DCOMP."NemotecnicoSubyacente", 12, ' ' ) 
					     ELSE RPAD( DCOMP."NemotecnicoInstrumento", 12,' ') 
					END || 														-- INSTRUNENTO
					CASE DCOMP."IdClasificacionTipoInstrumento"
						 WHEN 'IF' THEN 'I'
						 WHEN 'RF' THEN 'M'
						 ELSE 'C' END	||										-- CONDICION DE MADUREZ
					LPAD(CAST(COALESCE(DCOMP."Cantidad", 0) AS NUMERIC(17,3)), 17, '0') || -- POSICION TRANSADA
					CASE RTRIM(DCOMP."IdMoneda")
						 WHEN 'CLP' THEN '60'
						 WHEN 'UF' THEN '61'
						 WHEN 'USD' THEN '62'
						 WHEN 'IVP' THEN '65'
						 WHEN 'EUR' THEN '-1'
						 ELSE '-0'
						 END ||													-- MONEDA
					LPAD(CAST(COALESCE(DCOMP."Monto", 0) AS NUMERIC(17,3)), 17, '0')	||	-- MONTO TRANSADO
					'R'	||														-- FORMA DE PAGO
					TO_VARCHAR(DCOMP."FechaLiquidacion", 'DDMMYYYY') ||			-- PARTIDA DE MADUREZ
					' ' ||														-- DESTINO COMPRA
					CASE LEFT(DCOMP."IdClasificacionTipoInstrumento",2) 
						 WHEN 'RV' THEN 'X'
						 ELSE ' '
						 END ||													-- DERECHOS SOBRE DIVIDENDOS
					/*CASE WHEN DCOMP."CodigoTipoOperacion" = 'ANSIMV' THEN 'E' 
						 WHEN LEFT(DCOMP."CodigoTipoOperacion",2) = 'CO' THEN 'E' 
						 ELSE 'A' END */
					'A'				 											-- ACCION
					AS "Linea"
					-------------------------------------------------------------------------------					
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"	DCOMP
					ON	Reg."FolioOperacion" = DCOMP."IdMovimientoComprobante" AND Reg."IdTipoOperacion" = DCOMP."IdTipoComprobante"
						AND DCOMP."IdEstadoComprobante" IN ('FAC','ENV')
						AND DCOMP."IdTipoProducto" IN ('INIF', 'INRF', 'INAC', 'INSI')
					LEFT JOIN :tbl_Ctas Ctas
					ON 	Reg."IdOperacionPendiente" = Ctas."IdOperacionPendiente"
			-----------------------------------------
			UNION ALL
			-----------------------------------------
			SELECT	Reg."IdRegistro",
					Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					Reg."Clave",
					GARAN."IdCliente",
					GARAN."IdPortafolio",
					'GT' AS "TipoRegistro",
					CASE WHEN GARAN."IdEstado" = 'VIG' THEN Ctas."CuentaCli_DCV"
						 ELSE v_NroCtaLiquidacion
					END  AS "CtaOrigen",
					CASE WHEN GARAN."IdEstado" = 'VIG' THEN v_NroCtaLiquidacion
						 ELSE Ctas."CuentaCli_DCV"
					END  AS "CtaDestino",
					-------------------------------------------------------------------------------
					TO_VARCHAR(GARAN."FechaIngresoRegistro", 'DDMMYYYY') ||		-- FECHA OPERACION 
					-- Por el momento no existen garantias Nominales... 
					-- falta este codigo para las cuentas + t.Ope.
					-- CASE WHEN LEFT(GARAN."IdConcepto",3) = 'ING'
					CASE WHEN GARAN."IdEstado" = 'VIG'
						 THEN RPAD( Ctas."CuentaCli_DCV", 8, ' ' )
						 ELSE RPAD( v_NroCtaLiquidacion, 8, ' ' ) END ||		-- CUENTA (CORREDOR)
					--CASE WHEN LEFT(GARAN."IdConcepto",3) = 'ING'
					CASE WHEN GARAN."IdEstado" = 'VIG'
						 THEN RPAD( v_NroCtaLiquidacion, 8, ' ' )
						 ELSE RPAD( Ctas."CuentaCli_DCV", 8, ' ' ) END ||		-- CONTRAPARTE
					RPAD(Reg."Clave", 15, ' ')	||								-- CLAVE
					-- TO_VARCHAR(GARAN."INGFechaLiquidacion", 'DDMMYYYY') ||		
					CASE WHEN GARAN."IdEstado" = 'VIG'
						 THEN TO_VARCHAR(GARAN."INGFechaLiquidacion", 'DDMMYYYY')
						 ELSE TO_VARCHAR(GARAN."EGRFechaLiquidacion", 'DDMMYYYY') END || -- FECHA LIQUIDACION
					'TC' ||														-- TIPO OPERACION : TRASPASO
					'  ' ||														-- MOVIMIENTO : '  ' 
					RPAD( CASE 
						  WHEN "NemoSVS" = '' THEN "Nemotecnico" 
						  ELSE "NemoSVS" END, 12, ' ')	||						-- NEMO SVS
					CASE GARAN."IdClasificacionTipoInstrumento"
						 WHEN 'IF' THEN 'I'
						 WHEN 'RF' THEN 'M'
						 ELSE 'C'
					END	||														-- CONDICION DE MADUREZ
--					LPAD(CAST(GARAN."Cantidad" AS NUMERIC(17,3)), 17, '0') || 	-- POSICION TRANSADA
					LPAD(CAST(SUM(GARAN."Cantidad") AS NUMERIC(17,3)), 17, '0') || 	-- POSICION TRANSADA
					CASE RTRIM(GARAN."IdMonedaInstrumento")
						 WHEN 'CLP' THEN '60'
						 WHEN 'UF' THEN '61'
						 WHEN 'USD' THEN '62'
						 WHEN 'IVP' THEN '65'
						 WHEN 'EUR' THEN '-1'
						 ELSE '-0'
						 END ||													-- MONEDA
--					LPAD(CAST(GARAN."MontoGarantia" AS NUMERIC(17,3)), 17, '0') ||	-- MONTO TRANSADO
					LPAD(CAST(SUM(GARAN."MontoGarantia") AS NUMERIC(17,3)), 17, '0') ||	-- MONTO TRANSADO
					'R'	||														-- FORMA DE PAGO
					'        ' ||												-- PARTIDAD DE MADUREZ
					' ' ||														-- DESTINO COMPRA
					' ' ||														-- DERECHOS SOBRE DIVIDENDOS
					'A' 														-- ACCION
					AS "Linea"
					-------------------------------------------------------------------------------					
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" GARAN				
					ON	Reg."FolioOperacion" = GARAN."FolioComprobante"--GARAN."FolioGarantia" --AND Reg."IdTipoOperacion" = 'INGGTA'
						AND GARAN."IdEstado" IN ('VIG', 'LIQ')
					LEFT JOIN :tbl_Ctas Ctas
					ON 	Reg."IdOperacionPendiente" = Ctas."IdOperacionPendiente"
			GROUP BY 
				Reg."IdRegistro",
				Reg."IdOperacionPendiente", 
				Reg."IdTipoOperacion", 
				Reg."FolioOperacion",
				Reg."Clave",
				GARAN."IdCliente",
				GARAN."IdPortafolio",
				GARAN."IdEstado",
				GARAN."FechaIngresoRegistro",
				Ctas."CuentaCli_DCV",
				Reg."Clave",
				GARAN."INGFechaLiquidacion",
				GARAN."EGRFechaLiquidacion",
				"NemoSVS",
				"Nemotecnico",
				GARAN."IdClasificacionTipoInstrumento",
				GARAN."IdMonedaInstrumento"			
			-----------------------------------------
			UNION ALL
			-----------------------------------------
			SELECT	Reg."IdRegistro",
					Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					Reg."Clave",
					OPER."IdCliente",
					OPER."IdPortafolio",
					'MA' AS "TipoRegistro",		
					CASE WHEN OPER."AfectaCustodia" <> 'S' THEN v_NroCtaPropia
						 ELSE CASE WHEN OPER."IdTipoOperacion" = 'VENTER' THEN v_NroCtaPropia
						 	  	   ELSE Ctas."CuentaCli_DCV"
						 	  END 
					END  AS "CtaOrigen",
					CASE WHEN OPER."AfectaCustodia" <> 'S' THEN Ctas."Socio_DCV"
						 ELSE CASE WHEN OPER."IdTipoOperacion" = 'VENTER' THEN Ctas."CuentaCli_DCV"
						 	  	   ELSE v_NroCtaPropia
						 	  END 
					END  AS "CtaDestino",										
					-------------------------------------------------------------------------------
					TO_VARCHAR(OPER."FechaCreacion", 'DDMMYYYY') ||				-- FECHA OPERACION 
					-- Sin certificado 
					CASE WHEN OPER."AfectaCustodia" <> 'S' 
						 THEN RPAD( v_NroCtaPropia, 8,' ')						 
						 ELSE CASE WHEN OPER."IdTipoOperacion" = 'VENTER'
						 	  THEN RPAD( v_NroCtaPropia, 8,' ')
						 	  ELSE RPAD( Ctas."CuentaCli_DCV", 8, ' ' )
						 	  END 
						 END ||		    										-- CUENTA (CORREDOR)
					CASE WHEN OPER."AfectaCustodia" <> 'S' 
						 THEN RPAD( Ctas."Socio_DCV", 8,' ')						 
						 ELSE CASE WHEN OPER."IdTipoOperacion" = 'VENTER'
						 	  THEN RPAD( Ctas."CuentaCli_DCV", 8,' ')
						 	  ELSE RPAD( v_NroCtaPropia, 8, ' ' )
						 	  END 
						 END ||		    										-- CONTRAPARTE
					RPAD(Reg."Clave", 15, ' ')	||								-- CLAVE
					TO_VARCHAR(OPER."FechaCondicionLiquidacion", 'DDMMYYYY') ||	-- FECHA LIQUIDACION
					CASE WHEN OPER."AfectaCustodia" = 'S' 
						 THEN 'TC'
						 ELSE 'CV' 
						 END ||													-- TIPO OPERACION 
					CASE WHEN OPER."AfectaCustodia" = 'S' 
						 THEN '  '
						 ELSE CASE OPER."IdTipoOperacion" 
						 	  WHEN 'COMTER' THEN 'CO'	 
						 	  WHEN 'VENTER' THEN 'VE'
						 	  ELSE '  ' END 
						 END ||													-- MOVIMIENTO : '  ' 
					RPAD( CASE WHEN COALESCE(CAR."NemoSVS", '') > '' 
							   THEN CAR."NemoSVS"
						  	   ELSE CAR."Nemotecnico" 
						  END, 12, ' ') ||-- INSTRUNENTO
					CASE DET."IdClasificacionTipoInstrumento"
						 WHEN 'IF' THEN 'I'
						 WHEN 'RF' THEN 'M'
						 ELSE 'C'
					END	||														-- CONDICION DE MADUREZ
					LPAD(CAST(DET."IngCantidad" AS NUMERIC(17,3)), 17, '0') || 	-- POSICION TRANSADA
					CASE RTRIM(DET."IdTipoReajuste")
						 WHEN 'CLP' THEN '60'
						 WHEN 'UF' THEN '61'
						 WHEN 'USD' THEN '62'
						 WHEN 'IVP' THEN '65'
						 WHEN 'EUR' THEN '-1'
						 ELSE '-0'
						 END ||													-- MONEDA
					LPAD(CAST(DET."MontoValorizado" AS NUMERIC(17,3)), 17, '0') ||	-- MONTO TRANSADO
					'R'	||														-- FORMA DE PAGO
					'        ' ||												-- PARTIDAD DE MADUREZ
					' ' ||														-- DESTINO COMPRA
					' ' ||														-- DERECHOS SOBRE DIVIDENDOS
					/*CASE OPER."IdTipoOperacion" 
						 WHEN 'COMTER' THEN 'A'
						 WHEN 'VENTER' THEN 'E'
						 ELSE ' ' END 											*/ 
					'A' 														-- ACCION
					AS "Linea"
					-------------------------------------------------------------------------------					
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Mesa.BD::MES.Operacion" OPER
					ON	Reg."FolioOperacion" = OPER."FolioOperacion" AND Reg."IdTipoOperacion" = OPER."IdTipoOperacion"
						AND OPER."IdEstado" = 'VIG'
					LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" DET
					ON 	OPER."IdOperacion" = DET."IdOperacion"
					LEFT JOIN :tbl_Nemos_MA CAR
					ON CAR."FolioCartera" = DET."FolioCarteraCustodia"
					LEFT JOIN :tbl_Ctas Ctas
					ON 	Reg."IdOperacionPendiente" = Ctas."IdOperacionPendiente";
/*			-----------------------------------------
			UNION ALL 
			-----------------------------------------
			SELECT	Reg."IdRegistro",
					Reg."IdOperacionPendiente", 
					Reg."IdTipoOperacion", 
					Reg."FolioOperacion",
					'ENV' AS "Estado",
					'' AS "GlosaEstado",					
					-------------------------------------------------------------------------------
					TO_VARCHAR(Trx."FechaSistemaCreacion", 'DDMMYYYY') ||		-- FECHA OPERACION
					RPAD( v_NroCtaLiquidacion, 8, ' ' ) ||						-- CUENTA (CORREDOR)
					RPAD( Ctas."CuentaCli_DCV", 8, ' ' ) ||						-- CONTRAPARTE
					RPAD(Reg."Clave", 15, ' ')	||								-- CLAVE
					TO_VARCHAR(Trx."FechaVencimiento", 'DDMMYYYY') ||			-- FECHA LIQUIDACION
					'TD' ||														-- TIPO OPERACION : Transferencia
					LEFT(Trx."IdTipoOperacion",2)  ||							-- MOVIMIENTO : CO/VE
					RPAD( Trx."Instrumento", 12, ' ' ) || 						-- INSTRUNENTO
					CASE Trx."CodigoClasificacionTipoInstrumento"
						 WHEN 'IF' THEN 'I'
						 WHEN 'RF' THEN 'M'
						 ELSE 'C'
					END	||														-- CONDICION DE MADUREZ
					LPAD(CAST(COALESCE(Trx."CantidadTransaccion", 0) AS NUMERIC(17,3)), 17, '0') || -- POSICION TRANSADA
					CASE RTRIM(Trx."IdMonedaInstrumento")
						 WHEN 'CLP' THEN '60'
						 WHEN 'UF' THEN '61'
						 WHEN 'USD' THEN '62'
						 WHEN 'IVP' THEN '65'
						 WHEN 'EUR' THEN '-1'
						 ELSE '-0'
						 END ||													-- MONEDA
					LPAD(CAST(COALESCE(Trx."MontoTransaccion", 0) AS NUMERIC(17,3)), 17, '0')	||	-- MONTO TRANSADO
					'R'	||														-- FORMA DE PAGO
					TO_VARCHAR(Trx."FechaVencimiento", 'DDMMYYYY') ||			-- PARTIDA DE MADUREZ
					' ' ||														-- DESTINO COMPRA
					CASE LEFT(Trx."CodigoClasificacionTipoInstrumento",2) 
						 WHEN 'RV' THEN 'X'
						 ELSE ' '
						 END ||													-- DERECHOS SOBRE DIVIDENDOS
					CASE WHEN LEFT(Trx."IdTipoOperacion",2) = 'CO' THEN 'E' 
						 ELSE 'A' END  											-- ACCION
					AS "Linea"
					-------------------------------------------------------------------------------					
			FROM 	:tbl_Registros Reg
					INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS Trx
					ON Trx."IdTransaccion" = Reg."FolioOperacion"
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" T_Ope
					ON Trx."IdTipoProducto" = T_Ope."IdTipoProducto" AND Trx."IdTipoOperacion" = T_Ope."IdTipoOperacion"				
					LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" AS CNT
					ON CNT."IdCorredora" = Trx."IdCorredorContraparte"
					LEFT JOIN :tbl_Ctas Ctas
					ON 	Reg."IdOperacionPendiente" = Ctas."IdOperacionPendiente"; */


		--------------------------------------------------------------
		-- ACTUALIZA EN LA TABLA DE OPERACIONES EL ESTADO COMO ENVIADO
		--------------------------------------------------------------
		tbl_Resultado =
			SELECT	TMP.*,
					CASE WHEN "TipoRegistro" = 'FD' THEN
						 	CASE WHEN RTRIM("Clave") = '' THEN 'ERR'
						 		 WHEN RTRIM("CtaOrigen") = '' THEN 'ERR'
						 		 WHEN RTRIM("CtaDestino") = '' THEN 'ERR'
						 		 ELSE 'ENV'
						 	END
						 ELSE
						 	CASE WHEN RTRIM("CtaOrigen") = '' OR 
						 		 	  RTRIM("CtaDestino") = '' THEN 'ERR'
						 		 ELSE 'ENV'
						 	END
					END AS "Estado",
					CASE WHEN "TipoRegistro" = 'FD' THEN
						 	CASE WHEN RTRIM("Clave") = '' THEN 'REG_SIN_CLAVE'
						 		 WHEN RTRIM("CtaOrigen") = '' THEN 'CTA_ORIGEN_NO_DEFINIDA'
						 		 WHEN RTRIM("CtaDestino") = '' THEN 'CTA_DESTINO_NO_DEFINIDA'
						 		 ELSE 'REG_ENVIADO'
						 	END
						 ELSE
						 	CASE WHEN RTRIM("CtaOrigen") = '' THEN 'CTA_ORIGEN_NO_DEFINIDA'
						 		 WHEN RTRIM("CtaDestino") = '' THEN 'CTA_DESTINO_NO_DEFINIDA'
						 		 ELSE 'REG_ENVIADO'
						 	END
					END AS "Glosa"
			FROM	:tbl_Tmp TMP;


		UPDATE	OpPendientes
		SET		OpPendientes."IdEstado" = TMP."Estado"
		FROM	"BSCL.Custodia.BD::CUS.OperacionesPendientes" OpPendientes,
				:tbl_Resultado TMP
		WHERE	OpPendientes."IdOperacionPendiente" = TMP."IdOperacionPendiente"
				AND OpPendientes."IdTipoOperacion" = TMP."IdTipoOperacion" 
				AND OpPendientes."FolioOperacion" = TMP."FolioOperacion";


		--------------------------------------------------------------
		-- RESULTADO FINAL A LA APLICACION
		--------------------------------------------------------------
		p_out_Data	=
			SELECT	LEFT("Linea", 130) AS "Linea"
					,"Estado"
			FROM	:tbl_Resultado
			WHERE	"Estado" = 'ENV'; 

		DROP TABLE #TmpCorredoras;

/*	
SELECT 'tbl_Resultado' AS "TABLA", * FROM :tbl_Resultado;
SELECT 'tbl_Ctas_TMP' AS "TABLA", * FROM :tbl_Ctas_TMP;
SELECT 'tbl_Ctas' AS "TABLA", * FROM :tbl_Ctas;
SELECT 'tbl_Socio' AS "TABLA", * FROM :tbl_Socio;
*/
	
    END; --Logica
END;   

GO
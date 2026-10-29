SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearOperacionSpot";

-- 
--  Proposito: Validar datos del cliente para ingresar orden.
--  Autor: BCS
--  Fecha Creacion: 01/07/2015  
--   .CRE::
GO
CREATE PROCEDURE "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearOperacionSpot"(
	IN p_IdEmpresa		BIGINT 			default 0,
    IN p_Reproceso		NVARCHAR(1)		default 'N',
    IN p_DatosOperacion	TABLE (	 "NumeroOperacion"				  NVARCHAR(40)
								, "IdOperacionSpot"               BIGINT           
								, "IdEmpresa"                     BIGINT          
								, "IdCliente"                     BIGINT        
								, "IdPortafolio"                  BIGINT          
								, "IdInstrumento"                 BIGINT    
								, "IdTipoOperacion"               NVARCHAR(6)            
								, "FechaOperacion"                SECONDDATE            
								, "FechaLiquidacion"              SECONDDATE     
								, "IdMonedaPrincipal"             NVARCHAR(3)     
								, "IdMonedaSecundaria"            NVARCHAR(3)     
								, "MontoMonedaPrincipal"          DECIMAL(26,8)   
								, "MontoMonedaSecundaria"         DECIMAL(26,8)
								, "TipoCambioCierre"              DECIMAL(26,8)
								, "TipoCambioCierreForward"       DECIMAL(26,8)
								, "ParidadCierre"                 DECIMAL(26,8)   
								, "ParidadCierreForward"          DECIMAL(26,8)   
								, "TipoCambioCosto"               DECIMAL(26,8)   
								, "ParidadCosto"                  DECIMAL(26,8)   
								, "MontoEquivalente"              DECIMAL(26,8)   
								, "MontoRentabilidad"             DECIMAL(26,8)   
								, "MontoRentabilidadCanal"        DECIMAL(26,8)   
								, "Margen"                        DECIMAL(26,8)   
								, "Utilidad"                      DECIMAL(26,8)   
								, "PuntaCompra"                   DECIMAL(26,8)   
								, "PuntaVenta"                    DECIMAL(26,8)   
								, "SolicitudTipoCambio"           NVARCHAR(1)     
								, "IdEvento"                      BIGINT     
								, "IdMesa"                        BIGINT          
								, "IdModulo"                      NVARCHAR(3)     
								, "IdTipoDolar"                   BIGINT          
								, "TipoComprobanteFacturacion"    NVARCHAR(3)     
								, "IdComprobanteFacturacion"      BIGINT     
								, "IdComprobanteTesoreriaIngreso" BIGINT     
								, "IdComprobanteTesoreriaEgreso"  BIGINT     
								, "IdAgente"                        BIGINT          
								, "BenOur"                        NVARCHAR(3)     
								, "EquivalenteConGasto"           DECIMAL(26,8)   
								, "TipoCambioCierreConGasto"      DECIMAL(26,8)   
								, "Observacion"                   NVARCHAR(520)   
								, "CodigoComercio"                BIGINT          
								, "FolioPlanilla"                 BIGINT          
								, "FolioInterfaz"                 NVARCHAR(50)    
								, "OperacionFicticia"             NVARCHAR(1)    
								, "IdTipoCanalIngreso"            NVARCHAR(3)    
								, "Estado"                        NVARCHAR(3)     
								, "IdUsuario"                     BIGINT    
								, "IdDatatec"                     NVARCHAR(40)
								, "IdUsuarioModifica"             BIGINT         
								, "FechaModifica"                 DATE),
								
	IN p_Portafolios	TABLE(	"NumeroOperacion"		NVARCHAR(40), 
								"IdPortafolio"			BIGINT,
								"IdPortafolioReal"		BIGINT,
								"IdEstadoPortafolio"	NVARCHAR(3),
								"IdCliente"				BIGINT,
								"IdClienteReal"			BIGINT,
								"IdEstadoCliente" 		NVARCHAR(3),
								"DocumentoIdentidad" 	NVARCHAR(40),
								"IdPerfilCobros"		NVARCHAR(40),
								"IdEmpleadoPortafolio"	BIGINT),
	
	OUT p_out_SalidaDetalle	TABLE ("NumeroOperacion" NVARCHAR(40), "IdDatatec" NVARCHAR(40), "Resultado" NVARCHAR(40), "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40)),
	OUT p_out_Salida		TABLE ("NumeroOperacion" NVARCHAR(40), "Resultado" NVARCHAR(40))
	--OK=Cumple todos los requisitos
	--ADV=Advertencia
	--AUT=Autorizacion
	--ERR=Error
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	
	DECLARE v_FechaSistema	NVARCHAR(10);
	DECLARE v_out_FlujoCajaErrContador BIGINT := 0;
	DECLARE v_Error	NVARCHAR(500);
	DECLARE v_Indicadores TABLE ("IdIndicador" NVARCHAR(10), "Fecha" DATE, "TipoCambio" DECIMAL(26,8), "Paridad" DECIMAL(26,8), "FechaValor" DATE, "Fuente" NVARCHAR(10));
	DECLARE v_Habitualidad TABLE ("IdCliente" BIGINT, "IndHabitualidad" NVARCHAR(1), "CodigoRegla" NVARCHAR(40),"TipoComprobanteHabitualidad" NVARCHAR(3));	
	DECLARE v_portafoliosSaldoCaja TABLE ("IdPortafolio" BIGINT, "IdMoneda" NVARCHAR(3), "ExisteSobreGiro" NVARCHAR(1));
		
	v_portafoliosSaldoCaja = SELECT	0 AS "IdPortafolio", '' AS "IdMoneda", '' AS "ExisteSobreGiro" FROM DUMMY WHERE 1=2;
										
	v_matriz_validaciones =
		SELECT
			ope."NumeroOperacion", ope."IdOperacionSpot", ope."IdDatatec",
			1 as "DOLAR_MAYOR_CERO", 
			1 as "MESA_ACTIVA", 
			1 as "MODULO_ACTIVO", 
			CASE WHEN "IdDatatec" != '0' THEN 1 ELSE 0 END as "HORARIO_VALIDO",
			CASE WHEN :p_Reproceso = 'N' THEN 1 ELSE 0 END as "FECHA_PROCESO_VALIDA", --OK
			1 as "TIPO_COMPROBANTE_VALIDA",
			1 as "HABITUALIDAD_CLIENTE", -- ESPERA
			1 as "VALOR_DOLAR_FECHA_PROCESO", 
			CASE WHEN "IdMonedaSecundaria" <> 'CLP' THEN 1 ELSE 0 END as "PARIDAD_PRINCIPAL_FECHA_PROCESO", 
			CASE WHEN "IdMonedaSecundaria" = 'CLP' THEN 1 ELSE 0 END as "TIPO_CAMBIO_PRINCIPAL_FECHA_PROCESO", 
			1 as "BLOQUEO_CONDICIONES_GENERALES", -- PERFILAMIENTO
			1 as "OPERACION_FICTICIA_VALIDA", -- PERFILAMIENTO
			1 as "CLIENTE_EXISTE",
			1 as "CLIENTE_HABILITADO",
			1 as "PORTAFOLIO_EXISTE",
			1 as "PORTAFOLIO_HABILITADO",
			1 as "OPERADOR_INGRESA",
			1 as "VALIDA_LIMITES",
			1 as "VALIDA_OPERADOR",-- puede operar productos arbitrajes empresas o puntas
			1 as "MONEDAS_DISTINTAS",
			1 as "INGRESAR_AGENTE_VALIDA",
			1 as "SOLICITUD_PUNTA_EXISTE",
			1 as "SPREAD_MAYOR_CERO",
			1 as "MONEDA_HABILITADA_VALIDA",
			1 as "TC_PARIDAD_FUERA_LIMITE",
			1 AS "PRODUCTO_NO_HABILITADO",			
			1 AS "CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO",
			1 AS "OPERACION_SPOT_FACTURADA"
		FROM :p_DatosOperacion ope;
	
		
	
    --Obtiene fecha actual del sistema
	CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);	
	CALL  "BSCL.Mesa.BD.Servicios::ObtenerValores" ('BCS',v_FechaSistema,v_Error,v_Indicadores);
	
 	
	
 	-- Obtiene indicadores(preferencias de negocio) para los portafolios
	v_IndicadoresPortafolio =
	SELECT	
		"IdPortafolio",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "OperaLineaSobregiro"
			
	FROM 
	(
		SELECT IND."IdPortafolio", IND."CodigoTipoIndicador", IND."IdIndicador", IND."ValorIndicador"
		FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS IND	
		INNER JOIN :p_DatosOperacion AS OPE ON (IND."IdPortafolio"= OPE."IdPortafolio")
		AND IND."CodigoTipoIndicador" IN ('GEN_OCCINV','GEN_SOBCTA')
	) 
	GROUP BY "IdPortafolio"; 	
 	
 	
	
	
	v_PortafolioCliente =
	SELECT
		 OPE."IdPortafolio"
		,OPE."NumeroOperacion"
		,OPE."IdOperacionSpot"
		,OPE."IdDatatec"
		,OPE."IdTipoOperacion"
		,OPE."IdMonedaPrincipal"     
		,OPE."IdMonedaSecundaria"     
		,OPE."MontoMonedaPrincipal"   
		,OPE."MontoMonedaSecundaria"
		,POR."IdPerfilProductosExtra"
		,POR."IdPerfilProductos"
		,(CASE WHEN IND."OperaCuentaInversion" = 'S' AND IND."OperaLineaSobregiro" = 'N' THEN 'S' ELSE 'N' END) AS "ValidaSaldoCaja"
		,IND."OperaLineaSobregiro"		      
	FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
	INNER JOIN :p_DatosOperacion AS OPE ON (OPE."IdPortafolio"= POR."IdPortafolio") 		
	LEFT JOIN :v_IndicadoresPortafolio AS IND ON (IND."IdPortafolio" = OPE."IdPortafolio");
	

 		
 	
 	 	
 	v_portafoliosFlujoCajaAConsultar = 	
 	SELECT "IdPortafolio", "IdMoneda", SUM("Monto") AS "Monto"
 	FROM 
 	(
	 	SELECT
	 		 "IdPortafolio"
		 	,(CASE 
		 		WHEN ("IdTipoOperacion" = 'VENSPT') THEN  "IdMonedaSecundaria"
		 		WHEN ("IdTipoOperacion" = 'COMSPT') THEN  "IdMonedaPrincipal"
		 	 END) AS "IdMoneda"	 	
		 	,(CASE 
		 		WHEN ("IdTipoOperacion" = 'VENSPT') THEN  "MontoMonedaSecundaria"
		 		WHEN ("IdTipoOperacion" = 'COMSPT') THEN  "MontoMonedaPrincipal"
		 	 END) AS "Monto"
	 	FROM :v_PortafolioCliente
	 	WHERE "ValidaSaldoCaja" = 'S' 	
 	)
 	GROUP BY "IdPortafolio","IdMoneda";
 	
 	
 	IF (NOT IS_EMPTY(:v_portafoliosFlujoCajaAConsultar)) THEN
		-- Consulta saldo flujo caja
		CALL "BSCL.Plataforma.BD.Servicios::SaldoFlujoCaja"
		(
		     p_IdEmpresa 				=> :p_IdEmpresa
			,p_FechaDesde 				=> :v_FechaSistema
			,p_FechaHasta 				=> :v_FechaSistema
			,p_Uso 						=> 'CUENTA_INVERSION'--'MESA_RF' ---- falta cambiar(crear un uso propio para SPOT en flujo caja)
			,p_DesplegarEnUI 			=> 'N'
			,p_AgruparPorOrigen 		=> 'N'
			,p_PortafolioAConsultar		=> :v_portafoliosFlujoCajaAConsultar    
		    ,p_out_FlujoCaja			=> v_out_FlujoCaja
			,p_out_FlujoCajaDetallado	=> v_out_FlujoCajaDetallado
			,p_out_ErroresContador		=> v_out_FlujoCajaErrContador
			,p_out_ErroresDetalle		=> v_out_FlujoCajaErrDetalle
		);
		
		
	v_portafoliosSaldoCaja =	SELECT  FC."IdPortafolio", FC."IdMoneda",
										(CASE
											WHEN (PFC."Monto" > FC."Saldo") THEN 'S'
											ELSE 'N'
										END) AS "ExisteSobreGiro"
								FROM :v_portafoliosFlujoCajaAConsultar AS PFC
								LEFT JOIN :v_out_FlujoCaja AS FC ON (FC."IdPortafolio" 	= PFC."IdPortafolio" AND FC."IdMoneda" 		= PFC."IdMoneda");
	SELECT  'saldo Caja' AS "saldoCaja",FC."IdPortafolio", FC."IdMoneda",PFC."Monto",FC."Saldo" 
                                FROM :v_portafoliosFlujoCajaAConsultar AS PFC
                                LEFT JOIN :v_out_FlujoCaja AS FC ON (FC."IdPortafolio"  = PFC."IdPortafolio" AND FC."IdMoneda"      = PFC."IdMoneda");
										
 	END IF; -- IF (NOT IS_EMPTY(:v_portafoliosFlujoCaja))
 	




	--* Para traer BID y ASK a los que no tienen
	v_Indicadores = 
		SELECT	"IdIndicador", T1."Fecha", 
				CASE WHEN T1."TipoCambio" > 0 THEN T1."TipoCambio" 
					 WHEN PAR."TipoCambioMercado" > 0 THEN PAR."TipoCambioMercado"
					 ELSE 0 END
					 AS "TipoCambio", 
				CASE WHEN T1."Paridad" > 0 THEN T1."Paridad"
					 WHEN PAR."ParidadMercado" > 0 THEN PAR."ParidadMercado"
					 WHEN PAR."ParidadBID" > 0 THEN PAR."ParidadBID"
					 WHEN PAR."ParidadASK" > 0 THEN PAR."ParidadASK"
					 ELSE 0 END		 
					 AS "Paridad",
				"FechaValor", "Fuente"
		FROM	:v_Indicadores T1
				LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PAR ON
				T1."IdIndicador" = PAR."IdMoneda" AND T1."Fecha" = PAR."Fecha";

					
	v_TablaRequisitos =
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."HORARIO_VALIDO" = 0 OR ope."MontoMonedaPrincipal" > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'EXCEDE_HORARIO' as "Codigo", CAST(ope."NumeroOperacion" AS NVARCHAR(100)) as "ValorParametro", 'IdOperacionSpot' as "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."IdOperacionSpot" = val."IdOperacionSpot"
		
			UNION

		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."DOLAR_MAYOR_CERO" = 0 OR ope."MontoMonedaPrincipal" > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'DOLAR_MENOR_IGUAL_CERO' as "Codigo", CAST(ope."NumeroOperacion" AS NVARCHAR(100)) as "ValorParametro", 'IdOperacionSpot' as "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."IdOperacionSpot" = val."IdOperacionSpot"
		
			UNION

		SELECT ope."NumeroOperacion", ope."IdDatatec",
			CASE WHEN val."MESA_ACTIVA" = 0 OR Mesa."IdEstadoMesa" = 'A' THEN 1 ELSE 4 END as "IdResultado",
			'MESA_SPOT_CERRADA' 				as "Codigo",
			CAST(ope."IdMesa" AS NVARCHAR(100)) as "ValorParametro",
			'MESA'								as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot" 
		INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" Mesa 
	   		ON Mesa."IdMesa" = ope."IdMesa"
		WHERE ope."IdOperacionSpot" = val."IdOperacionSpot"
		
		UNION
		
		SELECT ope."NumeroOperacion", ope."IdDatatec",
			CASE WHEN ope."IdMesa" = 0 THEN 4 ELSE 1 END as "IdResultado",
			'NO_EXISTE_MESA_SPOT_POR_DEFECTO'	as "Codigo",
			CAST(ope."IdMesa" AS NVARCHAR(100))	as "ValorParametro",
			'MESA'								as "NombreParametro"
		FROM :p_DatosOperacion ope
		
		--* ------------------------------------------------------------------------------------------
		UNION
		--* ------------------------------------------------------------------------------------------			
		SELECT 	ope."NumeroOperacion", ope."IdDatatec",
				CASE WHEN val."MODULO_ACTIVO" = 0 OR (Modulo."IdEstado" = 'ACT' AND Modulo."TipoEstado" = 'A') THEN 1 ELSE 4 END AS "IdResultado", 
				'UI_MODULO_CERRADO' AS "Codigo", 
				CAST(ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
				'IdOperacionSpot' AS "NombreParametro"
		FROM 	:p_DatosOperacion ope
				INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot" 
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" Modulo 
	    		ON Modulo."IdModulo" = 'MSP' AND Modulo."IdEmpresa" = :p_IdEmpresa
		WHERE 	ope."IdOperacionSpot" = val."IdOperacionSpot"
		--* ------------------------------------------------------------------------------------------
		UNION
		--* ------------------------------------------------------------------------------------------			
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."FECHA_PROCESO_VALIDA" = 0 OR v_FechaSistema = TO_VARCHAR(ope."FechaOperacion",'YYYY-MM-DD') THEN 1 ELSE 4 END as "IdResultado", 
		'FECHA_PROCESO_INVALIDA' as "Codigo", TO_VARCHAR(ope."FechaOperacion",'YYYY-MM-DD') as "ValorParametro", 'IdOperacionSpot' as "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."IdOperacionSpot" = val."IdOperacionSpot"
		
			UNION
		
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."TIPO_COMPROBANTE_VALIDA" = 0 OR (("IdTipoComprobante" like '%FA%' OR "IdTipoComprobante" = 'BOE' ) AND COALESCE(tipoComprobante."IdEstadoFacturacion",'') != '') THEN 1 ELSE 4 END as "IdResultado", 
		'TIPO_COMPROBANTE_INVALIDO' as "Codigo", CAST(ope."NumeroOperacion" AS NVARCHAR(100)) as "ValorParametro", 'IdOperacionSpot' as "NombreParametro"
		FROM :p_DatosOperacion ope 
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot" 
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" tipoComprobante
		ON tipoComprobante."IdTipoComprobante" = ope."TipoComprobanteFacturacion"
		AND tipoComprobante."IdEstadoFacturacion" = 'ACT'
		
			UNION
		
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."VALOR_DOLAR_FECHA_PROCESO" = 0 OR Indicadores."TipoCambio" > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'VALOR_DOLAR_ACTUAL_IGUAL_CERO' as "Codigo", CAST(ope."NumeroOperacion" AS NVARCHAR(100)) as "ValorParametro", 'IdOperacionSpot' as "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		LEFT JOIN :v_Indicadores Indicadores on Indicadores."IdIndicador" = 'USD'
		WHERE ope."IdOperacionSpot" = val."IdOperacionSpot" AND ope."IdMonedaPrincipal" <> 'USD'
		
			UNION

--"PARIDAD_SECUNDARIA_FECHA_PROCESO"		
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."TIPO_CAMBIO_PRINCIPAL_FECHA_PROCESO" = 0 OR Indicadores."TipoCambio" > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'TIPO_CAMBIO_PRINCIPAL_IGUAL_CERO' as "Codigo", CAST(ope."NumeroOperacion" AS NVARCHAR(100)) as "ValorParametro", 'IdOperacionSpot' as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot" 
		LEFT JOIN :v_Indicadores Indicadores on ope."IdMonedaPrincipal" = Indicadores."IdIndicador"
		
			UNION
-- NUEVO
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."PARIDAD_PRINCIPAL_FECHA_PROCESO" = 0 OR Indicadores."Paridad" > 0 THEN 1 ELSE 4 END as "IdResultado", 
		'PARIDAD_PRINCIPAL_IGUAL_CERO' as "Codigo", CAST(ope."NumeroOperacion" AS NVARCHAR(100)) as "ValorParametro", 'IdOperacionSpot' as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot" 
		LEFT JOIN :v_Indicadores Indicadores on ope."IdMonedaPrincipal" = Indicadores."IdIndicador"
		
			UNION
	
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."CLIENTE_EXISTE" = 0 OR COALESCE(cliente."IdCliente",-1) > 0 THEN 1 ELSE 4 END as "IdResultado", 
		      'CLIENTE_NO_EXISTE' as "Codigo", CAST(ope."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 'IdCliente' as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cliente on cliente."IdCliente" = ope."IdCliente"
		
			UNION

		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."CLIENTE_HABILITADO" = 0 OR (COALESCE(cliente."IdEstado",'BLO') = 'ACT' AND COALESCE(cliente."IdCliente",-1) > 0 )THEN 1 ELSE 4 END as "IdResultado",
		CASE
			WHEN cliente."IdEstado" = 'BLO' THEN 'CLIENTE_BLOQUEADO'
			WHEN cliente."IdEstado" = 'PEN' THEN 'CLIENTE_PENDIENTE_AUTORIZACION'
			ELSE 'CLIENTE_NO_HABILITADO' END as "Codigo",
		CAST(ope."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 'IdCliente' as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cliente on cliente."IdCliente" = ope."IdCliente"
		
			UNION
		
		SELECT
			ope."NumeroOperacion", ope."IdDatatec",
			CASE WHEN val."PORTAFOLIO_EXISTE" = 0 OR COALESCE(POR."IdPortafolio",-1) > 0 THEN 1 ELSE 4 END as "IdResultado",
			'PORTAFOLIO_NO_EXISTE' as "Codigo",
			CAST(ope."IdPortafolio" AS NVARCHAR(100)) as "ValorParametro",
			'IdPortafolio' as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON POR."IdPortafolio" = ope."IdPortafolio"
		
			UNION
		
		SELECT
			ope."NumeroOperacion", ope."IdDatatec",
			CASE
				WHEN val."PORTAFOLIO_HABILITADO" = 0 OR (COALESCE(POR."IdEstado",'BLO') = 'ACT' AND COALESCE(POR."IdPortafolio",-1) > 0 )THEN 1 ELSE 4 END as "IdResultado",
			CASE
				WHEN POR."IdEstado" = 'BLO' THEN 'PORTAFOLIO_BLOQUEADO'
				WHEN POR."IdEstado" = 'PEN' THEN 'PORTAFOLIO_PENDIENTE_AUTORIZACION'
				ELSE 'PORTAFOLIO_NO_HABILITADO' END as "Codigo",
			CAST(ope."IdPortafolio" AS NVARCHAR(100)) as "ValorParametro",
			'IdPortafolio' as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON POR."IdPortafolio" = ope."IdPortafolio"
		
		    UNION
		
		SELECT 
			ope."NumeroOperacion", 
			ope."IdDatatec",
			CASE WHEN val."MONEDA_HABILITADA_VALIDA" = 0 OR COALESCE(CuentaInversion."IdEstado",'INA') = 'ACT' THEN 1 ELSE 4 END as "IdResultado",			
		    'MONEDA_NO_HABILITADA_CUENTA_INVERSION' as "Codigo",
		    CAST(ope."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 
		    'IdCliente' as "NombreParametro"
		    
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"
		INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" CuentaInversion 
			  ON CuentaInversion."IdCliente" = ope."IdCliente"
	          AND CuentaInversion."IdPortafolio" = ope."IdPortafolio"
	          AND CuentaInversion."IdMoneda" = ope."IdMonedaPrincipal"

	    UNION
		
		SELECT 
			ope."NumeroOperacion", 
			ope."IdDatatec",
			CASE WHEN val."MONEDA_HABILITADA_VALIDA" = 0 OR COALESCE(CuentaInversion."IdEstado",'INA') = 'ACT' THEN 1 ELSE 4 END as "IdResultado",			
		    'MONEDA_NO_HABILITADA_CUENTA_INVERSION' as "Codigo",
		    CAST(ope."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 
		    'IdCliente' as "NombreParametro"
		    
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"
		INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarMonedaClienteCuentaInversion" CuentaInversion 
			  ON CuentaInversion."IdCliente" = ope."IdCliente"
	          AND CuentaInversion."IdPortafolio" = ope."IdPortafolio"
	          AND CuentaInversion."IdMoneda" = ope."IdMonedaSecundaria"	          
	    
		UNION
		
		SELECT 
			ope."NumeroOperacion", 
			ope."IdDatatec",
			CASE WHEN val."CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO" = 0 OR COALESCE("ExisteSobreGiro",'N') = 'N' THEN 1 ELSE 4 END as "IdResultado",
		    'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO' as "Codigo",
			CAST(ope."IdPortafolio" AS NVARCHAR(100)) as "ValorParametro",
			'IdPortafolio' as "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"			
		INNER JOIN :v_portafoliosSaldoCaja AS PSC
			ON (psc."IdPortafolio" = ope."IdPortafolio" AND	
				psc."IdMoneda" = (CASE WHEN ("IdTipoOperacion" = 'VENSPT') THEN "IdMonedaSecundaria" 
									   WHEN ("IdTipoOperacion" = 'COMSPT') THEN "IdMonedaPrincipal" END))
		
		UNION
		
		/*
		SELECT ope."NumeroOperacion", ope."IdDatatec",
		CASE WHEN val."SPREAD_MAYOR_CERO" = 0 OR (ope."MontoMonedaSecundaria" - ope."MontoMonedaPrincipal" > 0 )THEN 1 ELSE 4 END as "IdResultado", 
		      'SPREAD_MENOR_IGUAL_CERO' as "Codigo", CAST(ope."IdCliente" AS NVARCHAR(100)) as "ValorParametro", 'IdCliente' as "NombreParametro"
		FROM :p_DatosOperacion ope, :v_matriz_validaciones val
		WHERE ope."IdOperacionSpot" = val."IdOperacionSpot"
		*/
		
		--********** PRODUCTO_NO_HABILITADO **********
		SELECT
			 por."NumeroOperacion", por."IdDatatec"
			,CASE 
				WHEN val."PRODUCTO_NO_HABILITADO" = 0 THEN 1 
				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'ACT' AND 
					 IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'ACT' AND 
					(IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '' OR 
					 IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = por."IdTipoOperacion" OR 
					 IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") = '0') THEN 1
			 	ELSE 4 END									AS "IdResultado"
  			,CASE
  				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'INA'	THEN 'PERFIL_PRODUCTO_INACTIVO'
  				WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'INA'				THEN 'PRODUCTO_INACTIVO'
  				WHEN IFNULL(perfExtra."EstadoPerfilProductos",perfBase."EstadoPerfilProductos") = 'BLO' THEN 'PERFIL_PRODUCTO_BLOQUEADO'
  				WHEN IFNULL(perfExtra."EstadoProducto",perfBase."EstadoProducto") = 'BLO'				THEN 'PRODUCTO_BLOQUEADO'
  				WHEN IFNULL(perfExtra."IdTipoOperacion",perfBase."IdTipoOperacion") NOT IN ('','0',por."IdTipoOperacion") THEN 'TIPO_OPERACION_SIN_VALOR'
  				ELSE 'ERROR_PRODUCTO' END						AS "Codigo"
  			,CAST(por."NumeroOperacion" AS NVARCHAR(100))	AS "ValorParametro"
  			,'IdOperacionSpot'								AS "NombreParametro"
		FROM :v_PortafolioCliente por
		INNER JOIN :v_matriz_validaciones val
			ON por."IdOperacionSpot" = val."IdOperacionSpot"
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfExtra
			ON (por."IdPerfilProductosExtra" = perfExtra."IdPerfilProductos" AND 
				perfExtra."IdTipoProducto" = 'MEME' AND 
				perfExtra."IdEmpresa" = :p_IdEmpresa AND 
				(perfExtra."IdTipoOperacion" = por."IdTipoOperacion" OR perfExtra."IdTipoOperacion" = '0'))
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" perfBase
			ON (por."IdPerfilProductos" = perfBase."IdPerfilProductos" AND 
				perfBase."IdTipoProducto" = 'MEME' AND 
				perfBase."IdEmpresa" = :p_IdEmpresa AND 
				(perfBase."IdTipoOperacion" = por."IdTipoOperacion" OR perfBase."IdTipoOperacion" = '0'))
		
		UNION
		
		/*SELECT
			ope."NumeroOperacion",
			ope."IdDatatec",
			CASE WHEN Val."OPERACION_SPOT_FACTURADA" = 0 OR
					  SPOT."IdComprobanteFacturacion" = 0 OR
					  MOVFAC."IdEstadoFacturacion" <> 'FAC' OR 
					  (MOVFAC."IdEstadoFacturacion" = 'FAC' AND MOVNCR."IdComprobanteReferencia" IS NOT NULL) THEN 1 
				 ELSE 4 END AS "IdResultado",
			'OPERACION_SPOT_FACTURADA' AS "Codigo", 
			CAST(ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
			'IdOperacionSpot' AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones Val
			ON ope."IdOperacionSpot" = Val."IdOperacionSpot"
		INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
			ON ope."IdOperacionSpot" = SPOT."IdOperacionSpot"
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVFAC
			ON SPOT."IdComprobanteFacturacion" = MOVFAC."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVNCR
			ON (MOVFAC."IdComprobante" > 0 AND
				MOVFAC."IdComprobante" = MOVNCR."IdComprobanteReferencia" AND
				MOVNCR."IdTipoComprobante" IN ('NCE','NCR'))
		WHERE SPOT."Estado" = 'VIG'*/
		SELECT
			ope."NumeroOperacion",
			ope."IdDatatec",
			CASE WHEN Val."OPERACION_SPOT_FACTURADA" = 0 OR SPTFAC."PuedeModificarAnular" = 1 THEN 1 ELSE 4 END AS "IdResultado", 
			SPTFAC."CodigoModificarAnular" AS "Codigo", 
			CAST(ope."NumeroOperacion" AS NVARCHAR(100)) AS "ValorParametro", 
			'IdOperacionSpot' AS "NombreParametro"
		FROM :p_DatosOperacion ope
		INNER JOIN :v_matriz_validaciones Val
			ON ope."IdOperacionSpot" = Val."IdOperacionSpot"
		INNER JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarSpotFacturado" SPTFAC
			ON (ope."IdOperacionSpot" = SPTFAC."IdOperacionSpot" AND
				SPTFAC."IdEmpresa" = :p_IdEmpresa)
		WHERE SPTFAC."Estado" = 'VIG'
		;
	
	
	--* Paridad/TC Fuera de Limite
	v_Val_Limites = 
		SELECT	OPE."NumeroOperacion",
				OPE."IdInstrumento",
				OPE."IdMonedaPrincipal",
				OPE."IdDatatec",
				OPE."IdMonedaSecundaria",
				OPE."TipoCambioCierre"					AS "TCCierre",
				OPE."TipoCambioCierreForward"			AS "TCCierreForward",
				OPE."ParidadCierre",
				OPE."ParidadCierreForward",
				OPE."TipoCambioCosto"					AS "TCCosto",
				OPE."ParidadCosto",
				COALESCE(PYT."IdParidadYTipoCambio",0)											AS "IdParidadYTipoCambio",
				COALESCE(PYT."ParidadMercado",0)												AS "ParidadMercado",
				COALESCE(PYT."ParidadBID",0)													AS "ParidadBID",
				COALESCE(PYT."ParidadASK",0)													AS "ParidadASK",
				COALESCE(PYT."LimiteSuperiorParidad",0)											AS "LimiteSuperiorParidad",
				COALESCE(PYT."LimiteInferiorParidad",0)											AS "LimiteInferiorParidad",
				(COALESCE(PYT."ParidadMercado",0) + COALESCE(PYT."LimiteSuperiorParidad",0)) 	AS "LimiteSuperiorPAR",
				(COALESCE(PYT."ParidadMercado",0) - COALESCE(PYT."LimiteInferiorParidad",0)) 	AS "LimiteInferiorPAR",
				COALESCE(PYT."TipoCambioMercado",0)													AS "TipoCambioMercado",
				COALESCE(PYT."LimiteSuperiorTipoCambio",0)											AS "LimiteSuperiorTipoCambio",
				COALESCE(PYT."LimiteInferiorTipoCambio",0)											AS "LimiteInferiorTipoCambio",
				(COALESCE(PYT."TipoCambioMercado",0) + COALESCE(PYT."LimiteSuperiorTipoCambio",0)) 	AS "LimiteSuperiorTC",
				(COALESCE(PYT."TipoCambioMercado",0) - COALESCE(PYT."LimiteInferiorTipoCambio",0)) 	AS "LimiteInferiorTC"
		FROM :p_DatosOperacion OPE 
		LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PYT 
			ON (PYT."IdMoneda" = OPE."IdMonedaPrincipal" AND
				PYT."Fecha" = TO_VARCHAR(OPE."FechaOperacion",'YYYY-MM-DD'))
		--WHERE PYT."Fecha" BETWEEN TO_SECONDDATE(v_FechaSistema) AND ADD_SECONDS(TO_SECONDDATE(v_FechaSistema),86399)
		;


	--* OPERACIONES MX/CLP
	v_Limites_TC = 
		SELECT
			PARTC."NumeroOperacion",
			PARTC."IdDatatec",
			CASE WHEN MAT."TC_PARIDAD_FUERA_LIMITE" = 0 THEN 1 
				 WHEN PARTC."TCCierre" > PARTC."LimiteSuperiorTC" OR PARTC."TCCierre" < "LimiteInferiorTC" THEN 3
				 ELSE 1 END							AS "IdResultado",
			'TC_FUERA_LIMITES'						AS "Codigo",
			CAST(PARTC."TCCierre" AS NVARCHAR(100))	AS "ValorParametro",
			'TCCierre'								AS "NombreParametro"
		FROM :v_Val_Limites PARTC
		LEFT JOIN :v_matriz_validaciones MAT 
			ON PARTC."NumeroOperacion" = MAT."NumeroOperacion"
		WHERE PARTC."IdMonedaSecundaria" = 'CLP'
		UNION ALL
		--Validacion TCCosto
		SELECT
			PARTC."NumeroOperacion",
			PARTC."IdDatatec",
			CASE WHEN MAT."TC_PARIDAD_FUERA_LIMITE" = 0 THEN 1 
				 WHEN PARTC."TCCosto" > PARTC."LimiteSuperiorTC" OR PARTC."TCCosto" < "LimiteInferiorTC" THEN 3
				 ELSE 1 END							AS "IdResultado",
			'TC_COSTO_FUERA_LIMITES'				AS "Codigo",
			CAST(PARTC."TCCosto" AS NVARCHAR(100))	AS "ValorParametro",
			'TCCosto'								AS "NombreParametro"
		FROM :v_Val_Limites PARTC
		LEFT JOIN :v_matriz_validaciones MAT 
			ON PARTC."NumeroOperacion" = MAT."NumeroOperacion"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" Instrumento
		 	ON PARTC."IdInstrumento" = Instrumento."IdInstrumento"
		WHERE PARTC."IdMonedaSecundaria" = 'CLP'
		  AND Instrumento."Nemotecnico" = 'DISTRI';
			

	--* OPERACIONES MX/USD
	v_Limites_Par = 
		SELECT
			PARTC."NumeroOperacion",
			PARTC."IdDatatec",
			CASE WHEN MAT."TC_PARIDAD_FUERA_LIMITE" = 0 THEN 1 
				 WHEN PARTC."ParidadCierre" > PARTC."LimiteSuperiorPAR" OR PARTC."ParidadCierre" < "LimiteInferiorPAR" THEN 3
				 ELSE 1 END									AS "IdResultado",
			'PARIDAD_FUERA_LIMITES'							AS "Codigo",
			CAST(PARTC."ParidadCierre" AS NVARCHAR(100))	AS "ValorParametro",
			'ParidadCierre'									AS "NombreParametro"
		FROM :v_Val_Limites PARTC
		LEFT JOIN :v_matriz_validaciones MAT
			ON PARTC."NumeroOperacion" = MAT."NumeroOperacion"
		WHERE PARTC."IdMonedaSecundaria" <> 'CLP'
		UNION ALL
		--Validacion ParidadCosto
		SELECT
			PARTC."NumeroOperacion",
			PARTC."IdDatatec",
			CASE WHEN MAT."TC_PARIDAD_FUERA_LIMITE" = 0 THEN 1 
				 WHEN PARTC."ParidadCosto" > PARTC."LimiteSuperiorPAR" OR PARTC."ParidadCosto" < "LimiteInferiorPAR" THEN 3
				 ELSE 1 END									AS "IdResultado",
			'PARIDAD_COSTO_FUERA_LIMITES'					AS "Codigo",
			CAST(PARTC."ParidadCosto" AS NVARCHAR(100))		AS "ValorParametro",
			'ParidadCosto'									AS "NombreParametro"
		FROM :v_Val_Limites PARTC
		LEFT JOIN :v_matriz_validaciones MAT
			ON PARTC."NumeroOperacion" = MAT."NumeroOperacion"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" Instrumento
		 	ON PARTC."IdInstrumento" = Instrumento."IdInstrumento"
		WHERE PARTC."IdMonedaSecundaria" <> 'CLP'
		  AND Instrumento."Nemotecnico" = 'ARBDIS';
	

	v_TablaRequisitos = 
		SELECT	*
		FROM	:v_TablaRequisitos
		--*------
		UNION
		--*------
		SELECT	*
		FROM	:v_Limites_TC	
		--*------
		UNION
		--*------
		SELECT	*
		FROM	:v_Limites_Par;
		
		SELECT 'v_portafoliosSaldoCaja' AS "v_portafoliosSaldoCaja",* FROM :v_portafoliosSaldoCaja;
		SELECT "ExisteSobreGiro",
                ope."NumeroOperacion", 
                ope."IdDatatec",
                CASE WHEN val."CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO" = 0 OR COALESCE("ExisteSobreGiro",'N') = 'N' THEN 1 ELSE 4 END as "IdResultado",
                'CLIENTE_NO_OPERA_CON_LINEA_SOBREGIRO' as "Codigo",
                CAST(ope."IdPortafolio" AS NVARCHAR(100)) as "ValorParametro",
                'IdPortafolio' as "NombreParametro"
            FROM :p_DatosOperacion ope
            INNER JOIN :v_matriz_validaciones val on ope."IdOperacionSpot" = val."IdOperacionSpot"          
            INNER JOIN :v_portafoliosSaldoCaja AS PSC ON 
            (
                    psc."IdPortafolio" = ope."IdPortafolio" 
                AND psc."IdMoneda" = (CASE WHEN ("IdTipoOperacion" = 'VENSPT') THEN  "IdMonedaSecundaria" WHEN ("IdTipoOperacion" = 'COMSPT') THEN  "IdMonedaPrincipal" END)
            );
        
	
	p_out_SalidaDetalle =	SELECT 
								tb."NumeroOperacion", 
								tb."IdDatatec",
								CASE "IdResultado"
									WHEN 1 THEN 'OK'
									WHEN 2 THEN 'ADV'
									WHEN 3 THEN 'AUT'
									WHEN 4 THEN 'ERR'
								END as "Resultado",
								tb."Codigo" ,
								tb."ValorParametro" ,
								tb."NombreParametro" 
							FROM :v_TablaRequisitos tb
							WHERE "IdResultado" != 1
							ORDER BY "NumeroOperacion";
	
	p_out_Salida = 	SELECT "NumeroOperacion",CASE MAX("IdResultado")
									WHEN 1 THEN 'OK'
									WHEN 2 THEN 'ADV'
									WHEN 3 THEN 'AUT'
									WHEN 4 THEN 'ERR'
								END AS "Resultado"
								
					FROM :v_TablaRequisitos 
					GROUP BY "NumeroOperacion"
					ORDER BY "NumeroOperacion";
	
END;
GO
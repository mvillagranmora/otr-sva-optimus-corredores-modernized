SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" AS

SELECT 
		CASE WHEN OS."IdTipoOperacion" = 'COMSPT' THEN 'CO' ELSE 'VE' END AS TIPO_OPER
		,CASE 
			WHEN COALESCE(TES."IdEstado",'') = 'LIQ' THEN 'L' 
			WHEN OS."Estado" = 'ANU' THEN 'N' 
			WHEN OS."Estado" = 'VIG' THEN 'V'
			WHEN OS."Estado" = 'PEN' THEN 'P'
			WHEN OS."Estado" = 'MOD' THEN 'M'
		END AS ESTADO
		
		, TO_NVARCHAR(OS."FechaOperacion", 'YYYY-MM-dd') AS FEC_MVTO
		, OS."IdOperacionSpot" AS SEC_MOVTO
		, CE."DocumentoIdentidad" AS RUT_CORR
		, 1 AS SEC_RUT_CORR
        ,"BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CC."DocumentoIdentidad")  AS "RUT_CLI"
		, COALESCE (CP."CodigoPortafolio" ,'-1')  AS "SEC_RUT_CLI" -- DECIMAL(2,0)
		, left(coalesce(CEMP."CodigoEmpleado",''),4) AS COD_AGENTE
		, '000' AS COD_BCO_PAGO
		, '000' AS COD_BCO_RECIBO
		
		, LEFT(CM."Codigo",4) AS COD_MESA
		
		, CASE WHEN OS."IdTipoOperacion" = 'COMSPT' THEN 'OFAN' ELSE 'EFAN' END  AS COD_MOVTO
		, coalesce(SUC."CodigoSucursal",'') AS COD_SUC
		, '' AS CTA_BCO_PAGO --?
		, '' AS CTA_BCO_RECIBO --?
		, 'O' AS DONDE_PAGO --?
		, 'O' AS DONDE_RECIBO --?
		, 0 AS FOLIO_CMBTE_EGR
		, 0 AS FOLIO_CMBTE_ING
		, 0 AS FOLIO_CMBTE_SPR
		, 0 AS FOLIO_COMP_CONT
		, 0 AS FOLIO_DOC_ADJ
		, '' AS FORMA_PAGO
		, '' AS FORMA_RECIBO
		, left(OS."Observacion",80) AS GLOSA
		
		, CAST(TO_VARCHAR (UTCTOLOCAL(OS."FechaOperacion",'America/Santiago','platform'),'YYYY-MM-DD:HH24:MI:SS.FF7') AS VARCHAR(26)) AS HORA_MOVTO --char](26) NOT NULL, ********* 
		, 'E' AS IND_ORD_EMIT
		, CASE WHEN OS."IdMonedaPrincipal" = 'USD' THEN OS."MontoMonedaPrincipal" ELSE OS."MontoMonedaSecundaria" END AS MONTO_DOLARES
		, CAST(CASE WHEN OS."IdMonedaPrincipal" = 'CLP' THEN OS."MontoMonedaPrincipal" ELSE OS."MontoMonedaSecundaria" END AS DECIMAL(17,2)) AS MONTO_PESOS
		, 0 AS SEC_MOVTO_ABONO
		, 0 AS SEC_MOVTO_CARGO
		, coalesce(MVTE."IdMovimientoComprobante",0) AS SEC_MOVTO_FAC
		, 0 AS SEC_ABONO_DOL
		, 0 AS SEC_CARGO_DOL
		, 0 AS SPREAD
		, OS."TipoCambioCosto" AS TASA_AGENTE
		, OS."TipoCambioCierre" AS TASA_CAMBIO
		, CASE MVTE."IdTipoComprobante" WHEN 'FAE' THEN 'FD' WHEN 'FEE' THEN 'FX' ELSE '' END AS TIPO_COMP_ADJ
		, '' AS TIPO_MON_SPREAD
		, OS."IdUsuario" AS USUARIO --?
		, '0' AS USUARIO_CER --?
		, OS."Utilidad" AS UTILITARIO
		, '' AS CONCEPTO_MVTO
		, OS."TipoCambioCosto" AS COSTO_FONDO --?
		, LEFT(OS."IdMonedaPrincipal",2) AS TIPO_MONEDA
		, '' AS IND_SOL_ANUL
		, 0 AS HORA_PRTO
		, 0 AS HORA_CEDO
		, '' AS GLOSA_AUX
		, '' AS TIPO_CLI
		, OS."MontoMonedaPrincipal" AS CANTIDAD_ORIGEN
		, LEFT(OS."IdMonedaPrincipal",2) AS MONEDA
		, OS."ParidadCosto" AS PARIDAD
		, OS."TipoCambioCierre" AS TIPO_CAMBIO
		, OS."ParidadCierre" AS PARIDAD_US
		, CASE  
			WHEN OS."IdTipoCanalIngreso" = 'MES' AND ins."Nemotecnico" = 'DISTRI' THEN 'DIST'
			WHEN OS."IdTipoCanalIngreso" = 'MES' AND ins."Nemotecnico" = 'INTERB' THEN 'MPTA'
			WHEN OS."IdTipoCanalIngreso" = 'DAT' THEN 'DATA' ELSE '' END AS ORIGEN --?  MPTA DIST
		, '' AS IND_OPE_MOD
		, 0 AS USUARIO_MOD
		, COALESCE(OS."IdOperacionSpotOrigen", 0) AS FOLIO_OPERACION
		, '' AS ESTADOCAJA
		, '$' AS MONEDA_SECUNDARIA
		, 'SA' AS TIPO_OPERACION
		, '' AS FPA_ID_RECIBO --?
		, '' AS FPA_ID_ENTREGO --?
		, '' AS COD_BCO_ORIGEN
		, '' AS CTA_BCO_ORIGEN
		, '' AS COD_SUCURSAL
		, '' AS ESTADOENVIO
		, '' AS OBSERVACIONENVIO
	FROM "BSCL.Mesa.BD::SPT.OperacionSpot" OS
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins on ins."IdInstrumento" = OS."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CE ON OS."IdEmpresa"  = CE."IdEmpresa"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."IdCliente" = OS."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio" = OS."IdPortafolio"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTE ON OS."IdOperacionSpot" = MVTE."IdTransaccion" AND MVTE."IdEstadoFacturacion" = 'FAC'  AND MVTE."IdMovimientoComprobante"  = OS."IdComprobanteFacturacion"
			AND ( ("NemotecnicoInstrumento" = 'ARBINT' AND MVTE."IdTipoOperacion" = 'COMSPT') OR ("NemotecnicoInstrumento"= 'DISTRI' AND MVTE."IdTipoOperacion" IN ('COMSPT','VENSPT') ) )
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AG ON OS."IdAgente" = AG."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" CEMP ON CEMP."IdPersona" = AG."IdPersona"
	LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarMesas" CM ON CM."IdMesa" = OS."IdMesa"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC ON CEMP."IdSucursal" = SUC."IdSucursal"
	LEFT JOIN 
	(
		SELECT TES."IdComprobanteOrigen",CMBTE."IdEstado"
		FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" TES
		LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" CMBTEDETOR ON CMBTEDETOR."IdIngresoEgreso" = TES."IdIngresoEgreso" and TES."IdTipoMovimiento"  = 'MOVING' 
		LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CMBTEDET ON CMBTEDETOR."IdComprobanteDetalle" = CMBTEDET."IdComprobanteDetalle"
		LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" CMBTE ON CMBTEDET."IdComprobante" = CMBTE."IdComprobante"
		WHERE CMBTE."IdEstado" = 'LIQ'
	) as TES ON TES."IdComprobanteOrigen"  = MVTE."IdComprobante"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

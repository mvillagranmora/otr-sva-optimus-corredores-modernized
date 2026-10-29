SET SCHEMA "BSCL";


	
DROP TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"
(	
	"IdComprobante"						BIGINT,
	"IdComprobanteDetalle"				BIGINT,
	"IdComprobanteFormaDePago"			BIGINT,
	"AfectaCuentaInversion"				NVARCHAR(1),
	"FechaMovimiento"					NVARCHAR(10),
	"IdConcepto"						NVARCHAR(6),
	"IdPersona"							BIGINT,
	"NombrePersona"						NVARCHAR(100),
	"TitularPago"						NVARCHAR(1),
	"IdTipoPersonaTitularPago"			NVARCHAR(3),
	"IdPersonaTitularPago"				BIGINT,
	"IdMoneda"							NVARCHAR(3),
	"Monto"								DECIMAL(26,8),
	"IdPortafolio"						BIGINT,
	"IdTipoMovimientoCtaInvCliente"		NVARCHAR(6),
	"IdTipoMovimientoCtaInvCorredora"	NVARCHAR(6),
	"IdTipoComprobante"					NVARCHAR(3),
	"IdTipoMovimientoAsociado"			NVARCHAR(6),
	"IdMovimientoAsociado"				BIGINT
)  ON COMMIT DELETE ROWS;


-------------------TABLA A BORRAR UNA VEZ ELIMINADA LA INTERMITENCIA
DROP TABLE "BSCL.Tesoreria.BD::TEM.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"  CASCADE;
CREATE COLUMN TABLE "BSCL.Tesoreria.BD::TEM.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion"
(	
	"IdComprobanteInformado"			BIGINT,
	"IdComprobanteFormaDePagoInformado" BIGINT,
	"IdComprobanteDetalle"				BIGINT,
	"IdComprobanteFormaDePago"			BIGINT,
	"AfectaCuentaInversion"				NVARCHAR(1),
	"FechaMovimiento"					NVARCHAR(10),
	"IdConcepto"						NVARCHAR(6),
	"IdPersona"							BIGINT,
	"NombrePersona"						NVARCHAR(100),
	"TitularPago"						NVARCHAR(1),
	"IdTipoPersonaTitularPago"			NVARCHAR(3),
	"IdPersonaTitularPago"				BIGINT,
	"IdMoneda"							NVARCHAR(3),
	"Monto"								DECIMAL(26,8),
	"IdPortafolio"						BIGINT,
	"IdTipoMovimientoCtaInvCliente"		NVARCHAR(6),
	"IdTipoMovimientoCtaInvCorredora"	NVARCHAR(6),
	"IdTipoComprobante"					NVARCHAR(3),
	"IdTipoMovimientoAsociado"			NVARCHAR(6),
	"IdMovimientoAsociado"				BIGINT,
	"IdEstado"							NVARCHAR(3),
	"LiquidacionParcialEnviaACTA"		NVARCHAR(1),
	"SecuenciaRelacionada"				BIGINT
);
-------------------TABLA A BORRAR UNA VEZ ELIMINADA LA INTERMITENCIA


DROP TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle"
(
	"IdComprobante"			 	BIGINT,
	"Codigo" 					NVARCHAR(100),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle"
(	
	"IdComprobante"	 			BIGINT,
	"Codigo" 					NVARCHAR(100),
	"RequiereAutorizacion" 		NVARCHAR(40),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle"
(	
	"IdComprobante" 			BIGINT,
	"Codigo" 					NVARCHAR(100),
	"Descripcion" 				NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso"
(	
	"Campo" 					NVARCHAR(40),
	"Id" 						BIGINT,
	"Servicio" 					NVARCHAR(5000),
	"Resultado"					NVARCHAR(3)
)  ON COMMIT DELETE ROWS;



DROP TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_TablaCodigosMensajes"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"CodigoDato1" 	NVARCHAR(100),
	"ValorDato1" 	NVARCHAR(520),
	"CodigoDato2" 	NVARCHAR(100),
	"ValorDato2" 	NVARCHAR(520),
	"Resultado"		NVARCHAR(4)
)  ON COMMIT DELETE ROWS;

									
COMMIT;

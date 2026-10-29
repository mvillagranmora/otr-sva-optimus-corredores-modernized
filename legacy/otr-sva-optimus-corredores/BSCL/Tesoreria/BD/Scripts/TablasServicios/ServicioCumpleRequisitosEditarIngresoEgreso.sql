SET SCHEMA "BSCL"; 

-- Entrada
DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar"    cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago"     		cascade;
 
-- Salida
DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados"    cascade;
DROP TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_ErroresDetalle"      	   	cascade;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_IngresoEgresoEditar"
(
	"NumeroReferencia"							BIGINT, 
	"IdTipoMovimiento"  						NVARCHAR(6),  -- MOVING, MOVEGR
   	"IdTipoPersona"								NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
   	"IdPersona"    								BIGINT,   
   	"IdPortafolio"								BIGINT,
   	"IdMoneda"									NVARCHAR(3),
   	"Monto" 									DECIMAL(26,8),
   	"FechaLiquidacion"           				NVARCHAR(10),                                                                      
   	"IdConcepto" 								NVARCHAR(6),
   	"IdTipoPersonaTitularPago"					NVARCHAR(3),  -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
   	"IdPersonaTitularPago"    					BIGINT,
   	"IdPortafolioTitularPago"					BIGINT,
   	"IdCaja"   									BIGINT,
   	"SecuenciaAgrupacion" 						NVARCHAR(40), --Aquellos mov con la misma secuencia no son mezclados en distintos comprobante
   	"SecuenciaRelacionada"						BIGINT,		  --Permite relacionar distintos mov y generar un orden de liquidación entre ellos.
   	"Glosa" 									NVARCHAR(260),
									   	
   	-- Parámetros Opcionales(Referenciales)  
   	"IdEmpleadoDigitador" 						BIGINT,
   	"IdSucursalEmpleadoDigitador" 				BIGINT,
   	"IdTipoProducto"   							NVARCHAR(4),
    "IdTipoOperacion" 							NVARCHAR(6),									    
    "IdTipoComprobante"							NVARCHAR(3),
   	"IdComprobante"								BIGINT,
   	"FechaComprobante"							NVARCHAR(40),
    "FechaLiquidacionComprobante"				NVARCHAR(40),
									    
    "IdOperacion" 								BIGINT,			
    "FechaOperacion"							NVARCHAR(40),
    "IdCondicionLiquidacion"					NVARCHAR(2),
    "IdBolsa" 									NVARCHAR(3),
    "IdMonedaOperacion" 						NVARCHAR(3),
    "MontoOperacion" 							DECIMAL(26,8),
    "IdTipoReajuste" 							NVARCHAR(3),
    "FechaParidad" 								NVARCHAR(40),
    "ValorParidad" 								DECIMAL(26,8),
    "FolioCartera" 								BIGINT,
    "IdInstrumento" 							BIGINT,
    "IdTipoInstrumento" 						NVARCHAR(3),
    "IdClasificacionTipoInstrumento" 			NVARCHAR(3),
									  
    -- Indicarores
    "Bilateral"									NVARCHAR(1), --Cleanimg
    "GeneraComprobante"							NVARCHAR(1), --S: genera comprobante automático									    
    "AgruparEnComprobante"						NVARCHAR(1), --S: permite agrupar varios mov. en un comprobante
	"MueveCuentaInversion"						NVARCHAR(1), --S: mueve cuenta inversion N: No mueve cuenta inversion
    "TitularPago"								NVARCHAR(1)  --S: Socio que genera movimiento es quién cobra o paga
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Entrada_FormaDePago"
 LIKE "BSCL.Tesoreria.BD.Tipos::TES.FormaDePago"  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_ErroresDetalle"
(
	"NumeroReferencia" 		BIGINT, 
	"Codigo" 				NVARCHAR(100),
	"Descripcion" 			NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Tesoreria.BD::GBL.ServicioCumpleRequisitosEditarIngresoEgreso_Salida_MovimientosRechazados" 
(
	"NumeroReferencia" BIGINT
)  ON COMMIT DELETE ROWS;

											
commit


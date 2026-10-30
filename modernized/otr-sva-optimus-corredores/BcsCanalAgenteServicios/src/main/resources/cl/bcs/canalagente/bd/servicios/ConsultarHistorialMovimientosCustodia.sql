SET SCHEMA BSCL;
GO
DROP PROCEDURE "BSCL.CanalAgente.BD.Servicios::ConsultarHistorialMovimientosCustodia";
GO
CREATE PROCEDURE
-- 
--  Proposito: Consulta Historial de Movimientos Cuenta Inversion 
--  Autor: rENZo Ulloa
--  Fecha Creacion: 
--   
"BSCL.CanalAgente.BD.Servicios::ConsultarHistorialMovimientosCustodia" (
	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdPortafolio		BIGINT,
	IN p_FechaDesde			NVARCHAR(10),
	IN p_FechaHasta			NVARCHAR(10),
				
	OUT p_out_Resultado		TABLE (
							"IdCartera" 						BIGINT,
					    	"IdCliente" 						BIGINT,
					    	"IdPortafolio" 						BIGINT,
					    	"FechaOperacion" 					DATE,
					    	"TipoOperacion" 					NVARCHAR(6),
					    	"IdConcepto" 						NVARCHAR(6),
					    	"NombreConcepto" 					NVARCHAR(100),
					    	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
					    	"IdTipoInstrumento" 				NVARCHAR(3),
					    	"IdInstrumento" 					BIGINT,
					    	"Nemotecnico" 						NVARCHAR(40),
					    	"IdEmisor" 							NVARCHAR(40),
					    	"FechaVencimientoInstr" 			DATE,
					    	"FechaLiquidacion" 					DATE,
					    	"IdTipoReajuste" 					NVARCHAR(3),
					    	"NemoSVS" 							NVARCHAR(40),
					    	"IdMonedaInstr" 					NVARCHAR(3),
					    	"IdBoveda" 							NVARCHAR(3),
					    	"Cantidad" 							DECIMAL(26,8),
					    	"Precio"	 						DECIMAL(26,8),
					    	"Tasa"		 						DECIMAL(26,8),
					    	"Monto"	 							DECIMAL(26,8),
					    	"ValorRescate"						DECIMAL(26,8),
					    	"Saldo" 							DECIMAL(26,8),
					    	"FolioCartera"						BIGINT,
					    	"FolioComprobante"					BIGINT,
					    	"FechaComprobante"					DATE,
					    	"IdTipoComprobante"					NVARCHAR(6),
					    	"CodigoPortafolio"					NVARCHAR(100),
					    	"NombreCliente"						NVARCHAR(500),
					    	"DocumentoIdentidad"				NVARCHAR(40)
						) DEFAULT EMPTY,
	
	
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY,	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)) DEFAULT EMPTY
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	
	CALL "BSCL.Custodia.BD.Servicios::ConsultarCartolaMovimientos" ( 
	p_IdEmpresa							=> :p_IdEmpresa,
    --p_IdCliente							=> :p_IdEmpresa,
	p_IdPortafolio						=> :p_IdPortafolio,
	p_FechaInicio						=> :p_FechaDesde,
	p_FechaTermino						=> :p_FechaHasta);
    
    p_out_Resultado = SELECT "IdCartera",cus."IdCliente",cus."IdPortafolio","FechaOperacion","TipoOperacion","IdConcepto",
					    	"NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdEmisor","FechaVencimientoInstr",
					    	"FechaLiquidacion","IdTipoReajuste","NemoSVS","IdMonedaInstr","IdBoveda","Cantidad","Precio","Tasa","Monto","ValorRescate",
					    	"Saldo","FolioCartera","FolioComprobante","FechaComprobante","IdTipoComprobante",por."CodigoPortafolio","NombreCliente","DocumentoIdentidad"
					   FROM "BSCL.Custodia.BD::GBL.ConsultarCartolaMovimientos_Salida_Tabla" cus
					   LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por ON por."IdPortafolio" = cus."IdPortafolio" ;

END

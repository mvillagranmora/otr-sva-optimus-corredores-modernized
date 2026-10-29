SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarItemsManualesPosicionMRF";

-- 
--  Proposito: Consulta para Mantenedor de Items Manuales de Posicion MRF de UI
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 16/12/2016  
-- 
  
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarItemsManualesPosicionMRF" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),	--ID del Modulo que lo invoca
		  
	-- Parametros entrada
	IN p_Fecha				DATE,
	IN p_IdMetodo			BIGINT,
	
	-- Parametro salida para grilla de PlazosFechas
	OUT p_out_ItemsManuales	TABLE (	"IdPosicion"	BIGINT,
									"IdEvento"		BIGINT,
									"IdMetodo"		BIGINT,
									"Fecha"			DATE,
									"IdMatriz"		BIGINT,
									"Item"			NVARCHAR(255),
									"NombreEvento"	NVARCHAR(60),
									"MontoEvento"	DECIMAL(26,8),
									"CodigoUsuario"		NVARCHAR(100),
									"FechaUltimaModificacion" LONGDATE
									)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	v_out_ItemsManuales =
		SELECT
			 IMP."IdMetodo"
			,IMP."NombreMetodo"
			,IMP."Glosa"  		
			,IMP."IdTipoReajuste"
			,IMP."IdMesa"
			,IMP."IdMatriz"
			,IMP."IdAgrupacion"
			,IMP."IdEvento"
			,IMP."IdTipoEvento"
			,IMP."IndAccion"
			,IMP."IdOperacion"
			,IMP."IdTipoProducto"
			,IMP."IdTipoOperacion"
			,IMP."IdTipoComprobante"
			,IMP."NombreEvento"
			,IMP."IdTipoEstado"
			,IMP."IdEmpresa"
			,IMP."IdParametro"
			,IMP."NombreTipoParametro"
			,IMP."ValorParametro"
			,IMP."NombreParametro"	AS "Item"
			,COALESCE(IMP."IdPosicion",0) AS "IdPosicion"
			,COALESCE(IMP."Fecha",'0001-01-01') AS "Fecha"
			,COALESCE(IMP."IdMatrizPOS",0) AS "IdMatrizPOS"
			,COALESCE(IMP."MontoEvento",0) AS "MontoEvento"
			,IMP."CodigoUsuario"
			,IMP."FechaUltimaModificacion"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarItemsManualesPosicionMRF" IMP
		WHERE (IMP."Fecha"		= p_Fecha OR IMP."Fecha" IS NULL)
		  AND IMP."IdMetodo"	= p_IdMetodo
		  AND IMP."IdEmpresa"	= p_IdEmpresa;
	
	
	p_out_ItemsManuales =
		SELECT "IdPosicion", "IdEvento", "IdMetodo", "Fecha", "IdMatriz", "Item", "NombreEvento", "MontoEvento", "CodigoUsuario", "FechaUltimaModificacion" FROM :v_out_ItemsManuales;

END; 

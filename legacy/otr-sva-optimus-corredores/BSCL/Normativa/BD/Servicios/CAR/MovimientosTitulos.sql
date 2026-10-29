SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.MovimientosTitulos";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.MovimientosTitulos" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL" READS SQL DATA
AS
BEGIN

	p_VariablesProceso = 
	SELECT "IdCliente","IdPortafolio",0 AS "FolioCartera",'' AS "IdClasificacionTipoInstrumento",'' AS "IdTipoInstrumento",0 AS "IdInstrumento"
	FROM :p_Datos;
	
	CALL "BSCL.Custodia.BD.Servicios::ConsultarCustodiaMovimientos" (
		p_IdEmpresa			=> p_IdEmpresa,
	    p_IdUsuario			=> p_IdUsuario,
	    p_IdModulo			=> p_IdModulo,
		p_FechaInicio		=> p_FechaDesde,
		p_FechaTermino		=> p_FechaHasta,
		p_VariablesProceso	=> :p_VariablesProceso,
	    p_out_Tabla			=> v_ResultadoConsultaMovimientosCustodia
	);

	SELECT  M."IdCliente",
			M."IdPortafolio",
			M."IdTipoComprobante",
			M."FolioComprobante",
			M."Accion",
			M."TipoOperacion",
			M."IdTipoOperacion",
    		T."Nombre" AS "NombreTipoOperacion",
			TO_VARCHAR(M."FechaOperacion" , 'DD-MM-YYYY') AS "FechaOperacion",
			M."IdClasificacionTipoInstrumento",
    		M."Nemotecnico",
    		M."IdEmisor" AS "Emisor",
    		M."IdTipoReajuste",
    		M."IdMonedaInstr" AS "IdMonedaInstrumento",
    		M."IdConcepto",
    		C."Nombre" AS "NombreConcepto",
    		M."Cantidad",
    		CASE WHEN M."Accion" = 'RETIRO' THEN '-'||CAST(M."Cantidad" AS NVARCHAR) ELSE CAST(M."Cantidad" AS NVARCHAR) END AS "CantidadString",    		
    		M."PrecioMercado",
			M."TasaMercado",
			M."Precio" AS "PrecioCompra",
			M."Tasa" AS "TasaCompra",
			M."Monto" AS "MontoCompraMonedaInstrumento",
			M."MontoMercadoMonedaLocal",
			M."MontoMercadoMonedaInstrumento",
			M."MontoMercadoMonedaAdicional"
	FROM :v_ResultadoConsultaMovimientosCustodia AS M
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS T ON (T."IdTipoOperacion" = M."IdTipoOperacion")
	INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS C ON (C."IdConcepto" = M."IdConcepto")
	ORDER BY "NroFila" DESC;
	
END;
GO

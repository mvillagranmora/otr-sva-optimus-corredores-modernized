SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacionAnalitica";

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarAsignacionAnalitica" AS
SELECT
    T."IdEmpresa",
    A."IdAsignacion",
    CASE WHEN IFNULL(A."FechaIngresoRegistro",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  A."FechaIngresoRegistro" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE LEFT(TO_CHAR(ADD_SECONDS (A."FechaIngresoRegistro", CONFIGURACION."Valor")),19)
        END 
    END AS "FechaIngresoRegistro",
    CASE WHEN IFNULL(A."FechaAsignacion",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  A."FechaAsignacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE LEFT(TO_CHAR(ADD_SECONDS (A."FechaAsignacion", CONFIGURACION."Valor")),19)
        END 
    END AS "FechaAsignacion",
    CASE WHEN A."IdEstadoAsignacion" = '' THEN NULL ELSE A."IdEstadoAsignacion" END AS "IdEstadoAsignacion",
    A."CantidadAsignada",
    A."MontoAsignado",
    A."ValorRescateAsignado",
    A."MontoAsignadoLocal",
    A."TipoCambio",
    A."MontoAsignadoLiquidacion",
    CASE WHEN A."IdTransaccion" = 0 THEN NULL ELSE A."IdTransaccion" END AS "IdTransaccion",
    CAST(T."Folio" AS NVARCHAR(40)) AS "FolioTransaccion",
    CASE WHEN IFNULL(O."FechaOperacion",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaOperacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE LEFT(TO_CHAR(ADD_SECONDS (O."FechaOperacion", CONFIGURACION."Valor")),19)
        END 
    END AS "FechaOperacionTransaccion",
    T."Cantidad" AS "CantidadTransaccion",
    T."Precio" AS "PrecioTransaccion",
   -- T."ValorCaptacion" AS "ValorCaptacionTransaccion",
    T."ValorRescate" AS "ValorRescateTransaccion",
    T."Tasa" AS "TasaTransaccion",
    T."Monto" AS "MontoTransaccion",
    CASE WHEN T."IdTransaccionOd" = 0 THEN NULL ELSE T."IdTransaccionOd" END AS "IdTransaccionOd",
    CASE WHEN T."IndicadorOrdenDirecta" = 'OD' THEN CAST(T."Folio" AS NVARCHAR(40)) ELSE '' END "FolioTransaccionOD",
    CASE WHEN T."IdTransaccionPlazo" = 0 THEN NULL ELSE T."IdTransaccionPlazo" END AS "IdTransaccionPlazo",
    CASE WHEN T."IdEstado" = '' THEN NULL ELSE T."IdEstado" END AS "IdEstadoTransaccion",
    CASE WHEN T."CodigoOperadorCorredor" = '' THEN NULL ELSE CAST(T."CodigoOperadorCorredor" AS NVARCHAR(40)) END AS "IdOperadorCorredor",
    CASE WHEN T."CodigoOperadorContraparte" = '' THEN NULL ELSE CAST(T."CodigoOperadorContraparte" AS NVARCHAR(40)) END AS "IdOperadorContraparte",
    CASE WHEN T."IdCorredorContraparte" = 0 THEN NULL ELSE T."IdCorredorContraparte" END AS "IdCorredorContraparte",
    CASE WHEN CC."CodigoCorredora" = '' THEN NULL ELSE CAST(CC."CodigoCorredora" AS NVARCHAR(40)) END AS "CodigoCorredorContraparte",
    CASE WHEN T."IdBolsa" = '' THEN NULL ELSE T."IdBolsa" END AS "IdBolsaTransaccion",
   -- T."EsOD",
    T."IndicadorOrdenDirecta",
    CASE WHEN A."IdOrden" = 0 THEN NULL ELSE A."IdOrden" END AS "IdOrden",
    O."Cantidad" AS "CantidadOrden",
    CASE WHEN O."IdTipoPrecio" = '' THEN NULL ELSE O."IdTipoPrecio" END AS "IdTipoPrecioOrden",
    O."PrecioOrden",
    CASE T."EsVentaCorta" WHEN 'S' THEN O."TasaVentaCorta" ELSE T."Tasa" END AS "TasaOrden",
    O."ValorRescate" AS "ValorRescateOrden",
    O."IdTipoVigenciaOrden",
    O."FechaVigencia" AS "FechaVigenciaOrden",
    
    CASE WHEN IFNULL(O."FechaOperacion",'')= '' THEN '0001-01-01 00:00:00' 
        ELSE  
           CASE WHEN  O."FechaOperacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
           ELSE TO_CHAR(TO_SECONDDATE(UTCTOLOCAL (TO_TIMESTAMP(o."FechaOperacion", 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform')))
        END 
    END AS "FechaOperacionOrden",
    
    CAST(O."Glosa" AS NVARCHAR(520)) AS "GlosaOrden",
    CASE WHEN O."IdLibroOrdenes" = 0 THEN NULL ELSE O."IdLibroOrdenes" END AS "IdLibroOrdenes",
    CAST(LO."NombreLibroOrdenes" AS NVARCHAR(100)) AS "NombreLibroOrdenes",
    T."EsAPlazo",
    T."FolioCartera",
    CASE WHEN A."IdMovimientoComprobante" = 0 THEN NULL ELSE A."IdMovimientoComprobante" END AS "IdMovimientoComprobante",
    CASE WHEN T."IdClasificacionTipoInstrumento" = '' THEN NULL ELSE T."IdClasificacionTipoInstrumento" END AS "IdClasificacionTipoInstrumento",
    CASE WHEN T."IdTipoInstrumento" = '' THEN NULL ELSE T."IdTipoInstrumento" END AS "IdTipoInstrumento",
    CASE WHEN T."IdInstrumento" = 0 THEN NULL ELSE T."IdInstrumento" END AS "IdInstrumento",
    CASE WHEN T."IdClasificacionTipoInstrumento" ='IF' THEN CAST(T."NemotecnicoSVS" AS NVARCHAR(40)) ELSE CAST(T."Nemotecnico" AS NVARCHAR(40)) END AS "Nemotecnico",
    CASE WHEN T."IdMoneda" = '' THEN NULL ELSE T."IdMoneda" END AS "IdMoneda",
    CASE WHEN T."IdTipoReajuste" = '' THEN NULL ELSE T."IdTipoReajuste" END AS "IdTipoReajuste",
    T."EsFungible",
    CASE WHEN T."IdEmisor" = '' THEN NULL ELSE CAST(T."IdEmisor" AS NVARCHAR(40)) END AS "IdEmisor",
    CASE WHEN T."IdInstrumentoSubyacente" = 0 THEN NULL ELSE T."IdInstrumentoSubyacente" END AS "IdInstrumentoSubyacente",
    COALESCE(CAST(I."Nemotecnico" AS NVARCHAR(40)),'') AS "NemotecnicoSubyacente",
    CASE WHEN I."IdMoneda" = '' THEN NULL ELSE I."IdMoneda" END AS "IdMonedaSubyacente",
    CASE WHEN T."IdCondicionDeLiquidacion" = '' THEN NULL ELSE T."IdCondicionDeLiquidacion" END AS "IdCondicionDeLiquidacion",
    TO_NVARCHAR(T."FechaLiquidacion",'YYYY-MM-DD') as "FechaLiquidacion",
    CASE WHEN O."IdMonedaLiquidacionOrden" = '' THEN NULL ELSE O."IdMonedaLiquidacionOrden" END AS "IdMonedaLiquidacion",
    TO_NVARCHAR(T."FechaVencimiento",'YYYY-MM-DD') AS "FechaVencimiento",
    T."Plazo",
    T."EsVentaCorta",
    T."IndicadorDCV",
    CASE WHEN T."IdTipoOperacion" = '' THEN NULL ELSE T."IdTipoOperacion" END AS "IdTipoOperacion",
    O."PrecioMercado",
    CASE WHEN O."IdCliente" = 0 THEN NULL ELSE O."IdCliente" END AS "IdCliente",
    CAST(AC."Nombres" AS NVARCHAR(100)) AS "Nombres",
    CAST(AC."ApellidoPaterno" AS NVARCHAR(100)) AS "ApellidoPaterno",
    CAST(AC."ApellidoMaterno" AS NVARCHAR(100)) AS "ApellidoMaterno",
    CASE WHEN AC."IdPersona" = 0 THEN NULL ELSE AC."IdPersona" END AS "IdPersona",
    CAST(AC."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad",
    CASE WHEN O."IdPortafolioCliente" = 0 THEN NULL ELSE O."IdPortafolioCliente" END AS "IdPortafolioCliente",
    CAST(P."CodigoPortafolio" AS NVARCHAR(100)) AS "CodigoPortafolio",
    CAST(P."DescripcionPortafolio" AS NVARCHAR(100)) AS "DescripcionPortafolio",
    CAST(P."Proposito" AS NVARCHAR(520)) AS "PropositoPortafolio",
    CASE WHEN O."IdTipoCanalIngreso" = '' THEN NULL ELSE O."IdTipoCanalIngreso" END AS "IdTipoCanalIngreso",
    CASE WHEN O."IdProducto" = '' THEN NULL ELSE O."IdProducto" END AS "IdProducto",
    CASE WHEN O."IdPorcentajeVisibilidad" = 0 THEN NULL ELSE O."IdPorcentajeVisibilidad" END AS "IdPorcentajeVisibilidad",
	CASE WHEN A."IdOperacionSpot" = 0 THEN NULL ELSE A."IdOperacionSpot" END AS "IdOperacionSpot"
FROM "BSCL.Orden.BD::ORD.Asignacion" A
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" T ON T."IdTransaccion" = A."IdTransaccion"
INNER JOIN "BSCL.Orden.BD::ORD.Orden" O ON O."IdOrden" = A."IdOrden"
INNER JOIN "BSCL.Orden.BD::ORD.LibroOrdenes" LO ON LO."IdLibroOrdenes" = O."IdLibroOrdenes" AND LO."IdEmpresa" = T."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" I ON I."IdInstrumento" = T."IdInstrumentoSubyacente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AC ON AC."IdCliente" = O."IdCliente"
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" P ON P."IdPortafolio" = O."IdPortafolioCliente"
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CC ON CC."IdCorredora" = T."IdCorredorContraparte" AND CC."IdBolsa" = T."IdBolsa"
CROSS JOIN (select top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro" = 'PLA.SegundosZonaHoraria') AS CONFIGURACION;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarAsignacionAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
SELECT "IdCliente",TIT."IdPortafolio","TipoComprobante" as "IdTipoComprobante","FolioComprobante","Operacion" as "NombreTipoOperacion",
to_nvarchar("FechaOperacion",'DD-MM-YYYY') as "FechaOperacion","Instrumentos" as "Nemotecnico","Emisor" AS "Emisor",TIT."IdMoneda" AS "IdMonedaInstrumento",
CASE WHEN MVTO.IND_TIT_CCTE='T' AND MVTO.IND_CUST_CTDO='S'THEN (-1 * TIT."Cantidad") ELSE "Cantidad"  END AS "Cantidad","PrecioTir" AS "PrecioCompra",
CASE WHEN MVTO.IND_TIT_CCTE='T' AND MVTO.IND_CUST_CTDO='S'THEN(-1 * TIT."MontoNeto") ELSE "MontoNeto" END  AS "MontoCompraMonedaInstrumento", INS."IdClasificacionTipoInstrumento" 
FROM "BSCL.Normativa.BD::CAR.MovimientosTitulosSebra" AS TIT
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON P."IdPortafolio" = TIT."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."Nemotecnico"  = TIT."Instrumentos"
LEFT JOIN "BSCL.Sebra.BD::MIG.TBCTMVTO" MVTO ON (MVTO.SEC_MOVTO = TIT."FolioComprobante" ) 
WHERE "FechaOperacion" >= :FechaDesde AND "FechaOperacion" <= :FechaHasta
ORDER BY SEC_MOVIMIENTO

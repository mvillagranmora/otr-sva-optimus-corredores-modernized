-- Datos de movimientos de cargo abono
SELECT 
(	
	COALESCE("NombreCliente",'') || ';' ||
	COALESCE("DocumentoIdentidad",'') || ';' ||
	COALESCE("CodigoPortafolio",'')  || ';' ||
	TO_NVARCHAR("FechaSistema",'DD-MM-YYYY')  || ';' ||
	TO_NVARCHAR("FechaEfectivaMovimiento",'DD-MM-YYYY')  || ';' ||
	COALESCE("NombreConcepto",'') || ';' ||
	COALESCE("EsCargoAbonoPorTraspasoTraducido",'') || ';' ||
	COALESCE("CodigoMoneda",'') || ';' ||		
	REPLACE(CAST(COALESCE("Cargo",0) AS NVARCHAR(50)), '.', ',')  || ';' ||
	REPLACE(CAST(COALESCE("Abono",0) AS NVARCHAR(50)), '.', ',') || ';' ||	
	COALESCE("NombreTipoComprobante",'') || ';' || 		
	COALESCE("IdComprobante",0) || ';' || 			
	COALESCE("FolioComprobante",'') || ';' ||		
	TO_NVARCHAR("FechaComprobante",'DD-MM-YYYY') || ';' ||
	COALESCE("NombreTipoProducto",'') || ';' ||
	COALESCE("NombreTipoOperacion",'') || ';' ||
	TO_NVARCHAR("FechaOperacion",'DD-MM-YYYY') || ';' ||
	TO_NVARCHAR("FechaVencimientoOperacion",'DD-MM-YYYY') || ';' ||
	COALESCE("IdOperacion",0) || ';' ||
	REPLACE (CAST(COALESCE("MontoOperacion",0) AS NVARCHAR(50)), '.', ',') || ';' ||
	COALESCE("IdCargoAbonoMovimiento",0) || ';' ||
	COALESCE("IdCargoAbonoRelacionadoTraspaso",0) || ';' ||
	COALESCE("NombreClienteRelacionado",'') || ';' ||
	COALESCE("CodigoPortafolioRelacionado",'') || ';' ||
	COALESCE("CodigoUsuarioCreador",'') || ';' ||
	TO_NVARCHAR("FechaCertificacion",'DD-MM-YYYY') || ';' ||
	COALESCE("CodigoUsuarioCertificador",'') || ';' ||
	COALESCE("Descripcion",'') || ';' ||
	COALESCE("NombreEstado",'') || ';' ||
	COALESCE("GlosaAnulacion",'') || ';' ||
	COALESCE("AutorizacionEstado",'') || ';' ||
	COALESCE("NombreEstadoEvento",'') || ';' ||
	COALESCE("Producto",'')	|| ';' ||
	COALESCE("Relacion",'') 
) AS "Linea"

FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos"
WHERE "FechaSistema" 	>= :FechaDesde 
AND   "FechaSistema" 	<= :FechaHasta
AND   COALESCE("IdEjecutivo", 0) = (CASE WHEN :IdEjecutivo = 0 THEN COALESCE("IdEjecutivo", 0) ELSE :IdEjecutivo END)
AND   "IdMoneda" 		= (CASE WHEN :IdMoneda = '0' THEN "IdMoneda" ELSE :IdMoneda END)
AND   "IdTipoMovimiento" = (CASE WHEN :IdTipoMovimiento = '0' THEN "IdTipoMovimiento" ELSE :IdTipoMovimiento END)
AND   "IdConcepto" 		= (CASE WHEN :IdConcepto = '0' THEN "IdConcepto" ELSE :IdConcepto END)
AND   "IdEstado" 		= (CASE WHEN :IdEstado = '0' THEN "IdEstado" ELSE :IdEstado END);




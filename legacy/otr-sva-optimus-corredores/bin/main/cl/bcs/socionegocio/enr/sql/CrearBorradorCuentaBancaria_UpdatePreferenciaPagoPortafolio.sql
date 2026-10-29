UPDATE  PREFPAGO
SET
	PREFPAGO."PorDefecto" = 'N'
FROM "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio" PREFPAGO
INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE ON PRE."IdPreferenciaPago" = PREFPAGO."IdPreferenciaPago"
WHERE PREFPAGO."IdEnrolamiento" = :IdEnrolamiento
AND PRE."IdMoneda" = :IdMoneda 
AND PRE."RecibePagaPortafolio" = 'R'
AND PRE."IdTipoProducto" NOT IN ('MEME','MERF','MEFW');
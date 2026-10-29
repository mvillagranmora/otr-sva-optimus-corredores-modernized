UPDATE  DOC
SET
	DOC."PorDefecto" = 'N'
FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = DOC."IdPortafolio" 
INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE ON PRE."IdPreferenciaPago" = DOC."IdPreferenciaPago"
WHERE POR."IdCliente" = :IdCliente 
AND PRE."IdMoneda" = :IdMoneda 
AND PRE."RecibePagaPortafolio" = 'R'
AND PRE."IdTipoProducto" NOT IN ('MEME','MERF','MEFW');
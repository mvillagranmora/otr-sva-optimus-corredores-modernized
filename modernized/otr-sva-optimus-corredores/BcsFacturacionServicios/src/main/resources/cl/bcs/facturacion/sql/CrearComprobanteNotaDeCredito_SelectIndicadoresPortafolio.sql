SELECT 
	"IdTipoIndicador",
	"CodigoTipoIndicador",
	"ValorIndicador",
	"IdPortafolio",
	"IdCliente"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  
WHERE
"CodigoTipoIndicador" IN ('ORDOCUSTRV', 'ORDOCUSTRF', 'ORDSOBREGIRORV','ORDSOBREGIRORF', 'ORDSOBREGIROIF')   AND
"IdTipoEntidad" = 'PORTAFOLIO'							AND
"IdPortafolio" IN (:idsPortafolios)
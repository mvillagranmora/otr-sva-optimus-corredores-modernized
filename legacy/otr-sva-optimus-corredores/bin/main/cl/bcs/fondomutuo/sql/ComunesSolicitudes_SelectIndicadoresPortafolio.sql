SELECT 
	"IdCliente"
	,"IdPortafolio"
	,"ValorIndicador"
	,"CodigoTipoIndicador"
FROM 
"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
WHERE "IdPortafolio" IN (:IdPortafolio) AND "CodigoTipoIndicador" = :CodigoTipoIndicador;
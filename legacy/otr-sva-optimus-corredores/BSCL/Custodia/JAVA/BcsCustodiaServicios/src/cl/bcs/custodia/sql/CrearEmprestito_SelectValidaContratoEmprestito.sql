SELECT
	"ValorIndicador"
FROM
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"
WHERE
	"CodigoTipoIndicador" = 'CE'
AND "IdCliente" = :p_IdClientePrestamista;
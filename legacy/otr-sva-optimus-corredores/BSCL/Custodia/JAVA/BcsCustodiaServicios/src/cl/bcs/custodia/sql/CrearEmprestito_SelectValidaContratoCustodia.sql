SELECT "ValorIndicador"
FROM
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"
WHERE
	"CodigoTipoIndicador" = 'CC'
AND "IdCliente" = :p_IdClientePrestamista;
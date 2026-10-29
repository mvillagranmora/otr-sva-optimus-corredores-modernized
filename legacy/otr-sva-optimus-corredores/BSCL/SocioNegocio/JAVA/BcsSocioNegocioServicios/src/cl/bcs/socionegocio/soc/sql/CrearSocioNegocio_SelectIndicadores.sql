		SELECT
			"IdIndicador",
			"IdTipoIndicador",
			"ValorIndicador"
		FROM
			"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"
WHERE
	"CodigoTipoIndicador" = :p_CodigoTipoIndicador
AND (:p_IdCliente IS NULL OR MAP("IdCliente", NULL, 0, "IdCliente") = :p_IdCliente)
AND (:p_IdPersona IS NULL OR MAP("IdPersona", NULL, 0, "IdPersona") = :p_IdPersona);
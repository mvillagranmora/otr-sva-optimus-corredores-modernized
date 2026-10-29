SELECT "CodigoCorredora", "Codigo", "IdCorredora"
FROM
	"BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"
WHERE
	"IdBolsa" = 'BCS'
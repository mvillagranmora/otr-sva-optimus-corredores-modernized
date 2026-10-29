INSERT INTO
    "BSCL.Plataforma.BD::IND.Indicador" (
        "IdIndicador",
        "IdEntidad",
        "IdTipoEntidad",
        "IdTipoIndicador",
        "ModeloCliente",
        "IdEmpresa",
        "ValorIndicador",
        "FechaIngreso",
        "FechaCreacion",
        "FechaModificacion",
        "IdEstadoAutorizacion",
        "FechaVencimiento",
        "IdCarga"
    )  
SELECT
	"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL,
	:IdPortafolio,
	'PORTAFOLIO',
	"IdTipoIndicador",
	:ModeloCliente,
	:IdEmpresa,
	:ValorIndicador,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	'ING',
	'0001-01-01 00:00:00.000',
	0
FROM
	"BSCL.Plataforma.BD::IND.TipoIndicador"
WHERE
	"CodigoTipoIndicador" = :CodigoTipoIndicador;
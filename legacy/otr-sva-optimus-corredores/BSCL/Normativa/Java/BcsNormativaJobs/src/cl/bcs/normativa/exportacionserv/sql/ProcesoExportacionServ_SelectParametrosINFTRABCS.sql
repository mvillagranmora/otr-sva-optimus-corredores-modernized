SELECT "IdSolicitudParametros",
	"TipoParametro",
	"Valor"
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
WHERE SOL."TipoParametro" IN ('FINI','FTER','IDCLI','NEMO') AND SOL."IdSolicitudCircular" = :IdSolicitudCircular;
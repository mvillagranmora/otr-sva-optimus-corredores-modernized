INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"(
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
	"FechaVencimiento"
)SELECT
	"BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL AS "IdIndicador",
	:IdPortafolio AS "IdEntidad",
	ATR."IdTipoEntidad",
	ATR."IdTipoIndicador",
	ATR."ModeloCliente",
	ATR."IdEmpresa",
	CASE ATR."CodigoTipoIndicador"
		WHEN 'CAR_DIRELE' THEN TO_NVARCHAR(Cliente."IdDireccionElectronicaPrincipal")
		WHEN 'CAR_DIRFIS' THEN TO_NVARCHAR(Cliente."IdDireccionFisicaPrincipal")
		WHEN 'FAC_DIRELE' THEN TO_NVARCHAR(Cliente."IdDireccionElectronicaPrincipal")
		WHEN 'GEN_CODDIR' THEN TO_NVARCHAR(Cliente."IdDireccionFisicaPrincipal")
		WHEN 'GEN_DIR_PROPIA' THEN TO_NVARCHAR('S')
		ELSE ATR."ValorIndicador" 
	END AS "ValorIndicador",
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	'ING' AS "IdEstadoAutorizacion",
	'0001-01-01 00:00:01'
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" Cliente
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" P ON Cliente."IdCliente" = P."IdCliente" AND P."IdPortafolio"  = :IdPortafolio
INNER JOIN (
	SELECT TI."CodigoTipoIndicador", I.* FROM BSCL."BSCL.Plataforma.BD::IND.TipoIndicador" TI
	INNER JOIN BSCL."BSCL.Plataforma.BD::IND.Indicador" I ON TI."IdTipoIndicador" = I."IdTipoIndicador"
	WHERE TI."IdTipoEntidad" = 'PORTAFOLIO' AND TI."IdUso" = 'PRE'
) ATR ON Cliente."IdTipoCliente" = ATR."ModeloCliente" AND ATR."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdEntidad" = 0
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" I ON I."IdEntidad" = P."IdPortafolio" AND I."IdTipoEntidad" = 'PORTAFOLIO' AND ATR."IdIndicador" = I."IdIndicador"
WHERE "IdPortafolio" = :IdPortafolio AND I."IdEntidad" IS NULL 
--HEREDAR SOLO LAS SIGUIENTES PREFERENCIAS DE PORTAFOLIO DESDE EL MODELO TIPO CLIENTE
AND ATR."CodigoTipoIndicador" IN ('CAR_DIRELE', 'CAR_DIRFIS', 'FAC_DIRELE',  'ORDMODALIDAD','GEN_CODDIR','GEN_DIR_PROPIA');
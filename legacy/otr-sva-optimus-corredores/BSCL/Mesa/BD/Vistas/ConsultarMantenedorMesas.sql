SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarMantenedorMesas"; 
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarMantenedorMesas" AS
SELECT
	 MES."IdMesa"
    ,MES."IdEmpresa"
	,MES."IdCliente"
	,CLI."DocumentoIdentidad"
	,CASE WHEN CLI."Tipo"='J' THEN IFNULL(CLI."RazonSocial",' - ') ELSE IFNULL(CLI."NombreCompleto",' - ') END AS "NombreCliente"
	,MES."IdPortafolio"
	,IFNULL(POR."CodigoPropositoPortafolio",' - ') AS "CodigoPortafolio"
	,MES."Codigo"
	,MES."Nombre"
	,MES."IdTipoProducto"
	,TPMM."Nombre" AS "TipoProducto"
	,MES."IdEstadoMesa"
	,CASE MES."IdEstadoMesa"
		WHEN 'A' THEN 'ABIERTO'
		WHEN 'C' THEN 'CERRADO'
		WHEN 'B' THEN 'BLOQUEADO'
		ELSE '' END AS "EstadoMesa"
	,MES."IdEstadoRegistro"
	,CASE MES."IdEstadoRegistro"
		WHEN 'ACT' THEN 'ACTIVO'
		WHEN 'INA' THEN 'INACTIVO'
		ELSE '' END AS "EstadoRegistro"
	,MES."IdUsuarioModificacion"
	,IFNULL(USR."CodigoUsuario",' - ') AS "CodigoUsuario"
	,MES."FechaModificacion"
	,TO_CHAR(MES."FechaModificacion",'DD-MM-YYYY HH24:MI:SS') AS "FechaModificacionFrmt"
	,MES."PorDefecto"
	,CASE WHEN MES."PorDefecto"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "PorDefectoUI"
	,MES."ValorRazonable"
	,PAR."NombreParametro" AS "NombreValorRazonable"
	,TPMM."IdTipoProductoMesaModulo"
	,UPPER(COALESCE(IFRS."Codigo",'')) AS "CodigoIFRS"
	,UPPER(COALESCE(IFRS."Descripcion",'')) AS "DescripcionIFRS"
	,MES."AplicaFusion"
	,CASE WHEN MES."AplicaFusion"='S' THEN 'UI_SI' ELSE 'UI_NO' END AS "AplicaFusionUI"
	FROM "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	ON (MES."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
	ON (MES."IdPortafolio" = POR."IdPortafolio")
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo" TPMM
	ON (MES."IdTipoProducto" = TPMM."IdTipoProductoMesa" AND TPMM."IdEstado" = 'ACT' AND MES."IdEmpresa" = TPMM."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USR
	ON (MES."IdUsuarioModificacion" = USR."IdUsuario" AND USR."IdEstado" = 'ACT' AND MES."IdEmpresa" = USR."IdEmpresa")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarParametro" PAR
	ON (MES."ValorRazonable" = PAR."ValorParametro" AND PAR."IdEstado" = 'VIG' AND MES."IdEmpresa" = PAR."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCodigoIFRS" IFRS 
	ON (MES."CodigoIFRS" = IFRS."Codigo")
ORDER BY MES."IdTipoProducto" ASC, MES."Nombre" ASC;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarMantenedorMesas" TO BCS_DESARROLLADOR WITH GRANT OPTION;

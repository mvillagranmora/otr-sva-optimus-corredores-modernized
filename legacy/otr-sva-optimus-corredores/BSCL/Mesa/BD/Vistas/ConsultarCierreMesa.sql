SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" AS
SELECT
	 MES."IdMesa"
    ,MES."IdEmpresa"
	,MES."IdCliente"
	,MES."IdPortafolio"
	,MES."Codigo"
	,MES."Nombre" AS "NombreMesa"
	,MES."IdTipoProducto"
	,TPMM."Nombre" AS "NombreTipoProducto"
	,MES."IdEstadoMesa"
	,CASE MES."IdEstadoMesa"
		WHEN 'A' THEN 'ABIERTO'
		WHEN 'C' THEN 'CERRADO'
		WHEN 'B' THEN 'BLOQUEADO'
		ELSE '' END AS "EstadoMesa"
	,MES."IdEstadoRegistro"
	,MES."IdUsuarioModificacion"
	,IFNULL(USR."CodigoUsuario",' - ') AS "CodigoUsuario"
	,MES."FechaModificacion"
	,TO_CHAR(MES."FechaModificacion",'DD-MM-YYYY HH24:MI:SS') AS "FechaModificacionFrmt"
	,MES."PorDefecto"
	,TPMM."IdTipoProductoMesaModulo"
	,TPMM."IdTipoProductoMesa"
	,TPMM."IdTipoProductoModulo"
	,MO."NombreModulo"
	,MO."IdEstado" AS "IdEstadoModulo"
	,CASE WHEN MO."IdEstado" = 'CER' THEN 'C' ELSE 'A' END AS "TipoEstadoModulo"
	,'MÓDULO '||MO."NombreModulo"||' '||MO."NombreEstado" AS "MsjEstadoModulo"
	,CONF."Valor" AS "FechaActualSistema"
FROM "BSCL.Mesa.BD.Vistas::ConsultarMesas" MES
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarTipoProductoMesaModulo" TPMM
	ON (MES."IdTipoProducto" = TPMM."IdTipoProductoMesa" AND TPMM."IdEstado" = 'ACT' AND MES."IdEmpresa" = TPMM."IdEmpresa")

--MSH
--INNER JOIN "BSCL.Plataforma.BD.Vistas::CRE.ConsultarCierreModulos" MO
--	ON (TPMM."IdTipoProductoModulo" = MO."IdModulo" AND TPMM."IdEmpresa" = MO."IdEmpresa")
INNER JOIN "BSCL.Plataforma.BD.Vistas::PAU.ConsultarModulosDetalle" MO
	ON (TPMM."IdTipoProductoModulo" = MO."CodigoModulo" AND TPMM."IdEmpresa" = MO."IdEmpresa")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USR
	ON (MES."IdUsuarioModificacion" = USR."IdUsuario" AND USR."IdEstado" = 'ACT' AND MES."IdEmpresa" = USR."IdEmpresa")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF
	ON (MES."IdEmpresa" = CONF."IdEmpresa" and CONF."CodigoParametro" = 'PLA.FechaSistema')
WHERE MES."IdEstadoRegistro" = 'ACT'; 
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarCierreMesa" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO
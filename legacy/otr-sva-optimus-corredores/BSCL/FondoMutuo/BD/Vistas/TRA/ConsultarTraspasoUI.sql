SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoUI";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoUI"
AS
SELECT 
	 TRASPASO."IdTraspaso"
    ,TRASPASO."IdEmpresa"
    ,TRASPASO."IdModulo"
	,TRASPASO."IdUsuario"
    ,TRASPASO."IdCliente"
    ,TRASPASO."IdPortafolio"
 	,TRASPASO."IdEmisor"
	,TRASPASO."IdMotivoTraspaso"
	,TRASPASO."IdTipoMovimientoTraspaso"
    ,TRASPASO."IdTipoTraspaso"
    ,TRASPASO."Glosa"
    ,TRASPASO."FechaHoraSuscripcion"
    ,TRASPASO."IdDivisionPais"
    ,TRASPASO."FolioTraspaso"
    ,TRASPASO."IdEstado"
    ,TRASPASO."Monto" 
    ,TRASPASO."FechaIngreso"
    ,TRASPASO."FechaAceptacion"
    ,TRASPASO."FechaAnulacion"
    ,TRASPASO."FechaModificacion"
    ,TRASPASO."FechaIngresoRegistro"
    ,TRASPASO."FechaAceptacionRegistro" 
    ,TRASPASO."FechaAnulacionRegistro" 
    ,TRASPASO."FechaModificacionRegistro"
    ,TRASPASO."FolioMLT"
	,TRASPASO."FolioCSC"
	,TRASPASO."IdEmpleadoEjecutivo"
	,PORTAFOLIO."CodigoPortafolio"
	,CLIENTE."RazonSocial"
	,CLIENTE."NombreCompleto"
	,CLIENTE."DocumentoIdentidad"
	,CLIENTE."IdPersona"
	,COALESCE(ESTADO."Nombre",'') 				AS "EstadoTraspaso"
	,COALESCE(EMISOR."NombreCorto",'')   		AS "NombreCortoEmisor"
	,COALESCE(EMISOR."DocumentoIdentidad",'') 	AS "DocumentoIdentidadEmisor"
	,COALESCE(MOTIVO."Descripcion",'')			AS "MotivoTraspaso"
	,COALESCE(TIPOMOV."Descripcion",'')			AS "TipoMovimientoTraspaso"
	,COALESCE(TIPOTRAS."Descripcion",'')		AS "TipoTraspaso"
	,COALESCE(USUARIO."CodigoUsuario",'')		AS "CodigoUsuario"
	,DAYS_BETWEEN (TO_DATE (TRASPASO."FechaIngreso", 'YYYY-MM-DD'), TO_DATE(CONFIG."FechaSistema", 'YYYY-MM-DD')) "DiasTranscurridos"
	
	,COALESCE(PEREJEC."NombreCompleto",'') 	AS "NombreCompletoEjecutivo"
	,COALESCE(EJECUTIVO."CodigoEmpleado",'') 	AS "CodigoEjecutivo"
	,COALESCE(EJECUTIVO."IdSucursal",0) 		AS "IdSucursal"
	,COALESCE(SUCURSAL."NombreSucursal",'') 	AS "NombreSucursal"
	,COALESCE(SUCURSAL."CodigoSucursal",'') 	AS "CodigoSucursal"
	,COALESCE(TRASPASO."IdEntidadRecaudadora",0) AS "IdEntidadRecaudadora"
	,COALESCE(ENTIDAD."Nombre",'') 				AS "NombreEntidadRecaudadora"
	,COALESCE(ENTIDAD."DocumentoIdentidad",'') 	AS "DocumentoIdentidadEntidadRecaudadora"
	,COALESCE(ENTIDAD."Codigo",0)				AS "CodigoEntidadRecaudadora"
	,COALESCE(FONDO."IdMoneda", 'CLP') AS "IdMonedaFondo"
FROM "BSCL.FondoMutuo.BD::TRA.Traspaso" AS TRASPASO
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					AS PORTAFOLIO 		ON  ( PORTAFOLIO."IdPortafolio" = TRASPASO."IdPortafolio" )
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 		AS CLIENTE 			ON  ( CLIENTE."IdCliente" = TRASPASO."IdCliente" )
INNER JOIN (
			SELECT  "Valor" AS "FechaSistema","IdEmpresa"
			 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
			 WHERE "CodigoParametro" = 'PLA.FechaSistema'
) AS CONFIG ON (CONFIG."IdEmpresa"=TRASPASO."IdEmpresa")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 					AS ESTADO			ON  ( ESTADO."IdEstado" =  TRASPASO."IdEstado")
LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor"	AS EMISOR			ON  ( EMISOR."IdEmisor" =  TRASPASO."IdEmisor")
LEFT OUTER  JOIN "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora"	AS ENTIDAD		ON  ( ENTIDAD."IdEntidadRecaudadora" =  TRASPASO."IdEntidadRecaudadora") --Esto es asi dado que el combo maneja el Codigo como Id
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::TRA.MotivoTraspaso"			AS MOTIVO			ON  ( MOTIVO."IdMotivoTraspaso" =  TRASPASO."IdMotivoTraspaso")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::TRA.TipoMovimientoTraspaso"	AS TIPOMOV			ON  ( TIPOMOV."IdTipoMovimientoTraspaso" =  TRASPASO."IdTipoMovimientoTraspaso")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::TRA.TipoTraspaso"				AS TIPOTRAS			ON  ( TIPOTRAS."IdTipoTraspaso" =  TRASPASO."IdTipoTraspaso")
LEFT OUTER  JOIN "BSCL.Plataforma.BD::ACC.Usuario" 					AS USUARIO			ON  ( USUARIO."IdUsuario" = TRASPASO."IdUsuario")
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" 				 AS EJECUTIVO 		ON  ( TRASPASO."IdEmpleadoEjecutivo" = EJECUTIVO."IdEmpleado")
LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PEREJEC 		ON  ( PEREJEC."IdPersona" = EJECUTIVO."IdPersona")
LEFT OUTER  JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal"  AS SUCURSAL 		ON  ( EJECUTIVO."IdSucursal" = SUCURSAL."IdSucursal")
INNER JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen" AS DETALLEORIGEN ON ( DETALLEORIGEN."IdTraspaso" = TRASPASO."IdTraspaso")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIEDEFONDO ON (SERIEDEFONDO."IdSerieFondo" = DETALLEORIGEN."IdSerieFondo")
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON (FONDO."IdFondo" = SERIEDEFONDO."IdFondo");

GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarTraspasoUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
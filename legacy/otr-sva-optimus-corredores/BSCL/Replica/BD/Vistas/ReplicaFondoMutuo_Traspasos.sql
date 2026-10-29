SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos" AS

	SELECT
		WST."IdStack" AS "IDSTACK",
        "TipoTraspaso" || ':' || TO_VARCHAR("IdTraspaso")  AS TABLE_KEY,
        "Action" AS ACTION,
		"TipoTraspaso"	AS	"TIPOTRASPASO",
		"IdTraspaso"	AS	"IDTRASPASO",
		"DocumentoIdentidad"	AS	"DOCUMENTOIDENTIDAD",
		"IdCliente"	AS	"IDCLIENTE",
		"IdPortafolio"	AS	"IDPORTAFOLIO",
		"IdTipoMovimientoTraspaso"	AS	"IDTIPOMOVIMIENTOTRASPASO",
		"FechaIngresoSolicitud"	AS	"FECHAINGRESOSOLICITUD",
		"IdSolicitudAporteRescate"	AS	"IDSOLICITUDAPORTERESCATE",
		"NombreEntidadRecaudadora"	AS	"NOMBREENTIDADRECAUDADORA",
		"DocumentoIdentidadEntidadRecaudadora"	AS	"DOCUMENTOIDENTIDADENTIDADRECAUDADORA",
		"FolioCSC"	AS	"FOLIOCSC"	

	FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTraspasosAnalitica"
    LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_Traspasos' AND WST."Key" = "TipoTraspaso" || ':' || TO_VARCHAR("IdTraspaso")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_Traspasos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

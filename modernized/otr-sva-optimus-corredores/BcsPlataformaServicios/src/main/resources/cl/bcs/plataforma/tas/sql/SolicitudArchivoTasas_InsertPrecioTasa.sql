INSERT INTO "BSCL.Plataforma.BD::TAS.PrecioTasa"(
	"IdPrecioTasa",
	"IdFuente",
	"IdTipoInstrumento",
	"IdInstrumento",
	"Nemotecnico",
	"IdTipoTasa",
	"Tasa",
	"TasaEstimada",
	"IdEstado",
	"IdEmpresa",
	"IdUsuarioCreador",
	"FechaSolicitud",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"RutaArchivo",
	"NombreArchivo",
	"TipoRegistro",
	"IdMoneda",
	"IdClasificacionTipoInstrumento"
)
SELECT
    "BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasa".NEXTVAL AS "IdPrecioTasa",
    :IdFuente AS "IdFuente",
    INS."IdTipoInstrumento",
    INS."IdInstrumento",
    INS."Nemotecnico",
    'TASA' AS "IdTipoTasa",
    0 AS "Tasa",
    0 AS "TasaEstimada",
    'CRA' AS "IdEstado",
    :p_IdEmpresa AS "IdEmpresa",
    :p_IdUsuario AS "IdUsuarioCreador",
    :FechaHoraUI AS "FechaSolicitud",
    :FechaSistema AS "FechaIngreso",
    CURRENT_UTCTIMESTAMP AS "FechaCreacion",
    CURRENT_UTCTIMESTAMP AS "FechaModificacion",
    :RutaArchivo AS "RutaArchivo",
    :NombreArchivo AS "NombreArchivo",
    'TASA' AS "TipoRegistro",
    INS."IdMoneda",
    INS."IdClasificacionTipoInstrumento"
FROM 
    #InstrumentosSelect INS
WHERE 
    (
        SELECT COUNT(*)
        FROM "BSCL.Plataforma.BD::TAS.PrecioTasa"
        WHERE "Nemotecnico" = INS."Nemotecnico" 
            AND "IdClasificacionTipoInstrumento" = INS."IdClasificacionTipoInstrumento" 
            AND "IdFuente" = :IdFuente 
            AND "FechaSolicitud" = :FechaHoraUI 
            AND "Borrado" = 'S'
    ) > 0
    AND (
        SELECT COUNT(*)
        FROM "BSCL.Plataforma.BD::TAS.PrecioTasa"
        WHERE "Nemotecnico" = INS."Nemotecnico" 
            AND "IdClasificacionTipoInstrumento" = INS."IdClasificacionTipoInstrumento" 
            AND "IdFuente" = :IdFuente 
            AND "FechaSolicitud" = :FechaHoraUI 
            AND "Borrado" = 'N'
    ) = 0
    OR (
        SELECT COUNT(*)
        FROM "BSCL.Plataforma.BD::TAS.PrecioTasa"
        WHERE "Nemotecnico" = INS."Nemotecnico" 
            AND "IdClasificacionTipoInstrumento" = INS."IdClasificacionTipoInstrumento" 
            AND "IdFuente" = :IdFuente 
            AND "FechaSolicitud" = :FechaHoraUI
    ) = 0
ORDER BY INS."Nemotecnico";


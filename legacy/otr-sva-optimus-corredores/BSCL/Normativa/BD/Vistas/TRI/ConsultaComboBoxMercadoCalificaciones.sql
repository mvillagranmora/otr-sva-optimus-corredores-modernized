SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultaComboBoxMercadoCalificaciones";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultaComboBoxMercadoCalificaciones" (
    "IdClasificacionTipoInstrumento",
    "Nombre"
) AS 
SELECT 
    'AC' AS "IdClasificacionTipoInstrumento",
    'ACCIONES' AS "Nombre"
FROM DUMMY
UNION
SELECT 
    'CF' AS "IdClasificacionTipoInstrumento",
    'C.F.I' AS "Nombre"
FROM DUMMY
UNION
SELECT 
    'FFM' AS "IdClasificacionTipoInstrumento",
    'FONDOS MUTUOS' AS "Nombre"
FROM DUMMY
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultaComboBoxMercadoCalificaciones" TO bcs_desarrollador WITH GRANT OPTION;
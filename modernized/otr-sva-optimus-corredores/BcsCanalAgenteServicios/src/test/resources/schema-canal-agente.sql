-- H2 schema for BcsCanalAgenteServicios integration tests.
-- Mirrors BSCL.Portafolio.BD::POR.Portafolio (HANA) with H2-compatible column names.

CREATE TABLE IF NOT EXISTS PORTAFOLIO_T (
    ID_PORTAFOLIO BIGINT NOT NULL PRIMARY KEY,
    ID_CLIENTE    BIGINT NOT NULL
);

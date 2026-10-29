-- Marcelo Yévenes
-- CELBUHOPRO-998
-- Script para insertar Servicios para consultar origen de calificaciones tributarias e identificar el ambiente actual(bolsa o corredor)


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55669998, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarOrigenCalificacionTributaria', 0, 'Q', '', '', '', 'S'
FROM DUMMY WHERE
NOT EXISTS(SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55669998 AND "RutaServicio" = 'BSCL.Normativa.BD.Vistas::TRI.ConsultarOrigenCalificacionTributaria');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55669999, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarAmbienteEsBolsa', 0, 'Q', '', '', '', 'S'
FROM DUMMY WHERE
NOT EXISTS(SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55669999 AND "RutaServicio" = 'BSCL.Normativa.BD.Vistas::TRI.ConsultarAmbienteEsBolsa');

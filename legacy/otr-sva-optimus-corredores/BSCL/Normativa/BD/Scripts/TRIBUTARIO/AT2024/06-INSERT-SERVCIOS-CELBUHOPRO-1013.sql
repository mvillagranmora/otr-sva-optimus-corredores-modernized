-- Marcelo Yévenes
-- CELBUHOPRO-1013
-- Script para insertar Servicios para consultar el periodo comercial de calificaciones tributarias


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 55670000, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarPeriodoTributarioCalificaciones', 0, 'Q', '', '', '', 'S'
FROM DUMMY WHERE
NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=55670000 AND "RutaServicio"='BSCL.Normativa.BD.Vistas::TRI.ConsultarPeriodoTributarioCalificaciones' AND "TipoServicio"='Q');
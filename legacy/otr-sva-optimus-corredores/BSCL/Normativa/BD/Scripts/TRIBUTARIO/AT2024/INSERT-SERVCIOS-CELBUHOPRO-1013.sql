-- Marcelo Yévenes
-- CELBUHOPRO-1013
-- Script para insertar Servicios para consultar el periodo comercial de calificaciones tributarias


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(55670000, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarPeriodoTributarioCalificaciones', 0, 'Q', '', '', '', 'S');
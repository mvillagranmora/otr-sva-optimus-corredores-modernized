-- Marcelo Rojas
-- CELBUHOPRO-909
-- Script para insertar Servicio GrabarDetalle DJ1944

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(600094, 'BSCL.Normativa.BD.Servicios::TRI.DJ1944.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600094);


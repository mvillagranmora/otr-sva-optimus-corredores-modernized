-- Marcelo Rojas
-- CELBUHOPRO-909
-- Script para insertar Servicio GrabarDetalle DJ1919

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3245, 'BSCL.Normativa.BD.Servicios::TRI.DJ1919.GrabarDetalle', 2, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3245);


SET SCHEMA "BSCL";

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 3616;
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 3617;
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 3618;
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 3619;
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 3620;
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 3621;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3619, 'BSCL.Normativa.BD.Servicios::TRI.DJ1874.ActualizarEstadoInstrumentoDJ1874', 0, 'P', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3620, 'BSCL.Normativa.BD.Servicios::TRI.DJ1874.ConsultarValorizacionDJ1874', 0, 'P', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3621, 'BSCL.Normativa.BD.Servicios::TRI.DJ1874.Procesar4porcientoDJ1874', 0, 'P', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3616, 'BSCL.Normativa.BD.Vistas::TRI.DJ1874.ConsultarNemos', 2, 'Q', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3617, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarClientesComplemento4Porciento', 2, 'Q', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3618, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarNemoComplemento4Porciento', 2, 'Q', '', '', '', 'S');

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE  "CodigoComplemento" = 'CALCULO4PORCIENTO' AND "Esquema"='DJ1874';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento"
("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1874', 'CALCULO4PORCIENTO', 'Retencion 4% Art. 104');




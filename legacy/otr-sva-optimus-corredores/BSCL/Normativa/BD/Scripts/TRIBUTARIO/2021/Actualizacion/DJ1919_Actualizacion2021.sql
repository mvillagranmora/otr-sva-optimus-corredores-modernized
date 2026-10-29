
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1919';

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ID','','',1,'Secuencia','Ayuda  de Secuencia','ID',0,'0','N','N','N','S','N','N','S','N','',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','Numero','C2','',2,'Numero','Ayuda de Numero','ID',0,'0','N','N','N','S','N','N','N','N','1. Datos de las Operaciones',1,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','RutEmisor','C3','',3,'Rut Emisor','Ayuda de Rut Emisor','RUT',0,'','N','S','N','S','N','N','N','S','1. Datos de las Operaciones',2,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','TaxID','C4','',4,'Tax ID Inversionista','Ayuda de Tax ID','RUT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',3,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','RutCliente','C5','',5,'Rut Cliente','Ayuda de Rut Cliente','RUT_CLIENTE',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',4,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','CodigoPaisConstitucion','C6','CodigoPais',6,'Codigo Pais Constitucion','Ayuda de Codigo Pais Constitucion','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',5,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','CodigoPaisResidencia','C7','CodigoPais',7,'Codigo Pais Residencia','Ayuda de Codigo Pais Residencia','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',6,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','PorcentajePropiedadDirectaIndirecta','C8','',8,'% de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','Ayuda de % de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','DECIMAL',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',7,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ParticipacionDirectaIndirectaControl','C9','',9,'Participación directa o indirecta en el control del Emisor','Ayuda de Participación directa o indirecta en el control del Emisor','QTY',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',8,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES	 ('DJ1919','2020','NemotecnicoSVS','C10','',10,'Nemotécnico SVS','Ayuda de Nemotécnico SVS','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',9,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','FechaOperacion','C11','',11,'Fecha Operación','Ayuda de Fecha Operación','DATE_FILTER',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',10,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','NumeroCuentaDCV','C12','',12,'N° Cuenta DCV','Ayuda de N° Cuenta DCV','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',11,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','Moneda','C13','',13,'Moneda o unidad de reajuste','Ayuda de moneda o unidad de reajuste','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',12,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorEmision','C14','',14,'Valor de emisión en moneda o unidad de reajuste','Ayuda de valor de emisión en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',13,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorColocacion','C15','',15,'Valor de colocación en moneda o unidad de reajuste','Ayuda de valor de colocación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',14,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorAdquisicion','C16','',16,'Valor de adquisición en moneda o unidad de reajuste','Ayuda de valor de adquisición en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',15,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorAdquisicionEnPesos','C17','',17,'Valor de adquisición en Pesos','Ayuda de valor de adquisición en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',16,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorEnajenacion','C18','',18,'Valor de enajenación','Ayuda de valor de enajenación','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',17,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorEnajenacionEnPesos','C19','',19,'Valor de enajenación en Pesos','Ayuda de valor de enajenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',18,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','MayorValorEnajenacion','C20','',20,'Mayor valor enagenación en moneda o unidad de reajuste','Ayuda de Mayor valor enagenación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',19,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','MayorValorEnajenacionEnPesos','C21','',21,'Mayor valor enagenación en Pesos','Ayuda de Mayor valor enagenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',20,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdDocumento','','',22,'Secuencia Docto','Ayuda de Secuencia Docto','ID',0,'0','N','N','N','S','S','S','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','VboUsuario','','',23,'VoBo','Ayuda de VoBo','TEXT',1,'N','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','Observaciones','','',24,'Observaciones','Ayuda de Observaciones','TEXT',250,'','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','FuenteDatos','','',25,'Fuente','Ayuda de Fuente','TEXT',0,'INICIAL','N','N','N','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdCliente','','',26,'Secuencia Cliente','Ayuda de Secuencia Cliente','ID_CLIENTE',0,'0','S','S','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdPortafolio','','',27,'Secuencia Portafolio','Ayuda de Secuencia Portafolio','ID_PORTAFOLIO',0,'0','S','N','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdOperacion','','',28,'Id Operacion','','ID',0,'0','N','S','N','S','S','S','N','S','1. Transacciones informadas',0,0);


INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ID','','',1,'Secuencia','Ayuda  de Secuencia','ID',0,'0','N','N','N','S','N','N','S','N','',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','Numero','C2','',2,'Numero','Ayuda de Numero','ID',0,'0','N','N','N','S','N','N','N','N','1. Datos de las Operaciones',1,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','RutEmisor','C3','',3,'Rut Emisor','Ayuda de Rut Emisor','RUT',0,'','N','S','N','S','N','N','N','S','1. Datos de las Operaciones',2,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','TaxID','C4','',4,'Tax ID Inversionista','Ayuda de Tax ID','RUT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',3,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','RutCliente','C5','',5,'Rut Cliente','Ayuda de Rut Cliente','RUT_CLIENTE',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',4,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','CodigoPaisConstitucion','C6','CodigoPais',6,'Codigo Pais Constitucion','Ayuda de Codigo Pais Constitucion','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',5,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','CodigoPaisResidencia','C7','CodigoPais',7,'Codigo Pais Residencia','Ayuda de Codigo Pais Residencia','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',6,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','PorcentajePropiedadDirectaIndirecta','C8','',8,'% de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','Ayuda de % de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','DECIMAL',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',7,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ParticipacionDirectaIndirectaControl','C9','',9,'Participación directa o indirecta en el control del Emisor','Ayuda de Participación directa o indirecta en el control del Emisor','QTY',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',8,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES	 ('DJ1919','2021','NemotecnicoSVS','C10','',10,'Nemotécnico SVS','Ayuda de Nemotécnico SVS','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',9,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','FechaOperacion','C11','',11,'Fecha Operación','Ayuda de Fecha Operación','DATE_FILTER',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',10,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','NumeroCuentaDCV','C12','',12,'N° Cuenta DCV','Ayuda de N° Cuenta DCV','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',11,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','Moneda','C13','',13,'Moneda o unidad de reajuste','Ayuda de moneda o unidad de reajuste','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',12,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorEmision','C14','',14,'Valor de emisión en moneda o unidad de reajuste','Ayuda de valor de emisión en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',13,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorColocacion','C15','',15,'Valor de colocación en moneda o unidad de reajuste','Ayuda de valor de colocación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',14,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorAdquisicion','C16','',16,'Valor de adquisición en moneda o unidad de reajuste','Ayuda de valor de adquisición en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',15,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorAdquisicionEnPesos','C17','',17,'Valor de adquisición en Pesos','Ayuda de valor de adquisición en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',16,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorEnajenacion','C18','',18,'Valor de enajenación','Ayuda de valor de enajenación','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',17,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorEnajenacionEnPesos','C19','',19,'Valor de enajenación en Pesos','Ayuda de valor de enajenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',18,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','MayorValorEnajenacion','C20','',20,'Mayor valor enagenación en moneda o unidad de reajuste','Ayuda de Mayor valor enagenación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',19,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','MayorValorEnajenacionEnPesos','C21','',21,'Mayor valor enagenación en Pesos','Ayuda de Mayor valor enagenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',20,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdDocumento','','',22,'Secuencia Docto','Ayuda de Secuencia Docto','ID',0,'0','N','N','N','S','S','S','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','VboUsuario','','',23,'VoBo','Ayuda de VoBo','TEXT',1,'N','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','Observaciones','','',24,'Observaciones','Ayuda de Observaciones','TEXT',250,'','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','FuenteDatos','','',25,'Fuente','Ayuda de Fuente','TEXT',0,'INICIAL','N','N','N','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdCliente','','',26,'Secuencia Cliente','Ayuda de Secuencia Cliente','ID_CLIENTE',0,'0','S','S','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdPortafolio','','',27,'Secuencia Portafolio','Ayuda de Secuencia Portafolio','ID_PORTAFOLIO',0,'0','S','N','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdOperacion','','',28,'Id Operacion','','ID',0,'0','N','S','N','S','S','S','N','S','1. Transacciones informadas',0,0); 



/* MEJORA CARGA EXTERNA CON EXCEL DE EXPORTACION CELBUHOPRO-416 */
/*  Felipe Sepúlveda */

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=1, "Titulo"='Secuencia', "Ayuda"='Ayuda  de Secuencia', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='ID';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C2', "TipoDiccionario"='', "Orden"=2, "Titulo"='Numero', "Ayuda"='Ayuda de Numero', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=1, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='Numero';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C3', "TipoDiccionario"='', "Orden"=3, "Titulo"='Rut Emisor', "Ayuda"='Ayuda de Rut Emisor', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=2, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='RutEmisor';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C4', "TipoDiccionario"='', "Orden"=4, "Titulo"='Tax ID Inversionista', "Ayuda"='Ayuda de Tax ID', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=3, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='TaxID';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C5', "TipoDiccionario"='', "Orden"=5, "Titulo"='Rut Cliente', "Ayuda"='Ayuda de Rut Cliente', "TipoDato"='RUT_CLIENTE', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=4, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='RutCliente';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C6', "TipoDiccionario"='CodigoPais', "Orden"=6, "Titulo"='Codigo Pais Constitucion', "Ayuda"='Ayuda de Codigo Pais Constitucion', "TipoDato"='NEMO', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=5, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='CodigoPaisConstitucion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C7', "TipoDiccionario"='CodigoPais', "Orden"=7, "Titulo"='Codigo Pais Residencia', "Ayuda"='Ayuda de Codigo Pais Residencia', "TipoDato"='NEMO', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=6, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='CodigoPaisResidencia';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C8', "TipoDiccionario"='', "Orden"=8, "Titulo"='% de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor', "Ayuda"='Ayuda de % de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor', "TipoDato"='DECIMAL', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=7, "CantidadDecimales"=2 WHERE "Esquema"='DJ1919' AND "Nombre"='PorcentajePropiedadDirectaIndirecta';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C9', "TipoDiccionario"='', "Orden"=9, "Titulo"='Participación directa o indirecta en el control del Emisor', "Ayuda"='Ayuda de Participación directa o indirecta en el control del Emisor', "TipoDato"='QTY', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=8, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='ParticipacionDirectaIndirectaControl';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C10', "TipoDiccionario"='', "Orden"=10, "Titulo"='Nemotécnico SVS', "Ayuda"='Ayuda de Nemotécnico SVS', "TipoDato"='NEMO', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=9, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='NemotecnicoSVS';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C11', "TipoDiccionario"='', "Orden"=11, "Titulo"='Fecha Operación', "Ayuda"='Ayuda de Fecha Operación', "TipoDato"='DATE_FILTER', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=10, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='FechaOperacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C12', "TipoDiccionario"='', "Orden"=12, "Titulo"='N° Cuenta DCV', "Ayuda"='Ayuda de N° Cuenta DCV', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=11, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='NumeroCuentaDCV';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C13', "TipoDiccionario"='CodigoMonedaDJ1919', "Orden"=13, "Titulo"='Moneda o unidad de reajuste', "Ayuda"='Ayuda de moneda o unidad de reajuste', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=12, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='Moneda';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C14', "TipoDiccionario"='', "Orden"=14, "Titulo"='Valor de emisión en moneda o unidad de reajuste', "Ayuda"='Ayuda de valor de emisión en moneda o unidad de reajuste', "TipoDato"='DECIMAL', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=13, "CantidadDecimales"=2 WHERE "Esquema"='DJ1919' AND "Nombre"='ValorEmision';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C15', "TipoDiccionario"='', "Orden"=15, "Titulo"='Valor de colocación en moneda o unidad de reajuste', "Ayuda"='Ayuda de valor de colocación en moneda o unidad de reajuste', "TipoDato"='DECIMAL', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=14, "CantidadDecimales"=2 WHERE "Esquema"='DJ1919' AND "Nombre"='ValorColocacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C16', "TipoDiccionario"='', "Orden"=16, "Titulo"='Valor de adquisición en moneda o unidad de reajuste', "Ayuda"='Ayuda de valor de adquisición en moneda o unidad de reajuste', "TipoDato"='DECIMAL', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=15, "CantidadDecimales"=2 WHERE "Esquema"='DJ1919' AND "Nombre"='ValorAdquisicion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C17', "TipoDiccionario"='', "Orden"=17, "Titulo"='Valor de adquisición en Pesos', "Ayuda"='Ayuda de valor de adquisición en Pesos', "TipoDato"='AMOUNT', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=16, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='ValorAdquisicionEnPesos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C18', "TipoDiccionario"='', "Orden"=18, "Titulo"='Valor de enajenación', "Ayuda"='Ayuda de valor de enajenación', "TipoDato"='DECIMAL', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=17, "CantidadDecimales"=2 WHERE "Esquema"='DJ1919' AND "Nombre"='ValorEnajenacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C19', "TipoDiccionario"='', "Orden"=19, "Titulo"='Valor de enajenación en Pesos', "Ayuda"='Ayuda de valor de enajenación en Pesos', "TipoDato"='AMOUNT', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=18, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='ValorEnajenacionEnPesos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C20', "TipoDiccionario"='', "Orden"=20, "Titulo"='Mayor valor enagenación en moneda o unidad de reajuste', "Ayuda"='Ayuda de Mayor valor enagenación en moneda o unidad de reajuste', "TipoDato"='DECIMAL', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=19, "CantidadDecimales"=2 WHERE "Esquema"='DJ1919' AND "Nombre"='MayorValorEnajenacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C21', "TipoDiccionario"='', "Orden"=21, "Titulo"='Mayor valor enagenación en Pesos', "Ayuda"='Ayuda de Mayor valor enagenación en Pesos', "TipoDato"='AMOUNT', "Largo"=15, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Datos de las Operaciones', "OrdenCargaExterna"=20, "CantidadDecimales"=2 WHERE "Esquema"='DJ1919' AND "Nombre"='MayorValorEnajenacionEnPesos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=22, "Titulo"='Secuencia Docto', "Ayuda"='Ayuda de Secuencia Docto', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='S', "Comun"='S', "Clave"='N', "Grupo"='5. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='IdDocumento';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=23, "Titulo"='VoBo', "Ayuda"='Ayuda de VoBo', "TipoDato"='TEXT', "Largo"=1, "ValorDefecto"='N', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='5. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='VboUsuario';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=24, "Titulo"='Observaciones', "Ayuda"='Ayuda de Observaciones', "TipoDato"='TEXT', "Largo"=250, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='5. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='Observaciones';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=25, "Titulo"='Fuente', "Ayuda"='Ayuda de Fuente', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='INICIAL', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='5. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='FuenteDatos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=26, "Titulo"='Secuencia Cliente', "Ayuda"='Ayuda de Secuencia Cliente', "TipoDato"='ID_CLIENTE', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='IdCliente';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=27, "Titulo"='Secuencia Portafolio', "Ayuda"='Ayuda de Secuencia Portafolio', "TipoDato"='ID_PORTAFOLIO', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='IdPortafolio';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=28, "Titulo"='Id Operacion', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='S', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1919' AND "Nombre"='IdOperacion';

/* MEJORA CARGA EXTERNA CON EXCEL DE EXPORTACION CELBUHOPRO-416 */
